package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.ui.screens.MainNavigationContainer
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StudyBuddyViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Retrieve view model instance using our custom provider factory
        val viewModel = ViewModelProvider(
            this, 
            StudyBuddyViewModel.provideFactory(application)
        )[StudyBuddyViewModel::class.java]

        setContent {
            MyApplicationTheme {
                MainNavigationContainer(viewModel = viewModel)
            }
        }
    }
}
