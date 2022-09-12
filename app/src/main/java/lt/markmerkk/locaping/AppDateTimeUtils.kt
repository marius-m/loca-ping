package lt.markmerkk.locaping

import okhttp3.internal.concurrent.formatDuration
import org.joda.time.DateTime
import org.joda.time.Duration
import org.joda.time.DurationFieldType
import org.joda.time.LocalDateTime
import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.PeriodFormatterBuilder
import timber.log.Timber

/**
 * Date time utilities for project
 */
object AppDateTimeUtils {
    const val DEFAULT_FORMAT_DATE = "yyyy-MM-dd"
    const val DEFAULT_FORMAT_DATETIME = "yyyy-MM-dd'T'HH:mm:ssZZ"
    val dtFormatterDate = DateTimeFormat.forPattern(DEFAULT_FORMAT_DATE)
    val dtFormatterDateTime = DateTimeFormat.forPattern(DEFAULT_FORMAT_DATETIME)

    @SuppressWarnings("MagicNumber")
    val defaultLocalDateTime = LocalDateTime(1970, 1, 1, 0, 0, 0)

    @SuppressWarnings("MagicNumber")
    val defaultDateTime = DateTime(1970, 1, 1, 0, 0, 0)

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

    private val periodFormatter = PeriodFormatterBuilder()
        .appendDays()
        .appendSuffix("d")
        .appendHours()
        .appendSuffix("h")
        .appendMinutes()
        .appendSuffix("m")
        .appendSeconds()
        .appendSuffix("s")
        .toFormatter()

    fun formatReadableDuration(duration: Duration): String {
        return periodFormatter.print(duration.toPeriod())
    }

    fun formatReadableDurationShort(duration: Duration): String {
        if (duration.standardMinutes <= 0)
            return "0m"
        val builder = StringBuilder()
        val type = PeriodType.forFields(arrayOf(DurationFieldType.hours(), DurationFieldType.minutes()))
        val period = Period(duration, type)
        if (period.days != 0)
            builder.append(period.days).append("d").append(" ")
        if (period.hours != 0)
            builder.append(period.hours).append("h").append(" ")
        if (period.minutes != 0)
            builder.append(period.minutes).append("m").append(" ")
        if (builder.isNotEmpty() && builder[builder.length - 1] == " "[0])
            builder.deleteCharAt(builder.length - 1)
        return builder.toString()
    }
}

fun Duration.formatReadable(): String {
    return AppDateTimeUtils.formatReadableDuration(this)
}