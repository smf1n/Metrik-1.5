package app.metrik.data

import java.io.File

/**
 * Читает информацию о CPU:
 * - текущие частоты ядер
 * - мин/макс частоты
 * - температуры
 * - загрузку из /proc/stat
 */
object CpuReader {

    /**
     * Текущие частоты всех ядер (в кГц).
     */
    fun currentFrequencies(): List<Long> {
        val cores = Runtime.getRuntime().availableProcessors()
        val result = mutableListOf<Long>()
        for (i in 0 until cores) {
            result.add(readLong("/sys/devices/system/cpu/cpu$i/cpufreq/scaling_cur_freq"))
        }
        return result
    }

    /**
     * Минимальные частоты всех ядер (в кГц).
     */
    fun minFrequencies(): List<Long> {
        val cores = Runtime.getRuntime().availableProcessors()
        val result = mutableListOf<Long>()
        for (i in 0 until cores) {
            result.add(readLong("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_min_freq"))
        }
        return result
    }

    /**
     * Максимальные частоты всех ядер (в кГц).
     */
    fun maxFrequencies(): List<Long> {
        val cores = Runtime.getRuntime().availableProcessors()
        val result = mutableListOf<Long>()
        for (i in 0 until cores) {
            result.add(readLong("/sys/devices/system/cpu/cpu$i/cpufreq/cpuinfo_max_freq"))
        }
        return result
    }

    /**
     * Средняя текущая частота (в кГц).
     */
    fun averageCurrentFreq(): Long {
        val freqs = currentFrequencies().filter { it > 0 }
        return if (freqs.isEmpty()) 0L else freqs.sum() / freqs.size
    }

    /**
     * Максимальная частота среди всех ядер (в кГц).
     */
    fun maxFreq(): Long {
        return maxFrequencies().maxOrNull() ?: 0L
    }

    /**
     * Минимальная частота среди всех ядер (в кГц).
     */
    fun minFreq(): Long {
        return minFrequencies().filter { it > 0 }.minOrNull() ?: 0L
    }

    /**
     * Температура CPU в °C.
     * Пробует разные пути (зависит от вендора).
     */
    fun temperature(): Float {
        val paths = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/class/thermal/thermal_zone1/temp",
            "/sys/devices/system/cpu/cpu0/thermal_zone0/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp",
            "/sys/class/hwmon/hwmon0/temp1_input"
        )
        for (path in paths) {
            val raw = readLong(path)
            if (raw > 0) {
                // Обычно в миллиградусах. Если > 1000 — делим на 1000.
                return if (raw > 1000) raw / 1000f else raw.toFloat()
            }
        }
        return 0f
    }

    /**
     * Загрузка CPU в % (0-100).
     * Читает /proc/stat дважды с интервалом.
     */
    fun usage(): Float {
        try {
            val first = readProcStat() ?: return 0f
            Thread.sleep(200)
            val second = readProcStat() ?: return 0f

            val totalDiff = second.total - first.total
            val idleDiff = second.idle - first.idle

            if (totalDiff <= 0) return 0f
            val usage = (totalDiff - idleDiff).toFloat() / totalDiff * 100f
            return usage.coerceIn(0f, 100f)
        } catch (e: Exception) {
            return 0f
        }
    }

    // ============================================================
    // /proc/stat
    // ============================================================

    private data class CpuStat(val total: Long, val idle: Long)

    private fun readProcStat(): CpuStat? {
        return try {
            val line = File("/proc/stat").readLines().firstOrNull { it.startsWith("cpu ") }
                ?: return null
            val parts = line.split("\\s+".toRegex()).filter { it.isNotEmpty() }
            if (parts.size < 5) return null

            // parts[0] = "cpu", далее user, nice, system, idle, iowait, irq, softirq, ...
            val user = parts[1].toLongOrNull() ?: 0
            val nice = parts[2].toLongOrNull() ?: 0
            val system = parts[3].toLongOrNull() ?: 0
            val idle = parts[4].toLongOrNull() ?: 0
            val iowait = parts.getOrNull(5)?.toLongOrNull() ?: 0
            val irq = parts.getOrNull(6)?.toLongOrNull() ?: 0
            val softirq = parts.getOrNull(7)?.toLongOrNull() ?: 0

            val total = user + nice + system + idle + iowait + irq + softirq
            CpuStat(total, idle)
        } catch (e: Exception) {
            null
        }
    }

    // ============================================================
    // Утилита чтения long из файла
    // ============================================================

    private fun readLong(path: String): Long {
        return try {
            val f = File(path)
            if (f.exists() && f.canRead()) {
                f.readText().trim().toLongOrNull() ?: 0L
            } else 0L
        } catch (e: Exception) {
            0L
        }
    }

    // ============================================================
    // Форматирование
    // ============================================================

    /**
     * Частота в кГц → строка "2.80 ГГц"
     */
    fun formatFreq(khz: Long): String {
        if (khz <= 0) return "N/A"
        val mhz = khz / 1000.0
        return if (mhz >= 1000) {
            String.format("%.2f ГГц", mhz / 1000)
        } else {
            String.format("%.0f МГц", mhz)
        }
    }
}
