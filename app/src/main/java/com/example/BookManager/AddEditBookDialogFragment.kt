package com.example.BookManager

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels

class AddEditBookDialogFragment : DialogFragment() {

    private lateinit var titleEditText: EditText
    private lateinit var authorEditText: EditText
    private lateinit var pagesEditText: EditText
    private lateinit var totalPagesEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button

    private var existingBook: Book? = null

    private val bookViewModel: BookViewModel by activityViewModels()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext(), com.google.android.material.R.style.Theme_MaterialComponents_Dialog_Alert)
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_add_edit_book, null)

        titleEditText = view.findViewById(R.id.editTextTitle)
        authorEditText = view.findViewById(R.id.editTextAuthor)
        pagesEditText = view.findViewById(R.id.editTextPages)
        totalPagesEditText = view.findViewById(R.id.editTotalPages)
        saveButton = view.findViewById(R.id.buttonSave)
        cancelButton = view.findViewById(R.id.buttonCancel)

        arguments?.let {
            existingBook = it.getParcelable(ARG_BOOK)
            existingBook?.let { book ->
                titleEditText.setText(book.title)
                authorEditText.setText(book.author)
                pagesEditText.setText(book.pagesRead.toString())
                totalPagesEditText.setText(book.totalPages.toString())
            }
        }

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val author = authorEditText.text.toString().trim()
            val pages = pagesEditText.text.toString().toIntOrNull()
            val totalPages = totalPagesEditText.text.toString().toIntOrNull()

            if (title.isEmpty() || author.isEmpty() || pages == null || pages <= 0 || totalPages == null || totalPages <= 0) {
                Toast.makeText(requireContext(), "Please enter valid data", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (pages > totalPages) {
                Toast.makeText(requireContext(), "Pages read cannot exceed total pages", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val book = existingBook?.copy(pagesRead = pages, title = title, author = author, totalPages = totalPages)
                ?: Book(title = title, author = author, pagesRead = pages, totalPages = totalPages ?: 0)

            if (existingBook == null) {
                bookViewModel.insert(book)
            } else {
                bookViewModel.update(book)
            }

            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        builder.setView(view)
        return builder.create()
    }

    companion object {
        private const val ARG_BOOK = "book_arg"

        fun newInstance(book: Book?): AddEditBookDialogFragment {
            val fragment = AddEditBookDialogFragment()
            val args = Bundle()
            args.putParcelable(ARG_BOOK, book)
            fragment.arguments = args
            return fragment
        }
    }
}
