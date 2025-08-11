package com.example.bookManager.core.data.repo

import androidx.lifecycle.LiveData
import com.example.bookManager.core.model.Book

interface BookRepository {
    val allBooks: LiveData<List<Book>>

    suspend fun insert(book: Book)
    suspend fun update(book: Book)
    suspend fun delete(book: Book)
    suspend fun deleteAll()

    // helpers used by your ViewModel sync
    suspend fun getDeletedUuids(): List<String>
    suspend fun clearDeletedUuids()
    suspend fun getAllBooksOnce(): List<Book>
    suspend fun getBookByUuid(uuid: String): Book?

    // filters used by MainActivity
    fun getFinishedBooks(): LiveData<List<Book>>
    fun getBooksUnderPercentage(percent: Int): LiveData<List<Book>>
    fun getBooksAbovePercentage(percent: Int): LiveData<List<Book>>
    fun getBooksByAuthor(author: String): LiveData<List<Book>>
    fun getBooksByTitle(title: String): LiveData<List<Book>>
}
