// 1.1.1
package it.armandopagliara.workouttracker

import android.content.Context
import java.io.File

/**
 * Modello dati che rappresenta un singolo esercizio all'interno di una scheda.
 *
 * @property id Identificatore unico dell'esercizio (es. "Giorno 1_Leg Extension_123456").
 * @property giorno Nome del giorno/scheda di appartenenza (es. "Giorno 1: Gambe").
 * @property nome Nome dell'esercizio (es. "Leg Extension").
 * @property muscoli Distretto muscolare interessato (es. "Quadricipiti").
 * @property target Serie e ripetizioni target (es. "3 x 8").
 * @property recupero Tempo di recupero in secondi (stringa numerica, es. "120").
 * @property kg Peso / Carico utilizzato (es. "50kg" o vuoto).
 * @property note Note aggiuntive dell'utente (es. "Eseguito con buffer" o vuoto).
 */
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

/**
 * Gestore dell'archiviazione locale e del parsing/serializzazione in formato CSV.
 */
object WorkoutCsvManager {
    private const val FILE_NAME = "workout_config.csv"

    /** Configurazione CSV di default per la prima installazione o in caso di reset */
    val defaultCsvContent = """
        giorno,nome,muscoli,target,recupero,kg,note
        Giorno 1: Gambe,Riscaldamento,Tapis roulant,5',300,,
        Giorno 1: Gambe,Leg Extension,Quadricipiti,3 x 8,120,,
        Giorno 1: Gambe,Leg Curl,Bicipiti femorali,3 x 8,120,,
    """.trimIndent()

    /** Restituisce il riferimento al file CSV archiviato nella memoria interna privata dell'app */
    private fun getFile(context: Context): File {
        return File(context.filesDir, FILE_NAME)
    }

    /**
     * Carica e decodifica la lista degli esercizi dal file locale.
     * Se il file non esiste, crea quello di default.
     */
    fun loadExercises(context: Context): List<Exercise> {
        val file = getFile(context)
        if (!file.exists()) {
            file.writeText(defaultCsvContent)
        }
        return parseCsv(file.readText())
    }

    /** Salva la lista corrente di esercizi sul file CSV locale */
    fun saveExercises(context: Context, exercises: List<Exercise>) {
        val csvText = toCsv(exercises)
        getFile(context).writeText(csvText)
    }

    /**
     * Salva una stringa CSV grezza nel file locale previa validazione della sintassi.
     * @return true se il salvataggio ha avuto successo, false se il formato non è valido.
     */
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

    /** Restituisce il contenuto testo del file CSV per l'editor avanzato */
    fun getRawCsv(context: Context): String {
        val file = getFile(context)
        if (!file.exists()) {
            file.writeText(defaultCsvContent)
        }
        return file.readText()
    }

    /** Ripristina il file CSV alla configurazione iniziale di fabbrica */
    fun resetToDefault(context: Context): List<Exercise> {
        getFile(context).writeText(defaultCsvContent)
        return parseCsv(defaultCsvContent)
    }

    /**
     * Converte una stringa di testo CSV in una lista di oggetti [Exercise].
     * Filtra automaticamente valori non validi o vuoti.
     */
    fun parseCsv(csvText: String): List<Exercise> {
        val list = mutableListOf<Exercise>()
        val lines = csvText.lines()
        if (lines.isEmpty()) return list

        // Salta l'intestazione se presente
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
                val rawRecupero = tokens.getOrElse(4) { "" }.trim()
                val kg = tokens.getOrElse(5) { "" }.trim()
                val note = tokens.getOrElse(6) { "" }.trim()

                // Filtra il campo recupero accettando solo valori interamente numerici > 0
                val validRecupero = if ((rawRecupero.toIntOrNull() ?: 0) > 0) rawRecupero else ""

                if (giorno.isNotEmpty() && nome.isNotEmpty()) {
                    list.add(
                        Exercise(
                            id = "${giorno}_${nome}_$i",
                            giorno = giorno,
                            nome = nome,
                            muscoli = muscoli,
                            target = target,
                            recupero = validRecupero,
                            kg = kg,
                            note = note
                        )
                    )
                }
            }
        }
        return list
    }

    /** Parser per singola riga CSV con gestione dei virgoletti e delle virgole interne */
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

    /** Serializza una lista di oggetti [Exercise] in formato CSV con escaping dei caratteri */
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