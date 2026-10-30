package com.example.data.util

import java.net.URI
import java.util.Locale

enum class SafeLinkCategory {
    GOOGLE_MAPS,
    GOFOOD,
    GRABFOOD,
    SHOPEEFOOD,
}

sealed interface LinkValidationResult {
    data class Valid(val normalizedUrl: String, val host: String) : LinkValidationResult
    data class Invalid(val errorMessage: String) : LinkValidationResult
}

data class PendingExternalLink(
    val url: String,
    val label: String,
    val host: String,
    val isTrustedOfficialDomain: Boolean,
)

/**
 * Multi-layered anti-phishing & link safety validator for Arek Surabaya.
 *
 * Protects citizens from fake APK downloads, deceptive shortlinks (bit.ly, tinyurl, etc.),
 * non-HTTPS schemes, credentials-in-URLs, raw IP phishing pages, and fake Google Maps / Food Delivery domains.
 */
object LinkSecurityValidator {

    private val blockedShorteners = setOf(
        "bit.ly",
        "tinyurl.com",
        "s.id",
        "cutt.ly",
        "rebrand.ly",
        "t.co",
        "is.gd",
        "ow.ly",
        "rb.gy",
        "shorturl.at",
        "adf.ly",
        "tiny.cc",
        "linktr.ee",
    )

    private val dangerousExtensions = listOf(
        ".apk",
        ".xapk",
        ".exe",
        ".bat",
        ".scr",
        ".msi",
        ".sh",
        ".jar",
        ".dex",
        ".zip",
        ".rar",
    )

    private val ipv4Regex = Regex("^\\d{1,3}(\\.\\d{1,3}){3}$")

    fun validateOptionalLink(
        rawInput: String,
        category: SafeLinkCategory,
    ): LinkValidationResult {
        val trimmed = rawInput.trim()
        if (trimmed.isBlank() || trimmed.equals("null", ignoreCase = true)) {
            return LinkValidationResult.Valid(normalizedUrl = "", host = "")
        }

        if (trimmed.length > 500) {
            return LinkValidationResult.Invalid("Panjang link melebihi batas aman (maksimal 500 karakter).")
        }

        if (trimmed.any { it.isWhitespace() }) {
            return LinkValidationResult.Invalid("Link tidak boleh mengandung spasi. Salin 1 link utuh.")
        }

        val lowerInput = trimmed.lowercase(Locale.ROOT)
        if (
            lowerInput.startsWith("javascript:") ||
            lowerInput.startsWith("data:") ||
            lowerInput.startsWith("file:") ||
            lowerInput.startsWith("intent:") ||
            lowerInput.startsWith("vbscript:")
        ) {
            return LinkValidationResult.Invalid("Format link berbahaya terdeteksi dan diblokir demi keamanan warga.")
        }

        if (lowerInput.startsWith("http://")) {
            return LinkValidationResult.Invalid("Demi keamanan warga, hanya link terenkripsi https:// yang diizinkan.")
        }

        val candidate = if (lowerInput.startsWith("https://")) {
            trimmed
        } else if (trimmed.contains("://")) {
            return LinkValidationResult.Invalid("Skema link tidak dikenali. Gunakan link resmi diawali https://.")
        } else {
            "https://$trimmed"
        }

        val parsed = try {
            URI(candidate)
        } catch (_: Exception) {
            return LinkValidationResult.Invalid("Format URL tidak valid. Pastikan menyalin link asli dari aplikasi resmi.")
        }

        val scheme = parsed.scheme?.lowercase(Locale.ROOT).orEmpty()
        if (scheme != "https") {
            return LinkValidationResult.Invalid("Hanya link resmi dengan protokol aman (HTTPS) yang diizinkan.")
        }

        if (!parsed.userInfo.isNullOrBlank()) {
            return LinkValidationResult.Invalid("Link mencurigakan (mengandung karakter samaran '@') tidak diizinkan.")
        }

        val host = parsed.host?.lowercase(Locale.ROOT)?.removePrefix("www.")?.trim('.').orEmpty()
        if (host.isBlank() || !host.contains('.')) {
            return LinkValidationResult.Invalid("Domain link tidak valid.")
        }

        if (ipv4Regex.matches(host) || host.startsWith("[")) {
            return LinkValidationResult.Invalid("Link alamat IP mentah diblokir demi mencegah situs jebakan/phishing.")
        }

        if (blockedShorteners.any { host == it || host.endsWith(".$it") }) {
            return LinkValidationResult.Invalid(
                "Pemendek link umum ($host) ditolak karena rawan menyembunyikan link palsu. Gunakan link langsung dari aplikasinya.",
            )
        }

        val pathLower = (parsed.rawPath ?: "").lowercase(Locale.ROOT)
        val queryLower = (parsed.rawQuery ?: "").lowercase(Locale.ROOT)
        if (dangerousExtensions.any { ext -> pathLower.endsWith(ext) || queryLower.contains(ext) }) {
            return LinkValidationResult.Invalid(
                "Link unduhan aplikasi/file berbahaya (.apk/.zip) diblokir demi keamanan warga!",
            )
        }

        return when (category) {
            SafeLinkCategory.GOOGLE_MAPS -> validateGoogleMapsHostAndPath(candidate, host, pathLower)
            SafeLinkCategory.GOFOOD -> validateGoFoodHost(candidate, host)
            SafeLinkCategory.GRABFOOD -> validateGrabFoodHost(candidate, host)
            SafeLinkCategory.SHOPEEFOOD -> validateShopeeFoodHost(candidate, host)
        }
    }

