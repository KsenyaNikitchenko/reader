package com.eodev.reader.ui.components

import android.net.Uri
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eodev.reader.data.model.BookWithDetails


@Composable
fun BookItem(
    book: BookWithDetails,
    onItemClick: () -> Unit,
    onReadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable (
                interactionSource = remember { MutableInteractionSource() },
                indication = LocalIndication.current,
                onClick = onItemClick ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookCover(
                coverPath = book.book.coverImagePath,
                modifier = Modifier.size(80.dp, 120.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = book.book.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = book.authorName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!book.seriesName.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Серия: ${book.seriesName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (book.book.readProgressPercent > 0) {
                        Column {
                            Text(
                                text = "Прогресс: ${(book.book.readProgressPercent * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LinearProgressIndicator(
                                progress = book.book.readProgressPercent,
                                modifier = Modifier
                                    .width(100.dp)
                                    .height(4.dp)
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }
                    Button(
                        onClick = onReadClick,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Text(
                            text = if (book.book.readProgressPercent > 0) "Продолжить" else "Читать",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BookCover(
    coverPath: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(8.dp)
            )
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (coverPath != null) {
            AsyncImage(
                model = Uri.parse(coverPath),
                contentDescription = "Обложка книги",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = "📖",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}