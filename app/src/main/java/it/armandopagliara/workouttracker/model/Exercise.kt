package it.armandopagliara.workouttracker.model

/**
 * Modello dati che rappresenta un singolo esercizio all'interno di una scheda.
 *
 * @property id Identificatore unico dell'esercizio (es. "Scheda 1_Leg Extension_123456").
 * @property scheda Nome della scheda di appartenenza (es. "Scheda 1: Gambe").
 * @property nome Nome dell'esercizio (es. "Leg Extension").
 * @property muscoli Distretto muscolare interessato (es. "Quadricipiti").
 * @property target Serie e ripetizioni target (es. "3 x 8").
 * @property recupero Tempo di recupero in secondi (stringa numerica, es. "120").
 * @property kg Peso / Carico utilizzato (es. "50kg" o vuoto).
 * @property note Note aggiuntive dell'utente (es. "Eseguito con buffer" o vuoto).
 */
data class Exercise(
    val id: String,
    val scheda: String,
    val nome: String,
    val muscoli: String,
    val target: String,
    val recupero: String,
    val kg: String = "",
    val note: String = ""
)