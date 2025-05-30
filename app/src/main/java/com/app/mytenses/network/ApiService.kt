package com.app.mytenses.network

import com.app.mytenses.model.Lesson
import com.app.mytenses.model.UserData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface ApiService {
    @GET("api/lessons")
    suspend fun getLessons(): Response<Map<String, Lesson>>

    @GET("api/users/{username}")
    suspend fun getUserData(@Path("username") username: String): Response<UserData>
}