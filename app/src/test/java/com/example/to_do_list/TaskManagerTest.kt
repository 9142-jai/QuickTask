package com.example.to_do_list

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
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

    @Test
    fun addTaskTrimsTitleAndRejectsBlankTitle() {
        val manager = TaskManager()
        val task = manager.addTask("  Read notes  ")
        assertEquals("Read notes", task.title)
        assertFalse(runCatching { manager.addTask("  ") }.isSuccess)
        assertEquals(1, manager.getTotalCount())
    }

    @Test
    fun editAndToggleTaskPreserveMetadata() {
        val manager = TaskManager()
        val task = manager.addTask("Draft", category = "College", priority = "High", dueDateEpochDay = 20)

        assertTrue(manager.editTask(task.copy(title = "  Final draft ")))
        assertTrue(manager.setCompleted(task.id, true))
        val updated = manager.getTasks().single()
        assertEquals("Final draft", updated.title)
        assertEquals("College", updated.category)
        assertEquals("High", updated.priority)
        assertEquals(20L, updated.dueDateEpochDay)
        assertTrue(updated.isCompleted)
        assertTrue(manager.setCompleted(task.id, false))
        assertEquals(1, manager.getPendingCount())
    }

    @Test
    fun filtersCombineQueryCategoryPriorityAndOverdueStatus() {
        val manager = TaskManager()
        manager.addTask("Submit report", "Work", "High", 9)
        manager.addTask("Read report", "Personal", "Low", 11)
        manager.addTask("Report submitted", "Work", "High", 8).also {
            manager.completeTask(it.id)
        }

        val filtered = manager.getTasks(
            query = "REPORT",
            category = "Work",
            priority = "High",
            status = "Overdue",
            todayEpochDay = 10
        )
        assertEquals(1, filtered.size)
        assertEquals("Submit report", filtered.single().title)
        assertEquals(1, manager.getOverdueCount(todayEpochDay = 10))
    }

    @Test
    fun editRejectsBlankTitleAndMissingTask() {
        val manager = TaskManager()
        val task = manager.addTask("Keep title")
        assertFalse(manager.editTask(task.copy(title = "   ")))
        assertFalse(manager.editTask(Task(id = 99, title = "Missing")))
        assertNull(manager.getTasks().firstOrNull { it.id == 99 })
    }
}
