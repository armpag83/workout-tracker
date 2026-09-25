package com.example.workouttracker

import android.os.Bundle
import androidx.activity.ComponentActivity
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                WorkoutApp()
            }
        }
    }
}

@Composable
fun WorkoutApp() {
    var selectedTab by remember { mutableStateOf(0) }
    val titles = listOf("Giorno 1: Spinta", "Giorno 2: Trazione")

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 14.sp) }
                )
            }
        }

        val currentList = if (selectedTab == 0) WorkoutData.giorno1 else WorkoutData.giorno2

        LazyColumn(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            items(currentList) { exercise ->
                ExerciseCard(exercise = exercise)
            }
        }
    }
}

@Composable
fun ExerciseCard(exercise: Exercise) {
    val context = LocalContext.current
    val storage = remember { WorkoutStorage(context) }

    var weight by remember { mutableStateOf(storage.getWeight(exercise.id)) }
    var repsCompleted by remember { mutableStateOf(storage.getReps(exercise.id)) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = exercise.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Text(text = "${exercise.category} | Target: ${exercise.target} | Rec: ${exercise.rest}", 
                 fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = weight,
                    onValueChange = { 
                        weight = it
                        storage.saveLog(exercise.id, weight, repsCompleted)
                    },
                    label = { Text("Kg") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = repsCompleted,
                    onValueChange = { 
                        repsCompleted = it
                        storage.saveLog(exercise.id, weight, repsCompleted)
                    },
                    label = { Text("Eseguite / Note") },
                    modifier = Modifier.weight(2f),
                    singleLine = true
                )
            }
        }
    }
}