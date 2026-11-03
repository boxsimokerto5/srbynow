package com.example

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.doubleClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.pinch
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.core.app.ApplicationProvider
import com.example.data.audio.RadioPlaybackState
import com.example.data.audio.RadioStationInfo
import com.example.data.model.CulinaryReview
import com.example.data.model.CulinarySpot
import com.example.data.model.EventComment
import com.example.data.model.IncidentComment
import com.example.data.model.IncidentReport
import com.example.data.model.SurabayaEvent
import com.example.data.model.UiState
import com.example.data.model.UserAccount
import com.example.data.model.UserMoment
import com.example.data.model.NearbyProfile
import com.example.data.model.CitizenPoll
import com.example.data.model.ChatRoom
import com.example.data.model.PollOption
import com.example.data.model.PollComment
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CreatePublicGroupSheetContent
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.KulinerScreen
import com.example.ui.screens.PollingScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TemanSekitarScreen
import com.example.ui.sheets.ChatConversationSheetContent
import com.example.ui.sheets.CitizenProfileSheetContent
import com.example.ui.sheets.CulinaryDetailSheetContent
import com.example.ui.sheets.EventDetailSheetContent
import com.example.ui.sheets.IncidentDetailSheetContent
import com.example.ui.sheets.PollDetailSheetContent
import com.example.ui.sheets.ProfileAndPrivacySheetContent
import com.example.data.model.POST_TYPE_LENSA_WARGA
import com.example.data.model.POST_TYPE_MOMEN
import com.example.ui.sheets.CreateTambalBanSheetContent
import com.example.ui.sheets.RadioPlayerSheetContent
import com.example.ui.theme.SurabayaHariIniTheme
import com.example.ui.viewmodel.NearbyPeerItem
import com.example.data.model.TambalBanDraft
import com.example.data.util.TambalBanDraftManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w412dp-h892dp-xxhdpi")
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Arek Surabaya", appName)
  }

  @Test
  fun `incident detail sheet keeps image and details fixed while comments scroll with Gaya 2`() {
    val report = IncidentReport(
      id = "inc_1",
      authorName = "Eko wahyudi",
      title = "jalan berlobang",
      description = "jalan berlobang cukup dalam di lajur kiri",
      category = "INFO_WARGA",
      district = "Gubeng",
      address = "rungkut",
      verifiedCount = 1,
    )
    val comments = (1..10).map { idx ->
      IncidentComment(
        id = "cmt_$idx",
        incidentId = "inc_1",
        authorName = "Warga $idx",
        text = "Pantauan situasi nomor $idx di lokasi masih ramai lancar.",
        createdAt = System.currentTimeMillis() - (11 - idx) * 60_000L,
      )
    }

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(820.dp)) {
          IncidentDetailSheetContent(
            report = report,
            comments = comments,
            isSaved = false,
            isVerifiedByMe = true,
            onVerify = {},
            onBookmark = {},
            onSendComment = {},
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    // Initial state: photo, details, comment #1, and sticky input are displayed
    composeTestRule.onNodeWithTag("sheet_incident_image").assertIsDisplayed()
    composeTestRule.onNodeWithText("jalan berlobang").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sheet_verify_incident_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("incident_comment_input").assertIsDisplayed()
    composeTestRule.onNodeWithText("Pantauan situasi nomor 1 di lokasi masih ramai lancar.").assertIsDisplayed()

    val initialImageHeight = composeTestRule
      .onNodeWithTag("sheet_incident_image")
      .getUnclippedBoundsInRoot()
      .height
    assertTrue("Expected initial Gaya 2 image height ~140dp, got $initialImageHeight", initialImageHeight >= 136.dp)

    // Scroll comments downward (finger swipes UP on comment list)
    composeTestRule.onNodeWithTag("incident_comments_list").performTouchInput {
      swipeUp()
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("incident_comments_list").performTouchInput {
      swipeUp()
    }
    composeTestRule.waitForIdle()

    // Gaya 2 check: photo shrank smoothly toward 84dp, AND upper details + bottom input stayed fixed & visible!
    val shrunkImageHeight = composeTestRule
      .onNodeWithTag("sheet_incident_image")
      .getUnclippedBoundsInRoot()
      .height
    assertTrue("Expected shrunk Gaya 2 image height < initial ($shrunkImageHeight < $initialImageHeight)", shrunkImageHeight < initialImageHeight)

    composeTestRule.onNodeWithTag("sheet_incident_image").assertIsDisplayed()
    composeTestRule.onNodeWithText("jalan berlobang").assertIsDisplayed()
    composeTestRule.onNodeWithText("jalan berlobang cukup dalam di lajur kiri").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sheet_verify_incident_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("incident_comment_input").assertIsDisplayed()
    composeTestRule.onNodeWithText("Pantauan situasi nomor 10 di lokasi masih ramai lancar.").assertIsDisplayed()

    // Scroll comments back upward (finger swipes DOWN on comment list) -> restores photo height
    composeTestRule.onNodeWithTag("incident_comments_list").performTouchInput {
      swipeDown()
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("incident_comments_list").performTouchInput {
      swipeDown()
    }
    composeTestRule.waitForIdle()

    val restoredImageHeight = composeTestRule
      .onNodeWithTag("sheet_incident_image")
      .getUnclippedBoundsInRoot()
      .height
    assertTrue("Expected restored image height > shrunk ($restoredImageHeight > $shrunkImageHeight)", restoredImageHeight > shrunkImageHeight)
    composeTestRule.onNodeWithText("Pantauan situasi nomor 1 di lokasi masih ramai lancar.").assertIsDisplayed()
  }

  @Test
  fun `culinary detail sheet shrinks photo dynamically with Gaya 2 as reviews scroll`() {
    val spot = CulinarySpot(
      id = "cul_1",
      name = "Rawon Setan Embong Malang",
      category = "LEGENDARIS",
      description = "Rawon legendaris kuah hitam pekat daging empuk mantap.",
      district = "Genteng",
      priceRange = "Rp 30.000 - Rp 45.000",
      ratingAvg = 4.9,
      reviewCount = 10,
    )
    val reviews = (1..10).map { idx ->
      CulinaryReview(
        id = "rev_$idx",
        culinaryId = "cul_1",
        authorName = "Pecinta Rawon $idx",
        rating = 5,
        comment = "Ulasan rasa rawon nomor $idx sangat gurih dan mantap!",
        createdAt = System.currentTimeMillis() - (11 - idx) * 60_000L,
      )
    }

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(820.dp)) {
          CulinaryDetailSheetContent(
            spot = spot,
            reviews = reviews,
            isSaved = false,
            onBookmark = {},
            onSubmitReview = { _, _ -> },
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("sheet_kuliner_image").assertIsDisplayed()
    composeTestRule.onNodeWithText("Rawon Setan Embong Malang").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sheet_kuliner_maps_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("kuliner_review_input").assertIsDisplayed()
    composeTestRule.onNodeWithText("Ulasan rasa rawon nomor 1 sangat gurih dan mantap!").assertIsDisplayed()

    val initialHeight = composeTestRule
      .onNodeWithTag("sheet_kuliner_image")
      .getUnclippedBoundsInRoot()
      .height
    assertTrue("Expected initial image height ~140dp, got $initialHeight", initialHeight >= 136.dp)

    // Scroll reviews downward (finger swipes UP on review list)
    composeTestRule.onNodeWithTag("kuliner_reviews_list").performTouchInput {
      swipeUp()
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("kuliner_reviews_list").performTouchInput {
      swipeUp()
    }
    composeTestRule.waitForIdle()

    val shrunkHeight = composeTestRule
      .onNodeWithTag("sheet_kuliner_image")
      .getUnclippedBoundsInRoot()
      .height
    assertTrue("Expected shrunk culinary image height < initial ($shrunkHeight < $initialHeight)", shrunkHeight < initialHeight)

    composeTestRule.onNodeWithTag("sheet_kuliner_image").assertIsDisplayed()
    composeTestRule.onNodeWithText("Rawon Setan Embong Malang").assertIsDisplayed()
    composeTestRule.onNodeWithTag("kuliner_review_input").assertIsDisplayed()
  }

  @Test
  fun `event detail sheet shrinks photo dynamically with Gaya 2 as details scroll`() {
    val event = SurabayaEvent(
      id = "ev_1",
      title = "Festival Rujak Uleg Surabaya",
      category = "FESTIVAL",
      eventDateText = "12 Mei 2026",
      timeText = "08:00 - 14:00",
      venueName = "Jalan Kembang Jepun",
      district = "Pabean Cantikan",
      description = (1..8).joinToString("\n") { "Rundown sesi $it: Parade cobek uleg raksasa dan kuliner khas Surabaya bersama ratusan chef dan ribuan warga kota." },
      status = "PENDING",
      interestedCount = 45,
    )

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(640.dp)) {
          EventDetailSheetContent(
            event = event,
            isAdmin = true,
            isSaved = false,
            isInterested = false,
            onToggleInterest = {},
            onToggleBookmark = {},
            onModerate = { _, _ -> },
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("sheet_event_image").assertIsDisplayed()
    composeTestRule.onNodeWithText("Festival Rujak Uleg Surabaya").assertIsDisplayed()
    composeTestRule.onNodeWithText("Jalan Kembang Jepun").assertIsDisplayed()

    val initialHeight = composeTestRule
      .onNodeWithTag("sheet_event_image")
      .getUnclippedBoundsInRoot()
      .height
    assertTrue("Expected initial event image height ~140dp, got $initialHeight", initialHeight >= 136.dp)

    // Scroll details downward (finger swipes UP on details scroll)
    composeTestRule.onNodeWithTag("event_details_scroll").performTouchInput {
      swipeUp()
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("event_details_scroll").performTouchInput {
      swipeUp()
    }
    composeTestRule.waitForIdle()

    val shrunkHeight = composeTestRule
      .onNodeWithTag("sheet_event_image")
      .getUnclippedBoundsInRoot()
      .height
    assertTrue("Expected shrunk event image height < initial ($shrunkHeight < $initialHeight)", shrunkHeight < initialHeight)

    composeTestRule.onNodeWithTag("sheet_event_image").assertIsDisplayed()
    composeTestRule.onNodeWithText("Festival Rujak Uleg Surabaya").assertIsDisplayed()
  }

  @Test
  fun `create incident sheet blocks fake links and enables submit only for official or empty maps url`() {
    var submittedTitle = ""
    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(880.dp)) {
          com.example.ui.sheets.CreateIncidentSheetContent(
            defaultDistrict = "Rungkut",
            isSubmitting = false,
            onSubmit = { title, _, _, _, _, _, _, _ ->
              submittedTitle = title
            },
          )
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("form_pick_multi_photo_btn").assertIsDisplayed()
    composeTestRule.onNodeWithText("Kec. Rungkut").assertIsDisplayed()

    // Fill required fields
    composeTestRule.onNodeWithTag("form_incident_title").performTextInput("Pohon Tumbang di Rungkut")
    composeTestRule.onNodeWithTag("form_incident_address").performTextInput("Jl. Raya Rungkut No. 12")
    composeTestRule.onNodeWithTag("form_incident_desc").performTextInput("Harap berhati-hati melintas arah selatan.")
    composeTestRule.waitForIdle()

    // Empty optional maps URL -> submit is enabled
    composeTestRule.onNodeWithTag("submit_incident_btn").assertIsEnabled()

    // Enter a fake/shortener phishing link -> submit becomes disabled!
    composeTestRule.onNodeWithTag("form_incident_maps_url").performTextInput("https://bit.ly/undangan-apk")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("submit_incident_btn").assertIsNotEnabled()

    // Replace with official Google Maps shortlink -> submit becomes enabled again!
    composeTestRule.onNodeWithTag("form_incident_maps_url").performTextClearance()
    composeTestRule.onNodeWithTag("form_incident_maps_url").performTextInput("https://maps.app.goo.gl/RungkutAman123")
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("submit_incident_btn").assertIsEnabled()

    val schemaFile = java.io.File("pocketbase_collections.json").let {
      if (it.exists()) it else java.io.File("../pocketbase_collections.json")
    }
    if (schemaFile.exists()) {
      val collections = org.json.JSONArray(schemaFile.readText())
      val namesWithFileFields = mutableMapOf<String, MutableList<String>>()
      for (i in 0 until collections.length()) {
        val col = collections.getJSONObject(i)
        val colName = col.getString("name")
        val schema = col.getJSONArray("schema")
        for (j in 0 until schema.length()) {
          val field = schema.getJSONObject(j)
          if (field.getString("type") == "file") {
            namesWithFileFields.getOrPut(colName) { mutableListOf() }.add(field.getString("name"))
          }
        }
      }
      assertTrue(namesWithFileFields["incidents"].orEmpty().contains("photo"))
      assertTrue(namesWithFileFields["events"].orEmpty().contains("photo"))
      assertTrue(namesWithFileFields["culinary"].orEmpty().contains("photo"))
      assertTrue(namesWithFileFields["users"].orEmpty().contains("avatar"))
      assertTrue(namesWithFileFields["nearby_profiles"].orEmpty().contains("avatar"))
    }
    assertEquals("", submittedTitle)
  }

  @Test
  fun `tapping maps button on post with custom mapsUrl shows SafeExternalLinkConfirmationDialog first`() {
    val report = IncidentReport(
      id = "inc_safe_1",
      authorName = "Rina",
      title = "Penutupan Jalan Darmo",
      description = "Ada car free day pagi ini.",
      category = "LALU_LINTAS",
      district = "Wonokromo",
      address = "Jl. Raya Darmo",
      mapsUrl = "https://maps.app.goo.gl/DarmoSurabaya99",
      verifiedCount = 3,
    )

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(820.dp)) {
          IncidentDetailSheetContent(
            report = report,
            comments = emptyList(),
            isSaved = false,
            isVerifiedByMe = false,
            onVerify = {},
            onBookmark = {},
            onSendComment = {},
          )
        }
      }
    }

    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("verified_link_security_strip").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sheet_incident_maps_btn").performClick()
    composeTestRule.waitForIdle()

    // Safe exit confirmation dialog must appear showing full destination URL and domain
    composeTestRule.onNodeWithTag("safe_external_link_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("safe_external_link_preview_url").assertIsDisplayed()
    composeTestRule.onNodeWithText("Domain tujuan: maps.app.goo.gl").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dialog_report_suspicious_link_btn").assertIsDisplayed()

    composeTestRule.onNodeWithTag("safe_external_link_cancel_btn").performClick()
    composeTestRule.waitForIdle()
  }

  @Test
  fun `sensitive photo blur shield hides reported image until Lihat Foto clicked and exposes Lapor Blokir Admin controls`() {
    var reportClicked = false
    var blockClicked = false
    var adminDeleteClicked = false

    val blurredReport = IncidentReport(
      id = "inc_blur_1",
      authorId = "usr_offender_99",
      authorName = "Akun Mencurigakan",
      title = "Kiriman Dilaporkan Warga",
      description = "Foto kiriman ini telah dilaporkan 5 warga sehingga otomatis diburamkan.",
      category = "INFO_WARGA",
      district = "Tegalsari",
      address = "Jl. Basuki Rahmat",
      photoUrl = "https://173.249.59.183:8091/api/files/incidents/inc_blur_1/test.jpg",
      contentReportCount = 5,
      contentReportedByUids = listOf("u1", "u2", "u3", "u4", "u5"),
    )

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(840.dp)) {
          IncidentDetailSheetContent(
            report = blurredReport,
            comments = emptyList(),
            isSaved = false,
            isVerifiedByMe = false,
            currentUserId = "current_citizen_1",
            isAdmin = true,
            onVerify = {},
            onBookmark = {},
            onSendComment = {},
            onReportContent = { reportClicked = true },
            onBlockAuthor = { blockClicked = true },
            onAdminDeletePost = { adminDeleteClicked = true },
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    // 1. Sensitive Photo Blur Shield is displayed over the photo!
    composeTestRule.onNodeWithTag("sensitive_photo_blur_shield").assertIsDisplayed()
    composeTestRule.onNodeWithTag("reveal_sensitive_photo_btn").assertIsDisplayed()

    // 2. Tapping "Lihat Foto" reveals photo and shows "Samarkan" pill to re-blur
    composeTestRule.onNodeWithTag("reveal_sensitive_photo_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("rehide_sensitive_photo_btn").assertIsDisplayed()

    // Tapping "Samarkan" restores the protective blur shield
    composeTestRule.onNodeWithTag("rehide_sensitive_photo_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("sensitive_photo_blur_shield").assertIsDisplayed()

    // 3. Citizen "Lapor Asusila" opens confirmation dialog and triggers callback
    composeTestRule.onNodeWithTag("citizen_report_content_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("citizen_report_content_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("confirm_report_content_btn").performClick()
    composeTestRule.waitForIdle()
    assertTrue("Expected onReportContent to be invoked", reportClicked)

    // 4. Citizen "Blokir Penulis" opens confirmation dialog and triggers callback
    composeTestRule.onNodeWithTag("block_author_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("block_author_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("confirm_block_user_btn").performClick()
    composeTestRule.waitForIdle()
    assertTrue("Expected onBlockAuthor to be invoked", blockClicked)

    // 5. Admin "Hapus Permanen" opens confirmation dialog and triggers callback
    composeTestRule.onNodeWithTag("admin_delete_post_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("admin_delete_post_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("confirm_admin_delete_btn").performClick()
    composeTestRule.waitForIdle()
    assertTrue("Expected onAdminDeletePost to be invoked", adminDeleteClicked)
  }

  @Test
  fun `splash screen displays icon mascot, app title, and invokes finish callback`() {
    var splashFinished = false

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(840.dp)) {
          SplashScreen(
            splashDurationMillis = 5000L,
            onSplashFinished = { splashFinished = true },
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    // 1. Verify splash screen container, logo mascot, title, and badges are displayed
    composeTestRule.onNodeWithTag("splash_screen_container").assertIsDisplayed()
    composeTestRule.onNodeWithTag("splash_screen_logo", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("splash_screen_logo_card", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("splash_city_badge", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("splash_app_title", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Arek Surabaya", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("splash_progress_indicator", useUnmergedTree = true).assertIsDisplayed()

    // 2. Tapping anywhere on the splash screen invokes onSplashFinished immediately
    composeTestRule.onNodeWithTag("splash_screen_container").performClick()
    composeTestRule.waitForIdle()
    assertTrue("Expected splash screen onSplashFinished to be invoked upon tap", splashFinished)
  }

  @Test
  fun `home screen displays Radio SS 100 FM shortcut and banner and invokes callback`() {
    var radioOpened = false

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(840.dp)) {
          HomeScreen(
            userAccount = UserAccount(id = "usr_1", displayName = "Bambang"),
            isAdmin = false,
            currentDistrict = "Gubeng",
            incidentsState = UiState.Success(emptyList()),
            destinationsState = UiState.Success(emptyList()),
            momentsState = UiState.Success(emptyList()),
            onNavigateTab = {},
            onOpenIncidentDetail = {},
            onOpenDestinationDetail = {},
            onOpenMomentDetail = {},
            onOpenBookmarks = {},
            onOpenProfileAndAdmin = {},
            onOpenRadio = { radioOpened = true },
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    // 1. Verify Radio SS shortcut in menu section is displayed
    composeTestRule.onNodeWithTag("home_shortcut_radio_ss", useUnmergedTree = true).assertIsDisplayed()

    // 2. Click shortcut and verify callback invoked
    composeTestRule.onNodeWithTag("home_shortcut_radio_ss").performClick()
    composeTestRule.waitForIdle()
    assertTrue("Expected onOpenRadio to be invoked by shortcut", radioOpened)

    // 3. Verify prominent featured radio banner button is displayed
    composeTestRule.onNodeWithTag("home_btn_listen_radio", useUnmergedTree = true).assertIsDisplayed()
  }

  @Test
  fun `radio player sheet displays station info, controls, and toggles play state`() {
    var togglePlayClicked = false
    var updatedVolume: Float? = null
    var closed = false

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(840.dp)) {
          RadioPlayerSheetContent(
            playbackState = RadioPlaybackState.PLAYING,
            volume = 0.85f,
            errorMessage = null,
            stationInfo = RadioStationInfo(),
            onTogglePlay = { togglePlayClicked = true },
            onSetVolume = { updatedVolume = it },
            onClose = { closed = true },
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    // 1. Verify sheet content, play button, and controls
    composeTestRule.onNodeWithTag("radio_player_sheet_content").assertIsDisplayed()
    composeTestRule.onNodeWithTag("radio_play_pause_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("radio_volume_slider").assertIsDisplayed()
    composeTestRule.onNodeWithTag("radio_whatsapp_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("radio_phone_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("radio_share_btn").assertIsDisplayed()

    // 2. Toggle play button
    composeTestRule.onNodeWithTag("radio_play_pause_btn").performClick()
    composeTestRule.waitForIdle()
    assertTrue("Expected onTogglePlay to be invoked", togglePlayClicked)

    // 3. Close button
    composeTestRule.onNodeWithTag("radio_close_btn").performClick()
    composeTestRule.waitForIdle()
    assertTrue("Expected onClose to be invoked", closed)
  }

  @Test
  fun `surabaya radio mini player displays controls and handles interaction`() {
    var toggleClicked = false
    var closeClicked = false
    var openFullClicked = false

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(120.dp)) {
          SurabayaRadioMiniPlayer(
            playbackState = RadioPlaybackState.PLAYING,
            stationInfo = RadioStationInfo(),
            onToggle = { toggleClicked = true },
            onClose = { closeClicked = true },
            onOpenFull = { openFullClicked = true },
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    // 1. Verify mini player bar and buttons
    composeTestRule.onNodeWithTag("radio_mini_player_bar").assertIsDisplayed()
    composeTestRule.onNodeWithTag("radio_mini_play_pause_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("radio_mini_close_btn").assertIsDisplayed()

    // 2. Click play/pause on mini player
    composeTestRule.onNodeWithTag("radio_mini_play_pause_btn").performClick()
    composeTestRule.waitForIdle()
    assertTrue("Expected mini player onToggle to be invoked", toggleClicked)

    // 3. Click close on mini player
    composeTestRule.onNodeWithTag("radio_mini_close_btn").performClick()
    composeTestRule.waitForIdle()
    assertTrue("Expected mini player onClose to be invoked", closeClicked)

    // 4. Click bar to open full player
    composeTestRule.onNodeWithTag("radio_mini_player_bar").performClick()
    composeTestRule.waitForIdle()
    assertTrue("Expected onOpenFull to be invoked", openFullClicked)
  }

  @Test
  fun `polling screen displays polls, options, filters and handles voting and sharing actions`() {
    val samplePoll = CitizenPoll(
      id = "poll_test_1",
      title = "Pembangunan Jalur Sepeda Terproteksi",
      description = "Dukungan warga untuk memperluas jalur sepeda terpisah di Jalan Basuki Rahmat.",
      category = "TRANSPORTASI",
      district = "Genteng",
      totalVotes = 10,
      options = listOf(
        PollOption(id = "opt_1", text = "Sangat Setuju", voteCount = 7),
        PollOption(id = "opt_2", text = "Cukup Setuju", voteCount = 2),
        PollOption(id = "opt_3", text = "Kurang Perlu", voteCount = 1),
      ),
    )

    var votedOptionId: String? = null
    var sharedTextPollId: String? = null
    var sharedJpgPollId: String? = null
    var detailOpenedPollId: String? = null
    var createPollOpened = false

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(892.dp)) {
          PollingScreen(
            pollsState = UiState.Success(listOf(samplePoll)),
            pendingPollsState = UiState.Success(emptyList()),
            myProposedPolls = emptyList(),
            currentUserId = "user_test_me",
            isAdmin = false,
            onVote = { pollId, optId -> votedOptionId = optId },
            onOpenPollDetail = { detailOpenedPollId = it },
            onOpenCreatePoll = { createPollOpened = true },
            onOpenAdminPending = {},
            onShareText = { sharedTextPollId = it.id },
            onShareJpg = { sharedJpgPollId = it.id },
            onDownloadJpg = {},
            onDeletePoll = {},
            onBackToHome = {},
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    // 1. Verify screen, card, and title
    composeTestRule.onNodeWithTag("polling_screen").assertIsDisplayed()
    composeTestRule.onNodeWithTag("poll_card_poll_test_1").assertIsDisplayed()
    composeTestRule.onNodeWithText("Pembangunan Jalur Sepeda Terproteksi").assertIsDisplayed()

    // 2. Click option to vote
    composeTestRule.onNodeWithTag("btn_card_vote_opt_opt_1").performClick()
    composeTestRule.waitForIdle()
    assertEquals("opt_1", votedOptionId)

    // 3. Click share text
    composeTestRule.onNodeWithTag("btn_card_share_text_poll_test_1").performClick()
    composeTestRule.waitForIdle()
    assertEquals("poll_test_1", sharedTextPollId)

    // 4. Click share JPG
    composeTestRule.onNodeWithTag("btn_card_share_jpg_poll_test_1").performClick()
    composeTestRule.waitForIdle()
    assertEquals("poll_test_1", sharedJpgPollId)

    // 5. Click comments to open detail
    composeTestRule.onNodeWithTag("btn_card_comments_poll_test_1").performClick()
    composeTestRule.waitForIdle()
    assertEquals("poll_test_1", detailOpenedPollId)

    // 6. Click FAB to create poll
    composeTestRule.onNodeWithTag("fab_create_poll").performClick()
    composeTestRule.waitForIdle()
    assertTrue(createPollOpened)
  }

  @Test
  fun `poll detail sheet renders options, comments, and triggers text and JPG sharing`() {
    val samplePoll = CitizenPoll(
      id = "poll_detail_test",
      title = "Penataan Sentra Wisata Kuliner (SWK) Surabaya",
      description = "Pemberlakuan pembayaran QRIS dan standardisasi kebersihan di seluruh SWK.",
      category = "KULINER_WISATA",
      district = "Wonokromo",
      totalVotes = 25,
      options = listOf(
        PollOption(id = "opt_yes", text = "Setuju Wajib QRIS", voteCount = 20),
        PollOption(id = "opt_no", text = "Biarkan Fleksibel", voteCount = 5),
      ),
    )
    val comments = listOf(
      PollComment(
        id = "cmt_1",
        pollId = "poll_detail_test",
        authorName = "Cak Hadi",
        text = "Sangat setuju agar pembukuan UMKM Surabaya makin tertib dan aman.",
      ),
    )

    var shareTextCalled = false
    var shareJpgCalled = false
    var downloadJpgCalled = false
    var dismissCalled = false

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(800.dp)) {
          PollDetailSheetContent(
            poll = samplePoll,
            comments = comments,
            currentUserId = "user_me",
            isAdmin = false,
            onVote = { _, _ -> },
            onAddComment = {},
            onShareText = { shareTextCalled = true },
            onShareJpg = { shareJpgCalled = true },
            onDownloadJpg = { downloadJpgCalled = true },
            onDismiss = { dismissCalled = true },
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    // 1. Verify title and comment
    composeTestRule.onNodeWithText("Penataan Sentra Wisata Kuliner (SWK) Surabaya").assertIsDisplayed()
    composeTestRule.onNodeWithText("Sangat setuju agar pembukuan UMKM Surabaya makin tertib dan aman.").assertIsDisplayed()

    // 2. Click Share Text button
    composeTestRule.onNodeWithTag("btn_share_poll_text").performClick()
    composeTestRule.waitForIdle()
    assertTrue(shareTextCalled)

    // 3. Click Share JPG button
    composeTestRule.onNodeWithTag("btn_share_poll_jpg").performClick()
    composeTestRule.waitForIdle()
    assertTrue(shareJpgCalled)

    // 4. Click Download JPG button
    composeTestRule.onNodeWithTag("btn_download_poll_jpg").performClick()
    composeTestRule.waitForIdle()
    assertTrue(downloadJpgCalled)

    // 5. Click Close button
    composeTestRule.onNodeWithTag("btn_close_poll_detail").performClick()
    composeTestRule.waitForIdle()
    assertTrue(dismissCalled)
  }

  @Test
  fun citizenProfileSheet_displaysMomentsAndTriggersDetailClick() {
    var openedMomentId = ""
    var greetedUser = ""

    val sampleProfile = NearbyProfile(
      userId = "citizen_456",
      displayName = "Cak Hadi",
      district = "Wonokromo",
      bio = "Pecinta rawon dan gowes pagi.",
      interests = "Gowes, Kuliner, Fotografi",
    )
    val sampleMoments = listOf(
      UserMoment(
        id = "mom_101",
        authorId = "citizen_456",
        authorName = "Cak Hadi",
        caption = "Sunset cakep nang Jembatan Ujung Galuh!",
        locationName = "Jembatan Ujung Galuh",
        district = "Wonokromo",
        likesCount = 18,
      ),
      UserMoment(
        id = "mom_102",
        authorId = "citizen_456",
        authorName = "Cak Hadi",
        caption = "Sarapan lontong balap legendaris.",
        locationName = "Pasar Wonokromo",
        district = "Wonokromo",
        likesCount = 25,
      ),
    )

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        CitizenProfileSheetContent(
          citizenUserId = "citizen_456",
          initialName = "Cak Hadi",
          citizenProfile = sampleProfile,
          citizenMoments = sampleMoments,
          currentUserId = "current_user_123",
          isAdmin = false,
          onOpenMomentDetail = { openedMomentId = it },
          onGreetCitizen = { greetedUser = it.displayName },
          onBlockCitizen = { _, _ -> },
          onEditMyProfile = {},
          onClose = {},
        )
      }
    }

    // Verify profile header information
    composeTestRule.onNodeWithTag("citizen_profile_sheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("stat_moments_count").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cak Hadi").assertIsDisplayed()
    composeTestRule.onNodeWithText("Pecinta rawon dan gowes pagi.").assertIsDisplayed()

    // Verify Instagram-style moment grid thumbnail
    composeTestRule.onNodeWithTag("moment_thumbnail_mom_101").assertIsDisplayed()
    composeTestRule.onNodeWithTag("moment_thumbnail_mom_101").performClick()
    composeTestRule.waitForIdle()
    assertEquals("mom_101", openedMomentId)

    // Verify Greet button
    composeTestRule.onNodeWithTag("greet_citizen_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("greet_citizen_btn").performClick()
    composeTestRule.waitForIdle()
    assertEquals("Cak Hadi", greetedUser)
  }

  @Test
  fun userProfileSheet_showsMyMomentsAndPostButton() {
    var postMomentTriggered = false
    var openedMomentId = ""

    val myAccount = UserAccount(
      userId = "my_uid",
      displayName = "Ning Sarah",
      district = "Genteng",
      bio = "Halo warga Surabaya!",
    )
    val myMoments = listOf(
      UserMoment(
        id = "mom_mine_1",
        authorId = "my_uid",
        authorName = "Ning Sarah",
        caption = "Ngopi sore nang Tunjungan",
        district = "Genteng",
        likesCount = 42,
      ),
    )

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        ProfileAndPrivacySheetContent(
          userAccount = myAccount,
          nearbyProfile = NearbyProfile(userId = "my_uid", displayName = "Ning Sarah", district = "Genteng"),
          userEmail = "sarah@arek.sby",
          isAdmin = false,
          focusPrivacyOnly = false,
          userMoments = myMoments,
          onOpenMomentDetail = { openedMomentId = it },
          onOpenCreateMoment = { postMomentTriggered = true },
          onToggleAdmin = {},
          onSaveProfile = { _, _, _, _, _, _, _ -> },
          onSignOut = {},
        )
      }
    }

    composeTestRule.onNodeWithTag("tab_my_moments").assertIsDisplayed()
    composeTestRule.onNodeWithTag("moment_thumbnail_mom_mine_1").assertIsDisplayed()
    composeTestRule.onNodeWithTag("moment_thumbnail_mom_mine_1").performClick()
    composeTestRule.waitForIdle()
    assertEquals("mom_mine_1", openedMomentId)

    composeTestRule.onNodeWithTag("btn_post_new_moment_from_profile").performClick()
    composeTestRule.waitForIdle()
    assertTrue(postMomentTriggered)
  }

  @Test
  fun `kulinerScreen sorts spots by nearest gps distance by default and shows distance badges`() {
    val userLat = -7.2575
    val userLng = 112.7521 // Surabaya Pusat / Genteng

    // Spot 1: Far away (~7.5 km away in Rungkut)
    val spotFar = CulinarySpot(
      id = "spot_far",
      name = "Soto Rungkut Madura",
      category = "RAWON_SOTO",
      district = "Rungkut",
      lat = -7.3200,
      lng = 112.7700,
      ratingAvg = 4.9,
    )
    // Spot 2: Very close (~100 m away in Genteng)
    val spotNear = CulinarySpot(
      id = "spot_near",
      name = "Rawon Embong Malang Mantap",
      category = "RAWON_SOTO",
      district = "Genteng",
      lat = -7.2580,
      lng = 112.7525,
      ratingAvg = 4.2,
    )
    // Spot 3: Medium (~2 km away in Gubeng)
    val spotMedium = CulinarySpot(
      id = "spot_medium",
      name = "Penyetan Bang Ali Gubeng",
      category = "PENYETAN_SAMBAL",
      district = "Gubeng",
      lat = -7.2750,
      lng = 112.7550,
      ratingAvg = 4.7,
    )

    val culinaryList = listOf(spotFar, spotNear, spotMedium)

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(820.dp)) {
          KulinerScreen(
            culinaryState = UiState.Success(culinaryList),
            bookmarks = emptyList(),
            currentUserId = "user_test",
            userLat = userLat,
            userLng = userLng,
            isLiveGpsActive = true,
            onSelectSpot = {},
            onToggleBookmark = {},
            onOpenCreateSheet = {},
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    // Verify distance badges and cards are rendered in nearest-first order
    composeTestRule.onNodeWithTag("sort_kuliner_distance").assertIsDisplayed()
    composeTestRule.onNodeWithTag("sort_kuliner_rating").assertIsDisplayed()
    composeTestRule.onNodeWithTag("kuliner_gps_status").assertIsDisplayed()

    composeTestRule.onNodeWithTag("kuliner_card_spot_near").assertIsDisplayed()
    composeTestRule.onNodeWithTag("kuliner_card_spot_medium").assertIsDisplayed()
    composeTestRule.onNodeWithTag("kuliner_distance_tag_spot_near").assertIsDisplayed()
    composeTestRule.onNodeWithTag("kuliner_distance_tag_spot_medium").assertIsDisplayed()

    val nearTop = composeTestRule.onNodeWithTag("kuliner_card_spot_near").getUnclippedBoundsInRoot().top
    val mediumTop = composeTestRule.onNodeWithTag("kuliner_card_spot_medium").getUnclippedBoundsInRoot().top
    assertTrue("Expected nearest spot to appear above medium spot ($nearTop < $mediumTop)", nearTop < mediumTop)

    // Switch to Rating sort -> highest rated spot_far (4.9) moves to top
    composeTestRule.onNodeWithTag("sort_kuliner_rating").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("kuliner_card_spot_far").assertIsDisplayed()
    val farTopWhenRating = composeTestRule.onNodeWithTag("kuliner_card_spot_far").getUnclippedBoundsInRoot().top
    val mediumTopWhenRating = composeTestRule.onNodeWithTag("kuliner_card_spot_medium").getUnclippedBoundsInRoot().top
    assertTrue("Expected highest-rated spot above medium spot ($farTopWhenRating < $mediumTopWhenRating)", farTopWhenRating < mediumTopWhenRating)

    // Switch back to GPS Nearest sort -> spot_near returns to #1
    composeTestRule.onNodeWithTag("sort_kuliner_distance").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("kuliner_card_spot_near").assertIsDisplayed()
  }

  @Test
  fun `homeScreen prioritizes nearest culinary spots near top and orders carousel by GPS distance`() {
    val userLat = -7.2575
    val userLng = 112.7521

    val spotFar = CulinarySpot(
      id = "home_far",
      name = "Soto Rungkut Jauh",
      category = "RAWON_SOTO",
      district = "Rungkut",
      lat = -7.3200,
      lng = 112.7700,
      ratingAvg = 4.9,
    )
    val spotNear = CulinarySpot(
      id = "home_near",
      name = "Rawon Genteng Dekat",
      category = "RAWON_SOTO",
      district = "Genteng",
      lat = -7.2580,
      lng = 112.7525,
      ratingAvg = 4.3,
    )

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(892.dp)) {
          HomeScreen(
            userAccount = UserAccount(id = "usr_1", displayName = "Cak Budi"),
            isAdmin = false,
            currentDistrict = "Genteng",
            userLat = userLat,
            userLng = userLng,
            isLiveGpsActive = true,
            incidentsState = UiState.Success(emptyList()),
            destinationsState = UiState.Success(emptyList()),
            culinaryState = UiState.Success(listOf(spotFar, spotNear)),
            momentsState = UiState.Success(emptyList()),
            onNavigateTab = {},
            onOpenIncidentDetail = {},
            onOpenDestinationDetail = {},
            onOpenMomentDetail = {},
            onOpenBookmarks = {},
            onOpenProfileAndAdmin = {},
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    // Kuliner shortcut is right at the start of the menu
    composeTestRule.onNodeWithTag("home_shortcut_kuliner", useUnmergedTree = true).assertIsDisplayed()

    // Scroll slightly to the culinary carousel right below shortcuts
    composeTestRule.onNodeWithTag("home_screen_content")
      .performScrollToNode(hasTestTag("home_culinary_lazy_row"))
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("home_culinary_home_near").assertIsDisplayed()
    composeTestRule.onNodeWithText("Terdekat #1", useUnmergedTree = true).assertIsDisplayed()

    val nearLeft = composeTestRule.onNodeWithTag("home_culinary_home_near").getUnclippedBoundsInRoot().left
    val farLeft = composeTestRule.onNodeWithTag("home_culinary_home_far").getUnclippedBoundsInRoot().left
    assertTrue("Expected nearest culinary card first on HomeScreen ($nearLeft < $farLeft)", nearLeft < farLeft)
  }

  @Test
  fun `chatScreen hides create group button for regular citizens and shows it only for Admin`() {
    val sampleGroup = ChatRoom(
      id = "pub_warga_sby",
      title = "Ruang Warga Surabaya",
      subtitle = "Ngobrol santai kabar kota hari ini",
      roomType = "PUBLIC",
      lastMessage = "Halo rek!",
    )

    var isAdminState by androidx.compose.runtime.mutableStateOf(false)
    var createGroupClicked = false

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(840.dp)) {
          ChatScreen(
            publicRooms = listOf(sampleGroup),
            directRooms = emptyList(),
            currentUserId = "user_1",
            selectedSubTab = 0,
            onSelectSubTab = {},
            onStartNewDirectChat = {},
            onOpenRoom = {},
            isAdmin = isAdminState,
            onCreatePublicGroup = { createGroupClicked = true },
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    // 1. Regular citizen: sees admin-managed notice + 1-on-1 Chat Warga button, NO Buat Grup controls
    composeTestRule.onNodeWithTag("chat_group_admin_notice").assertIsDisplayed()
    composeTestRule.onNodeWithTag("chat_start_direct_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("chat_create_group_btn").assertDoesNotExist()
    composeTestRule.onNodeWithTag("fab_create_public_group").assertDoesNotExist()

    // 2. Switch to Admin: Buat Grup button, FAB, and Admin banner appear
    isAdminState = true
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("chat_group_admin_banner").assertIsDisplayed()
    composeTestRule.onNodeWithTag("chat_create_group_btn").assertIsDisplayed()
    composeTestRule.onNodeWithTag("fab_create_public_group").assertIsDisplayed()
    composeTestRule.onNodeWithTag("chat_group_admin_notice").assertDoesNotExist()

    composeTestRule.onNodeWithTag("chat_create_group_btn").performClick()
    composeTestRule.waitForIdle()
    assertTrue("Expected onCreatePublicGroup callback when Admin taps Buat Grup", createGroupClicked)
  }

  @Test
  fun `createPublicGroupSheetContent validates inputs and submits group details for Admin`() {
    var submittedTitle = ""
    var submittedSubtitle = ""
    var submittedWelcome = ""

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(840.dp)) {
          CreatePublicGroupSheetContent(
            isSubmitting = false,
            onSubmit = { t, s, w ->
              submittedTitle = t
              submittedSubtitle = s
              submittedWelcome = w
            },
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("create_public_group_sheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("submit_create_group_btn").assertIsNotEnabled()

    composeTestRule.onNodeWithTag("form_group_title").performTextInput("Grup Kuliner Malam SBY")
    composeTestRule.onNodeWithTag("form_group_subtitle").performTextInput("Berbagi info warung malam & kaki lima")
    composeTestRule.onNodeWithTag("form_group_welcome").performTextInput("Monggo bagi rekomendasi kuliner malam favoritmu!")
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("submit_create_group_btn").assertIsEnabled()
    composeTestRule.onNodeWithTag("submit_create_group_btn").performClick()
    composeTestRule.waitForIdle()

    assertEquals("Grup Kuliner Malam SBY", submittedTitle)
    assertEquals("Berbagi info warung malam & kaki lima", submittedSubtitle)
    assertEquals("Monggo bagi rekomendasi kuliner malam favoritmu!", submittedWelcome)
  }

  @Test
  fun `temanSekitarScreen filters Perempuan and LakiLaki displays estimated distance and opens profile or photo zoom`() {
    val femalePeer = NearbyProfile(
      id = "peer_nadia",
      userId = "uid_nadia",
      displayName = "Nadia Kusuma",
      gender = "PEREMPUAN",
      district = "Wonokromo",
      bio = "Suka hunting kuliner penyetan & kopi susu.",
      interests = "Kuliner, Kopi",
    )
    val malePeer = NearbyProfile(
      id = "peer_bima",
      userId = "uid_bima",
      displayName = "Bima Pratama",
      gender = "LAKI_LAKI",
      district = "Gubeng",
      bio = "Hobi lari pagi di Taman Bungkul.",
      interests = "Lari Pagi, Fotografi",
    )

    val peers = listOf(
      NearbyPeerItem(profile = femalePeer, distanceKm = 0.34),
      NearbyPeerItem(profile = malePeer, distanceKm = 1.82),
    )

    var openedProfilePeer: NearbyProfile? = null

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(860.dp)) {
          TemanSekitarScreen(
            nearbyPeersState = UiState.Success(peers),
            userAccount = UserAccount(id = "me", displayName = "Cak Raka", district = "Genteng", searchRadiusKm = 10.0),
            isLiveGpsActive = true,
            onRefreshGps = {},
            onOpenPrivacySheet = {},
            onGreetPeer = {},
            onOpenCitizenProfile = { openedProfilePeer = it },
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    // 1. Both citizens & their estimated distances and gender badges are displayed under "Semua (2)"
    composeTestRule.onNodeWithTag("filter_gender_all").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filter_gender_female").assertIsDisplayed()
    composeTestRule.onNodeWithTag("filter_gender_male").assertIsDisplayed()

    composeTestRule.onNodeWithTag("nearby_peer_card_peer_nadia").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nearby_peer_card_peer_bima").assertIsDisplayed()
    composeTestRule.onNodeWithTag("peer_distance_badge_peer_nadia", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("peer_distance_badge_peer_bima", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Estimasi ± 350 m", useUnmergedTree = true).assertIsDisplayed()

    // 2. Filter "Perempuan" -> only Nadia appears, Bima is hidden
    composeTestRule.onNodeWithTag("filter_gender_female").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("nearby_peer_card_peer_nadia").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nearby_peer_card_peer_bima").assertDoesNotExist()

    // 3. Filter "Laki-laki" -> only Bima appears, Nadia is hidden
    composeTestRule.onNodeWithTag("filter_gender_male").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("nearby_peer_card_peer_bima").assertIsDisplayed()
    composeTestRule.onNodeWithTag("nearby_peer_card_peer_nadia").assertDoesNotExist()

    // 4. Filter back to "Semua" -> clicking a person's card triggers slide-up profile bottom sheet callback
    composeTestRule.onNodeWithTag("filter_gender_all").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("nearby_peer_card_peer_nadia").performClick()
    composeTestRule.waitForIdle()
    assertEquals("uid_nadia", openedProfilePeer?.userId)

    // 5. Clicking bagian foto (avatar) opens the 2-finger pinch-to-zoom dialog directly
    composeTestRule.onNodeWithTag("peer_avatar_photo_peer_nadia", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("citizen_photo_zoom_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("100%").assertIsDisplayed()

    // Perform 2-finger pinch outward on the photo canvas -> zoom percentage increases above 100%
    composeTestRule.onNodeWithTag("citizen_photo_zoom_canvas").performTouchInput {
      pinch(
        start0 = Offset(center.x - 20f, center.y),
        end0 = Offset(center.x - 140f, center.y),
        start1 = Offset(center.x + 20f, center.y),
        end1 = Offset(center.x + 140f, center.y),
        durationMillis = 250L,
      )
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("citizen_photo_zoom_close_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("citizen_photo_zoom_dialog").assertDoesNotExist()
  }

  @Test
  fun `citizenProfileSheetContent shows estimated distance and gender and opens 2 finger zoomable photo on avatar click`() {
    val profile = NearbyProfile(
      id = "peer_sari",
      userId = "uid_sari",
      displayName = "Sari Wulandari",
      gender = "PEREMPUAN",
      district = "Genteng",
      bio = "Pecinta kuliner Jalan Tunjungan & kopi pagi.",
      interests = "Kuliner, Jalan Pagi",
    )

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(860.dp)) {
          CitizenProfileSheetContent(
            citizenUserId = "uid_sari",
            initialName = "Sari Wulandari",
            citizenProfile = profile,
            citizenMoments = emptyList(),
            currentUserId = "uid_viewer",
            isAdmin = false,
            estimatedDistanceKm = 1.6,
            onOpenMomentDetail = {},
            onGreetCitizen = {},
            onBlockCitizen = { _, _ -> },
            onEditMyProfile = {},
            onClose = {},
          )
        }
      }
    }

    composeTestRule.waitForIdle()

    // Verify slide-up profile header displays estimated distance and gender badges
    composeTestRule.onNodeWithTag("citizen_profile_sheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("citizen_profile_distance_badge", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("citizen_profile_gender_badge", useUnmergedTree = true).assertIsDisplayed()

    // Tap the profile photo inside the slide-up profile sheet -> opens fullscreen zoom dialog
    composeTestRule.onNodeWithTag("citizen_profile_avatar_btn").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("citizen_photo_zoom_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithText("100%").assertIsDisplayed()

    // Double-tap zoom or 2-finger pinch works smoothly
    composeTestRule.onNodeWithTag("citizen_photo_zoom_canvas").performTouchInput {
      doubleClick()
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("250%").assertIsDisplayed()

    // Close fullscreen photo viewer
    composeTestRule.onNodeWithTag("citizen_photo_zoom_close_btn").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("citizen_photo_zoom_dialog").assertDoesNotExist()
  }

  @Test
  fun `createTambalBanSheetContent renders smoothly without crashing`() {
    var closeClicked = false
    composeTestRule.setContent {
      SurabayaHariIniTheme {
        CreateTambalBanSheetContent(
          defaultDistrict = "Genteng",
          onSubmit = { _, _, _, _, _, _, _, _, _, _, _, _, _ -> },
          onClose = { closeClicked = true },
        )
      }
    }
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("btn_close_sheet").assertIsDisplayed()
    composeTestRule.onNodeWithTag("input_tambal_name").assertIsDisplayed()
    composeTestRule.onNodeWithTag("input_tambal_address").assertIsDisplayed()
    composeTestRule.onNodeWithTag("btn_submit_tambal_ban").assertExists()
  }

  @Test
  fun `profileSheet displays Momen and Lensa Warga categories and filters correctly`() {
    val sampleMoments = listOf(
      UserMoment(id = "m1", authorId = "u1", authorName = "Budi", caption = "Nongkrong di Balai Pemuda", postType = POST_TYPE_MOMEN),
      UserMoment(id = "m2", authorId = "u1", authorName = "Budi", caption = "Kemegahan Monumen Tugu Pahlawan", postType = POST_TYPE_LENSA_WARGA, locationName = "Tugu Pahlawan"),
    )

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        ProfileAndPrivacySheetContent(
          userAccount = UserAccount(id = "u1", displayName = "Budi"),
          nearbyProfile = null,
          userEmail = "budi@surabaya.id",
          isAdmin = false,
          focusPrivacyOnly = false,
          userMoments = sampleMoments,
          onToggleAdmin = {},
          onSaveProfile = { _, _, _, _, _, _, _ -> },
          onSignOut = {},
        )
      }
    }
    composeTestRule.waitForIdle()

    // Verify both tabs exist: Momen and Lensa Warga
    composeTestRule.onNodeWithTag("tab_my_moments").assertIsDisplayed()
    composeTestRule.onNodeWithTag("tab_my_lensa_warga").assertIsDisplayed()

    // Click Lensa Warga tab
    composeTestRule.onNodeWithTag("tab_my_lensa_warga").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("moment_thumbnail_m2").assertIsDisplayed()
  }

  @Test
  fun `chat conversation sheet displays user photo name and location and triggers profile open on click`() {
    val sampleRoom = ChatRoom(
      id = "room_cak_dimas",
      roomKey = "direct_user_1_peer_dimas",
      roomType = "DIRECT",
      title = "Cak Dimas Surabaya",
      subtitle = "Genteng • Tegalsari",
      participantUids = listOf("my_uid", "peer_dimas"),
      participantNames = "Saya|Cak Dimas Surabaya",
    )
    val samplePeerProfile = NearbyProfile(
      id = "peer_dimas",
      userId = "peer_dimas",
      displayName = "Cak Dimas Surabaya",
      district = "Genteng",
      avatarColorHex = "#006B5E",
    )

    var profileOpened = false

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(860.dp)) {
          ChatConversationSheetContent(
            room = sampleRoom,
            messages = emptyList(),
            currentUserId = "my_uid",
            peerProfile = samplePeerProfile,
            onOpenPeerProfile = { profileOpened = true },
            onSendMessage = { _, _ -> },
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // 1. Verify user name and location are displayed, and "Sinkron 2 Arah" / "Room ID" are replaced
    composeTestRule.onNodeWithTag("chat_peer_profile_header").assertIsDisplayed()
    composeTestRule.onNodeWithText("Cak Dimas Surabaya", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithTag("chat_peer_location_row", useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Kec. Genteng, Surabaya", substring = true, useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Sinkron 2 Arah").assertDoesNotExist()
    composeTestRule.onNodeWithTag("chat_sheet_room_id").assertDoesNotExist()

    // 2. Click the profile header -> triggers onOpenPeerProfile
    composeTestRule.onNodeWithTag("chat_peer_profile_header").performClick()
    composeTestRule.waitForIdle()
    assertTrue("Expected profileOpened callback when chat header is clicked", profileOpened)
  }

  @Test
  fun `event detail sheet displays comments, submits new comment, and opens commenter citizen profile on click`() {
    val sampleEvent = SurabayaEvent(
      id = "event_test_bungkul",
      title = "Festival Malam Akustik Taman Bungkul",
      category = "FESTIVAL",
      eventDateText = "Sabtu, 14 Nov 2026",
      timeText = "18.30 - 22.00 WIB",
      venueName = "Amphitheater Taman Bungkul",
      district = "Wonokromo",
      address = "Jl. Taman Bungkul",
      status = "APPROVED",
      interestedCount = 42,
    )

    val sampleComments = listOf(
      EventComment(
        id = "cmt_101",
        eventId = "event_test_bungkul",
        authorId = "user_nadia_wonokromo",
        authorName = "Nadia Kusuma",
        text = "Ada yang mau budhal bareng jam 7 malam rek?",
        createdAt = System.currentTimeMillis() - 1000 * 60 * 30,
      )
    )

    var sentComment = ""
    var openedAuthorId = ""
    var openedAuthorName = ""

    composeTestRule.setContent {
      SurabayaHariIniTheme {
        Box(modifier = Modifier.width(412.dp).height(860.dp)) {
          EventDetailSheetContent(
            event = sampleEvent,
            comments = sampleComments,
            isAdmin = false,
            isSaved = false,
            isInterested = false,
            currentUserId = "my_uid",
            onToggleInterest = {},
            onToggleBookmark = {},
            onModerate = { _, _ -> },
            onSendComment = { sentComment = it },
            onOpenCitizenProfile = { uid, name ->
              openedAuthorId = uid
              openedAuthorName = name
            },
          )
        }
      }
    }
    composeTestRule.waitForIdle()

    // 1. Verify event title and comment section
    composeTestRule.onNodeWithText("Festival Malam Akustik Taman Bungkul", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Diskusi & Janjian Bareng Warga (1)", substring = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Nadia Kusuma", substring = true, useUnmergedTree = true).assertIsDisplayed()
    composeTestRule.onNodeWithText("Ada yang mau budhal bareng jam 7 malam rek?", substring = true, useUnmergedTree = true).assertIsDisplayed()

    // 2. Click author name to open their citizen profile
    composeTestRule.onNodeWithTag("event_comment_author_cmt_101", useUnmergedTree = true).performClick()
    composeTestRule.waitForIdle()
    assertEquals("user_nadia_wonokromo", openedAuthorId)
    assertEquals("Nadia Kusuma", openedAuthorName)

    // 3. Send a new comment
    composeTestRule.onNodeWithTag("event_comment_input").performTextInput("Aku budhal bareng mbak!")
    composeTestRule.onNodeWithTag("event_comment_send_btn").performClick()
    composeTestRule.waitForIdle()
    assertEquals("Aku budhal bareng mbak!", sentComment)
  }

  @Test
  fun `tambal ban draft manager saves and loads and clears draft properly`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    TambalBanDraftManager.clearDraft(context)
    assertNull(TambalBanDraftManager.loadDraft(context))

    val draft = TambalBanDraft(
      name = "Tambal Ban Cacing Cak Munir",
      description = "Siap 24 jam tambal ban tubeless dan bakar",
      district = "Gubeng",
      address = "Jl. Karang Menjangan No. 42",
      mapsUrl = "https://maps.app.goo.gl/sample123",
      phoneNumber = "081234567890",
      whatsappNumber = "081234567890",
      is24Hours = true,
      isTubeless = true,
      forMotor = true,
      forMobil = false,
      canCallOut = true,
    )

    TambalBanDraftManager.saveDraft(context, draft)
    val loaded = TambalBanDraftManager.loadDraft(context)
    assertNotNull(loaded)
    assertEquals("Tambal Ban Cacing Cak Munir", loaded?.name)
    assertEquals("Gubeng", loaded?.district)
    assertEquals(true, loaded?.is24Hours)
    assertEquals(true, loaded?.canCallOut)

    TambalBanDraftManager.clearDraft(context)
    assertNull(TambalBanDraftManager.loadDraft(context))
  }
}
