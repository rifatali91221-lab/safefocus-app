package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SafeFocusDao {

    @Query("SELECT * FROM controlled_apps ORDER BY sortOrder ASC")
    fun observeApps(): Flow<List<ControlledAppEntity>>

    @Query("SELECT * FROM controlled_apps ORDER BY sortOrder ASC")
    suspend fun getAppsSnapshot(): List<ControlledAppEntity>

    @Query("SELECT * FROM controlled_apps WHERE packageName = :packageName LIMIT 1")
    suspend fun getApp(packageName: String): ControlledAppEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApps(apps: List<ControlledAppEntity>)

    @Update
    suspend fun updateApp(app: ControlledAppEntity)

    @Query("SELECT * FROM focus_settings WHERE id = 1 LIMIT 1")
    fun observeSettings(): Flow<FocusSettingsEntity?>

    @Query("SELECT * FROM focus_settings WHERE id = 1 LIMIT 1")
    suspend fun getSettingsSnapshot(): FocusSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSettings(settings: FocusSettingsEntity)

    @Query("SELECT * FROM motivational_quotes ORDER BY id DESC")
    fun observeQuotes(): Flow<List<MotivationalQuoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuotes(quotes: List<MotivationalQuoteEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: MotivationalQuoteEntity)

    @Query("DELETE FROM motivational_quotes WHERE id = :id")
    suspend fun deleteQuoteById(id: Int)

    @Query("SELECT * FROM study_questions ORDER BY id DESC")
    fun observeQuestions(): Flow<List<StudyQuestionEntity>>

    @Query("SELECT * FROM study_questions ORDER BY id DESC")
    suspend fun getQuestionsSnapshot(): List<StudyQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<StudyQuestionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: StudyQuestionEntity)

    @Query("DELETE FROM study_questions WHERE id = :id")
    suspend fun deleteQuestionById(id: Int)
}
