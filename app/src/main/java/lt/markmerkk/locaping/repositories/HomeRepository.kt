package lt.markmerkk.locaping.repositories

import lt.markmerkk.locaping.network.Api
import lt.markmerkk.locaping.network.DataResult
import lt.markmerkk.locaping.network.NetworkErrorHandler
import lt.markmerkk.locaping.network.safeCall

class HomeRepository(
    private val networkErrorHandler: NetworkErrorHandler,
    private val api: Api,
) {
    suspend fun postPing(content: String): DataResult<String> {
        return api.postPing(content)
            .safeCall(networkErrorHandler)
    }
}