package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextReplacement
import com.example.data.model.KosComment
import com.example.data.model.KosKontrakItem
import com.example.data.model.UiState
import com.example.data.model.UserAccount
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KosKontrakScreen
import com.example.ui.screens.collectAdminFlaggedItems
import com.example.ui.sheets.CreateKosSheetContent
import com.example.ui.sheets.KosDetailSheetContent
import com.example.ui.theme.SurabayaHariIniTheme
import com.example.ui.viewmodel.MainTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h892dp-xxhdpi")
class KosKontrakRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `HomeScreen displays Info Kos & Kontrak shortcut and navigates to MainTab KOS_KONTRAK`() {
        var navigatedTab: MainTab? = null

        composeTestRule.setContent {
            SurabayaHariIniTheme {
                HomeScreen(
                    userAccount = UserAccount(displayName = "Cak Eko", district = "Genteng"),
                    isAdmin = false,
                    currentDistrict = "Genteng",
                    incidentsState = UiState.Success(emptyList()),
                    destinationsState = UiState.Success(emptyList()),
                    momentsState = UiState.Success(emptyList()),
                    onNavigateTab = { navigatedTab = it },
                    onOpenIncidentDetail = {},
                    onOpenDestinationDetail = {},
                    onOpenMomentDetail = {},
                    onOpenBookmarks = {},
                    onOpenProfileAndAdmin = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_shortcut_kos").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(MainTab.KOS_KONTRAK, navigatedTab)
    }

    @Test
    fun `CreateKosSheetContent blocks phishing map links and submits monthly or yearly listing`() {
        var submittedTitle = ""
        var submittedType = ""
        var submittedBilling = ""
        var submittedPrice = -1L
        var submittedMapsUrl = ""
        var submittedWa = ""

        composeTestRule.setContent {
            SurabayaHariIniTheme {
                CreateKosSheetContent(
                    defaultDistrict = "Rungkut",
                    onSubmit = { title, propType, billing, price, _, _, _, _, mapsUrl, wa, _, _ ->
                        submittedTitle = title
                        submittedType = propType
                        submittedBilling = billing
                        submittedPrice = price
                        submittedMapsUrl = mapsUrl
                        submittedWa = wa
                    },
                    onClose = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("kos_input_title")
            .performScrollTo()
            .performTextInput("Kontrakan Rumah 2KT Rungkut Asri")
        composeTestRule.onNodeWithTag("kos_type_KONTRAKAN")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("kos_billing_TAHUNAN")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("kos_input_price")
            .performScrollTo()
            .performTextInput("21000000")
        composeTestRule.onNodeWithTag("kos_input_address")
            .performScrollTo()
            .performTextInput("Perumahan Rungkut Asri Timur Blok D")

        // Enter suspicious shortlink -> must show error and disable submit
        composeTestRule.onNodeWithTag("kos_input_maps_url")
            .performScrollTo()
            .performTextInput("https://bit.ly/jebakan-kos")
        composeTestRule.onNodeWithTag("kos_input_whatsapp")
            .performScrollTo()
            .performTextInput("081330776622")
        composeTestRule.onNodeWithTag("kos_input_description")
            .performScrollTo()
            .performTextInput("2 kamar tidur, carport mobil berkanopi, bebas banjir dekat UPN.")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("kos_submit_btn")
            .performScrollTo()
            .assertIsNotEnabled()

        // Replace with official Google Maps link -> enables submit
        composeTestRule.onNodeWithTag("kos_input_maps_url")
            .performScrollTo()
            .performTextReplacement("https://maps.app.goo.gl/surabayaRungkut01")
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("kos_submit_btn")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        assertEquals("Kontrakan Rumah 2KT Rungkut Asri", submittedTitle)
        assertEquals("KONTRAKAN", submittedType)
        assertEquals("TAHUNAN", submittedBilling)
        assertEquals(21000000L, submittedPrice)
        assertEquals("https://maps.app.goo.gl/surabayaRungkut01", submittedMapsUrl)
        assertEquals("081330776622", submittedWa)
    }

    @Test
    fun `KosKontrakScreen filters by billing period and lets owner toggle PENUH TERSEWA`() {
        var items by mutableStateOf(
            listOf(
                KosKontrakItem(
                    id = "kos_mine_001",
                    ownerId = "uid_owner_me",
                    ownerName = "Cak Hendra",
                    title = "Kos Putra Gebang Dekat ITS",
                    propertyType = "KOS_PUTRA",
                    billingPeriod = "BULANAN",
                    price = 850000L,
                    availableRooms = 2,
                    facilities = listOf("AC", "WiFi"),
                    district = "Sukolilo",
                    address = "Jl. Gebang Lor",
                    isFull = false,
                ),
                KosKontrakItem(
                    id = "ktr_other_002",
                    ownerId = "uid_other",
                    ownerName = "Bu Rini",
                    title = "Kontrakan Rumah Keluarga Ketintang",
                    propertyType = "KONTRAKAN",
                    billingPeriod = "TAHUNAN",
                    price = 19500000L,
                    availableRooms = 1,
                    facilities = listOf("Parkir Mobil"),
                    district = "Gayungan",
                    address = "Jl. Ketintang Baru",
                    isFull = false,
                ),
            ),
        )

        composeTestRule.setContent {
            SurabayaHariIniTheme {
                KosKontrakScreen(
                    kosState = UiState.Success(items),
                    bookmarks = emptyList(),
                    currentUserId = "uid_owner_me",
                    isAdmin = false,
                    onOpenKosDetail = {},
                    onOpenCreateKos = {},
                    onToggleFull = { clicked ->
                        items = items.map {
                            if (it.id == clicked.id) it.copy(isFull = !it.isFull, availableRooms = if (!it.isFull) 0 else 1) else it
                        }
                    },
                    onToggleBookmark = {},
                    onBackToHome = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // Filter Sewa Tahunan -> only yearly kontrakan shows
        composeTestRule.onNodeWithTag("kos_period_TAHUNAN").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onAllNodesWithTag("kos_card_kos_mine_001").assertCountEquals(0)
        composeTestRule.onNodeWithTag("kos_card_ktr_other_002").assertIsDisplayed()

        // Switch back to Semua Durasi & mark own kos as PENUH / TERSEWA
        composeTestRule.onNodeWithTag("kos_period_ALL").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("kos_card_toggle_full_kos_mine_001")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        assertTrue(items.first { it.id == "kos_mine_001" }.isFull)

        // Switch to Semua Info filter to inspect PENUH / TERSEWA badge on card
        composeTestRule.onNodeWithTag("kos_filter_all").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("kos_card_kos_mine_001").performScrollTo()
        composeTestRule.onNodeWithTag("kos_full_badge_kos_mine_001", useUnmergedTree = true).assertIsDisplayed()
    }

    @Test
    fun `KosDetailSheetContent supports direct chat, WA button, Maps button, and QnA comments with PEMILIK badge`() {
        val kosItem = KosKontrakItem(
            id = "kos_detail_01",
            ownerId = "uid_bu_sri",
            ownerName = "Bu Sri Wahyuni",
            title = "Kos Putri Dharmawangsa Dekat Unair B",
            propertyType = "KOS_PUTRI",
            billingPeriod = "BULANAN",
            price = 1150000L,
            availableRooms = 2,
            facilities = listOf("KM Dalam", "AC", "WiFi"),
            district = "Gubeng",
            address = "Jl. Dharmawangsa Dalam",
            mapsUrl = "https://maps.google.com/?q=-7.2738,112.7582",
            whatsappNumber = "6281231004455",
            description = "Bersih, aman, bebas banjir.",
        )

        var comments by mutableStateOf(
            listOf(
                KosComment(
                    id = "kcm_owner_1",
                    kosId = "kos_detail_01",
                    authorId = "uid_bu_sri",
                    authorName = "Bu Sri Wahyuni",
                    authorDistrict = "Gubeng",
                    text = "Silakan survei sore hari jam 16.00 ya nak.",
                ),
            ),
        )
        var chatClicked = false

        composeTestRule.setContent {
            SurabayaHariIniTheme {
                KosDetailSheetContent(
                    item = kosItem,
                    comments = comments,
                    isSaved = false,
                    currentUserId = "uid_seeker_ning",
                    isAdmin = false,
                    onToggleFull = {},
                    onChatOwner = { chatClicked = true },
                    onToggleBookmark = {},
                    onSendComment = { txt ->
                        comments = comments + KosComment(
                            id = "kcm_seeker_2",
                            kosId = "kos_detail_01",
                            authorId = "uid_seeker_ning",
                            authorName = "Ning Raras",
                            text = txt,
                        )
                    },
                    onReportContent = {},
                    onReportLink = {},
                    onDelete = null,
                    onClose = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("kos_open_maps_btn").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("kos_wa_owner_btn").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("kos_chat_owner_btn").performScrollTo().performClick()
        assertTrue(chatClicked)

        // Owner reply badge visible
        composeTestRule.onNodeWithTag("kos_owner_reply_badge_kcm_owner_1", useUnmergedTree = true).assertIsDisplayed()

        // Citizen posts a question
        composeTestRule.onNodeWithTag("kos_comment_input")
            .performScrollTo()
            .performTextInput("Listrik token per kamar berapa VA bu?")
        composeTestRule.onNodeWithTag("kos_send_comment_btn")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Listrik token per kamar berapa VA bu?").assertIsDisplayed()
    }

    @Test
    fun `collectAdminFlaggedItems includes flagged kos_listings for Admin moderation`() {
        val flaggedKos = KosKontrakItem(
            id = "kos_flag_99",
            ownerId = "uid_scammer",
            ownerName = "Oknum DP Kos",
            title = "Kos Murah Minta Transfer DP Duluan",
            district = "Wonokromo",
            address = "Jl. Wonokromo",
            mapsUrl = "https://maps.google.com/?q=-7.30,112.73",
            contentReportCount = 2,
            linkReportCount = 1,
        )

        val flagged = collectAdminFlaggedItems(
            incidents = emptyList(),
            approvedEvents = emptyList(),
            pendingEvents = emptyList(),
            culinarySpots = emptyList(),
            moments = emptyList(),
            destinations = emptyList(),
            services = emptyList(),
            barkasItems = emptyList(),
            kosListings = listOf(flaggedKos),
        )

        assertEquals(1, flagged.size)
        assertEquals("kos_listings", flagged.first().collection)
        assertEquals("Info Kos & Kontrak", flagged.first().sourceLabel)
        assertEquals(3, flagged.first().totalSignals)
        assertTrue(flagged.first().hasExternalLink)
    }
}
