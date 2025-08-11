package com.example.bookManager.core.data.remote

import android.util.Log
import com.example.bookManager.core.model.Book
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class FirestoreBookDataSource {

    private val firestore = FirebaseFirestore.getInstance()
    private val userId: String
        get() = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"

    private fun booksCollection() =
        firestore.collection("users").document(userId).collection("books")

    fun uploadBook(book: Book) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            Log.w("FirestoreUpload", "Cannot upload - user not authenticated")
            return
        }

        if (book.uuid.isBlank()) {
            book.uuid = UUID.randomUUID().toString()
            Log.w("FirestoreUpload", "Book UUID was blank, generated new UUID: ${book.uuid}")
        }

        val bookData = mapOf(
            "id" to book.id,
            "uuid" to book.uuid,
            "title" to book.title,
            "author" to book.author,
            "pagesRead" to book.pagesRead,
            "totalPages" to book.totalPages
        )

        booksCollection().document(book.uuid).set(bookData)
            .addOnSuccessListener {
                Log.d("FirestoreUpload", "Book uploaded successfully: ${book.uuid}")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreUpload", "Failed to upload book: ${book.uuid}", e)
            }
    }

    fun deleteBook(book: Book) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            Log.w("FirestoreDelete", "Cannot delete - user not authenticated")
            return
        }

        if (book.uuid.isBlank()) {
            Log.e("FirestoreDelete", "UUID is empty. Cannot delete from cloud.")
            return
        }

        booksCollection().document(book.uuid).delete()
            .addOnSuccessListener {
                Log.d("FirestoreDelete", "Book deleted successfully: ${book.uuid}")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDelete", "Failed to delete book: ${book.uuid}", e)
            }
    }

    fun deleteByUuid(uuid: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            Log.w("FirestoreDelete", "Cannot delete - user not authenticated")
            return
        }

        if (uuid.isBlank()) {
            Log.w("FirestoreDelete", "UUID is blank, cannot delete")
            return
        }

        booksCollection().document(uuid).delete()
            .addOnSuccessListener {
                Log.d("FirestoreDelete", "Book deleted by UUID: $uuid")
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreDelete", "Failed to delete book by UUID: $uuid", e)
            }
    }

    fun fetchAllBooks(onResult: (List<Book>) -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            Log.w("FirestoreFetch", "Cannot fetch - user not authenticated")
            onResult(emptyList())
            return
        }

        booksCollection().get()
            .addOnSuccessListener { snapshot ->
                try {
                    val books = mutableListOf<Book>()
                    for (document in snapshot.documents) {
                        try {
                            val book = Book(
                                id = (document.getLong("id") ?: 0).toInt(),
                                uuid = document.getString("uuid") ?: "",
                                title = document.getString("title") ?: "",
                                author = document.getString("author") ?: "",
                                pagesRead = (document.getLong("pagesRead") ?: 0).toInt(),
                                totalPages = (document.getLong("totalPages") ?: 0).toInt()
                            )

                            // Ensure UUID is set
                            if (book.uuid.isBlank()) {
                                book.uuid = document.id // Use document ID as fallback
                            }

                            books.add(book)
                        } catch (e: Exception) {
                            Log.e("FirestoreFetch", "Error parsing book document: ${document.id}", e)
                        }
                    }
                    Log.d("FirestoreFetch", "Fetched ${books.size} books from cloud")
                    onResult(books)
                } catch (e: Exception) {
                    Log.e("FirestoreFetch", "Error processing fetch results", e)
                    onResult(emptyList())
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreFetch", "Failed to fetch books from cloud", e)
                onResult(emptyList())
            }
    }

    fun clearCloudData() {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            Log.w("FirestoreClear", "Cannot clear - user not authenticated")
            return
        }

        booksCollection().get()
            .addOnSuccessListener { snapshot ->
                val batch = firestore.batch()
                for (document in snapshot.documents) {
                    batch.delete(document.reference)
                }
                batch.commit()
                    .addOnSuccessListener {
                        Log.d("FirestoreClear", "All cloud data cleared successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("FirestoreClear", "Failed to clear cloud data", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreClear", "Failed to fetch documents for clearing", e)
            }
    }
}