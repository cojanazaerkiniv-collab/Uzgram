package com.uzgram.messenger.features.profile

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uzgram.messenger.ui.components.AvatarImage
import com.uzgram.messenger.ui.theme.UzBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val user = state.user ?: return

    var displayName by remember(user.displayName) { mutableStateOf(user.displayName) }
    var username by remember(user.username) { mutableStateOf(user.username ?: "") }
    var bio by remember(user.bio) { mutableStateOf(user.bio ?: "") }
    var isSaving by remember { mutableStateOf(false) }
    var displayNameError by remember { mutableStateOf<String?>(null) }
    var usernameError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        displayNameError = if (displayName.isBlank()) "Ism bo'sh bo'lishi mumkin emas" else null
        usernameError = if (username.isNotBlank() && !username.matches(Regex("^[a-zA-Z0-9_]{5,32}$"))) {
            "5-32 ta belgi: harf, raqam, _ ruxsat etiladi"
        } else null
        return displayNameError == null && usernameError == null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profilni tahrirlash") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (validate()) {
                                isSaving = true
                                // viewModel.updateProfile(displayName, username, bio)
                            }
                        },
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Saqlash", color = UzBlue)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clickable { viewModel.showAvatarOptions() }
            ) {
                AvatarImage(
                    url = user.avatarUrl,
                    fallbackName = user.displayName,
                    size = 100.dp,
                    modifier = Modifier.clip(CircleShape)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { viewModel.showAvatarOptions() }) {
                Text("Rasmni o'zgartirish", color = UzBlue)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Display name
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it; displayNameError = null },
                label = { Text("Ism va familiya") },
                modifier = Modifier.fillMaxWidth(),
                isError = displayNameError != null,
                supportingText = displayNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                leadingIcon = { Icon(Icons.Outlined.Person, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Username
            OutlinedTextField(
                value = username,
                onValueChange = { username = it.lowercase().replace(" ", "_"); usernameError = null },
                label = { Text("Foydalanuvchi nomi") },
                modifier = Modifier.fillMaxWidth(),
                isError = usernameError != null,
                supportingText = usernameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                leadingIcon = { Text("@", style = MaterialTheme.typography.bodyLarge) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii,
                    imeAction = ImeAction.Next
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Bio
            OutlinedTextField(
                value = bio,
                onValueChange = { if (it.length <= 150) bio = it },
                label = { Text("O'zingiz haqida") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Outlined.Notes, contentDescription = null) },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done
                ),
                maxLines = 4,
                supportingText = { Text("${bio.length}/150", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
