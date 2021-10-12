package com.example.finalgithubappsubmission.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.contentprovider.FavoriteProvider
import com.example.finalgithubappsubmission.contentprovider.MappingHelper
import com.example.finalgithubappsubmission.users.database.UserEntity

class StackRemoteViewFactory(private val context: Context): RemoteViewsService.RemoteViewsFactory{
    companion object{
        private const val FORCE_ITEM_COUNT = 5
    }
    private var userEntityArrayList = ArrayList<UserEntity>()
    private var userEntityArrayListSize = 0
    private lateinit var handlerThread: HandlerThread
    private lateinit var contentObserver: ContentObserver
    private fun setUserDataInformation(){
        val cursor = context.contentResolver.query(FavoriteProvider.getAuthorityPath(), null, null, null, null, null)
        val cursoredArrayList = MappingHelper.convertCursorToArrayList(cursor)
        userEntityArrayList.addAll(cursoredArrayList)
        userEntityArrayListSize = userEntityArrayList.size
    }
    override fun onCreate() {
        handlerThread = HandlerThread("widget_handler_thread")
        handlerThread.start()
        val handler = Handler(handlerThread.looper)
        contentObserver = object : ContentObserver(handler){
            override fun onChange(selfChange: Boolean) {
                super.onChange(selfChange)
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val widget = ComponentName(context, StackViewFavoriteUsersWidget::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(widget)
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.stack_view)
            }
        }
        context.contentResolver.registerContentObserver(FavoriteProvider.getAuthorityPath(), true, contentObserver)
    }
    override fun getLoadingView(): RemoteViews? = null

    override fun getItemId(p0: Int): Long = 0

    override fun onDataSetChanged() {
        userEntityArrayList.clear()
        val identityToken= Binder.clearCallingIdentity()
        setUserDataInformation()
        Binder.restoreCallingIdentity(identityToken)
    }

    override fun hasStableIds(): Boolean = false
    private fun initializeEmptyView(imageItemRemoteView: RemoteViews): RemoteViews{
        with(imageItemRemoteView){
            setViewVisibility(R.id.container_data, View.INVISIBLE)
            setViewVisibility(R.id.tv_no_data, View.VISIBLE)
            setPendingIntentTemplate(R.id.img_favorite_user, null)
        }
        return imageItemRemoteView
    }
    private fun initializeDataView(imageItemRemoteView: RemoteViews, position: Int): RemoteViews{
        val fillInIntent = Intent()
        fillInIntent.action = StackViewFavoriteUsersWidget.OPEN_DETAIL_USER
        val bundle = Bundle()
        bundle.putParcelable(
            StackViewFavoriteUsersWidget.EXTRA_PEOPLE_ITEM,
            userEntityArrayList[position]
        )
        fillInIntent.putExtra(StackViewFavoriteUsersWidget.EXTRA_ITEM, bundle)
        with(imageItemRemoteView) {
            setViewVisibility(R.id.tv_no_data, View.GONE)
            setViewVisibility(R.id.container_data, View.VISIBLE)
            setOnClickFillInIntent(R.id.img_favorite_user, fillInIntent)
            setTextViewText(R.id.tv_username, userEntityArrayList[position].entityName)
            try {
                setImageViewBitmap(
                    R.id.img_favorite_user,
                    WidgetComponent.loadBitmapImage(
                        context,
                        userEntityArrayList[position],
                        240,
                        240
                    )
                )
            } catch (e: Exception) {

            }
        }
        return imageItemRemoteView
    }
    override fun getViewAt(position: Int): RemoteViews?{
        val imageItemRemoteView = RemoteViews(context.packageName, R.layout.stack_view_widget_item)
        return if (position < userEntityArrayListSize) {
            initializeDataView(imageItemRemoteView, position)
        } else{
            initializeEmptyView(imageItemRemoteView)
        }
    }

    override fun getCount(): Int {
        return if (userEntityArrayListSize <= 5){
            FORCE_ITEM_COUNT
        } else{
            userEntityArrayList.size
        }
    }

    override fun getViewTypeCount(): Int = 1

    override fun onDestroy() {
        handlerThread.quitSafely()
        context.contentResolver.unregisterContentObserver(contentObserver)
        userEntityArrayList.clear()
    }

}