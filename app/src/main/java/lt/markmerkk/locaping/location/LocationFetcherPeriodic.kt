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
import lt.markmerkk.locaping.entities.LocationSource
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import org.joda.time.Duration
import timber.log.Timber

/**
 * Attaches and fetches location.
 * Will report location to [onLocationChange].
 * Starts / ends work on lifecycle events.
 * Lifecycle: [onAttach], [onDetach]
 */
class LocationFetcherPeriodic(
    private val appContext: Context,
    private val timeProvider: AppTimeProvider,
    private val locationSource: LocationSource,
    private var onLocationChange: ((AppLocation) -> Unit)? = null,
) : LocationFetcher {

    private val locationProviderClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(appContext)

    override fun onAttach() {
        startLocationUpdates()
    }

    override fun onDetach() {
        stopLocationUpdates()
    }

    override fun fetchLocation() { }

    override fun fetchLocationSync(
        durationTimeout: Duration
    ): AppLocation? = null

    @SuppressWarnings("MissingPermission")
    private fun startLocationUpdates() {
        // TODO Check if permission is grant
        val locationRequest = LocationRequest.create().apply {
            interval = LocationFetcher.DEFAULT_UPDATE_INTERVAL_MILLIS
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
                .fromLocation(timeProvider, locationResult.lastLocation, locationSource)
            Timber.tag(Tags.LOCATION)
                .d("locationCallback(location: %s)".withLogInstance(this@LocationFetcherPeriodic), currentLocation)
            onLocationChange?.invoke(currentLocation)
        }
    }

    //endregion
}
