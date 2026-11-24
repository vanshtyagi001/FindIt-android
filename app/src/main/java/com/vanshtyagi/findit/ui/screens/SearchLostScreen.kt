package com.vanshtyagi.findit.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.vanshtyagi.findit.ui.components.CategoryDropdown
import com.vanshtyagi.findit.ui.components.FieldLabel
import com.vanshtyagi.findit.ui.components.FormHeaderBanner
import com.vanshtyagi.findit.ui.components.ImagePickerCard
import com.vanshtyagi.findit.ui.components.LoadingOverlay
import com.vanshtyagi.findit.ui.components.findItTextFieldColors
import com.vanshtyagi.findit.ui.theme.FindItColors
import com.vanshtyagi.findit.ui.viewmodel.FindItUiState

@Composable
fun SearchLostScreen(
    uiState: FindItUiState,
    onBack: () -> Unit,
    onSearch: (Uri, String, String, String, String, String) -> Unit
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var category by remember { mutableStateOf(uiState.itemTypes.firstOrNull() ?: "Electronics") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    val fieldColors = findItTextFieldColors()

    Scaffold(
        containerColor = FindItColors.OffWhite,
        bottomBar = {
            Surface(
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = {
                        val uri = imageUri
                        when {
                            uri == null ->
                                localError = "Please select or capture a reference photo."
                            location.isBlank() ->
                                localError = "Last Known Location is required."
                            else -> {
                                localError = null
                                onSearch(uri, category, location, color, brand, description)
                            }
                        }
                    },
                    enabled = !uiState.isSubmitting,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FindItColors.RoyalBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Find Matches with AI", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                FormHeaderBanner(
                    title = "Search Lost Item",
                    subtitle = "Upload a photo or describe your item so Gemini AI can match found reports.",
                    badgeText = "3-Tier AI Match",
                    accentColor = FindItColors.RoyalBlue,
                    onBack = onBack
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ImagePickerCard(
                        selectedImageUri = imageUri,
                        accentColor = FindItColors.RoyalBlue,
                        required = true,
                        helperText = "Tap to upload a reference photo",
                        onImageSelected = {
                            imageUri = it
                            localError = null
                        }
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Item Details",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = FindItColors.DeepNavy
                            )

                            CategoryDropdown(
                                selectedCategory = category,
                                categories = uiState.itemTypes,
                                asteriskColor = FindItColors.RequiredRed,
                                onCategorySelected = { category = it }
                            )

                            OutlinedTextField(
                                value = location,
                                onValueChange = { location = it },
                                label = {
                                    FieldLabel(
                                        "Last Known Location",
                                        required = true,
                                        asteriskColor = FindItColors.RequiredRed
                                    )
                                },
                                placeholder = { Text("e.g., Cafeteria or Main Library") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = {
                                    FieldLabel("Description / Identifying Marks (Optional)")
                                },
                                placeholder = { Text("e.g., Black leather wallet with scratch on corner") },
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = color,
                                onValueChange = { color = it },
                                label = { FieldLabel("Color (Optional)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = brand,
                                onValueChange = { brand = it },
                                label = { FieldLabel("Brand (Optional)") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    (localError ?: uiState.errorMessage)?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }

            if (uiState.isSubmitting) {
                LoadingOverlay(uiState.loadingMessage)
            }
        }
    }
}
