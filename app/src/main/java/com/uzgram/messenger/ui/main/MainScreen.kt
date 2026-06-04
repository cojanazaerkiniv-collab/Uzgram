package com.uzgram.messenger.ui.main

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.uzgram.messenger.features.chat.ChatListScreen
import com.uzgram.messenger.ui.theme.UzBlue

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badgeCount: Int = 0
) {
    object Chats : BottomNavItem("chats", "Chats", Icons.Filled.Chat, Icons.Outlined.Chat)
    object Contacts : BottomNavItem("contacts", "Contacts", Icons.Filled.People, Icons.Outlined.People)
    object Stories : BottomNavItem("stories", "Stories", Icons.Filled.AutoStories, Icons.Outlined.AutoStories)
    object Settings : BottomNavItem("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

@Composable
fun MainScreen(
    onChatClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val tabs = listOf(
        BottomNavItem.Chats,
        BottomNavItem.Contacts,
        BottomNavItem.Stories,
        BottomNavItem.Settings
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                tabs.forEach { tab ->
                    val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            navController.navigate(tab.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            BadgedBox(
                                badge = {
                                    if (tab.badgeCount > 0) Badge { Text("${tab.badgeCount}") }
                                }
                            ) {
                                Icon(
                                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                                    contentDescription = tab.label
                                )
                            }
                        },
                        label = { Text(tab.label) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = UzBlue,
                            selectedTextColor = UzBlue,
                            indicatorColor = UzBlue.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Chats.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(BottomNavItem.Chats.route) {
                ChatListScreen(
                    onChatClick = onChatClick,
                    onSearchClick = {},
                    onNewChatClick = {}
                )
            }
            composable(BottomNavItem.Contacts.route) {
                ContactsPlaceholder()
            }
            composable(BottomNavItem.Stories.route) {
                StoriesPlaceholder()
            }
            composable(BottomNavItem.Settings.route) {
                SettingsPlaceholder(onLogout = onLogout)
            }
        }
    }
}

@Composable
private fun ContactsPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.People, null, modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Contacts", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun StoriesPlaceholder() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.AutoStories, null, modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Text("Stories", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun SettingsPlaceholder(onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Settings", style = MaterialTheme.typography.headlineSmall,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        val items = listOf("Edit Profile", "Notifications", "Privacy & Security",
            "Appearance", "Language", "Data & Storage", "Devices")
        items.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                onClick = {}
            ) {
                ListItem(
                    headlineContent = { Text(item) },
                    trailingContent = { Icon(Icons.Filled.ChevronRight, null) }
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Outlined.Logout, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out")
        }
    }
}
