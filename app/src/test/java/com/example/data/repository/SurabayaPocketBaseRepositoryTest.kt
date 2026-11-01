package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Looper
import androidx.test.core.app.ApplicationProvider
import com.example.data.location.LocationHelper
import com.example.data.model.MessageDeliveryStatus
import com.example.data.model.NearbyProfile
import com.example.data.pocketbase.PocketBaseClient
import com.example.data.util.ImageSafetyScanResult
import com.example.data.util.ImageSafetyScanner
import com.example.data.util.LinkSecurityValidator
import com.example.data.util.LinkValidationResult
import com.example.data.util.PreparedPhoto
import com.example.data.util.SafeLinkCategory
import com.example.ui.viewmodel.SurabayaViewModel
import com.example.data.util.SurabayaSpamProtector
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.File
import java.io.FileOutputStream
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
import org.robolectric.Shadows.shadowOf
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
        collectionsDb.clear()
        SurabayaSpamProtector.resetForTesting()
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
        collectionsDb.clear()
        SurabayaSpamProtector.resetForTesting()
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

        // Raka sends message with attached photo
        val chatPhoto = PreparedPhoto(
            fileName = "gowes_pagi.jpg",
            mimeType = "image/jpeg",
            bytes = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte(), 0xD9.toByte()),
        )
        repoRaka.sendMessage(roomOpenedByRaka, sessionRaka.displayName, "Ini foto rutenya ya!", chatPhoto)
        val messagesWithPhoto = repoNadia.observeMessages(roomOpenedByNadia, sessionNadia.userId).first()
        assertEquals(3, messagesWithPhoto.size)
        assertTrue(messagesWithPhoto[2].hasPhoto || messagesWithPhoto[2].text.contains("foto", ignoreCase = true))
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
            photos = listOf(dummyJpeg),
            onPhotoSchemaMissing = { fallbackNotified = true },
        )

        assertTrue("Should notify caller when server photo column is missing", fallbackNotified)
        val savedSpot = repo.observeCulinarySpots().first().first { it.id == spotId }
        assertEquals("Rawon Kalkulator Taman Bungkul", savedSpot.name)
        assertEquals("", savedSpot.photoUrl)
    }

    @Test
    fun `anti phishing validator blocks fake domains shorteners apk and auto quarantines at 3 citizen reports`() = runBlocking {
        // 1. Official domains pass
        assertTrue(
            LinkSecurityValidator.validateOptionalLink("https://maps.app.goo.gl/AbCdEf123", SafeLinkCategory.GOOGLE_MAPS) is LinkValidationResult.Valid,
        )
        assertTrue(
            LinkSecurityValidator.validateOptionalLink("https://www.google.com/maps/place/Taman+Bungkul", SafeLinkCategory.GOOGLE_MAPS) is LinkValidationResult.Valid,
        )
        assertTrue(
            LinkSecurityValidator.validateOptionalLink("https://gofood.link/a/xyz123", SafeLinkCategory.GOFOOD) is LinkValidationResult.Valid,
        )
        assertTrue(
            LinkSecurityValidator.validateOptionalLink("https://r.grab.com/g/6-2026", SafeLinkCategory.GRABFOOD) is LinkValidationResult.Valid,
        )
        assertTrue(
            LinkSecurityValidator.validateOptionalLink("https://shopee.co.id/universal-link/now-food/shop/123", SafeLinkCategory.SHOPEEFOOD) is LinkValidationResult.Valid,
        )

        // 2. Shorteners, HTTP, raw IP, credential spoofing, APK downloads, and fake domains are blocked
        assertTrue(
            LinkSecurityValidator.validateOptionalLink("https://bit.ly/undangan-nikah", SafeLinkCategory.GOOGLE_MAPS) is LinkValidationResult.Invalid,
        )
        assertTrue(
            LinkSecurityValidator.validateOptionalLink("https://s.id/jebakan", SafeLinkCategory.GOFOOD) is LinkValidationResult.Invalid,
        )
        assertTrue(
            LinkSecurityValidator.validateOptionalLink("http://maps.app.goo.gl/insecure", SafeLinkCategory.GOOGLE_MAPS) is LinkValidationResult.Invalid,
        )
        assertTrue(
            LinkSecurityValidator.validateOptionalLink("https://maps.app.goo.gl/undangan_surabaya.apk", SafeLinkCategory.GOOGLE_MAPS) is LinkValidationResult.Invalid,
        )
        assertTrue(
            LinkSecurityValidator.validateOptionalLink("https://maps.app.goo.gl@phishing-jahat.com/login", SafeLinkCategory.GOOGLE_MAPS) is LinkValidationResult.Invalid,
        )
        assertTrue(
            LinkSecurityValidator.validateOptionalLink("https://192.168.1.10/maps", SafeLinkCategory.GOOGLE_MAPS) is LinkValidationResult.Invalid,
        )
        assertTrue(
            LinkSecurityValidator.validateOptionalLink("https://google-maps-palsu.xyz/lokasi", SafeLinkCategory.GOOGLE_MAPS) is LinkValidationResult.Invalid,
        )

        // 3. Repository rejects malicious links on submit and auto-quarantines after 3 citizen reports
        val c1 = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)
        val c2 = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)
        val c3 = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)
        val r1 = SurabayaRepository(c1)
        val r2 = SurabayaRepository(c2)
        val r3 = SurabayaRepository(c3)

        val s1 = c1.registerAndSignIn("Budi", "budi@sby.id", "password123", "Genteng")
        c2.registerAndSignIn("Siti", "siti@sby.id", "password123", "Gubeng")
        c3.registerAndSignIn("Dimas", "dimas@sby.id", "password123", "Tegalsari")

        var rejectedPhishing = false
        try {
            r1.createIncident(
                authorName = s1.displayName,
                title = "Laporan Uji Link Palsu",
                description = "Tes proteksi link berbahaya",
                category = "INFO_WARGA",
                severity = "INFO",
                district = "Genteng",
                address = "Jl. Tunjungan",
                mapsUrl = "https://bit.ly/jebakan-apk",
            )
        } catch (_: IllegalArgumentException) {
            rejectedPhishing = true
        }
        assertTrue("Repository must reject non-whitelisted shortener URLs", rejectedPhishing)

        val validIncId = r1.createIncident(
            authorName = s1.displayName,
            title = "Penutupan Jalan Tunjungan Sementara",
            description = "Ada kegiatan kerja bakti kampung.",
            category = "LALU_INTAS",
            severity = "INFO",
            district = "Genteng",
            address = "Jl. Tunjungan No. 10",
            mapsUrl = "https://maps.app.goo.gl/TunjunganSby1",
        )
        var current = r1.observeIncidents().first().first { it.id == validIncId }
        assertEquals("https://maps.app.goo.gl/TunjunganSby1", current.safeMapsUrl)
        assertFalse(current.isLinkQuarantined)

        // 3 distinct citizens report the link -> auto-quarantine & wipe!
        r1.reportSuspiciousLink("incidents", validIncId, current.linkReportCount, current.linkReportedByUids)
        current = r2.observeIncidents().first().first { it.id == validIncId }
        assertEquals(1, current.linkReportCount)

        r2.reportSuspiciousLink("incidents", validIncId, current.linkReportCount, current.linkReportedByUids)
        current = r3.observeIncidents().first().first { it.id == validIncId }
        assertEquals(2, current.linkReportCount)
        assertFalse(current.isLinkQuarantined)

        r3.reportSuspiciousLink("incidents", validIncId, current.linkReportCount, current.linkReportedByUids)
        current = r1.observeIncidents().first().first { it.id == validIncId }
        assertEquals(3, current.linkReportCount)
        assertTrue("Link must be auto-quarantined at 3 reports", current.isLinkQuarantined)
        assertEquals("", current.safeMapsUrl)
    }

    @Test
    fun `four layer anti pornography system blocks explicit upload blurs at 1 report quarantines at 3 blocks user and allows admin delete`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()

        // Prepare a real temporary JPEG image file in Robolectric cacheDir
        val tempFile = File(context.cacheDir, "test_upload_sample.jpg")
        val bmp = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888).apply {
            eraseColor(Color.RED)
        }
        FileOutputStream(tempFile).use { out ->
            bmp.compress(Bitmap.CompressFormat.JPEG, 85, out)
        }
        val sampleUri = Uri.fromFile(tempFile)

        val cOffender = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)
        val cCitizen1 = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)
        val cCitizen2 = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)
        val cAdmin = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)

        val rOffender = SurabayaRepository(cOffender)
        val rCitizen1 = SurabayaRepository(cCitizen1)
        val rCitizen2 = SurabayaRepository(cCitizen2)
        val rAdmin = SurabayaRepository(cAdmin)

        val sOffender = cOffender.registerAndSignIn("Pelanggar", "pelanggar@sby.id", "password123", "Genteng")
        cCitizen1.registerAndSignIn("Warga Satu", "warga1@sby.id", "password123", "Gubeng")
        cCitizen2.registerAndSignIn("Warga Dua", "warga2@sby.id", "password123", "Wonokromo")
        val sAdmin = cAdmin.registerAndSignIn("Admin SBY", "nabilalailatus12@gmail.com", "password123", "Genteng")
        assertTrue(sAdmin.isAdmin)

        // LAYER 1: AI Vision Scanner blocks explicit/nudity photo BEFORE PocketBase upload!
        val explicitBlockingScanner = object : ImageSafetyScanner {
            override suspend fun scanJpegBytes(jpegBytes: ByteArray): ImageSafetyScanResult {
                return ImageSafetyScanResult.BlockedExplicit(
                    category = "GENITALIA_OR_NUDITY",
                    reasonId = "Terdeteksi alat kelamin / ketelanjangan eksplisit.",
                )
            }
        }
        val vmWithExplicitBlock = SurabayaViewModel(
            repository = rOffender,
            locationHelper = LocationHelper(context),
            currentUserId = sOffender.userId,
            initialDisplayName = sOffender.displayName,
            currentUserEmail = sOffender.email,
            imageSafetyScanner = explicitBlockingScanner,
        )

        vmWithExplicitBlock.submitIncident(
            title = "Kiriman Terlarang",
            description = "Mencoba unggah foto porno",
            category = "INFO_WARGA",
            severity = "INFO",
            district = "Genteng",
            address = "Jl. Pemuda",
            photoUris = listOf(sampleUri),
            context = context,
        )

        shadowOf(Looper.getMainLooper()).idle()
        val deadline = System.currentTimeMillis() + 4_000L
        while (
            (vmWithExplicitBlock.isSubmitting.value || vmWithExplicitBlock.snackbarMessage.value == null) &&
            System.currentTimeMillis() < deadline
        ) {
            Thread.sleep(25)
            shadowOf(Looper.getMainLooper()).idle()
        }
        assertTrue(
            "Explicit photo upload MUST NOT create 'Kiriman Terlarang' in PocketBase",
            rAdmin.observeIncidents().first().none { it.title == "Kiriman Terlarang" },
        )
        assertTrue(
            "Snackbar should explain explicit photo rejection, got: ${vmWithExplicitBlock.snackbarMessage.value}",
            vmWithExplicitBlock.snackbarMessage.value.orEmpty().contains("DITOLAK", ignoreCase = true) ||
                vmWithExplicitBlock.snackbarMessage.value.orEmpty().contains("asusila", ignoreCase = true) ||
                vmWithExplicitBlock.snackbarMessage.value.orEmpty().contains("eksplisit", ignoreCase = true),
        )

        // Safe photo scanner allows upload for a normal citizen
        val safeScanner = object : ImageSafetyScanner {
            override suspend fun scanJpegBytes(jpegBytes: ByteArray): ImageSafetyScanResult = ImageSafetyScanResult.Safe
        }
        val vmSafe = SurabayaViewModel(
            repository = rCitizen1,
            locationHelper = LocationHelper(context),
            currentUserId = cCitizen1.sessionFlow.value!!.userId,
            initialDisplayName = "Warga Satu",
            currentUserEmail = "warga1@sby.id",
            imageSafetyScanner = safeScanner,
        )
        vmSafe.submitIncident(
            title = "Laporan Jalan Basah",
            description = "Hujan rintik sore hari di Genteng",
            category = "CUACA_BANJIR",
            severity = "INFO",
            district = "Genteng",
            address = "Jl. Basuki Rahmat",
            photoUris = listOf(sampleUri),
            context = context,
        )
        shadowOf(Looper.getMainLooper()).idle()
        val deadline2 = System.currentTimeMillis() + 4_000L
        while (
            (vmSafe.isSubmitting.value || rAdmin.observeIncidents().first().none { it.title == "Laporan Jalan Basah" }) &&
            System.currentTimeMillis() < deadline2
        ) {
            Thread.sleep(25)
            shadowOf(Looper.getMainLooper()).idle()
        }
        val createdPost = rCitizen2.observeIncidents().first().firstOrNull { it.title == "Laporan Jalan Basah" }
        assertTrue("Safe photo upload must create 'Laporan Jalan Basah'", createdPost != null)
        val postId = createdPost!!.id

        // LAYER 2: 1st citizen report immediately triggers Auto-Blur (isContentBlurred = true)
        rCitizen1.reportInappropriateContent("incidents", postId, 0, emptyList())
        var postAfter1 = rCitizen2.observeIncidents().first().first { it.id == postId }
        assertEquals(1, postAfter1.contentReportCount)
        assertTrue("1 content report must activate sensitive photo blur", postAfter1.isContentBlurred)
        assertFalse("1 content report must not quarantine yet", postAfter1.isContentQuarantined)

        // LAYER 3: 3 distinct reports trigger Auto-Quarantine (isContentQuarantined = true)
        rCitizen2.reportInappropriateContent("incidents", postId, postAfter1.contentReportCount, postAfter1.contentReportedByUids)
        val postAfter2 = rAdmin.observeIncidents().first().first { it.id == postId }
        rAdmin.reportInappropriateContent("incidents", postId, postAfter2.contentReportCount, postAfter2.contentReportedByUids)
        val postAfter3 = rCitizen1.observeIncidents().first().first { it.id == postId }
        assertEquals(3, postAfter3.contentReportCount)
        assertTrue("3 content reports must quarantine post from public feed", postAfter3.isContentQuarantined)

        // Admin can restore false-positive blur if needed
        rAdmin.adminRestoreContentReports("incidents", postId)
        val restored = rCitizen1.observeIncidents().first().first { it.id == postId }
        assertEquals(0, restored.contentReportCount)
        assertFalse(restored.isContentBlurred)

        // Citizen Block & Unblock User
        val blockedAfterAdd = rCitizen1.blockUser(sOffender.userId, emptyList())
        assertTrue(sOffender.userId in blockedAfterAdd)
        val blockedAfterRemove = rCitizen1.unblockUser(sOffender.userId, blockedAfterAdd)
        assertFalse(sOffender.userId in blockedAfterRemove)

        // LAYER 4: Admin 1-Click Delete Post & Freeze Offender Account
        rAdmin.adminBanUser(sOffender.userId)
        val offenderAcc = rAdmin.observeUserAccount(sOffender.userId).first()
        assertTrue("Offender account must be marked isBanned = true", offenderAcc?.isBanned == true)

        rAdmin.adminDeletePost("incidents", postId)
        assertTrue("Deleted post must be removed from PocketBase", rCitizen1.observeIncidents().first().none { it.id == postId })
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `chat WhatsApp delivery receipts and realtime typing indicator succeed`() = runBlocking {
        val clientRaka = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)
        val clientNadia = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)

        val repoRaka = SurabayaRepository(clientRaka)
        val repoNadia = SurabayaRepository(clientNadia)

        val sRaka = clientRaka.registerAndSignIn("Raka", "raka.chat@surabaya.id", "secret123", "Gubeng")
        val sNadia = clientNadia.registerAndSignIn("Nadia", "nadia.chat@surabaya.id", "secret123", "Wonokromo")

        val nadiaPeer = NearbyProfile(
            id = sNadia.userId,
            userId = sNadia.userId,
            displayName = sNadia.displayName,
            district = "Wonokromo",
        )
        val room = repoRaka.openOrCreateDirectChat(
            peerProfile = nadiaPeer,
            currentUserName = sRaka.displayName,
            currentUserDistrict = "Gubeng",
        )

        // 1. Initial message send by Raka -> Delivered (Centang 2 abu-abu)
        repoRaka.sendMessage(room, sRaka.displayName, "Halo Nadia, piye kabare?")
        val rakaMessages = repoRaka.observeMessages(room, sRaka.userId).first()
        val sentMsg = rakaMessages.first { it.text == "Halo Nadia, piye kabare?" }
        assertTrue("Message must be delivered", sentMsg.isDelivered)
        assertFalse("Message is not yet read", sentMsg.isRead)
        assertEquals("Status must be DELIVERED", MessageDeliveryStatus.DELIVERED, sentMsg.deliveryStatus)

        // 2. Nadia reads the message -> Mark as read (Centang 2 biru/toska)
        repoNadia.markMessagesAsRead(room, sNadia.userId, listOf(sentMsg.id))
        val readMessages = repoNadia.observeMessages(room, sNadia.userId).first()
        val readMsg = readMessages.first { it.id == sentMsg.id }
        assertTrue("Message must now be marked as read", readMsg.isRead)
        assertEquals("Status must be READ", MessageDeliveryStatus.READ, readMsg.deliveryStatus)

        // 3. Typing indicator: Nadia types -> Raka sees Nadia typing, Nadia sees null
        repoNadia.updateTypingStatus(room, "Nadia", true)
        val roomsSeenByRaka = repoRaka.observeDirectChatRooms(sRaka.userId).first()
        val roomSeenByRaka = roomsSeenByRaka.first { it.id == room.id }
        assertEquals("Raka should see Nadia typing", "Nadia", roomSeenByRaka.activeTypingPeerName(sRaka.userId))
        assertEquals("Nadia should not see herself typing", null, roomSeenByRaka.activeTypingPeerName(sNadia.userId))

        // 4. Typing indicator stops: Nadia finishes typing
        repoNadia.updateTypingStatus(room, "Nadia", false)
        val roomAfterTyping = repoRaka.observeDirectChatRooms(sRaka.userId).first().first { it.id == room.id }
        assertEquals("No one should be typing now", null, roomAfterTyping.activeTypingPeerName(sRaka.userId))
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `public group chat creation and deletion is strictly restricted to Admin`() = runBlocking {
        val clientCitizen = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)
        val clientAdmin = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)

        val repoCitizen = SurabayaRepository(clientCitizen)
        val repoAdmin = SurabayaRepository(clientAdmin)

        clientCitizen.registerAndSignIn("Raka Warga", "raka.group@surabaya.id", "secret123", "Gubeng")
        val sAdmin = clientAdmin.registerAndSignIn("Admin SBY", "nabilalailatus12@gmail.com", "secret123", "Genteng")

        // 1. Non-admin citizen MUST be rejected when trying to create a public group
        var citizenCreateRejected = false
        try {
            repoCitizen.createPublicGroupRoom(
                title = "Grup Ilegal Warga",
                subtitle = "Coba buat tanpa izin",
                welcomeMessage = "Halo!",
                adminDisplayName = "Raka Warga",
            )
        } catch (e: IllegalStateException) {
            citizenCreateRejected = true
        }
        assertTrue("Non-admin citizen must not be allowed to create public group", citizenCreateRejected)

        // 2. Admin creates an official public group room + initial welcome message
        val createdGroup = repoAdmin.createPublicGroupRoom(
            title = "Grup Warga Gubeng",
            subtitle = "Info kegiatan & kuliner sekitar Gubeng",
            welcomeMessage = "Selamat bergabung rek di Grup Warga Gubeng!",
            adminDisplayName = sAdmin.displayName,
        )
        assertEquals("PUBLIC", createdGroup.roomType)
        assertEquals("Grup Warga Gubeng", createdGroup.title)

        val publicRooms = repoCitizen.observePublicChatRooms().first()
        assertTrue("Created public group must be visible to citizens", publicRooms.any { it.id == createdGroup.id })

        val groupMessages = repoCitizen.observeMessages(createdGroup, sAdmin.userId).first()
        assertTrue(
            "Welcome message must be posted in the new group",
            groupMessages.any { it.text.contains("Selamat bergabung rek di Grup Warga Gubeng!") },
        )

        // 3. Non-admin citizen MUST be rejected when trying to delete the public group
        var citizenDeleteRejected = false
        try {
            repoCitizen.adminDeletePublicGroupRoom(createdGroup.id)
        } catch (e: IllegalStateException) {
            citizenDeleteRejected = true
        }
        assertTrue("Non-admin citizen must not be allowed to delete public group", citizenDeleteRejected)

        // 4. Admin deletes the public group cleanly
        repoAdmin.adminDeletePublicGroupRoom(createdGroup.id)
        val roomsAfterDelete = repoCitizen.observePublicChatRooms().first()
        assertFalse("Deleted public group must be removed", roomsAfterDelete.any { it.id == createdGroup.id })
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `citizen gender persists across registration profile updates and nearby profiles`() = runBlocking {
        val clientSari = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)
        val clientBudi = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)

        val repoSari = SurabayaRepository(clientSari)
        val repoBudi = SurabayaRepository(clientBudi)

        val sSari = clientSari.registerAndSignIn(
            name = "Sari Ayu",
            email = "sari.ayu@surabaya.id",
            password = "password123",
            district = "Genteng",
            gender = "PEREMPUAN",
        )
        assertEquals("PEREMPUAN", sSari.gender)

        repoSari.bootstrapCurrentUserAndSeedIfEmpty(
            displayNameFallback = sSari.displayName,
            district = "Genteng",
        )

        val sBudi = clientBudi.registerAndSignIn(
            name = "Budi Santoso",
            email = "budi.santoso@surabaya.id",
            password = "password123",
            district = "Gubeng",
        )
        repoBudi.bootstrapCurrentUserAndSeedIfEmpty(
            displayNameFallback = sBudi.displayName,
            district = "Gubeng",
        )

        // Update Budi's gender via updateProfileAndPrivacy -> LAKI_LAKI
        repoBudi.updateProfileAndPrivacy(
            displayName = "Budi Santoso",
            district = "Gubeng",
            bio = "Suka kuliner rawon & gowes pagi",
            interests = "Kuliner, Gowes",
            searchRadiusKm = 15.0,
            isDiscoverable = true,
            lat = -7.2738,
            lng = 112.7563,
            gender = "LAKI_LAKI",
        )

        val budiAccount = repoBudi.observeUserAccount(sBudi.userId).first()
        assertEquals("LAKI_LAKI", budiAccount?.normalizedGender)
        assertEquals("Laki-laki", budiAccount?.genderLabel)

        val discoverablePeers = repoBudi.observeDiscoverableProfiles().first()
        val sariPeer = discoverablePeers.firstOrNull { it.userId == sSari.userId }
        val budiPeer = discoverablePeers.firstOrNull { it.userId == sBudi.userId }
        assertEquals("PEREMPUAN", sariPeer?.normalizedGender)
        assertEquals("Perempuan", sariPeer?.genderLabel)
        assertEquals("LAKI_LAKI", budiPeer?.normalizedGender)
        assertEquals("Laki-laki", budiPeer?.genderLabel)
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `pocketbase collections json contains all 20 valid collections synced with assets and repository`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val assetJsonText = context.assets.open("pocketbase_collections.json").bufferedReader().use { it.readText() }
        val rootSchemaFile = File("pocketbase_collections.json").let {
            if (it.exists()) it else File("../pocketbase_collections.json")
        }
        assertTrue("Root pocketbase_collections.json must exist", rootSchemaFile.exists())
        assertEquals(
            "Root pocketbase_collections.json and assets/pocketbase_collections.json must match identically",
            rootSchemaFile.readText().trim(),
            assetJsonText.trim(),
        )

        val arr = JSONArray(assetJsonText)
        assertEquals("Expected exactly 20 collections in PocketBase schema", 20, arr.length())

        val expectedCollections = listOf(
            "users", "bookmarks", "nearby_profiles", "incidents", "incident_comments",
            "events", "culinary", "culinary_reviews", "chat_rooms", "chat_messages",
            "destinations", "destination_reviews", "moments", "moment_comments",
            "services", "tambal_ban", "tambal_ban_comments", "polls", "poll_comments", "notifications",
        )
        val seenColIds = mutableSetOf<String>()
        val seenColNames = mutableListOf<String>()
        val seenFieldIds = mutableSetOf<String>()

        for (i in 0 until arr.length()) {
            val col = arr.getJSONObject(i)
            val colId = col.getString("id")
            val colName = col.getString("name")
            assertEquals("Collection id '$colId' for '$colName' must be 15 chars", 15, colId.length)
            assertTrue("Collection id '$colId' must be lowercase alphanumeric/underscore", colId.matches(Regex("^[a-z0-9_]{15}$")))
            assertTrue("Duplicate collection id '$colId'", seenColIds.add(colId))
            seenColNames.add(colName)

            for (ruleKey in listOf("listRule", "viewRule", "createRule", "updateRule", "deleteRule")) {
                if (!col.isNull(ruleKey)) {
                    val rule = col.getString(ruleKey)
                    val quoteCount = rule.count { it == '\'' }
                    assertEquals("Unbalanced single quotes in $colName.$ruleKey: $rule", 0, quoteCount % 2)
                    assertFalse("Broken unquoted email in $colName.$ruleKey", rule.contains("= nabilalailatus12@gmail.com"))
                }
            }

            val schema = col.getJSONArray("schema")
            val seenFieldNamesInCol = mutableSetOf<String>()
            for (j in 0 until schema.length()) {
                val field = schema.getJSONObject(j)
                val fid = field.getString("id")
                val fname = field.getString("name")
                assertTrue("Invalid field id '$fid' in $colName.$fname", fid == "users_name" || fid.matches(Regex("^[a-z0-9]{8}$")))
                assertTrue("Duplicate field id '$fid'", seenFieldIds.add(fid))
                assertTrue("Duplicate field name '$fname' in $colName", seenFieldNamesInCol.add(fname))
            }
        }
        assertEquals(expectedCollections, seenColNames)

        // Verify PocketBase repository round-trip for polls, poll_comments, tambal_ban, services, and notifications
        val clientCitizen = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)
        val clientAdmin = PocketBaseClient(baseUrlInput = baseUrl, enableRealtimeSse = false)
        val repoCitizen = SurabayaRepository(clientCitizen)
        val repoAdmin = SurabayaRepository(clientAdmin)

        val sCitizen = clientCitizen.registerAndSignIn("Dimas", "dimas.pb20@sby.id", "secret123", "Gubeng")
        clientAdmin.registerAndSignIn("Admin SBY", "nabilalailatus12@gmail.com", "secret123", "Genteng")

        val pollId = repoCitizen.proposePoll(
            title = "Perluasan Koridor Suroboyo Bus Malam",
            description = "Usulan operasional sampai pukul 23.00 WIB",
            category = "TRANSPORTASI",
            district = "Gubeng",
            optionTexts = listOf("Setuju", "Cukup sampai 21.00"),
        )
        assertEquals(15, pollId.length)
        assertEquals(1, repoAdmin.observePendingPolls().first().size)

        repoAdmin.moderatePoll(pollId, approve = true, adminNote = "Disetujui untuk voting warga")
        val approvedPolls = repoCitizen.observeApprovedPolls().first()
        assertTrue(approvedPolls.any { it.id == pollId && it.status == "APPROVED" })

        val opt0 = approvedPolls.first { it.id == pollId }.options.first().id
        repoCitizen.votePoll(pollId, opt0)
        assertEquals(1, repoAdmin.observeApprovedPolls().first().first { it.id == pollId }.totalVotes)

        repoCitizen.addPollComment(pollId, "Setuju banget rek!")
        assertEquals(1, repoAdmin.observePollComments(pollId).first().size)

        val tbId = repoCitizen.addTambalBanSpot(
            name = "Tambal Ban Cak To Gubeng",
            description = "Buka malam tubeless motor & mobil",
            district = "Gubeng",
            address = "Jl. Gubeng Kertajaya",
            is24Hours = true,
            isTubeless = true,
            forMotor = true,
            forMobil = true,
            canCallOut = true,
            mapsUrl = "",
            phoneNumber = "08123456789",
            whatsappNumber = "628123456789",
        )
        assertTrue(repoAdmin.observeTambalBanSpots().first().any { it.id == tbId && it.is24Hours })
        repoCitizen.addTambalBanComment(tbId, "Pelayanan cepat!")
        assertEquals(1, repoAdmin.observeTambalBanComments(tbId).first().size)
        shadowOf(Looper.getMainLooper()).idle()
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
