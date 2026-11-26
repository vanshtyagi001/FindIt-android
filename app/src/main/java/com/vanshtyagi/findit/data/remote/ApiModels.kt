package com.vanshtyagi.findit.data.remote

import com.google.gson.annotations.SerializedName

data class HealthResponse(
    val status: String,
    val service: String,
    @SerializedName("item_types") val itemTypes: List<String> = emptyList(),
    val thresholds: ThresholdsDto? = null,
    val timestamp: String? = null
)

data class ThresholdsDto(
    val metadata: Double,
    val description: Double,
    val image: Double
)

data class ItemDto(
    val id: Int,
    val status: String,
    @SerializedName("item_type") val itemType: String,
    val color: String? = null,
    val brand: String? = null,
    val location: String,
    @SerializedName("image_filename") val imageFilename: String,
    @SerializedName("image_url") val imageUrl: String? = null,
    @SerializedName("ai_description") val aiDescription: String? = null,
    @SerializedName("contact_info") val contactInfo: String,
    val timestamp: String? = null
)

data class ItemsListResponse(
    val items: List<ItemDto> = emptyList(),
    val total: Int = 0,
    val limit: Int = 50,
    val offset: Int = 0
)

data class ReportFoundResponse(
    val message: String,
    @SerializedName("ai_warning") val aiWarning: String? = null,
    val item: ItemDto,
    @SerializedName("lost_match_count") val lostMatchCount: Int = 0,
    @SerializedName("lost_matches") val lostMatches: List<MatchResultDto> = emptyList()
)

data class SearchLostResponse(
    @SerializedName("search_description") val searchDescription: String,
    @SerializedName("ai_warning") val aiWarning: String? = null,
    @SerializedName("match_count") val matchCount: Int = 0,
    val matches: List<MatchResultDto> = emptyList(),
    @SerializedName("lost_item") val lostItem: ItemDto? = null
)

data class MatchResultDto(
    val confidence: Double,
    val scores: MatchScoresDto,
    val item: ItemDto
)

data class MatchScoresDto(
    val metadata: Double,
    val description: Double,
    val image: Double,
    val confidence: Double
)

data class AdminLoginRequest(
    val username: String,
    val password: String
)

data class AdminLoginResponse(
    val token: String,
    @SerializedName("expires_in") val expiresIn: Int
)

data class UserDto(
    val id: Int,
    val name: String,
    val email: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val city: String? = null,
    @SerializedName("alternate_contact") val alternateContact: String? = null,
    @SerializedName("profile_complete") val profileComplete: Boolean = false
)

data class UpdateProfileRequest(
    val name: String,
    val phone: String,
    val address: String,
    val city: String? = null,
    @SerializedName("alternate_contact") val alternateContact: String? = null
)

data class ProfileResponse(
    val message: String? = null,
    val user: UserDto
)

data class UserLoginRequest(
    val email: String,
    val password: String
)

data class UserRegisterRequest(
    val name: String,
    val email: String,
    val password: String
)

data class UserAuthResponse(
    val token: String,
    @SerializedName("expires_in") val expiresIn: Int,
    val user: UserDto
)

data class GoogleLoginRequest(
    @SerializedName("id_token") val idToken: String
)

data class MessageResponse(
    val message: String? = null,
    val error: String? = null
)

data class NotificationDto(
    val id: Int,
    @SerializedName("user_id") val userId: Int,
    @SerializedName("found_item_id") val foundItemId: Int? = null,
    @SerializedName("lost_item_id") val lostItemId: Int? = null,
    val title: String,
    val message: String,
    val confidence: Double? = null,
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("found_item") val foundItem: ItemDto? = null
)

data class NotificationsResponse(
    val notifications: List<NotificationDto> = emptyList(),
    @SerializedName("unread_count") val unreadCount: Int = 0
)

data class ApiErrorResponse(
    val error: String? = null
)
