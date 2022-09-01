package lt.markmerkk.locaping.entities

import android.location.Location

data class AppLocation(
    val lat: Double,
    val long: Double,
) {
    companion object {
        fun asEmpty(): AppLocation = AppLocation(
            lat = 0.0,
            long = 0.0,
        )

        fun fromLocation(gmsLocation: Location?): AppLocation {
            if (gmsLocation == null) {
                return asEmpty()
            }
            return AppLocation(
                lat = gmsLocation.latitude,
                long = gmsLocation.longitude,
            )
        }
    }
}