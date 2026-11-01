package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.AppNotification
import com.example.data.model.AppNotificationType
import com.example.data.model.BarkasComment
import com.example.data.model.BarkasItem
import com.example.data.model.BookmarkItem
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.SurabayaBarkasSeed
import com.example.data.model.CitizenPoll
import com.example.data.model.CitizenService
import com.example.data.model.CulinaryReview
import com.example.data.model.CulinarySpot
import com.example.data.model.DestinationReview
import com.example.data.model.IncidentComment
import com.example.data.model.IncidentReport
import com.example.data.model.MomentComment
import com.example.data.model.NearbyProfile
import com.example.data.model.PollComment
import com.example.data.model.PollOption
import com.example.data.model.SurabayaDestination
import com.example.data.model.SurabayaEvent
import com.example.data.model.TambalBanComment
import com.example.data.model.TambalBanSpot
import com.example.data.model.UserAccount
import com.example.data.model.UserMoment
import com.example.data.model.POST_TYPE_MOMEN
import com.example.data.model.POST_TYPE_LENSA_WARGA
import com.example.data.model.normalizeCitizenGender
import com.example.data.pocketbase.MultipartFilePart
import com.example.data.pocketbase.PocketBaseClient
import com.example.data.pocketbase.PocketBaseException
import com.example.data.seed.SurabayaCuratedSeed
import com.example.data.util.LinkSecurityValidator
import com.example.data.util.LinkValidationResult
import com.example.data.util.OperationType
import com.example.data.util.PreparedPhoto
import com.example.data.util.SafeLinkCategory
import com.example.data.util.DuplicateContentException
import com.example.data.util.SpamCheckResult
import com.example.data.util.SurabayaDistricts
import com.example.data.util.SurabayaSpamProtector
import com.example.data.util.defaultCulinaryImageKey
import com.example.data.util.extractLatLngFromMapsUrl
import com.example.data.util.defaultEventImageKey
import com.example.data.util.defaultIncidentImageKey
import com.example.data.util.logBackendError
import com.example.data.util.parsePocketBaseTimestamp
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CopyOnWriteArraySet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

const val OFFICIAL_AREK_SURABAYA_UID = "arek_surabaya_official"
const val OFFICIAL_AREK_SURABAYA_NAME = "Arek Surabaya (Resmi)"
const val OFFICIAL_AREK_SURABAYA_DISTRICT = "Balaikota Surabaya"

