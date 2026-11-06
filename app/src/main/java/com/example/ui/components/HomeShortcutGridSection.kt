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
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import com.example.data.model.HomeBannerItem
import kotlinx.coroutines.delay
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.example.data.model.HomeBannerConfig
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.input.pointer.PointerEventPass
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
import com.example.ui.theme.SuperAppGold
import com.example.ui.theme.SuperAppGoldBg
import com.example.ui.theme.SuperAppGoldBorder
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
        RADIO_SS,
        DESTINASI,
        LENSA_WARGA,
        BARKAS,
        KOS,
        TAMBAL_BAN,
        TRANSIT,
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
    bannerConfig: HomeBannerConfig = HomeBannerConfig(),
    onSaveBannerConfig: (HomeBannerConfig) -> Unit = {},
    onBannerClick: (HomeBannerConfig) -> Unit = {},
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
        // Optional Admin "Atur Urutan" Control (title 'Pilihan buat kamu' removed as requested)
        if (isAdmin) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
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

        // Gojek-Style Hanging Ticket Banner / Admin Customizable Promo Banner
        GojekStyleTicketBanner(
            bannerConfig = bannerConfig,
            isAdmin = isAdmin,
            onSaveBannerConfig = onSaveBannerConfig,
            onBannerClick = onBannerClick,
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
    val cardBackground: Color,
    val cardBorder: Color,
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun HomeShortcutButton(
    item: HomeShortcutItem,
    onAdminLongPress: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    val theme = when (item.id) {
        HomeShortcutIds.EVENT -> ShortcutThemeData(
            tagText = "Agenda",
            displayTitle = "Event Kota",
            tagColor = Color(0xFFBE185D),
            gradient = Brush.verticalGradient(listOf(Color(0xFFEC4899), Color(0xFFBE185D))),
            cardBackground = Color(0xFFFFF1F2), // Soft Rose Pastel
            cardBorder = Color(0xFFFECDD3),
        )
        HomeShortcutIds.SEKITAR -> ShortcutThemeData(
            tagText = "Arek",
            displayTitle = "Sekitar",
            tagColor = SuperAppGreenDeep,
            gradient = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
            cardBackground = Color(0xFFECFDF5), // Soft Mint Pastel
            cardBorder = Color(0xFFA7F3D0),
        )
        HomeShortcutIds.KULINER -> ShortcutThemeData(
            tagText = "Sentra",
            displayTitle = "Kuliner SBY",
            tagColor = SuperAppCoral,
            gradient = Brush.verticalGradient(listOf(Color(0xFFFF6B6B), Color(0xFFEE2737))),
            cardBackground = Color(0xFFFFF7ED), // Soft Peach/Orange Pastel
            cardBorder = Color(0xFFFFEDD5),
        )
        HomeShortcutIds.POLLING -> ShortcutThemeData(
            tagText = "Suara",
            displayTitle = "Polling SBY",
            tagColor = SuperAppPurple,
            gradient = Brush.verticalGradient(listOf(Color(0xFF9333EA), Color(0xFF6B21A8))),
            cardBackground = Color(0xFFFAF5FF), // Soft Lilac/Purple Pastel
            cardBorder = Color(0xFFE9D5FF),
        )
        HomeShortcutIds.MOMEN -> ShortcutThemeData(
            tagText = "Cerita",
            displayTitle = "Momen Arek",
            tagColor = SuperAppGreenDeep,
            gradient = Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF047857))),
            cardBackground = Color(0xFFF0FDF4), // Soft Sage Pastel
            cardBorder = Color(0xFFBBF7D0),
        )
        HomeShortcutIds.KEJADIAN -> ShortcutThemeData(
            tagText = "Siaga",
            displayTitle = "Lapor 112",
            tagColor = AlertCrimson,
            gradient = Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFFB91C1C))),
            cardBackground = Color(0xFFFEF2F2), // Soft Coral/Red Pastel
            cardBorder = Color(0xFFFECACA),
        )
        HomeShortcutIds.RADIO_SS -> ShortcutThemeData(
            tagText = "Live",
            displayTitle = "Radio SS",
            tagColor = SuperAppGoldText,
            gradient = Brush.verticalGradient(listOf(Color(0xFFFBBF24), Color(0xFFD97706))),
            cardBackground = Color(0xFFFFFBEB), // Soft Warm Sun Pastel
            cardBorder = Color(0xFFFDE68A),
        )
        HomeShortcutIds.DESTINASI -> ShortcutThemeData(
            tagText = "Ikonik",
            displayTitle = "Destinasi",
            tagColor = SuperAppBlue,
            gradient = Brush.verticalGradient(listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
            cardBackground = Color(0xFFEFF6FF), // Soft Sky Blue Pastel
            cardBorder = Color(0xFFBFDBFE),
        )
        HomeShortcutIds.LENSA_WARGA -> ShortcutThemeData(
            tagText = "Visual",
            displayTitle = "Lensa SBY",
            tagColor = Color(0xFF0E7490),
            gradient = Brush.verticalGradient(listOf(Color(0xFF06B6D4), Color(0xFF0E7490))),
            cardBackground = Color(0xFFECFEFF), // Soft Cyan/Aqua Pastel
            cardBorder = Color(0xFFA5F3FC),
        )
        HomeShortcutIds.BARKAS -> ShortcutThemeData(
            tagText = "Jual Beli",
            displayTitle = "Barkas SBY",
            tagColor = Color(0xFF15803D),
            gradient = Brush.verticalGradient(listOf(Color(0xFF16A34A), Color(0xFF15803D))),
            cardBackground = Color(0xFFF7FEE7), // Soft Lime/Fresh Herb Pastel
            cardBorder = Color(0xFFD9F99D),
        )
        HomeShortcutIds.KOS -> ShortcutThemeData(
            tagText = "Hunian",
            displayTitle = "Info Kos",
            tagColor = Color(0xFF115E59),
            gradient = Brush.verticalGradient(listOf(Color(0xFF0D9488), Color(0xFF115E59))),
            cardBackground = Color(0xFFF0FDFA), // Soft Teal Pastel
            cardBorder = Color(0xFF99F6E4),
        )
        HomeShortcutIds.TAMBAL_BAN -> ShortcutThemeData(
            tagText = "Darurat",
            displayTitle = "Tambal Ban",
            tagColor = Color(0xFFB45309),
            gradient = Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFB45309))),
            cardBackground = Color(0xFFFEFCE8), // Soft Canary Yellow Pastel
            cardBorder = Color(0xFFFEF08A),
        )
        HomeShortcutIds.TRANSIT -> ShortcutThemeData(
            tagText = "Rute Bus",
            displayTitle = "WiraWiri",
            tagColor = Color(0xFF9F1239),
            gradient = Brush.verticalGradient(listOf(Color(0xFFE11D48), Color(0xFF9F1239))),
            cardBackground = Color(0xFFFFF1F2), // Soft Crimson Pastel
            cardBorder = Color(0xFFFFE4E6),
        )
        HomeShortcutIds.CHAT -> ShortcutThemeData(
            tagText = "Kongkow",
            displayTitle = "Ruang Chat",
            tagColor = SuperAppBlue,
            gradient = Brush.verticalGradient(listOf(Color(0xFF0284C7), Color(0xFF0369A1))),
            cardBackground = Color(0xFFF0F9FF), // Soft Ocean/Periwinkle Pastel
            cardBorder = Color(0xFFBAE6FD),
        )
        HomeShortcutIds.JASA -> ShortcutThemeData(
            tagText = "Layanan",
            displayTitle = "Jasa Warga",
            tagColor = Color(0xFF1E40AF),
            gradient = Brush.verticalGradient(listOf(Color(0xFF2563EB), Color(0xFF1E40AF))),
            cardBackground = Color(0xFFEEF2FF), // Soft Indigo/Blue Pastel
            cardBorder = Color(0xFFC7D2FE),
        )
        HomeShortcutIds.BOOKMARKS -> ShortcutThemeData(
            tagText = "Favorit",
            displayTitle = "Tersimpan",
            tagColor = Color(0xFFA16207),
            gradient = Brush.verticalGradient(listOf(Color(0xFFEAB308), Color(0xFFA16207))),
            cardBackground = Color(0xFFFFFBEB), // Soft Amber Gold Pastel
            cardBorder = Color(0xFFFDE68A),
        )
        else -> ShortcutThemeData(
            tagText = "Layanan",
            displayTitle = item.title,
            tagColor = Color(0xFF374151),
            gradient = Brush.verticalGradient(listOf(Color(0xFF6B7280), Color(0xFF374151))),
            cardBackground = Color(0xFFF9FAFB),
            cardBorder = Color(0xFFE5E7EB),
        )
    }

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = theme.cardBackground,
        border = BorderStroke(1.dp, theme.cardBorder),
        shadowElevation = 0.5.dp,
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
                    .padding(horizontal = 4.dp, vertical = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                // Top Text: Subtitle / Tag and Title
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = if (item.badge != null) 3.dp else 0.dp),
                ) {
                    Text(
                        text = theme.tagText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        fontWeight = FontWeight.SemiBold,
                        color = theme.tagColor,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = if (item.badge != null) 14.dp else 2.dp),
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
                    color = Color(0xFF9333EA),
                    modifier = Modifier.align(Alignment.TopEnd),
                ) {
                    Text(
                        text = item.badge,
                        color = Color.White,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun rememberSurabayaImageLoader(): ImageLoader {
    val context = LocalContext.current
    return remember(context) {
        ImageLoader.Builder(context)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .crossfade(true)
            .build()
    }
}

@Composable
fun SingleFullGraphicBannerView(
    item: HomeBannerItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageLoader = rememberSurabayaImageLoader()
    val context = LocalContext.current

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFFF3E8FF),
        border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
        shadowElevation = 2.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(132.dp)
            .clip(RoundedCornerShape(18.dp))
            .testTag("home_full_promo_banner_${item.id}"),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (item.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(item.imageUrl)
                        .crossfade(true)
                        .build(),
                    imageLoader = imageLoader,
                    contentDescription = item.title.ifBlank { "Banner Promosi Surabaya" },
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                when (item.presetKey) {
                    "PRESET_FESTIVAL" -> Image(
                        painter = painterResource(id = R.drawable.img_event_festival_1793207386551),
                        contentDescription = "Festival Surabaya",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    "PRESET_KULINER" -> Image(
                        painter = painterResource(id = R.drawable.img_kuliner_rawon_1793207383497),
                        contentDescription = "Kuliner Rawon Surabaya",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    "PRESET_HERO_CITY" -> Image(
                        painter = painterResource(id = R.drawable.img_hero_surabaya_1793207382246),
                        contentDescription = "Kota Pahlawan Surabaya",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    "PRESET_MASCOT" -> Image(
                        painter = painterResource(id = R.drawable.img_game_shark_mascot),
                        contentDescription = "Maskot Suro & Boyo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    "PRESET_KEJADIAN" -> Image(
                        painter = painterResource(id = R.drawable.img_kejadian_kota_1793207386068),
                        contentDescription = "Pantauan Kota Surabaya",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                    )
                    else -> Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.horizontalGradient(listOf(Color(0xFFFFDDE5), Color(0xFFF5E8FF), Color(0xFFEDE9FE)))),
                    )
                }
            }

            if (item.title.isNotBlank() && item.title != "Besok klaim hadiah poin warga yang lebih...") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.70f),
                                ),
                                startY = 60f,
                            ),
                        ),
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                ) {
                    if (item.badgeText.isNotBlank() && item.badgeText != "1/7 Hari") {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuperAppGoldBg,
                            border = BorderStroke(1.dp, SuperAppGoldBorder),
                            modifier = Modifier.padding(bottom = 4.dp),
                        ) {
                            Text(
                                text = item.badgeText,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SuperAppGoldText,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            )
                        }
                    }
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (item.subtitle.isNotBlank()) {
                        Text(
                            text = item.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            } else if (item.badgeText.isNotBlank() && item.badgeText != "1/7 Hari") {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Black.copy(alpha = 0.60f),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp),
                ) {
                    Text(
                        text = item.badgeText,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun SingleTicketBannerView(
    item: HomeBannerItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageLoader = rememberSurabayaImageLoader()
    val context = LocalContext.current

    val displayTitle = if (item.title.isNotBlank()) item.title else "Besok klaim hadiah poin warga yang lebih..."
    val displaySubtitle = if (item.subtitle.isNotBlank()) item.subtitle else "Berakhir dalam 6 Hari"
    val displayBadge = if (item.badgeText.isNotBlank()) item.badgeText else "1/7 Hari"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 2.dp, bottom = 2.dp),
    ) {
        Surface(
            onClick = onClick,
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
                .testTag("home_ticket_banner_${item.id}"),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        item.imageUrl.isNotBlank() -> {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(item.imageUrl)
                                    .crossfade(true)
                                    .build(),
                                imageLoader = imageLoader,
                                contentDescription = "Banner Promosi Surabaya",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        item.presetKey == "PRESET_FESTIVAL" -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_event_festival_1793207386551),
                                contentDescription = "Festival Surabaya",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        item.presetKey == "PRESET_KULINER" -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_kuliner_rawon_1793207383497),
                                contentDescription = "Kuliner Rawon Surabaya",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        item.presetKey == "PRESET_HERO_CITY" -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_hero_surabaya_1793207382246),
                                contentDescription = "Kota Pahlawan Surabaya",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        item.presetKey == "PRESET_MASCOT" -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_game_shark_mascot),
                                contentDescription = "Maskot Suro & Boyo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        item.presetKey == "PRESET_KEJADIAN" -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_kejadian_kota_1793207386068),
                                contentDescription = "Pantauan Kota Surabaya",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                            )
                        }
                        else -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_game_treasure_chest),
                                contentDescription = "Peti Hadiah Warga",
                                modifier = Modifier.size(46.dp),
                                contentScale = ContentScale.Fit,
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = displayTitle,
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
                            modifier = Modifier.weight(1f, fill = false),
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
                                        text = displayBadge,
                                        color = Color(0xFF92400E),
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }

                            Text(
                                text = displaySubtitle,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = Color(0xFF4B5563),
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
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
}

