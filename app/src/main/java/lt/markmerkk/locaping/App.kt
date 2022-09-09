package lt.markmerkk.locaping

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.startup.AppInitializer
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import lt.markmerkk.locaping.firebase.AppFirebase
import net.danlew.android.joda.JodaTimeInitializer
import timber.log.Timber
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
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}