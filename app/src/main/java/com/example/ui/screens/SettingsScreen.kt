package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.rounded.AccessibilityNew
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Calculate
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.Layers
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SystemUpdateAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.FocusSettingsEntity
import com.example.data.local.MotivationalQuoteEntity
import com.example.data.local.StudyQuestionEntity
import com.example.ui.components.VioletGlassCard
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
import com.example.ui.viewmodel.PermissionType

@Composable
fun SettingsScreen(
    settings: FocusSettingsEntity,
    quotes: List<MotivationalQuoteEntity>,
    questions: List<StudyQuestionEntity>,
    systemAccessibilityGranted: Boolean,
    systemOverlayGranted: Boolean,
    showUpdatePopup: Boolean,
    onPermissionSwitch: (PermissionType, Boolean) -> Unit,
    onUpdateMaxRenewMinutes: (Int) -> Unit,
    onToggleMathQuestions: (Boolean) -> Unit,
    onToggleBookQuestions: (Boolean) -> Unit,
    onAddQuote: (String) -> Unit,
    onDeleteQuote: (Int) -> Unit,
    onAddStudyQuestion: (String, String, String) -> Unit,
    onDeleteStudyQuestion: (Int) -> Unit,
    onToggleUpdatePopup: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var newQuoteInput by remember { mutableStateOf("") }
    var newQuestionInput by remember { mutableStateOf("") }
    var newAnswerInput by remember { mutableStateOf("") }
    var newSubjectInput by remember { mutableStateOf("") }

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

            Text(
                text = "সেটিংস ও সুরক্ষা",
                style = MaterialTheme.typography.headlineMedium,
                color = PureLavenderWhite
            )

            // ১. ধাপে ধাপে পারমিশন সেটআপ উইজার্ড
            StepByStepPermissionWizard(
                settings = settings,
                systemAccessibilityGranted = systemAccessibilityGranted,
                systemOverlayGranted = systemOverlayGranted,
                onPermissionSwitch = onPermissionSwitch
            )

            // ২. রুলস ও রিনিউ সীমা সেকশন
            VioletGlassCard(cornerRadius = 24.dp, borderAlpha = 0.28f) {
                Text(
                    text = "রিনিউ রুলস ও প্রশ্নের ধরন",
                    style = MaterialTheme.typography.titleMedium,
                    color = PureLavenderWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "১৫ সেকেন্ড বিরতির পর প্রশ্ন সঠিক হলে সর্বোচ্চ ৫ মিনিটের জন্য অ্যাপ চালু করা যাবে।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightVioletText.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "সর্বোচ্চ রিনিউ সময়সীমা: ${settings.maxRenewMinutes} মিনিট (সর্বোচ্চ সীমা ৫ মিনিট)",
                    style = MaterialTheme.typography.labelLarge.tabular(),
                    color = LightVioletBorder
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    (1..5).forEach { min ->
                        val selected = settings.maxRenewMinutes == min
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (selected) NormalVioletPrimary else DeepVioletBg)
                                .border(
                                    1.dp,
                                    if (selected) LightVioletBorder else LightVioletBorder.copy(alpha = 0.22f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onUpdateMaxRenewMinutes(min) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$min মি.",
                                style = MaterialTheme.typography.labelMedium.tabular(),
                                color = PureLavenderWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                RuleToggleRow(
                    icon = Icons.Rounded.Calculate,
                    label = "গণিতের প্রশ্ন চালু রাখুন",
                    checked = settings.useMathQuestions,
                    onCheckedChange = onToggleMathQuestions
                )

                Spacer(modifier = Modifier.height(8.dp))

                RuleToggleRow(
                    icon = Icons.Rounded.MenuBook,
                    label = "বইয়ের ডাটাশিট / নোটসের প্রশ্ন",
                    checked = settings.useBookQuestions,
                    onCheckedChange = onToggleBookQuestions
                )
            }

            // ৩. নিজের বইয়ের ডাটাশিট / নোটস প্রশ্ন ব্যাংক
            VioletGlassCard(cornerRadius = 24.dp, borderAlpha = 0.26f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MenuBook,
                        contentDescription = null,
                        tint = LightVioletBorder,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "বইয়ের ডাটাশিট ও নোটস প্রশ্ন ব্যাংক",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureLavenderWhite
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "আপনার পড়ার বই বা নোটস থেকে প্রশ্ন ও উত্তর যোগ করুন (সম্পূর্ণ অফলাইনে সংরক্ষিত)।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightVioletText.copy(alpha = 0.78f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                VioletTextField(
                    value = newSubjectInput,
                    onValueChange = { newSubjectInput = it },
                    label = "বিষয় / বইয়ের নাম (ঐচ্ছিক)"
                )
                Spacer(modifier = Modifier.height(8.dp))
                VioletTextField(
                    value = newQuestionInput,
                    onValueChange = { newQuestionInput = it },
                    label = "প্রশ্ন লিখুন..."
                )
                Spacer(modifier = Modifier.height(8.dp))
                VioletTextField(
                    value = newAnswerInput,
                    onValueChange = { newAnswerInput = it },
                    label = "সঠিক উত্তর লিখুন..."
                )
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = {
                        if (newQuestionInput.isNotBlank() && newAnswerInput.isNotBlank()) {
                            onAddStudyQuestion(newQuestionInput, newAnswerInput, newSubjectInput)
                            newQuestionInput = ""
                            newAnswerInput = ""
                            newSubjectInput = ""
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NormalVioletPrimary,
                        contentColor = PureLavenderWhite
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .testTag("add_book_question_btn")
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("প্রশ্ন যোগ করুন", style = MaterialTheme.typography.labelLarge)
                }

                if (questions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    questions.forEach { q ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(DeepVioletBg.copy(alpha = 0.75f))
                                .border(1.dp, LightVioletBorder.copy(alpha = 0.16f), RoundedCornerShape(14.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = q.subjectNote,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LightVioletMuted
                                )
                                Text(
                                    text = q.question,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PureLavenderWhite
                                )
                                Text(
                                    text = "উত্তর: ${q.answer}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = LightVioletBorder
                                )
                            }
                            IconButton(onClick = { onDeleteStudyQuestion(q.id) }) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "প্রশ্ন মুছুন",
                                    tint = LightVioletText.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }
                }
            }

            // ৪. নিজের মোটিভেশনাল উক্তি যোগ করার অপশন
            VioletGlassCard(cornerRadius = 24.dp, borderAlpha = 0.26f) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FormatQuote,
                        contentDescription = null,
                        tint = LightVioletBorder,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "১৫ সেকেন্ড বিরতির উক্তি",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureLavenderWhite
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VioletTextField(
                        value = newQuoteInput,
                        onValueChange = { newQuoteInput = it },
                        label = "নিজের উক্তি লিখুন...",
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (newQuoteInput.isNotBlank()) {
                                onAddQuote(newQuoteInput)
                                newQuoteInput = ""
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NormalVioletPrimary,
                            contentColor = PureLavenderWhite
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("add_quote_btn")
                    ) {
                        Text("যোগ", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                quotes.forEach { quote ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DeepVioletBg.copy(alpha = 0.65f))
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "“${quote.text}”",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightVioletText,
                            modifier = Modifier.weight(1f)
                        )
                        if (!quote.isDefault) {
                            IconButton(onClick = { onDeleteQuote(quote.id) }) {
                                Icon(
                                    imageVector = Icons.Rounded.DeleteOutline,
                                    contentDescription = "উক্তি মুছুন",
                                    tint = LightVioletMuted
                                )
                            }
                        }
                    }
                }
            }

            // ৫. প্রাইভেসি ও নিরাপত্তা ভিজ্যুয়াল ডায়াগ্রাম
            PrivacyArchitectureDiagramCard(
                onOpenUpdateDialog = { onToggleUpdatePopup(true) }
            )

            Spacer(modifier = Modifier.height(120.dp))
        }
    }

    if (showUpdatePopup) {
        TransparentUpdateLinksDialog(onDismiss = { onToggleUpdatePopup(false) })
    }
}

