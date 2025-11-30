package com.example.helloworld

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.example.helloworld.db.AppDatabase
import com.example.helloworld.db.Point
import kotlin.math.atan2

class DrawingView(context: Context) : View(context) {

    private val paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 8f
        style = Paint.Style.STROKE
    }

    private val pathPoints = mutableListOf<Pair<Float, Float>>()
    private val db = AppDatabase.getInstance(context)

    // ----- ZOOM -----
    private var scaleFactor = 1f
    private val scaleDetector = ScaleGestureDetector(context, object :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.5f, 5f)
            invalidate()
            return true
        }
    })

    // ----- ROTATION -----
    private var rotationDegrees = 0f
    private var prevAngle = 0f
    private var isRotating = false

    // ----- PANNING -----
    private var offsetX = 0f
    private var offsetY = 0f
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var isPanning = false

    init {
        // beim Start alle gespeicherten Punkte laden
        val savedPoints = db.pointDao().getAll()
        pathPoints.addAll(savedPoints.map { it.x to it.y })
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()

        // Transformation: zuerst verschieben (pan), dann skalieren (zoom), dann drehen
        canvas.translate(offsetX, offsetY)
        canvas.scale(scaleFactor, scaleFactor, width / 2f, height / 2f)
        canvas.rotate(rotationDegrees, width / 2f, height / 2f)

        // Punkte zeichnen
        for ((x, y) in pathPoints) {
            canvas.drawCircle(x, y, 10f, paint)
        }

        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Zoom
        scaleDetector.onTouchEvent(event)

        // Rotation (zwei Finger)
        if (event.pointerCount == 2) {
            val dx = event.getX(1) - event.getX(0)
            val dy = event.getY(1) - event.getY(0)
            val angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()

            if (!isRotating) {
                prevAngle = angle
                isRotating = true
            } else {
                val delta = angle - prevAngle
                rotationDegrees += delta
                prevAngle = angle
            }
            invalidate()
            return true
        } else {
            isRotating = false
        }

        // Panning (ein Finger ohne Zeichnen)
        if (event.pointerCount == 1) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastTouchX = event.x
                    lastTouchY = event.y
                    isPanning = true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isPanning) return true
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY
                    offsetX += dx
                    offsetY += dy
                    lastTouchX = event.x
                    lastTouchY = event.y
                    invalidate()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    isPanning = false
                }
            }

            // Zeichnen (optional: gleichzeitig)
            val x = event.x - offsetX
            val y = event.y - offsetY
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    pathPoints.add(x to y)
                    db.pointDao().insert(Point(x = x, y = y))
                }
            }
        }

        return true
    }

    fun clear() {
        pathPoints.clear()
        db.pointDao().clear()
        offsetX = 0f
        offsetY = 0f
        scaleFactor = 1f
        rotationDegrees = 0f
        invalidate()
    }
}
