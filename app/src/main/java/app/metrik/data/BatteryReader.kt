package app.metrik.data

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

/**
 * Читает информацию о батарее в реальном времени.
 * Используется для мониторинга во время тестов.
 */
object BatteryReader {

    /**
     * Разовый снимок состояния батареи.
     */
    fun snapshot(context: Context): BatteryInfo {
        val intent = context.registerReceiver(
            null,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )

        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val percent = if (level >= 0 && scale > 0) (level * 100 / scale) else 0

        val voltage = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) ?: 0
        val tempRaw = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0
        val temp = tempRaw / 10f

        val healthInt = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, 0) ?: 0
        val health = when (healthInt) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over voltage"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unspecified failure"
            BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
            else -> "Unknown"
        }

        val tech = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: ""

        val statusInt = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, 0) ?: 0
        val status = when (statusInt) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
            BatteryManager.BATTERY_STATUS_FULL -> "Full"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not charging"
            else -> "Unknown"
        }

        val current = readCurrent(context)

        return BatteryInfo(
            level = percent,
            voltage = voltage,
            temp = temp,
            current = current,
            health = health,
            technology = tech,
            status = status,
            isCharging = statusInt == BatteryManager.BATTERY_STATUS_CHARGING ||
                         statusInt == BatteryManager.BATTERY_STATUS_FULL
        )
    }

    /**
     * Текущий ток (мА). Отрицательный — разряд, положительный — заряд.
     * Может вернуть 0, если устройство не поддерживает.
     */
    private fun readCurrent(context: Context): Int {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val value = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
            // Некоторые вендоры возвращают в мкА
            if (value in -100000..100000 && value != 0) {
                if (value > 1000 || value < -1000) value / 1000 else value
            } else {
                value
            }
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Температура батареи в °C.
     */
    fun temperature(context: Context): Float = snapshot(context).temp

    /**
     * Уровень заряда в %.
     */
    fun level(context: Context): Int = snapshot(context).level
}
