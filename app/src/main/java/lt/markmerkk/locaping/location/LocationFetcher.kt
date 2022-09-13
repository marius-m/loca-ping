package lt.markmerkk.locaping.location

import lt.markmerkk.locaping.entities.AppLocation
import org.joda.time.DateTime
import org.joda.time.Duration

interface LocationFetcher {

    fun onAttach()
    fun onDetach()
    fun fetchLocation()

    fun fetchLocationSync(
        durationTimeout: Duration = DEFAULT_TIMEOUT_DURATION,
    ): AppLocation?

    companion object {
        const val DEFAULT_TIMEOUT_SEC: Long = 10
        const val DEFAULT_TIMEOUT_SHORT_SEC: Long = 5
        const val DEFAULT_UPDATE_INTERVAL_MINS: Long = 15
//        const val DEFAULT_UPDATE_INTERVAL_MINS: Long = 1 // debug
        const val DEFAULT_UPDATE_INTERVAL_MILLIS: Long = DEFAULT_UPDATE_INTERVAL_MINS * 60 * 1000

        val DEFAULT_TIMEOUT_DURATION_SHORT: Duration = Duration
            .standardSeconds(DEFAULT_TIMEOUT_SHORT_SEC)
        val DEFAULT_TIMEOUT_DURATION: Duration = Duration.standardSeconds(DEFAULT_TIMEOUT_SEC)
    }
}
