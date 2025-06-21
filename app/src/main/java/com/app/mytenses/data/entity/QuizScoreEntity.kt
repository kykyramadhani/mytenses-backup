package com.app.mytenses.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "quiz_scores",
    foreignKeys = [ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["username"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["userId"])]
)
data class QuizScoreEntity(
    @PrimaryKey val scoreId: String,
    val userId: String, // Foreign key ke UserEntity
    val quizId: String,
    val dateTaken: String,
    val score: Int
)