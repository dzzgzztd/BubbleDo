package com.example.bubbledo.cloud

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

data class DriveFileList(
    val files: List<DriveFile>
)

data class DriveFile(
    val id: String,
    val name: String
)

interface DriveApi {

    @GET("files")
    suspend fun listFiles(
        @Query("spaces") spaces: String = "appDataFolder",
        @Query("q") query: String? = null,
        @Query("fields") fields: String = "files(id, name)"
    ): DriveFileList

    @GET("files/{fileId}")
    @Streaming
    suspend fun downloadFile(@Path("fileId") fileId: String): Response<ResponseBody>

    @Multipart
    @POST("upload/drive/v3/files?uploadType=multipart")
    suspend fun uploadFile(
        @Part("metadata") metadata: RequestBody,
        @Part file: MultipartBody.Part
    ): DriveFile

    @PATCH("files/{fileId}")
    suspend fun updateFile(
        @Path("fileId") fileId: String,
        @Body fileContent: RequestBody
    ): DriveFile
}
