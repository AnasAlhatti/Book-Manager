package com.example.BookManager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.UUID

class BookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookRepository
    val allBooks: LiveData<List<Book>>
    private val cloudRepo = FirestoreBookRepository()

    init {
        val db = BookDatabase.getDatabase(application)
        val dao = db.bookDao()
        val deletedDao = db.deletedBookDao()

        // Firebase safe: only pass remote=true if user is signed in
        val useCloud = FirebaseAuth.getInstance().currentUser != null
        repository = BookRepository(dao, deletedDao, application.applicationContext, useCloud)

        allBooks = repository.allBooks
    }

    fun insert(book: Book) {
        viewModelScope.launch {
            // Ensure UUID is set before any operations
            if (book.uuid.isBlank()) {
                book.uuid = UUID.randomUUID().toString()
            }

            repository.insert(book)

            // Only sync to cloud if user is authenticated (not anonymous)
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && !currentUser.isAnonymous) {
                cloudRepo.uploadBook(book)
            }
        }
    }

    fun update(book: Book) {
        viewModelScope.launch {
            // Ensure UUID is set
            if (book.uuid.isBlank()) {
                book.uuid = UUID.randomUUID().toString()
            }

            repository.update(book)

            // Only sync to cloud if user is authenticated (not anonymous)
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && !currentUser.isAnonymous) {
                cloudRepo.uploadBook(book)
            }
        }
    }

    fun delete(book: Book) {
        viewModelScope.launch {
            repository.delete(book)

            // Only sync to cloud if user is authenticated (not anonymous)
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && !currentUser.isAnonymous) {
                cloudRepo.deleteBook(book)
            }
        }
    }

    fun deleteAll() {
        viewModelScope.launch {
            repository.deleteAll()

            // Only clear cloud if user is authenticated (not anonymous)
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null && !currentUser.isAnonymous) {
                cloudRepo.clearCloudData()
            }
        }
    }

    fun syncWithCloud(onComplete: () -> Unit = {}) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser.isAnonymous) {
            onComplete()
            return
        }

        viewModelScope.launch {
            try {
                val deletedUuids = repository.getDeletedUuids()

                // 1. Delete from Firestore first
                deletedUuids.forEach { uuid ->
                    cloudRepo.deleteByUuid(uuid)
                }

                // 2. Upload local books to cloud
                val localBooks = repository.getAllBooksOnce()
                localBooks.forEach { book ->
                    if (book.uuid.isBlank()) {
                        book.uuid = UUID.randomUUID().toString()
                        repository.update(book) // Update local with UUID
                    }
                    cloudRepo.uploadBook(book)
                }

                // 3. Download and merge cloud books
                cloudRepo.fetchAllBooks { cloudBooks ->
                    viewModelScope.launch {
                        try {
                            for (cloudBook in cloudBooks) {
                                if (cloudBook.uuid.isBlank()) {
                                    cloudBook.uuid = UUID.randomUUID().toString()
                                }

                                val existingBook = repository.getBookByUuid(cloudBook.uuid)
                                if (existingBook == null) {
                                    repository.insert(cloudBook)
                                } else {
                                    // Update existing book with cloud data
                                    repository.update(cloudBook)
                                }
                            }

                            // Clear deleted books table after successful sync
                            repository.clearDeletedUuids()
                            onComplete()
                        } catch (e: Exception) {
                            e.printStackTrace()
                            onComplete()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete()
            }
        }
    }

    fun uploadToCloud(book: Book) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null && !currentUser.isAnonymous) {
            if (book.uuid.isBlank()) {
                book.uuid = UUID.randomUUID().toString()
            }
            cloudRepo.uploadBook(book)
        }
    }

    fun getFinishedBooks() = repository.getFinishedBooks()
    fun getBooksUnderPercentage(percent: Int) = repository.getBooksUnderPercentage(percent)
    fun getBooksAbovePercentage(percent: Int) = repository.getBooksAbovePercentage(percent)
    fun getBooksByAuthor(author: String) = repository.getBooksByAuthor(author)
    fun getBooksByTitle(title: String) = repository.getBooksByTitle(title)

    val repositoryAccess get() = repository
}