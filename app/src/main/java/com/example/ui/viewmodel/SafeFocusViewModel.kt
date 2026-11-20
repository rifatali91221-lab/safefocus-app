package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.CalmSoundPlayer
import com.example.data.bridge.SafeFocusBridgeBus
import com.example.data.local.ControlledAppEntity
import com.example.data.local.FocusSettingsEntity
import com.example.data.local.MotivationalQuoteEntity
import com.example.data.local.SafeFocusDatabase
import com.example.data.local.StudyQuestionEntity
import com.example.data.repository.SafeFocusRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class SafeFocusTab {
    HOME, CONTROL, SETTINGS
}

enum class PermissionType(
    val titleBn: String,
    val explanationBn: String,
    val privacyGuaranteeBn: String
) {
    ACCESSIBILITY(
        titleBn = "অ্যাক্সেসিবিলিটি সার্ভিস (Accessibility Service)",
        explanationBn = "আপনি যখন YouTube Shorts, Facebook Reels বা Instagram Reels-এ ঢুকবেন কিংবা ব্লক করা অ্যাপ খুলবেন, তখন শুধুমাত্র সেই মুহূর্তে ১৫ সেকেন্ডের সচেতন বিরতি দেখাতে এই পারমিশনটি প্রয়োজন।",
        privacyGuaranteeBn = "নিরাপত্তা নিশ্চয়তা: এটি কখনোই আপনার চ্যাট, পাসওয়ার্ড, কী-বোর্ড লেখা বা ব্যক্তিগত কোনো তথ্য পড়ে না বা সংরক্ষণ করে না।"
    ),
    OVERLAY(
        titleBn = "পপআপ উইন্ডো (ওভারলে)",
        explanationBn = "অন্য অ্যাপের ওপর ছোট ফ্লোটিং SafeFocus আইকন এবং ১৫ সেকেন্ডের মনোযোগ পপআপ দেখানোর জন্য পপআপ উইন্ডো (Display over other apps) পারমিশনটি প্রয়োজন।",
        privacyGuaranteeBn = "নিরাপত্তা নিশ্চয়তা: আপনি চাইলে ফ্লোটিং আইকনে চাপ দিয়ে যেকোনো সময় এটি পজ বা বন্ধ করতে পারবেন।"
    ),
    BACKGROUND(
        titleBn = "ব্যাকগ্রাউন্ড সার্ভিস ও নোটিফিকেশন",
        explanationBn = "আপনার নির্ধারিত ব্লক টাইমার এবং ৫ মিনিটের রিনিউ অটো-লক টাইমার অ্যাপ বন্ধ থাকলেও নিখুঁতভাবে চালু রাখতে ব্যাকগ্রাউন্ড সার্ভিস প্রয়োজন।",
        privacyGuaranteeBn = "নিরাপত্তা নিশ্চয়তা: এটি সম্পূর্ণ অফলাইনে আপনার ফোনের ভেতরেই কাজ করে, কোনো ব্যাকগ্রাউন্ড ডেটা পাঠায় না।"
    )
}

enum class PauseStage {
    COUNTDOWN_15S,
    CHOICE_AFTER_15S,
    QUIZ_CHALLENGE
}

sealed interface ChallengeQuiz {
    val labelBn: String
    val promptBn: String
    val hintBn: String
    fun isCorrect(input: String): Boolean

    data class MathQuiz(
        val expression: String,
        val expectedAnswer: Int
    ) : ChallengeQuiz {
        override val labelBn: String = "গণিতের প্রশ্ন"
        override val promptBn: String = "নিচের হিসাবটি সমাধান করুন: $expression = ?"
        override val hintBn: String = "সঠিক সংখ্যা লিখুন"
        override fun isCorrect(input: String): Boolean {
            return input.trim().toIntOrNull() == expectedAnswer
        }
    }

    data class BookQuiz(
        val studyQuestion: StudyQuestionEntity
    ) : ChallengeQuiz {
        override val labelBn: String = "বইয়ের ডাটাশিট / নোটস (${studyQuestion.subjectNote})"
        override val promptBn: String = studyQuestion.question
        override val hintBn: String = "আপনার নোটস অনুযায়ী উত্তর লিখুন"
        override fun isCorrect(input: String): Boolean {
            return input.trim().equals(studyQuestion.answer.trim(), ignoreCase = true)
        }
    }
}

