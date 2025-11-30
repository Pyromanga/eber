package com.example.helloworld

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class MyView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    var mode: String = "none"  // "dot" oder "line"

    private val paint = Paint().apply {
        color = Color.RED
        strokeWidth = 10f
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        when (mode) {
            "dot" -> canvas.drawCircle(width/2f, height/2f, 30f, paint)
            "line" -> canvas.drawLine(50f, 50f, width-50f, height-50f, paint)
        }
    }
}
