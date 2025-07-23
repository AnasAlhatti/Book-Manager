package com.example.BookManager

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface BookDao {
    @Query("SELECT * FROM book_table ORDER BY id DESC")
    fun getAllBooks(): LiveData<List<Book>>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(book: Book)

    @Update
    suspend fun update(book: Book)

    @Delete
    suspend fun delete(book: Book)

    @Query("SELECT * FROM book_table WHERE (pagesRead * 1.0 / totalPages) * 100 = 100")
    fun getFinishedBooks(): LiveData<List<Book>>

    @Query("SELECT * FROM book_table WHERE (pagesRead * 1.0 / totalPages) * 100 < 20")
    fun getBooksUnder20(): LiveData<List<Book>>

    @Query("SELECT * FROM book_table WHERE (pagesRead * 1.0 / totalPages) * 100 < 50")
    fun getBooksUnder50(): LiveData<List<Book>>

    @Query("SELECT * FROM book_table WHERE (pagesRead * 1.0 / totalPages) * 100 >= 50")
    fun getBooksAbove50(): LiveData<List<Book>>

    @Query("SELECT * FROM book_table WHERE (pagesRead * 1.0 / totalPages) * 100 < :percentage")
    fun getBooksUnderPercentage(percentage: Int): LiveData<List<Book>>

    @Query("SELECT * FROM book_table WHERE (pagesRead * 1.0 / totalPages) * 100 >= :percentage")
    fun getBooksAbovePercentage(percentage: Int): LiveData<List<Book>>

    @Query("SELECT * FROM book_table WHERE LOWER(author) LIKE LOWER(:author)")
    fun getBooksByAuthor(author: String): LiveData<List<Book>>

    @Query("SELECT * FROM book_table WHERE LOWER(title) LIKE LOWER(:title)")
    fun getBooksByTitle(title: String): LiveData<List<Book>>
}