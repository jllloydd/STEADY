package com.bonak.steady

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Overlay

class TextOverlay(private val text: String) : Overlay() {
    private val textPaint = Paint().apply {
        color = Color.WHITE
        textSize = 40f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val backgroundPaint = Paint().apply {
        color = Color.BLACK
        alpha = 150
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (!shadow) {
            val x = (mapView.width / 2).toFloat()
            val y = 50f


            val textWidth = textPaint.measureText(text)
            val textHeight = textPaint.descent() - textPaint.ascent()


            val backgroundRect = RectF(
                x - textWidth / 2 - 20,
                y + textPaint.ascent() - 10,
                x + textWidth / 2 + 20,
                y + textPaint.descent() + 10
            )
            canvas.drawRoundRect(backgroundRect, 10f, 10f, backgroundPaint)


            canvas.drawText(text, x, y, textPaint)
        }
    }
}