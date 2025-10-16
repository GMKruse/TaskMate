package com.example.taskmate.activities

import IGroupRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmate.models.Email
import com.example.taskmate.models.Group
import com.example.taskmate.models.User
import com.example.taskmate.repositories.IUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupOverviewViewModel(
    private val userRepository: IUserRepository,
    private val groupRepository: IGroupRepository
) : ViewModel() {

    data class ViewState(
        val user: User? = null,
        val isLoading: Boolean = false,
        val groups: List<Group> = emptyList(),
        val error: String? = null
    )

    private val _viewState = MutableStateFlow(ViewState())
    val viewState: StateFlow<ViewState> = _viewState

    init {
        fetchUser()
    }

    private fun fetchUser() {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true) }
            try {
                val user = userRepository.getCurrentUser()
                _viewState.update { it.copy(user) }
                if (user != null) {
                    fetchGroups(user.email)
                } else {
                    _viewState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(error = "Failed to load user", isLoading = false) }
            }
        }
    }

    private fun fetchGroups(email: Email) {
        _viewState.update { it.copy(isLoading = true) }
        groupRepository.fetchGroupsForUser(email) { groups ->
            _viewState.update { it.copy(groups = groups, isLoading = false) }
        }
    }

    fun refreshGroups() {
        if (_viewState.value.user != null) {
            fetchGroups(_viewState.value.user!!.email)
        } else {
            fetchUser()
        }
    }

    fun clearError() {
        _viewState.update { it.copy(error = null) }
    }
}
