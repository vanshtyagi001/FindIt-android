package com.vanshtyagi.findit.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.Pets
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vanshtyagi.findit.data.remote.ItemDto
import com.vanshtyagi.findit.ui.components.bounceClick
import com.vanshtyagi.findit.ui.theme.FindItColors
import com.vanshtyagi.findit.ui.viewmodel.FindItUiState
import java.time.Duration
import java.time.Instant

private data class QuickCategory(
    val label: String,
    val matchTypes: List<String>,
    val icon: ImageVector,
    val bgColor: Color,
    val tintColor: Color
)

private val quickCategories = listOf(
    QuickCategory(
        label = "Electronics",
        matchTypes = listOf("Electronics"),
        icon = Icons.Outlined.Devices,
        bgColor = Color(0xFFDBEAFE),
        tintColor = Color(0xFF1D4ED8)
    ),
    QuickCategory(
        label = "Keys &\nWallet",
        matchTypes = listOf("Keys & Wallet", "Keys", "Wallet/Purse"),
        icon = Icons.Outlined.Key,
        bgColor = Color(0xFFFEF3C7),
        tintColor = Color(0xFFD97706)
    ),
    QuickCategory(
        label = "Pets",
        matchTypes = listOf("Pets", "Pet"),
        icon = Icons.Outlined.Pets,
        bgColor = Color(0xFFFFEDD5),
        tintColor = Color(0xFFEA580C)
    ),
    QuickCategory(
        label = "Bags &\nLuggage",
        matchTypes = listOf("Bags & Luggage", "Bag/Backpack"),
        icon = Icons.Outlined.WorkOutline,
        bgColor = Color(0xFFFEE2E2),
        tintColor = Color(0xFFDC2626)
    ),
    QuickCategory(
        label = "Documents",
        matchTypes = listOf("Documents", "Identification", "Book/Notebook"),
        icon = Icons.Outlined.Description,
        bgColor = Color(0xFFCCFBF1),
        tintColor = Color(0xFF0F766E)
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: FindItUiState,
    onNavigateReportFound: () -> Unit,
    onNavigateSearchLost: () -> Unit,
    onNavigateBrowseAll: () -> Unit,
    onNavigateAdmin: () -> Unit,
    onNavigateMyItems: () -> Unit,
    onNavigateMessages: () -> Unit,
    onRefresh: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<QuickCategory?>(null) }
    var showQuickActionSheet by remember { mutableStateOf(false) }

    val filteredItems = remember(uiState.recentFoundItems, searchQuery, selectedCategory) {
        uiState.recentFoundItems.filter { item ->
            val matchesCategory = selectedCategory == null ||
                selectedCategory!!.matchTypes.any { it.equals(item.itemType, ignoreCase = true) }
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                item.itemType.contains(q, ignoreCase = true) ||
                item.location.contains(q, ignoreCase = true) ||
                (item.brand?.contains(q, ignoreCase = true) == true) ||
                (item.color?.contains(q, ignoreCase = true) == true) ||
                (item.aiDescription?.contains(q, ignoreCase = true) == true)
            matchesCategory && matchesQuery
        }
    }

    Scaffold(
        containerColor = FindItColors.OffWhite,
        bottomBar = {
            FindItBottomBar(
                onHome = onRefresh,
                onReport = onNavigateReportFound,
                onFabClick = { showQuickActionSheet = true },
                onMyItems = onNavigateMyItems,
                onMessages = onNavigateMessages
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
            item {
                HeaderWithOverlappingCards(
                    searchQuery = searchQuery,
                    onSearchQueryChange = { searchQuery = it },
                    onLostClick = onNavigateSearchLost,
                    onFoundClick = onNavigateReportFound,
                    onNotificationClick = onRefresh,
                    onProfileClick = onNavigateAdmin,
                    avatarUrl = uiState.currentUser?.avatarUrl
                )
            }

            uiState.reportSuccessMessage?.let { msg ->
                item {
                    Surface(
                        color = FindItColors.FoundBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(12.dp),
                            color = FindItColors.FoundText,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            item {
                Column(modifier = Modifier.padding(top = 18.dp)) {
                    Text(
                        text = "Quick Categories",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FindItColors.DeepNavy,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(
                            items = quickCategories,
                            key = { it.label },
                            contentType = { "category_tile" }
                        ) { cat ->
                            val isSelected = selectedCategory == cat
                            QuickCategoryTile(
                                category = cat,
                                isSelected = isSelected,
                                onClick = {
                                    selectedCategory = if (isSelected) null else cat
                                }
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Recent Activities",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FindItColors.DeepNavy
                        )
                        if (uiState.isSyncing) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                color = FindItColors.RoyalBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (selectedCategory != null || searchQuery.isNotBlank()) {
                            Text(
                                text = "Clear filter",
                                style = MaterialTheme.typography.labelMedium,
                                color = FindItColors.RoyalBlue,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.clickable {
                                    selectedCategory = null
                                    searchQuery = ""
                                }
                            )
                        }
                        Text(
                            text = "See All",
                            style = MaterialTheme.typography.labelMedium,
                            color = FindItColors.RoyalBlue,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable(onClick = onNavigateBrowseAll)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            if (uiState.isLoadingItems && filteredItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = FindItColors.DeepNavy)
                    }
                }
            } else if (filteredItems.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Text(
                            text = if (uiState.recentFoundItems.isEmpty()) {
                                "No recent activities yet. Tap + to report or search an item."
                            } else {
                                "No items match your current filter."
                            },
                            modifier = Modifier.padding(18.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                items(
                    items = filteredItems,
                    key = { it.id },
                    contentType = { "activity_card" }
                ) { item ->
                    RecentActivityCard(
                        item = item,
                        onClick = onNavigateSearchLost,
                        modifier = Modifier
                            .animateItem()
                            .padding(horizontal = 16.dp, vertical = 5.dp)
                    )
                }
            }
        }

            // Solid navy status bar scrim so scrolled content never merges with notification icons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsTopHeight(WindowInsets.statusBars)
                    .background(FindItColors.DeepNavy)
                    .align(Alignment.TopCenter)
            )
        }
    }

    if (showQuickActionSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQuickActionSheet = false },
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            contentWindowInsets = { WindowInsets.navigationBars },
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 2.dp)
                        .width(38.dp)
                        .height(4.dp)
                        .background(Color(0xFFCBD5E1), CircleShape)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column {
                    Text(
                        text = "Quick Action",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = FindItColors.DeepNavy
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Select an option to continue",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF64748B)
                    )
                }

                ActionSheetButton(
                    title = "I Lost Something",
                    subtitle = "Search found items using Gemini AI matching",
                    accentColor = FindItColors.RoyalBlue,
                    icon = Icons.Default.Search,
                    onClick = {
                        showQuickActionSheet = false
                        onNavigateSearchLost()
                    }
                )
                ActionSheetButton(
                    title = "I Found Something",
                    subtitle = "Upload a photo so the owner can find it",
                    accentColor = FindItColors.Terracotta,
                    icon = Icons.Outlined.PanTool,
                    onClick = {
                        showQuickActionSheet = false
                        onNavigateReportFound()
                    }
                )
            }
        }
    }
}

@Composable
private fun HeaderWithOverlappingCards(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onLostClick: () -> Unit,
    onFoundClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    avatarUrl: String? = null
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Deep Navy Header Background stopping midway behind the action cards
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(bottom = 52.dp)
                .background(
                    color = FindItColors.DeepNavy,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Hello,",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Normal
                    )
                    Text(
                        text = "Welcome!",
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onNotificationClick) {
                        Box {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Refresh / Notifications",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .align(Alignment.TopEnd)
                                    .background(FindItColors.Terracotta, CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onProfileClick),
                        color = Color.White.copy(alpha = 0.2f),
                        shape = CircleShape,
                        border = if (!avatarUrl.isNullOrBlank()) BorderStroke(1.5.dp, Color.White.copy(alpha = 0.85f)) else null
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (!avatarUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(avatarUrl)
                                        .crossfade(200)
                                        .build(),
                                    contentDescription = "User Profile",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Admin / Profile",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pill-shaped white search bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = CircleShape,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = Color(0xFF64748B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Search for lost or found items...",
                                color = Color(0xFF94A3B8),
                                fontSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = onSearchQueryChange,
                            singleLine = true,
                            textStyle = TextStyle(
                                color = Color(0xFF0F172A),
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(FindItColors.DeepNavy),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(18.dp))

            // 2-Column Action Cards overlapping only the bottom edge of the navy header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryActionCard(
                    title = "I Lost\nSomething",
                    backgroundColor = FindItColors.RoyalBlue,
                    leadingIcon = Icons.Default.Search,
                    badgeSymbol = "?",
                    onClick = onLostClick,
                    modifier = Modifier.weight(1f)
                )
                PrimaryActionCard(
                    title = "I Found\nSomething",
                    backgroundColor = FindItColors.Terracotta,
                    leadingIcon = Icons.Outlined.PanTool,
                    badgeSymbol = "+",
                    onClick = onFoundClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PrimaryActionCard(
    title: String,
    backgroundColor: Color,
    leadingIcon: ImageVector,
    badgeSymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(132.dp)
            .bounceClick(pressedScale = 0.96f, onClick = onClick)
            .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color.White.copy(alpha = 0.22f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeSymbol,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            Text(
                text = title,
                color = Color.White,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun QuickCategoryTile(
    category: QuickCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val labelColor by animateColorAsState(
        targetValue = if (isSelected) FindItColors.DeepNavy else Color(0xFF475569),
        animationSpec = tween(180),
        label = "catLabelColor"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(64.dp)
            .bounceClick(pressedScale = 0.93f, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(category.bgColor)
                .then(
                    if (isSelected) {
                        Modifier.border(2.dp, FindItColors.DeepNavy, RoundedCornerShape(12.dp))
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.label,
                tint = category.tintColor,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.label,
            fontSize = 11.sp,
            lineHeight = 13.sp,
            textAlign = TextAlign.Center,
            color = labelColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 2
        )
    }
}

@Composable
private fun RecentActivityCard(
    item: ItemDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isLost = item.status.equals("lost", ignoreCase = true)
    val badgeBg = if (isLost) FindItColors.LostBg else FindItColors.FoundBg
    val badgeText = if (isLost) FindItColors.LostText else FindItColors.FoundText
    val statusLabel = if (isLost) "LOST" else "FOUND"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.imageUrl)
                    .crossfade(200)
                    .build(),
                contentDescription = item.itemType,
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF1F5F9)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = buildString {
                        append(item.itemType)
                        if (!item.brand.isNullOrBlank()) append(" (${item.brand})")
                        append(if (isLost) " lost in " else " found in ")
                        append(item.location)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${item.location} • ${formatRelativeTime(item.timestamp)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = badgeBg,
                        shape = CircleShape
                    ) {
                        Text(
                            text = statusLabel,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                            color = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FindItBottomBar(
    onHome: () -> Unit,
    onReport: () -> Unit,
    onFabClick: () -> Unit,
    onMyItems: () -> Unit,
    onMessages: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = Color.White,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(64.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(
                    label = "Home",
                    icon = Icons.Default.Home,
                    selected = true,
                    onClick = onHome
                )
                BottomNavItem(
                    label = "Report",
                    icon = Icons.Outlined.Description,
                    selected = false,
                    onClick = onReport
                )
                Spacer(modifier = Modifier.width(56.dp))
                BottomNavItem(
                    label = "My Items",
                    icon = Icons.Outlined.ShoppingBag,
                    selected = false,
                    onClick = onMyItems
                )
                BottomNavItem(
                    label = "Messages",
                    icon = Icons.Outlined.ChatBubbleOutline,
                    selected = false,
                    onClick = onMessages
                )
            }
        }

        FloatingActionButton(
            onClick = onFabClick,
            shape = CircleShape,
            containerColor = FindItColors.DeepNavy,
            contentColor = Color.White,
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
            modifier = Modifier
                .size(56.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-24).dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Quick Action", modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun BottomNavItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (selected) FindItColors.DeepNavy else Color(0xFF64748B)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = tint,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ActionSheetButton(
    title: String,
    subtitle: String,
    accentColor: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFF8FAFC)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(accentColor, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = FindItColors.DeepNavy
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF475569)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8)
            )
        }
    }
}

private fun formatRelativeTime(isoTimestamp: String?): String {
    if (isoTimestamp.isNullOrBlank()) return "Recently"
    return try {
        val instant = Instant.parse(isoTimestamp)
        val mins = Duration.between(instant, Instant.now()).toMinutes().coerceAtLeast(0)
        when {
            mins < 1 -> "Just now"
            mins < 60 -> "${mins}m ago"
            mins < 1440 -> "${mins / 60} hrs ago"
            else -> "${mins / 1440}d ago"
        }
    } catch (_: Exception) {
        "Recently"
    }
}
