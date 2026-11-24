package com.vanshtyagi.findit.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
fun ReportFoundScreen(
    uiState: FindItUiState,
    onBack: () -> Unit,
    onSubmit: (Uri, String, String, String, String, String, String) -> Unit
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var category by remember { mutableStateOf(uiState.itemTypes.firstOrNull() ?: "Electronics") }
    var location by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
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
                            uri == null -> localError = "Please select or capture an item photo."
                            location.isBlank() -> localError = "Location Found is required."
                            contactInfo.isBlank() -> localError = "Contact Info is required."
                            else -> {
                                localError = null
                                onSubmit(uri, category, location, contactInfo, color, brand, description)
                            }
                        }
                    },
                    enabled = !uiState.isSubmitting,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FindItColors.Terracotta,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding()
                        .height(52.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submit Found Item", fontWeight = FontWeight.Bold)
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
                    title = "Report Found Item",
                    subtitle = "Snap a clear photo and share where you found it to help the owner.",
                    badgeText = "Found Item",
                    accentColor = FindItColors.Terracotta,
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
                        accentColor = FindItColors.Terracotta,
                        required = true,
                        helperText = "Tap to upload a photo",
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
                                text = "Item & Finder Details",
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
                                        "Location Found",
                                        required = true,
                                        asteriskColor = FindItColors.RequiredRed
                                    )
                                },
                                placeholder = { Text("e.g., Main Library 2nd Floor") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = contactInfo,
                                onValueChange = { contactInfo = it },
                                label = {
                                    FieldLabel(
                                        "Your Contact Info",
                                        required = true,
                                        asteriskColor = FindItColors.RequiredRed
                                    )
                                },
                                placeholder = { Text("Email or phone number") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = fieldColors,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = description,
                                onValueChange = { description = it },
                                label = { FieldLabel("Description / Identifying Marks (Optional)") },
                                placeholder = { Text("e.g., Found on table near window, has a sticker") },
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
