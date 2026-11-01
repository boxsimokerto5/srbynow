package com.example

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.model.ChatRoom
import com.example.data.model.CitizenPoll
import com.example.data.model.IncidentReport
import com.example.data.model.NearbyProfile
import com.example.data.model.PollOption
import com.example.data.model.SurabayaEvent
import com.example.data.model.UiState
import com.example.data.model.UserAccount
import com.example.data.model.UserMoment
import com.example.data.pocketbase.PocketBaseClient
import com.example.data.repository.SurabayaRepository
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.collectAdminFlaggedItems
import com.example.ui.sheets.ProfileAndPrivacySheetContent
import com.example.ui.theme.SurabayaHariIniTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h892dp-xxhdpi")
class AdminDashboardRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `eccko_w4 email is recognized as primary official admin while regular citizen is not`() {
        assertEquals("eccko.w4@gmail.com", PocketBaseClient.PRIMARY_ADMIN_EMAIL)
        assertTrue(PocketBaseClient.isOfficialAdminEmail("eccko.w4@gmail.com"))
        assertTrue(PocketBaseClient.isOfficialAdminEmail("  ECCKO.W4@GMAIL.COM  "))
        assertTrue(SurabayaRepository.isOfficialAdminEmail("eccko.w4@gmail.com"))

