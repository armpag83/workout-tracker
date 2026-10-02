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

/** Pannello Cronometro / Timer con 5 lampeggi ed effetto sonoro ad alta frequenza */
@Composable
fun StopwatchPanel(targetRecuperoSeconds: Int?) {
    var isTimerMode by remember { mutableStateOf(false) }
    var timeInTenths by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var isBlinking by remember { mutableStateOf(false) }
    var blinkState by remember { mutableStateOf(false) }
    
    // Stato attivazione/disattivazione allarme acustico
    var isSoundEnabled by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(targetRecuperoSeconds) {
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

    // Gestione avviso visivo e sonoro (5 lampeggi con bip ad un'ottava superiore)
    LaunchedEffect(isBlinking) {
        if (isBlinking) {
            var toneGen: ToneGenerator? = null
            try {
                if (isSoundEnabled) {
                    toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                }
                repeat(5) {
                    blinkState = true
                    toneGen?.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
                    delay(250L)
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

                    // Mostra la campanella SOLO in modalità Timer
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

                        // Ripristina il tempo di recupero target in modalità timer
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