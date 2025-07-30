package com.example.BookManager

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_books")
data class DeletedBook(
    @PrimaryKey val uuid: String
)

