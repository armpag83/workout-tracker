package it.armandopagliara.workouttracker.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.armandopagliara.workouttracker.model.Exercise

/** Formatatatore per trasformare i secondi in formato mm' ss" */
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

/** Componente Card per la visualizzazione di ciascun esercizio */
@Composable
fun ExerciseCard(
    exercise: Exercise,
    isHighlighted: Boolean,
    onCardClick: () -> Unit,
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
            .padding(vertical = 6.dp)
            .clickable { onCardClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = if (isHighlighted) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isHighlighted) 6.dp else 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
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
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifica Esercizio")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Default.Delete, contentDescription = "Elimina Esercizio", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (exercise.kg.isNotBlank() || exercise.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (exercise.kg.isNotBlank()) {
                        Text(
                            text = "Kg: ${exercise.kg}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (exercise.note.isNotBlank()) {
                        Text(
                            text = "Note: ${exercise.note}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}