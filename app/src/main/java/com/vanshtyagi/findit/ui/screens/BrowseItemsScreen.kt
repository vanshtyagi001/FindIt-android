package com.vanshtyagi.findit.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vanshtyagi.findit.data.remote.ItemDto
import com.vanshtyagi.findit.ui.theme.FindItColors
import com.vanshtyagi.findit.ui.viewmodel.FindItUiState

private enum class ItemStatusFilter(val label: String) {
    ALL("All"),
    FOUND("Found"),
    LOST("Lost")
}

private enum class ItemSortOrder(val label: String) {
    NEWEST("Newest"),
    OLDEST("Oldest"),
    TYPE_AZ("A–Z Type")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowseItemsScreen(
    uiState: FindItUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit
) {
    LaunchedEffect(Unit) {
        onRefresh()
    }

    var searchQuery by remember { mutableStateOf("") }
    var statusFilter by remember { mutableStateOf(ItemStatusFilter.ALL) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var sortOrder by remember { mutableStateOf(ItemSortOrder.NEWEST) }
    var showSortMenu by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<ItemDto?>(null) }

    val sourceItems = uiState.allItems

    // Count items per category based on current status + search filter
    val categoryCounts = remember(sourceItems, statusFilter, searchQuery) {
        val q = searchQuery.trim()
        sourceItems
            .filter { item ->
                val matchesStatus = when (statusFilter) {
                    ItemStatusFilter.ALL -> true
                    ItemStatusFilter.FOUND -> !item.status.equals("lost", ignoreCase = true)
                    ItemStatusFilter.LOST -> item.status.equals("lost", ignoreCase = true)
                }
                val matchesQuery = q.isEmpty() ||
                    item.itemType.contains(q, ignoreCase = true) ||
                    item.location.contains(q, ignoreCase = true) ||
                    (item.brand?.contains(q, ignoreCase = true) == true) ||
                    (item.color?.contains(q, ignoreCase = true) == true) ||
                    (item.aiDescription?.contains(q, ignoreCase = true) == true)
                matchesStatus && matchesQuery
            }
            .groupingBy { it.itemType }
            .eachCount()
    }

    val filteredItems = remember(sourceItems, statusFilter, selectedCategory, searchQuery, sortOrder) {
        val q = searchQuery.trim()
        val list = sourceItems.filter { item ->
            val matchesStatus = when (statusFilter) {
                ItemStatusFilter.ALL -> true
                ItemStatusFilter.FOUND -> !item.status.equals("lost", ignoreCase = true)
                ItemStatusFilter.LOST -> item.status.equals("lost", ignoreCase = true)
            }
            val matchesCategory = selectedCategory == null ||
                item.itemType.equals(selectedCategory, ignoreCase = true)
            val matchesQuery = q.isEmpty() ||
                item.itemType.contains(q, ignoreCase = true) ||
                item.location.contains(q, ignoreCase = true) ||
                (item.brand?.contains(q, ignoreCase = true) == true) ||
                (item.color?.contains(q, ignoreCase = true) == true) ||
                (item.aiDescription?.contains(q, ignoreCase = true) == true)
            matchesStatus && matchesCategory && matchesQuery
        }
        when (sortOrder) {
            ItemSortOrder.NEWEST -> list.sortedByDescending { it.id }
            ItemSortOrder.OLDEST -> list.sortedBy { it.id }
            ItemSortOrder.TYPE_AZ -> list.sortedBy { it.itemType.lowercase() }
        }
    }

    val hasActiveFilters = statusFilter != ItemStatusFilter.ALL ||
        selectedCategory != null ||
        searchQuery.isNotBlank()

    Scaffold(
        containerColor = FindItColors.OffWhite,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Deep Navy Header with Search Bar
            Surface(
                color = FindItColors.DeepNavy,
                shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                            Column {
                                Text(
                                    text = "Browse All Items",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${filteredItems.size} of ${sourceItems.size} items",
                                    color = Color.White.copy(alpha = 0.78f),
                                    fontSize = 12.sp
                                )
                            }
                        }

                        IconButton(onClick = onRefresh) {
                            if (uiState.isLoadingAllItems) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Items",
                                    tint = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Search Bar
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = CircleShape,
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(19.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search item, brand, color, location...",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 13.sp
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = { searchQuery = it },
                                    singleLine = true,
                                    textStyle = TextStyle(
                                        color = Color(0xFF0F172A),
                                        fontSize = 13.sp
                                    ),
                                    cursorBrush = SolidColor(FindItColors.DeepNavy),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = Color(0xFF64748B),
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clickable { searchQuery = "" }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Status & Sort Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ItemStatusFilter.entries.forEach { filter ->
                        val selected = statusFilter == filter
                        FilterChip(
                            selected = selected,
                            onClick = { statusFilter = filter },
                            label = {
                                Text(
                                    text = filter.label,
                                    fontSize = 12.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FindItColors.DeepNavy,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White,
                                labelColor = Color(0xFF475569)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selected,
                                borderColor = Color(0xFFE2E8F0)
                            )
                        )
                    }
                }

                Box {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.clickable { showSortMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SwapVert,
                                contentDescription = "Sort",
                                tint = FindItColors.DeepNavy,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = sortOrder.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FindItColors.DeepNavy
                            )
                        }
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        ItemSortOrder.entries.forEach { order ->
                            DropdownMenuItem(
                                text = { Text(order.label) },
                                onClick = {
                                    sortOrder = order
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Category Filter Chips
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    val selected = selectedCategory == null
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategory = null },
                        label = {
                            Text(
                                text = "All Categories (${categoryCounts.values.sum()})",
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FindItColors.RoyalBlue,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF475569)
                        )
                    )
                }
                items(uiState.itemTypes) { type ->
                    val count = categoryCounts[type] ?: 0
                    val selected = selectedCategory.equals(type, ignoreCase = true)
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedCategory = if (selected) null else type
                        },
                        label = {
                            Text(
                                text = if (count > 0) "$type ($count)" else type,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = FindItColors.RoyalBlue,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = Color(0xFF475569)
                        )
                    )
                }
            }

            if (hasActiveFilters) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Clear all filters",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FindItColors.RoyalBlue,
                        modifier = Modifier.clickable {
                            statusFilter = ItemStatusFilter.ALL
                            selectedCategory = null
                            searchQuery = ""
                        }
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Items List
            if (uiState.isLoadingAllItems && filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = FindItColors.DeepNavy)
                }
            } else if (filteredItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Inventory2,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = "No matching items",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = FindItColors.DeepNavy
                        )
                        Text(
                            text = "Try clearing filters or searching a different keyword.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = filteredItems,
                        key = { it.id }
                    ) { item ->
                        BrowseItemCard(
                            item = item,
                            onClick = { selectedItem = item },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }

    // Item Detail Bottom Sheet
    selectedItem?.let { item ->
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val context = LocalContext.current
        ModalBottomSheet(
            onDismissRequest = { selectedItem = null },
            sheetState = sheetState,
            containerColor = Color.White
        ) {
            ItemDetailSheetContent(
                item = item,
                onContact = {
                    val contact = item.contactInfo.trim()
                    if (contact.contains("@")) {
                        val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$contact"))
                        runCatching { context.startActivity(intent) }
                    } else {
                        val digits = contact.filter { it.isDigit() || it == '+' }
                        if (digits.length >= 7) {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits"))
                            runCatching { context.startActivity(intent) }
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun BrowseItemCard(
    item: ItemDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isLost = item.status.equals("lost", ignoreCase = true)
    val badgeBg = if (isLost) FindItColors.LostBg else FindItColors.FoundBg
    val badgeText = if (isLost) FindItColors.LostText else FindItColors.FoundText
    val statusLabel = if (isLost) "LOST" else "FOUND"
    val fullImageUrl = item.imageUrl?.takeIf { it.isNotBlank() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(68.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF1F5F9)
            ) {
                if (fullImageUrl != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(fullImageUrl)
                            .crossfade(200)
                            .build(),
                        contentDescription = item.itemType,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.Category,
                            contentDescription = null,
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = buildString {
                            append(item.itemType)
                            if (!item.brand.isNullOrBlank()) append(" • ${item.brand}")
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = FindItColors.DeepNavy,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(color = badgeBg, shape = CircleShape) {
                        Text(
                            text = statusLabel,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 2.dp),
                            color = badgeText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (!item.aiDescription.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = item.aiDescription,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF475569),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                            text = item.location,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF64748B),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (!item.color.isNullOrBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null,
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = item.color,
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemDetailSheetContent(
    item: ItemDto,
    onContact: () -> Unit
) {
    val context = LocalContext.current
    val fullImageUrl = item.imageUrl?.takeIf { it.isNotBlank() }
    val isLost = item.status.equals("lost", ignoreCase = true)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (fullImageUrl != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(fullImageUrl)
                    .crossfade(200)
                    .build(),
                contentDescription = item.itemType,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = buildString {
                    append(item.itemType)
                    if (!item.brand.isNullOrBlank()) append(" (${item.brand})")
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = FindItColors.DeepNavy
            )
            Surface(
                color = if (isLost) FindItColors.LostBg else FindItColors.FoundBg,
                shape = CircleShape
            ) {
                Text(
                    text = if (isLost) "LOST" else "FOUND",
                    color = if (isLost) FindItColors.LostText else FindItColors.FoundText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        Text(
            text = "Location: ${item.location}" +
                (item.color?.takeIf { it.isNotBlank() }?.let { "  •  Color: $it" } ?: ""),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF475569)
        )

        if (!item.aiDescription.isNullOrBlank()) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Text(
                    text = item.aiDescription,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF334155),
                    lineHeight = 19.sp,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        Button(
            onClick = onContact,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = FindItColors.RoyalBlue,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(Icons.Outlined.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Contact: ${item.contactInfo}", fontWeight = FontWeight.Bold)
        }
    }
}
