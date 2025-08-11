package com.example.bookManager

import androidx.lifecycle.LiveData
import android.content.Context
import android.util.Log
import java.util.UUID

class BookRepository(
    private val bookDao: BookDao,
    private val deletedBookDao: DeletedBookDao,
    context: Context,
    useCloud: Boolean
) {
    val allBooks: LiveData<List<Book>> = bookDao.getAllBooks()
    // Cloud operations are now handled by ViewModel using FirestoreBookRepository

    suspend fun insert(book: Book) {
        try {
            // Ensure UUID is set before inserting
            if (book.uuid.isBlank()) {
                book.uuid = UUID.randomUUID().toString()
                Log.d("BookRepository", "Generated UUID for new book: ${book.uuid}")
            }

            bookDao.insert(book)
            Log.d("BookRepository", "Book inserted locally: ${book.uuid}")

            // Note: Cloud sync is handled by ViewModel, not here
        } catch (e: Exception) {
            Log.e("BookRepository", "Error inserting book", e)
            throw e
        }
    }

    suspend fun update(book: Book) {
        try {
            // Ensure UUID is set before updating
            if (book.uuid.isBlank()) {
                book.uuid = UUID.randomUUID().toString()
                Log.d("BookRepository", "Generated UUID for existing book: ${book.uuid}")
            }

            bookDao.update(book)
            Log.d("BookRepository", "Book updated locally: ${book.uuid}")

            // Note: Cloud sync is handled by ViewModel, not here
        } catch (e: Exception) {
            Log.e("BookRepository", "Error updating book", e)
            throw e
        }
    }

    suspend fun delete(book: Book) {
        try {
            bookDao.delete(book)

            // Track deletion for sync purposes
            if (book.uuid.isNotBlank()) {
                deletedBookDao.insert(DeletedBook(book.uuid))
                Log.d("BookRepository", "Book deleted and tracked: ${book.uuid}")
            } else {
                Log.w("BookRepository", "Deleted book has no UUID, cannot track for cloud sync")
            }

            // Note: Cloud sync is handled by ViewModel, not here
        } catch (e: Exception) {
            Log.e("BookRepository", "Error deleting book", e)
            throw e
        }
    }

    suspend fun deleteAll() {
        try {
            // Get all books before deleting to track UUIDs
            val allBooks = bookDao.getAllBooksNow()
            for (book in allBooks) {
                if (book.uuid.isNotBlank()) {
                    deletedBookDao.insert(DeletedBook(book.uuid))
                }
            }

            bookDao.deleteAll()
            Log.d("BookRepository", "All books deleted locally and tracked for cloud sync")
        } catch (e: Exception) {
            Log.e("BookRepository", "Error deleting all books", e)
            throw e
        }
    }

    suspend fun getDeletedUuids(): List<String> {
        return try {
            deletedBookDao.getAll().map { it.uuid }
        } catch (e: Exception) {
            Log.e("BookRepository", "Error getting deleted UUIDs", e)
            emptyList()
        }
    }

    suspend fun clearDeletedUuids() {
        try {
            deletedBookDao.clear()
            Log.d("BookRepository", "Deleted UUIDs cleared")
        } catch (e: Exception) {
            Log.e("BookRepository", "Error clearing deleted UUIDs", e)
        }
    }

    suspend fun getAllBooksOnce(): List<Book> {
        return try {
            bookDao.getAllBooksNow()
        } catch (e: Exception) {
            Log.e("BookRepository", "Error getting all books", e)
            emptyList()
        }
    }

    suspend fun getBookByUuid(uuid: String): Book? {
        return try {
            bookDao.getBookByUuid(uuid)
        } catch (e: Exception) {
            Log.e("BookRepository", "Error getting book by UUID: $uuid", e)
            null
        }
    }

    fun getFinishedBooks() = bookDao.getFinishedBooks()
    fun getBooksUnderPercentage(percent: Int) = bookDao.getBooksUnderPercentage(percent)
    fun getBooksAbovePercentage(percent: Int) = bookDao.getBooksAbovePercentage(percent)
    fun getBooksByAuthor(author: String) = bookDao.getBooksByAuthor("%$author%")
    fun getBooksByTitle(title: String) = bookDao.getBooksByTitle("%$title%")
}