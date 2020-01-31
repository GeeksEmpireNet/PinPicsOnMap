/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.Functions

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.VectorDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.preference.PreferenceManager
import android.telephony.TelephonyManager
import android.text.Html
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.orientation.compasshd.R
import java.io.FileOutputStream
import java.util.*

class FunctionsClass {

    lateinit var activity: Activity
    lateinit var context: Context

    var API: Int = 23

    constructor(context: Context) {
        this.context = context

        API = android.os.Build.VERSION.SDK_INT
    }

    constructor(activity: Activity, context: Context) {
        this.activity = activity
        this.context = context

        API = android.os.Build.VERSION.SDK_INT
    }

    /*Location Info*/
    fun locationDetail(): String? {

        return PublicVariable.LOCATION_INFO_DETAIL
    }

    fun locationCityName(): String? {

        return PublicVariable.LOCATION_CITY_NAME
    }

    /*Maps*/
    fun setCameraPosition(googleMap: GoogleMap, LocationXY: LatLng, tilt: Boolean) {
        if (tilt) {
            val cameraPosition = CameraPosition.Builder()
                    .target(LocationXY)
                    .zoom(13f)
                    .bearing(0f)
                    .tilt(45f)
                    .build()
            val cameraUpdateFactory = CameraUpdateFactory.newCameraPosition(cameraPosition)
            googleMap.moveCamera(cameraUpdateFactory)
        } else if (!tilt) {
            val cameraPosition = CameraPosition.Builder()
                    .target(LocationXY)
                    .zoom(13f)
                    .bearing(0f)
                    .tilt(0f)
                    .build()
            val cameraUpdateFactory = CameraUpdateFactory.newCameraPosition(cameraPosition)
            googleMap.moveCamera(cameraUpdateFactory)
        }
    }

    /*File*/
    fun saveFileAppendLine(fileName: String, content: String) {
        try {
            val fileOutputStream: FileOutputStream = context.openFileOutput(fileName, Context.MODE_APPEND)
            fileOutputStream.write((content + "\n").toByteArray())

            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun savePreference(PreferenceName: String, KEY: String, VALUE: String) {
        val sharedPreferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        val editorSharedPreferences = sharedPreferences.edit()
        editorSharedPreferences.putString(KEY, VALUE)
        editorSharedPreferences.apply()
    }

    fun savePreference(PreferenceName: String, KEY: String, VALUE: Int) {
        val sharedPreferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        val editorSharedPreferences = sharedPreferences.edit()
        editorSharedPreferences.putInt(KEY, VALUE)
        editorSharedPreferences.apply()
    }

    fun savePreference(PreferenceName: String, KEY: String, VALUE: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        val editorSharedPreferences = sharedPreferences.edit()
        editorSharedPreferences.putBoolean(KEY, VALUE)
        editorSharedPreferences.apply()
    }

    fun saveDefaultPreference(KEY: String, VALUE: Int) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editorSharedPreferences = sharedPreferences.edit()
        editorSharedPreferences.putInt(KEY, VALUE)
        editorSharedPreferences.apply()
    }

    fun readFileLine(fileName: String): Array<String>? {
        if (context.getFileStreamPath(fileName).exists()) {
            return context.getFileStreamPath(fileName).readLines().toTypedArray()
        } else {
            return null
        }
    }

    fun readPreference(PreferenceName: String, KEY: String, defaultVALUE: String): String? {
        return context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE).getString(KEY, defaultVALUE)
    }

