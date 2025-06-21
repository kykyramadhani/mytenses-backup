package com.app.mytenses.data.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.app.mytenses.data.entity.UserEntity
import com.app.mytenses.data.dao.UserDao
import com.app.mytenses.data.entity.LessonProgressEntity
import com.app.mytenses.data.entity.QuizScoreEntity
import com.app.mytenses.utils.Converters // Pastikan impor ini ada

@Database(entities = [UserEntity::class, LessonProgressEntity::class, QuizScoreEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class) // Daftarkan konverter di sini
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
                Log.d("AppDatabase", "Database created at: ${context.filesDir.parent}/databases/mytenses_database")
                INSTANCE = instance
                instance
            }
        }
    }
}