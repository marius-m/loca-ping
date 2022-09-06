package lt.markmerkk.locaping

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lt.markmerkk.locaping.entities.AppLocation
import lt.markmerkk.locaping.network.DataResult
import lt.markmerkk.locaping.repositories.HomeRepository
import lt.markmerkk.locaping.utils.AppDTFormatter
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import lt.markmerkk.locaping.workers.TrackLocationWorker
import org.joda.time.DateTime
import org.joda.time.Period
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service(), LifecycleOwner {

    @Inject lateinit var homeRepository: HomeRepository

    private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)
    private lateinit var channelId: String
    private var dtLocationLast: DateTime? = null
    private var locationFetchCount: Int = 0
    private val locationProviderClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(this.applicationContext)

    //region LC

    override fun onCreate() {
        lifecycleDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        channelId = resources.getString(R.string.default_notification_channel_id)
        Timber.tag("TEST").i("onCreate()".withLogInstance(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleDispatcher.onServicePreSuperOnStart()
        prepareForegroundNotification()
        startLocationUpdates()
        startTrackLocationManager()
        Timber.tag("TEST").i("onStartCommand()".withLogInstance(this))
        return START_STICKY
    }

    override fun onStart(intent: Intent?, startId: Int) {
        lifecycleDispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    override fun onDestroy() {
        lifecycleDispatcher.onServicePreSuperOnDestroy()
        Timber.tag("TEST").i("onDestroy()".withLogInstance(this))
        stopLocationUpdates()
        stopTrackLocationManager()
        stopForeground(0)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        lifecycleDispatcher.onServicePreSuperOnBind()
        return null
    }

    override fun getLifecycle(): Lifecycle = lifecycleDispatcher.lifecycle

    //endregion

    fun startTrackLocationManager() {
        val locationWorker =
            PeriodicWorkRequestBuilder<TrackLocationWorker>(15, TimeUnit.MINUTES)
                .addTag(WM_TAG_LOCATION)
                .build()
        WorkManager
            .getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                WM_TAG_LOCATION,
                ExistingPeriodicWorkPolicy.KEEP,
                locationWorker,
            )
    }

    fun stopTrackLocationManager() {
        WorkManager
            .getInstance(applicationContext)
            .cancelAllWorkByTag(WM_TAG_LOCATION)
    }

    @SuppressWarnings("MissingPermission")
    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = DEFAULT_UPDATE_INTERVAL_MILLIS
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
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

    private fun prepareForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                channelId,
                "Location Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            AppConstants.REQUEST_TAG_LOCATION,
            notificationIntent,
            0
        )
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(getString(R.string.app_name))
            .setContentTitle("Foreground notification")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(AppConstants.NOTIFICATION_ID, notification)
    }

    //region Listeners

    private val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            val currentLocation = AppLocation.fromLocation(locationResult.lastLocation)
            val dtCurrent = DateTime.now()
            val durationLocationLastFetchAsFormatted = if (dtLocationLast == null) {
                "Never"
            } else {
                val durationFromLast = Period(dtLocationLast, dtCurrent)
                    .toStandardDuration()
                AppDTFormatter.humanReadableDuration(durationFromLast)
            }
            val message = (
                "locationCallback(" +
                    "location: %s," +
                    " durationLastFetch: %s," +
                    " locationFetchCount: %s," +
                    " dtCurrent: %s" +
                    ")"
                ).format(
                currentLocation,
                durationLocationLastFetchAsFormatted,
                locationFetchCount.toString(),
                AppDTFormatter.longFormatDateTime.print(dtCurrent)
            )
            Timber.tag("TEST").d(message)
            lifecycleScope.launch {
                val result = homeRepository.postPingDetail(
                    coordLat = currentLocation.lat,
                    coordLong = currentLocation.long,
                    dtLastPing = dtLocationLast,
                    dtCurrent = dtCurrent,
                )
                when (result) {
                    is DataResult.Error -> {
                        Timber.tag("TEST")
                            .d("postPingError(error: %s)", result.throwable)
                    }
                    is DataResult.Success -> {
                        Timber.tag("TEST")
                            .d("postPingSuccess(content: %s)", result.result)
                    }
                }
            }
            this@LocationService.dtLocationLast = dtCurrent
            this@LocationService.locationFetchCount++
        }
    }

    //endregion

    companion object {
        private const val DEFAULT_UPDATE_INTERVAL_MINS: Long = 5
        private const val DEFAULT_UPDATE_INTERVAL_MILLIS: Long = 1000 * 60 * DEFAULT_UPDATE_INTERVAL_MINS

        const val WM_TAG_LOCATION = "WM_TAG.LOCATION"
    }
}
