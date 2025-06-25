package com.eodev.reader.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eodev.reader.data.model.MonthlyBook
import java.text.SimpleDateFormat
import java.time.Month
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

@Composable
fun MonthlyStatistics(
    books: List<MonthlyBook>,
    month: Int,
    year: Int
) {
    val monthName = remember(month) {
        Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Статистика за $monthName $year",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn (
            modifier = Modifier.fillMaxWidth()
        ){
            items(books) { book ->
                BookRatingItem(
                    title = book.title,
                    author = book.author,
                    rating = book.rating,
                    date = book.date
                )
            }
        }
    }
}

@Composable
fun BookRatingItem(
    title: String,
    author: String,
    rating: Int,
    date: Date,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column (
            modifier = Modifier.padding(16.dp).fillMaxWidth()
        ) {
            Text(
                text = "$title",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Автор: $author",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Дата прочтения: ${SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            StarRating(rating = rating)
        }
    }
}

@Composable
fun StarRating(rating: Int) {
    Row {
        repeat(5) { index ->
            Icon(
                imageVector = if (index < rating) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (index < rating) Color.Yellow else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}