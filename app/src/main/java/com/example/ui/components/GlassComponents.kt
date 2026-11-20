package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.bridge.SafeFocusBridgeBus
import com.example.ui.theme.ActiveGlowViolet
import com.example.ui.theme.DeepVioletBg
import com.example.ui.theme.DeepVioletGlass
import com.example.ui.theme.DeepVioletSurface
import com.example.ui.theme.LightVioletBorder
import com.example.ui.theme.LightVioletMuted
import com.example.ui.theme.LightVioletText
import com.example.ui.theme.NormalVioletAccent
import com.example.ui.theme.NormalVioletPrimary
import com.example.ui.theme.NumericLatinFamily
import com.example.ui.theme.PureLavenderWhite
import com.example.ui.viewmodel.SafeFocusTab
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun VioletGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    borderAlpha: Float = 0.24f,
    contentPadding: Dp = 18.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DeepVioletSurface.copy(alpha = 0.88f),
                        DeepVioletBg.copy(alpha = 0.82f)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        LightVioletBorder.copy(alpha = borderAlpha + 0.14f),
                        LightVioletBorder.copy(alpha = borderAlpha * 0.5f)
                    )
                ),
                shape = shape
            )
            .padding(contentPadding),
        content = content
    )
}

@Composable
fun SafeFocusTopHeader(
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val statusBg = if (isActive) {
        NormalVioletPrimary.copy(alpha = 0.34f)
    } else {
        DeepVioletSurface.copy(alpha = 0.72f)
    }
    val statusBorder = if (isActive) {
        LightVioletBorder.copy(alpha = 0.55f)
    } else {
        LightVioletBorder.copy(alpha = 0.25f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DeepVioletSurface.copy(alpha = 0.90f),
                        DeepVioletBg.copy(alpha = 0.78f)
                    )
                )
            )
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 430.dp)
                    .align(Alignment.CenterHorizontally)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(
                                        NormalVioletAccent.copy(alpha = 0.55f),
                                        DeepVioletSurface
                                    )
                                )
                            )
                            .border(1.dp, LightVioletBorder.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = "SafeFocus আইকন",
                            tint = LightVioletText,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Text(
                        text = "SafeFocus",
                        fontFamily = NumericLatinFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        letterSpacing = 0.2.sp,
                        color = PureLavenderWhite
                    )
                }

                Surface(
                    shape = RoundedCornerShape(50),
                    color = statusBg,
                    border = BorderStroke(1.dp, statusBorder),
                    modifier = Modifier.testTag("top_status_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isActive) LightVioletText else LightVioletMuted.copy(alpha = 0.45f)
                                )
                        )
                        Text(
                            text = if (isActive) "সক্রিয়" else "বিরতিতে",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isActive) PureLavenderWhite else LightVioletText.copy(alpha = 0.8f)
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                LightVioletBorder.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun LiquidGlassBottomBar(
    selectedTab: SafeFocusTab,
    onSelectTab: (SafeFocusTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = remember {
        listOf(
            Triple(SafeFocusTab.HOME, "হোম", Icons.Rounded.Home to Icons.Outlined.Home),
            Triple(SafeFocusTab.CONTROL, "কন্ট্রোল", Icons.Rounded.Tune to Icons.Outlined.Tune),
            Triple(SafeFocusTab.SETTINGS, "সেটিংস", Icons.Rounded.Settings to Icons.Outlined.Settings)
        )
    }

    val targetIndex = tabs.indexOfFirst { it.first == selectedTab }.coerceAtLeast(0).toFloat()
    val animatedIndex by animateFloatAsState(
        targetValue = targetIndex,
        animationSpec = spring(
            dampingRatio = 0.66f,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "liquid_tab_glide"
    )
    // দূরত্বের ওপর ভিত্তি করে তরলের মতো প্রসারণ (Liquid Stretch)
    val stretchFraction = (abs(animatedIndex - targetIndex) * 0.45f).coerceIn(0f, 0.36f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        val barShape = RoundedCornerShape(32.dp)
        BoxWithConstraints(
            modifier = Modifier
                .widthIn(max = 410.dp)
                .fillMaxWidth()
                .height(66.dp)
                .clip(barShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            DeepVioletGlass.copy(alpha = 0.92f),
                            DeepVioletBg.copy(alpha = 0.94f)
                        )
                    )
                )
                .border(
                    width = 1.2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LightVioletBorder.copy(alpha = 0.48f),
                            LightVioletBorder.copy(alpha = 0.14f)
                        )
                    ),
                    shape = barShape
                )
                .padding(6.dp)
        ) {
            val totalWidth = maxWidth
            val segmentWidth = totalWidth / tabs.size

            // লিকুইড গ্লাস ইন্ডিকেটর পিল
            Box(
                modifier = Modifier
                    .offset(x = segmentWidth * animatedIndex)
                    .width(segmentWidth)
                    .fillMaxHeight()
                    .graphicsLayer {
                        scaleX = 1f + stretchFraction
                        scaleY = 1f - (stretchFraction * 0.22f)
                    }
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                NormalVioletAccent.copy(alpha = 0.52f),
                                NormalVioletPrimary.copy(alpha = 0.34f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PureLavenderWhite.copy(alpha = 0.55f),
                                LightVioletBorder.copy(alpha = 0.22f)
                            )
                        ),
                        shape = RoundedCornerShape(26.dp)
                    )
            )

            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                tabs.forEach { (tab, label, icons) ->
                    val isSelected = tab == selectedTab
                    val interactionSource = remember { MutableInteractionSource() }
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(26.dp))
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null
                            ) { onSelectTab(tab) }
                            .testTag("nav_tab_${tab.name.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) icons.first else icons.second,
                            contentDescription = label,
                            tint = if (isSelected) PureLavenderWhite else LightVioletText.copy(alpha = 0.62f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSelected) PureLavenderWhite else LightVioletText.copy(alpha = 0.68f),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FloatingInAppBubble(
    isMasterActive: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleMaster: () -> Unit,
    onTriggerQuickPauseTest: () -> Unit,
    onHideBubble: () -> Unit,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val maxXPx = with(density) { (maxWidth - 76.dp).toPx().coerceAtLeast(24f) }
        val maxYPx = with(density) { (maxHeight - 180.dp).toPx().coerceAtLeast(120f) }

        var offsetX by remember { mutableFloatStateOf(maxXPx) }
        var offsetY by remember { mutableFloatStateOf(maxYPx * 0.55f) }

        val clampedX = offsetX.coerceIn(16f, maxXPx)
        val clampedY = offsetY.coerceIn(100f, maxYPx)

        Column(
            modifier = Modifier
                .offset { IntOffset(clampedX.roundToInt(), clampedY.roundToInt()) },
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut()
            ) {
                VioletGlassCard(
                    cornerRadius = 22.dp,
                    borderAlpha = 0.45f,
                    contentPadding = 12.dp,
                    modifier = Modifier
                        .padding(bottom = 10.dp)
                        .width(196.dp)
                ) {
                    Text(
                        text = "ফ্লোটিং কন্ট্রোল",
                        style = MaterialTheme.typography.labelLarge,
                        color = PureLavenderWhite
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    QuickBubbleActionRow(
                        icon = Icons.Rounded.PowerSettingsNew,
                        label = if (isMasterActive) "বিরতিতে নিন" else "সক্রিয় করুন",
                        onClick = onToggleMaster
                    )
                    QuickBubbleActionRow(
                        icon = if (isMasterActive) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                        label = "১৫সে. বিরতি টেস্ট",
                        onClick = onTriggerQuickPauseTest
                    )
                    QuickBubbleActionRow(
                        icon = Icons.Rounded.Close,
                        label = "বাবল বন্ধ করুন",
                        onClick = onHideBubble
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = NormalVioletPrimary,
                border = BorderStroke(1.5.dp, LightVioletBorder),
                shadowElevation = 10.dp,
                modifier = Modifier
                    .size(56.dp)
                    .pointerInput(maxXPx, maxYPx) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            offsetX = (offsetX + dragAmount.x).coerceIn(16f, maxXPx)
                            offsetY = (offsetY + dragAmount.y).coerceIn(100f, maxYPx)
                        }
                    }
                    .clickable { onToggleExpand() }
                    .testTag("floating_popup_bubble")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Shield,
                        contentDescription = "ফ্লোটিং SafeFocus বাবল",
                        tint = PureLavenderWhite,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickBubbleActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(12.dp))
            .background(DeepVioletSurface.copy(alpha = 0.7f))
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = LightVioletText,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = PureLavenderWhite
        )
    }
    Spacer(modifier = Modifier.height(6.dp))
}

