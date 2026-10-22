package com.example.areacalc

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import java.util.Locale
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Overlay drawn on top of the camera preview showing:
 *  - a 3x3 rule-of-thirds grid, and
 *  - a gyroscope-driven tilt / level indicator (a horizon line that rotates
 *    with the device roll, plus a centre reticle and an angle read-out).
 */
class TiltOverlayView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val density = resources.displayMetrics.density

    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(80, 255, 255, 255)
        strokeWidth = 1f * density
        style = Paint.Style.STROKE
    }

    private val referencePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(70, 255, 255, 255)
        strokeWidth = 1f * density
        style = Paint.Style.STROKE
    }

    private val horizonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2.5f * density
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val reticlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 2f * density
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 12f * density
        textAlign = Paint.Align.CENTER
    }

    private val textBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.argb(150, 0, 0, 0)
        style = Paint.Style.FILL
    }

    private var roll = 0f
    private var pitch = 0f
    private var hasData = false

    /** Called from the sensor callback with the current device tilt in degrees. */
    fun updateTilt(newRoll: Float, newPitch: Float) {
        if (hasData && abs(newRoll - roll) < 0.1f && abs(newPitch - pitch) < 0.1f) return
        roll = newRoll
        pitch = newPitch
        hasData = true
        invalidate()
    }

    /** Hides the tilt indicator (e.g. when the screen is paused). */
    fun clear() {
        hasData = false
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        drawGrid(canvas, w, h)
        drawLevel(canvas, w, h)
        drawReadout(canvas, w, h)
    }

    private fun drawGrid(canvas: Canvas, w: Float, h: Float) {
        val x1 = w / 3f
        val x2 = w * 2f / 3f
        val y1 = h / 3f
        val y2 = h * 2f / 3f
        canvas.drawLine(x1, 0f, x1, h, gridPaint)
        canvas.drawLine(x2, 0f, x2, h, gridPaint)
        canvas.drawLine(0f, y1, w, y1, gridPaint)
        canvas.drawLine(0f, y2, w, y2, gridPaint)
    }

    private fun drawLevel(canvas: Canvas, w: Float, h: Float) {
        val cx = w / 2f
        val cy = h / 2f
        val isLevel = hasData && abs(roll) <= LEVEL_TOLERANCE && abs(pitch) <= LEVEL_TOLERANCE
        val color = if (isLevel) LEVEL_COLOR else TILT_COLOR
        horizonPaint.color = color
        reticlePaint.color = color

        val half = min(w, h) * 0.4f

        // Fixed reference line: where a perfectly level horizon would sit.
        canvas.drawLine(cx - half, cy, cx + half, cy, referencePaint)

        // Horizon line that follows the gyroscope roll.
        val rad = Math.toRadians(roll.toDouble())
        val dx = (cos(rad) * half).toFloat()
        val dy = (sin(rad) * half).toFloat()
        canvas.drawLine(cx - dx, cy - dy, cx + dx, cy + dy, horizonPaint)

        // Centre reticle.
        val r = min(w, h) * 0.045f
        canvas.drawRect(cx - r, cy - r, cx + r, cy + r, reticlePaint)
        canvas.drawLine(cx, cy - r * 1.9f, cx, cy - r * 1.15f, reticlePaint)
        canvas.drawLine(cx, cy + r * 1.15f, cx, cy + r * 1.9f, reticlePaint)
    }

    private fun drawReadout(canvas: Canvas, w: Float, h: Float) {
        if (!hasData) return
        val isLevel = abs(roll) <= LEVEL_TOLERANCE && abs(pitch) <= LEVEL_TOLERANCE
        val label = if (isLevel) {
            "LEVEL"
        } else {
            String.format(Locale.US, "Tilt %.0f\u00b0 \u00b7 Pitch %.0f\u00b0", roll, pitch)
        }
        textPaint.color = if (isLevel) LEVEL_COLOR else Color.WHITE
        val textY = h - 16f * density
        val textWidth = textPaint.measureText(label)
        val pad = 8f * density
        canvas.drawRoundRect(
            RectF(
                w / 2f - textWidth / 2f - pad,
                textY - textPaint.textSize - pad / 2f,
                w / 2f + textWidth / 2f + pad,
                textY + pad / 2f
            ),
            6f * density,
            6f * density,
            textBgPaint
        )
        canvas.drawText(label, w / 2f, textY, textPaint)
    }

    companion object {
        private const val LEVEL_TOLERANCE = 1.5f
        private val LEVEL_COLOR = Color.rgb(76, 175, 80)
        private val TILT_COLOR = Color.rgb(255, 213, 79)
    }
}
