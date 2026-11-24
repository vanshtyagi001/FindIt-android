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
    val item: ItemDto
)

data class SearchLostResponse(
    @SerializedName("search_description") val searchDescription: String,
    @SerializedName("ai_warning") val aiWarning: String? = null,
    @SerializedName("match_count") val matchCount: Int = 0,
    val matches: List<MatchResultDto> = emptyList()
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

data class MessageResponse(
    val message: String? = null,
    val error: String? = null
)

data class ApiErrorResponse(
    val error: String? = null
)
