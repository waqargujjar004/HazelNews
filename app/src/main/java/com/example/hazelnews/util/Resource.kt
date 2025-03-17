package com.example.hazelnews.util

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val isLastPage: Boolean = false
) {
    class Success<T>(data: T, isLastPage: Boolean = false) : Resource<T>(data = data, isLastPage = isLastPage)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data = data, message = message)
    class Loading<T> : Resource<T>()
}