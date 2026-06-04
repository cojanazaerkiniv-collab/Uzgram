package com.uzgram.messenger.features.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uzgram.messenger.ui.theme.UzBlue

@Composable
fun ResetPasswordScreen(
    resetToken: String,
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.authState.collectAsState()

    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    val confirmFocus = remember { FocusRequester() }

    val passwordsMatch = newPassword == confirmPassword
    val isStrong = newPassword.length >= 8 &&
            newPassword.any { it.isUpperCase() } &&
            newPassword.any { it.isDigit() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) {
            Icon(Icons.Filled.ArrowBack, contentDescription = "Orqaga")
        }

        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .size(80.dp)
                .background(UzBlue.copy(alpha = 0.12f), RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.LockReset, contentDescription = null, tint = UzBlue, modifier = Modifier.size(40.dp))
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Yangi parol o'rnatish",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "Kamida 8 ta belgidan iborat, katta harf va raqam bo'lgan kuchli parol tanlang.",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = newPassword,
            onValueChange = { newPassword = it; viewModel.clearError() },
            label = { Text("Yangi parol") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showNew = !showNew }) {
                    Icon(
                        if (showNew) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { confirmFocus.requestFocus() }),
            isError = newPassword.isNotEmpty() && !isStrong,
            supportingText = {
                if (newPassword.isNotEmpty() && !isStrong) {
                    Text(
                        "Kamida 8 ta belgi, 1 ta katta harf va 1 ta raqam kerak",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp
                    )
                } else if (isStrong) {
                    Text("Kuchli parol", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Parolni tasdiqlang") },
            leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
            trailingIcon = {
                IconButton(onClick = { showConfirm = !showConfirm }) {
                    Icon(
                        if (showConfirm) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = null
                    )
                }
            },
            visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            isError = confirmPassword.isNotEmpty() && !passwordsMatch,
            supportingText = {
                if (confirmPassword.isNotEmpty() && !passwordsMatch) {
                    Text("Parollar mos kelmadi", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
                }
                state.error?.let { Text(it, color = MaterialTheme.colorScheme.error, fontSize = 11.sp) }
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(confirmFocus),
            shape = RoundedCornerShape(14.dp),
            singleLine = true
        )

        Spacer(Modifier.height(24.dp))

        PasswordStrengthIndicator(password = newPassword)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.clearError()
            },
            enabled = isStrong && passwordsMatch && newPassword.isNotEmpty() && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UzBlue)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Parolni o'zgartirish", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun PasswordStrengthIndicator(password: String) {
    if (password.isEmpty()) return

    val strength = when {
        password.length < 6 -> 1
        password.length < 8 -> 2
        password.length >= 8 && password.any { it.isUpperCase() } && password.any { it.isDigit() } -> 4
        password.length >= 8 -> 3
        else -> 2
    }

    val (label, color) = when (strength) {
        1    -> "Juda zaif" to MaterialTheme.colorScheme.error
        2    -> "Zaif" to MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
        3    -> "O'rtacha" to MaterialTheme.colorScheme.tertiary
        else -> "Kuchli" to MaterialTheme.colorScheme.primary
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Parol kuchi:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            repeat(4) { i ->
                LinearProgressIndicator(
                    progress = { if (i < strength) 1f else 0f },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp),
                    color = color,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}
