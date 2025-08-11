package com.example.bookManager

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class BookAdapter(
    private val onEditClick: (Book) -> Unit,
    private val onDeleteClick: (Book) -> Unit
) : RecyclerView.Adapter<BookAdapter.BookViewHolder>() {

    private var books: List<Book> = listOf()

    inner class BookViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.titleText)
        val authorText: TextView = itemView.findViewById(R.id.authorText)
        val progressText: TextView = itemView.findViewById(R.id.progressText)
        val pagesRead: TextView = itemView.findViewById(R.id.pagesRead)
        val totalPages: TextView = itemView.findViewById(R.id.totalPages)
        val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)
        val editButton: ImageButton = itemView.findViewById(R.id.editButton)
        val progressBar: ProgressBar     = itemView.findViewById(R.id.progressBar)
        val checkmark: ImageView = itemView.findViewById(R.id.checkmark)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        return BookViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookViewHolder, position: Int) {
        val book = books[position]
        holder.titleText.text = book.title
        holder.authorText.text = "by ${book.author}"
        val progress = if (book.totalPages <= 0 || book.pagesRead < 0 || book.pagesRead > book.totalPages) {
            0
        } else {
            (book.pagesRead.toFloat() / book.totalPages * 100).toInt()
        }
        holder.progressText.text = "Progress: $progress%"
        holder.progressBar.progress = progress
        val context = holder.itemView.context
        val color = when {
            progress >= 100 -> android.R.color.holo_green_dark
            progress >= 50 -> android.R.color.holo_orange_light
            else -> android.R.color.holo_red_light
        }
        holder.progressBar.progressTintList = ContextCompat.getColorStateList(context, color)
        holder.checkmark.visibility = if (progress >= 100) View.VISIBLE else View.GONE
        holder.pagesRead.text = "Read Pages: ${book.pagesRead}"
        holder.totalPages.text = "Total Pages: ${book.totalPages}"
        holder.deleteButton.setOnClickListener { onDeleteClick(book) }
        holder.editButton.setOnClickListener { onEditClick(book) }
    }

    override fun getItemCount() = books.size

    fun setBooks(bookList: List<Book>) {
        books = bookList
        notifyDataSetChanged()
    }
}