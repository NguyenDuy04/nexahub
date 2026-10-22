package com.beulangthang.nexahub.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.beulangthang.nexahub.data.local.AppLanguage
import com.beulangthang.nexahub.data.local.PreferencesManager
import com.beulangthang.nexahub.data.local.ThemeMode
import com.beulangthang.nexahub.data.local.TodoDatabaseHelper
import com.beulangthang.nexahub.data.model.Category
import com.beulangthang.nexahub.data.model.Priority
import com.beulangthang.nexahub.data.model.Task
import com.beulangthang.nexahub.data.repository.TaskRepository
import com.beulangthang.nexahub.ui.util.LocalAppStrings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class FilterTab {
    ALL, PENDING, COMPLETED;

    val label: String
        @Composable
        get() {
            val strings = LocalAppStrings.current
            return when (this) {
                ALL -> strings.tabAll
                PENDING -> strings.tabInProgress
                COMPLETED -> strings.tabCompleted
            }
        }
}

enum class SortOption {
    CREATED_DESC, CREATED_ASC, DUE_DATE, PRIORITY_DESC;

    val label: String
        @Composable
        get() {
            val strings = LocalAppStrings.current
            return when (this) {
                CREATED_DESC -> strings.sortNewest
                CREATED_ASC -> strings.sortOldest
                DUE_DATE -> strings.sortDueDate
                PRIORITY_DESC -> strings.sortPriority
            }
        }
}

data class TaskStatistics(
    val totalCount: Int = 0,
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val highPriorityCount: Int = 0,
    val completionPercentage: Float = 0f
)

private data class FilterParams(
    val searchQuery: String,
    val filterTab: FilterTab,
    val category: Category?,
    val priority: Priority?,
    val sortOption: SortOption
)

