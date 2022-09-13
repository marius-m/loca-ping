package lt.markmerkk.locaping

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.AppInitializer
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import lt.markmerkk.locaping.alarm.RemindersManager
import lt.markmerkk.locaping.firebase.AppFirebase
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import lt.markmerkk.locaping.workers.WorkerSendPings
import net.danlew.android.joda.JodaTimeInitializer
import org.joda.time.Duration
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class App: Application(), Configuration.Provider {

    @Inject lateinit var appFirebase: AppFirebase
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var timeProvider: AppTimeProvider

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        AppInitializer.getInstance(this)
            .initializeComponent(JodaTimeInitializer::class.java)
        appFirebase.onCreate()
        enqueueLocationSendingWork()
        scheduleAlarmManager(context = this)
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun enqueueLocationSendingWork() {
        Timber.tag(Tags.LOCATION)
            .i("enqueueLocationSending()".withLogInstance(this))
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val work = PeriodicWorkRequestBuilder<WorkerSendPings>(30, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()
        WorkManager
            .getInstance(applicationContext)
            .enqueueUniquePeriodicWork(WorkManagerTags.PING, ExistingPeriodicWorkPolicy.KEEP, work)
    }

    private fun scheduleAlarmManager(context: Context) {
        RemindersManager.startReminder(
            context = context,
            timeProvider = timeProvider,
        )
    }
}