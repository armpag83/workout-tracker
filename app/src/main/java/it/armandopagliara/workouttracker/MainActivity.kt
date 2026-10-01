package it.armandopagliara.workouttracker

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Impedisce allo schermo di spegnersi durante l'allenamento
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MaterialTheme {
                WorkoutApp()
            }
        }
    }
}

/**
 * Helper per formattare i secondi di recupero in una stringa leggibile (es. 90 -> "1' 30"").
 * Restituisce stringa vuota se il valore non è valido o <= 0.
 */
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutApp() {
    val context = LocalContext.current
    val currentAppVersion = "1.1.1" // Versione corrente dell'app

    // Gestione stato esercizi e persistenza della scheda attiva
    var exercises by remember { mutableStateOf(WorkoutCsvManager.loadExercises(context)) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var highlightedExerciseId by rememberSaveable { mutableStateOf<String?>(null) }

    // Stati per menu e dialogo editor CSV
    var showMenu by remember { mutableStateOf(false) }
    var showCsvEditorDialog by remember { mutableStateOf(false) }

    // Stati per la gestione dei dialoghi di editing ed eliminazione
    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showNewDayDialog by remember { mutableStateOf(false) }
    var showRenameDayDialog by remember { mutableStateOf(false) }
    var showDeleteDayDialog by remember { mutableStateOf(false) }
    var showCsvAccessConfirmDialog by remember { mutableStateOf(false) }

    // --- Gestione Tutorial di Benvenuto (Onboarding ad ogni installazione/aggiornamento) ---
    val sharedPrefs = remember { context.getSharedPreferences("workout_tracker_prefs", Context.MODE_PRIVATE) }
    val lastSeenVersion = sharedPrefs.getString("last_seen_version", "")
    var showWelcomeTutorial by remember { mutableStateOf(lastSeenVersion != currentAppVersion) }

    // Estrazione dinamica delle schede/giorni
    val days = remember(exercises) {
        exercises.map { it.giorno }.distinct().ifEmpty { listOf("Giorno 1: Spinta") }
    }

    // Identificazione dell'esercizio selezionato e del relativo tempo di recupero
    val selectedExercise = exercises.find { it.id == highlightedExerciseId }
    val targetRecuperoSeconds = selectedExercise?.recupero?.toIntOrNull()?.takeIf { it > 0 }

    // Launcher per Importazione CSV
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                val content = inputStream?.bufferedReader()?.use { reader -> reader.readText() }
                if (!content.isNullOrEmpty()) {
                    if (WorkoutCsvManager.saveRawCsv(context, content)) {
                        exercises = WorkoutCsvManager.loadExercises(context)
                        selectedTab = 0
                        highlightedExerciseId = null
                        Toast.makeText(context, "Configurazione importata!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Formato CSV non valido", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Errore durante l'importazione", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Launcher per Esportazione CSV
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            try {
                val outputStream = context.contentResolver.openOutputStream(it)
                val currentCsv = WorkoutCsvManager.toCsv(exercises)
                outputStream?.bufferedWriter()?.use { writer -> writer.write(currentCsv) }
                Toast.makeText(context, "CSV esportato con successo!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Errore durante l'esportazione", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher),
                            contentDescription = "Logo App",
                            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(10.dp))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = "Workout", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 15.sp)
                            Text(text = "Tracker", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, lineHeight = 15.sp)
                        }
                    }
                },
                actions = {
                    // Pulsante Tutorial / Info
                    IconButton(onClick = { showWelcomeTutorial = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Guida App")
                    }

                    // Nuova scheda
                    TextButton(onClick = { showNewDayDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuova scheda", fontSize = 13.sp)
                    }

                    // Menu CSV
                    TextButton(onClick = { showCsvAccessConfirmDialog = true }) {
                        Text("⚙️ CSV", fontSize = 13.sp)
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("✏️ Modifica CSV (Testo)") },
                            onClick = { showMenu = false; showCsvEditorDialog = true }
                        )
                        DropdownMenuItem(
                            text = { Text("📂 Importa CSV da File") },
                            onClick = { showMenu = false; importLauncher.launch("*/*") }
                        )
                        DropdownMenuItem(
                            text = { Text("💾 Esporta CSV su File") },
                            onClick = { showMenu = false; exportLauncher.launch("workout_config.csv") }
                        )
                        DropdownMenuItem(
                            text = { Text("🔄 Ripristina Predefinito") },
                            onClick = {
                                showMenu = false
                                exercises = WorkoutCsvManager.resetToDefault(context)
                                selectedTab = 0
                                highlightedExerciseId = null
                                Toast.makeText(context, "Configurazione ripristinata", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Barra di scorrimento delle schede
            ScrollableTabRow(
                selectedTabIndex = selectedTab.coerceAtMost(days.size - 1),
                edgePadding = 8.dp
            ) {
                days.forEachIndexed { index, dayName ->
                    val isSelected = selectedTab == index
                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        text = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(dayName, fontSize = 13.sp)
                                if (isSelected) {
                                    IconButton(
                                        onClick = { showRenameDayDialog = true },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Rinomina Scheda",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    )
                }

                IconButton(onClick = { showAddExerciseDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Aggiungi Esercizio", tint = MaterialTheme.colorScheme.primary)
                }
            }

            val currentDayName = days.getOrElse(selectedTab.coerceAtMost(days.size - 1)) { "" }
            val currentList = exercises.filter { it.giorno == currentDayName }

            // Elenco Esercizi della scheda attiva
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                items(currentList, key = { it.id }) { exercise ->
                    val isHighlighted = exercise.id == highlightedExerciseId
                    ExerciseCard(
                        exercise = exercise,
                        isHighlighted = isHighlighted,
                        onCardClick = { highlightedExerciseId = if (isHighlighted) null else exercise.id },
                        onEditClick = { exerciseToEdit = exercise },
                        onDeleteClick = { exerciseToDelete = exercise }
                    )
                }

                if (currentDayName.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { showDeleteDayDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Elimina intera scheda", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }

            // Cronometro / Timer con gestione decimi di secondo
            StopwatchPanel(targetRecuperoSeconds = targetRecuperoSeconds)
        }
    }

    // --- TUTORIAL / ONBOARDING DI BENVENUTO ---
    if (showWelcomeTutorial) {
        WelcomeTutorialDialog(
            versionName = currentAppVersion,
            onDismiss = { dontShowAgain ->
                if (dontShowAgain) {
                    sharedPrefs.edit().putString("last_seen_version", currentAppVersion).apply()
                }
                showWelcomeTutorial = false
            }
        )
    }

    // --- DIALOGHI DI GESTIONE SCHEDE ED ESERCIZI ---
    if (showRenameDayDialog) {
        val currentDayName = days.getOrElse(selectedTab.coerceAtMost(days.size - 1)) { "" }
        var newDayName by remember { mutableStateOf(currentDayName) }

        AlertDialog(
            onDismissRequest = { showRenameDayDialog = false },
            title = { Text("Rinomina Scheda") },
            text = {
                OutlinedTextField(
                    value = newDayName,
                    onValueChange = { newDayName = it },
                    label = { Text("Nome scheda") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newDayName.trim()
                        if (trimmed.isNotBlank() && trimmed != currentDayName) {
                            val newList = exercises.map {
                                if (it.giorno == currentDayName) it.copy(giorno = trimmed) else it
                            }
                            exercises = newList
                            WorkoutCsvManager.saveExercises(context, newList)
                            showRenameDayDialog = false
                            Toast.makeText(context, "Scheda rinominata!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Salva") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRenameDayDialog = false }) { Text("Annulla") }
            }
        )
    }

    if (showCsvAccessConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCsvAccessConfirmDialog = false },
            title = { Text("Gestione CSV") },
            text = { Text("Vuoi accedere al menu di configurazione e modifica del file CSV?") },
            confirmButton = {
                Button(onClick = { showCsvAccessConfirmDialog = false; showMenu = true }) { Text("Accedi") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCsvAccessConfirmDialog = false }) { Text("Annulla") }
            }
        )
    }

    if (showDeleteDayDialog) {
        val currentDayName = days.getOrElse(selectedTab.coerceAtMost(days.size - 1)) { "" }
        AlertDialog(
            onDismissRequest = { showDeleteDayDialog = false },
            title = { Text("Conferma eliminazione scheda") },
            text = { Text("Sei sicuro di voler eliminare l'intera scheda \"$currentDayName\" e tutti gli esercizi contenuti?") },
            confirmButton = {
                Button(
                    onClick = {
                        val newList = exercises.filter { it.giorno != currentDayName }
                        exercises = newList
                        WorkoutCsvManager.saveExercises(context, newList)
                        selectedTab = 0
                        highlightedExerciseId = null
                        showDeleteDayDialog = false
                        Toast.makeText(context, "Scheda eliminata", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Elimina Scheda") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDayDialog = false }) { Text("Annulla") }
            }
        )
    }

    exerciseToEdit?.let { ex ->
        ExerciseFormDialog(
            title = "Modifica Esercizio",
            initialExercise = ex,
            onDismiss = { exerciseToEdit = null },
            onConfirm = { updated ->
                val newList = exercises.map { if (it.id == updated.id) updated else it }
                exercises = newList
                WorkoutCsvManager.saveExercises(context, newList)
                exerciseToEdit = null
                Toast.makeText(context, "Esercizio aggiornato", Toast.LENGTH_SHORT).show()
            }
        )
    }

    exerciseToDelete?.let { ex ->
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text("Conferma eliminazione") },
            text = { Text("Sei sicuro di voler eliminare l'esercizio \"${ex.nome}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        val newList = exercises.filter { it.id != ex.id }
                        if (highlightedExerciseId == ex.id) highlightedExerciseId = null
                        exercises = newList
                        WorkoutCsvManager.saveExercises(context, newList)
                        exerciseToDelete = null
                        Toast.makeText(context, "Esercizio eliminato", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Elimina") }
            },
            dismissButton = {
                OutlinedButton(onClick = { exerciseToDelete = null }) { Text("Annulla") }
            }
        )
    }

    if (showAddExerciseDialog) {
        val currentDay = days.getOrElse(selectedTab.coerceAtMost(days.size - 1)) { "Giorno 1" }
        val newEx = Exercise(
            id = "${currentDay}_${System.currentTimeMillis()}",
            giorno = currentDay,
            nome = "",
            muscoli = "",
            target = "",
            recupero = "90"
        )
        ExerciseFormDialog(
            title = "Nuovo Esercizio ($currentDay)",
            initialExercise = newEx,
            onDismiss = { showAddExerciseDialog = false },
            onConfirm = { created ->
                val newList = exercises + created
                exercises = newList
                WorkoutCsvManager.saveExercises(context, newList)
                showAddExerciseDialog = false
                Toast.makeText(context, "Esercizio aggiunto", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showNewDayDialog) {
        var newDayName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewDayDialog = false },
            title = { Text("Crea Nuova Scheda") },
            text = {
                OutlinedTextField(
                    value = newDayName,
                    onValueChange = { newDayName = it },
                    label = { Text("Nome scheda (es. Giorno 3: Gambe)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDayName.isNotBlank()) {
                            val dummyEx = Exercise(
                                id = "${newDayName}_${System.currentTimeMillis()}",
                                giorno = newDayName.trim(),
                                nome = "Riscaldamento",
                                muscoli = "Generale",
                                target = "5'",
                                recupero = "180"
                            )
                            val newList = exercises + dummyEx
                            exercises = newList
                            WorkoutCsvManager.saveExercises(context, newList)
                            selectedTab = days.size
                            showNewDayDialog = false
                            Toast.makeText(context, "Nuova scheda creata!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Crea") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNewDayDialog = false }) { Text("Annulla") }
            }
        )
    }

    if (showCsvEditorDialog) {
        var rawCsvText by remember { mutableStateOf(WorkoutCsvManager.getRawCsv(context)) }

        AlertDialog(
            onDismissRequest = { showCsvEditorDialog = false },
            title = { Text("Modifica Configurazione CSV", fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = rawCsvText,
                    onValueChange = { rawCsvText = it },
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    label = { Text("giorno,nome,muscoli,target,recupero,kg,note") },
                    textStyle = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (WorkoutCsvManager.saveRawCsv(context, rawCsvText)) {
                            exercises = WorkoutCsvManager.loadExercises(context)
                            selectedTab = 0
                            highlightedExerciseId = null
                            showCsvEditorDialog = false
                            Toast.makeText(context, "CSV Salvato!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Errore nella sintassi CSV", Toast.LENGTH_LONG).show()
                        }
                    }
                ) { Text("Salva") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCsvEditorDialog = false }) { Text("Annulla") }
            }
        )
    }
}

/** Componente per il tutorial/onboarding di benvenuto */
@Composable
fun WelcomeTutorialDialog(
    versionName: String,
    onDismiss: (dontShowAgain: Boolean) -> Unit
) {
    var dontShowAgain by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = { onDismiss(dontShowAgain) },
        title = {
            Column {
                Text(text = "Benvenuto in Workout Tracker!", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "Novità e guida versione $versionName", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(text = "Ecco le funzionalità principali dell'applicazione:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                
                Text(text = "• 📋 Schede Personalizzate: Crea, modifica e naviga facilmente tra le tue schede di allenamento.", fontSize = 13.sp)
                Text(text = "• 🎯 Selezione Esercizio: Tocca un esercizio per evidenziarlo durante la serie corrente.", fontSize = 13.sp)
                Text(text = "• ⏱️ Timer Intelligente: Toccando un esercizio con recupero impostato, il cronometro si trasforma in un conto alla rovescia automatico con segnale visivo di fine tempo.", fontSize = 13.sp)
                Text(text = "• ✏️ Modifica Facile: Modifica carico, note o tempi tramite l'icona della matita.", fontSize = 13.sp)
                Text(text = "• 💾 Backup CSV: Esporta o importa la tua scheda in formato CSV per non perdere mai i tuoi dati.", fontSize = 13.sp)

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
                Text("Inizia Allenamento")
            }
        }
    )
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
    // Assembla solo gli attributi presenti (nasconde i vuoti)
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

/** Pannello Cronometro / Timer con precisione al decimo di secondo, allarme acustico e reset intelligenti */
@Composable
fun StopwatchPanel(targetRecuperoSeconds: Int?) {
    val context = LocalContext.current
    var isTimerMode by remember { mutableStateOf(false) }
    var timeInTenths by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }
    var isBlinking by remember { mutableStateOf(false) }
    var blinkState by remember { mutableStateOf(false) }
    
    // Stato per attivare/disattivare l'allarme sonoro (memorizzato durante l'uso)
    var isSoundEnabled by rememberSaveable { mutableStateOf(true) }

    // Sincronizzazione automatica all'evidenziazione di un esercizio
    LaunchedEffect(targetRecuperoSeconds) {
        isRunning = false
        isBlinking = false
        blinkState = false
        if (targetRecuperoSeconds != null && targetRecuperoSeconds > 0) {
            isTimerMode = true
            timeInTenths = targetRecuperoSeconds * 10L
        } else {
            isTimerMode = false
            timeInTenths = 0L
        }
    }

    // Avanzamento/Conto alla rovescia ogni decimo di secondo
    LaunchedEffect(isRunning, isTimerMode) {
        while (isRunning) {
            delay(100L)
            if (isTimerMode) {
                if (timeInTenths > 0) {
                    timeInTenths--
                    if (timeInTenths == 0L) {
                        isRunning = false
                        isBlinking = true
                    }
                } else {
                    isRunning = false
                }
            } else {
                timeInTenths++
            }
        }
    }

    // Gestione avviso visivo (lampeggio) ed effetto sonoro di fine recupero
    LaunchedEffect(isBlinking) {
        if (isBlinking) {
            // Riproduce il suono di allarme se attivo
            if (isSoundEnabled) {
                try {
                    val toneGen = ToneGenerator(AudioManager.STREAM_ALARM, 100)
                    toneGen.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_OR_VOLUMED_ACK, 500)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            // Blocco try-finally per prevenire blocchi sul colore rosso
            try {
                repeat(4) {
                    blinkState = true
                    delay(250L)
                    blinkState = false
                    delay(250L)
                }
            } finally {
                blinkState = false
                isBlinking = false
            }
        }
    }

    val minutes = (timeInTenths / 10) / 60
    val seconds = (timeInTenths / 10) % 60
    val tenths = timeInTenths % 10
    val formattedTime = String.format("%02d:%02d.%d", minutes, seconds, tenths)

    // Colori dinamici dello sfondo e del testo
    val containerColor = if (blinkState) Color(0xFFFF3300) else MaterialTheme.colorScheme.surface
    val textColor = if (blinkState) Color.White else if (isTimerMode) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary

    Surface(
        color = containerColor,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedTime,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    // Pulsante di attivazione/disattivazione allarme sonoro
                    IconButton(
                        onClick = { isSoundEnabled = !isSoundEnabled },
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(32.dp)
                    ) {
                        Text(
                            text = if (isSoundEnabled) "🔔" else "🔕",
                            fontSize = 18.sp
                        )
                    }
                }

                if (isTimerMode) {
                    Text(
                        text = "Recupero (Timer)",
                        fontSize = 11.sp,
                        color = if (blinkState) Color.White else MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        isBlinking = false
                        blinkState = false
                        isRunning = true
                    },
                    enabled = !isRunning && (!isTimerMode || timeInTenths > 0),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("START", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        isRunning = false
                        isBlinking = false
                        blinkState = false
                    },
                    enabled = isRunning,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("STOP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        isRunning = false
                        isBlinking = false
                        blinkState = false

                        // Ripristina il tempo di recupero se in modalità timer
                        if (targetRecuperoSeconds != null && targetRecuperoSeconds > 0) {
                            isTimerMode = true
                            timeInTenths = targetRecuperoSeconds * 10L
                        } else {
                            isTimerMode = false
                            timeInTenths = 0L
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("RST", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}