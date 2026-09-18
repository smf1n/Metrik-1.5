package app.metrik

import android.app.Application
import app.metrik.ui.ThemeChoice

/**
 * Application-класс Metrik.
 * Запускается первым при старте приложения.
 * Инициализирует тему из сохранённых настроек.
 */
class MetrikApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Загружаем сохранённую тему
        ThemeChoice.init(this)
    }
}
