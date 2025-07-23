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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        bookViewModel = ViewModelProvider(this).get(BookViewModel::class.java)
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