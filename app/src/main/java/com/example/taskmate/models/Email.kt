package com.example.taskmate.models

@JvmInline
value class Email(val value: String) {
    override fun toString(): String = value
}
