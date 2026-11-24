package com.vanshtyagi.findit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.vanshtyagi.findit.ui.components.FieldLabel
import com.vanshtyagi.findit.ui.components.FormHeaderBanner
import com.vanshtyagi.findit.ui.components.LoadingOverlay
import com.vanshtyagi.findit.ui.components.findItTextFieldColors
import com.vanshtyagi.findit.ui.theme.FindItColors
import com.vanshtyagi.findit.ui.viewmodel.FindItUiState

@Composable
fun AuthScreen(
    uiState: FindItUiState,
    onBack: () -> Unit,
    onUserLogin: (String, String) -> Unit,
    onUserRegister: (String, String, String) -> Unit,
    onUserLogout: () -> Unit,
    onAdminLogin: (String, String) -> Unit,
    onOpenAdminPanel: () -> Unit
) {
    var isAdminMode by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var adminUsername by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }

    val fieldColors = findItTextFieldColors()
    val accentColor = if (isAdminMode) FindItColors.Terracotta else FindItColors.RoyalBlue

    Scaffold(
        containerColor = FindItColors.OffWhite
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                FormHeaderBanner(
                    title = when {
                        uiState.currentUser != null && !isAdminMode -> "Your Account"
                        isAdminMode -> "Admin Portal"
                        isSignUp -> "Create Account"
                        else -> "Welcome Back"
                    },
                    subtitle = when {
                        uiState.currentUser != null && !isAdminMode -> "Signed in to FindIt"
                        isAdminMode -> "Manage reported items and clean up listings."
                        isSignUp -> "Join FindIt to track and report lost or found items."
                        else -> "Sign in to your FindIt account to continue."
                    },
                    badgeText = if (isAdminMode) "Admin Access" else "User Account",
                    accentColor = accentColor,
                    onBack = onBack
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Role Switch Card (User <-> Admin)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .background(accentColor.copy(alpha = 0.12f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isAdminMode) Icons.Default.AdminPanelSettings else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = accentColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Admin Login",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FindItColors.DeepNavy
                                    )
                                    Text(
                                        text = if (isAdminMode) "Switched to Administrator" else "Switch for admin access",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                            Switch(
                                checked = isAdminMode,
                                onCheckedChange = {
                                    isAdminMode = it
                                    localError = null
                                    password = ""
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = FindItColors.Terracotta,
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = Color(0xFFCBD5E1)
                                )
                            )
                        }
                    }

                    if (!isAdminMode && uiState.currentUser != null) {
                        // Signed-in User Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = uiState.currentUser.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = FindItColors.DeepNavy
                                )
                                Text(
                                    text = uiState.currentUser.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedButton(
                                    onClick = onUserLogout,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth().height(48.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Sign Out", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    } else if (isAdminMode && uiState.adminToken != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Admin session active",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FindItColors.DeepNavy
                                )
                                Button(
                                    onClick = onOpenAdminPanel,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = FindItColors.DeepNavy,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                ) {
                                    Text("Open Admin Dashboard", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // Main Auth Form Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                if (!isAdminMode) {
                                    // Clean Sign In / Sign Up Segmented Pill
                                    Surface(
                                        color = FindItColors.OffWhite,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            AuthTabPill(
                                                label = "Sign In",
                                                selected = !isSignUp,
                                                onClick = {
                                                    isSignUp = false
                                                    localError = null
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                            AuthTabPill(
                                                label = "Sign Up",
                                                selected = isSignUp,
                                                onClick = {
                                                    isSignUp = true
                                                    localError = null
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Administrator Credentials",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FindItColors.DeepNavy
                                    )
                                }

                                if (!isAdminMode && isSignUp) {
                                    OutlinedTextField(
                                        value = name,
                                        onValueChange = { name = it },
                                        label = { FieldLabel("Full Name", required = true) },
                                        placeholder = { Text("e.g., Vansh Tyagi") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = fieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                if (isAdminMode) {
                                    OutlinedTextField(
                                        value = adminUsername,
                                        onValueChange = { adminUsername = it },
                                        label = { FieldLabel("Admin Username", required = true) },
                                        placeholder = { Text("Enter admin username") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = fieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                } else {
                                    OutlinedTextField(
                                        value = email,
                                        onValueChange = { email = it },
                                        label = { FieldLabel("Email Address", required = true) },
                                        placeholder = { Text("you@example.com") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = fieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { FieldLabel("Password", required = true) },
                                    placeholder = { Text(if (isSignUp && !isAdminMode) "Minimum 6 characters" else "Enter your password") },
                                    singleLine = true,
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(
                                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                                contentDescription = "Toggle password visibility",
                                                tint = FindItColors.DeepNavy
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = fieldColors,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                (localError ?: uiState.errorMessage)?.let { err ->
                                    Text(
                                        text = err,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Button(
                                    onClick = {
                                        localError = null
                                        if (isAdminMode) {
                                            when {
                                                adminUsername.isBlank() -> localError = "Admin username is required."
                                                password.isBlank() -> localError = "Password is required."
                                                else -> onAdminLogin(adminUsername, password)
                                            }
                                        } else if (isSignUp) {
                                            when {
                                                name.isBlank() -> localError = "Full name is required."
                                                email.isBlank() || !email.contains("@") -> localError = "Enter a valid email address."
                                                password.length < 6 -> localError = "Password must be at least 6 characters."
                                                else -> onUserRegister(name, email, password)
                                            }
                                        } else {
                                            when {
                                                email.isBlank() -> localError = "Email is required."
                                                password.isBlank() -> localError = "Password is required."
                                                else -> onUserLogin(email, password)
                                            }
                                        }
                                    },
                                    enabled = !uiState.isSubmitting,
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isAdminMode) FindItColors.DeepNavy else FindItColors.RoyalBlue,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                ) {
                                    Text(
                                        text = when {
                                            isAdminMode -> "Sign In as Admin"
                                            isSignUp -> "Create Account"
                                            else -> "Sign In"
                                        },
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.isSubmitting) {
                LoadingOverlay(uiState.loadingMessage)
            }
        }
    }
}

@Composable
private fun AuthTabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (selected) Color.White else Color.Transparent,
        shadowElevation = if (selected) 2.dp else 0.dp,
        shape = RoundedCornerShape(10.dp)
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) FindItColors.DeepNavy else Color(0xFF64748B)
            )
        }
    }
}
