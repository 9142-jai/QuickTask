package com.example.to_do_list

data class Task(
    val id: Int,
    val title: String,
    var isCompleted: Boolean = false
)