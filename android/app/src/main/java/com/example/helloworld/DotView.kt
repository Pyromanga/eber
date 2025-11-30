package com.example.helloworld

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.view.View

class DotView(context: Context) : View(context) {

    private val paint = Paint().apply {
        color = 0xFF000000.toInt()
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val x = width / 2f
        val y = height / 2f
        canvas.drawCircle(x, y, 30f, paint)
    }
}
