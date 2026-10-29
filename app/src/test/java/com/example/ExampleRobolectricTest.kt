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
import com.example.data.model.IncidentComment
import com.example.data.model.IncidentReport
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
}
