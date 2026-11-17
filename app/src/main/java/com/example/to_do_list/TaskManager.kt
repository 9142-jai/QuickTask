package com.example.to_do_list

object TaskRules {
    fun normalizedTitleOrNull(title: String): String? =
        title.trim().takeIf { it.isNotEmpty() }

    fun isOverdue(task: Task, todayEpochDay: Long): Boolean =
        !task.isCompleted && task.dueDateEpochDay?.let { it < todayEpochDay } == true

    fun filter(
        tasks: List<Task>,
        query: String,
        category: String?,
        priority: String?,
        status: String,
        todayEpochDay: Long
    ): List<Task> {
        val normalizedQuery = query.trim()
        return tasks.filter { task ->
            (normalizedQuery.isEmpty() || task.title.contains(normalizedQuery, ignoreCase = true)) &&
                (category == null || task.category == category) &&
                (priority == null || task.priority == priority) &&
                when (status) {
                    "Pending" -> !task.isCompleted
                    "Completed" -> task.isCompleted
                    "Overdue" -> isOverdue(task, todayEpochDay)
                    else -> true
                }
        }
    }
}

class TaskManager {
    private val tasks = mutableListOf<Task>()
    private var nextId = 1

    fun addTask(
        title: String,
        category: String = "Personal",
        priority: String = "Medium",
        dueDateEpochDay: Long? = null
    ): Task {
        val normalizedTitle = requireNotNull(TaskRules.normalizedTitleOrNull(title)) {
            "Task title cannot be empty"
        }
        val task = Task(
            id = nextId++,
            title = normalizedTitle,
            category = category,
            priority = priority,
            dueDateEpochDay = dueDateEpochDay
        )
        tasks.add(task)
        return task
    }

    fun editTask(task: Task): Boolean {
        val normalizedTitle = TaskRules.normalizedTitleOrNull(task.title) ?: return false
        val index = tasks.indexOfFirst { it.id == task.id }
        if (index < 0) return false
        tasks[index] = task.copy(title = normalizedTitle)
        return true
    }

    fun setCompleted(id: Int, completed: Boolean): Boolean {
        val index = tasks.indexOfFirst { it.id == id }
        if (index < 0) return false
        tasks[index] = tasks[index].copy(isCompleted = completed)
        return true
    }

    fun completeTask(id: Int): Boolean = setCompleted(id, true)

    fun deleteTask(id: Int): Boolean = tasks.removeAll { it.id == id }

    fun getTasks(): List<Task> = tasks.toList()

    fun getCompletedCount(): Int = tasks.count { it.isCompleted }

    fun getTotalCount(): Int = tasks.size

    fun getPendingCount(): Int = tasks.count { !it.isCompleted }

    fun getOverdueCount(todayEpochDay: Long): Int =
        tasks.count { TaskRules.isOverdue(it, todayEpochDay) }

    fun getTasks(
        query: String,
        category: String?,
        priority: String?,
        status: String,
        todayEpochDay: Long
    ): List<Task> = TaskRules.filter(tasks, query, category, priority, status, todayEpochDay)
}
