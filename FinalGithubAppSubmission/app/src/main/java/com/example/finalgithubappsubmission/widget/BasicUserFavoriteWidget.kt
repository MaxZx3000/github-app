package com.example.finalgithubappsubmission.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.HandlerThread
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.contentprovider.FavoriteProvider
import com.example.finalgithubappsubmission.contentprovider.MappingHelper
import com.example.finalgithubappsubmission.settings.SettingsComponent
import com.example.finalgithubappsubmission.users.database.UserDAO
import com.example.finalgithubappsubmission.users.database.UserDatabase
import com.example.finalgithubappsubmission.users.database.UserEntity
import kotlinx.coroutines.*
import java.util.logging.Handler
import kotlin.random.Random

class BasicUserFavoriteWidget : AppWidgetProvider(){
    private lateinit var contentObserver: ContentObserver
    private lateinit var handlerThread: HandlerThread
    companion object{
        private const val BACKGROUND_KEYWORD = "setBackgroundColor"
        fun initializeView(context: Context){
            val userEntity = giveRandomUserEntity(context)
            if (userEntity != null) initializeUserFavoriteViews(context, userEntity)
            else initializeUserFavoriteEmptyViews(context)
        }
        private fun giveRandomUserEntity(context: Context): UserEntity?{
            val uri = Uri.parse("${FavoriteProvider.getAuthorityPath()}/${FavoriteProvider.RANDOM_USER_FAVORITE}")
            val cursor = context.contentResolver.query(uri, null, null, null, null, null) ?: return null
            cursor.moveToFirst()
            val arrayList = MappingHelper.convertCursorToArrayList(cursor)
            cursor.close()
            return arrayList[0]
        }
        private fun initializeUserFavoriteViews(context: Context, userEntity: UserEntity){
            val manager = AppWidgetManager.getInstance(context)
            val remoteView = RemoteViews(context.packageName, R.layout.favorite_widget)
            val favoriteWidget = ComponentName(context, BasicUserFavoriteWidget::class.java)
            with(remoteView){
                setEmptyDataBackground(this, context.getString(R.string.cannot_process_image))
                initializeImage(context, this, userEntity)
                setTextViewText(R.id.tv_username, userEntity.entityName)
                setViewVisibility(R.id.btn_details, View.VISIBLE)
                setViewVisibility(R.id.btn_random, View.VISIBLE)
                setViewVisibility(R.id.tv_username, View.VISIBLE)
                val appWidgetIds = manager.getAppWidgetIds(favoriteWidget)
                setOnClickPendingIntent(R.id.btn_details, WidgetComponent.giveDetailPendingIntent(context, userEntity))
                setOnClickPendingIntent(R.id.btn_random, giveBroadcastPendingIntent(context, appWidgetIds))
            }
            manager.updateAppWidget(favoriteWidget, remoteView)
        }
        private fun initializeImage(context: Context, remoteView: RemoteViews, userEntity: UserEntity){
            with(remoteView){
                try{
                    val bitmapImage = WidgetComponent.loadBitmapImage(context, userEntity, 220, 220)
                    setImageViewBitmap(R.id.img_favorite_user, bitmapImage)
                    setViewVisibility(R.id.container_watermark, View.GONE)
                }
                catch (e: Exception){

                }
            }
        }
        private fun initializeUserFavoriteEmptyViews(context: Context){
            val widget = ComponentName(context, BasicUserFavoriteWidget::class.java)
            val manager = AppWidgetManager.getInstance(context)
            val remoteView = RemoteViews(context.packageName, R.layout.favorite_widget)
            with(remoteView){
                setViewVisibility(R.id.container_watermark, View.VISIBLE)
                setViewVisibility(R.id.btn_details, View.GONE)
                setViewVisibility(R.id.btn_random, View.GONE)
                setViewVisibility(R.id.tv_username, View.GONE)
                setEmptyDataBackground(this, context.getString(R.string.empty_image_data))
            }
            manager.updateAppWidget(widget, remoteView)
        }
        private fun setEmptyDataBackground(remoteView: RemoteViews, message: String){
            with(remoteView){
                setViewVisibility(R.id.container_watermark, View.VISIBLE)
                setTextViewText(R.id.tv_message, message)
                setImageViewResource(R.id.img_favorite_user, SettingsComponent.generatePrimaryDarkColor())
                setInt(R.id.img_favorite_user, BACKGROUND_KEYWORD, SettingsComponent.generatePrimaryDarkColor())
            }
        }
        private fun giveBroadcastPendingIntent(context: Context, appWidgetIds: IntArray): PendingIntent{
            val intent = Intent()
            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        val handlerThread = HandlerThread("basic_user_favorite_widget")
        handlerThread.start()
        val handler = android.os.Handler(handlerThread.looper)
        val contentObserver = object : ContentObserver(handler){
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                initializeView(context)
            }
        }
        context.contentResolver.registerContentObserver(FavoriteProvider.getAuthorityPath(), true, contentObserver)
    }
    override fun onDisabled(context: Context) {
        handlerThread.quitSafely()
        context.contentResolver.unregisterContentObserver(contentObserver)
    }
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        GlobalScope.launch(Dispatchers.IO) {
            initializeView(context)
        }
    }
}
