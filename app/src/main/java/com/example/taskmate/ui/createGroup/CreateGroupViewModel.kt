package com.example.taskmate.ui.createGroup

import GroupRepository
import IGroupRepository
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmate.managers.userManager.IUserManager
import com.example.taskmate.models.Email
import com.example.taskmate.models.Group
import com.example.taskmate.models.User
import com.example.taskmate.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateGroupViewModel(
    private val userManager: IUserManager,
    private val groupRepository: IGroupRepository
    ) : ViewModel() {

    data class ViewState(
        val currentUser: User,
        val groupName: String,
        val memberEmails: List<Email>,
        val emailInput: String,
        val emailError: String?,
        val creatingGroup: Boolean = false
    )

    private val _viewState = MutableStateFlow<ViewState>(ViewState(
        currentUser = userManager.getCurrentUserOrLogOut(),
        groupName = "",
        memberEmails = emptyList(),
        emailInput = "",
        emailError = null,
        creatingGroup = false
    ))
    val viewState: StateFlow<ViewState> = _viewState

    fun onGroupNameChange(name: String) {
        _viewState.update { it.copy(groupName = name) }
    }

    fun onEmailInputChange(input: String) {
        _viewState.update { it.copy(emailInput = input, emailError = null) }
    }

    fun addEmail(currentUserEmail: Email) {
        val emailStr = _viewState.value.emailInput.trim()
        if (emailStr.isBlank()) return

        val emailObj = Email(emailStr)
        when {
            !isValidEmail(emailStr) -> _viewState.update { it.copy(emailError = "Invalid email") }
            emailObj == currentUserEmail -> _viewState.update { it.copy(emailError = "Can't add yourself") }
            emailObj in _viewState.value.memberEmails -> _viewState.update { it.copy(emailError = "Email already added") }
            else -> {
                _viewState.update { it.copy(
                    memberEmails = it.memberEmails + emailObj,
                    emailInput = "",
                    emailError = null
                ) }
            }
        }
    }

    fun removeEmail(email: Email) {
        _viewState.update { it.copy(
            memberEmails = it.memberEmails - email,
            emailError = null
        ) }
    }

    fun dismissEmailError() {
        _viewState.update { it.copy(emailError = null) }
    }

    fun createGroup(currentUserEmail: Email, onResult: (Boolean) -> Unit) {
        if (_viewState.value.creatingGroup) return

        val currentState = _viewState.value

        if (currentState.currentUser.email != currentUserEmail) {
            onResult(false)
            return
        }

        _viewState.update { it.copy(creatingGroup = true) }

        val filteredMembers = _viewState.value.memberEmails.filter { it != currentUserEmail }
        val members = (filteredMembers + currentUserEmail).distinct()

        val group = Group(
            name = _viewState.value.groupName,
            createdBy = currentState.currentUser.id,
            members = members,
            createdAt = System.currentTimeMillis()
        )

        groupRepository.createGroup(group) { success, errMsg ->
            if (success) {
                onResult(true)
            } else {
                onResult(false)
            }
            _viewState.update { it.copy(creatingGroup = false) }
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}