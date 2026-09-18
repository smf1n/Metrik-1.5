package app.metrik.data

import android.content.Context
import android.os.Environment
import android.os.StatFs
import java.util.Locale

/**
 * Читает информацию о хранилище (Storage).
 * Работает с internal storage (data) и external storage (sdcard).
 */
object StorageReader {

    /**
     * Снимок internal storage (где лежат приложения).
     */
    fun internal(): StorageInfo {
        return readStat(Environment.getDataDirectory().path)
    }

    /**
     * Снимок external storage (общее хранилище).
     */
    fun external(): StorageInfo {
        return try {
            readStat(Environment.getExternalStorageDirectory().path)
        } catch (e: Exception) {
            StorageInfo()
        }
    }

    /**
     * Снимок кэша приложения (то, куда пишем тесты).
     */
    fun cacheDir(context: Context): StorageInfo {
        return readStat(context.cacheDir.path)
    }

    /**
     * Основное хранилище (internal).
     */
    fun primary(): StorageInfo = internal()

    // ============================================================
    // Утилита чтения StatFs
    // ============================================================

    private fun readStat(path: String): StorageInfo {
        return try {
            val stat = StatFs(path)
            val blockSize = stat.blockSizeLong
            val total = stat.blockCountLong * blockSize
            val free = stat.availableBlocksLong * blockSize
            StorageInfo(
                total = total,
                free = free,
                used = total - free
            )
        } catch (e: Exception) {
            StorageInfo()
        }
    }

    // ============================================================
    // Форматирование
    // ============================================================

    /**
     * Байты → "128.5 ГБ" / "512 МБ"
     */
    fun formatBytes(bytes: Long): String {
        if (bytes <= 0) return "0 B"
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        val gb = mb / 1024.0
        val tb = gb / 1024.0

        return when {
            tb >= 1.0 -> String.format(Locale.US, "%.2f ТБ", tb)
            gb >= 1.0 -> String.format(Locale.US, "%.1f ГБ", gb)
            mb >= 1.0 -> String.format(Locale.US, "%.0f МБ", mb)
            kb >= 1.0 -> String.format(Locale.US, "%.0f КБ", kb)
            else -> "$bytes Б"
        }
    }

    /**
     * Использование в процентах: "41%"
     */
    fun formatUsedPercent(info: StorageInfo): String {
        if (info.total <= 0) return "0%"
        val percent = info.used.toDouble() / info.total * 100
        return String.format(Locale.US, "%.0f%%", percent)
    }
}
