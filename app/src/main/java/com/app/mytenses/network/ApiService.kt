package com.app.mytenses.network

import com.app.mytenses.model.ApiResponse
import com.app.mytenses.model.Lesson
import com.app.mytenses.model.MaterialsResponse
import com.app.mytenses.model.Question
import com.app.mytenses.model.UserData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @GET("api/lessons")
    suspend fun getLessons(): Response<Lesson>

    @GET("api/materials")
    suspend fun getMaterials(): Response<MaterialsResponse>

    @GET("api/users/{username}")
    suspend fun getUserData(@Path("username") username: String): Response<UserData>

    @GET("api/questions")
    suspend fun getQuestions(): Response<Map<String, List<Question>>>


}