data class PauseShieldState(
    val packageName: String,
    val appName: String,
    val triggerReason: String,
    val secondsRemaining: Int = 15,
    val stage: PauseStage = PauseStage.COUNTDOWN_15S,
    val activeQuiz: ChallengeQuiz? = null,
    val userAnswer: String = "",
    val errorText: String? = null
)

data class SafeFocusUiState(
    val selectedTab: SafeFocusTab = SafeFocusTab.HOME,
    val apps: List<ControlledAppEntity> = emptyList(),
    val settings: FocusSettingsEntity = FocusSettingsEntity(),
    val quotes: List<MotivationalQuoteEntity> = emptyList(),
    val questions: List<StudyQuestionEntity> = emptyList(),
    val nowMillis: Long = System.currentTimeMillis(),
    val isRefreshing: Boolean = false,
    val systemAccessibilityGranted: Boolean = false,
    val systemOverlayGranted: Boolean = false,
    val pendingPermissionDialog: PermissionType? = null,
    val pendingRestrictedGuideFor: PermissionType? = null,
    val pauseShield: PauseShieldState? = null,
    val showUpdatePopup: Boolean = false,
    val isBubbleMenuExpanded: Boolean = false,
    val statusFeedback: String? = null
)

class SafeFocusViewModel(application: Application) : AndroidViewModel(application) {

    private val database = SafeFocusDatabase.getInstance(application)
    private val repository = SafeFocusRepository(application, database.safeFocusDao())
    private val soundPlayer = CalmSoundPlayer()

