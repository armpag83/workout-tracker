package it.armandopagliara.workouttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

data class Esercizio(
    val id: String = java.util.UUID.randomUUID().toString(),
    val nome: String,
    val muscoli: String,
    val target: String,
    val recuperoSec: Int,
    val kg: String,
    val note: String
)

data class Scheda(
    val id: String = java.util.UUID.randomUUID().toString(),
    val nome: String,
    val esercizi: List<Esercizio> = emptyList()
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WorkoutTrackerScreen()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTrackerScreen() {
    var schede by remember {
        mutableStateOf(
            listOf(
                Scheda(
                    nome = "G1: petto spalle tricipiti",
                    esercizi = listOf(
                        Esercizio(
                            nome = "Riscaldamento tapis/scale",
                            muscoli = "",
                            target = "5-8' al 3%",
                            recuperoSec = 60,
                            kg = "",
                            note = ""
                        ),
                        Esercizio(
                            nome = "Squat Controllato",
                            muscoli = "Quadricipiti / Glutei",
                            target = "3 x 12",
                            recuperoSec = 60,
                            kg = "70",
                            note = "Squat rosso"
                        ),
                        Esercizio(
                            nome = "Distensioni Panca Inclinata Manubri / Bilanciere",
                            muscoli = "Pettorali",
                            target = "3 x 10-8-6",
                            recuperoSec = 60,
                            kg = "18-20",
                            note = "10-8-6 ripetizioni"
                        )
                    )
                ),
                Scheda(nome = "G2: deltoidi bicipiti"),
                Scheda(nome = "G3: gambe")
            )
        )
    }

    var selectedSchedaIndex by remember { mutableIntStateOf(0) }
    var selectedEsercizioIndex by remember { mutableIntStateOf(-1) }

    // Riferimento alla scheda corrente disponibile per tutto il Composable (inclusi i Dialog)
    val correnteScheda = schede.getOrNull(selectedSchedaIndex)
    
    var showNewSchedaDialog by remember { mutableStateOf(false) }
    var showNewEsercizioDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }

    // Timer State
    var timerSeconds by remember { mutableIntStateOf(57) }
    var isTimerRunning by remember { mutableStateOf(false) }

    LaunchedEffect(isTimerRunning) {
        while (isTimerRunning && timerSeconds > 0) {
            delay(1000L)
            timerSeconds--
        }
        if (timerSeconds == 0) {
            isTimerRunning = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier.size(36.dp),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.FitnessCenter,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Workout Tracker",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showInfoDialog = true }) {
                        Icon(Icons.Default.Info, contentDescription = "Info")
                    }
                    TextButton(onClick = { showNewSchedaDialog = true }) {
                        Text("+ Nuova scheda", fontWeight = FontWeight.SemiBold)
                    }
                    Box {
                        IconButton(onClick = { showSettingsMenu = true }) {
                            Icon(Icons.Default.Settings, contentDescription = "Impostazioni")
                        }
                        DropdownMenu(
                            expanded = showSettingsMenu,
                            onDismissRequest = { showSettingsMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Salva in Archivio Locale") },
                                onClick = { showSettingsMenu = false },
                                leadingIcon = { Icon(Icons.Default.Save, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Carica da Archivio Locale") },
                                onClick = { showSettingsMenu = false },
                                leadingIcon = { Icon(Icons.Default.Folder, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Modifica CSV (Testo)") },
                                onClick = { showSettingsMenu = false },
                                leadingIcon = { Icon(Icons.Default.Edit, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Importa CSV da File") },
                                onClick = { showSettingsMenu = false },
                                leadingIcon = { Icon(Icons.Default.FileOpen, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Esporta CSV su File") },
                                onClick = { showSettingsMenu = false },
                                leadingIcon = { Icon(Icons.Default.FileDownload, null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Ripristina Predefinito") },
                                onClick = { showSettingsMenu = false },
                                leadingIcon = { Icon(Icons.Default.Restore, null) }
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = String.format("%02d:%02d.0", timerSeconds / 60, timerSeconds % 60),
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("🔔", fontSize = 18.sp)
                        }
                        Text(
                            text = "Recupero (Timer)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { isTimerRunning = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("START")
                        }
                        Button(
                            onClick = { isTimerRunning = false },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) {
                            Text("STOP")
                        }
                        OutlinedButton(
                            onClick = {
                                isTimerRunning = false
                                timerSeconds = 60
                            }
                        ) {
                            Text("RST")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // TAB STRIP: PULSANTE + FISSO A SINISTRA E SCHEDE SCORREVOLI A DESTRA
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsante '+' FISSO a sinistra
                    IconButton(
                        onClick = { showNewEsercizioDialog = true },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Aggiungi Esercizio",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Schede scorrevoli
                    if (schede.isNotEmpty()) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedSchedaIndex.coerceIn(0, schede.size - 1),
                            edgePadding = 0.dp,
                            divider = {},
                            modifier = Modifier.weight(1f)
                        ) {
                            schede.forEachIndexed { index, scheda ->
                                val isSelected = selectedSchedaIndex == index
                                Tab(
                                    selected = isSelected,
                                    onClick = { selectedSchedaIndex = index },
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            if (isSelected) {
                                                IconButton(
                                                    onClick = { /* Freccia Sinistra */ },
                                                    modifier = Modifier.size(18.dp)
                                                ) {
                                                    Icon(Icons.Default.ChevronLeft, contentDescription = "Sposta Sinistra")
                                                }
                                            }
                                            Text(
                                                text = scheda.nome,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Edit,
                                                    contentDescription = "Rinomina",
                                                    modifier = Modifier.size(16.dp),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                                IconButton(
                                                    onClick = { /* Freccia Destra */ },
                                                    modifier = Modifier.size(18.dp)
                                                ) {
                                                    Icon(Icons.Default.ChevronRight, contentDescription = "Sposta Destra")
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // LISTA ESERCIZI
            if (correnteScheda != null && correnteScheda.esercizi.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(correnteScheda.esercizi) { index, esercizio ->
                        val isSelected = selectedEsercizioIndex == index
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { selectedEsercizioIndex = if (isSelected) -1 else index },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) 
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                else 
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isSelected) {
                                    Column(
                                        modifier = Modifier.padding(end = 8.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.KeyboardArrowUp,
                                            contentDescription = "Sposta Su",
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Icon(
                                            Icons.Default.KeyboardArrowDown,
                                            contentDescription = "Sposta Giù",
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = esercizio.nome,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    val dettagli = buildString {
                                        if (esercizio.muscoli.isNotBlank()) append("${esercizio.muscoli} | ")
                                        append("Target: ${esercizio.target}")
                                        if (esercizio.recuperoSec > 0) append(" | Recupero: ${esercizio.recuperoSec / 60}'")
                                    }
                                    Text(
                                        text = dettagli,
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (esercizio.kg.isNotBlank() || esercizio.note.isNotBlank()) {
                                        Row(modifier = Modifier.padding(top = 4.dp)) {
                                            if (esercizio.kg.isNotBlank()) {
                                                Text(
                                                    text = "Kg: ${esercizio.kg} ",
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                            if (esercizio.note.isNotBlank()) {
                                                Text(
                                                    text = "Note: ${esercizio.note}",
                                                    fontSize = 13.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    IconButton(onClick = { /* Copia */ }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplica", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { /* Modifica */ }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Edit, contentDescription = "Modifica", modifier = Modifier.size(18.dp))
                                    }
                                    IconButton(onClick = { /* Elimina */ }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Delete, contentDescription = "Elimina", tint = Color.Red, modifier = Modifier.size(18.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nessun esercizio presente.\nTocca '+' a sinistra per aggiungerne uno.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }

    // DIALOGS
    if (showNewEsercizioDialog) {
        AlertDialog(
            onDismissRequest = { showNewEsercizioDialog = false },
            title = { Text("Nuovo Esercizio (${correnteScheda?.nome ?: ""})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Nome Esercizio") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Muscoli (es. Petto)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = "",
                        onValueChange = {},
                        label = { Text("Target (es. 4 x 8)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(onClick = {}) { Text("-5") }
                        OutlinedTextField(
                            value = "90",
                            onValueChange = {},
                            label = { Text("Recupero (sec)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedButton(onClick = {}) { Text("+5") }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            label = { Text("Kg") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = "",
                            onValueChange = {},
                            label = { Text("Note") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showNewEsercizioDialog = false }) {
                    Text("Salva")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showNewEsercizioDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Workout Tracker v1.0.1") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Novità della versione 1.0.1:")
                    Text("• Pulsante '+' fisso a sinistra della barra delle schede.")
                    Text("• Ottimizzazione delle prestazioni e gestione memoria.")
                }
            },
            confirmButton = {
                Button(onClick = { showInfoDialog = false }) {
                    Text("Ho capito")
                }
            }
        )
    }
}