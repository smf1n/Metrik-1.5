package app.metrik.data

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.Display
import android.view.WindowManager
import java.util.Locale

/**
 * Читает информацию о дисплее:
 * разрешение, плотность, частота обновления, диагональ.
 */
object DisplayReader {

    /**
     * Снимок текущего состояния дисплея.
     */
    fun snapshot(context: Context): DisplayInfo {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()

        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)

        val widthPx = metrics.widthPixels
        val heightPx = metrics.heightPixels
        val densityDpi = metrics.densityDpi
        val density = metrics.density

        val refreshRate = currentRefreshRate(context)

        // Диагональ в дюймах
        val widthIn = widthPx / densityDpi.toFloat()
        val heightIn = heightPx / densityDpi.toFloat()
        val diagonal = Math.sqrt(
            (widthIn * widthIn + heightIn * heightIn).toDouble()
        ).toFloat()

        return DisplayInfo(
            width = widthPx,
            height = heightPx,
            density = density,
            densityDpi = densityDpi,
            refreshRate = refreshRate,
            diagonal = diagonal
        )
    }

    /**
     * Текущая частота обновления (Гц).
     */
    private fun currentRefreshRate(context: Context): Float {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val dm = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
                val display = dm.getDisplay(Display.DEFAULT_DISPLAY)
                display?.refreshRate ?: 60f
            } else {
                @Suppress("DEPRECATION")
                val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
                @Suppress("DEPRECATION")
                wm.defaultDisplay.refreshRate
            }
        } catch (e: Exception) {
            60f
        }
    }

    /**
     * Форматирование диагонали: "6.1\""
     */
    fun formatDiagonal(inches: Float): String {
        return String.format(Locale.US, "%.1f\"", inches)
    }

    /**
     * Плотность как строка: "xxhdpi (420 dpi)"
     */
    fun formatDensity(dpi: Int): String {
        val bucket = when {
            dpi < 140 -> "ldpi"
            dpi < 200 -> "mdpi"
            dpi < 280 -> "hdpi"
            dpi < 400 -> "xhdpi"
            dpi < 560 -> "xxhdpi"
            else -> "xxxhdpi"
        }
        return "$bucket ($dpi dpi)"
    }

    /**
     * Частота обновления: "120 Гц"
     */
    fun formatRefresh(rate: Float): String {
        return String.format(Locale.US, "%.0f Гц", rate)
    }

    /**
     * Разрешение: "1080 × 2340"
     */
    fun formatResolution(width: Int, height: Int): String {
        return "$width × $height"
    }
}
