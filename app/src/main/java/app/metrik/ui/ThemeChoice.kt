package app.metrik.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/**
 * Хранит текущий выбор темы.
 * Простой Singleton — не использует DataStore, чтобы не тянуть зависимости.
 */
object ThemeChoice {

    private const val PREFS_NAME = "metrik_prefs"
    private const val KEY_THEME = "theme"

    var current by mutableStateOf(MetrikTheme.AMOLED)
        private set

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val saved = prefs.getString(KEY_THEME, MetrikTheme.AMOLED.name)
        current = try {
            MetrikTheme.valueOf(saved ?: MetrikTheme.AMOLED.name)
        } catch (e: Exception) {
            MetrikTheme.AMOLED
        }
    }

    fun set(context: Context, theme: MetrikTheme) {
        current = theme
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME, theme.name).apply()
    }
}
