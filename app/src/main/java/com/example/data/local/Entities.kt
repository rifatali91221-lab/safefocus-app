package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "controlled_apps")
data class ControlledAppEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val reelsLabel: String,
    val isBlocked: Boolean = false,
    val blockReelsAndShorts: Boolean = true,
    val blockDurationMinutes: Int = 30,
    val blockExpiresAtMillis: Long = 0L,
    val renewExpiresAtMillis: Long = 0L,
    val sortOrder: Int = 0
)

@Entity(tableName = "focus_settings")
data class FocusSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val isMasterActive: Boolean = false,
    val maxRenewMinutes: Int = 5,
    val useMathQuestions: Boolean = true,
    val useBookQuestions: Boolean = true,
    val soundEnabled: Boolean = true,
    val accessibilityBridgeEnabled: Boolean = false,
    val overlayBubbleEnabled: Boolean = false,
    val backgroundServiceEnabled: Boolean = false,
    val blockedAttemptsCount: Int = 0
)

@Entity(tableName = "motivational_quotes")
data class MotivationalQuoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "study_questions")
data class StudyQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val question: String,
    val answer: String,
    val subjectNote: String = "বইয়ের নোটস",
    val isDefault: Boolean = false
)
