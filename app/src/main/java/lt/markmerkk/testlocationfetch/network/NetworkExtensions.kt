package lt.markmerkk.testlocationfetch.network

import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.ApiSuccessModelMapper
import com.skydoves.sandwich.map
import lt.markmerkk.testlocationfetch.network.exceptions.ApiException
import lt.markmerkk.testlocationfetch.network.exceptions.NetworkConnectionException
import lt.markmerkk.testlocationfetch.network.NetworkErrorHandler.Companion.toApiException
import lt.markmerkk.testlocationfetch.network.exceptions.UnknownErrorException

@SuppressWarnings("TooGenericExceptionCaught")
fun <T> ApiResponse<T>.safeCall(
    networkErrorHandler: NetworkErrorHandler,
): DataResult<T> {
    return when (this) {
        is ApiResponse.Success -> {
            DataResult.Success(data)
        }
        is ApiResponse.Failure.Error -> {
            DataResult.Error(createApiException(networkErrorHandler))
        }
        is ApiResponse.Failure.Exception -> {
            DataResult.Error(networkErrorHandler.adaptNetworkException(exception))
        }
    }
}

@Throws(
    ApiException::class,
    NetworkConnectionException::class,
    UnknownErrorException::class,
)
@SuppressWarnings("TooGenericExceptionCaught")
fun <T, V> ApiResponse<T>.safeCall(
    networkErrorHandler: NetworkErrorHandler,
    mapper: ApiSuccessModelMapper<T, V>
): DataResult<V> {
    return when (this) {
        is ApiResponse.Success -> {
            DataResult.Success(mapper.map(this))
        }
        is ApiResponse.Failure.Error -> {
            DataResult.Error(createApiException(networkErrorHandler))
        }
        is ApiResponse.Failure.Exception -> {
            DataResult.Error(networkErrorHandler.adaptNetworkException(exception))
        }
    }
}

fun <T> ApiResponse.Failure.Error<T>.createApiException(networkErrorHandler: NetworkErrorHandler) =
    map(networkErrorHandler.errorResponseMapper).toApiException()
