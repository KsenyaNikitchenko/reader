package com.eodev.reader.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eodev.reader.data.model.BookRating
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun RatingItem(rating: BookRating) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    Icon(
                        imageVector = if (i <= rating.rating) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (i <= rating.rating) Color.Yellow else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(rating.date),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (rating.review.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = rating.review,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}


@Composable
fun RatingDialog(
    onDismiss: () -> Unit,
    onSave: (Int, String) -> Unit
) {
    var rating by remember { mutableStateOf(4) }
    var review by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Оцените книгу") },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (i in 1..5) {
                        IconButton(onClick = { rating = i }) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Filled.Star else Icons.Outlined.Star,
                                contentDescription = "Оценка $i",
                                tint = if (i <= rating) Color.Yellow else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = review,
                    onValueChange = { review = it },
                    label = { Text("Ваш отзыв") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (review.isNotBlank()){
                        onSave(rating, review)
                    } else{
                        onSave(rating, "")
                    }
                },
                enabled = rating in 1..5
            ) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text("Отмена")
            }
        }
    )
}