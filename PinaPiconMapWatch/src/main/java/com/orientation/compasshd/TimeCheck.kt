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

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.orientation.compasshd.Util.Functions.FunctionsClass
import java.util.*

class TimeCheck : Activity() {

    lateinit var functionsClass: FunctionsClass

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        functionsClass = FunctionsClass(this@TimeCheck, applicationContext)

        if (!Settings.canDrawOverlays(applicationContext)
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.INTERNET) == PackageManager.PERMISSION_DENIED) {
            startActivity(Intent(applicationContext, Configuration::class.java))
            this.finish()
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(applicationContext, BindService::class.java))
        } else {
            startService(Intent(applicationContext, BindService::class.java))
        }
        if (run) {
            finish()
            return
        }

        val sharedPreferences = getSharedPreferences(".Config", Context.MODE_PRIVATE)
        val pathDefault = Uri.parse("android.resource://" + packageName + "/" + R.drawable.pick_image)
        imagePath = sharedPreferences.getString("path", pathDefault.path)

        imageSize = 75
        run = true

        val hrs = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hrs >= 18 || hrs < 6) {//Night
            time = "night"
            try {
                val night = Intent(this@TimeCheck, FloatingCompass::class.java)
                night.putExtra("time", "night")
                night.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startService(night)
            } catch (e: Exception) {
                e.printStackTrace();
            } finally {

            }
        } else {
            time = "day"
            try {
                val day = Intent(this@TimeCheck, FloatingCompass::class.java)
                day.putExtra("time", "day")
                day.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startService(day)
            } catch (e: Exception) {
                e.printStackTrace();
            } finally {

            }
        }

        finish()
    }

    companion object {
        var time: String = ""
        var imagePath: String? = null
        var imageSize: Int = 0

        var run = false
    }
}
