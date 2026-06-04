package com.uzgram.messenger.features.auth

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uzgram.messenger.ui.theme.UzBlue
import kotlinx.coroutines.delay

@Composable
fun OtpVerificationScreen(
    email: String,
    onVerified: () -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.authState.collectAsState()

    var otpDigits by remember { mutableStateOf(List(6) { "" }) }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    var resendCountdown by remember { mutableIntStateOf(60) }
    var canResend by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
        while (resendCountdown > 0) {
            delay(1000)
            resendCountdown--
        }
        canResend = true
    }

    LaunchedEffect(state.isVerified) {
        if (state.isVerified) onVerified()
    }

    val otp = otpDigits.joinToString("")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
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
            Icon(
                Icons.Filled.MarkEmailRead,
                contentDescription = null,
                tint = UzBlue,
                modifier = Modifier.size(40.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        Text(
            "Emailni tasdiqlang",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(8.dp))

        Text(
            "6 xonali kod yuborildi:\n$email",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(Modifier.height(40.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            otpDigits.forEachIndexed { i, digit ->
                OtpDigitBox(
                    digit = digit,
                    isFocused = false,
                    focusRequester = focusRequesters[i],
                    onValueChange = { value ->
                        val newDigits = otpDigits.toMutableList()
                        when {
                            value.isEmpty() -> {
                                newDigits[i] = ""
                                otpDigits = newDigits
                                if (i > 0) focusRequesters[i - 1].requestFocus()
                            }
                            value.length == 1 -> {
                                newDigits[i] = value
                                otpDigits = newDigits
                                if (i < 5) focusRequesters[i + 1].requestFocus()
                            }
                            value.length == 6 -> {
                                value.forEachIndexed { idx, c ->
                                    if (idx < 6) newDigits[idx] = c.toString()
                                }
                                otpDigits = newDigits
                                focusRequesters[5].requestFocus()
                            }
                        }
                    }
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        AnimatedVisibility(visible = state.error != null) {
            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel.verifyEmail(otp) },
            enabled = otp.length == 6 && !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = UzBlue)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Tasdiqlash", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(24.dp))

        if (canResend) {
            TextButton(onClick = {
                viewModel.resendVerificationCode(email)
                resendCountdown = 60
                canResend = false
            }) {
                Text("Kodni qayta yuborish", color = UzBlue)
            }
        } else {
            Text(
                "Qayta yuborish: ${resendCountdown}s",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun OtpDigitBox(
    digit: String,
    isFocused: Boolean,
    focusRequester: FocusRequester,
    onValueChange: (String) -> Unit
) {
    val textStyle = MaterialTheme.typography.headlineSmall.copy(
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground
    )

    BasicTextField(
        value = TextFieldValue(digit, selection = TextRange(digit.length)),
        onValueChange = { onValueChange(it.text) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        textStyle = textStyle,
        modifier = Modifier
            .size(48.dp)
            .focusRequester(focusRequester)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = if (digit.isNotEmpty()) 2.dp else 1.dp,
                color = if (digit.isNotEmpty()) UzBlue else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(12.dp)
            ),
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center) {
                innerTextField()
            }
        }
    )
}
