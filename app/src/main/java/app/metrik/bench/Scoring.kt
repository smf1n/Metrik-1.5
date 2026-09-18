package app.metrik.bench

/**
 * Система баллов Metrik.
 *
 * НЕ привязана к конкретному устройству.
 * Использует физические константы — балл показывает реальную производительность.
 *
 * Максимум: 7000 баллов.
 *
 * Каждая категория: 0-1000 баллов.
 * Балл = (Реальное значение / Константа) × 1000, ограничено 1000.
 */
object Scoring {

    // ============================================================
    // ФИЗИЧЕСКИЕ КОНСТАНТЫ (не привязаны к телефону)
    // ============================================================

    // AES шифрование
    const val CONST_AES_SINGLE = 1000.0     // МБ/с → 1000 баллов
    const val CONST_AES_MULTI = 4000.0      // МБ/с → 1000 баллов

    // Storage
    const val CONST_ST_WRITE = 1500.0       // МБ/с → 1000 баллов
    const val CONST_ST_READ = 2500.0        // МБ/с → 1000 баллов
    const val CONST_ST_RANDOM = 500.0       // МБ/с → 1000 баллов

    // Троттлинг: 0% = 1000 баллов, -50% и хуже = 0
    const val CONST_THROTTLING_WORST = -50f

    // ============================================================
    // МАКСИМУМЫ
    // ============================================================

    const val MAX_PER_TEST = 1000
    const val MAX_TOTAL = 7000

    // ============================================================
    // ПОДСЧЁТ
    // ============================================================

    fun calculate(
        aesSingleMbPerSec: Double,
        aesMultiMbPerSec: Double,
        storageWriteMbPerSec: Double,
        storageReadMbPerSec: Double,
        storageRandomMbPerSec: Double,
        throttlingPercent: Float
    ): BenchResult {

        val aesSingleScore = scoreOf(aesSingleMbPerSec, CONST_AES_SINGLE)
        val aesMultiScore = scoreOf(aesMultiMbPerSec, CONST_AES_MULTI)
        val stWriteScore = scoreOf(storageWriteMbPerSec, CONST_ST_WRITE)
        val stReadScore = scoreOf(storageReadMbPerSec, CONST_ST_READ)
        val stRandomScore = scoreOf(storageRandomMbPerSec, CONST_ST_RANDOM)
        val throttleScore = throttleScore(throttlingPercent)

        val total = (
            aesSingleScore +
            aesMultiScore +
            stWriteScore +
            stReadScore +
            stRandomScore +
            throttleScore
        ).coerceIn(0, MAX_TOTAL)

        // Группируем в категории для удобства
        val cpuScore = (aesSingleScore + aesMultiScore).coerceAtMost(2000)
        val storageScore = (stWriteScore + stReadScore + stRandomScore).coerceAtMost(3000)
        val batteryScore = throttleScore.coerceAtMost(1000)
        val gpuScore = 0  // GPU пока не тестируем

        return BenchResult(
            aesSingleMbPerSec = aesSingleMbPerSec,
            aesMultiMbPerSec = aesMultiMbPerSec,
            storageWriteMbPerSec = storageWriteMbPerSec,
            storageReadMbPerSec = storageReadMbPerSec,
            storageRandomMbPerSec = storageRandomMbPerSec,
            throttlingPercent = throttlingPercent,
            cpuScore = cpuScore,
            storageScore = storageScore,
            batteryScore = batteryScore,
            gpuScore = gpuScore,
            totalScore = total,
            rating = rating(total)
        )
    }

    // ============================================================
    // ФОРМУЛЫ
    // ============================================================

    /**
     * Балл от 0 до 1000 по значению и константе.
     * Если значение >= константы — 1000.
     */
    private fun scoreOf(value: Double, constant: Double): Int {
        if (constant <= 0 || value <= 0) return 0
        val ratio = value / constant
        return (ratio * MAX_PER_TEST).toInt().coerceIn(0, MAX_PER_TEST)
    }

    /**
     * Балл за троттлинг.
     * 0% (нет троттлинга) = 1000.
     * -50% и хуже = 0.
     */
    private fun throttleScore(throttling: Float): Int {
        if (throttling >= 0f) return MAX_PER_TEST
        if (throttling <= CONST_THROTTLING_WORST) return 0
        val ratio = 1f - (throttling / CONST_THROTTLING_WORST)
        return (ratio * MAX_PER_TEST).toInt().coerceIn(0, MAX_PER_TEST)
    }

    // ============================================================
    // РЕЙТИНГ
    // ============================================================

    fun rating(totalScore: Int): String {
        return when {
            totalScore >= 6000 -> "⭐ Excellent"
            totalScore >= 4500 -> "✅ Very Good"
            totalScore >= 3000 -> "👍 Good"
            totalScore >= 1800 -> "🟡 Average"
            totalScore >= 800 -> "🟠 Low"
            else -> "🔴 Very Low"
        }
    }

    // ============================================================
    // ТРОТТЛИНГ
    // ============================================================

    fun calculateThrottling(startAes: Double, endAes: Double): Float {
        if (startAes <= 0) return 0f
        return ((endAes - startAes) / startAes * 100).toFloat()
    }

    // ============================================================
    // ОЦЕНКА ПО КАТЕГОРИИ (эмодзи)
    // ============================================================

    fun categoryEmoji(score: Int, max: Int): String {
        if (max <= 0) return ""
        val ratio = score.toFloat() / max
        return when {
            ratio >= 0.9f -> "🏆"
            ratio >= 0.75f -> "⭐"
            ratio >= 0.6f -> "✅"
            ratio >= 0.4f -> "🟡"
            ratio >= 0.2f -> "🟠"
            else -> "🔴"
        }
    }
}