data class TaskUiState(
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val searchQuery: String = "",
    val activeFilterTab: FilterTab = FilterTab.ALL,
    val selectedCategory: Category? = null,
    val selectedPriority: Priority? = null,
    val sortOption: SortOption = SortOption.CREATED_DESC,
    val statistics: TaskStatistics = TaskStatistics(),
    val isLoading: Boolean = false,
    val lastDeletedTask: Task? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val appLanguage: AppLanguage = AppLanguage.VIETNAMESE
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val dbHelper = TodoDatabaseHelper(application)
    private val repository = TaskRepository(dbHelper)
    private val preferencesManager = PreferencesManager(application)

    val themeMode: StateFlow<ThemeMode> = preferencesManager.themeMode
    val appLanguage: StateFlow<AppLanguage> = preferencesManager.appLanguage

    private val _searchQuery = MutableStateFlow("")
    private val _filterTab = MutableStateFlow(FilterTab.ALL)
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    private val _selectedPriority = MutableStateFlow<Priority?>(null)
    private val _sortOption = MutableStateFlow(SortOption.CREATED_DESC)
    private val _lastDeletedTask = MutableStateFlow<Task?>(null)

    private val _filterParams = combine(
        _searchQuery,
        _filterTab,
        _selectedCategory,
        _selectedPriority,
        _sortOption
    ) { query, filterTab, category, priority, sortOption ->
        FilterParams(query, filterTab, category, priority, sortOption)
    }

    val uiState: StateFlow<TaskUiState> = combine(
        repository.tasks,
        _filterParams,
        _lastDeletedTask,
        themeMode,
        appLanguage
    ) { tasks, params, lastDeleted, theme, lang ->
        val stats = calculateStatistics(tasks)
        val filtered = filterAndSortTasks(
            tasks = tasks,
            query = params.searchQuery,
            filterTab = params.filterTab,
            category = params.category,
            priority = params.priority,
            sortOption = params.sortOption
        )

        TaskUiState(
            tasks = tasks,
            filteredTasks = filtered,
            searchQuery = params.searchQuery,
            activeFilterTab = params.filterTab,
            selectedCategory = params.category,
            selectedPriority = params.priority,
            sortOption = params.sortOption,
            statistics = stats,
            lastDeletedTask = lastDeleted,
            themeMode = theme,
            appLanguage = lang
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TaskUiState(isLoading = true)
    )

    init {
        viewModelScope.launch {
            repository.refreshTasks()
        }
    }

    private fun calculateStatistics(allTasks: List<Task>): TaskStatistics {
        val total = allTasks.size
        if (total == 0) return TaskStatistics()

        val completed = allTasks.count { it.isCompleted }
        val active = total - completed
        val highPriority = allTasks.count { it.priority == Priority.HIGH && !it.isCompleted }
        val percentage = (completed.toFloat() / total.toFloat()) * 100f

        return TaskStatistics(
            totalCount = total,
            activeCount = active,
            completedCount = completed,
            highPriorityCount = highPriority,
            completionPercentage = percentage
        )
    }

    private fun filterAndSortTasks(
        tasks: List<Task>,
        query: String,
        filterTab: FilterTab,
        category: Category?,
        priority: Priority?,
        sortOption: SortOption
    ): List<Task> {
        var result = tasks

        result = when (filterTab) {
            FilterTab.ALL -> result
            FilterTab.PENDING -> result.filter { !it.isCompleted }
            FilterTab.COMPLETED -> result.filter { it.isCompleted }
        }

        if (query.isNotBlank()) {
            val trimmedQuery = query.trim().lowercase()
            result = result.filter {
                it.title.lowercase().contains(trimmedQuery) ||
                        it.description.lowercase().contains(trimmedQuery)
            }
        }

        if (category != null) {
            result = result.filter { it.category == category }
        }

        if (priority != null) {
            result = result.filter { it.priority == priority }
        }

        return when (sortOption) {
            SortOption.CREATED_DESC -> result.sortedByDescending { it.createdAt }
            SortOption.CREATED_ASC -> result.sortedBy { it.createdAt }
            SortOption.DUE_DATE -> result.sortedWith(
                compareBy<Task> { it.dueDate ?: Long.MAX_VALUE }
                    .thenByDescending { it.createdAt }
            )
            SortOption.PRIORITY_DESC -> result.sortedByDescending { it.priority.value }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        preferencesManager.setThemeMode(mode)
    }

    fun setAppLanguage(language: AppLanguage) {
        preferencesManager.setAppLanguage(language)
    }

    fun toggleQuickTheme() {
        val next = when (themeMode.value) {
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.LIGHT
            ThemeMode.SYSTEM -> ThemeMode.DARK
        }
        preferencesManager.setThemeMode(next)
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilterTab(tab: FilterTab) {
        _filterTab.value = tab
    }

    fun setSelectedCategory(category: Category?) {
        _selectedCategory.value = if (_selectedCategory.value == category) null else category
    }

    fun setSelectedPriority(priority: Priority?) {
        _selectedPriority.value = if (_selectedPriority.value == priority) null else priority
    }

    fun setSortOption(sortOption: SortOption) {
        _sortOption.value = sortOption
    }

    fun addTask(title: String, description: String, priority: Priority, category: Category, dueDate: Long?) {
        viewModelScope.launch {
            val task = Task(
                title = title.trim(),
                description = description.trim(),
                priority = priority,
                category = category,
                dueDate = dueDate
            )
            repository.addTask(task)
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun toggleTaskCompletion(task: Task) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(task)
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            _lastDeletedTask.value = task
            repository.deleteTask(task.id)
        }
    }

    fun undoDelete() {
        val taskToRestore = _lastDeletedTask.value ?: return
        viewModelScope.launch {
            repository.addTask(taskToRestore)
            _lastDeletedTask.value = null
        }
    }

    fun clearLastDeletedTask() {
        _lastDeletedTask.value = null
    }

    fun clearCompletedTasks() {
        viewModelScope.launch {
            repository.clearCompletedTasks()
        }
    }
}
