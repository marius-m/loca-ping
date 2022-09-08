package lt.markmerkk.locaping

import org.joda.time.DateTime
import org.joda.time.DateTimeUtils

@SuppressWarnings("MagicNumber")
class AppTimeProviderTest : AppTimeProvider {

    init {
        DateTimeUtils.setCurrentMillisFixed(1L)
    }

    private val now = DateTime.now()

    override fun now(): DateTime = now
}
