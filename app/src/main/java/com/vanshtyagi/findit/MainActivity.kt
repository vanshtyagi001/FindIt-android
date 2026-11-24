package com.vanshtyagi.findit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.vanshtyagi.findit.ui.screens.AdminScreen
import com.vanshtyagi.findit.ui.screens.HomeScreen
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
    const val RESULTS = "results"
    const val ADMIN = "admin"
    const val MY_ITEMS = "my_items"
    const val MESSAGES = "messages"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(FindItColors.DeepNavy.toArgb())
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
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                uiState = uiState,
                onNavigateReportFound = {
                    viewModel.clearMessages()
                    navController.navigate(Routes.REPORT_FOUND)
                },
                onNavigateSearchLost = {
                    viewModel.clearMessages()
                    navController.navigate(Routes.SEARCH_LOST)
                },
                onNavigateAdmin = {
                    viewModel.clearMessages()
                    navController.navigate(Routes.ADMIN)
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
            ReportFoundScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onSubmit = { uri, type, loc, contact, color, brand ->
                    viewModel.reportFound(
                        context = context,
                        imageUri = uri,
                        itemType = type,
                        location = loc,
                        contactInfo = contact,
                        color = color,
                        brand = brand,
                        onSuccess = {
                            navController.popBackStack()
                        }
                    )
                }
            )
        }

        composable(Routes.SEARCH_LOST) {
            SearchLostScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onSearch = { uri, type, loc, color, brand ->
                    viewModel.searchLost(
                        context = context,
                        imageUri = uri,
                        itemType = type,
                        location = loc,
                        color = color,
                        brand = brand,
                        onResultsReady = {
                            navController.navigate(Routes.RESULTS)
                        }
                    )
                }
            )
        }

        composable(Routes.RESULTS) {
            ResultsScreen(
                searchResponse = uiState.searchResponse,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.ADMIN) {
            AdminScreen(
                uiState = uiState,
                onBack = { navController.popBackStack() },
                onLogin = { user, pass -> viewModel.adminLogin(user, pass) },
                onLogout = { viewModel.adminLogout() },
                onDeleteItem = { id -> viewModel.deleteItem(id) },
                onClearAll = { viewModel.clearAllItems() }
            )
        }

        composable(Routes.MY_ITEMS) {
            PlaceholderTabScreen(
                title = "My Items",
                description = "Your submitted lost and found reports will appear here once user accounts are enabled.",
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MESSAGES) {
            PlaceholderTabScreen(
                title = "Messages",
                description = "In-app finder/owner chat threads will appear here. For now, use the Contact button on any match card.",
                onBack = { navController.popBackStack() }
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
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
