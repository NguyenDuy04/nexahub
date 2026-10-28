package com.beulangthang.nexahub.ui.screen.tabs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.beulangthang.nexahub.data.model.Task
import com.beulangthang.nexahub.ui.components.TaskItem
import com.beulangthang.nexahub.ui.util.LocalAppStrings
import com.beulangthang.nexahub.ui.viewmodel.TaskUiState
import com.beulangthang.nexahub.ui.viewmodel.TaskViewModel
import java.util.Calendar

@Composable
fun CalendarTab(
    viewModel: TaskViewModel,
    uiState: TaskUiState,
    onTaskClick: (Task) -> Unit,
    onEditTask: (Task) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    val now = System.currentTimeMillis()
    val startOfToday = remember(now) {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    val endOfToday = remember(startOfToday) { startOfToday + 24 * 60 * 60 * 1000 - 1 }

    val overdueTasks = uiState.tasks.filter { task ->
        !task.isCompleted && task.dueDate != null && task.dueDate < startOfToday
    }

    val todayTasks = uiState.tasks.filter { task ->
        task.dueDate != null && task.dueDate in startOfToday..endOfToday
    }

    val upcomingTasks = uiState.tasks.filter { task ->
        task.dueDate != null && task.dueDate > endOfToday
    }

    val noDueDateTasks = uiState.tasks.filter { task ->
        task.dueDate == null
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 8.dp,
            bottom = 88.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section: Overdue
        if (overdueTasks.isNotEmpty()) {
            item {
                CalendarSectionHeader(
                    title = "${strings.overdue} (${overdueTasks.size})",
                    color = MaterialTheme.colorScheme.error,
                    icon = Icons.Default.Event
                )
            }
            items(overdueTasks, key = { "overdue_${it.id}" }) { task ->
                TaskItem(
                    task = task,
                    onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                    onTaskClick = { onTaskClick(task) },
                    onEditClick = { onEditTask(task) },
                    onDeleteClick = { viewModel.deleteTask(task) }
                )
            }
        }

        // Section: Today
        item {
            CalendarSectionHeader(
                title = "Hôm nay (${todayTasks.size})",
                color = MaterialTheme.colorScheme.primary,
                icon = Icons.Default.Schedule
            )
        }
        if (todayTasks.isEmpty()) {
            item {
                Text(
                    text = "Không có công việc nào cần làm trong hôm nay.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )
            }
        } else {
            items(todayTasks, key = { "today_${it.id}" }) { task ->
                TaskItem(
                    task = task,
                    onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                    onTaskClick = { onTaskClick(task) },
                    onEditClick = { onEditTask(task) },
                    onDeleteClick = { viewModel.deleteTask(task) }
                )
            }
        }

        // Section: Upcoming
        if (upcomingTasks.isNotEmpty()) {
            item {
                CalendarSectionHeader(
                    title = "Sắp tới (${upcomingTasks.size})",
                    color = Color(0xFF009688),
                    icon = Icons.Default.CalendarMonth
                )
            }
            items(upcomingTasks, key = { "upcoming_${it.id}" }) { task ->
                TaskItem(
                    task = task,
                    onToggleCompletion = { viewModel.toggleTaskCompletion(task) },
                    onTaskClick = { onTaskClick(task) },
                    onEditClick = { onEditTask(task) },
                    onDeleteClick = { viewModel.deleteTask(task) }
                )
            }
        }

        // Section: No Due Date
        if (noDueDateTasks.isNotEmpty()) {
            item {
                CalendarSectionHeader(
                    title = "${strings.noDueDate} (${noDueDateTasks.size})",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    icon = Icons.Default.Event
                )
            }
            items(noDueDateTasks, key = { "nodue_${it.id}" }) { task ->
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

@Composable
private fun CalendarSectionHeader(
    title: String,
    color: Color,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                color = color
            )
        )
    }
}
