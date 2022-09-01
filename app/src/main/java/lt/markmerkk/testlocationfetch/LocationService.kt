package lt.markmerkk.testlocationfetch

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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lt.markmerkk.testlocationfetch.entities.AppLocation
import lt.markmerkk.testlocationfetch.network.DataResult
import lt.markmerkk.testlocationfetch.repositories.HomeRepository
import lt.markmerkk.testlocationfetch.utils.AppDTFormatter
import lt.markmerkk.testlocationfetch.utils.LogUtils.withLogInstance
import org.joda.time.DateTime
import org.joda.time.Period
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service(), LifecycleOwner {

    @Inject lateinit var homeRepository: HomeRepository

    private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)

    private var dtLocationLast: DateTime? = null
    private var locationFetchCount: Int = 0
    private val locationProviderClient: FusedLocationProviderClient
        get() = LocationServices.getFusedLocationProviderClient(this.applicationContext)

    //region LC

    override fun onCreate() {
        lifecycleDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        Timber.tag("TEST").i("onCreate()".withLogInstance(this))
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleDispatcher.onServicePreSuperOnStart()
        prepareForegroundNotification()
        startLocationUpdates()
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
        locationProviderClient.removeLocationUpdates(locationCallback)
        stopForeground(0)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        lifecycleDispatcher.onServicePreSuperOnBind()
        return null
    }

    override fun getLifecycle(): Lifecycle = lifecycleDispatcher.lifecycle

    //endregion

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

    private fun prepareForegroundNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID_DEFAULT,
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
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID_DEFAULT)
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
                when (val result = homeRepository.postPing(content = message)) {
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
        private const val CHANNEL_ID_DEFAULT = "65beac30-0e52-415a-a52f-f2a24728e787"
        private const val DEFAULT_UPDATE_INTERVAL_MILLIS: Long = 1000 * 15
    }
}
