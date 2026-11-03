package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.R
import com.example.ui.theme.AlertCrimson
import com.example.ui.theme.HeaderMintGradient
import com.example.ui.theme.MintSoftGradient
import com.example.ui.theme.OutlineVariantLight
import com.example.ui.theme.SuperAppBlue
import com.example.ui.theme.SuperAppBlueBg
import com.example.ui.theme.SuperAppBorder
import com.example.ui.theme.SuperAppCoral
import com.example.ui.theme.SuperAppCoralBg
import com.example.ui.theme.SuperAppGoldBg
import com.example.ui.theme.SuperAppGoldText
import com.example.ui.theme.SuperAppGreenContainer
import com.example.ui.theme.SuperAppGreenDeep
import com.example.ui.theme.SuperAppMutedText
import com.example.ui.theme.SuperAppPurple
import com.example.ui.theme.SuperAppPurpleBg
import com.example.ui.theme.ToskaOnPrimaryContainer
import com.example.ui.theme.ToskaPrimary
import com.example.ui.theme.ToskaPrimaryDeep
import kotlin.math.roundToInt
import kotlinx.coroutines.launch

object HomeShortcutIds {
    const val KULINER = "kuliner"
    const val POLLING = "polling"
    const val MOMEN = "momen"
    const val LENSA_WARGA = "lensa_warga"
    const val RADIO_SS = "radio_ss"
    const val KEJADIAN = "kejadian"
    const val DESTINASI = "destinasi"
    const val EVENT = "event"
    const val SEKITAR = "sekitar"
    const val CHAT = "chat"
    const val JASA = "jasa"
    const val BARKAS = "barkas"
    const val KOS = "kos"
    const val TAMBAL_BAN = "tambal_ban"
    const val TRANSIT = "transit"
    const val BOOKMARKS = "bookmarks"

    const val ITEMS_PER_PAGE = 8
    const val COLUMNS_PER_ROW = 4

    val DEFAULT_ORDER: List<String> = listOf(
        EVENT,
        SEKITAR,
        KULINER,
        POLLING,
        MOMEN,
        KEJADIAN,
        LENSA_WARGA,
        DESTINASI,
        BARKAS,
        KOS,
        TAMBAL_BAN,
        TRANSIT,
        RADIO_SS,
        CHAT,
        JASA,
        BOOKMARKS,
    )

    fun normalizeOrder(
        rawIds: List<String>,
        availableIds: List<String> = DEFAULT_ORDER,
    ): List<String> {
        val validSet = availableIds.toSet()
        val seen = LinkedHashSet<String>()
        for (raw in rawIds) {
            val clean = raw.trim().lowercase()
            if (clean in validSet) {
                seen.add(clean)
            }
        }
        for (fallback in availableIds) {
            seen.add(fallback)
        }
        return seen.toList()
    }

    fun parseSerializedOrder(raw: String): List<String> {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return DEFAULT_ORDER
        val body = trimmed.removePrefix("[").removeSuffix("]")
        val parsed = body.split(",")
            .map { token -> token.trim().removeSurrounding("\"").removeSurrounding("'") }
            .filter { it.isNotEmpty() }
        return normalizeOrder(parsed)
    }

    fun serializeOrder(ids: List<String>): String {
        val normalized = normalizeOrder(ids)
        return normalized.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")
    }
}

data class HomeShortcutItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badge: String? = null,
    val testTag: String,
    val drawableRes: Int? = null,
    val onClick: () -> Unit,
)

fun orderHomeShortcuts(
    allShortcuts: List<HomeShortcutItem>,
    orderedIds: List<String>,
): List<HomeShortcutItem> {
    val byId = allShortcuts.associateBy { it.id }
    val normalizedIds = HomeShortcutIds.normalizeOrder(
        rawIds = orderedIds,
        availableIds = allShortcuts.map { it.id },
    )
    return normalizedIds.mapNotNull { byId[it] }
}

