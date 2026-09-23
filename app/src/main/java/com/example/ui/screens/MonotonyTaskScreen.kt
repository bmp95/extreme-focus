package com.example.ui.screens

import android.os.SystemClock
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentPasteOff
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
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
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CardSurface
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NeonGreenSuccess
import com.example.ui.theme.PitchBlack
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

enum class MonotonyMode {
    MANUAL_50_ITEMS,
    TEDIOUS_LOGIC_PUZZLE
}

@Composable
fun MonotonyTaskScreen(
    packageName: String,
    appName: String,
    onCompletedWhitelist15Min: () -> Unit,
    onCancel: () -> Unit
) {
    var selectedMode by remember { mutableStateOf(MonotonyMode.MANUAL_50_ITEMS) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PitchBlack)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // Header
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
                    contentDescription = "Volver",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TAREA DE MONOTONÍA",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp,
                        color = AmberWarning
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(NeonGreenSuccess.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                            .border(1.dp, NeonGreenSuccess.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+15 MIN VENTANA",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreenSuccess
                        )
                    }
                }
                Text(
                    text = "Gana 15 minutos en $appName mediante esfuerzo consciente",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Mode Selector Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurface)
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            val isManual = selectedMode == MonotonyMode.MANUAL_50_ITEMS
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isManual) CardSurface else Color.Transparent)
                    .border(1.dp, if (isManual) CardBorder else Color.Transparent, RoundedCornerShape(9.dp))
                    .clickable { selectedMode = MonotonyMode.MANUAL_50_ITEMS }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ListAlt,
                        contentDescription = null,
                        tint = if (isManual) TextPrimary else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "50 Entradas manuales",
                        fontSize = 12.sp,
                        fontWeight = if (isManual) FontWeight.Bold else FontWeight.Medium,
                        color = if (isManual) TextPrimary else TextMuted
                    )
                }
            }

            val isPuzzle = selectedMode == MonotonyMode.TEDIOUS_LOGIC_PUZZLE
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isPuzzle) CardSurface else Color.Transparent)
                    .border(1.dp, if (isPuzzle) CardBorder else Color.Transparent, RoundedCornerShape(9.dp))
                    .clickable { selectedMode = MonotonyMode.TEDIOUS_LOGIC_PUZZLE }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Extension,
                        contentDescription = null,
                        tint = if (isPuzzle) TextPrimary else TextMuted,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Puzle Lógico Tedioso",
                        fontSize = 12.sp,
                        fontWeight = if (isPuzzle) FontWeight.Bold else FontWeight.Medium,
                        color = if (isPuzzle) TextPrimary else TextMuted
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        AnimatedContent(targetState = selectedMode, label = "monotony_mode_transition") { mode ->
            when (mode) {
                MonotonyMode.MANUAL_50_ITEMS -> {
                    Manual50ItemsChallenge(
                        appName = appName,
                        onSuccess = onCompletedWhitelist15Min
                    )
                }
                MonotonyMode.TEDIOUS_LOGIC_PUZZLE -> {
                    TediousLogicPuzzleChallenge(
                        appName = appName,
                        onSuccess = onCompletedWhitelist15Min
                    )
                }
            }
        }
    }
}

/**
 * TAREA 1: Entrada manual de 50 ítems.
 *
 * Exige teclear 50 ítems o razones/acciones reflexivas distintas.
 * Mecanismos anti-trampa estrictos:
 * 1. Prohibido Pegar desde portapapeles (detección de inserción en ráfaga).
 * 2. Mínimo 3 caracteres legibles por ítem.
 * 3. No permite duplicar ítems ya ingresados.
 * 4. Contador de progreso reactivo continuo (0/50 -> 50/50).
 */
