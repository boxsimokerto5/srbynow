package com.example

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.compose.ui.test.swipeUp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.height
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CulinaryReview
import com.example.data.model.CulinarySpot
import com.example.data.model.IncidentComment
import com.example.data.model.IncidentReport
import com.example.data.model.SurabayaEvent
import com.example.ui.sheets.CulinaryDetailSheetContent
import com.example.ui.sheets.EventDetailSheetContent
import com.example.ui.sheets.IncidentDetailSheetContent
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
}
