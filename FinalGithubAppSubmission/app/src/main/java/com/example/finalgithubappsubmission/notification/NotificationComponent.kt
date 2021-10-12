package com.example.finalgithubappsubmission.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.finalgithubappsubmission.settings.SettingsComponent

object NotificationComponent{
    class NotificationSettings(
        val context: Context,
        val channelID: String,
        val notificationID: Int,
        val pendingIntent: PendingIntent,
        val vibration: LongArray,
        val title: String,
        val description: String,
        val drawableId: Int
    )
    private const val ONE_TIME = 100
    fun setNotification(notificationSettings: NotificationSettings){
        lateinit var notificationCompat: Notification
        with(notificationSettings){
            notificationCompat = NotificationCompat.Builder(context, channelID)
                .setAutoCancel(true)
                .setSmallIcon(drawableId)
                .setContentTitle(title)
                .setContentIntent(pendingIntent)
                .setContentText(description)
                .setVibrate(vibration)
                .setColor(SettingsComponent.generatePrimaryDarkColor())
                .setChannelId(channelID)
                .build()
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val notificationChannel = NotificationChannel(channelID, title, NotificationManager.IMPORTANCE_DEFAULT)
                notificationChannel.vibrationPattern = vibration
                notificationChannel.enableVibration(true)
                notificationManager.createNotificationChannel(notificationChannel)
            }
            notificationManager.notify(notificationID, notificationCompat)
        }
    }
    fun checkIfNotificationExist(notificationID: Int, context: Context): Boolean{
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            for (n in notificationManager.activeNotifications){
                if (n.id == notificationID){
                    return true
                }
            }
        }
        return false
    }
    fun cancelNotification(notificationID: Int, context: Context){
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationID)
    }
    fun getPendingIntent(context: Context, destClass: Class<*>, intentFlag: Int): PendingIntent{
        val intent = Intent(context, destClass)
        intent.flags = intentFlag
        return PendingIntent.getActivity(context, ONE_TIME, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

}