package lt.markmerkk.locaping.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import lt.markmerkk.locaping.AppConstants
import lt.markmerkk.locaping.AppTimeProvider
import lt.markmerkk.locaping.Tags
import lt.markmerkk.locaping.utils.LogUtils.withLogInstance
import org.joda.time.Duration
import timber.log.Timber

object RemindersManager {
    const val DEFAULT_ALARM_TIMEOUT_MIN = 15L
    fun startReminder(
        context: Context,
        am: AlarmManager =context.getSystemService(Context.ALARM_SERVICE) as AlarmManager,
        timeProvider: AppTimeProvider,
        reminderDurationFromNow: Duration = Duration.standardMinutes(DEFAULT_ALARM_TIMEOUT_MIN),
        reminderId: Int = AppConstants.REQUEST_REMINDER_ID,
    ) {
        val intent =
            Intent(context.applicationContext, AlarmReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(
                    context.applicationContext,
                    reminderId,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            }
        val now = timeProvider.now()
        val dtTarget = now.plus(reminderDurationFromNow)
        Timber.tag(Tags.LOCATION)
            .d("startReminder(reminderNextAt: %s)".withLogInstance(this), dtTarget)
//        // Needs additional permission
//        alarmManager.setExactAndAllowWhileIdle(
//            AlarmManager.RTC_WAKEUP,
//            dtTarget.millis,
//            intent,
//        )

        // No additional permission
        stopReminder(context, am, reminderId = reminderId)
        am.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            dtTarget.millis,
            intent,
        )
    }

    fun stopReminder(
        context: Context,
        am: AlarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager,
        reminderId: Int = AppConstants.REQUEST_REMINDER_ID,
    ) {
        Timber.tag(Tags.LOCATION)
            .d("stopReminder()".withLogInstance(this))
        val intent = Intent(context, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                0
            )
        }
        am.cancel(intent)
    }
}