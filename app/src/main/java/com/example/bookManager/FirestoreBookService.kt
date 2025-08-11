package com.example.bookManager

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FirestoreBookService(private val context: Context) {

    private val user = FirebaseAuth.getInstance().currentUser
    private val collection = if (user != null) {
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(user.uid)
            .collection("books")
    } else null

    fun uploadBook(book: Book) {
        collection?.document(book.id.toString())?.set(book)
    }

    fun deleteBook(book: Book) {
        collection?.document(book.id.toString())?.delete()
    }

    fun fetchAllBooks(callback: (List<Book>) -> Unit) {
        collection?.get()
            ?.addOnSuccessListener { snapshot ->
                val books = snapshot.toObjects(Book::class.java)
                callback(books)
                Toast.makeText(context, "Synced", Toast.LENGTH_SHORT).show()
            }
            ?.addOnFailureListener {
                Log.e("FirestoreSync", "Cloud sync failed: ${it.message}")
                Toast.makeText(context, "Cloud sync failed. Firestore might be disabled.", Toast.LENGTH_LONG).show()
            }
    }
}
