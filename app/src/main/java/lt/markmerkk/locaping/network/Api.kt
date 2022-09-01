package lt.markmerkk.locaping.network

import com.skydoves.sandwich.ApiResponse
import retrofit2.http.Body
import retrofit2.http.POST

@SuppressWarnings("TooManyFunctions", "LongParameterList")
interface Api {

    @JvmSuppressWildcards
    @POST("/api/v1/ping")
    suspend fun postPing(
        @Body content: String,
    ): ApiResponse<String>
}
