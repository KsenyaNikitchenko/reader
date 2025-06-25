package com.eodev.reader.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.eodev.reader.data.model.Series
import com.eodev.reader.data.model.SeriesWithAuthor
import kotlinx.coroutines.flow.Flow

@Dao
interface SeriesDao {
    @Insert
    suspend fun insert(series: Series): Long

    @Update
    suspend fun update(series: Series)

    @Delete
    suspend fun delete(series: Series)

    @Query("SELECT * FROM series WHERE id = :seriesId")
    suspend fun getSeriesById(seriesId: Long): Series?

    @Query("SELECT * FROM series WHERE name = :name AND authorId = :authorId LIMIT 1")
    suspend fun getSeriesByNameAndAuthor(name: String, authorId: Long): Series?

    @Query("SELECT * FROM series WHERE authorId = :authorId ORDER BY name ASC")
    fun getSeriesByAuthor(authorId: Long): Flow<List<Series>>

    @Query("SELECT series.*, authors.name as authorName FROM series JOIN authors ON series.authorId = authors.id ORDER BY series.name ASC")
    fun getAllSeriesWithAuthors(): Flow<List<SeriesWithAuthor>>

    @Query("SELECT COUNT(*) FROM series")
    suspend fun getSeriesCount(): Int
}