package com.example.data.util

import com.example.data.model.CitizenService
import com.example.data.model.CulinarySpot
import com.example.data.model.IncidentReport
import com.example.data.model.SurabayaEvent
import com.example.data.model.UserMoment
import java.security.MessageDigest
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlin.math.min

sealed interface SpamCheckResult {
    data object Allowed : SpamCheckResult
    data class Blocked(
        val reason: String,
        val isDuplicate: Boolean = false,
        val isRateLimit: Boolean = false,
        val remainingSeconds: Long = 0L,
    ) : SpamCheckResult
}

class DuplicateContentException(
    val userFriendlyMessage: String,
) : IllegalArgumentException(userFriendlyMessage)

class SpamRateLimitException(
    val userFriendlyMessage: String,
    val remainingSeconds: Long = 0L,
) : IllegalStateException(userFriendlyMessage)

object SurabayaSpamProtector {

    // Minimum cooldowns in milliseconds between new submissions per category
    const val COOLDOWN_SERVICES_MS = 25_000L
    const val COOLDOWN_MOMENTS_MS = 15_000L
    const val COOLDOWN_CULINARY_MS = 25_000L
    const val COOLDOWN_INCIDENTS_MS = 15_000L
    const val COOLDOWN_EVENTS_MS = 25_000L
    const val COOLDOWN_EVENT_INTEREST_CLICK_MS = 600L

    // Tracks last submission timestamp: "$userId:$category" -> timestamp
    private val lastSubmissions = ConcurrentHashMap<String, Long>()

    // Tracks last event interest toggle click: "$userId:$eventId" -> timestamp
    private val lastInterestClicks = ConcurrentHashMap<String, Long>()

    fun resetForTesting() {
        lastSubmissions.clear()
        lastInterestClicks.clear()
    }

    /**
     * Normalizes text for comparison:
     * - Lowercase
     * - Strips emojis, punctuation, and non-alphanumeric characters
     * - Normalizes repeated whitespace
     */
    fun normalizeText(input: String): String {
        if (input.isBlank()) return ""
        val clean = StringBuilder(input.length)
        for (ch in input.lowercase()) {
            if (ch.isLetterOrDigit() || ch.isWhitespace()) {
                clean.append(ch)
            } else {
                clean.append(' ')
            }
        }
        return clean.toString().replace(Regex("\\s+"), " ").trim()
    }

