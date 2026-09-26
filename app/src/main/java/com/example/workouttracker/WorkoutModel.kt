package com.example.workouttracker

import android.content.Context
import java.io.File

data class Exercise(
    val id: String,
    val giorno: String,
    val nome: String,
    val muscoli: String,
    val target: String,
    val recupero: String,
    var kg: String = "",
    var note: String = ""
)

object WorkoutCsvManager {
    private const val FILE_NAME = "workout_config.csv"

    val defaultCsvContent = """
        giorno,nome,muscoli,target,recupero,kg,note
        Giorno 1: Gambe,Riscaldamento,Tapis roulant,5',3%,,
        Giorno 1: Gambe,Leg Extension,Quadricipiti,3 x 8,2',,
        Giorno 1: Gambe,Leg Curl,Bicipiti femorali,3 x 8,2',,
    """.trimIndent()

    private fun getFile(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    fun loadExercises(context: Context): List<Exercise> {
        val file = getFile(context)
        if (!file.exists()) {
            file.writeText(defaultCsvContent)
        }
        return parseCsv(file.readText())
    }

    fun saveExercises(context: Context, exercises: List<Exercise>) {
        val csvText = toCsv(exercises)
        getFile(context).writeText(csvText)
    }

    fun saveRawCsv(context: Context, csvText: String): Boolean {
        return try {
            val parsed = parseCsv(csvText)
            if (parsed.isNotEmpty()) {
                getFile(context).writeText(csvText)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    fun getRawCsv(context: Context): String {
        val file = getFile(context)
        if (!file.exists()) {
            file.writeText(defaultCsvContent)
        }
        return file.readText()
    }

    fun resetToDefault(context: Context): List<Exercise> {
        getFile(context).writeText(defaultCsvContent)
        return parseCsv(defaultCsvContent)
    }

    fun parseCsv(csvText: String): List<Exercise> {
        val list = mutableListOf<Exercise>()
        val lines = csvText.lines()
        if (lines.isEmpty()) return list

        val startIndex = if (lines.firstOrNull()?.trim()?.lowercase()?.startsWith("giorno") == true) 1 else 0

        for (i in startIndex until lines.size) {
            val line = lines[i].trim()
            if (line.isEmpty()) continue
            val tokens = parseCsvLine(line)
            if (tokens.size >= 5) {
                val giorno = tokens.getOrElse(0) { "" }.trim()
                val nome = tokens.getOrElse(1) { "" }.trim()
                val muscoli = tokens.getOrElse(2) { "" }.trim()
                val target = tokens.getOrElse(3) { "" }.trim()
                val recupero = tokens.getOrElse(4) { "" }.trim()
                val kg = tokens.getOrElse(5) { "" }.trim()
                val note = tokens.getOrElse(6) { "" }.trim()

                if (giorno.isNotEmpty() && nome.isNotEmpty()) {
                    list.add(
                        Exercise(
                            id = "${giorno}_${nome}_$i",
                            giorno = giorno,
                            nome = nome,
                            muscoli = muscoli,
                            target = target,
                            recupero = recupero,
                            kg = kg,
                            note = note
                        )
                    )
                }
            }
        }
        return list
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            if (ch == '"') {
                inQuotes = !inQuotes
            } else if (ch == ',' && !inQuotes) {
                result.add(sb.toString())
                sb.clear()
            } else {
                sb.append(ch)
            }
        }
        result.add(sb.toString())
        return result
    }

    fun toCsv(exercises: List<Exercise>): String {
        val sb = StringBuilder()
        sb.append("giorno,nome,muscoli,target,recupero,kg,note\n")
        for (ex in exercises) {
            val fields = listOf(ex.giorno, ex.nome, ex.muscoli, ex.target, ex.recupero, ex.kg, ex.note)
            val line = fields.joinToString(",") { field ->
                if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
                    "\"" + field.replace("\"", "\"\"") + "\""
                } else {
                    field
                }
            }
            sb.append(line).append("\n")
        }
        return sb.toString()
    }
}