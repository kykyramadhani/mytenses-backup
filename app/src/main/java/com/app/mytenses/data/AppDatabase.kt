package com.app.mytenses.data

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.app.mytenses.model.CompletedLesson
import com.app.mytenses.model.Converters
import com.app.mytenses.model.QuizScore
import com.app.mytenses.model.UserData

@Database(entities = [UserData::class, CompletedLesson::class, QuizScore::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mytenses_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}