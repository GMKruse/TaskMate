package com.example.taskmate.managers.userManager

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
    suspend fun register(name: String, email: String, password: String): String?
    fun logout()
    fun getCurrentUserOrLogOut(): User
}
