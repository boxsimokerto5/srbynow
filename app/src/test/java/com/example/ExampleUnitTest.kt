package com.example

import com.example.data.model.NearbyProfile
import com.example.data.model.UserAccount
import com.example.data.model.effectiveCitizenAvatarUrl
import com.example.data.model.effectiveCitizenDisplayName
import com.example.data.model.effectiveCitizenGender
import com.example.data.model.isCitizenProfileComplete
import com.example.data.model.isValidCitizenDisplayName
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun citizenDisplayNameValidation_blocksEmptyAndPlaceholders() {
        assertFalse(isValidCitizenDisplayName(""))
        assertFalse(isValidCitizenDisplayName("   "))
        assertFalse(isValidCitizenDisplayName("A"))
        assertFalse(isValidCitizenDisplayName("Warga Surabaya"))
        assertFalse(isValidCitizenDisplayName("warga surabaya"))
        assertFalse(isValidCitizenDisplayName("Warga"))
        assertFalse(isValidCitizenDisplayName("Pengguna"))
        assertFalse(isValidCitizenDisplayName("Anonymous"))

        assertTrue(isValidCitizenDisplayName("Cak Budi"))
        assertTrue(isValidCitizenDisplayName("Ning Sari"))
        assertTrue(isValidCitizenDisplayName("Ai"))
    }

    @Test
    fun citizenProfileCompleteness_requiresPhotoNameAndGender() {
        val emptyAccount = UserAccount(
            id = "u1",
            userId = "u1",
            displayName = "budi123",
            rawName = "",
            gender = "",
            avatarUrl = "",
        )
        assertFalse(isCitizenProfileComplete(emptyAccount, null))

        // Photo + name present, but gender missing -> incomplete
        val missingGender = emptyAccount.copy(
            displayName = "Budi Santoso",
            rawName = "Budi Santoso",
            avatarUrl = "https://pb.example/api/files/users/u1/avatar.jpg",
            gender = "",
        )
        assertFalse(isCitizenProfileComplete(missingGender, null))

        // Photo + gender present, but server rawName blank (only email fallback in displayName) -> incomplete
        val missingRealName = emptyAccount.copy(
            displayName = "budi123",
            rawName = "",
            avatarUrl = "https://pb.example/api/files/users/u1/avatar.jpg",
            gender = "LAKI_LAKI",
        )
        assertFalse(isCitizenProfileComplete(missingRealName, null))

        // Name + gender present, but photo missing -> incomplete
        val missingPhoto = emptyAccount.copy(
            displayName = "Budi Santoso",
            rawName = "Budi Santoso",
            avatarUrl = "",
            gender = "LAKI_LAKI",
        )
        assertFalse(isCitizenProfileComplete(missingPhoto, null))

        // All 3 present directly on UserAccount -> complete
        val completeMale = missingPhoto.copy(
            avatarUrl = "https://pb.example/api/files/users/u1/avatar.jpg",
        )
        assertTrue(isCitizenProfileComplete(completeMale, null))

        // Photo & gender stored on NearbyProfile fallback -> complete
        val nearbyFallback = NearbyProfile(
            id = "u1",
            userId = "u1",
            displayName = "Ning Ayu",
            gender = "Perempuan",
            avatarUrl = "https://pb.example/api/files/nearby_profiles/u1/ning.jpg",
        )
        assertEquals("https://pb.example/api/files/nearby_profiles/u1/ning.jpg", effectiveCitizenAvatarUrl(emptyAccount, nearbyFallback))
        assertEquals("Ning Ayu", effectiveCitizenDisplayName(emptyAccount, nearbyFallback))
        assertEquals("PEREMPUAN", effectiveCitizenGender(emptyAccount, nearbyFallback))
        assertTrue(isCitizenProfileComplete(emptyAccount, nearbyFallback))
    }
}
