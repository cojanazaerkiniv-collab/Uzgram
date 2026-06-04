package com.uzgram.messenger.utils

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
    val isLoading get() = this is Loading

    fun getOrNull(): T? = if (this is Success) data else null

    fun getOrThrow(): T = when (this) {
        is Success -> data
        is Error -> throw Exception(message)
        is Loading -> throw IllegalStateException("Still loading")
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (String) -> Unit): Result<T> {
        if (this is Error) action(message)
        return this
    }

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
        is Loading -> Loading
    }
}

suspend fun <T> safeApiCall(block: suspend () -> T): Result<T> {
    return try {
        Result.Success(block())
    } catch (e: retrofit2.HttpException) {
        val code = e.code()
        val message = when (code) {
            400 -> "Bad request"
            401 -> "Unauthorized"
            403 -> "Forbidden"
            404 -> "Not found"
            409 -> "Conflict — already exists"
            429 -> "Too many requests"
            500 -> "Server error"
            else -> e.message ?: "Unknown error"
        }
        Result.Error(message, code)
    } catch (e: java.net.SocketTimeoutException) {
        Result.Error("Connection timed out")
    } catch (e: java.io.IOException) {
        Result.Error("No internet connection")
    } catch (e: Exception) {
        Result.Error(e.message ?: "Unknown error")
    }
}
