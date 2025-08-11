package com.example.bookManager

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_books")
data class DeletedBook(
    @PrimaryKey val uuid: String
)

