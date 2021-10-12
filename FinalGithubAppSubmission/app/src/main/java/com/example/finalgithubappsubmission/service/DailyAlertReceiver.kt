package com.example.finalgithubappsubmission.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import androidx.core.net.toUri
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.activity.HomeActivity
import com.example.finalgithubappsubmission.notification.NotificationComponent
import com.example.finalgithubappsubmission.settings.SettingsComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DailyAlertReceiver: BroadcastReceiver(){
    companion object{
        const val ID_REPEATING = 101
        private const val DAILY_ALERT_CHANNEL_ID = "com.example.finalgithubappsubmission.DAILY_ALERT_CHANNEL_ID"
        private const val NOTIFICATION_ID = 200
        private const val RINGTONE_DURATION: Long = 6000
    }
    override fun onReceive(context: Context, p1: Intent) {
        val notificationSettings = NotificationComponent.NotificationSettings(
            context,
            DAILY_ALERT_CHANNEL_ID,
            NOTIFICATION_ID,
            NotificationComponent.getPendingIntent(context, HomeActivity::class.java, Intent.FLAG_ACTIVITY_CLEAR_TOP),
            LongArray(5){500},
            context.getString(R.string.notification_hello_title),
            context.getString(R.string.notification_hello_description),
            R.drawable.ic_people
        )
        val ringtoneName = SettingsComponent.settingsPreference.getString(SettingsComponent.TONE_URI_KEY, null)
        val actualRingtone: Ringtone = if (ringtoneName != null){
            RingtoneManager.getRingtone(context, ringtoneName.toUri())
        }
        else{
            RingtoneManager.getRingtone(context, RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM))
        }
        actualRingtone.play()
        GlobalScope.launch(Dispatchers.Unconfined) {
            delay(RINGTONE_DURATION)
            actualRingtone.stop()
        }
        NotificationComponent.setNotification(notificationSettings)
    }
}