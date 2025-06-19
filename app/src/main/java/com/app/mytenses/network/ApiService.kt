package com.app.mytenses.network

import com.app.mytenses.model.Lesson
import com.app.mytenses.model.LessonProgress
import com.app.mytenses.model.LessonProgressResponse

import com.app.mytenses.model.MaterialsResponse
import com.app.mytenses.model.Question
import com.app.mytenses.model.UserData
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.PUT
import retrofit2.http.Body

interface ApiService {
    @GET("api/lessons")
    suspend fun getLessons(): Response<Map<String, Lesson>>

    @GET("api/materials")
    suspend fun getMaterials(): Response<MaterialsResponse>

    @GET("api/users/{username}")
    suspend fun getUserData(@Path("username") username: String): Response<UserData>

    @GET("api/questions")
    suspend fun getQuestions(): Response<Map<String, List<Question>>>

    @PUT("api/users/{username}")
    suspend fun updateUserData(
        @Path("username") username: String,
        @Body data: Map<String, String?>
    ): Response<Unit>

    @PUT("api/users/{username}/lessons/{lessonId}")
    suspend fun updateLessonProgress(
        @Path("username") username: String,
        @Path("lessonId") lessonId: String,
        @Body progress: LessonProgress
    ): Response<Unit>

    @GET("api/users/{username}/lessons")
    suspend fun getLessonProgress(@Path("username") username: String): Response<LessonProgressResponse>

}