@Composable
fun AppBrandEmblem(
    packageName: String,
    isBlocked: Boolean,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isBlocked) {
        NormalVioletPrimary.copy(alpha = 0.35f)
    } else {
        DeepVioletSurface.copy(alpha = 0.9f)
    }
    val strokeColor = if (isBlocked) LightVioletBorder else LightVioletBorder.copy(alpha = 0.38f)

    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(1.dp, strokeColor, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
    ) {
        when (packageName) {
            SafeFocusBridgeBus.PKG_YOUTUBE -> {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "YouTube",
                    tint = LightVioletText,
                    modifier = Modifier.size(26.dp)
                )
            }
            SafeFocusBridgeBus.PKG_FACEBOOK -> {
                Text(
                    text = "f",
                    fontFamily = NumericLatinFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 23.sp,
                    color = LightVioletText
                )
            }
            else -> {
                // Instagram মিনিমাল ভায়োলেট ক্যামেরা প্রতীক
                Canvas(modifier = Modifier.size(20.dp)) {
                    drawRoundRect(
                        color = LightVioletText,
                        topLeft = Offset.Zero,
                        size = size,
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx()),
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawCircle(
                        color = LightVioletText,
                        radius = size.minDimension * 0.24f,
                        style = Stroke(width = 2.dp.toPx())
                    )
                    drawCircle(
                        color = ActiveGlowViolet,
                        radius = size.minDimension * 0.08f,
                        center = Offset(size.width * 0.76f, size.height * 0.24f)
                    )
                }
            }
        }
    }
}

fun formatCountdownMmSs(expiresAtMillis: Long, nowMillis: Long): String {
    val remainingSec = ((expiresAtMillis - nowMillis) / 1000L).coerceAtLeast(0L)
    val mins = remainingSec / 60L
    val secs = remainingSec % 60L
    return String.format("%02d:%02d", mins, secs)
}
