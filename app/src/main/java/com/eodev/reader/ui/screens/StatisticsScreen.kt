package com.eodev.reader.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.eodev.reader.ui.viewmodel.BookViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eodev.reader.LocalNavigation
import com.eodev.reader.data.model.Book
import com.eodev.reader.ui.components.DrawerContent
import com.eodev.reader.ui.components.MonthlyStatistics
import com.eodev.reader.ui.components.YearlyStatistics
import com.eodev.reader.ui.navigation.Screen
import java.time.Instant
import java.time.LocalDate
import java.time.Month
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: BookViewModel
){
    var selectedPeriod by remember { mutableStateOf("Месяц") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    val monthlyBooks by viewModel.getMonthlyBooks(selectedDate.year, selectedDate.monthValue)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val yearlyStats by viewModel.getYearlyStatistics(selectedDate.year)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val ratingStats by viewModel.getRatingStatistics(selectedDate.year)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val navController= LocalNavigation.current

    var showDatePicker by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
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
                    onAllAuthorsClick = {
                        navController.navigate(Screen.AUTHORS.name)
                        scope.launch { drawerState.close() }
                    },
                    onAllSeriesClick = {
                        navController.navigate(Screen.SERIES.name)
                        scope.launch { drawerState.close() }
                    },
                    onStatisticsClick = {
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Статистика") },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Default.Menu, contentDescription = "Меню")
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                    ) {
                        Text(
                            text = when (selectedPeriod) {
                                "Месяц" -> "${Month.of(selectedDate.monthValue).getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedDate.year}"
                                else -> selectedDate.year.toString()
                            }
                        )
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Выбрать дату")
                    }

                    FilterChip(
                        selected = selectedPeriod == "Месяц",
                        onClick = { selectedPeriod = "Месяц" },
                        label = { Text("Месяц") }
                    )
                    FilterChip(
                        selected = selectedPeriod == "Год",
                        onClick = { selectedPeriod = "Год" },
                        label = { Text("Год") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Отображение статистики
                when (selectedPeriod) {
                    "Месяц" -> {
                        if(monthlyBooks.isNullOrEmpty()){
                            Text("Нет данных за выбранный месяц")

                        } else{
                            MonthlyStatistics(
                                books = monthlyBooks,
                                month = selectedDate.monthValue,
                                year = selectedDate.year
                            )
                        }
                    }
                    "Год" ->{
                        if(yearlyStats.isNullOrEmpty()){
                            Text("Нет данных за выбранный год")
                        } else {
                            YearlyStatistics(
                                yearlyStats = yearlyStats,
                                ratingStats = ratingStats,
                                year = selectedDate.year
                            )
                        }
                    }
                }
            }
        }

        // DatePicker Dialog
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    Button(onClick = {
                        datePickerState.selectedDateMillis?.let {
                            selectedDate = Instant.ofEpochMilli(it)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }) {
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("Отмена")
                    }
                }
            ) {
                DatePicker(
                    state = datePickerState,
                    title = {
                        Text(
                            text = "Выберите ${if (selectedPeriod == "Месяц") "месяц" else "год"}",
                            modifier = Modifier.padding(8.dp)
                        )
                    },
                    showModeToggle = false
                )
            }
        }
    }
}