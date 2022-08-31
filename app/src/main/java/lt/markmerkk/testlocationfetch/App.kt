package lt.markmerkk.testlocationfetch

import android.app.Application
import androidx.startup.AppInitializer
import net.danlew.android.joda.JodaTimeInitializer
import timber.log.Timber

class App: Application() {

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        AppInitializer.getInstance(this)
            .initializeComponent(JodaTimeInitializer::class.java)
    }
}