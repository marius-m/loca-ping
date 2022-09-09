package lt.markmerkk.locaping.repositories

import lt.markmerkk.locaping.AppDateTimeUtils
import lt.markmerkk.locaping.entities.LocationSource
import lt.markmerkk.locaping.network.Api
import lt.markmerkk.locaping.network.DataResult
import lt.markmerkk.locaping.network.NetworkErrorHandler
import lt.markmerkk.locaping.network.requests.ReqPingDetail
import lt.markmerkk.locaping.network.safeCall
import org.joda.time.DateTime
import java.lang.StringBuilder

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
        dtCurrent: DateTime,
        locationSource: LocationSource,
        extras: String = "",
    ): DataResult<String> {
        val newExtras = StringBuilder(extras)
            .append("source: ")
            .append(locationSource.name)
            .append(";")
        return api.postPingDetail(
            ReqPingDetail(
                coordLat = coordLat,
                coordLong = coordLong,
                dtCurrent = AppDateTimeUtils.dtFormatterDateTime.print(dtCurrent),
                extras = newExtras.toString(),
            )
        ).safeCall(networkErrorHandler)
    }
}