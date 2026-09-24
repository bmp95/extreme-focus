package app.extremefocus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.extremefocus.ui.theme.AmberWarning
import app.extremefocus.ui.theme.CardBorder
import app.extremefocus.ui.theme.CardSurface
import app.extremefocus.ui.theme.DarkSurface
import app.extremefocus.ui.theme.NeonGreenSuccess
import app.extremefocus.ui.theme.PitchBlack
import app.extremefocus.ui.theme.TextMuted
import app.extremefocus.ui.theme.TextPrimary
import app.extremefocus.ui.theme.TextSecondary

@Composable
fun PermissionsGuideScreen(
    state: PermissionsState,
    onRequestUsage: () -> Unit,
    onRequestAccessibility: () -> Unit,
    onRequestOverlay: () -> Unit,
    onRequestNotifications: () -> Unit,
    onRequestPostNotifications: () -> Unit,
    onRequestDeviceAdmin: () -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(scrollState)
    ) {
        // Navigation Header
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
                    text = "Configuración de permisos",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "Permite que el sistema aplique las restricciones",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Card 1: Usage Stats
        PermissionCard(
            title = "Datos de uso de aplicaciones",
            description = "Calcula el tiempo real que pasas en cada app durante el día para advertirte cuando llegues al límite.",
            isGranted = state.hasUsageStats,
            icon = Icons.Default.BarChart,
            onConfigure = onRequestUsage
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Card 2: Accessibility
        PermissionCard(
            title = "Servicio de detección activa",
            description = "Detecta cuando abres una aplicación restringida para cerrar la ventana y redirigirte al inicio de inmediato.",
            isGranted = state.isAccessibilityEnabled,
            icon = Icons.Default.AccessibilityNew,
            onConfigure = onRequestAccessibility
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Card 3: Overlay
        PermissionCard(
            title = "Superposición en pantalla (SYSTEM_ALERT_WINDOW)",
            description = "Mantiene la Tarea de Monotonía y la pantalla de bloqueo flotando de forma ineludible sobre cualquier app bloqueada para evitar atajos.",
            isGranted = state.hasOverlay,
            icon = Icons.Default.Layers,
            onConfigure = onRequestOverlay
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Card 4: notification listener access — cancels other apps' notifications
        PermissionCard(
            title = "Silenciado de notificaciones",
            description = "Filtra las alertas de las apps seleccionadas para evitar interrupciones mientras trabajas.",
            isGranted = state.hasNotificationAccess,
            icon = Icons.Default.NotificationsOff,
            onConfigure = onRequestNotifications
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Card 5: POST_NOTIFICATIONS — shows this app's own status notification
        PermissionCard(
            title = "Aviso de protección activa",
            description = "Permite mostrar el aviso permanente que confirma que la vigilancia sigue en marcha. Sin él no sabrás si la protección se ha detenido.",
            isGranted = state.canPostNotifications,
            icon = Icons.Default.NotificationsActive,
            onConfigure = onRequestPostNotifications
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Card 6: Device Admin
        PermissionCard(
            title = "Protección contra desinstalación",
            description = "Impide desinstalar la app por impulso para mantener tu compromiso en momentos de debilidad.",
            isGranted = state.isDeviceAdminActive,
            icon = Icons.Default.AdminPanelSettings,
            onConfigure = onRequestDeviceAdmin
        )

        Spacer(modifier = Modifier.height(28.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = NeonGreenSuccess,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Privacidad local en el dispositivo",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Todos los datos de uso y eventos se procesan y almacenan exclusivamente en la memoria de este teléfono.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onConfigure: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            if (isGranted) NeonGreenSuccess.copy(alpha = 0.12f)
                            else DarkSurface
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isGranted) NeonGreenSuccess else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (isGranted) "Activado correctamente" else "Requiere activación",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isGranted) NeonGreenSuccess else AmberWarning
                    )
                }

                if (isGranted) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Concedido",
                        tint = NeonGreenSuccess,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            if (!isGranted) {
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onConfigure,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TextPrimary
                    )
                ) {
                    Text(
                        text = "Configurar permiso",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = PitchBlack
                    )
                }
            }
        }
    }
}
