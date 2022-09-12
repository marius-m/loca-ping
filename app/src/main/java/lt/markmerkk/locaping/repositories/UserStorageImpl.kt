package lt.markmerkk.locaping.repositories

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import org.joda.time.DateTime

class UserStorageSharedPref(
    application: Application
) : UserStorage {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(
            UserStorageSharedPref::class.simpleName,
            Context.MODE_PRIVATE
        )

    private var tokenFb: String = sharedPreferences.getString(KEY_FB_TOKEN, "")!!
    private var lastFetchInMillis: Long = sharedPreferences.getLong(KEY_LAST_FETCH_MILLIS, 0L)!!

    override fun saveTokenFb(newToken: String) {
        this.tokenFb = newToken
        sharedPreferences.edit()
            .putString(KEY_FB_TOKEN, newToken)
            .apply()
    }

    override fun markLastFetch(now: DateTime) {
        this.lastFetchInMillis = now.millis
        sharedPreferences.edit()
            .putLong(KEY_LAST_FETCH_MILLIS, this.lastFetchInMillis)
            .apply()
    }

    override fun lastFetchInMillis(): Long = lastFetchInMillis

    override fun clear() {
        sharedPreferences.edit()
            .remove(KEY_FB_TOKEN)
            .apply()
    }

    companion object {
        private const val KEY_FB_TOKEN = "settings.login.idToken"
        private const val KEY_LAST_FETCH_MILLIS = "settings.lastFetchMillis"
    }
}
