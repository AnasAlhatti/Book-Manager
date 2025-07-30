package com.example.BookManager

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            // Not logged in -> show login
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            // Already logged in -> show main app
            startActivity(Intent(this, MainActivity::class.java))
        }

        finish() // Close launcher activity
    }
}
