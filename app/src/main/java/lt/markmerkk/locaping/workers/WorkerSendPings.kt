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
import lt.markmerkk.locaping.db.LocationEntry.Companion.toAppLocation
import lt.markmerkk.locaping.entities.AppLocation
import lt.markmerkk.locaping.entities.LocationSource
import lt.markmerkk.locaping.network.DataResult
import lt.markmerkk.locaping.repositories.HomeRepository
import lt.markmerkk.locaping.repositories.UserStorage
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import timber.log.Timber
import javax.inject.Inject

@HiltWorker
class WorkerSendPings @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @Inject lateinit var timeProvider: AppTimeProvider
    @Inject lateinit var homeRepository: HomeRepository
    @Inject lateinit var appDatabase: AppDatabase
    @Inject lateinit var userStorage: UserStorage

    override suspend fun doWork(): Result = coroutineScope {
        Timber.tag(Tags.LOCATION)
            .d("doWork.init()".withLogInstance(this@WorkerSendPings))
        return@coroutineScope try {
            val pings = appDatabase
                .locationDao()
                .findAllAfterLastFetch(lastFetch = userStorage.lastFetchInMillis())
            Timber.tag(Tags.LOCATION)
                .d("doWork.fetchDb(pings: %s)".withLogInstance(this@WorkerSendPings), pings)
            val dataResults = pings.map { locationEntry ->
                val appLocation = locationEntry.toAppLocation()
                Timber.tag(Tags.LOCATION)
                    .d("doWork.postPing(ping: %s)".withLogInstance(this@WorkerSendPings), appLocation)
                val resultPing = postPing(appLocation = appLocation)
                when (resultPing) {
                    is DataResult.Error -> {
                        Timber.tag(Tags.LOCATION).w(
                            resultPing.throwable,
                            "doWork.postPingError(error: %s)".withLogInstance(this@WorkerSendPings),
                            resultPing.throwable.toString(),
                        )
                    }
                    is DataResult.Success -> {
                        Timber.tag(Tags.LOCATION).d(
                            "doWork.postPingSuccess(content: %s)".withLogInstance(this@WorkerSendPings),
                            resultPing.result,
                        )
                    }
                }
                resultPing
            }
            val now = timeProvider.now()
            Timber.tag(Tags.LOCATION).d(
                "doWork.markLastFetch(now: %s)".withLogInstance(this@WorkerSendPings),
                now,
            )
            userStorage.markLastFetch(now)
            val hasErrors = dataResults
                .filterIsInstance<DataResult.Error>()
                .isNotEmpty()
            if (hasErrors) {
                Timber.tag(Tags.LOCATION).e("doWork.failureInRequests()".withLogInstance(this))
                return@coroutineScope Result.failure()
            }
            Timber.tag(Tags.LOCATION)
                .d("doWork.success()".withLogInstance(this@WorkerSendPings))
            Result.success()
        } catch (e: Exception) {
            Timber.tag(Tags.LOCATION).e(e, "doWork.failure()".withLogInstance(this))
            Result.failure()
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
