package com.uzgram.messenger.features.miniapps

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.uzgram.messenger.domain.model.MiniApp
import com.uzgram.messenger.domain.model.MiniAppCategory
import com.uzgram.messenger.ui.components.AvatarImage
import com.uzgram.messenger.ui.theme.UzBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiniAppsScreen(
    onOpenApp: (appId: String, botUsername: String, launchUrl: String) -> Unit,
    viewModel: MiniAppsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedCategory by remember { mutableStateOf<MiniAppCategory?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mini Ilovalar", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Outlined.Search, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            // Installed apps (horizontal scroll)
            if (state.installedApps.isNotEmpty()) {
                item {
                    SectionHeader("Mening ilovalarim")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(state.installedApps, key = { "installed_${it.id}" }) { app ->
                            InstalledAppItem(
                                app = app,
                                onClick = { onOpenApp(app.id, app.botUsername, app.launchUrl) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // Category filter chips
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { selectedCategory = null; viewModel.selectCategory(null) },
                            label = { Text("Barchasi") }
                        )
                    }
                    items(MiniAppCategory.entries) { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat; viewModel.selectCategory(cat) },
                            label = { Text(cat.labelResKey) }
                        )
                    }
                }
            }

            // Featured
            if (state.featuredApps.isNotEmpty() && selectedCategory == null) {
                item {
                    SectionHeader("Tavsiya etiladi")
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(state.featuredApps, key = { "featured_${it.id}" }) { app ->
                            AppBannerCard(app = app, onClick = { onOpenApp(app.id, app.botUsername, app.launchUrl) })
                        }
                    }
                }
            }

            // All apps
            val apps = if (selectedCategory != null) {
                state.allApps.filter { it.category == selectedCategory }
            } else {
                state.allApps
            }

            item { SectionHeader(if (selectedCategory != null) selectedCategory!!.labelResKey else "Barcha ilovalar") }

            items(apps.chunked(2), key = { it.map { a -> a.id }.joinToString() }) { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    row.forEach { app ->
                        AppGridCard(
                            app = app,
                            modifier = Modifier.weight(1f),
                            onClick = { onOpenApp(app.id, app.botUsername, app.launchUrl) },
                            onInstall = { viewModel.toggleInstall(app) }
                        )
                    }
                    if (row.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
    )
}

@Composable
private fun InstalledAppItem(app: MiniApp, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable(onClick = onClick)
    ) {
        AvatarImage(
            url = app.iconUrl,
            fallbackName = app.name,
            size = 60.dp,
            modifier = Modifier.clip(RoundedCornerShape(14.dp))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = app.name,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AppBannerCard(app: MiniApp, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.size(200.dp, 120.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AvatarImage(
                    url = app.iconUrl,
                    fallbackName = app.name,
                    size = 48.dp,
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(app.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(app.category.labelResKey, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                        Text(" ${String.format("%.1f", app.rating)}", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun AppGridCard(
    app: MiniApp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onInstall: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AvatarImage(
                    url = app.iconUrl,
                    fallbackName = app.name,
                    size = 44.dp,
                    modifier = Modifier.clip(RoundedCornerShape(10.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(app.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(app.category.labelResKey, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                app.shortDescription,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(12.dp))
                    Text(" ${String.format("%.1f", app.rating)}", fontSize = 11.sp)
                }
                TextButton(
                    onClick = onInstall,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                ) {
                    Text(
                        if (app.isInstalled) "Ochish" else "O'rnatish",
                        fontSize = 12.sp,
                        color = UzBlue
                    )
                }
            }
        }
    }
}
