package it.armandopagliara.workouttracker.data

import android.content.Context
import it.armandopagliara.workouttracker.model.Exercise
import java.io.File

object WorkoutCsvManager {
    private const val FILE_NAME = "workout_config.csv"
    private const val CSV_HEADER = "scheda,nome,muscoli,target,recupero,kg,note"

    private val DEFAULT_CSV = """
        scheda,nome,muscoli,target,recupero,kg,note
        Scheda A: Spinta,Panca Piana,Pettorali,4x8,90,70,Bilanciere olimpico
        Scheda A: Spinta,Dip,Pettorali / Tricipiti,3x10,60,,Zavorra 10kg
        Scheda B: Trazione,Trazioni,Dorsali,4x6,120,,Presa prona
        Scheda B: Trazione,Rematore,Dorsali,3x8,90,60,Bilanciere
    """.trimIndent()

    fun getFile(context: Context): File = File(context.filesDir, FILE_NAME)

    fun loadExercises(context: Context): List<Exercise> {
        val file = getFile(context)
        if (!file.exists()) {
            file.writeText(DEFAULT_CSV)
        }
        return parseCsv(file.readText())
    }

    fun saveExercises(context: Context, exercises: List<Exercise>): Boolean {
        return try {
            val file = getFile(context)
            file.writeText(toCsv(exercises))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun getRawCsv(context: Context): String {
        val file = getFile(context)
        if (!file.exists()) {
            file.writeText(DEFAULT_CSV)
        }
        return file.readText()
    }

    fun saveRawCsv(context: Context, rawText: String): Boolean {
        return try {
            val parsed = parseCsv(rawText)
            if (parsed.isNotEmpty()) {
                val file = getFile(context)
                file.writeText(toCsv(parsed))
                true
            } else false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun resetToDefault(context: Context): List<Exercise> {
        val file = getFile(context)
        file.writeText(DEFAULT_CSV)
        return parseCsv(DEFAULT_CSV)
    }

    fun toCsv(exercises: List<Exercise>): String {
        val sb = StringBuilder()
        sb.append(CSV_HEADER).append("\n")
        exercises.forEach { ex ->
            val row = listOf(
                ex.scheda.escapeCsv(),
                ex.nome.escapeCsv(),
                ex.muscoli.escapeCsv(),
                ex.target.escapeCsv(),
                ex.recupero.escapeCsv(),
                ex.kg.escapeCsv(),
                ex.note.escapeCsv()
            ).joinToString(",")
            sb.append(row).append("\n")
        }
        return sb.toString().trimEnd()
    }

    fun parseCsv(csvText: String): List<Exercise> {
        val list = mutableListOf<Exercise>()
        val lines = csvText.lines().map { it.trim() }.filter { it.isNotBlank() }
        if (lines.isEmpty()) return list

        val firstLineLower = lines.first().lowercase()
        val startIndex = if (firstLineLower.startsWith("scheda") || firstLineLower.startsWith("giorno")) 1 else 0

        for (i in startIndex until lines.size) {
            val line = lines[i]
            val tokens = parseCsvLine(line)
            if (tokens.size >= 5) {
                val scheda = tokens.getOrElse(0) { "Scheda 1" }.ifBlank { "Scheda 1" }
                val nome = tokens.getOrElse(1) { "" }
                val muscoli = tokens.getOrElse(2) { "" }
                val target = tokens.getOrElse(3) { "" }
                val recupero = tokens.getOrElse(4) { "90" }
                val kg = tokens.getOrElse(5) { "" }
                val note = tokens.getOrElse(6) { "" }

                if (nome.isNotBlank()) {
                    list.add(
                        Exercise(
                            id = "${scheda}_${i}_${System.currentTimeMillis()}",
                            scheda = scheda,
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
        val tokens = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when (ch) {
                '"' -> inQuotes = !inQuotes
                ',' -> {
                    if (inQuotes) {
                        sb.append(ch)
                    } else {
                        tokens.add(sb.toString().trim())
                        sb.setLength(0)
                    }
                }
                else -> sb.append(ch)
            }
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    private fun String.escapeCsv(): String {
        return if (contains(",") || contains("\"") || contains("\n")) {
            "\"${replace("\"", "\"\"")}\""
        } else {
            this
        }
    }
}