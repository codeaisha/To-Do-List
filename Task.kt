package com.example.todolist

data class Task(
    val title: String,
    val description: String,
    val date: String, // Date when the task was added
    val time: String  // Time when the task was added
)
