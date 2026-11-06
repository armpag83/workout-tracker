// 1.1.0
package it.armandopagliara.workouttracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.armandopagliara.workouttracker.model.Exercise

@Composable
fun ExerciseCard(
    exercise: Exercise,
    isHighlighted: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onCardClick: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Rilevamento Tablet per aumento dimensioni (+20%)
    val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600
    val scale = if (isTablet) 1.20f else 1.0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = (4 * scale).dp)
            .clip(RoundedCornerShape((16 * scale).dp))
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted)
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding((12 * scale).dp),
            verticalAlignment = Alignment.Top
        ) {
            // Frecce spostamento verticale esercizio
            if (isHighlighted) {
                Column(
                    modifier = Modifier.padding(end = (8 * scale).dp),
                    verticalArrangement = Arrangement.spacedBy((4 * scale).dp)
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        modifier = Modifier.size((24 * scale).dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Sposta Su",
                            tint = if (canMoveUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        modifier = Modifier.size((24 * scale).dp)
                    ) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Sposta Giù",
                            tint = if (canMoveDown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                // Nome esercizio + Icone in alto a destra
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = exercise.nome,
                        fontSize = (16 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = (8 * scale).dp)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy((2 * scale).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onCopyClick, modifier = Modifier.size((28 * scale).dp)) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Duplica",
                                modifier = Modifier.size((18 * scale).dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onEditClick, modifier = Modifier.size((28 * scale).dp)) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Modifica",
                                modifier = Modifier.size((18 * scale).dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDeleteClick, modifier = Modifier.size((28 * scale).dp)) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Elimina",
                                modifier = Modifier.size((18 * scale).dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                // Dettagli Esercizio: "Muscoli coinvolti" & "Recupero/durata"
                val dettagli = buildString {
                    if (exercise.muscoli.isNotBlank()) append("Muscoli coinvolti: ${exercise.muscoli} | ")
                    if (exercise.target.isNotBlank()) append("Target: ${exercise.target}")
                    if (exercise.recupero.isNotBlank() && exercise.recupero != "0") {
                        val sec = exercise.recupero.toIntOrNull() ?: 0
                        val recStr = if (sec >= 60 && sec % 60 == 0) "${sec / 60}'" else "${sec}s"
                        append(" | Recupero/durata: $recStr")
                    }
                }
                if (dettagli.isNotBlank()) {
                    Text(
                        text = dettagli,
                        fontSize = (13 * scale).sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = (4 * scale).dp)
                    )
                }

                // Kg e Note estese sotto le icone
                if (exercise.kg.isNotBlank() || exercise.note.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = (8 * scale).dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (exercise.kg.isNotBlank()) {
                            Text(
                                text = "Kg: ${exercise.kg}",
                                fontSize = (13 * scale).sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = (12 * scale).dp)
                            )
                        }
                        if (exercise.note.isNotBlank()) {
                            Text(
                                text = "Note: ${exercise.note}",
                                fontSize = (13 * scale).sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}