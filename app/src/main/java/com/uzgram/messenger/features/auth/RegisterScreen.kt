package com.uzgram.messenger.features.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uzgram.messenger.ui.theme.UzBlue

@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.authState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var currentStep by remember { mutableIntStateOf(1) } // 1: Account, 2: Profile

    val passwordMatch = password == confirmPassword
    val passwordStrong = password.length >= 8 && password.any { it.isUpperCase() } &&
            password.any { it.isLowerCase() } && password.any { it.isDigit() }
    val usernameValid = username.matches(Regex("[a-zA-Z0-9_]{5,32}"))

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.LoginSuccess -> onRegisterSuccess()
                else -> {}
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 60.dp, bottom = 40.dp)
        ) {
            // Back / Step indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 1) {
                    IconButton(onClick = { currentStep-- }) {
                        Icon(Icons.Outlined.ArrowBack, "Back")
                    }
                } else {
                    TextButton(onClick = onNavigateToLogin) {
                        Text("Sign In", color = UzBlue)
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Step $currentStep of 2",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { currentStep / 2f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = UzBlue,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = if (currentStep == 1) "Create account" else "Set up profile",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (currentStep == 1) "Join UzGram — it's free" else "Tell us about yourself",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            AnimatedVisibility(state.error != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(state.error ?: "", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            if (currentStep == 1) {
                // STEP 1: Email + Password
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email address") },
                    leadingIcon = { Icon(Icons.Outlined.Email, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                null
                            )
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    supportingText = {
                        if (password.isNotEmpty() && !passwordStrong) {
                            Text("Must be 8+ chars with uppercase, lowercase, and number",
                                color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm password") },
                    leadingIcon = { Icon(Icons.Outlined.Lock, null) },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && !passwordMatch,
                    supportingText = {
                        if (confirmPassword.isNotEmpty() && !passwordMatch) {
                            Text("Passwords don't match", color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
            } else {
                // STEP 2: Profile info
                OutlinedTextField(
                    value = displayName,
                    onValueChange = { if (it.length <= 64) displayName = it },
                    label = { Text("Display name") },
                    leadingIcon = { Icon(Icons.Outlined.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    supportingText = { Text("${displayName.length}/64") }
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { if (it.length <= 32) username = it.lowercase() },
                    label = { Text("Username") },
                    leadingIcon = { Text("  @  ", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    isError = username.isNotEmpty() && !usernameValid,
                    supportingText = {
                        if (username.isNotEmpty() && !usernameValid) {
                            Text("5-32 chars, only letters, numbers, underscore",
                                color = MaterialTheme.colorScheme.error)
                        }
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { if (it.length <= 200) bio = it },
                    label = { Text("Bio (optional)") },
                    leadingIcon = { Icon(Icons.Outlined.Info, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    maxLines = 3,
                    supportingText = { Text("${bio.length}/200") }
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    if (currentStep == 1) {
                        if (email.isNotBlank() && passwordStrong && passwordMatch) currentStep = 2
                    } else {
                        viewModel.register(email.trim(), password, displayName.trim(), username.trim(), bio.ifBlank { null })
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(14.dp),
                enabled = when (currentStep) {
                    1 -> email.isNotBlank() && passwordStrong && passwordMatch && !state.isLoading
                    else -> displayName.isNotBlank() && usernameValid && !state.isLoading
                },
                colors = ButtonDefaults.buttonColors(containerColor = UzBlue)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Text(
                        if (currentStep == 1) "Continue" else "Create Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
