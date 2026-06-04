package com.uzgram.messenger.features.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.uzgram.messenger.R
import com.uzgram.messenger.ui.theme.UzBlue

/**
 * Google Sign-In button component.
 * Used in both LoginScreen and RegisterScreen.
 */
@Composable
fun GoogleSignInButton(
    onGoogleToken: (idToken: String) -> Unit,
    onError: (String) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val helper = remember { GoogleAuthHelper(context) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            when (val signInResult = helper.handleSignInResult(result.data)) {
                is GoogleSignInResult.Success -> onGoogleToken(signInResult.idToken)
                is GoogleSignInResult.Error   -> onError(signInResult.message)
            }
        }
    }

    OutlinedButton(
        onClick = { launcher.launch(helper.getSignInIntent()) },
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(14.dp),
        enabled = !isLoading,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                strokeWidth = 2.dp,
                color = UzBlue
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Google "G" logo
                GoogleLogo()
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.continue_with_google),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun GoogleLogo(size: Int = 20) {
    // Google "G" rendered as colored text (fallback without image)
    Box(
        modifier = Modifier.size(size.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "G",
            fontSize = (size * 0.75).sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4285F4) // Google Blue
        )
    }
}

/**
 * Composable for the Google auth part of the login flow.
 * Shows a divider ("or") and the Google sign-in button.
 */
@Composable
fun GoogleAuthSection(
    onGoogleToken: (String) -> Unit,
    onError: (String) -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = "  yoki  ",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(20.dp))

    GoogleSignInButton(
        onGoogleToken = onGoogleToken,
        onError = onError,
        isLoading = isLoading
    )
}
