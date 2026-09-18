package app.metrik.bench

/**
 * Итоговый результат бенчмарка Metrik.
 *
 * Содержит:
 * - реальные физические значения (МБ/с, °C, мА)
 * - нормализованный балл (S23 = 10 000)
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

    // ===== Баллы (нормализованные, S23 = 10 000) =====
    val cpuScore: Int = 0,        // макс 4000
    val storageScore: Int = 0,    // макс 3000
    val batteryScore: Int = 0,    // макс 2000
    val gpuScore: Int = 0,        // макс 1000
    val totalScore: Int = 0,      // макс 10000

    // ===== Рейтинг =====
    val rating: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val deviceName: String = "",
    val deviceModel: String = ""
) {

    /**
     * Троттлинг: падение производительности от нагрева.
     * Считается как (AES single в конце теста) / (AES single в начале).
     */
    fun hasThrottling(): Boolean = throttlingPercent < -3f

    fun summary(): String {
        return buildString {
            append("$totalScore / 10000")
            if (rating.isNotEmpty()) append(" · $rating")
        }
    }
}
