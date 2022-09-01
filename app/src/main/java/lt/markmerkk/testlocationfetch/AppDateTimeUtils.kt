package lt.markmerkk.testlocationfetch

import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat
import timber.log.Timber

/**
 * Date time utilities for project
 */
object AppDateTimeUtils {
    const val DEFAULT_FORMAT_DATE = "yyyy-MM-dd"
    const val DEFAULT_FORMAT_DATETIME = "yyyy-MM-dd'T'HH:mm:ssZZ"
    const val DEFAULT_FORMAT_TIME_DETAIL = "HH:mm:ssZZ"
    const val DEFAULT_FORMAT_TIME_BASIC = "HH'h' mm'min'"
    const val DEFAULT_FORMAT_DATETIME_BASIC = "yyyy-MM-dd HH:mm"
    val dtFormatterDate = DateTimeFormat.forPattern(DEFAULT_FORMAT_DATE)
    val dtFormatterDateTime = DateTimeFormat.forPattern(DEFAULT_FORMAT_DATETIME)

    val dtFormatterTimeDetail = DateTimeFormat.forPattern(DEFAULT_FORMAT_TIME_DETAIL)
    val dtFormatterDateTimeBasic = DateTimeFormat.forPattern(DEFAULT_FORMAT_DATETIME_BASIC)

    @SuppressWarnings("MagicNumber")
    val defaultLocalDateTime = LocalDateTime(1970, 1, 1, 0, 0, 0)

    /**
     * Prints an exception if not available
     */
    fun parseLocalDateTimeOrNull(dateTimeAsString: String?): LocalDateTime? {
        return try {
            dtFormatterDateTime.parseLocalDateTime(dateTimeAsString)
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "Error parsing local date time")
            null
        }
    }

    fun parseDateTimeOrNull(dateTimeAsString: String?): DateTime? {
        return try {
            dtFormatterDateTime.parseDateTime(dateTimeAsString)
        } catch (e: IllegalArgumentException) {
            Timber.w(e, "Error parsing date time")
            null
        }
    }

    fun parseLocalDateTimeOrDefault(dateTimeAsString: String?): LocalDateTime {
        return parseLocalDateTimeOrNull(dateTimeAsString) ?: defaultLocalDateTime
    }
}
