package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.service.SafeFocusAccessibilityService
import com.example.ui.components.FloatingInAppBubble
import com.example.ui.components.LiquidGlassBottomBar
import com.example.ui.components.SafeFocusTopHeader
import com.example.ui.screens.ControlScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PauseShieldOverlay
import com.example.ui.screens.PermissionExplanationModal
import com.example.ui.screens.RestrictedSettingGuideModal
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DeepVioletBg
import com.example.ui.theme.DeepVioletSurface
import com.example.ui.theme.LightVioletBorder
import com.example.ui.theme.NormalVioletPrimary
import com.example.ui.theme.PureLavenderWhite
import com.example.ui.theme.SafeFocusTheme
import com.example.ui.viewmodel.PermissionType
import com.example.ui.viewmodel.SafeFocusTab
import com.example.ui.viewmodel.SafeFocusViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: SafeFocusViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleAccessibilityIntent(intent)

        setContent {
            SafeFocusTheme {
                SafeFocusRoot(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAccessibilityIntent(intent)
    }

    private fun handleAccessibilityIntent(intent: Intent?) {
        val pkg = intent?.getStringExtra(SafeFocusAccessibilityService.EXTRA_TRIGGER_PACKAGE) ?: return
        val name = intent.getStringExtra(SafeFocusAccessibilityService.EXTRA_TRIGGER_APP_NAME) ?: "App"
        val reason = intent.getStringExtra(SafeFocusAccessibilityService.EXTRA_TRIGGER_REASON)
            ?: "$name শিল্ড সক্রিয়"
        viewModel.triggerPauseShield(pkg, name, reason)
        intent.removeExtra(SafeFocusAccessibilityService.EXTRA_TRIGGER_PACKAGE)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeFocusRoot(viewModel: SafeFocusViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val bridgeStatus by com.example.data.bridge.SafeFocusBridgeBus.status.collectAsStateWithLifecycle()
    val pullState = rememberPullToRefreshState()

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.refreshSystemPermissions()
    }

    // সাব-স্ক্রিন ও পপআপের জন্য BackHandler
    BackHandler(enabled = state.pauseShield != null || state.selectedTab != SafeFocusTab.HOME) {
        if (state.pauseShield != null) {
            viewModel.returnToStudyFromPause()
        } else if (state.selectedTab != SafeFocusTab.HOME) {
            viewModel.selectTab(SafeFocusTab.HOME)
        }
    }

    LaunchedEffect(state.statusFeedback) {
        if (state.statusFeedback != null) {
            delay(2800L)
            viewModel.clearStatusFeedback()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DeepVioletBg,
                        DeepVioletSurface.copy(alpha = 0.92f),
                        DeepVioletBg
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // ফিক্সড টপ গ্লাস হেডার
            SafeFocusTopHeader(isActive = state.settings.isMasterActive)

            // পুল-টু-রিফ্রেশ সহ মূল কন্টেন্ট এরিয়া
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { viewModel.refreshAll() },
                state = pullState,
                indicator = {
                    PullToRefreshDefaults.Indicator(
                        state = pullState,
                        isRefreshing = state.isRefreshing,
                        containerColor = DeepVioletSurface,
                        color = LightVioletBorder,
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (state.selectedTab) {
                    SafeFocusTab.HOME -> {
                        HomeScreen(
                            isMasterActive = state.settings.isMasterActive,
                            apps = state.apps,
                            nowMillis = state.nowMillis,
                            onToggleMaster = { viewModel.toggleMasterActive() },
                            onQuickToggleApp = { app -> viewModel.onQuickToggleBlockFromHome(app) }
                        )
                    }

                    SafeFocusTab.CONTROL -> {
                        ControlScreen(
                            apps = state.apps,
                            nowMillis = state.nowMillis,
                            blockedAttemptsCount = state.settings.blockedAttemptsCount,
                            lastBridgeEvent = bridgeStatus.lastDetectedEvent,
                            onToggleReels = { pkg, enabled -> viewModel.toggleReelsForApp(pkg, enabled) },
                            onUpdateDuration = { pkg, mins -> viewModel.updateBlockDuration(pkg, mins) },
                            onBlockNow = { pkg -> viewModel.blockAppNow(pkg) },
                            onTriggerRenewOrAttempt = { app ->
                                viewModel.simulateBridgeIntercept(app, isReelsTrigger = false)
                            },
                            onSimulateReelsTrigger = { app ->
                                viewModel.simulateBridgeIntercept(app, isReelsTrigger = true)
                            }
                        )
                    }

                    SafeFocusTab.SETTINGS -> {
                        SettingsScreen(
                            settings = state.settings,
                            quotes = state.quotes,
                            questions = state.questions,
                            systemAccessibilityGranted = state.systemAccessibilityGranted,
                            systemOverlayGranted = state.systemOverlayGranted,
                            showUpdatePopup = state.showUpdatePopup,
                            onPermissionSwitch = { type, enabled ->
                                viewModel.onPermissionSwitchClicked(type, enabled)
                            },
                            onUpdateMaxRenewMinutes = { mins -> viewModel.updateMaxRenewMinutes(mins) },
                            onToggleMathQuestions = { viewModel.toggleMathQuestions(it) },
                            onToggleBookQuestions = { viewModel.toggleBookQuestions(it) },
                            onAddQuote = { viewModel.addCustomQuote(it) },
                            onDeleteQuote = { viewModel.deleteQuote(it) },
                            onAddStudyQuestion = { q, a, s -> viewModel.addStudyQuestion(q, a, s) },
                            onDeleteStudyQuestion = { viewModel.deleteStudyQuestion(it) },
                            onToggleUpdatePopup = { viewModel.toggleUpdatePopup(it) }
                        )
                    }
                }
            }
        }

        // ভাসমান লিকুইড গ্লাস বটম নেভিগেশন বার
        LiquidGlassBottomBar(
            selectedTab = state.selectedTab,
            onSelectTab = { viewModel.selectTab(it) },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // ওভারলে / পপআপ উইন্ডো চালু থাকলে ফ্লোটিং পপআপ আইকন
        if (state.settings.overlayBubbleEnabled || state.systemOverlayGranted) {
            FloatingInAppBubble(
                isMasterActive = state.settings.isMasterActive,
                isExpanded = state.isBubbleMenuExpanded,
                onToggleExpand = { viewModel.toggleBubbleMenu() },
                onToggleMaster = { viewModel.toggleMasterActive() },
                onTriggerQuickPauseTest = {
                    val sampleApp = state.apps.firstOrNull()
                    if (sampleApp != null) {
                        viewModel.simulateBridgeIntercept(sampleApp, isReelsTrigger = true)
                    }
                },
                onHideBubble = {
                    viewModel.onPermissionSwitchClicked(PermissionType.OVERLAY, false)
                }
            )
        }

        // ছোট স্ট্যাটাস ফিডব্যাক টোস্ট
        AnimatedVisibility(
            visible = state.statusFeedback != null && state.pauseShield == null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 104.dp, start = 24.dp, end = 24.dp)
        ) {
            state.statusFeedback?.let { msg ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(NormalVioletPrimary.copy(alpha = 0.92f))
                        .border(1.dp, LightVioletBorder.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.labelMedium,
                        color = PureLavenderWhite
                    )
                }
            }
        }

        // বাংলা পারমিশন কারণ পপআপ ("বুঝেছি" বাটন সহ)
        state.pendingPermissionDialog?.let { permType ->
            PermissionExplanationModal(
                permissionType = permType,
                onDismiss = { viewModel.dismissPermissionExplanation() },
                onConfirmUnderstood = { viewModel.confirmPermissionUnderstood() }
            )
        }

        // "Android-এ 'Restricted setting' আসলে কী করবেন?" গাইড বক্স পপআপ
        state.pendingRestrictedGuideFor?.let { permType ->
            RestrictedSettingGuideModal(
                permissionType = permType,
                onOpenAppInfo = { viewModel.openAppInfoFromRestrictedGuide() },
                onGoToSettings = { viewModel.proceedFromRestrictedGuideToSettings() },
                onDismiss = { viewModel.dismissRestrictedGuide() }
            )
        }

        // ১৫ সেকেন্ডের বিরতি ও কুইজ ওভারলে স্ক্রিন
        AnimatedVisibility(
            visible = state.pauseShield != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            state.pauseShield?.let { shield ->
                PauseShieldOverlay(
                    shield = shield,
                    quotes = state.quotes,
                    soundEnabled = state.settings.soundEnabled,
                    maxRenewMinutes = state.settings.maxRenewMinutes,
                    onToggleSound = { viewModel.togglePauseSound() },
                    onReturnToStudy = { viewModel.returnToStudyFromPause() },
                    onProceedToQuiz = { viewModel.proceedToRenewQuiz() },
                    onAnswerChange = { viewModel.updateQuizAnswer(it) },
                    onSwitchQuizType = { viewModel.switchQuizType() },
                    onSubmitAnswer = { viewModel.submitQuizAnswer() }
                )
            }
        }
    }
}
