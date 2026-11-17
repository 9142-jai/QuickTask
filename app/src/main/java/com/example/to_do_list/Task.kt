package com.example.to_do_list

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class Task(
    @field:PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val isCompleted: Boolean = false,
    val category: String = "Personal",
    val priority: String = "Medium",
    val dueDateEpochDay: Long? = null
)
