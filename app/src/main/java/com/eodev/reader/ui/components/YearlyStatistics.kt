package com.eodev.reader.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import java.time.format.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eodev.reader.data.model.RatingStatistic
import com.eodev.reader.data.model.YearlyStatistic
import java.time.Month
import java.util.Locale

@Composable
fun YearlyStatistics(
    yearlyStats: List<YearlyStatistic>,
    ratingStats: List<RatingStatistic>,
    year: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Статистика за $year год",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Диаграмма по месяцам
        Text(
            text = "Книги по месяцам",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(16.dp))
        MonthlyBarChart(yearlyStats = yearlyStats)

        Spacer(modifier = Modifier.height(24.dp))

        // Статистика по рейтингам
        Text(
            text = "Распределение по оценкам",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        RatingDistribution(ratingStats = ratingStats)
    }

}

@Composable
fun MonthlyBarChart(yearlyStats: List<YearlyStatistic>) {
    val maxBooks = yearlyStats.maxOfOrNull { it.count } ?: 1
    val buttonColor = Color(0xFF009688)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(top = 16.dp, bottom = 16.dp)) {
            val barWidth = size.width / 12
            val heightPerBook = (size.height - 50f)/ maxBooks

            yearlyStats.forEach { stat ->
                val barHeight = stat.count * heightPerBook
                val left = barWidth * (stat.month - 1)
                val centerX = left + barWidth / 2

                drawRect(
                    color = buttonColor,
                    topLeft = Offset(left + 4f, size.height - barHeight - 30f),
                    size = Size(barWidth - 8f, barHeight)
                )
                drawContext.canvas.nativeCanvas.apply {
                    val text = stat.count.toString()
                    val textPaint = Paint().apply {
                        textSize = 32f // Увеличенный размер
                        color = android.graphics.Color.BLACK
                        isFakeBoldText = true // Жирный текст
                    }
                    val textWidth = textPaint.measureText(text)

                    drawText(
                        text,
                        centerX - textWidth / 2,
                        size.height - barHeight - 42f, // Позиция над столбцом
                        textPaint
                    )
                }
                // Подписи месяцев
                drawContext.canvas.nativeCanvas.apply {
                    val monthName = Month.of(stat.month).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                    val monthPaint = Paint().apply {
                        textSize = 32f
                        color = android.graphics.Color.BLACK
                        isFakeBoldText = true
                    }
                    val monthTextWidth = monthPaint.measureText(monthName)

                    drawText(
                        monthName,
                        centerX - monthTextWidth / 2,
                        size.height + 10f,
                        monthPaint
                    )
                }
            }
        }
    }
}

@Composable
fun RatingDistribution(ratingStats: List<RatingStatistic>) {
    val total = ratingStats.sumOf { it.count }.toFloat()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ratingStats.sortedBy { it.rating }.forEach { stat ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                StarRating(rating = stat.rating)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "${stat.count} (${(stat.count / total * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(16.dp))
                LinearProgressIndicator(
                    progress = if (total > 0) stat.count / total else 0f,
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp),
                    color = when (stat.rating) {
                        5 -> Color(0xFF009688)
                        4 -> Color(0xFF8BC34A)
                        3 -> Color(0xFFFFEB3B)
                        2 ->  Color(0xFFFFC107)
                        else -> Color(0xFFFF5722)
                    }
                )
            }
        }
    }
}