class SurabayaRepository(
    private val pb: PocketBaseClient,
) {
    constructor(context: Context) : this(PocketBaseClient.getInstance(context))

    private data class LocalContentReportOverlay(
        val count: Int,
        val uids: List<String>,
        val forceOverride: Boolean = false,
    )

    private val localContentReports = ConcurrentHashMap<String, LocalContentReportOverlay>()
    private val localDeletedRecords = CopyOnWriteArraySet<String>()
    private val localBlockedUids = ConcurrentHashMap<String, List<String>>()
    private val localBannedUids = CopyOnWriteArraySet<String>()
    private val localGenderOverrides = ConcurrentHashMap<String, String>()
    private val localMutationTick = MutableStateFlow(0L)
    private val localReadMsgIds = ConcurrentHashMap.newKeySet<String>()
    private val localReadNotifIds = ConcurrentHashMap.newKeySet<String>()
    private val localTypingStatuses = ConcurrentHashMap<String, String>() // roomId -> "uid:name:timestamp"

    private fun escapePbValue(value: String): String = value.replace("'", "\\'")

    private val inMemoryMoments = ConcurrentHashMap<String, UserMoment>()
    private val inMemoryMomentComments = ConcurrentHashMap<String, CopyOnWriteArrayList<MomentComment>>()
    private val inMemoryDestinations = ConcurrentHashMap<String, SurabayaDestination>()
    private val inMemoryDestinationReviews = ConcurrentHashMap<String, CopyOnWriteArrayList<DestinationReview>>()
    private val inMemoryServices = ConcurrentHashMap<String, CitizenService>()
    private val inMemoryPolls = ConcurrentHashMap<String, CitizenPoll>()
    private val inMemoryPollComments = ConcurrentHashMap<String, CopyOnWriteArrayList<PollComment>>()
    private val inMemoryTambalBan = ConcurrentHashMap<String, TambalBanSpot>()
    private val inMemoryTambalBanComments = ConcurrentHashMap<String, CopyOnWriteArrayList<TambalBanComment>>()
    private val inMemoryBarkas = ConcurrentHashMap<String, BarkasItem>()
    private val inMemoryBarkasComments = ConcurrentHashMap<String, CopyOnWriteArrayList<BarkasComment>>()
    private val cachedIncidents = ConcurrentHashMap<String, IncidentReport>()
    private val cachedEvents = ConcurrentHashMap<String, SurabayaEvent>()
    private val cachedCulinary = ConcurrentHashMap<String, CulinarySpot>()
    private val inMemoryNotifications = ConcurrentHashMap<String, AppNotification>()
    private val localNotificationsTick = MutableStateFlow(0L)

    init {
        // Initialize rich starter moments
        for (item in SurabayaCuratedSeed.starterMoments("system_seed", "Warga Surabaya")) {
            val m = mapMoment(item)
            inMemoryMoments[m.id] = m
        }
        // Initialize rich starter destinations
        for (item in SurabayaCuratedSeed.starterDestinations("system_admin")) {
            val d = mapDestination(item)
            inMemoryDestinations[d.id] = d
        }
        // Initialize rich starter community services
        for (item in SurabayaCuratedSeed.starterServices("system_seed", "Warga Surabaya")) {
            val s = mapCitizenService(item)
            inMemoryServices[s.id] = s
        }
        // Initialize rich starter notifications
        val now = System.currentTimeMillis()
        val starterNotifs = listOf(
            AppNotification(
                id = "notif_chat_1",
                userId = "",
                type = AppNotificationType.CHAT,
                title = "Pesan Baru dari Cak Sugeng",
                message = "Rek, sido cangkrukan nang Balai Pemuda sore iki? Tak enteni yo!",
                targetId = "public_balai_pemuda",
                timestamp = now - 1000 * 60 * 15,
                isRead = false,
            ),
            AppNotification(
                id = "notif_event_1",
                userId = "",
                type = AppNotificationType.EVENT,
                title = "Event Baru: Festival Rujak Uleg 2026",
                message = "Pendaftaran warga & stand kuliner telah dibuka resmi di Kya-Kya Kembang Jepun.",
                targetId = "event_rujak_uleg_2026",
                timestamp = now - 1000 * 60 * 45,
                isRead = false,
            ),
            AppNotification(
                id = "notif_incident_1",
                userId = "",
                type = AppNotificationType.INCIDENT,
                title = "Info Lalu Lintas: Rekayasa Jl. Tunjungan",
                message = "Penyekatan arus lalu lintas situasional di sekitar Jl. Tunjungan karena parade budaya.",
                targetId = "inc_001",
                timestamp = now - 1000 * 60 * 120,
                isRead = false,
            ),
            AppNotification(
                id = "notif_chat_2",
                userId = "",
                type = AppNotificationType.CHAT,
                title = "Ning Maya di Ruang Warga",
                message = "Rekomendasi rawon kalkun di daerah Tambaksari mantap tenan, lur!",
                targetId = "room_public_tunjungan",
                timestamp = now - 1000 * 60 * 240,
                isRead = true,
            ),
        )
        for (notif in starterNotifs) {
            inMemoryNotifications[notif.id] = notif
        }
        // Initialize rich starter citizen polls & comments
        for (poll in SurabayaCuratedSeed.starterPolls("system_seed")) {
            inMemoryPolls[poll.id] = poll
        }
        for ((pollId, comments) in SurabayaCuratedSeed.starterPollComments()) {
            inMemoryPollComments[pollId] = CopyOnWriteArrayList(comments)
        }
        // Initialize rich starter tambal ban spots & comments (Salam Satu Aspal)
        for (spot in SurabayaCuratedSeed.starterTambalBanSpots("system_seed")) {
            inMemoryTambalBan[spot.id] = spot
        }
        for ((spotId, comments) in SurabayaCuratedSeed.starterTambalBanComments()) {
            inMemoryTambalBanComments[spotId] = CopyOnWriteArrayList(comments)
        }
        // Initialize rich starter Barkas Warga (Jual Beli Barang Bekas) & comments
        for (barkas in SurabayaBarkasSeed.starterItems("system_seed")) {
            inMemoryBarkas[barkas.id] = barkas
        }
        for ((barkasId, comments) in SurabayaBarkasSeed.starterComments()) {
            inMemoryBarkasComments[barkasId] = CopyOnWriteArrayList(comments)
        }
    }

    private fun bumpMutationTick() {
        localMutationTick.value = localMutationTick.value + 1L
    }

    private fun requireUserId(): String {
        return pb.currentSession?.userId?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("Pengguna wajib masuk ke akun Warga Surabaya terlebih dahulu.")
    }

    // --- 1. USER PROFILE, ADMIN ROLE & INITIAL SEEDING ---

    fun observeUserAccount(userId: String): Flow<UserAccount?> {
        return combine(
            pb.observeCollection("users", pollIntervalMs = 8_000L) {
                pb.getOneOrNull("users", userId)
            },
            localMutationTick,
        ) { json, _ ->
            json?.let(::mapUserAccount)
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.GET, "users/$userId")
            throw e
        }
    }

    fun observeAdminRole(userId: String): Flow<Boolean> {
        return pb.observeCollection("users", pollIntervalMs = 8_000L) {
            val json = pb.getOneOrNull("users", userId)
            val email = json?.optString("email").orEmpty().ifBlank { pb.currentSession?.email.orEmpty() }
            val isBootstrapped = isOfficialAdminEmail(email)
            val isFlagged = json?.optBoolean("is_admin", false) ?: (pb.currentSession?.isAdmin == true)
            isBootstrapped || isFlagged
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.GET, "users/$userId")
            throw e
        }
    }

    suspend fun bootstrapCurrentUserAndSeedIfEmpty(
        displayNameFallback: String,
        lat: Double = SurabayaDistricts.DEFAULT_LAT,
        lng: Double = SurabayaDistricts.DEFAULT_LNG,
        district: String = "Genteng",
    ) {
        val uid = requireUserId()
        val session = pb.currentSession
        val userName = (session?.displayName?.takeIf { it.isNotBlank() } ?: displayNameFallback).take(80)
        val isBootstrappedAdmin = isOfficialAdminEmail(session?.email)

        try {
            val existingUser = pb.getOneOrNull("users", uid)
            if (existingUser != null) {
                val curName = existingUser.optString("name")
                val curDistrict = existingUser.optString("district")
                val curAdmin = existingUser.optBoolean("is_admin", false)
                if (curName.isBlank() || curDistrict.isBlank() || (isBootstrappedAdmin && !curAdmin)) {
                    val patch = JSONObject().apply {
                        if (curName.isBlank()) put("name", userName)
                        if (curDistrict.isBlank()) put("district", district)
                        if (existingUser.optString("bio").isBlank()) put("bio", "Warga aktif Arek Surabaya")
                        if (existingUser.optDouble("search_radius_km", 0.0) <= 0.0) put("search_radius_km", 10.0)
                        if (!existingUser.has("is_discoverable")) put("is_discoverable", true)
                        if (isBootstrappedAdmin && !curAdmin) put("is_admin", true)
                    }
                    pb.updateRecord("users", uid, patch)
                }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.w(TAG, "User profile bootstrap warning: ${e.message}")
        }

        // Ensure current citizen has their own NearbyProfile record with id == uid
        try {
            val existingNearby = pb.getOneOrNull("nearby_profiles", uid)
            val sessionGender = normalizeCitizenGender(session?.gender.orEmpty())
            if (sessionGender.isNotBlank()) {
                localGenderOverrides[uid] = sessionGender
            }
            if (existingNearby == null) {
                val colorPalette = listOf("#006B5E", "#00798C", "#C86D1F", "#2B6CB0", "#0F766E")
                val pickedColor = colorPalette[(uid.hashCode() and Int.MAX_VALUE) % colorPalette.size]
                val payload = JSONObject().apply {
                    put("id", uid)
                    put("user_id", uid)
                    put("display_name", userName)
                    if (sessionGender.isNotBlank()) put("gender", sessionGender)
                    put("avatar_color_hex", pickedColor)
                    put("bio", "Halo rek! Salam kenal dari $district.")
                    put("district", district)
                    put("interests", "Kuliner SBY, Kopi, Event Kota")
                    put("lat", lat)
                    put("lng", lng)
                    put("is_discoverable", true)
                }
                createOrIgnoreExisting("nearby_profiles", payload)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.w(TAG, "Nearby profile bootstrap warning: ${e.message}")
        }

        val canSeedApproved = isBootstrappedAdmin || (pb.currentSession?.isAdmin == true)
        seedCommunityCollectionsIfEmpty(uid, userName, canSeedApproved)
    }

    private suspend fun seedCommunityCollectionsIfEmpty(
        uid: String,
        userName: String,
        canSeedApprovedEvents: Boolean,
    ) {
        try {
            val incidents = pb.getList("incidents", perPage = 1)
            if (incidents.isEmpty()) {
                for (item in SurabayaCuratedSeed.starterIncidents(uid, userName)) {
                    createOrIgnoreExisting("incidents", item)
                }
            }

            val culinary = pb.getList("culinary", perPage = 1)
            if (culinary.isEmpty()) {
                for (item in SurabayaCuratedSeed.starterCulinary(uid)) {
                    createOrIgnoreExisting("culinary", item)
                }
            }

            val pendingEvents = pb.getList("events", filter = "status = 'PENDING'", perPage = 1)
            if (pendingEvents.isEmpty()) {
                for (item in SurabayaCuratedSeed.starterPendingEvents(uid)) {
                    createOrIgnoreExisting("events", item)
                }
            }

            if (canSeedApprovedEvents) {
                seedApprovedEventsIfEmpty(uid)
                val publicRooms = pb.getList("chat_rooms", filter = "room_type = 'PUBLIC'", perPage = 1)
                if (publicRooms.isEmpty()) {
                    for (item in SurabayaCuratedSeed.starterPublicRooms(uid)) {
                        createOrIgnoreExisting("chat_rooms", item)
                    }
                }
            }

            val peers = pb.getList("nearby_profiles", filter = "is_discoverable = true", perPage = 3)
            if (peers.size <= 1) {
                for (item in SurabayaCuratedSeed.starterCommunityPeers(uid)) {
                    createOrIgnoreExisting("nearby_profiles", item)
                }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.w(TAG, "Seed check warning: ${e.message}")
        }
    }

    private suspend fun seedApprovedEventsIfEmpty(uid: String) {
        try {
            val approved = pb.getList("events", filter = "status = 'APPROVED'", perPage = 1)
            if (approved.isEmpty()) {
                for (item in SurabayaCuratedSeed.starterApprovedEvents(uid)) {
                    createOrIgnoreExisting("events", item)
                }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.w(TAG, "Approved event seed warning: ${e.message}")
        }
    }

    private suspend fun createOrIgnoreExisting(collection: String, payload: JSONObject) {
        try {
            pb.createRecord(collection, payload)
        } catch (e: PocketBaseException) {
            // Ignore duplicate ID race if two devices open simultaneously
            if (e.statusCode != 400) throw e
        }
    }

    suspend fun setAdminMode(enabled: Boolean) {
        val uid = requireUserId()
        try {
            val patch = JSONObject().apply {
                put("is_admin", enabled)
            }
            pb.updateRecord("users", uid, patch)
            if (enabled) {
                seedApprovedEventsIfEmpty(uid)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.UPDATE, "users/$uid")
            throw e
        }
    }

    suspend fun updateProfileAndPrivacy(
        displayName: String,
        district: String,
        bio: String,
        interests: String,
        searchRadiusKm: Double,
        isDiscoverable: Boolean,
        lat: Double,
        lng: Double,
        gender: String = "",
        avatarPhoto: PreparedPhoto? = null,
        onPhotoSchemaMissing: (() -> Unit)? = null,
    ) {
        val uid = requireUserId()
        val cleanName = displayName.trim().take(80).ifEmpty { "Warga Surabaya" }
        val cleanDistrict = district.trim().take(60).ifEmpty { "Genteng" }
        val cleanGender = normalizeCitizenGender(gender)
        val cleanBio = bio.trim().take(240)
        val cleanInterests = interests.trim().take(200)
        val clampedRadius = searchRadiusKm.coerceIn(1.0, 50.0)
        var userAvatarSaved = false
        var nearbyAvatarSaved = false
        localGenderOverrides[uid] = cleanGender

        try {
            val userPatch = JSONObject().apply {
                put("name", cleanName)
                put("district", cleanDistrict)
                put("gender", cleanGender)
                put("bio", cleanBio)
                put("search_radius_km", clampedRadius)
                put("is_discoverable", isDiscoverable)
            }
            if (avatarPhoto != null) {
                val userFields = mapOf(
                    "name" to cleanName,
                    "district" to cleanDistrict,
                    "gender" to cleanGender,
                    "bio" to cleanBio,
                    "search_radius_km" to clampedRadius.toString(),
                    "is_discoverable" to isDiscoverable.toString(),
                )
                val updatedUser = try {
                    pb.updateRecordMultipart(
                        collection = "users",
                        recordId = uid,
                        fields = userFields,
                        filePart = MultipartFilePart("avatar", avatarPhoto.fileName, avatarPhoto.mimeType, avatarPhoto.bytes),
                    )
                } catch (e: PocketBaseException) {
                    if (e.statusCode == 400) {
                        pb.updateRecord("users", uid, userPatch)
                    } else {
                        throw e
                    }
                }
                userAvatarSaved = updatedUser.optString("avatar").isNotBlank()
            } else {
                pb.updateRecord("users", uid, userPatch)
            }

            val safeUid = escapePbValue(uid)
            val existingNearby = pb.getOneOrNull("nearby_profiles", uid)
                ?: pb.getList("nearby_profiles", filter = "user_id = '$safeUid'", perPage = 1).firstOrNull()
            val nearbyRecordId = existingNearby?.optString("id")?.takeIf { it.isNotBlank() } ?: uid
            localGenderOverrides[nearbyRecordId] = cleanGender

            if (existingNearby != null) {
                val nearbyPatch = JSONObject().apply {
                    put("display_name", cleanName)
                    put("gender", cleanGender)
                    put("bio", cleanBio)
                    put("district", cleanDistrict)
                    put("interests", cleanInterests)
                    put("lat", lat)
                    put("lng", lng)
                    put("is_discoverable", isDiscoverable)
                }
                if (avatarPhoto != null) {
                    val nearbyFields = mapOf(
                        "display_name" to cleanName,
                        "gender" to cleanGender,
                        "bio" to cleanBio,
                        "district" to cleanDistrict,
                        "interests" to cleanInterests,
                        "lat" to lat.toString(),
                        "lng" to lng.toString(),
                        "is_discoverable" to isDiscoverable.toString(),
                    )
                    val updatedNearby = try {
                        pb.updateRecordMultipart(
                            collection = "nearby_profiles",
                            recordId = nearbyRecordId,
                            fields = nearbyFields,
                            filePart = MultipartFilePart("avatar", avatarPhoto.fileName, avatarPhoto.mimeType, avatarPhoto.bytes),
                        )
                    } catch (e: PocketBaseException) {
                        if (e.statusCode == 400) {
                            pb.updateRecord("nearby_profiles", nearbyRecordId, nearbyPatch)
                        } else {
                            throw e
                        }
                    }
                    nearbyAvatarSaved = updatedNearby.optString("avatar").isNotBlank()
                } else {
                    pb.updateRecord("nearby_profiles", nearbyRecordId, nearbyPatch)
                }
            } else {
                val createPayload = JSONObject().apply {
                    put("id", uid)
                    put("user_id", uid)
                    put("display_name", cleanName)
                    put("gender", cleanGender)
                    put("avatar_color_hex", "#006B5E")
                    put("bio", cleanBio)
                    put("district", cleanDistrict)
                    put("interests", cleanInterests)
                    put("lat", lat)
                    put("lng", lng)
                    put("is_discoverable", isDiscoverable)
                }
                if (avatarPhoto != null) {
                    val createFields = mapOf(
                        "id" to uid,
                        "user_id" to uid,
                        "display_name" to cleanName,
                        "gender" to cleanGender,
                        "avatar_color_hex" to "#006B5E",
                        "bio" to cleanBio,
                        "district" to cleanDistrict,
                        "interests" to cleanInterests,
                        "lat" to lat.toString(),
                        "lng" to lng.toString(),
                        "is_discoverable" to isDiscoverable.toString(),
                    )
                    val createdNearby = try {
                        pb.createRecordMultipart(
                            collection = "nearby_profiles",
                            fields = createFields,
                            filePart = MultipartFilePart("avatar", avatarPhoto.fileName, avatarPhoto.mimeType, avatarPhoto.bytes),
                        )
                    } catch (e: PocketBaseException) {
                        if (e.statusCode == 400) {
                            pb.createRecord("nearby_profiles", createPayload)
                        } else {
                            throw e
                        }
                    }
                    nearbyAvatarSaved = createdNearby.optString("avatar").isNotBlank()
                } else {
                    pb.createRecord("nearby_profiles", createPayload)
                }
            }

            if (avatarPhoto != null && !userAvatarSaved && !nearbyAvatarSaved) {
                onPhotoSchemaMissing?.invoke()
            }
            bumpMutationTick()
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.UPDATE, "users/$uid")
            throw e
        }
    }

    // --- 2. BOOKMARKS ---

    fun observeBookmarks(userId: String): Flow<List<BookmarkItem>> {
        val safeUid = escapePbValue(userId)
        return pb.observeCollection("bookmarks", pollIntervalMs = 8_000L) {
            pb.getList(
                collection = "bookmarks",
                filter = "user_id = '$safeUid'",
                sort = "-created",
            ).map(::mapBookmark)
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.LIST, "bookmarks")
            throw e
        }
    }

    suspend fun toggleBookmark(
        targetId: String,
        targetType: String,
        title: String,
        subtitle: String,
        district: String,
    ): Boolean {
        val uid = requireUserId()
        val bookmarkRecordId = PocketBaseClient.deterministicPbId("bm_${uid}_${targetType.lowercase()}_$targetId")
        return try {
            val existing = pb.getOneOrNull("bookmarks", bookmarkRecordId)
            if (existing != null) {
                pb.deleteRecord("bookmarks", bookmarkRecordId)
                false
            } else {
                val payload = JSONObject().apply {
                    put("id", bookmarkRecordId)
                    put("user_id", uid)
                    put("target_id", targetId)
                    put("target_type", targetType)
                    put("title", title.trim().take(140).ifEmpty { "Item Surabaya" })
                    put("subtitle", subtitle.trim().take(200))
                    put("district", district.trim().take(60).ifEmpty { "Surabaya" })
                }
                pb.createRecord("bookmarks", payload)
                true
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.WRITE, "bookmarks/$bookmarkRecordId")
            throw e
        }
    }

    // --- 2.1 PEMBERITAHUAN WARGA (NOTIFICATIONS) ---

    private fun mapNotification(j: JSONObject): AppNotification {
        val id = j.optString("id")
        val rawType = j.optString("notif_type", "SYSTEM").uppercase()
        val parsedType = runCatching { AppNotificationType.valueOf(rawType) }
            .getOrDefault(AppNotificationType.SYSTEM)
        val isRead = j.optBoolean("is_read", false) || id in localReadNotifIds
        return AppNotification(
            id = id,
            userId = j.optString("user_id"),
            type = parsedType,
            title = j.optString("title"),
            message = j.optString("message"),
            targetId = j.optString("target_id"),
            timestamp = parsePocketBaseTimestamp(j.optString("created")) ?: System.currentTimeMillis(),
            isRead = isRead,
        )
    }

    fun observeNotifications(userId: String): Flow<List<AppNotification>> {
        val safeUid = escapePbValue(userId)
        return combine(
            pb.observeCollection("notifications", pollIntervalMs = 8_000L) {
                try {
                    val filter = if (safeUid.isNotBlank()) {
                        "user_id = '' || user_id = '$safeUid'"
                    } else {
                        "user_id = ''"
                    }
                    pb.getList(collection = "notifications", filter = filter, sort = "-created")
                } catch (_: Exception) {
                    emptyList()
                }
            },
            localMutationTick,
            localNotificationsTick,
        ) { remoteList, _, _ ->
            val combinedMap = inMemoryNotifications.toMutableMap()
            for (item in remoteList) {
                val mapped = mapNotification(item)
                combinedMap[mapped.id] = mapped
            }
            combinedMap.values
                .filter { it.userId.isBlank() || it.userId == userId }
                .map { if (it.id in localReadNotifIds) it.copy(isRead = true) else it }
                .sortedByDescending { it.timestamp }
        }.catch {
            emit(
                inMemoryNotifications.values
                    .filter { it.userId.isBlank() || it.userId == userId }
                    .sortedByDescending { it.timestamp },
            )
        }
    }

    fun markNotificationAsRead(notificationId: String) {
        localReadNotifIds.add(notificationId)
        val existing = inMemoryNotifications[notificationId]
        if (existing != null && !existing.isRead) {
            inMemoryNotifications[notificationId] = existing.copy(isRead = true)
        }
        localNotificationsTick.value = localNotificationsTick.value + 1L
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                pb.updateRecord("notifications", notificationId, JSONObject().apply {
                    put("is_read", true)
                })
            }
        }
    }

    fun markAllNotificationsAsRead(userId: String) {
        val markedIds = mutableListOf<String>()
        for ((key, notif) in inMemoryNotifications) {
            if ((notif.userId.isBlank() || notif.userId == userId) && !notif.isRead) {
                inMemoryNotifications[key] = notif.copy(isRead = true)
                localReadNotifIds.add(key)
                markedIds.add(key)
            }
        }
        localNotificationsTick.value = localNotificationsTick.value + 1L
        if (markedIds.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                for (id in markedIds) {
                    runCatching {
                        pb.updateRecord("notifications", id, JSONObject().apply {
                            put("is_read", true)
                        })
                    }
                }
            }
        }
    }

    fun addNotification(notification: AppNotification) {
        val notifId = notification.id.takeIf { it.length == 15 }
            ?: PocketBaseClient.deterministicPbId("ntf_${notification.id.ifBlank { UUID.randomUUID().toString() }}")
        inMemoryNotifications[notifId] = notification.copy(id = notifId)
        localNotificationsTick.value = localNotificationsTick.value + 1L
        CoroutineScope(Dispatchers.IO).launch {
            runCatching {
                val payload = JSONObject().apply {
                    put("id", notifId)
                    put("user_id", notification.userId.take(64))
                    put("notif_type", notification.type.name)
                    put("title", notification.title.trim().take(160).ifBlank { "Info Surabaya" })
                    put("message", notification.message.trim().take(800).ifBlank { "Pembaruan informasi warga." })
                    put("target_id", notification.targetId.take(64))
                    put("is_read", notification.isRead)
                }
                pb.createRecord("notifications", payload)
            }
        }
    }

    // --- 3. KEJADIAN SURABAYA (INCIDENTS) ---

    fun observeIncidents(): Flow<List<IncidentReport>> {
        return combine(
            pb.observeCollection("incidents", pollIntervalMs = 6_000L) {
                pb.getList(
                    collection = "incidents",
                    sort = "-created",
                )
            },
            localMutationTick,
        ) { list, _ ->
            val mapped = list.map(::mapIncident)
                .filter { "incidents:${it.id}" !in localDeletedRecords && it.authorId !in localBannedUids }
            mapped.forEach { cachedIncidents[it.id] = it }
            mapped
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.LIST, "incidents")
            throw e
        }
    }

    suspend fun createIncident(
        authorName: String,
        title: String,
        description: String,
        category: String,
        severity: String,
        district: String,
        address: String,
        mapsUrl: String = "",
        imageKey: String = defaultIncidentImageKey(category),
        photo: PreparedPhoto? = null,
        photos: List<PreparedPhoto> = emptyList(),
        onPhotoSchemaMissing: (() -> Unit)? = null,
        onMultiPhotoFallbackUsed: (() -> Unit)? = null,
    ): String {
        val uid = requireUserId()
        val spamCheck = SurabayaSpamProtector.checkIncidentDuplicate(
            userId = uid,
            title = title,
            address = address,
            description = description,
            existingIncidents = cachedIncidents.values.toList(),
        )
        if (spamCheck is SpamCheckResult.Blocked) {
            throw DuplicateContentException(spamCheck.reason)
        }

        val recordId = PocketBaseClient.deterministicPbId("inc_${UUID.randomUUID()}")
        val (lat, lng) = SurabayaDistricts.coordinateFor(district)
        val cleanAuthor = authorName.trim().take(80).ifEmpty { "Warga Surabaya" }
        val cleanTitle = title.trim().take(140)
        val cleanDesc = description.trim().take(1200)
        val cleanDistrict = district.trim().take(60)
        val cleanAddress = address.trim().take(180)
        val cleanMapsUrl = requireSafeOptionalUrl(mapsUrl, SafeLinkCategory.GOOGLE_MAPS)
        val boundedPhotos = when {
            photos.isNotEmpty() -> photos.take(4)
            photo != null -> listOf(photo)
            else -> emptyList()
        }

        val payload = JSONObject().apply {
            put("id", recordId)
            put("author_id", uid)
            put("author_name", cleanAuthor)
            put("title", cleanTitle)
            put("description", cleanDesc)
            put("category", category)
            put("severity", severity)
            put("district", cleanDistrict)
            put("address", cleanAddress)
            put("lat", lat)
            put("lng", lng)
            put("image_key", imageKey)
            put("maps_url", cleanMapsUrl)
            put("verified_count", 1)
            put("verified_by_uids", uid)
            put("comment_count", 0)
        }
        return try {
            val created = if (boundedPhotos.isNotEmpty()) {
                val fields = mapOf(
                    "id" to recordId,
                    "author_id" to uid,
                    "author_name" to cleanAuthor,
                    "title" to cleanTitle,
                    "description" to cleanDesc,
                    "category" to category,
                    "severity" to severity,
                    "district" to cleanDistrict,
                    "address" to cleanAddress,
                    "lat" to lat.toString(),
                    "lng" to lng.toString(),
                    "image_key" to imageKey,
                    "maps_url" to cleanMapsUrl,
                    "verified_count" to "1",
                    "verified_by_uids" to uid,
                    "comment_count" to "0",
                )
                val allFileParts = boundedPhotos.mapIndexed { idx, p ->
                    MultipartFilePart(
                        fieldName = "photo",
                        fileName = if (boundedPhotos.size > 1) "${idx + 1}_${p.fileName}" else p.fileName,
                        mimeType = p.mimeType,
                        bytes = p.bytes,
                    )
                }
                val resp = try {
                    pb.createRecordMultipartFiles(
                        collection = "incidents",
                        fields = fields,
                        fileParts = allFileParts,
                    )
                } catch (e: PocketBaseException) {
                    if (e.statusCode == 400 && allFileParts.size > 1) {
                        try {
                            val singleResp = pb.createRecordMultipartFiles(
                                collection = "incidents",
                                fields = fields,
                                fileParts = listOf(allFileParts.first()),
                            )
                            onMultiPhotoFallbackUsed?.invoke()
                            singleResp
                        } catch (fallbackErr: PocketBaseException) {
                            if (fallbackErr.statusCode == 400) {
                                onPhotoSchemaMissing?.invoke()
                                return pb.createRecord("incidents", payload).optString("id").ifBlank { recordId }
                            } else {
                                throw fallbackErr
                            }
                        }
                    } else if (e.statusCode == 400) {
                        onPhotoSchemaMissing?.invoke()
                        val fallbackId = pb.createRecord("incidents", payload).optString("id").ifBlank { recordId }
                        SurabayaSpamProtector.recordSubmission(uid, "incidents")
                        cachedIncidents[fallbackId] = IncidentReport(
                            id = fallbackId,
                            authorId = uid,
                            authorName = cleanAuthor,
                            title = cleanTitle,
                            description = cleanDesc,
                            category = category,
                            severity = severity,
                            district = cleanDistrict,
                            address = cleanAddress,
                            mapsUrl = cleanMapsUrl,
                            createdAt = System.currentTimeMillis(),
                        )
                        return fallbackId
                    } else {
                        throw e
                    }
                }
                if (extractFileNames(resp, "photo").isEmpty()) {
                    onPhotoSchemaMissing?.invoke()
                }
                resp
            } else {
                pb.createRecord("incidents", payload)
            }
            val finalId = created.optString("id").ifBlank { recordId }
            SurabayaSpamProtector.recordSubmission(uid, "incidents")
            cachedIncidents[finalId] = IncidentReport(
                id = finalId,
                authorId = uid,
                authorName = cleanAuthor,
                title = cleanTitle,
                description = cleanDesc,
                category = category,
                severity = severity,
                district = cleanDistrict,
                address = cleanAddress,
                mapsUrl = cleanMapsUrl,
                createdAt = System.currentTimeMillis(),
            )
            finalId
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.CREATE, "incidents/$recordId")
            throw e
        }
    }

    suspend fun toggleVerifyIncident(incident: IncidentReport) {
        val uid = requireUserId()
        val currentList = incident.verifiedByUids.toMutableList()
        val alreadyVerified = uid in currentList
        if (alreadyVerified) {
            currentList.remove(uid)
        } else if (currentList.size < 80) {
            currentList.add(uid)
        }
        val newCount = if (alreadyVerified) {
            (incident.verifiedCount - 1).coerceAtLeast(0)
        } else {
            incident.verifiedCount + 1
        }
        try {
            val patch = JSONObject().apply {
                put("verified_count", newCount)
                put("verified_by_uids", encodeUidList(currentList))
            }
            pb.updateRecord("incidents", incident.id, patch)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.UPDATE, "incidents/${incident.id}")
            throw e
        }
    }

    fun observeIncidentComments(incidentId: String): Flow<List<IncidentComment>> {
        val safeIncidentId = escapePbValue(incidentId)
        return pb.observeCollection("incident_comments", pollIntervalMs = 5_000L) {
            pb.getList(
                collection = "incident_comments",
                filter = "incident_id = '$safeIncidentId'",
                sort = "+created",
            ).map(::mapIncidentComment)
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.LIST, "incident_comments")
            throw e
        }
    }

    suspend fun addIncidentComment(
        incident: IncidentReport,
        authorName: String,
        text: String,
    ) {
        val uid = requireUserId()
        val commentId = PocketBaseClient.deterministicPbId("cmt_${UUID.randomUUID()}")
        try {
            val payload = JSONObject().apply {
                put("id", commentId)
                put("incident_id", incident.id)
                put("author_id", uid)
                put("author_name", authorName.trim().take(80).ifEmpty { "Warga Surabaya" })
                put("text", text.trim().take(500))
            }
            pb.createRecord("incident_comments", payload)
            val patch = JSONObject().apply {
                put("comment_count", incident.commentCount + 1)
            }
            pb.updateRecord("incidents", incident.id, patch)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.CREATE, "incident_comments/$commentId")
            throw e
        }
    }

    // --- 4. EVENT SURABAYA & ADMIN MODERATION ---

    fun observeApprovedEvents(): Flow<List<SurabayaEvent>> {
        return combine(
            pb.observeCollection("events", pollIntervalMs = 6_000L) {
                pb.getList(
                    collection = "events",
                    filter = "status = 'APPROVED'",
                    sort = "-created",
                )
            },
            localMutationTick,
        ) { list, _ ->
            val mapped = list.map(::mapEvent)
                .filter { "events:${it.id}" !in localDeletedRecords && it.proposerId !in localBannedUids }
            mapped.forEach { cachedEvents[it.id] = it }
            mapped
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.LIST, "events")
            throw e
        }
    }

    fun observePendingEvents(): Flow<List<SurabayaEvent>> {
        return combine(
            pb.observeCollection("events", pollIntervalMs = 6_000L) {
                pb.getList(
                    collection = "events",
                    filter = "status = 'PENDING'",
                    sort = "-created",
                )
            },
            localMutationTick,
        ) { list, _ ->
            val mapped = list.map(::mapEvent)
                .filter { "events:${it.id}" !in localDeletedRecords && it.proposerId !in localBannedUids }
            mapped.forEach { cachedEvents[it.id] = it }
            mapped
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.LIST, "events")
            throw e
        }
    }

    fun observeMyProposedEvents(userId: String): Flow<List<SurabayaEvent>> {
        val safeUid = escapePbValue(userId)
        return combine(
            pb.observeCollection("events", pollIntervalMs = 6_000L) {
                pb.getList(
                    collection = "events",
                    filter = "proposer_id = '$safeUid'",
                    sort = "-created",
                )
            },
            localMutationTick,
        ) { list, _ ->
            val mapped = list.map(::mapEvent)
                .filter { "events:${it.id}" !in localDeletedRecords }
            mapped.forEach { cachedEvents[it.id] = it }
            mapped
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.LIST, "events")
            throw e
        }
    }

    suspend fun proposeEvent(
        proposerName: String,
        title: String,
        category: String,
        eventDateText: String,
        timeText: String,
        venueName: String,
        district: String,
        address: String,
        priceLabel: String,
        description: String,
        mapsUrl: String = "",
        imageKey: String = defaultEventImageKey(category),
        photo: PreparedPhoto? = null,
        photos: List<PreparedPhoto> = emptyList(),
        onPhotoSchemaMissing: (() -> Unit)? = null,
        onMultiPhotoFallbackUsed: (() -> Unit)? = null,
    ): String {
        val uid = requireUserId()
        val spamCheck = SurabayaSpamProtector.checkEventProposalDuplicate(
            userId = uid,
            title = title,
            venueName = venueName,
            eventDateText = eventDateText,
            existingEvents = cachedEvents.values.toList(),
        )
        if (spamCheck is SpamCheckResult.Blocked) {
            throw DuplicateContentException(spamCheck.reason)
        }

        val recordId = PocketBaseClient.deterministicPbId("ev_${UUID.randomUUID()}")
        val (lat, lng) = SurabayaDistricts.coordinateFor(district)
        val cleanProposer = proposerName.trim().take(80).ifEmpty { "Warga Surabaya" }
        val cleanTitle = title.trim().take(140)
        val cleanDate = eventDateText.trim().take(60)
        val cleanTime = timeText.trim().take(50)
        val cleanVenue = venueName.trim().take(100)
        val cleanDistrict = district.trim().take(60)
        val cleanAddress = address.trim().take(180)
        val cleanPrice = priceLabel.trim().take(50).ifEmpty { "Gratis" }
        val cleanDesc = description.trim().take(1200)
        val cleanMapsUrl = requireSafeOptionalUrl(mapsUrl, SafeLinkCategory.GOOGLE_MAPS)
        val boundedPhotos = when {
            photos.isNotEmpty() -> photos.take(4)
            photo != null -> listOf(photo)
            else -> emptyList()
        }

        val payload = JSONObject().apply {
            put("id", recordId)
            put("proposer_id", uid)
            put("proposer_name", cleanProposer)
            put("title", cleanTitle)
            put("category", category)
            put("event_date_text", cleanDate)
            put("time_text", cleanTime)
            put("venue_name", cleanVenue)
            put("district", cleanDistrict)
            put("address", cleanAddress)
            put("lat", lat)
            put("lng", lng)
            put("price_label", cleanPrice)
            put("description", cleanDesc)
            put("image_key", imageKey)
            put("maps_url", cleanMapsUrl)
            put("status", "PENDING")
            put("admin_note", "")
            put("interested_count", 1)
            put("interested_uids", uid)
        }
        return try {
            val created = if (boundedPhotos.isNotEmpty()) {
                val fields = mapOf(
                    "id" to recordId,
                    "proposer_id" to uid,
                    "proposer_name" to cleanProposer,
                    "title" to cleanTitle,
                    "category" to category,
                    "event_date_text" to cleanDate,
                    "time_text" to cleanTime,
                    "venue_name" to cleanVenue,
                    "district" to cleanDistrict,
                    "address" to cleanAddress,
                    "lat" to lat.toString(),
                    "lng" to lng.toString(),
                    "price_label" to cleanPrice,
                    "description" to cleanDesc,
                    "image_key" to imageKey,
                    "maps_url" to cleanMapsUrl,
                    "status" to "PENDING",
                    "admin_note" to "",
                    "interested_count" to "1",
                    "interested_uids" to uid,
                )
                val allFileParts = boundedPhotos.mapIndexed { idx, p ->
                    MultipartFilePart(
                        fieldName = "photo",
                        fileName = if (boundedPhotos.size > 1) "${idx + 1}_${p.fileName}" else p.fileName,
                        mimeType = p.mimeType,
                        bytes = p.bytes,
                    )
                }
                val resp = try {
                    pb.createRecordMultipartFiles(
                        collection = "events",
                        fields = fields,
                        fileParts = allFileParts,
                    )
                } catch (e: PocketBaseException) {
                    if (e.statusCode == 400 && allFileParts.size > 1) {
                        try {
                            val singleResp = pb.createRecordMultipartFiles(
                                collection = "events",
                                fields = fields,
                                fileParts = listOf(allFileParts.first()),
                            )
                            onMultiPhotoFallbackUsed?.invoke()
                            singleResp
                        } catch (fallbackErr: PocketBaseException) {
                            if (fallbackErr.statusCode == 400) {
                                onPhotoSchemaMissing?.invoke()
                                val fallbackId = pb.createRecord("events", payload).optString("id").ifBlank { recordId }
                                SurabayaSpamProtector.recordSubmission(uid, "events")
                                cachedEvents[fallbackId] = SurabayaEvent(
                                    id = fallbackId,
                                    proposerId = uid,
                                    proposerName = cleanProposer,
                                    title = cleanTitle,
                                    category = category,
                                    eventDateText = cleanDate,
                                    timeText = cleanTime,
                                    venueName = cleanVenue,
                                    district = cleanDistrict,
                                    address = cleanAddress,
                                    priceLabel = cleanPrice,
                                    description = cleanDesc,
                                    mapsUrl = cleanMapsUrl,
                                    status = "PENDING",
                                    interestedCount = 1,
                                    interestedUids = listOf(uid),
                                    createdAt = System.currentTimeMillis(),
                                )
                                return fallbackId
                            } else {
                                throw fallbackErr
                            }
                        }
                    } else if (e.statusCode == 400) {
                        onPhotoSchemaMissing?.invoke()
                        val fallbackId = pb.createRecord("events", payload).optString("id").ifBlank { recordId }
                        SurabayaSpamProtector.recordSubmission(uid, "events")
                        cachedEvents[fallbackId] = SurabayaEvent(
                            id = fallbackId,
                            proposerId = uid,
                            proposerName = cleanProposer,
                            title = cleanTitle,
                            category = category,
                            eventDateText = cleanDate,
                            timeText = cleanTime,
                            venueName = cleanVenue,
                            district = cleanDistrict,
                            address = cleanAddress,
                            priceLabel = cleanPrice,
                            description = cleanDesc,
                            mapsUrl = cleanMapsUrl,
                            status = "PENDING",
                            interestedCount = 1,
                            interestedUids = listOf(uid),
                            createdAt = System.currentTimeMillis(),
                        )
                        return fallbackId
                    } else {
                        throw e
                    }
                }
                if (extractFileNames(resp, "photo").isEmpty()) {
                    onPhotoSchemaMissing?.invoke()
                }
                resp
            } else {
                pb.createRecord("events", payload)
            }
            val finalId = created.optString("id").ifBlank { recordId }
            SurabayaSpamProtector.recordSubmission(uid, "events")
            cachedEvents[finalId] = SurabayaEvent(
                id = finalId,
                proposerId = uid,
                proposerName = cleanProposer,
                title = cleanTitle,
                category = category,
                eventDateText = cleanDate,
                timeText = cleanTime,
                venueName = cleanVenue,
                district = cleanDistrict,
                address = cleanAddress,
                priceLabel = cleanPrice,
                description = cleanDesc,
                mapsUrl = cleanMapsUrl,
                status = "PENDING",
                interestedCount = 1,
                interestedUids = listOf(uid),
                createdAt = System.currentTimeMillis(),
            )
            finalId
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.CREATE, "events/$recordId")
            throw e
        }
    }

    suspend fun moderateEvent(
        eventId: String,
        approve: Boolean,
        adminNote: String,
    ) {
        requireUserId()
        try {
            val patch = JSONObject().apply {
                put("status", if (approve) "APPROVED" else "REJECTED")
                put("admin_note", adminNote.trim().take(300))
            }
            pb.updateRecord("events", eventId, patch)
            if (approve) {
                runCatching {
                    val eventRec = pb.getOneOrNull("events", eventId)
                    if (eventRec != null) {
                        val event = mapEvent(eventRec)
                        broadcastEventDirectMessageToCitizens(event)
                    }
                }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.UPDATE, "events/$eventId")
            throw e
        }
    }

    suspend fun toggleEventInterest(event: SurabayaEvent) {
        val uid = requireUserId()
        val (list, nextCount) = SurabayaSpamProtector.toggleEventAttendance(
            currentUids = event.interestedUids,
            userId = uid,
        )
        try {
            val patch = JSONObject().apply {
                put("interested_count", nextCount)
                put("interested_uids", encodeUidList(list))
            }
            pb.updateRecord("events", event.id, patch)
            cachedEvents[event.id] = event.copy(
                interestedUids = list,
                interestedCount = nextCount,
            )
            bumpMutationTick()
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.UPDATE, "events/${event.id}")
            throw e
        }
    }

    // --- 5. KULINER SURABAYA & REVIEWS ---

    fun observeCulinarySpots(): Flow<List<CulinarySpot>> {
        return combine(
            pb.observeCollection("culinary", pollIntervalMs = 7_000L) {
                pb.getList(
                    collection = "culinary",
                    sort = "-rating_avg,-created",
                )
            },
            localMutationTick,
        ) { list, _ ->
            val mapped = list.map(::mapCulinarySpot)
                .filter { "culinary:${it.id}" !in localDeletedRecords && it.createdBy !in localBannedUids }
            mapped.forEach { cachedCulinary[it.id] = it }
            mapped
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.LIST, "culinary")
            throw e
        }
    }

    suspend fun createCulinarySpot(
        authorName: String,
        name: String,
        category: String,
        description: String,
        district: String,
        address: String,
        priceRange: String,
        openHour: Int,
        closeHour: Int,
        mapsUrl: String = "",
        gofoodUrl: String = "",
        grabfoodUrl: String = "",
        shopeefoodUrl: String = "",
        customLat: Double? = null,
        customLng: Double? = null,
        imageKey: String = defaultCulinaryImageKey(category),
        photos: List<PreparedPhoto> = emptyList(),
        onPhotoSchemaMissing: (() -> Unit)? = null,
        onMultiPhotoFallbackUsed: (() -> Unit)? = null,
    ): String {
        val uid = requireUserId()
        val spamCheck = SurabayaSpamProtector.checkCulinaryDuplicate(
            userId = uid,
            name = name,
            district = district,
            description = description,
            existingCulinary = cachedCulinary.values.toList(),
        )
        if (spamCheck is SpamCheckResult.Blocked) {
            throw DuplicateContentException(spamCheck.reason)
        }

        val recordId = PocketBaseClient.deterministicPbId("kul_${UUID.randomUUID()}")
        val cleanMapsUrl = requireSafeOptionalUrl(mapsUrl, SafeLinkCategory.GOOGLE_MAPS)
        val (lat, lng) = when {
            customLat != null && customLng != null && (customLat != 0.0 || customLng != 0.0) -> {
                customLat to customLng
            }
            else -> extractLatLngFromMapsUrl(cleanMapsUrl) ?: SurabayaDistricts.coordinateFor(district)
        }
        val cleanAuthor = authorName.trim().take(80).ifEmpty { "Warga Surabaya" }
        val cleanName = name.trim().take(100)
        val cleanDesc = description.trim().take(1000)
        val cleanDistrict = district.trim().take(60)
        val cleanAddress = address.trim().take(180)
        val cleanPrice = priceRange.trim().take(60).ifEmpty { "Rp 15.000 - Rp 30.000" }
        val cleanGofoodUrl = requireSafeOptionalUrl(gofoodUrl, SafeLinkCategory.GOFOOD)
        val cleanGrabfoodUrl = requireSafeOptionalUrl(grabfoodUrl, SafeLinkCategory.GRABFOOD)
        val cleanShopeefoodUrl = requireSafeOptionalUrl(shopeefoodUrl, SafeLinkCategory.SHOPEEFOOD)
        val safeOpen = openHour.coerceIn(0, 23)
        val safeClose = closeHour.coerceIn(0, 24)
        val boundedPhotos = photos.take(4)

        val payload = JSONObject().apply {
            put("id", recordId)
            put("created_by", uid)
            put("author_name", cleanAuthor)
            put("name", cleanName)
            put("category", category)
            put("description", cleanDesc)
            put("district", cleanDistrict)
            put("address", cleanAddress)
            put("price_range", cleanPrice)
            put("open_hour", safeOpen)
            put("close_hour", safeClose)
            put("lat", lat)
            put("lng", lng)
            put("image_key", imageKey)
            put("maps_url", cleanMapsUrl)
            put("gofood_url", cleanGofoodUrl)
            put("grabfood_url", cleanGrabfoodUrl)
            put("shopeefood_url", cleanShopeefoodUrl)
            put("rating_avg", 5.0)
            put("review_count", 1)
        }
        return try {
            val created = if (boundedPhotos.isNotEmpty()) {
                val fields = mapOf(
                    "id" to recordId,
                    "created_by" to uid,
                    "author_name" to cleanAuthor,
                    "name" to cleanName,
                    "category" to category,
                    "description" to cleanDesc,
                    "district" to cleanDistrict,
                    "address" to cleanAddress,
                    "price_range" to cleanPrice,
                    "open_hour" to safeOpen.toString(),
                    "close_hour" to safeClose.toString(),
                    "lat" to lat.toString(),
                    "lng" to lng.toString(),
                    "image_key" to imageKey,
                    "maps_url" to cleanMapsUrl,
                    "gofood_url" to cleanGofoodUrl,
                    "grabfood_url" to cleanGrabfoodUrl,
                    "shopeefood_url" to cleanShopeefoodUrl,
                    "rating_avg" to "5.0",
                    "review_count" to "1",
                )
                val allFileParts = boundedPhotos.mapIndexed { idx, p ->
                    MultipartFilePart(
                        fieldName = "photo",
                        fileName = if (boundedPhotos.size > 1) "${idx + 1}_${p.fileName}" else p.fileName,
                        mimeType = p.mimeType,
                        bytes = p.bytes,
                    )
                }
                val resp = try {
                    pb.createRecordMultipartFiles(
                        collection = "culinary",
                        fields = fields,
                        fileParts = allFileParts,
                    )
                } catch (e: PocketBaseException) {
                    if (e.statusCode == 400 && allFileParts.size > 1) {
                        // Server may still have maxSelect = 1 before re-importing pocketbase_collections.json
                        try {
                            val singleResp = pb.createRecordMultipartFiles(
                                collection = "culinary",
                                fields = fields,
                                fileParts = listOf(allFileParts.first()),
                            )
                            onMultiPhotoFallbackUsed?.invoke()
                            singleResp
                        } catch (fallbackErr: PocketBaseException) {
                            if (fallbackErr.statusCode == 400) {
                                onPhotoSchemaMissing?.invoke()
                                return pb.createRecord("culinary", payload).optString("id").ifBlank { recordId }
                            } else {
                                throw fallbackErr
                            }
                        }
                    } else if (e.statusCode == 400) {
                        onPhotoSchemaMissing?.invoke()
                        val fallbackId = pb.createRecord("culinary", payload).optString("id").ifBlank { recordId }
                        SurabayaSpamProtector.recordSubmission(uid, "culinary")
                        cachedCulinary[fallbackId] = CulinarySpot(
                            id = fallbackId,
                            createdBy = uid,
                            authorName = cleanAuthor,
                            name = cleanName,
                            category = category,
                            description = cleanDesc,
                            district = cleanDistrict,
                            address = cleanAddress,
                            priceRange = cleanPrice,
                            openHour = safeOpen,
                            closeHour = safeClose,
                            mapsUrl = cleanMapsUrl,
                            gofoodUrl = cleanGofoodUrl,
                            grabfoodUrl = cleanGrabfoodUrl,
                            shopeefoodUrl = cleanShopeefoodUrl,
                            createdAt = System.currentTimeMillis(),
                        )
                        return fallbackId
                    } else {
                        throw e
                    }
                }
                if (extractFileNames(resp, "photo").isEmpty()) {
                    onPhotoSchemaMissing?.invoke()
                }
                resp
            } else {
                pb.createRecord("culinary", payload)
            }
            val finalId = created.optString("id").ifBlank { recordId }
            SurabayaSpamProtector.recordSubmission(uid, "culinary")
            cachedCulinary[finalId] = CulinarySpot(
                id = finalId,
                createdBy = uid,
                authorName = cleanAuthor,
                name = cleanName,
                category = category,
                description = cleanDesc,
                district = cleanDistrict,
                address = cleanAddress,
                priceRange = cleanPrice,
                openHour = safeOpen,
                closeHour = safeClose,
                mapsUrl = cleanMapsUrl,
                gofoodUrl = cleanGofoodUrl,
                grabfoodUrl = cleanGrabfoodUrl,
                shopeefoodUrl = cleanShopeefoodUrl,
                createdAt = System.currentTimeMillis(),
            )
            finalId
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.CREATE, "culinary/$recordId")
            throw e
        }
    }

    fun observeCulinaryReviews(culinaryId: String): Flow<List<CulinaryReview>> {
        val safeCulId = escapePbValue(culinaryId)
        return pb.observeCollection("culinary_reviews", pollIntervalMs = 5_000L) {
            pb.getList(
                collection = "culinary_reviews",
                filter = "culinary_id = '$safeCulId'",
                sort = "-created",
            ).map(::mapCulinaryReview)
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.LIST, "culinary_reviews")
            throw e
        }
    }

    suspend fun addCulinaryReview(
        spot: CulinarySpot,
        authorName: String,
        rating: Int,
        comment: String,
    ) {
        val uid = requireUserId()
        val safeRating = rating.coerceIn(1, 5)
        val reviewId = PocketBaseClient.deterministicPbId("rev_${UUID.randomUUID()}")
        val nextCount = spot.reviewCount + 1
        val nextAvg = (((spot.ratingAvg * spot.reviewCount) + safeRating) / nextCount * 10.0).toInt() / 10.0
        try {
            val reviewPayload = JSONObject().apply {
                put("id", reviewId)
                put("culinary_id", spot.id)
                put("author_id", uid)
                put("author_name", authorName.trim().take(80).ifEmpty { "Warga Surabaya" })
                put("rating", safeRating)
                put("comment", comment.trim().take(500))
            }
            pb.createRecord("culinary_reviews", reviewPayload)

            val spotPatch = JSONObject().apply {
                put("rating_avg", nextAvg.coerceIn(0.0, 5.0))
                put("review_count", nextCount)
            }
            pb.updateRecord("culinary", spot.id, spotPatch)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.CREATE, "culinary_reviews/$reviewId")
            throw e
        }
    }

    // --- 5B. KEAMANAN TAUTAN WARGA (ANTI-PHISHING REPORT & ADMIN LINK WIPE) ---

    suspend fun reportSuspiciousLink(
        collection: String,
        recordId: String,
        currentCount: Int,
        currentReportedUids: List<String>,
    ): Int {
        val uid = requireUserId()
        val updatedList = currentReportedUids.toMutableList()
        if (uid in updatedList) {
            return currentCount.coerceAtLeast(updatedList.size)
        }
        if (updatedList.size < 80) {
            updatedList.add(uid)
        }
        val nextCount = (currentCount + 1).coerceAtLeast(updatedList.size)
        val shouldAutoWipe = nextCount >= LinkSecurityValidator.AUTO_HIDE_LINK_REPORT_THRESHOLD

        try {
            val patch = JSONObject().apply {
                put("link_report_count", nextCount)
                put("link_reported_uids", encodeUidList(updatedList))
                if (shouldAutoWipe) {
                    put("maps_url", "")
                    if (collection == "culinary") {
                        put("gofood_url", "")
                        put("grabfood_url", "")
                        put("shopeefood_url", "")
                    }
                }
            }
            try {
                pb.updateRecord(collection, recordId, patch)
            } catch (e: PocketBaseException) {
                if (e.statusCode == 400 && shouldAutoWipe) {
                    // Fallback if live server hasn't imported link_report_count yet: wipe the suspicious URLs directly
                    val fallbackPatch = JSONObject().apply {
                        put("maps_url", "")
                        if (collection == "culinary") {
                            put("gofood_url", "")
                            put("grabfood_url", "")
                            put("shopeefood_url", "")
                        }
                    }
                    pb.updateRecord(collection, recordId, fallbackPatch)
                } else if (e.statusCode != 400) {
                    throw e
                }
            }
            return nextCount
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.UPDATE, "$collection/$recordId")
            throw e
        }
    }

    suspend fun clearAllExternalLinksByAdmin(
        collection: String,
        recordId: String,
    ) {
        requireUserId()
        try {
            val patch = JSONObject().apply {
                put("maps_url", "")
                if (collection == "culinary") {
                    put("gofood_url", "")
                    put("grabfood_url", "")
                    put("shopeefood_url", "")
                }
            }
            pb.updateRecord(collection, recordId, patch)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.UPDATE, "$collection/$recordId")
            throw e
        }
    }

    // --- 5C. KEAMANAN KONTEN VISUAL, AUTO-BLUR, KARANTINA & BLOKIR PENGGUNA ---

    suspend fun reportInappropriateContent(
        collection: String,
        recordId: String,
        currentCount: Int,
        currentReportedUids: List<String>,
    ): Int {
        val uid = requireUserId()
        val key = "$collection:$recordId"
        val existingOverlay = localContentReports[key]
        val baseUids = existingOverlay?.uids ?: currentReportedUids
        val baseCount = existingOverlay?.count ?: currentCount
        val updatedList = baseUids.toMutableList()
        if (uid in updatedList) {
            return baseCount.coerceAtLeast(updatedList.size)
        }
        if (updatedList.size < 80) {
            updatedList.add(uid)
        }
        val nextCount = (baseCount + 1).coerceAtLeast(updatedList.size)
        localContentReports[key] = LocalContentReportOverlay(
            count = nextCount,
            uids = updatedList,
            forceOverride = false,
        )
        bumpMutationTick()

        try {
            val patch = JSONObject().apply {
                put("content_report_count", nextCount)
                put("content_reported_uids", encodeUidList(updatedList))
            }
            try {
                pb.updateRecord(collection, recordId, patch)
                localContentReports.remove(key)
            } catch (e: PocketBaseException) {
                if (e.statusCode != 400) throw e
                Log.w(TAG, "PocketBase schema missing content_report_count on $collection; using reactive overlay.")
            }
            return nextCount
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.UPDATE, "$collection/$recordId")
            throw e
        }
    }

    suspend fun adminRestoreContentReports(
        collection: String,
        recordId: String,
    ) {
        requireUserId()
        val key = "$collection:$recordId"
        localContentReports[key] = LocalContentReportOverlay(
            count = 0,
            uids = emptyList(),
            forceOverride = true,
        )
        if (collection == "barkas_items") {
            inMemoryBarkas[recordId]?.let { item ->
                inMemoryBarkas[recordId] = item.copy(contentReportCount = 0, contentReportedByUids = emptyList())
            }
        } else if (collection == "services") {
            inMemoryServices[recordId]?.let { svc ->
                inMemoryServices[recordId] = svc.copy(contentReportCount = 0, contentReportedByUids = emptyList())
            }
        }
        bumpMutationTick()

        try {
            val patch = JSONObject().apply {
                put("content_report_count", 0)
                put("content_reported_uids", "")
            }
            try {
                pb.updateRecord(collection, recordId, patch)
                localContentReports.remove(key)
            } catch (e: PocketBaseException) {
                if (e.statusCode != 400 && e.statusCode != 404) throw e
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.UPDATE, "$collection/$recordId")
            throw e
        }
    }

    suspend fun adminDeletePost(
        collection: String,
        recordId: String,
    ) {
        requireUserId()
        if (collection == "barkas_items") {
            inMemoryBarkas.remove(recordId)
            inMemoryBarkasComments.remove(recordId)
        } else if (collection == "services") {
            inMemoryServices.remove(recordId)
        }
        localDeletedRecords.add("$collection:$recordId")
        bumpMutationTick()
        try {
            pb.deleteRecord(collection, recordId)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: PocketBaseException) {
            if (e.statusCode != 404) {
                logBackendError(e, OperationType.DELETE, "$collection/$recordId")
                throw e
            }
        } catch (e: Exception) {
            logBackendError(e, OperationType.DELETE, "$collection/$recordId")
            throw e
        }
    }

    suspend fun blockUser(
        targetUid: String,
        currentBlockedUids: List<String>,
    ): List<String> {
        val uid = requireUserId()
        val cleanTarget = targetUid.trim()
        if (cleanTarget.isBlank() || cleanTarget == uid) return currentBlockedUids
        val base = localBlockedUids[uid] ?: currentBlockedUids
        val updated = (base + cleanTarget).map { it.trim() }.filter { it.isNotBlank() }.distinct().take(120)
        localBlockedUids[uid] = updated
        bumpMutationTick()

        try {
            val patch = JSONObject().apply {
                put("blocked_uids", encodeUidList(updated))
            }
            try {
                pb.updateRecord("users", uid, patch)
            } catch (e: PocketBaseException) {
                if (e.statusCode != 400) throw e
                Log.w(TAG, "PocketBase users.blocked_uids not yet imported; using reactive overlay.")
            }
            return updated
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.UPDATE, "users/$uid")
            throw e
        }
    }

    suspend fun unblockUser(
        targetUid: String,
        currentBlockedUids: List<String>,
    ): List<String> {
        val uid = requireUserId()
        val cleanTarget = targetUid.trim()
        val base = localBlockedUids[uid] ?: currentBlockedUids
        val updated = base.filter { it.isNotBlank() && it != cleanTarget }
        localBlockedUids[uid] = updated
        bumpMutationTick()

        try {
            val patch = JSONObject().apply {
                put("blocked_uids", encodeUidList(updated))
            }
            try {
                pb.updateRecord("users", uid, patch)
            } catch (e: PocketBaseException) {
                if (e.statusCode != 400) throw e
            }
            return updated
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.UPDATE, "users/$uid")
            throw e
        }
    }

    suspend fun adminBanUser(targetUid: String) {
        val uid = requireUserId()
        val cleanTarget = targetUid.trim()
        if (cleanTarget.isBlank() || cleanTarget == uid) return
        localBannedUids.add(cleanTarget)
        bumpMutationTick()

        try {
            try {
                val userPatch = JSONObject().apply {
                    put("is_banned", true)
                    put("is_discoverable", false)
                }
                pb.updateRecord("users", cleanTarget, userPatch)
            } catch (e: PocketBaseException) {
                if (e.statusCode == 400) {
                    val fallbackPatch = JSONObject().apply { put("is_discoverable", false) }
                    runCatching { pb.updateRecord("users", cleanTarget, fallbackPatch) }
                } else if (e.statusCode != 403 && e.statusCode != 404) {
                    throw e
                }
            }

            try {
                val nearbyPatch = JSONObject().apply {
                    put("is_banned", true)
                    put("is_discoverable", false)
                }
                pb.updateRecord("nearby_profiles", cleanTarget, nearbyPatch)
            } catch (e: PocketBaseException) {
                if (e.statusCode == 400) {
                    val fallbackPatch = JSONObject().apply { put("is_discoverable", false) }
                    runCatching { pb.updateRecord("nearby_profiles", cleanTarget, fallbackPatch) }
                } else if (e.statusCode != 403 && e.statusCode != 404) {
                    throw e
                }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.UPDATE, "users/$cleanTarget")
            throw e
        }
    }

    suspend fun adminUnbanUser(targetUid: String) {
        val uid = requireUserId()
        val cleanTarget = targetUid.trim()
        if (cleanTarget.isBlank() || cleanTarget == uid) return
        localBannedUids.remove(cleanTarget)
        bumpMutationTick()

        try {
            try {
                val userPatch = JSONObject().apply {
                    put("is_banned", false)
                    put("is_discoverable", true)
                }
                pb.updateRecord("users", cleanTarget, userPatch)
            } catch (e: PocketBaseException) {
                if (e.statusCode == 400) {
                    val fallbackPatch = JSONObject().apply { put("is_discoverable", true) }
                    runCatching { pb.updateRecord("users", cleanTarget, fallbackPatch) }
                } else if (e.statusCode != 403 && e.statusCode != 404) {
                    throw e
                }
            }

            try {
                val nearbyPatch = JSONObject().apply {
                    put("is_banned", false)
                    put("is_discoverable", true)
                }
                pb.updateRecord("nearby_profiles", cleanTarget, nearbyPatch)
            } catch (e: PocketBaseException) {
                if (e.statusCode == 400) {
                    val fallbackPatch = JSONObject().apply { put("is_discoverable", true) }
                    runCatching { pb.updateRecord("nearby_profiles", cleanTarget, fallbackPatch) }
                } else if (e.statusCode != 403 && e.statusCode != 404) {
                    throw e
                }
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.UPDATE, "users/$cleanTarget")
            throw e
        }
    }

    fun observeBannedProfiles(): Flow<List<NearbyProfile>> {
        return combine(
            pb.observeCollection("nearby_profiles", pollIntervalMs = 10_000L) {
                pb.getList(
                    collection = "nearby_profiles",
                    sort = "-updated",
                )
            },
            localMutationTick,
        ) { list, _ ->
            val mapped = list.map(::mapNearbyProfile)
            val fromBackendOrOverlay = mapped.filter { profile ->
                profile.isBanned || profile.id in localBannedUids || profile.userId in localBannedUids
            }
            val coveredIds = fromBackendOrOverlay.flatMap { listOf(it.id, it.userId) }.toSet()
            val syntheticBanned = localBannedUids
                .filter { it.isNotBlank() && it !in coveredIds }
                .map { uid ->
                    NearbyProfile(
                        id = uid,
                        userId = uid,
                        displayName = "Warga ($uid)",
                        district = "Surabaya",
                        isDiscoverable = false,
                        isBanned = true,
                    )
                }
            (fromBackendOrOverlay + syntheticBanned).distinctBy { it.userId.ifBlank { it.id } }
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.LIST, "nearby_profiles")
            emit(emptyList())
        }
    }

    // --- 6. TEMAN SEKITAR (NEARBY FRIENDS) ---

    fun observeDiscoverableProfiles(): Flow<List<NearbyProfile>> {
        return combine(
            pb.observeCollection("nearby_profiles", pollIntervalMs = 7_000L) {
                pb.getList(
                    collection = "nearby_profiles",
                    filter = "is_discoverable = true",
                    sort = "-updated",
                )
            },
            localMutationTick,
        ) { list, _ ->
            list.map(::mapNearbyProfile)
                .filter { !it.isBanned && it.id !in localBannedUids && it.userId !in localBannedUids }
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.LIST, "nearby_profiles")
            throw e
        }
    }

    // --- 7. CHAT WARGA SURABAYA (PUBLIC & DIRECT TWO-WAY) ---

    fun observePublicChatRooms(): Flow<List<ChatRoom>> {
        return pb.observeCollection("chat_rooms", pollIntervalMs = 4_000L) {
            pb.getList(
                collection = "chat_rooms",
                filter = "room_type = 'PUBLIC'",
                sort = "-updated",
            ).map(::mapChatRoom)
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.LIST, "chat_rooms")
            throw e
        }
    }

    fun observeDirectChatRooms(userId: String): Flow<List<ChatRoom>> {
        val safeUid = escapePbValue(userId)
        return pb.observeCollection("chat_rooms", pollIntervalMs = 3_500L) {
            pb.getList(
                collection = "chat_rooms",
                filter = "room_type = 'DIRECT' && (participant_uid_1 = '$safeUid' || participant_uid_2 = '$safeUid')",
                sort = "-updated",
            ).map(::mapChatRoom)
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.LIST, "chat_rooms")
            throw e
        }
    }

    suspend fun openOrCreateDirectChat(
        peerProfile: NearbyProfile,
        currentUserName: String,
        currentUserDistrict: String,
    ): ChatRoom {
        val uid = requireUserId()
        val rawPeerId = peerProfile.id.ifBlank { peerProfile.userId }.trim()
        val peerUidForList = if (rawPeerId == uid || rawPeerId.isBlank()) {
            "self_${uid.take(40)}"
        } else {
            rawPeerId.take(56)
        }
        val sortedPair = listOf(uid, peerUidForList).sorted()
        val roomKey = buildDirectRoomId(uid, peerUidForList)
        val roomId = PocketBaseClient.deterministicPbId(roomKey)

        val cleanCurrentName = currentUserName.trim().replace("|", " ").take(60).ifEmpty { "Warga Surabaya" }
        val cleanPeerName = peerProfile.displayName.trim().replace("|", " ").take(60).ifEmpty { "Warga Surabaya" }
        val alignedNames = if (sortedPair[0] == uid) {
            "$cleanCurrentName|$cleanPeerName"
        } else {
            "$cleanPeerName|$cleanCurrentName"
        }

        return try {
            val existing = pb.getOneOrNull("chat_rooms", roomId)
            if (existing != null) {
                return mapChatRoom(existing)
            }
            val subtitleText = "${peerProfile.district} • $currentUserDistrict".take(140)
            val payload = JSONObject().apply {
                put("id", roomId)
                put("room_key", roomKey)
                put("room_type", "DIRECT")
                put("title", cleanPeerName.take(100))
                put("subtitle", subtitleText)
                put("participant_uid_1", sortedPair[0])
                put("participant_uid_2", sortedPair[1])
                put("participant_names", alignedNames.take(160))
                put("last_message", "Mulai sapaan hangat warga Surabaya 👋")
                put("last_sender_name", cleanCurrentName)
                put("created_by", uid)
            }
            val created = pb.createRecord("chat_rooms", payload)
            mapChatRoom(created)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.WRITE, "chat_rooms/$roomId")
            throw e
        }
    }

    fun buildEventDmText(event: SurabayaEvent): String {
        return buildString {
            append("🎉 Halo Cak & Ning! Ada agenda event seru terkini di Surabaya:\n\n")
            append("📌 ${event.title}\n")
            if (event.eventDateText.isNotBlank()) append("📅 Tanggal: ${event.eventDateText}")
            if (event.timeText.isNotBlank()) append(" • ${event.timeText}")
            append("\n")
            if (event.venueName.isNotBlank()) append("📍 Lokasi: ${event.venueName}, ${event.district}\n")
            if (event.address.isNotBlank() && event.address != event.venueName) append("🗺️ Alamat: ${event.address}\n")
            append("🎟️ Tiket: ${event.priceLabel}\n\n")
            if (event.description.isNotBlank()) append("${event.description}\n\n")
            append("✨ Poster resmi ini otomatis tersimpan di Galeri HP Anda (Album: ArekSurabaya). Mari ramaikan dan lestarikan guyub rukun Surabaya!")
        }.take(1000)
    }

    suspend fun syncEventDirectMessagesForUser(
        userId: String,
        userName: String,
        events: List<SurabayaEvent>,
    ) = withContext(Dispatchers.IO) {
        if (userId.isBlank() || events.isEmpty()) return@withContext
        val cleanUserName = userName.trim().replace("|", " ").take(60).ifEmpty { "Warga Surabaya" }
        val sortedPair = listOf(OFFICIAL_AREK_SURABAYA_UID, userId).sorted()
        val roomKey = buildDirectRoomId(OFFICIAL_AREK_SURABAYA_UID, userId)
        val roomId = PocketBaseClient.deterministicPbId(roomKey)

        try {
            val existingRoom = pb.getOneOrNull("chat_rooms", roomId)
            val latestEvent = events.maxByOrNull { it.createdAt ?: 0L } ?: events.first()
            val alignedNames = if (sortedPair[0] == OFFICIAL_AREK_SURABAYA_UID) {
                "$OFFICIAL_AREK_SURABAYA_NAME|$cleanUserName"
            } else {
                "$cleanUserName|$OFFICIAL_AREK_SURABAYA_NAME"
            }

            if (existingRoom == null) {
                val roomPayload = JSONObject().apply {
                    put("id", roomId)
                    put("room_key", roomKey)
                    put("room_type", "DIRECT")
                    put("title", OFFICIAL_AREK_SURABAYA_NAME)
                    put("subtitle", "Informasi & Event Resmi Kota Surabaya")
                    put("participant_uid_1", sortedPair[0])
                    put("participant_uid_2", sortedPair[1])
                    put("participant_names", alignedNames.take(160))
                    put("last_message", "🎉 Info Event: ${latestEvent.title}".take(300))
                    put("last_sender_name", OFFICIAL_AREK_SURABAYA_NAME)
                    put("created_by", OFFICIAL_AREK_SURABAYA_UID)
                }
                runCatching { pb.createRecord("chat_rooms", roomPayload) }
            } else {
                val roomPatch = JSONObject().apply {
                    put("last_message", "🎉 Info Event: ${latestEvent.title}".take(300))
                    put("last_sender_name", OFFICIAL_AREK_SURABAYA_NAME)
                }
                runCatching { pb.updateRecord("chat_rooms", roomId, roomPatch) }
            }

            for (event in events) {
                val msgId = PocketBaseClient.deterministicPbId("evdm_${event.id}_${userId}")
                val existingMsg = pb.getOneOrNull("chat_messages", msgId)
                if (existingMsg == null) {
                    val eventPhoto = event.allPhotos.firstOrNull() ?: event.photoUrl
                    val msgPayload = JSONObject().apply {
                        put("id", msgId)
                        put("room_id", roomId)
                        put("room_key", roomKey)
                        put("room_type", "DIRECT")
                        put("participant_uid_1", sortedPair[0])
                        put("participant_uid_2", sortedPair[1])
                        put("sender_id", OFFICIAL_AREK_SURABAYA_UID)
                        put("sender_name", OFFICIAL_AREK_SURABAYA_NAME)
                        put("text", buildEventDmText(event))
                        if (eventPhoto.isNotBlank()) {
                            put("photo_url", eventPhoto)
                        }
                        put("is_delivered", true)
                        put("is_read", false)
                    }
                    runCatching { pb.createRecord("chat_messages", msgPayload) }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "syncEventDirectMessagesForUser warning: ${e.message}")
        }
    }

    suspend fun broadcastEventDirectMessageToCitizens(event: SurabayaEvent) = withContext(Dispatchers.IO) {
        try {
            val citizenRecords = pb.getList("nearby_profiles", perPage = 100)
            for (citizen in citizenRecords) {
                val citizenUid = citizen.optString("user_id").ifBlank { citizen.optString("id") }
                val citizenName = citizen.optString("display_name").ifBlank { "Warga Surabaya" }
                if (citizenUid.isNotBlank() && citizenUid != OFFICIAL_AREK_SURABAYA_UID) {
                    syncEventDirectMessagesForUser(citizenUid, citizenName, listOf(event))
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "broadcastEventDirectMessageToCitizens warning: ${e.message}")
        }
    }

    private suspend fun isCurrentSessionAdmin(uid: String): Boolean {
        val userRec = pb.getOneOrNull("users", uid)
        val email = userRec?.optString("email").orEmpty().ifBlank { pb.currentSession?.email.orEmpty() }
        return isOfficialAdminEmail(email) ||
            userRec?.optBoolean("is_admin", false) == true ||
            (pb.currentSession?.isAdmin == true)
    }

    suspend fun createPublicGroupRoom(
        title: String,
        subtitle: String,
        welcomeMessage: String = "",
        adminDisplayName: String = "Admin Surabaya",
    ): ChatRoom {
        val uid = requireUserId()
        if (!isCurrentSessionAdmin(uid)) {
            throw IllegalStateException("Hanya akun Admin resmi Surabaya yang dapat membuat grup publik baru.")
        }

        val cleanTitle = title.trim().take(100)
        if (cleanTitle.length < 3) {
            throw IllegalArgumentException("Nama grup publik minimal 3 karakter.")
        }
        val cleanSubtitle = subtitle.trim().take(140).ifEmpty { "Ruang diskusi publik resmi warga Surabaya" }
        val cleanWelcome = welcomeMessage.trim().take(300)
            .ifEmpty { "Sugeng rawuh di grup $cleanTitle! Mari berdiskusi dengan santun & saling membantu sesama warga Surabaya." }
        val cleanAdminName = adminDisplayName.trim().take(80).ifEmpty { "Admin Surabaya" }

        val slug = cleanTitle
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
            .take(36)
            .ifEmpty { "grup" }
        val uniqueSuffix = UUID.randomUUID().toString().replace("-", "").take(6)
        val roomKey = "public_${slug}_$uniqueSuffix"
        val roomId = PocketBaseClient.deterministicPbId(roomKey)

        return try {
            val payload = JSONObject().apply {
                put("id", roomId)
                put("room_key", roomKey)
                put("room_type", "PUBLIC")
                put("title", cleanTitle)
                put("subtitle", cleanSubtitle)
                put("participant_uid_1", "")
                put("participant_uid_2", "")
                put("participant_names", "")
                put("last_message", cleanWelcome)
                put("last_sender_name", cleanAdminName)
                put("created_by", uid)
            }
            val createdJson = pb.createRecord("chat_rooms", payload)
            val createdRoom = mapChatRoom(createdJson)

            // Post initial welcome message in the newly created public group
            val welcomeMsgId = PocketBaseClient.deterministicPbId("msg_welcome_$roomKey")
            val welcomeMsgPayload = JSONObject().apply {
                put("id", welcomeMsgId)
                put("room_id", createdRoom.id)
                put("room_key", createdRoom.roomKey)
                put("room_type", "PUBLIC")
                put("participant_uid_1", "")
                put("participant_uid_2", "")
                put("sender_id", uid)
                put("sender_name", cleanAdminName)
                put("text", cleanWelcome)
            }
            runCatching { pb.createRecord("chat_messages", welcomeMsgPayload) }
            localMutationTick.value = System.currentTimeMillis()
            createdRoom
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.CREATE, "chat_rooms/$roomId")
            throw e
        }
    }

    suspend fun adminDeletePublicGroupRoom(roomId: String) {
        val uid = requireUserId()
        if (!isCurrentSessionAdmin(uid)) {
            throw IllegalStateException("Hanya akun Admin resmi Surabaya yang dapat menghapus grup publik.")
        }
        val cleanRoomId = roomId.trim()
        if (cleanRoomId.isEmpty()) return
        try {
            val existing = pb.getOneOrNull("chat_rooms", cleanRoomId)
            if (existing != null && existing.optString("room_type") != "PUBLIC") {
                throw IllegalArgumentException("Hanya ruang grup publik yang dapat dihapus melalui menu ini.")
            }
            pb.deleteRecord("chat_rooms", cleanRoomId)
            localMutationTick.value = System.currentTimeMillis()
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.DELETE, "chat_rooms/$cleanRoomId")
            throw e
        }
    }

    fun observeMessages(
        room: ChatRoom,
        currentUserId: String,
    ): Flow<List<ChatMessage>> {
        val safeRoomId = escapePbValue(room.id)
        val safeUid = escapePbValue(currentUserId)
        val filterQuery = if (room.roomType == "DIRECT") {
            "room_id = '$safeRoomId' && room_type = 'DIRECT' && (participant_uid_1 = '$safeUid' || participant_uid_2 = '$safeUid')"
        } else {
            "room_id = '$safeRoomId' && room_type = 'PUBLIC'"
        }
        return combine(
            pb.observeCollection("chat_messages", pollIntervalMs = 2_500L) {
                pb.getList(
                    collection = "chat_messages",
                    filter = filterQuery,
                    sort = "+created",
                    perPage = 150,
                )
            },
            localMutationTick,
        ) { remoteList, _ ->
            remoteList.map(::mapChatMessage)
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.LIST, "chat_messages/${room.id}")
            throw e
        }
    }

    suspend fun sendMessage(
        room: ChatRoom,
        senderName: String,
        text: String,
        photo: PreparedPhoto? = null,
    ): String {
        val uid = requireUserId()
        val cleanText = text.trim().take(1000)
        if (cleanText.isEmpty() && photo == null) return ""
        val cleanSender = senderName.trim().take(80).ifEmpty { "Warga Surabaya" }
        val msgId = PocketBaseClient.deterministicPbId("msg_${UUID.randomUUID()}")
        val uid1 = if (room.roomType == "DIRECT") room.participantUids.getOrNull(0).orEmpty() else ""
        val uid2 = if (room.roomType == "DIRECT") room.participantUids.getOrNull(1).orEmpty() else ""
        val effectiveText = cleanText.ifEmpty { if (photo != null) "📷 Foto" else "" }

        try {
            if (photo != null) {
                val fields = mapOf(
                    "id" to msgId,
                    "room_id" to room.id,
                    "room_key" to room.roomKey.ifBlank { room.id },
                    "room_type" to room.roomType,
                    "participant_uid_1" to uid1,
                    "participant_uid_2" to uid2,
                    "sender_id" to uid,
                    "sender_name" to cleanSender,
                    "text" to effectiveText,
                )
                val filePart = MultipartFilePart(
                    fieldName = "photo",
                    fileName = photo.fileName,
                    mimeType = photo.mimeType,
                    bytes = photo.bytes,
                )
                try {
                    pb.createRecordMultipartFiles("chat_messages", fields, listOf(filePart))
                } catch (e: Exception) {
                    Log.w(TAG, "Multipart upload for chat photo failed, falling back to JSON: ${e.message}")
                    val fallbackPayload = JSONObject().apply {
                        fields.forEach { (k, v) -> put(k, v) }
                        put("text", if (cleanText.isNotBlank()) "$cleanText (📷 Foto terlampir)" else "📷 Foto terlampir")
                    }
                    pb.createRecord("chat_messages", fallbackPayload)
                }
            } else {
                val msgPayload = JSONObject().apply {
                    put("id", msgId)
                    put("room_id", room.id)
                    put("room_key", room.roomKey.ifBlank { room.id })
                    put("room_type", room.roomType)
                    put("participant_uid_1", uid1)
                    put("participant_uid_2", uid2)
                    put("sender_id", uid)
                    put("sender_name", cleanSender)
                    put("text", effectiveText)
                }
                pb.createRecord("chat_messages", msgPayload)
            }

            val roomPatch = JSONObject().apply {
                put("last_message", (if (photo != null && cleanText.isBlank()) "📷 Foto" else effectiveText).take(300))
                put("last_sender_name", cleanSender)
            }
            pb.updateRecord("chat_rooms", room.id, roomPatch)
            return msgId
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.CREATE, "chat_messages/$msgId")
            throw e
        }
    }

    /**
     * Store-and-forward server storage pruner.
     * When recipient has downloaded the photo to local cache and Galeri:
     * - Marks message as delivered.
     * - If it's a 1-on-1 direct chat, clears the photo attachment from server disk immediately,
     *   ensuring server storage is never burdened while preserving full local offline access!
     */
    suspend fun markChatPhotoDeliveredAndPruneServer(msgId: String, isDirect: Boolean) {
        if (msgId.isBlank()) return
        withContext(Dispatchers.IO) {
            try {
                val patch = JSONObject().apply {
                    put("is_delivered", true)
                    if (isDirect) {
                        put("photo", JSONObject.NULL)
                    }
                }
                pb.updateRecord("chat_messages", msgId, patch)
                Log.d(TAG, "Chat photo $msgId marked delivered (isDirect=$isDirect, serverPruned=$isDirect)")
            } catch (e: Exception) {
                Log.w(TAG, "markChatPhotoDeliveredAndPruneServer error: ${e.message}")
            }
        }
    }

    /**
     * Periodic/lazy cleanup of older delivered server photos to guarantee zero storage bloat.
     */
    suspend fun pruneOldServerChatPhotos() {
        withContext(Dispatchers.IO) {
            try {
                val oldPhotos = pb.getList(
                    collection = "chat_messages",
                    filter = "photo != '' && is_read = true",
                    perPage = 30,
                )
                for (item in oldPhotos) {
                    val id = item.optString("id")
                    if (id.isNotBlank()) {
                        runCatching {
                            pb.updateRecord("chat_messages", id, JSONObject().apply {
                                put("photo", JSONObject.NULL)
                            })
                        }
                    }
                }
            } catch (e: Exception) {
                // Non-critical background cleanup
            }
        }
    }

    fun markMessagesAsRead(room: ChatRoom, currentUserId: String, messageIds: List<String>) {
        if (messageIds.isEmpty()) return
        var newlyMarked = false
        for (id in messageIds) {
            if (localReadMsgIds.add(id)) {
                newlyMarked = true
            }
        }
        if (newlyMarked) {
            bumpMutationTick()
        }
        CoroutineScope(Dispatchers.IO).launch {
            for (id in messageIds) {
                try {
                    pb.updateRecord("chat_messages", id, JSONObject().apply {
                        put("is_read", true)
                    })
                } catch (e: Exception) {
                    // Handled gracefully if schema update is pending
                }
            }
        }
    }

    suspend fun updateTypingStatus(room: ChatRoom, userName: String, isTyping: Boolean) {
        val uid = pb.currentSession?.userId?.takeIf { it.isNotBlank() } ?: return
        val now = System.currentTimeMillis()
        val typingPayload = if (isTyping) "$uid:$userName:$now" else ""
        if (isTyping) {
            localTypingStatuses[room.id] = typingPayload
        } else {
            localTypingStatuses.remove(room.id)
        }
        bumpMutationTick()

        try {
            pb.updateRecord("chat_rooms", room.id, JSONObject().apply {
                put("typing_status", typingPayload)
            })
        } catch (e: Exception) {
            // Handled gracefully if typing_status column is not yet migrated
        }
    }

    // --- JSON -> DOMAIN MAPPERS ---

    private fun mapUserAccount(j: JSONObject): UserAccount {
        val id = j.optString("id")
        val email = j.optString("email")
        val name = j.optString("name").ifBlank { email.substringBefore("@").ifBlank { "Warga Surabaya" } }
        val serverBlocked = decodeUidList(j.optString("blocked_uids"))
        val effectiveBlocked = localBlockedUids[id] ?: serverBlocked
        val effectiveBanned = j.optBoolean("is_banned", false) || id in localBannedUids
        val resolvedGender = localGenderOverrides[id]
            ?: normalizeCitizenGender(j.optString("gender"))
        return UserAccount(
            id = id,
            userId = id,
            displayName = name,
            district = j.optString("district").ifBlank { "Genteng" },
            gender = resolvedGender,
            bio = j.optString("bio"),
            searchRadiusKm = j.optDouble("search_radius_km", 10.0).takeIf { !it.isNaN() && it > 0.0 } ?: 10.0,
            isDiscoverable = if (j.has("is_discoverable")) j.optBoolean("is_discoverable", true) else true,
            isAdmin = j.optBoolean("is_admin", false) || isOfficialAdminEmail(email),
            avatarUrl = pb.buildFileUrl("users", id, j.optString("avatar")),
            blockedUids = effectiveBlocked,
            isBanned = effectiveBanned,
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
            updatedAt = parsePocketBaseTimestamp(j.optString("updated")),
        )
    }

    private fun mapBookmark(j: JSONObject): BookmarkItem = BookmarkItem(
        id = j.optString("id"),
        userId = j.optString("user_id"),
        targetId = j.optString("target_id"),
        targetType = j.optString("target_type", "KULINER"),
        title = j.optString("title"),
        subtitle = j.optString("subtitle"),
        district = j.optString("district", "Surabaya"),
        createdAt = parsePocketBaseTimestamp(j.optString("created")),
    )

    private fun seedPeerDefaultGender(recordId: String): String {
        return when (recordId) {
            PocketBaseClient.deterministicPbId("peer_bima_gubeng") -> "LAKI_LAKI"
            PocketBaseClient.deterministicPbId("peer_reza_rungkut") -> "LAKI_LAKI"
            PocketBaseClient.deterministicPbId("peer_nadia_wonokromo") -> "PEREMPUAN"
            PocketBaseClient.deterministicPbId("peer_sari_genteng") -> "PEREMPUAN"
            else -> ""
        }
    }

    private fun mapNearbyProfile(j: JSONObject): NearbyProfile {
        val id = j.optString("id")
        val uid = j.optString("user_id")
        val serverGender = normalizeCitizenGender(j.optString("gender"))
        val resolvedGender = localGenderOverrides[id]
            ?: (if (id == uid) localGenderOverrides[uid] else null)
            ?: serverGender.ifBlank { seedPeerDefaultGender(id) }
        return NearbyProfile(
            id = id,
            userId = uid,
            displayName = j.optString("display_name", "Warga Surabaya"),
            gender = resolvedGender,
            avatarColorHex = j.optString("avatar_color_hex").ifBlank { "#006B5E" },
            avatarUrl = pb.buildFileUrl("nearby_profiles", id, j.optString("avatar")),
            bio = j.optString("bio"),
            district = j.optString("district").ifBlank { "Genteng" },
            interests = j.optString("interests").ifBlank { "Kuliner, Ngopi, Keliling Kota" },
            lat = j.optDouble("lat", SurabayaDistricts.DEFAULT_LAT),
            lng = j.optDouble("lng", SurabayaDistricts.DEFAULT_LNG),
            isDiscoverable = j.optBoolean("is_discoverable", true),
            isBanned = j.optBoolean("is_banned", false) || id in localBannedUids || uid in localBannedUids,
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
            updatedAt = parsePocketBaseTimestamp(j.optString("updated")),
        )
    }

    private fun mapIncident(j: JSONObject): IncidentReport {
        val id = j.optString("id")
        val cat = j.optString("category", "INFO_WARGA").ifBlank { "INFO_WARGA" }
        val photoFileNames = extractFileNames(j, "photo")
        val resolvedPhotoUrls = photoFileNames
            .map { pb.buildFileUrl("incidents", id, it) }
            .filter { it.isNotBlank() }

        val serverContentCount = j.optInt("content_report_count", 0)
        val serverContentUids = decodeUidList(j.optString("content_reported_uids"))
        val overlay = localContentReports["incidents:$id"]
        val effectiveContentCount = when {
            overlay == null -> serverContentCount
            overlay.forceOverride -> overlay.count
            else -> maxOf(serverContentCount, overlay.count)
        }
        val effectiveContentUids = when {
            overlay == null -> serverContentUids
            overlay.forceOverride -> overlay.uids
            else -> (serverContentUids + overlay.uids).distinct()
        }

        return IncidentReport(
            id = id,
            authorId = j.optString("author_id"),
            authorName = j.optString("author_name", "Warga Surabaya"),
            title = j.optString("title"),
            description = j.optString("description"),
            category = cat,
            severity = j.optString("severity", "INFO").ifBlank { "INFO" },
            district = j.optString("district", "Genteng").ifBlank { "Genteng" },
            address = j.optString("address"),
            lat = j.optDouble("lat", SurabayaDistricts.DEFAULT_LAT),
            lng = j.optDouble("lng", SurabayaDistricts.DEFAULT_LNG),
            imageKey = j.optString("image_key").ifBlank { defaultIncidentImageKey(cat) },
            photoUrl = resolvedPhotoUrls.firstOrNull().orEmpty(),
            photoUrls = resolvedPhotoUrls,
            mapsUrl = LinkSecurityValidator.sanitizeOrEmpty(j.optString("maps_url"), SafeLinkCategory.GOOGLE_MAPS),
            linkReportCount = j.optInt("link_report_count", 0),
            linkReportedByUids = decodeUidList(j.optString("link_reported_uids")),
            contentReportCount = effectiveContentCount,
            contentReportedByUids = effectiveContentUids,
            verifiedCount = j.optInt("verified_count", 0),
            verifiedByUids = decodeUidList(j.optString("verified_by_uids")),
            commentCount = j.optInt("comment_count", 0),
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
            updatedAt = parsePocketBaseTimestamp(j.optString("updated")),
        )
    }

    private fun mapIncidentComment(j: JSONObject): IncidentComment = IncidentComment(
        id = j.optString("id"),
        incidentId = j.optString("incident_id"),
        authorId = j.optString("author_id"),
        authorName = j.optString("author_name", "Warga Surabaya"),
        text = j.optString("text"),
        createdAt = parsePocketBaseTimestamp(j.optString("created")),
    )

    private fun mapEvent(j: JSONObject): SurabayaEvent {
        val id = j.optString("id")
        val cat = j.optString("category", "FESTIVAL").ifBlank { "FESTIVAL" }
        val photoFileNames = extractFileNames(j, "photo")
        val resolvedPhotoUrls = photoFileNames
            .map { pb.buildFileUrl("events", id, it) }
            .filter { it.isNotBlank() }

        val serverContentCount = j.optInt("content_report_count", 0)
        val serverContentUids = decodeUidList(j.optString("content_reported_uids"))
        val overlay = localContentReports["events:$id"]
        val effectiveContentCount = when {
            overlay == null -> serverContentCount
            overlay.forceOverride -> overlay.count
            else -> maxOf(serverContentCount, overlay.count)
        }
        val effectiveContentUids = when {
            overlay == null -> serverContentUids
            overlay.forceOverride -> overlay.uids
            else -> (serverContentUids + overlay.uids).distinct()
        }

        return SurabayaEvent(
            id = id,
            proposerId = j.optString("proposer_id"),
            proposerName = j.optString("proposer_name", "Warga Surabaya"),
            title = j.optString("title"),
            category = cat,
            eventDateText = j.optString("event_date_text"),
            timeText = j.optString("time_text"),
            venueName = j.optString("venue_name"),
            district = j.optString("district", "Genteng").ifBlank { "Genteng" },
            address = j.optString("address"),
            lat = j.optDouble("lat", SurabayaDistricts.DEFAULT_LAT),
            lng = j.optDouble("lng", SurabayaDistricts.DEFAULT_LNG),
            priceLabel = j.optString("price_label").ifBlank { "Gratis" },
            description = j.optString("description"),
            imageKey = j.optString("image_key").ifBlank { defaultEventImageKey(cat) },
            photoUrl = resolvedPhotoUrls.firstOrNull().orEmpty(),
            photoUrls = resolvedPhotoUrls,
            mapsUrl = LinkSecurityValidator.sanitizeOrEmpty(j.optString("maps_url"), SafeLinkCategory.GOOGLE_MAPS),
            linkReportCount = j.optInt("link_report_count", 0),
            linkReportedByUids = decodeUidList(j.optString("link_reported_uids")),
            contentReportCount = effectiveContentCount,
            contentReportedByUids = effectiveContentUids,
            status = j.optString("status", "PENDING").ifBlank { "PENDING" },
            adminNote = j.optString("admin_note"),
            interestedCount = j.optInt("interested_count", 0),
            interestedUids = decodeUidList(j.optString("interested_uids")),
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
            updatedAt = parsePocketBaseTimestamp(j.optString("updated")),
        )
    }

    private fun mapCulinarySpot(j: JSONObject): CulinarySpot {
        val id = j.optString("id")
        val cat = j.optString("category", "LEGENDARIS").ifBlank { "LEGENDARIS" }
        val photoFileNames = extractFileNames(j, "photo")
        val resolvedPhotoUrls = photoFileNames
            .map { pb.buildFileUrl("culinary", id, it) }
            .filter { it.isNotBlank() }

        val serverContentCount = j.optInt("content_report_count", 0)
        val serverContentUids = decodeUidList(j.optString("content_reported_uids"))
        val overlay = localContentReports["culinary:$id"]
        val effectiveContentCount = when {
            overlay == null -> serverContentCount
            overlay.forceOverride -> overlay.count
            else -> maxOf(serverContentCount, overlay.count)
        }
        val effectiveContentUids = when {
            overlay == null -> serverContentUids
            overlay.forceOverride -> overlay.uids
            else -> (serverContentUids + overlay.uids).distinct()
        }

        val districtName = j.optString("district", "Genteng").ifBlank { "Genteng" }
        val sanitizedMapsUrl = LinkSecurityValidator.sanitizeOrEmpty(j.optString("maps_url"), SafeLinkCategory.GOOGLE_MAPS)
        val rawLat = j.optDouble("lat", Double.NaN)
        val rawLng = j.optDouble("lng", Double.NaN)
        val hasValidStoredLatLng = !rawLat.isNaN() && !rawLng.isNaN() && (rawLat != 0.0 || rawLng != 0.0)
        val (resolvedLat, resolvedLng) = extractLatLngFromMapsUrl(sanitizedMapsUrl)
            ?: if (hasValidStoredLatLng) {
                rawLat to rawLng
            } else {
                SurabayaDistricts.coordinateFor(districtName)
            }

        return CulinarySpot(
            id = id,
            createdBy = j.optString("created_by"),
            authorName = j.optString("author_name", "Warga Surabaya"),
            name = j.optString("name"),
            category = cat,
            description = j.optString("description"),
            district = districtName,
            address = j.optString("address"),
            priceRange = j.optString("price_range").ifBlank { "Rp 20.000 - Rp 35.000" },
            openHour = j.optInt("open_hour", 8),
            closeHour = j.optInt("close_hour", 22),
            lat = resolvedLat,
            lng = resolvedLng,
            imageKey = j.optString("image_key").ifBlank { defaultCulinaryImageKey(cat) },
            photoUrl = resolvedPhotoUrls.firstOrNull().orEmpty(),
            photoUrls = resolvedPhotoUrls,
            mapsUrl = sanitizedMapsUrl,
            gofoodUrl = LinkSecurityValidator.sanitizeOrEmpty(j.optString("gofood_url"), SafeLinkCategory.GOFOOD),
            grabfoodUrl = LinkSecurityValidator.sanitizeOrEmpty(j.optString("grabfood_url"), SafeLinkCategory.GRABFOOD),
            shopeefoodUrl = LinkSecurityValidator.sanitizeOrEmpty(j.optString("shopeefood_url"), SafeLinkCategory.SHOPEEFOOD),
            linkReportCount = j.optInt("link_report_count", 0),
            linkReportedByUids = decodeUidList(j.optString("link_reported_uids")),
            contentReportCount = effectiveContentCount,
            contentReportedByUids = effectiveContentUids,
            ratingAvg = j.optDouble("rating_avg", 4.8),
            reviewCount = j.optInt("review_count", 1),
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
            updatedAt = parsePocketBaseTimestamp(j.optString("updated")),
        )
    }

    private fun extractFileNames(j: JSONObject, fieldName: String): List<String> {
        val arr = j.optJSONArray(fieldName)
        if (arr != null) {
            return buildList(arr.length()) {
                for (i in 0 until arr.length()) {
                    val item = arr.optString(i).trim()
                    if (item.isNotBlank() && item != "null") add(item)
                }
            }
        }
        val single = j.optString(fieldName).trim()
        return if (single.isNotBlank() && single != "null" && !single.startsWith("[")) {
            listOf(single)
        } else {
            emptyList()
        }
    }

    private fun requireSafeOptionalUrl(raw: String, category: SafeLinkCategory): String {
        return when (val res = LinkSecurityValidator.validateOptionalLink(raw, category)) {
            is LinkValidationResult.Valid -> res.normalizedUrl
            is LinkValidationResult.Invalid -> throw IllegalArgumentException(res.errorMessage)
        }
    }

    private fun mapCulinaryReview(j: JSONObject): CulinaryReview = CulinaryReview(
        id = j.optString("id"),
        culinaryId = j.optString("culinary_id"),
        authorId = j.optString("author_id"),
        authorName = j.optString("author_name", "Warga Surabaya"),
        rating = j.optInt("rating", 5),
        comment = j.optString("comment"),
        createdAt = parsePocketBaseTimestamp(j.optString("created")),
    )

    private fun mapChatRoom(j: JSONObject): ChatRoom {
        val uid1 = j.optString("participant_uid_1")
        val uid2 = j.optString("participant_uid_2")
        val participants = listOf(uid1, uid2).filter { it.isNotBlank() }
        val id = j.optString("id")
        val remoteTyping = j.optString("typing_status")
        val localTyping = localTypingStatuses[id].orEmpty()
        val effectiveTyping = localTyping.ifBlank { remoteTyping }
        return ChatRoom(
            id = id,
            roomKey = j.optString("room_key"),
            roomType = j.optString("room_type", "PUBLIC"),
            title = j.optString("title"),
            subtitle = j.optString("subtitle"),
            participantUids = participants,
            participantNames = j.optString("participant_names"),
            lastMessage = j.optString("last_message"),
            lastSenderName = j.optString("last_sender_name"),
            typingStatus = effectiveTyping,
            createdBy = j.optString("created_by"),
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
            updatedAt = parsePocketBaseTimestamp(j.optString("updated")),
        )
    }

    private fun mapChatMessage(j: JSONObject): ChatMessage {
        val uid1 = j.optString("participant_uid_1")
        val uid2 = j.optString("participant_uid_2")
        val participants = listOf(uid1, uid2).filter { it.isNotBlank() }
        val id = j.optString("id")
        val photoFileNames = extractFileNames(j, "photo")
        val resolvedPhotoUrl = photoFileNames.firstOrNull()?.let { pb.buildFileUrl("chat_messages", id, it) }
            ?: j.optString("photo_url").ifBlank { j.optString("image_url") }
        val isDeliveredRemote = if (j.has("is_delivered")) j.optBoolean("is_delivered", true) else true
        val isReadRemote = j.optBoolean("is_read", false)
        val isReadLocal = localReadMsgIds.contains(id)
        val isRead = isReadRemote || isReadLocal
        val text = j.optString("text")
        val hasAttachment = photoFileNames.isNotEmpty() || resolvedPhotoUrl.isNotBlank() || j.optBoolean("has_photo", false) || text.contains("📷 Foto")
        return ChatMessage(
            id = id,
            roomId = j.optString("room_id"),
            roomKey = j.optString("room_key"),
            roomType = j.optString("room_type", "PUBLIC"),
            participantUids = participants,
            senderId = j.optString("sender_id"),
            senderName = j.optString("sender_name", "Warga Surabaya"),
            text = text,
            photoUrl = resolvedPhotoUrl,
            isDelivered = isDeliveredRemote,
            isRead = isRead,
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
            hasPhotoAttachment = hasAttachment,
        )
    }

    private fun encodeUidList(list: List<String>): String {
        return list.map { it.trim() }.filter { it.isNotBlank() }.distinct().joinToString("|").take(1950)
    }

    private fun decodeUidList(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return raw.split("|").map { it.trim() }.filter { it.isNotBlank() }
    }

    // --- 7. MOMEN WARGA (USER MOMENTS) ---

    fun observeMoments(): Flow<List<UserMoment>> {
        return combine(
            pb.observeCollection("moments", pollIntervalMs = 6_000L) {
                try {
                    pb.getList(collection = "moments", sort = "-created")
                } catch (e: Exception) {
                    emptyList()
                }
            },
            localMutationTick,
        ) { remoteList, _ ->
            val remoteMoments = remoteList.map(::mapMoment)
            val combinedMap = inMemoryMoments.toMutableMap()
            for (m in remoteMoments) {
                combinedMap[m.id] = m
            }
            combinedMap.values
                .filter { "moments:${it.id}" !in localDeletedRecords && it.authorId !in localBannedUids }
                .sortedByDescending { it.createdAt ?: 0L }
        }.catch { e ->
            Log.w(TAG, "observeMoments fallback: ${e.message}")
            emit(inMemoryMoments.values.filter { "moments:${it.id}" !in localDeletedRecords }.sortedByDescending { it.createdAt ?: 0L })
        }
    }

    suspend fun createMoment(
        authorName: String,
        authorDistrict: String,
        caption: String,
        locationName: String,
        district: String,
        postType: String = POST_TYPE_MOMEN,
        photo: PreparedPhoto? = null,
        photos: List<PreparedPhoto> = emptyList(),
    ): String {
        val uid = requireUserId()
        val spamCheck = SurabayaSpamProtector.checkMomentDuplicate(
            userId = uid,
            caption = caption,
            existingMoments = inMemoryMoments.values.toList(),
        )
        if (spamCheck is SpamCheckResult.Blocked) {
            throw DuplicateContentException(spamCheck.reason)
        }

        val recordId = PocketBaseClient.deterministicPbId("mom_${UUID.randomUUID()}")
        val boundedPhotos = when {
            photos.isNotEmpty() -> photos.take(4)
            photo != null -> listOf(photo)
            else -> emptyList()
        }
        val cleanAuthor = authorName.trim().take(80).ifEmpty { "Warga Surabaya" }
        val cleanCaption = caption.trim().take(1000)
        val cleanLocation = locationName.trim().take(100)
        val cleanDistrict = district.trim().take(60).ifEmpty { "Genteng" }

        val payload = JSONObject().apply {
            put("id", recordId)
            put("author_id", uid)
            put("author_name", cleanAuthor)
            put("author_district", authorDistrict)
            put("caption", cleanCaption)
            put("location_name", cleanLocation)
            put("district", cleanDistrict)
            put("post_type", postType)
            put("image_key", "hero_surabaya")
            put("likes_count", 0)
            put("liked_by_uids", "")
            put("comment_count", 0)
        }

        val fields = mapOf(
            "id" to recordId,
            "author_id" to uid,
            "author_name" to cleanAuthor,
            "author_district" to authorDistrict,
            "caption" to cleanCaption,
            "location_name" to cleanLocation,
            "district" to cleanDistrict,
            "post_type" to postType,
            "image_key" to "hero_surabaya",
            "likes_count" to "0",
            "liked_by_uids" to "",
            "comment_count" to "0",
        )

        val multipartFiles = boundedPhotos.mapIndexed { idx, p ->
            MultipartFilePart(
                fieldName = "photo",
                fileName = if (boundedPhotos.size > 1) "${idx + 1}_${p.fileName}" else p.fileName,
                mimeType = p.mimeType,
                bytes = p.bytes,
            )
        }

        try {
            if (multipartFiles.isNotEmpty()) {
                pb.createRecordMultipartFiles("moments", fields, multipartFiles)
            } else {
                pb.createRecord("moments", payload)
            }
        } catch (e: Exception) {
            Log.w(TAG, "createMoment PB save: ${e.message}, saved to local memory")
        }

        val moment = UserMoment(
            id = recordId,
            authorId = uid,
            authorName = cleanAuthor,
            authorDistrict = authorDistrict,
            caption = cleanCaption,
            locationName = cleanLocation,
            district = cleanDistrict,
            postType = postType,
            imageKey = "hero_surabaya",
            likesCount = 0,
            likedByUids = emptyList(),
            commentCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        inMemoryMoments[recordId] = moment
        SurabayaSpamProtector.recordSubmission(uid, "moments")
        bumpMutationTick()
        return recordId
    }

    suspend fun toggleMomentLike(moment: UserMoment) {
        val uid = requireUserId()
        val isLiked = uid in moment.likedByUids
        val newUids = if (isLiked) moment.likedByUids - uid else moment.likedByUids + uid
        val newCount = if (isLiked) maxOf(0, moment.likesCount - 1) else moment.likesCount + 1

        val updated = moment.copy(
            likesCount = newCount,
            likedByUids = newUids,
        )
        inMemoryMoments[moment.id] = updated
        bumpMutationTick()

        try {
            pb.updateRecord("moments", moment.id, JSONObject().apply {
                put("likes_count", newCount)
                put("liked_by_uids", newUids.joinToString("|"))
            })
        } catch (e: Exception) {
            Log.w(TAG, "toggleMomentLike PB update: ${e.message}")
        }
    }

    fun observeMomentComments(momentId: String): Flow<List<MomentComment>> {
        return combine(
            pb.observeCollection("moment_comments", pollIntervalMs = 5_000L) {
                try {
                    val filter = "moment_id = '${escapePbValue(momentId)}'"
                    pb.getList(collection = "moment_comments", filter = filter, sort = "created")
                } catch (e: Exception) {
                    emptyList()
                }
            },
            localMutationTick,
        ) { remoteList, _ ->
            val remote = remoteList.map(::mapMomentComment)
            val localList = inMemoryMomentComments[momentId] ?: emptyList()
            (remote + localList).distinctBy { it.id }.sortedBy { it.createdAt ?: 0L }
        }.catch {
            emit(inMemoryMomentComments[momentId] ?: emptyList())
        }
    }

    suspend fun addMomentComment(momentId: String, text: String): String {
        val uid = requireUserId()
        val userRec = pb.getOneOrNull("users", uid)
        val authorName = userRec?.optString("name").orEmpty().ifBlank {
            pb.currentSession?.displayName.orEmpty().ifBlank { "Warga Surabaya" }
        }
        val authorDistrict = userRec?.optString("district").orEmpty().ifBlank {
            pb.currentSession?.district.orEmpty().ifBlank { "Genteng" }
        }
        val cleanText = text.trim().take(500)
        if (cleanText.isBlank()) return ""

        val commentId = PocketBaseClient.deterministicPbId("mc_${UUID.randomUUID()}")
        val payload = JSONObject().apply {
            put("id", commentId)
            put("moment_id", momentId)
            put("author_id", uid)
            put("author_name", authorName)
            put("author_district", authorDistrict)
            put("text", cleanText)
        }

        val cur = inMemoryMoments[momentId]
        val nextCommentCount = (cur?.commentCount ?: 0) + 1

        try {
            pb.createRecord("moment_comments", payload)
            runCatching {
                pb.updateRecord("moments", momentId, JSONObject().apply {
                    put("comment_count", nextCommentCount)
                })
            }
        } catch (e: Exception) {
            Log.w(TAG, "addMomentComment PB: ${e.message}")
        }

        val comment = MomentComment(
            id = commentId,
            momentId = momentId,
            authorId = uid,
            authorName = authorName,
            text = cleanText,
            createdAt = System.currentTimeMillis(),
        )
        val list = inMemoryMomentComments.getOrPut(momentId) { CopyOnWriteArrayList() }
        list.add(comment)

        if (cur != null) {
            inMemoryMoments[momentId] = cur.copy(commentCount = nextCommentCount)
        }
        bumpMutationTick()
        return commentId
    }

    // --- 8. DESTINASI SURABAYA (ADMIN-CURATED ATTRACTIONS) ---

    fun observeDestinations(): Flow<List<SurabayaDestination>> {
        return combine(
            pb.observeCollection("destinations", pollIntervalMs = 8_000L) {
                try {
                    pb.getList(collection = "destinations", sort = "-created")
                } catch (e: Exception) {
                    emptyList()
                }
            },
            localMutationTick,
        ) { remoteList, _ ->
            val remoteDestinations = remoteList.map(::mapDestination)
            val combinedMap = inMemoryDestinations.toMutableMap()
            for (d in remoteDestinations) {
                combinedMap[d.id] = d
            }
            combinedMap.values
                .filter { "destinations:${it.id}" !in localDeletedRecords && it.createdBy !in localBannedUids }
                .sortedByDescending { it.ratingAvg }
        }.catch { e ->
            Log.w(TAG, "observeDestinations fallback: ${e.message}")
            emit(inMemoryDestinations.values.filter { "destinations:${it.id}" !in localDeletedRecords }.sortedByDescending { it.ratingAvg })
        }
    }

    suspend fun createDestination(
        title: String,
        category: String,
        description: String,
        district: String,
        address: String,
        ticketPrice: String,
        operationalHours: String,
        bestTime: String,
        mapsUrl: String = "",
        photos: List<PreparedPhoto> = emptyList(),
    ): String {
        val uid = requireUserId()
        // Admin authorization check - only admin accounts can post destinations!
        val userRec = pb.getOneOrNull("users", uid)
        val email = userRec?.optString("email").orEmpty().ifBlank { pb.currentSession?.email.orEmpty() }
        val isAdmin = isOfficialAdminEmail(email) ||
            userRec?.optBoolean("is_admin", false) == true ||
            (pb.currentSession?.isAdmin == true)
        if (!isAdmin) {
            throw IllegalStateException("Hanya akun Admin resmi Surabaya yang memiliki hak untuk menambah destinasi wisata.")
        }

        val recordId = PocketBaseClient.deterministicPbId("dest_${UUID.randomUUID()}")
        val cleanTitle = title.trim().take(140)
        val cleanCategory = category.trim().take(50).ifEmpty { "WISATA_SEJARAH" }
        val cleanDesc = description.trim().take(1200)
        val cleanDistrict = district.trim().take(60).ifEmpty { "Genteng" }
        val cleanAddress = address.trim().take(180)
        val cleanPrice = ticketPrice.trim().take(50).ifEmpty { "Gratis" }
        val cleanHours = operationalHours.trim().take(50).ifEmpty { "24 Jam" }
        val cleanBestTime = bestTime.trim().take(50).ifEmpty { "Sore hari" }
        val cleanMapsUrl = requireSafeOptionalUrl(mapsUrl, SafeLinkCategory.GOOGLE_MAPS)

        val (lat, lng) = extractLatLngFromMapsUrl(cleanMapsUrl) ?: SurabayaDistricts.coordinateFor(cleanDistrict)

        val payload = JSONObject().apply {
            put("id", recordId)
            put("created_by", uid)
            put("author_name", "Admin Resmi Surabaya")
            put("title", cleanTitle)
            put("category", cleanCategory)
            put("description", cleanDesc)
            put("district", cleanDistrict)
            put("address", cleanAddress)
            put("ticket_price", cleanPrice)
            put("operational_hours", cleanHours)
            put("best_time", cleanBestTime)
            put("lat", lat)
            put("lng", lng)
            put("image_key", "hero_surabaya")
            put("maps_url", cleanMapsUrl)
            put("rating_avg", 5.0)
            put("review_count", 0)
        }

        val fields = mapOf(
            "id" to recordId,
            "created_by" to uid,
            "author_name" to "Admin Resmi Surabaya",
            "title" to cleanTitle,
            "category" to cleanCategory,
            "description" to cleanDesc,
            "address" to cleanAddress,
            "district" to cleanDistrict,
            "ticket_price" to cleanPrice,
            "operational_hours" to cleanHours,
            "best_time" to cleanBestTime,
            "lat" to lat.toString(),
            "lng" to lng.toString(),
            "image_key" to "hero_surabaya",
            "maps_url" to cleanMapsUrl,
            "rating_avg" to "5.0",
            "review_count" to "0",
        )

        val multipartFiles = photos.take(4).mapIndexed { idx, p ->
            MultipartFilePart(
                fieldName = "photo",
                fileName = if (photos.size > 1) "${idx + 1}_${p.fileName}" else p.fileName,
                mimeType = p.mimeType,
                bytes = p.bytes,
            )
        }

        try {
            if (multipartFiles.isNotEmpty()) {
                pb.createRecordMultipartFiles("destinations", fields, multipartFiles)
            } else {
                pb.createRecord("destinations", payload)
            }
        } catch (e: Exception) {
            Log.w(TAG, "createDestination PB save: ${e.message}, storing locally")
        }

        val destination = SurabayaDestination(
            id = recordId,
            createdBy = uid,
            authorName = "Admin Resmi Surabaya",
            title = cleanTitle,
            category = category,
            description = cleanDesc,
            district = cleanDistrict,
            address = cleanAddress,
            ticketPrice = cleanPrice,
            operationalHours = cleanHours,
            bestTime = cleanBestTime,
            lat = lat,
            lng = lng,
            imageKey = "hero_surabaya",
            mapsUrl = cleanMapsUrl,
            ratingAvg = 5.0,
            reviewCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        inMemoryDestinations[recordId] = destination
        bumpMutationTick()
        return recordId
    }

    fun observeDestinationReviews(destinationId: String): Flow<List<DestinationReview>> {
        return combine(
            pb.observeCollection("destination_reviews", pollIntervalMs = 6_000L) {
                try {
                    val filter = "destination_id = '${escapePbValue(destinationId)}'"
                    pb.getList(collection = "destination_reviews", filter = filter, sort = "-created")
                } catch (e: Exception) {
                    emptyList()
                }
            },
            localMutationTick,
        ) { remoteList, _ ->
            val remote = remoteList.map(::mapDestinationReview)
            val localList = inMemoryDestinationReviews[destinationId] ?: emptyList()
            (remote + localList).distinctBy { it.id }.sortedByDescending { it.createdAt ?: 0L }
        }.catch {
            emit(inMemoryDestinationReviews[destinationId] ?: emptyList())
        }
    }

    suspend fun addDestinationReview(destinationId: String, rating: Int, comment: String): String {
        val uid = requireUserId()
        val userRec = pb.getOneOrNull("users", uid)
        val authorName = userRec?.optString("name").orEmpty().ifBlank {
            pb.currentSession?.displayName.orEmpty().ifBlank { "Warga Surabaya" }
        }
        val cleanComment = comment.trim().take(500)
        val clampedRating = rating.coerceIn(1, 5)

        val reviewId = PocketBaseClient.deterministicPbId("dr_${UUID.randomUUID()}")
        val payload = JSONObject().apply {
            put("id", reviewId)
            put("destination_id", destinationId)
            put("author_id", uid)
            put("author_name", authorName)
            put("rating", clampedRating)
            put("comment", cleanComment)
        }

        val cur = inMemoryDestinations[destinationId]
        val newCount = (cur?.reviewCount ?: 0) + 1
        val newAvg = if (cur != null) {
            (((cur.ratingAvg * cur.reviewCount) + clampedRating) / newCount * 10.0).toInt() / 10.0
        } else {
            clampedRating.toDouble()
        }

        try {
            pb.createRecord("destination_reviews", payload)
            runCatching {
                pb.updateRecord("destinations", destinationId, JSONObject().apply {
                    put("rating_avg", newAvg.coerceIn(0.0, 5.0))
                    put("review_count", newCount)
                })
            }
        } catch (e: Exception) {
            Log.w(TAG, "addDestinationReview PB: ${e.message}")
        }

        val review = DestinationReview(
            id = reviewId,
            destinationId = destinationId,
            authorId = uid,
            authorName = authorName,
            rating = clampedRating,
            comment = cleanComment,
            createdAt = System.currentTimeMillis(),
        )
        val list = inMemoryDestinationReviews.getOrPut(destinationId) { CopyOnWriteArrayList() }
        list.add(0, review)

        if (cur != null) {
            inMemoryDestinations[destinationId] = cur.copy(
                ratingAvg = newAvg,
                reviewCount = newCount,
            )
        }
        bumpMutationTick()
        return reviewId
    }

    private fun mapMoment(j: JSONObject): UserMoment {
        val id = j.optString("id")
        val photoFileNames = extractFileNames(j, "photo")
        val resolvedPhotoUrls = photoFileNames
            .map { pb.buildFileUrl("moments", id, it) }
            .filter { it.isNotBlank() }

        val serverContentCount = j.optInt("content_report_count", 0)
        val serverContentUids = decodeUidList(j.optString("content_reported_uids"))
        val overlay = localContentReports["moments:$id"]
        val effectiveContentCount = when {
            overlay == null -> serverContentCount
            overlay.forceOverride -> overlay.count
            else -> maxOf(serverContentCount, overlay.count)
        }
        val effectiveContentUids = when {
            overlay == null -> serverContentUids
            overlay.forceOverride -> overlay.uids
            else -> (serverContentUids + overlay.uids).distinct()
        }

        val postType = j.optString("post_type", POST_TYPE_MOMEN).ifBlank { POST_TYPE_MOMEN }

        return UserMoment(
            id = id,
            authorId = j.optString("author_id"),
            authorName = j.optString("author_name", "Warga Surabaya"),
            authorDistrict = j.optString("author_district", "Genteng"),
            caption = j.optString("caption"),
            locationName = j.optString("location_name"),
            district = j.optString("district", "Genteng"),
            postType = postType,
            imageKey = j.optString("image_key", "hero_surabaya"),
            photoUrl = resolvedPhotoUrls.firstOrNull().orEmpty(),
            photoUrls = resolvedPhotoUrls,
            likesCount = j.optInt("likes_count", 0),
            likedByUids = decodeUidList(j.optString("liked_by_uids")),
            commentCount = j.optInt("comment_count", 0),
            contentReportCount = effectiveContentCount,
            contentReportedByUids = effectiveContentUids,
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
            updatedAt = parsePocketBaseTimestamp(j.optString("updated")),
        )
    }

    private fun mapMomentComment(j: JSONObject): MomentComment = MomentComment(
        id = j.optString("id"),
        momentId = j.optString("moment_id"),
        authorId = j.optString("author_id"),
        authorName = j.optString("author_name", "Warga Surabaya"),
        text = j.optString("text"),
        createdAt = parsePocketBaseTimestamp(j.optString("created")),
    )

    private fun mapDestination(j: JSONObject): SurabayaDestination {
        val id = j.optString("id")
        val photoFileNames = extractFileNames(j, "photo")
        val resolvedPhotoUrls = photoFileNames
            .map { pb.buildFileUrl("destinations", id, it) }
            .filter { it.isNotBlank() }

        val serverContentCount = j.optInt("content_report_count", 0)
        val serverContentUids = decodeUidList(j.optString("content_reported_uids"))
        val overlay = localContentReports["destinations:$id"]
        val effectiveContentCount = when {
            overlay == null -> serverContentCount
            overlay.forceOverride -> overlay.count
            else -> maxOf(serverContentCount, overlay.count)
        }
        val effectiveContentUids = when {
            overlay == null -> serverContentUids
            overlay.forceOverride -> overlay.uids
            else -> (serverContentUids + overlay.uids).distinct()
        }

        val districtName = j.optString("district", "Genteng").ifBlank { "Genteng" }
        val sanitizedMapsUrl = LinkSecurityValidator.sanitizeOrEmpty(j.optString("maps_url"), SafeLinkCategory.GOOGLE_MAPS)
        val rawLat = j.optDouble("lat", Double.NaN)
        val rawLng = j.optDouble("lng", Double.NaN)
        val hasValidStoredLatLng = !rawLat.isNaN() && !rawLng.isNaN() && (rawLat != 0.0 || rawLng != 0.0)
        val (resolvedLat, resolvedLng) = extractLatLngFromMapsUrl(sanitizedMapsUrl)
            ?: if (hasValidStoredLatLng) {
                rawLat to rawLng
            } else {
                SurabayaDistricts.coordinateFor(districtName)
            }

        return SurabayaDestination(
            id = id,
            createdBy = j.optString("created_by"),
            authorName = j.optString("author_name", "Admin Resmi Surabaya"),
            title = j.optString("title"),
            category = j.optString("category", "WISATA_SEJARAH"),
            description = j.optString("description"),
            district = districtName,
            address = j.optString("address"),
            ticketPrice = j.optString("ticket_price", "Gratis"),
            operationalHours = j.optString("operational_hours", "24 Jam"),
            bestTime = j.optString("best_time", "Sore hari"),
            lat = resolvedLat,
            lng = resolvedLng,
            imageKey = j.optString("image_key", "hero_surabaya"),
            photoUrl = resolvedPhotoUrls.firstOrNull().orEmpty(),
            photoUrls = resolvedPhotoUrls,
            mapsUrl = sanitizedMapsUrl,
            ratingAvg = j.optDouble("rating_avg", 4.8),
            reviewCount = j.optInt("review_count", 0),
            linkReportCount = j.optInt("link_report_count", 0),
            linkReportedByUids = decodeUidList(j.optString("link_reported_uids")),
            contentReportCount = effectiveContentCount,
            contentReportedByUids = effectiveContentUids,
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
            updatedAt = parsePocketBaseTimestamp(j.optString("updated")),
        )
    }

    private fun mapDestinationReview(j: JSONObject): DestinationReview = DestinationReview(
        id = j.optString("id"),
        destinationId = j.optString("destination_id"),
        authorId = j.optString("author_id"),
        authorName = j.optString("author_name", "Warga Surabaya"),
        rating = j.optInt("rating", 5),
        comment = j.optString("comment"),
        createdAt = parsePocketBaseTimestamp(j.optString("created")),
    )

    private fun mapCitizenService(j: JSONObject): CitizenService {
        val id = j.optString("id")
        val remotePhoto = j.optString("flyer_photo_url").trim()
        val photoFileNames = extractFileNames(j, "flyer_photos")
        val resolvedPhotoUrls = photoFileNames
            .map { fileName ->
                if (fileName.startsWith("http") || fileName.startsWith("data:")) {
                    fileName
                } else {
                    pb.buildFileUrl("services", id, fileName)
                }
            }
            .filter { it.isNotBlank() }
            .toMutableList()
        if (resolvedPhotoUrls.isEmpty() && remotePhoto.isNotBlank()) {
            resolvedPhotoUrls.add(
                if (remotePhoto.startsWith("http") || remotePhoto.startsWith("data:")) {
                    remotePhoto
                } else {
                    pb.buildFileUrl("services", id, remotePhoto)
                },
            )
        }

        val overlay = localContentReports["services:$id"]
        val serverContentCount = j.optInt("content_report_count", 0)
        val serverContentUids = decodeUidList(j.optString("content_reported_uids"))
        val effectiveContentCount = when {
            overlay == null -> serverContentCount
            overlay.forceOverride -> overlay.count
            else -> serverContentCount + overlay.count
        }
        val effectiveContentUids = when {
            overlay == null -> serverContentUids
            overlay.forceOverride -> overlay.uids
            else -> (serverContentUids + overlay.uids).distinct()
        }

        return CitizenService(
            id = id,
            providerId = j.optString("provider_id"),
            providerName = j.optString("provider_name", "Warga Surabaya"),
            title = j.optString("title"),
            category = j.optString("category", "ELEKTRONIK_AC"),
            price = j.optLong("price", 75000L),
            priceUnit = j.optString("price_unit", "unit"),
            district = j.optString("district", "Gubeng"),
            coverageArea = j.optString("coverage_area", "Seluruh Surabaya & Sekitarnya"),
            phoneNumber = j.optString("phone_number"),
            whatsappNumber = j.optString("whatsapp_number"),
            description = j.optString("description"),
            flyerPhotoUrl = resolvedPhotoUrls.firstOrNull().orEmpty(),
            flyerPhotoUrls = resolvedPhotoUrls,
            ratingAvg = j.optDouble("rating_avg", 4.9),
            reviewCount = j.optInt("review_count", 0),
            verifiedBadge = j.optBoolean("verified_badge", true),
            contentReportCount = effectiveContentCount,
            contentReportedByUids = effectiveContentUids,
            isBanned = j.optBoolean("is_banned", false),
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
            updatedAt = parsePocketBaseTimestamp(j.optString("updated")),
        )
    }

    fun observeCitizenServices(): Flow<List<CitizenService>> {
        return combine(
            pb.observeCollection("services", pollIntervalMs = 8_000L) {
                try {
                    pb.getList(collection = "services", sort = "-created")
                } catch (e: Exception) {
                    emptyList()
                }
            },
            localMutationTick,
        ) { remoteList, _ ->
            val remoteServices = remoteList.map(::mapCitizenService)
            val combinedMap = inMemoryServices.toMutableMap()
            for (s in remoteServices) {
                combinedMap[s.id] = s
            }
            combinedMap.values
                .filter { "services:${it.id}" !in localDeletedRecords && it.providerId !in localBannedUids }
                .sortedByDescending { it.createdAt ?: 0L }
        }.catch { e ->
            Log.w(TAG, "observeCitizenServices fallback: ${e.message}")
            emit(inMemoryServices.values.filter { "services:${it.id}" !in localDeletedRecords }.sortedByDescending { it.createdAt ?: 0L })
        }
    }

    suspend fun createCitizenService(
        providerName: String,
        title: String,
        category: String,
        price: Long,
        priceUnit: String,
        district: String,
        coverageArea: String,
        phoneNumber: String,
        whatsappNumber: String,
        description: String,
        flyerPhoto: PreparedPhoto? = null,
        flyerPhotos: List<PreparedPhoto> = emptyList(),
    ): String {
        val uid = requireUserId()
        val cleanPhone = phoneNumber.trim().replace(Regex("[^0-9+]"), "").take(25)
        val cleanWa = (if (whatsappNumber.isNotBlank()) whatsappNumber else phoneNumber)
            .trim().replace(Regex("[^0-9]"), "")
            .let { if (it.startsWith("0")) "62" + it.drop(1) else it }
            .take(25)

        val spamCheck = SurabayaSpamProtector.checkServiceDuplicate(
            userId = uid,
            title = title,
            description = description,
            phoneNumber = cleanPhone,
            whatsappNumber = cleanWa,
            existingServices = inMemoryServices.values.toList(),
        )
        if (spamCheck is SpamCheckResult.Blocked) {
            throw DuplicateContentException(spamCheck.reason)
        }

        val recordId = PocketBaseClient.deterministicPbId("srv_${UUID.randomUUID()}")
        val boundedPhotos = when {
            flyerPhotos.isNotEmpty() -> flyerPhotos.take(3)
            flyerPhoto != null -> listOf(flyerPhoto)
            else -> emptyList()
        }

        val newService = CitizenService(
            id = recordId,
            providerId = uid,
            providerName = providerName.trim().take(80).ifEmpty { "Warga Surabaya" },
            title = title.trim().take(120),
            category = category.trim().ifEmpty { "ELEKTRONIK_AC" },
            price = price.coerceAtLeast(0L),
            priceUnit = priceUnit.trim().take(30).ifEmpty { "unit" },
            district = district.trim().ifEmpty { "Gubeng" },
            coverageArea = coverageArea.trim().take(150).ifEmpty { "Seluruh Surabaya" },
            phoneNumber = cleanPhone,
            whatsappNumber = cleanWa,
            description = description.trim().take(1200),
            flyerPhotoUrl = boundedPhotos.firstOrNull()?.dataUri.orEmpty(),
            flyerPhotoUrls = boundedPhotos.map { it.dataUri },
            ratingAvg = 5.0,
            reviewCount = 1,
            verifiedBadge = true,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )

        inMemoryServices[recordId] = newService
        SurabayaSpamProtector.recordSubmission(uid, "services")
        bumpMutationTick()

        try {
            val body = JSONObject().apply {
                put("id", recordId)
                put("provider_id", uid)
                put("provider_name", newService.providerName)
                put("title", newService.title)
                put("category", newService.category)
                put("price", newService.price)
                put("price_unit", newService.priceUnit)
                put("district", newService.district)
                put("coverage_area", newService.coverageArea)
                put("phone_number", newService.phoneNumber)
                put("whatsapp_number", newService.whatsappNumber)
                put("description", newService.description)
                put("rating_avg", 5.0)
                put("review_count", 1)
                put("verified_badge", true)
            }
            val fields = mapOf(
                "id" to recordId,
                "provider_id" to uid,
                "provider_name" to newService.providerName,
                "title" to newService.title,
                "category" to newService.category,
                "price" to newService.price.toString(),
                "price_unit" to newService.priceUnit,
                "district" to newService.district,
                "coverage_area" to newService.coverageArea,
                "phone_number" to newService.phoneNumber,
                "whatsapp_number" to newService.whatsappNumber,
                "description" to newService.description,
                "rating_avg" to "5.0",
                "review_count" to "1",
                "verified_badge" to "true",
            )
            val fileParts = boundedPhotos.mapIndexed { idx, p ->
                MultipartFilePart(
                    fieldName = "flyer_photos",
                    fileName = if (boundedPhotos.size > 1) "${idx + 1}_${p.fileName}" else p.fileName,
                    mimeType = p.mimeType,
                    bytes = p.bytes,
                )
            }
            try {
                if (fileParts.isNotEmpty()) {
                    pb.createRecordMultipartFiles("services", fields, fileParts)
                } else {
                    pb.createRecord("services", body)
                }
            } catch (e: Exception) {
                Log.w(TAG, "PocketBase services remote creation warning: ${e.message}")
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.w(TAG, "createCitizenService remote failed, kept in local state", e)
        }

        return recordId
    }

    suspend fun reportServiceContent(serviceId: String, reporterUid: String) {
        val current = inMemoryServices[serviceId]
        val currentCount = current?.contentReportCount ?: 0
        val currentUids = current?.contentReportedByUids ?: emptyList()
        reportInappropriateContent(
            collection = "services",
            recordId = serviceId,
            currentCount = currentCount,
            currentReportedUids = currentUids,
        )
        if (current != null) {
            val updated = current.copy(
                contentReportCount = current.contentReportCount + 1,
                contentReportedByUids = (current.contentReportedByUids + reporterUid).distinct(),
            )
            inMemoryServices[serviceId] = updated
            bumpMutationTick()
        }
    }

    suspend fun deleteCitizenService(serviceId: String, requesterUid: String, isAdmin: Boolean) {
        val s = inMemoryServices[serviceId]
        if (s != null && (s.providerId == requesterUid || isAdmin)) {
            inMemoryServices.remove(serviceId)
            localDeletedRecords.add("services:$serviceId")
            bumpMutationTick()
            try {
                pb.deleteRecord("services", serviceId)
            } catch (e: Exception) {
                Log.w(TAG, "Remote delete service warning: ${e.message}")
            }
        }
    }

    // --- 9. POLLING WARGA & SUARA RAKYAT ---

    private fun encodePollOptions(options: List<PollOption>): String {
        val arr = JSONArray()
        for (opt in options) {
            arr.put(
                JSONObject().apply {
                    put("id", opt.id)
                    put("text", opt.text)
                    put("voteCount", opt.voteCount)
                },
            )
        }
        return arr.toString()
    }

    private fun decodePollOptions(raw: String): List<PollOption> {
        if (raw.isBlank()) return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList(arr.length()) {
                for (i in 0 until arr.length()) {
                    val obj = arr.optJSONObject(i) ?: continue
                    val id = obj.optString("id").ifBlank { "opt_${i + 1}" }
                    val text = obj.optString("text")
                    val voteCount = obj.optInt("voteCount", 0)
                    if (text.isNotBlank()) {
                        add(PollOption(id = id, text = text, voteCount = voteCount))
                    }
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun encodeVotesByUser(votes: Map<String, String>): String {
        val obj = JSONObject()
        for ((uid, optId) in votes) {
            if (uid.isNotBlank() && optId.isNotBlank()) {
                obj.put(uid, optId)
            }
        }
        return obj.toString()
    }

    private fun decodeVotesByUser(raw: String): Map<String, String> {
        if (raw.isBlank()) return emptyMap()
        return try {
            val obj = JSONObject(raw)
            val result = mutableMapOf<String, String>()
            val keys = obj.keys()
            while (keys.hasNext()) {
                val k = keys.next()
                val v = obj.optString(k)
                if (k.isNotBlank() && v.isNotBlank()) {
                    result[k] = v
                }
            }
            result
        } catch (_: Exception) {
            emptyMap()
        }
    }

    private fun mapPoll(j: JSONObject): CitizenPoll {
        val id = j.optString("id")
        val options = decodePollOptions(j.optString("options_json"))
        val votesMap = decodeVotesByUser(j.optString("votes_by_user_json"))
        val voterUids = (decodeUidList(j.optString("voter_uids")) + votesMap.keys).distinct()
        val computedTotal = options.sumOf { it.voteCount }
        val storedTotal = j.optInt("total_votes", computedTotal)
        return CitizenPoll(
            id = id,
            proposerId = j.optString("proposer_id"),
            proposerName = j.optString("proposer_name", "Warga Surabaya"),
            title = j.optString("title"),
            description = j.optString("description"),
            category = j.optString("category", "UMUM").ifBlank { "UMUM" },
            district = j.optString("district", "Genteng").ifBlank { "Genteng" },
            status = j.optString("status", "PENDING").ifBlank { "PENDING" },
            adminNote = j.optString("admin_note"),
            options = options,
            totalVotes = maxOf(storedTotal, computedTotal),
            voterUids = voterUids,
            votesByUser = votesMap,
            commentCount = j.optInt("comment_count", 0),
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
            updatedAt = parsePocketBaseTimestamp(j.optString("updated")),
        )
    }

    private fun mapPollComment(j: JSONObject): PollComment = PollComment(
        id = j.optString("id"),
        pollId = j.optString("poll_id"),
        authorId = j.optString("author_id"),
        authorName = j.optString("author_name", "Warga Surabaya"),
        authorDistrict = j.optString("author_district", "Genteng").ifBlank { "Genteng" },
        text = j.optString("text"),
        createdAt = parsePocketBaseTimestamp(j.optString("created")),
    )

    fun observeApprovedPolls(): Flow<List<CitizenPoll>> {
        return combine(
            pb.observeCollection("polls", pollIntervalMs = 6_000L) {
                try {
                    pb.getList(collection = "polls", filter = "status = 'APPROVED'", sort = "-created")
                } catch (_: Exception) {
                    emptyList()
                }
            },
            localMutationTick,
        ) { remoteList, _ ->
            val combinedMap = inMemoryPolls.toMutableMap()
            for (item in remoteList) {
                val p = mapPoll(item)
                combinedMap[p.id] = p
                inMemoryPolls[p.id] = p
            }
            combinedMap.values
                .filter { it.status == "APPROVED" && "polls:${it.id}" !in localDeletedRecords }
                .sortedByDescending { it.createdAt ?: 0L }
        }.catch {
            emit(
                inMemoryPolls.values
                    .filter { it.status == "APPROVED" && "polls:${it.id}" !in localDeletedRecords }
                    .sortedByDescending { it.createdAt ?: 0L },
            )
        }
    }

    fun observePendingPolls(): Flow<List<CitizenPoll>> {
        return combine(
            pb.observeCollection("polls", pollIntervalMs = 6_000L) {
                try {
                    pb.getList(collection = "polls", filter = "status = 'PENDING'", sort = "-created")
                } catch (_: Exception) {
                    emptyList()
                }
            },
            localMutationTick,
        ) { remoteList, _ ->
            val combinedMap = inMemoryPolls.toMutableMap()
            for (item in remoteList) {
                val p = mapPoll(item)
                combinedMap[p.id] = p
                inMemoryPolls[p.id] = p
            }
            combinedMap.values
                .filter { it.status == "PENDING" && "polls:${it.id}" !in localDeletedRecords }
                .sortedByDescending { it.createdAt ?: 0L }
        }.catch {
            emit(
                inMemoryPolls.values
                    .filter { it.status == "PENDING" && "polls:${it.id}" !in localDeletedRecords }
                    .sortedByDescending { it.createdAt ?: 0L },
            )
        }
    }

    fun observeMyProposedPolls(userId: String): Flow<List<CitizenPoll>> {
        val safeUid = escapePbValue(userId)
        return combine(
            pb.observeCollection("polls", pollIntervalMs = 6_000L) {
                try {
                    pb.getList(collection = "polls", filter = "proposer_id = '$safeUid'", sort = "-created")
                } catch (_: Exception) {
                    emptyList()
                }
            },
            localMutationTick,
        ) { remoteList, _ ->
            val combinedMap = inMemoryPolls.toMutableMap()
            for (item in remoteList) {
                val p = mapPoll(item)
                combinedMap[p.id] = p
                inMemoryPolls[p.id] = p
            }
            combinedMap.values
                .filter { it.proposerId == userId && "polls:${it.id}" !in localDeletedRecords }
                .sortedByDescending { it.createdAt ?: 0L }
        }.catch {
            emit(
                inMemoryPolls.values
                    .filter { it.proposerId == userId && "polls:${it.id}" !in localDeletedRecords }
                    .sortedByDescending { it.createdAt ?: 0L },
            )
        }
    }

    fun observePollById(pollId: String): Flow<CitizenPoll?> {
        return combine(
            pb.observeCollection("polls", pollIntervalMs = 5_000L) {
                try {
                    pb.getOneOrNull("polls", pollId)
                } catch (_: Exception) {
                    null
                }
            },
            localMutationTick,
        ) { remoteJson, _ ->
            if ("polls:$pollId" in localDeletedRecords) {
                null
            } else if (remoteJson != null) {
                val mapped = mapPoll(remoteJson)
                inMemoryPolls[pollId] = mapped
                mapped
            } else {
                inMemoryPolls[pollId]
            }
        }.catch {
            emit(inMemoryPolls[pollId]?.takeIf { "polls:${it.id}" !in localDeletedRecords })
        }
    }

    suspend fun proposePoll(
        title: String,
        description: String,
        category: String,
        district: String,
        optionTexts: List<String>,
    ): String {
        val uid = requireUserId()
        val account = runCatching { pb.getOneOrNull("users", uid) }.getOrNull()
        val proposerName = account?.optString("name")?.takeIf { it.isNotBlank() }
            ?: pb.currentSession?.displayName?.takeIf { it.isNotBlank() }
            ?: "Warga Surabaya"

        val cleanTitle = title.trim().take(150)
        val cleanDesc = description.trim().take(1500)
        val cleanDistrict = district.trim().ifEmpty { "Genteng" }
        val cleanCategory = category.trim().ifEmpty { "UMUM" }

        if (cleanTitle.length < 5) {
            throw IllegalArgumentException("Judul usulan polling minimal 5 karakter.")
        }

        val validOptions = optionTexts
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .take(5)

        if (validOptions.size < 2) {
            throw IllegalArgumentException("Usulan polling minimal wajib memiliki 2 opsi pilihan.")
        }

        val pollId = PocketBaseClient.deterministicPbId("poll_${UUID.randomUUID()}")
        val opts = validOptions.mapIndexed { idx, optText ->
            PollOption(
                id = "opt_${idx + 1}_" + UUID.randomUUID().toString().replace("-", "").take(6),
                text = optText.take(120),
                voteCount = 0,
            )
        }

        val newPoll = CitizenPoll(
            id = pollId,
            proposerId = uid,
            proposerName = proposerName,
            title = cleanTitle,
            description = cleanDesc,
            category = cleanCategory,
            district = cleanDistrict,
            status = "PENDING",
            options = opts,
            totalVotes = 0,
            voterUids = emptyList(),
            votesByUser = emptyMap(),
            commentCount = 0,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )

        inMemoryPolls[pollId] = newPoll
        bumpMutationTick()

        try {
            val payload = JSONObject().apply {
                put("id", pollId)
                put("proposer_id", uid)
                put("proposer_name", proposerName.take(80))
                put("title", cleanTitle)
                put("description", cleanDesc)
                put("category", cleanCategory.take(40))
                put("district", cleanDistrict.take(60))
                put("status", "PENDING")
                put("admin_note", "")
                put("options_json", encodePollOptions(opts))
                put("total_votes", 0)
                put("voter_uids", "")
                put("votes_by_user_json", "{}")
                put("comment_count", 0)
            }
            pb.createRecord("polls", payload)
        } catch (e: Exception) {
            Log.w(TAG, "proposePoll PB save fallback: ${e.message}")
        }

        return pollId
    }

    suspend fun moderatePoll(pollId: String, approve: Boolean, adminNote: String = "") {
        val nextStatus = if (approve) "APPROVED" else "REJECTED"
        val cleanNote = adminNote.trim().take(300)
        val current = inMemoryPolls[pollId]
        if (current != null) {
            inMemoryPolls[pollId] = current.copy(
                status = nextStatus,
                adminNote = cleanNote,
                updatedAt = System.currentTimeMillis(),
            )
            bumpMutationTick()
        }
        try {
            pb.updateRecord("polls", pollId, JSONObject().apply {
                put("status", nextStatus)
                put("admin_note", cleanNote)
            })
        } catch (e: Exception) {
            Log.w(TAG, "moderatePoll PB update fallback: ${e.message}")
        }
    }

    suspend fun votePoll(pollId: String, optionId: String): Boolean {
        val uid = requireUserId()
        val current = inMemoryPolls[pollId]
            ?: runCatching { pb.getOneOrNull("polls", pollId)?.let(::mapPoll) }.getOrNull()
            ?: return false
        if (current.status != "APPROVED") {
            throw IllegalStateException("Polling ini belum disetujui atau sudah ditutup.")
        }

        val existingVote = current.votesByUser[uid]
        if (existingVote == optionId) {
            return true
        }

        val updatedOptions = current.options.map { opt ->
            when {
                opt.id == optionId -> opt.copy(voteCount = opt.voteCount + 1)
                existingVote != null && opt.id == existingVote -> opt.copy(voteCount = (opt.voteCount - 1).coerceAtLeast(0))
                else -> opt
            }
        }

        val newVotesMap = current.votesByUser.toMutableMap().apply {
            put(uid, optionId)
        }
        val newVoterUids = (current.voterUids + uid).distinct()
        val newTotalVotes = updatedOptions.sumOf { it.voteCount }

        val updatedPoll = current.copy(
            options = updatedOptions,
            totalVotes = newTotalVotes,
            voterUids = newVoterUids,
            votesByUser = newVotesMap,
            updatedAt = System.currentTimeMillis(),
        )

        inMemoryPolls[pollId] = updatedPoll
        bumpMutationTick()

        try {
            val patch = JSONObject().apply {
                put("options_json", encodePollOptions(updatedOptions))
                put("total_votes", newTotalVotes)
                put("voter_uids", encodeUidList(newVoterUids))
                put("votes_by_user_json", encodeVotesByUser(newVotesMap))
            }
            try {
                pb.updateRecord("polls", pollId, patch)
            } catch (e: PocketBaseException) {
                if (e.statusCode == 404) {
                    val createFromSeed = JSONObject().apply {
                        put("id", PocketBaseClient.deterministicPbId(pollId).takeIf { pollId.length != 15 } ?: pollId)
                        put("proposer_id", uid)
                        put("proposer_name", updatedPoll.proposerName.take(80))
                        put("title", updatedPoll.title.take(160))
                        put("description", updatedPoll.description.take(1600))
                        put("category", updatedPoll.category.take(40))
                        put("district", updatedPoll.district.take(60))
                        put("status", updatedPoll.status)
                        put("admin_note", updatedPoll.adminNote.take(300))
                        put("options_json", encodePollOptions(updatedOptions))
                        put("total_votes", newTotalVotes)
                        put("voter_uids", encodeUidList(newVoterUids))
                        put("votes_by_user_json", encodeVotesByUser(newVotesMap))
                        put("comment_count", updatedPoll.commentCount)
                    }
                    runCatching { pb.createRecord("polls", createFromSeed) }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "votePoll PB sync fallback: ${e.message}")
        }

        return true
    }

    fun observePollComments(pollId: String): Flow<List<PollComment>> {
        val safePollId = escapePbValue(pollId)
        return combine(
            pb.observeCollection("poll_comments", pollIntervalMs = 5_000L) {
                try {
                    pb.getList(collection = "poll_comments", filter = "poll_id = '$safePollId'", sort = "-created")
                } catch (_: Exception) {
                    emptyList()
                }
            },
            localMutationTick,
        ) { remoteList, _ ->
            val remote = remoteList.map(::mapPollComment)
            val localList = inMemoryPollComments[pollId]?.toList() ?: emptyList()
            (remote + localList).distinctBy { it.id }.sortedByDescending { it.createdAt ?: 0L }
        }.catch {
            emit(inMemoryPollComments[pollId]?.toList()?.sortedByDescending { it.createdAt ?: 0L } ?: emptyList())
        }
    }

    suspend fun addPollComment(pollId: String, text: String): String {
        val uid = requireUserId()
        val cleanText = text.trim().take(500)
        if (cleanText.isBlank()) {
            throw IllegalArgumentException("Komentar warga tidak boleh kosong.")
        }
        val account = runCatching { pb.getOneOrNull("users", uid) }.getOrNull()
        val authorName = account?.optString("name")?.takeIf { it.isNotBlank() }
            ?: pb.currentSession?.displayName?.takeIf { it.isNotBlank() }
            ?: "Warga Surabaya"
        val authorDistrict = account?.optString("district")?.takeIf { it.isNotBlank() }
            ?: pb.currentSession?.district?.takeIf { it.isNotBlank() }
            ?: "Genteng"

        val commentId = PocketBaseClient.deterministicPbId("pcom_${UUID.randomUUID()}")
        val newComment = PollComment(
            id = commentId,
            pollId = pollId,
            authorId = uid,
            authorName = authorName,
            authorDistrict = authorDistrict,
            text = cleanText,
            createdAt = System.currentTimeMillis(),
        )

        val list = inMemoryPollComments.computeIfAbsent(pollId) { CopyOnWriteArrayList() }
        list.add(newComment)

        val nextCount = (inMemoryPolls[pollId]?.commentCount ?: (list.size - 1)) + 1
        inMemoryPolls[pollId]?.let { poll ->
            inMemoryPolls[pollId] = poll.copy(
                commentCount = nextCount,
                updatedAt = System.currentTimeMillis(),
            )
        }
        bumpMutationTick()

        try {
            val payload = JSONObject().apply {
                put("id", commentId)
                put("poll_id", pollId)
                put("author_id", uid)
                put("author_name", authorName.take(80))
                put("author_district", authorDistrict.take(60))
                put("text", cleanText)
            }
            pb.createRecord("poll_comments", payload)
            runCatching {
                pb.updateRecord("polls", pollId, JSONObject().apply {
                    put("comment_count", nextCount)
                })
            }
        } catch (e: Exception) {
            Log.w(TAG, "addPollComment PB sync fallback: ${e.message}")
        }

        return commentId
    }

    suspend fun deletePoll(pollId: String, requesterUid: String, isAdmin: Boolean) {
        val p = inMemoryPolls[pollId]
        if (p == null || p.proposerId == requesterUid || isAdmin) {
            inMemoryPolls.remove(pollId)
            inMemoryPollComments.remove(pollId)
            localDeletedRecords.add("polls:$pollId")
            bumpMutationTick()
            runCatching { pb.deleteRecord("polls", pollId) }
        }
    }

    // --- TAMBAL BAN TERDEKAT (SALAM SATU ASPAL) OPERATIONS ---

    private fun mapTambalBanSpot(j: JSONObject): TambalBanSpot {
        val id = j.optString("id")
        val districtName = j.optString("district", "Genteng").ifBlank { "Genteng" }
        val sanitizedMapsUrl = LinkSecurityValidator.sanitizeOrEmpty(j.optString("maps_url"), SafeLinkCategory.GOOGLE_MAPS)
        val rawLat = j.optDouble("lat", Double.NaN)
        val rawLng = j.optDouble("lng", Double.NaN)
        val hasValidStoredLatLng = !rawLat.isNaN() && !rawLng.isNaN() && (rawLat != 0.0 || rawLng != 0.0)
        val (resolvedLat, resolvedLng) = extractLatLngFromMapsUrl(sanitizedMapsUrl)
            ?: if (hasValidStoredLatLng) {
                rawLat to rawLng
            } else {
                SurabayaDistricts.coordinateFor(districtName)
            }
        val photoFile = extractFileNames(j, "photo").firstOrNull()
        val resolvedPhotoUrl = when {
            !photoFile.isNullOrBlank() -> pb.buildFileUrl("tambal_ban", id, photoFile)
            else -> j.optString("photo_url")
        }
        val verifiedUids = decodeUidList(j.optString("verified_uids"))
        return TambalBanSpot(
            id = id,
            authorId = j.optString("author_id"),
            authorName = j.optString("author_name", "Warga Surabaya"),
            name = j.optString("name"),
            description = j.optString("description"),
            district = districtName,
            address = j.optString("address"),
            latitude = resolvedLat,
            longitude = resolvedLng,
            is24Hours = j.optBoolean("is_24_hours", false),
            isTubeless = j.optBoolean("is_tubeless", true),
            forMotor = j.optBoolean("for_motor", true),
            forMobil = j.optBoolean("for_mobil", false),
            canCallOut = j.optBoolean("can_call_out", false),
            photoUrl = resolvedPhotoUrl,
            mapsUrl = sanitizedMapsUrl,
            phoneNumber = j.optString("phone_number"),
            whatsappNumber = j.optString("whatsapp_number"),
            verifiedCount = maxOf(j.optInt("verified_count", 0), verifiedUids.size),
            verifiedUserIds = verifiedUids,
            commentCount = j.optInt("comment_count", 0),
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
            updatedAt = parsePocketBaseTimestamp(j.optString("updated")),
        )
    }

    private fun mapTambalBanComment(j: JSONObject): TambalBanComment = TambalBanComment(
        id = j.optString("id"),
        spotId = j.optString("spot_id"),
        authorId = j.optString("author_id"),
        authorName = j.optString("author_name", "Warga Surabaya"),
        authorDistrict = j.optString("author_district", "Genteng").ifBlank { "Genteng" },
        text = j.optString("text"),
        createdAt = parsePocketBaseTimestamp(j.optString("created")),
    )

    fun observeTambalBanSpots(): Flow<List<TambalBanSpot>> {
        return combine(
            pb.observeCollection("tambal_ban", pollIntervalMs = 7_000L) {
                try {
                    pb.getList(collection = "tambal_ban", sort = "-created")
                } catch (_: Exception) {
                    emptyList()
                }
            },
            localMutationTick,
        ) { remoteList, _ ->
            val combinedMap = inMemoryTambalBan.toMutableMap()
            for (item in remoteList) {
                val spot = mapTambalBanSpot(item)
                combinedMap[spot.id] = spot
                inMemoryTambalBan[spot.id] = spot
            }
            combinedMap.values
                .filter { "tambal_ban:${it.id}" !in localDeletedRecords && it.authorId !in localBannedUids }
                .sortedByDescending { it.verifiedCount * 10 + (it.commentCount * 2) }
        }.catch {
            emit(
                inMemoryTambalBan.values
                    .filter { "tambal_ban:${it.id}" !in localDeletedRecords }
                    .sortedByDescending { it.verifiedCount * 10 + (it.commentCount * 2) },
            )
        }
    }

    fun observeTambalBanById(spotId: String): Flow<TambalBanSpot?> {
        return combine(
            pb.observeCollection("tambal_ban", pollIntervalMs = 6_000L) {
                try {
                    pb.getOneOrNull("tambal_ban", spotId)
                } catch (_: Exception) {
                    null
                }
            },
            localMutationTick,
        ) { remoteJson, _ ->
            if ("tambal_ban:$spotId" in localDeletedRecords) {
                null
            } else if (remoteJson != null) {
                val mapped = mapTambalBanSpot(remoteJson)
                inMemoryTambalBan[spotId] = mapped
                mapped
            } else {
                inMemoryTambalBan[spotId]
            }
        }.catch {
            emit(inMemoryTambalBan[spotId]?.takeIf { "tambal_ban:${it.id}" !in localDeletedRecords })
        }
    }

    fun observeTambalBanComments(spotId: String): Flow<List<TambalBanComment>> {
        val safeSpotId = escapePbValue(spotId)
        return combine(
            pb.observeCollection("tambal_ban_comments", pollIntervalMs = 5_000L) {
                try {
                    pb.getList(collection = "tambal_ban_comments", filter = "spot_id = '$safeSpotId'", sort = "-created")
                } catch (_: Exception) {
                    emptyList()
                }
            },
            localMutationTick,
        ) { remoteList, _ ->
            val remote = remoteList.map(::mapTambalBanComment)
            val localList = inMemoryTambalBanComments[spotId]?.toList() ?: emptyList()
            (remote + localList).distinctBy { it.id }.sortedByDescending { it.createdAt ?: 0L }
        }.catch {
            emit(inMemoryTambalBanComments[spotId]?.toList()?.sortedByDescending { it.createdAt ?: 0L } ?: emptyList())
        }
    }

    suspend fun addTambalBanSpot(
        name: String,
        description: String,
        district: String,
        address: String,
        is24Hours: Boolean,
        isTubeless: Boolean,
        forMotor: Boolean,
        forMobil: Boolean,
        canCallOut: Boolean,
        mapsUrl: String = "",
        phoneNumber: String = "",
        whatsappNumber: String = "",
        photo: PreparedPhoto? = null,
    ): String {
        val uid = requireUserId()
        val authorName = pb.currentSession?.displayName?.ifBlank { "Warga Surabaya" } ?: "Warga Surabaya"

        if (name.trim().length < 4) {
            throw IllegalArgumentException("Nama lokasi tambal ban minimal 4 karakter.")
        }
        if (address.trim().length < 5) {
            throw IllegalArgumentException("Alamat lokasi tambal ban minimal 5 karakter.")
        }

        val cleanMapsUrl = if (mapsUrl.isNotBlank()) {
            requireSafeOptionalUrl(mapsUrl, SafeLinkCategory.GOOGLE_MAPS)
        } else {
            ""
        }

        val spotId = PocketBaseClient.deterministicPbId("tb_${UUID.randomUUID()}")
        val cleanName = name.trim().take(120)
        val cleanDesc = description.trim().take(1000)
        val cleanDistrict = district.trim().ifEmpty { "Genteng" }.take(60)
        val cleanAddress = address.trim().take(200)
        val cleanPhone = phoneNumber.trim().take(30)
        val cleanWa = whatsappNumber.trim().take(30)
        val (lat, lng) = extractLatLngFromMapsUrl(cleanMapsUrl) ?: SurabayaDistricts.coordinateFor(cleanDistrict)
        val now = System.currentTimeMillis()

        var uploadedPhotoUrl = photo?.dataUri.orEmpty()
        try {
            if (photo != null) {
                val fields = mapOf(
                    "id" to spotId,
                    "author_id" to uid,
                    "author_name" to authorName.take(80),
                    "name" to cleanName,
                    "description" to cleanDesc,
                    "district" to cleanDistrict,
                    "address" to cleanAddress,
                    "lat" to lat.toString(),
                    "lng" to lng.toString(),
                    "is_24_hours" to is24Hours.toString(),
                    "is_tubeless" to isTubeless.toString(),
                    "for_motor" to forMotor.toString(),
                    "for_mobil" to forMobil.toString(),
                    "can_call_out" to canCallOut.toString(),
                    "maps_url" to cleanMapsUrl,
                    "phone_number" to cleanPhone,
                    "whatsapp_number" to cleanWa,
                    "verified_count" to "1",
                    "verified_uids" to uid,
                    "comment_count" to "0",
                )
                val fileParts = listOf(MultipartFilePart("photo", photo.fileName, photo.mimeType, photo.bytes))
                val created = pb.createRecordMultipartFiles("tambal_ban", fields, fileParts)
                val serverId = created.optString("id").ifBlank { spotId }
                val storedFile = extractFileNames(created, "photo").firstOrNull()
                if (!storedFile.isNullOrBlank()) {
                    uploadedPhotoUrl = pb.buildFileUrl("tambal_ban", serverId, storedFile)
                }
            } else {
                val payload = JSONObject().apply {
                    put("id", spotId)
                    put("author_id", uid)
                    put("author_name", authorName.take(80))
                    put("name", cleanName)
                    put("description", cleanDesc)
                    put("district", cleanDistrict)
                    put("address", cleanAddress)
                    put("lat", lat)
                    put("lng", lng)
                    put("is_24_hours", is24Hours)
                    put("is_tubeless", isTubeless)
                    put("for_motor", forMotor)
                    put("for_mobil", forMobil)
                    put("can_call_out", canCallOut)
                    put("maps_url", cleanMapsUrl)
                    put("phone_number", cleanPhone)
                    put("whatsapp_number", cleanWa)
                    put("verified_count", 1)
                    put("verified_uids", uid)
                    put("comment_count", 0)
                }
                pb.createRecord("tambal_ban", payload)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Gagal simpan lokasi tambal ban ke server, disimpan lokal: ${e.message}")
        }

        val newSpot = TambalBanSpot(
            id = spotId,
            authorId = uid,
            authorName = authorName,
            name = cleanName,
            description = cleanDesc,
            district = cleanDistrict,
            address = cleanAddress,
            latitude = lat,
            longitude = lng,
            is24Hours = is24Hours,
            isTubeless = isTubeless,
            forMotor = forMotor,
            forMobil = forMobil,
            canCallOut = canCallOut,
            photoUrl = uploadedPhotoUrl,
            mapsUrl = cleanMapsUrl,
            phoneNumber = cleanPhone,
            whatsappNumber = cleanWa,
            verifiedCount = 1,
            verifiedUserIds = listOf(uid),
            commentCount = 0,
            createdAt = now,
            updatedAt = now,
        )

        inMemoryTambalBan[spotId] = newSpot
        bumpMutationTick()
        return spotId
    }

    suspend fun verifyTambalBanSpot(spotId: String): Boolean {
        val uid = requireUserId()
        val current = inMemoryTambalBan[spotId]
            ?: runCatching { pb.getOneOrNull("tambal_ban", spotId)?.let(::mapTambalBanSpot) }.getOrNull()
            ?: return false

        val alreadyVerified = uid in current.verifiedUserIds
        val updatedIds = if (alreadyVerified) {
            current.verifiedUserIds - uid
        } else {
            current.verifiedUserIds + uid
        }

        val updated = current.copy(
            verifiedCount = updatedIds.size,
            verifiedUserIds = updatedIds,
            updatedAt = System.currentTimeMillis(),
        )

        inMemoryTambalBan[spotId] = updated
        bumpMutationTick()

        try {
            pb.updateRecord("tambal_ban", spotId, JSONObject().apply {
                put("verified_count", updatedIds.size)
                put("verified_uids", encodeUidList(updatedIds))
            })
        } catch (e: Exception) {
            Log.w(TAG, "verifyTambalBanSpot PB sync fallback: ${e.message}")
        }

        return !alreadyVerified
    }

    suspend fun addTambalBanComment(spotId: String, text: String): String {
        val uid = requireUserId()
        val authorName = pb.currentSession?.displayName?.ifBlank { "Warga Surabaya" } ?: "Warga Surabaya"
        val district = pb.currentSession?.district?.ifBlank { "Genteng" } ?: "Genteng"

        if (text.trim().length < 3) {
            throw IllegalArgumentException("Komentar minimal 3 karakter.")
        }

        val cleanText = text.trim().take(500)
        val cmtId = PocketBaseClient.deterministicPbId("tbc_${UUID.randomUUID()}")
        val now = System.currentTimeMillis()

        val newComment = TambalBanComment(
            id = cmtId,
            spotId = spotId,
            authorId = uid,
            authorName = authorName,
            authorDistrict = district,
            text = cleanText,
            createdAt = now,
        )

        val list = inMemoryTambalBanComments.computeIfAbsent(spotId) { CopyOnWriteArrayList() }
        list.add(0, newComment)

        val nextCount = (inMemoryTambalBan[spotId]?.commentCount ?: (list.size - 1)) + 1
        inMemoryTambalBan[spotId]?.let { spot ->
            inMemoryTambalBan[spotId] = spot.copy(
                commentCount = nextCount,
                updatedAt = now,
            )
        }

        bumpMutationTick()

        try {
            val payload = JSONObject().apply {
                put("id", cmtId)
                put("spot_id", spotId)
                put("author_id", uid)
                put("author_name", authorName.take(80))
                put("author_district", district.take(60))
                put("text", cleanText)
            }
            pb.createRecord("tambal_ban_comments", payload)
            runCatching {
                pb.updateRecord("tambal_ban", spotId, JSONObject().apply {
                    put("comment_count", nextCount)
                })
            }
        } catch (e: Exception) {
            Log.w(TAG, "addTambalBanComment PB sync fallback: ${e.message}")
        }

        return cmtId
    }

    suspend fun deleteTambalBanSpot(spotId: String, requesterUid: String, isAdmin: Boolean) {
        val spot = inMemoryTambalBan[spotId]
        if (spot == null || spot.authorId == requesterUid || isAdmin) {
            inMemoryTambalBan.remove(spotId)
            inMemoryTambalBanComments.remove(spotId)
            localDeletedRecords.add("tambal_ban:$spotId")
            bumpMutationTick()
            runCatching { pb.deleteRecord("tambal_ban", spotId) }
        }
    }

    // --- BARKAS WARGA (JUAL BELI BARANG BEKAS SURABAYA) OPERATIONS ---

    private fun mapBarkasItem(j: JSONObject): BarkasItem {
        val id = j.optString("id")
        val photoFiles = extractFileNames(j, "photos")
        val resolvedPhotos = when {
            photoFiles.isNotEmpty() -> photoFiles.map { pb.buildFileUrl("barkas_items", id, it) }
            else -> j.optString("photo_url")
                .split("|")
                .map { it.trim() }
                .filter { it.isNotBlank() }
        }
        val overlay = localContentReports["barkas_items:$id"]
        val serverContentCount = j.optInt("content_report_count", 0)
        val serverContentUids = decodeUidList(j.optString("content_reported_uids"))
        val effectiveContentCount = when {
            overlay == null -> serverContentCount
            overlay.forceOverride -> overlay.count
            else -> serverContentCount + overlay.count
        }
        val effectiveContentUids = when {
            overlay == null -> serverContentUids
            overlay.forceOverride -> overlay.uids
            else -> (serverContentUids + overlay.uids).distinct()
        }

        return BarkasItem(
            id = id,
            sellerId = j.optString("seller_id"),
            sellerName = j.optString("seller_name", "Warga Surabaya").ifBlank { "Warga Surabaya" },
            title = j.optString("title"),
            category = j.optString("category", "LAINNYA").ifBlank { "LAINNYA" },
            itemCondition = j.optString("item_condition", "BEKAS_TERAWAT").ifBlank { "BEKAS_TERAWAT" },
            price = j.optLong("price", 0L).coerceAtLeast(0L),
            isNegotiable = j.optBoolean("is_negotiable", true),
            district = j.optString("district", "Genteng").ifBlank { "Genteng" },
            codLocation = j.optString("cod_location"),
            description = j.optString("description"),
            whatsappNumber = j.optString("whatsapp_number"),
            photoUrl = resolvedPhotos.firstOrNull().orEmpty(),
            photoUrls = resolvedPhotos,
            isSold = j.optBoolean("is_sold", false),
            commentCount = j.optInt("comment_count", 0),
            contentReportCount = effectiveContentCount,
            contentReportedByUids = effectiveContentUids,
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
            updatedAt = parsePocketBaseTimestamp(j.optString("updated")),
        )
    }

    private fun mapBarkasComment(j: JSONObject): BarkasComment = BarkasComment(
        id = j.optString("id"),
        barkasId = j.optString("barkas_id"),
        authorId = j.optString("author_id"),
        authorName = j.optString("author_name", "Warga Surabaya").ifBlank { "Warga Surabaya" },
        authorDistrict = j.optString("author_district", "Genteng").ifBlank { "Genteng" },
        text = j.optString("text"),
        createdAt = parsePocketBaseTimestamp(j.optString("created")),
    )

    fun observeBarkasItems(): Flow<List<BarkasItem>> {
        return combine(
            pb.observeCollection("barkas_items", pollIntervalMs = 6_500L) {
                try {
                    pb.getList(collection = "barkas_items", sort = "-created")
                } catch (_: Exception) {
                    emptyList()
                }
            },
            localMutationTick,
        ) { remoteList, _ ->
            val combinedMap = inMemoryBarkas.toMutableMap()
            for (json in remoteList) {
                val mapped = mapBarkasItem(json)
                combinedMap[mapped.id] = mapped
                inMemoryBarkas[mapped.id] = mapped
            }
            combinedMap.values
                .filter { "barkas_items:${it.id}" !in localDeletedRecords && it.sellerId !in localBannedUids }
                .sortedWith(
                    compareBy<BarkasItem> { it.isSold }
                        .thenByDescending { it.createdAt ?: 0L },
                )
        }.catch {
            emit(
                inMemoryBarkas.values
                    .filter { "barkas_items:${it.id}" !in localDeletedRecords }
                    .sortedWith(
                        compareBy<BarkasItem> { it.isSold }
                            .thenByDescending { it.createdAt ?: 0L },
                    ),
            )
        }
    }

    fun observeBarkasById(barkasId: String): Flow<BarkasItem?> {
        return combine(
            pb.observeCollection("barkas_items", pollIntervalMs = 5_500L) {
                try {
                    pb.getOneOrNull("barkas_items", barkasId)
                } catch (_: Exception) {
                    null
                }
            },
            localMutationTick,
        ) { remoteJson, _ ->
            if ("barkas_items:$barkasId" in localDeletedRecords) {
                null
            } else if (remoteJson != null) {
                val mapped = mapBarkasItem(remoteJson)
                inMemoryBarkas[barkasId] = mapped
                mapped
            } else {
                inMemoryBarkas[barkasId]
            }
        }.catch {
            emit(inMemoryBarkas[barkasId]?.takeIf { "barkas_items:${it.id}" !in localDeletedRecords })
        }
    }

    fun observeBarkasComments(barkasId: String): Flow<List<BarkasComment>> {
        val safeBarkasId = escapePbValue(barkasId)
        return combine(
            pb.observeCollection("barkas_comments", pollIntervalMs = 4_500L) {
                try {
                    pb.getList(
                        collection = "barkas_comments",
                        filter = "barkas_id = '$safeBarkasId'",
                        sort = "created",
                    )
                } catch (_: Exception) {
                    emptyList()
                }
            },
            localMutationTick,
        ) { remoteList, _ ->
            val remote = remoteList.map(::mapBarkasComment)
            val localList = inMemoryBarkasComments[barkasId]?.toList() ?: emptyList()
            (remote + localList).distinctBy { it.id }.sortedBy { it.createdAt ?: 0L }
        }.catch {
            emit(inMemoryBarkasComments[barkasId]?.toList()?.sortedBy { it.createdAt ?: 0L } ?: emptyList())
        }
    }

    suspend fun createBarkasItem(
        sellerName: String,
        title: String,
        category: String,
        itemCondition: String,
        price: Long,
        isNegotiable: Boolean,
        district: String,
        codLocation: String,
        description: String,
        whatsappNumber: String = "",
        photos: List<PreparedPhoto> = emptyList(),
    ): String {
        val uid = requireUserId()
        val cleanTitle = title.trim().take(140)
        if (cleanTitle.length < 3) {
            throw IllegalArgumentException("Nama barang bekas minimal 3 karakter.")
        }
        val cleanDescription = description.trim().take(1500)
        if (cleanDescription.length < 5) {
            throw IllegalArgumentException("Jelaskan kondisi atau kelengkapan barang minimal 5 karakter.")
        }

        val cleanWa = whatsappNumber
            .trim()
            .replace(Regex("[^0-9]"), "")
            .let { if (it.startsWith("0")) "62" + it.drop(1) else it }
            .take(28)

        val recordId = PocketBaseClient.deterministicPbId("brk_${UUID.randomUUID()}")
        val boundedPhotos = photos.take(4)
        val cleanSellerName = sellerName.trim().take(80).ifEmpty {
            pb.currentSession?.displayName?.ifBlank { "Warga Surabaya" } ?: "Warga Surabaya"
        }
        val cleanCategory = category.trim().uppercase().ifEmpty { "LAINNYA" }.take(40)
        val cleanCondition = itemCondition.trim().uppercase().ifEmpty { "BEKAS_TERAWAT" }.take(40)
        val cleanDistrict = district.trim().ifEmpty { "Genteng" }.take(60)
        val cleanCod = codLocation.trim().take(180)
        val safePrice = price.coerceAtLeast(0L)
        val now = System.currentTimeMillis()

        var resolvedPhotoUrls = boundedPhotos.map { it.dataUri }

        try {
            if (boundedPhotos.isNotEmpty()) {
                val fields = mapOf(
                    "id" to recordId,
                    "seller_id" to uid,
                    "seller_name" to cleanSellerName,
                    "title" to cleanTitle,
                    "category" to cleanCategory,
                    "item_condition" to cleanCondition,
                    "price" to safePrice.toString(),
                    "is_negotiable" to isNegotiable.toString(),
                    "district" to cleanDistrict,
                    "cod_location" to cleanCod,
                    "description" to cleanDescription,
                    "whatsapp_number" to cleanWa,
                    "is_sold" to "false",
                    "comment_count" to "0",
                    "content_report_count" to "0",
                    "content_reported_uids" to "",
                )
                val fileParts = boundedPhotos.mapIndexed { idx, p ->
                    MultipartFilePart(
                        fieldName = "photos",
                        fileName = if (boundedPhotos.size > 1) "${idx + 1}_${p.fileName}" else p.fileName,
                        mimeType = p.mimeType,
                        bytes = p.bytes,
                    )
                }
                val created = pb.createRecordMultipartFiles("barkas_items", fields, fileParts)
                val serverId = created.optString("id").ifBlank { recordId }
                val storedFiles = extractFileNames(created, "photos")
                if (storedFiles.isNotEmpty()) {
                    resolvedPhotoUrls = storedFiles.map { pb.buildFileUrl("barkas_items", serverId, it) }
                }
            } else {
                val payload = JSONObject().apply {
                    put("id", recordId)
                    put("seller_id", uid)
                    put("seller_name", cleanSellerName)
                    put("title", cleanTitle)
                    put("category", cleanCategory)
                    put("item_condition", cleanCondition)
                    put("price", safePrice)
                    put("is_negotiable", isNegotiable)
                    put("district", cleanDistrict)
                    put("cod_location", cleanCod)
                    put("description", cleanDescription)
                    put("whatsapp_number", cleanWa)
                    put("is_sold", false)
                    put("comment_count", 0)
                    put("content_report_count", 0)
                    put("content_reported_uids", "")
                }
                pb.createRecord("barkas_items", payload)
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.w(TAG, "createBarkasItem PB sync fallback: ${e.message}")
        }

        val newItem = BarkasItem(
            id = recordId,
            sellerId = uid,
            sellerName = cleanSellerName,
            title = cleanTitle,
            category = cleanCategory,
            itemCondition = cleanCondition,
            price = safePrice,
            isNegotiable = isNegotiable,
            district = cleanDistrict,
            codLocation = cleanCod,
            description = cleanDescription,
            whatsappNumber = cleanWa,
            photoUrl = resolvedPhotoUrls.firstOrNull().orEmpty(),
            photoUrls = resolvedPhotoUrls,
            isSold = false,
            commentCount = 0,
            createdAt = now,
            updatedAt = now,
        )

        inMemoryBarkas[recordId] = newItem
        SurabayaSpamProtector.recordSubmission(uid, "barkas_items")
        bumpMutationTick()
        return recordId
    }

    suspend fun toggleBarkasSoldStatus(
        barkasId: String,
        requesterUid: String = requireUserId(),
        isAdmin: Boolean = false,
        explicitSold: Boolean? = null,
    ): Boolean {
        val uid = requesterUid.ifBlank { requireUserId() }
        val current = inMemoryBarkas[barkasId]
            ?: runCatching { pb.getOneOrNull("barkas_items", barkasId)?.let(::mapBarkasItem) }.getOrNull()
            ?: throw IllegalArgumentException("Barang tidak ditemukan.")

        if (current.sellerId != uid && !isAdmin && !isCurrentSessionAdmin(uid)) {
            throw IllegalStateException("Hanya pemilik barang yang dapat mengubah status terjual.")
        }

        val nextSold = explicitSold ?: !current.isSold
        val updated = current.copy(
            isSold = nextSold,
            updatedAt = System.currentTimeMillis(),
        )
        inMemoryBarkas[barkasId] = updated
        bumpMutationTick()

        try {
            pb.updateRecord(
                "barkas_items",
                barkasId,
                JSONObject().apply {
                    put("is_sold", nextSold)
                },
            )
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.w(TAG, "toggleBarkasSoldStatus PB sync fallback: ${e.message}")
        }

        return nextSold
    }

    suspend fun addBarkasComment(barkasId: String, text: String): String {
        val uid = requireUserId()
        val cleanText = text.trim().take(600)
        if (cleanText.length < 2) {
            throw IllegalArgumentException("Komentar minimal 2 karakter.")
        }

        val account = runCatching { pb.getOneOrNull("users", uid) }.getOrNull()
        val authorName = account?.optString("name")?.takeIf { it.isNotBlank() }
            ?: pb.currentSession?.displayName?.takeIf { it.isNotBlank() }
            ?: "Warga Surabaya"
        val authorDistrict = account?.optString("district")?.takeIf { it.isNotBlank() }
            ?: pb.currentSession?.district?.takeIf { it.isNotBlank() }
            ?: "Genteng"

        val commentId = PocketBaseClient.deterministicPbId("bcm_${UUID.randomUUID()}")
        val now = System.currentTimeMillis()
        val newComment = BarkasComment(
            id = commentId,
            barkasId = barkasId,
            authorId = uid,
            authorName = authorName,
            authorDistrict = authorDistrict,
            text = cleanText,
            createdAt = now,
        )

        val list = inMemoryBarkasComments.computeIfAbsent(barkasId) { CopyOnWriteArrayList() }
        list.add(newComment)

        val currentItem = inMemoryBarkas[barkasId]
            ?: runCatching { pb.getOneOrNull("barkas_items", barkasId)?.let(::mapBarkasItem) }.getOrNull()
        val nextCount = maxOf((currentItem?.commentCount ?: 0) + 1, list.size)
        if (currentItem != null) {
            inMemoryBarkas[barkasId] = currentItem.copy(
                commentCount = nextCount,
                updatedAt = now,
            )
        }
        bumpMutationTick()

        try {
            val payload = JSONObject().apply {
                put("id", commentId)
                put("barkas_id", barkasId)
                put("author_id", uid)
                put("author_name", authorName.take(80))
                put("author_district", authorDistrict.take(60))
                put("text", cleanText)
            }
            pb.createRecord("barkas_comments", payload)
            runCatching {
                pb.updateRecord(
                    "barkas_items",
                    barkasId,
                    JSONObject().apply {
                        put("comment_count", nextCount)
                    },
                )
            }
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            Log.w(TAG, "addBarkasComment PB sync fallback: ${e.message}")
        }

        return commentId
    }

    suspend fun reportBarkasContent(barkasId: String, reporterUid: String) {
        val current = inMemoryBarkas[barkasId]
        val currentCount = current?.contentReportCount ?: 0
        val currentUids = current?.contentReportedByUids ?: emptyList()
        reportInappropriateContent(
            collection = "barkas_items",
            recordId = barkasId,
            currentCount = currentCount,
            currentReportedUids = currentUids,
        )
        if (current != null && reporterUid !in current.contentReportedByUids) {
            inMemoryBarkas[barkasId] = current.copy(
                contentReportCount = current.contentReportCount + 1,
                contentReportedByUids = (current.contentReportedByUids + reporterUid).distinct(),
            )
            bumpMutationTick()
        }
    }

    suspend fun deleteBarkasItem(barkasId: String, requesterUid: String, isAdmin: Boolean) {
        val current = inMemoryBarkas[barkasId]
        if (current == null || current.sellerId == requesterUid || isAdmin || isCurrentSessionAdmin(requesterUid)) {
            inMemoryBarkas.remove(barkasId)
            inMemoryBarkasComments.remove(barkasId)
            localDeletedRecords.add("barkas_items:$barkasId")
            bumpMutationTick()
            runCatching { pb.deleteRecord("barkas_items", barkasId) }
        }
    }

    companion object {
        private const val TAG = "SurabayaRepoPB"
        const val PRIMARY_ADMIN_EMAIL = PocketBaseClient.PRIMARY_ADMIN_EMAIL
        const val BOOTSTRAPPED_ADMIN_EMAIL = PocketBaseClient.BOOTSTRAPPED_ADMIN_EMAIL

        fun isOfficialAdminEmail(email: String?): Boolean = PocketBaseClient.isOfficialAdminEmail(email)

        fun buildDirectRoomId(uidA: String, uidB: String): String {
            val cleanA = uidA.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(56)
            val cleanB = uidB.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(56)
            val sorted = listOf(cleanA, cleanB).sorted()
            return "dm_${sorted[0]}_${sorted[1]}"
        }
    }
}
