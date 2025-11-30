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

    enum class Mode {
        DRAW,
        TRANSFORM
    }

    var currentMode = Mode.DRAW

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
        // Zoom / Rotation nur im Transform-Modus
        if (currentMode == Mode.TRANSFORM) {
            scaleDetector.onTouchEvent(event)

            if (event.pointerCount == 2) {
                // Rotation
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

            // Panning (ein Finger)
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
            }
        }

        // Zeichnen nur im Draw-Modus
        if (currentMode == Mode.DRAW && event.pointerCount == 1) {
            val (x, y) = toCanvasCoordinates(event.x, event.y)
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                    pathPoints.add(x to y)
                    db.pointDao().insert(Point(x = x, y = y))
                    invalidate()
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

    private fun toCanvasCoordinates(x: Float, y: Float): Pair<Float, Float> {
        // Schritt 1: Verschiebung (Pan) abziehen
        var px = x - offsetX
        var py = y - offsetY

        // Schritt 2: Rotation um die Mitte zurückdrehen
        val cx = width / 2f
        val cy = height / 2f
        val rad = Math.toRadians(-rotationDegrees.toDouble()) // invers rotieren
        val cos = kotlin.math.cos(rad)
        val sin = kotlin.math.sin(rad)
        val nx = cos * (px - cx) - sin * (py - cy) + cx
        val ny = sin * (px - cx) + cos * (py - cy) + cy
        px = nx.toFloat() // Fixed Typo Here
        py = ny.toFloat()

        // Schritt 3: Zoom abziehen
        px = (px - cx) / scaleFactor + cx
        py = (py - cy) / scaleFactor + cy

        return px to py
    }
}