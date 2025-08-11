package com.example.bookManager

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "book_table")
data class Book(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var uuid: String = "",  // <-- NEW: unique identifier
    var title: String = "",
    var author: String = "",
    var pagesRead: Int = 0,
    var totalPages: Int = 0
) : Parcelable {
    constructor() : this(0, "", "", "", 0, 0)
}
