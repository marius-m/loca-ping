package lt.markmerkk.testlocationfetch.network

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.skydoves.sandwich.ApiErrorModelMapper
import com.skydoves.sandwich.ApiResponse
import lt.markmerkk.testlocationfetch.network.exceptions.ApiException
import lt.markmerkk.testlocationfetch.network.exceptions.NetworkConnectionException
import lt.markmerkk.testlocationfetch.network.entities.ErrorResponse
import lt.markmerkk.testlocationfetch.network.exceptions.UnknownErrorException
import java.io.IOException
import timber.log.Timber

class NetworkErrorHandler(
    objectMapper: ObjectMapper
) {

    val errorResponseMapper = ErrorMapper(objectMapper)

    fun adaptNetworkException(throwable: Throwable): Throwable {
        return if (throwable is IOException) {
            NetworkConnectionException(throwable)
        } else {
            UnknownErrorException(throwable)
        }
    }

    class ErrorMapper(
        private val objectMapper: ObjectMapper
    ) : ApiErrorModelMapper<ErrorResponse> {

        override fun map(apiErrorResponse: ApiResponse.Failure.Error<*>): ErrorResponse {
            val response = apiErrorResponse.errorBody!!.string()
            return try {
                val errorResponse = objectMapper.readValue(response, ErrorResponse::class.java)
                errorResponse
            } catch (e: JsonMappingException) {
                Timber.w(e)
                createUnknownError(apiErrorResponse)
            } catch (e: JsonParseException) {
                Timber.w(e)
                createUnknownError(apiErrorResponse)
            }
        }

        private fun createUnknownError(apiErrorResponse: ApiResponse.Failure.Error<*>) =
            ErrorResponse("unknown error", apiErrorResponse.statusCode.code)
    }

    companion object {
        fun ErrorResponse.toApiException(): ApiException {
            return ApiException(message)
        }
    }
}
