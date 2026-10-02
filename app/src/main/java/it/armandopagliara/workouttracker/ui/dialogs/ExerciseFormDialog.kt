package it.armandopagliara.workouttracker.ui.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import it.armandopagliara.workouttracker.model.Exercise

/** Dialogo form per aggiungere o modificare un esercizio */
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
    
    var recuperoSecText by remember {
        val initialSec = initialExercise.recupero.toIntOrNull()
        mutableStateOf(if (initialSec != null && initialSec > 0) initialSec.toString() else "")
    }
    var kg by remember { mutableStateOf(initialExercise.kg) }
    var note by remember { mutableStateOf(initialExercise.note) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome Esercizio") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = muscoli,
                    onValueChange = { muscoli = it },
                    label = { Text("Muscoli (es. Petto)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = target,
                    onValueChange = { target = it },
                    label = { Text("Target (es. 4 x 8)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            val current = recuperoSecText.toIntOrNull() ?: 0
                            if (current >= 5) recuperoSecText = (current - 5).toString() else recuperoSecText = ""
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) { Text("-5") }

                    OutlinedTextField(
                        value = recuperoSecText,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) recuperoSecText = input
                        },
                        label = { Text("Recupero (sec)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedButton(
                        onClick = {
                            val current = recuperoSecText.toIntOrNull() ?: 0
                            recuperoSecText = (current + 5).toString()
                        },
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) { Text("+5") }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = kg,
                        onValueChange = { kg = it },
                        label = { Text("Kg") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note") },
                        singleLine = true,
                        modifier = Modifier.weight(2f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nome.isNotBlank()) {
                        val secVal = recuperoSecText.toIntOrNull()
                        val finalRecupero = if (secVal != null && secVal > 0) secVal.toString() else ""
                        
                        onConfirm(
                            initialExercise.copy(
                                nome = nome.trim(),
                                muscoli = muscoli.trim(),
                                target = target.trim(),
                                recupero = finalRecupero,
                                kg = kg.trim(),
                                note = note.trim()
                            )
                        )
                    }
                }
            ) { Text("Salva") }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Annulla") }
        }
    )
}