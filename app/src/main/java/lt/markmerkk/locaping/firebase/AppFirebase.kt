package lt.markmerkk.locaping.firebase

import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import lt.markmerkk.locaping.repositories.UserStorage
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import timber.log.Timber

class AppFirebase(
    userStorage: UserStorage
) {
    fun onCreate() {
        FirebaseMessaging.getInstance()
            .token
            .addOnCompleteListener(
                OnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Timber.tag("TEST")
                            .w(
                                task.exception,
                                "onCreate().fetchToken().failure()".withLogInstance(this)
                            )
                        return@OnCompleteListener
                    }
                    val token = task.result
                    Timber.tag("TEST")
                        .i("onCreate().fetchToken(token: %s)".withLogInstance(this), token)
                }
            )
    }
}
