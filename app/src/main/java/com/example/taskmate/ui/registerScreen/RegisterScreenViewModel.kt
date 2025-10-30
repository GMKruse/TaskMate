package com.example.taskmate.ui.registerScreen

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmate.managers.userManager.IUserManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterScreenViewModel(
    private val userManager: IUserManager
) : ViewModel() {

    data class ViewState(
        val name: String = "",
        val email: String = "",
        val password: String = "",
        val errorMessage: String? = null,
        val isRegistering: Boolean = false,
        val userRegistered: String? = null
    )

    private val _viewState = MutableStateFlow(ViewState())
    val viewState: StateFlow<ViewState> = _viewState

    fun onNameChange(name: String) {
        _viewState.update { it.copy(name = name) }
    }

    fun onEmailChange(email: String) {
        _viewState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _viewState.update { it.copy(password = password, errorMessage = null) }
    }

    fun dismissError() {
        _viewState.update { it.copy(errorMessage = null) }
    }

    fun dismissUserRegistered() {
        _viewState.update { it.copy(userRegistered = null) }
    }

    fun register() {
        // Prevent multiple simultaneous registration attempts
        if (_viewState.value.isRegistering) return

        val currentState = _viewState.value

        // Validate inputs
        when {
            currentState.email.isBlank() -> {
                _viewState.update { it.copy(errorMessage = "Email is required") }
                return
            }
            !isValidEmail(currentState.email) -> {
                _viewState.update { it.copy(errorMessage = "Invalid email format") }
                return
            }
            currentState.password.isBlank() -> {
                _viewState.update { it.copy(errorMessage = "Password is required") }
                return
            }
            !isValidPassword(currentState.password) -> {
                _viewState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
                return
            }
        }

        // Set registering flag to disable UI
        _viewState.update { it.copy(isRegistering = true, errorMessage = null) }

        viewModelScope.launch {
            val errorMessage = userManager.register(
                name = currentState.name.ifBlank { "User" },
                email = currentState.email,
                password = currentState.password
            )

            if (errorMessage != null) {
                // Failure
                _viewState.update { it.copy(
                    errorMessage = errorMessage,
                    isRegistering = false
                ) }
            } else {
                // Success
                _viewState.update { it.copy(
                    name = "",
                    email = "",
                    password = "",
                    userRegistered = currentState.email,
                    isRegistering = false
                ) }
            }

        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
}