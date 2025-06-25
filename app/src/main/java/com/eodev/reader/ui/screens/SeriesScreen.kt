package com.eodev.reader.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eodev.reader.LocalNavigation
import com.eodev.reader.data.model.Book
import com.eodev.reader.data.model.BookWithDetails
import com.eodev.reader.ui.components.DrawerContent
import com.eodev.reader.ui.components.GenericBooksScreen
import com.eodev.reader.ui.components.GenericListScreen
import com.eodev.reader.ui.navigation.Screen
import com.eodev.reader.ui.viewmodel.BookViewModel
import kotlinx.coroutines.launch

//SeriesScreen.kt
@Composable
fun SeriesScreen(
    viewModel: BookViewModel,
    onSeriesClick: (String) -> Unit,
){
    val series by viewModel.seriesWithCount.collectAsStateWithLifecycle()
    val navController = LocalNavigation.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    GenericListScreen(
        title = "Все серии",
        items = series,
        viewModel = viewModel,
        onItemClick = {series -> onSeriesClick(series.series)},
        itemContent = { series ->
            Row (
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = "Серия",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = series.series,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${series.count}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

        },
        drawerContent = {
            DrawerContent(
                viewModel = viewModel,
                onMenuItemClick = { menuItem ->
                    when (menuItem) {
                        "Все книги" -> {
                            viewModel.loadAllBooks()
                            navController.navigate(Screen.MAIN.name) {
                                popUpTo(Screen.MAIN.name) { inclusive = true }
                            }
                        }
                        "Прочитанное" -> {
                            viewModel.loadReadBooks()
                            navController.navigate(Screen.MAIN.name) {
                                popUpTo(Screen.MAIN.name) { inclusive = true }
                            }
                        }
                        "Читаю сейчас" -> {
                            viewModel.loadCurrentlyReadingBooks()
                            navController.navigate(Screen.MAIN.name) {
                                popUpTo(Screen.MAIN.name) { inclusive = true }
                            }
                        }
                    }
                    scope.launch { drawerState.close() }
                },
                onAllAuthorsClick = { navController.navigate(Screen.AUTHORS.name) },
                onAllSeriesClick = { scope.launch { drawerState.close() } },
                onStatisticsClick = { navController.navigate(Screen.STATISTICS.name) }
            )
        }
    )
}

// SeriesBooksScreen.kt
@Composable
fun SeriesBooksScreen(
    series: String,
    viewModel: BookViewModel,
    onBackClick: () -> Unit,
    onBookClick: (BookWithDetails) -> Unit
) {

    GenericBooksScreen(
        title = series,
        viewModel=viewModel,
        onBackClick = onBackClick,
        onBookClick= onBookClick,
        loadBooks = { viewModel.loadBooksBySeries(series)},
        emptyMessage = "Книги из этой серии отсутствуют в бибилиотеке"
    )
}