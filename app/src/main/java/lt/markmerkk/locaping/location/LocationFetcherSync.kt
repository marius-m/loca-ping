package lt.markmerkk.locaping.location

import android.content.Context
import android.os.HandlerThread
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority.PRIORITY_HIGH_ACCURACY
import lt.markmerkk.locaping.AppDateTimeUtils
import lt.markmerkk.locaping.AppTimeProvider
import lt.markmerkk.locaping.Tags
import lt.markmerkk.locaping.entities.AppLocation
import lt.markmerkk.locaping.formatReadable
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import org.jetbrains.annotations.TestOnly
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.Period
import timber.log.Timber
import java.util.concurrent.atomic.AtomicReference

/**
 * A synchronouse operation to fetch location by blocking its execution method
 * It will 'hold' a thread until it gets a location or fails the operation
 * Lifecycle: [onDetach]
 */
class LocationFetcherSync(
    private val appContext: Context,
    private val timeProvider: AppTimeProvider
) : LocationFetcher {

    private val locationProviderClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(appContext)
    private val appLocation = AtomicReference<AppLocation?>(null)
    private val handlerThread = HandlerThread("LocationHandlerThread")

    override fun onAttach() { }

    override fun onDetach() {
        stopLocationUpdates()
        handlerThread.quit()
    }

    override fun fetchLocation() { }

    @SuppressWarnings("MissingPermission")
    override fun fetchLocationSync(
        durationTimeout: Duration,
    ): AppLocation? {
        val dtFetchStart = timeProvider.now()
        appLocation.set(null)
        stopLocationUpdates()
        startLocationUpdates()
        val dtTimeout = dtFetchStart.plus(durationTimeout)
        Timber.tag(Tags.LOCATION).d(
            "fetchLocation.init(fetchStart: %s, durationTimeout: %s, dtTimeout: %s)".withLogInstance(this),
            dtFetchStart,
            durationTimeout.formatReadable(),
            dtTimeout
        )
        do {
            val now = timeProvider.now()
            val timeoutIn = Period(now, dtTimeout).toStandardDuration()
            Timber.tag(Tags.LOCATION).d(
                "fetchLocation.check(thread: %s, timeOutIn: %s)".withLogInstance(this),
                Thread.currentThread(),
                timeoutIn.formatReadable(),
            )
            val shouldWaitForObject = shouldWaitForObject(
                dtNow = now,
                dtTimeout = dtTimeout,
                targetObject = appLocation.get(),
            )
            Thread.sleep(300L)
        } while (shouldWaitForObject)
        Timber.tag(Tags.LOCATION).d(
            "fetchLocation.result(appLocation: %s)".withLogInstance(this),
            appLocation.get()
        )
        return appLocation.get()
    }

    @SuppressWarnings("MissingPermission")
    private fun startLocationUpdates() {
        // TODO Check if permission is grant
        Timber.tag(Tags.LOCATION).d("startLocationUpdates()".withLogInstance(this))
        handlerThread.start()
        val locationRequest = LocationRequest.create().apply {
            interval = DEFAULT_UPDATE_INTERVAL_MILLIS
            priority = PRIORITY_HIGH_ACCURACY
        }
        locationProviderClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            handlerThread.looper,
        )
    }

    private fun stopLocationUpdates() {
        Timber.tag(Tags.LOCATION).d("stopLocationUpdates()".withLogInstance(this))
        locationProviderClient.removeLocationUpdates(locationCallback)
        handlerThread.quit()
    }

    //region Listeners

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val currentLocation = AppLocation
                .fromLocation(timeProvider, locationResult.lastLocation)
            Timber.tag(Tags.LOCATION).d(
                "locationCallback(thread: %s, location: %s)".withLogInstance(this@LocationFetcherSync),
                Thread.currentThread(),
                currentLocation,
            )
            appLocation.set(currentLocation)
            stopLocationUpdates()
        }
    }

    //endregion

    companion object {
        private const val DEFAULT_TIMEOUT_SEC: Long = 10
        private const val DEFAULT_UPDATE_INTERVAL_MINS: Long = 5
        private const val DEFAULT_UPDATE_INTERVAL_MILLIS: Long = DEFAULT_UPDATE_INTERVAL_MINS * 60 * 1000

        val DEFAULT_TIMEOUT_DURATION: Duration = Duration.standardSeconds(DEFAULT_TIMEOUT_SEC)

        @TestOnly
        internal fun shouldWaitForObject(
            dtNow: DateTime,
            dtTimeout: DateTime,
            targetObject: Any? = null,
        ): Boolean {
            val isTimeout = dtNow.isAfter(dtTimeout)
            val isObjReady = targetObject != null
            Timber.tag(Tags.LOCATION).d(
                "shouldWaitForObject(isTimeout: %s, isObjReady: %s)".withLogInstance(this),
                isTimeout,
                isObjReady,
            )
            return !isTimeout && !isObjReady
        }
    }
}
