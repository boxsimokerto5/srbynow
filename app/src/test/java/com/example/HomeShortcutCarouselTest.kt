package com.example

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import com.example.ui.components.HomeShortcutIds
import com.example.ui.components.HomeShortcutItem
import com.example.ui.components.orderHomeShortcuts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeShortcutCarouselTest {

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
        // Resetting back to DEFAULT_ORDER restores Kuliner to #1
        val resetOrdered = orderHomeShortcuts(shortcuts, HomeShortcutIds.DEFAULT_ORDER)
        assertEquals(HomeShortcutIds.KULINER, resetOrdered.first().id)
    }
}