    /**
     * Computes a SHA-256 fingerprint from normalized fields.
     */
    fun computeFingerprint(vararg parts: String): String {
        val combined = parts.joinToString("|") { normalizeText(it) }
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Computes similarity between two texts (0.0 to 1.0) using token overlap and Levenshtein distance.
     */
    fun computeSimilarity(textA: String, textB: String): Double {
        val normA = normalizeText(textA)
        val normB = normalizeText(textB)

        if (normA == normB) return 1.0
        if (normA.isEmpty() || normB.isEmpty()) return 0.0

        // 1. Token Jaccard similarity
        val tokensA = normA.split(" ").filter { it.isNotBlank() }.toSet()
        val tokensB = normB.split(" ").filter { it.isNotBlank() }.toSet()
        val intersectionSize = tokensA.intersect(tokensB).size
        val unionSize = tokensA.union(tokensB).size
        val tokenSimilarity = if (unionSize > 0) intersectionSize.toDouble() / unionSize.toDouble() else 0.0

        // 2. Character Levenshtein distance for short strings or title checks
        val maxLen = max(normA.length, normB.length)
        val levenshteinSim = if (maxLen <= 200) {
            val dist = levenshteinDistance(normA, normB)
            1.0 - (dist.toDouble() / maxLen.toDouble())
        } else {
            tokenSimilarity
        }

        return max(tokenSimilarity, levenshteinSim)
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val m = s1.length
        val n = s2.length
        var prev = IntArray(n + 1) { it }
        var curr = IntArray(n + 1)

        for (i in 1..m) {
            curr[0] = i
            val c1 = s1[i - 1]
            for (j in 1..n) {
                val cost = if (c1 == s2[j - 1]) 0 else 1
                curr[j] = min(
                    min(curr[j - 1] + 1, prev[j] + 1),
                    prev[j - 1] + cost,
                )
            }
            val temp = prev
            prev = curr
            curr = temp
        }
        return prev[n]
    }

    /**
     * Checks if user is in rate-limit cooldown for a category.
     */
    fun checkRateLimit(userId: String, category: String, cooldownMs: Long): SpamCheckResult {
        if (userId.isBlank()) return SpamCheckResult.Allowed
        val key = "$userId:$category"
        val lastTime = lastSubmissions[key] ?: return SpamCheckResult.Allowed
        val elapsed = System.currentTimeMillis() - lastTime
        if (elapsed < cooldownMs) {
            val remainingSec = ((cooldownMs - elapsed) / 1000L).coerceAtLeast(1L)
            return SpamCheckResult.Blocked(
                reason = "Mohon tunggu $remainingSec detik sebelum membuat kiriman baru (jeda anti-spam).",
                isRateLimit = true,
                remainingSeconds = remainingSec,
            )
        }
        return SpamCheckResult.Allowed
    }

    fun recordSubmission(userId: String, category: String) {
        if (userId.isNotBlank()) {
            lastSubmissions["$userId:$category"] = System.currentTimeMillis()
        }
    }

    // --- Specific Category Checks ---

    fun checkServiceDuplicate(
        userId: String,
        title: String,
        description: String,
        phoneNumber: String = "",
        whatsappNumber: String = "",
        existingServices: List<CitizenService>,
    ): SpamCheckResult {
        val rateLimit = checkRateLimit(userId, "services", COOLDOWN_SERVICES_MS)
        if (rateLimit is SpamCheckResult.Blocked) return rateLimit

        val normTitle = normalizeText(title)
        val normDesc = normalizeText(description)

        val userServices = existingServices.filter { it.providerId == userId }
        for (item in userServices) {
            val existTitle = normalizeText(item.title)
            val existDesc = normalizeText(item.description)

            // Exact match
            if (normTitle == existTitle && normDesc == existDesc) {
                return SpamCheckResult.Blocked(
                    reason = "Anda sudah pernah memposting promosi jasa yang sama persis. Cukup perbarui kiriman Anda sebelumnya.",
                    isDuplicate = true,
                )
            }

            // High similarity match (e.g. adding dots, spaces, or minor punctuation changes)
            val titleSim = computeSimilarity(normTitle, existTitle)
            val descSim = computeSimilarity(normDesc, existDesc)
            if (titleSim >= 0.85 && (descSim >= 0.75 || normDesc.isBlank() || existDesc.isBlank())) {
                return SpamCheckResult.Blocked(
                    reason = "Promosi jasa dengan nama dan rincian ini sudah ada di daftar layanan Anda.",
                    isDuplicate = true,
                )
            }
        }
        return SpamCheckResult.Allowed
    }

    fun checkMomentDuplicate(
        userId: String,
        caption: String,
        existingMoments: List<UserMoment>,
    ): SpamCheckResult {
        val rateLimit = checkRateLimit(userId, "moments", COOLDOWN_MOMENTS_MS)
        if (rateLimit is SpamCheckResult.Blocked) return rateLimit

        val normCaption = normalizeText(caption)
        if (normCaption.length < 5) return SpamCheckResult.Allowed

        val userMoments = existingMoments.filter { it.authorId == userId }
        for (item in userMoments) {
            val existCaption = normalizeText(item.caption)
            if (normCaption == existCaption) {
                return SpamCheckResult.Blocked(
                    reason = "Anda sudah pernah membagikan momen dengan teks yang sama persis. Bagikan cerita atau suasana yang berbeda ya rek!",
                    isDuplicate = true,
                )
            }
            if (computeSimilarity(normCaption, existCaption) >= 0.88) {
                return SpamCheckResult.Blocked(
                    reason = "Momen serupa sudah pernah Anda posting sebelumnya.",
                    isDuplicate = true,
                )
            }
        }
        return SpamCheckResult.Allowed
    }

    fun checkCulinaryDuplicate(
        userId: String,
        name: String,
        district: String,
        description: String,
        existingCulinary: List<CulinarySpot>,
    ): SpamCheckResult {
        val rateLimit = checkRateLimit(userId, "culinary", COOLDOWN_CULINARY_MS)
        if (rateLimit is SpamCheckResult.Blocked) return rateLimit

        val normName = normalizeText(name)
        val normDistrict = normalizeText(district)

        val userSpots = existingCulinary.filter { it.createdBy == userId }
        for (item in userSpots) {
            val existName = normalizeText(item.name)
            val existDistrict = normalizeText(item.district)

            if (normName == existName && (normDistrict == existDistrict || normDistrict.isBlank())) {
                return SpamCheckResult.Blocked(
                    reason = "Tempat kuliner '$name' sudah pernah Anda daftarkan di Arek Surabaya.",
                    isDuplicate = true,
                )
            }
            if (computeSimilarity(normName, existName) >= 0.88 && normDistrict == existDistrict) {
                return SpamCheckResult.Blocked(
                    reason = "Kuliner serupa di kecamatan yang sama sudah ada di daftar Anda.",
                    isDuplicate = true,
                )
            }
        }
        return SpamCheckResult.Allowed
    }

    fun checkIncidentDuplicate(
        userId: String,
        title: String,
        address: String,
        description: String,
        existingIncidents: List<IncidentReport>,
    ): SpamCheckResult {
        val rateLimit = checkRateLimit(userId, "incidents", COOLDOWN_INCIDENTS_MS)
        if (rateLimit is SpamCheckResult.Blocked) return rateLimit

        val normTitle = normalizeText(title)
        val normAddress = normalizeText(address)

        val userIncidents = existingIncidents.filter { it.authorId == userId }
        for (item in userIncidents) {
            val existTitle = normalizeText(item.title)
            val existAddress = normalizeText(item.address)

            if (normTitle == existTitle && normAddress == existAddress) {
                return SpamCheckResult.Blocked(
                    reason = "Laporan kejadian yang sama persis sudah pernah Anda kirimkan. Anda dapat memantau atau berkomentar di laporan tersebut.",
                    isDuplicate = true,
                )
            }
            if (computeSimilarity(normTitle, existTitle) >= 0.88) {
                return SpamCheckResult.Blocked(
                    reason = "Laporan kejadian serupa sudah pernah Anda kirimkan.",
                    isDuplicate = true,
                )
            }
        }
        return SpamCheckResult.Allowed
    }

    fun checkEventProposalDuplicate(
        userId: String,
        title: String,
        venueName: String,
        eventDateText: String,
        existingEvents: List<SurabayaEvent>,
    ): SpamCheckResult {
        val rateLimit = checkRateLimit(userId, "events", COOLDOWN_EVENTS_MS)
        if (rateLimit is SpamCheckResult.Blocked) return rateLimit

        val normTitle = normalizeText(title)
        val normVenue = normalizeText(venueName)

        val userEvents = existingEvents.filter { it.proposerId == userId }
        for (item in userEvents) {
            val existTitle = normalizeText(item.title)
            val existVenue = normalizeText(item.venueName)

            if (normTitle == existTitle && (normVenue == existVenue || normVenue.isBlank())) {
                return SpamCheckResult.Blocked(
                    reason = "Usulan event '$title' sudah pernah Anda ajukan dan sedang dalam antrean moderasi Admin.",
                    isDuplicate = true,
                )
            }
            if (computeSimilarity(normTitle, existTitle) >= 0.88) {
                return SpamCheckResult.Blocked(
                    reason = "Usulan event serupa sudah pernah Anda ajukan.",
                    isDuplicate = true,
                )
            }
        }
        return SpamCheckResult.Allowed
    }

    // --- Event Attendance / Hadir Deduplication Logic ---

    /**
     * Checks if a user is clicking "Hadir / Tertarik" too rapidly on the same event.
     * Prevents fast clicking spam and race conditions.
     */
    fun canToggleEventInterest(userId: String, eventId: String): Boolean {
        if (userId.isBlank() || eventId.isBlank()) return true
        val key = "$userId:$eventId"
        val now = System.currentTimeMillis()
        val lastClick = lastInterestClicks[key] ?: 0L
        if (now - lastClick < COOLDOWN_EVENT_INTEREST_CLICK_MS) {
            return false // Click ignored due to rapid debounce
        }
        lastInterestClicks[key] = now
        return true
    }

    /**
     * Guaranteed Idempotent Unique Attendance Toggle:
     * - Uses a Set to ensure 1 user ID can NEVER exist more than once.
     * - The attendance count is STRICTLY derived from the size of the set.
     * - Returns Pair(newUniqueList, newExactCount).
     */
    fun toggleEventAttendance(
        currentUids: List<String>,
        userId: String,
    ): Pair<List<String>, Int> {
        val uniqueSet = LinkedHashSet<String>()
        for (uid in currentUids) {
            val clean = uid.trim()
            if (clean.isNotBlank()) {
                uniqueSet.add(clean)
            }
        }

        val cleanUser = userId.trim()
        if (cleanUser.isNotBlank()) {
            if (cleanUser in uniqueSet) {
                uniqueSet.remove(cleanUser)
            } else {
                if (uniqueSet.size < 200) {
                    uniqueSet.add(cleanUser)
                }
            }
        }

        val resultList = uniqueSet.toList()
        return Pair(resultList, resultList.size)
    }
}
