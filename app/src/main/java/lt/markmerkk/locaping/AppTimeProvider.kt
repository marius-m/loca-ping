package lt.markmerkk.locaping

import org.joda.time.LocalDateTime

/**
 * Provides date / time instances
 */
interface AppTimeProvider {
    fun now(): LocalDateTime
    fun print(localDateTime: LocalDateTime): String
}