@Composable
fun Manual50ItemsChallenge(
    appName: String,
    onSuccess: () -> Unit
) {
    val items = remember { mutableStateListOf<String>() }
    var currentInput by remember { mutableStateOf(TextFieldValue("")) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var lastKeystrokeTime by remember { mutableLongStateOf(0L) }
    var completed by remember { mutableStateOf(false) }

    val listState = rememberLazyListState()

    LaunchedEffect(items.size) {
        if (items.size >= 50 && !completed) {
            completed = true
            delay(600)
            onSuccess()
        }
        if (items.isNotEmpty()) {
            listState.animateScrollToItem(items.size - 1)
        }
    }

    val progress = (items.size / 50f).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            // Progress Bar & Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PROGRESO DE ENTRADAS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Text(
                        text = "${items.size} de 50 ítems completados",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (items.size >= 50) NeonGreenSuccess else TextPrimary
                    )
                }

                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(DarkSurface)
                        .border(
                            1.dp,
                            if (items.size >= 50) NeonGreenSuccess else CardBorder,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = if (items.size >= 50) NeonGreenSuccess else AccentCyan
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (items.size >= 50) NeonGreenSuccess else AccentCyan,
                trackColor = DarkSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Teclea 50 ítems reflexivos, tareas pendientes o motivos conscientes. No se permite pegar texto ni repetir ítems.",
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Input Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .border(
                            1.dp,
                            if (errorMessage != null) CrimsonDanger else CardBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = currentInput,
                        onValueChange = { newValue ->
                            errorMessage = null
                            val now = SystemClock.uptimeMillis()
                            val textDelta = newValue.text.length - currentInput.text.length

                            // Anti-Paste Protection: detect bursts of > 3 chars typed in < 50ms
                            if (textDelta > 3 && (now - lastKeystrokeTime < 50 || lastKeystrokeTime == 0L)) {
                                errorMessage = "¡Portapapeles denegado! Escribe cada ítem manualmente."
                                currentInput = TextFieldValue("")
                                return@BasicTextField
                            }

                            lastKeystrokeTime = now
                            currentInput = newValue
                        },
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        cursorBrush = SolidColor(AccentCyan),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            autoCorrectEnabled = false,
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                submitItem(
                                    currentInput.text,
                                    items,
                                    onSuccessSubmit = {
                                        currentInput = TextFieldValue("")
                                        errorMessage = null
                                    },
                                    onError = { err -> errorMessage = err }
                                )
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("monotony_item_input")
                    )

                    if (currentInput.text.isEmpty()) {
                        Text(
                            text = "Ítem #${items.size + 1} (ej. Leer 10 págs, terminar reporte...)",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        submitItem(
                            currentInput.text,
                            items,
                            onSuccessSubmit = {
                                currentInput = TextFieldValue("")
                                errorMessage = null
                            },
                            onError = { err -> errorMessage = err }
                        )
                    },
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("monotony_add_btn"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentInput.text.isNotBlank()) TextPrimary else DarkSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = currentInput.text.isNotBlank() && items.size < 50
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Añadir ítem",
                        tint = if (currentInput.text.isNotBlank()) PitchBlack else TextMuted
                    )
                }
            }

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = CrimsonDanger,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = errorMessage ?: "",
                        fontSize = 11.sp,
                        color = CrimsonDanger,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Items List
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(DarkSurface.copy(alpha = 0.5f))
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                    .padding(8.dp)
            ) {
                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.ContentPasteOff,
                                contentDescription = null,
                                tint = TextMuted,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sin ítems aún. Teclea el ítem #1 para empezar.",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(4.dp)
                    ) {
                        itemsIndexed(items) { index, itemText ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurface)
                                    .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = AccentCyan
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = itemText,
                                    fontSize = 13.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = NeonGreenSuccess,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (completed) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreenSuccess.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonGreenSuccess,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¡50 ítems registrados! Desbloqueando 15 minutos en $appName...",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreenSuccess
                    )
                }
            }
        }
    }
}

private fun submitItem(
    rawText: String,
    itemsList: MutableList<String>,
    onSuccessSubmit: () -> Unit,
    onError: (String) -> Unit
) {
    val clean = rawText.trim()
    if (clean.length < 3) {
        onError("El ítem debe tener al menos 3 caracteres significativos.")
        return
    }

    if (itemsList.any { it.equals(clean, ignoreCase = true) }) {
        onError("Este ítem ya ha sido registrado. Cada entrada debe ser única.")
        return
    }

    itemsList.add(clean)
    onSuccessSubmit()
}

/**
 * TAREA 2: Puzle Lógico Tedioso.
 *
 * Consiste en resolver una serie deliberadamente metódica y fría de 15 pasos
 * matemáticos y de paridad combinatoria. Cada paso requiere atención cognitiva
 * para romper el bucle dopaminérgico impulsivo.
 * Un solo error penaliza con +2 pasos adicionales o resetea el paso actual.
 */
