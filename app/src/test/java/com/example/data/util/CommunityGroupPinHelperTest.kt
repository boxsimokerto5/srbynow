package com.example.data.util

import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CommunityGroupPinHelperTest {

    @Test
    fun `four digit PIN validation and sanitization enforce exact 4 numeric digits`() {
        assertEquals("1234", CommunityGroupPinHelper.sanitizePinDigits("12a3b456"))
        assertTrue(CommunityGroupPinHelper.isValidFourDigitPin("0482"))
        assertFalse(CommunityGroupPinHelper.isValidFourDigitPin("123"))
        assertFalse(CommunityGroupPinHelper.isValidFourDigitPin("12345"))
        assertFalse(CommunityGroupPinHelper.isValidFourDigitPin("12ab"))
        assertEquals(2, CommunityGroupPinHelper.MAX_GROUPS_PER_CITIZEN)
    }

    @Test
    fun `PIN hash verification binds to roomId and rejects wrong PINs`() {
        val roomA = "room_rt05_gubeng"
        val roomB = "room_rt09_wonokromo"
        val hashA = CommunityGroupPinHelper.computePinHash(roomA, "2580")

        assertEquals(16, hashA.length)
        assertTrue(CommunityGroupPinHelper.verifyPin(roomA, hashA, "2580"))
        assertFalse(CommunityGroupPinHelper.verifyPin(roomA, hashA, "2581"))
        // Same PIN on a different room produces a distinct hash
        assertFalse(CommunityGroupPinHelper.verifyPin(roomB, hashA, "2580"))
    }

    @Test
    fun `metadata encoding preserves PIN hash and creator display name backward compatibly`() {
        val hash = CommunityGroupPinHelper.computePinHash("room_1", "7788")
        val encoded = CommunityGroupPinHelper.encodeParticipantNamesMetadata(hash, "Cak Eko")

        assertEquals(hash, CommunityGroupPinHelper.extractPinHashFromMetadata("", encoded))
        assertEquals("Cak Eko", CommunityGroupPinHelper.extractCreatorNameFromMetadata("", encoded))

        val openEncoded = CommunityGroupPinHelper.encodeParticipantNamesMetadata("", "Ning Sari")
        assertEquals("", CommunityGroupPinHelper.extractPinHashFromMetadata("", openEncoded))
        assertEquals("Ning Sari", CommunityGroupPinHelper.extractCreatorNameFromMetadata("", openEncoded))
    }

    @Test
    fun `Admin and room creator always bypass PIN while regular citizens require matching PIN hash`() {
        val roomId = "room_komunitas_1"
        val oldHash = CommunityGroupPinHelper.computePinHash(roomId, "1111")
        val newHash = CommunityGroupPinHelper.computePinHash(roomId, "9999")

        // 1. Admin always enters freely without unlocking
        assertTrue(
            CommunityGroupPinHelper.hasRoomAccess(
                pinHash = oldHash,
                roomId = roomId,
                roomCreatedBy = "creator_uid",
                currentUserId = "admin_uid",
                isAdmin = true,
                unlockedPinHashesByRoomId = emptyMap(),
            ),
        )

        // 2. Room creator always enters freely without unlocking
        assertTrue(
            CommunityGroupPinHelper.hasRoomAccess(
                pinHash = oldHash,
                roomId = roomId,
                roomCreatedBy = "creator_uid",
                currentUserId = "creator_uid",
                isAdmin = false,
                unlockedPinHashesByRoomId = emptyMap(),
            ),
        )

        // 3. Regular citizen blocked before entering PIN
        assertFalse(
            CommunityGroupPinHelper.hasRoomAccess(
                pinHash = oldHash,
                roomId = roomId,
                roomCreatedBy = "creator_uid",
                currentUserId = "citizen_uid",
                isAdmin = false,
                unlockedPinHashesByRoomId = emptyMap(),
            ),
        )

        // 4. Regular citizen unlocked oldHash -> granted; when creator changes PIN to newHash -> re-locked!
        val serialized = CommunityGroupPinHelper.serializeUnlockedPinMap(mapOf(roomId to oldHash))
        val parsed = CommunityGroupPinHelper.parseUnlockedPinMap(serialized)

        assertTrue(
            CommunityGroupPinHelper.hasRoomAccess(
                pinHash = oldHash,
                roomId = roomId,
                roomCreatedBy = "creator_uid",
                currentUserId = "citizen_uid",
                isAdmin = false,
                unlockedPinHashesByRoomId = parsed,
            ),
        )
        assertFalse(
            "Changing group PIN must require regular citizens to enter the new 4-digit PIN",
            CommunityGroupPinHelper.hasRoomAccess(
                pinHash = newHash,
                roomId = roomId,
                roomCreatedBy = "creator_uid",
                currentUserId = "citizen_uid",
                isAdmin = false,
                unlockedPinHashesByRoomId = parsed,
            ),
        )
    }

    @Test
    fun `root and assets pocketbase_collections json stay strictly in sync`() {
        val rootFile = File("pocketbase_collections.json").let {
            if (it.exists()) it else File("../pocketbase_collections.json")
        }
        val assetFile = File("app/src/main/assets/pocketbase_collections.json").let {
            if (it.exists()) it else File("src/main/assets/pocketbase_collections.json")
        }
        assertTrue("Root pocketbase_collections.json must exist", rootFile.exists())
        assertTrue("Assets pocketbase_collections.json must exist", assetFile.exists())
        val rootText = rootFile.readText().trim()
        val assetText = assetFile.readText().trim()
        assertEquals(rootText, assetText)
        assertTrue(rootText.contains("\"pin_hash\""))
        assertTrue(rootText.contains("created_by = @request.auth.id"))
    }
}
