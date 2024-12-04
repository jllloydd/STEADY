package com.bonak.steady

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.view.MotionEvent
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class CancelNavigationOverlay(private val onCancel: () -> Unit) : Overlay() {
    private val buttonPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        textAlign = Paint.Align.CENTER
    }
    private val buttonRect = RectF(20f, 20f, 220f, 100f)

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (!shadow) {
            canvas.drawRoundRect(buttonRect, 10f, 10f, buttonPaint)
            canvas.drawText("Cancel", buttonRect.centerX(), buttonRect.centerY() + 15f, textPaint)
        }
    }

    override fun onSingleTapConfirmed(e: MotionEvent, mapView: MapView): Boolean {
        if (buttonRect.contains(e.x, e.y)) {
            onCancel()
            return true
        }
        return false
    }
}