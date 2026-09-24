package app.extremefocus.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.extremefocus.domain.PermissionsState
import app.extremefocus.domain.SetupPlan
import app.extremefocus.domain.SetupStep
import app.extremefocus.domain.SetupTier
import app.extremefocus.ui.theme.AmberWarning
import app.extremefocus.ui.theme.CardBorder
import app.extremefocus.ui.theme.CardSurface
import app.extremefocus.ui.theme.DarkSurface
import app.extremefocus.ui.theme.NeonGreenSuccess
import app.extremefocus.ui.theme.PitchBlack
import app.extremefocus.ui.theme.TextMuted
import app.extremefocus.ui.theme.TextPrimary
import app.extremefocus.ui.theme.TextSecondary

private data class StepCopy(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val actionLabel: String = "Activar"
)

private fun copyFor(step: SetupStep): StepCopy = when (step) {
    SetupStep.USAGE_ACCESS -> StepCopy(
        title = "Acceso al uso",
        description = "Sin esto la app no puede saber cuántos minutos llevas en cada aplicación, que es la base de todo lo demás.",
        icon = Icons.Default.BarChart
    )
    SetupStep.ACCESSIBILITY -> StepCopy(
        title = "Detección de apertura",
        description = "Es lo que permite cerrar una app restringida en el instante en que la abres. Sin esto no se bloquea nada.",
        icon = Icons.Default.AccessibilityNew
    )
    SetupStep.BATTERY_UNRESTRICTED -> StepCopy(
        title = "Sin restricción de batería",
        description = "El sistema congela las apps en segundo plano para ahorrar batería. Si lo hace con esta, la vigilancia se para sin avisar.",
        icon = Icons.Default.PowerSettingsNew,
        actionLabel = "Quitar restricción"
    )
    SetupStep.AUTOSTART -> StepCopy(
        title = "Autoarranque (Xiaomi)",
        description = "Tu móvil cierra las apps que no tienen autoarranque permitido. Actívalo para que la protección siga viva tras reiniciar.",
        icon = Icons.Default.RestartAlt,
        actionLabel = "Abrir ajustes de Xiaomi"
    )
    SetupStep.POST_NOTIFICATIONS -> StepCopy(
        title = "Avisos de la app",
        description = "Permite mostrar el aviso permanente de que la protección sigue activa, y alertarte si se cae.",
        icon = Icons.Default.NotificationsActive
    )
    SetupStep.OVERLAY -> StepCopy(
        title = "Superposición",
        description = "Dibuja el reto de fricción por encima de la app bloqueada, para que no puedas esquivarlo volviendo atrás.",
        icon = Icons.Default.Layers
    )
    SetupStep.NOTIFICATION_LISTENER -> StepCopy(
        title = "Silenciar notificaciones",
        description = "Cancela las notificaciones de las apps que limitas: sin el gancho, no hay recaída.",
        icon = Icons.Default.NotificationsOff
    )
    SetupStep.DEVICE_ADMIN -> StepCopy(
        title = "Protección anti-desinstalación",
        description = "Dificulta desinstalar la app en un momento de debilidad. Actívalo solo cuando estés seguro.",
        icon = Icons.Default.AdminPanelSettings
    )
}

@Composable
fun PermissionsGuideScreen(
    state: PermissionsState,
    includeAutostart: Boolean,
    onRequestStep: (SetupStep) -> Unit,
    onBack: () -> Unit
) {
    val scrollState = rememberScrollState()
    val nextStep = SetupPlan.nextStep(state, includeAutostart)
    val canBlock = SetupPlan.canBlock(state)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(scrollState)
    ) {
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
            Text(
                text = "Puesta en marcha",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        StatusCard(canBlock = canBlock, state = state, includeAutostart = includeAutostart)

        Spacer(modifier = Modifier.height(24.dp))

        SectionTitle(
            text = "Imprescindibles",
            subtitle = "Con estos dos ya bloquea. Son los únicos obligatorios."
        )

        SetupStep.entries.filter { it.tier == SetupTier.ESSENTIAL }.forEach { step ->
            Spacer(modifier = Modifier.height(12.dp))
            StepCard(
                step = step,
                isGranted = SetupPlan.isGranted(step, state),
                isNext = step == nextStep,
                onConfigure = { onRequestStep(step) }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        SectionTitle(
            text = "Refuerzo",
            subtitle = if (canBlock) {
                "Opcionales, pero sin ellos el sistema puede parar la vigilancia sin avisarte."
            } else {
                "Disponibles cuando termines lo imprescindible."
            }
        )

        SetupPlan.steps(includeAutostart).filter { it.tier == SetupTier.SHIELD }.forEach { step ->
            Spacer(modifier = Modifier.height(12.dp))
            StepCard(
                step = step,
                isGranted = SetupPlan.isGranted(step, state),
                isNext = step == nextStep,
                onConfigure = { onRequestStep(step) }
            )
        }

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
                        text = "Todo se queda en este teléfono",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Los datos de uso y los eventos se procesan y almacenan solo aquí. No hay servidor ni cuenta.",
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
private fun StatusCard(
    canBlock: Boolean,
    state: PermissionsState,
    includeAutostart: Boolean
) {
    val shieldGranted = SetupPlan.grantedCount(state, SetupTier.SHIELD, includeAutostart)
    val shieldTotal = SetupPlan.totalCount(SetupTier.SHIELD, includeAutostart)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                1.dp,
                if (canBlock) NeonGreenSuccess.copy(alpha = 0.45f) else AmberWarning.copy(alpha = 0.45f),
                RoundedCornerShape(18.dp)
            ),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (canBlock) Icons.Default.Shield else Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = if (canBlock) NeonGreenSuccess else AmberWarning,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = if (canBlock) "Ya está bloqueando" else "Todavía no bloquea nada",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (canBlock) NeonGreenSuccess else AmberWarning
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (canBlock) {
                    "Refuerzo: $shieldGranted de $shieldTotal. Cada uno que añadas se lo pone más difícil al sistema para pararte la vigilancia."
                } else {
                    "Te faltan permisos imprescindibles. Android obliga a concederlos uno a uno: sigue el resaltado y vuelve aquí."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 19.sp
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String, subtitle: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = TextPrimary
    )
    Spacer(modifier = Modifier.height(4.dp))
    Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = TextMuted,
        lineHeight = 17.sp
    )
}

@Composable
private fun StepCard(
    step: SetupStep,
    isGranted: Boolean,
    isNext: Boolean,
    onConfigure: () -> Unit
) {
    val copy = copyFor(step)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                if (isNext) 2.dp else 1.dp,
                if (isNext) AmberWarning else CardBorder,
                RoundedCornerShape(16.dp)
            ),
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
                            if (isGranted) NeonGreenSuccess.copy(alpha = 0.12f) else DarkSurface
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = copy.icon,
                        contentDescription = null,
                        tint = if (isGranted) NeonGreenSuccess else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = copy.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = when {
                            isGranted -> "Listo"
                            isNext -> "Siguiente paso"
                            step == SetupStep.AUTOSTART -> "No se puede comprobar solo"
                            else -> "Pendiente"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = when {
                            isGranted -> NeonGreenSuccess
                            isNext -> AmberWarning
                            else -> TextMuted
                        }
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

            if (!isGranted) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = copy.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(14.dp))
                Button(
                    onClick = onConfigure,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isNext) AmberWarning else TextPrimary
                    )
                ) {
                    Text(
                        text = copy.actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = PitchBlack
                    )
                }
            }
        }
    }
}
