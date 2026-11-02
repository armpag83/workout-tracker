// DEV_1.0.3
package it.armandopagliara.workouttracker

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import kotlinx.coroutines.delay

import it.armandopagliara.workouttracker.data.LocalWorkoutStore
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

        val isTablet = resources.configuration.smallestScreenWidthDp >= 600
        requestedOrientation = if (isTablet) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true

        setContent {
            MaterialTheme {
                WorkoutApp()
            }
        }
    }

    fun bringToForeground() {
        try {
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun WorkoutApp() {
    val context = LocalContext.current
    val activity = context as? MainActivity
    val currentAppVersion = BuildConfig.VERSION_NAME

    val isTablet = LocalConfiguration.current.smallestScreenWidthDp >= 600
    val scale = if (isTablet) 1.20f else 1.0f

    var exercises by remember { mutableStateOf(WorkoutCsvManager.loadExercises(context)) }
    var selectedTab by rememberSaveable { mutableStateOf(0) }
    var highlightedExerciseId by rememberSaveable { mutableStateOf<String?>(null) }

    var exerciseToCopy by remember { mutableStateOf<Exercise?>(null) }

    var isTimerRunning by remember { mutableStateOf(false) }
    var pendingExerciseIdSelection by remember { mutableStateOf<String?>(null) }
    var showConfirmExerciseChangeDialog by remember { mutableStateOf(false) }

    var showMenu by remember { mutableStateOf(false) }
    var showCsvEditorDialog by remember { mutableStateOf(false) }
    var showSavePresetDialog by remember { mutableStateOf(false) }
    var showLoadPresetDialog by remember { mutableStateOf(false) }

    var exerciseToEdit by remember { mutableStateOf<Exercise?>(null) }
    var exerciseToDelete by remember { mutableStateOf<Exercise?>(null) }
    var showAddExerciseDialog by remember { mutableStateOf(false) }
    var showNewSchedaDialog by remember { mutableStateOf(false) }
    var showRenameSchedaDialog by remember { mutableStateOf(false) }
    var showDeleteSchedaDialog by remember { mutableStateOf(false) }
    var showCsvAccessConfirmDialog by remember { mutableStateOf(false) }

    var showAddExerciseTooltip by remember { mutableStateOf(false) }

    LaunchedEffect(showAddExerciseTooltip) {
        if (showAddExerciseTooltip) {
            delay(3000L)
            showAddExerciseTooltip = false
        }
    }

    val sharedPrefs = remember { context.getSharedPreferences("workout_tracker_prefs", Context.MODE_PRIVATE) }
    val lastSeenVersion = sharedPrefs.getString("last_seen_version", "")
    val isFreshInstall = lastSeenVersion.isNullOrEmpty()

    var showWelcomeTutorial by remember { mutableStateOf(lastSeenVersion != currentAppVersion) }
    var showLoadFullBodyPrompt by remember { mutableStateOf(false) }

    // Fallback su "Scheda personale" se non ci sono esercizi caricati
    val schede = remember(exercises) {
        exercises.map { it.scheda }.distinct().ifEmpty { listOf("Scheda personale") }
    }

    val selectedExercise = exercises.find { it.id == highlightedExerciseId }
    val targetRecuperoSeconds = selectedExercise?.recupero?.toIntOrNull()?.takeIf { it > 0 }

    fun moveScheda(fromIndex: Int, toIndex: Int) {
        val schedaList = schede.toMutableList()
        if (fromIndex in schedaList.indices && toIndex in schedaList.indices) {
            val moved = schedaList.removeAt(fromIndex)
            schedaList.add(toIndex, moved)

            val reorderedExercises = schedaList.flatMap { name ->
                exercises.filter { it.scheda == name }
            }

            exercises = reorderedExercises
            WorkoutCsvManager.saveExercises(context, reorderedExercises)
            selectedTab = toIndex
        }
    }

    fun moveExercise(fromIndex: Int, toIndex: Int, currentScheda: String) {
        val schedaExercises = exercises.filter { it.scheda == currentScheda }.toMutableList()
        if (fromIndex in schedaExercises.indices && toIndex in schedaExercises.indices) {
            val movedItem = schedaExercises.removeAt(fromIndex)
            schedaExercises.add(toIndex, movedItem)

            val newExercises = mutableListOf<Exercise>()
            var inserted = false
            for (ex in exercises) {
                if (ex.scheda == currentScheda) {
                    if (!inserted) {
                        newExercises.addAll(schedaExercises)
                        inserted = true
                    }
                } else {
                    newExercises.add(ex)
                }
            }
            exercises = newExercises
            WorkoutCsvManager.saveExercises(context, newExercises)
        }
    }

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
                            modifier = Modifier.size((40 * scale).dp).clip(RoundedCornerShape((10 * scale).dp))
                        )
                        Spacer(modifier = Modifier.width((8 * scale).dp))
                        Column {
                            Text(text = "Workout", fontSize = (14 * scale).sp, fontWeight = FontWeight.ExtraBold, lineHeight = (15 * scale).sp)
                            Text(text = "Tracker", fontSize = (14 * scale).sp, fontWeight = FontWeight.ExtraBold, lineHeight = (15 * scale).sp)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showWelcomeTutorial = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Guida App", modifier = Modifier.size((24 * scale).dp))
                    }

                    TextButton(onClick = { showNewSchedaDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size((18 * scale).dp))
                        Spacer(modifier = Modifier.width((4 * scale).dp))
                        Text("Nuova scheda", fontSize = (13 * scale).sp)
                    }

                    IconButton(onClick = { showCsvAccessConfirmDialog = true }) {
                        Text("⚙️", fontSize = (18 * scale).sp)
                    }

                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("💾 Salva in Archivio Locale", fontSize = (14 * scale).sp) },
                            onClick = { showMenu = false; showSavePresetDialog = true }
                        )
                        DropdownMenuItem(
                            text = { Text("📁 Carica da Archivio Locale", fontSize = (14 * scale).sp) },
                            onClick = { showMenu = false; showLoadPresetDialog = true }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("✏️ Modifica CSV (Testo)", fontSize = (14 * scale).sp) },
                            onClick = { showMenu = false; showCsvEditorDialog = true }
                        )
                        DropdownMenuItem(
                            text = { Text("📂 Importa CSV da File", fontSize = (14 * scale).sp) },
                            onClick = { showMenu = false; importLauncher.launch("*/*") }
                        )
                        DropdownMenuItem(
                            text = { Text("💾 Esporta CSV su File", fontSize = (14 * scale).sp) },
                            onClick = { showMenu = false; exportLauncher.launch("workout_config.csv") }
                        )
                        DropdownMenuItem(
                            text = { Text("🔄 Ripristina Predefinito", fontSize = (14 * scale).sp) },
                            onClick = {
                                showMenu = false
                                exercises = WorkoutCsvManager.resetToDefault(context)
                                selectedTab = 0
                                highlightedExerciseId = null
                                exerciseToCopy = null
                                Toast.makeText(context, "Configurazione ripristinata", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(start = (4 * scale).dp, end = (2 * scale).dp)
                            .clip(RoundedCornerShape((20 * scale).dp))
                            .background(
                                if (showAddExerciseTooltip) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else Color.Transparent
                            )
                            .combinedClickable(
                                onClick = { showAddExerciseDialog = true },
                                onLongClick = { showAddExerciseTooltip = true }
                            )
                            .padding(horizontal = (8 * scale).dp, vertical = (8 * scale).dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aggiungi Esercizio",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size((24 * scale).dp)
                        )
                        AnimatedVisibility(visible = showAddExerciseTooltip) {
                            Text(
                                text = "Aggiungi esercizio",
                                fontSize = (12 * scale).sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = (4 * scale).dp, end = (4 * scale).dp)
                            )
                        }
                    }

                    ScrollableTabRow(
                        selectedTabIndex = selectedTab.coerceAtMost((schede.size - 1).coerceAtLeast(0)),
                        edgePadding = 0.dp,
                        modifier = Modifier.weight(1f)
                    ) {
                        schede.forEachIndexed { index, schedaName ->
                            val isSelected = selectedTab == index
                            Tab(
                                selected = isSelected,
                                onClick = { selectedTab = index },
                                text = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy((2 * scale).dp)
                                    ) {
                                        if (isSelected) {
                                            IconButton(
                                                onClick = { moveScheda(index, index - 1) },
                                                enabled = index > 0,
                                                modifier = Modifier.size((22 * scale).dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.KeyboardArrowLeft,
                                                    contentDescription = "Sposta scheda a sinistra",
                                                    modifier = Modifier.size((16 * scale).dp),
                                                    tint = if (index > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                )
                                            }
                                        }

                                        Text(schedaName, fontSize = (13 * scale).sp)

                                        if (isSelected) {
                                            IconButton(
                                                onClick = { showRenameSchedaDialog = true },
                                                modifier = Modifier.size((22 * scale).dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.Edit,
                                                    contentDescription = "Rinomina Scheda",
                                                    modifier = Modifier.size((14 * scale).dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }

                                            IconButton(
                                                onClick = { moveScheda(index, index + 1) },
                                                enabled = index < schede.size - 1,
                                                modifier = Modifier.size((22 * scale).dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.KeyboardArrowRight,
                                                    contentDescription = "Sposta scheda a destra",
                                                    modifier = Modifier.size((16 * scale).dp),
                                                    tint = if (index < schede.size - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                )
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            val currentSchedaName = schede.getOrElse(selectedTab.coerceAtMost((schede.size - 1).coerceAtLeast(0))) { "" }
            val currentList = exercises.filter { it.scheda == currentSchedaName }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = (12 * scale).dp, vertical = (6 * scale).dp)
            ) {
                itemsIndexed(currentList, key = { _, exercise -> exercise.id }) { index, exercise ->
                    val isHighlighted = exercise.id == highlightedExerciseId

                    ExerciseCard(
                        exercise = exercise,
                        isHighlighted = isHighlighted,
                        canMoveUp = index > 0,
                        canMoveDown = index < currentList.size - 1,
                        onCardClick = {
                            val targetSelection = if (isHighlighted) null else exercise.id
                            if (isTimerRunning && targetSelection != highlightedExerciseId) {
                                pendingExerciseIdSelection = targetSelection
                                showConfirmExerciseChangeDialog = true
                            } else {
                                highlightedExerciseId = targetSelection
                            }
                        },
                        onMoveUp = { moveExercise(index, index - 1, currentSchedaName) },
                        onMoveDown = { moveExercise(index, index + 1, currentSchedaName) },
                        onCopyClick = { exerciseToCopy = exercise },
                        onEditClick = { exerciseToEdit = exercise },
                        onDeleteClick = { exerciseToDelete = exercise }
                    )
                }

                if (currentSchedaName.isNotEmpty() && currentList.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height((12 * scale).dp))
                        Button(
                            onClick = { showDeleteSchedaDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.onErrorContainer
                            ),
                            modifier = Modifier.fillMaxWidth().padding(vertical = (8 * scale).dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Elimina intera scheda", fontWeight = FontWeight.Bold, fontSize = (15 * scale).sp, color = MaterialTheme.colorScheme.error)
                                Spacer(modifier = Modifier.width((12 * scale).dp))
                                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size((20 * scale).dp))
                            }
                        }
                    }
                }
            }

            StopwatchPanel(
                targetRecuperoSeconds = targetRecuperoSeconds,
                selectedExerciseId = highlightedExerciseId,
                onRunningStateChange = { running -> isTimerRunning = running },
                onTimerFinished = {
                    activity?.bringToForeground()
                }
            )
        }
    }

    // --- POPUP CARICAMENTO FULL BODY PRIMA INSTALLAZIONE ---
    if (showLoadFullBodyPrompt) {
        AlertDialog(
            onDismissRequest = { showLoadFullBodyPrompt = false },
            title = {
                Text(
                    text = "Caricare la scheda d'esempio?",
                    fontSize = (18 * scale).sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Vuoi caricare la scheda d'esempio Full body?",
                    fontSize = (14 * scale).sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (WorkoutCsvManager.saveRawCsv(context, WorkoutCsvManager.FULL_BODY_CSV)) {
                            exercises = WorkoutCsvManager.loadExercises(context)
                            selectedTab = 0
                            highlightedExerciseId = null
                            Toast.makeText(context, "Scheda Full body caricata!", Toast.LENGTH_SHORT).show()
                        }
                        showLoadFullBodyPrompt = false
                    }
                ) {
                    Text("Sì", fontSize = (14 * scale).sp)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { showLoadFullBodyPrompt = false }
                ) {
                    Text("No, lascia vuoto", fontSize = (14 * scale).sp)
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
                // Se è la prima installazione, attiva la richiesta per caricare la scheda Full Body
                if (isFreshInstall) {
                    showLoadFullBodyPrompt = true
                }
            }
        )
    }

    if (showNewSchedaDialog) {
        var newSchedaName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewSchedaDialog = false },
            title = { Text("Crea Nuova Scheda", fontSize = (18 * scale).sp) },
            text = {
                OutlinedTextField(
                    value = newSchedaName,
                    onValueChange = { newSchedaName = it.take(40) },
                    label = { Text("Nome scheda (max 40)", fontSize = (12 * scale).sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSchedaName.isNotBlank()) {
                            val dummyEx = Exercise(
                                id = "${newSchedaName}_${System.currentTimeMillis()}",
                                scheda = newSchedaName.trim(),
                                nome = "Riscaldamento",
                                muscoli = "Generale",
                                target = "5'",
                                recupero = "180"
                            )
                            val newList = exercises + dummyEx
                            exercises = newList
                            WorkoutCsvManager.saveExercises(context, newList)
                            selectedTab = schede.size
                            showNewSchedaDialog = false
                            Toast.makeText(context, "Nuova scheda creata!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Crea", fontSize = (14 * scale).sp) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNewSchedaDialog = false }) { Text("Annulla", fontSize = (14 * scale).sp) }
            }
        )
    }

    if (showRenameSchedaDialog) {
        val currentSchedaName = schede.getOrElse(selectedTab.coerceAtMost((schede.size - 1).coerceAtLeast(0))) { "" }
        var newSchedaName by remember { mutableStateOf(currentSchedaName) }

        AlertDialog(
            onDismissRequest = { showRenameSchedaDialog = false },
            title = { Text("Rinomina Scheda", fontSize = (18 * scale).sp) },
            text = {
                OutlinedTextField(
                    value = newSchedaName,
                    onValueChange = { newSchedaName = it.take(40) },
                    label = { Text("Nome scheda (max 40)", fontSize = (12 * scale).sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newSchedaName.trim()
                        if (trimmed.isNotBlank() && trimmed != currentSchedaName) {
                            val newList = exercises.map {
                                if (it.scheda == currentSchedaName) it.copy(scheda = trimmed) else it
                            }
                            exercises = newList
                            WorkoutCsvManager.saveExercises(context, newList)
                            showRenameSchedaDialog = false
                            Toast.makeText(context, "Scheda rinominata!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Salva", fontSize = (14 * scale).sp) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRenameSchedaDialog = false }) { Text("Annulla", fontSize = (14 * scale).sp) }
            }
        )
    }

    exerciseToCopy?.let { exToCopy ->
        AlertDialog(
            onDismissRequest = { exerciseToCopy = null },
            title = { Text("Incolla \"${exToCopy.nome}\" in:", fontWeight = FontWeight.Bold, fontSize = (18 * scale).sp) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy((6 * scale).dp)
                ) {
                    schede.forEach { targetScheda ->
                        OutlinedButton(
                            onClick = {
                                val existsInTarget = exercises.any {
                                    it.scheda == targetScheda && it.nome.trim().equals(exToCopy.nome.trim(), ignoreCase = true)
                                }
                                val finalName = if (existsInTarget) "${exToCopy.nome} copia".take(70) else exToCopy.nome

                                val pasted = exToCopy.copy(
                                    id = "${targetScheda}_${System.currentTimeMillis()}",
                                    scheda = targetScheda,
                                    nome = finalName
                                )
                                val newList = exercises + pasted
                                exercises = newList
                                WorkoutCsvManager.saveExercises(context, newList)
                                Toast.makeText(context, "\"$finalName\" incollato in \"$targetScheda\"", Toast.LENGTH_SHORT).show()
                                exerciseToCopy = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("📋 $targetScheda", fontWeight = FontWeight.SemiBold, fontSize = (14 * scale).sp)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                Button(
                    onClick = { exerciseToCopy = null },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Annulla", fontSize = (14 * scale).sp)
                }
            }
        )
    }

    if (showSavePresetDialog) {
        var presetName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showSavePresetDialog = false },
            title = { Text("Salva nell'Archivio Locale", fontSize = (18 * scale).sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy((8 * scale).dp)) {
                    Text("Assegna un nome al preset locale per poterlo ricaricare in seguito:", fontSize = (13 * scale).sp)
                    OutlinedTextField(
                        value = presetName,
                        onValueChange = { presetName = it.take(40) },
                        label = { Text("Nome preset (max 40)", fontSize = (12 * scale).sp) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val currentCsv = WorkoutCsvManager.toCsv(exercises)
                        if (LocalWorkoutStore.savePreset(context, presetName, currentCsv)) {
                            Toast.makeText(context, "Preset \"$presetName\" salvato!", Toast.LENGTH_SHORT).show()
                            showSavePresetDialog = false
                        } else {
                            Toast.makeText(context, "Nome non valido o errore salvataggio", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) { Text("Salva", fontSize = (14 * scale).sp) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSavePresetDialog = false }) { Text("Annulla", fontSize = (14 * scale).sp) }
            }
        )
    }

    if (showLoadPresetDialog) {
        val savedPresets = remember { LocalWorkoutStore.listSavedPresets(context) }
        AlertDialog(
            onDismissRequest = { showLoadPresetDialog = false },
            title = { Text("Carica dall'Archivio Locale", fontSize = (18 * scale).sp) },
            text = {
                if (savedPresets.isEmpty()) {
                    Text("Nessun preset salvato in archivio.", fontSize = (13 * scale).sp)
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy((6 * scale).dp),
                        modifier = Modifier.fillMaxWidth().heightIn(max = (260 * scale).dp)
                    ) {
                        itemsIndexed(savedPresets) { _, name ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TextButton(
                                    onClick = {
                                        val content = LocalWorkoutStore.loadPreset(context, name)
                                        if (content != null && WorkoutCsvManager.saveRawCsv(context, content)) {
                                            exercises = WorkoutCsvManager.loadExercises(context)
                                            selectedTab = 0
                                            highlightedExerciseId = null
                                            Toast.makeText(context, "Preset \"$name\" caricato!", Toast.LENGTH_SHORT).show()
                                            showLoadPresetDialog = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("📂 $name", fontWeight = FontWeight.Bold, fontSize = (14 * scale).sp)
                                }
                                IconButton(
                                    onClick = {
                                        LocalWorkoutStore.deletePreset(context, name)
                                        showLoadPresetDialog = false
                                        Toast.makeText(context, "Preset eliminato", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Elimina preset", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size((20 * scale).dp))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                OutlinedButton(onClick = { showLoadPresetDialog = false }) { Text("Chiudi", fontSize = (14 * scale).sp) }
            }
        )
    }

    if (showConfirmExerciseChangeDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmExerciseChangeDialog = false },
            title = { Text("Attenzione", fontWeight = FontWeight.Bold, fontSize = (18 * scale).sp) },
            text = { Text("Recupero in corso! Cambiando esercizio il timer si fermerà.", fontSize = (14 * scale).sp) },
            confirmButton = {
                Button(
                    onClick = {
                        highlightedExerciseId = pendingExerciseIdSelection
                        showConfirmExerciseChangeDialog = false
                    }
                ) { Text("Procedi", fontSize = (14 * scale).sp) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmExerciseChangeDialog = false }) { Text("Annulla", fontSize = (14 * scale).sp) }
            }
        )
    }

    if (showCsvAccessConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showCsvAccessConfirmDialog = false },
            title = { Text("Impostazioni & Archivio", fontSize = (18 * scale).sp) },
            text = { Text("Vuoi accedere al menu di gestione dell'archivio locale e dei file CSV?", fontSize = (14 * scale).sp) },
            confirmButton = {
                Button(onClick = { showCsvAccessConfirmDialog = false; showMenu = true }) { Text("Accedi", fontSize = (14 * scale).sp) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCsvAccessConfirmDialog = false }) { Text("Annulla", fontSize = (14 * scale).sp) }
            }
        )
    }

    if (showDeleteSchedaDialog) {
        val currentSchedaName = schede.getOrElse(selectedTab.coerceAtMost((schede.size - 1).coerceAtLeast(0))) { "" }
        AlertDialog(
            onDismissRequest = { showDeleteSchedaDialog = false },
            title = { Text("Conferma eliminazione scheda", fontSize = (18 * scale).sp) },
            text = { Text("Sei sicuro di voler eliminare l'intera scheda \"$currentSchedaName\" e tutti gli esercizi contenuti?", fontSize = (14 * scale).sp) },
            confirmButton = {
                Button(
                    onClick = {
                        val newList = exercises.filter { it.scheda != currentSchedaName }
                        exercises = newList
                        WorkoutCsvManager.saveExercises(context, newList)
                        selectedTab = 0
                        highlightedExerciseId = null
                        showDeleteSchedaDialog = false
                        Toast.makeText(context, "Scheda eliminata", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Elimina Scheda", fontSize = (14 * scale).sp) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteSchedaDialog = false }) { Text("Annulla", fontSize = (14 * scale).sp) }
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
            title = { Text("Conferma eliminazione", fontSize = (18 * scale).sp) },
            text = { Text("Sei sicuro di voler eliminare l'esercizio \"${ex.nome}\"?", fontSize = (14 * scale).sp) },
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
                ) { Text("Elimina", fontSize = (14 * scale).sp) }
            },
            dismissButton = {
                OutlinedButton(onClick = { exerciseToDelete = null }) { Text("Annulla", fontSize = (14 * scale).sp) }
            }
        )
    }

    if (showAddExerciseDialog) {
        val currentScheda = schede.getOrElse(selectedTab.coerceAtMost((schede.size - 1).coerceAtLeast(0))) { "Scheda personale" }
        val newEx = Exercise(
            id = "${currentScheda}_${System.currentTimeMillis()}",
            scheda = currentScheda,
            nome = "",
            muscoli = "",
            target = "",
            recupero = "90"
        )
        ExerciseFormDialog(
            title = "Nuovo Esercizio ($currentScheda)",
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

    if (showCsvEditorDialog) {
        var rawCsvText by remember { mutableStateOf(WorkoutCsvManager.getRawCsv(context)) }

        AlertDialog(
            onDismissRequest = { showCsvEditorDialog = false },
            title = { Text("Modifica Configurazione CSV", fontSize = (16 * scale).sp) },
            text = {
                OutlinedTextField(
                    value = rawCsvText,
                    onValueChange = { rawCsvText = it },
                    modifier = Modifier.fillMaxWidth().height((300 * scale).dp),
                    label = { Text("scheda,nome,muscoli,target,recupero,kg,note", fontSize = (12 * scale).sp) },
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = (12 * scale).sp)
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
                ) { Text("Salva", fontSize = (14 * scale).sp) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showCsvEditorDialog = false }) { Text("Annulla", fontSize = (14 * scale).sp) }
            }
        )
    }
}