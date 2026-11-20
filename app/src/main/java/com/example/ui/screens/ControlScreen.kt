package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.PlayCircleOutline
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ControlledAppEntity
import com.example.ui.components.AppBrandEmblem
import com.example.ui.components.VioletGlassCard
import com.example.ui.components.formatCountdownMmSs
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ControlScreen(
    apps: List<ControlledAppEntity>,
    nowMillis: Long,
    blockedAttemptsCount: Int,
    lastBridgeEvent: String,
    onToggleReels: (String, Boolean) -> Unit,
    onUpdateDuration: (String, Int) -> Unit,
    onBlockNow: (String) -> Unit,
    onTriggerRenewOrAttempt: (ControlledAppEntity) -> Unit,
    onSimulateReelsTrigger: (ControlledAppEntity) -> Unit,
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            Column {
                Text(
                    text = "অ্যাপ ও রিলস কন্ট্রোল",
                    style = MaterialTheme.typography.headlineMedium,
                    color = PureLavenderWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "YouTube, Facebook ও Instagram-এর ব্লক সময়, অটো-আনব্লক টাইমার এবং Shorts/Reels নিয়ন্ত্রণ করুন।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightVioletText.copy(alpha = 0.8f)
                )
            }

            apps.forEach { app ->
                ControlAppDetailCard(
                    app = app,
                    nowMillis = nowMillis,
                    onToggleReels = { enabled -> onToggleReels(app.packageName, enabled) },
                    onUpdateDuration = { mins -> onUpdateDuration(app.packageName, mins) },
                    onBlockNow = { onBlockNow(app.packageName) },
                    onTriggerRenewOrAttempt = { onTriggerRenewOrAttempt(app) }
                )
            }

            // অ্যান্ড্রয়েড ব্রিজ ও লাইভ সিমুলেশন প্যানেল
            VioletGlassCard(
                cornerRadius = 24.dp,
                borderAlpha = 0.34f,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VerifiedUser,
                        contentDescription = null,
                        tint = LightVioletBorder,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "অ্যান্ড্রয়েড ব্রিজ ও সিমুলেশন টেস্ট",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureLavenderWhite
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "কোনো অ্যাপ খোলা বা Reels/Shorts-এ ঢোকার সাথে সাথে ১৫ সেকেন্ডের বিরতি পপআপ কীভাবে কাজ করে তা এখান থেকেই সরাসরি পরীক্ষা করুন।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightVioletText.copy(alpha = 0.78f)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "মোট বিরতি প্রতিরোধ: $blockedAttemptsCount বার • সর্বশেষ: $lastBridgeEvent",
                    style = MaterialTheme.typography.labelSmall.tabular(),
                    color = LightVioletMuted
                )

                Spacer(modifier = Modifier.height(12.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    apps.forEach { app ->
                        OutlinedButton(
                            onClick = { onSimulateReelsTrigger(app) },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, LightVioletBorder.copy(alpha = 0.42f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("sim_reels_${app.displayName.lowercase()}")
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PlayCircleOutline,
                                contentDescription = null,
                                tint = LightVioletText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${app.displayName} ${app.reelsLabel} টেস্ট",
                                style = MaterialTheme.typography.labelMedium,
                                color = PureLavenderWhite
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

@Composable
private fun ControlAppDetailCard(
    app: ControlledAppEntity,
    nowMillis: Long,
    onToggleReels: (Boolean) -> Unit,
    onUpdateDuration: (Int) -> Unit,
    onBlockNow: () -> Unit,
    onTriggerRenewOrAttempt: () -> Unit
) {
    val isBlockTimerActive = app.isBlocked && app.blockExpiresAtMillis > nowMillis
    val isRenewActive = !app.isBlocked && app.renewExpiresAtMillis > nowMillis
    val presetMinutes = listOf(15, 30, 60, 120)

    VioletGlassCard(
        cornerRadius = 24.dp,
        borderAlpha = if (app.isBlocked) 0.44f else 0.24f,
        modifier = Modifier.fillMaxWidth()
    ) {
        // অ্যাপ হেডার + রিলস সুইচ
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppBrandEmblem(
                    packageName = app.packageName,
                    isBlocked = app.isBlocked
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = app.displayName,
                        fontFamily = NumericLatinFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = PureLavenderWhite
                    )
                    Text(
                        text = "${app.reelsLabel} ফিল্টার ও টাইমার",
                        style = MaterialTheme.typography.labelSmall,
                        color = LightVioletText.copy(alpha = 0.72f)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (app.blockReelsAndShorts) "ON" else "OFF",
                    fontFamily = NumericLatinFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = if (app.blockReelsAndShorts) PureLavenderWhite else LightVioletMuted
                )
                Switch(
                    checked = app.blockReelsAndShorts,
                    onCheckedChange = onToggleReels,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = PureLavenderWhite,
                        checkedTrackColor = NormalVioletPrimary,
                        uncheckedThumbColor = LightVioletBorder,
                        uncheckedTrackColor = DeepVioletBg,
                        uncheckedBorderColor = LightVioletBorder.copy(alpha = 0.35f)
                    ),
                    modifier = Modifier.testTag("switch_reels_${app.displayName.lowercase()}")
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // লাইভ অটো-আনব্লক বা রিনিউ টাইমার বক্স
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DeepVioletBg.copy(alpha = 0.78f))
                .border(
                    width = 1.dp,
                    color = LightVioletBorder.copy(alpha = 0.22f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isRenewActive) Icons.Rounded.LockClock else Icons.Rounded.Timer,
                        contentDescription = null,
                        tint = LightVioletBorder,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = when {
                            isBlockTimerActive -> "অটো-আনব্লক হতে বাকি:"
                            isRenewActive -> "৫মি. রিনিউ শেষে অটো-লক:"
                            app.isBlocked -> "স্থায়ী ব্লক সক্রিয়:"
                            else -> "নির্ধারিত ব্লক সময়:"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightVioletText
                    )
                }

                Text(
                    text = when {
                        isBlockTimerActive -> formatCountdownMmSs(app.blockExpiresAtMillis, nowMillis)
                        isRenewActive -> formatCountdownMmSs(app.renewExpiresAtMillis, nowMillis)
                        else -> "${app.blockDurationMinutes} মিনিট"
                    },
                    style = MaterialTheme.typography.titleMedium.tabular(),
                    color = PureLavenderWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ব্লক সময় নির্ধারণ চিপস + স্টেপার
        Text(
            text = "কত মিনিট ব্লক থাকবে তা ঠিক করুন:",
            style = MaterialTheme.typography.labelMedium,
            color = LightVioletText.copy(alpha = 0.85f)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            presetMinutes.forEach { mins ->
                val selected = app.blockDurationMinutes == mins
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .minimumInteractiveComponentSize()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (selected) NormalVioletPrimary else DeepVioletSurface
                        )
                        .border(
                            width = 1.dp,
                            color = if (selected) LightVioletBorder else LightVioletBorder.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onUpdateDuration(mins) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$mins মি.",
                        style = MaterialTheme.typography.labelMedium.tabular(),
                        color = PureLavenderWhite
                    )
                }
            }

            IconButton(
                onClick = { onUpdateDuration((app.blockDurationMinutes - 5).coerceAtLeast(5)) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepVioletSurface)
                    .border(1.dp, LightVioletBorder.copy(alpha = 0.24f), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Remove,
                    contentDescription = "৫ মিনিট কমান",
                    tint = LightVioletText,
                    modifier = Modifier.size(18.dp)
                )
            }
            IconButton(
                onClick = { onUpdateDuration((app.blockDurationMinutes + 5).coerceAtMost(240)) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DeepVioletSurface)
                    .border(1.dp, LightVioletBorder.copy(alpha = 0.24f), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "৫ মিনিট বাড়ান",
                    tint = LightVioletText,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onBlockNow,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NormalVioletPrimary,
                    contentColor = PureLavenderWhite
                ),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("control_block_now_${app.displayName.lowercase()}")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Block,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (app.isBlocked) "সময় রিসেট" else "এখনই ব্লক",
                    style = MaterialTheme.typography.labelLarge
                )
            }

            OutlinedButton(
                onClick = onTriggerRenewOrAttempt,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, LightVioletBorder.copy(alpha = 0.45f)),
                modifier = Modifier
                    .weight(1f)
                    .minimumInteractiveComponentSize()
                    .testTag("control_renew_${app.displayName.lowercase()}")
            ) {
                Text(
                    text = "রিনিউ / টেস্ট",
                    style = MaterialTheme.typography.labelLarge,
                    color = LightVioletText
                )
            }
        }
    }
}
