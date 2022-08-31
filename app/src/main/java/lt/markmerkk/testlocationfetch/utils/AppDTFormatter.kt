package lt.markmerkk.testlocationfetch.utils

import org.joda.time.Duration
import org.joda.time.DurationFieldType
import org.joda.time.LocalDate
import org.joda.time.Period
import org.joda.time.PeriodType
import org.joda.time.format.DateTimeFormat
import org.joda.time.format.PeriodFormatterBuilder

object AppDTFormatter {

    const val TIME_SHORT_FORMAT = "HH:mm"
    const val DATE_SHORT_FORMAT = "yyyy-MM-dd"
    const val DATE_LONG_FORMAT = "yyyy-MM-dd HH:mm"
    const val DATE_VERY_LONG_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS"

    val formatTime = DateTimeFormat.forPattern(TIME_SHORT_FORMAT)!!
    val formatDate = DateTimeFormat.forPattern(DATE_SHORT_FORMAT)!!
    val longFormatDateTime = DateTimeFormat.forPattern(DATE_LONG_FORMAT)!!
    val veryLongFormat = DateTimeFormat.forPattern(DATE_VERY_LONG_FORMAT)!!

    val defaultDate = LocalDate(1970, 1, 1)

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

    private val periodFormatterShort = PeriodFormatterBuilder()
        .appendDays()
        .appendSuffix("d")
        .appendHours()
        .appendSuffix("h")
        .appendMinutes()
        .appendSuffix("m")
        .appendSeconds()
        .appendSuffix("s")
        .toFormatter()

    fun humanReadableDuration(duration: Duration): String {
        return periodFormatter.print(duration.toPeriod())
    }

    fun humanReadableDurationShort(duration: Duration): String {
        if (duration.standardMinutes <= 0) {
            return "0m"
        }
        val builder = StringBuilder()
        val type =
            PeriodType.forFields(arrayOf(DurationFieldType.hours(), DurationFieldType.minutes()))
        val period = Period(duration, type)
        if (period.days != 0) {
            builder.append(period.days).append("d").append(" ")
        }
        if (period.hours != 0) {
            builder.append(period.hours).append("h").append(" ")
        }
        if (period.minutes != 0) {
            builder.append(period.minutes).append("m").append(" ")
        }
        if (builder.isNotEmpty() && builder[builder.length - 1] == " "[0]) {
            builder.deleteCharAt(builder.length - 1)
        }
        return builder.toString()
    }
}
