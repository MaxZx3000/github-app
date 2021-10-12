package com.example.finalgithubappsubmission.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.children
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.users.database.UserDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object SettingsComponent{
    private const val SETTINGS_PREFERENCE_KEY = "com.example.finalgithubappsubmission.SETTINGS_PREFERENCE_KEY"
    const val NAME_KEY = "com.example.finalgithubappsubmission.NAME_KEY"
    const val DAILY_ALERT_HOUR_KEY = "com.example.finalgithubappsubmission.TIME_KEY"
    const val DAILY_ALERT_MINUTE_KEY = "com.example.finalgithubappsubmission.MINUTE_KEY"
    const val TONE_URI_KEY = "com.example.finalgithubappsubmission.TONE_KEY"
    const val RED_KEY = "com.example.finalgithubappsubmission.RED_KEY"
    const val LOCALE_KEY = "com.example.finalgithubappsubmission.LOCALE_KEY"
    const val GREEN_KEY = "com.example.finalgithubappsubmission.GREEN_KEY"
    const val BLUE_KEY = "com.example.finalgithubappsubmission.BLUE_KEY"
    private const val WIDGET_ENABLE_KEY = "com.example.finalgithubappsubmission.DARK_MODE_KEY"
    const val KEYWORDS_SET_KEY = "com.example.finalgithubappsubmission.KEYWORDS_SET_KEY"
    const val DAILY_ALERT_ENABLE_KEY = "com.example.finalgithubappsubmission.DAILY_ALERT_ENABLE_KEY"
    var redColorValue = 0
    var greenColorValue = 0
    var blueColorValue = 0
    const val DAILY_ALERT_DEFAULT_HOUR = 9
    private var colorAccentValue = 0
    private const val MAX_RGB_VALUE = 255
    private const val ACCENTIZE_FORMULA = 0.9
    lateinit var settingsPreference: SharedPreferences
    private var mutableKeywords = mutableSetOf<String>()
    fun initializePreference(context: Context){
        settingsPreference = context.getSharedPreferences(SETTINGS_PREFERENCE_KEY, Context.MODE_PRIVATE)
        val keywordsPreference = settingsPreference.getStringSet(KEYWORDS_SET_KEY, null)
        if (settingsPreference.getString(NAME_KEY, null) == null){
            initializeDefaultPreference()
        }
        if (keywordsPreference != null){
            mutableKeywords.addAll(keywordsPreference)
        }
        initializeAllColors()
    }

    fun clearAllData(context: Context){
        mutableKeywords.clear()
        settingsPreference.edit { clear() }
        GlobalScope.launch(Dispatchers.IO) {
            UserDatabase.getDatabase(context as Context, null).userDAO().deleteAll()
        }
    }
    fun overrideActivityColor(activity: AppCompatActivity, rootView: ViewGroup){
        colorizeBar(activity)
        colorizeTaggedViews(activity, rootView)
    }
    private fun initializeDefaultPreference(){
        settingsPreference.edit{
            putInt(RED_KEY, 155)
            putInt(GREEN_KEY, 155)
            putInt(BLUE_KEY, 155)
            putBoolean(DAILY_ALERT_ENABLE_KEY, false)
            putInt(DAILY_ALERT_MINUTE_KEY, 0)
            putInt(DAILY_ALERT_HOUR_KEY, DAILY_ALERT_DEFAULT_HOUR)
            putBoolean(WIDGET_ENABLE_KEY, false)
            putStringSet(KEYWORDS_SET_KEY, emptySet())
        }
    }
    fun addHistoryData(keyword: String){
        mutableKeywords.add(keyword)
        settingsPreference.edit()
            .putStringSet(KEYWORDS_SET_KEY, mutableKeywords)
            .apply()
    }
    fun initializeAllColors(){
        redColorValue = settingsPreference.getInt(RED_KEY, 0)
        greenColorValue = settingsPreference.getInt(GREEN_KEY, 0)
        blueColorValue = settingsPreference.getInt(BLUE_KEY, 0)
        colorAccentValue = generateAccentColor()
    }
    fun generatePrimaryColor(): Int{
        return Color.rgb(redColorValue, greenColorValue, blueColorValue)
    }
    fun generatePrimaryDarkColor(): Int{
        return Color.rgb(redColorValue / 2, greenColorValue / 2, blueColorValue / 2)
    }
    fun generateRedColor(): Int{
        return Color.rgb(255, 0, 0)
    }
    fun getAccentColor(): Int{
        return colorAccentValue
    }
    fun getDarkAccentColor(): Int{
        return colorAccentValue / 2
    }

    private fun generateAccentColor(): Int{
        val minValue = minOf(redColorValue, greenColorValue, blueColorValue)
        val accentedMinValue: Int
        if (minValue < MAX_RGB_VALUE / 2){
            accentedMinValue =  minValue + (ACCENTIZE_FORMULA * (MAX_RGB_VALUE - minValue)).toInt()
        } else{
            accentedMinValue =  minValue - (ACCENTIZE_FORMULA * minValue).toInt()
        }
        var accentColor = Color.rgb(redColorValue, greenColorValue, blueColorValue)
        when(minValue) {
            redColorValue -> {
                accentColor = Color.rgb(accentedMinValue, greenColorValue, blueColorValue)
            }
            greenColorValue -> {
                accentColor = Color.rgb(redColorValue, accentedMinValue, blueColorValue)
            }
            blueColorValue -> {
                accentColor = Color.rgb(redColorValue, greenColorValue, accentedMinValue)
            }
        }
        return accentColor
    }
    fun colorizeTaggedViews(context: Context, rootView: ViewGroup){
        for (colorPrimaryTaggedView in rootView.children.iterator()){
            when(colorPrimaryTaggedView.tag){
                context.resources.getString(R.string.color_primary_tag) -> {
                    colorPrimaryTaggedView.backgroundTintList = ColorStateList.valueOf(generatePrimaryColor())
                }
                context.resources.getString(R.string.color_primary_dark_tag) -> {
                    colorPrimaryTaggedView.backgroundTintList = ColorStateList.valueOf(generatePrimaryDarkColor())
                }
                context.resources.getString(R.string.color_accent_tag) -> {
                    colorPrimaryTaggedView.backgroundTintList = ColorStateList.valueOf(colorAccentValue)
                }
            }
            if (colorPrimaryTaggedView is ViewGroup){
                colorizeTaggedViews(context, colorPrimaryTaggedView)
            }
        }
    }
    fun colorizeStatusBarOnly(activity: AppCompatActivity){
        activity.window.statusBarColor = generatePrimaryDarkColor()
    }
    fun colorizeBar(activity: AppCompatActivity){
        activity.supportActionBar?.setBackgroundDrawable(ColorDrawable(generatePrimaryColor()))
        activity.window.statusBarColor = generatePrimaryDarkColor()
    }
}