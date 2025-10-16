package com.example.taskmate.models

data class Group(
    val id: String = "",
    val name: String = "",
    val createdBy: UserId = UserId(""),
    val members: List<Email> = emptyList(),
    val createdAt: Long = 0L
)