    private val _uiState = MutableStateFlow(SafeFocusUiState())
    val uiState: StateFlow<SafeFocusUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
            refreshSystemPermissions()
        }

        viewModelScope.launch {
            combine(
                repository.appsFlow,
                repository.settingsFlow,
                repository.quotesFlow,
                repository.questionsFlow
            ) { apps, settings, quotes, questions ->
                Quad(apps, settings, quotes, questions)
            }.collect { (apps, settings, quotes, questions) ->
                if (quotes.isEmpty()) {
                    repository.ensureDefaultQuotesIfEmpty(0)
                }
                SafeFocusBridgeBus.updateStatus(
                    isMasterActive = settings.isMasterActive,
                    overlayEnabled = settings.overlayBubbleEnabled,
                    blockedAppsCount = apps.count { it.isBlocked }
                )
                _uiState.update {
                    it.copy(
                        apps = apps,
                        settings = settings,
                        quotes = quotes,
                        questions = questions
                    )
                }
            }
        }

        // লাইভ টাইমার ও অটো-আনব্লক / অটো-রিলক টিকার (প্রতি ১ সেকেন্ডে)
        viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                repository.processTimersTick(now)
                _uiState.update { it.copy(nowMillis = now) }
                delay(1000L)
            }
        }

        // ১৫ সেকেন্ডের কাউন্টডাউন টিকার
        viewModelScope.launch {
            while (isActive) {
                delay(1000L)
                val shield = _uiState.value.pauseShield
                if (shield != null && shield.stage == PauseStage.COUNTDOWN_15S) {
                    val nextSec = (shield.secondsRemaining - 1).coerceAtLeast(0)
                    if (nextSec == 0) {
                        soundPlayer.stop()
                        _uiState.update {
                            it.copy(
                                pauseShield = shield.copy(
                                    secondsRemaining = 0,
                                    stage = PauseStage.CHOICE_AFTER_15S
                                )
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(pauseShield = shield.copy(secondsRemaining = nextSec))
                        }
                    }
                }
            }
        }

        // নেটিভ AccessibilityService বা Overlay থেকে আসা ব্রিজ ইভেন্ট শোনা
        viewModelScope.launch {
            SafeFocusBridgeBus.triggerEvents.collect { event ->
                triggerPauseShield(
                    packageName = event.packageName,
                    appName = event.appName,
                    reason = event.reason
                )
            }
        }

        viewModelScope.launch {
            SafeFocusBridgeBus.toggleMasterRequests.collect {
                toggleMasterActive()
            }
        }
    }

    fun selectTab(tab: SafeFocusTab) {
        _uiState.update { it.copy(selectedTab = tab, isBubbleMenuExpanded = false) }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val now = System.currentTimeMillis()
            repository.processTimersTick(now)
            refreshSystemPermissions()
            delay(550L)
            _uiState.update {
                it.copy(
                    isRefreshing = false,
                    nowMillis = System.currentTimeMillis(),
                    statusFeedback = "সব টাইমার ও স্ট্যাটাস রিফ্রেশ হয়েছে"
                )
            }
        }
    }

    fun refreshSystemPermissions() {
        val accGranted = repository.isSystemAccessibilityEnabled()
        val overlayGranted = repository.isSystemOverlayGranted()
        _uiState.update {
            it.copy(
                systemAccessibilityGranted = accGranted,
                systemOverlayGranted = overlayGranted
            )
        }
    }

    fun toggleMasterActive() {
        viewModelScope.launch {
            val next = !_uiState.value.settings.isMasterActive
            repository.setMasterActive(next)
            _uiState.update {
                it.copy(
                    statusFeedback = if (next) {
                        "SafeFocus সক্রিয় হয়েছে — ডিস্ট্র্যাকশন সুরক্ষা চালু"
                    } else {
                        "SafeFocus এখন বিরতিতে রয়েছে"
                    }
                )
            }
        }
    }

    fun onQuickToggleBlockFromHome(app: ControlledAppEntity) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val isCurrentlyRenewed = !app.isBlocked && app.renewExpiresAtMillis > now
            if (app.isBlocked || isCurrentlyRenewed) {
                // সিস্টেম সক্রিয় থাকলে আনব্লক করতে চাইলে ১৫ সেকেন্ডের বিরতি ও প্রশ্ন ধাপ আসবে
                if (_uiState.value.settings.isMasterActive) {
                    triggerPauseShield(
                        packageName = app.packageName,
                        appName = app.displayName,
                        reason = "${app.displayName} আনব্লক বা রিনিউ করার অনুরোধ"
                    )
                } else {
                    repository.unblockAppDirect(app.packageName)
                    _uiState.update {
                        it.copy(statusFeedback = "${app.displayName} আনব্লক করা হয়েছে")
                    }
                }
            } else {
                repository.blockAppNow(app.packageName)
                _uiState.update {
                    it.copy(
                        statusFeedback = "${app.displayName} ${app.blockDurationMinutes} মিনিটের জন্য ব্লক হয়েছে"
                    )
                }
            }
        }
    }

    fun toggleReelsForApp(packageName: String, enabled: Boolean) {
        viewModelScope.launch {
            repository.toggleReelsBlock(packageName, enabled)
        }
    }

    fun updateBlockDuration(packageName: String, minutes: Int) {
        viewModelScope.launch {
            repository.setBlockDurationMinutes(packageName, minutes)
        }
    }

    fun blockAppNow(packageName: String) {
        viewModelScope.launch {
            repository.blockAppNow(packageName)
            _uiState.update { it.copy(statusFeedback = "অ্যাপটি তাৎক্ষণিকভাবে ব্লক করা হয়েছে") }
        }
    }

    fun simulateBridgeIntercept(app: ControlledAppEntity, isReelsTrigger: Boolean) {
        val reason = if (isReelsTrigger) {
            "সিমুলেশন ব্রিজ: ${app.displayName} ${app.reelsLabel} খোলা হয়েছে"
        } else {
            "সিমুলেশন ব্রিজ: লক করা ${app.displayName} অ্যাপে প্রবেশের চেষ্টা"
        }
        SafeFocusBridgeBus.emitTrigger(
            com.example.data.bridge.BridgeTriggerEvent(
                packageName = app.packageName,
                appName = app.displayName,
                reason = reason,
                isReelsOrShorts = isReelsTrigger
            )
        )
    }

    fun triggerPauseShield(packageName: String, appName: String, reason: String) {
        viewModelScope.launch {
            repository.incrementBlockedAttempts()
            if (_uiState.value.settings.soundEnabled) {
                soundPlayer.start(viewModelScope)
            }
            _uiState.update {
                it.copy(
                    isBubbleMenuExpanded = false,
                    pauseShield = PauseShieldState(
                        packageName = packageName,
                        appName = appName,
                        triggerReason = reason,
                        secondsRemaining = 15,
                        stage = PauseStage.COUNTDOWN_15S
                    )
                )
            }
        }
    }

    fun togglePauseSound() {
        viewModelScope.launch {
            val next = !_uiState.value.settings.soundEnabled
            repository.setSoundEnabled(next)
            if (_uiState.value.pauseShield?.stage == PauseStage.COUNTDOWN_15S) {
                if (next) soundPlayer.start(viewModelScope) else soundPlayer.stop()
            }
        }
    }

    fun returnToStudyFromPause() {
        soundPlayer.stop()
        val pkg = _uiState.value.pauseShield?.packageName
        viewModelScope.launch {
            if (pkg != null) {
                val app = _uiState.value.apps.firstOrNull { it.packageName == pkg }
                if (app != null && !app.isBlocked) {
                    repository.blockAppNow(pkg)
                }
            }
            _uiState.update {
                it.copy(
                    pauseShield = null,
                    statusFeedback = "চমৎকার সিদ্ধান্ত! পড়াশোনা ও জরুরি কাজে ফিরে যান"
                )
            }
        }
    }

    fun proceedToRenewQuiz() {
        val shield = _uiState.value.pauseShield ?: return
        val quiz = generateChallengeQuiz()
        _uiState.update {
            it.copy(
                pauseShield = shield.copy(
                    stage = PauseStage.QUIZ_CHALLENGE,
                    activeQuiz = quiz,
                    userAnswer = "",
                    errorText = null
                )
            )
        }
    }

    fun updateQuizAnswer(input: String) {
        val shield = _uiState.value.pauseShield ?: return
        _uiState.update {
            it.copy(
                pauseShield = shield.copy(userAnswer = input, errorText = null)
            )
        }
    }

    fun switchQuizType() {
        val shield = _uiState.value.pauseShield ?: return
        val nextQuiz = when (shield.activeQuiz) {
            is ChallengeQuiz.MathQuiz -> {
                val questions = _uiState.value.questions
                if (questions.isNotEmpty() && _uiState.value.settings.useBookQuestions) {
                    ChallengeQuiz.BookQuiz(questions.random())
                } else {
                    createMathQuiz()
                }
            }
            is ChallengeQuiz.BookQuiz, null -> createMathQuiz()
        }
        _uiState.update {
            it.copy(
                pauseShield = shield.copy(
                    activeQuiz = nextQuiz,
                    userAnswer = "",
                    errorText = null
                )
            )
        }
    }

    fun submitQuizAnswer() {
        val shield = _uiState.value.pauseShield ?: return
        val quiz = shield.activeQuiz ?: return
        if (quiz.isCorrect(shield.userAnswer)) {
            val renewLimit = _uiState.value.settings.maxRenewMinutes.coerceIn(1, 5)
            viewModelScope.launch {
                repository.grantTemporaryRenew(shield.packageName, renewLimit)
                soundPlayer.stop()
                _uiState.update {
                    it.copy(
                        pauseShield = null,
                        statusFeedback = "${shield.appName} সর্বোচ্চ $renewLimit মিনিটের জন্য রিনিউ হয়েছে — সময় শেষে আবার বন্ধ হবে"
                    )
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    pauseShield = shield.copy(
                        errorText = "উত্তরটি সঠিক হয়নি। মনোযোগ দিয়ে আবার চেষ্টা করুন!"
                    )
                )
            }
        }
    }

    fun onPermissionSwitchClicked(type: PermissionType, targetEnabled: Boolean) {
        if (targetEnabled) {
            // চালু করার আগে বাংলা কারণ পপআপ দেখাতে হবে
            _uiState.update { it.copy(pendingPermissionDialog = type) }
        } else {
            viewModelScope.launch {
                when (type) {
                    PermissionType.ACCESSIBILITY -> repository.updateBridgePermissionFlags(accessibility = false)
                    PermissionType.OVERLAY -> repository.updateBridgePermissionFlags(overlay = false)
                    PermissionType.BACKGROUND -> repository.updateBridgePermissionFlags(background = false)
                }
            }
        }
    }

    fun dismissPermissionExplanation() {
        _uiState.update { it.copy(pendingPermissionDialog = null) }
    }

    fun confirmPermissionUnderstood() {
        val type = _uiState.value.pendingPermissionDialog ?: return
        if (type == PermissionType.BACKGROUND) {
            _uiState.update {
                it.copy(
                    pendingPermissionDialog = null,
                    statusFeedback = "ব্যাকগ্রাউন্ড সার্ভিস চালু হয়েছে"
                )
            }
            viewModelScope.launch {
                repository.updateBridgePermissionFlags(background = true)
            }
        } else {
            // অ্যাক্সেসিবিলিটি বা ওভারলের ক্ষেত্রে সরাসরি সেটিংসে না পাঠিয়ে Restricted Setting গাইড বক্স দেখানো হবে
            _uiState.update {
                it.copy(
                    pendingPermissionDialog = null,
                    pendingRestrictedGuideFor = type
                )
            }
        }
    }

    fun dismissRestrictedGuide() {
        _uiState.update { it.copy(pendingRestrictedGuideFor = null) }
    }

    fun openAppInfoFromRestrictedGuide() {
        repository.openAppInfoSettings()
    }

    fun proceedFromRestrictedGuideToSettings() {
        val type = _uiState.value.pendingRestrictedGuideFor ?: return
        _uiState.update { it.copy(pendingRestrictedGuideFor = null) }
        viewModelScope.launch {
            when (type) {
                PermissionType.ACCESSIBILITY -> {
                    repository.updateBridgePermissionFlags(accessibility = true)
                    repository.openSystemAccessibilitySettings()
                }
                PermissionType.OVERLAY -> {
                    repository.updateBridgePermissionFlags(overlay = true)
                    repository.openSystemOverlaySettings()
                }
                PermissionType.BACKGROUND -> {
                    repository.updateBridgePermissionFlags(background = true)
                }
            }
        }
    }

    fun updateMaxRenewMinutes(minutes: Int) {
        viewModelScope.launch { repository.setMaxRenewMinutes(minutes) }
    }

    fun toggleMathQuestions(enabled: Boolean) {
        viewModelScope.launch {
            repository.setQuestionRules(enabled, _uiState.value.settings.useBookQuestions)
        }
    }

    fun toggleBookQuestions(enabled: Boolean) {
        viewModelScope.launch {
            repository.setQuestionRules(_uiState.value.settings.useMathQuestions, enabled)
        }
    }

    fun addCustomQuote(text: String) {
        viewModelScope.launch {
            repository.addQuote(text)
            _uiState.update { it.copy(statusFeedback = "নতুন মোটিভেশনাল উক্তি যোগ হয়েছে") }
        }
    }

    fun deleteQuote(id: Int) {
        viewModelScope.launch { repository.deleteQuote(id) }
    }

    fun addStudyQuestion(question: String, answer: String, subjectNote: String) {
        viewModelScope.launch {
            repository.addStudyQuestion(question, answer, subjectNote)
            _uiState.update { it.copy(statusFeedback = "বইয়ের ডাটাশিট প্রশ্ন সংরক্ষিত হয়েছে") }
        }
    }

    fun deleteStudyQuestion(id: Int) {
        viewModelScope.launch { repository.deleteStudyQuestion(id) }
    }

    fun toggleUpdatePopup(show: Boolean) {
        _uiState.update { it.copy(showUpdatePopup = show) }
    }

    fun toggleBubbleMenu() {
        _uiState.update { it.copy(isBubbleMenuExpanded = !it.isBubbleMenuExpanded) }
    }

    fun clearStatusFeedback() {
        _uiState.update { it.copy(statusFeedback = null) }
    }

    private fun generateChallengeQuiz(): ChallengeQuiz {
        val settings = _uiState.value.settings
        val questions = _uiState.value.questions
        val canBook = settings.useBookQuestions && questions.isNotEmpty()
        val canMath = settings.useMathQuestions
        return when {
            canBook && canMath -> if (Random.nextBoolean()) ChallengeQuiz.BookQuiz(questions.random()) else createMathQuiz()
            canBook -> ChallengeQuiz.BookQuiz(questions.random())
            else -> createMathQuiz()
        }
    }

    private fun createMathQuiz(): ChallengeQuiz.MathQuiz {
        val a = Random.nextInt(12, 48)
        val b = Random.nextInt(7, 29)
        val c = Random.nextInt(2, 9)
        return when (Random.nextInt(3)) {
            0 -> ChallengeQuiz.MathQuiz("$a + $b", a + b)
            1 -> ChallengeQuiz.MathQuiz("${a + b} - $b", a)
            else -> ChallengeQuiz.MathQuiz("$b × $c", b * c)
        }
    }

    override fun onCleared() {
        super.onCleared()
        soundPlayer.stop()
    }

    private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
