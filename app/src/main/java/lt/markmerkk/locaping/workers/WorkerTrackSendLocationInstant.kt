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
class WorkerTrackSendLocationInstant @AssistedInject constructor(
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
            .d("doWork.init()".withLogInstance(this@WorkerTrackSendLocationInstant))
        return@coroutineScope try {
            locationFetcher.onAttach()
            Timber.tag(Tags.LOCATION)
                .d("doWork.fetchLocation()".withLogInstance(this@WorkerTrackSendLocationInstant))
            val newLocation = locationFetcher
                .fetchLocationSync(durationTimeout = LocationFetcher.DEFAULT_TIMEOUT_DURATION)
            Timber.tag(Tags.LOCATION).d(
                "doWork.success(newDuration: %s)".withLogInstance(this@WorkerTrackSendLocationInstant),
                newLocation,
            )
            if (newLocation != null) {
                when (val resultPing = postPing(appLocation = newLocation)) {
                    is DataResult.Error -> {
                        Timber.tag(Tags.LOCATION).w(
                            resultPing.throwable,
                            "doWork.postPingError(error: %s)".withLogInstance(this@WorkerTrackSendLocationInstant),
                            resultPing.throwable.toString(),
                        )
                        Result.failure()
                    }
                    is DataResult.Success -> {
                        Timber.tag(Tags.LOCATION).d(
                            "postPingSuccess(content: %s)".withLogInstance(this@WorkerTrackSendLocationInstant),
                            resultPing.result,
                        )
                        Result.success()
                    }
                }
            } else {
                Result.failure()
            }
        } catch (e: Exception) {
            Timber.tag(Tags.LOCATION).e(e, "doWork.failure()".withLogInstance(this))
            Result.failure()
        } finally {
            locationFetcher.onDetach()
        }
    }

    private suspend fun postPing(appLocation: AppLocation): DataResult<String> {
        return homeRepository.postPingDetail(
            coordLat = appLocation.lat,
            coordLong = appLocation.long,
            dtCurrent = appLocation.dtCurrent,
            locationSource = appLocation.source,
        )
    }
}
