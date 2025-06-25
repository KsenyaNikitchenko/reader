package com.eodev.reader.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.eodev.reader.ui.screens.AuthorBooksScreen
import com.eodev.reader.ui.screens.AuthorsScreen
import com.eodev.reader.ui.screens.BookListScreen
import com.eodev.reader.ui.screens.BookDetailsScreen
import com.eodev.reader.ui.screens.ReaderScreen
import com.eodev.reader.ui.screens.SeriesBooksScreen
import com.eodev.reader.ui.screens.SeriesScreen
import com.eodev.reader.ui.screens.StatisticsScreen
import com.eodev.reader.ui.viewmodel.BookViewModel

enum class Screen{
    MAIN, AUTHORS, SERIES, AUTHOR_BOOKS, SERIES_BOOKS, BOOK_DETAILS, READER, STATISTICS
}
@Composable
fun ReaderNavigation(
    navController: NavHostController,
    viewModel: BookViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.MAIN.name
    ) {
        //Главный экран
        composable(Screen.MAIN.name){
            BookListScreen (
                viewModel = viewModel,
                onBookClick = { book ->
                    navController.navigate("${Screen.BOOK_DETAILS.name}/${book.book.id}")
                },
                onReadBook = { book ->
                    navController.navigate("${Screen.READER.name}/${book.book.id}")
                }
            )
        }
        // Экран списка всех авторов
        composable(Screen.AUTHORS.name){
            AuthorsScreen(
                viewModel = viewModel,
                onAuthorClick = { author ->
                    navController.navigate("${Screen.AUTHOR_BOOKS.name}/$author")
                },
            )
        }
        // Экран списка всех серий
        composable(Screen.SERIES.name){
            SeriesScreen(
                viewModel = viewModel,
                onSeriesClick = { series ->
                    navController.navigate("${Screen.SERIES_BOOKS.name}/$series")
                },
            )
        }
        //Экран со статистикой
        composable(Screen.STATISTICS.name){
            StatisticsScreen(
                viewModel= viewModel
            )
        }
        // Экран книг конкретного автора
        composable(
            route = "${Screen.AUTHOR_BOOKS.name}/{author}",
            arguments = listOf(navArgument("author") { type = NavType.StringType })
        ) { backStackEntry ->
            val author = backStackEntry.arguments?.getString("author") ?: ""
            AuthorBooksScreen(
                author = author,
                viewModel = viewModel,
                onBackClick = { navController.navigate("${Screen.AUTHORS.name}") },
                onBookClick = { book ->
                    navController.navigate("${Screen.BOOK_DETAILS.name}/${book.book.id}")
                }
            )
        }
        //Экран книг конкретной серии
        composable(
            route = "${Screen.SERIES_BOOKS.name}/{series}",
            arguments = listOf(navArgument("series") { type = NavType.StringType })
        ) { backStackEntry ->
            val series = backStackEntry.arguments?.getString("series") ?: ""
            SeriesBooksScreen(
                series = series,
                viewModel = viewModel,
                onBackClick = { navController.navigate("${Screen.SERIES.name}") },
                onBookClick = { book ->
                    navController.navigate("${Screen.BOOK_DETAILS.name}/${book.book.id}")
                }
            )
        }
        //Экран с детальной информацией о книге
        composable(
            "${Screen.BOOK_DETAILS.name}/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: -1L
            BookDetailsScreen(
                bookId = bookId,
                viewModel = viewModel,
                onBackPressed = { navController.popBackStack() },
                onReadBook = {
                    navController.navigate("${Screen.READER.name}/$bookId")
                }
            )
        }
        //Экран читалки
        composable(
            "${Screen.READER.name}/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.LongType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getLong("bookId") ?: -1L
            ReaderScreen(
                bookId = bookId,
                viewModel = viewModel,
                onBackPressed = { navController.popBackStack() }
            )
        }
    }
}

