package com.example.bookManager.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.bookManager.core.model.DeletedBook

@Dao
interface DeletedBookDao {

    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insert(book: DeletedBook)

    @Query("SELECT * FROM deleted_books")
    suspend fun getAll(): List<DeletedBook>

    @Query("DELETE FROM deleted_books")
    suspend fun clear()
}