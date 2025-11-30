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
        val container = findViewById<FrameLayout>(R.id.viewContainer)

        button.setOnClickListener {
            // Punkt-View einfügen, aber TextView bleibt stehen
            val dotView = DotView(this)
            container.removeAllViews()  // optional — wenn du vorherigen Punkt entfernen willst
            container.addView(dotView)
        }
    }
}
