package com.example.BookManager

import androidx.lifecycle.LiveData

class BookRepository(private val bookDao: BookDao) {

    val allBooks: LiveData<List<Book>> = bookDao.getAllBooks()

    suspend fun insert(book: Book) {
        bookDao.insert(book)
    }

    suspend fun update(book: Book) {
        bookDao.update(book)
    }

    suspend fun delete(book: Book) {
        bookDao.delete(book)
    }

    fun getFinishedBooks() = bookDao.getFinishedBooks()
    fun getBooksUnder20() = bookDao.getBooksUnder20()
    fun getBooksUnder50() = bookDao.getBooksUnder50()
    fun getBooksAbove50() = bookDao.getBooksAbove50()
    fun getBooksUnderPercentage(percent: Int) = bookDao.getBooksUnderPercentage(percent)
    fun getBooksAbovePercentage(percent: Int) = bookDao.getBooksAbovePercentage(percent)
    fun getBooksByAuthor(author: String) = bookDao.getBooksByAuthor("%$author%")
    fun getBooksByTitle(title: String) = bookDao.getBooksByTitle("%$title%")
}
