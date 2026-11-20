package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MotivationalQuoteEntity
import com.example.ui.components.VioletGlassCard
import com.example.ui.theme.ActiveGlowViolet
import com.example.ui.theme.DeepVioletBg
import com.example.ui.theme.DeepVioletSurface
import com.example.ui.theme.LightVioletBorder
import com.example.ui.theme.LightVioletMuted
import com.example.ui.theme.LightVioletText
import com.example.ui.theme.NormalVioletAccent
import com.example.ui.theme.NormalVioletPrimary
import com.example.ui.theme.PureLavenderWhite
import com.example.ui.theme.tabular
import com.example.ui.viewmodel.ChallengeQuiz
import com.example.ui.viewmodel.PauseShieldState
import com.example.ui.viewmodel.PauseStage

@Composable
fun PauseShieldOverlay(
    shield: PauseShieldState,
    quotes: List<MotivationalQuoteEntity>,
    soundEnabled: Boolean,
    maxRenewMinutes: Int,
    onToggleSound: () -> Unit,
    onReturnToStudy: () -> Unit,
    onProceedToQuiz: () -> Unit,
    onAnswerChange: (String) -> Unit,
    onSwitchQuizType: () -> Unit,
    onSubmitAnswer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeQuoteList = quotes.ifEmpty {
        listOf(
            MotivationalQuoteEntity(text = "তুমি করতে পারবা"),
            MotivationalQuoteEntity(text = "এখন তোমার পড়ার সময়"),
            MotivationalQuoteEntity(text = "মনোযোগী হও"),
            MotivationalQuoteEntity(text = "অনেক হয়েছে"),
            MotivationalQuoteEntity(text = "বাবার কষ্ট দেখো")
        )
    }
    val quoteIndex = ((15 - shield.secondsRemaining) / 3).coerceIn(0, activeQuoteList.lastIndex) % activeQuoteList.size
    val currentQuote = activeQuoteList[quoteIndex].text

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DeepVioletBg,
                        DeepVioletSurface,
                        DeepVioletBg
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 420.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // শীর্ষ সচেতনতা ব্যাজ ও মিউট বাটন
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = NormalVioletPrimary.copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, LightVioletBorder.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Shield,
                            contentDescription = null,
                            tint = LightVioletText,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = shield.triggerReason,
                            style = MaterialTheme.typography.labelMedium,
                            color = PureLavenderWhite
                        )
                    }
                }

                OutlinedButton(
                    onClick = onToggleSound,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(1.dp, LightVioletBorder.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("pause_sound_toggle")
                ) {
                    Icon(
                        imageVector = if (soundEnabled) {
                            Icons.AutoMirrored.Rounded.VolumeUp
                        } else {
                            Icons.AutoMirrored.Rounded.VolumeOff
                        },
                        contentDescription = if (soundEnabled) "সাউন্ড মিউট করুন" else "সাউন্ড চালু করুন",
                        tint = LightVioletText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (soundEnabled) "শান্ত সাউন্ড" else "মিউট",
                        style = MaterialTheme.typography.labelMedium,
                        color = LightVioletText
                    )
                }
            }

            // মাঝের মূল ধাপ (১৫ সেকেন্ড কাউন্টডাউন -> পছন্দ -> প্রশ্ন)
            when (shield.stage) {
                PauseStage.COUNTDOWN_15S -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier.size(180.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val progress = shield.secondsRemaining / 15f
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawArc(
                                    color = LightVioletBorder.copy(alpha = 0.16f),
                                    startAngle = -90f,
                                    sweepAngle = 360f,
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                )
                                drawArc(
                                    brush = Brush.sweepGradient(
                                        colors = listOf(NormalVioletPrimary, LightVioletBorder, ActiveGlowViolet)
                                    ),
                                    startAngle = -90f,
                                    sweepAngle = 360f * progress,
                                    useCenter = false,
                                    style = Stroke(width = 10.dp.toPx(), cap = StrokeCap.Round)
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = shield.secondsRemaining.toString().padStart(2, '0'),
                                    style = MaterialTheme.typography.displayLarge.tabular(),
                                    color = PureLavenderWhite
                                )
                                Text(
                                    text = "সেকেন্ড বিরতি",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LightVioletText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(34.dp))

                        // স্মুথ অ্যানিমেটেড মোটিভেশনাল উক্তি
                        AnimatedContent(
                            targetState = currentQuote,
                            transitionSpec = {
                                (fadeIn(tween(500)) + slideInVertically { it / 4 }) togetherWith
                                    (fadeOut(tween(400)) + slideOutVertically { -it / 4 })
                            },
                            label = "animated_quote"
                        ) { quoteText ->
                            VioletGlassCard(
                                cornerRadius = 24.dp,
                                borderAlpha = 0.36f,
                                contentPadding = 22.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "“$quoteText”",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = PureLavenderWhite,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "একটি গভীর শ্বাস নিন এবং নিজের লক্ষ্য মনে করুন",
                            style = MaterialTheme.typography.bodyMedium,
                            color = LightVioletText.copy(alpha = 0.78f),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                PauseStage.CHOICE_AFTER_15S -> {
                    VioletGlassCard(
                        cornerRadius = 26.dp,
                        borderAlpha = 0.4f,
                        contentPadding = 22.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(NormalVioletPrimary.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.School,
                                    contentDescription = null,
                                    tint = LightVioletText,
                                    modifier = Modifier.size(30.dp)
                                )
                            }

                            Text(
                                text = "১৫ সেকেন্ডের বিরতি সম্পন্ন!",
                                style = MaterialTheme.typography.titleLarge,
                                color = PureLavenderWhite,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "এখন আপনার সিদ্ধান্ত: পড়াশোনা বা জরুরি কাজে ফিরে যাবেন, নাকি প্রশ্নের উত্তর দিয়ে সর্বোচ্চ $maxRenewMinutes মিনিটের জন্য ${shield.appName} রিনিউ করবেন?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = LightVioletText,
                                textAlign = TextAlign.Center
                            )

                            Button(
                                onClick = onReturnToStudy,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NormalVioletPrimary,
                                    contentColor = PureLavenderWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("pause_return_study_btn")
                            ) {
                                Text(
                                    text = "পড়াশোনা ও কাজে ফিরি",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            OutlinedButton(
                                onClick = onProceedToQuiz,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, LightVioletBorder.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("pause_proceed_quiz_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LockClock,
                                    contentDescription = null,
                                    tint = LightVioletText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "প্রশ্ন সমাধান করে রিনিউ ($maxRenewMinutes মি.)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = LightVioletText
                                )
                            }
                        }
                    }
                }

                PauseStage.QUIZ_CHALLENGE -> {
                    val quiz = shield.activeQuiz
                    VioletGlassCard(
                        cornerRadius = 26.dp,
                        borderAlpha = 0.42f,
                        contentPadding = 22.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (quiz != null) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.MenuBook,
                                        contentDescription = null,
                                        tint = LightVioletBorder,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = quiz.labelBn,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = LightVioletBorder
                                    )
                                }

                                TextButton(onClick = onSwitchQuizType) {
                                    Icon(
                                        imageVector = Icons.Rounded.SwapHoriz,
                                        contentDescription = "প্রশ্নের ধরন বদলান",
                                        tint = LightVioletText,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "বদলান",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = LightVioletText
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = quiz.promptBn,
                                style = MaterialTheme.typography.titleLarge,
                                color = PureLavenderWhite
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedTextField(
                                value = shield.userAnswer,
                                onValueChange = onAnswerChange,
                                label = { Text(quiz.hintBn) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = if (quiz is ChallengeQuiz.MathQuiz) {
                                        KeyboardType.Number
                                    } else {
                                        KeyboardType.Text
                                    }
                                ),
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NormalVioletAccent,
                                    unfocusedBorderColor = LightVioletBorder.copy(alpha = 0.35f),
                                    focusedTextColor = PureLavenderWhite,
                                    unfocusedTextColor = PureLavenderWhite,
                                    focusedLabelColor = LightVioletBorder,
                                    unfocusedLabelColor = LightVioletMuted,
                                    cursorColor = LightVioletBorder
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("quiz_answer_input")
                            )

                            if (shield.errorText != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = shield.errorText,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LightVioletBorder
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = onSubmitAnswer,
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NormalVioletPrimary,
                                    contentColor = PureLavenderWhite
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .minimumInteractiveComponentSize()
                                    .testTag("quiz_submit_btn")
                            ) {
                                Text(
                                    text = "যাচাই করে সর্বোচ্চ $maxRenewMinutes মিনিট চালু করুন",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            TextButton(
                                onClick = onReturnToStudy,
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = "বাতিল করে পড়াশোনায় ফিরে যাই",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = LightVioletMuted
                                )
                            }
                        }
                    }
                }
            }

            // নিচের শান্ত ফোকাস বার্তা
            Text(
                text = "সময় শেষে অ্যাপটি স্বয়ংক্রিয়ভাবে আবার লক হয়ে যাবে • SafeFocus",
                style = MaterialTheme.typography.labelSmall,
                color = LightVioletText.copy(alpha = 0.56f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}
