package com.example.taskmate.models

data class Group(
    val id: String = "",
    val name: String = "",
    val createdBy: String = "",
    val members: List<String> = emptyList(),
    val createdAt: Long = 0L
)
