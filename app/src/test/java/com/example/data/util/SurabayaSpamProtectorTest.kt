package com.example.data.util

import com.example.data.model.CitizenService
import com.example.data.model.CulinarySpot
import com.example.data.model.IncidentReport
import com.example.data.model.SurabayaEvent
import com.example.data.model.UserMoment
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SurabayaSpamProtectorTest {

    @Before
    fun setUp() {
        SurabayaSpamProtector.resetForTesting()
    }

    @Test
    fun `text normalization collapses whitespace strips punctuation and lowercases`() {
        val raw = "  PROMOSI Jasa AC Murah, Daerah Gubeng!!  \n  Mantap 👍 "
        val normalized = SurabayaSpamProtector.normalizeText(raw)
        assertEquals("promosi jasa ac murah daerah gubeng mantap", normalized)
    }

    @Test
    fun `similarity check detects identical and minor punctuation variations`() {
        val s1 = "Jasa service cuci AC panggilan Surabaya"
        val s2 = "Jasa service cuci AC panggilan Surabaya..."
        val s3 = "JASA SERVICE CUCI AC PANGGILAN SURABAYA!!!"
        val s4 = "Jual nasi goreng pedas pasar malam"

        assertTrue("Identical texts after normalization should have similarity ~1.0", SurabayaSpamProtector.computeSimilarity(s1, s2) >= 0.95)
        assertTrue("Case and punctuation variations should have similarity ~1.0", SurabayaSpamProtector.computeSimilarity(s1, s3) >= 0.95)
        assertTrue("Completely different texts should have low similarity", SurabayaSpamProtector.computeSimilarity(s1, s4) < 0.3)
    }

    @Test
    fun `service duplicate prevention blocks identical and near identical services by same user`() {
        val uid = "user_eko_1"
        val existing = listOf(
            CitizenService(
                id = "srv_1",
                providerId = uid,
                title = "Servis AC Bergaransi Rungkut",
                description = "Menerima cuci AC split, isi freon, dan perbaikan mati total.",
                phoneNumber = "08123456789",
            ),
        )

        // 1. Exact duplicate title & description is blocked
        val exactResult = SurabayaSpamProtector.checkServiceDuplicate(
            userId = uid,
            title = "Servis AC Bergaransi Rungkut",
            description = "Menerima cuci AC split, isi freon, dan perbaikan mati total.",
            phoneNumber = "08123456789",
            existingServices = existing,
        )
        assertTrue("Exact service duplicate must be blocked", exactResult is SpamCheckResult.Blocked)
        assertTrue((exactResult as SpamCheckResult.Blocked).isDuplicate)

        // 2. Minor variation (added dots & case) is blocked
        val variationResult = SurabayaSpamProtector.checkServiceDuplicate(
            userId = uid,
            title = "SERVIS AC BERGARANSI RUNGKUT...",
            description = "Menerima cuci AC split, isi freon, dan perbaikan mati total!",
            phoneNumber = "08123456789",
            existingServices = existing,
        )
        assertTrue("Minor variation service duplicate must be blocked", variationResult is SpamCheckResult.Blocked)

        // 3. Different service by the same user is allowed
        val differentResult = SurabayaSpamProtector.checkServiceDuplicate(
            userId = uid,
            title = "Jasa Pemasangan Kanopi Baja Ringan",
            description = "Pemasangan kanopi galvalum dan polycarbonate garansi 1 tahun.",
            phoneNumber = "08123456789",
            existingServices = existing,
        )
        assertTrue("Distinct service should be allowed", differentResult is SpamCheckResult.Allowed)

        // 4. Same service by a DIFFERENT user is allowed
        val otherUserResult = SurabayaSpamProtector.checkServiceDuplicate(
            userId = "user_budi_2",
            title = "Servis AC Bergaransi Rungkut",
            description = "Menerima cuci AC split, isi freon, dan perbaikan mati total.",
            phoneNumber = "08199988877",
            existingServices = existing,
        )
        assertTrue("Service by a different citizen should be allowed", otherUserResult is SpamCheckResult.Allowed)
    }

    @Test
    fun `moment duplicate check blocks exact and similar caption by same user`() {
        val uid = "user_siti_1"
        val existing = listOf(
            UserMoment(
                id = "mom_1",
                authorId = uid,
                caption = "Suasana sore di Alun-Alun Surabaya sangat syahdu rek!",
            ),
        )

        val dupResult = SurabayaSpamProtector.checkMomentDuplicate(
            userId = uid,
            caption = "Suasana sore di Alun-Alun Surabaya sangat syahdu rek!...",
            existingMoments = existing,
        )
        assertTrue("Duplicate moment caption must be blocked", dupResult is SpamCheckResult.Blocked)

        val allowedResult = SurabayaSpamProtector.checkMomentDuplicate(
            userId = uid,
            caption = "Pagi hari sejuk olahraga keliling Taman Harmoni Keputih",
            existingMoments = existing,
        )
        assertTrue("Fresh moment caption should be allowed", allowedResult is SpamCheckResult.Allowed)
    }

    @Test
    fun `culinary duplicate check blocks duplicate spot name and district by same user`() {
        val uid = "user_kuliner_1"
        val existing = listOf(
            CulinarySpot(
                id = "cul_1",
                createdBy = uid,
                name = "Bebek Sinjay Tidar",
                district = "Sawahan",
                description = "Bebek kremes bumbu mangga muda khas Madura.",
            ),
        )

        val dupResult = SurabayaSpamProtector.checkCulinaryDuplicate(
            userId = uid,
            name = "Bebek Sinjay Tidar",
            district = "Sawahan",
            description = "Bebek kremes bumbu mangga.",
            existingCulinary = existing,
        )
        assertTrue("Duplicate culinary spot must be blocked", dupResult is SpamCheckResult.Blocked)

        val freshResult = SurabayaSpamProtector.checkCulinaryDuplicate(
            userId = uid,
            name = "Soto Ayam Cak Har",
            district = "Mulyorejo",
            description = "Soto ayam kuah koya gurih.",
            existingCulinary = existing,
        )
        assertTrue("Distinct culinary spot should be allowed", freshResult is SpamCheckResult.Allowed)
    }

    @Test
    fun `idempotent event attendance toggle guarantees 1 user equals 1 count and toggles correctly`() {
        val userA = "usr_alif"
        val userB = "usr_bambang"

        // Step 1: Initial empty list -> User A joins
        val (listAfterA, countAfterA) = SurabayaSpamProtector.toggleEventAttendance(emptyList(), userA)
        assertEquals(1, countAfterA)
        assertEquals(listOf(userA), listAfterA)

        // Step 2: User B joins -> count is 2
        val (listAfterB, countAfterB) = SurabayaSpamProtector.toggleEventAttendance(listAfterA, userB)
        assertEquals(2, countAfterB)
        assertEquals(setOf(userA, userB), listAfterB.toSet())

        // Step 3: User A toggles again -> cancels attendance -> count drops to 1
        val (listAfterACancel, countAfterACancel) = SurabayaSpamProtector.toggleEventAttendance(listAfterB, userA)
        assertEquals(1, countAfterACancel)
        assertEquals(listOf(userB), listAfterACancel)

        // Step 4: Rapid spamming safety: even if called repeatedly with dirty list containing duplicate strings,
        // it sanitizes and always sets count to EXACT unique set size
        val dirtyList = listOf(userA, userA, userA, userB, userB, "  ", "")
        val (sanitizedList, sanitizedCount) = SurabayaSpamProtector.toggleEventAttendance(dirtyList, userA)
        // User A was present in dirty list -> toggle removes User A, leaves only User B
        assertEquals(1, sanitizedCount)
        assertEquals(listOf(userB), sanitizedList)
    }

    @Test
    fun `event interest toggle debounce prevents rapid spam clicking within 600ms`() {
        val user = "usr_test_debounce"
        val eventId = "ev_festival_1"

        assertTrue("First click should be permitted", SurabayaSpamProtector.canToggleEventInterest(user, eventId))
        assertFalse("Second click immediately after must be debounced/blocked", SurabayaSpamProtector.canToggleEventInterest(user, eventId))
    }

    @Test
    fun `rate limit cooldown enforces minimum wait interval between submissions`() {
        val user = "usr_fast_poster"
        val cat = "moments"

        // First check: allowed
        val res1 = SurabayaSpamProtector.checkRateLimit(user, cat, 5000L)
        assertTrue(res1 is SpamCheckResult.Allowed)

        // Record submission
        SurabayaSpamProtector.recordSubmission(user, cat)

        // Immediate next check: blocked due to rate limit cooldown
        val res2 = SurabayaSpamProtector.checkRateLimit(user, cat, 5000L)
        assertTrue(res2 is SpamCheckResult.Blocked)
        assertTrue((res2 as SpamCheckResult.Blocked).isRateLimit)
        assertTrue((res2 as SpamCheckResult.Blocked).remainingSeconds > 0)
    }
}
