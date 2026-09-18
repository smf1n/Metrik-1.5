package app.metrik.data

/**
 * Полный профиль устройства.
 * Заполняется DeviceScanner при старте приложения.
 * Все поля — реальные данные с устройства.
 */
data class DeviceProfile(
    // ===== Общие =====
    val manufacturer: String = "",
    val model: String = "",
    val device: String = "",
    val product: String = "",
    val brand: String = "",
    val androidVersion: String = "",
    val apiLevel: Int = 0,
    val buildId: String = "",
    val kernelVersion: String = "",
    val uptime: String = "",

    // ===== CPU =====
    val soc: String = "",
    val cpuAbi: String = "",
    val cpuCores: Int = 0,
    val cpuMinFreq: Long = 0,
    val cpuMaxFreq: Long = 0,
    val cpuCurrentFreq: Long = 0,
    val cpuTemp: Float = 0f,

    // ===== GPU =====
    val gpuRenderer: String = "",
    val gpuVendor: String = "",
    val gpuVersion: String = "",

    // ===== RAM =====
    val ramTotal: Long = 0,
    val ramAvailable: Long = 0,
    val ramUsed: Long = 0,
    val ramThreshold: Long = 0,
    val isLowRam: Boolean = false,

    // ===== Storage =====
    val storageTotal: Long = 0,
    val storageFree: Long = 0,
    val storageUsed: Long = 0,

    // ===== Battery =====
    val batteryLevel: Int = 0,
    val batteryVoltage: Int = 0,       // мВ
    val batteryTemp: Float = 0f,       // °C
    val batteryCurrent: Int = 0,       // мА (отрицательный = разряд)
    val batteryHealth: String = "",
    val batteryTechnology: String = "",
    val batteryStatus: String = "",
    val isCharging: Boolean = false,

    // ===== Display =====
    val screenWidth: Int = 0,
    val screenHeight: Int = 0,
    val screenDensity: Float = 0f,
    val screenDensityDpi: Int = 0,
    val screenRefreshRate: Float = 0f,
    val screenDiagonal: Float = 0f,

    // ===== Прочее =====
    val locale: String = "",
    val timezone: String = ""
) {
    /**
     * Краткая модель для отображения: "Samsung SM-S911B"
     */
    fun displayName(): String {
        return if (manufacturer.isNotEmpty() && model.isNotEmpty()) {
            "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
        } else {
            model
        }
    }

    /**
     * Заполнен ли профиль (сканирование завершено).
     */
    fun isComplete(): Boolean {
        return model.isNotEmpty() && cpuCores > 0
    }
}
