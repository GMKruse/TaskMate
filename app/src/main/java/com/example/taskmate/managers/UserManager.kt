package com.example.taskmate.managers

import com.example.taskmate.models.User
import com.example.taskmate.models.Email
import com.example.taskmate.repositories.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        _state.value = UserManagerState.Loading
        withContext(Dispatchers.IO) {
            val result = userRepository.login(Email(email), password)
            if (result.isSuccess) {
                val user = userRepository.getCurrentUser()
                if (user != null) {
                    _state.value = UserManagerState.LoggedIn(user)
                } else {
                    _state.value = UserManagerState.LoggedOut(error = "Failed to fetch user data")
                }
            } else {
                _state.value = UserManagerState.LoggedOut(error = result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    override fun logout() {
        userRepository.logout()
        _state.value = UserManagerState.LoggedOut()
    }
}
