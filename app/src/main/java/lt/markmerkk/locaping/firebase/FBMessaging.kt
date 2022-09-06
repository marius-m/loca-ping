package lt.markmerkk.locaping.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import lt.markmerkk.locaping.repositories.UserStorage
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class FBMessaging : FirebaseMessagingService() {

    @Inject lateinit var userStorage: UserStorage

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        userStorage.saveTokenFb(newToken = token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val fbData = FBData.from(message)
        Timber.tag("TEST")
            .i("onMessageReceived(message: %s)".withLogInstance(this), fbData)
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
