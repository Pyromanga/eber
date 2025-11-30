// MyView.kt
package com.example.helloworld

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class MyView(context: Context) : View(context) {
    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 10f
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Zeichne einen Strich von (50,50) nach (300,300)
        canvas.drawLine(50f, 50f, 300f, 300f, paint)
    }
}
