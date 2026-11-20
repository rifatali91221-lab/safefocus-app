package com.example.data.repository

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import androidx.core.content.ContextCompat
import com.example.data.bridge.SafeFocusBridgeBus
import com.example.data.local.ControlledAppEntity
import com.example.data.local.FocusSettingsEntity
import com.example.data.local.MotivationalQuoteEntity
import com.example.data.local.SafeFocusDao
import com.example.data.local.StudyQuestionEntity
import com.example.service.FloatingBubbleService
import com.example.service.SafeFocusAccessibilityService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SafeFocusRepository(
    private val context: Context,
    private val dao: SafeFocusDao
) {
    val appsFlow: Flow<List<ControlledAppEntity>> = dao.observeApps()
    val settingsFlow: Flow<FocusSettingsEntity> = dao.observeSettings().map {
        it ?: FocusSettingsEntity()
    }
    val quotesFlow: Flow<List<MotivationalQuoteEntity>> = dao.observeQuotes()
    val questionsFlow: Flow<List<StudyQuestionEntity>> = dao.observeQuestions()

    suspend fun ensureSeeded() {
        if (dao.getAppsSnapshot().isEmpty()) {
            dao.upsertApps(
                listOf(
                    ControlledAppEntity(
                        packageName = SafeFocusBridgeBus.PKG_YOUTUBE,
                        displayName = "YouTube",
                        reelsLabel = "Shorts",
                        isBlocked = false,
                        blockReelsAndShorts = true,
                        blockDurationMinutes = 30,
                        sortOrder = 0
                    ),
                    ControlledAppEntity(
                        packageName = SafeFocusBridgeBus.PKG_FACEBOOK,
                        displayName = "Facebook",
                        reelsLabel = "Reels",
                        isBlocked = true,
                        blockReelsAndShorts = true,
                        blockDurationMinutes = 45,
                        blockExpiresAtMillis = System.currentTimeMillis() + 45 * 60_000L,
                        sortOrder = 1
                    ),
                    ControlledAppEntity(
                        packageName = SafeFocusBridgeBus.PKG_INSTAGRAM,
                        displayName = "Instagram",
                        reelsLabel = "Reels",
                        isBlocked = true,
                        blockReelsAndShorts = true,
                        blockDurationMinutes = 60,
                        blockExpiresAtMillis = System.currentTimeMillis() + 60 * 60_000L,
                        sortOrder = 2
                    )
                )
            )
        }

        if (dao.getSettingsSnapshot() == null) {
            dao.upsertSettings(FocusSettingsEntity())
        }

        if (dao.getQuestionsSnapshot().isEmpty()) {
            dao.insertQuestions(
                listOf(
                    StudyQuestionEntity(
                        question = "বাংলাদেশের জাতীয় কবির নাম কী?",
                        answer = "কাজী নজরুল ইসলাম",
                        subjectNote = "বাংলা সাহিত্য",
                        isDefault = true
                    ),
                    StudyQuestionEntity(
                        question = "পানির রাসায়নিক সংকেত কী?",
                        answer = "H2O",
                        subjectNote = "সাধারণ বিজ্ঞান",
                        isDefault = true
                    ),
                    StudyQuestionEntity(
                        question = "এক সমকোণ সমান কত ডিগ্রি?",
                        answer = "90",
                        subjectNote = "জ্যামিতি নোটস",
                        isDefault = true
                    )
                )
            )
        }
    }

    suspend fun ensureDefaultQuotesIfEmpty(existingCount: Int) {
        if (existingCount == 0) {
            dao.insertQuotes(
                listOf(
                    MotivationalQuoteEntity(text = "তুমি করতে পারবা", isDefault = true),
                    MotivationalQuoteEntity(text = "এখন তোমার পড়ার সময়", isDefault = true),
                    MotivationalQuoteEntity(text = "মনোযোগী হও", isDefault = true),
                    MotivationalQuoteEntity(text = "অনেক হয়েছে", isDefault = true),
                    MotivationalQuoteEntity(text = "বাবার কষ্ট দেখো", isDefault = true)
                )
            )
        }
    }

    suspend fun getSettings(): FocusSettingsEntity {
        return dao.getSettingsSnapshot() ?: FocusSettingsEntity().also { dao.upsertSettings(it) }
    }

    suspend fun getApps(): List<ControlledAppEntity> = dao.getAppsSnapshot()

    suspend fun getQuestions(): List<StudyQuestionEntity> = dao.getQuestionsSnapshot()

    suspend fun setMasterActive(active: Boolean) {
        val current = getSettings()
        dao.upsertSettings(current.copy(isMasterActive = active))
    }

    suspend fun setSoundEnabled(enabled: Boolean) {
        val current = getSettings()
        dao.upsertSettings(current.copy(soundEnabled = enabled))
    }

    suspend fun setMaxRenewMinutes(minutes: Int) {
        val clamped = minutes.coerceIn(1, 5)
        val current = getSettings()
        dao.upsertSettings(current.copy(maxRenewMinutes = clamped))
    }

    suspend fun setQuestionRules(useMath: Boolean, useBook: Boolean) {
        val current = getSettings()
        // অন্তত একটি প্রশ্নের ধরন চালু রাখতে হবে
        val safeMath = if (!useMath && !useBook) true else useMath
        dao.upsertSettings(
            current.copy(useMathQuestions = safeMath, useBookQuestions = useBook)
        )
    }

    suspend fun updateBridgePermissionFlags(
        accessibility: Boolean? = null,
        overlay: Boolean? = null,
        background: Boolean? = null
    ) {
        val current = getSettings()
        val updated = current.copy(
            accessibilityBridgeEnabled = accessibility ?: current.accessibilityBridgeEnabled,
            overlayBubbleEnabled = overlay ?: current.overlayBubbleEnabled,
            backgroundServiceEnabled = background ?: current.backgroundServiceEnabled
        )
        dao.upsertSettings(updated)
        syncNativeOverlayService(updated.overlayBubbleEnabled || updated.backgroundServiceEnabled)
    }

    suspend fun incrementBlockedAttempts() {
        val current = getSettings()
        dao.upsertSettings(current.copy(blockedAttemptsCount = current.blockedAttemptsCount + 1))
    }

    suspend fun toggleReelsBlock(packageName: String, enabled: Boolean) {
        val app = dao.getApp(packageName) ?: return
        dao.updateApp(app.copy(blockReelsAndShorts = enabled))
    }

    suspend fun setBlockDurationMinutes(packageName: String, minutes: Int) {
        val app = dao.getApp(packageName) ?: return
        val newExpiry = if (app.isBlocked) {
            System.currentTimeMillis() + minutes * 60_000L
        } else {
            app.blockExpiresAtMillis
        }
        dao.updateApp(
            app.copy(
                blockDurationMinutes = minutes.coerceIn(5, 240),
                blockExpiresAtMillis = newExpiry
            )
        )
    }

    suspend fun blockAppNow(packageName: String) {
        val app = dao.getApp(packageName) ?: return
        val expiresAt = System.currentTimeMillis() + app.blockDurationMinutes * 60_000L
        dao.updateApp(
            app.copy(
                isBlocked = true,
                blockExpiresAtMillis = expiresAt,
                renewExpiresAtMillis = 0L
            )
        )
    }

    suspend fun unblockAppDirect(packageName: String) {
        val app = dao.getApp(packageName) ?: return
        dao.updateApp(
            app.copy(
                isBlocked = false,
                blockExpiresAtMillis = 0L,
                renewExpiresAtMillis = 0L
            )
        )
    }

    suspend fun grantTemporaryRenew(packageName: String, minutes: Int) {
        val app = dao.getApp(packageName) ?: return
        val clampedMinutes = minutes.coerceIn(1, 5)
        val renewExpiresAt = System.currentTimeMillis() + clampedMinutes * 60_000L
        dao.updateApp(
            app.copy(
                isBlocked = false,
                blockExpiresAtMillis = 0L,
                renewExpiresAtMillis = renewExpiresAt
            )
        )
    }

    suspend fun processTimersTick(nowMillis: Long) {
        val apps = dao.getAppsSnapshot()
        for (app in apps) {
            if (app.isBlocked && app.blockExpiresAtMillis > 0L && nowMillis >= app.blockExpiresAtMillis) {
                // নির্ধারিত ব্লক সময় শেষ — অটোমেটিক আনব্লক
                dao.updateApp(
                    app.copy(
                        isBlocked = false,
                        blockExpiresAtMillis = 0L
                    )
                )
            } else if (!app.isBlocked && app.renewExpiresAtMillis > 0L && nowMillis >= app.renewExpiresAtMillis) {
                // ৫ মিনিটের রিনিউ সময় শেষ — আবার স্বয়ংক্রিয়ভাবে ব্লক হবে
                val nextBlockExpiry = nowMillis + app.blockDurationMinutes * 60_000L
                dao.updateApp(
                    app.copy(
                        isBlocked = true,
                        renewExpiresAtMillis = 0L,
                        blockExpiresAtMillis = nextBlockExpiry
                    )
                )
            }
        }
    }

    suspend fun addQuote(text: String) {
        val trimmed = text.trim()
        if (trimmed.isNotEmpty()) {
            dao.insertQuote(MotivationalQuoteEntity(text = trimmed, isDefault = false))
        }
    }

    suspend fun deleteQuote(id: Int) {
        dao.deleteQuoteById(id)
    }

    suspend fun addStudyQuestion(question: String, answer: String, subjectNote: String) {
        val q = question.trim()
        val a = answer.trim()
        if (q.isNotEmpty() && a.isNotEmpty()) {
            dao.insertQuestion(
                StudyQuestionEntity(
                    question = q,
                    answer = a,
                    subjectNote = subjectNote.trim().ifEmpty { "বইয়ের নোটস" },
                    isDefault = false
                )
            )
        }
    }

    suspend fun deleteStudyQuestion(id: Int) {
        dao.deleteQuestionById(id)
    }

    fun isSystemAccessibilityEnabled(): Boolean {
        val expectedComponent = ComponentName(context, SafeFocusAccessibilityService::class.java)
        val enabledServices = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val splitter = TextUtils.SimpleStringSplitter(':')
        splitter.setString(enabledServices)
        while (splitter.hasNext()) {
            val componentName = ComponentName.unflattenFromString(splitter.next())
            if (componentName == expectedComponent) return true
        }
        return false
    }

    fun isSystemOverlayGranted(): Boolean {
        return Settings.canDrawOverlays(context)
    }

    fun openSystemAccessibilitySettings() {
        runCatching {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun openSystemOverlaySettings() {
        runCatching {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                android.net.Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun openAppInfoSettings() {
        runCatching {
            val intent = Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                android.net.Uri.parse("package:${context.packageName}")
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun syncNativeOverlayService(shouldRun: Boolean) {
        val canDraw = isSystemOverlayGranted()
        val intent = Intent(context, FloatingBubbleService::class.java)
        if (shouldRun && canDraw) {
            runCatching {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    ContextCompat.startForegroundService(context, intent)
                } else {
                    context.startService(intent)
                }
            }
        } else if (!shouldRun) {
            runCatching { context.stopService(intent) }
        }
    }
}
