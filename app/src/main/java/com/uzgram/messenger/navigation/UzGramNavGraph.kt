package com.uzgram.messenger.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.*
import androidx.navigation.compose.*
import com.uzgram.messenger.features.auth.AuthViewModel
import com.uzgram.messenger.features.auth.LoginScreen
import com.uzgram.messenger.features.auth.RegisterScreen
import com.uzgram.messenger.features.chat.MessagingScreen
import com.uzgram.messenger.features.main.MainScreen
import com.uzgram.messenger.features.miniapps.MiniAppWebViewScreen
import com.uzgram.messenger.features.profile.EditProfileScreen
import com.uzgram.messenger.features.settings.*
import com.uzgram.messenger.features.story.StoryEditorScreen
import com.uzgram.messenger.features.story.StoryViewerScreen

sealed class Screen(val route: String) {
    object Login      : Screen("login")
    object Register   : Screen("register")
    object Main       : Screen("main")
    object Chat       : Screen("chat/{chatId}") {
        fun createRoute(chatId: String) = "chat/$chatId"
    }
    object UserProfile : Screen("user/{userId}") {
        fun createRoute(userId: String) = "user/$userId"
    }
    object Settings          : Screen("settings")
    object PrivacySettings   : Screen("settings/privacy")
    object NotifSettings     : Screen("settings/notifications")
    object AppearanceSettings: Screen("settings/appearance")
    object EditProfile       : Screen("edit-profile")
    object NewChat           : Screen("new-chat")
    object NewGroup          : Screen("new-group")
    object StoryViewer       : Screen("story/{userId}?index={index}") {
        fun createRoute(userId: String, index: Int = 0) = "story/$userId?index=$index"
    }
    object StoryEditor       : Screen("story-editor")
    object MiniApp           : Screen("mini-app/{appId}?botUsername={botUsername}&launchUrl={launchUrl}") {
        fun createRoute(appId: String, botUsername: String, launchUrl: String): String {
            val encoded = java.net.URLEncoder.encode(launchUrl, "UTF-8")
            return "mini-app/$appId?botUsername=$botUsername&launchUrl=$encoded"
        }
    }
}

data class AuthState(
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false
)

@Composable
fun UzGramNavGraph(
    authState: AuthState,
    navController: NavHostController = rememberNavController()
) {
    val startDestination = when {
        authState.isLoading -> Screen.Login.route
        authState.isLoggedIn -> Screen.Main.route
        else -> Screen.Login.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { slideInHorizontally(tween(260)) { it / 5 } + fadeIn(tween(260)) },
        exitTransition = { slideOutHorizontally(tween(260)) { -it / 5 } + fadeOut(tween(260)) },
        popEnterTransition = { slideInHorizontally(tween(260)) { -it / 5 } + fadeIn(tween(260)) },
        popExitTransition = { slideOutHorizontally(tween(260)) { it / 5 } + fadeOut(tween(260)) }
    ) {
        // ── Auth ──────────────────────────────────────────────────────────
        composable(Screen.Login.route) {
            val vm: AuthViewModel = hiltViewModel()
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onLoginSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = vm
            )
        }

        composable(Screen.Register.route) {
            val vm: AuthViewModel = hiltViewModel()
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Main.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                viewModel = vm
            )
        }

        // ── Main (5-tab shell) ────────────────────────────────────────────
        composable(Screen.Main.route) {
            MainScreen(
                onNavigateToChat = { chatId -> navController.navigate(Screen.Chat.createRoute(chatId)) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onNavigateToStoryViewer = { userId, idx -> navController.navigate(Screen.StoryViewer.createRoute(userId, idx)) },
                onNavigateToStoryEditor = { navController.navigate(Screen.StoryEditor.route) },
                onNavigateToMiniApp = { appId, bot, url -> navController.navigate(Screen.MiniApp.createRoute(appId, bot, url)) },
                onNavigateToCall = { _, _ -> /* CallService handles outgoing calls */ }
            )
        }

        // ── Messaging ─────────────────────────────────────────────────────
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType })
        ) { back ->
            val chatId = back.arguments?.getString("chatId") ?: return@composable
            MessagingScreen(
                chatId = chatId,
                onBack = { navController.popBackStack() },
                onNavigateToProfile = { userId -> navController.navigate(Screen.UserProfile.createRoute(userId)) },
                onNavigateToCall = { /* isVideo -> start call */ }
            )
        }

        // ── User profile ──────────────────────────────────────────────────
        composable(
            route = Screen.UserProfile.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { back ->
            val userId = back.arguments?.getString("userId") ?: return@composable
            // UserProfileScreen(userId = userId, onBack = { navController.popBackStack() })
        }

        // ── Settings cluster ──────────────────────────────────────────────
        composable(Screen.Settings.route) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
                onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onPrivacySecurity = { navController.navigate(Screen.PrivacySettings.route) },
                onNotifications = { navController.navigate(Screen.NotifSettings.route) },
                onAppearance = { navController.navigate(Screen.AppearanceSettings.route) },
                onLanguage = { /* language picker */ },
                onDataStorage = { /* data & storage */ },
                onDevices = { /* active sessions */ },
                onPremium = { /* premium screen */ },
                onHelp = { /* help */ },
                onAbout = { /* about */ },
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.PrivacySettings.route) {
            PrivacySettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.NotifSettings.route) {
            NotificationsSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.AppearanceSettings.route) {
            AppearanceSettingsScreen(onBack = { navController.popBackStack() })
        }

        composable(Screen.EditProfile.route) {
            EditProfileScreen(onBack = { navController.popBackStack() })
        }

        // ── Stories ───────────────────────────────────────────────────────
        composable(
            route = Screen.StoryViewer.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType },
                navArgument("index") { type = NavType.IntType; defaultValue = 0 }
            )
        ) { back ->
            val userId = back.arguments?.getString("userId") ?: return@composable
            val index  = back.arguments?.getInt("index") ?: 0
            StoryViewerScreen(
                userId = userId,
                initialIndex = index,
                onClose = { navController.popBackStack() }
            )
        }

        composable(Screen.StoryEditor.route) {
            StoryEditorScreen(
                onPublish = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Mini Apps ─────────────────────────────────────────────────────
        composable(
            route = Screen.MiniApp.route,
            arguments = listOf(
                navArgument("appId") { type = NavType.StringType },
                navArgument("botUsername") { type = NavType.StringType; defaultValue = "" },
                navArgument("launchUrl") { type = NavType.StringType; defaultValue = "" }
            )
        ) { back ->
            val appId       = back.arguments?.getString("appId") ?: return@composable
            val botUsername = back.arguments?.getString("botUsername") ?: ""
            val launchUrl   = java.net.URLDecoder.decode(
                back.arguments?.getString("launchUrl") ?: "", "UTF-8"
            )
            MiniAppWebViewScreen(
                appId = appId,
                botUsername = botUsername,
                launchUrl = launchUrl,
                onClose = { navController.popBackStack() }
            )
        }
    }
}