    fun readPreference(PreferenceName: String, KEY: String, defaultVALUE: Int): Int {
        return context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE).getInt(KEY, defaultVALUE)
    }

    fun readPreference(PreferenceName: String, KEY: String, defaultVALUE: Boolean): Boolean {
        return context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE).getBoolean(KEY, defaultVALUE)
    }

    fun readDefaultPreference(KEY: String, defaultVALUE: Int): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY, defaultVALUE)
    }

    fun readDefaultPreference(KEY: String, defaultVALUE: String): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY, defaultVALUE)
    }

    fun readDefaultPreference(KEY: String, defaultVALUE: Boolean): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY, defaultVALUE)
    }

    /*Device Check Point*/
    fun getTime(): String {
        val time: String
        val simpleDate: Calendar = Calendar.getInstance()
        val hours = simpleDate.get(Calendar.HOUR_OF_DAY)
        if (hours >= 18 || hours < 6) {//Night
            time = "night"
        } else {//Day
            time = "day"
        }
        return time
    }

    fun returnAPI(): Int {
        return API
    }

    fun appVersionName(pack: String): String {
        var Version = "0"

        try {
            val packInfo = context.packageManager.getPackageInfo(pack, 0)
            Version = packInfo.versionName
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return Version
    }

    fun appVersionCode(pack: String): Int {
        var VersionCode = 0

        try {
            val packInfo = context.packageManager.getPackageInfo(pack, 0)
            VersionCode = packInfo.versionCode
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return VersionCode
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }

    private fun capitalize(text: String?): String {
        if (text == null || text.length == 0) {
            return ""
        }
        val first = text[0]
        return if (Character.isUpperCase(first)) {
            text
        } else {
            Character.toUpperCase(first) + text.substring(1)
        }
    }

    fun getCountryIso(): String {
        var countryISO = "Undefined"
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            countryISO = telephonyManager.simCountryIso
            if (countryISO.length < 2) {
                countryISO = "Undefined"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            countryISO = "Undefined"
        }

        return countryISO.toUpperCase()
    }

    fun networkConnection(): Boolean {
        val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

    /*Action Center*/
    fun notificationCreator(titleText: String, contentText: String, notificationId: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val mBuilder = Notification.Builder(context)
            mBuilder.setContentTitle(Html.fromHtml("<b><font color='" + context.getColor(R.color.default_color_darker) + "'>" + titleText + "</font></b>"))
            mBuilder.setContentText(Html.fromHtml("<font color='" + context.getColor(R.color.default_color_light) + "'>" + contentText + "</font>"))
            mBuilder.setTicker(context.resources.getString(R.string.app_name))
            mBuilder.setSmallIcon(R.drawable.ic_notification)
            mBuilder.setAutoCancel(true)
            mBuilder.setColor(context.getColor(R.color.default_color))
            mBuilder.setPriority(Notification.PRIORITY_HIGH)

            val newUpdate = Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.play_store_link) + context.packageName))
            val newUpdatePendingIntent = PendingIntent.getActivity(context, 5, newUpdate, PendingIntent.FLAG_UPDATE_CURRENT)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val notificationChannel = NotificationChannel(context.packageName, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(notificationChannel)
                mBuilder.setChannelId(context.packageName)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val builderActionNotification = Notification.Action.Builder(
                        null,
                        context.getString(R.string.rate),
                        newUpdatePendingIntent
                )
                mBuilder.addAction(builderActionNotification.build())
            }
            mBuilder.setContentIntent(newUpdatePendingIntent)
            notificationManager.notify(notificationId, mBuilder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /*GUI*/
    fun drawableToBitmap(drawable: Drawable): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is VectorDrawable) {
            bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap!!)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        } else if (drawable is BitmapDrawable) {
            if (drawable.bitmap != null) {
                bitmap = drawable.bitmap
            }
        } else if (drawable is LayerDrawable) {

            bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap!!)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        } else {
            bitmap = Bitmap.createBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap!!)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
        return bitmap
    }

    /*Firebase Remote Config*/
    fun versionCodeRemoteConfigKey(): String {
        var versionCodeKey: String? = null
        if (readPreference(".BETA", "isBetaTester", false)) {
            versionCodeKey = context.getString(R.string.BETAintegerVersionCodeNewUpdateWear)
        } else {
            versionCodeKey = context.getString(R.string.integerVersionCodeNewUpdateWear)
        }
        return versionCodeKey
    }

    fun versionNameRemoteConfigKey(): String {
        var versionCodeKey: String? = null
        if (readPreference(".BETA", "isBetaTester", false)) {
            versionCodeKey = context.getString(R.string.BETAstringVersionNameNewUpdateWear)
        } else {
            versionCodeKey = context.getString(R.string.stringVersionNameNewUpdateWear)
        }
        return versionCodeKey
    }

    fun upcomingChangeLogRemoteConfigKey(): String {
        var versionCodeKey: String? = null
        if (readPreference(".BETA", "isBetaTester", false)) {
            versionCodeKey = context.getString(R.string.BETAstringUpcomingChangeLogWear)
        } else {
            versionCodeKey = context.getString(R.string.stringUpcomingChangeLogWear)
        }
        return versionCodeKey
    }
}