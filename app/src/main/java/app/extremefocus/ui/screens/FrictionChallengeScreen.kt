package app.extremefocus.ui.screens

import android.os.SystemClock
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.extremefocus.ui.theme.AccentCyan
import app.extremefocus.ui.theme.AmberWarning
import app.extremefocus.ui.theme.CardBorder
import app.extremefocus.ui.theme.CardSurface
import app.extremefocus.ui.theme.CrimsonDanger
import app.extremefocus.ui.theme.DarkSurface
import app.extremefocus.ui.theme.NeonGreenSuccess
import app.extremefocus.ui.theme.PitchBlack
import app.extremefocus.ui.theme.TextMuted
import app.extremefocus.ui.theme.TextPrimary
import app.extremefocus.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun FrictionChallengeScreen(
    packageName: String,
    appName: String,
    challengeType: ChallengeType,
    onChallengeCompleted: (Int) -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onCancel,
                modifier = Modifier
                    .size(40.dp)
                    .background(CardSurface, CircleShape)
                    .border(1.dp, CardBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Cancelar",
                    tint = TextPrimary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "PRUEBA DE FRICCIÓN COGNITIVA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp,
                    color = CrimsonDanger
                )
                Text(
                    text = "Para desbloquear $appName (3 minutos)",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (challengeType) {
            ChallengeType.MANIFESTO_TRANSCRIPTION -> {
                ManifestoTranscriptionChallenge(onSuccess = { onChallengeCompleted(3) })
            }
            ChallengeType.ABYSS_TOUCH -> {
                AbyssTouchChallenge(onSuccess = { onChallengeCompleted(3) })
            }
            ChallengeType.MONOTONY_GRID -> {
                MonotonyGridChallenge(onSuccess = { onChallengeCompleted(3) })
            }
            ChallengeType.MONOTONY_TASK -> {
                MonotonyGridChallenge(onSuccess = { onChallengeCompleted(15) })
            }
        }
    }
}

/**
 * Challenge 1: The Transcription of Shame
 * Requires typing a strict self-reflective sentence manually.
 *
 * ANTI-CHEAT DEFENSES:
 * 1. Blocks Voice-to-Text / Audio dictation burst inputs (human typing speed cap).
 * 2. Blocks Clipboard Paste (prevent multi-character instant dump).
 * 3. Disables Auto-correct, text predictions and suggestions.
 * 4. Any discrepancy or burst input immediately resets progress to 0.
 */
