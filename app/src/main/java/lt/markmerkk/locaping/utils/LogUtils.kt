package lt.markmerkk.locaping.utils

/**
 * Debugging utils
 */
object LogUtils {

    /**
     * Converts [obj] to instance signature
     * Easier to figure out different object instance usage in combination to logging
     */
    @JvmStatic
    fun asStringInstance(obj: Any?): String {
        return if (obj != null) {
            String.format(
                "[%s@%s]",
                obj.javaClass.simpleName,
                Integer.toHexString(obj.hashCode())
            )
        } else "[null@instance]"
    }

    fun Any?.toStringInstance(): String {
        return asStringInstance(this)
    }

    @JvmStatic
    fun objMessage(obj: Any?, message: String): String {
        return String.format("%s: %s", obj.toStringInstance(), message)
    }

    fun String.withLogInstance(obj: Any?): String {
        return objMessage(obj, this)
    }
}
