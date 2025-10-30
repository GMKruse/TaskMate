package com.example.taskmate.ui.LoginScreen

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmate.managers.userManager.IUserManager
import com.example.taskmate.managers.userManager.UserManagerState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginScreenViewModel(
    private val userManager: IUserManager
) : ViewModel() {

    data class ViewState(
        val email: String = "",
        val password: String = "",
        val passwordVisible: Boolean = false,
        val errorMessage: String? = null,
        val isLoggingIn: Boolean = false
    )

    private val _viewState = MutableStateFlow(ViewState())
    val viewState: StateFlow<ViewState> = _viewState

    init {
        // Observe UserManager state for errors
        viewModelScope.launch {
            userManager.state.collect { state ->
                when (state) {
                    is UserManagerState.LoggedOut -> {
                        _viewState.update { it.copy(
                            isLoggingIn = false,
                            errorMessage = state.error
                        ) }
                    }
                    else -> {}
                }
            }
        }
    }

    fun onEmailChange(email: String) {
        _viewState.update { it.copy(email = email, errorMessage = null) }
    }

    fun onPasswordChange(password: String) {
        _viewState.update { it.copy(password = password, errorMessage = null) }
    }

    fun togglePasswordVisibility() {
        _viewState.update { it.copy(passwordVisible = !it.passwordVisible) }
    }

    fun dismissError() {
        _viewState.update { it.copy(errorMessage = null) }
    }

    fun login() {
        if (_viewState.value.isLoggingIn) return

        val currentState = _viewState.value

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
        }

        viewModelScope.launch {
            userManager.login(
                email = currentState.email,
                password = currentState.password
            )
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
