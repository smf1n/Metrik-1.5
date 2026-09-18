package app.metrik.data

import android.opengl.GLES20

/**
 * Читает информацию о GPU через OpenGL.
 * Все методы должны вызываться из потока с активным GL-контекстом,
 * либо используем "безопасную заглушку" при первом запуске.
 *
 * Простой путь: этот класс хранит значения, полученные из Activity
 * (после создания GLSurfaceView или при первом запросе).
 */
object GpuReader {

    // Кэшируем, чтобы не дёргать GL много раз
    private var cachedRenderer: String = ""
    private var cachedVendor: String = ""
    private var cachedVersion: String = ""
    private var cachedGlsl: String = ""

    /**
     * Обновляет информацию о GPU.
     * Вызывать из потока с GL-контекстом (например, из GLSurfaceView.Renderer.onSurfaceCreated).
     */
    fun updateFromGl() {
        try {
            cachedRenderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: ""
            cachedVendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: ""
            cachedVersion = GLES20.glGetString(GLES20.GL_VERSION) ?: ""
            cachedGlsl = GLES20.glGetString(GLES20.GL_SHADING_LANGUAGE_VERSION) ?: ""
        } catch (e: Exception) {
            // GL-контекста нет — оставляем пустые значения
        }
    }

    fun renderer(): String = cachedRenderer.ifEmpty { "Unknown" }
    fun vendor(): String = cachedVendor.ifEmpty { "Unknown" }
    fun version(): String = cachedVersion.ifEmpty { "Unknown" }
    fun glslVersion(): String = cachedGlsl.ifEmpty { "Unknown" }

    /**
     * Парсит вендора из строки рендерера.
     * "Adreno (TM) 740" → "Qualcomm (Adreno)"
     * "Mali-G78 MP14" → "ARM (Mali)"
     * "PowerVR ..." → "Imagination (PowerVR)"
     * "Apple A17 GPU" → "Apple"
     */
    fun vendorFromRenderer(): String {
        val r = renderer().lowercase()
        return when {
            r.contains("adreno") -> "Qualcomm"
            r.contains("mali") -> "ARM"
            r.contains("powervr") -> "Imagination"
            r.contains("apple") -> "Apple"
            r.contains("intel") -> "Intel"
            r.contains("nvidia") -> "NVIDIA"
            r.contains("vivante") -> "Vivante"
            r.contains("videocore") -> "Broadcom"
            else -> vendor()
        }
    }

    /**
     * Короткое имя GPU: "Adreno 740"
     */
    fun shortName(): String {
        val r = renderer()
        // Убираем "(TM)" и лишние пробелы
        return r.replace("(TM)", "")
            .replace("(R)", "")
            .replace("\\s+".toRegex(), " ")
            .trim()
    }
}
