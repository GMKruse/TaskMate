package com.example.taskmate.ui.createGroup

import GroupRepository
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmate.models.Email
import com.example.taskmate.models.Group
import com.example.taskmate.models.User
import com.example.taskmate.models.ViewState
import com.example.taskmate.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateGroupViewModel : ViewModel() {
    // DataState contains only loaded data (currentUser). When viewState is Data, currentUser is non-null.
    data class DataState(
        val currentUser: User
    )

    private val groupRepository = GroupRepository()
    private val userRepository = UserRepository()

    // Inputs owned by the view
    private val _groupName = MutableStateFlow("")
    val groupName: StateFlow<String> = _groupName

    private val _emailInput = MutableStateFlow("")
    val emailInput: StateFlow<String> = _emailInput

    private val _memberEmails = MutableStateFlow<List<Email>>(emptyList())
    val memberEmails: StateFlow<List<Email>> = _memberEmails

    private val _emailError = MutableStateFlow<String?>(null)
    val emailError: StateFlow<String?> = _emailError

    private val _creatingGroup = MutableStateFlow(false)
    val creatingGroup: StateFlow<Boolean> = _creatingGroup

    private val _viewState = MutableStateFlow<ViewState<DataState, String>>(ViewState.Loading)
    val viewState: StateFlow<ViewState<DataState, String>> = _viewState

    init {
        fetchCurrentUser()
    }

    private fun fetchCurrentUser() {
        viewModelScope.launch {
            _viewState.update { ViewState.Loading }
            try {
                val user = userRepository.getCurrentUser()
                if (user != null) {
                    _viewState.update { ViewState.Data(DataState(currentUser = user)) }
                } else {
                    _viewState.update { ViewState.Error("No user logged in") }
                }
            } catch (_: Exception) {
                _viewState.update { ViewState.Error("Failed to load current user") }
            }
        }
    }

    fun onGroupNameChange(name: String) {
        _groupName.value = name
    }

    fun onEmailInputChange(input: String) {
        _emailInput.value = input
        _emailError.value = null
    }

    // Now requires the current user's email passed in from the view (non-nullable)
    fun addEmail(currentUserEmail: Email) {
        val emailStr = _emailInput.value.trim()
        if (emailStr.isBlank()) return

        val emailObj = Email(emailStr)
        when {
            !isValidEmail(emailStr) -> _emailError.value = "Invalid email"
            emailObj == currentUserEmail -> _emailError.value = "Can't add yourself"
            emailObj in _memberEmails.value -> _emailError.value = "Email already added"
            else -> {
                _memberEmails.update { it + emailObj }
                _emailInput.value = ""
                _emailError.value = null
            }
        }
    }

    fun removeEmail(email: Email) {
        _memberEmails.update { it - email }
        _emailError.value = null
    }

    fun dismissEmailError() {
        _emailError.value = null
    }

    // createGroup now requires the current user's email to be passed in from the view
    fun createGroup(currentUserEmail: Email, onResult: (Boolean) -> Unit) {
        // Prevent creating while an operation is already in progress
        if (_creatingGroup.value) return

        // Ensure we still have Data state with a currentUser - the caller should only call when the view has loaded it
        val dataState = when (val s = _viewState.value) {
            is ViewState.Data -> s.data
            else -> null
        }

        if (dataState == null) {
            _viewState.update { ViewState.Error("No current user") }
            onResult(false)
            return
        }

        // Prevent mismatch: ensure the passed email matches the loaded user
        if (dataState.currentUser.email != currentUserEmail) {
            _viewState.update { ViewState.Error("Current user mismatch") }
            onResult(false)
            return
        }

        // set creating flag so UI can disable inputs and show a button spinner without dropping Data state
        _creatingGroup.update { true }

        val filteredMembers = _memberEmails.value.filter { it != currentUserEmail }
        val members = (filteredMembers + currentUserEmail).distinct()

        val group = Group(
            name = _groupName.value,
            createdBy = dataState.currentUser.id,
            members = members,
            createdAt = System.currentTimeMillis()
        )

        groupRepository.createGroup(group) { success, errMsg ->
            if (success) {
                onResult(true)
            } else {
                _viewState.update { ViewState.Error(errMsg ?: "Failed to create group") }
                onResult(false)
            }
            _creatingGroup.update { false }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}