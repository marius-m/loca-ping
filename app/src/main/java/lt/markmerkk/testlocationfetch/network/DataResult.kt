package lt.markmerkk.testlocationfetch.network

sealed class DataResult<out T> {
    data class Success<T>(val result: T) : DataResult<T>()
    data class Error(val throwable: Throwable) : DataResult<Nothing>()

    inline fun onSuccess(function: (T) -> Unit): DataResult<T> {
        if (this is Success) {
            function(result)
        }
        return this
    }

    inline fun onError(function: (Throwable) -> Unit): DataResult<T> {
        if (this is Error) {
            function(throwable)
        }
        return this
    }

    inline fun <Y> map(function: (T) -> DataResult<Y>): DataResult<Y> {
        return when (this) {
            is Error -> this
            is Success -> function(this.result)
        }
    }

    inline fun <Y> mapResult(function: (T) -> Y): DataResult<Y> {
        return when (this) {
            is Error -> this
            is Success -> Success(function(this.result))
        }
    }

    fun takeOrThrow(): T {
        when (this) {
            is Error -> throw throwable
            is Success -> return result
        }
    }
}
