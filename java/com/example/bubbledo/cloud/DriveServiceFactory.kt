package com.example.bubbledo.cloud

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object DriveServiceFactory {

    fun create(token: String): DriveApi {
        val authInterceptor = Interceptor { chain ->
            val newRequest = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
            chain.proceed(newRequest)
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/drive/v3/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(DriveApi::class.java)
    }

    fun createMetadataJson(name: String): RequestBody {
        val json = """{"name": "$name", "parents": ["appDataFolder"]}"""
        return json.toRequestBody("application/json; charset=utf-8".toMediaType())
    }

    fun createFilePart(data: ByteArray, filename: String): MultipartBody.Part {
        val requestBody = data.toRequestBody("application/json".toMediaType())
        return MultipartBody.Part.createFormData("file", filename, requestBody)
    }

    fun createJsonRequestBody(json: String): RequestBody {
        return json.toRequestBody("application/json".toMediaType())
    }
}
