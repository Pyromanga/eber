package com.example.helloworld

import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Standard-Layout laden
            setContentView(R.layout.activity_main)

            val textView: TextView = findViewById(R.id.textView)
            textView.text = "App läuft!"

        } catch (e: Exception) {
            Log.e("CrashLogger", "Fehler in MainActivity: ${e.message}", e)

            val errorText = TextView(this)
            errorText.text = "Fehler beim Start: ${e.message}"
            errorText.textSize = 18f

            val layout = LinearLayout(this)
            layout.orientation = LinearLayout.VERTICAL
            layout.gravity = android.view.Gravity.CENTER
            layout.addView(errorText)
            setContentView(layout)
        }
    }
}
