package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.navigation.compose.rememberNavController
import com.example.data.repository.FirebaseDriverRepository
import com.example.ui.navigation.DriverAppNavigation
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.DriverViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Shared Firebase-backed repository instance for driver app
        val repository = FirebaseDriverRepository(this)

        setContent {
            val viewModel = remember { DriverViewModel(repository) }
            val isArabic by viewModel.isArabic.collectAsState()
            val navController = rememberNavController()

            // Dynamic RTL layout direction based on Arabic toggle
            val layoutDirection = if (isArabic) LayoutDirection.Rtl else LayoutDirection.Ltr

            CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
                MyApplicationTheme {
                    Surface(modifier = Modifier.fillMaxSize()) {
                        DriverAppNavigation(
                            navController = navController,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}
