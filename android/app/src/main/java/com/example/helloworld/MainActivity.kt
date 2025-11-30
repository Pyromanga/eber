package com.example.helloworld

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            setContentView(R.layout.activity_main) // oder MyView(this)

            // Beispiel: Zugriff auf TextView
            val textView: TextView = findViewById(R.id.textView)
            textView.text = "App läuft!"

        } catch (e: Exception) {
            // Fehler in Logcat sichtbar
            Log.e("CrashLogger", "Fehler beim Start: ${e.message}", e)

            // Optional: Fehler auf dem Bildschirm anzeigen
            val tv = TextView(this)
            tv.text = "Fehler: ${e.message}"
            setContentView(tv)
        }
    }
}
