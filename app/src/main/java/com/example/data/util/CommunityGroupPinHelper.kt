package com.example.data.util

import java.security.MessageDigest

object CommunityGroupPinHelper {
    const val PIN_LENGTH = 4
    const val MAX_GROUPS_PER_CITIZEN = 2

    private const val SALT_PREFIX = "sby_grp_pin_v1"
    private const val ENCODED_PREFIX = "pin4_"
    private const val MASKED_LAST_MESSAGE_PREFIX = "🔒 Pesan komunitas dikunci PIN"

    fun sanitizePinDigits(raw: String): String =
        raw.filter { it.isDigit() }.take(PIN_LENGTH)

    fun isValidFourDigitPin(pin: String): Boolean =
        pin.length == PIN_LENGTH && pin.all { it.isDigit() }

    /**
     * Deterministically hashes a 4-digit PIN bound to a specific [roomId] or [roomKey]
     * into a compact 16-hex-character SHA-256 digest so raw PINs are never exposed over the network.
     */
    fun computePinHash(roomSeed: String, rawPin: String): String {
        val cleanPin = rawPin.trim()
        if (!isValidFourDigitPin(cleanPin)) return ""
        val normalizedSeed = roomSeed.trim().lowercase().ifEmpty { "default_room" }
        val payload = "$SALT_PREFIX:$normalizedSeed:$cleanPin"
        val digest = MessageDigest.getInstance("SHA-256").digest(payload.toByteArray(Charsets.UTF_8))
        return digest.take(8).joinToString("") { "%02x".format(it) }
    }

    fun verifyPin(roomSeed: String, expectedPinHash: String, candidatePin: String): Boolean {
        val cleanExpected = expectedPinHash.trim().lowercase()
        if (cleanExpected.isEmpty()) return true
        val candidateClean = candidatePin.trim()
        if (!isValidFourDigitPin(candidateClean)) return false
        return computePinHash(roomSeed, candidateClean) == cleanExpected
    }

    /**
     * Encodes PIN hash + creator display name into `participant_names` field on `chat_rooms`
     * so PIN protection works out-of-the-box even before re-importing `pocketbase_collections.json`.
     * Format: `pin4_<16hex>|<creatorName>` or `<creatorName>` when open.
     */
    fun encodeParticipantNamesMetadata(pinHash: String, creatorDisplayName: String): String {
        val cleanHash = pinHash.trim().lowercase()
        val cleanCreator = creatorDisplayName.replace("|", " ").trim().take(72)
        return if (cleanHash.isNotEmpty()) {
            "$ENCODED_PREFIX$cleanHash|$cleanCreator"
        } else {
            cleanCreator
        }
    }

    fun extractPinHashFromMetadata(explicitPinHash: String, participantNamesField: String): String {
        val direct = explicitPinHash.trim().lowercase()
        if (direct.isNotEmpty()) return direct
        val raw = participantNamesField.trim()
        if (!raw.startsWith(ENCODED_PREFIX)) return ""
        return raw.removePrefix(ENCODED_PREFIX).substringBefore("|").trim().lowercase()
    }

    fun extractCreatorNameFromMetadata(explicitCreatorName: String, participantNamesField: String): String {
        val direct = explicitCreatorName.trim()
        if (direct.isNotEmpty()) return direct
        val raw = participantNamesField.trim()
        return if (raw.startsWith(ENCODED_PREFIX)) {
            raw.substringAfter("|", "").trim()
        } else {
            // Only treat non-pipe values as creator name in PUBLIC rooms
            if (!raw.contains("|")) raw else ""
        }
    }

    fun maskedLobbyPreview(): String = MASKED_LAST_MESSAGE_PREFIX

    /**
     * Parses locally stored unlocked room states (`roomId=pinHash;roomId2=pinHash2`).
     * Storing `roomId -> pinHash` ensures that if the group creator changes the PIN,
     * regular citizens must enter the new PIN while the creator & Admin still enter freely!
     */
    fun parseUnlockedPinMap(serialized: String?): Map<String, String> {
        if (serialized.isNullOrBlank()) return emptyMap()
        val result = LinkedHashMap<String, String>()
        serialized.split(";").forEach { entry ->
            val trimmed = entry.trim()
            if (trimmed.isNotEmpty() && trimmed.contains("=")) {
                val roomId = trimmed.substringBefore("=").trim()
                val hash = trimmed.substringAfter("=").trim().lowercase()
                if (roomId.isNotEmpty() && hash.isNotEmpty()) {
                    result[roomId] = hash
                }
            }
        }
        return result
    }

    fun serializeUnlockedPinMap(map: Map<String, String>): String =
        map.entries
            .filter { it.key.isNotBlank() && it.value.isNotBlank() }
            .joinToString(";") { "${it.key.trim()}=${it.value.trim().lowercase()}" }

    /**
     * Determines whether a user can immediately open a room without being prompted for a PIN.
     * - Official Admins ALWAYS bypass PIN (`isAdmin == true`) to monitor against illegal use.
     * - Room Creators ALWAYS bypass PIN (`roomCreatedBy == currentUserId`).
     * - Open rooms (`pinHash.isBlank()`) require no PIN.
     * - Regular citizens bypass PIN only if they previously unlocked the exact current `pinHash`.
     */
    fun hasRoomAccess(
        pinHash: String,
        roomId: String,
        roomCreatedBy: String,
        currentUserId: String,
        isAdmin: Boolean,
        unlockedPinHashesByRoomId: Map<String, String>,
    ): Boolean {
        val cleanHash = pinHash.trim().lowercase()
        if (cleanHash.isEmpty()) return true
        if (isAdmin) return true
        if (currentUserId.isNotBlank() && roomCreatedBy == currentUserId) return true
        return unlockedPinHashesByRoomId[roomId]?.lowercase() == cleanHash
    }
}
