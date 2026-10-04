package it.armandopagliara.workouttracker

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import it.armandopagliara.workouttracker.data.WorkoutCsvManager
import it.armandopagliara.workouttracker.model.Exercise
import it.armandopagliara.workouttracker.ui.components.ExerciseCard
import it.armandopagliara.workouttracker.ui.components.StopwatchPanel
import it.armandopagliara.workouttracker.ui.dialogs.ExerciseFormDialog
import it.armandopagliara.workouttracker.ui.dialogs.WelcomeTutorialDialog

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            MaterialTheme {
                WorkoutApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutApp() {
    val context = LocalContext.current
    val currentAppVersion = "1.1.5"

    var exercises by remember { mutableStateOf(WorkoutCsvManager.loadExercises(context)) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var highlightedExerciseId by rememberSaveable { mutableStateOf<String?>(null) }

    // Tracciamento dello stato di esecuzione del timer
    var isTimerRunning by remember { mutableStateOf(false) }
    var pendingExerciseIdSelection by remember { mutableStateOf<String?>(null) }
    var showConfirmExerciseChangeDialog by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }
    var showCsvEditorDialog by remember { mutableStateOf(false) }

    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showNewDayDialog by remember { mutableStateOf(false) }
    var showRenameDayDialog by remember { mutableStateOf(false) }
    var showDeleteDayDialog by remember { mutableStateOf(false) }
    var showCsvAccessConfirmDialog by remember { mutableStateOf(false) }

    // --- GESTIONE POPUP BENVENUTO / AGGIORNAMENTO ---
    val sharedPrefs = remember { context.getSharedPreferences("workout_tracker_prefs", Context.MODE_PRIVATE) }
    val lastSeenVersion = sharedPrefs.getString("last_seen_version", "")
    var showWelcomeTutorial by remember { mutableStateOf(lastSeenVersion != currentAppVersion) }

    val days = remember(exercises) {
        exercises.map { it.giorno }.distinct().ifEmpty { listOf("Giorno 1: Spinta") }
    }

    val selectedExercise = exercises.find { it.id == highlightedExerciseId }
    val targetRecuperoSeconds = selectedExercise?.recupero?.toIntOrNull()?.takeIf { it > 0 }

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
                    IconButton(onClick = { showWelcomeTutorial = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Guida App")
                    }

                    TextButton(onClick = { showNewDayDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuova scheda", fontSize = 13.sp)
                    }

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
            ScrollableTabRow(
                selectedTabIndex = selectedTab.coerceAtMost((days.size - 1).coerceAtLeast(0)),
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

            val currentDayName = days.getOrElse(selectedTab.coerceAtMost((days.size - 1).coerceAtLeast(0))) { "" }
            val currentList = exercises.filter { it.giorno == currentDayName }

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                items(currentList, key = { it.id }) { exercise ->
                    val isHighlighted = exercise.id == highlightedExerciseId
                    ExerciseCard(
                        exercise = exercise,
                        isHighlighted = isHighlighted,
                        onCardClick = {
                            val targetSelection = if (isHighlighted) null else exercise.id
                            if (isTimerRunning && targetSelection != highlightedExerciseId) {
                                pendingExerciseIdSelection = targetSelection
                                showConfirmExerciseChangeDialog = true
                            } else {
                                highlightedExerciseId = targetSelection
                            }
                        },
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

            StopwatchPanel(
                targetRecuperoSeconds = targetRecuperoSeconds,
                onRunningStateChange = { running -> isTimerRunning = running }
            )
        }
    }

    // --- ALERT CONFERMA CAMBIO ESERCIZIO CON TIMER IN CORSO ---
    if (showConfirmExerciseChangeDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmExerciseChangeDialog = false },
            title = { Text("Attenzione", fontWeight = FontWeight.Bold) },
            text = { Text("Recupero in corso! Cambiando esercizio il timer si fermerà.") },
            confirmButton = {
                Button(
                    onClick = {
                        highlightedExerciseId = pendingExerciseIdSelection
                        showConfirmExerciseChangeDialog = false
                    }
                ) {
                    Text("Procedi")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showConfirmExerciseChangeDialog = false }
                ) {
                    Text("Annulla")
                }
            }
        )
    }

    if (showWelcomeTutorial) {
        WelcomeTutorialDialog(
            versionName = currentAppVersion,
            initialDontShowAgain = lastSeenVersion == currentAppVersion,
            onDismiss = { dontShowAgain ->
                if (dontShowAgain) {
                    sharedPrefs.edit().putString("last_seen_version", currentAppVersion).apply()
                } else {
                    sharedPrefs.edit().remove("last_seen_version").apply()
                }
                showWelcomeTutorial = false
            }
        )
    }

    if (showRenameDayDialog) {
        val currentDayName = days.getOrElse(selectedTab.coerceAtMost((days.size - 1).coerceAtLeast(0))) { "" }
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
        val currentDayName = days.getOrElse(selectedTab.coerceAtMost((days.size - 1).coerceAtLeast(0))) { "" }
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
        val currentDay = days.getOrElse(selectedTab.coerceAtMost((days.size - 1).coerceAtLeast(0))) { "Giorno 1" }
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