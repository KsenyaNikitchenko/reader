package com.eodev.reader.ui.screens

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eodev.reader.LocalNavigation
import kotlinx.coroutines.launch
import com.eodev.reader.data.model.Book
import com.eodev.reader.data.model.BookWithDetails
import com.eodev.reader.ui.components.BookItem
import com.eodev.reader.ui.components.DrawerContent
import com.eodev.reader.ui.navigation.Screen
import com.eodev.reader.ui.viewmodel.BookViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookListScreen(
    viewModel: BookViewModel,
    onBookClick: (BookWithDetails) -> Unit,
    onReadBook: (BookWithDetails) -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val screenTitle by viewModel.screenTitle.collectAsStateWithLifecycle()
    val books by viewModel.currentBooks.collectAsStateWithLifecycle()
    val navController=LocalNavigation.current

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.addBooksFromUris(uris)
        }
    }
    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            viewModel.clearMessage()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            viewModel.clearError()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                DrawerContent(
                    viewModel = viewModel,
                    onMenuItemClick = { menuItem ->
                        when (menuItem) {
                            "Все книги" -> viewModel.loadAllBooks()
                            "Прочитанное" -> viewModel.loadReadBooks()
                            "Читаю сейчас" -> viewModel.loadCurrentlyReadingBooks()
                        }
                        scope.launch { drawerState.close() }
                    },
                    onAllAuthorsClick = {
                        navController.navigate(Screen.AUTHORS.name)
                        scope.launch { drawerState.close() }
                    },
                    onAllSeriesClick = {
                        navController.navigate(Screen.SERIES.name)
                        scope.launch { drawerState.close() }
                    },
                    onStatisticsClick = {
                        navController.navigate(Screen.STATISTICS.name)
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(screenTitle) },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Меню")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        filePickerLauncher.launch(arrayOf("*/*"))
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить книги")
                }
            }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(books) { book ->
                            BookItem(
                                book = book,
                                onItemClick = { onBookClick(book) },
                                onReadClick = { onReadBook(book) }
                            )
                        }
                    }
                }
            }
        }
    }
}