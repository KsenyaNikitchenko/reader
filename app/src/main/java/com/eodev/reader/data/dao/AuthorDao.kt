package com.eodev.reader.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.eodev.reader.data.model.Author
import kotlinx.coroutines.flow.Flow

@Dao
interface AuthorDao {
    @Insert
    suspend fun insert(author: Author): Long

    @Update
    suspend fun update(author: Author)

    @Delete
    suspend fun delete(author: Author)

    @Query("SELECT * FROM authors WHERE id = :authorId")
    suspend fun getAuthorById(authorId: Long): Author?

    @Query("SELECT * FROM authors WHERE name = :name LIMIT 1")
    suspend fun getAuthorByName(name: String): Author?

    @Query("SELECT * FROM authors ORDER BY name ASC")
    fun getAllAuthors(): Flow<List<Author>>

    @Query("SELECT COUNT(*) FROM authors")
    suspend fun getAuthorsCount(): Int
}