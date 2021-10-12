package com.example.finalgithubappsubmission.fragments

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.ContentObserver
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.provider.Settings
import android.widget.TimePicker
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.activity.SettingsActivity
import com.example.finalgithubappsubmission.contentprovider.FavoriteProvider
import com.example.finalgithubappsubmission.service.DailyAlertReceiver
import com.example.finalgithubappsubmission.settings.SettingsComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class PreferenceFragment: PreferenceFragmentCompat(), Preference.OnPreferenceClickListener, SharedPreferences.OnSharedPreferenceChangeListener, TimePickerDialog.OnTimeSetListener{
    companion object{
        const val TONE_REQUEST_CODE = 1200
        const val LANGUAGE_CHANGE_REQUEST_CODE = 1199
        const val DEFAULT_HOUR = 9
        const val DEFAULT_MINUTE = 0
        const val RINGTONE_CANCEL_STATUS = 0
    }
    private var dailyReminderTone: Preference? = null
    private var dailyReminderTime: Preference? = null
    private var colorAppTheme: Preference? = null
    private var dailyReminderSwitch: SwitchPreference? = null
    private var username: EditTextPreference? = null
    private var factoryReset: Preference? = null
    private var languagePreference: Preference? = null
    private lateinit var settingsPreference: SharedPreferences

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        init()
    }
    override fun onStart() {
        super.onStart()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }
    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }
    private fun init() {
        settingsPreference = SettingsComponent.settingsPreference

        username = findPreference(resources.getString(R.string.key_username))
        val sharedUsername = settingsPreference.getString(SettingsComponent.NAME_KEY, "No Name")
        username?.summary = sharedUsername
        username?.text = sharedUsername
        dailyReminderTone = findPreference(resources.getString(R.string.key_daily_reminder_tone))
        dailyReminderTone?.onPreferenceClickListener = this
        setRingtoneTitle()

        dailyReminderSwitch = findPreference(resources.getString(R.string.key_enable_daily_alert))
        dailyReminderSwitch?.isChecked = settingsPreference.getBoolean(SettingsComponent.DAILY_ALERT_ENABLE_KEY, false)

        dailyReminderTime = findPreference(resources.getString(R.string.key_daily_reminder_time))
        dailyReminderTime?.isEnabled  = dailyReminderSwitch?.isChecked == true

        val calendarInstance = getDailyAlertCalendarInstance(settingsPreference.getInt(SettingsComponent.DAILY_ALERT_HOUR_KEY, 9), settingsPreference.getInt(SettingsComponent.DAILY_ALERT_MINUTE_KEY, 0))
        dailyReminderTime?.summary = getStringDateFormat(calendarInstance)
        dailyReminderTime?.onPreferenceClickListener = this

        dailyReminderTone = findPreference(resources.getString(R.string.key_daily_reminder_tone))
        dailyReminderTone?.isEnabled = dailyReminderSwitch?.isChecked == true

        colorAppTheme = findPreference(resources.getString(R.string.key_color_app_theme))
        colorAppTheme?.onPreferenceClickListener = this@PreferenceFragment

        factoryReset = findPreference(resources.getString(R.string.key_factory_reset))
        factoryReset?.onPreferenceClickListener = this

        languagePreference = findPreference(resources.getString(R.string.key_language))
        languagePreference?.onPreferenceClickListener = this
    }
    private fun getDailyAlertCalendarInstance(hour: Int, minute: Int): Calendar {
        val calendar = Calendar.getInstance()
        with(calendar){
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }
        val timeDifference = calendar.timeInMillis - System.currentTimeMillis()
        if (timeDifference <= 0){
            calendar.add(Calendar.DATE, 1)
        }
        return calendar
    }
    private fun setRingtoneTitle(){
        val uri = SettingsComponent.settingsPreference.getString(SettingsComponent.TONE_URI_KEY, null)?.toUri()
        val ringtone = if (uri != null){
            RingtoneManager.getRingtone(context, uri)
        }
        else{
            RingtoneManager.getRingtone(context, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        }
        val ringtoneTitle = ringtone.getTitle(context)
        dailyReminderTone?.summary = ringtoneTitle
    }
    private fun getStringDateFormat(calendar: Calendar): String{
        val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault())
        return currentTime.format(calendar.time)
    }
    override fun onPreferenceClick(preference: Preference): Boolean {
        when(preference.key){
            resources.getString(R.string.key_daily_reminder_time) -> {
                dailyAlertTimeSetting()
            }
            resources.getString(R.string.key_daily_reminder_tone) -> {
                dailyAlertToneSetting()
            }
            resources.getString(R.string.key_color_app_theme) -> {
                setColorPrimaryDialog()
            }
            resources.getString(R.string.key_factory_reset) -> {
                setFactoryResetDialog()
            }
            resources.getString(R.string.key_language) -> {
                editLanguage()
            }
        }
        return true
    }

    override fun onSharedPreferenceChanged(sharedPreference: SharedPreferences, key: String) {
        when (key) {
            resources.getString(R.string.key_username) -> {
                setUsername(sharedPreference.getString(key, "No name"))
            }
            resources.getString(R.string.key_enable_daily_alert) -> {
                if (sharedPreference.getBoolean(key, false)){
                    setDailyAlert()
                }
                else{
                    unsetDailyAlert()
                }
            }
        }
    }

    private fun setFactoryResetDialog(){
        AlertDialog.Builder(context)
            .setCancelable(false)
            .setMessage(getString(R.string.reset_all_data))
            .setTitle(getString(R.string.warning))
            .setNegativeButton(getString(R.string.no)){ dialogInterface, _ ->
                dialogInterface.dismiss()
            }
            .setPositiveButton(getString(R.string.yes)){ _, _ ->
                clearData(FavoriteProvider.getAuthorityPath())
            }
            .create()
            .show()
    }
    private fun clearData(contentUri: Uri){
        unsetDailyAlert()
        val handlerThread = HandlerThread("delete_all_favorite_users")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        val contentObserver = object : ContentObserver(handler){}
        context?.contentResolver?.registerContentObserver(contentUri, true, contentObserver)
        GlobalScope.launch(Dispatchers.IO) {
            val deleteUri = Uri.parse("${contentUri}/${FavoriteProvider.DELETE_ALL_KEYWORD}")
            context?.contentResolver?.delete(deleteUri, null, null)
            context?.contentResolver?.notifyChange(contentUri, contentObserver)
            SettingsComponent.clearAllData(context as Context)
            handlerThread.quitSafely()
            context?.contentResolver?.unregisterContentObserver(contentObserver)
            activity?.setResult(SettingsActivity.RESET_ACTIVITY_RESULT_CODE)
            activity?.finish()
        }
    }
    private fun editLanguage(){
        val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
        startActivityForResult(intent, LANGUAGE_CHANGE_REQUEST_CODE)
    }
    private fun getDailyAlertPendingIntent(): PendingIntent {
        val intent = Intent(context as Context, DailyAlertReceiver::class.java)
        return PendingIntent.getBroadcast(context, DailyAlertReceiver.ID_REPEATING, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    private fun setDailyAlert(){
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        SettingsComponent.settingsPreference.edit{
            putBoolean(SettingsComponent.DAILY_ALERT_ENABLE_KEY, true)
        }
        dailyReminderTime?.isEnabled = true
        dailyReminderTone?.isEnabled = true
        val calendarInstance = getDailyAlertCalendarInstance(SettingsComponent.settingsPreference.getInt(SettingsComponent.DAILY_ALERT_HOUR_KEY, 9), SettingsComponent.settingsPreference.getInt(SettingsComponent.DAILY_ALERT_MINUTE_KEY, 0))
        dailyReminderTime?.summary = getStringDateFormat(calendarInstance)
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendarInstance.timeInMillis, AlarmManager.INTERVAL_DAY, getDailyAlertPendingIntent())
    }
    private fun unsetDailyAlert(){
        val alarmManager = context?.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        SettingsComponent.settingsPreference.edit {
            putBoolean(SettingsComponent.DAILY_ALERT_ENABLE_KEY, false)
        }
        dailyReminderTime?.isEnabled = false
        dailyReminderTone?.isEnabled = false
        getDailyAlertPendingIntent().cancel()
        alarmManager.cancel(getDailyAlertPendingIntent())
    }
    private fun setUsername(value: String?){
        username?.summary = value
        username?.icon
        SettingsComponent.settingsPreference.edit{
            putString(SettingsComponent.NAME_KEY, value)
        }
    }
    private fun setColorPrimaryDialog(){
        val dialogFragment = ColorDialogFragment()
        dialogFragment.iOKButton = object : ColorDialogFragment.IOKButton{
            override fun okButtonAction(colorInt: Int) {
                SettingsComponent.initializeAllColors()
                SettingsComponent.colorizeBar(activity as AppCompatActivity)
            }
        }
        dialogFragment.show(childFragmentManager, null)
    }
    private fun dailyAlertTimeSetting(){
        val timePicker = TimePickerDialog(context, this, DEFAULT_HOUR, DEFAULT_MINUTE, true)
        timePicker.updateTime(SettingsComponent.settingsPreference.getInt(SettingsComponent.DAILY_ALERT_HOUR_KEY, DEFAULT_HOUR), SettingsComponent.settingsPreference.getInt(SettingsComponent.DAILY_ALERT_MINUTE_KEY, DEFAULT_MINUTE))
        timePicker.setIcon(R.drawable.ic_time)
        timePicker.show()
    }
    private fun dailyAlertToneSetting(){
        val defaultRingtone = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
        val intent = Intent(RingtoneManager.ACTION_RINGTONE_PICKER)
        with(intent){
            putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.select_ringtone))
            putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALARM)
            putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
            putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, defaultRingtone)
        }
        startActivityForResult(intent, TONE_REQUEST_CODE)
    }
    private fun applyRingtone(data: Intent?){
        val ringtoneURI =  data?.getParcelableExtra<Uri>(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
        SettingsComponent.settingsPreference.edit{
            putString(SettingsComponent.TONE_URI_KEY, ringtoneURI.toString())
        }
        setRingtoneTitle()
    }
    override fun onTimeSet(p0: TimePicker?, hour: Int, minute: Int) {
        SettingsComponent.settingsPreference.edit{
            putInt(SettingsComponent.DAILY_ALERT_HOUR_KEY, hour)
            putInt(SettingsComponent.DAILY_ALERT_MINUTE_KEY, minute)
        }
        setDailyAlert()
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            TONE_REQUEST_CODE -> {
                if (resultCode != RINGTONE_CANCEL_STATUS) applyRingtone(data)
            }
            LANGUAGE_CHANGE_REQUEST_CODE -> {
                GlobalScope.launch(Dispatchers.IO) {
                    delay(200)
                    activity?.runOnUiThread {
                        activity?.recreate()
                    }
                }
            }
        }
    }
}