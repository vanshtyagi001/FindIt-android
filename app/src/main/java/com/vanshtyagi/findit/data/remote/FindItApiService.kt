package com.vanshtyagi.findit.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface FindItApiService {

    @GET("health")
    suspend fun getHealth(): Response<HealthResponse>

    @GET("items")
    suspend fun listItems(
        @Query("status") status: String? = null,
        @Query("item_type") itemType: String? = null,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0
    ): Response<ItemsListResponse>

    @Multipart
    @POST("report-found")
    suspend fun reportFound(
        @Part itemImage: MultipartBody.Part,
        @Part("item_type") itemType: RequestBody,
        @Part("location") location: RequestBody,
        @Part("contact_info") contactInfo: RequestBody,
        @Part("color") color: RequestBody? = null,
        @Part("brand") brand: RequestBody? = null
    ): Response<ReportFoundResponse>

    @Multipart
    @POST("search-lost")
    suspend fun searchLost(
        @Part itemImage: MultipartBody.Part,
        @Part("item_type") itemType: RequestBody,
        @Part("location") location: RequestBody,
        @Part("color") color: RequestBody? = null,
        @Part("brand") brand: RequestBody? = null
    ): Response<SearchLostResponse>

    @POST("admin/login")
    suspend fun adminLogin(
        @Body body: AdminLoginRequest
    ): Response<AdminLoginResponse>

    @DELETE("admin/items/{itemId}")
    suspend fun deleteItem(
        @Header("Authorization") bearerToken: String,
        @Path("itemId") itemId: Int
    ): Response<MessageResponse>

    @POST("admin/clear-all")
    suspend fun clearAllItems(
        @Header("Authorization") bearerToken: String
    ): Response<MessageResponse>
}
