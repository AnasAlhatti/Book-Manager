package com.example.BookManager

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var bookViewModel: BookViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var bookAdapter: BookAdapter
    private lateinit var filterPrefs: FilterPreferences
    private fun applySavedFilter() {
        val (type, value) = filterPrefs.getFilter()
        if (type != null) {
            when (type) {
                "FINISHED" -> bookViewModel.getFinishedBooks().observe(this) {
                    bookAdapter.setBooks(it)
                }
                "UNDER" -> bookViewModel.getBooksUnderPercentage(value!!.toInt()).observe(this) {
                    bookAdapter.setBooks(it)
                }
                "ABOVE" -> bookViewModel.getBooksAbovePercentage(value!!.toInt()).observe(this) {
                    bookAdapter.setBooks(it)
                }
                "AUTHOR" -> bookViewModel.getBooksByAuthor(value!!).observe(this) {
                    bookAdapter.setBooks(it)
                }
                "TITLE" -> bookViewModel.getBooksByTitle(value!!).observe(this) {
                    bookAdapter.setBooks(it)
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)
        filterPrefs = FilterPreferences(this)
        applySavedFilter()
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        bookAdapter = BookAdapter(
            onEditClick = { book -> showEditDialog(book) },
            onDeleteClick = { book -> bookViewModel.delete(book) }
        )
        recyclerView.adapter = bookAdapter

        bookViewModel.allBooks.observe(this) { books ->
            bookAdapter.setBooks(books)
        }

        val addButton: FloatingActionButton = findViewById(R.id.fabAdd)
        addButton.setOnClickListener { showAddDialog() }

        val filterButton: FloatingActionButton = findViewById(R.id.fabFilter)
        filterButton.setOnClickListener {
            val dialog = FilterDialogFragment(
                onFilterApplied = { type, value ->
                    when (type) {
                        FilterDialogFragment.FilterType.FINISHED -> {
                            filterPrefs.saveFilter("FINISHED", null)
                            bookViewModel.getFinishedBooks().observe(this) {
                                bookAdapter.setBooks(it)
                            }
                        }
                        FilterDialogFragment.FilterType.UNDER_PERCENTAGE -> {
                            filterPrefs.saveFilter("UNDER", value)
                            bookViewModel.getBooksUnderPercentage(value!!.toInt()).observe(this) {
                                bookAdapter.setBooks(it)
                            }
                        }
                        FilterDialogFragment.FilterType.ABOVE_PERCENTAGE -> {
                            filterPrefs.saveFilter("ABOVE", value)
                            bookViewModel.getBooksAbovePercentage(value!!.toInt()).observe(this) {
                                bookAdapter.setBooks(it)
                            }
                        }
                        FilterDialogFragment.FilterType.AUTHOR -> {
                            filterPrefs.saveFilter("AUTHOR", value)
                            bookViewModel.getBooksByAuthor(value!!).observe(this) {
                                bookAdapter.setBooks(it)
                            }
                        }
                        FilterDialogFragment.FilterType.TITLE -> {
                            filterPrefs.saveFilter("TITLE", value)
                            bookViewModel.getBooksByTitle(value!!).observe(this) {
                                bookAdapter.setBooks(it)
                            }
                        }
                    }
                },
                onClearFilter = {
                    filterPrefs.clearFilter()
                    bookViewModel.allBooks.observe(this) {
                        bookAdapter.setBooks(it)
                    }
                }
            )
            dialog.show(supportFragmentManager, "FilterDialog")
        }
    }

    private fun showAddDialog() {
        val dialog = AddEditBookDialogFragment.newInstance(null)
        dialog.show(supportFragmentManager, "AddBookDialog")
    }

    private fun showEditDialog(book: Book) {
        val dialog = AddEditBookDialogFragment.newInstance(book)
        dialog.show(supportFragmentManager, "EditBookDialog")

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}