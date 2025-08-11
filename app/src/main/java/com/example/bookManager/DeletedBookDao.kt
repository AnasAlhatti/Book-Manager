package com.example.bookManager

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeletedBookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: DeletedBook)

    @Query("SELECT * FROM deleted_books")
    suspend fun getAll(): List<DeletedBook>

    @Query("DELETE FROM deleted_books")
    suspend fun clear()
}

