/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
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
import com.orientation.compasshd.Compass.TimeCheckCompass
import com.orientation.compasshd.Util.SettingGUI

class BindService : Service() {

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(333, if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { bindServiceHigh() } else { bindServiceLow() })

        return Service.START_STICKY
    }

    private fun bindServiceLow(): Notification {
        val notificationBuilder = Notification.Builder(this)
        notificationBuilder.setContentTitle(getString(R.string.full_app_name))
        notificationBuilder.setContentText(getString(R.string.bindDesc))
        notificationBuilder.setTicker(getString(R.string.full_app_name))
        notificationBuilder.setSmallIcon(R.drawable.ic_notification)
        notificationBuilder.setColor(getColor(R.color.default_color))

        val res = resources
        val bM = BitmapFactory.decodeResource(res, R.drawable.ic_launcher)
        notificationBuilder.setLargeIcon(bM)
        notificationBuilder.setAutoCancel(false)

        val open = Intent(this, TimeCheckCompass::class.java)
        val main = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_UPDATE_CURRENT)

        val settingGUI = Intent(this, SettingGUI::class.java)
        val pV = PendingIntent.getActivity(this, 0, settingGUI, PendingIntent.FLAG_UPDATE_CURRENT)

        notificationBuilder.addAction(0, getString(R.string.preferencesCompass), pV)
        notificationBuilder.setContentIntent(main)

        return notificationBuilder.build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun bindServiceHigh(): Notification {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationChannel = NotificationChannel(packageName, getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
        notificationManager.createNotificationChannel(notificationChannel)

        val notificationBuilder = Notification.Builder(this, packageName)

        notificationBuilder.setContentTitle(getString(R.string.full_app_name))
        notificationBuilder.setContentText(getString(R.string.bindDesc))
        notificationBuilder.setTicker(getString(R.string.full_app_name))
        notificationBuilder.setSmallIcon(R.drawable.ic_notification)
        notificationBuilder.setColor(getColor(R.color.default_color))

        val res = resources
        val bM = BitmapFactory.decodeResource(res, R.drawable.ic_launcher)
        notificationBuilder.setLargeIcon(bM)
        notificationBuilder.setAutoCancel(false)
        notificationBuilder.setChannelId(packageName)

        val open = Intent(this, TimeCheckCompass::class.java)
        val main = PendingIntent.getActivity(this, 0, open, PendingIntent.FLAG_UPDATE_CURRENT)

        val settingGUI = Intent(this, SettingGUI::class.java)
        val pV = PendingIntent.getActivity(this, 0, settingGUI, PendingIntent.FLAG_UPDATE_CURRENT)

        notificationBuilder.addAction(0, getString(R.string.preferencesCompass), pV)
        notificationBuilder.setContentIntent(main)

        return notificationBuilder.build()
    }
}
