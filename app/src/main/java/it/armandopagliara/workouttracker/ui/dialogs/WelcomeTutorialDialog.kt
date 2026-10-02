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
                Text(text = "Funzionalità dell'applicazione:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                
                Text(text = "• 📋 Schede Personalizzate: Crea, rinomina e naviga facilmente tra le tue schede.", fontSize = 13.sp)
                Text(text = "• 🎯 Selezione Esercizio: Tocca un esercizio per evidenziarlo durante la serie.", fontSize = 13.sp)
                Text(text = "• ⏱️ Timer & Reset Intelligente: Tocca un esercizio con recupero per attivare il conto alla rovescia. Il tasto RST ripristina il tempo di recupero anziché azzerarlo.", fontSize = 13.sp)
                Text(text = "• 🔔 Allarme Visivo e Acustico: Allo scadere del tempo lo schermo lampeggia 5 volte con un bip sonoro. In modalità Timer puoi silenziarlo con l'icona 🔔/🔕.", fontSize = 13.sp)
                Text(text = "• ✏️ Modifica Dati: Modifica carico (Kg), note o target con la matita.", fontSize = 13.sp)
                Text(text = "• 💾 Import/Export CSV: Gestisci i backup della tua scheda in formato CSV.", fontSize = 13.sp)

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