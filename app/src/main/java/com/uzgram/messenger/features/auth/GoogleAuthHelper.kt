package com.uzgram.messenger.features.auth

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

/**
 * Helper class for Google OAuth 2.0 sign-in flow.
 * Wraps GoogleSignInClient for use in Compose screens.
 *
 * Web Client ID is from Google Cloud Console → OAuth 2.0 Credentials
 * → Web client (auto created by Google Service) → Client ID
 */
class GoogleAuthHelper(private val context: Context) {

    companion object {
        // Replace with your actual Web Client ID from google-services.json
        const val WEB_CLIENT_ID = "YOUR_WEB_CLIENT_ID.apps.googleusercontent.com"
    }

    private val googleSignInClient: GoogleSignInClient by lazy {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, options)
    }

    /**
     * Returns the sign-in intent to launch with [ActivityResultLauncher].
     */
    fun getSignInIntent(): Intent = googleSignInClient.signInIntent

    /**
     * Parse the result from the Google sign-in intent.
     * @param data — Intent from onActivityResult
     * @return [GoogleSignInAccount] or null on failure
     */
    fun handleSignInResult(data: Intent?): GoogleSignInResult {
        return try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            GoogleSignInResult.Success(
                idToken   = account.idToken ?: throw Exception("No ID token"),
                email     = account.email ?: "",
                name      = account.displayName ?: "",
                photoUrl  = account.photoUrl?.toString()
            )
        } catch (e: ApiException) {
            GoogleSignInResult.Error("Google sign-in failed: ${e.statusCode}")
        } catch (e: Exception) {
            GoogleSignInResult.Error(e.message ?: "Unknown error")
        }
    }

    /**
     * Sign out from Google (clears local token).
     */
    fun signOut() {
        googleSignInClient.signOut()
    }

    /**
     * Returns the last signed-in Google account (if any).
     * Useful for silent sign-in on app start.
     */
    fun getLastSignedInAccount(): GoogleSignInAccount? {
        return GoogleSignIn.getLastSignedInAccount(context)
    }
}

sealed class GoogleSignInResult {
    data class Success(
        val idToken: String,
        val email: String,
        val name: String,
        val photoUrl: String?
    ) : GoogleSignInResult()

    data class Error(val message: String) : GoogleSignInResult()
}
