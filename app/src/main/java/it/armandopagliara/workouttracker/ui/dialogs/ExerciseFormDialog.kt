// 1.1.0
package it.armandopagliara.workouttracker.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.armandopagliara.workouttracker.model.Exercise

@Composable
fun ExerciseFormDialog(
    title: String,
    initialExercise: Exercise,
    onDismiss: () -> Unit,
    onConfirm: (Exercise) -> Unit
) {
    var nome by remember { mutableStateOf(initialExercise.nome) }
    var muscoli by remember { mutableStateOf(initialExercise.muscoli) }
    var target by remember { mutableStateOf(initialExercise.target) }
    var recupero by remember { mutableStateOf(initialExercise.recupero) }
    var kg by remember { mutableStateOf(initialExercise.kg) }
    var note by remember { mutableStateOf(initialExercise.note) }

    val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600
    val scale = if (isTablet) 1.20f else 1.0f

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                fontSize = (18 * scale).sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
                // Limit: nome (70)
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it.take(70) },
                    label = { Text("Nome Esercizio (max 70)", fontSize = (12 * scale).sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Limit: muscoli (40)
                OutlinedTextField(
                    value = muscoli,
                    onValueChange = { muscoli = it.take(40) },
                    label = { Text("Muscoli coinvolti (max 40)", fontSize = (12 * scale).sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Limit: target (20)
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it.take(20) },
                    label = { Text("Target (es. 3 x 12) (max 20)", fontSize = (12 * scale).sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // Limit: recupero (4)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)
                ) {
                    OutlinedButton(onClick = {
                        val current = recupero.toIntOrNull() ?: 0
                        if (current >= 5) recupero = (current - 5).toString().take(4)
                    }) {
                        Text("-5", fontSize = (12 * scale).sp)
                    }
                    OutlinedTextField(
                        value = recupero,
                        onValueChange = { recupero = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("Recupero/durata (sec)", fontSize = (12 * scale).sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = {
                        val current = recupero.toIntOrNull() ?: 0
                        recupero = (current + 5).toString().take(4)
                    }) {
                        Text("+5", fontSize = (12 * scale).sp)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
                    // Limit: kg (15)
                    OutlinedTextField(
                        value = kg,
                        onValueChange = { kg = it.take(15) },
                        label = { Text("Kg (max 15)", fontSize = (12 * scale).sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    // Limit: note (300)
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it.take(300) },
                        label = { Text("Note (max 300)", fontSize = (12 * scale).sp) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                onConfirm(
                    initialExercise.copy(
                        nome = nome,
                        muscoli = muscoli,
                        target = target,
                        recupero = recupero,
                        kg = kg,
                        note = note
                    )
                )
            }) {
                Text("Salva", fontSize = (14 * scale).sp)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annulla", fontSize = (14 * scale).sp)
            }
        }
    )
}