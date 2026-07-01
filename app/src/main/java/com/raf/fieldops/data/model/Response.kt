package com.raf.fieldops.data.model

sealed class Response {

    data object Startup : Response()

    data object Loading : Response()

    data object Success : Response()

    data class Failure(val e: Exception) : Response()

    data object NotConfirmed : Response()
}
