package app.metrik.data

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.WindowManager
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Собирает всю информацию об устройстве.
 * Работает на любом Android 8+ (API 26+).
 */
object DeviceScanner {

    fun scan(context: Context): DeviceProfile {
        return DeviceProfile(
            // Общие
            manufacturer = Build.MANUFACTURER ?: "",
            model = Build.MODEL ?: "",
            device = Build.DEVICE ?: "",
            product = Build.PRODUCT ?: "",
            brand = Build.BRAND ?: "",
            androidVersion = Build.VERSION.RELEASE ?: "",
            apiLevel = Build.VERSION.SDK_INT,
            buildId = Build.DISPLAY ?: "",
            kernelVersion = System.getProperty("os.version") ?: "",
            uptime = formatUptime(SystemClock.elapsedRealtime()),

            // CPU
            soc = readSoc(),
            cpuAbi = Build.SUPPORTED_ABIS.firstOrNull() ?: "",
            cpuCores = Runtime.getRuntime().availableProcessors(),
            cpuMinFreq = readCpuFreq("cpuinfo_min_freq"),
            cpuMaxFreq = readCpuFreq("cpuinfo_max_freq"),
            cpuCurrentFreq = readCurrentCpuFreq(),
            cpuTemp = readCpuTemp(),

            // GPU
            gpuRenderer = "",
            gpuVendor = "",
            gpuVersion = "",

            // RAM
            ram = readRam(context),

            // Storage
            storage = readStorage(),

            // Battery
            battery = readBattery(context),

            // Display
            display = readDisplay(context),

            // Прочее
            locale = Locale.getDefault().toString(),
            timezone = java.util.TimeZone.getDefault().id
        )
    }

    // ============================================================
    // CPU
    // ============================================================

    private fun readSoc(): String {
        // Пытаемся прочитать из /proc/cpuinfo
        try {
            val file = java.io.File("/proc/cpuinfo")
            if (file.exists()) {
                val hardware = file.readLines()
                    .firstOrNull { it.startsWith("Hardware") }
                    ?.substringAfter(":")?.trim()
                if (!hardware.isNullOrEmpty()) return hardware

                val model = file.readLines()
                    .firstOrNull { it.startsWith("model name") }
                    ?.substringAfter(":")?.trim()
                if (!model.isNullOrEmpty()) return model
            }
        } catch (_: Exception) {}

        // Fallback на Build
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL ?: Build.HARDWARE ?: ""
        } else {
            Build.HARDWARE ?: ""
        }
    }

    private fun readCpuFreq(file: String): Long {
        return try {
            val f = java.io.File("/sys/devices/system/cpu/cpu0/cpufreq/$file")
            if (f.exists()) f.readText().trim().toLongOrNull() ?: 0L else 0L
        } catch (_: Exception) { 0L }
    }

    private fun readCurrentCpuFreq(): Long {
        return try {
            val f = java.io.File("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")
            if (f.exists()) f.readText().trim().toLongOrNull() ?: 0L else 0L
        } catch (_: Exception) { 0L }
    }

    private fun readCpuTemp(): Float {
        // Пробуем разные пути (зависят от устройства)
        val paths = listOf(
            "/sys/class/thermal/thermal_zone0/temp",
            "/sys/devices/system/cpu/cpu0/thermal_zone0/temp",
            "/sys/devices/virtual/thermal/thermal_zone0/temp"
        )
        for (path in paths) {
            try {
                val f = java.io.File(path)
                if (f.exists()) {
                    val raw = f.readText().trim().toLongOrNull() ?: continue
                    // Обычно в миллиградусах
                    return if (raw > 1000) raw / 1000f else raw.toFloat()
                }
            } catch (_: Exception) {}
        }
        return 0f
    }

    // ============================================================
    // RAM
    // ============================================================

    private fun readRam(context: Context): RamInfo {
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val mi = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return RamInfo(
            total = mi.totalMem,
            available = mi.availMem,
            used = mi.totalMem - mi.availMem,
            threshold = mi.threshold,
            isLowRam = am.isLowRamDevice
        )
    }

    // ============================================================
    // Storage
    // ============================================================

    private fun readStorage(): StorageInfo {
        return try {
            val stat = StatFs(Environment.getDataDirectory().path)
            val total = stat.blockCountLong * stat.blockSizeLong
            val free = stat.availableBlocksLong * stat.blockSizeLong
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
    // Battery
    // ============================================================

    private fun readBattery(context: Context): BatteryInfo {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0

        val tempRaw = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val temp = tempRaw / 10f

        val healthInt = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0) ?: 0
        val health = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        val tech = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: ""

        val statusInt = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, 0) ?: 0
        val status = when (statusInt) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            else -> "Unknown"
        }

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val current = try {
            bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        } catch (e: Exception) { 0 }

        return BatteryInfo(
            level = percent,
            voltage = voltage,
            temp = temp,
            current = current,
            health = health,
            technology = tech,
            status = status,
            isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING ||
                         statusInt == BatteryManager.BATTERY_STATUS_FULL
        )
    }

    // ============================================================
    // Display
    // ============================================================

    private fun readDisplay(context: Context): DisplayInfo {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()

        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)

        val refreshRate = try {
            @Suppress("DEPRECATION")
            wm.defaultDisplay.refreshRate
        } catch (e: Exception) { 60f }

        val widthPx = metrics.widthPixels
        val heightPx = metrics.heightPixels
        val densityDpi = metrics.densityDpi
        val density = metrics.density

        // Диагональ в дюймах
        val widthIn = widthPx / densityDpi.toFloat()
        val heightIn = heightPx / densityDpi.toFloat()
        val diagonal = Math.sqrt(
            (widthIn * widthIn + heightIn * heightIn).toDouble()
        ).toFloat()

        return DisplayInfo(
            width = widthPx,
            height = heightPx,
            density = density,
            densityDpi = densityDpi,
            refreshRate = refreshRate,
            diagonal = diagonal
        )
    }

    // ============================================================
    // Утилиты
    // ============================================================

    private fun formatUptime(ms: Long): String {
        val days = TimeUnit.MILLISECONDS.toDays(ms)
        val hours = TimeUnit.MILLISECONDS.toHours(ms) % 24
        val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
        return buildString {
            if (days > 0) append("${days}д ")
            if (hours > 0 || days > 0) append("${hours}ч ")
            append("${minutes}м")
        }
    }
}

// ============================================================
// Вспомогательные data-классы
// ============================================================

data class RamInfo(
    val total: Long = 0,
    val available: Long = 0,
    val used: Long = 0,
    val threshold: Long = 0,
    val isLowRam: Boolean = false
)

data class StorageInfo(
    val total: Long = 0,
    val free: Long = 0,
    val used: Long = 0
)

data class BatteryInfo(
    val level: Int = 0,
    val voltage: Int = 0,
    val temp: Float = 0f,
    val current: Int = 0,
    val health: String = "",
    val technology: String = "",
    val status: String = "",
    val isCharging: Boolean = false
)

data class DisplayInfo(
    val width: Int = 0,
    val height: Int = 0,
    val density: Float = 0f,
    val densityDpi: Int = 0,
    val refreshRate: Float = 0f,
    val diagonal: Float = 0f
)
