package com.example.taskmate.ui.specificTask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmate.models.Task
import com.example.taskmate.models.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.taskmate.repositories.ITaskRepository

class SpecificTaskViewModel(
    private val taskId: String,
    private val taskRepository: ITaskRepository // To be implemented by your team
) : ViewModel() {

    private val _viewState = MutableStateFlow<ViewState<Task, String>>(ViewState.Loading)
    val viewState: StateFlow<ViewState<Task, String>> = _viewState

    init {
        fetchTask()
    }

    private fun fetchTask() {
        viewModelScope.launch {
            _viewState.update { ViewState.Loading }
            try {
                val task = taskRepository.getTaskById(taskId) // suspend fun
                if (task != null) {
                    _viewState.update { ViewState.Data(task) }
                } else {
                    _viewState.update { ViewState.Error("Task not found") }
                }
            } catch (e: Exception) {
                _viewState.update { ViewState.Error("Failed to load task") }
            }
        }
    }

    fun setTaskCompleted(completed: Boolean) {
        val currentTask = (viewState.value as? ViewState.Data)?.data ?: return
        viewModelScope.launch {
            val success = taskRepository.updateTaskCompletion(currentTask.id, completed)
            if (success) {
                // Update local state
                _viewState.update { ViewState.Data(currentTask.copy(isCompleted = completed)) }
            } else {
                _viewState.update { ViewState.Error("Failed to update task") }
            }
        }
    }

    fun deleteTask(onResult: (Boolean) -> Unit) {
        val currentTask = (viewState.value as? ViewState.Data)?.data ?: return
        viewModelScope.launch {
            val success = taskRepository.deleteTask(currentTask.id)
            onResult(success)
        }
    }
}