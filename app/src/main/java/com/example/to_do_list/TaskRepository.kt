package com.example.to_do_list

import kotlinx.coroutines.flow.Flow

class TaskRepository(private val taskDao: TaskDao) {
    val tasks: Flow<List<Task>> = taskDao.observeTasks()

    suspend fun addTask(task: Task): Long? {
        val normalizedTitle = TaskRules.normalizedTitleOrNull(task.title) ?: return null
        return taskDao.insert(task.copy(id = 0, title = normalizedTitle))
    }

    suspend fun updateTask(task: Task): Boolean {
        val normalizedTitle = TaskRules.normalizedTitleOrNull(task.title) ?: return false
        return taskDao.update(task.copy(title = normalizedTitle)) > 0
    }

    suspend fun setCompleted(taskId: Int, completed: Boolean): Boolean =
        taskDao.setCompleted(taskId, completed) > 0

    suspend fun deleteTask(taskId: Int): Boolean =
        taskDao.deleteById(taskId) > 0
}
