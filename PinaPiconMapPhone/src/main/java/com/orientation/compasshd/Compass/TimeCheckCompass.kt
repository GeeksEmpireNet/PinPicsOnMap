/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 2:50 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Compass

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.google.firebase.auth.FirebaseAuth
import com.orientation.compasshd.BindService
import com.orientation.compasshd.Configuration
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.Functions.FunctionsClass
import com.orientation.compasshd.Util.Functions.FunctionsClassPreferences
import com.orientation.compasshd.Util.LoginCheckpoint
import com.orientation.compasshd.Util.SettingGUI
import java.util.*

class TimeCheckCompass : Activity() {

    lateinit var functionsClass: FunctionsClass

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)

        functionsClass = FunctionsClass(this@TimeCheckCompass)
        functionsClass.addAppShortcuts()

        val hrs = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hrs >= 18 || hrs < 6) {//Night
            time = "night"
        } else {
            time = "day"
        }

        val API = Build.VERSION.SDK_INT
        if (API >= 23) {
            requestPermissions(arrayOf(
                    android.Manifest.permission.INTERNET
            ), 666)
            if (!Settings.canDrawOverlays(applicationContext)
                    || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.GET_ACCOUNTS) != PackageManager.PERMISSION_GRANTED
                    || !functionsClass.readDefaultPreference("UserAgreement", false)) {
                startActivity(Intent(applicationContext, Configuration::class.java)
                        .putExtra("time", time)
                        .putExtra("TimeCheck", "TimeCheckCompass"))
                this.finish()
                return
            }
        }

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(applicationContext, LoginCheckpoint::class.java)
                    .putExtra("time", time)
                    .putExtra("TimeCheck", "TimeCheckCompass"))
        } else {
            startService(Intent(applicationContext, BindService::class.java))
            if (run == true) {
                startActivity(Intent(applicationContext, SettingGUI::class.java))
                finish()
                return
            }

            val compassConfiguration = FunctionsClassPreferences(applicationContext).CompassConfiguration()

            val pathDefault = Uri.parse("android.resource://" + packageName + "/" + R.drawable.pick_image)
            imagePath = compassConfiguration.readCompassConfig(FunctionsClassPreferences.COMPASS_IMAGE_FILE_PATH, pathDefault.path) as String?

            when (compassConfiguration.readCompassConfig(FunctionsClassPreferences.COMPASS_VIEW_SIZE_MODE, 2)) {
                1 -> imageSize = 100
                2 -> imageSize = 200
                3 -> imageSize = 300
            }
            run = true

            if (hrs >= 18 || hrs < 6) {//Night
                time = "night"
                try {
                    val night = Intent(applicationContext, FloatingCompass::class.java)
                    night.putExtra("time", "night")
                    night.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startService(night)
                } catch (e: Exception) {
                    e.printStackTrace(); }
            } else {
                time = "day"
                try {
                    val day = Intent(applicationContext, FloatingCompass::class.java)
                    day.putExtra("time", "day")
                    day.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startService(day)
                } catch (e: Exception) {
                    e.printStackTrace(); }
            }
        }
        finish()
    }

    companion object {
        var time: String = "day"
        var imagePath: String? = null
        var imageSize: Int = 0

        var run = false
    }
}
