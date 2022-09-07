package lt.markmerkk.locaping.entities

import android.location.Location
import lt.markmerkk.locaping.AppTimeProvider
import org.joda.time.DateTime

data class AppLocation(
    val lat: Double,
    val long: Double,
    val dtCurrent: DateTime,
) {
    companion object {
        fun asEmpty(timeProvider: AppTimeProvider): AppLocation = AppLocation(
            lat = 0.0,
            long = 0.0,
            dtCurrent = timeProvider.now(),
        )

        fun fromLocation(
            timeProvider: AppTimeProvider,
            gmsLocation: Location?,
        ): AppLocation {
            if (gmsLocation == null) {
                return asEmpty(timeProvider)
            }
            return AppLocation(
                lat = gmsLocation.latitude,
                long = gmsLocation.longitude,
                dtCurrent = timeProvider.now(),
            )
        }
    }
}