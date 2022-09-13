package lt.markmerkk.locaping.firebase
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import lt.markmerkk.locaping.AppDateTimeUtils
import lt.markmerkk.locaping.AppTimeProvider
import lt.markmerkk.locaping.Tags
import lt.markmerkk.locaping.repositories.UserStorage
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import lt.markmerkk.locaping.workers.WorkerTrackSendLocationDeferred
import lt.markmerkk.locaping.workers.WorkerTrackSendLocationInstant
import org.joda.time.DateTime
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FBMessaging : FirebaseMessagingService() {

    @Inject lateinit var userStorage: UserStorage
    @Inject lateinit var timeProvider: AppTimeProvider

    override fun onCreate() {
        super.onCreate()
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        userStorage.saveTokenFb(newToken = token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val fbData = FBData.from(message)
        Timber.tag(Tags.LOCATION)
            .i(
                "onMessageReceived(thread: %s, message: %s)".withLogInstance(this),
                Thread.currentThread(),
                fbData
            )
//        enqueueLocationTrackingWorkInstant(now = timeProvider.now())
        enqueueLocationTrackingWorkDeferred(now = timeProvider.now())
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun enqueueLocationTrackingWorkInstant(now: DateTime) {
        Timber.tag(Tags.LOCATION)
            .i("enqueueLocationTrackingWork()".withLogInstance(this))
        val workTrackLocation: WorkRequest = OneTimeWorkRequestBuilder<WorkerTrackSendLocationInstant>()
            .setInputData(
                Data.Builder()
                    .putString(
                        WorkerTrackSendLocationInstant.BUNDLE_KEY_PUSH_DT_RAW,
                        AppDateTimeUtils.dtFormatterDateTime.print(now),
                    )
                    .build()
            )
            .build()
        WorkManager
            .getInstance(applicationContext)
            .enqueue(workTrackLocation)
    }

    private fun enqueueLocationTrackingWorkDeferred(now: DateTime) {
        Timber.tag(Tags.LOCATION)
            .i("enqueueLocationTrackingWork()".withLogInstance(this))
        val workTrackLocation: WorkRequest = OneTimeWorkRequestBuilder<WorkerTrackSendLocationDeferred>()
            .setInputData(
                Data.Builder()
                    .putString(
                        WorkerTrackSendLocationDeferred.BUNDLE_KEY_PUSH_DT_RAW,
                        AppDateTimeUtils.dtFormatterDateTime.print(now),
                    )
                    .build()
            )
            .build()
        WorkManager
            .getInstance(applicationContext)
            .enqueue(workTrackLocation)
    }

    private data class FBData(
        val priority: Int,
        val data: Map<String, String>,
        val senderId: String,
        val from: String,
        val to: String,
        val messageId: String,
        val messageType: String,
    ) {
        companion object {
            fun from(remoteMessage: RemoteMessage): FBData {
                return FBData(
                    priority = remoteMessage.priority,
                    data = remoteMessage.data,
                    senderId = remoteMessage.senderId ?: "",
                    from = remoteMessage.from ?: "",
                    to = remoteMessage.to ?: "",
                    messageId = remoteMessage.messageId ?: "",
                    messageType = remoteMessage.messageType ?: "",
                )
            }
        }
    }
}
