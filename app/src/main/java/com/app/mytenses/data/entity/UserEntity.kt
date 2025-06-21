package com.app.mytenses.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val username: String,
    val name: String,
    val email: String,
    val bio: String,
    val completedLessons: List<String>
)