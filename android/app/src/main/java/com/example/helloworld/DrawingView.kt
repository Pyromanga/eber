package com.example.helloworld

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.example.helloworld.db.AppDatabase
import com.example.helloworld.db.Point
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.hypot

class DrawingView(context: Context) : View(context) {

    enum class Mode {
        DRAW,
        TRANSFORM
    }

    var currentMode = Mode.DRAW

    // 1. & 2. FIX: Paint Style für saubere Linien
    private val paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 15f         // Etwas dicker, damit man es gut sieht
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND // WICHTIG: Macht die Enden rund (sieht aus wie ein Punkt)
        strokeJoin = Paint.Join.ROUND // WICHTIG: Macht Ecken rund
        isAntiAlias = true
    }

    // Wir nutzen einen Path zum Zeichnen (viel schneller und sauberer als drawCircle)
    private val drawingPath = Path()
    private val currentSegmentPoints = mutableListOf<Point>() // Temporärer Speicher für DB Save
    
    // DB Zugriff
    private val db = AppDatabase.getInstance(context)
    private val scope = CoroutineScope(Dispatchers.IO) // Für DB Operationen im Hintergrund

    // ----- TRANSFORM STATE -----
    private var scaleFactor = 1f
    private var rotationDegrees = 0f
    private var offsetX = 0f
    private var offsetY = 0f

    // Helper für Transformations-Gestik
    private var lastFocusX = 0f
    private var lastFocusY = 0f
    private var isTransforming = false
    private var lastAngle = 0f
    
    // Scale Detector
    private val scaleDetector = ScaleGestureDetector(context, object :
        ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val oldScale = scaleFactor
            scaleFactor *= detector.scaleFactor
            scaleFactor = scaleFactor.coerceIn(0.1f, 10f) // Limits setzen
            
            // Optional: Zoom zum Fokuspunkt (komplexer, hier vereinfacht global)
            return true
        }
    })

    init {
        // Beim Start Punkte laden und Path rekonstruieren
        // ACHTUNG: Das sollte eigentlich asynchron passieren, blockiert hier kurz den Start
        scope.launch(Dispatchers.Main) {
            val savedPoints = kotlinx.coroutines.withContext(Dispatchers.IO) {
                db.pointDao().getAll()
            }
            if (savedPoints.isNotEmpty()) {
                // Wir rekonstruieren den Pfad einfachheitshalber als Punkte-Linie
                // Für eine perfekte App müsstest du eigentlich "Linien-Segmente" speichern
                var first = true
                for (p in savedPoints) {
                    if (first) {
                        drawingPath.moveTo(p.x, p.y)
                        first = false
                    } else {
                        // Prüfen ob Punkte weit auseinander liegen (neue Linie), 
                        // hier vereinfacht verbinden wir alles. 
                        // Ideal: Speichere "ActionType" (MOVE/DOWN) in DB.
                        drawingPath.lineTo(p.x, p.y)
                    }
                }
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()

        // Transformationen anwenden
        // Reihenfolge ist extrem wichtig: Translate -> Rotate -> Scale (um den Pivot)
        
        // 1. Zum Mittelpunkt verschieben (für Rotation/Zoom um Mitte)
        val px = width / 2f
        val py = height / 2f
        
        canvas.translate(offsetX, offsetY)
        
        // Rotation und Scale um den Screen-Mittelpunkt
        canvas.translate(px, py)
        canvas.scale(scaleFactor, scaleFactor)
        canvas.rotate(rotationDegrees)
        canvas.translate(-px, -py)

        // Pfad zeichnen (statt 1000 einzelne Kreise -> Performance & saubere Linie)
        canvas.drawPath(drawingPath, paint)

        canvas.restore()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // Transformations-Logik (Zoom/Pan/Rotate)
        if (currentMode == Mode.TRANSFORM) {
            scaleDetector.onTouchEvent(event)
            
            // Eigene Logik für Pan & Rotate
            val pointerCount = event.pointerCount
            
            // 1. Berechne den Schwerpunkt (Fokus) aller Finger
            var sumX = 0f
            var sumY = 0f
            for (i in 0 until pointerCount) {
                sumX += event.getX(i)
                sumY += event.getY(i)
            }
            val focusX = sumX / pointerCount
            val focusY = sumY / pointerCount

            // 3. FIX: Flickering verhindern
            // Wenn sich die Anzahl der Finger ändert (einer hochgehoben/dazugekommen),
            // müssen wir den "letzten Punkt" resetten, sonst springt die Berechnung.
            if (event.actionMasked == MotionEvent.ACTION_DOWN ||
                event.actionMasked == MotionEvent.ACTION_POINTER_DOWN ||
                event.actionMasked == MotionEvent.ACTION_POINTER_UP) {
                lastFocusX = focusX
                lastFocusY = focusY
                
                // Reset Rotation anchor
                if (pointerCount == 2) {
                    val dy = event.getY(1) - event.getY(0)
                    val dx = event.getX(1) - event.getX(0)
                    lastAngle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                }
                return true
            }

            // Panning (Verschieben)
            val dx = focusX - lastFocusX
            val dy = focusY - lastFocusY
            
            offsetX += dx
            offsetY += dy
            
            lastFocusX = focusX // Update für nächsten Move
            lastFocusY = focusY

            // Rotation (nur bei genau 2 Fingern)
            if (pointerCount == 2) {
                val rawDy = event.getY(1) - event.getY(0)
                val rawDx = event.getX(1) - event.getX(0)
                val angle = Math.toDegrees(atan2(rawDy.toDouble(), rawDx.toDouble())).toFloat()
                
                // Delta berechnen
                var deltaAngle = angle - lastAngle
                // Winkel-Sprünge korrigieren (-180 zu 180)
                while (deltaAngle > 180) deltaAngle -= 360
                while (deltaAngle < -180) deltaAngle += 360
                
                rotationDegrees += deltaAngle
                lastAngle = angle
            }

            invalidate()
            return true
        }

        // Zeichnen Logik
        if (currentMode == Mode.DRAW && event.pointerCount == 1) {
            // Umrechnen der Touch-Koordinaten in "Zeichnungskoordinaten" (Rückgängig machen von Zoom/Pan)
            val (worldX, worldY) = toWorldCoordinates(event.x, event.y)

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    drawingPath.moveTo(worldX, worldY)
                    // Um einen einzelnen Punkt zu malen bei nur "Click":
                    drawingPath.addCircle(worldX, worldY, 1f, Path.Direction.CW) 
                    
                    currentSegmentPoints.clear()
                    currentSegmentPoints.add(Point(x = worldX, y = worldY))
                    invalidate()
                }
                MotionEvent.ACTION_MOVE -> {
                    // 2. FIX: Hier verbinden wir die Punkte mit Linien -> Keine Lücken
                    drawingPath.lineTo(worldX, worldY)
                    
                    // Sammeln für DB, aber NICHT hier speichern!
                    currentSegmentPoints.add(Point(x = worldX, y = worldY))
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    // 2. FIX: Erst jetzt speichern wir den Batch in die DB (Hintergrund)
                    savePointsToDb(currentSegmentPoints.toList())
                }
            }
            return true
        }
        return false
    }

    private fun savePointsToDb(points: List<Point>) {
        if (points.isEmpty()) return
        scope.launch {
            // Batch insert wäre besser, aber Loop im Background ist ok für kleine Mengen
            for(p in points) {
                db.pointDao().insert(p)
            }
        }
    }

    // Umrechnung von Screen-Touch -> Zeichnungs-Ebene
    private fun toWorldCoordinates(touchX: Float, touchY: Float): Pair<Float, Float> {
        val px = width / 2f
        val py = height / 2f

        // 1. Pan rückgängig machen
        var x = touchX - offsetX
        var y = touchY - offsetY

        // 2. Rotation und Scale um Center rückgängig machen
        // Verschieben zum Ursprung (px, py)
        x -= px
        y -= py

        // Scale inverse
        x /= scaleFactor
        y /= scaleFactor

        // Rotate inverse
        val rad = Math.toRadians(-rotationDegrees.toDouble())
        val cos = kotlin.math.cos(rad)
        val sin = kotlin.math.sin(rad)
        
        val newX = x * cos - y * sin
        val newY = x * sin + y * cos

        // Zurückschieben
        return Pair((newX + px).toFloat(), (newY + py).toFloat())
    }
    
    fun clear() {
        drawingPath.reset()
        offsetX = 0f
        offsetY = 0f
        scaleFactor = 1f
        rotationDegrees = 0f
        scope.launch {
            db.pointDao().clear()
        }
        invalidate()
    }
    // In DrawingView.kt
fun setStrokeWidth(width: Float) {
    paint.strokeWidth = width.coerceIn(5f, 50f) // Sicherstellen, dass die Breite Sinn macht
    invalidate()
}
}