@Composable
private fun StepByStepPermissionWizard(
    settings: FocusSettingsEntity,
    systemAccessibilityGranted: Boolean,
    systemOverlayGranted: Boolean,
    onPermissionSwitch: (PermissionType, Boolean) -> Unit
) {
    val isStep1Done = settings.backgroundServiceEnabled
    val isStep2Done = systemOverlayGranted || settings.overlayBubbleEnabled
    val isStep3Done = systemAccessibilityGranted || settings.accessibilityBridgeEnabled
    val completedCount = listOf(isStep1Done, isStep2Done, isStep3Done).count { it }
    val progressFraction = completedCount / 3f

    VioletGlassCard(cornerRadius = 24.dp, borderAlpha = 0.34f) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ধাপে ধাপে পারমিশন সেটআপ",
                    style = MaterialTheme.typography.titleMedium,
                    color = PureLavenderWhite
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "ভয় পাওয়ার কিছু নেই—প্রয়োজন অনুযায়ী যেকোনো একটি ধাপ সম্পন্ন করুন।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightVioletText.copy(alpha = 0.78f)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(NormalVioletPrimary.copy(alpha = 0.3f))
                    .border(1.dp, LightVioletBorder.copy(alpha = 0.45f), RoundedCornerShape(50))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "$completedCount/3 সম্পন্ন",
                    style = MaterialTheme.typography.labelMedium.tabular(),
                    color = PureLavenderWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ভায়োলেট গ্লাস প্রোগ্রেস বার
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(50))
                .background(DeepVioletBg)
                .border(0.5.dp, LightVioletBorder.copy(alpha = 0.2f), RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progressFraction.coerceAtLeast(0.06f))
                    .height(6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(NormalVioletAccent)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        WizardStepRow(
            stepBadge = "ধাপ ১",
            icon = Icons.Rounded.NotificationsActive,
            title = "ব্যাকগ্রাউন্ড সার্ভিস",
            subtitle = if (isStep1Done) {
                "অফলাইন টাইমার ও অটো-লক সচল আছে"
            } else {
                "অটো-লক টাইমার সচল রাখতে এটি চালু করুন"
            },
            isCompleted = isStep1Done,
            onToggle = { onPermissionSwitch(PermissionType.BACKGROUND, it) },
            testTag = "perm_background_switch"
        )

        Spacer(modifier = Modifier.height(10.dp))

        WizardStepRow(
            stepBadge = "ধাপ ২",
            icon = Icons.Rounded.Layers,
            title = "পপআপ উইন্ডো / ওভারলে",
            subtitle = when {
                systemOverlayGranted -> "সিস্টেমে ওভারলে অনুমতি চালু আছে"
                settings.overlayBubbleEnabled -> "ইন-অ্যাপ ফ্লোটিং বাবল সক্রিয়"
                else -> "ভাসমান ফোকাস আইকনের জন্য (প্রয়োজনে)"
            },
            isCompleted = isStep2Done,
            onToggle = { onPermissionSwitch(PermissionType.OVERLAY, it) },
            testTag = "perm_overlay_switch"
        )

        Spacer(modifier = Modifier.height(10.dp))

        WizardStepRow(
            stepBadge = "ধাপ ৩",
            icon = Icons.Rounded.AccessibilityNew,
            title = "অ্যাক্সেসিবিলিটি সার্ভিস",
            subtitle = when {
                systemAccessibilityGranted -> "সিস্টেমে Reels ও Shorts শিল্ড সক্রিয়"
                settings.accessibilityBridgeEnabled -> "অ্যান্ড্রয়েড ব্রিজ মোডে চালু আছে"
                else -> "Shorts ও Reels ধরতে সহজ গাইডসহ চালু করুন"
            },
            isCompleted = isStep3Done,
            onToggle = { onPermissionSwitch(PermissionType.ACCESSIBILITY, it) },
            testTag = "perm_accessibility_switch"
        )
    }
}

