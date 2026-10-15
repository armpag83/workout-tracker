// DEV_0.2.12
package it.armandopagliara.workouttracker.ui.dialogs

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

/** Dialogo di benvenuto e guida rapida aggiornato con le ultime funzionalità */
@Composable
fun WelcomeTutorialDialog(
    versionName: String,
    initialDontShowAgain: Boolean,
    onDismiss: (Boolean) -> Unit
) {
    var dontShowAgain by remember { mutableStateOf(initialDontShowAgain) }

    AlertDialog(
        onDismissRequest = { onDismiss(dontShowAgain) },
        title = {
            Text(
                text = "Workout Tracker v$versionName",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Benvenuto! Ecco le principali funzionalità e le ultime novità disponibili:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                HorizontalDivider()

                TutorialFeatureItem(
                    icon = "↔️",
                    title = "Riordinamento Schede",
                    description = "Seleziona una scheda e usa le frecce < e > per spostarla a sinistra o a destra nell'elenco."
                )

                TutorialFeatureItem(
                    icon = "↕️",
                    title = "Riordinamento Esercizi",
                    description = "Tocca un esercizio per evidenziarlo e usa le frecce ↑ e ↓ a sinistra per cambiarne la posizione."
                )

                TutorialFeatureItem(
                    icon = "📋",
                    title = "Copia & Incolla Smart",
                    description = "Duplica un esercizio in qualsiasi scheda dal menu \"Incolla in:\". I duplicati ricevono automaticamente il suffisso \"copia\"."
                )

                TutorialFeatureItem(
                    icon = "⏱️",
                    title = "Timer Reattivo & Suoni",
                    description = "Il timer si aggiorna istantaneamente quando modifichi il recupero. Allarme visivo e sonoro bilanciato a fine tempo."
                )

                TutorialFeatureItem(
                    icon = "⚙️",
                    title = "Gestione CSV & Backup",
                    description = "Modifica direttamente il testo, importa o esporta il tuo file CSV toccando l'icona dell'ingranaggio in alto."
                )

                HorizontalDivider()

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Non mostrare più per questa versione",
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDismiss(dontShowAgain) }) {
                Text("Ho capito")
            }
        }
    )
}

@Composable
private fun TutorialFeatureItem(
    icon: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = icon,
            fontSize = 16.sp,
            modifier = Modifier.padding(end = 8.dp, top = 2.dp)
        )
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}