// 1.1.8
package it.armandopagliara.workouttracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
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

/** Componente Card con feedback visivo durante il trascinamento e pulsante di copia */
@Composable
fun ExerciseCard(
    exercise: Exercise,
    isHighlighted: Boolean,
    isDragging: Boolean = false,
    onCardClick: () -> Unit,
    onCopyClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    dragModifier: Modifier = Modifier
) {
    val detailsList = mutableListOf<String>()
    if (exercise.muscoli.isNotBlank()) detailsList.add(exercise.muscoli)
    if (exercise.target.isNotBlank()) detailsList.add("Target: ${exercise.target}")
    val formattedRec = formatRecupero(exercise.recupero)
    if (formattedRec.isNotBlank()) detailsList.add("Recupero: $formattedRec")

    val detailsText = detailsList.joinToString(" | ")

    val containerColor = when {
        isDragging -> MaterialTheme.colorScheme.secondaryContainer
        isHighlighted -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    val elevation = when {
        isDragging -> 12.dp
        isHighlighted -> 6.dp
        else -> 2.dp
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isHighlighted || isDragging) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icona di trascinamento
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Trascina per riordinare",
                tint = if (isDragging) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                modifier = dragModifier
                    .padding(end = 8.dp)
                    .size(24.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = exercise.nome,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (isHighlighted || isDragging) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                if (detailsText.isNotBlank()) {
                    Text(
                        text = detailsText,
                        fontSize = 12.sp,
                        color = if (isHighlighted || isDragging) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.secondary
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