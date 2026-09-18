package app.metrik.bench

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.util.Locale

/**
 * Storage бенчмарк.
 * Пишет и читает реальный файл 256 МБ в cacheDir.
 *
 * Sequential write: пишем большими блоками.
 * Sequential read: читаем большими блоками.
 * Random 4K read: читаем случайные блоки по 4 КБ.
 *
 * Возвращает: МБ/с для каждой операции.
 */
object StorageBench {

    private const val FILE_SIZE_MB = 256
    private const val BLOCK_SIZE = 1024 * 1024         // 1 МБ
    private const val RANDOM_BLOCK = 4 * 1024          // 4 КБ
    private const val FILE_NAME = "metrik_bench_test.bin"

    data class StorageResult(
        val writeMbPerSec: Double,
        val readMbPerSec: Double,
        val randomReadMbPerSec: Double
    )

    /**
     * Полный тест Storage: запись + чтение + random.
     */
    suspend fun run(
        context: Context,
        onProgress: (Int) -> Unit = {}
    ): StorageResult = withContext(Dispatchers.IO) {

        val file = File(context.cacheDir, FILE_NAME)
        val buffer = ByteArray(BLOCK_SIZE) { (it and 0xFF).toByte() }

        // === ЗАПИСЬ (0-40%) ===
        onProgress(0)
        val writeStart = System.currentTimeMillis()
        FileOutputStream(file).use { fos ->
            for (i in 0 until FILE_SIZE_MB) {
                fos.write(buffer)
                val p = ((i + 1) * 40 / FILE_SIZE_MB)
                onProgress(p)
            }
            fos.flush()
            fos.fd.sync()   // сброс на диск
        }
        val writeMs = (System.currentTimeMillis() - writeStart).coerceAtLeast(1)
        val writeMbPerSec = FILE_SIZE_MB.toDouble() / (writeMs / 1000.0)

        // === ЧТЕНИЕ (40-80%) ===
        onProgress(40)
        val readStart = System.currentTimeMillis()
        RandomAccessFile(file, "r").use { raf ->
            val buf = ByteArray(BLOCK_SIZE)
            for (i in 0 until FILE_SIZE_MB) {
                raf.readFully(buf)
                val p = 40 + ((i + 1) * 40 / FILE_SIZE_MB)
                onProgress(p)
            }
        }
        val readMs = (System.currentTimeMillis() - readStart).coerceAtLeast(1)
        val readMbPerSec = FILE_SIZE_MB.toDouble() / (readMs / 1000.0)

        // === RANDOM 4K READ (80-100%) ===
        onProgress(80)
        val randomStart = System.currentTimeMillis()
        val randomCount = 10_000  // 10 000 случайных чтений по 4 КБ = 40 МБ
        RandomAccessFile(file, "r").use { raf ->
            val buf = ByteArray(RANDOM_BLOCK)
            val fileSize = raf.length()
            val maxOffset = fileSize - RANDOM_BLOCK
            for (i in 0 until randomCount) {
                val offset = (Math.random() * maxOffset).toLong()
                raf.seek(offset)
                raf.readFully(buf)
            }
        }
        val randomMs = (System.currentTimeMillis() - randomStart).coerceAtLeast(1)
        val randomBytes = randomCount.toLong() * RANDOM_BLOCK
        val randomMbPerSec = (randomBytes / (1024.0 * 1024.0)) / (randomMs / 1000.0)

        // Удаляем файл
        file.delete()
        onProgress(100)

        StorageResult(
            writeMbPerSec = writeMbPerSec,
            readMbPerSec = readMbPerSec,
            randomReadMbPerSec = randomMbPerSec
        )
    }

    // ============================================================
    // Форматирование
    // ============================================================

    fun formatSpeed(mbPerSec: Double): String {
        return when {
            mbPerSec >= 1000 -> String.format(Locale.US, "%.2f ГБ/с", mbPerSec / 1024)
            else -> String.format(Locale.US, "%.0f МБ/с", mbPerSec)
        }
    }
}
