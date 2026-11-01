package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.model.TambalBanSpot
import com.example.data.model.UiState
import com.example.ui.screens.TambalBanScreen
import com.example.ui.sheets.CreateIncidentSheetContent
import com.example.ui.sheets.PermissionDisclosureBottomSheet
import com.example.ui.sheets.PermissionSheetMode
import com.example.ui.theme.SurabayaHariIniTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h892dp-xxhdpi")
class PermissionDisclosureRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `tambal ban gps button opens slide-up location permission disclosure sheet before requesting permission`() {
        val sampleSpots = listOf(
            TambalBanSpot(
                id = "tb_1",
                name = "Tambal Ban Bungkul 24 Jam",
                description = "Sedia tubeless motor & mobil",
                district = "Wonokromo",
                address = "Jl. Raya Darmo No. 12",
                is24Hours = true,
                isTubeless = true,
                forMotor = true,
                forMobil = true,
                canCallOut = false,
                authorId = "u1",
                authorName = "Cak Budi",
            ),
        )

        composeTestRule.setContent {
            SurabayaHariIniTheme {
                TambalBanScreen(
                    spotsState = UiState.Success(sampleSpots),
                    currentUserId = "u1",
                    isLiveGpsActive = false,
                    onBackToHome = {},
                    onOpenDetail = {},
                    onCreateSpot = {},
                    onRefreshLocation = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // Tap the contextual GPS banner on TambalBanScreen
        composeTestRule.onNodeWithTag("tambal_ban_gps_status_banner").performClick()
        composeTestRule.waitForIdle()

        // Verify the slide-up bottom sheet appears with clear Play-Console-compliant explanations
        composeTestRule.onNodeWithTag("permission_disclosure_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Izin Akses Lokasi (GPS)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tambal Ban, Kuliner & Destinasi Terdekat").assertIsDisplayed()
        composeTestRule.onNodeWithText("Titik Akurat Laporan Kejadian Warga").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_permission_sheet_continue").assertIsDisplayed()

        // Tap "Nanti Saja" and verify sheet closes gracefully without crashing
        composeTestRule.onNodeWithTag("btn_permission_sheet_later").performClick()
        composeTestRule.waitForIdle()
    }

    @Test
    fun `incident report form gps card opens slide-up location permission disclosure sheet`() {
        composeTestRule.setContent {
            SurabayaHariIniTheme {
                CreateIncidentSheetContent(
                    defaultDistrict = "Genteng",
                    isLiveGpsActive = false,
                    onSubmit = { _, _, _, _, _, _, _, _ -> },
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("form_incident_use_gps_card").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("permission_disclosure_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithText("Titik Koordinat Akurat Laporan Kejadian Warga").assertIsDisplayed()
    }

    @Test
    fun `overview and notification permission disclosure modes display clear usage explanations`() {
        var dismissed = false
        composeTestRule.setContent {
            SurabayaHariIniTheme {
                PermissionDisclosureBottomSheet(
                    mode = PermissionSheetMode.Overview,
                    onDismiss = { dismissed = true },
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Transparansi Izin Akses Aplikasi").assertIsDisplayed()
        composeTestRule.onNodeWithText("Izin Akses Lokasi (GPS)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Izin Pemberitahuan Warga").assertIsDisplayed()
        composeTestRule.onNodeWithTag("btn_permission_sheet_close").performClick()
        composeTestRule.waitForIdle()

        assertTrue(dismissed)
    }
}
