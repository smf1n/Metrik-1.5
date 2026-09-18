package app.metrik.util

import android.content.Context
import app.metrik.bench.BenchResult
import org.json.JSONArray
import org.json.JSONObject

/**
 * Хранит историю замеров и настройки в SharedPreferences.
 * Максимум 20 последних замеров.
 */
object Preferences {

    private const val PREFS_NAME = "metrik_prefs"
    private const val KEY_HISTORY = "history"
    private const val MAX_HISTORY = 20

    // ============================================================
    // ИСТОРИЯ
    // ============================================================

    fun saveResult(context: Context, result: BenchResult) {
        val current = loadHistory(context).toMutableList()

        // Добавляем в начало
        current.add(0, result)

        // Обрезаем до MAX_HISTORY
        while (current.size > MAX_HISTORY) {
            current.removeAt(current.size - 1)
        }

        val array = JSONArray()
        current.forEach { array.put(resultToJson(it)) }

        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_HISTORY, array.toString())
            .apply()
    }

    fun loadHistory(context: Context): List<BenchResult> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_HISTORY, null) ?: return emptyList()

        return try {
            val array = JSONArray(raw)
            val list = mutableListOf<BenchResult>()
            for (i in 0 until array.length()) {
                list.add(jsonToResult(array.getJSONObject(i)))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clearHistory(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .remove(KEY_HISTORY)
            .apply()
    }

    // ============================================================
    // JSON
    // ============================================================

    private fun resultToJson(r: BenchResult): JSONObject {
        return JSONObject().apply {
            put("aesSingleMbPerSec", r.aesSingleMbPerSec)
            put("aesMultiMbPerSec", r.aesMultiMbPerSec)
            put("storageWriteMbPerSec", r.storageWriteMbPerSec)
            put("storageReadMbPerSec", r.storageReadMbPerSec)
            put("storageRandomMbPerSec", r.storageRandomMbPerSec)
            put("cpuTempStart", r.cpuTempStart.toDouble())
            put("cpuTempEnd", r.cpuTempEnd.toDouble())
            put("batteryTempStart", r.batteryTempStart.toDouble())
            put("batteryTempEnd", r.batteryTempEnd.toDouble())
            put("throttlingPercent", r.throttlingPercent.toDouble())
            put("cpuScore", r.cpuScore)
            put("storageScore", r.storageScore)
            put("batteryScore", r.batteryScore)
            put("gpuScore", r.gpuScore)
            put("totalScore", r.totalScore)
            put("rating", r.rating)
            put("timestamp", r.timestamp)
            put("deviceName", r.deviceName)
            put("deviceModel", r.deviceModel)
        }
    }

    private fun jsonToResult(o: JSONObject): BenchResult {
        return BenchResult(
            aesSingleMbPerSec = o.optDouble("aesSingleMbPerSec", 0.0),
            aesMultiMbPerSec = o.optDouble("aesMultiMbPerSec", 0.0),
            storageWriteMbPerSec = o.optDouble("storageWriteMbPerSec", 0.0),
            storageReadMbPerSec = o.optDouble("storageReadMbPerSec", 0.0),
            storageRandomMbPerSec = o.optDouble("storageRandomMbPerSec", 0.0),
            cpuTempStart = o.optDouble("cpuTempStart", 0.0).toFloat(),
            cpuTempEnd = o.optDouble("cpuTempEnd", 0.0).toFloat(),
            batteryTempStart = o.optDouble("batteryTempStart", 0.0).toFloat(),
            batteryTempEnd = o.optDouble("batteryTempEnd", 0.0).toFloat(),
            throttlingPercent = o.optDouble("throttlingPercent", 0.0).toFloat(),
            cpuScore = o.optInt("cpuScore", 0),
            storageScore = o.optInt("storageScore", 0),
            batteryScore = o.optInt("batteryScore", 0),
            gpuScore = o.optInt("gpuScore", 0),
            totalScore = o.optInt("totalScore", 0),
            rating = o.optString("rating", ""),
            timestamp = o.optLong("timestamp", System.currentTimeMillis()),
            deviceName = o.optString("deviceName", ""),
            deviceModel = o.optString("deviceModel", "")
        )
    }
}
