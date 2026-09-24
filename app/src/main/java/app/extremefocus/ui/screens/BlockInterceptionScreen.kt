package app.extremefocus.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.extremefocus.domain.ToughLoveQuoteEngine
import app.extremefocus.ui.theme.AmberWarning
import app.extremefocus.ui.theme.CardBorder
import app.extremefocus.ui.theme.CardSurface
import app.extremefocus.ui.theme.CrimsonDanger
import app.extremefocus.ui.theme.DarkSurface
import app.extremefocus.ui.theme.PitchBlack
import app.extremefocus.ui.theme.TextMuted
import app.extremefocus.ui.theme.TextPrimary
import app.extremefocus.ui.theme.TextSecondary

@Composable
fun BlockInterceptionScreen(
    packageName: String,
    appName: String,
    minutesSpent: Int,
    onCloseToHome: () -> Unit,
    onStartChallenge: (ChallengeType) -> Unit
) {
    val quote = remember(packageName) {
        ToughLoveQuoteEngine.getQuoteForApp(appName, minutesSpent)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Minimal Icon Badge
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(DarkSurface)
                    .border(1.dp, CardBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LockClock,
                    contentDescription = null,
                    tint = CrimsonDanger,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Límite diario alcanzado",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = CrimsonDanger
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = appName,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "$minutesSpent minutos consumidos hoy",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Human Quote Box
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = CardSurface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = quote.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "\"${quote.body}\"",
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 22.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Primary Clean CTA: Exit
            Button(
                onClick = onCloseToHome,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("surrender_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = TextPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Cerrar y volver a mis tareas",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = PitchBlack
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "O gánate 3 minutos",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ChallengeChip(
                    text = "Declarar intención",
                    modifier = Modifier.weight(1f),
                    onClick = { onStartChallenge(ChallengeType.DECLARE_INTENT) }
                )
                Spacer(modifier = Modifier.width(10.dp))
                ChallengeChip(
                    text = "Mantener pulsado",
                    modifier = Modifier.weight(1f),
                    onClick = { onStartChallenge(ChallengeType.ABYSS_TOUCH) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                ChallengeChip(
                    text = "Transcribir",
                    modifier = Modifier.weight(1f),
                    onClick = { onStartChallenge(ChallengeType.MANIFESTO_TRANSCRIPTION) }
                )
                Spacer(modifier = Modifier.width(10.dp))
                ChallengeChip(
                    text = "Rejilla monótona",
                    modifier = Modifier.weight(1f),
                    onClick = { onStartChallenge(ChallengeType.MONOTONY_GRID) }
                )
            }
        }

        // Top right close button
        IconButton(
            onClick = onCloseToHome,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(38.dp)
                .background(DarkSurface, CircleShape)
                .border(1.dp, CardBorder, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = TextSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ChallengeChip(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(DarkSurface)
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary
        )
    }
}
