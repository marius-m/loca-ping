package lt.markmerkk.locaping.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import lt.markmerkk.locaping.AppTimeProvider
import lt.markmerkk.locaping.Tags
import lt.markmerkk.locaping.db.AppDatabase
import lt.markmerkk.locaping.db.LocationEntry
import lt.markmerkk.locaping.entities.LocationSource
import lt.markmerkk.locaping.location.LocationFetcher
import lt.markmerkk.locaping.location.LocationFetcherSync
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver(), LifecycleOwner {

    @Inject lateinit var timeProvider: AppTimeProvider
    @Inject lateinit var appDatabase: AppDatabase

    private val lifecycleRegistry = LifecycleRegistry(this)

    override fun onReceive(context: Context, intent: Intent) {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        Timber.tag(Tags.LOCATION)
            .d("onReceive.init()".withLogInstance(this))
        val locationFetcher: LocationFetcher = LocationFetcherSync(
            appContext = context.applicationContext,
            timeProvider = timeProvider,
            locationSource = LocationSource.PUSH_NOTIFICATION_WORKER,
        )
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            locationFetcher.onAttach()
            Timber.tag(Tags.LOCATION)
                .d("onReceive.fetchLocation()".withLogInstance(this))
            val newLocation = locationFetcher
                .fetchLocationSync(durationTimeout = LocationFetcher.DEFAULT_TIMEOUT_DURATION_SHORT)
            Timber.tag(Tags.LOCATION).d(
                "onReceive.successLocation(newLocation: %s)".withLogInstance(this),
                newLocation,
            )
            if (newLocation != null) {
                lifecycleScope.launch {
                    lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
                    appDatabase
                        .locationDao()
                        .insert(
                            LocationEntry.fromAppLocation(
                                appLocation = newLocation,
                                locationSource = LocationSource.ALARM_MANAGER,
                            )
                        )
                    Timber.tag(Tags.LOCATION).d(
                        "onReceive.insertToDb(newLocation: %s)".withLogInstance(this),
                        newLocation,
                    )
                }
            }
            Timber.tag(Tags.LOCATION).d(
                "onReceive.scheduleNextAlarm()".withLogInstance(this),
                newLocation,
            )
            RemindersManager.stopReminder(context)
            RemindersManager.startReminder(
                context = context,
                timeProvider = timeProvider,
                reminderDurationFromNow = Duration.standardMinutes(15)
            )
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        } catch (e: Exception) {
            Timber.tag(Tags.LOCATION).w(
                e,
                "onReceive.error(e: %s)".withLogInstance(this),
                e,
            )
        } finally {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        }
    }

    override fun getLifecycle(): Lifecycle = lifecycleRegistry
}