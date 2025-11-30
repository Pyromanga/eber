package com.example.helloworld

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View // Neu: Wird benötigt, falls du myView/drawingView in einer anderen Methode nutzen willst
import android.widget.Button
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.Switch // Nur falls du den SaveSwitch nutzt

class MainActivity : AppCompatActivity() {

    // 1. DEKLARATION ALS KLASSENFELDER (Properties)
    // Damit die Views in der gesamten Activity (auch in Listenern) sichtbar sind.
    private lateinit var drawingView: DrawingView
    private lateinit var myView: MyView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 2. ALLE VIEWS FINDEN
        val container = findViewById<FrameLayout>(R.id.viewContainer)
        val modeButton = findViewById<Button>(R.id.modeButton)
        val showDotButton = findViewById<Button>(R.id.showDotButton)
        val showLineButton = findViewById<Button>(R.id.showLineButton)
        
        // NEUE VIEWS vom Playground
        val widthSeekBar = findViewById<SeekBar>(R.id.widthSeekBar)
        // val saveSwitch = findViewById<Switch>(R.id.saveSwitch) // Falls du den Switch benutzt

        // 3. CUSTOM VIEWS INITIALISIEREN UND HINZUFÜGEN
        // Hier erfolgt die Zuweisung zu den Klassenfeldern (lateinit var)
        myView = MyView(this)
        container.addView(myView)

        drawingView = DrawingView(this)
        container.addView(drawingView)
        
        // 4. LISTENER FESTLEGEN (Jetzt ist 'drawingView' sicher initialisiert!)

        // A) SeekBar Listener für die Liniendicke
        // Der Zugriff auf drawingView ist jetzt OK, da es oben initialisiert wurde.
        drawingView.setStrokeWidth(widthSeekBar.progress.toFloat()) 

        widthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // Delegiere die Logik an die DrawingView
                drawingView.setStrokeWidth(progress.toFloat()) 
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // B) Modus-Button
        modeButton.setOnClickListener {
            // Logik-Delegation
            drawingView.currentMode = if (drawingView.currentMode == DrawingView.Mode.DRAW) 
                DrawingView.Mode.TRANSFORM 
            else 
                DrawingView.Mode.DRAW
            
            modeButton.text = if (drawingView.currentMode == DrawingView.Mode.DRAW) "Zeichnen" else "Verschieben/Zoom"
        }

        // C) MyView Buttons
        showDotButton.setOnClickListener {
            myView.mode = "dot"
            myView.invalidate()
        }

        showLineButton.setOnClickListener {
            myView.mode = "line"
            myView.invalidate()
        }
    }
}