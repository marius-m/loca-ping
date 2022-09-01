package lt.markmerkk.testlocationfetch.network

import com.skydoves.sandwich.ApiResponse
import retrofit2.http.POST

@SuppressWarnings("TooManyFunctions", "LongParameterList")
interface Api {

    @JvmSuppressWildcards
    @POST("/api/v1/ping")
    suspend fun postFreightComment(): ApiResponse<String>
}
