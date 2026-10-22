package com.beulangthang.nexahub.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.beulangthang.nexahub.data.model.Category
import com.beulangthang.nexahub.data.model.Priority
import com.beulangthang.nexahub.data.model.Task

class TodoDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "nexahub_todo.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_TASKS = "tasks"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_IS_COMPLETED = "is_completed"
        private const val COLUMN_PRIORITY = "priority"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_DUE_DATE = "due_date"
        private const val COLUMN_CREATED_AT = "created_at"
        private const val COLUMN_UPDATED_AT = "updated_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_TASKS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_IS_COMPLETED INTEGER NOT NULL DEFAULT 0,
                $COLUMN_PRIORITY INTEGER NOT NULL DEFAULT 2,
                $COLUMN_CATEGORY TEXT NOT NULL DEFAULT 'OTHER',
                $COLUMN_DUE_DATE INTEGER,
                $COLUMN_CREATED_AT INTEGER NOT NULL,
                $COLUMN_UPDATED_AT INTEGER NOT NULL
            )
        """.trimIndent()

        db.execSQL(createTableQuery)
        seedInitialTasks(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_TASKS")
        onCreate(db)
    }

    private fun seedInitialTasks(db: SQLiteDatabase) {
        val now = System.currentTimeMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L

        val initialTasks = listOf(
            Task(
                title = "Hoàn thành dự án Nexahub Todo App",
                description = "Xây dựng giao diện Jetpack ComposeMaterial 3 và kết nối cơ sở dữ liệu.",
                isCompleted = false,
                priority = Priority.HIGH,
                category = Category.WORK,
                dueDate = now + oneDayMillis,
                createdAt = now,
                updatedAt = now
            ),
            Task(
                title = "Mua đồ tạp hóa tuần này",
                description = "Sữa, bánh mì, rau củ tươi, hoa quả và cà phê.",
                isCompleted = false,
                priority = Priority.MEDIUM,
                category = Category.SHOPPING,
                dueDate = now + (2 * oneDayMillis),
                createdAt = now - 1000,
                updatedAt = now - 1000
            ),
            Task(
                title = "Tập thể dục 30 phút buổi sáng",
                description = "Chạy bộ hoặc tập Yoga khởi động ngày mới tràn đầy năng lượng.",
                isCompleted = true,
                priority = Priority.HIGH,
                category = Category.HEALTH,
                dueDate = now,
                createdAt = now - 2000,
                updatedAt = now - 2000
            ),
            Task(
                title = "Đọc 10 trang sách Lập trình Kotlin",
                description = "Tìm hiểu thêm về Coroutines và Flow trong Android.",
                isCompleted = false,
                priority = Priority.LOW,
                category = Category.STUDY,
                dueDate = now + (3 * oneDayMillis),
                createdAt = now - 3000,
                updatedAt = now - 3000
            )
        )

        initialTasks.forEach { task ->
            val values = ContentValues().apply {
                put(COLUMN_TITLE, task.title)
                put(COLUMN_DESCRIPTION, task.description)
                put(COLUMN_IS_COMPLETED, if (task.isCompleted) 1 else 0)
                put(COLUMN_PRIORITY, task.priority.value)
                put(COLUMN_CATEGORY, task.category.name)
                put(COLUMN_DUE_DATE, task.dueDate)
                put(COLUMN_CREATED_AT, task.createdAt)
                put(COLUMN_UPDATED_AT, task.updatedAt)
            }
            db.insert(TABLE_TASKS, null, values)
        }
    }

    fun getAllTasks(): List<Task> {
        val taskList = mutableListOf<Task>()
        val selectQuery = "SELECT * FROM $TABLE_TASKS ORDER BY $COLUMN_IS_COMPLETED ASC, $COLUMN_CREATED_AT DESC"
        val db = readableDatabase

        db.rawQuery(selectQuery, null).use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID)
                val titleIndex = cursor.getColumnIndexOrThrow(COLUMN_TITLE)
                val descIndex = cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)
                val completedIndex = cursor.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)
                val priorityIndex = cursor.getColumnIndexOrThrow(COLUMN_PRIORITY)
                val categoryIndex = cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)
                val dueDateIndex = cursor.getColumnIndexOrThrow(COLUMN_DUE_DATE)
                val createdIndex = cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)
                val updatedIndex = cursor.getColumnIndexOrThrow(COLUMN_UPDATED_AT)

                do {
                    val id = cursor.getLong(idIndex)
                    val title = cursor.getString(titleIndex)
                    val description = cursor.getString(descIndex) ?: ""
                    val isCompleted = cursor.getInt(completedIndex) == 1
                    val priority = Priority.fromValue(cursor.getInt(priorityIndex))
                    val category = Category.fromName(cursor.getString(categoryIndex) ?: "OTHER")
                    val dueDate = if (cursor.isNull(dueDateIndex)) null else cursor.getLong(dueDateIndex)
                    val createdAt = cursor.getLong(createdIndex)
                    val updatedAt = cursor.getLong(updatedIndex)

                    taskList.add(
                        Task(
                            id = id,
                            title = title,
                            description = description,
                            isCompleted = isCompleted,
                            priority = priority,
                            category = category,
                            dueDate = dueDate,
                            createdAt = createdAt,
                            updatedAt = updatedAt
                        )
                    )
                } while (cursor.moveToNext())
            }
        }
        return taskList
    }

    fun insertTask(task: Task): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_IS_COMPLETED, if (task.isCompleted) 1 else 0)
            put(COLUMN_PRIORITY, task.priority.value)
            put(COLUMN_CATEGORY, task.category.name)
            put(COLUMN_DUE_DATE, task.dueDate)
            put(COLUMN_CREATED_AT, task.createdAt)
            put(COLUMN_UPDATED_AT, task.updatedAt)
        }
        return db.insert(TABLE_TASKS, null, values)
    }

    fun updateTask(task: Task): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, task.title)
            put(COLUMN_DESCRIPTION, task.description)
            put(COLUMN_IS_COMPLETED, if (task.isCompleted) 1 else 0)
            put(COLUMN_PRIORITY, task.priority.value)
            put(COLUMN_CATEGORY, task.category.name)
            put(COLUMN_DUE_DATE, task.dueDate)
            put(COLUMN_UPDATED_AT, System.currentTimeMillis())
        }
        return db.update(TABLE_TASKS, values, "$COLUMN_ID = ?", arrayOf(task.id.toString()))
    }

    fun deleteTask(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_TASKS, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun clearCompletedTasks(): Int {
        val db = writableDatabase
        return db.delete(TABLE_TASKS, "$COLUMN_IS_COMPLETED = ?", arrayOf("1"))
    }
}
