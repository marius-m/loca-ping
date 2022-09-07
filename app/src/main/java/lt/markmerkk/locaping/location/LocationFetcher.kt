package lt.markmerkk.locaping.location

import android.content.Context
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import lt.markmerkk.locaping.AppTimeProvider
import lt.markmerkk.locaping.Tags
import lt.markmerkk.locaping.entities.AppLocation
import lt.markmerkk.locaping.utils.AppDTFormatter
import org.joda.time.DateTime
import org.joda.time.Period
import timber.log.Timber

/**
 * Lifecycle: [onAttach], [onDetach]
 */
class LocationFetcher(
    private val appContext: Context,
    private val timeProvider: AppTimeProvider,
    private var onLocationChange: ((AppLocation) -> Unit)? = null,
) {

    private val locationProviderClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(appContext)

    fun onAttach() {
        startLocationUpdates()
    }

    fun onDetach() {
        stopLocationUpdates()
    }

    @SuppressWarnings("MissingPermission")
    private fun startLocationUpdates() {
        // TODO Check if permission is grant
        val locationRequest = LocationRequest.create().apply {
            interval = DEFAULT_UPDATE_INTERVAL_MILLIS
            priority = PRIORITY_HIGH_ACCURACY
        }
        locationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper()
        )
    }

    private fun stopLocationUpdates() {
        locationProviderClient.removeLocationUpdates(locationCallback)
    }

    //region Listeners

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val currentLocation = AppLocation
                .fromLocation(timeProvider, locationResult.lastLocation)
            val message = (
                "locationCallback(" +
                    "location: %s" +
                    ")"
                ).format(
                currentLocation,
            )
            Timber.tag(Tags.LOCATION).d(message)
            onLocationChange?.invoke(currentLocation)
        }
    }

    //endregion

    companion object {
        private const val DEFAULT_UPDATE_INTERVAL_MINS: Long = 5
        private const val DEFAULT_UPDATE_INTERVAL_MILLIS: Long = DEFAULT_UPDATE_INTERVAL_MINS * 60 * 1000
    }
}
