package com.beulangthang.nexahub.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.beulangthang.nexahub.data.local.ThemeMode
import com.beulangthang.nexahub.data.model.Task
import com.beulangthang.nexahub.ui.components.AddEditTaskBottomSheet
import com.beulangthang.nexahub.ui.components.EmptyStateView
import com.beulangthang.nexahub.ui.components.FilterAndSearchSection
import com.beulangthang.nexahub.ui.components.SettingsDialog
import com.beulangthang.nexahub.ui.components.TaskDetailDialog
import com.beulangthang.nexahub.ui.components.TaskItem
import com.beulangthang.nexahub.ui.components.TaskSummaryCard
import com.beulangthang.nexahub.ui.util.LocalAppStrings
import com.beulangthang.nexahub.ui.viewmodel.TaskViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoHomeScreen(
    viewModel: TaskViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showAddEditSheet by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<Task?>(null) }
    var selectedTaskForDetail by remember { mutableStateOf<Task?>(null) }
    var showClearCompletedDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

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

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = strings.appName,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    Icon(
                        imageVector = Icons.Default.TaskAlt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 12.dp)
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

                    // Settings Button
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = strings.settingsTitle,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Clear Completed Tasks Button
                    if (uiState.statistics.completedCount > 0) {
                        IconButton(onClick = { showClearCompletedDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = strings.clearCompletedTitle,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    editingTask = null
                    showAddEditSheet = true
                },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(strings.addTask) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 8.dp,
                    bottom = 88.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary Progress Dashboard
                item {
                    TaskSummaryCard(statistics = uiState.statistics)
                }

                // Search & Filters
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    FilterAndSearchSection(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = viewModel::setSearchQuery,
                        activeTab = uiState.activeFilterTab,
                        onTabSelected = viewModel::setFilterTab,
                        selectedCategory = uiState.selectedCategory,
                        onCategorySelected = viewModel::setSelectedCategory,
                        selectedPriority = uiState.selectedPriority,
                        onPrioritySelected = viewModel::setSelectedPriority,
                        selectedSortOption = uiState.sortOption,
                        onSortOptionSelected = viewModel::setSortOption
                    )
                }

                item {
                    Text(
                        text = "${strings.taskListHeader} (${uiState.filteredTasks.size})",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                // Tasks List or Empty State
                if (uiState.filteredTasks.isEmpty()) {
                    item {
                        EmptyStateView(
                            isSearching = uiState.searchQuery.isNotBlank() ||
                                    uiState.selectedCategory != null ||
                                    uiState.selectedPriority != null,
                            onAddNewTaskClick = {
                                editingTask = null
                                showAddEditSheet = true
                            }
                        )
                    }
                } else {
                    items(
                        items = uiState.filteredTasks,
                        key = { it.id }
                    ) { task ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(),
                            exit = fadeOut()
                        ) {
                            TaskItem(
                                task = task,
                                onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                                onTaskClick = { selectedTaskForDetail = task },
                                onEditClick = {
                                    editingTask = task
                                    showAddEditSheet = true
                                },
                                onDeleteClick = { viewModel.deleteTask(task) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Settings Dialog
    if (showSettingsDialog) {
        SettingsDialog(
            currentThemeMode = uiState.themeMode,
            currentLanguage = uiState.appLanguage,
            onThemeModeSelected = viewModel::setThemeMode,
            onLanguageSelected = viewModel::setAppLanguage,
            onDismiss = { showSettingsDialog = false }
        )
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
