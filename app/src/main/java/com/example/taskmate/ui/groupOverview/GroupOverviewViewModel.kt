package com.example.taskmate.ui.groupOverview

import IGroupRepository
import androidx.lifecycle.ViewModel
import com.example.taskmate.managers.userManager.IUserManager
import com.example.taskmate.models.DataState
import com.example.taskmate.models.Group
import com.example.taskmate.models.User
import com.example.taskmate.services.IQuoteService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class GroupOverviewViewModel(
    private val userManager: IUserManager,
    private val groupRepository: IGroupRepository,
    private val quoteService: IQuoteService
) : ViewModel() {

    data class ViewState(
        val user: User,
        val groups: DataState<List<Group>, Nothing?>,
        val quote: DataState<String, Nothing?>
    )

    private val _viewState = MutableStateFlow(
        ViewState(
            user = userManager.getCurrentUserOrLogOut(),
            groups = DataState.Loading,
            quote = DataState.Loading
        )
    )
    val viewState: StateFlow<ViewState> = _viewState

    init {
        fetchGroups(_viewState.value.user)
        fetchQuote()
    }

    private fun fetchGroups(user: User) {
        _viewState.update { it.copy(groups = DataState.Loading) }
        groupRepository.fetchGroupsForUser(user.email) { groups ->
            _viewState.update { it.copy(groups = DataState.Data(groups)) }
        }
    }

    private fun fetchQuote() {
        _viewState.update { it.copy(quote = DataState.Loading) }
        quoteService.fetchTodayQuote(
            onSuccess = { quote ->
                _viewState.update { it.copy(quote = DataState.Data(quote)) }
            },
            onError = { error ->
                _viewState.update { it.copy(quote = DataState.Data("Just do it!")) }
            }
        )
    }

    fun refreshGroups() {
        fetchGroups(_viewState.value.user)
    }
}