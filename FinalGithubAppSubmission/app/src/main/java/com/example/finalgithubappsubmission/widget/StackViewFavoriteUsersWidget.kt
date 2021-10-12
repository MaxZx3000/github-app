package com.example.finalgithubappsubmission.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.finalgithubappsubmission.R
import com.example.finalgithubappsubmission.users.database.UserEntity

class StackViewFavoriteUsersWidget : AppWidgetProvider() {
    companion object{
        const val OPEN_DETAIL_USER = "com.example.finalgithubappsubmission.OPEN_DETAIL_USER"
        const val EXTRA_ITEM = "com.example.finalgithubappsubmission.EXTRA_ITEM"
        const val EXTRA_PEOPLE_ITEM = "com.example.finalgithubappsubmission.EXTRA_PEOPLE_ITEM"
        fun initializeView(context: Context){
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val remoteStackViewIntent = Intent(context, StackWidgetService::class.java)
            val stackViewFavoriteUsersWidgetComponent = ComponentName(context, StackViewFavoriteUsersWidget::class.java)
            val views = RemoteViews(context.packageName, R.layout.stack_view_favorite_users_widget)
            val pendingIntent = getStackViewPendingBroadcast(context)
            with(views){
                setRemoteAdapter(R.id.stack_view, remoteStackViewIntent)
                setPendingIntentTemplate(R.id.stack_view, pendingIntent)
            }
            appWidgetManager.updateAppWidget(stackViewFavoriteUsersWidgetComponent, views)
        }
        private fun getStackViewPendingBroadcast(context: Context): PendingIntent{
            val broadcastIntent = Intent(context, StackViewFavoriteUsersWidget::class.java)
            return PendingIntent.getBroadcast(context, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == OPEN_DETAIL_USER){
            val bundle = intent.getBundleExtra(EXTRA_ITEM)
            val userEntity = bundle?.getParcelable<UserEntity>(EXTRA_PEOPLE_ITEM)
            val pendingIntent = WidgetComponent.giveDetailPendingIntent(context, userEntity as UserEntity)
            pendingIntent.send()
        }
    }
    override fun onEnabled(context: Context) {

    }

    override fun onDisabled(context: Context) {

    }
    private fun updateAppWidget(
        context: Context
    ) {
        initializeView(context)
    }
}