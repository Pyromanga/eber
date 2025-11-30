package com.example.helloworld

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import com.example.helloworld.db.AppDatabase
import com.example.helloworld.db.Point

class DrawingView(context: Context) : View(context) {

    private val paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }

    private val pathPoints = mutableListOf<Pair<Float, Float>>()
    private val db = AppDatabase.getInstance(context)

    init {
        // beim Start alle gespeicherten Punkte laden
        val savedPoints = db.pointDao().getAll()
        pathPoints.addAll(savedPoints.map { it.x to it.y })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for ((x, y) in pathPoints) {
            canvas.drawCircle(x, y, 10f, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                pathPoints.add(x to y)
                db.pointDao().insert(Point(x = x, y = y))
            }
        }

        invalidate()
        return true
    }

    fun clear() {
        pathPoints.clear()
        db.pointDao().clear()
        invalidate()
    }
}
