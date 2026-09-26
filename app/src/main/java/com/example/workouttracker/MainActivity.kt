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
        
        // Mantiene lo schermo acceso durante l'uso
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

    // Ricava i giorni disponibili dinamicamente dal file CSV (es. "Giorno 1: Spinta", "Giorno 2: Trazione")
    val days = remember(exercises) {
        exercises.map { it.giorno }.distinct().ifEmpty { listOf("Giorno 1") }
    }

    // Picker per importare un file CSV esterno
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

    // Picker per esportare la configurazione CSV su file
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
                    TextButton(onClick = { showMenu = true }) {
                        Text("⚙️ CSV")
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
            // Tab per i giorni dell'allenamento
            TabRow(selectedTabIndex = selectedTab.coerceAtMost(days.size - 1)) {
                days.forEachIndexed { index, dayName ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(dayName, fontSize = 13.sp) }
                    )
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
                        }
                    )
                }
            }

            // Pannello Cronometro fisso in basso
            StopwatchPanel()
        }
    }

    // Dialog Editor Testuale CSV integrato
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
fun ExerciseCard(exercise: Exercise, onUpdate: (Exercise) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = exercise.nome, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(
                text = "${exercise.muscoli} | Target: ${exercise.target} | Rec: ${exercise.recupero}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )

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