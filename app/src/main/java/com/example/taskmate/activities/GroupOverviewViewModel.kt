package com.example.taskmate.activities

import IGroupRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmate.models.Group
import com.example.taskmate.repositories.IUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupOverviewViewModel(
    userRepository: IUserRepository,
    private val groupRepository: IGroupRepository
) : ViewModel() {
    // Data class to hold all state for group overview
    data class ViewState(
        val userName: String = "",
        val isLoading: Boolean = false,
        val groups: List<Group> = emptyList(),
        val error: String? = null
    )

    private val userRepository = userRepository

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
                _viewState.update { it.copy(userName = user?.name ?: "User") }
                if (user != null) {
                    fetchGroups(user.id.value)
                } else {
                    _viewState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(error = "Failed to load user", isLoading = false) }
            }
        }
    }

    private fun fetchGroups(userId: String) {
        _viewState.update { it.copy(isLoading = true) }
        groupRepository.fetchGroupsForUser(userId) { groups ->
            _viewState.update { it.copy(groups = groups, isLoading = false) }
        }
    }

    fun clearError() {
        _viewState.update { it.copy(error = null) }
    }
}
