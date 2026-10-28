package com.beulangthang.nexahub.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beulangthang.nexahub.data.local.ThemeMode
import com.beulangthang.nexahub.data.model.Task
import com.beulangthang.nexahub.ui.components.AddEditTaskBottomSheet
import com.beulangthang.nexahub.ui.components.TaskDetailDialog
import com.beulangthang.nexahub.ui.screen.tabs.AnalyticsTab
import com.beulangthang.nexahub.ui.screen.tabs.CalendarTab
import com.beulangthang.nexahub.ui.screen.tabs.CategoriesTab
import com.beulangthang.nexahub.ui.screen.tabs.NotesTab
import com.beulangthang.nexahub.ui.screen.tabs.SettingsTab
import com.beulangthang.nexahub.ui.util.LocalAppStrings
import com.beulangthang.nexahub.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

enum class NavigationTab(
    val titleRes: @Composable () -> String,
    val icon: ImageVector
) {
    NOTES(
        titleRes = { LocalAppStrings.current.navNotes },
        icon = Icons.Default.EditNote
    ),
    CATEGORIES(
        titleRes = { LocalAppStrings.current.navCategories },
        icon = Icons.Default.Category
    ),
    CALENDAR(
        titleRes = { LocalAppStrings.current.navCalendar },
        icon = Icons.Default.CalendarMonth
    ),
    ANALYTICS(
        titleRes = { LocalAppStrings.current.navAnalytics },
        icon = Icons.Default.Analytics
    ),
    SETTINGS(
        titleRes = { LocalAppStrings.current.navSettings },
        icon = Icons.Default.Settings
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TaskViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddEditSheet by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var selectedTaskForDetail by remember { mutableStateOf<Task?>(null) }
    var showClearCompletedDialog by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle Snackbar for Undo delete
    LaunchedEffect(uiState.lastDeletedTask) {
        val deleted = uiState.lastDeletedTask
        if (deleted != null) {
            scope.launch {
                val messageStr = String.format(strings.deletedMessage, deleted.title)
                val result = snackbarHostState.showSnackbar(
                    message = messageStr,
                    actionLabel = strings.undo,
                    duration = SnackbarDuration.Short
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.undoDelete()
                } else {
                    viewModel.clearLastDeletedTask()
                }
            }
        }
    }

    val currentTab = NavigationTab.entries[selectedTabIndex]

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = when (currentTab) {
                            NavigationTab.NOTES -> strings.appName
                            NavigationTab.CATEGORIES -> strings.navCategories
                            NavigationTab.CALENDAR -> strings.navCalendar
                            NavigationTab.ANALYTICS -> strings.navAnalytics
                            NavigationTab.SETTINGS -> strings.settingsTitle
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = currentTab.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                },
                actions = {
                    // Quick Theme Toggle
                    IconButton(onClick = { viewModel.toggleQuickTheme() }) {
                        Icon(
                            imageVector = if (uiState.themeMode == ThemeMode.DARK) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = strings.themeTitle,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationTab.entries.forEachIndexed { index, tab ->
                    val isSelected = selectedTabIndex == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.titleRes()
                            )
                        },
                        label = {
                            Text(
                                text = tab.titleRes(),
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTabIndex in 0..2) {
                FloatingActionButton(
                    onClick = {
                        editingTask = null
                        showAddEditSheet = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = strings.addTask)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = selectedTabIndex,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "TabContent"
            ) { targetIndex ->
                when (NavigationTab.entries[targetIndex]) {
                    NavigationTab.NOTES -> NotesTab(
                        viewModel = viewModel,
                        uiState = uiState,
                        onTaskClick = { selectedTaskForDetail = it },
                        onEditTask = {
                            editingTask = it
                            showAddEditSheet = true
                        },
                        onAddNewTask = {
                            editingTask = null
                            showAddEditSheet = true
                        }
                    )
                    NavigationTab.CATEGORIES -> CategoriesTab(
                        uiState = uiState,
                        onCategorySelected = { category ->
                            viewModel.setSelectedCategory(category)
                            selectedTabIndex = 0 // Switch to Notes tab to see filtered list
                        }
                    )
                    NavigationTab.CALENDAR -> CalendarTab(
                        viewModel = viewModel,
                        uiState = uiState,
                        onTaskClick = { selectedTaskForDetail = it },
                        onEditTask = {
                            editingTask = it
                            showAddEditSheet = true
                        }
                    )
                    NavigationTab.ANALYTICS -> AnalyticsTab(
                        uiState = uiState
                    )
                    NavigationTab.SETTINGS -> SettingsTab(
                        viewModel = viewModel,
                        uiState = uiState,
                        onClearCompletedClick = { showClearCompletedDialog = true }
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet for Add/Edit Task
    if (showAddEditSheet) {
        AddEditTaskBottomSheet(
            taskToEdit = editingTask,
            onDismiss = { showAddEditSheet = false },
            onSave = { title, description, priority, category, dueDate ->
                if (editingTask == null) {
                    viewModel.addTask(title, description, priority, category, dueDate)
                } else {
                    val updated = editingTask!!.copy(
                        title = title,
                        description = description,
                        priority = priority,
                        category = category,
                        dueDate = dueDate,
                        updatedAt = System.currentTimeMillis()
                    )
                    viewModel.updateTask(updated)
                }
                showAddEditSheet = false
            },
            sheetState = bottomSheetState
        )
    }

    // Task Detail Dialog
    selectedTaskForDetail?.let { task ->
        TaskDetailDialog(
            task = task,
            onDismiss = { selectedTaskForDetail = null },
            onToggleCompletion = {
                viewModel.toggleTaskCompletion(task)
                selectedTaskForDetail = task.copy(isCompleted = !task.isCompleted)
            },
            onEdit = {
                editingTask = task
                showAddEditSheet = true
            },
            onDelete = {
                viewModel.deleteTask(task)
            }
        )
    }

    // Confirm Clear Completed Dialog
    if (showClearCompletedDialog) {
        AlertDialog(
            onDismissRequest = { showClearCompletedDialog = false },
            title = { Text(strings.clearCompletedTitle) },
            text = { Text(strings.clearCompletedConfirm) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearCompletedTasks()
                        showClearCompletedDialog = false
                    }
                ) {
                    Text(strings.clearAllButton, color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearCompletedDialog = false }) {
                    Text(strings.cancel)
                }
            }
        )
    }
}
