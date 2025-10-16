package com.example.taskmate.models

data class User(
    val id: UserId = UserId(""),
    val email: Email = Email(""),
    val name: String = ""
)
