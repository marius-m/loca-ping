package lt.markmerkk.locaping.repositories

import org.joda.time.DateTime

/**
 * Holds user related data
 */
interface UserStorage {
    fun saveTokenFb(newToken: String)
    fun markLastFetch(now: DateTime)
    fun lastFetchInMillis(): Long
    fun clear()
}
