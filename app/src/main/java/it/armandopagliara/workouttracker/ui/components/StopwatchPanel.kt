// DEV_0.2.10
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

/** Pannello Cronometro / Timer sincronizzato con l'esercizio e il relativo tempo di recupero */
@Composable
fun StopwatchPanel(
    targetRecuperoSeconds: Int?,
    selectedExerciseId: String?,
    onRunningStateChange: (Boolean) -> Unit = {}
) {
    var isTimerMode by remember { mutableStateOf(false) }
    var timeInTenths by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var isBlinking by remember { mutableStateOf(false) }
    var blinkState by remember { mutableStateOf(false) }

    var isSoundEnabled by rememberSaveable { mutableStateOf(true) }

    // Comunica lo stato di esecuzione del Timer all'esterno
    LaunchedEffect(isRunning, isTimerMode) {
        onRunningStateChange(isRunning && isTimerMode)
    }

    // Si attiva a OGNI cambio di esercizio O modifica del tempo di recupero
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

    LaunchedEffect(isRunning, isTimerMode) {
        while (isRunning) {
            delay(100L)
            if (isTimerMode) {
                if (timeInTenths > 0) {
                    timeInTenths--
                    if (timeInTenths == 0L) {
                        isRunning = false
                        isBlinking = true
                    }
                } else {
                    isRunning = false
                }
            } else {
                timeInTenths++
            }
        }
    }

    // Allarme visivo e sonoro (5 lampeggi con 3 bip veloci)
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
                            toneGen?.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 50)
                            delay(80L)
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
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedTime,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    if (isTimerMode) {
                        IconButton(
                            onClick = { isSoundEnabled = !isSoundEnabled },
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(32.dp)
                        ) {
                            Text(
                                text = if (isSoundEnabled) "🔔" else "🔕",
                                fontSize = 18.sp
                            )
                        }
                    }
                }

                if (isTimerMode) {
                    Text(
                        text = "Recupero (Timer)",
                        fontSize = 11.sp,
                        color = if (blinkState) Color.White else MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        isBlinking = false
                        blinkState = false
                        isRunning = true
                    },
                    enabled = !isRunning && (!isTimerMode || timeInTenths > 0),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("START", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        isRunning = false
                        isBlinking = false
                        blinkState = false
                    },
                    enabled = isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("STOP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("RST", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}