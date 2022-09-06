package lt.markmerkk.locaping.repositories

import android.app.Application
import android.content.Context
import android.content.SharedPreferences

class UserStorageSharedPref(
    application: Application
) : UserStorage {

    private val sharedPreferences: SharedPreferences =
        application.getSharedPreferences(
            UserStorageSharedPref::class.simpleName,
            Context.MODE_PRIVATE
        )

    private var tokenFb: String = sharedPreferences.getString(KEY_FB_TOKEN, "")!!

    override fun saveTokenFb(newToken: String) {
        this.tokenFb = newToken
        sharedPreferences.edit()
            .putString(KEY_FB_TOKEN, newToken)
            .apply()
    }

    override fun clear() {
        sharedPreferences.edit()
            .remove(KEY_FB_TOKEN)
            .apply()
    }

    companion object {
        private const val KEY_FB_TOKEN = "settings.login.idToken"
    }
}
