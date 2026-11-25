package com.vanshtyagi.findit.data.repository

import com.google.gson.Gson
import com.vanshtyagi.findit.data.remote.AdminLoginRequest
import com.vanshtyagi.findit.data.remote.AdminLoginResponse
import com.vanshtyagi.findit.data.remote.ApiErrorResponse
import com.vanshtyagi.findit.data.remote.FindItApiService
import com.vanshtyagi.findit.data.remote.GoogleLoginRequest
import com.vanshtyagi.findit.data.remote.HealthResponse
import com.vanshtyagi.findit.data.remote.ItemsListResponse
import com.vanshtyagi.findit.data.remote.MessageResponse
import com.vanshtyagi.findit.data.remote.ProfileResponse
import com.vanshtyagi.findit.data.remote.ReportFoundResponse
import com.vanshtyagi.findit.data.remote.RetrofitClient
import com.vanshtyagi.findit.data.remote.SearchLostResponse
import com.vanshtyagi.findit.data.remote.UpdateProfileRequest
import com.vanshtyagi.findit.data.remote.UserAuthResponse
import com.vanshtyagi.findit.data.remote.UserLoginRequest
import com.vanshtyagi.findit.data.remote.UserRegisterRequest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File

class FindItRepository(
    private val api: FindItApiService = RetrofitClient.api
) {
    private val gson = Gson()
    private val textPlain = "text/plain".toMediaType()
    private val imageJpeg = "image/jpeg".toMediaType()

    suspend fun checkHealth(): Result<HealthResponse> = safeCall {
        api.getHealth()
    }

    suspend fun listItems(status: String? = "found", limit: Int = 50): Result<ItemsListResponse> = safeCall {
        api.listItems(status = status, limit = limit)
    }

    suspend fun reportFound(
        imageFile: File,
        itemType: String,
        location: String,
        contactInfo: String,
        color: String?,
        brand: String?,
        description: String?
    ): Result<ReportFoundResponse> = safeCall {
        val imagePart = MultipartBody.Part.createFormData(
            "item_image",
            imageFile.name,
            imageFile.asRequestBody(imageJpeg)
        )
        api.reportFound(
            itemImage = imagePart,
            itemType = itemType.trim().toRequestBody(textPlain),
            location = location.trim().toRequestBody(textPlain),
            contactInfo = contactInfo.trim().toRequestBody(textPlain),
            color = color?.trim()?.takeIf { it.isNotEmpty() }?.toRequestBody(textPlain),
            brand = brand?.trim()?.takeIf { it.isNotEmpty() }?.toRequestBody(textPlain),
            description = description?.trim()?.takeIf { it.isNotEmpty() }?.toRequestBody(textPlain)
        )
    }

    suspend fun searchLost(
        imageFile: File,
        itemType: String,
        location: String,
        color: String?,
        brand: String?,
        description: String?
    ): Result<SearchLostResponse> = safeCall {
        val imagePart = MultipartBody.Part.createFormData(
            "item_image",
            imageFile.name,
            imageFile.asRequestBody(imageJpeg)
        )
        api.searchLost(
            itemImage = imagePart,
            itemType = itemType.trim().toRequestBody(textPlain),
            location = location.trim().toRequestBody(textPlain),
            color = color?.trim()?.takeIf { it.isNotEmpty() }?.toRequestBody(textPlain),
            brand = brand?.trim()?.takeIf { it.isNotEmpty() }?.toRequestBody(textPlain),
            description = description?.trim()?.takeIf { it.isNotEmpty() }?.toRequestBody(textPlain)
        )
    }

    suspend fun userRegister(name: String, email: String, password: String): Result<UserAuthResponse> = safeCall {
        api.userRegister(UserRegisterRequest(name.trim(), email.trim(), password))
    }

    suspend fun userLogin(email: String, password: String): Result<UserAuthResponse> = safeCall {
        api.userLogin(UserLoginRequest(email.trim(), password))
    }

    suspend fun googleLogin(idToken: String): Result<UserAuthResponse> = safeCall {
        api.googleLogin(GoogleLoginRequest(idToken))
    }

    suspend fun updateProfile(
        token: String,
        name: String,
        phone: String,
        address: String,
        city: String,
        alternateContact: String
    ): Result<ProfileResponse> = safeCall {
        api.updateProfile(
            "Bearer $token",
            UpdateProfileRequest(
                name = name.trim(),
                phone = phone.trim(),
                address = address.trim(),
                city = city.trim().ifEmpty { null },
                alternateContact = alternateContact.trim().ifEmpty { null }
            )
        )
    }

    suspend fun adminLogin(username: String, password: String): Result<AdminLoginResponse> = safeCall {
        api.adminLogin(AdminLoginRequest(username.trim(), password))
    }

    suspend fun deleteItem(token: String, itemId: Int): Result<MessageResponse> = safeCall {
        api.deleteItem("Bearer $token", itemId)
    }

    suspend fun clearAllItems(token: String): Result<MessageResponse> = safeCall {
        api.clearAllItems("Bearer $token")
    }

    private suspend fun <T> safeCall(block: suspend () -> Response<T>): Result<T> {
        return try {
            val response = block()
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    Result.success(body)
                } else {
                    Result.failure(IllegalStateException("Empty response from server."))
                }
            } else {
                val errorJson = response.errorBody()?.string()
                val parsedMsg = try {
                    gson.fromJson(errorJson, ApiErrorResponse::class.java)?.error
                } catch (_: Exception) {
                    null
                }
                Result.failure(
                    Exception(parsedMsg ?: "Request failed (${response.code()})")
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