@Composable
private fun WizardStepRow(
    stepBadge: String,
    icon: ImageVector,
    title: String,
    subtitle: String,
    isCompleted: Boolean,
    onToggle: (Boolean) -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(DeepVioletBg.copy(alpha = 0.78f))
            .border(
                width = 1.dp,
                color = if (isCompleted) LightVioletBorder.copy(alpha = 0.45f) else LightVioletBorder.copy(alpha = 0.18f),
                shape = RoundedCornerShape(18.dp)
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
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isCompleted) NormalVioletPrimary else DeepVioletSurface
                        )
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = stepBadge,
                        style = MaterialTheme.typography.labelSmall,
                        color = PureLavenderWhite
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (isCompleted) NormalVioletAccent.copy(alpha = 0.28f) else DeepVioletSurface
                        )
                        .border(
                            0.8.dp,
                            if (isCompleted) LightVioletBorder.copy(alpha = 0.55f) else LightVioletBorder.copy(alpha = 0.22f),
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 9.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = if (isCompleted) "চালু আছে" else "বাকি আছে",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCompleted) PureLavenderWhite else LightVioletMuted
                    )
                }
            }

            Text(
                text = if (isCompleted) "ON" else "OFF",
                fontFamily = NumericLatinFamily,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isCompleted) LightVioletText else LightVioletMuted
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(NormalVioletPrimary.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = LightVioletText,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.labelLarge,
                        color = PureLavenderWhite
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = LightVioletText.copy(alpha = 0.78f)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = isCompleted,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = PureLavenderWhite,
                    checkedTrackColor = NormalVioletPrimary,
                    uncheckedThumbColor = LightVioletBorder,
                    uncheckedTrackColor = DeepVioletSurface,
                    uncheckedBorderColor = LightVioletBorder.copy(alpha = 0.35f)
                ),
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@Composable
private fun RuleToggleRow(
    icon: ImageVector,
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DeepVioletBg.copy(alpha = 0.68f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(icon, contentDescription = null, tint = LightVioletBorder, modifier = Modifier.size(18.dp))
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = PureLavenderWhite)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = PureLavenderWhite,
                checkedTrackColor = NormalVioletPrimary,
                uncheckedThumbColor = LightVioletBorder,
                uncheckedTrackColor = DeepVioletSurface
            )
        )
    }
}

