package com.example

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.core.app.ApplicationProvider
import com.example.data.audio.RadioPlaybackState
import com.example.data.audio.RadioStationInfo
import com.example.data.model.CulinaryReview
import com.example.data.model.CulinarySpot
import com.example.data.model.IncidentComment
import com.example.data.model.IncidentReport
import com.example.data.model.SurabayaEvent
import com.example.data.model.UiState
import com.example.data.model.UserAccount
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.sheets.CulinaryDetailSheetContent
import com.example.ui.sheets.EventDetailSheetContent
import com.example.ui.sheets.IncidentDetailSheetContent
import com.example.ui.sheets.RadioPlayerSheetContent
import com.example.ui.theme.SurabayaHariIniTheme
import org.junit.Assert.assertEquals
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
      description = "Foto kiriman ini telah dilaporkan 1 warga sehingga otomatis diburamkan.",
      category = "INFO_WARGA",
      district = "Tegalsari",
      address = "Jl. Basuki Rahmat",
      photoUrl = "https://173.249.59.183:8091/api/files/incidents/inc_blur_1/test.jpg",
      contentReportCount = 1,
      contentReportedByUids = listOf("other_user_1"),
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
}


