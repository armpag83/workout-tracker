// 1.1.8
package it.armandopagliara.workouttracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.armandopagliara.workouttracker.model.Exercise

/** Formattatore per trasformare i secondi in formato mm' ss" */
fun formatRecupero(recuperoStr: String): String {
    val totalSec = recuperoStr.toIntOrNull() ?: return ""
    if (totalSec <= 0) return ""
    val m = totalSec / 60
    val s = totalSec % 60
    return when {
        m > 0 && s > 0 -> "${m}' ${s}\""
        m > 0 -> "${m}'"
        else -> "${s}\""
    }
}

/** Card Esercizio con frecce su/giù visibili solo quando l'esercizio è selezionato */
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
    val detailsList = mutableListOf<String>()
    if (exercise.muscoli.isNotBlank()) detailsList.add(exercise.muscoli)
    if (exercise.target.isNotBlank()) detailsList.add("Target: ${exercise.target}")
    val formattedRec = formatRecupero(exercise.recupero)
    if (formattedRec.isNotBlank()) detailsList.add("Recupero: $formattedRec")

    val detailsText = detailsList.joinToString(" | ")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isHighlighted) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 6.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Frecce Su e Giù visibili SOLO quando la card è selezionata
            if (isHighlighted) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = canMoveUp,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "Sposta in alto",
                            tint = if (canMoveUp) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                    IconButton(
                        onClick = onMoveDown,
                        enabled = canMoveDown,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Sposta in basso",
                            tint = if (canMoveDown) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.nome,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isHighlighted) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (detailsText.isNotBlank()) {
                    Text(
                        text = detailsText,
                        fontSize = 12.sp,
                        color = if (isHighlighted) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondary
                    )
                }

                if (exercise.kg.isNotBlank() || exercise.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (exercise.kg.isNotBlank()) {
                            Text(
                                text = "Kg: ${exercise.kg}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (exercise.note.isNotBlank()) {
                            Text(
                                text = "Note: ${exercise.note}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
                IconButton(onClick = onCopyClick) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copia Esercizio",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onEditClick) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Modifica Esercizio",
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Elimina Esercizio",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}