package com.example.helloworld

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button = findViewById<Button>(R.id.showDotButton)
        val button2 = findViewById<Button>(R.id.showLineButton)
        val container = findViewById<FrameLayout>(R.id.viewContainer)
val modeButton = findViewById<Button>(R.id.modeButton)
modeButton.setOnClickListener {
    drawingView.currentMode = if (drawingView.currentMode == Mode.DRAW) Mode.TRANSFORM else Mode.DRAW
    modeButton.text = if (drawingView.currentMode == Mode.DRAW) "Zeichnen" else "Verschieben/Zoom"
}


        val myView = MyView(this)
        container.addView(myView)
        
        val drawingView = DrawingView(this)
        container.addView(drawingView)
        
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
