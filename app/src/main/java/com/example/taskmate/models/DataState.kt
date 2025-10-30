package com.example.taskmate.models

sealed class DataState<out T, out E> {
    object Loading : DataState<Nothing, Nothing>()
    data class Data<out T>(val data: T) : DataState<T, Nothing>()
    data class Error<out E>(val error: E) : DataState<Nothing, E>()
}