@Composable
private fun VioletTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NormalVioletAccent,
            unfocusedBorderColor = LightVioletBorder.copy(alpha = 0.28f),
            focusedTextColor = PureLavenderWhite,
            unfocusedTextColor = PureLavenderWhite,
            focusedLabelColor = LightVioletBorder,
            unfocusedLabelColor = LightVioletMuted,
            cursorColor = LightVioletBorder,
            focusedContainerColor = DeepVioletBg.copy(alpha = 0.7f),
            unfocusedContainerColor = DeepVioletBg.copy(alpha = 0.55f)
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
private fun PrivacyArchitectureDiagramCard(
    onOpenUpdateDialog: () -> Unit
) {
    VioletGlassCard(
        cornerRadius = 24.dp,
        borderAlpha = 0.35f,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                tint = LightVioletBorder,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "প্রাইভেসি ও নিরাপত্তা ডায়াগ্রাম",
                style = MaterialTheme.typography.titleMedium,
                color = PureLavenderWhite
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // ৩টি ধাপের ভিজ্যুয়াল কানেক্টেড ডায়াগ্রাম
        DiagramNode(
            stepNumber = "১",
            title = "১০০% অফলাইন লোকাল স্টোরেজ",
            detail = "আপনার সব টাইমার, উক্তি ও বইয়ের নোটস কেবল ফোনের অফলাইন ডেটাবেসে থাকে। কোনো সার্ভারে ডাটা যায় না।"
        )
        DiagramConnectorLine()
        DiagramNode(
            stepNumber = "২",
            title = "কোনো ডাটা চুরি বা নষ্ট নয়",
            detail = "পাসওয়ার্ড, মেসেজ, ছবি বা ব্রাউজিং হিস্ট্রি কখনোই পড়া হয় না; কোনো অনুমতি ছাড়া ফোনে কিছুই পরিবর্তন হয় না।"
        )
        DiagramConnectorLine()
        DiagramNode(
            stepNumber = "৩",
            title = "স্বচ্ছ আপডেট (Telegram ও GitHub)",
            detail = "নতুন ভার্সন এলে গোপনে আপডেট হয় না—পপআপে সরাসরি Telegram ও GitHub লিংক দেখানো হয়।"
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedButton(
            onClick = onOpenUpdateDialog,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, LightVioletBorder.copy(alpha = 0.5f)),
            modifier = Modifier
                .fillMaxWidth()
                .minimumInteractiveComponentSize()
                .testTag("open_update_links_btn")
        ) {
            Icon(
                imageVector = Icons.Rounded.SystemUpdateAlt,
                contentDescription = null,
                tint = LightVioletText,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "আপডেট পপআপ ও Telegram / GitHub দেখুন",
                style = MaterialTheme.typography.labelLarge,
                color = PureLavenderWhite
            )
        }
    }
}

