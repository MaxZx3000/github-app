package com.example.finalgithubappsubmission.widget

import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import com.bumptech.glide.Glide
import com.example.finalgithubappsubmission.activity.HomeActivity
import com.example.finalgithubappsubmission.activity.UserDetailActivity
import com.example.finalgithubappsubmission.users.database.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object WidgetComponent{
    fun giveDetailPendingIntent(context: Context, userEntity: UserEntity): PendingIntent {
        val parentIntent = Intent(context, UserDetailActivity::class.java)
        parentIntent.putExtra(UserDetailActivity.MAIN_USER_KEY, userEntity)
        val homeIntent = Intent(context, HomeActivity::class.java)
        return TaskStackBuilder.create(context)
            .addNextIntent(homeIntent)
            .addNextIntent(parentIntent)
            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }
    fun loadBitmapImage(context: Context, userEntity: UserEntity, width: Int, height: Int): Bitmap{
        return Glide.with(context).asBitmap().load(userEntity.entityImageLink).submit(width, height).get()
    }
}