package com.app.mytenses.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "lesson_progress",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["username"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"])]
)
data class LessonProgressEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String, // Foreign key ke UserEntity
    val lessonId: String,
    val progress: Int,
    val status: String
)