        assertFalse(PocketBaseClient.isOfficialAdminEmail("warga.surabaya@gmail.com"))
        assertFalse(PocketBaseClient.isOfficialAdminEmail(""))
        assertFalse(PocketBaseClient.isOfficialAdminEmail(null))
    }

    @Test
    fun `HomeScreen hides admin banner for regular citizen and shows it for official admin`() {
        var openedAdmin = false
        var adminModeState = false

        composeTestRule.setContent {
            SurabayaHariIniTheme {
                HomeScreen(
                    userAccount = UserAccount(displayName = "Cak Eko", district = "Genteng", isAdmin = adminModeState),
                    isAdmin = adminModeState,
                    currentDistrict = "Genteng",
                    incidentsState = UiState.Success(emptyList()),
                    destinationsState = UiState.Success(emptyList()),
                    momentsState = UiState.Success(emptyList()),
                    pendingEventsCount = 2,
                    pendingPollsCount = 1,
                    flaggedContentCount = 3,
                    onNavigateTab = {},
                    onOpenIncidentDetail = {},
                    onOpenDestinationDetail = {},
                    onOpenMomentDetail = {},
                    onOpenAdminDashboard = { openedAdmin = true },
                    onOpenBookmarks = {},
                    onOpenProfileAndAdmin = {},
                )
            }
        }

        // Regular citizen sees zero admin banners
        composeTestRule.onAllNodesWithTag("home_admin_dashboard_banner").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("home_open_admin_dashboard_btn").assertCountEquals(0)
    }

    @Test
    fun `ProfileAndPrivacySheetContent shows Pusat Kendali Admin only for eccko_w4 and hides for citizen`() {
        var openedDashboardCount = 0

        composeTestRule.setContent {
            SurabayaHariIniTheme {
                ProfileAndPrivacySheetContent(
                    userAccount = UserAccount(displayName = "Admin Eko", district = "Gubeng", isAdmin = true),
                    nearbyProfile = null,
                    userEmail = "eccko.w4@gmail.com",
                    isAdmin = true,
                    focusPrivacyOnly = false,
                    onToggleAdmin = {},
                    onOpenAdminDashboard = { openedDashboardCount++ },
                    onSaveProfile = { _, _, _, _, _, _, _ -> },
                    onSignOut = {},
                )
            }
        }

        composeTestRule.onNodeWithTag("profile_open_admin_dashboard_btn").assertIsDisplayed().performClick()
        assertEquals(1, openedDashboardCount)
    }

    @Test
    fun `ProfileAndPrivacySheetContent hides admin controls for regular citizen`() {
        composeTestRule.setContent {
            SurabayaHariIniTheme {
                ProfileAndPrivacySheetContent(
                    userAccount = UserAccount(displayName = "Warga Biasa", district = "Wonokromo", isAdmin = false),
                    nearbyProfile = null,
                    userEmail = "warga.biasa@gmail.com",
                    isAdmin = false,
                    focusPrivacyOnly = true,
                    onToggleAdmin = {},
                    onOpenAdminDashboard = {},
                    onSaveProfile = { _, _, _, _, _, _, _ -> },
                    onSignOut = {},
                )
            }
        }

        composeTestRule.onAllNodesWithTag("profile_open_admin_dashboard_btn").assertCountEquals(0)
        composeTestRule.onAllNodesWithTag("admin_mode_switch").assertCountEquals(0)
    }

    @Test
    fun `AdminDashboardScreen blocks non-admin access and executes bulk approve, UGC moderation, and unban for eccko_w4`() {
        val samplePendingEvent = SurabayaEvent(
            id = "ev_pending_1",
            proposerId = "u_citizen_1",
            proposerName = "Ning Rini",
            title = "Festival Rujak Uleg Balai Kota",
            eventDateText = "Minggu, 15 Nov 2026",
            timeText = "08:00 WIB",
            venueName = "Taman Surya",
            district = "Genteng",
            status = "PENDING",
        )
        val samplePendingPoll = CitizenPoll(
            id = "poll_pending_1",
            proposerId = "u_citizen_2",
            proposerName = "Cak Arif",
            title = "Perluasan Jalur Sepeda Raya Darmo",
            district = "Tegalsari",
            status = "PENDING",
            options = listOf(
                PollOption(id = "opt1", text = "Sangat Setuju"),
                PollOption(id = "opt2", text = "Evaluasi Dulu"),
            ),
        )
        val flaggedItems = collectAdminFlaggedItems(
            incidents = emptyList(),
            approvedEvents = emptyList(),
            pendingEvents = emptyList(),
            culinarySpots = emptyList(),
            moments = listOf(
                UserMoment(
                    id = "mom_spam_1",
                    authorId = "u_spammer_99",
                    authorName = "Akun Spam",
                    caption = "Kiriman melanggar aturan komunitas",
                    district = "Rungkut",
                    contentReportCount = 3,
                ),
            ),
            destinations = emptyList(),
            services = emptyList(),
        )
        val frozenProfile = NearbyProfile(
            id = "prof_frozen_1",
            userId = "u_spammer_99",
            displayName = "Akun Spam",
            district = "Rungkut",
            isBanned = true,
        )

        var bulkApprovedPair: Pair<Int, Int>? = null
        var restoredTarget: Pair<String, String>? = null
        var bannedAuthorTarget: Pair<String, String>? = null
        var unbannedTarget: Pair<String, String>? = null

        composeTestRule.setContent {
            SurabayaHariIniTheme {
                AdminDashboardScreen(
                    adminEmail = "eccko.w4@gmail.com",
                    currentAdminUid = "uid_admin_eccko",
                    isAdmin = true,
                    pendingEvents = listOf(samplePendingEvent),
                    pendingPolls = listOf(samplePendingPoll),
                    flaggedItems = flaggedItems,
                    incidents = listOf(
                        IncidentReport(
                            id = "inc_1",
                            title = "Pohon Tumbang Jl. Darmo",
                            severity = "TINGGI",
                            district = "Wonokromo",
                            address = "Jl. Raya Darmo",
                        ),
                    ),
                    publicChatRooms = listOf(
                        ChatRoom(id = "room_pub_1", title = "Ngobrol Arek Suroboyo", subtitle = "Ruang Publik Resmi"),
                    ),
                    bannedProfiles = listOf(frozenProfile),
                    onBackToHome = {},
                    onApproveAllPending = { evs, polls -> bulkApprovedPair = evs.size to polls.size },
                    onModerateEvent = { _, _, _ -> },
                    onModeratePoll = { _, _, _ -> },
                    onOpenEventDetail = {},
                    onOpenPollDetail = {},
                    onOpenFlaggedItem = {},
                    onRestoreFlaggedItem = { col, id -> restoredTarget = col to id },
                    onClearFlaggedLinks = { _, _ -> },
                    onDeletePostPermanently = { _, _ -> },
                    onBanAuthor = { uid, name -> bannedAuthorTarget = uid to name },
                    onUnbanAuthor = { uid, name -> unbannedTarget = uid to name },
                    onOpenIncidentDetail = {},
                    onToggleVerifyIncident = {},
                    onOpenCreatePublicGroup = {},
                    onOpenCreateDestination = {},
                    onOpenPublicRoom = {},
                    onDeletePublicRoom = {},
                )
            }
        }

        // Verify header shows primary admin email & bulk approve works
        composeTestRule.onNodeWithText("eccko.w4@gmail.com").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_approve_all_btn").assertIsDisplayed().performClick()
        assertEquals(1 to 1, bulkApprovedPair)

        // Switch to Tab 1: Laporan UGC & verify Restore + Ban Author callbacks
        composeTestRule.onNodeWithTag("admin_tab_reports").performClick()
        composeTestRule.onNodeWithTag("admin_restore_mom_spam_1").assertIsDisplayed().performClick()
        assertEquals("moments" to "mom_spam_1", restoredTarget)

        composeTestRule.onNodeWithTag("admin_ban_author_mom_spam_1").assertIsDisplayed().performClick()
        assertEquals("u_spammer_99" to "Akun Spam", bannedAuthorTarget)

        // Switch to Tab 3: Komunitas & Akun Dibekukan -> verify Unban callback
        composeTestRule.onNodeWithTag("admin_tab_community").performClick()
        composeTestRule.onNodeWithTag("admin_unban_btn_u_spammer_99").assertIsDisplayed().performClick()
        assertEquals("u_spammer_99" to "Akun Spam", unbannedTarget)
    }
}