@Composable
fun HomeShortcutGridSection(
    shortcuts: List<HomeShortcutItem>,
    shortcutOrderIds: List<String>,
    isAdmin: Boolean,
    onSaveShortcutOrder: (List<String>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var showAdminReorderSheet by remember { mutableStateOf(false) }

    val orderedShortcuts = remember(shortcuts, shortcutOrderIds) {
        orderHomeShortcuts(shortcuts, shortcutOrderIds)
    }
    val pages = remember(orderedShortcuts) {
        orderedShortcuts.chunked(HomeShortcutIds.ITEMS_PER_PAGE)
    }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size.coerceAtLeast(1) },
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        // Section Header + Optional Admin "Atur Urutan" Control (Gojek Style "Pilihan buat kamu")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = stringResource(R.string.home_shortcuts_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(2.dp)
                ) {
                    val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    drawLine(
                        color = Color(0xFFD1D5DB),
                        start = Offset(0f, size.height / 2),
                        end = Offset(size.width, size.height / 2),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = pathEffect,
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SuperAppGreenContainer,
                    border = BorderStroke(1.dp, SuperAppGreenDeep.copy(alpha = 0.25f)),
                ) {
                    Text(
                        text = "${orderedShortcuts.size} Layanan",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                        fontWeight = FontWeight.Bold,
                        color = SuperAppGreenDeep,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }

                if (isAdmin) {
                    Surface(
                        onClick = { showAdminReorderSheet = true },
                        shape = RoundedCornerShape(12.dp),
                        color = ToskaPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.35f)),
                        modifier = Modifier
                            .testTag("home_btn_admin_reorder_shortcuts")
                            .minimumInteractiveComponentSize(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Tune,
                                contentDescription = stringResource(R.string.home_shortcuts_admin_arrange),
                                tint = ToskaPrimaryDeep,
                                modifier = Modifier.size(13.dp),
                            )
                            Text(
                                text = stringResource(R.string.home_shortcuts_admin_arrange),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.5.sp),
                                fontWeight = FontWeight.Bold,
                                color = ToskaPrimaryDeep,
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2x4 Horizontal Swipeable Pager (8 shortcuts per slide = 2 rows x 4 columns)
        HorizontalPager(
            state = pagerState,
            pageSpacing = 10.dp,
            beyondViewportPageCount = pages.size,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_shortcuts_pager"),
        ) { pageIndex ->
            val pageItems = pages.getOrNull(pageIndex).orEmpty()
            val rows = pageItems.chunked(HomeShortcutIds.COLUMNS_PER_ROW)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_shortcuts_page_$pageIndex"),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                for (rowItems in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        for (item in rowItems) {
                            HomeShortcutButton(
                                item = item,
                                onAdminLongPress = if (isAdmin) {
                                    {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showAdminReorderSheet = true
                                    }
                                } else {
                                    null
                                },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        val emptySlots = HomeShortcutIds.COLUMNS_PER_ROW - rowItems.size
                        repeat(emptySlots) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Compact Page Indicator Dots + Swipe Hint
        if (pages.size > 1) {
            Spacer(modifier = Modifier.height(7.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Spacer(modifier = Modifier.width(64.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.testTag("home_shortcuts_page_indicators"),
                ) {
                    pages.indices.forEach { idx ->
                        val isSelected = pagerState.currentPage == idx
                        val pillWidth by animateDpAsState(
                            targetValue = if (isSelected) 20.dp else 7.dp,
                            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            label = "shortcut_dot_width_$idx",
                        )
                        val dotColor by animateColorAsState(
                            targetValue = if (isSelected) ToskaPrimaryDeep else ToskaPrimary.copy(alpha = 0.25f),
                            label = "shortcut_dot_color_$idx",
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    scope.launch { pagerState.animateScrollToPage(idx) }
                                }
                                .padding(vertical = 4.dp, horizontal = 2.dp)
                                .testTag("home_shortcuts_page_dot_$idx"),
                            contentAlignment = Alignment.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(pillWidth)
                                    .height(6.dp)
                                    .clip(CircleShape)
                                    .background(dotColor),
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(
                        R.string.home_shortcuts_swipe_hint,
                        pagerState.currentPage + 1,
                        pages.size,
                    ),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(86.dp),
                )
            }
        }

        // Gojek-Style Hanging Ticket Banner ("Besok klaim hadiah yang lebih...")
        GojekStyleTicketBanner(
            title = "Besok klaim hadiah poin warga yang lebih...",
            badgeText = "1/7 Hari",
            subText = "Berakhir dalam 6 Hari",
            modifier = Modifier.padding(horizontal = 2.dp),
        )
    }

    if (isAdmin && showAdminReorderSheet) {
        AdminShortcutLayoutSheet(
            allShortcuts = shortcuts,
            initialOrderIds = orderedShortcuts.map { it.id },
            onDismiss = { showAdminReorderSheet = false },
            onSaveOrder = { updatedIds ->
                onSaveShortcutOrder(updatedIds)
                showAdminReorderSheet = false
            },
        )
    }
}

private data class ShortcutThemeData(
    val tagText: String,
    val displayTitle: String,
    val tagColor: Color,
    val gradient: Brush,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeShortcutButton(
    item: HomeShortcutItem,
    onAdminLongPress: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val theme = when (item.id) {
        HomeShortcutIds.KULINER -> ShortcutThemeData(
            tagText = "Sentra",
            displayTitle = "Kuliner SBY",
            tagColor = SuperAppCoral,
            gradient = Brush.verticalGradient(listOf(Color(0xFFFF6B6B), Color(0xFFEE2737))),
        )
        HomeShortcutIds.POLLING -> ShortcutThemeData(
            tagText = "Suara",
            displayTitle = "Polling SBY",
            tagColor = SuperAppPurple,
            gradient = Brush.verticalGradient(listOf(Color(0xFF9333EA), Color(0xFF6B21A8))),
        )
        HomeShortcutIds.MOMEN -> ShortcutThemeData(
            tagText = "Cerita",
            displayTitle = "Momen Arek",
            tagColor = SuperAppGreenDeep,
            gradient = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF047857))),
        )
        HomeShortcutIds.LENSA_WARGA -> ShortcutThemeData(
            tagText = "Visual",
            displayTitle = "Lensa SBY",
            tagColor = Color(0xFF0E7490),
            gradient = Brush.verticalGradient(listOf(Color(0xFF06B6D4), Color(0xFF0E7490))),
        )
        HomeShortcutIds.RADIO_SS -> ShortcutThemeData(
            tagText = "Live",
            displayTitle = "Radio SS",
            tagColor = SuperAppGoldText,
            gradient = Brush.verticalGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706))),
        )
        HomeShortcutIds.KEJADIAN -> ShortcutThemeData(
            tagText = "Siaga",
            displayTitle = "Lapor 112",
            tagColor = AlertCrimson,
            gradient = Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C))),
        )
        HomeShortcutIds.DESTINASI -> ShortcutThemeData(
            tagText = "Ikonik",
            displayTitle = "Destinasi",
            tagColor = SuperAppBlue,
            gradient = Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
        )
        HomeShortcutIds.EVENT -> ShortcutThemeData(
            tagText = "Agenda",
            displayTitle = "Event Kota",
            tagColor = Color(0xFFBE185D),
            gradient = Brush.verticalGradient(listOf(Color(0xFFEC4899), Color(0xFFBE185D))),
        )
        HomeShortcutIds.SEKITAR -> ShortcutThemeData(
            tagText = "Arek",
            displayTitle = "Sekitar",
            tagColor = SuperAppGreenDeep,
            gradient = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
        )
        HomeShortcutIds.CHAT -> ShortcutThemeData(
            tagText = "Kongkow",
            displayTitle = "Ruang Chat",
            tagColor = SuperAppBlue,
            gradient = Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1))),
        )
        HomeShortcutIds.JASA -> ShortcutThemeData(
            tagText = "Layanan",
            displayTitle = "Jasa Warga",
            tagColor = Color(0xFF1E40AF),
            gradient = Brush.verticalGradient(listOf(Color(0xFF2563EB), Color(0xFF1E40AF))),
        )
        HomeShortcutIds.BARKAS -> ShortcutThemeData(
            tagText = "Jual Beli",
            displayTitle = "Barkas SBY",
            tagColor = Color(0xFF15803D),
            gradient = Brush.verticalGradient(listOf(Color(0xFF16A34A), Color(0xFF15803D))),
        )
        HomeShortcutIds.KOS -> ShortcutThemeData(
            tagText = "Hunian",
            displayTitle = "Info Kos",
            tagColor = Color(0xFF115E59),
            gradient = Brush.verticalGradient(listOf(Color(0xFF0D9488), Color(0xFF115E59))),
        )
        HomeShortcutIds.TAMBAL_BAN -> ShortcutThemeData(
            tagText = "Darurat",
            displayTitle = "Tambal Ban",
            tagColor = Color(0xFFB45309),
            gradient = Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFB45309))),
        )
        HomeShortcutIds.TRANSIT -> ShortcutThemeData(
            tagText = "Rute Bus",
            displayTitle = "WiraWiri",
            tagColor = Color(0xFF9F1239),
            gradient = Brush.verticalGradient(listOf(Color(0xFFE11D48), Color(0xFF9F1239))),
        )
        HomeShortcutIds.BOOKMARKS -> ShortcutThemeData(
            tagText = "Favorit",
            displayTitle = "Tersimpan",
            tagColor = Color(0xFFA16207),
            gradient = Brush.verticalGradient(listOf(Color(0xFFEAB308), Color(0xFFA16207))),
        )
        else -> ShortcutThemeData(
            tagText = "Layanan",
            displayTitle = item.title,
            tagColor = Color(0xFF374151),
            gradient = Brush.verticalGradient(listOf(Color(0xFF6B7280), Color(0xFF374151))),
        )
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFE5E7EB)),
        shadowElevation = 0.8.dp,
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(18.dp))
            .combinedClickable(
                onClick = item.onClick,
                onLongClick = onAdminLongPress,
            )
            .testTag(item.testTag)
            .minimumInteractiveComponentSize(),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 4.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Top Text: Subtitle / Tag and Title
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = theme.tagText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = theme.tagColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = theme.displayTitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF111827),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Game Illustration or Vibrant Badge
                if (item.drawableRes != null) {
                    Image(
                        painter = painterResource(id = item.drawableRes),
                        contentDescription = item.title,
                        modifier = Modifier
                            .size(48.dp)
                            .padding(bottom = 2.dp),
                        contentScale = ContentScale.Fit,
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(theme.gradient)
                            .padding(1.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.White.copy(alpha = 0.28f), Color.Transparent),
                                    ),
                                ),
                        )
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }

            if (item.badge != null) {
                Surface(
                    shape = RoundedCornerShape(bottomStart = 8.dp, topEnd = 18.dp),
                    color = Color(0xFFA855F7),
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Text(
                        text = item.badge,
                        color = Color.White,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun GojekStyleTicketBanner(
    title: String = "Besok klaim hadiah poin warga yang lebih...",
    badgeText: String = "1/7 Hari",
    subText: String = "Berakhir dalam 6 Hari",
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var showDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 2.dp),
    ) {
        Surface(
            onClick = {
                showDialog = true
                onClick()
            },
            shape = RoundedCornerShape(18.dp),
            color = Color.Transparent,
            border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
            shadowElevation = 0.5.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 5.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFFDDE5),
                            Color(0xFFF5E8FF),
                            Color(0xFFEDE9FE),
                        ),
                    ),
                )
                .testTag("home_ticket_banner"),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                // 3D RPG Treasure Chest
                Image(
                    painter = painterResource(id = R.drawable.img_game_treasure_chest),
                    contentDescription = "Peti Hadiah Warga",
                    modifier = Modifier.size(46.dp),
                    contentScale = ContentScale.Fit,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2937),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Filled.Info,
                            contentDescription = "Info Hadiah",
                            tint = Color(0xFF9333EA),
                            modifier = Modifier.size(16.dp),
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFFEF3C7),
                                border = BorderStroke(1.dp, Color(0xFFFBBF24)),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CalendarMonth,
                                        contentDescription = null,
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(12.dp),
                                    )
                                    Text(
                                        text = badgeText,
                                        color = Color(0xFF92400E),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            Text(
                                text = subText,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color(0xFF4B5563),
                                fontWeight = FontWeight.Medium,
                            )
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color(0xFF6B7280),
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }

        // Dual Metallic Hanging Eyelets (Slit Rings like Gojek Ticket)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            HangingEyelet()
            HangingEyelet()
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Siap Rek!")
                }
            },
            title = {
                Text(
                    text = "Misi Warga Arek Suroboyo",
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Text(
                    "Terus pantau kota, dengarkan Radio SS 100 FM, gunakan layanan warga, dan lapor insiden di sekitarmu untuk mengumpulkan Poin Warga Aktif & klaim lencana warga teladan!",
                )
            },
        )
    }
}

