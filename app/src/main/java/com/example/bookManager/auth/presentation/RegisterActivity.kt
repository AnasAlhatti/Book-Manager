package com.example.bookManager.auth.presentation

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.bookManager.MainActivity
import com.example.bookManager.R
import com.google.firebase.auth.FirebaseAuth

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        findViewById<Button>(R.id.btnCreateAccount).setOnClickListener {
            val email = findViewById<EditText>(R.id.editRegisterEmail).text.toString()
            val password = findViewById<EditText>(R.id.editRegisterPassword).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.editConfirmPassword).text.toString()

            if (email.isBlank() || password.length < 6) {
                showToast("Email required and password must be at least 6 characters")
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                showToast("Passwords do not match")
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    showToast("Account created")
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
                .addOnFailureListener {
                    showToast("Registration failed: ${it.message}")
                }
        }
        findViewById<Button>(R.id.btnBackToLogin).setOnClickListener {
            finish() // closes RegisterActivity and returns to LoginActivity
        }
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
