package app.metrik.data

/**
 * Полный профиль устройства.
 * Заполняется DeviceScanner при старте приложения.
 * Все поля — реальные данные с устройства.
 */
data class DeviceProfile(
    // Общие
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

    // CPU
    val soc: String = "",
    val cpuAbi: String = "",
    val cpuCores: Int = 0,
    val cpuMinFreq: Long = 0,
    val cpuMaxFreq: Long = 0,
    val cpuCurrentFreq: Long = 0,
    val cpuTemp: Float = 0f,

    // GPU
    val gpuRenderer: String = "",
    val gpuVendor: String = "",
    val gpuVersion: String = "",

    // Вложенные структуры
    val ram: RamInfo = RamInfo(),
    val storage: StorageInfo = StorageInfo(),
    val battery: BatteryInfo = BatteryInfo(),
    val display: DisplayInfo = DisplayInfo(),

    // Прочее
    val locale: String = "",
    val timezone: String = ""
) {
    fun displayName(): String {
        return if (manufacturer.isNotEmpty() && model.isNotEmpty()) {
            "${manufacturer.replaceFirstChar { it.uppercase() }} $model"
        } else {
            model
        }
    }

    fun isComplete(): Boolean = model.isNotEmpty() && cpuCores > 0
}
