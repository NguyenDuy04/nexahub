package com.beulangthang.nexahub.ui.screen.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beulangthang.nexahub.data.model.Task
import com.beulangthang.nexahub.ui.components.EmptyStateView
import com.beulangthang.nexahub.ui.components.FilterAndSearchSection
import com.beulangthang.nexahub.ui.components.TaskItem
import com.beulangthang.nexahub.ui.components.TaskSummaryCard
import com.beulangthang.nexahub.ui.util.LocalAppStrings
import com.beulangthang.nexahub.ui.viewmodel.TaskUiState
import com.beulangthang.nexahub.ui.viewmodel.TaskViewModel

@Composable
fun NotesTab(
    viewModel: TaskViewModel,
    uiState: TaskUiState,
    onTaskClick: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    onAddNewTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 88.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Dashboard summary
        item {
            TaskSummaryCard(statistics = uiState.statistics)
        }

        // Filters and search
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

        if (uiState.filteredTasks.isEmpty()) {
            item {
                EmptyStateView(
                    isSearching = uiState.searchQuery.isNotBlank() ||
                            uiState.selectedCategory != null ||
                            uiState.selectedPriority != null,
                    onAddNewTaskClick = onAddNewTask
                )
            }
        } else {
            items(
                items = uiState.filteredTasks,
                key = { it.id }
            ) { task ->
                TaskItem(
                    task = task,
                    onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                    onTaskClick = { onTaskClick(task) },
                    onEditClick = { onEditTask(task) },
                    onDeleteClick = { viewModel.deleteTask(task) }
                )
            }
        }
    }
}
