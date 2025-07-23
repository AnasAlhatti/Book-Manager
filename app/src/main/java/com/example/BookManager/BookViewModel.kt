package com.example.BookManager

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class BookViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BookRepository
    val allBooks: LiveData<List<Book>>

    init {
        val dao = BookDatabase.getDatabase(application).bookDao()
        repository = BookRepository(dao)
        allBooks = repository.allBooks
    }

    fun insert(book: Book) = viewModelScope.launch {
        repository.insert(book)
    }

    fun update(book: Book) = viewModelScope.launch {
        repository.update(book)
    }

    fun delete(book: Book) = viewModelScope.launch {
        repository.delete(book)
    }

    fun getFinishedBooks() = repository.getFinishedBooks()
    fun getBooksUnder20() = repository.getBooksUnder20()
    fun getBooksUnder50() = repository.getBooksUnder50()
    fun getBooksAbove50() = repository.getBooksAbove50()
    fun getBooksUnderPercentage(percent: Int) = repository.getBooksUnderPercentage(percent)
    fun getBooksAbovePercentage(percent: Int) = repository.getBooksAbovePercentage(percent)
    fun getBooksByAuthor(author: String) = repository.getBooksByAuthor(author)
    fun getBooksByTitle(title: String) = repository.getBooksByTitle(title)

}
