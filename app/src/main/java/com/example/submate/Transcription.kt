package com.example.submate

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

class Transcription : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_speech)

        val imageBack = findViewById<View>(R.id.imageBack)

        imageBack.setOnClickListener {
            onBackPressed()
        }
    }
}
