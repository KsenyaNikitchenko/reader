package com.eodev.reader

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.eodev.reader.ui.navigation.ReaderNavigation
import com.eodev.reader.ui.theme.ReaderTheme
import com.eodev.reader.ui.viewmodel.BookViewModel
import com.eodev.reader.ui.viewmodel.BookViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ReaderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val application = application as ReaderApplication
                    val viewModel: BookViewModel = viewModel(
                        factory = BookViewModelFactory(application.repository)
                    )
                    CompositionLocalProvider(LocalNavigation provides navController) {
                        ReaderNavigation(
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

val LocalNavigation = staticCompositionLocalOf<NavHostController> {
    error("Navigation controller not provided")
}