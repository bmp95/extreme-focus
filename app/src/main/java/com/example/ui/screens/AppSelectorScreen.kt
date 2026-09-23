package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MonitoredAppEntity
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonGreenSuccess
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

data class SuggestedPresetApp(
    val packageName: String,
    val appName: String,
    val category: String
)

val CATALOG_PRESETS = listOf(
    // Video & Streaming
    SuggestedPresetApp("com.google.android.youtube", "YouTube", "VIDEO"),
    SuggestedPresetApp("tv.twitch.android.app", "Twitch", "VIDEO"),
    SuggestedPresetApp("com.netflix.mediaclient", "Netflix", "VIDEO"),
    SuggestedPresetApp("com.disney.disneyplus", "Disney+", "VIDEO"),
    SuggestedPresetApp("com.amazon.avod.thirdpartyclient", "Prime Video", "VIDEO"),

    // Redes Sociales
    SuggestedPresetApp("com.instagram.android", "Instagram", "SOCIAL"),
    SuggestedPresetApp("com.zhiliaoapp.musically", "TikTok", "SOCIAL"),
    SuggestedPresetApp("com.twitter.android", "X (Twitter)", "SOCIAL"),
    SuggestedPresetApp("com.reddit.frontpage", "Reddit", "SOCIAL"),
    SuggestedPresetApp("com.facebook.katana", "Facebook", "SOCIAL"),
    SuggestedPresetApp("com.snapchat.android", "Snapchat", "SOCIAL"),
    SuggestedPresetApp("com.pinterest", "Pinterest", "SOCIAL"),

    // Juegos Móviles populares
    SuggestedPresetApp("com.supercell.brawlstars", "Brawl Stars", "GAME"),
    SuggestedPresetApp("com.supercell.clashroyale", "Clash Royale", "GAME"),
    SuggestedPresetApp("com.supercell.clashofclans", "Clash of Clans", "GAME"),
    SuggestedPresetApp("com.roblox.client", "Roblox", "GAME"),
    SuggestedPresetApp("com.dts.freefireth", "Free Fire", "GAME"),
    SuggestedPresetApp("com.activision.callofduty.shooter", "Call of Duty Mobile", "GAME"),
    SuggestedPresetApp("com.king.candycrushsaga", "Candy Crush Saga", "GAME"),
    SuggestedPresetApp("com.kiloo.subwaysurf", "Subway Surfers", "GAME"),
    SuggestedPresetApp("com.miHoYo.GenshinImpact", "Genshin Impact", "GAME"),
    SuggestedPresetApp("com.ea.gp.fifamobile", "EA SPORTS FC", "GAME")
)

