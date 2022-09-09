package lt.markmerkk.locaping

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ServiceLifecycleDispatcher
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import lt.markmerkk.locaping.entities.AppLocation
import lt.markmerkk.locaping.entities.LocationSource
import lt.markmerkk.locaping.loaders.LocationLoader
import lt.markmerkk.locaping.location.LocationFetcher
import lt.markmerkk.locaping.location.LocationFetcherFirstOut
import lt.markmerkk.locaping.location.LocationFetcherPeriodic
import lt.markmerkk.locaping.repositories.HomeRepository
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service(), LifecycleOwner {

    @Inject lateinit var homeRepository: HomeRepository
    @Inject lateinit var timeProvider: AppTimeProvider

    private val lifecycleDispatcher = ServiceLifecycleDispatcher(this)
    private lateinit var channelId: String
    private lateinit var locationFetcher: LocationFetcher
    private lateinit var locationLoader: LocationLoader

    //region LC

    override fun onCreate() {
        lifecycleDispatcher.onServicePreSuperOnCreate()
        super.onCreate()
        Timber.tag(Tags.LOCATION).i("onCreate()".withLogInstance(this))
        locationFetcher = LocationFetcherPeriodic(
            appContext = this.applicationContext,
            timeProvider = timeProvider,
            onLocationChange = listenerOnLocationChange,
        )
        locationLoader = LocationLoader(
            homeRepository = homeRepository,
            lifecycleScope = lifecycleScope,
        )
        channelId = resources.getString(R.string.default_notification_channel_id)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleDispatcher.onServicePreSuperOnStart()
        Timber.tag(Tags.LOCATION).i("onStartCommand()".withLogInstance(this))
        locationFetcher.onAttach()
        locationFetcher.fetchLocation()
        prepareForegroundNotification()
        return START_STICKY
    }

    override fun onStart(intent: Intent?, startId: Int) {
        lifecycleDispatcher.onServicePreSuperOnStart()
        super.onStart(intent, startId)
    }

    override fun onDestroy() {
        lifecycleDispatcher.onServicePreSuperOnDestroy()
        Timber.tag(Tags.LOCATION).i("onDestroy()".withLogInstance(this))
        locationFetcher.onDetach()
        stopForeground(0)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        lifecycleDispatcher.onServicePreSuperOnBind()
        return null
    }

    override fun getLifecycle(): Lifecycle = lifecycleDispatcher.lifecycle

    //endregion

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

    private val listenerOnLocationChange: (AppLocation) -> Unit = { appLocation ->
        locationLoader.postPing(
            currentLocation = appLocation,
            dtCurrent = appLocation.dtCurrent,
            source = LocationSource.FOREGROUND_SERVICE,
        )
    }

    //endregion

    companion object {
    }
}
