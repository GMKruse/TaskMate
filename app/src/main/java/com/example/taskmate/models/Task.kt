package com.example.taskmate.models

data class Task(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val isCompleted: Boolean = false,
    val groupId: String = ""
)
