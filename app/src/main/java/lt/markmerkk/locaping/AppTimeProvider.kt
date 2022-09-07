package lt.markmerkk.locaping

import org.joda.time.DateTime
import org.joda.time.LocalDateTime

/**
 * Provides date / time instances
 */
interface AppTimeProvider {
    fun now(): DateTime
}
