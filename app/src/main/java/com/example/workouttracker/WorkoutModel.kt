package com.example.workouttracker

import android.content.Context
import android.content.SharedPreferences

data class Exercise(
    val id: String,
    val name: String,
    val category: String,
    val target: String,
    val rest: String
)

object WorkoutData {
    val giorno1 = listOf(
        Exercise("g1_1", "Squat Controllato", "Gambe (Base)", "3 x 8", "2'"),
        Exercise("g1_2", "Distensioni Panca Inclinata Manubri", "Petto", "4 x 6-8", "2'"),
        Exercise("g1_3", "Alzate Laterali ai Cavi", "Spalle", "3 x 12", "1'"),
        Exercise("g1_4", "Leg Curl", "Gambe (Posteriore)", "3 x 10-12", "1'30\""),
        Exercise("g1_5", "Pushdown ai Cavi con Corda", "Tricipiti", "3 x 10-12", "45\""),
        Exercise("g1_6", "Crunch + Plank Isometrico", "Core", "3 x cedimento / 3 x 45s", "45\"")
    )

    val giorno2 = listOf(
        Exercise("g2_1", "Leg Press 45°", "Gambe (Base)", "3 x 8-10", "2'"),
        Exercise("g2_2", "Lat Machine Avanti", "Dorso", "4 x 6-8", "2'"),
        Exercise("g2_3", "Rematore Manubrio su Panca", "Dorso", "3 x 8 / lato", "1'30\""),
        Exercise("g2_4", "Leg Curl", "Gambe (Posteriore)", "3 x 10-12", "1'30\""),
        Exercise("g2_5", "Crucifix Reverse Fly", "Spalle / Cuffia", "3 x 12-15", "45\""),
        Exercise("g2_6", "Curl Manubri in Piedi", "Bicipiti", "3 x 10", "45\""),
        Exercise("g2_7", "Leg Raises + Crunch Inverso", "Core", "3 x 12-15", "45\"")
    )
}

class WorkoutStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("workout_db", Context.MODE_PRIVATE)

    fun saveLog(id: String, weight: String, reps: String) {
        prefs.edit().putString("${id}_weight", weight).putString("${id}_reps", reps).apply()
    }

    fun getWeight(id: String): String = prefs.getString("${id}_weight", "") ?: ""
    fun getReps(id: String): String = prefs.getString("${id}_reps", "") ?: ""
}