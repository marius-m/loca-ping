package lt.markmerkk.locaping.repositories

import lt.markmerkk.locaping.AppDateTimeUtils
import lt.markmerkk.locaping.network.Api
import lt.markmerkk.locaping.network.DataResult
import lt.markmerkk.locaping.network.NetworkErrorHandler
import lt.markmerkk.locaping.network.requests.ReqPingDetail
import lt.markmerkk.locaping.network.safeCall
import org.joda.time.DateTime

class HomeRepository(
    private val networkErrorHandler: NetworkErrorHandler,
    private val api: Api,
) {
    suspend fun postPing(content: String): DataResult<String> {
        return api.postPing(content)
            .safeCall(networkErrorHandler)
    }

    suspend fun postPingDetail(
        coordLat: Double,
        coordLong: Double,
        dtLastPing: DateTime?,
        dtCurrent: DateTime,
        extras: String = "",
    ): DataResult<String> {
        val _dtLast = if (dtLastPing == null) {
            dtCurrent
        } else {
            dtLastPing
        }
        return api.postPingDetail(
            ReqPingDetail(
                coordLat = coordLat,
                coordLong = coordLong,
                dtLastPing = AppDateTimeUtils.dtFormatterDateTime.print(_dtLast),
                dtCurrent = AppDateTimeUtils.dtFormatterDateTime.print(dtCurrent),
                extras = extras,
            )
        ).safeCall(networkErrorHandler)
    }
}