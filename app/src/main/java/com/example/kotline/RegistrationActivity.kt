package com.example.kotline

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotline.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.parcelize.Parcelize
import java.util.UUID


class RegistrationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)  //activating viewBinding
        if (supportActionBar != null)
            supportActionBar?.hide()
        setContentView(binding.root)
        //ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
        //val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        //v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
        //insets
        //}
        binding.buttonRegistration.setOnClickListener {registrationProcess()}
        //opening loginActivity
        binding.existingAccountLoginTextView.setOnClickListener {
            Log.d("Registration", "Opening loginActivity")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.selectAccountPictureButton.setOnClickListener{
            Log.d("Registration", "Trying to select photo")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent,0)
        }
    }

    var selectedPhoto: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == 0 && resultCode == Activity.RESULT_OK && data != null){
            Log.d("RegisterActivity", "Photo was selected")
            selectedPhoto = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhoto)
            binding.selectAccountPicture.setImageBitmap(bitmap)

            //  val bitmapDrawable = BitmapDrawable(bitmap)
            //  binding.selectAccountPictureButton.setBackgroundDrawable(bitmapDrawable)
        }
    }

    private fun registrationProcess(){
        val username = binding.usernameRegistration.text.toString()
        val email = binding.emailRegistration.text.toString()
        val password = binding.passwordRegistration.text.toString()
        val passwordConfirm = binding.confirmPasswordRegistration.text.toString()

        //logging
        Log.d("MainActivity", "Username:" + username)
        Log.d("MainActivity", "Email:" + email)
        Log.d("MainActivity", "Password:" + password)
        Log.d("MainActivity", "PasswordConfirm:" + passwordConfirm)

        //checking password and passwordConfirm
        if (validateInputs(username, email, password, passwordConfirm)){
            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("Registration", "Successfully created new account")
                        Toast.makeText(this, "Successfully created new account!", Toast.LENGTH_SHORT).show()
                        uploadImageFirebase()
                    }
                    else {
                        Log.d("Registration", "Not created")
                        return@addOnCompleteListener
                    }
                }
                .addOnFailureListener{
                    Log.d("Registration", "Not created")
                    Toast.makeText(this, "Failed to create new account: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadImageFirebase(){
        if (selectedPhoto == null) return
        val filename = UUID.randomUUID().toString()
        val reference = FirebaseStorage.getInstance().getReference("/images/$filename")

        reference.putFile(selectedPhoto!!)
            .addOnSuccessListener {
                Log.d("Registration", "Photo uploaded successfully: ${it.metadata?.path}")
                reference.downloadUrl.addOnSuccessListener{
                    Log.d("Registration", "File location: $it")
                    saveUserToDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                Toast.makeText(this, "Photo was not saved to Database!", Toast.LENGTH_SHORT).show()
                Log.d("Registration", "Photo was not saved")
            }
    }

    private fun saveUserToDatabase(accountImageUrl: String){
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val reference = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid, binding.usernameRegistration.text.toString(), accountImageUrl)
        reference.setValue(user)
            .addOnSuccessListener {
                Log.d("Registration", "User saved to Database")

                //запуск нового activity, где отображаются последние сообщения
                val intent = Intent(this, LastMessagesActivity::class.java)

                //закрываем предыдущие activity
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
            .addOnFailureListener{
                Toast.makeText(this, "User was not saved to Database!", Toast.LENGTH_SHORT).show()
                Log.d("Registration", "User was not saved to Database")
            }
    }


    @Parcelize
    class User(val uid: String, val username: String, val accountImageUrl: String):Parcelable{
        constructor() : this("","","")
    }

    private fun validateInputs(
        username: String,
        email: String,
        password: String,
        passwordConfirm: String
    ): Boolean {
        if (username.isBlank()) {
            Log.d("Registration validation", "Username is empty!")
            Toast.makeText(this, "Username is empty!", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isBlank()) {
            Log.d("Registration validation", "Email is empty!")
            Toast.makeText(this, "Email is empty", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isBlank()) {
            Log.d("Registration validation", "Password is empty!")
            Toast.makeText(this, "Password is empty!", Toast.LENGTH_SHORT).show()
            return false
        }
        if (passwordConfirm.isBlank()) {
            Log.d("Registration validation", "Password confirmation is empty!")
            Toast.makeText(this, "Password confirmation is empty!", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != passwordConfirm) {
            Log.d("Registration validation", "Passwords do not match!")
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}