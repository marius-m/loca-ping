package lt.markmerkk.locaping.location

import android.content.Context
import android.os.Handler
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
import org.jetbrains.annotations.TestOnly
import org.joda.time.DateTime
import org.joda.time.Duration
import timber.log.Timber
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference

/**
 * Lifecycle: [onDetach]
 */
class LocationFetcherFirstOut(
    private val appContext: Context,
    private val timeProvider: AppTimeProvider,
    private var onLocationChange: ((AppLocation) -> Unit)? = null,
) {

    private val locationProviderClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(appContext)
    private val appLocation = AtomicReference<AppLocation?>(null)
    private var handlerThread = HandlerThread("LocationHandlerThread")

    @SuppressWarnings("MissingPermission")
    fun fetchLocation(
        dtFetchStart: DateTime = timeProvider.now(),
        durationTimeout: Duration = Duration.standardSeconds(5),
    ) {
        this.handlerThread = recreateHandler(handlerThread)
        this.appLocation.set(null)
        stopLocationUpdates()
        startLocationUpdates(handlerThread = handlerThread)
        val dtTimeout = dtFetchStart.plus(durationTimeout)
        Timber.tag(Tags.LOCATION).d(
            "fetchLocation.init(fetchStart: %s, durationTimeout: %s, dtTimeout: %s)".withLogInstance(this),
            dtFetchStart,
            durationTimeout,
            dtTimeout
        )
    }

    fun onDetach() {
        stopLocationUpdates()
    }

    private fun recreateHandler(handlerThread: HandlerThread?): HandlerThread {
        handlerThread?.quitSafely()
        return HandlerThread("LocationHandlerThread")
    }

    @SuppressWarnings("MissingPermission")
    private fun startLocationUpdates(handlerThread: HandlerThread) {
        // TODO Check if permission is grant
        Timber.tag(Tags.LOCATION).d("startLocationUpdates()".withLogInstance(this))
        val locationRequest = LocationRequest.create().apply {
            interval = DEFAULT_UPDATE_INTERVAL_MILLIS
            priority = PRIORITY_HIGH_ACCURACY
        }
        Timber.tag(Tags.LOCATION)
            .d("startLocationUpdates.requestLocationUpdates(handler: %s)".withLogInstance(this), handlerThread)
        locationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            null,
        )
    }

    private fun stopLocationUpdates() {
        Timber.tag(Tags.LOCATION).d("stopLocationUpdates()".withLogInstance(this))
        locationProviderClient.removeLocationUpdates(locationCallback)
        handlerThread.quitSafely()
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
            appLocation.set(currentLocation)
            onLocationChange?.invoke(currentLocation)
            stopLocationUpdates()
        }
    }

    //endregion

    companion object {
        private const val DEFAULT_UPDATE_INTERVAL_MINS: Long = 5
        private const val DEFAULT_UPDATE_INTERVAL_MILLIS: Long = DEFAULT_UPDATE_INTERVAL_MINS * 60 * 1000
    }
}
