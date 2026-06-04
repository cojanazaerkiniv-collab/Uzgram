package com.uzgram.messenger.features.discover

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uzgram.messenger.domain.model.MiniApp
import com.uzgram.messenger.domain.model.MiniAppCategory
import com.uzgram.messenger.domain.model.User
import com.uzgram.messenger.ui.components.AvatarImage
import com.uzgram.messenger.ui.theme.UzBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
    onUserClick: (userId: String) -> Unit,
    onMiniAppClick: (appId: String) -> Unit,
    onGroupClick: (chatId: String) -> Unit,
    viewModel: DiscoverViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var query by remember { mutableStateOf("") }
    var activeTab by remember { mutableIntStateOf(0) }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val tabs = listOf("Barchasi", "Odamlar", "Guruhlar", "Kanallar", "Botlar", "Mini ilovalar")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Izlash", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
                // Search bar
                SearchBarField(
                    query = query,
                    onQueryChange = { q -> query = q; viewModel.search(q) },
                    onClear = { query = ""; viewModel.clearSearch() },
                    focusRequester = focusRequester
                )

                if (query.isNotBlank()) {
                    // Filter tabs
                    ScrollableTabRow(
                        selectedTabIndex = activeTab,
                        edgePadding = 16.dp,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = UzBlue,
                        divider = {}
                    ) {
                        tabs.forEachIndexed { idx, label ->
                            Tab(
                                selected = activeTab == idx,
                                onClick = { activeTab = idx },
                                text = { Text(label, fontSize = 13.sp) }
                            )
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (query.isBlank()) {
            // Discovery home
            DiscoverHome(
                featuredApps = state.featuredApps,
                topUsers = state.suggestedUsers,
                categories = MiniAppCategory.entries,
                onMiniAppClick = onMiniAppClick,
                onCategoryClick = { viewModel.filterByCategory(it) },
                onUserClick = onUserClick
            )
        } else {
            // Search results
            SearchResults(
                state = state,
                activeTab = activeTab,
                onUserClick = onUserClick,
                onGroupClick = onGroupClick,
                onMiniAppClick = onMiniAppClick
            )
        }
    }
}

@Composable
private fun SearchBarField(
    query: String,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    focusRequester: FocusRequester
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text("Xabarlar, foydalanuvchilar, guruhlar…", fontSize = 14.sp) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .focusRequester(focusRequester),
        leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Filled.Close, contentDescription = null)
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = UzBlue,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
        )
    )
}

@Composable
private fun DiscoverHome(
    featuredApps: List<MiniApp>,
    topUsers: List<User>,
    categories: List<MiniAppCategory>,
    onMiniAppClick: (String) -> Unit,
    onCategoryClick: (MiniAppCategory) -> Unit,
    onUserClick: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        // Featured apps banner
        if (featuredApps.isNotEmpty()) {
            item {
                SectionTitle("Tavsiya etilgan ilovalar")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(featuredApps, key = { it.id }) { app ->
                        FeaturedAppCard(app = app, onClick = { onMiniAppClick(app.id) })
                    }
                }
            }
        }

        // Categories grid
        item {
            SectionTitle("Kategoriyalar")
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(categories.take(9)) { cat ->
                    CategoryChip(category = cat, onClick = { onCategoryClick(cat) })
                }
            }
        }

        // Suggested users
        if (topUsers.isNotEmpty()) {
            item { SectionTitle("Tanishlar bilan bog'laning") }
            items(topUsers, key = { it.id }) { user ->
                UserSearchResultItem(user = user, onClick = { onUserClick(user.id) })
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun SearchResults(
    state: DiscoverUiState,
    activeTab: Int,
    onUserClick: (String) -> Unit,
    onGroupClick: (String) -> Unit,
    onMiniAppClick: (String) -> Unit
) {
    if (state.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        val showUsers = activeTab == 0 || activeTab == 1
        val showMiniApps = activeTab == 0 || activeTab == 5

        if (showUsers && state.foundUsers.isNotEmpty()) {
            if (activeTab == 0) item { SectionTitle("Odamlar") }
            items(state.foundUsers, key = { "u_${it.id}" }) { user ->
                UserSearchResultItem(user = user, onClick = { onUserClick(user.id) })
            }
        }

        if (showMiniApps && state.foundApps.isNotEmpty()) {
            if (activeTab == 0) item { SectionTitle("Mini ilovalar") }
            items(state.foundApps, key = { "a_${it.id}" }) { app ->
                MiniAppSearchResultItem(app = app, onClick = { onMiniAppClick(app.id) })
            }
        }

        if (state.foundUsers.isEmpty() && state.foundApps.isEmpty() && !state.isLoading) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Outlined.SearchOff, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Hech narsa topilmadi", fontSize = 16.sp)
                    Text("Boshqa so'z bilan urinib ko'ring", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
private fun FeaturedAppCard(app: MiniApp, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(140.dp, 180.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AvatarImage(url = app.iconUrl, fallbackName = app.name, size = 56.dp, modifier = Modifier.clip(RoundedCornerShape(14.dp)))
            Spacer(modifier = Modifier.height(8.dp))
            Text(app.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(app.category.labelResKey, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                Text(" ${String.format("%.1f", app.rating)}", fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun CategoryChip(category: MiniAppCategory, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp), contentAlignment = Alignment.Center) {
            Text(category.labelResKey, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun UserSearchResultItem(user: User, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(url = user.avatarUrl, fallbackName = user.displayName, size = 50.dp, modifier = Modifier.clip(CircleShape))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(user.displayName, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            if (!user.username.isNullOrBlank()) {
                Text("@${user.username}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            }
        }
        if (user.isVerified) {
            Icon(Icons.Filled.Verified, contentDescription = null, tint = UzBlue, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun MiniAppSearchResultItem(app: MiniApp, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarImage(url = app.iconUrl, fallbackName = app.name, size = 50.dp, modifier = Modifier.clip(RoundedCornerShape(12.dp)))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(app.name, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Text(app.shortDescription, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(14.dp))
            Text(" ${String.format("%.1f", app.rating)}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
