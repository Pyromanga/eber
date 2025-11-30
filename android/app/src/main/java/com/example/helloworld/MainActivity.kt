package com.example.helloworld

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.helloworld.MyView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            // Standard Layout laden
            setContentView(R.layout.activity_main)

            val textView: TextView = findViewById(R.id.textView)
            val btnCustomView: Button = findViewById(R.id.btnCustomView)

            // Button klickbar machen
            btnCustomView.setOnClickListener {
                try {
                    // Dein Custom View laden
                    val customView = MyView(this)
                    setContentView(customView)
                } catch (e: Exception) {
                    // Fehler abfangen
                    Log.e("CrashLogger", "Fehler beim Laden von MyView: ${e.message}", e)

                    val errorText = TextView(this)
                    errorText.text = "Fehler: ${e.message}"
                    errorText.textSize = 18f
                    setContentView(errorText)
                }
            }

        } catch (e: Exception) {
            // Fehler beim Initialisieren des Main Layout
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
