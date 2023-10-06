package com.example.submate

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*



class UserSetup : AppCompatActivity() {

    data class User(val username: String, val profileImageUri: String)

    private lateinit var userImage: CircleImageView
    private var selectedImageUri: Uri? = null
    private val defaultImageResId = R.mipmap.man_foreground // Default image resource ID
    private lateinit var database: FirebaseDatabase
    private lateinit var usersRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Hide the action bar (optional)
        supportActionBar?.hide()
        setContentView(R.layout.activity_user_setup)

        userImage = findViewById(R.id.userImage)

        // Set the default image to the CircleImageView
        userImage.setImageResource(defaultImageResId)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance()
        usersRef = database.reference.child("users")

        userImage.setOnClickListener {
            checkGalleryPermission()
        }

        val confirmButton = findViewById<Button>(R.id.continue_button)
        val nameEditText = findViewById<EditText>(R.id.userName)

        confirmButton.setOnClickListener {
            val enteredName = nameEditText.text.toString()
            val username = if (enteredName.isNotEmpty()) {
                enteredName
            } else {
                generateRandomUsername()
            }

            val user = User(username, selectedImageUri?.toString() ?: "")

            val userId = usersRef.push().key // Generate a unique key for the user
            userId?.let {
                usersRef.child(it).setValue(user)
            }

            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("name", username)
            intent.putExtra("profile_image_uri", selectedImageUri?.toString())
            startActivity(intent)
        }
    }

    private fun checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                IMAGE_PICK_REQUEST
            )
        } else {
            // Permission is already granted, you can open the gallery
            openGalleryForImage()
        }
    }

    private fun openGalleryForImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, IMAGE_PICK_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == IMAGE_PICK_REQUEST && resultCode == RESULT_OK) {
            val selectedImage = data?.data

            if (selectedImage != null) {
                // Save the selected image to a file
                val imageFile = createImageFile()
                selectedImageUri = Uri.fromFile(imageFile)

                try {
                    val inputStream = contentResolver.openInputStream(selectedImage)
                    val outputStream = FileOutputStream(imageFile)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()

                    userImage.setImageURI(selectedImageUri)

                    Log.d("UserSetup", "Selected image URI: $selectedImageUri")
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun createImageFile(): File {
        val timeStamp =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

    private fun generateRandomUsername(): String {
        val random = Random()
        val randomNumber = random.nextInt(1000)
        return "User$randomNumber"
    }

    companion object {
        private const val IMAGE_PICK_REQUEST = 1
    }
}