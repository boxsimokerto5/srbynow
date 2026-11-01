package com.example

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.data.audio.RadioPlaybackState
import com.example.data.audio.RadioStationInfo
import com.example.ui.sheets.RadioPlayerSheetContent
import com.example.ui.sheets.appendTranscribedVoiceText
import com.example.ui.sheets.buildSuaraSurabayaWhatsAppMessage
import com.example.ui.theme.SurabayaHariIniTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h892dp-xxhdpi")
class RadioGatekeeperReportRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `station info uses official Suara Surabaya WhatsApp number 628553010055`() {
        val station = RadioStationInfo()
        assertEquals("628553010055", station.hotlineWhatsApp)
        assertEquals("03199000000", station.hotlinePhone)
        assertEquals("03199534567", station.emergencyPhone)
    }

    @Test
    fun `appendTranscribedVoiceText capitalizes and joins spoken sentences cleanly`() {
        val first = appendTranscribedVoiceText("", "arus lalu lintas ahmad yani arah waru padat merayap")
        assertEquals("Arus lalu lintas ahmad yani arah waru padat merayap", first)

        val second = appendTranscribedVoiceText(first, "ada truk mogok di lajur kiri")
        assertEquals(
            "Arus lalu lintas ahmad yani arah waru padat merayap. Ada truk mogok di lajur kiri",
            second,
        )
    }

    @Test
    fun `buildSuaraSurabayaWhatsAppMessage formats first-time and returning reporter rules accurately`() {
        val firstTimeMsg = buildSuaraSurabayaWhatsAppMessage(
            isFirstTimeReporter = true,
            reporterName = "Cak Budi",
            reporterAddress = "Jl. Ketabang Kali No. 10, Genteng, Surabaya",
            birthYear = "1991",
            category = "Lalu Lintas / Macet",
            incidentLocation = "Jl. A. Yani depan Royal Plaza arah Waru",
            reportBody = "Lalin padat merayap sekitar 500 meter karena antrean putar balik.",
            gpsMapsUrl = "https://www.google.com/maps/search/?api=1&query=-7.309100,112.734500",
            timestampWib = "18:45 WIB",
        )

        assertTrue(firstTimeMsg.contains("Halo Gatekeeper Radio Suara Surabaya 100 FM"))
        assertTrue(firstTimeMsg.contains("*DATA PELAPOR (Pertama Kali Menghubungi)*"))
        assertTrue(firstTimeMsg.contains("• Nama: Cak Budi"))
        assertTrue(firstTimeMsg.contains("• Alamat: Jl. Ketabang Kali No. 10, Genteng, Surabaya"))
        assertTrue(firstTimeMsg.contains("• Tahun Kelahiran: 1991"))
        assertTrue(firstTimeMsg.contains("• Kategori: Lalu Lintas / Macet"))
        assertTrue(firstTimeMsg.contains("• Lokasi & Arah: Jl. A. Yani depan Royal Plaza arah Waru"))
        assertTrue(firstTimeMsg.contains("• Titik GPS Lokasi: https://www.google.com/maps/search/?api=1&query=-7.309100,112.734500"))

        val returningMsg = buildSuaraSurabayaWhatsAppMessage(
            isFirstTimeReporter = false,
            reporterName = "Ning Sari",
            reporterAddress = "Kec. Gubeng, Surabaya",
            birthYear = "",
            category = "Cuaca / Banjir",
            incidentLocation = "Jl. Dharmawangsa",
            reportBody = "Hujan deras disertai angin, jarak pandang terbatas.",
            timestampWib = "19:00 WIB",
        )

        assertTrue(returningMsg.contains("*DATA PELAPOR*"))
        assertFalse(returningMsg.contains("Pertama Kali Menghubungi"))
        assertTrue(returningMsg.contains("• Nama: Ning Sari"))
        assertTrue(returningMsg.contains("• Domisili: Kec. Gubeng, Surabaya"))
    }

    @Test
    fun `gatekeeper form validates first-time birth year, supports voice button, and launches official wa_me 628553010055`() {
        val app = ApplicationProvider.getApplicationContext<android.app.Application>()

        composeTestRule.setContent {
            SurabayaHariIniTheme {
                Box(modifier = Modifier.width(412.dp).height(920.dp)) {
                    RadioPlayerSheetContent(
                        playbackState = RadioPlaybackState.PLAYING,
                        volume = 0.85f,
                        errorMessage = null,
                        stationInfo = RadioStationInfo(),
                        onTogglePlay = {},
                        onSetVolume = {},
                        onClose = {},
                        defaultReporterName = "Budi Santoso",
                        defaultDistrict = "Wonokromo",
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        // Official badge & voice-to-text button are displayed
        composeTestRule.onNodeWithText("0855-3010-055").assertIsDisplayed()
        composeTestRule.onNodeWithTag("radio_voice_to_text_btn").assertIsDisplayed()

        // Tapping voice-to-text in Robolectric (no recognizer installed or launches intent) should never crash
        composeTestRule.onNodeWithTag("radio_voice_to_text_btn").performClick()
        composeTestRule.waitForIdle()

        // Attempting to submit as first-time reporter without birth year shows validation & expands identity editor
        composeTestRule.onNodeWithTag("radio_whatsapp_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("radio_form_validation_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("radio_input_birth_year").assertIsDisplayed()

        // Complete birth year, incident location, and report body
        composeTestRule.onNodeWithTag("radio_input_birth_year").performTextReplacement("1992")
        composeTestRule.onNodeWithTag("radio_input_location").performScrollTo()
            .performTextReplacement("Jl. Raya Darmo arah Taman Bungkul")
        composeTestRule.onNodeWithTag("radio_input_report_body").performScrollTo()
            .performTextReplacement("Arus lalu lintas ramai lancar pasca hujan reda.")
        composeTestRule.waitForIdle()

        // Toggle WhatsApp message preview and verify content
        composeTestRule.onNodeWithTag("radio_toggle_preview_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("radio_whatsapp_preview_box").performScrollTo().assertIsDisplayed()

        // Clear any prior started activities from speech click, then submit to WhatsApp
        val shadowApp = shadowOf(app)
        while (shadowApp.nextStartedActivity != null) {
            // drain queue
        }

        composeTestRule.onNodeWithTag("radio_whatsapp_btn").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        val startedIntent = shadowApp.nextStartedActivity
        assertNotNull("Expected ACTION_VIEW WhatsApp intent to be launched", startedIntent)
        assertEquals(Intent.ACTION_VIEW, startedIntent.action)
        val uriString = startedIntent.dataString.orEmpty()
        assertTrue(
            "Expected official WA SS 628553010055 URL, got: $uriString",
            uriString.startsWith("https://wa.me/628553010055?text="),
        )
        assertTrue("Expected encoded birth year 1992 in WA URL", uriString.contains("1992"))
    }
}
