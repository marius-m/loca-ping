package lt.markmerkk.locaping.location

import android.content.Context
import android.os.HandlerThread
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
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import org.joda.time.Duration
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference

/**
 * Fetches location asynchronously. After getting first location, will cancel callback
 * Lifecycle: [onDetach]
 */
class LocationFetcherFirstOut(
    private val appContext: Context,
    private val timeProvider: AppTimeProvider,
    private var onLocationChange: ((AppLocation) -> Unit)? = null,
) : LocationFetcher {

    private val locationProviderClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(appContext)

    override fun onAttach() {}

    override fun onDetach() {
        stopLocationUpdates()
    }

    @SuppressWarnings("MissingPermission")
    override fun fetchLocation() {
        stopLocationUpdates()
        startLocationUpdates()
        Timber.tag(Tags.LOCATION).d("fetchLocation.init()".withLogInstance(this))
    }

    override fun fetchLocationSync(
        durationTimeout: Duration
    ): AppLocation? = null

    @SuppressWarnings("MissingPermission")
    private fun startLocationUpdates() {
        // TODO Check if permission is grant
        Timber.tag(Tags.LOCATION).d("startLocationUpdates()".withLogInstance(this))
        val locationRequest = LocationRequest.create().apply {
            interval = LocationFetcher.DEFAULT_UPDATE_INTERVAL_MILLIS
            priority = PRIORITY_HIGH_ACCURACY
        }
        Timber.tag(Tags.LOCATION)
            .d("startLocationUpdates.requestLocationUpdates()".withLogInstance(this))
        locationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.myLooper(),
        )
    }

    private fun stopLocationUpdates() {
        Timber.tag(Tags.LOCATION).d("stopLocationUpdates()".withLogInstance(this))
        locationProviderClient.removeLocationUpdates(locationCallback)
    }

    //region Listeners

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val currentLocation = AppLocation
                .fromLocation(timeProvider, locationResult.lastLocation)
            Timber.tag(Tags.LOCATION).d(
                "locationCallback(thread: %s, location: %s)".withLogInstance(this@LocationFetcherFirstOut),
                Thread.currentThread(),
                currentLocation,
            )
            onLocationChange?.invoke(currentLocation)
            stopLocationUpdates()
        }
    }

    //endregion
}