@Composable
private fun DiagramNode(
    stepNumber: String,
    title: String,
    detail: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DeepVioletBg.copy(alpha = 0.85f))
            .border(1.dp, LightVioletBorder.copy(alpha = 0.28f), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(NormalVioletPrimary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                style = MaterialTheme.typography.labelLarge,
                color = PureLavenderWhite
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = PureLavenderWhite
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = LightVioletText.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun DiagramConnectorLine() {
    Box(
        modifier = Modifier
            .padding(start = 25.dp)
            .height(18.dp)
            .width(2.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawLine(
                color = LightVioletBorder.copy(alpha = 0.55f),
                start = Offset(size.width / 2f, 0f),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
            )
        }
    }
}

@Composable
fun PermissionExplanationModal(
    permissionType: PermissionType,
    onDismiss: () -> Unit,
    onConfirmUnderstood: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVioletSurface,
        titleContentColor = PureLavenderWhite,
        textContentColor = LightVioletText,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Shield,
                contentDescription = null,
                tint = LightVioletBorder,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = permissionType.titleBn,
                style = MaterialTheme.typography.titleLarge,
                color = PureLavenderWhite
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = permissionType.explanationBn,
                    style = MaterialTheme.typography.bodyLarge,
                    color = PureLavenderWhite
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DeepVioletBg)
                        .border(1.dp, LightVioletBorder.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = permissionType.privacyGuaranteeBn,
                        style = MaterialTheme.typography.bodyMedium,
                        color = LightVioletText
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmUnderstood,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NormalVioletPrimary,
                    contentColor = PureLavenderWhite
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("permission_understood_btn")
            ) {
                Text("বুঝেছি", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("পরে করব", style = MaterialTheme.typography.labelLarge, color = LightVioletMuted)
            }
        }
    )
}

