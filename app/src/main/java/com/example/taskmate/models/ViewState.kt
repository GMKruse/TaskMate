package com.example.taskmate.models

sealed class ViewState<out T, out E> {
    object Loading : ViewState<Nothing, Nothing>()
    data class Data<out T>(val data: T) : ViewState<T, Nothing>()
    data class Error<out E>(val error: E) : ViewState<Nothing, E>()
}