@Composable
fun ManifestoTranscriptionChallenge(onSuccess: () -> Unit) {
    val targetPhrase = "Reconozco que estoy perdiendo el control de mi tiempo y elijo voluntariamente someterme a esta tediosa prueba por tres miserables minutos de distraccion vacia."
    var userText by remember { mutableStateOf("") }
    var errorCount by remember { mutableIntStateOf(0) }
    var cheatAttemptDetected by remember { mutableStateOf(false) }
    var cheatMessage by remember { mutableStateOf("") }
    var lastKeystrokeTime by remember { mutableLongStateOf(0L) }
    var isDone by remember { mutableStateOf(false) }

    val matchesSoFar = targetPhrase.startsWith(userText)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRANSCRIBE EL MANIFIESTO",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = AmberWarning
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(CrimsonDanger.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                        .border(1.dp, CrimsonDanger.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MicOff,
                        contentDescription = "Dictado por voz prohibido",
                        tint = CrimsonDanger,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "VOZ / PEGADO BLOQUEADOS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = CrimsonDanger
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Escribe con exactitud la frase en tu teclado físico o digital. La entrada por voz (dictado), el pegado de texto y los atajos automáticos están prohibidos y reinician el contador a cero:",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Target Text Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(10.dp))
                    .border(1.dp, CardBorder, RoundedCornerShape(10.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = targetPhrase,
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 20.sp,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Anti-Cheat Hardened Input Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(DarkSurface, RoundedCornerShape(10.dp))
                    .border(
                        1.dp,
                        if (cheatAttemptDetected) CrimsonDanger
                        else if (matchesSoFar) NeonGreenSuccess
                        else CardBorder,
                        RoundedCornerShape(10.dp)
                    )
                    .padding(12.dp)
            ) {
                if (userText.isEmpty()) {
                    Text(
                        text = "Escribe carácter por carácter manualmente...",
                        fontSize = 12.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                BasicTextField(
                    value = userText,
                    onValueChange = { input ->
                        val now = SystemClock.elapsedRealtime()
                        val diff = input.length - userText.length

                        // ANTI-CHEAT 1: Detect Burst Injection (Dictation / Paste / Macro)
                        // Dictation/voice-to-text drops multiple words (often > 2-3 characters) in a single instant (<70ms)
                        if (diff > 2) {
                            cheatAttemptDetected = true
                            cheatMessage = "¡Intento de atajo detectado! (Dictado por voz o Pegado prohibido). Sanción: reinicio."
                            errorCount++
                            userText = ""
                            lastKeystrokeTime = now
                            return@BasicTextField
                        }

                        // ANTI-CHEAT 2: Machine Inhuman Keystroke Speed (<35ms per character)
                        if (diff > 0 && lastKeystrokeTime > 0L && (now - lastKeystrokeTime) < 35L) {
                            cheatAttemptDetected = true
                            cheatMessage = "Velocidad de tecleo no humana detectada. Reiniciando."
                            errorCount++
                            userText = ""
                            lastKeystrokeTime = now
                            return@BasicTextField
                        }

                        lastKeystrokeTime = now

                        // Standard accuracy check
                        if (targetPhrase.startsWith(input)) {
                            userText = input
                            cheatAttemptDetected = false
                            if (input == targetPhrase) {
                                isDone = true
                                onSuccess()
                            }
                        } else {
                            errorCount++
                            cheatAttemptDetected = false
                            // Reset input on spelling error
                            userText = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("transcription_input"),
                    textStyle = TextStyle(
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 20.sp
                    ),
                    cursorBrush = SolidColor(AmberWarning),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false,
                        keyboardType = KeyboardType.Password, // Password type hides voice-to-text / suggestions on Gboard & AOSP keyboards
                        imeAction = ImeAction.Done
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (cheatAttemptDetected) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = CrimsonDanger,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = cheatMessage,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CrimsonDanger
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Progreso: ${userText.length} / ${targetPhrase.length}",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (matchesSoFar) NeonGreenSuccess else CrimsonDanger
                )
                if (errorCount > 0) {
                    Text(
                        text = "Fallos / Intentos: $errorCount",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CrimsonDanger
                    )
                }
            }

            if (isDone) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "PRUEBA SUPERADA. 3 MINUTOS OTORGADOS.",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = NeonGreenSuccess
                )
            }
        }
    }
}

/**
 * Challenge 2: The Abyss Touch
 * Requires holding a continuous touch for 45 seconds without letting go.
 */
@Composable
fun AbyssTouchChallenge(onSuccess: () -> Unit) {
    val totalRequiredSeconds = 45f // 45 seconds continuous hold
    var isPressing by remember { mutableStateOf(false) }
    var secondsHeld by remember { mutableStateOf(0f) }
    var completed by remember { mutableStateOf(false) }

    LaunchedEffect(isPressing) {
        if (isPressing && !completed) {
            val startTime = SystemClock.elapsedRealtime()
            val initialSeconds = secondsHeld
            while (isPressing && secondsHeld < totalRequiredSeconds) {
                delay(100)
                val elapsed = (SystemClock.elapsedRealtime() - startTime) / 1000f
                secondsHeld = (initialSeconds + elapsed).coerceAtMost(totalRequiredSeconds)
                if (secondsHeld >= totalRequiredSeconds) {
                    completed = true
                    onSuccess()
                    break
                }
            }
        } else if (!isPressing && !completed) {
            if (secondsHeld > 0f) {
                secondsHeld = 0f
            }
        }
    }

    val progress = (secondsHeld / totalRequiredSeconds).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LA ESPERA DEL ABISMO",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = CrimsonDanger
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Mantén presionado el centro durante ${totalRequiredSeconds.toInt()} segundos continuos. Si sueltas el dedo antes de tiempo, el contador volverá a CERO.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Large Touch Target
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(if (isPressing) CrimsonDanger.copy(alpha = 0.2f) else DarkSurface)
                    .border(2.dp, if (isPressing) CrimsonDanger else CardBorder, CircleShape)
                    .pointerInput(completed) {
                        if (!completed) {
                            detectTapGestures(
                                onPress = {
                                    isPressing = true
                                    tryAwaitRelease()
                                    isPressing = false
                                }
                            )
                        }
                    }
                    .testTag("abyss_hold_btn"),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(190.dp),
                    color = if (completed) NeonGreenSuccess else CrimsonDanger,
                    trackColor = PitchBlack,
                    strokeWidth = 6.dp
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = if (completed) Icons.Default.CheckCircle else Icons.Default.TouchApp,
                        contentDescription = null,
                        tint = if (completed) NeonGreenSuccess else if (isPressing) CrimsonDanger else TextMuted,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (completed) "SUPERADO" else "${secondsHeld.toInt()}s / ${totalRequiredSeconds.toInt()}s",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    )
                    Text(
                        text = if (completed) "3 MIN OTORGADOS" else if (isPressing) "NO SUELTES..." else "MANTÉN PULSADO",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        color = if (isPressing) AmberWarning else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = if (isPressing) "Siente cómo cada segundo de tu existencia transcurre en la nada..." else "Pulsa y no retires el contacto.",
                fontSize = 11.sp,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Challenge 3: Monotony Grid
 * Requires clicking a sequence of monotonous numbers in strict order up to 50.
 */
@Composable
fun MonotonyGridChallenge(onSuccess: () -> Unit) {
    val items = remember {
        (1..50).shuffled()
    }
    var currentTarget by remember { mutableIntStateOf(1) }
    var errors by remember { mutableIntStateOf(0) }
    var completed by remember { mutableStateOf(false) }

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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Secuencia de monotonía",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "$currentTarget / 50",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = AccentCyan
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Presiona los números en orden ascendente estricto del 1 al 50. Un solo fallo o despiste te reinicia al número 1.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Objetivo actual: ",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                    Text(
                        text = "$currentTarget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = AmberWarning
                    )
                }
                if (errors > 0) {
                    Text(
                        text = "Reinicios por error: $errors",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CrimsonDanger
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(items) { _, num ->
                    val isAlreadyClicked = num < currentTarget
                    val isCurrent = num == currentTarget
                    Box(
                        modifier = Modifier
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isAlreadyClicked -> DarkSurface.copy(alpha = 0.4f)
                                    isCurrent -> AccentCyan.copy(alpha = 0.15f)
                                    else -> DarkSurface
                                }
                            )
                            .border(
                                1.dp,
                                when {
                                    isAlreadyClicked -> Color.Transparent
                                    isCurrent -> AccentCyan
                                    else -> CardBorder
                                },
                                RoundedCornerShape(8.dp)
                            )
                            .clickable(enabled = !completed && !isAlreadyClicked) {
                                if (num == currentTarget) {
                                    if (currentTarget == 50) {
                                        completed = true
                                        onSuccess()
                                    } else {
                                        currentTarget++
                                    }
                                } else {
                                    errors++
                                    currentTarget = 1
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isAlreadyClicked) "✓" else num.toString(),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                            color = when {
                                isAlreadyClicked -> NeonGreenSuccess
                                isCurrent -> TextPrimary
                                else -> TextSecondary
                            }
                        )
                    }
                }
            }

            if (completed) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Prueba de paciencia superada. 3 minutos otorgados.",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = NeonGreenSuccess
                )
            }
        }
    }
}
