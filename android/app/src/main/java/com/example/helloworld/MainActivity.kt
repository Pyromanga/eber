package com.example.helloworld

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.SeekBar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

val widthSeekBar = findViewById<SeekBar>(R.id.widthSeekBar)

// Die Liniendicke beim Start setzen
// Annahme: Du hast eine öffentliche Setter-Funktion in DrawingView
drawingView.setStrokeWidth(widthSeekBar.progress.toFloat()) 

widthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        // Ändert die Liniendicke in der DrawingView
        drawingView.setStrokeWidth(progress.toFloat()) 
    }
    // Andere Methoden (onStartTrackingTouch, onStopTrackingTouch) hier ignorieren
    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
})

        // 1. Find views
        val button = findViewById<Button>(R.id.showDotButton)
        val button2 = findViewById<Button>(R.id.showLineButton)
        val container = findViewById<FrameLayout>(R.id.viewContainer)
        val modeButton = findViewById<Button>(R.id.modeButton)

        // 2. Initialize Custom Views FIRST
        val myView = MyView(this)
        container.addView(myView)

        val drawingView = DrawingView(this)
        container.addView(drawingView)

        // 3. Set Listeners (Now drawingView is safe to use)
        modeButton.setOnClickListener {
            // Use DrawingView.Mode to access the Enum
            drawingView.currentMode = if (drawingView.currentMode == DrawingView.Mode.DRAW) 
                DrawingView.Mode.TRANSFORM 
            else 
                DrawingView.Mode.DRAW
            
            modeButton.text = if (drawingView.currentMode == DrawingView.Mode.DRAW) "Zeichnen" else "Verschieben/Zoom"
        }

        button.setOnClickListener {
            myView.mode = "dot"
            myView.invalidate()
        }

        button2.setOnClickListener {
            myView.mode = "line"
            myView.invalidate()
        }
    }
}