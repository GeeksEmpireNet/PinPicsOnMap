/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 4:08 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Maps

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.core.CrashlyticsCore
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.orientation.compasshd.BuildConfig
import com.orientation.compasshd.Configuration
import com.orientation.compasshd.Util.Functions.FunctionsClass
import com.orientation.compasshd.Util.LoginCheckpoint
import io.fabric.sdk.android.Fabric
import java.util.*

class TimeCheckMap : Activity() {

    lateinit var functionsClass: FunctionsClass

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)

        val crashlyticsCore = CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build()
        Fabric.with(this, Crashlytics.Builder().core(crashlyticsCore).build())

        FirebaseApp.initializeApp(applicationContext)

        functionsClass = FunctionsClass(applicationContext)
        functionsClass.addAppShortcuts()

        val hrs = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hrs >= 18 || hrs < 6) {//Night
            time = "night"
        } else {
            time = "day"
        }

        val API = Build.VERSION.SDK_INT
        if (API > 22) {
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
                        .putExtra("TimeCheck", "TimeCheckMap"))
                this.finish()
                return
            }
        }

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(applicationContext, LoginCheckpoint::class.java)
                    .putExtra("time", time)
                    .putExtra("TimeCheck", "TimeCheckMap"))
        } else {
            val DayNight = Intent(applicationContext, PhoneMapsView::class.java)
            if (hrs >= 18 || hrs < 6) {//Night
                time = "night"
                DayNight.putExtra("time", "night")
            } else {
                time = "day"
                DayNight.putExtra("time", "day")
            }
            DayNight.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(DayNight)
        }
        finish()
    }

    companion object {
        var time: String = "day"
    }
}
