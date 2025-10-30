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
        val isRegistering: Boolean = false
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

    fun register(onResult: (Boolean) -> Unit) {
        // Prevent multiple simultaneous registration attempts
        if (_viewState.value.isRegistering) return

        val currentState = _viewState.value

        // Validate inputs
        when {
            currentState.email.isBlank() -> {
                _viewState.update { it.copy(errorMessage = "Email is required") }
                onResult(false)
                return
            }
            !isValidEmail(currentState.email) -> {
                _viewState.update { it.copy(errorMessage = "Invalid email format") }
                onResult(false)
                return
            }
            currentState.password.isBlank() -> {
                _viewState.update { it.copy(errorMessage = "Password is required") }
                onResult(false)
                return
            }
            !isValidPassword(currentState.password) -> {
                _viewState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
                onResult(false)
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

            if (errorMessage == null) {
                // Success
                onResult(true)
            } else {
                // Failure
                _viewState.update { it.copy(
                    errorMessage = errorMessage,
                    isRegistering = false
                ) }
                onResult(false)
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