package it.armandopagliara.workouttracker.data

import android.content.Context
import java.io.File

/** Gestore dell'archivio locale dei preset delle schede all'interno dell'app */
object LocalWorkoutStore {
    private const val FOLDER_NAME = "saved_presets"

    private fun getFolder(context: Context): File {
        val folder = File(context.filesDir, FOLDER_NAME)
        if (!folder.exists()) folder.mkdirs()
        return folder
    }

    fun listSavedPresets(context: Context): List<String> {
        val folder = getFolder(context)
        return folder.listFiles { _, name -> name.endsWith(".csv") }
            ?.map { it.nameWithoutExtension }
            ?.sorted()
            ?: emptyList()
    }

    fun savePreset(context: Context, name: String, csvContent: String): Boolean {
        return try {
            val safeName = name.trim().replace(Regex("[^a-zA-Z0-9_\\-\\s]"), "_")
            if (safeName.isBlank()) return false
            val file = File(getFolder(context), "$safeName.csv")
            file.writeText(csvContent)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun loadPreset(context: Context, presetName: String): String? {
        val file = File(getFolder(context), "$presetName.csv")
        return if (file.exists()) file.readText() else null
    }

    fun deletePreset(context: Context, presetName: String): Boolean {
        val file = File(getFolder(context), "$presetName.csv")
        return if (file.exists()) file.delete() else false
    }
}