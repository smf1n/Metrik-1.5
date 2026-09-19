
package app.metrik.bench

/**
 * Система баллов Metrik.
 *
 * НЕ привязана к конкретному устройству.
 * Использует физические константы — балл показывает реальную производительность.
 *
 * Максимум: 7 000 000 баллов.
 *
 * Каждая категория: 0-1 000 000 баллов.
 * Балл = (Реальное значение / Константа) × 1 000 000, ограничено 1 000 000.
 *
 * Масштабирование ×1000 по сравнению с v1.5 — для солидности цифр.
 */
object Scoring {

    // ============================================================
    // ФИЗИЧЕСКИЕ КОНСТАНТЫ
    // ============================================================

    const val CONST_AES_SINGLE = 1000.0     // МБ/с → 1 000 000 баллов
    const val CONST_AES_MULTI = 4000.0      // МБ/с → 1 000 000 баллов

    const val CONST_ST_WRITE = 1500.0       // МБ/с → 1 000 000 баллов
    const val CONST_ST_READ = 2500.0        // МБ/с → 1 000 000 баллов
    const val CONST_ST_RANDOM = 500.0       // МБ/с → 1 000 000 баллов

    const val CONST_THROTTLING_WORST = -50f

    // ============================================================
    // МАКСИМУМЫ
    // ============================================================

    const val MAX_PER_TEST = 1_000_000
    const val MAX_CPU = 2_000_000
    const val MAX_STORAGE = 3_000_000
    const val MAX_BATTERY = 1_000_000
    const val MAX_GPU = 1_000_000
    const val MAX_TOTAL = 7_000_000

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
            aesSingleScore.toLong() +
            aesMultiScore.toLong() +
            stWriteScore.toLong() +
            stReadScore.toLong() +
            stRandomScore.toLong() +
            throttleScore.toLong()
        ).coerceIn(0L, MAX_TOTAL.toLong())

        val cpuScore = (aesSingleScore + aesMultiScore).coerceAtMost(MAX_CPU)
        val storageScore = (stWriteScore + stReadScore + stRandomScore).coerceAtMost(MAX_STORAGE)
        val batteryScore = throttleScore.coerceAtMost(MAX_BATTERY)
        val gpuScore = 0

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

    private fun scoreOf(value: Double, constant: Double): Int {
        if (constant <= 0 || value <= 0) return 0
        val ratio = value / constant
        return (ratio * MAX_PER_TEST).toInt().coerceIn(0, MAX_PER_TEST)
    }

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
            totalScore >= 6_000_000 -> "EXCELLENT"
            totalScore >= 4_500_000 -> "VERY GOOD"
            totalScore >= 3_000_000 -> "GOOD"
            totalScore >= 1_800_000 -> "AVERAGE"
            totalScore >= 800_000 -> "LOW"
            else -> "VERY LOW"
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
    // УРОВЕНЬ КАТЕГОРИИ (без эмодзи)
    // ============================================================

    fun categoryLevel(score: Int, max: Int): String {
        if (max <= 0) return ""
        val ratio = score.toFloat() / max
        return when {
            ratio >= 0.9f -> "MAX"
            ratio >= 0.75f -> "HIGH"
            ratio >= 0.6f -> "MID"
            ratio >= 0.4f -> "OK"
            ratio >= 0.2f -> "LOW"
            else -> "BAD"
        }
    }

    // ============================================================
    // ФОРМАТИРОВАНИЕ
    // ============================================================

    /**
     * 3592000 → "3 592 000"
     */
    fun formatScore(score: Int): String {
        val s = score.toString()
        val sb = StringBuilder()
        for ((i, c) in s.withIndex()) {
            if (i > 0 && (s.length - i) % 3 == 0) sb.append(' ')
            sb.append(c)
        }
        return sb.toString()
    }
}