data class LogicPuzzleStep(
    val id: Int,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String
)

val TEDIOUS_PUZZLES = listOf(
    LogicPuzzleStep(
        id = 1,
        prompt = "¿Cuál es el siguiente número primo mayor estricto que 47?",
        options = listOf("49", "51", "53", "57"),
        correctIndex = 2,
        explanation = "49=7x7, 51=3x17. El siguiente primo es 53."
    ),
    LogicPuzzleStep(
        id = 2,
        prompt = "Calcula la paridad binaria: (1011 XOR 1100) en base decimal:",
        options = listOf("5", "7", "3", "9"),
        correctIndex = 1,
        explanation = "1011 XOR 1100 = 0111 en binario = 7 en decimal."
    ),
    LogicPuzzleStep(
        id = 3,
        prompt = "Si todos los Zorgs son Grims y ningún Grim es Blip, ¿qué afirmación es necesariamente cierta?",
        options = listOf("Algún Zorg es Blip", "Ningún Zorg es Blip", "Todos los Blips son Zorgs", "No se puede saber"),
        correctIndex = 1,
        explanation = "Dado que los Zorgs están dentro de Grims y ningún Grim es Blip, ningún Zorg puede ser Blip."
    ),
    LogicPuzzleStep(
        id = 4,
        prompt = "Encuentra el valor de x: 3x - 17 = 46",
        options = listOf("19", "21", "23", "25"),
        correctIndex = 1,
        explanation = "3x = 63 => x = 21."
    ),
    LogicPuzzleStep(
        id = 5,
        prompt = "Secuencia lógica: 2, 6, 12, 20, 30, ¿cuál sigue?",
        options = listOf("40", "42", "44", "46"),
        correctIndex = 1,
        explanation = "Diferencias sucesivas: +4, +6, +8, +10 => +12 = 42."
    ),
    LogicPuzzleStep(
        id = 6,
        prompt = "¿Cuántos segundos exactos hay en 2 horas y 17 minutos?",
        options = listOf("8,120 s", "8,220 s", "8,320 s", "8,420 s"),
        correctIndex = 1,
        explanation = "2h = 7200s; 17m = 1020s. Total = 8220 segundos."
    ),
    LogicPuzzleStep(
        id = 7,
        prompt = "Si una tarea requiere 45 minutos y te quedan 3 periodos Pomodoro de 25 min, ¿cuánto tiempo libre te sobrará?",
        options = listOf("15 min", "20 min", "30 min", "40 min"),
        correctIndex = 2,
        explanation = "3 x 25 = 75 min totales. 75 - 45 = 30 min libres."
    ),
    LogicPuzzleStep(
        id = 8,
        prompt = "¿Cuál es el valor de 2^7 - 3^3?",
        options = listOf("99", "101", "103", "105"),
        correctIndex = 1,
        explanation = "128 - 27 = 101."
    ),
    LogicPuzzleStep(
        id = 9,
        prompt = "Si A es mayor que B, C es menor que B, y D es mayor que A, ¿cuál es el menor de todos?",
        options = listOf("A", "B", "C", "D"),
        correctIndex = 2,
        explanation = "D > A > B > C. El menor es C."
    ),
    LogicPuzzleStep(
        id = 10,
        prompt = "¿Cuál es la suma de los primeros 10 números naturales (1 al 10)?",
        options = listOf("45", "50", "55", "60"),
        correctIndex = 2,
        explanation = "(10 * 11) / 2 = 55."
    ),
    LogicPuzzleStep(
        id = 11,
        prompt = "Negación lógica de 'Todos los días pierdo el tiempo en el móvil':",
        options = listOf("Ningún día pierdo el tiempo", "Al menos un día no pierdo el tiempo", "Siempre aprovecho el tiempo", "Casi nunca pierdo el tiempo"),
        correctIndex = 1,
        explanation = "La negación de un universal 'Para todo x, P(x)' es el existencial 'Existe al menos un x tal que NO P(x)'."
    ),
    LogicPuzzleStep(
        id = 12,
        prompt = "¿Cuál es el residuo de dividir 158 entre 7?",
        options = listOf("2", "3", "4", "5"),
        correctIndex = 2,
        explanation = "7 * 22 = 154. 158 - 154 = residuo 4."
    ),
    LogicPuzzleStep(
        id = 13,
        prompt = "Secuencia de letras: B, E, H, K, ¿cuál sigue en el alfabeto?",
        options = listOf("M", "N", "O", "P"),
        correctIndex = 1,
        explanation = "Salto de 3 en 3 letras: B (+3) E (+3) H (+3) K (+3) N."
    ),
    LogicPuzzleStep(
        id = 14,
        prompt = "Si inviertes 15 minutos en Instagram ahora, ¿cuántos minutos te restan de una jornada laboral de 8 horas (480 min)?",
        options = listOf("455 min", "460 min", "465 min", "470 min"),
        correctIndex = 2,
        explanation = "480 - 15 = 465 minutos."
    ),
    LogicPuzzleStep(
        id = 15,
        prompt = "Último paso lógico: ¿El autocontrol inmediato supera a la gratificación instantánea?",
        options = listOf("No, la dopamina manda", "Sí, la disciplina forja el destino", "Es indiferente", "Solo a veces"),
        correctIndex = 1,
        explanation = "La disciplina siempre supera a la distracción vacía."
    )
)

