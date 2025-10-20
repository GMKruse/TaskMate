package com.example.taskmate.managers.userManager

import com.example.taskmate.models.Email
import com.example.taskmate.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.withContext

class UserManager private constructor() : IUserManager {
    private val _state = MutableStateFlow<UserManagerState>(UserManagerState.LoggedOut())
    override val state: StateFlow<UserManagerState> = _state.asStateFlow()
    private val userRepository = UserRepository()

    companion object {
        @Volatile
        private var INSTANCE: UserManager? = null
        fun getInstance(): UserManager = INSTANCE ?: synchronized(this) {
            INSTANCE ?: UserManager().also { INSTANCE = it }
        }
    }

    override suspend fun login(email: String, password: String) {
        _state.update { UserManagerState.Loading }

        try {
            val result = withContext(Dispatchers.IO) {
                userRepository.login(Email(email), password)
            }

            if (result.isSuccess) {
                val user = withContext(Dispatchers.IO) {
                    userRepository.getCurrentUser()
                }
                if (user != null) {
                    _state.update { UserManagerState.LoggedIn(user) }
                } else {
                    _state.update { UserManagerState.LoggedOut(error = "Failed to fetch user data") }
                }
            } else {
                _state.update { UserManagerState.LoggedOut(error = "Login failed") }
            }
        } catch (e: Exception) {
            _state.update { UserManagerState.LoggedOut(error = "An unexpected error occurred") }
        }
    }

    override suspend fun register(name: String, email: String, password: String) {
        _state.update { UserManagerState.Loading }

        try {
            val result = withContext(Dispatchers.IO) {
                userRepository.register(name, Email(email), password)
            }

            if (result.isSuccess) {
                val user = withContext(Dispatchers.IO) {
                    userRepository.getCurrentUser()
                }
                if (user != null) {
                    _state.update { UserManagerState.LoggedIn(user) }
                } else {
                    _state.update { UserManagerState.LoggedOut(error = "Failed to fetch user data") }
                }
            } else {
                _state.update { UserManagerState.LoggedOut(error = result.exceptionOrNull()?.message ?: "Registration failed") }
            }
        } catch (e: Exception) {
            _state.update { UserManagerState.LoggedOut(error = e.message ?: "An unexpected error occurred") }
        }
    }

    override fun logout() {
        userRepository.logout()
        _state.update { UserManagerState.LoggedOut() }
    }
}