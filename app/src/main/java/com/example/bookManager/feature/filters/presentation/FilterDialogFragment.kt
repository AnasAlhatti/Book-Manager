package com.example.bookManager.feature.filters.presentation

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.bookManager.R

class FilterDialogFragment(
    private val onFilterApplied: (FilterType, String?) -> Unit,
    private val onClearFilter: () -> Unit
) : DialogFragment() {

    enum class FilterType {
        FINISHED, UNDER_PERCENTAGE, ABOVE_PERCENTAGE, AUTHOR, TITLE
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.dialog_filter_book, null)

        val spinner = view.findViewById<Spinner>(R.id.spinnerFilterType)
        val valueInput = view.findViewById<EditText>(R.id.editTextFilterValue)
        val applyButton = view.findViewById<Button>(R.id.buttonApply)
        val cancelButton = view.findViewById<Button>(R.id.buttonCancel)
        val clearButton = view.findViewById<Button>(R.id.buttonClear)

        val options = listOf(
            "Finished books",
            "Books under percentage",
            "Books above percentage",
            "Books by author",
            "Books by title"
        )

        spinner.adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, options)

        applyButton.setOnClickListener {
            val selected = spinner.selectedItem.toString()
            val value = valueInput.text.toString().trim()

            val type = when (selected) {
                "Finished books" -> FilterType.FINISHED
                "Books under percentage" -> FilterType.UNDER_PERCENTAGE
                "Books above percentage" -> FilterType.ABOVE_PERCENTAGE
                "Books by author" -> FilterType.AUTHOR
                "Books by title" -> FilterType.TITLE
                else -> null
            }

            if (type == null || (type != FilterType.FINISHED && value.isEmpty())) {
                Toast.makeText(context, "Please enter filter value", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            onFilterApplied(type, value)
            dismiss()
        }

        clearButton.setOnClickListener {
            onClearFilter()
            dismiss()
        }

        cancelButton.setOnClickListener {
            dismiss()
        }

        builder.setView(view)
        return builder.create()
    }
}