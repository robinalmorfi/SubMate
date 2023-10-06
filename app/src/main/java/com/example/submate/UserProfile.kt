package com.example.submate

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import de.hdodenhof.circleimageview.CircleImageView
import android.text.Editable
import android.net.Uri
import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.content.pm.PackageManager

class UserProfile : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var userProfile2: CircleImageView
    private val defaultImageResId = R.mipmap.man_foreground // Default image resource ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Find the TextView with ID textView2
        val textView2 = findViewById<TextView>(R.id.textView2)

        // Retrieve the entered name from the extras
        val enteredName = intent.getStringExtra("name")

        // Set the text of textView2 to the entered name
        textView2.text = enteredName

        // Find the ImageView with ID userProfile2
        userProfile2 = findViewById(R.id.userProfile2)

        // Retrieve the selected image URI from the extras
        val selectedImageUriString = intent.getStringExtra("profile_image_uri")

        // Check if a valid image URI was passed
        if (selectedImageUriString != null) {
            val selectedImageUri = Uri.parse(selectedImageUriString)

            // Set the selected image URI as the image source for userProfile2
            userProfile2.setImageURI(selectedImageUri)
        } else {
            // If no image URI is provided, set the default image
            userProfile2.setImageResource(defaultImageResId)
        }

        showEditTextDialog()
        setupSaveButton()

        // Set OnClickListener for textView7
        val textView7 = findViewById<TextView>(R.id.textView7)
        textView7.setOnClickListener {
            checkGalleryPermission()
        }
    }

    private fun showEditTextDialog() {
        val textView9 = findViewById<TextView>(R.id.textView9)

        textView9.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val inflater = layoutInflater
            val dialogLayout = inflater.inflate(R.layout.activity_edit_profile_name_dialog_box, null)
            val dialogEditText = dialogLayout.findViewById<EditText>(R.id.et_editText)

            // Find textView2 from the current activity layout (activity_user_profile.xml)
            val textView2 = findViewById<TextView>(R.id.textView2)


            // Set the initial text of dialogEditText to match textView2
            dialogEditText.text = Editable.Factory.getInstance().newEditable(textView2.text.toString())

            with(builder) {
                setTitle("Edit Username")
                setPositiveButton("OK") { dialog, which ->
                    val editedText = dialogEditText.text.toString()
                    textView2.text = editedText // Set the text to textView2
                }
                setNegativeButton("Cancel") { dialog, which ->
                    Log.d("Main", "Negative button clicked")
                }
                setView(dialogLayout)
                show()
            }
        }
    }

    private fun setupSaveButton() {
        val saveButton = findViewById<Button>(R.id.button6)

        saveButton.setOnClickListener {
            // Get the edited text from textView2
            val textView2 = findViewById<TextView>(R.id.textView2)
            val editedText = textView2.text.toString()

            // Create an Intent to navigate back to MainActivity
            val intent = Intent(this, MainActivity::class.java)

            // Pass the edited text as an extra to MainActivity
            intent.putExtra("editedText", editedText)

            // Start the MainActivity
            startActivity(intent)

            // Finish the current UserProfile activity
            finish()
        }
    }

    private fun checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PICK_IMAGE_REQUEST
            )
        } else {
            // Permission is already granted, you can open the gallery
            openGallery()
        }
    }

    private fun openGallery() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent, PICK_IMAGE_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedImageUri = data.data
            userProfile2.setImageURI(selectedImageUri)
        }
    }
}
