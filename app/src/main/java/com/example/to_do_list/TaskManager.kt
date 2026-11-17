package com.example.to_do_list



class TaskManager {

    private val tasks = mutableListOf<Task>()
    private var nextId = 1

    fun addTask(title: String): Task {
        val task = Task(nextId++, title)
        tasks.add(task)
        return task
    }

    fun completeTask(id: Int): Boolean {
        val task = tasks.find { it.id == id } ?: return false
        task.isCompleted = true
        return true
    }

    fun deleteTask(id: Int): Boolean {
        return tasks.removeIf { it.id == id }
    }

    fun getTasks(): List<Task> {
        return tasks.toList()
    }
    fun getCompletedCount(): Int {
        return tasks.count { it.isCompleted }
    }

    fun getTotalCount(): Int {
        return tasks.size
    }
}