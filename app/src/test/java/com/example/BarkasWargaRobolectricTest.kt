package com.example

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.data.model.BarkasComment
import com.example.data.model.BarkasItem
import com.example.data.model.UiState
import com.example.data.model.UserAccount
import com.example.ui.screens.BarkasScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.collectAdminFlaggedItems
import com.example.ui.sheets.BarkasDetailSheetContent
import com.example.ui.sheets.CreateBarkasSheetContent
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
class BarkasWargaRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `HomeScreen displays Barkas Warga shortcut and navigates to MainTab BARKAS`() {
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

        composeTestRule.onNodeWithTag("home_shortcut_barkas").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(MainTab.BARKAS, navigatedTab)
    }

    @Test
    fun `CreateBarkasSheetContent submits complete secondhand listing`() {
        var submittedTitle = ""
        var submittedCategory = ""
        var submittedPrice = -1L
        var submittedCod = ""

        composeTestRule.setContent {
            SurabayaHariIniTheme {
                CreateBarkasSheetContent(
                    defaultDistrict = "Wonokromo",
                    onSubmit = { title, cat, _, price, _, _, codLoc, _, _, _ ->
                        submittedTitle = title
                        submittedCategory = cat
                        submittedPrice = price
                        submittedCod = codLoc
                    },
                    onClose = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("barkas_input_title")
            .performScrollTo()
            .performTextInput("Kipas Angin Miyako Berdiri Normal")
        composeTestRule.onNodeWithTag("barkas_cat_PERABOT")
            .performScrollTo()
            .performClick()
        composeTestRule.onNodeWithTag("barkas_input_price")
            .performScrollTo()
            .performTextInput("175000")
        composeTestRule.onNodeWithTag("barkas_input_cod_location")
            .performScrollTo()
            .performTextInput("Taman Bungkul Raya Darmo")
        composeTestRule.onNodeWithTag("barkas_input_description")
            .performScrollTo()
            .performTextInput("Angin masih kencang 3 speed normal semua, siap tes di tempat.")
        composeTestRule.onNodeWithTag("barkas_submit_btn")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        assertEquals("Kipas Angin Miyako Berdiri Normal", submittedTitle)
        assertEquals("PERABOT", submittedCategory)
        assertEquals(175000L, submittedPrice)
        assertEquals("Taman Bungkul Raya Darmo", submittedCod)
    }

    @Test
    fun `BarkasScreen shows owner quick sold button only for seller and toggles sold badge`() {
        var items by mutableStateOf(
            listOf(
                BarkasItem(
                    id = "item_mine_001",
                    sellerId = "uid_owner",
                    sellerName = "Cak Budi",
                    title = "Sepeda Lipat Polygon Urbano",
                    category = "KENDARAAN",
                    price = 2000000L,
                    district = "Wonokromo",
                    codLocation = "Taman Bungkul",
                    isSold = false,
                ),
                BarkasItem(
                    id = "item_other_002",
                    sellerId = "uid_other",
                    sellerName = "Ning Rina",
                    title = "Kulkas Kos 1 Pintu",
                    category = "PERABOT",
                    price = 900000L,
                    district = "Gubeng",
                    codLocation = "Unair B",
                    isSold = false,
                ),
            )
        )

        composeTestRule.setContent {
            SurabayaHariIniTheme {
                BarkasScreen(
                    barkasState = UiState.Success(items),
                    bookmarks = emptyList(),
                    currentUserId = "uid_owner",
                    isAdmin = false,
                    onOpenBarkasDetail = {},
                    onOpenCreateBarkas = {},
                    onToggleSold = { target ->
                        items = items.map {
                            if (it.id == target.id) it.copy(isSold = !it.isSold) else it
                        }
                    },
                    onToggleBookmark = {},
                    onBackToHome = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // Owner sees toggle button on own card, but NOT on someone else's card
        composeTestRule.onNodeWithTag("barkas_card_toggle_sold_item_mine_001")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("barkas_card_toggle_sold_item_other_002")
            .assertCountEquals(0)

        // Owner clicks Tandai Terjual
        composeTestRule.onNodeWithTag("barkas_card_toggle_sold_item_mine_001")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(items.first { it.id == "item_mine_001" }.isSold)

        // Switch filter to Semua Barang to inspect the sold badge inside the clickable card
        composeTestRule.onNodeWithTag("barkas_filter_all")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("barkas_card_item_mine_001")
            .performScrollTo()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("barkas_sold_badge_item_mine_001", useUnmergedTree = true)
            .assertIsDisplayed()
    }

    @Test
    fun `BarkasDetailSheetContent supports citizen comments, PENJUAL badge, and sold state lock`() {
        var currentItem by mutableStateOf(
            BarkasItem(
                id = "brk_001",
                sellerId = "seller_hendra",
                sellerName = "Cak Hendra",
                title = "Monitor LG 24 Inch IPS",
                category = "ELEKTRONIK",
                price = 1100000L,
                isNegotiable = true,
                district = "Tegalsari",
                codLocation = "Tunjungan Plaza",
                whatsappNumber = "081234567890",
                description = "Bebas dead pixel lengkap kabel HDMI.",
                isSold = false,
            )
        )
        var comments by mutableStateOf(
            listOf(
                BarkasComment(
                    id = "c_seller_1",
                    barkasId = "brk_001",
                    authorId = "seller_hendra",
                    authorName = "Cak Hendra",
                    authorDistrict = "Tegalsari",
                    text = "Silakan tanya-tanya rek, barang masih mulus!",
                )
            )
        )
        var viewerUid by mutableStateOf("buyer_reza")

        composeTestRule.setContent {
            SurabayaHariIniTheme {
                BarkasDetailSheetContent(
                    item = currentItem,
                    comments = comments,
                    isSaved = false,
                    currentUserId = viewerUid,
                    isAdmin = false,
                    onToggleSold = { currentItem = currentItem.copy(isSold = !currentItem.isSold) },
                    onChatSeller = {},
                    onToggleBookmark = {},
                    onSendComment = { txt ->
                        comments = comments + BarkasComment(
                            id = "c_buyer_2",
                            barkasId = "brk_001",
                            authorId = viewerUid,
                            authorName = "Cak Reza",
                            authorDistrict = "Sawahan",
                            text = txt,
                        )
                    },
                    onReport = {},
                    onDelete = null,
                    onClose = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // Non-owner buyer sees Chat & WA buttons, does NOT see owner sold toggle button
        composeTestRule.onNodeWithTag("barkas_chat_seller_btn").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("barkas_wa_seller_btn").performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("barkas_detail_toggle_sold_btn").assertCountEquals(0)

        // Seller's comment displays PENJUAL badge
        composeTestRule.onNodeWithTag("barkas_seller_reply_badge_c_seller_1").performScrollTo().assertIsDisplayed()

        // Buyer posts a comment
        composeTestRule.onNodeWithTag("barkas_comment_input")
            .performScrollTo()
            .performTextInput("Nego 950 ribu COD sore ini di TP boleh cak?")
        composeTestRule.onNodeWithTag("barkas_send_comment_btn")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Nego 950 ribu COD sore ini di TP boleh cak?")
            .performScrollTo()
            .assertIsDisplayed()

        // Switch viewer to owner and mark item as TERJUAL
        viewerUid = "seller_hendra"
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("barkas_detail_toggle_sold_btn")
            .performScrollTo()
            .performClick()
        composeTestRule.waitForIdle()
        assertTrue(currentItem.isSold)

        // Switch back to buyer: sold banner appears and contact buttons are hidden
        viewerUid = "buyer_reza"
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("barkas_detail_sold_banner").performScrollTo().assertIsDisplayed()
        composeTestRule.onAllNodesWithTag("barkas_chat_seller_btn").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("barkas_wa_seller_btn").assertCountEquals(0)
    }

    @Test
    fun `collectAdminFlaggedItems includes reported Barkas Warga listings for Pusat Kendali Admin`() {
        val reportedBarkas = BarkasItem(
            id = "brk_flagged_99",
            sellerId = "spammer_uid",
            sellerName = "Oknum Penjual",
            title = "Barang Mencurigakan Minta Transfer DP",
            price = 500000L,
            district = "Genteng",
            description = "Wajib transfer DP dulu",
            contentReportCount = 2,
        )

        val flagged = collectAdminFlaggedItems(
            incidents = emptyList(),
            approvedEvents = emptyList(),
            pendingEvents = emptyList(),
            culinarySpots = emptyList(),
            moments = emptyList(),
            destinations = emptyList(),
            services = emptyList(),
            barkasItems = listOf(reportedBarkas),
        )

        assertEquals(1, flagged.size)
        assertEquals("barkas_items", flagged.first().collection)
        assertEquals("Barkas Warga", flagged.first().sourceLabel)
        assertEquals("brk_flagged_99", flagged.first().id)
        assertEquals(2, flagged.first().contentReportCount)
    }
}
