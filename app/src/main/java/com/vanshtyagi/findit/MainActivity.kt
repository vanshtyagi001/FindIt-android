package com.vanshtyagi.findit

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vanshtyagi.findit.data.remote.ItemDto
import com.vanshtyagi.findit.ui.screens.AdminScreen
import com.vanshtyagi.findit.ui.screens.AuthScreen
import com.vanshtyagi.findit.ui.screens.BrowseItemsScreen
import com.vanshtyagi.findit.ui.screens.HomeScreen
import com.vanshtyagi.findit.ui.screens.ItemDetailBottomSheet
import com.vanshtyagi.findit.ui.screens.NotificationsScreen
import com.vanshtyagi.findit.ui.screens.ReportFoundScreen
import com.vanshtyagi.findit.ui.screens.ResultsScreen
import com.vanshtyagi.findit.ui.screens.SearchLostScreen
import com.vanshtyagi.findit.ui.theme.FindItColors
import com.vanshtyagi.findit.ui.theme.FindItTheme
import com.vanshtyagi.findit.ui.viewmodel.FindItViewModel

private object Routes {
    const val HOME = "home"
    const val REPORT_FOUND = "report_found"
    const val SEARCH_LOST = "search_lost"
    const val BROWSE_ITEMS = "browse_items"
    const val NOTIFICATIONS = "notifications"
    const val RESULTS = "results"
    const val AUTH = "auth"
    const val ADMIN = "admin"
    const val MY_ITEMS = "my_items"
    const val MESSAGES = "messages"
}

private val bottomTabRoutes = setOf(Routes.MY_ITEMS, Routes.MESSAGES)

private val pushEnter = slideInHorizontally(
    animationSpec = tween(380, easing = FastOutSlowInEasing),
    initialOffsetX = { it }
) + fadeIn(animationSpec = tween(220), initialAlpha = 0.65f)

private val pushExit = slideOutHorizontally(
    animationSpec = tween(380, easing = FastOutSlowInEasing),
    targetOffsetX = { -(it * 0.25f).toInt() }
) + fadeOut(animationSpec = tween(280), targetAlpha = 0.85f)

private val popEnter = slideInHorizontally(
    animationSpec = tween(340, easing = FastOutSlowInEasing),
    initialOffsetX = { -(it * 0.25f).toInt() }
) + fadeIn(animationSpec = tween(260), initialAlpha = 0.85f)

private val popExit = slideOutHorizontally(
    animationSpec = tween(340, easing = FastOutSlowInEasing),
    targetOffsetX = { it }
) + fadeOut(animationSpec = tween(220), targetAlpha = 0.65f)

private val tabEnter = fadeIn(animationSpec = tween(200))
private val tabExit = fadeOut(animationSpec = tween(150))

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(FindItColors.DeepNavy.toArgb()),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.WHITE,
                android.graphics.Color.WHITE
            )
        )
        setContent {
            FindItTheme {
                FindItApp()
            }
        }

    }
}

