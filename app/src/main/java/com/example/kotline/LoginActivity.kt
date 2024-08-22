package com.example.kotline

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.kotline.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityLoginBinding.inflate(layoutInflater)  //activating viewBinding
        val view = binding.root
        if (supportActionBar != null)
            supportActionBar?.hide()
        setContentView(view)

        binding.loginButton.setOnClickListener{
            loginProcess()
        }

        binding.registerAccountButton.setOnClickListener {
            Log.d("MainActivity", "Opening register activity")
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun loginProcess() {
        val email = binding.emailLogin.text.toString()
        val password = binding.passwordLogin.text.toString()
        if (validateInputs(email, password)) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        Log.d("Main", "Successfully logged in!")
                        Toast.makeText(this, "Successfully logged in!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LastMessagesActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Successfully logged in!", Toast.LENGTH_SHORT).show()
                        Log.d("Main", "Not logged in!")
                        return@addOnCompleteListener
                    }
                }
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
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
        return true
    }

}