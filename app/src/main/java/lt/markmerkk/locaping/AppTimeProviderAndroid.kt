package lt.markmerkk.locaping

import org.joda.time.DateTime
import org.joda.time.DateTimeZone

class AppTimeProviderAndroid : AppTimeProvider {

    private val dtz = DateTimeZone.getDefault()
    override fun now(): DateTime = DateTime.now()
}