@Composable
fun FullGraphicPromoBanner(
    bannerConfig: HomeBannerConfig,
    isAdmin: Boolean,
    onAdminEdit: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val activeBanners = bannerConfig.activeBanners
    val firstItem = activeBanners.firstOrNull() ?: HomeBannerItem()
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp),
    ) {
        SingleFullGraphicBannerView(
            item = firstItem,
            onClick = onClick,
        )
        if (isAdmin) {
            Surface(
                onClick = onAdminEdit,
                shape = CircleShape,
                color = Color(0xFF7C3AED),
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .testTag("admin_edit_banner_full_btn"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Atur Banner",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = "Atur Banner",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }
}

@Composable
fun GojekStyleTicketBanner(
    bannerConfig: HomeBannerConfig = HomeBannerConfig(),
    isAdmin: Boolean = false,
    onSaveBannerConfig: (HomeBannerConfig) -> Unit = {},
    onBannerClick: (HomeBannerConfig) -> Unit = {},
    title: String = bannerConfig.title,
    badgeText: String = bannerConfig.badgeText,
    subText: String = bannerConfig.subtitle,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedInfoBanner by remember { mutableStateOf<HomeBannerItem?>(null) }
    var showAdminEditor by remember { mutableStateOf(false) }

    val bannerItems = remember(bannerConfig) { bannerConfig.activeBanners }
    if (bannerItems.isEmpty() && !isAdmin) {
        return
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { bannerItems.size.coerceAtLeast(1) })
    val isDragged by pagerState.interactionSource.collectIsDraggedAsState()
    var isUserTouching by remember { mutableStateOf(false) }

    // Otomatis bergeser setiap 4.5 detik, jeda seketika jika disentuh/ditahan tekanan tangan atau digeser manual
    LaunchedEffect(bannerItems.size, isDragged, isUserTouching) {
        if (bannerItems.size > 1 && !isDragged && !isUserTouching) {
            while (true) {
                delay(4500L)
                val nextPage = (pagerState.currentPage + 1) % bannerItems.size
                pagerState.animateScrollToPage(
                    page = nextPage,
                    animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 4.dp)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        val anyPressed = event.changes.any { it.pressed }
                        if (isUserTouching != anyPressed) {
                            isUserTouching = anyPressed
                        }
                    }
                }
            },
    ) {
        // Horizontal Pager Carousel: geser manual halus dengan snapping
        HorizontalPager(
            state = pagerState,
            pageSpacing = 10.dp,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("home_banners_carousel"),
        ) { pageIdx ->
            val item = bannerItems.getOrNull(pageIdx) ?: return@HorizontalPager
            val handleItemClick = {
                if (item.targetType == "NONE" || item.targetType == "POPUP_INFO") {
                    selectedInfoBanner = item
                } else {
                    onBannerClick(
                        HomeBannerConfig(
                            id = item.id,
                            title = item.title,
                            subtitle = item.subtitle,
                            badgeText = item.badgeText,
                            imageUrl = item.imageUrl,
                            presetKey = item.presetKey,
                            targetType = item.targetType,
                            targetPayload = item.targetPayload,
                            displayMode = item.displayMode,
                        ),
                    )
                }
                onClick()
            }

            if (item.isFullBanner) {
                SingleFullGraphicBannerView(
                    item = item,
                    onClick = handleItemClick,
                )
            } else {
                SingleTicketBannerView(
                    item = item,
                    onClick = handleItemClick,
                )
            }
        }

        // Indikator Titik & Nomor Kapsul (contoh: 1/3) di pojok kanan bawah
        if (bannerItems.size > 1) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.58f),
                border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.35f)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 12.dp, bottom = 10.dp)
                    .testTag("home_banner_indicator"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${bannerItems.size}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        repeat(bannerItems.size) { idx ->
                            val isCurr = pagerState.currentPage == idx
                            val dotWidth by animateDpAsState(
                                targetValue = if (isCurr) 10.dp else 4.dp,
                                label = "banner_dot_$idx",
                            )
                            Box(
                                modifier = Modifier
                                    .width(dotWidth)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurr) Color.White else Color.White.copy(alpha = 0.45f)),
                            )
                        }
                    }
                }
            }
        }

        // Tombol Mengambang "Atur Banner" khusus Admin
        if (isAdmin) {
            Surface(
                onClick = { showAdminEditor = true },
                shape = CircleShape,
                color = Color(0xFF7C3AED),
                shadowElevation = 4.dp,
                border = BorderStroke(1.dp, Color.White),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .testTag("admin_edit_banner_btn"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = "Atur Banner",
                        tint = Color.White,
                        modifier = Modifier.size(13.dp),
                    )
                    Text(
                        text = "Atur Banner",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                    )
                }
            }
        }
    }

    selectedInfoBanner?.let { banner ->
        AlertDialog(
            onDismissRequest = { selectedInfoBanner = null },
            confirmButton = {
                TextButton(onClick = { selectedInfoBanner = null }) {
                    Text("Siap Rek!")
                }
            },
            title = {
                Text(
                    text = banner.title.ifBlank { "Info Arek Surabaya" },
                    fontWeight = FontWeight.Bold,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = banner.subtitle.ifBlank { "Informasi resmi seputar kota dan layanan warga." },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Pantau terus agenda kota Surabaya, nikmati kuliner khas, ikuti polling, gunakan layanan warga, dan lapor insiden di sekitarmu!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
        )
    }

    if (isAdmin && showAdminEditor) {
        AdminBannerEditorDialog(
            currentConfig = bannerConfig,
            onDismiss = { showAdminEditor = false },
            onSave = { updated ->
                onSaveBannerConfig(updated)
                showAdminEditor = false
            },
        )
    }
}

@Composable
fun AdminBannerEditorDialog(
    currentConfig: HomeBannerConfig,
    onDismiss: () -> Unit,
    onSave: (HomeBannerConfig) -> Unit,
) {
    val bannerList = remember {
        mutableStateListOf<HomeBannerItem>().apply {
            addAll(currentConfig.activeBanners)
        }
    }
    var selectedIndex by remember { mutableIntStateOf(0) }
    val safeIndex = selectedIndex.coerceIn(0, (bannerList.size - 1).coerceAtLeast(0))
    val currentItem = bannerList.getOrNull(safeIndex) ?: HomeBannerItem()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            if (uri != null && bannerList.isNotEmpty()) {
                val updated = bannerList[safeIndex].copy(
                    imageUrl = uri.toString(),
                    presetKey = "CUSTOM",
                    displayMode = "FULL_BANNER",
                )
                bannerList[safeIndex] = updated
            }
        },
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("admin_banner_editor_dialog"),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.Campaign,
                    contentDescription = null,
                    tint = Color(0xFF7C3AED),
                )
                Text(
                    text = "Kelola Banner Beranda (Admin)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text(
                    text = "Daftar Slide Banner (" + bannerList.size + "/5):",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )

                // Slide Selector Tabs + Add Slide Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    bannerList.indices.forEach { idx ->
                        val isSelected = idx == safeIndex
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedIndex = idx },
                            label = { Text("Slide " + (idx + 1)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF7C3AED),
                                selectedLabelColor = Color.White,
                            ),
                        )
                    }

                    if (bannerList.size < 5) {
                        OutlinedButton(
                            onClick = {
                                val newSlide = HomeBannerItem(
                                    id = java.util.UUID.randomUUID().toString().take(12),
                                    title = "Pengumuman Arek Surabaya #" + (bannerList.size + 1),
                                    subtitle = "Info terhangat seputar kota",
                                    badgeText = "INFO",
                                    displayMode = "FULL_BANNER",
                                    presetKey = "PRESET_FESTIVAL",
                                    order = bannerList.size,
                                )
                                bannerList.add(newSlide)
                                selectedIndex = bannerList.size - 1
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tambah Slide", fontSize = 11.sp)
                        }
                    }
                }

                // Action Reorder & Delete for this slide
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = {
                                if (safeIndex > 0) {
                                    val temp = bannerList[safeIndex]
                                    bannerList[safeIndex] = bannerList[safeIndex - 1]
                                    bannerList[safeIndex - 1] = temp
                                    selectedIndex = safeIndex - 1
                                }
                            },
                            enabled = safeIndex > 0,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                Icons.Filled.ArrowBack,
                                contentDescription = "Geser ke Depan",
                                tint = if (safeIndex > 0) Color(0xFF7C3AED) else Color.Gray,
                            )
                        }

                        IconButton(
                            onClick = {
                                if (safeIndex < bannerList.size - 1) {
                                    val temp = bannerList[safeIndex]
                                    bannerList[safeIndex] = bannerList[safeIndex + 1]
                                    bannerList[safeIndex + 1] = temp
                                    selectedIndex = safeIndex + 1
                                }
                            },
                            enabled = safeIndex < bannerList.size - 1,
                            modifier = Modifier.size(32.dp),
                        ) {
                            Icon(
                                Icons.Filled.ArrowForward,
                                contentDescription = "Geser ke Belakang",
                                tint = if (safeIndex < bannerList.size - 1) Color(0xFF7C3AED) else Color.Gray,
                            )
                        }
                    }

                    if (bannerList.size > 1) {
                        TextButton(
                            onClick = {
                                bannerList.removeAt(safeIndex)
                                selectedIndex = (safeIndex - 1).coerceAtLeast(0)
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = AlertCrimson),
                        ) {
                            Icon(Icons.Filled.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Hapus Slide Ini", fontSize = 12.sp)
                        }
                    }
                }

                // Live Preview of Selected Slide
                Text(
                    text = "Pratinjau Slide " + (safeIndex + 1) + ":",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                if (currentItem.isFullBanner) {
                    SingleFullGraphicBannerView(item = currentItem, onClick = {})
                } else {
                    SingleTicketBannerView(item = currentItem, onClick = {})
                }

                // 1. Tipe Desain Tampilan
                Text(
                    text = "Gaya Tampilan Slide:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = currentItem.displayMode == "FULL_BANNER",
                        onClick = {
                            bannerList[safeIndex] = currentItem.copy(displayMode = "FULL_BANNER")
                        },
                        label = { Text("Grafis Penuh (Foto/Poster)", fontSize = 11.sp) },
                    )
                    FilterChip(
                        selected = currentItem.displayMode == "TICKET",
                        onClick = {
                            bannerList[safeIndex] = currentItem.copy(displayMode = "TICKET")
                        },
                        label = { Text("Tiket Pastel", fontSize = 11.sp) },
                    )
                }

                // 2. Foto / Gambar
                Text(
                    text = "Foto / Gambar Banner:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                Button(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                ) {
                    Icon(imageVector = Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Pilih Foto dari Galeri HP")
                }

                OutlinedTextField(
                    value = currentItem.imageUrl,
                    onValueChange = { newUrl ->
                        bannerList[safeIndex] = currentItem.copy(
                            imageUrl = newUrl,
                            presetKey = if (newUrl.isNotBlank()) "CUSTOM" else currentItem.presetKey,
                        )
                    },
                    label = { Text("Atau Tempel URL Gambar (https://...)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                // 3. Preset Cepat Khas Surabaya
                Text(
                    text = "Atau Gunakan Gambar Tema Surabaya:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    val presets = listOf(
                        "DEFAULT_TREASURE" to "🎁 Hadiah Poin",
                        "PRESET_FESTIVAL" to "🎪 Festival SBY",
                        "PRESET_KULINER" to "🍲 Kuliner Rawon",
                        "PRESET_HERO_CITY" to "🏛️ Kota Pahlawan",
                        "PRESET_MASCOT" to "🦈 Sura & Baya",
                        "PRESET_KEJADIAN" to "🚨 Info Kota",
                    )
                    presets.forEach { (key, label) ->
                        val selected = currentItem.presetKey == key && currentItem.imageUrl.isBlank()
                        FilterChip(
                            selected = selected,
                            onClick = {
                                bannerList[safeIndex] = currentItem.copy(
                                    presetKey = key,
                                    imageUrl = "",
                                )
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFEDE9FE),
                                selectedLabelColor = Color(0xFF6D28D9),
                            ),
                        )
                    }
                }

                // 4. Judul & Teks
                Text(
                    text = "Teks & Label Banner:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                OutlinedTextField(
                    value = currentItem.title,
                    onValueChange = { newTitle ->
                        bannerList[safeIndex] = currentItem.copy(title = newTitle)
                    },
                    label = { Text("Judul Banner") },
                    placeholder = { Text("cth: Festival Rujak Uleg 2026") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = currentItem.subtitle,
                    onValueChange = { newSub ->
                        bannerList[safeIndex] = currentItem.copy(subtitle = newSub)
                    },
                    label = { Text("Sub-judul / Durasi / Lokasi") },
                    placeholder = { Text("cth: Minggu, Alun-alun Surabaya") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                OutlinedTextField(
                    value = currentItem.badgeText,
                    onValueChange = { newBadge ->
                        bannerList[safeIndex] = currentItem.copy(badgeText = newBadge)
                    },
                    label = { Text("Badge Label") },
                    placeholder = { Text("cth: EVENT, 1/7 Hari, PROMO, PENTING") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )

                // 5. Aksi Klik Banner
                Text(
                    text = "Aksi saat Warga Mengetuk Banner:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                )
                val targets = listOf(
                    "POPUP_INFO" to "Tampilkan Info Dialog",
                    "SEKITAR" to "Buka Menu Sekitar (Teman & Komunitas)",
                    "KULINER" to "Buka Kuliner Surabaya",
                    "EVENT" to "Buka Agenda Event Kota",
                    "DESTINASI" to "Buka Destinasi Wisata",
                    "RADIO" to "Buka Radio Suara Surabaya",
                    "POLLING" to "Buka Polling Warga",
                    "WEB" to "Buka Tautan Web",
                )
                targets.forEach { (type, label) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                bannerList[safeIndex] = currentItem.copy(targetType = type)
                            },
                    ) {
                        RadioButton(
                            selected = currentItem.targetType == type,
                            onClick = {
                                bannerList[safeIndex] = currentItem.copy(targetType = type)
                            },
                        )
                        Text(label, fontSize = 12.5.sp)
                    }
                }

                if (currentItem.targetType == "WEB") {
                    OutlinedTextField(
                        value = currentItem.targetPayload,
                        onValueChange = { newPayload ->
                            bannerList[safeIndex] = currentItem.copy(targetPayload = newPayload)
                        },
                        label = { Text("URL Website Tujuan (https://...)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalBanners = bannerList.mapIndexed { idx, itm ->
                        itm.copy(order = idx)
                    }
                    val first = finalBanners.firstOrNull() ?: HomeBannerItem()
                    onSave(
                        currentConfig.copy(
                            id = first.id,
                            title = first.title.ifBlank { "Besok klaim hadiah poin warga yang lebih..." },
                            subtitle = first.subtitle.ifBlank { "Berakhir dalam 6 Hari" },
                            badgeText = first.badgeText.ifBlank { "1/7 Hari" },
                            imageUrl = first.imageUrl,
                            presetKey = first.presetKey,
                            targetType = first.targetType,
                            targetPayload = first.targetPayload,
                            displayMode = first.displayMode,
                            updatedAt = System.currentTimeMillis(),
                            banners = finalBanners,
                        ),
                    )
                },
                modifier = Modifier.testTag("admin_banner_save_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
            ) {
                Text("Simpan Semua Banner (" + bannerList.size + ")")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(
                    onClick = {
                        onSave(HomeBannerConfig())
                    },
                ) {
                    Text("Reset Standar", color = AlertCrimson)
                }
                TextButton(onClick = onDismiss) {
                    Text("Batal")
                }
            }
        },
    )
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
