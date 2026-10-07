// DEV_0.2.8
package it.armandopagliara.workouttracker.ui.dialogs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Componente per la guida all'uso e il changelog di versione */
@Composable
fun WelcomeTutorialDialog(
    versionName: String,
    initialDontShowAgain: Boolean = true,
    onDismiss: (dontShowAgain: Boolean) -> Unit
) {
    var dontShowAgain by remember { mutableStateOf(initialDontShowAgain) }

    AlertDialog(
        onDismissRequest = { onDismiss(dontShowAgain) },
        title = {
            Column {
                Text(text = "Benvenuto in Workout Tracker!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "Guida e novità versione $versionName", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Novità e funzionalità:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                
                Text(text = "• 🔀 Riordinamento Esercizi: usa le frecce per spostare gli esercizi e riordinarli.", fontSize = 13.sp)
                Text(text = "• 📋 Copia & Incolla: copia un esercizio con l'icona 📋 e incollalo facilmente in un'altra scheda.", fontSize = 13.sp)
                Text(text = "• ⏱️ Timer & Reset Intelligente: conto alla rovescia con tasto RST per ripristinare il recupero.", fontSize = 13.sp)
                Text(text = "• 🔔 Allarme Visivo e Acustico: allo scadere del tempo lo schermo lampeggia con 3 bip veloci. Muto attivabile con 🔔/🔕.", fontSize = 13.sp)
                Text(text = "• ✏️ Modifica Dati & CSV: modifica carico, note e target, oppure importa/esporta backup CSV.", fontSize = 13.sp)

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { dontShowAgain = !dontShowAgain }
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it }
                    )
                    Text(text = "Non mostrare più per questo aggiornamento", fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss(dontShowAgain) }) {
                Text("Chiudi")
            }
        }
    )
}