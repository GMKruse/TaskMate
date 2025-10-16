package com.example.taskmate.activities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmate.models.Group
import com.example.taskmate.repositories.IUserRepository
import com.example.taskmate.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupOverviewViewModel(userRepository: IUserRepository) : ViewModel() {
    // Data class to hold all state for group overview
    data class ViewState(
        val userName: String = "",
        val isLoading: Boolean = false,
        val groups: List<Group> = emptyList(),
        val error: String? = null
    )

    private val userRepository = userRepository
    // TODO: Replace with actual group repository
    // private val groupRepository = GroupRepository()

    private val _viewState = MutableStateFlow(ViewState())
    val viewState: StateFlow<ViewState> = _viewState

    init {
        fetchUser()
        // fetchGroups() // Uncomment and implement when group fetching is available
    }

    private fun fetchUser() {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true) }
            try {
                val user = userRepository.getCurrentUser()
                _viewState.update { it.copy(userName = user?.name ?: "User", isLoading = false) }
            } catch (e: Exception) {
                _viewState.update { it.copy(error = "Failed to load user", isLoading = false) }
            }
        }
    }

    // Example stub for group fetching
    /*
    private fun fetchGroups() {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true) }
            try {
                val groups = groupRepository.getGroupsForUser()
                _viewState.update { it.copy(groups = groups, isLoading = false) }
            } catch (e: Exception) {
                _viewState.update { it.copy(error = "Failed to load groups", isLoading = false) }
            }
        }
    }
    */

    fun clearError() {
        _viewState.update { it.copy(error = null) }
    }
}
