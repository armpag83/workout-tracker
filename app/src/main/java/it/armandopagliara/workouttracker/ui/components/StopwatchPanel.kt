// DEV_1.0.2
package it.armandopagliara.workouttracker.ui.components

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Pannello Cronometro / Timer con precisione al decimo di secondo, supporto Tablet e callback di completamento */
@Composable
fun StopwatchPanel(
    targetRecuperoSeconds: Int?,
    selectedExerciseId: String?,
    onRunningStateChange: (Boolean) -> Unit = {},
    onTimerFinished: (() -> Unit)? = null
) {
    // Rilevamento Tablet (+20% scala)
    val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600
    val scale = if (isTablet) 1.20f else 1.0f

    var isTimerMode by remember { mutableStateOf(false) }
    var timeInTenths by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var isBlinking by remember { mutableStateOf(false) }
    var blinkState by remember { mutableStateOf(false) }

    var isSoundEnabled by rememberSaveable { mutableStateOf(true) }

    // Comunica lo stato di esecuzione all'esterno
    LaunchedEffect(isRunning, isTimerMode) {
        onRunningStateChange(isRunning && isTimerMode)
    }

    // Si attiva al cambio esercizio o alla modifica del tempo di recupero
    LaunchedEffect(selectedExerciseId, targetRecuperoSeconds) {
        isRunning = false
        isBlinking = false
        blinkState = false
        if (targetRecuperoSeconds != null && targetRecuperoSeconds > 0) {
            isTimerMode = true
            timeInTenths = targetRecuperoSeconds * 10L
        } else {
            isTimerMode = false
            timeInTenths = 0L
        }
    }

    // Ciclo del Timer / Cronometro al decimo di secondo
    LaunchedEffect(isRunning, isTimerMode) {
        while (isRunning) {
            delay(100L)
            if (isTimerMode) {
                if (timeInTenths > 0) {
                    timeInTenths--
                    if (timeInTenths == 0L) {
                        isRunning = false
                        isBlinking = true
                        onTimerFinished?.invoke() // Avvisa l'Activity per tornare in primo piano
                    }
                } else {
                    isRunning = false
                }
            } else {
                timeInTenths++
            }
        }
    }

    // Allarme visivo (lampeggio rosso) e sonoro
    LaunchedEffect(isBlinking) {
        if (isBlinking) {
            var toneGen: ToneGenerator? = null
            try {
                if (isSoundEnabled) {
                    toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                }
                repeat(5) {
                    blinkState = true
                    if (isSoundEnabled) {
                        repeat(3) {
                            toneGen?.startTone(ToneGenerator.TONE_CDMA_LOW_L, 140)
                            delay(220L)
                        }
                    } else {
                        delay(240L)
                    }
                    blinkState = false
                    delay(250L)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                toneGen?.release()
                blinkState = false
                isBlinking = false
            }
        }
    }

    val minutes = (timeInTenths / 10) / 60
    val seconds = (timeInTenths / 10) % 60
    val tenths = timeInTenths % 10
    val formattedTime = String.format("%02d:%02d.%d", minutes, seconds, tenths)

    val containerColor = if (blinkState) Color(0xFFFF3300) else MaterialTheme.colorScheme.surface
    val textColor = if (blinkState) Color.White else if (isTimerMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary

    Surface(
        color = containerColor,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = (16 * scale).dp, vertical = (12 * scale).dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedTime,
                        fontSize = (30 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    if (isTimerMode) {
                        IconButton(
                            onClick = { isSoundEnabled = !isSoundEnabled },
                            modifier = Modifier
                                .padding(start = (4 * scale).dp)
                                .size((32 * scale).dp)
                        ) {
                            Text(
                                text = if (isSoundEnabled) "🔔" else "🔕",
                                fontSize = (18 * scale).sp
                            )
                        }
                    }
                }

                if (isTimerMode) {
                    Text(
                        text = "Recupero/durata (Timer)",
                        fontSize = (11 * scale).sp,
                        color = if (blinkState) Color.White else MaterialTheme.colorScheme.secondary
                    )
                } else {
                    Text(
                        text = "Cronometro",
                        fontSize = (11 * scale).sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy((6 * scale).dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        isBlinking = false
                        blinkState = false
                        isRunning = true
                    },
                    enabled = !isRunning && (!isTimerMode || timeInTenths > 0),
                    contentPadding = PaddingValues(horizontal = (12 * scale).dp, vertical = (8 * scale).dp)
                ) {
                    Text("START", fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        isRunning = false
                        isBlinking = false
                        blinkState = false
                    },
                    enabled = isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = (12 * scale).dp, vertical = (8 * scale).dp)
                ) {
                    Text("STOP", fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        isRunning = false
                        isBlinking = false
                        blinkState = false

                        if (targetRecuperoSeconds != null && targetRecuperoSeconds > 0) {
                            isTimerMode = true
                            timeInTenths = targetRecuperoSeconds * 10L
                        } else {
                            isTimerMode = false
                            timeInTenths = 0L
                        }
                    },
                    contentPadding = PaddingValues(horizontal = (12 * scale).dp, vertical = (8 * scale).dp)
                ) {
                    Text("RST", fontSize = (12 * scale).sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}