@Composable
fun TediousLogicPuzzleChallenge(
    appName: String,
    onSuccess: () -> Unit
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    var errorsCount by remember { mutableIntStateOf(0) }
    var lastErrorExplanation by remember { mutableStateOf<String?>(null) }
    var completed by remember { mutableStateOf(false) }

    val totalSteps = TEDIOUS_PUZZLES.size
    val currentPuzzle = TEDIOUS_PUZZLES.getOrNull(currentStepIndex)
    val progress = (currentStepIndex.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)

    LaunchedEffect(currentStepIndex) {
        if (currentStepIndex >= totalSteps && !completed) {
            completed = true
            delay(500)
            onSuccess()
        }
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardSurface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            // Header with Progress Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PUZLE LÓGICO METÓDICO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                    Text(
                        text = "Paso ${minOf(currentStepIndex + 1, totalSteps)} de $totalSteps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (completed) NeonGreenSuccess else TextPrimary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (errorsCount > 0) {
                        Text(
                            text = "Fallos: $errorsCount",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonDanger,
                            modifier = Modifier
                                .background(CrimsonDanger.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(DarkSurface)
                            .border(
                                1.dp,
                                if (completed) NeonGreenSuccess else CardBorder,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (completed) NeonGreenSuccess else AmberWarning
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (completed) NeonGreenSuccess else AmberWarning,
                trackColor = DarkSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Resuelve cada problema deductivo con calma. Un fallo penaliza tu avance para forzar la sobriedad cognitiva.",
                fontSize = 12.sp,
                color = TextMuted,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (currentPuzzle != null && !completed) {
                // Question Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(DarkSurface)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "PROBLEMA #${currentStepIndex + 1}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = AccentCyan
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentPuzzle.prompt,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            lineHeight = 22.sp
                        )
                    }
                }

                if (lastErrorExplanation != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(CrimsonDanger.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .border(1.dp, CrimsonDanger.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = CrimsonDanger,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = lastErrorExplanation ?: "",
                            fontSize = 12.sp,
                            color = CrimsonDanger,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Options List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    currentPuzzle.options.forEachIndexed { optIndex, optionText ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DarkSurface)
                                .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                                .clickable {
                                    if (optIndex == currentPuzzle.correctIndex) {
                                        lastErrorExplanation = null
                                        currentStepIndex++
                                    } else {
                                        errorsCount++
                                        lastErrorExplanation = "Incorrecto. ${currentPuzzle.explanation}"
                                        // Slight step rollback if user errs
                                        if (currentStepIndex > 0) {
                                            currentStepIndex--
                                        }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 14.dp)
                                .testTag("puzzle_option_$optIndex")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(CardSurface)
                                        .border(1.dp, CardBorder, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = ('A' + optIndex).toString(),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = TextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = optionText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }

            if (completed) {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(NeonGreenSuccess.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = NeonGreenSuccess,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "¡Puzle lógico superado! Otorgando 15 minutos en $appName...",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreenSuccess
                    )
                }
            }
        }
    }
}
