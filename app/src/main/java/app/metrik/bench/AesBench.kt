package app.metrik.bench

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * AES-256 бенчмарк.
 * Реальное шифрование данных — объективный тест CPU.
 * Используется javax.crypto (аппаратное ускорение AES на большинстве SoC).
 *
 * Single-core: одно ядро шифрует блок.
 * Multi-core: N ядер параллельно шифруют свои блоки.
 *
 * Результат: МБ/с (сколько мегабайт шифруется за секунду).
 */
object AesBench {

    private const val KEY = "MetrikBench2025SecretKey32Bytes" // 32 байта = AES-256
    private const val BLOCK_SIZE = 1024 * 1024  // 1 МБ блок
    private const val DURATION_MS = 3000L       // 3 секунды на тест

    /**
     * Single-core тест.
     * Возвращает: МБ/с
     */
    suspend fun runSingleCore(
        onProgress: (Int) -> Unit = {}
    ): Double = withContext(Dispatchers.Default) {
        runAes(durationMs = DURATION_MS, onProgress = onProgress)
    }

    /**
     * Multi-core тест.
     * Возвращает: МБ/с (суммарно по всем ядрам).
     */
    suspend fun runMultiCore(
        onProgress: (Int) -> Unit = {}
    ): Double = withContext(Dispatchers.Default) {

        val cores = Runtime.getRuntime().availableProcessors()
        val startTime = System.currentTimeMillis()
        val endTime = startTime + DURATION_MS

        val jobs = (0 until cores).map { coreId ->
            async(Dispatchers.Default) {
                var bytesProcessed = 0L
                while (System.currentTimeMillis() < endTime) {
                    bytesProcessed += encryptBlock()
                }
                bytesProcessed
            }
        }

        val progressJob = async {
            while (System.currentTimeMillis() < endTime) {
                val p = ((System.currentTimeMillis() - startTime) * 100 / DURATION_MS).toInt()
                onProgress(p.coerceIn(0, 100))
                delay(100)
            }
        }

        val totalBytes = jobs.awaitAll().sum()
        progressJob.await()

        val elapsedSec = (System.currentTimeMillis() - startTime).coerceAtLeast(1) / 1000.0
        (totalBytes / (1024.0 * 1024.0)) / elapsedSec
    }

    // ============================================================
    // Ядро теста
    // ============================================================

    private fun runAes(
        durationMs: Long,
        onProgress: (Int) -> Unit
    ): Double {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + durationMs
        var bytesProcessed = 0L

        while (System.currentTimeMillis() < endTime) {
            bytesProcessed += encryptBlock()

            val elapsed = System.currentTimeMillis() - startTime
            if (elapsed % 200 < 50) {
                val p = (elapsed * 100 / durationMs).toInt()
                onProgress(p.coerceIn(0, 100))
            }
        }

        onProgress(100)
        val elapsedSec = (System.currentTimeMillis() - startTime).coerceAtLeast(1) / 1000.0
        return (bytesProcessed / (1024.0 * 1024.0)) / elapsedSec
    }

    private fun encryptBlock(): Long {
        return try {
            val data = ByteArray(BLOCK_SIZE) { (it and 0xFF).toByte() }
            val keySpec = SecretKeySpec(KEY.toByteArray(), "AES")
            val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipher.init(Cipher.ENCRYPT_MODE, keySpec)
            val encrypted = cipher.doFinal(data)
            encrypted.size.toLong()
        } catch (e: Exception) {
            0L
        }
    }

    // ============================================================
    // Форматирование
    // ============================================================

    fun formatSpeed(mbPerSec: Double): String {
        return String.format(java.util.Locale.US, "%.1f МБ/с", mbPerSec)
    }
}
