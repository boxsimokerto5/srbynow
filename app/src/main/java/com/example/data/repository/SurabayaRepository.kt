package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.model.BookmarkItem
import com.example.data.model.ChatMessage
import com.example.data.model.ChatRoom
import com.example.data.model.CulinaryReview
import com.example.data.model.CulinarySpot
import com.example.data.model.IncidentComment
import com.example.data.model.IncidentReport
import com.example.data.model.NearbyProfile
import com.example.data.model.SurabayaEvent
import com.example.data.model.UserAccount
import com.example.data.pocketbase.MultipartFilePart
import com.example.data.pocketbase.PocketBaseClient
import com.example.data.pocketbase.PocketBaseException
import com.example.data.seed.SurabayaCuratedSeed
import com.example.data.util.LinkSecurityValidator
import com.example.data.util.LinkValidationResult
import com.example.data.util.OperationType
import com.example.data.util.PreparedPhoto
import com.example.data.util.SafeLinkCategory
import com.example.data.util.SurabayaDistricts
import com.example.data.util.defaultCulinaryImageKey
import com.example.data.util.defaultEventImageKey
import com.example.data.util.defaultIncidentImageKey
import com.example.data.util.logBackendError
import com.example.data.util.parsePocketBaseTimestamp
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import org.json.JSONObject

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
    private val localMutationTick = MutableStateFlow(0L)

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
            val isBootstrapped = email.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true)
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
        val isBootstrappedAdmin = session?.email?.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true) == true

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
            if (existingNearby == null) {
                val colorPalette = listOf("#006B5E", "#00798C", "#C86D1F", "#2B6CB0", "#0F766E")
                val pickedColor = colorPalette[(uid.hashCode() and Int.MAX_VALUE) % colorPalette.size]
                val payload = JSONObject().apply {
                    put("id", uid)
                    put("user_id", uid)
                    put("display_name", userName)
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
            }

            val publicRooms = pb.getList("chat_rooms", filter = "room_type = 'PUBLIC'", perPage = 1)
            if (publicRooms.isEmpty()) {
                for (item in SurabayaCuratedSeed.starterPublicRooms(uid)) {
                    createOrIgnoreExisting("chat_rooms", item)
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
        avatarPhoto: PreparedPhoto? = null,
        onPhotoSchemaMissing: (() -> Unit)? = null,
    ) {
        val uid = requireUserId()
        val cleanName = displayName.trim().take(80).ifEmpty { "Warga Surabaya" }
        val cleanDistrict = district.trim().take(60).ifEmpty { "Genteng" }
        val cleanBio = bio.trim().take(240)
        val cleanInterests = interests.trim().take(200)
        val clampedRadius = searchRadiusKm.coerceIn(1.0, 50.0)
        var missingAvatarSchema = false

        try {
            val userPatch = JSONObject().apply {
                put("name", cleanName)
                put("district", cleanDistrict)
                put("bio", cleanBio)
                put("search_radius_km", clampedRadius)
                put("is_discoverable", isDiscoverable)
            }
            if (avatarPhoto != null) {
                val userFields = mapOf(
                    "name" to cleanName,
                    "district" to cleanDistrict,
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
                        missingAvatarSchema = true
                        pb.updateRecord("users", uid, userPatch)
                    } else {
                        throw e
                    }
                }
                if (updatedUser.optString("avatar").isBlank()) {
                    missingAvatarSchema = true
                }
            } else {
                pb.updateRecord("users", uid, userPatch)
            }

            val existingNearby = pb.getOneOrNull("nearby_profiles", uid)
            if (existingNearby != null) {
                val nearbyPatch = JSONObject().apply {
                    put("display_name", cleanName)
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
                            recordId = uid,
                            fields = nearbyFields,
                            filePart = MultipartFilePart("avatar", avatarPhoto.fileName, avatarPhoto.mimeType, avatarPhoto.bytes),
                        )
                    } catch (e: PocketBaseException) {
                        if (e.statusCode == 400) {
                            missingAvatarSchema = true
                            pb.updateRecord("nearby_profiles", uid, nearbyPatch)
                        } else {
                            throw e
                        }
                    }
                    if (updatedNearby.optString("avatar").isBlank()) {
                        missingAvatarSchema = true
                    }
                } else {
                    pb.updateRecord("nearby_profiles", uid, nearbyPatch)
                }
            } else {
                val createPayload = JSONObject().apply {
                    put("id", uid)
                    put("user_id", uid)
                    put("display_name", cleanName)
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
                            missingAvatarSchema = true
                            pb.createRecord("nearby_profiles", createPayload)
                        } else {
                            throw e
                        }
                    }
                    if (createdNearby.optString("avatar").isBlank()) {
                        missingAvatarSchema = true
                    }
                } else {
                    pb.createRecord("nearby_profiles", createPayload)
                }
            }

            if (avatarPhoto != null && missingAvatarSchema) {
                onPhotoSchemaMissing?.invoke()
            }
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
            list.map(::mapIncident)
                .filter { "incidents:${it.id}" !in localDeletedRecords && it.authorId !in localBannedUids }
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
                        return pb.createRecord("incidents", payload).optString("id").ifBlank { recordId }
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
            created.optString("id").ifBlank { recordId }
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
            list.map(::mapEvent)
                .filter { "events:${it.id}" !in localDeletedRecords && it.proposerId !in localBannedUids }
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
            list.map(::mapEvent)
                .filter { "events:${it.id}" !in localDeletedRecords && it.proposerId !in localBannedUids }
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
            list.map(::mapEvent)
                .filter { "events:${it.id}" !in localDeletedRecords }
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
                                return pb.createRecord("events", payload).optString("id").ifBlank { recordId }
                            } else {
                                throw fallbackErr
                            }
                        }
                    } else if (e.statusCode == 400) {
                        onPhotoSchemaMissing?.invoke()
                        return pb.createRecord("events", payload).optString("id").ifBlank { recordId }
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
            created.optString("id").ifBlank { recordId }
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
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.UPDATE, "events/$eventId")
            throw e
        }
    }

    suspend fun toggleEventInterest(event: SurabayaEvent) {
        val uid = requireUserId()
        val list = event.interestedUids.toMutableList()
        val already = uid in list
        if (already) {
            list.remove(uid)
        } else if (list.size < 80) {
            list.add(uid)
        }
        val nextCount = if (already) {
            (event.interestedCount - 1).coerceAtLeast(0)
        } else {
            event.interestedCount + 1
        }
        try {
            val patch = JSONObject().apply {
                put("interested_count", nextCount)
                put("interested_uids", encodeUidList(list))
            }
            pb.updateRecord("events", event.id, patch)
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
            list.map(::mapCulinarySpot)
                .filter { "culinary:${it.id}" !in localDeletedRecords && it.createdBy !in localBannedUids }
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
        imageKey: String = defaultCulinaryImageKey(category),
        photos: List<PreparedPhoto> = emptyList(),
        onPhotoSchemaMissing: (() -> Unit)? = null,
        onMultiPhotoFallbackUsed: (() -> Unit)? = null,
    ): String {
        val uid = requireUserId()
        val recordId = PocketBaseClient.deterministicPbId("kul_${UUID.randomUUID()}")
        val (lat, lng) = SurabayaDistricts.coordinateFor(district)
        val cleanAuthor = authorName.trim().take(80).ifEmpty { "Warga Surabaya" }
        val cleanName = name.trim().take(100)
        val cleanDesc = description.trim().take(1000)
        val cleanDistrict = district.trim().take(60)
        val cleanAddress = address.trim().take(180)
        val cleanPrice = priceRange.trim().take(60).ifEmpty { "Rp 15.000 - Rp 30.000" }
        val cleanMapsUrl = requireSafeOptionalUrl(mapsUrl, SafeLinkCategory.GOOGLE_MAPS)
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
                        return pb.createRecord("culinary", payload).optString("id").ifBlank { recordId }
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
            created.optString("id").ifBlank { recordId }
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
                if (e.statusCode != 400) throw e
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
        try {
            pb.deleteRecord(collection, recordId)
            localDeletedRecords.add("$collection:$recordId")
            bumpMutationTick()
        } catch (ce: CancellationException) {
            throw ce
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
        return pb.observeCollection("chat_messages", pollIntervalMs = 2_500L) {
            pb.getList(
                collection = "chat_messages",
                filter = filterQuery,
                sort = "+created",
                perPage = 150,
            ).map(::mapChatMessage)
        }.catch { e ->
            if (e is Exception) logBackendError(e, OperationType.LIST, "chat_messages/${room.id}")
            throw e
        }
    }

    suspend fun sendMessage(
        room: ChatRoom,
        senderName: String,
        text: String,
    ) {
        val uid = requireUserId()
        val cleanText = text.trim().take(1000)
        if (cleanText.isEmpty()) return
        val cleanSender = senderName.trim().take(80).ifEmpty { "Warga Surabaya" }
        val msgId = PocketBaseClient.deterministicPbId("msg_${UUID.randomUUID()}")
        val uid1 = if (room.roomType == "DIRECT") room.participantUids.getOrNull(0).orEmpty() else ""
        val uid2 = if (room.roomType == "DIRECT") room.participantUids.getOrNull(1).orEmpty() else ""

        try {
            val msgPayload = JSONObject().apply {
                put("id", msgId)
                put("room_id", room.id)
                put("room_key", room.roomKey.ifBlank { room.id })
                put("room_type", room.roomType)
                put("participant_uid_1", uid1)
                put("participant_uid_2", uid2)
                put("sender_id", uid)
                put("sender_name", cleanSender)
                put("text", cleanText)
            }
            pb.createRecord("chat_messages", msgPayload)

            val roomPatch = JSONObject().apply {
                put("last_message", cleanText.take(300))
                put("last_sender_name", cleanSender)
            }
            pb.updateRecord("chat_rooms", room.id, roomPatch)
        } catch (ce: CancellationException) {
            throw ce
        } catch (e: Exception) {
            logBackendError(e, OperationType.CREATE, "chat_messages/$msgId")
            throw e
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
        return UserAccount(
            id = id,
            userId = id,
            displayName = name,
            district = j.optString("district").ifBlank { "Genteng" },
            bio = j.optString("bio"),
            searchRadiusKm = j.optDouble("search_radius_km", 10.0).takeIf { !it.isNaN() && it > 0.0 } ?: 10.0,
            isDiscoverable = if (j.has("is_discoverable")) j.optBoolean("is_discoverable", true) else true,
            isAdmin = j.optBoolean("is_admin", false) || email.equals(BOOTSTRAPPED_ADMIN_EMAIL, ignoreCase = true),
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

    private fun mapNearbyProfile(j: JSONObject): NearbyProfile {
        val id = j.optString("id")
        val uid = j.optString("user_id")
        return NearbyProfile(
            id = id,
            userId = uid,
            displayName = j.optString("display_name", "Warga Surabaya"),
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

        return CulinarySpot(
            id = id,
            createdBy = j.optString("created_by"),
            authorName = j.optString("author_name", "Warga Surabaya"),
            name = j.optString("name"),
            category = cat,
            description = j.optString("description"),
            district = j.optString("district", "Genteng").ifBlank { "Genteng" },
            address = j.optString("address"),
            priceRange = j.optString("price_range").ifBlank { "Rp 20.000 - Rp 35.000" },
            openHour = j.optInt("open_hour", 8),
            closeHour = j.optInt("close_hour", 22),
            lat = j.optDouble("lat", SurabayaDistricts.DEFAULT_LAT),
            lng = j.optDouble("lng", SurabayaDistricts.DEFAULT_LNG),
            imageKey = j.optString("image_key").ifBlank { defaultCulinaryImageKey(cat) },
            photoUrl = resolvedPhotoUrls.firstOrNull().orEmpty(),
            photoUrls = resolvedPhotoUrls,
            mapsUrl = LinkSecurityValidator.sanitizeOrEmpty(j.optString("maps_url"), SafeLinkCategory.GOOGLE_MAPS),
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
        return ChatRoom(
            id = j.optString("id"),
            roomKey = j.optString("room_key"),
            roomType = j.optString("room_type", "PUBLIC"),
            title = j.optString("title"),
            subtitle = j.optString("subtitle"),
            participantUids = participants,
            participantNames = j.optString("participant_names"),
            lastMessage = j.optString("last_message"),
            lastSenderName = j.optString("last_sender_name"),
            createdBy = j.optString("created_by"),
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
            updatedAt = parsePocketBaseTimestamp(j.optString("updated")),
        )
    }

    private fun mapChatMessage(j: JSONObject): ChatMessage {
        val uid1 = j.optString("participant_uid_1")
        val uid2 = j.optString("participant_uid_2")
        val participants = listOf(uid1, uid2).filter { it.isNotBlank() }
        return ChatMessage(
            id = j.optString("id"),
            roomId = j.optString("room_id"),
            roomKey = j.optString("room_key"),
            roomType = j.optString("room_type", "PUBLIC"),
            participantUids = participants,
            senderId = j.optString("sender_id"),
            senderName = j.optString("sender_name", "Warga Surabaya"),
            text = j.optString("text"),
            createdAt = parsePocketBaseTimestamp(j.optString("created")),
        )
    }

    private fun encodeUidList(list: List<String>): String {
        return list.map { it.trim() }.filter { it.isNotBlank() }.distinct().joinToString("|").take(1950)
    }

    private fun decodeUidList(raw: String): List<String> {
        if (raw.isBlank()) return emptyList()
        return raw.split("|").map { it.trim() }.filter { it.isNotBlank() }
    }

    private fun escapePbValue(raw: String): String {
        return raw.replace("\\", "\\\\").replace("'", "\\'")
    }

    companion object {
        private const val TAG = "SurabayaRepoPB"
        const val BOOTSTRAPPED_ADMIN_EMAIL = "nabilalailatus12@gmail.com"

        fun buildDirectRoomId(uidA: String, uidB: String): String {
            val cleanA = uidA.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(56)
            val cleanB = uidB.trim().replace(Regex("[^a-zA-Z0-9_\\-]"), "_").take(56)
            val sorted = listOf(cleanA, cleanB).sorted()
            return "dm_${sorted[0]}_${sorted[1]}"
        }
    }
}
