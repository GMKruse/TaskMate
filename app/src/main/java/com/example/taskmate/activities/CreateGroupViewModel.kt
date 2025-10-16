package com.example.taskmate.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmate.models.Group
import com.example.taskmate.models.Email
import com.example.taskmate.models.User
import com.example.taskmate.repositories.UserRepository
import GroupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data class to hold all state
data class DataState(
    val groupName: String = "",
    val emailInput: String = "",
    val memberEmails: List<Email> = emptyList(),
    val isLoading: Boolean = false,
    val emailError: Boolean = false,
    val currentUser: User? = null
)

class CreateGroupViewModel : ViewModel() {
    private val groupRepository = GroupRepository()
    private val userRepository = UserRepository()

    private val _dataState = MutableStateFlow(DataState())
    val dataState: StateFlow<DataState> = _dataState

    init {
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            val user = userRepository.getCurrentUser()
            _dataState.update { it.copy(currentUser = user) }
        }
    }

    fun onGroupNameChange(name: String) {
        _dataState.update { it.copy(groupName = name) }
    }

    fun onEmailInputChange(input: String) {
        _dataState.update { it.copy(emailInput = input, emailError = false) }
    }

    fun addEmail() {
        val state = _dataState.value
        val emailStr = state.emailInput
        val emailObj = Email(emailStr)
        val current = state.currentUser
        if (emailStr.isNotBlank() && current != null && emailObj != current.email) {
            if (isValidEmail(emailStr) && emailObj !in state.memberEmails && emailObj != current.email) {
                _dataState.update {
                    it.copy(
                        memberEmails = it.memberEmails + emailObj,
                        emailInput = "",
                        emailError = false
                    )
                }
            } else {
                _dataState.update { it.copy(emailError = true) }
            }
        }
    }

    fun removeEmail(email: Email) {
        _dataState.update { it.copy(memberEmails = it.memberEmails - email) }
    }

    fun createGroup(onResult: (Boolean) -> Unit) {
        val state = _dataState.value
        val current = state.currentUser ?: return
        _dataState.update { it.copy(isLoading = true) }
        val filteredMembers = state.memberEmails.filter { it != current.email }
        val members = (filteredMembers + current.email).distinct()
        val group = Group(
            name = state.groupName,
            createdBy = current.id,
            members = members,
            createdAt = System.currentTimeMillis()
        )
        groupRepository.createGroup(group) { success, _ ->
            _dataState.update { it.copy(isLoading = false) }
            onResult(success)
        }
    }

    fun dismissEmailError() {
        _dataState.update { it.copy(emailError = false) }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}
