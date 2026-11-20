package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        ControlledAppEntity::class,
        FocusSettingsEntity::class,
        MotivationalQuoteEntity::class,
        StudyQuestionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class SafeFocusDatabase : RoomDatabase() {
    abstract fun safeFocusDao(): SafeFocusDao

    companion object {
        @Volatile
        private var INSTANCE: SafeFocusDatabase? = null

        fun getInstance(context: Context): SafeFocusDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    SafeFocusDatabase::class.java,
                    "safefocus_offline.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