@Composable
fun RestrictedSettingGuideModal(
    permissionType: PermissionType,
    onOpenAppInfo: () -> Unit,
    onGoToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVioletSurface,
        titleContentColor = PureLavenderWhite,
        textContentColor = LightVioletText,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                tint = LightVioletBorder,
                modifier = Modifier.size(30.dp)
            )
        },
        title = {
            Text(
                text = "Android-এ 'Restricted setting' আসলে কী করবেন?",
                style = MaterialTheme.typography.titleLarge,
                color = PureLavenderWhite
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DeepVioletBg)
                        .border(1.dp, LightVioletBorder.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "কেন এই সতর্কতা আসে:",
                            style = MaterialTheme.typography.labelLarge,
                            color = LightVioletBorder
                        )
                        Text(
                            text = "Play Store-এর বাইরে থেকে APK ইনস্টল করলে Android নিরাপত্তার খাতিরে নিজে থেকেই এটি দেখায়—এটি কোনো ভাইরাস বা হ্যাকিংয়ের সংকেত নয়। SafeFocus সম্পূর্ণ অফলাইনে চলে এবং কোনো ব্যক্তিগত ডাটা সংগ্রহ করে না।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = PureLavenderWhite
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DeepVioletBg.copy(alpha = 0.78f))
                        .border(1.dp, LightVioletBorder.copy(alpha = 0.22f), RoundedCornerShape(14.dp))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "সমাধানের সহজ ৩টি ধাপ:",
                            style = MaterialTheme.typography.labelLarge,
                            color = LightVioletBorder
                        )
                        Text(
                            text = "ধাপ ১: ফোনের Settings > Apps > SafeFocus (App Info)-এ যান।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightVioletText
                        )
                        Text(
                            text = "ধাপ ২: উপরের ডান কোণায় থাকা তিন ডট (⋮) মেনুতে ট্যাপ করুন।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightVioletText
                        )
                        Text(
                            text = "ধাপ ৩: \"Allow restricted settings\" অপশনে ট্যাপ করে অনুমতি দিন, তারপর আবার এখানে এসে ${permissionType.titleBn} চালু করুন।",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightVioletText
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        OutlinedButton(
                            onClick = onOpenAppInfo,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, LightVioletBorder.copy(alpha = 0.42f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("restricted_open_app_info_btn")
                        ) {
                            Text(
                                text = "সরাসরি SafeFocus App Info খুলুন",
                                style = MaterialTheme.typography.labelMedium,
                                color = PureLavenderWhite
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onGoToSettings,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NormalVioletPrimary,
                    contentColor = PureLavenderWhite
                ),
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("restricted_go_settings_btn")
            ) {
                Text("এখনই সেটিংসে যান", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .minimumInteractiveComponentSize()
                    .testTag("restricted_later_btn")
            ) {
                Text("পরে করব", style = MaterialTheme.typography.labelLarge, color = LightVioletMuted)
            }
        }
    )
}

@Composable
fun TransparentUpdateLinksDialog(
    onDismiss: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DeepVioletSurface,
        title = {
            Text(
                text = "নতুন ভার্সন ও স্বচ্ছ আপডেট",
                style = MaterialTheme.typography.titleLarge,
                color = PureLavenderWhite
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "SafeFocus বর্তমান ভার্সন: v1.0 (অফলাইন এডিশন)। আমরা কখনোই আপনার অনুমতি ছাড়া গোপনে কোনো পরিবর্তন করি না। নতুন আপডেট এলে নিচের অফিশিয়াল লিংক দেখতে পাবেন:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightVioletText
                )
                OutlinedButton(
                    onClick = { runCatching { uriHandler.openUri("https://t.me/safefocus_official") } },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LightVioletBorder.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Telegram চ্যানেল: t.me/safefocus_official",
                        fontFamily = NumericLatinFamily,
                        fontSize = 13.sp,
                        color = PureLavenderWhite
                    )
                }
                OutlinedButton(
                    onClick = { runCatching { uriHandler.openUri("https://github.com/safefocus-app/safefocus-android") } },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, LightVioletBorder.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "GitHub রিলিজ: github.com/safefocus-app",
                        fontFamily = NumericLatinFamily,
                        fontSize = 13.sp,
                        color = PureLavenderWhite
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = NormalVioletPrimary,
                    contentColor = PureLavenderWhite
                )
            ) {
                Text("ঠিক আছে", style = MaterialTheme.typography.labelLarge)
            }
        }
    )
}
