package app.extremefocus.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SentimentVeryDissatisfied
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.extremefocus.data.local.MonitoredAppEntity
import app.extremefocus.domain.PlatformRecommendationEngine
import app.extremefocus.domain.RestrictionTier
import app.extremefocus.ui.theme.AccentCyan
import app.extremefocus.ui.theme.AmberWarning
import app.extremefocus.ui.theme.BrandFacebook
import app.extremefocus.ui.theme.BrandInstagram
import app.extremefocus.ui.theme.BrandReddit
import app.extremefocus.ui.theme.BrandTikTok
import app.extremefocus.ui.theme.BrandTwitter
import app.extremefocus.ui.theme.BrandYouTube
import app.extremefocus.ui.theme.CardBorder
import app.extremefocus.ui.theme.CardBorderHover
import app.extremefocus.ui.theme.CardSurface
import app.extremefocus.ui.theme.CrimsonDanger
import app.extremefocus.ui.theme.DarkSurface
import app.extremefocus.ui.theme.NeonGreenSuccess
import app.extremefocus.ui.theme.PitchBlack
import app.extremefocus.ui.theme.TextMuted
import app.extremefocus.ui.theme.TextPrimary
import app.extremefocus.ui.theme.TextSecondary

@Composable
fun DashboardScreen(
    monitoredApps: List<MonitoredAppEntity>,
    permissionsState: PermissionsState,
    onNavigate: (AppScreen) -> Unit,
    onToggleBlock: (MonitoredAppEntity) -> Unit,
    onToggleNotificationBlock: (String, Boolean) -> Unit,
    onUpdateLimit: (String, Int) -> Unit,
    onApplyRestrictionTier: (RestrictionTier) -> Unit,
    onSimulateIntercept: (String) -> Unit,
    onRefreshStats: () -> Unit
) {
    val totalUsage = monitoredApps.sumOf { it.currentUsageMinutes }
    val totalAllowedBudget = monitoredApps.sumOf { it.dailyLimitMinutes }.coerceAtLeast(1)
    val totalBlockedAttempts = monitoredApps.sumOf { it.timesBlockedToday }
    val appsExceeded = monitoredApps.count { it.currentUsageMinutes >= it.dailyLimitMinutes && it.isBlocked }

    val missingPermissions = !permissionsState.hasUsageStats ||
            !permissionsState.isAccessibilityEnabled ||
            !permissionsState.hasOverlay ||
            !permissionsState.hasNotificationAccess ||
            !permissionsState.isDeviceAdminActive

    var selectedGlobalTier by remember { mutableStateOf<RestrictionTier?>(RestrictionTier.BALANCED) }

    // Dialog state for custom minute entry and humiliation check
    var customDialogApp by remember { mutableStateOf<MonitoredAppEntity?>(null) }
    var humiliationDialogData by remember { mutableStateOf<Pair<MonitoredAppEntity, Int>?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Editorial Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Focus",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (missingPermissions) "Protección pendiente" else "Protección activa",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (missingPermissions) AmberWarning else NeonGreenSuccess
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = onRefreshStats,
                        modifier = Modifier
                            .size(40.dp)
                            .background(DarkSurface, CircleShape)
                            .border(1.dp, CardBorder, CircleShape)
                            .testTag("refresh_stats_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sincronizar",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { onNavigate(AppScreen.AuditLogs) },
                        modifier = Modifier
                            .size(40.dp)
                            .background(DarkSurface, CircleShape)
                            .border(1.dp, CardBorder, CircleShape)
                            .testTag("view_logs_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Historial",
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 2. Clean Daily Progress Card
        item {
            CleanDailySummaryCard(
                totalUsage = totalUsage,
                totalBudget = totalAllowedBudget,
                totalBlockedAttempts = totalBlockedAttempts,
                appsExceeded = appsExceeded,
                missingPermissions = missingPermissions,
                onConfigurePermissions = { onNavigate(AppScreen.PermissionsGuide) }
            )
        }

        // 3. 3-Tier Global Restriction Selector
        item {
            RestrictionTierSelectorSection(
                selectedTier = selectedGlobalTier,
                onTierSelected = { tier ->
                    selectedGlobalTier = tier
                    onApplyRestrictionTier(tier)
                }
            )
        }

        // 4. Section Title & Add Action
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Límites por aplicación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Ajusta perfiles o define tu tiempo manualmente",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(DarkSurface)
                        .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
                        .clickable { onNavigate(AppScreen.AppSelector) }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Añadir",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }

        // 5. Clean Monitored App Cards
        items(monitoredApps, key = { it.packageName }) { app ->
            CleanAppItemCard(
                app = app,
                onToggleBlock = { onToggleBlock(app) },
                onToggleNotification = { onToggleNotificationBlock(app.packageName, app.blockNotifications) },
                onSelectLimitMinutes = { newMinutes ->
                    if (PlatformRecommendationEngine.isExcessive(app.packageName, newMinutes)) {
                        humiliationDialogData = Pair(app, newMinutes)
                    } else {
                        onUpdateLimit(app.packageName, newMinutes)
                    }
                },
                onOpenCustomMinutesDialog = {
                    customDialogApp = app
                },
                onSimulateTest = { onSimulateIntercept(app.packageName) }
            )
        }
    }

    // Modal: Custom Minutes Entry Dialog
    customDialogApp?.let { app ->
        CustomMinutesInputDialog(
            app = app,
            onDismiss = { customDialogApp = null },
            onConfirm = { minutes ->
                customDialogApp = null
                if (PlatformRecommendationEngine.isExcessive(app.packageName, minutes)) {
                    humiliationDialogData = Pair(app, minutes)
                } else {
                    onUpdateLimit(app.packageName, minutes)
                }
            }
        )
    }

    // Modal: Humiliating Tough-Love Confirmation Dialog
    humiliationDialogData?.let { (app, minutes) ->
        HumiliatingConfirmationDialog(
            app = app,
            minutes = minutes,
            onDismiss = { humiliationDialogData = null },
            onConfirmAnyway = {
                onUpdateLimit(app.packageName, minutes)
                humiliationDialogData = null
            }
        )
    }
}

@Composable
private fun RestrictionTierSelectorSection(
    selectedTier: RestrictionTier?,
    onTierSelected: (RestrictionTier) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = TextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Nivel de restricción recomendado",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Aplica cuotas automáticas según estudios de atención para cada plataforma (TikTok, Instagram, YouTube, X, Reddit):",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RestrictionTier.values().forEach { tier ->
                    val isSelected = selectedTier == tier
                    val accentColor = when (tier) {
                        RestrictionTier.STRICT -> CrimsonDanger
                        RestrictionTier.BALANCED -> AccentCyan
                        RestrictionTier.LENIENT -> AmberWarning
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) DarkSurface else PitchBlack)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) accentColor else CardBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onTierSelected(tier) }
                            .padding(vertical = 12.dp, horizontal = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = tier.title,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) TextPrimary else TextSecondary
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = tier.badge,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (isSelected) accentColor else TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CleanDailySummaryCard(
    totalUsage: Int,
    totalBudget: Int,
    totalBlockedAttempts: Int,
    appsExceeded: Int,
    missingPermissions: Boolean,
    onConfigurePermissions: () -> Unit
) {
    val usageRatio = (totalUsage.toFloat() / totalBudget.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = usageRatio,
        animationSpec = spring(stiffness = 300f),
        label = "progress"
    )

    val progressColor by animateColorAsState(
        targetValue = when {
            usageRatio >= 1.0f -> CrimsonDanger
            usageRatio >= 0.8f -> AmberWarning
            else -> NeonGreenSuccess
        },
        label = "color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Subtle, elegant Circular Meter
                Box(
                    modifier = Modifier.size(68.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = DarkSurface,
                        strokeWidth = 6.dp
                    )
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = progressColor,
                        strokeWidth = 6.dp
                    )
                    Text(
                        text = "${(usageRatio * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.width(18.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "$totalUsage de $totalBudget min usados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = if (totalBlockedAttempts > 0) {
                            "$totalBlockedAttempts bloqueos aplicados hoy"
                        } else {
                            "Manteniendo la concentración"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            if (missingPermissions) {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AmberWarning.copy(alpha = 0.12f))
                        .border(1.dp, AmberWarning.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        .clickable { onConfigurePermissions() }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = AmberWarning,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Completar configuración de seguridad",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberWarning
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForwardIos,
                        contentDescription = null,
                        tint = AmberWarning,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CleanAppItemCard(
    app: MonitoredAppEntity,
    onToggleBlock: () -> Unit,
    onToggleNotification: () -> Unit,
    onSelectLimitMinutes: (Int) -> Unit,
    onOpenCustomMinutesDialog: () -> Unit,
    onSimulateTest: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val isExceeded = app.currentUsageMinutes >= app.dailyLimitMinutes && app.isBlocked
    val progress = (app.currentUsageMinutes.toFloat() / app.dailyLimitMinutes.toFloat()).coerceIn(0f, 1f)
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(stiffness = 250f),
        label = "item_progress"
    )

    val brandColor = resolveBrandColor(app.packageName)
    val strictMin = PlatformRecommendationEngine.getRecommendedMinutes(app.packageName, RestrictionTier.STRICT)
    val balancedMin = PlatformRecommendationEngine.getRecommendedMinutes(app.packageName, RestrictionTier.BALANCED)
    val lenientMin = PlatformRecommendationEngine.getRecommendedMinutes(app.packageName, RestrictionTier.LENIENT)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                if (isExceeded) CrimsonDanger.copy(alpha = 0.5f) else CardBorder,
                RoundedCornerShape(18.dp)
            )
            .testTag("app_card_${app.packageName}"),
        colors = CardDefaults.cardColors(
            containerColor = CardSurface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Minimalist Avatar Indicator
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(brandColor.copy(alpha = 0.12f))
                        .border(1.dp, brandColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = app.appName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = brandColor
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = app.appName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                        if (isExceeded) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "• Agotado",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonDanger
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${app.currentUsageMinutes} de ${app.dailyLimitMinutes} min hoy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                // Clean iOS-style Switch
                Switch(
                    checked = app.isBlocked,
                    onCheckedChange = { onToggleBlock() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = TextPrimary,
                        checkedTrackColor = CrimsonDanger,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurface
                    )
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Clean Minimal Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(CircleShape),
                color = if (isExceeded) CrimsonDanger else if (progress > 0.75f) AmberWarning else brandColor,
                trackColor = DarkSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Subdued Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Notifications Mute Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (app.blockNotifications) DarkSurface else Color.Transparent)
                        .border(
                            1.dp,
                            if (app.blockNotifications) CardBorderHover else Color.Transparent,
                            RoundedCornerShape(10.dp)
                        )
                        .clickable { onToggleNotification() }
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = if (app.blockNotifications) AmberWarning else TextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (app.blockNotifications) "Sin notificaciones" else "Con notificaciones",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (app.blockNotifications) TextPrimary else TextMuted
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Quick Limit Tune
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface)
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                            .clickable { expanded = !expanded }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${app.dailyLimitMinutes}m",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }

                    // Test Intercept Trigger
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface)
                            .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                            .clickable { onSimulateTest() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Probar",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Smooth expandable chips with 3 tiers + custom option
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(animationSpec = spring(stiffness = 300f)) + fadeIn(animationSpec = tween(150)),
                exit = shrinkVertically(animationSpec = spring(stiffness = 300f)) + fadeOut(animationSpec = tween(150))
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    Text(
                        text = "Seleccionar nivel de duración o personalizar:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Strict Chip
                        TierChip(
                            label = "Estricto",
                            minutes = strictMin,
                            isSelected = app.dailyLimitMinutes == strictMin,
                            onClick = {
                                onSelectLimitMinutes(strictMin)
                                expanded = false
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Balanced Chip
                        TierChip(
                            label = "Equilibrado",
                            minutes = balancedMin,
                            isSelected = app.dailyLimitMinutes == balancedMin,
                            onClick = {
                                onSelectLimitMinutes(balancedMin)
                                expanded = false
                            },
                            modifier = Modifier.weight(1.1f)
                        )

                        // Lenient Chip
                        TierChip(
                            label = "Flexible",
                            minutes = lenientMin,
                            isSelected = app.dailyLimitMinutes == lenientMin,
                            onClick = {
                                onSelectLimitMinutes(lenientMin)
                                expanded = false
                            },
                            modifier = Modifier.weight(1f)
                        )

                        // Custom Chip
                        Box(
                            modifier = Modifier
                                .weight(0.9f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurface)
                                .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    expanded = false
                                    onOpenCustomMinutesDialog()
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Otro",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TierChip(
    label: String,
    minutes: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) TextPrimary else DarkSurface)
            .border(
                1.dp,
                if (isSelected) TextPrimary else CardBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${minutes}m",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) PitchBlack else TextPrimary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontSize = 9.sp,
                color = if (isSelected) PitchBlack.copy(alpha = 0.8f) else TextMuted
            )
        }
    }
}

@Composable
private fun CustomMinutesInputDialog(
    app: MonitoredAppEntity,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var textValue by remember { mutableStateOf(app.dailyLimitMinutes.toString()) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        title = {
            Text(
                text = "Límite personalizado para ${app.appName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column {
                Text(
                    text = "Introduce cuántos minutos diarios deseas autorizar antes del bloqueo:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedTextField(
                    value = textValue,
                    onValueChange = {
                        textValue = it.filter { char -> char.isDigit() }
                        errorText = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Minutos diarios") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
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

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorText ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = CrimsonDanger
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = textValue.toIntOrNull()
                    if (parsed == null || parsed <= 0) {
                        errorText = "Introduce un valor válido mayor a 0."
                    } else {
                        onConfirm(parsed)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Guardar", color = PitchBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}

@Composable
private fun HumiliatingConfirmationDialog(
    app: MonitoredAppEntity,
    minutes: Int,
    onDismiss: () -> Unit,
    onConfirmAnyway: () -> Unit
) {
    val humiliatingMessage = remember(app.packageName, minutes) {
        PlatformRecommendationEngine.getHumiliatingMessage(app.appName, minutes)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        icon = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(CrimsonDanger.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SentimentVeryDissatisfied,
                    contentDescription = null,
                    tint = CrimsonDanger,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = "¿En serio? ${minutes} minutos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CrimsonDanger,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = humiliatingMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "La recomendación científica para ${app.appName} es de máximo ${PlatformRecommendationEngine.getRecommendedMinutes(app.packageName, RestrictionTier.BALANCED)} min.",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirmAnyway,
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonDanger),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Asumo mi debilidad", color = PitchBlack, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Text("Rectificar y bajar límite", color = TextPrimary)
            }
        }
    )
}

private fun resolveBrandColor(packageName: String): Color {
    val lower = packageName.lowercase()
    return when {
        lower.contains("instagram") -> BrandInstagram
        lower.contains("tiktok") || lower.contains("musically") -> BrandTikTok
        lower.contains("youtube") -> BrandYouTube
        lower.contains("facebook") -> BrandFacebook
        lower.contains("twitter") || lower.contains("x.com") -> BrandTwitter
        lower.contains("reddit") -> BrandReddit
        lower.contains("twitch") -> Color(0xFF9146FF)
        lower.contains("brawlstars") || lower.contains("clash") -> Color(0xFFFFB800)
        lower.contains("roblox") -> Color(0xFFE2231A)
        lower.contains("netflix") -> Color(0xFFE50914)
        lower.contains("game") -> Color(0xFF00E676)
        else -> AccentCyan
    }
}