@Composable
private fun HangingEyelet() {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFFD1D5DB),
        border = BorderStroke(1.dp, Color(0xFF9CA3AF)),
        modifier = Modifier
            .width(9.dp)
            .height(13.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(7.dp)
                    .background(Color(0xFF6B7280), RoundedCornerShape(2.dp)),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminShortcutLayoutSheet(
    allShortcuts: List<HomeShortcutItem>,
    initialOrderIds: List<String>,
    onDismiss: () -> Unit,
    onSaveOrder: (List<String>) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current

    val shortcutsById = remember(allShortcuts) { allShortcuts.associateBy { it.id } }
    val availableIds = remember(allShortcuts) { allShortcuts.map { it.id } }

    var draftIds by remember(initialOrderIds) {
        mutableStateOf(HomeShortcutIds.normalizeOrder(initialOrderIds, availableIds))
    }
    var selectedItemId by remember { mutableStateOf<String?>(null) }

    // Drag-and-drop state across all 16 slots (spanning Halaman 1 & Halaman 2 seamlessly)
    val slotBounds = remember { mutableStateMapOf<Int, Rect>() }
    var arenaRootOrigin by remember { mutableStateOf(Offset.Zero) }
    var draggingItemId by remember { mutableStateOf<String?>(null) }
    var dragFingerRoot by remember { mutableStateOf(Offset.Zero) }
    var dragTouchOffsetInSlot by remember { mutableStateOf(Offset.Zero) }
    var draggedCardSizePx by remember { mutableStateOf(Offset(180f, 150f)) }

    val currentDraftIds by rememberUpdatedState(draftIds)
    val currentArenaOrigin by rememberUpdatedState(arenaRootOrigin)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = null,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .testTag("admin_shortcut_reorder_sheet"),
        ) {
            // Sheet Header
            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HeaderMintGradient)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.admin_shortcut_sheet_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = ToskaOnPrimaryContainer,
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = ToskaPrimaryDeep,
                            ) {
                                Text(
                                    text = "ADMIN",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.admin_shortcut_sheet_subtitle),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .minimumInteractiveComponentSize()
                            .testTag("admin_shortcut_close_btn"),
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Tutup",
                            tint = ToskaOnPrimaryContainer,
                        )
                    }
                }
            }

            // Interactive Drag & Drop + Tap-to-Swap Arena
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            arenaRootOrigin = coords.positionInRoot()
                        }
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = { startOffset ->
                                    val rootPoint = currentArenaOrigin + startOffset
                                    val hitEntry = slotBounds.entries.firstOrNull { (_, rect) ->
                                        rect.contains(rootPoint)
                                    } ?: slotBounds.entries.minByOrNull { (_, rect) ->
                                        val dx = rect.center.x - rootPoint.x
                                        val dy = rect.center.y - rootPoint.y
                                        dx * dx + dy * dy
                                    }?.takeIf { (_, rect) ->
                                        val dx = rect.center.x - rootPoint.x
                                        val dy = rect.center.y - rootPoint.y
                                        (dx * dx + dy * dy) <= 14000f
                                    }

                                    val hitIdx = hitEntry?.key
                                    val hitRect = hitEntry?.value
                                    val itemId = hitIdx?.let { currentDraftIds.getOrNull(it) }
                                    if (itemId != null && hitRect != null) {
                                        draggingItemId = itemId
                                        selectedItemId = null
                                        dragFingerRoot = rootPoint
                                        dragTouchOffsetInSlot = rootPoint - hitRect.topLeft
                                        draggedCardSizePx = Offset(hitRect.width, hitRect.height)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    val activeId = draggingItemId ?: return@detectDragGestures
                                    change.consume()
                                    dragFingerRoot += dragAmount

                                    val targetSlot = slotBounds.entries.firstOrNull { (_, rect) ->
                                        rect.contains(dragFingerRoot)
                                    }?.key

                                    if (targetSlot != null) {
                                        val fromIdx = currentDraftIds.indexOf(activeId)
                                        if (fromIdx != -1 && fromIdx != targetSlot && targetSlot in currentDraftIds.indices) {
                                            val updated = currentDraftIds.toMutableList()
                                            val moved = updated.removeAt(fromIdx)
                                            updated.add(targetSlot, moved)
                                            draftIds = updated
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        }
                                    }
                                },
                                onDragEnd = {
                                    draggingItemId = null
                                },
                                onDragCancel = {
                                    draggingItemId = null
                                },
                            )
                        }
                        .testTag("admin_shortcut_drag_arena"),
                ) {
                    val pageChunks = draftIds.chunked(HomeShortcutIds.ITEMS_PER_PAGE)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        pageChunks.forEachIndexed { pageIndex, pageIds ->
                            val isPrimaryPage = pageIndex == 0
                            val pageLabel = when (pageIndex) {
                                0 -> stringResource(R.string.admin_shortcut_page1_label)
                                1 -> stringResource(R.string.admin_shortcut_page2_label)
                                else -> stringResource(R.string.admin_shortcut_page_extra_label, pageIndex + 1)
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isPrimaryPage) {
                                    ToskaPrimary.copy(alpha = 0.06f)
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                },
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isPrimaryPage) {
                                        ToskaPrimary.copy(alpha = 0.38f)
                                    } else {
                                        OutlineVariantLight.copy(alpha = 0.7f)
                                    },
                                ),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.DragIndicator,
                                                contentDescription = null,
                                                tint = if (isPrimaryPage) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(15.dp),
                                            )
                                            Text(
                                                text = pageLabel,
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isPrimaryPage) ToskaPrimaryDeep else MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        Text(
                                            text = "Slot #${pageIndex * HomeShortcutIds.ITEMS_PER_PAGE + 1}–#${pageIndex * HomeShortcutIds.ITEMS_PER_PAGE + pageIds.size}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }

                                    val rows = pageIds.chunked(HomeShortcutIds.COLUMNS_PER_ROW)
                                    rows.forEachIndexed { rowIdx, rowIds ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        ) {
                                            rowIds.forEachIndexed { colIdx, itemId ->
                                                val globalSlotIndex =
                                                    pageIndex * HomeShortcutIds.ITEMS_PER_PAGE +
                                                        rowIdx * HomeShortcutIds.COLUMNS_PER_ROW +
                                                        colIdx
                                                val shortcut = shortcutsById[itemId]
                                                if (shortcut != null) {
                                                    val isBeingDragged = draggingItemId == itemId
                                                    val isSelectedForSwap = selectedItemId == itemId
                                                    AdminEditableShortcutTile(
                                                        item = shortcut,
                                                        slotNumber = globalSlotIndex + 1,
                                                        isBeingDragged = isBeingDragged,
                                                        isSelectedForSwap = isSelectedForSwap,
                                                        onClick = {
                                                            val prevSelected = selectedItemId
                                                            if (prevSelected == null) {
                                                                selectedItemId = itemId
                                                            } else if (prevSelected == itemId) {
                                                                selectedItemId = null
                                                            } else {
                                                                val idxA = draftIds.indexOf(prevSelected)
                                                                val idxB = draftIds.indexOf(itemId)
                                                                if (idxA != -1 && idxB != -1) {
                                                                    val swapped = draftIds.toMutableList()
                                                                    val tmp = swapped[idxA]
                                                                    swapped[idxA] = swapped[idxB]
                                                                    swapped[idxB] = tmp
                                                                    draftIds = swapped
                                                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                                }
                                                                selectedItemId = null
                                                            }
                                                        },
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .onGloballyPositioned { coords ->
                                                                slotBounds[globalSlotIndex] = coords.boundsInRoot()
                                                            }
                                                            .testTag("admin_shortcut_slot_${shortcut.id}"),
                                                    )
                                                }
                                            }
                                            val emptyCols = HomeShortcutIds.COLUMNS_PER_ROW - rowIds.size
                                            repeat(emptyCols) {
                                                Spacer(modifier = Modifier.weight(1f))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Floating Dragged Tile Preview under Admin's Finger
                    val activeDragShortcut = draggingItemId?.let { shortcutsById[it] }
                    if (activeDragShortcut != null) {
                        val localTopLeft = dragFingerRoot - arenaRootOrigin - dragTouchOffsetInSlot
                        val widthDp = with(density) { draggedCardSizePx.x.toDp() }
                        val activeSlotNum = draftIds.indexOf(activeDragShortcut.id) + 1
                        Box(
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = localTopLeft.x.roundToInt(),
                                        y = localTopLeft.y.roundToInt(),
                                    )
                                }
                                .width(widthDp)
                                .zIndex(10f)
                                .graphicsLayer {
                                    scaleX = 1.07f
                                    scaleY = 1.07f
                                },
                        ) {
                            AdminEditableShortcutTile(
                                item = activeDragShortcut,
                                slotNumber = activeSlotNum.coerceAtLeast(1),
                                isBeingDragged = false,
                                isSelectedForSwap = true,
                                isFloatingPreview = true,
                                onClick = {},
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }

                // Precision Helper Bar when an icon is tapped/selected
                val selectedShortcut = selectedItemId?.let { shortcutsById[it] }
                if (selectedShortcut != null) {
                    val selectedIndex = draftIds.indexOf(selectedShortcut.id)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ToskaPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_shortcut_selection_bar"),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = stringResource(R.string.admin_shortcut_selected_hint, selectedShortcut.title),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = ToskaOnPrimaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                if (selectedIndex > 0) {
                                    Surface(
                                        onClick = {
                                            val updated = draftIds.toMutableList()
                                            val moved = updated.removeAt(selectedIndex)
                                            updated.add(0, moved)
                                            draftIds = updated
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, ToskaPrimary.copy(alpha = 0.45f)),
                                        modifier = Modifier.testTag("admin_shortcut_move_first_btn"),
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.VerticalAlignTop,
                                                contentDescription = null,
                                                tint = ToskaPrimaryDeep,
                                                modifier = Modifier.size(13.dp),
                                            )
                                            Text(
                                                text = stringResource(R.string.admin_shortcut_move_first),
                                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                fontWeight = FontWeight.Bold,
                                                color = ToskaPrimaryDeep,
                                            )
                                        }
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        if (selectedIndex > 0) {
                                            val updated = draftIds.toMutableList()
                                            val moved = updated.removeAt(selectedIndex)
                                            updated.add(selectedIndex - 1, moved)
                                            draftIds = updated
                                        }
                                    },
                                    enabled = selectedIndex > 0,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("admin_shortcut_move_left_btn"),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "Geser Kiri",
                                        tint = ToskaPrimaryDeep,
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        if (selectedIndex in 0 until draftIds.lastIndex) {
                                            val updated = draftIds.toMutableList()
                                            val moved = updated.removeAt(selectedIndex)
                                            updated.add(selectedIndex + 1, moved)
                                            draftIds = updated
                                        }
                                    },
                                    enabled = selectedIndex in 0 until draftIds.lastIndex,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("admin_shortcut_move_right_btn"),
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "Geser Kanan",
                                        tint = ToskaPrimaryDeep,
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Action Bar: Reset Default + Simpan Tata Letak
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(
                    onClick = {
                        draftIds = HomeShortcutIds.normalizeOrder(HomeShortcutIds.DEFAULT_ORDER, availableIds)
                        selectedItemId = null
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, ToskaPrimaryDeep.copy(alpha = 0.45f)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier
                        .testTag("admin_shortcut_reset_btn")
                        .minimumInteractiveComponentSize(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.RestartAlt,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.admin_shortcut_reset_btn),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = ToskaPrimaryDeep,
                    )
                }

                Button(
                    onClick = { onSaveOrder(draftIds) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ToskaPrimaryDeep,
                        contentColor = Color.White,
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_shortcut_save_btn")
                        .minimumInteractiveComponentSize(),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        modifier = Modifier.size(17.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.admin_shortcut_save_btn),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminEditableShortcutTile(
    item: HomeShortcutItem,
    slotNumber: Int,
    isBeingDragged: Boolean,
    isSelectedForSwap: Boolean,
    isFloatingPreview: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = when {
            isFloatingPreview -> Color.White
            isSelectedForSwap -> ToskaPrimary.copy(alpha = 0.14f)
            else -> MaterialTheme.colorScheme.surface
        },
        border = BorderStroke(
            width = if (isSelectedForSwap || isFloatingPreview) 1.8.dp else 1.dp,
            color = if (isSelectedForSwap || isFloatingPreview) {
                ToskaPrimaryDeep
            } else {
                OutlineVariantLight.copy(alpha = 0.75f)
            },
        ),
        shadowElevation = if (isFloatingPreview) 10.dp else 1.dp,
        modifier = modifier
            .alpha(if (isBeingDragged) 0.28f else 1f)
            .then(
                if (isBeingDragged) {
                    Modifier.border(
                        width = 1.dp,
                        color = ToskaPrimaryDeep.copy(alpha = 0.45f),
                        shape = RoundedCornerShape(12.dp),
                    )
                } else {
                    Modifier
                },
            ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 6.dp, start = 3.dp, end = 3.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(9.dp))
                        .background(MintSoftGradient),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = ToskaPrimaryDeep,
                        modifier = Modifier.size(17.dp),
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            // Slot Index Badge (#1..#16)
            Surface(
                shape = RoundedCornerShape(topStart = 11.dp, bottomEnd = 6.dp),
                color = if (slotNumber <= HomeShortcutIds.ITEMS_PER_PAGE) {
                    ToskaPrimaryDeep
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                },
                modifier = Modifier.align(Alignment.TopStart),
            ) {
                Text(
                    text = "#$slotNumber",
                    color = Color.White,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                )
            }
        }
    }
}
