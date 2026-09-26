package com.example.workouttracker

import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddBox
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Mantiene lo schermo sempre acceso durante l'uso dell'app
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
    var exercises by remember { mutableStateOf(WorkoutCsvManager.loadExercises(context)) }
    var selectedTab by remember { mutableStateOf(0) }
    var showMenu by remember { mutableStateOf(false) }
    var showCsvEditorDialog by remember { mutableStateOf(false) }

    // Stati per i dialoghi di modifica, eliminazione, nuovo esercizio e nuova scheda
    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showNewDayDialog by remember { mutableStateOf(false) }

    // Lista dinamica dei giorni
    val days = remember(exercises) {
        exercises.map { it.giorno }.distinct().ifEmpty { listOf("Giorno 1: Spinta") }
    }

    // Launcher per Importazione/Esportazione CSV
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
                title = { Text("Workout Tracker", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    // Pulsante "Nuova scheda" nella TopBar
                    TextButton(onClick = { showNewDayDialog = true }) {
                        Icon(Icons.Default.AddBox, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Nuova scheda", fontSize = 13.sp)
                    }

                    // Pulsante Menu CSV
                    TextButton(onClick = { showMenu = true }) {
                        Text("⚙️ CSV", fontSize = 13.sp)
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("✏️ Modifica CSV (Testo)") },
                            onClick = {
                                showMenu = false
                                showCsvEditorDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("📂 Importa CSV da File") },
                            onClick = {
                                showMenu = false
                                importLauncher.launch("*/*")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("💾 Esporta CSV su File") },
                            onClick = {
                                showMenu = false
                                exportLauncher.launch("workout_config.csv")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("🔄 Ripristina Predefinito") },
                            onClick = {
                                showMenu = false
                                exercises = WorkoutCsvManager.resetToDefault(context)
                                selectedTab = 0
                                Toast.makeText(context, "Configurazione ripristinata", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Barra dei Tab con pulsante "Aggiungi" esercizio
            ScrollableTabRow(
                selectedTabIndex = selectedTab.coerceAtMost(days.size - 1),
                edgePadding = 8.dp
            ) {
                days.forEachIndexed { index, dayName ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(dayName, fontSize = 13.sp) }
                    )
                }

                // Pulsante (+) Aggiungi accanto ai tab
                IconButton(
                    onClick = { showAddExerciseDialog = true },
                    modifier = Modifier.align(Alignment.CenterVertically)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Aggiungi Esercizio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            val currentDayName = days.getOrElse(selectedTab) { "" }
            val currentList = exercises.filter { it.giorno == currentDayName }

            // Lista degli esercizi scorrevole
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                items(currentList, key = { it.id }) { exercise ->
                    ExerciseCard(
                        exercise = exercise,
                        onUpdate = { updatedEx ->
                            val updatedList = exercises.map {
                                if (it.id == updatedEx.id) updatedEx else it
                            }
                            exercises = updatedList
                            WorkoutCsvManager.saveExercises(context, updatedList)
                        },
                        onEditClick = { exerciseToEdit = exercise },
                        onDeleteClick = { exerciseToDelete = exercise }
                    )
                }
            }

            // Pannello Cronometro fisso in basso
            StopwatchPanel()
        }
    }

    // --- DIALOGHI DI GESTIONE ---

    // 1. Dialogo Modifica Esercizio Completa
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

    // 2. Dialogo Conferma Eliminazione Esercizio
    exerciseToDelete?.let { ex ->
        AlertDialog(
            onDismissRequest = { exerciseToDelete = null },
            title = { Text("Conferma eliminazione") },
            text = { Text("Sei sicuro di voler eliminare l'esercizio \"${ex.nome}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        val newList = exercises.filter { it.id != ex.id }
                        exercises = newList
                        WorkoutCsvManager.saveExercises(context, newList)
                        exerciseToDelete = null
                        Toast.makeText(context, "Esercizio eliminato", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Elimina")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { exerciseToDelete = null }) {
                    Text("Annulla")
                }
            }
        )
    }

    // 3. Dialogo Aggiungi Nuovo Esercizio alla scheda corrente
    if (showAddExerciseDialog) {
        val currentDay = days.getOrElse(selectedTab) { "Giorno 1" }
        val newEx = Exercise(
            id = "${currentDay}_${System.currentTimeMillis()}",
            giorno = currentDay,
            nome = "",
            muscoli = "",
            target = "",
            recupero = ""
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

    // 4. Dialogo Nuova Scheda / Giorno
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
                                recupero = "1'"
                            )
                            val newList = exercises + dummyEx
                            exercises = newList
                            WorkoutCsvManager.saveExercises(context, newList)
                            selectedTab = days.size // Passa alla nuova scheda
                            showNewDayDialog = false
                            Toast.makeText(context, "Nuova scheda creata!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Crea")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNewDayDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    // 5. Editor CSV Testuale Integrato
    if (showCsvEditorDialog) {
        var rawCsvText by remember { mutableStateOf(WorkoutCsvManager.getRawCsv(context)) }

        AlertDialog(
            onDismissRequest = { showCsvEditorDialog = false },
            title = { Text("Modifica Configurazione CSV", fontSize = 16.sp) },
            text = {
                OutlinedTextField(
                    value = rawCsvText,
                    onValueChange = { rawCsvText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
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
                            showCsvEditorDialog = false
                            Toast.makeText(context, "CSV Salvato!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Errore nella sintassi CSV", Toast.LENGTH_LONG).show()
                        }
                    }
                ) {
                    Text("Salva")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCsvEditorDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
fun ExerciseCard(
    exercise: Exercise,
    onUpdate: (Exercise) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = exercise.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        text = "${exercise.muscoli} | Target: ${exercise.target} | Rec: ${exercise.recupero}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Icone di Modifica e Cancellazione
                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Modifica Esercizio")
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Elimina Esercizio",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = exercise.kg,
                    onValueChange = { newKg ->
                        onUpdate(exercise.copy(kg = newKg))
                    },
                    label = { Text("Kg") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = exercise.note,
                    onValueChange = { newNote ->
                        onUpdate(exercise.copy(note = newNote))
                    },
                    label = { Text("Eseguite / Note") },
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )
            }
        }
    }
}

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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it },
                        label = { Text("Target (es. 4 x 8)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = recupero,
                        onValueChange = { recupero = it },
                        label = { Text("Recupero (es. 2')") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
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
                        onConfirm(
                            initialExercise.copy(
                                nome = nome.trim(),
                                muscoli = muscoli.trim(),
                                target = target.trim(),
                                recupero = recupero.trim(),
                                kg = kg.trim(),
                                note = note.trim()
                            )
                        )
                    }
                }
            ) {
                Text("Salva")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
fun StopwatchPanel() {
    var timeInSeconds by remember { mutableLongStateOf(0L) }
    var isRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(1000L)
            timeInSeconds++
        }
    }

    val minutes = timeInSeconds / 60
    val seconds = timeInSeconds % 60
    val formattedTime = String.format("%02d:%02d", minutes, seconds)

    Surface(
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
            Text(
                text = formattedTime,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { isRunning = true },
                    enabled = !isRunning,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("START", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { isRunning = false },
                    enabled = isRunning,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("STOP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        isRunning = false
                        timeInSeconds = 0L
                    },
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("RST", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}