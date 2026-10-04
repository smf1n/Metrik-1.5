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

        val total: Long = (
            aesSingleScore.toLong() +
            aesMultiScore.toLong() +
            stWriteScore.toLong() +
            stReadScore.toLong() +
            stRandomScore.toLong() +
            throttleScore.toLong()
        ).coerceIn(0L, MAX_TOTAL.toLong())

        val cpuScore: Int = (aesSingleScore.toLong() + aesMultiScore.toLong())
            .coerceAtMost(MAX_CPU.toLong()).toInt()
        val storageScore: Int = (stWriteScore.toLong() + stReadScore.toLong() + stRandomScore.toLong())
            .coerceAtMost(MAX_STORAGE.toLong()).toInt()
        val batteryScore: Int = throttleScore.toLong()
            .coerceAtMost(MAX_BATTERY.toLong()).toInt()
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
            totalScore = total.toInt(),
            rating = rating(total.toInt())
        )
    }
