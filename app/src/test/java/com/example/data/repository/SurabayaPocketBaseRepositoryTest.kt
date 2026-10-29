package com.example.data.repository

import com.example.data.model.NearbyProfile
import com.example.data.pocketbase.PocketBaseClient
import com.example.data.util.PreparedPhoto
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SurabayaPocketBaseRepositoryTest {

    private lateinit var server: HttpServer
    private lateinit var baseUrl: String

    // collectionName -> (recordId -> JSONObject)
    private val collectionsDb = ConcurrentHashMap<String, ConcurrentHashMap<String, JSONObject>>()
    private val seq = AtomicInteger(1)
    private val rejectMultipartFiles = AtomicBoolean(false)

    @Before
    fun setUp() {
        rejectMultipartFiles.set(false)
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.createContext("/api/health") { ex ->
            respondJson(ex, 200, JSONObject().put("code", 200).put("message", "API is healthy."))
        }
        server.createContext("/api/collections/") { ex ->
            handleCollectionsEndpoint(ex)
        }
        server.start()
        baseUrl = "http://127.0.0.1:${server.address.port}"
    }

    @After
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun `register login incident event moderation and symmetric two way roomId chat succeed`() = runBlocking {
        // Citizen A (Raka) & Citizen B (Nadia - Admin)
        val clientRaka = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)
        val clientNadia = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)

        val repoRaka = SurabayaRepository(clientRaka)
        val repoNadia = SurabayaRepository(clientNadia)

        val sessionRaka = clientRaka.registerAndSignIn(
            name = "Raka Pratama",
            email = "raka@surabaya.id",
            password = "password123",
            district = "Gubeng",
        )
        val sessionNadia = clientNadia.registerAndSignIn(
            name = "Nadia Admin",
            email = "nabilalailatus12@gmail.com",
            password = "password123",
            district = "Wonokromo",
        )

        assertTrue("PocketBase user IDs must be 15 lowercase alphanumeric chars", sessionRaka.userId.matches(Regex("^[a-z0-9]{15}$")))
        assertTrue(sessionNadia.isAdmin)

        // 1. Kejadian Surabaya creation & fetch
        val incId = repoRaka.createIncident(
            authorName = sessionRaka.displayName,
            title = "Lampu Lalu Lintas Gubeng Pojok Normal Kembali",
            description = "Arus kendaraan dari arah Pemuda ke Gubeng sudah kembali lancar.",
            category = "LALU_LINTAS",
            severity = "INFO",
            district = "Gubeng",
            address = "Jl. Gubeng Pojok, Surabaya",
        )
        val incidents = repoNadia.observeIncidents().first()
        assertEquals(1, incidents.size)
        assertEquals(incId, incidents.first().id)

        // 2. Event Proposal (PENDING) -> Admin Moderation (APPROVED)
        val eventId = repoRaka.proposeEvent(
            proposerName = sessionRaka.displayName,
            title = "Gowes Pagi Jembatan Suroboyo",
            category = "OLAHRAGA",
            eventDateText = "Minggu, 6 Des 2026",
            timeText = "06.00 WIB",
            venueName = "Pantai Kenjeran",
            district = "Kenjeran",
            address = "Jl. Pantai Lama Kenjeran",
            priceLabel = "Gratis",
            description = "Gowes sehat bareng komunitas warga Surabaya Timur.",
        )
        assertEquals(0, repoRaka.observeApprovedEvents().first().size)
        assertEquals(1, repoNadia.observePendingEvents().first().size)

        repoNadia.moderateEvent(eventId, approve = true, adminNote = "Disetujui Admin PocketBase.")
        val approvedEvents = repoRaka.observeApprovedEvents().first()
        assertEquals(1, approvedEvents.size)
        assertEquals("APPROVED", approvedEvents.first().status)

        // 3. Symmetric Two-Way Direct Chat by Shared Room ID
        val nadiaPeer = NearbyProfile(
            id = sessionNadia.userId,
            userId = sessionNadia.userId,
            displayName = sessionNadia.displayName,
            district = "Wonokromo",
        )
        val rakaPeer = NearbyProfile(
            id = sessionRaka.userId,
            userId = sessionRaka.userId,
            displayName = sessionRaka.displayName,
            district = "Gubeng",
        )

        val roomOpenedByRaka = repoRaka.openOrCreateDirectChat(
            peerProfile = nadiaPeer,
            currentUserName = sessionRaka.displayName,
            currentUserDistrict = "Gubeng",
        )
        val roomOpenedByNadia = repoNadia.openOrCreateDirectChat(
            peerProfile = rakaPeer,
            currentUserName = sessionNadia.displayName,
            currentUserDistrict = "Wonokromo",
        )

        // Both citizens MUST land on the exact same PocketBase room ID and roomKey!
        assertEquals(roomOpenedByRaka.id, roomOpenedByNadia.id)
        assertEquals(roomOpenedByRaka.roomKey, roomOpenedByNadia.roomKey)
        assertEquals("Nadia Admin", roomOpenedByRaka.displayTitleFor(sessionRaka.userId))
        assertEquals("Raka Pratama", roomOpenedByNadia.displayTitleFor(sessionNadia.userId))

        // Raka sends -> Nadia replies in the exact same room
        repoRaka.sendMessage(roomOpenedByRaka, sessionRaka.displayName, "Halo Mbak Nadia, event gowes jadi ya!")
        repoNadia.sendMessage(roomOpenedByNadia, sessionNadia.displayName, "Siap Mas Raka, sudah saya setujui di aplikasi!")

        val messagesSeenByRaka = repoRaka.observeMessages(roomOpenedByRaka, sessionRaka.userId).first()
        val messagesSeenByNadia = repoNadia.observeMessages(roomOpenedByNadia, sessionNadia.userId).first()

        assertEquals(2, messagesSeenByRaka.size)
        assertEquals(2, messagesSeenByNadia.size)
        assertEquals("Halo Mbak Nadia, event gowes jadi ya!", messagesSeenByNadia[0].text)
        assertEquals("Siap Mas Raka, sudah saya setujui di aplikasi!", messagesSeenByRaka[1].text)
    }

    @Test
    fun `multipart photo upload resolves PocketBase file URLs and falls back gracefully if schema missing`() = runBlocking {
        val client = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)
        val repo = SurabayaRepository(client)

        val session = client.registerAndSignIn(
            name = "Eko Wahyudi",
            email = "eko@surabaya.id",
            password = "password123",
            district = "Rungkut",
        )

        val dummyJpeg = PreparedPhoto(
            fileName = "kejadian_rungkut.jpg",
            mimeType = "image/jpeg",
            bytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte()),
        )

        var missingSchemaCallbackCalled = false
        val incId = repo.createIncident(
            authorName = session.displayName,
            title = "Genangan Air di Jalan Raya Rungkut",
            description = " Pengendara motor harap ambil lajur kanan.",
            category = "CUACA_BANJIR",
            severity = "PENTING",
            district = "Rungkut",
            address = "Jl. Raya Rungkut Madya",
            photo = dummyJpeg,
            onPhotoSchemaMissing = { missingSchemaCallbackCalled = true },
        )

        assertFalse(missingSchemaCallbackCalled)
        val uploadedIncident = repo.observeIncidents().first().first { it.id == incId }
        assertTrue(
            "Expected PocketBase file URL for uploaded incident photo, got: ${uploadedIncident.photoUrl}",
            uploadedIncident.photoUrl.startsWith("$baseUrl/api/files/incidents/$incId/"),
        )

        // Avatar upload updates both users & nearby_profiles
        val avatarPhoto = PreparedPhoto(
            fileName = "avatar_eko.jpg",
            mimeType = "image/jpeg",
            bytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0x01, 0xD9.toByte()),
        )
        repo.updateProfileAndPrivacy(
            displayName = "Eko Wahyudi SBY",
            district = "Rungkut",
            bio = "Warga Rungkut aktif",
            interests = "Kuliner, Gowes",
            searchRadiusKm = 12.0,
            isDiscoverable = true,
            lat = -7.31,
            lng = 112.78,
            avatarPhoto = avatarPhoto,
        )
        val updatedAccount = repo.observeUserAccount(session.userId).first()
        assertTrue(updatedAccount?.avatarUrl.orEmpty().contains("/api/files/users/${session.userId}/"))

        // Simulate server before pocketbase_collections.json re-import (HTTP 400 on multipart)
        rejectMultipartFiles.set(true)
        var fallbackNotified = false
        val spotId = repo.createCulinarySpot(
            authorName = session.displayName,
            name = "Rawon Kalkulator Taman Bungkul",
            category = "RAWON_SOTO",
            description = "Kuah kluwek pekat khas Surabaya.",
            district = "Wonokromo",
            address = "Taman Bungkul",
            priceRange = "Rp 25.000",
            openHour = 10,
            closeHour = 23,
            photo = dummyJpeg,
            onPhotoSchemaMissing = { fallbackNotified = true },
        )

        assertTrue("Should notify caller when server photo column is missing", fallbackNotified)
        val savedSpot = repo.observeCulinarySpots().first().first { it.id == spotId }
        assertEquals("Rawon Kalkulator Taman Bungkul", savedSpot.name)
        assertEquals("", savedSpot.photoUrl)
    }

    // --- Minimal In-Memory PocketBase 0.22 HTTP Server Mock (JSON + multipart/form-data) ---

    private fun handleCollectionsEndpoint(ex: HttpExchange) {
        val rawPath = ex.requestURI.path.removePrefix("/api/collections/")
        val segments = rawPath.split("/").filter { it.isNotBlank() }
        val collection = segments.getOrNull(0).orEmpty()
        val actionOrSub = segments.getOrNull(1).orEmpty()
        val recordId = segments.getOrNull(2).orEmpty()
        val table = collectionsDb.computeIfAbsent(collection) { ConcurrentHashMap() }
        val contentType = ex.requestHeaders.getFirst("Content-Type").orEmpty()
        val rawBytes = ex.requestBody.use { it.readBytes() }
        val bodyStr = String(rawBytes, Charsets.UTF_8)

        if (collection == "users" && actionOrSub == "auth-with-password" && ex.requestMethod == "POST") {
            val req = JSONObject(bodyStr)
            val identity = req.optString("identity").lowercase()
            val found = table.values.firstOrNull { it.optString("email").lowercase() == identity }
            if (found == null) {
                respondJson(ex, 400, JSONObject().put("message", "Failed to authenticate."))
            } else {
                val authResp = JSONObject()
                    .put("token", "pb_test_jwt_${found.optString("id")}")
                    .put("record", found)
                respondJson(ex, 200, authResp)
            }
            return
        }

        if (actionOrSub == "records") {
            when (ex.requestMethod) {
                "GET" -> {
                    if (recordId.isNotBlank()) {
                        val rec = table[recordId]
                        if (rec == null) {
                            respondJson(ex, 404, JSONObject().put("message", "The requested resource wasn't found."))
                        } else {
                            respondJson(ex, 200, rec)
                        }
                    } else {
                        val filterRaw = parseQueryParam(ex.requestURI.rawQuery, "filter")
                        val filtered = table.values.filter { matchesSimplePbFilter(it, filterRaw) }
                            .sortedBy { it.optString("created") }
                        val items = JSONArray()
                        filtered.forEach { items.put(it) }
                        respondJson(ex, 200, JSONObject().put("items", items))
                    }
                }
                "POST" -> {
                    val isMultipart = contentType.startsWith("multipart/form-data", ignoreCase = true)
                    if (isMultipart && rejectMultipartFiles.get()) {
                        respondJson(ex, 400, JSONObject().put("code", 400).put("message", "Failed to create record: unknown file field."))
                        return
                    }
                    val incoming = if (isMultipart) {
                        parseMultipartPayload(bodyStr, contentType)
                    } else {
                        JSONObject(bodyStr)
                    }
                    val id = incoming.optString("id").ifBlank {
                        PocketBaseClient.deterministicPbId("rec_${seq.getAndIncrement()}")
                    }
                    val nowStr = String.format("2026-10-29 07:30:%02d.000Z", seq.getAndIncrement() % 60)
                    val stored = JSONObject(incoming.toString()).apply {
                        put("id", id)
                        put("created", nowStr)
                        put("updated", nowStr)
                    }
                    table[id] = stored
                    respondJson(ex, 200, stored)
                }
                "PATCH" -> {
                    val isMultipart = contentType.startsWith("multipart/form-data", ignoreCase = true)
                    if (isMultipart && rejectMultipartFiles.get()) {
                        respondJson(ex, 400, JSONObject().put("code", 400).put("message", "Failed to update record."))
                        return
                    }
                    val existing = table[recordId]
                    if (existing == null) {
                        respondJson(ex, 404, JSONObject().put("message", "Not found"))
                    } else {
                        val patch = if (isMultipart) {
                            parseMultipartPayload(bodyStr, contentType)
                        } else {
                            JSONObject(bodyStr)
                        }
                        val merged = JSONObject(existing.toString())
                        val keys = patch.keys()
                        while (keys.hasNext()) {
                            val k = keys.next()
                            merged.put(k, patch.get(k))
                        }
                        merged.put("updated", String.format("2026-10-29 07:35:%02d.000Z", seq.getAndIncrement() % 60))
                        table[recordId] = merged
                        respondJson(ex, 200, merged)
                    }
                }
                "DELETE" -> {
                    table.remove(recordId)
                    ex.sendResponseHeaders(204, -1)
                    ex.close()
                }
            }
        }
    }

    private fun parseMultipartPayload(bodyStr: String, contentType: String): JSONObject {
        val boundary = contentType.substringAfter("boundary=", "").trim().trim('"')
        val result = JSONObject()
        if (boundary.isBlank()) return result
        val parts = bodyStr.split("--$boundary")
        for (rawPart in parts) {
            val part = rawPart.trim('\r', '\n')
            if (part.isBlank() || part == "--") continue
            val splitIdx = part.indexOf("\r\n\r\n")
            if (splitIdx <= 0) continue
            val headers = part.substring(0, splitIdx)
            val content = part.substring(splitIdx + 4).removeSuffix("\r\n")
            val nameMatch = Regex("name=\"([^\"]+)\"").find(headers) ?: continue
            val fieldName = nameMatch.groupValues[1]
            val fileMatch = Regex("filename=\"([^\"]+)\"").find(headers)
            if (fileMatch != null) {
                val origName = fileMatch.groupValues[1]
                val storedFileName = origName.substringBeforeLast(".") + "_pbtest." + origName.substringAfterLast(".", "jpg")
                result.put(fieldName, storedFileName)
            } else {
                when {
                    content == "true" -> result.put(fieldName, true)
                    content == "false" -> result.put(fieldName, false)
                    content.toIntOrNull() != null && !content.startsWith("0") -> result.put(fieldName, content.toInt())
                    content == "0" -> result.put(fieldName, 0)
                    content.toDoubleOrNull() != null -> result.put(fieldName, content.toDouble())
                    else -> result.put(fieldName, content)
                }
            }
        }
        return result
    }

    private fun parseQueryParam(rawQuery: String?, key: String): String {
        if (rawQuery.isNullOrBlank()) return ""
        return rawQuery.split("&")
            .mapNotNull { part ->
                val idx = part.indexOf('=')
                if (idx <= 0) null
                else part.substring(0, idx) to URLDecoder.decode(part.substring(idx + 1), "UTF-8")
            }
            .firstOrNull { it.first == key }
            ?.second
            .orEmpty()
    }

    private fun matchesSimplePbFilter(doc: JSONObject, filter: String): Boolean {
        if (filter.isBlank()) return true
        // Support simple PocketBase equality filters used in tests: a = 'x' && (b = 'y' || c = 'y')
        val regexEq = Regex("([a-z0-9_]+)\\s*=\\s*'([^']*)'")
        if (filter.contains("||")) {
            val beforeParen = filter.substringBefore("&& (").trim()
            val insideOr = filter.substringAfter("&& (", "").substringBeforeLast(")")
            val andOk = regexEq.findAll(beforeParen).all { m ->
                doc.optString(m.groupValues[1]) == m.groupValues[2]
            }
            val orOk = regexEq.findAll(insideOr).any { m ->
                doc.optString(m.groupValues[1]) == m.groupValues[2]
            }
            return andOk && orOk
        }
        if (filter.contains("= true")) {
            val field = filter.substringBefore("= true").trim()
            return doc.optBoolean(field, false)
        }
        return regexEq.findAll(filter).all { m ->
            doc.optString(m.groupValues[1]) == m.groupValues[2]
        }
    }

    private fun respondJson(ex: HttpExchange, code: Int, json: JSONObject) {
        val bytes = json.toString().toByteArray(Charsets.UTF_8)
        ex.responseHeaders.add("Content-Type", "application/json; charset=utf-8")
        ex.sendResponseHeaders(code, bytes.size.toLong())
        ex.responseBody.use { it.write(bytes) }
    }
}
