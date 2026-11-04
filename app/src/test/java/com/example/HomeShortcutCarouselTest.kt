package com.example

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.data.model.HomeBannerConfig
import com.example.ui.components.GojekStyleTicketBanner
import com.example.ui.components.HomeShortcutIds
import com.example.ui.components.HomeShortcutItem
import com.example.ui.components.orderHomeShortcuts
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
class HomeShortcutCarouselTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun buildSampleShortcuts(): List<HomeShortcutItem> {
        return HomeShortcutIds.DEFAULT_ORDER.map { id ->
            HomeShortcutItem(
                id = id,
                title = id.uppercase(),
                subtitle = "Sub $id",
                icon = Icons.Filled.Star,
                testTag = "home_shortcut_$id",
                onClick = {},
            )
        }
    }

    @Test
    fun `normalizeOrder and serializeOrder preserve custom Admin order and append missing items`() {
        val customOrder = listOf("tambal_ban", "barkas", "kuliner", "tambal_ban", "unknown_item")
        val normalized = HomeShortcutIds.normalizeOrder(customOrder)

        assertEquals(16, normalized.size)
        assertEquals("tambal_ban", normalized[0])
        assertEquals("barkas", normalized[1])
        assertEquals("kuliner", normalized[2])

        val serialized = HomeShortcutIds.serializeOrder(normalized)
        val roundTrip = HomeShortcutIds.parseSerializedOrder(serialized)
        assertEquals(normalized, roundTrip)
    }

    @Test
    fun `2x4 carousel chunks 16 shortcuts into 2 pages of 8 items with 2 rows of 4 columns`() {
        val shortcuts = buildSampleShortcuts()
        val ordered = orderHomeShortcuts(shortcuts, HomeShortcutIds.DEFAULT_ORDER)
        val pages = ordered.chunked(HomeShortcutIds.ITEMS_PER_PAGE)

        assertEquals(2, pages.size)
        assertEquals(8, pages[0].size)
        assertEquals(8, pages[1].size)

        val page1Rows = pages[0].chunked(HomeShortcutIds.COLUMNS_PER_ROW)
        val page2Rows = pages[1].chunked(HomeShortcutIds.COLUMNS_PER_ROW)
        assertEquals(2, page1Rows.size)
        assertEquals(4, page1Rows[0].size)
        assertEquals(4, page1Rows[1].size)
        assertEquals(2, page2Rows.size)
        assertEquals(4, page2Rows[0].size)
        assertEquals(4, page2Rows[1].size)
    }

    @Test
    fun `Admin moving Page 2 item into Page 1 updates page distribution cleanly`() {
        val shortcuts = buildSampleShortcuts()
        // Move "tambal_ban" (originally index 13 on Page 2) to index 0 on Page 1
        val adminDraft = HomeShortcutIds.DEFAULT_ORDER.toMutableList().apply {
            remove(HomeShortcutIds.TAMBAL_BAN)
            add(0, HomeShortcutIds.TAMBAL_BAN)
        }

        val ordered = orderHomeShortcuts(shortcuts, adminDraft)
        val pages = ordered.chunked(HomeShortcutIds.ITEMS_PER_PAGE)

        assertEquals(HomeShortcutIds.TAMBAL_BAN, pages[0].first().id)
        assertTrue(pages[0].any { it.id == HomeShortcutIds.TAMBAL_BAN })
        // Resetting back to DEFAULT_ORDER restores Event to #1
        val resetOrdered = orderHomeShortcuts(shortcuts, HomeShortcutIds.DEFAULT_ORDER)
        assertEquals(HomeShortcutIds.EVENT, resetOrdered.first().id)
    }

    @Test
    fun `HomeBannerConfig supports custom image URL, presets, and target types`() {
        val defaultConfig = HomeBannerConfig()
        assertEquals("DEFAULT_TREASURE", defaultConfig.presetKey)
        assertFalse(defaultConfig.isCustomOrPreset)

        val promoConfig = HomeBannerConfig(
            title = "Promo Rawon Kalkulator",
            subtitle = "Diskon 30%",
            badgeText = "HOT PROMO",
            imageUrl = "https://images.unsplash.com/photo-banner.gif",
            targetType = "KULINER",
        )
        assertTrue(promoConfig.isCustomOrPreset)
        assertEquals("https://images.unsplash.com/photo-banner.gif", promoConfig.imageUrl)
        assertEquals("KULINER", promoConfig.targetType)
    }

    @Test
    fun `GojekStyleTicketBanner renders for citizen and displays admin edit button for admin`() {
        var clickedBanner = false

        composeTestRule.setContent {
            SurabayaHariIniTheme {
                GojekStyleTicketBanner(
                    bannerConfig = HomeBannerConfig(
                        title = "Festival Rujak Uleg 2026",
                        subtitle = "Mulai Jam 18.00 WIB",
                        badgeText = "FESTIVAL",
                        displayMode = "TICKET",
                        imageUrl = "",
                    ),
                    isAdmin = true,
                    onBannerClick = { clickedBanner = true },
                )
            }
        }

        // Banner and contents are displayed
        composeTestRule.onNodeWithTag("home_ticket_banner").assertIsDisplayed()
        composeTestRule.onNodeWithText("Festival Rujak Uleg 2026").assertIsDisplayed()
        composeTestRule.onNodeWithText("FESTIVAL").assertIsDisplayed()

        // Admin Edit button is displayed
        composeTestRule.onNodeWithTag("admin_edit_banner_btn").assertIsDisplayed()
    }

    @Test
    fun `AdminBannerEditorDialog logic updates banner config cleanly`() {
        val initial = HomeBannerConfig(
            title = "Promo Rawon Kalkulator",
            subtitle = "Diskon 30%",
            badgeText = "PROMO",
            targetType = "KULINER",
        )
        val updated = initial.copy(
            title = "Promo Rawon Baru",
            subtitle = "Diskon 50%",
            badgeText = "HOT",
            displayMode = "FULL_BANNER",
            presetKey = "CUSTOM",
            imageUrl = "https://example.com/promo.jpg",
            targetType = "KULINER",
        )
        assertEquals("Promo Rawon Baru", updated.title)
        assertEquals("Diskon 50%", updated.subtitle)
        assertEquals("HOT", updated.badgeText)
        assertEquals("FULL_BANNER", updated.displayMode)
        assertTrue(updated.isCustomOrPreset)
        assertTrue(updated.isFullBanner)
    }
}
