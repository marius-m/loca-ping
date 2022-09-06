package lt.markmerkk.locaping.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import dagger.hilt.android.AndroidEntryPoint
import lt.markmerkk.locaping.repositories.UserStorage
import javax.inject.Inject

@AndroidEntryPoint
class FBMessaging : FirebaseMessagingService() {

    @Inject lateinit var userStorage: UserStorage

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        userStorage.saveTokenFb(newToken = token)
    }
}
