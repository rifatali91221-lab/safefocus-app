package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ControlledAppEntity
import com.example.ui.components.AppBrandEmblem
import com.example.ui.components.VioletGlassCard
import com.example.ui.components.formatCountdownMmSs
import com.example.ui.theme.ActiveGlowViolet
import com.example.ui.theme.DeepVioletBg
import com.example.ui.theme.DeepVioletSurface
import com.example.ui.theme.LightVioletBorder
import com.example.ui.theme.LightVioletMuted
import com.example.ui.theme.LightVioletText
import com.example.ui.theme.NormalVioletAccent
import com.example.ui.theme.NormalVioletPrimary
import com.example.ui.theme.NumericLatinFamily
import com.example.ui.theme.PureLavenderWhite
import com.example.ui.theme.tabular

@Composable
fun HomeScreen(
    isMasterActive: Boolean,
    apps: List<ControlledAppEntity>,
    nowMillis: Long,
    onToggleMaster: () -> Unit,
    onQuickToggleApp: (ControlledAppEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 430.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ৩৫%-৪২% উচ্চতায় বড় ভিপিএন-স্টাইল অ্যাক্টিভেশন সার্কেল রাখার জন্য সুষম স্পেস
            Spacer(modifier = Modifier.height(64.dp))

            PowerActivationCircle(
                isActive = isMasterActive,
                onClick = onToggleMaster
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (isMasterActive) {
                    "রিলস ও শর্টস সুরক্ষা এখন চালু আছে"
                } else {
                    "মনোযোগ সুরক্ষা চালু করতে ট্যাপ করুন"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = LightVioletText.copy(alpha = 0.84f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(34.dp))

            // শুধুমাত্র সমর্থিত ৩টি অ্যাপ: YouTube, Facebook, Instagram
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                apps.forEach { app ->
                    HomeSupportedAppCard(
                        app = app,
                        isMasterActive = isMasterActive,
                        nowMillis = nowMillis,
                        onQuickToggle = { onQuickToggleApp(app) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
private fun PowerActivationCircle(
    isActive: Boolean,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "power_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.48f,
        targetValue = 0.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    val coreScale by animateFloatAsState(
        targetValue = if (isActive) 1.04f else 1.0f,
        label = "core_scale"
    )
    val coreTopColor by animateColorAsState(
        targetValue = if (isActive) NormalVioletAccent else DeepVioletSurface,
        label = "core_top"
    )
    val coreBottomColor by animateColorAsState(
        targetValue = if (isActive) NormalVioletPrimary else DeepVioletBg,
        label = "core_bottom"
    )

    Box(
        modifier = Modifier.size(238.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isActive) {
            // বাইরের পালসিং ভায়োলেট গ্লো রিং
            Box(
                modifier = Modifier
                    .size(226.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                    }
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                ActiveGlowViolet.copy(alpha = pulseAlpha),
                                NormalVioletPrimary.copy(alpha = 0f)
                            )
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(198.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.5.dp,
                        color = LightVioletBorder.copy(alpha = 0.42f),
                        shape = CircleShape
                    )
            )
        } else {
            // বিরতিতে শান্ত রিং
            Box(
                modifier = Modifier
                    .size(196.dp)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = LightVioletBorder.copy(alpha = 0.22f),
                        shape = CircleShape
                    )
            )
        }

        Box(
            modifier = Modifier
                .size(172.dp)
                .graphicsLayer {
                    scaleX = coreScale
                    scaleY = coreScale
                }
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(coreTopColor, coreBottomColor)
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            LightVioletBorder.copy(alpha = if (isActive) 0.9f else 0.35f),
                            NormalVioletPrimary.copy(alpha = 0.45f)
                        )
                    ),
                    shape = CircleShape
                )
                .clickable { onClick() }
                .testTag("home_power_circle_button"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Rounded.PowerSettingsNew,
                    contentDescription = if (isActive) "সক্রিয়" else "শুরু করুন",
                    tint = PureLavenderWhite,
                    modifier = Modifier.size(44.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isActive) "সক্রিয়" else "শুরু করুন",
                    style = MaterialTheme.typography.titleLarge,
                    color = PureLavenderWhite
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isActive) "বন্ধ করতে চাপুন" else "সুরক্ষা চালু করুন",
                    style = MaterialTheme.typography.labelSmall,
                    color = LightVioletText.copy(alpha = 0.85f)
                )
            }
        }
    }
}

@Composable
private fun HomeSupportedAppCard(
    app: ControlledAppEntity,
    isMasterActive: Boolean,
    nowMillis: Long,
    onQuickToggle: () -> Unit
) {
    val isRenewActive = !app.isBlocked && app.renewExpiresAtMillis > nowMillis
    val isBlockTimerRunning = app.isBlocked && app.blockExpiresAtMillis > nowMillis

    val statusText = when {
        app.isBlocked && isBlockTimerRunning ->
            "ব্লক করা • ${formatCountdownMmSs(app.blockExpiresAtMillis, nowMillis)}"
        app.isBlocked -> "ব্লক করা আছে"
        isRenewActive ->
            "৫মি. রিনিউ • ${formatCountdownMmSs(app.renewExpiresAtMillis, nowMillis)}"
        isMasterActive && app.blockReelsAndShorts -> "${app.reelsLabel} বন্ধ আছে"
        else -> "আনব্লক / মুক্ত"
    }

    VioletGlassCard(
        cornerRadius = 22.dp,
        borderAlpha = if (app.isBlocked) 0.42f else 0.2f,
        contentPadding = 14.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AppBrandEmblem(
                    packageName = app.packageName,
                    isBlocked = app.isBlocked
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = app.displayName,
                        fontFamily = NumericLatinFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = PureLavenderWhite
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = if (app.isBlocked) Icons.Rounded.Lock else Icons.Rounded.LockOpen,
                            contentDescription = null,
                            tint = if (app.isBlocked) LightVioletBorder else LightVioletMuted,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = statusText,
                            style = if (isBlockTimerRunning || isRenewActive) {
                                MaterialTheme.typography.bodyMedium.tabular()
                            } else {
                                MaterialTheme.typography.bodyMedium
                            },
                            color = LightVioletText.copy(alpha = 0.85f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (app.isBlocked || isRenewActive) {
                OutlinedButton(
                    onClick = onQuickToggle,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, LightVioletBorder.copy(alpha = 0.5f)),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("home_unblock_${app.displayName.lowercase()}")
                ) {
                    Text(
                        text = "আনব্লক করুন",
                        style = MaterialTheme.typography.labelMedium,
                        color = LightVioletText
                    )
                }
            } else {
                Button(
                    onClick = onQuickToggle,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NormalVioletPrimary,
                        contentColor = PureLavenderWhite
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("home_block_${app.displayName.lowercase()}")
                ) {
                    Text(
                        text = "ব্লক করুন",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
