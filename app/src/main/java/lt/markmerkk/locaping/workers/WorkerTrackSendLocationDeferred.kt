package lt.markmerkk.locaping.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.coroutineScope
import lt.markmerkk.locaping.AppTimeProvider
import lt.markmerkk.locaping.Tags
import lt.markmerkk.locaping.db.AppDatabase
import lt.markmerkk.locaping.db.LocationEntry
import lt.markmerkk.locaping.entities.AppLocation
import lt.markmerkk.locaping.entities.LocationSource
import lt.markmerkk.locaping.location.LocationFetcher
import lt.markmerkk.locaping.location.LocationFetcherSync
import lt.markmerkk.locaping.network.DataResult
import lt.markmerkk.locaping.repositories.HomeRepository
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import timber.log.Timber
import javax.inject.Inject

@HiltWorker
class WorkerTrackSendLocationDeferred @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @Inject lateinit var timeProvider: AppTimeProvider
    @Inject lateinit var homeRepository: HomeRepository
    @Inject lateinit var appDatabase: AppDatabase

    override suspend fun doWork(): Result = coroutineScope {
        val locationFetcher: LocationFetcher = LocationFetcherSync(
            appContext = applicationContext,
            timeProvider = timeProvider,
            locationSource = LocationSource.PUSH_NOTIFICATION_WORKER,
        )
        Timber.tag(Tags.LOCATION)
            .d("doWork.init()".withLogInstance(this@WorkerTrackSendLocationDeferred))
        return@coroutineScope try {
            locationFetcher.onAttach()
            Timber.tag(Tags.LOCATION)
                .d("doWork.fetchLocation()".withLogInstance(this@WorkerTrackSendLocationDeferred))
            val newLocation = locationFetcher
                .fetchLocationSync(durationTimeout = LocationFetcher.DEFAULT_TIMEOUT_DURATION)
            Timber.tag(Tags.LOCATION).d(
                "doWork.success(newLocation: %s)".withLogInstance(this@WorkerTrackSendLocationDeferred),
                newLocation,
            )
            if (newLocation != null) {
                appDatabase
                    .locationDao()
                    .insert(
                        LocationEntry.fromAppLocation(
                            appLocation = newLocation,
                            locationSource = LocationSource.PUSH_NOTIFICATION_WORKER,
                        )
                    )
                Timber.tag(Tags.LOCATION).d(
                    "doWork.insertToDb(newLocation: %s)".withLogInstance(this@WorkerTrackSendLocationDeferred),
                    newLocation,
                )
                Result.success()
            } else {
                Timber.tag(Tags.LOCATION).e("doWork.noLocation()".withLogInstance(this))
                Result.failure()
            }
        } catch (e: Exception) {
            Timber.tag(Tags.LOCATION).e(e, "doWork.failure()".withLogInstance(this))
            Result.failure()
        } finally {
            locationFetcher.onDetach()
        }
    }
}
