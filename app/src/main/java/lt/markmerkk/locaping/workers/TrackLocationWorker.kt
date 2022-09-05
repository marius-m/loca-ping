package lt.markmerkk.locaping.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import timber.log.Timber

class TrackLocationWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        return try {
            Timber.tag("TEST").e("doWork()".withLogInstance(this))
            Result.success()
        } catch (e: Exception) {
            Timber.tag("TEST").e(e, "doWork.failure()".withLogInstance(this))
            Result.failure()
        }
    }
}
