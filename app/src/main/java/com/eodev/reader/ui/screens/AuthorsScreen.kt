package com.eodev.reader.ui.screens

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
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

//AuthorsScreen.kt
@Composable
fun AuthorsScreen(
    viewModel: BookViewModel,
    onAuthorClick: (String) -> Unit,
){
    val authors by viewModel.authorsWithCount.collectAsStateWithLifecycle()
    val navController= LocalNavigation.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    GenericListScreen(
        title = "Все авторы",
        items = authors,
        viewModel = viewModel,
        onItemClick = {author -> onAuthorClick(author.author)},
        itemContent = { author ->
            Row (
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ){
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Автор",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = author.author,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "${author.count}",
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
                onAllAuthorsClick = { scope.launch { drawerState.close() } },
                onAllSeriesClick = { navController.navigate(Screen.SERIES.name) },
                onStatisticsClick = { navController.navigate(Screen.STATISTICS.name) }
            )
        }
    )
}

// AuthorBooksScreen.kt
@Composable
fun AuthorBooksScreen(
    author: String,
    viewModel: BookViewModel,
    onBackClick: () -> Unit,
    onBookClick: (BookWithDetails) -> Unit
) {

    GenericBooksScreen(
        title = author,
        viewModel=viewModel,
        onBackClick = onBackClick,
        onBookClick= onBookClick,
        loadBooks = { viewModel.loadBooksByAuthor(author)},
        emptyMessage = "Книги этого автора отсутствуют в бибилиотеке"
    )
}