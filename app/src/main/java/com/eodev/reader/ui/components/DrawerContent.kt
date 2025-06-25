package com.eodev.reader.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eodev.reader.ui.viewmodel.BookViewModel

@Composable
fun DrawerContent(
    viewModel: BookViewModel,
    onMenuItemClick: (String) -> Unit,
    onAllAuthorsClick: () -> Unit,
    onAllSeriesClick: () -> Unit,
    onStatisticsClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text(
                text = "Библиотека",
                style = typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        item {
            DrawerMenuItem(
                icon = Icons.Default.Sort,
                text = "Все книги",
                onClick = { onMenuItemClick("Все книги") }
            )
        }

        item {
            DrawerMenuItem(
                icon = Icons.Default.DoneAll,
                text = "Прочитанное",
                onClick = { onMenuItemClick("Прочитанное") }
            )
        }

        item {
            DrawerMenuItem(
                icon = Icons.Default.Schedule,
                text = "Читаю сейчас",
                onClick = { onMenuItemClick("Читаю сейчас") }
            )
        }

        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        item {
            DrawerMenuItem(
                icon = Icons.Default.People,
                text = "Авторы",
                onClick = { onAllAuthorsClick()}
            )
        }
        item {
            DrawerMenuItem(
                icon = Icons.Default.Bookmarks,
                text = "Серии",
                onClick = { onAllSeriesClick()}
            )
        }

        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
        item{
            DrawerMenuItem(
                icon= Icons.Default.AlignVerticalBottom,
                text="Статистика",
                onClick = onStatisticsClick
            )
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    isSubItem: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(
                horizontal = if (isSubItem) 16.dp else 0.dp,
                vertical = 12.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = if (isSubItem)
                MaterialTheme.colorScheme.onSurfaceVariant
            else
                MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = text,
            style = if (isSubItem)
                typography.bodyMedium
            else
                typography.bodyLarge,
            color = if (isSubItem)
                MaterialTheme.colorScheme.onSurfaceVariant
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}