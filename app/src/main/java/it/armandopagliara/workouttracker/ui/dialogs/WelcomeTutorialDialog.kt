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
            Column {
                Text(
                    text = "Workout Tracker v$versionName",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Benvenuto! Ecco le principali funzionalità dell'app:",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                TutorialSection(
                    icon = "➕",
                    title = "Creazione Nuova Scheda",
                    description = "Tocca \"+ Nuova scheda\" nella barra in alto per aggiungere una nuova scheda di allenamento (es. Scheda C: Gambe)."
                )

                TutorialSection(
                    icon = "🏋️",
                    title = "Aggiunta Nuovo Esercizio",
                    description = "Tocca il pulsante \"+\" fisso a sinistra delle schede per aggiungere un esercizio alla scheda attiva. Tieni premuto il pulsante per visualizzare l'etichetta di aiuto."
                )

                TutorialSection(
                    icon = "↔️",
                    title = "Riordinamento Schede",
                    description = "Seleziona una scheda e usa le frecce < e > per spostarla a sinistra o a destra nell'elenco."
                )

                TutorialSection(
                    icon = "↕️",
                    title = "Riordinamento Esercizi",
                    description = "Tocca un esercizio per evidenziarlo e usa le frecce ↑ e ↓ a sinistra per cambiarne la posizione."
                )

                TutorialSection(
                    icon = "📋",
                    title = "Copia & Incolla Smart",
                    description = "Duplica un esercizio in qualsiasi scheda dal menu \"Incolla in:\". I duplicati ricevono automaticamente il suffisso \"copia\"."
                )

                TutorialSection(
                    icon = "⏱️",
                    title = "Timer Reattivo & Suoni",
                    description = "Il timer si aggiorna istantaneamente quando selezioni un esercizio con un tempo di recupero impostato. Allarme visivo e sonoro a fine tempo."
                )

                TutorialSection(
                    icon = "⚙️",
                    title = "Gestione CSV & Backup",
                    description = "Accedi all'icona dell'ingranaggio in alto per salvare/caricare preset locali, modificare direttamente il testo CSV o esportare/importare file."
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = dontShowAgain,
                        onCheckedChange = { dontShowAgain = it }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Non mostrare più per questa versione",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDismiss(dontShowAgain) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ho capito", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
private fun TutorialSection(
    icon: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(text = icon, fontSize = 20.sp)
        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}