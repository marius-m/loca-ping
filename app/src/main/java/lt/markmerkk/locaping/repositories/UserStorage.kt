package lt.markmerkk.locaping.repositories

/**
 * Holds user related data
 */
interface UserStorage {

    fun saveTokenFb(newToken: String)

    fun clear()
}
