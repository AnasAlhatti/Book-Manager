package com.example.bookManager.core.data.repo

import android.util.Log
import androidx.lifecycle.LiveData
import com.example.bookManager.core.data.local.BookDao
import com.example.bookManager.core.data.local.DeletedBookDao
import com.example.bookManager.core.model.Book
import com.example.bookManager.core.model.DeletedBook
import java.util.UUID

class BookRepositoryImpl(
    private val bookDao: BookDao,
    private val deletedBookDao: DeletedBookDao
) : BookRepository {

    override val allBooks: LiveData<List<Book>> = bookDao.getAllBooks()

    override suspend fun insert(book: Book) {
        try {
            if (book.uuid.isBlank()) {
                book.uuid = UUID.randomUUID().toString()
                Log.d("BookRepository", "Generated UUID for new book: ${book.uuid}")
            }
            bookDao.insert(book)
            Log.d("BookRepository", "Book inserted locally: ${book.uuid}")
        } catch (e: Exception) {
            Log.e("BookRepository", "Error inserting book", e)
            throw e
        }
    }

    override suspend fun update(book: Book) {
        try {
            if (book.uuid.isBlank()) {
                book.uuid = UUID.randomUUID().toString()
                Log.d("BookRepository", "Generated UUID for existing book: ${book.uuid}")
            }
            bookDao.update(book)
            Log.d("BookRepository", "Book updated locally: ${book.uuid}")
        } catch (e: Exception) {
            Log.e("BookRepository", "Error updating book", e)
            throw e
        }
    }

    override suspend fun delete(book: Book) {
        try {
            bookDao.delete(book)
            if (book.uuid.isNotBlank()) {
                deletedBookDao.insert(DeletedBook(book.uuid))
                Log.d("BookRepository", "Book deleted and tracked: ${book.uuid}")
            } else {
                Log.w("BookRepository", "Deleted book has no UUID, cannot track for cloud sync")
            }
        } catch (e: Exception) {
            Log.e("BookRepository", "Error deleting book", e)
            throw e
        }
    }

    override suspend fun deleteAll() {
        try {
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

    override suspend fun getDeletedUuids(): List<String> {
        return try {
            deletedBookDao.getAll().map { it.uuid }
        } catch (e: Exception) {
            Log.e("BookRepository", "Error getting deleted UUIDs", e)
            emptyList()
        }
    }

    override suspend fun clearDeletedUuids() {
        try {
            deletedBookDao.clear()
            Log.d("BookRepository", "Deleted UUIDs cleared")
        } catch (e: Exception) {
            Log.e("BookRepository", "Error clearing deleted UUIDs", e)
        }
    }

    override suspend fun getAllBooksOnce(): List<Book> {
        return try {
            bookDao.getAllBooksNow()
        } catch (e: Exception) {
            Log.e("BookRepository", "Error getting all books", e)
            emptyList()
        }
    }

    override suspend fun getBookByUuid(uuid: String): Book? {
        return try {
            bookDao.getBookByUuid(uuid)
        } catch (e: Exception) {
            Log.e("BookRepository", "Error getting book by UUID: $uuid", e)
            null
        }
    }

    override fun getFinishedBooks() = bookDao.getFinishedBooks()

    override fun getBooksUnderPercentage(percent: Int) = bookDao.getBooksUnderPercentage(percent)

    override fun getBooksAbovePercentage(percent: Int) = bookDao.getBooksAbovePercentage(percent)

    override fun getBooksByAuthor(author: String) = bookDao.getBooksByAuthor("%$author%")

    override fun getBooksByTitle(title: String) = bookDao.getBooksByTitle("%$title%")
}
