package com.example.taskmate.ui.specificTask

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.taskmate.models.DataState
import com.example.taskmate.models.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.taskmate.repositories.ITaskRepository

class SpecificTaskViewModel(
    private val taskId: String,
    private val taskRepository: ITaskRepository
) : ViewModel() {

    data class ViewState(
        val task: DataState<Task, String> = DataState.Loading
    )

    private val _viewState = MutableStateFlow<ViewState>(ViewState())
    val viewState: StateFlow<ViewState> = _viewState

    init {
        fetchTask()
    }

    private fun fetchTask() {
        viewModelScope.launch {
            _viewState.update { it.copy(task = DataState.Loading) }
            try {
                val task = taskRepository.getTaskById(taskId)
                if (task != null) {
                    _viewState.update { it.copy(task = DataState.Data(task)) }
                } else {
                    _viewState.update { it.copy(task = DataState.Error("Task not found")) }
                }
            } catch (e: Exception) {
                _viewState.update { it.copy(task = DataState.Error(e.message ?: "Unknown error")) }
            }
        }
    }

    fun setTaskCompleted(completed: Boolean) {
        viewModelScope.launch {
            val currentTask = when (val taskState = _viewState.value.task) {
                is DataState.Data -> taskState.data
                else -> return@launch
            }
            val success = taskRepository.updateTaskCompletion(currentTask.id, completed)
            if (success) {
                _viewState.update { it.copy(task = DataState.Data(currentTask.copy(isCompleted = completed))) }
            } else {
                _viewState.update { it.copy(task = DataState.Error("Failed to update task")) }
            }
        }
    }

    fun deleteTask(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val currentTask = when (val taskState = _viewState.value.task) {
                is DataState.Data -> taskState.data
                else -> return@launch
            }
            val success = taskRepository.deleteTask(currentTask.id)
            onResult(success)
        }
    }
}