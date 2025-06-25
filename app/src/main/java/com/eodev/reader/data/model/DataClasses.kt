package com.eodev.reader.data.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import com.eodev.reader.reader.EpubChapter
import com.eodev.reader.reader.EpubContent
import java.io.InputStream
import java.time.LocalDate
import java.util.Arrays
import java.util.Date

data class BookWithDetails(
    @Embedded val book: Book,
    @ColumnInfo(name = "authorName")
    val authorName: String,
    @ColumnInfo(name = "seriesName")
    val seriesName: String?
)

data class AuthorWithCount(
    val author: String,
    val count: Int
)

data class SeriesWithCount(
    val series: String,
    val count: Int
)
data class SeriesWithAuthor(
    @Embedded val series: Series,
    val authorName: String
)
data class MonthlyBook(
    val title: String,
    val author: String,
    val rating: Int,
    val date: Date
)

data class YearlyStatistic(
    val month: Int, // номер месяца
    val count: Int
)

data class RatingStatistic(
    val rating: Int, // оценка по пятибалльной шкале
    val count: Int
)

data class ReadingState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val content: EpubContent? = null,
    val currentChapter: Int = 0
) {
    val hasContent: Boolean get() = content != null && content.chapters.isNotEmpty()
    val currentChapterData: EpubChapter? get() = content?.getChapter(currentChapter)
    val progress: Float get() = content?.getCurrentProgress(currentChapter) ?: 0f
}

data class Fb2Cover(
    val mime: String,
    val href: String,
    val binaryData: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Fb2Cover

        if (mime != other.mime) return false
        if (href != other.href) return false
        if (!Arrays.equals(binaryData, other.binaryData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = mime.hashCode()
        result = 31 * result + href.hashCode()
        result = 31 * result + Arrays.hashCode(binaryData)
        return result
    }
}