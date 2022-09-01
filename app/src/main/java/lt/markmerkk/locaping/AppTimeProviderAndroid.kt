package lt.markmerkk.locaping

import org.joda.time.DateTimeZone
import org.joda.time.LocalDateTime

class AppTimeProviderAndroid : AppTimeProvider {

    private val dtz = DateTimeZone.getDefault()

    override fun now(): LocalDateTime = LocalDateTime.now()
    override fun print(localDateTime: LocalDateTime): String {
        val dateTime = localDateTime.toDateTime(dtz)
        return AppDateTimeUtils.dtFormatterDateTimeBasic.print(dateTime)
    }
}
