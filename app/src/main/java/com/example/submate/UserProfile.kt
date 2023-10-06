package com.example.submate

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView
import java.util.HashMap

class UserProfile : AppCompatActivity() {

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var userProfile2: CircleImageView
    private val defaultImageResId = R.mipmap.man_foreground // Default image resource ID
    private val db = FirebaseFirestore.getInstance()
    private var selectedImageUri: Uri? = null
    private lateinit var textView2: TextView
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Find the TextView with ID textView2
        textView2 = findViewById(R.id.textView2)

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
            selectedImageUri = Uri.parse(selectedImageUriString)

            // Set the selected image URI as the image source for userProfile2
            userProfile2.setImageURI(selectedImageUri)
        } else {
            // If no image URI is provided, set the default image
            userProfile2.setImageResource(defaultImageResId)
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE)

        // Check if there is previously saved text and image URI in SharedPreferences
        val savedUsername = sharedPreferences.getString("username", null)
        val savedImageUriString = sharedPreferences.getString("imageUri", null)

        // If there is saved text, set it to textView2
        savedUsername?.let {
            textView2.text = it
        }

        // If there is a saved image URI, set it to userProfile2
        savedImageUriString?.let {
            selectedImageUri = Uri.parse(it)
            userProfile2.setImageURI(selectedImageUri)
        }

        showEditTextDialog()
        setupSaveButton()

        // Set OnClickListener for textView7
        val textView7 = findViewById<TextView>(R.id.textView7)
        textView7.setOnClickListener {
            checkGalleryPermission()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            val selectedImage = data?.data

            if (selectedImage != null) {
                // Save the selected image to a URI
                selectedImageUri = selectedImage

                // Display the selected image in the userProfile2 ImageView
                userProfile2.setImageURI(selectedImageUri)

                // Save the selected image URI to SharedPreferences
                val editor = sharedPreferences.edit()
                editor.putString("imageUri", selectedImageUri.toString())
                editor.apply()
            }
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

                    // Save the edited text to SharedPreferences
                    val editor = sharedPreferences.edit()
                    editor.putString("username", editedText)
                    editor.apply()
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
            val editedText = textView2.text.toString()

            // Get the current image URI from selectedImageUri
            val currentImageUri = selectedImageUri

            // Save the edited text and image URI to Firebase or any other storage mechanism
            saveUserDataToFirestore(editedText, currentImageUri)

            // Assuming you want to navigate back to MainActivity after saving
            val intent = Intent(this, MainActivity::class.java)

            // Pass the edited text as an extra to MainActivity
            intent.putExtra("editedText", editedText)

            // Pass the selected image URI as an extra to MainActivity
            if (currentImageUri != null) {
                intent.putExtra("profile_image_uri", currentImageUri.toString())
            }

            // Start the MainActivity
            startActivity(intent)

            // Finish the current UserProfile activity
            finish()
        }
    }

    private fun checkGalleryPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
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

    private fun saveUserDataToFirestore(username: String, imageUri: Uri?) {
        // Create a reference to the Firestore collection where you want to store user data
        val usersCollection = db.collection("users")

        // Create a data map to store the username and image URI
        val userData = HashMap<String, Any>()
        userData["username"] = username
        if (imageUri != null) {
            userData["profileImageUri"] = imageUri.toString()
        }

        // You need to associate this data with the user (e.g., using their UID) in Firestore
        // Replace "user123" with the actual user ID
        val userId = "user"

        // Set the user data in Firestore
        usersCollection.document(userId)
            .set(userData)
            .addOnSuccessListener {
                // Data was successfully saved to Firestore
            }
            .addOnFailureListener { e ->
                // Handle errors here
                Log.e(TAG, "Error saving data to Firestore", e)
            }
    }

    companion object {
        private const val TAG = "UserProfile"
    }
}