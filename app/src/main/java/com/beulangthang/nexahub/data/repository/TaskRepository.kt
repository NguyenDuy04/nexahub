package com.beulangthang.nexahub.data.repository

import com.beulangthang.nexahub.data.local.TodoDatabaseHelper
import com.beulangthang.nexahub.data.model.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class TaskRepository(private val dbHelper: TodoDatabaseHelper) {

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    suspend fun refreshTasks() {
        withContext(Dispatchers.IO) {
            val list = dbHelper.getAllTasks()
            _tasks.value = list
        }
    }

    suspend fun addTask(task: Task): Long {
        return withContext(Dispatchers.IO) {
            val newId = dbHelper.insertTask(task)
            refreshTasks()
            newId
        }
    }

    suspend fun updateTask(task: Task) {
        withContext(Dispatchers.IO) {
            dbHelper.updateTask(task)
            refreshTasks()
        }
    }

    suspend fun deleteTask(id: Long) {
        withContext(Dispatchers.IO) {
            dbHelper.deleteTask(id)
            refreshTasks()
        }
    }

    suspend fun toggleTaskCompletion(task: Task) {
        val updatedTask = task.copy(
            isCompleted = !task.isCompleted,
            updatedAt = System.currentTimeMillis()
        )
        updateTask(updatedTask)
    }

    suspend fun clearCompletedTasks() {
        withContext(Dispatchers.IO) {
            dbHelper.clearCompletedTasks()
            refreshTasks()
        }
    }
}
