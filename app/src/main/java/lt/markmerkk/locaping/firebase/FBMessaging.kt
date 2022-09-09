package lt.markmerkk.locaping.firebase
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import lt.markmerkk.locaping.AppTimeProvider
import lt.markmerkk.locaping.Tags
import lt.markmerkk.locaping.WorkManagerTags
import lt.markmerkk.locaping.repositories.UserStorage
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import lt.markmerkk.locaping.workers.TrackLocationWorker
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
        enqueueLocationTrackingWork()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun enqueueLocationTrackingWork() {
        Timber.tag(Tags.LOCATION)
            .i("enqueueLocationTrackingWork()".withLogInstance(this))
        val workTrackLocation: WorkRequest = OneTimeWorkRequestBuilder<TrackLocationWorker>()
            .build()
//        val constraints = Constraints.Builder()
//            .setRequiredNetworkType(NetworkType.CONNECTED)
//            .build()
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
