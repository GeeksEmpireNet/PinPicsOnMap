/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 1:45 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi

class BindService : Service() {

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (Build.VERSION.SDK_INT < 26) {
            startForeground(333, bindServiceLow())
        } else {
            startForeground(333, bindServiceHigh())
        }

        return Service.START_STICKY
    }

    override fun onCreate() {
        super.onCreate()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    protected fun bindServiceLow(): Notification {
        val mBuilder = Notification.Builder(this@BindService)
        mBuilder.setContentTitle(resources.getString(R.string.full_app_name))
        mBuilder.setContentText(resources.getString(R.string.bindDesc))
        mBuilder.setTicker(resources.getString(R.string.full_app_name))
        mBuilder.setSmallIcon(R.drawable.ic_notification)
        mBuilder.setColor(resources.getColor(R.color.default_color))

        val res = resources
        val bitmap = BitmapFactory.decodeResource(res, R.drawable.ic_launcher)
        mBuilder.setLargeIcon(bitmap)
        mBuilder.setAutoCancel(false)

        val open = Intent(this, TimeCheck::class.java)
        val openApp = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_UPDATE_CURRENT)

        mBuilder.setContentIntent(openApp)

        return mBuilder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    protected fun bindServiceHigh(): Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(packageName, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)

        val mBuilder = Notification.Builder(this)

        mBuilder.setContentTitle(resources.getString(R.string.full_app_name))
        mBuilder.setContentText(resources.getString(R.string.bindDesc))
        mBuilder.setTicker(resources.getString(R.string.full_app_name))
        mBuilder.setSmallIcon(R.drawable.ic_notification)
        mBuilder.setColor(resources.getColor(R.color.default_color))

        val res = resources
        val bM = BitmapFactory.decodeResource(res, R.drawable.ic_launcher)
        mBuilder.setLargeIcon(bM)
        mBuilder.setAutoCancel(false)
        mBuilder.setChannelId(packageName)

        val open = Intent(this, TimeCheck::class.java)
        val openApp = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_UPDATE_CURRENT)

        mBuilder.setContentIntent(openApp)

        return mBuilder.build()
    }
}
