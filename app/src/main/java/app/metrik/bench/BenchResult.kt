package app.metrik.bench

/**
 * Итоговый результат бенчмарка Metrik.
 *
 * Содержит:
 * - реальные физические значения (МБ/с, °C, мА)
 * - нормализованный балл (макс 7 000 000)
 * - разбивку по категориям
 */
data class BenchResult(
    // ===== Реальные измерения =====
    val aesSingleMbPerSec: Double = 0.0,
    val aesMultiMbPerSec: Double = 0.0,
    val storageWriteMbPerSec: Double = 0.0,
    val storageReadMbPerSec: Double = 0.0,
    val storageRandomMbPerSec: Double = 0.0,
    val cpuTempStart: Float = 0f,
    val cpuTempEnd: Float = 0f,
    val batteryTempStart: Float = 0f,
    val batteryTempEnd: Float = 0f,
    val throttlingPercent: Float = 0f,
    val durationMs: Long = 0,

    // ===== Баллы =====
    val cpuScore: Int = 0,        // макс 2 000 000
    val storageScore: Int = 0,    // макс 3 000 000
    val batteryScore: Int = 0,    // макс 1 000 000
    val gpuScore: Int = 0,        // макс 1 000 000
    val totalScore: Int = 0,      // макс 7 000 000

    // ===== Рейтинг =====
    val rating: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val deviceName: String = "",
    val deviceModel: String = ""
) {

    fun hasThrottling(): Boolean = throttlingPercent < -3f

    fun summary(): String {
        return buildString {
            append("${Scoring.formatScore(totalScore)} / ${Scoring.formatScore(Scoring.MAX_TOTAL)}")
            if (rating.isNotEmpty()) append(" · $rating")
        }
    }
}
