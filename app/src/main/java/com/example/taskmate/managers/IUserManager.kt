package com.example.taskmate.managers

import com.example.taskmate.models.User
import kotlinx.coroutines.flow.StateFlow

sealed class UserManagerState {
    object Loading : UserManagerState()
    data class LoggedIn(val user: User) : UserManagerState()
    data class LoggedOut(val error: String? = null) : UserManagerState()
}

interface IUserManager {
    val state: StateFlow<UserManagerState>
    suspend fun login(email: String, password: String)
    fun logout()
}