@Composable
fun AppSelectorScreen(
    installedApps: List<InstalledAppInfo>,
    monitoredApps: List<MonitoredAppEntity>,
    onAddApp: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("TODAS") }

    val monitoredPackageSet = remember(monitoredApps) {
        monitoredApps.map { it.packageName }.toSet()
    }

    // Merge installed apps with our catalog so user can add games/apps even if in emulator
    val combinedAppList = remember(installedApps) {
        val installedMap = installedApps.associateBy { it.packageName }.toMutableMap()
        CATALOG_PRESETS.forEach { preset ->
            if (!installedMap.containsKey(preset.packageName)) {
                installedMap[preset.packageName] = InstalledAppInfo(
                    packageName = preset.packageName,
                    appName = preset.appName,
                    isSystemApp = false
                )
            }
        }
        installedMap.values.sortedBy { it.appName.lowercase() }
    }

    val filteredApps = remember(searchQuery, selectedCategoryFilter, combinedAppList) {
        combinedAppList.filter { app ->
            val matchesQuery = searchQuery.isBlank() ||
                    app.appName.contains(searchQuery, ignoreCase = true) ||
                    app.packageName.contains(searchQuery, ignoreCase = true)

            val matchesCategory = when (selectedCategoryFilter) {
                "JUEGOS" -> {
                    val lower = (app.appName + app.packageName).lowercase()
                    lower.contains("game") || lower.contains("clash") || lower.contains("brawl") ||
                            lower.contains("candy") || lower.contains("roblox") || lower.contains("freefire") ||
                            lower.contains("genshin") || lower.contains("subway") || lower.contains("duty") ||
                            lower.contains("fifa") || CATALOG_PRESETS.find { it.packageName == app.packageName }?.category == "GAME"
                }
                "VIDEO" -> {
                    val lower = (app.appName + app.packageName).lowercase()
                    lower.contains("youtube") || lower.contains("twitch") || lower.contains("video") ||
                            lower.contains("netflix") || lower.contains("disney") || lower.contains("prime") ||
                            CATALOG_PRESETS.find { it.packageName == app.packageName }?.category == "VIDEO"
                }
                "REDES" -> {
                    val lower = (app.appName + app.packageName).lowercase()
                    lower.contains("instagram") || lower.contains("tiktok") || lower.contains("musically") ||
                            lower.contains("twitter") || lower.contains("x.com") || lower.contains("reddit") ||
                            lower.contains("facebook") || lower.contains("snapchat") ||
                            CATALOG_PRESETS.find { it.packageName == app.packageName }?.category == "SOCIAL"
                }
                else -> true
            }

            matchesQuery && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Top Navigation Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(40.dp)
                    .background(DarkSurface, CircleShape)
                    .border(1.dp, CardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "Añadir a la cartera",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Redes, streaming de video y juegos móviles",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clean Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_app_input"),
            placeholder = {
                Text(
                    text = "Buscar app o juego (YouTube, Brawl Stars, etc.)...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.size(18.dp)
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = TextPrimary,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = DarkSurface,
                unfocusedContainerColor = DarkSurface
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Quick Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val categories = listOf("TODAS", "JUEGOS", "VIDEO", "REDES")
            items(categories) { category ->
                val isSelected = selectedCategoryFilter == category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) TextPrimary else DarkSurface)
                        .border(
                            1.dp,
                            if (isSelected) TextPrimary else CardBorder,
                            RoundedCornerShape(20.dp)
                        )
                        .clickable { selectedCategoryFilter = category }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = when (category) {
                            "JUEGOS" -> "🎮 Juegos"
                            "VIDEO" -> "📺 YouTube / Streaming"
                            "REDES" -> "💬 Redes sociales"
                            else -> "Todas"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PitchBlack else TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Catálogo disponible (${filteredApps.size})",
            style = MaterialTheme.typography.labelMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredApps, key = { it.packageName }) { app ->
                val isMonitored = monitoredPackageSet.contains(app.packageName)
                val isGame = app.packageName.contains("supercell") || app.packageName.contains("roblox") ||
                        app.packageName.contains("king") || app.appName.lowercase().contains("game")
                val isVideo = app.packageName.contains("youtube") || app.packageName.contains("twitch") ||
                        app.packageName.contains("netflix")

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
                        .clickable(enabled = !isMonitored) {
                            val category = if (isGame) "GAME" else if (isVideo) "VIDEO" else "SOCIAL"
                            onAddApp(app.packageName, app.appName)
                        }
                        .testTag("app_item_${app.packageName}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMonitored) DarkSurface.copy(alpha = 0.5f) else CardSurface
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isGame) {
                                Icon(
                                    imageVector = Icons.Default.Gamepad,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else if (isVideo) {
                                Icon(
                                    imageVector = Icons.Default.SmartDisplay,
                                    contentDescription = null,
                                    tint = TextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Text(
                                    text = app.appName.take(1).uppercase(),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                if (isGame) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Juego",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontSize = 10.sp,
                                        color = TextMuted
                                    )
                                }
                            }
                            Text(
                                text = app.packageName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                maxLines = 1
                            )
                        }

                        if (isMonitored) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .padding(horizontal = 10.dp, vertical = 5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeonGreenSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Añadida",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NeonGreenSuccess
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(DarkSurface)
                                    .border(1.dp, CardBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Añadir",
                                    tint = TextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
