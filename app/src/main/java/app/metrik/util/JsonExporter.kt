package app.metrik.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import app.metrik.bench.BenchResult
import app.metrik.bench.Scoring
import app.metrik.data.DeviceProfile
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Экспорт результатов бенчмарка в JSON-файл.
 */
object JsonExporter {

    fun buildJson(result: BenchResult, profile: DeviceProfile): String {
        val root = JSONObject()

        root.put("app", "Metrik")
        root.put("version", "1.5.1")
        root.put("exportedAt", System.currentTimeMillis())

        val device = JSONObject().apply {
            put("manufacturer", profile.manufacturer)
            put("model", profile.model)
            put("device", profile.device)
            put("product", profile.product)
            put("brand", profile.brand)
            put("androidVersion", profile.androidVersion)
            put("apiLevel", profile.apiLevel)
            put("buildId", profile.buildId)
            put("kernelVersion", profile.kernelVersion)
            put("uptime", profile.uptime)
            put("locale", profile.locale)
            put("timezone", profile.timezone)
        }
        root.put("device", device)

        val cpu = JSONObject().apply {
            put("soc", profile.soc)
            put("abi", profile.cpuAbi)
            put("cores", profile.cpuCores)
            put("minFreqKhz", profile.cpuMinFreq)
            put("maxFreqKhz", profile.cpuMaxFreq)
            put("currentFreqKhz", profile.cpuCurrentFreq)
            put("tempC", profile.cpuTemp.toDouble())
        }
        root.put("cpu", cpu)

        val ram = JSONObject().apply {
            put("totalBytes", profile.ram.total)
            put("availableBytes", profile.ram.available)
            put("usedBytes", profile.ram.used)
            put("isLowRam", profile.ram.isLowRam)
        }
        root.put("ram", ram)

        val storage = JSONObject().apply {
            put("totalBytes", profile.storage.total)
            put("freeBytes", profile.storage.free)
            put("usedBytes", profile.storage.used)
        }
        root.put("storage", storage)

        val battery = JSONObject().apply {
            put("level", profile.battery.level)
            put("voltageMv", profile.battery.voltage)
            put("tempC", profile.battery.temp.toDouble())
            put("currentMa", profile.battery.current)
            put("health", profile.battery.health)
            put("technology", profile.battery.technology)
            put("status", profile.battery.status)
            put("isCharging", profile.battery.isCharging)
        }
        root.put("battery", battery)

        val display = JSONObject().apply {
            put("width", profile.display.width)
            put("height", profile.display.height)
            put("densityDpi", profile.display.densityDpi)
            put("diagonalInches", profile.display.diagonal.toDouble())
            put("refreshRateHz", profile.display.refreshRate.toDouble())
        }
        root.put("display", display)

        val gpu = JSONObject().apply {
            put("renderer", profile.gpuRenderer)
            put("vendor", profile.gpuVendor)
            put("version", profile.gpuVersion)
        }
        root.put("gpu", gpu)

        val bench = JSONObject().apply {
            put("aesSingleMbPerSec", result.aesSingleMbPerSec)
            put("aesMultiMbPerSec", result.aesMultiMbPerSec)
            put("storageWriteMbPerSec", result.storageWriteMbPerSec)
            put("storageReadMbPerSec", result.storageReadMbPerSec)
            put("storageRandomMbPerSec", result.storageRandomMbPerSec)
            put("throttlingPercent", result.throttlingPercent.toDouble())
            put("cpuScore", result.cpuScore)
            put("storageScore", result.storageScore)
            put("batteryScore", result.batteryScore)
            put("gpuScore", result.gpuScore)
            put("totalScore", result.totalScore)
            put("maxTotal", Scoring.MAX_TOTAL)
            put("rating", result.rating)
            put("timestamp", result.timestamp)
        }
        root.put("benchmark", bench)

        return root.toString(2)
    }

    fun saveToFile(context: Context, json: String): File {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        val fileName = "metrik_${dateFormat.format(Date())}.json"
        val dir = File(context.cacheDir, "exports").apply { mkdirs() }
        val file = File(dir, fileName)
        file.writeText(json)
        return file
    }

    fun share(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Metrik Benchmark Result")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, "Share Metrik Result").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    fun exportAndShare(context: Context, result: BenchResult, profile: DeviceProfile) {
        val json = buildJson(result, profile)
        val file = saveToFile(context, json)
        share(context, file)
    }

    fun buildTextSummary(result: BenchResult): String {
        val total = Scoring.formatScore(result.totalScore)
        val max = Scoring.formatScore(Scoring.MAX_TOTAL)
        return buildString {
            appendLine("Metrik Benchmark Result")
            appendLine("━━━━━━━━━━━━━━━━━━━━━")
            appendLine("Устройство: ${result.deviceName}")
            appendLine("Балл: $total / $max")
            appendLine("Рейтинг: ${result.rating}")
            appendLine()
            appendLine("CPU:")
            appendLine("  AES single: ${"%.1f".format(result.aesSingleMbPerSec)} МБ/с")
            appendLine("  AES multi:  ${"%.1f".format(result.aesMultiMbPerSec)} МБ/с")
            appendLine("Storage:")
            appendLine("  Write:      ${"%.0f".format(result.storageWriteMbPerSec)} МБ/с")
            appendLine("  Read:       ${"%.0f".format(result.storageReadMbPerSec)} МБ/с")
            appendLine("  Random 4K:  ${"%.0f".format(result.storageRandomMbPerSec)} МБ/с")
            appendLine("Battery:")
            appendLine("  Throttling: ${"%.1f".format(result.throttlingPercent)}%")
            appendLine()
            append("via Metrik")
        }
    }
}
