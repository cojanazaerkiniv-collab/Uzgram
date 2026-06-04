package com.uzgram.messenger.features.main

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import com.uzgram.messenger.R
import com.uzgram.messenger.features.calls.CallHistoryScreen
import com.uzgram.messenger.features.chat.ChatListScreen
import com.uzgram.messenger.features.discover.DiscoverScreen
import com.uzgram.messenger.features.home.HomeScreen
import com.uzgram.messenger.features.profile.ProfileScreen

sealed class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val labelRes: Int
) {
    object Home : BottomNavItem(
        route = "home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        labelRes = R.string.nav_home
    )
    object Chats : BottomNavItem(
        route = "chats",
        selectedIcon = Icons.Filled.Chat,
        unselectedIcon = Icons.Outlined.Chat,
        labelRes = R.string.nav_chats
    )
    object Discover : BottomNavItem(
        route = "discover",
        selectedIcon = Icons.Filled.Search,
        unselectedIcon = Icons.Outlined.Search,
        labelRes = R.string.nav_discover
    )
    object Calls : BottomNavItem(
        route = "calls",
        selectedIcon = Icons.Filled.Call,
        unselectedIcon = Icons.Outlined.Call,
        labelRes = R.string.nav_calls
    )
    object Profile : BottomNavItem(
        route = "profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        labelRes = R.string.nav_profile
    )
}

private val NAV_ITEMS = listOf(
    BottomNavItem.Home,
    BottomNavItem.Chats,
    BottomNavItem.Discover,
    BottomNavItem.Calls,
    BottomNavItem.Profile
)

@Composable
fun MainScreen(
    onNavigateToChat: (chatId: String) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToStoryViewer: (userId: String, index: Int) -> Unit,
    onNavigateToStoryEditor: () -> Unit,
    onNavigateToMiniApp: (appId: String, botUsername: String, launchUrl: String) -> Unit,
    onNavigateToCall: (userId: String, isVideo: Boolean) -> Unit,
    missedCallCount: Int = 0,
    unreadChatCount: Int = 0
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp
            ) {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                NAV_ITEMS.forEach { item ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                    NavigationBarItem(
                        icon = {
                            BadgedBox(
                                badge = {
                                    when {
                                        item is BottomNavItem.Calls && missedCallCount > 0 ->
                                            Badge { Text(if (missedCallCount > 99) "99+" else missedCallCount.toString()) }
                                        item is BottomNavItem.Chats && unreadChatCount > 0 ->
                                            Badge { Text(if (unreadChatCount > 99) "99+" else unreadChatCount.toString()) }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = stringResource(item.labelRes)
                                )
                            }
                        },
                        label = { Text(stringResource(item.labelRes)) },
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                fadeIn(animationSpec = tween(180))
            },
            exitTransition = {
                fadeOut(animationSpec = tween(120))
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(180))
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(120))
            }
        ) {
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    onStoryClick = { userId, index -> onNavigateToStoryViewer(userId, index) },
                    onAddStory = onNavigateToStoryEditor,
                    onChatClick = onNavigateToChat
                )
            }

            composable(BottomNavItem.Chats.route) {
                ChatListScreen(
                    onChatClick = onNavigateToChat,
                    onNewChat = { /* navigate to new chat selector */ }
                )
            }

            composable(BottomNavItem.Discover.route) {
                DiscoverScreen(
                    onUserClick = { userId -> onNavigateToChat(userId) },
                    onMiniAppClick = { appId -> /* navigate to mini app detail */ },
                    onGroupClick = { chatId -> onNavigateToChat(chatId) }
                )
            }

            composable(BottomNavItem.Calls.route) {
                CallHistoryScreen(
                    onStartCall = { userId, isVideo -> onNavigateToCall(userId, isVideo) },
                    onCallClick = { /* navigate to call detail */ }
                )
            }

            composable(BottomNavItem.Profile.route) {
                ProfileScreen(
                    onSettings = onNavigateToSettings,
                    onEditProfile = onNavigateToEditProfile
                )
            }
        }
    }
}
