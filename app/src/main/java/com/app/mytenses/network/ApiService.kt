package com.app.mytenses.network

import com.app.mytenses.model.ApiResponse
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
    suspend fun getLessons(): Response<ApiResponse>

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


}