package com.example.data.remote

import com.example.data.model.KattoJobRequest
import com.example.data.model.KattoJobResponse
import com.example.data.model.KattoJobStatusResponse
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface KattoApiService {
    @POST("api/v1/jobs")
    suspend fun createJob(
        @Header("Authorization") authHeader: String,
        @Body request: KattoJobRequest
    ): Response<KattoJobResponse>

    @GET("api/v1/jobs/{id}")
    suspend fun getJobStatus(
        @Header("Authorization") authHeader: String,
        @Path("id") jobId: String
    ): Response<KattoJobStatusResponse>

    companion object {
        private const val BASE_URL = "https://katto.tech/"

        fun create(): KattoApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create())
                .build()
                .create(KattoApiService::class.java)
        }
    }
}
