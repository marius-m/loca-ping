package lt.markmerkk.locaping

import android.app.Application
import androidx.startup.AppInitializer
import dagger.hilt.android.HiltAndroidApp
import net.danlew.android.joda.JodaTimeInitializer
import timber.log.Timber

@HiltAndroidApp
class App: Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        AppInitializer.getInstance(this)
            .initializeComponent(JodaTimeInitializer::class.java)
    }
}