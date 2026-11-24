package com.vanshtyagi.findit.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.vanshtyagi.findit.ui.components.CategoryDropdown
import com.vanshtyagi.findit.ui.components.ImagePickerCard
import com.vanshtyagi.findit.ui.components.LoadingOverlay
import com.vanshtyagi.findit.ui.viewmodel.FindItUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportFoundScreen(
    uiState: FindItUiState,
    onBack: () -> Unit,
    onSubmit: (Uri, String, String, String, String, String) -> Unit
) {
    val context = LocalContext.current
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var category by remember { mutableStateOf(uiState.itemTypes.firstOrNull() ?: "Electronics") }
    var location by remember { mutableStateOf("") }
    var contactInfo by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Report Found Item") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ImagePickerCard(
                    selectedImageUri = imageUri,
                    onImageSelected = {
                        imageUri = it
                        localError = null
                    }
                )

                CategoryDropdown(
                    selectedCategory = category,
                    categories = uiState.itemTypes,
                    onCategorySelected = { category = it }
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location Found *") },
                    placeholder = { Text("e.g., Main Library 2nd Floor") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = contactInfo,
                    onValueChange = { contactInfo = it },
                    label = { Text("Your Contact Info *") },
                    placeholder = { Text("Email or phone number") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = color,
                    onValueChange = { color = it },
                    label = { Text("Color (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("Brand (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                (localError ?: uiState.errorMessage)?.let { err ->
                    Text(
                        text = err,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        val uri = imageUri
                        when {
                            uri == null -> localError = "Please select or capture an item photo."
                            location.isBlank() -> localError = "Location Found is required."
                            contactInfo.isBlank() -> localError = "Contact Info is required."
                            else -> {
                                localError = null
                                onSubmit(uri, category, location, contactInfo, color, brand)
                            }
                        }
                    },
                    enabled = !uiState.isSubmitting,
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Submit Found Item")
                }
            }

            if (uiState.isSubmitting) {
                LoadingOverlay(uiState.loadingMessage)
            }
        }
    }
}