    private fun validateGoogleMapsHostAndPath(
        normalizedUrl: String,
        host: String,
        pathLower: String,
    ): LinkValidationResult {
        val isMapsShortLink = host == "maps.app.goo.gl"
        val isLegacyGooGlMaps = host == "goo.gl" && pathLower.startsWith("/maps")
        val isMapsSubdomain = host == "maps.google.com" || host == "maps.google.co.id"
        val isGoogleMainMaps = (host == "google.com" || host == "google.co.id") && pathLower.startsWith("/maps")

        return if (isMapsShortLink || isLegacyGooGlMaps || isMapsSubdomain || isGoogleMainMaps) {
            LinkValidationResult.Valid(normalizedUrl = normalizedUrl, host = host)
        } else {
            LinkValidationResult.Invalid(
                "Hanya link resmi Google Maps yang diizinkan (contoh: maps.app.goo.gl/... atau google.com/maps/...).",
            )
        }
    }

    private fun validateGoFoodHost(normalizedUrl: String, host: String): LinkValidationResult {
        val allowed = host == "gofood.link" ||
            host.endsWith(".gofood.link") ||
            host == "gofood.co.id" ||
            host.endsWith(".gofood.co.id") ||
            host == "gojek.link" ||
            host.endsWith(".gojek.link")
        return if (allowed) {
            LinkValidationResult.Valid(normalizedUrl = normalizedUrl, host = host)
        } else {
            LinkValidationResult.Invalid(
                "Hanya link resmi GoFood yang diizinkan (gofood.link, gofood.co.id, atau gojek.link).",
            )
        }
    }

    private fun validateGrabFoodHost(normalizedUrl: String, host: String): LinkValidationResult {
        val allowed = host == "r.grab.com" ||
            host == "food.grab.com" ||
            host == "grab.com" ||
            host.endsWith(".grab.com")
        return if (allowed) {
            LinkValidationResult.Valid(normalizedUrl = normalizedUrl, host = host)
        } else {
            LinkValidationResult.Invalid(
                "Hanya link resmi GrabFood yang diizinkan (food.grab.com atau r.grab.com).",
            )
        }
    }

    private fun validateShopeeFoodHost(normalizedUrl: String, host: String): LinkValidationResult {
        val allowed = host == "shopee.co.id" ||
            host.endsWith(".shopee.co.id") ||
            host == "shp.ee" ||
            host.endsWith(".shp.ee") ||
            host == "shopeefood.co.id" ||
            host.endsWith(".shopeefood.co.id")
        return if (allowed) {
            LinkValidationResult.Valid(normalizedUrl = normalizedUrl, host = host)
        } else {
            LinkValidationResult.Invalid(
                "Hanya link resmi ShopeeFood yang diizinkan (shopee.co.id atau shp.ee).",
            )
        }
    }

    /**
     * Sanitizes URLs coming from or going to the database.
     * If a stored URL does not pass the category whitelist, returns `""` so malicious/legacy fake links
     * are automatically neutralized and never exposed to citizens.
     */
    fun sanitizeOrEmpty(rawInput: String, category: SafeLinkCategory): String {
        return when (val res = validateOptionalLink(rawInput, category)) {
            is LinkValidationResult.Valid -> res.normalizedUrl
            is LinkValidationResult.Invalid -> ""
        }
    }

    /**
     * Builds a transparent preview object for the Safe External Link Confirmation Dialog.
     */
    fun inspectForConfirmation(rawUrl: String, label: String, expectedCategory: SafeLinkCategory): PendingExternalLink? {
        val res = validateOptionalLink(rawUrl, expectedCategory)
        if (res !is LinkValidationResult.Valid || res.normalizedUrl.isBlank()) return null
        return PendingExternalLink(
            url = res.normalizedUrl,
            label = label,
            host = res.host,
            isTrustedOfficialDomain = true,
        )
    }

    /**
     * Encodes/decodes link report UIDs on PocketBase records without breaking backward compatibility.
     */
    fun decodeUidPipeList(raw: String): List<String> {
        if (raw.isBlank() || raw == "null") return emptyList()
        return raw.split("|").map { it.trim() }.filter { it.isNotBlank() }.distinct()
    }

    fun encodeUidPipeList(uids: List<String>): String {
        return uids.map { it.trim() }.filter { it.isNotBlank() }.distinct().joinToString("|").take(1950)
    }

    const val AUTO_HIDE_LINK_REPORT_THRESHOLD = 3
}
