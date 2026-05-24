package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.viewmodel.StudyBuddyViewModel

@Composable
fun MainNavigationContainer(
    viewModel: StudyBuddyViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("main_navigation_bar"),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 0) Icons.Default.MenuBook else Icons.Outlined.MenuBook,
                            contentDescription = "Homework"
                        )
                    },
                    label = { Text("Homework Hub") },
                    modifier = Modifier.testTag("nav_homework_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 1) Icons.Default.CalendarToday else Icons.Outlined.CalendarToday,
                            contentDescription = "Study Planner"
                        )
                    },
                    label = { Text("Plan Calendar") },
                    modifier = Modifier.testTag("nav_planner_tab")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedTab == 2) Icons.Default.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "AI Tutors"
                        )
                    },
                    label = { Text("AI Tutors") },
                    modifier = Modifier.testTag("nav_tutor_tab")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
            // innerPadding already consumes Status Bar + Bottom Bar safely, full edge-to-edge compliant!
        ) {
            when (selectedTab) {
                0 -> HomeworkScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
                1 -> StudyPlannerScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
                2 -> AITutorScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