@Composable
fun FindItApp(viewModel: FindItViewModel = viewModel()) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val appUiState by viewModel.uiState.collectAsState()
    var pendingRouteAfterAuth by remember { mutableStateOf<String?>(null) }
    var popupDetailItem by remember { mutableStateOf<ItemDto?>(null) }

    fun completeAuthNavigation() {
        val target = pendingRouteAfterAuth
        pendingRouteAfterAuth = null
        if (target != null) {
            navController.navigate(target) {
                popUpTo(Routes.AUTH) { inclusive = true }
            }
        } else {
            navController.popBackStack()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FindItColors.OffWhite)
    ) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        enterTransition = { pushEnter },
        exitTransition = { pushExit },
        popEnterTransition = { popEnter },
        popExitTransition = { popExit }
    ) {
        composable(
            route = Routes.HOME,
            exitTransition = {
                if (targetState.destination.route in bottomTabRoutes) tabExit else pushExit
            },
            popEnterTransition = {
                if (initialState.destination.route in bottomTabRoutes) tabEnter else popEnter
            }
        ) {
            val uiState by viewModel.uiState.collectAsState()
            HomeScreen(
                uiState = uiState,
                onNavigateReportFound = {
                    viewModel.clearMessages()
                    val state = viewModel.uiState.value
                    if (state.currentUser == null || state.userToken == null) {
                        pendingRouteAfterAuth = Routes.REPORT_FOUND
                        Toast.makeText(context, "Please sign in to report an item", Toast.LENGTH_SHORT).show()
                        navController.navigate(Routes.AUTH)
                    } else {
                        navController.navigate(Routes.REPORT_FOUND)
                    }
                },
                onNavigateSearchLost = {
                    viewModel.clearMessages()
                    val state = viewModel.uiState.value
                    if (state.currentUser == null || state.userToken == null) {
                        pendingRouteAfterAuth = Routes.SEARCH_LOST
                        Toast.makeText(context, "Please sign in to report a lost item", Toast.LENGTH_SHORT).show()
                        navController.navigate(Routes.AUTH)
                    } else {
                        navController.navigate(Routes.SEARCH_LOST)
                    }
                },
                onNavigateBrowseAll = {
                    viewModel.clearMessages()
                    navController.navigate(Routes.BROWSE_ITEMS)
                },
                onNavigateNotifications = {
                    viewModel.clearMessages()
                    navController.navigate(Routes.NOTIFICATIONS)
                },
                onNavigateAdmin = {
                    viewModel.clearMessages()
                    if (viewModel.uiState.value.adminToken != null) {
                        navController.navigate(Routes.ADMIN)
                    } else {
                        navController.navigate(Routes.AUTH)
                    }
                },
                onNavigateMyItems = {
                    navController.navigate(Routes.MY_ITEMS)
                },
                onNavigateMessages = {
                    navController.navigate(Routes.MESSAGES)
                },
                onRefresh = { viewModel.warmUpAndLoad() }
            )
        }

        composable(Routes.REPORT_FOUND) {
            val uiState by viewModel.uiState.collectAsState()
            ReportFoundScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onSubmit = { uri, type, loc, contact, color, brand, desc ->
                    viewModel.reportFound(
                        context = context,
                        imageUri = uri,
                        itemType = type,
                        location = loc,
                        contactInfo = contact,
                        color = color,
                        brand = brand,
                        description = desc,
                        onSuccess = {
                            navController.popBackStack()
                        }
                    )
                }
            )
        }

        composable(Routes.SEARCH_LOST) {
            val uiState by viewModel.uiState.collectAsState()
            SearchLostScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onSearch = { uri, type, loc, color, brand, desc ->
                    viewModel.searchLost(
                        context = context,
                        imageUri = uri,
                        itemType = type,
                        location = loc,
                        color = color,
                        brand = brand,
                        description = desc,
                        onResultsReady = {
                            navController.navigate(Routes.RESULTS)
                        }
                    )
                }
            )
        }

        composable(Routes.BROWSE_ITEMS) {
            val uiState by viewModel.uiState.collectAsState()
            BrowseItemsScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onRefresh = { viewModel.loadAllItems(forceRefresh = true) }
            )
        }

        composable(Routes.NOTIFICATIONS) {
            val uiState by viewModel.uiState.collectAsState()
            NotificationsScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onRefresh = { viewModel.loadNotifications() },
                onMarkRead = { id -> viewModel.markNotificationRead(id) },
                onMarkAllRead = { viewModel.markAllNotificationsRead() },
                onSignInClick = { navController.navigate(Routes.AUTH) }
            )
        }

        composable(Routes.RESULTS) {
            val uiState by viewModel.uiState.collectAsState()
            ResultsScreen(
                searchResponse = uiState.searchResponse,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AUTH) {
            val uiState by viewModel.uiState.collectAsState()
            AuthScreen(
                uiState = uiState,
                onBack = {
                    pendingRouteAfterAuth = null
                    navController.popBackStack()
                },
                onUserLogin = { email, pass ->
                    viewModel.userLogin(email, pass) {
                        if (viewModel.uiState.value.currentUser?.profileComplete == true) {
                            completeAuthNavigation()
                        }
                    }
                },
                onUserRegister = { name, email, pass ->
                    viewModel.userRegister(name, email, pass) {
                        if (viewModel.uiState.value.currentUser?.profileComplete == true) {
                            completeAuthNavigation()
                        }
                    }
                },
                onGoogleSignIn = {
                    viewModel.signInWithGoogle(context) {
                        if (viewModel.uiState.value.currentUser?.profileComplete == true) {
                            completeAuthNavigation()
                        }
                    }
                },
                onUpdateProfile = { name, phone, address, city, alt, onSaved ->
                    viewModel.updateProfile(name, phone, address, city, alt) {
                        onSaved()
                        if (pendingRouteAfterAuth != null) {
                            completeAuthNavigation()
                        }
                    }
                },
                onUserLogout = { viewModel.userLogout() },
                onAdminLogin = { user, pass ->
                    viewModel.adminLogin(user, pass) {
                        navController.navigate(Routes.ADMIN) {
                            popUpTo(Routes.AUTH) { inclusive = true }
                        }
                    }
                },
                onOpenAdminPanel = {
                    navController.navigate(Routes.ADMIN) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ADMIN) {
            val uiState by viewModel.uiState.collectAsState()
            AdminScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onLogin = { user, pass -> viewModel.adminLogin(user, pass) },
                onLogout = { viewModel.adminLogout() },
                onDeleteItem = { id -> viewModel.deleteItem(id) },
                onClearAll = { viewModel.clearAllItems() }
            )
        }

        composable(
            route = Routes.MY_ITEMS,
            enterTransition = { tabEnter },
            exitTransition = { tabExit },
            popEnterTransition = { tabEnter },
            popExitTransition = { tabExit }
        ) {
            PlaceholderTabScreen(
                title = "My Items",
                description = "Your submitted lost and found reports will appear here once user accounts are enabled.",
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.MESSAGES,
            enterTransition = { tabEnter },
            exitTransition = { tabExit },
            popEnterTransition = { tabEnter },
            popExitTransition = { tabExit }
        ) {
            PlaceholderTabScreen(
                title = "Messages",
                description = "In-app finder/owner chat threads will appear here. For now, use the Contact button on any match card.",
                onBack = { navController.popBackStack() }
            )
        }
    }

    // Popup Alert: User's lost item matched a newly found item
    appUiState.popupMatchNotification?.let { notif ->
        val foundItem = notif.foundItem
        Dialog(onDismissRequest = { viewModel.dismissMatchPopup() }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(FindItColors.FoundBg, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = FindItColors.FoundText,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Item Has Been Found!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = FindItColors.DeepNavy,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = notif.message,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                    if (foundItem?.imageUrl?.isNotBlank() == true) {
                        Spacer(modifier = Modifier.height(14.dp))
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(foundItem.imageUrl)
                                .crossfade(200)
                                .build(),
                            contentDescription = foundItem.itemType,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9))
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = {
                            viewModel.dismissMatchPopup()
                            if (foundItem != null) {
                                popupDetailItem = foundItem
                            } else {
                                navController.navigate(Routes.NOTIFICATIONS)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FindItColors.Terracotta,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("View Found Item", fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = { viewModel.dismissMatchPopup() },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Dismiss", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    // Popup Alert: Finder's newly reported found item matched an existing lost report
    appUiState.finderMatchedLostItem?.let { match ->
        val lostItem = match.item
        Dialog(onDismissRequest = { viewModel.dismissFinderMatchPopup() }) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color.White,
                tonalElevation = 6.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(Color(0xFFEFF6FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = FindItColors.RoyalBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Possible Owner Found!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = FindItColors.DeepNavy,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Someone already reported losing a matching ${lostItem.itemType} near ${lostItem.location}. We've notified them, and you can also view their report.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    Button(
                        onClick = {
                            viewModel.dismissFinderMatchPopup()
                            popupDetailItem = lostItem
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FindItColors.RoyalBlue,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text("View Lost Report", fontWeight = FontWeight.Bold)
                    }
                    TextButton(
                        onClick = { viewModel.dismissFinderMatchPopup() },
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text("Dismiss", color = Color(0xFF64748B), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    popupDetailItem?.let { item ->
        ItemDetailBottomSheet(
            item = item,
            onDismiss = { popupDetailItem = null }
        )
    }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PlaceholderTabScreen(
    title: String,
    description: String,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = FindItColors.OffWhite,
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FindItColors.DeepNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FindItColors.DeepNavy
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

    }
}
