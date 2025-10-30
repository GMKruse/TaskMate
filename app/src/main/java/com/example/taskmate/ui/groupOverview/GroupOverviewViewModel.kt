package com.example.taskmate.ui.groupOverview

import IGroupRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmate.models.Group
import com.example.taskmate.models.User
import com.example.taskmate.models.ViewState
import com.example.taskmate.repositories.IUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GroupOverviewViewModel(
    private val userRepository: IUserRepository,
    private val groupRepository: IGroupRepository
) : ViewModel() {

    data class DataState(
        val user: User,
        val groups: List<Group>
    )

    private val _viewState = MutableStateFlow<ViewState<DataState, String>>(ViewState.Loading)
    val viewState: StateFlow<ViewState<DataState, String>> = _viewState

    private var stopGroupsListener: (() -> Unit)? = null

    init {
        fetchUser()
    }

    private fun fetchUser() {
        viewModelScope.launch {
            _viewState.update { ViewState.Loading }
            try {
                val user = userRepository.getCurrentUser()
                if (user != null) {
                    startGroupsListener(user)
                } else {
                    _viewState.update { ViewState.Error("No user logged in") }
                }
            } catch (_: Exception) {
                _viewState.update { ViewState.Error("Failed to load user") }
            }
        }
    }

    private fun startGroupsListener(user: User) {
        // Stop existing listener if any
        stopGroupsListener?.invoke()

        _viewState.update { ViewState.Loading }

        // Start new real-time listener
        stopGroupsListener = groupRepository.listenToGroupsForUser(user.email) { groups ->
            _viewState.update { ViewState.Data(DataState(user = user, groups = groups)) }
        }
    }

    fun refreshGroups() {
        when (val s = _viewState.value) {
            is ViewState.Data -> {
                val data = s.data
                startGroupsListener(data.user)
            }
            else -> fetchUser()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopGroupsListener?.invoke() // Stop listening when ViewModel is destroyed
    }
}
