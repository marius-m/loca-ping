package lt.markmerkk.locaping

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.AppInitializer
import androidx.work.Configuration
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.HiltAndroidApp
import lt.markmerkk.locaping.firebase.AppFirebase
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import lt.markmerkk.locaping.workers.WorkerSendPings
import net.danlew.android.joda.JodaTimeInitializer
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class App: Application(), Configuration.Provider {

    @Inject lateinit var appFirebase: AppFirebase
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        AppInitializer.getInstance(this)
            .initializeComponent(JodaTimeInitializer::class.java)
        appFirebase.onCreate()
        enqueueLocationSendingWork()
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
            .addTag(WorkManagerTags.PING)
            .build()
        WorkManager
            .getInstance(applicationContext)
            .enqueue(work)
    }
}