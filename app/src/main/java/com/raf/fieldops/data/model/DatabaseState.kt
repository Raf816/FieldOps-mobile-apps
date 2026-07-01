package com.raf.fieldops.data.model

sealed class DatabaseState<out T> {

    data object Loading : DatabaseState<Nothing>()

    data class Success<T>(val data: T) : DatabaseState<T>()

    data class Failure(val message: String) : DatabaseState<Nothing>()
}
