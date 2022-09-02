package lt.markmerkk.locaping.network

import com.skydoves.sandwich.ApiResponse
import lt.markmerkk.locaping.network.requests.ReqPingDetail
import retrofit2.http.Body
import retrofit2.http.POST

@SuppressWarnings("TooManyFunctions", "LongParameterList")
interface Api {

    @POST("/api/v1/ping")
    suspend fun postPing(
        @Body content: String,
    ): ApiResponse<String>

    @POST("/api/v1/pingDetail")
    suspend fun postPingDetail(
        @Body content: ReqPingDetail,
    ): ApiResponse<String>
}
