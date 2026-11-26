package com.vanshtyagi.findit.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.vanshtyagi.findit.R
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    onGoogleSignIn: () -> Unit,
    onUpdateProfile: (String, String, String, String, String, () -> Unit) -> Unit,
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
    var infoMessage by remember { mutableStateOf<String?>(null) }

    val fieldColors = findItTextFieldColors()
    val accentColor by animateColorAsState(
        targetValue = if (isAdminMode) FindItColors.Terracotta else FindItColors.RoyalBlue,
        animationSpec = tween(durationMillis = 300),
        label = "authAccentColor"
    )
    val iconTint = Color(0xFF475569)

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
                        uiState.currentUser != null && !isAdminMode ->
                            if (uiState.currentUser.profileComplete) "Your Profile" else "Complete Your Profile"
                        isAdminMode -> "Admin Portal"
                        isSignUp -> "Create Account"
                        else -> "Welcome Back"
                    },
                    subtitle = when {
                        uiState.currentUser != null && !isAdminMode ->
                            if (uiState.currentUser.profileComplete) {
                                "Keep your contact details up to date for quick item handoffs."
                            } else {
                                "Add your contact & stay details so finders or owners can reach you."
                            }
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
                        .padding(horizontal = 16.dp, vertical = 18.dp)
                        .navigationBarsPadding(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!isAdminMode && uiState.currentUser != null) {
                        val user = uiState.currentUser
                        var profName by remember(user.id, user.name) { mutableStateOf(user.name) }
                        var profPhone by remember(user.id, user.phone) { mutableStateOf(user.phone.orEmpty()) }
                        var profAddress by remember(user.id, user.address) { mutableStateOf(user.address.orEmpty()) }
                        var profCity by remember(user.id, user.city) { mutableStateOf(user.city.orEmpty()) }
                        var profAlt by remember(user.id, user.alternateContact) { mutableStateOf(user.alternateContact.orEmpty()) }
                        var profError by remember { mutableStateOf<String?>(null) }
                        var profSaved by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Avatar + Name + Email + Completion Badge
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.size(60.dp),
                                        shape = CircleShape,
                                        color = FindItColors.RoyalBlue.copy(alpha = 0.12f),
                                        border = BorderStroke(2.dp, FindItColors.RoyalBlue)
                                    ) {
                                        if (!user.avatarUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(LocalContext.current)
                                                    .data(user.avatarUrl)
                                                    .crossfade(200)
                                                    .build(),
                                                contentDescription = "Profile Photo",
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                            )
                                        } else {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Person,
                                                    contentDescription = null,
                                                    tint = FindItColors.RoyalBlue,
                                                    modifier = Modifier.size(30.dp)
                                                )
                                            }
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = user.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = FindItColors.DeepNavy
                                        )
                                        Text(
                                            text = user.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF64748B)
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (user.profileComplete) FindItColors.FoundBg else FindItColors.LostBg
                                        ) {
                                            Text(
                                                text = if (user.profileComplete) "Profile Complete" else "Setup Needed",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (user.profileComplete) FindItColors.FoundText else FindItColors.Terracotta,
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }

                                // Privacy Notice Card
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Outlined.Security,
                                            contentDescription = null,
                                            tint = FindItColors.RoyalBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Your Privacy Matters: Your mobile number, stay address, and alternate contact are kept strictly private and never shown publicly. They are only used to coordinate safe item returns.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF1E3A8A),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }

                                // Full Name *
                                Column {
                                    FieldLabel(text = "Full Name", required = true)
                                    OutlinedTextField(
                                        value = profName,
                                        onValueChange = { profName = it; profError = null; profSaved = false },
                                        placeholder = { Text("Enter your full name") },
                                        leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null, tint = iconTint) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = fieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Email Address (read-only)
                                Column {
                                    FieldLabel(text = "Email Address", required = false)
                                    OutlinedTextField(
                                        value = user.email,
                                        onValueChange = {},
                                        enabled = false,
                                        leadingIcon = { Icon(Icons.Outlined.Email, contentDescription = null, tint = iconTint) },
                                        trailingIcon = { Icon(Icons.Outlined.Lock, contentDescription = "Locked", tint = Color(0xFF94A3B8)) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = fieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Text(
                                        text = "Email address cannot be changed.",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B),
                                        modifier = Modifier.padding(top = 4.dp, start = 2.dp)
                                    )
                                }

                                // Mobile Number *
                                Column {
                                    FieldLabel(text = "Mobile Number", required = true)
                                    OutlinedTextField(
                                        value = profPhone,
                                        onValueChange = { profPhone = it; profError = null; profSaved = false },
                                        placeholder = { Text("e.g., +91 98765 43210") },
                                        leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = iconTint) },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = fieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Stay / Residential Address *
                                Column {
                                    FieldLabel(text = "Stay / Residential Address", required = true)
                                    OutlinedTextField(
                                        value = profAddress,
                                        onValueChange = { profAddress = it; profError = null; profSaved = false },
                                        placeholder = { Text("e.g., Hostel Block B Room 204, or Street / Apartment") },
                                        leadingIcon = { Icon(Icons.Outlined.Home, contentDescription = null, tint = iconTint) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = fieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // City / Campus Area (Optional)
                                Column {
                                    FieldLabel(text = "City / Campus Area (Optional)", required = false)
                                    OutlinedTextField(
                                        value = profCity,
                                        onValueChange = { profCity = it; profError = null; profSaved = false },
                                        placeholder = { Text("e.g., Greater Noida / North Campus") },
                                        leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = iconTint) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = fieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                // Alternate Contact (Optional)
                                Column {
                                    FieldLabel(text = "Alternate Contact (Optional)", required = false)
                                    OutlinedTextField(
                                        value = profAlt,
                                        onValueChange = { profAlt = it; profError = null; profSaved = false },
                                        placeholder = { Text("Backup phone number or WhatsApp") },
                                        leadingIcon = { Icon(Icons.Outlined.Phone, contentDescription = null, tint = iconTint) },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        colors = fieldColors,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }

                                val activeError = profError ?: uiState.errorMessage
                                if (activeError != null) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFFEF2F2)
                                    ) {
                                        Text(
                                            text = activeError,
                                            color = FindItColors.LostText,
                                            fontSize = 13.sp,
                                            modifier = Modifier.padding(12.dp)
                                        )
                                    }
                                }

                                if (profSaved && activeError == null) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = FindItColors.FoundBg
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = null,
                                                tint = FindItColors.FoundText,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Text(
                                                text = "Profile saved securely.",
                                                color = FindItColors.FoundText,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        when {
                                            profName.trim().isEmpty() -> profError = "Full name is required."
                                            profPhone.trim().length < 7 -> profError = "Please enter a valid mobile number."
                                            profAddress.trim().length < 4 -> profError = "Please enter your stay or residential address."
                                            else -> {
                                                profError = null
                                                onUpdateProfile(profName, profPhone, profAddress, profCity, profAlt) {
                                                    profSaved = true
                                                }
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = FindItColors.RoyalBlue,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier.fillMaxWidth().height(50.dp)
                                ) {
                                    Text(
                                        text = if (user.profileComplete) "Update Profile" else "Save & Continue",
                                        fontWeight = FontWeight.Bold
                                    )
                                }

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
                                        containerColor = FindItColors.Terracotta,
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
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .animateContentSize(spring(stiffness = Spring.StiffnessMediumLow)),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (!isAdminMode) {
                                    // High-contrast Sign In / Sign Up Segmented Pill
                                    Surface(
                                        color = Color(0xFFEEF2F6),
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
                                                    infoMessage = null
                                                },
                                                modifier = Modifier.weight(1f)
                                            )
                                            AuthTabPill(
                                                label = "Sign Up",
                                                selected = isSignUp,
                                                onClick = {
                                                    isSignUp = true
                                                    localError = null
                                                    infoMessage = null
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

                                AnimatedContent(
                                    targetState = Pair(isAdminMode, isSignUp),
                                    transitionSpec = {
                                        (slideInVertically(
                                            animationSpec = tween(240, easing = FastOutSlowInEasing),
                                            initialOffsetY = { it / 6 }
                                        ) + fadeIn(tween(220))) togetherWith (slideOutVertically(
                                            animationSpec = tween(180, easing = FastOutSlowInEasing),
                                            targetOffsetY = { -it / 8 }
                                        ) + fadeOut(tween(140)))
                                    },
                                    label = "authFieldsTransition"
                                ) { (adminMode, signUpMode) ->
                                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        if (!adminMode && signUpMode) {
                                            OutlinedTextField(
                                                value = name,
                                                onValueChange = { name = it },
                                                label = { FieldLabel("Full Name", required = false) },
                                                placeholder = { Text("e.g., Vansh Tyagi") },
                                                leadingIcon = {
                                                    Icon(Icons.Outlined.Person, contentDescription = null, tint = iconTint)
                                                },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                colors = fieldColors,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        if (adminMode) {
                                            OutlinedTextField(
                                                value = adminUsername,
                                                onValueChange = { adminUsername = it },
                                                label = { FieldLabel("Admin Username", required = false) },
                                                placeholder = { Text("Enter admin username") },
                                                leadingIcon = {
                                                    Icon(Icons.Outlined.Person, contentDescription = null, tint = iconTint)
                                                },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                colors = fieldColors,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        } else {
                                            OutlinedTextField(
                                                value = email,
                                                onValueChange = { email = it },
                                                label = { FieldLabel("Email Address", required = false) },
                                                placeholder = { Text("you@example.com") },
                                                leadingIcon = {
                                                    Icon(Icons.Outlined.Email, contentDescription = null, tint = iconTint)
                                                },
                                                singleLine = true,
                                                shape = RoundedCornerShape(12.dp),
                                                colors = fieldColors,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }

                                        OutlinedTextField(
                                            value = password,
                                            onValueChange = { password = it },
                                            label = { FieldLabel("Password", required = false) },
                                            placeholder = {
                                                Text(if (signUpMode && !adminMode) "Minimum 6 characters" else "Enter your password")
                                            },
                                            leadingIcon = {
                                                Icon(Icons.Outlined.Lock, contentDescription = null, tint = iconTint)
                                            },
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

                                        if (!adminMode && !signUpMode) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.End
                                            ) {
                                                TextButton(
                                                    onClick = {
                                                        infoMessage = "Password reset via email is coming soon."
                                                    }
                                                ) {
                                                    Text(
                                                        text = "Forgot Password?",
                                                        color = FindItColors.RoyalBlue,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                (localError ?: uiState.errorMessage)?.let { err ->
                                    Text(
                                        text = err,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                infoMessage?.let { info ->
                                    Text(
                                        text = info,
                                        color = FindItColors.RoyalBlue,
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                Button(
                                    onClick = {
                                        localError = null
                                        infoMessage = null
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
                                        containerColor = accentColor,
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

                        if (!isAdminMode) {
                            // "or" divider + Continue with Google
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = Color(0xFFCBD5E1)
                                )
                                Text(
                                    text = "or",
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF64748B)
                                )
                                HorizontalDivider(
                                    modifier = Modifier.weight(1f),
                                    color = Color(0xFFCBD5E1)
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    localError = null
                                    infoMessage = null
                                    onGoogleSignIn()
                                },
                                enabled = !uiState.isSubmitting,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = Color.White,
                                    contentColor = FindItColors.DeepNavy
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google_logo),
                                    contentDescription = "Google",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Continue with Google",
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Subtle Admin/User Mode Switch Link below the form
                        Text(
                            text = buildAnnotatedString {
                                if (isAdminMode) {
                                    append("Not an administrator? ")
                                    withStyle(SpanStyle(color = FindItColors.RoyalBlue, fontWeight = FontWeight.SemiBold)) {
                                        append("Back to User Login")
                                    }
                                } else {
                                    append("Authorized staff? ")
                                    withStyle(SpanStyle(color = FindItColors.Terracotta, fontWeight = FontWeight.SemiBold)) {
                                        append("Switch to Admin Login")
                                    }
                                }
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    isAdminMode = !isAdminMode
                                    localError = null
                                    infoMessage = null
                                    password = ""
                                }
                                .padding(vertical = 8.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f, fill = false))

                        Text(
                            text = "By continuing, you agree to FindIt's Terms of Service & Privacy Policy.",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF94A3B8),
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp)
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

@Composable
private fun AuthTabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) Color.White else Color.Transparent,
        animationSpec = tween(200),
        label = "tabBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) FindItColors.DeepNavy else Color(0xFF64748B),
        animationSpec = tween(200),
        label = "tabText"
    )
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .then(
                if (selected) {
                    Modifier.border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(10.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        color = bgColor,
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
                color = textColor
            )
        }
    }
}
