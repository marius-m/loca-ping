package lt.markmerkk.locaping.location

import lt.markmerkk.locaping.entities.AppLocation
import org.joda.time.DateTime
import org.joda.time.Duration

interface LocationFetcher {

    fun onAttach()
    fun onDetach()
    fun fetchLocation(
        dtFetchStart: DateTime,
        durationTimeout: Duration
    )

    fun fetchLocationSync(
        dtFetchStart: DateTime,
        durationTimeout: Duration
    ): AppLocation?
}
