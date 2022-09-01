package lt.markmerkk.testlocationfetch.repositories

import lt.markmerkk.testlocationfetch.network.Api
import lt.markmerkk.testlocationfetch.network.DataResult
import lt.markmerkk.testlocationfetch.network.NetworkErrorHandler
import lt.markmerkk.testlocationfetch.network.safeCall

class HomeRepository(
    private val networkErrorHandler: NetworkErrorHandler,
    private val api: Api,
) {
    suspend fun postPing(content: String): DataResult<String> {
        return api.postPing(content)
            .safeCall(networkErrorHandler)
    }
}