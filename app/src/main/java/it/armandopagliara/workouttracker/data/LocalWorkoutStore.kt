// DEV_1.0.3
package it.armandopagliara.workouttracker.data

import android.content.Context

object LocalWorkoutStore {
    private const val PREFS_NAME = "workout_presets_archive"
    private const val DEFAULT_PRESET_NAME = "Full Body"

    private fun getPrefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Assicura che "Full Body" sia sempre presente tra le opzioni dell'Archivio Locale */
    fun ensureDefaultPreset(context: Context) {
        val prefs = getPrefs(context)
        if (!prefs.contains(DEFAULT_PRESET_NAME)) {
            savePreset(context, DEFAULT_PRESET_NAME, WorkoutCsvManager.FULL_BODY_CSV)
        }
    }

    fun listSavedPresets(context: Context): List<String> {
        ensureDefaultPreset(context)
        val prefs = getPrefs(context)
        return prefs.all.keys.sorted()
    }

    fun savePreset(context: Context, name: String, csvContent: String): Boolean {
        if (name.isBlank()) return false
        return try {
            val prefs = getPrefs(context)
            prefs.edit().putString(name.trim(), csvContent).apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun loadPreset(context: Context, name: String): String? {
        ensureDefaultPreset(context)
        val prefs = getPrefs(context)
        return prefs.getString(name, null)
    }

    fun deletePreset(context: Context, name: String): Boolean {
        return try {
            val prefs = getPrefs(context)
            prefs.edit().remove(name).apply()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}