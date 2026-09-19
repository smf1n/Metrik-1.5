package app.metrik.bench

import android.util.Log
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
 *
 * ФИКСЫ v1.5.1:
 * - шифруем маленькими блоками (16 КБ) — реалистичнее и не упирается в лимиты провайдера
 * - Cipher создаётся ОДИН РАЗ на тест
 * - ошибки логируются, а не тихо глотаются
 * - буфер переиспользуется
 */
object AesBench {

    private const val TAG = "MetrikAesBench"

    // Ровно 32 байта = AES-256
    private const val KEY = "MetrikBench2025SecretKey32Bytes"

    private const val CHUNK_SIZE = 16 * 1024       // 16 КБ
    private const val DURATION_MS = 3000L          // 3 секунды на тест

    suspend fun runSingleCore(
        onProgress: (Int) -> Unit = {}
    ): Double = withContext(Dispatchers.Default) {
        runAes(durationMs = DURATION_MS, onProgress = onProgress)
    }

    suspend fun runMultiCore(
        onProgress: (Int) -> Unit = {}
    ): Double = withContext(Dispatchers.Default) {

        val cores = Runtime.getRuntime().availableProcessors()
        val startTime = System.currentTimeMillis()
        val endTime = startTime + DURATION_MS

        val jobs = (0 until cores).map { _ ->
            async(Dispatchers.Default) {
                var bytesProcessed = 0L
                val cipher = createCipher()
                val buffer = ByteArray(CHUNK_SIZE) { (it and 0xFF).toByte() }
                if (cipher != null) {
                    while (System.currentTimeMillis() < endTime) {
                        bytesProcessed += encryptChunk(cipher, buffer)
                    }
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

    private fun runAes(
        durationMs: Long,
        onProgress: (Int) -> Unit
    ): Double {
        val startTime = System.currentTimeMillis()
        val endTime = startTime + durationMs
        var bytesProcessed = 0L

        val cipher = createCipher()
        val buffer = ByteArray(CHUNK_SIZE) { (it and 0xFF).toByte() }

        if (cipher == null) {
            Log.e(TAG, "AES cipher is null — шифрование недоступно")
            onProgress(100)
            return 0.0
        }

        while (System.currentTimeMillis() < endTime) {
            bytesProcessed += encryptChunk(cipher, buffer)

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

    /**
     * AES/ECB/NoPadding — буфер кратен 16 байтам (16 КБ = 1024 блока).
     * NoPadding быстрее и не кидает исключений на неверных размерах.
     */
    private fun createCipher(): Cipher? {
        return try {
            val keySpec = SecretKeySpec(KEY.toByteArray(Charsets.US_ASCII), "AES")
            val c = Cipher.getInstance("AES/ECB/NoPadding")
            c.init(Cipher.ENCRYPT_MODE, keySpec)
            c
        } catch (e: Exception) {
            Log.e(TAG, "Не удалось создать AES cipher: ${e.message}", e)
            null
        }
    }

    private fun encryptChunk(cipher: Cipher, buffer: ByteArray): Long {
        return try {
            val out = cipher.doFinal(buffer)
            out.size.toLong()
        } catch (e: Exception) {
            Log.e(TAG, "Ошибка шифрования блока: ${e.message}", e)
            0L
        }
    }

    fun formatSpeed(mbPerSec: Double): String {
        return String.format(java.util.Locale.US, "%.1f МБ/с", mbPerSec)
    }
}
