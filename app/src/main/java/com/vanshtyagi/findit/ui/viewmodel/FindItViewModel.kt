package com.vanshtyagi.findit.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.vanshtyagi.findit.BuildConfig
import com.vanshtyagi.findit.data.local.LocalCacheManager
import com.vanshtyagi.findit.data.remote.ItemDto
import com.vanshtyagi.findit.data.remote.MatchResultDto
import com.vanshtyagi.findit.data.remote.NotificationDto
import com.vanshtyagi.findit.data.remote.SearchLostResponse
import com.vanshtyagi.findit.data.remote.UserDto
import com.vanshtyagi.findit.data.repository.FindItRepository
import com.vanshtyagi.findit.data.util.ImageUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

enum class ServerStatus { WAKING, ONLINE, OFFLINE }

data class FindItUiState(
    val serverStatus: ServerStatus = ServerStatus.WAKING,
    val itemTypes: List<String> = DEFAULT_ITEM_TYPES,
    val recentFoundItems: List<ItemDto> = emptyList(),
    val allItems: List<ItemDto> = emptyList(),
    val allItemsTotal: Int = 0,
    val isLoadingItems: Boolean = false,
    val isLoadingAllItems: Boolean = false,
    val isSyncing: Boolean = false,
    val isSubmitting: Boolean = false,
    val loadingMessage: String = "",
    val reportSuccessMessage: String? = null,
    val searchResponse: SearchLostResponse? = null,
    val errorMessage: String? = null,
    val userToken: String? = null,
    val currentUser: UserDto? = null,
    val adminToken: String? = null,
    val adminStatusMessage: String? = null,
    val notifications: List<NotificationDto> = emptyList(),
    val unreadNotificationCount: Int = 0,
    val isLoadingNotifications: Boolean = false,
    val popupMatchNotification: NotificationDto? = null,
    val finderMatchedLostItem: MatchResultDto? = null
) {
    companion object {
        val DEFAULT_ITEM_TYPES = listOf(
            "Electronics",
            "Keys & Wallet",
            "Pets",
            "Bags & Luggage",
            "Documents",
            "Clothing",
            "Jewelry & Watch",
            "Other"
        )
    }
}

class FindItViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository: FindItRepository = FindItRepository()
    private val cacheManager: LocalCacheManager = LocalCacheManager(application)

    private val _uiState = MutableStateFlow(FindItUiState())
    val uiState: StateFlow<FindItUiState> = _uiState.asStateFlow()
    private val shownPopupNotificationIds = mutableSetOf<Int>()

    init {
        hydrateFromCache()
        warmUpAndLoad()
        startNotificationPolling()
    }

    private fun startNotificationPolling() {
        viewModelScope.launch {
            while (isActive) {
                delay(30_000L)
                if (_uiState.value.userToken != null) {
                    loadNotifications(showPopupIfUnread = true)
                }
            }
        }
    }

    private fun hydrateFromCache() {
        val cachedItems = cacheManager.getCachedItems()
        val cachedSession = cacheManager.getUserSession()
        if (cachedItems.isNotEmpty() || cachedSession != null) {
            _uiState.update { state ->
                state.copy(
                    recentFoundItems = cachedItems,
                    userToken = cachedSession?.first,
                    currentUser = cachedSession?.second
                )
            }
        }
    }

    fun warmUpAndLoad() {
        // Fetch items immediately in parallel with health check (Stale-While-Revalidate).
        loadRecentFoundItems()
        if (_uiState.value.userToken != null) {
            loadNotifications(showPopupIfUnread = true)
        }
        viewModelScope.launch {
            if (_uiState.value.serverStatus != ServerStatus.ONLINE) {
                _uiState.update { it.copy(serverStatus = ServerStatus.WAKING) }
            }
            repository.checkHealth()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            serverStatus = ServerStatus.ONLINE,
                            itemTypes = FindItUiState.DEFAULT_ITEM_TYPES
                        )
                    }
                }
                .onFailure {
                    if (_uiState.value.recentFoundItems.isEmpty()) {
                        _uiState.update { it.copy(serverStatus = ServerStatus.OFFLINE) }
                    }
                }
        }
    }

    fun loadRecentFoundItems() {
        viewModelScope.launch {
            val hasCached = _uiState.value.recentFoundItems.isNotEmpty()
            _uiState.update {
                it.copy(
                    isLoadingItems = !hasCached,
                    isSyncing = hasCached
                )
            }
            repository.listItems(status = "found", limit = 25)
                .onSuccess { resp ->
                    _uiState.update {
                        it.copy(
                            recentFoundItems = resp.items,
                            isLoadingItems = false,
                            isSyncing = false,
                            serverStatus = ServerStatus.ONLINE
                        )
                    }
                    withContext(Dispatchers.IO) {
                        cacheManager.saveCachedItems(resp.items)
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingItems = false,
                            isSyncing = false,
                            errorMessage = if (hasCached) null else err.message
                        )
                    }
                }
        }
    }

    fun loadAllItems(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            val currentAll = _uiState.value.allItems
            val seed = if (currentAll.isEmpty()) _uiState.value.recentFoundItems else currentAll
            _uiState.update {
                it.copy(
                    allItems = seed,
                    isLoadingAllItems = seed.isEmpty() || forceRefresh
                )
            }
            repository.listItems(status = null, limit = 100)
                .onSuccess { resp ->
                    _uiState.update {
                        it.copy(
                            allItems = resp.items,
                            allItemsTotal = resp.total,
                            isLoadingAllItems = false
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingAllItems = false,
                            errorMessage = if (seed.isNotEmpty()) null else err.message
                        )
                    }
                }
        }
    }

    fun reportFound(
        context: Context,
        imageUri: Uri,
        itemType: String,
        location: String,
        contactInfo: String,
        color: String,
        brand: String,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    loadingMessage = "Uploading photo & generating Gemini AI description...",
                    errorMessage = null,
                    reportSuccessMessage = null
                )
            }
            var tempFile: File? = null
            try {
                tempFile = withContext(Dispatchers.IO) {
                    ImageUtils.prepareImageForUpload(context, imageUri)
                }
                repository.reportFound(
                    token = _uiState.value.userToken,
                    imageFile = tempFile,
                    itemType = itemType,
                    location = location,
                    contactInfo = contactInfo,
                    color = color,
                    brand = brand,
                    description = description
                ).onSuccess { resp ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            reportSuccessMessage = resp.aiWarning?.let { w ->
                                "${resp.message} (Note: $w)"
                            } ?: resp.message,
                            finderMatchedLostItem = resp.lostMatches.firstOrNull()
                        )
                    }
                    loadRecentFoundItems()
                    onSuccess()
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = err.message ?: "Failed to report found item."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = e.message ?: "Unable to process image."
                    )
                }
            } finally {
                tempFile?.delete()
            }
        }
    }

    fun searchLost(
        context: Context,
        imageUri: Uri,
        itemType: String,
        location: String,
        color: String,
        brand: String,
        description: String,
        onResultsReady: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    loadingMessage = "Analyzing photo & running 3-tier Gemini AI matching...",
                    errorMessage = null,
                    searchResponse = null
                )
            }
            var tempFile: File? = null
            try {
                tempFile = withContext(Dispatchers.IO) {
                    ImageUtils.prepareImageForUpload(context, imageUri)
                }
                repository.searchLost(
                    token = _uiState.value.userToken,
                    imageFile = tempFile,
                    itemType = itemType,
                    location = location,
                    color = color,
                    brand = brand,
                    description = description
                ).onSuccess { resp ->
                    _uiState.update {
                        it.copy(isSubmitting = false, searchResponse = resp)
                    }
                    onResultsReady()
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = err.message ?: "Search failed."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = e.message ?: "Unable to process image."
                    )
                }
            } finally {
                tempFile?.delete()
            }
        }
    }

    fun userLogin(email: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, loadingMessage = "Signing in...", errorMessage = null)
            }
            repository.userLogin(email, password)
                .onSuccess { resp ->
                    cacheManager.saveUserSession(resp.token, resp.user)
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            userToken = resp.token,
                            currentUser = resp.user
                        )
                    }
                    loadNotifications(showPopupIfUnread = true)
                    onSuccess()
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = err.message ?: "Invalid email or password."
                        )
                    }
                }
        }
    }

    fun userRegister(name: String, email: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, loadingMessage = "Creating your account...", errorMessage = null)
            }
            repository.userRegister(name, email, password)
                .onSuccess { resp ->
                    cacheManager.saveUserSession(resp.token, resp.user)
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            userToken = resp.token,
                            currentUser = resp.user
                        )
                    }
                    loadNotifications(showPopupIfUnread = true)
                    onSuccess()
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = err.message ?: "Could not create account."
                        )
                    }
                }
        }
    }

    fun userLogout() {
        cacheManager.clearUserSession()
        shownPopupNotificationIds.clear()
        _uiState.update {
            it.copy(
                userToken = null,
                currentUser = null,
                notifications = emptyList(),
                unreadNotificationCount = 0,
                popupMatchNotification = null
            )
        }
    }

    fun loadNotifications(showPopupIfUnread: Boolean = false) {
        val token = _uiState.value.userToken ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingNotifications = it.notifications.isEmpty()) }
            repository.getNotifications(token)
                .onSuccess { resp ->
                    val firstUnread = if (showPopupIfUnread) {
                        resp.notifications.firstOrNull { !it.isRead && it.id !in shownPopupNotificationIds }
                    } else null
                    if (firstUnread != null) {
                        shownPopupNotificationIds.add(firstUnread.id)
                    }
                    _uiState.update {
                        it.copy(
                            notifications = resp.notifications,
                            unreadNotificationCount = resp.unreadCount,
                            isLoadingNotifications = false,
                            popupMatchNotification = firstUnread ?: it.popupMatchNotification
                        )
                    }
                }
                .onFailure {
                    _uiState.update { it.copy(isLoadingNotifications = false) }
                }
        }
    }

    fun markNotificationRead(notifId: Int) {
        val token = _uiState.value.userToken ?: return
        _uiState.update { state ->
            val updated = state.notifications.map {
                if (it.id == notifId) it.copy(isRead = true) else it
            }
            state.copy(
                notifications = updated,
                unreadNotificationCount = updated.count { !it.isRead }
            )
        }
        viewModelScope.launch {
            repository.markNotificationRead(token, notifId)
        }
    }

    fun markAllNotificationsRead() {
        val token = _uiState.value.userToken ?: return
        _uiState.update { state ->
            state.copy(
                notifications = state.notifications.map { it.copy(isRead = true) },
                unreadNotificationCount = 0
            )
        }
        viewModelScope.launch {
            repository.markAllNotificationsRead(token)
        }
    }

    fun dismissMatchPopup() {
        val current = _uiState.value.popupMatchNotification
        _uiState.update { it.copy(popupMatchNotification = null) }
        if (current != null && !current.isRead) {
            markNotificationRead(current.id)
        }
    }

    fun dismissFinderMatchPopup() {
        _uiState.update { it.copy(finderMatchedLostItem = null) }
    }

    fun updateProfile(
        name: String,
        phone: String,
        address: String,
        city: String,
        alternateContact: String,
        onSuccess: () -> Unit = {}
    ) {
        val token = _uiState.value.userToken ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, loadingMessage = "Saving profile...", errorMessage = null)
            }
            repository.updateProfile(
                token = token,
                name = name,
                phone = phone,
                address = address,
                city = city,
                alternateContact = alternateContact
            )
                .onSuccess { resp ->
                    cacheManager.updateCachedUser(resp.user)
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            currentUser = resp.user
                        )
                    }
                    onSuccess()
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = err.message ?: "Could not update profile."
                        )
                    }
                }
        }
    }

    fun signInWithGoogle(context: Context, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, loadingMessage = "Signing in with Google...", errorMessage = null)
            }
            try {
                val credentialManager = CredentialManager.create(context)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    repository.googleLogin(googleCredential.idToken)
                        .onSuccess { resp ->
                            cacheManager.saveUserSession(resp.token, resp.user)
                            _uiState.update {
                                it.copy(
                                    isSubmitting = false,
                                    userToken = resp.token,
                                    currentUser = resp.user
                                )
                            }
                            loadNotifications(showPopupIfUnread = true)
                            onSuccess()
                        }
                        .onFailure { err ->
                            _uiState.update {
                                it.copy(
                                    isSubmitting = false,
                                    errorMessage = err.message ?: "Google Sign-In verification failed."
                                )
                            }
                        }
                } else {
                    _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = "Unexpected Google credential type.")
                    }
                }
            } catch (_: GetCredentialCancellationException) {
                _uiState.update { it.copy(isSubmitting = false) }
            } catch (e: GetCredentialException) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = e.errorMessage?.toString() ?: "Google Sign-In failed."
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        errorMessage = e.message ?: "Google Sign-In failed."
                    )
                }
            }
        }
    }

    fun adminLogin(username: String, password: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, loadingMessage = "Signing in...", errorMessage = null)
            }
            repository.adminLogin(username, password)
                .onSuccess { resp ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            adminToken = resp.token,
                            adminStatusMessage = "Signed in as admin."
                        )
                    }
                    loadRecentFoundItems()
                    onSuccess()
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            errorMessage = err.message ?: "Invalid credentials."
                        )
                    }
                }
        }
    }

    fun deleteItem(itemId: Int) {
        val token = _uiState.value.adminToken ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, loadingMessage = "Deleting item #$itemId...") }
            repository.deleteItem(token, itemId)
                .onSuccess { resp ->
                    _uiState.update {
                        it.copy(isSubmitting = false, adminStatusMessage = resp.message)
                    }
                    loadRecentFoundItems()
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = err.message)
                    }
                }
        }
    }

    fun clearAllItems() {
        val token = _uiState.value.adminToken ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, loadingMessage = "Clearing all items...") }
            repository.clearAllItems(token)
                .onSuccess { resp ->
                    cacheManager.clearCachedItems()
                    _uiState.update {
                        it.copy(isSubmitting = false, adminStatusMessage = resp.message)
                    }
                    loadRecentFoundItems()
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(isSubmitting = false, errorMessage = err.message)
                    }
                }
        }
    }

    fun adminLogout() {
        _uiState.update { it.copy(adminToken = null, adminStatusMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { state ->
            if (state.errorMessage == null && state.reportSuccessMessage == null && state.adminStatusMessage == null) {
                state
            } else {
                state.copy(errorMessage = null, reportSuccessMessage = null, adminStatusMessage = null)
            }
        }
    }
}
