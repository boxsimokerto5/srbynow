package com.example.data.util

import java.nio.charset.StandardCharsets
import java.util.Base64

data class ChatReplyMetadata(
    val replyToId: String,
    val replyToSender: String,
    val replyToSnippet: String,
    val cleanText: String,
)

/**
 * Encodes and decodes WhatsApp-style reply quote metadata so that replies work seamlessly
 * both on servers that have the dedicated `reply_to_*` PocketBase columns AND on existing live
 * PocketBase servers before schema re-import (via compact header prefix).
 */
object ChatReplyHelper {
    private const val REPLY_PREFIX = "⟦R1:"
    private const val REPLY_SUFFIX = "⟧\n"
    private const val MAX_SNIPPET_LENGTH = 90
    private const val MAX_SENDER_LENGTH = 48

    fun sanitizeSnippet(rawText: String, hasPhoto: Boolean = false): String {
        val stripped = parseEmbeddedReply(rawText)?.cleanText ?: rawText
        val normalized = stripped
            .replace('\n', ' ')
            .replace('\r', ' ')
            .replace(Regex("\\s+"), " ")
            .trim()
        return when {
            normalized.isBlank() && hasPhoto -> "📷 Foto"
            normalized == "📷 Foto" -> "📷 Foto"
            hasPhoto && !normalized.startsWith("📷") -> "📷 ${normalized.take(MAX_SNIPPET_LENGTH)}"
            else -> normalized.take(MAX_SNIPPET_LENGTH)
        }
    }

    fun encodeTextWithReply(
        bodyText: String,
        replyToId: String,
        replyToSender: String,
        replyToSnippet: String,
    ): String {
        val cleanId = replyToId.trim().take(64)
        if (cleanId.isEmpty()) return bodyText.trim()

        val cleanSender = replyToSender.trim().take(MAX_SENDER_LENGTH).ifEmpty { "Warga Surabaya" }
        val cleanSnippet = replyToSnippet
            .replace('\n', ' ')
            .replace('\r', ' ')
            .trim()
            .take(MAX_SNIPPET_LENGTH)
            .ifEmpty { "Pesan" }

        val senderB64 = urlSafeBase64Encode(cleanSender)
        val snippetB64 = urlSafeBase64Encode(cleanSnippet)
        val header = "$REPLY_PREFIX$cleanId|$senderB64|$snippetB64$REPLY_SUFFIX"
        val maxBody = (1000 - header.length).coerceAtLeast(1)
        val effectiveBody = bodyText.trim().take(maxBody)
        return header + effectiveBody
    }

    fun parseEmbeddedReply(rawText: String): ChatReplyMetadata? {
        if (!rawText.startsWith(REPLY_PREFIX)) return null
        val endIdx = rawText.indexOf(REPLY_SUFFIX)
        if (endIdx <= REPLY_PREFIX.length) return null

        val payload = rawText.substring(REPLY_PREFIX.length, endIdx)
        val parts = payload.split("|")
        if (parts.size < 3) return null

        val replyId = parts[0].trim()
        val sender = urlSafeBase64Decode(parts[1]).ifBlank { "Warga Surabaya" }
        val snippet = urlSafeBase64Decode(parts[2]).ifBlank { "Pesan" }
        val cleanBody = rawText.substring(endIdx + REPLY_SUFFIX.length)
        if (replyId.isBlank()) return null

        return ChatReplyMetadata(
            replyToId = replyId,
            replyToSender = sender,
            replyToSnippet = snippet,
            cleanText = cleanBody,
        )
    }

    fun stripReplyMarkerForPreview(rawText: String): String {
        val parsed = parseEmbeddedReply(rawText) ?: return rawText
        return parsed.cleanText.ifBlank { "↩️ ${parsed.replyToSnippet}" }
    }

    private fun urlSafeBase64Encode(value: String): String {
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(value.toByteArray(StandardCharsets.UTF_8))
    }

    private fun urlSafeBase64Decode(encoded: String): String {
        return try {
            String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8)
        } catch (_: Exception) {
            ""
        }
    }
}
