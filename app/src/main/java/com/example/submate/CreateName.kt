package com.example.submate

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText

class CreateName : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the action bar (optional)
        supportActionBar?.hide()

        setContentView(R.layout.activity_create_name)

        val confirmButton = findViewById<Button>(R.id.name_button)
        val nameEditText = findViewById<EditText>(R.id.editTextTextPersonName)

        confirmButton.setOnClickListener {
            // Get the name entered in the EditText
            val enteredName = nameEditText.text.toString()

            // Create an Intent to start the MainMenu activity
            val intent = Intent(this, UserProfile::class.java)

            // Add the entered name as an extra to the Intent
            intent.putExtra("name", enteredName)

            // Start the MainMenu activity
            startActivity(intent)
        }
    }

    @Suppress("DEPRECATION") // Suppress deprecated warning
    private fun hideSystemUI() {
        val decorView = window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN)
    }
}
