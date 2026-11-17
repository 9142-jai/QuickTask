package com.example.to_do_list


import org.junit.Assert.*
import org.junit.Test

class TaskManagerTest {

    @Test
    fun addTaskTest() {
        val manager = TaskManager()

        manager.addTask("Learn Java")

        assertEquals(1, manager.getTotalCount())
    }

    @Test
    fun completeTaskTest() {
        val manager = TaskManager()

        val task = manager.addTask("Practice DSA")
        manager.completeTask(task.id)

        assertEquals(1, manager.getCompletedCount())
    }

    @Test
    fun deleteTaskTest() {
        val manager = TaskManager()

        val task = manager.addTask("Build app")
        manager.deleteTask(task.id)

        assertEquals(0, manager.getTotalCount())
    }
}