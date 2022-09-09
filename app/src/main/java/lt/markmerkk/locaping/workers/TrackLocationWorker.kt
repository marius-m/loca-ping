package lt.markmerkk.locaping.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.components.SingletonComponent
import lt.markmerkk.locaping.AppTimeProvider
import lt.markmerkk.locaping.AppTimeProviderAndroid
import lt.markmerkk.locaping.Tags
import lt.markmerkk.locaping.location.LocationFetcher
import lt.markmerkk.locaping.location.LocationFetcherFirstOut
import lt.markmerkk.locaping.location.LocationFetcherSync
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import org.joda.time.Duration
import timber.log.Timber
import javax.inject.Inject

class TrackLocationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    // Should be injected
    private val timeProvider: AppTimeProvider = AppTimeProviderAndroid()

    private val locationFetcher: LocationFetcher = LocationFetcherSync(
        appContext = context.applicationContext,
        timeProvider = timeProvider,
    )

    override fun doWork(): Result {
        Timber.tag(Tags.LOCATION)
            .d("doWork.init()".withLogInstance(this@TrackLocationWorker))
        return try {
            locationFetcher.onAttach()
            Timber.tag(Tags.LOCATION)
                .d("doWork.fetchLocation()".withLogInstance(this@TrackLocationWorker))
            val newLocation = locationFetcher
                .fetchLocationSync(durationTimeout = LocationFetcher.DEFAULT_TIMEOUT_DURATION)
            Timber.tag(Tags.LOCATION).d(
                "doWork.success(newDuration: %s)".withLogInstance(this@TrackLocationWorker),
                newLocation,
            )
            Result.success()
        } catch (e: Exception) {
            Timber.tag(Tags.LOCATION).e(e, "doWork.failure()".withLogInstance(this))
            Result.failure()
        } finally {
            locationFetcher.onDetach()
        }
    }
}
