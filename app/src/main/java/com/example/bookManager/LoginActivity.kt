package com.example.bookManager

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient

    private val RC_SIGN_IN = 9001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        if (FirebaseAuth.getInstance().currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        auth = FirebaseAuth.getInstance()

        // Google Sign-In setup
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // from google-services.json
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        findViewById<Button>(R.id.btnEmailLogin).setOnClickListener {
            val email = findViewById<EditText>(R.id.editEmail).text.toString()
            val pass = findViewById<EditText>(R.id.editPassword).text.toString()

            if (email.isNotBlank() && pass.length >= 6) {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener { goToMain() }
                    .addOnFailureListener { showToast("Login failed: ${it.message}") }
            } else {
                showToast("Enter valid email and 6+ character password")
            }
        }

        findViewById<Button>(R.id.btnRegister).setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        findViewById<Button>(R.id.btnGoogle).setOnClickListener {
            val intent = googleSignInClient.signInIntent
            startActivityForResult(intent, RC_SIGN_IN)
        }

        findViewById<Button>(R.id.btnGuest).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Continue as Guest?")
                .setMessage("Your books will not be saved in the cloud. You can sign in later to back up your data.")
                .setPositiveButton("Continue") { _, _ ->
                    auth.signInAnonymously()
                        .addOnSuccessListener { goToMain() }
                        .addOnFailureListener { showToast("Guest login failed") }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun goToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account: GoogleSignInAccount = task.result!!
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnSuccessListener { goToMain() }
                    .addOnFailureListener { showToast("Google sign-in failed") }
            } catch (e: Exception) {
                showToast("Google sign-in error: ${e.message}")
            }
        }
    }
}
