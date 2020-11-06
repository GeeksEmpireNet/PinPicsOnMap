/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 3:13 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.Functions

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.*
import android.graphics.drawable.*
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.telephony.TelephonyManager
import android.text.Html
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.wearable.Asset
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import com.orientation.compasshd.BuildConfig
import com.orientation.compasshd.R
import java.io.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class FunctionsClass @Inject constructor(var context: Context) {

    val API: Int = android.os.Build.VERSION.SDK_INT

    /*Checkpoint*/
    fun returnAPI(): Int {
        return API
    }

    fun networkConnection(): Boolean {
        val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetworkInfo = connectivityManager.activeNetworkInfo
        return activeNetworkInfo != null && activeNetworkInfo.isConnected
    }

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

    /*Location Info*/
    fun locationDetail(): String? {

        return PublicVariable.LOCATION_INFO_DETAIL
    }

    fun locationCountryName(): String? {

        return PublicVariable.LOCATION_COUNTRY_NAME
    }

    fun locationCityName(): String? {

        return PublicVariable.LOCATION_CITY_NAME
    }

    /*Maps*/
    fun tiltCameraInPosition(googleMap: GoogleMap, tilt: Boolean) {
        if (tilt) {
            val cameraPosition = CameraPosition.Builder()
                    .target(googleMap.cameraPosition.target)
                    .zoom(13f)
                    .bearing(0f)
                    .tilt(45f)
                    .build()
            val cameraUpdateFactory = CameraUpdateFactory.newCameraPosition(cameraPosition)
            googleMap.moveCamera(cameraUpdateFactory)
        } else if (!tilt) {
            val cameraPosition = CameraPosition.Builder()
                    .target(googleMap.cameraPosition.target)
                    .zoom(13f)
                    .bearing(0f)
                    .tilt(0f)
                    .build()
            val cameraUpdateFactory = CameraUpdateFactory.newCameraPosition(cameraPosition)
            googleMap.moveCamera(cameraUpdateFactory)
        }
    }

    /*AppShortcut*/
    fun addAppShortcuts() {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val shortcutManager = context.getSystemService(ShortcutManager::class.java) as ShortcutManager
                    shortcutManager.removeAllDynamicShortcuts()

                    val shortcutLabel: Array<String> = arrayOf(
                            context.getString(R.string.pinPicked),
                            context.getString(R.string.preferencesCompass),
                            context.getString(R.string.floatingshortcuts),
                            context.getString(R.string.supershortcuts)
                    )
                    val shortcutIcons: Array<Icon> = arrayOf(
                            Icon.createWithBitmap(drawableToBitmap(context.getDrawable(R.drawable.draw_picked)!!)),
                            Icon.createWithBitmap(drawableToBitmap(context.getDrawable(R.drawable.draw_preferences)!!)),
                            Icon.createWithBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.floating_shortcuts)),
                            Icon.createWithBitmap(BitmapFactory.decodeResource(context.resources, R.drawable.super_shortcuts))
                    )
                    val shortcutsIntent = arrayOf(
                            Intent().setAction("AddToMap").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            Intent().setAction("compass_preferences").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse(context.getString(R.string.link_shortcuts))).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            Intent().setAction(Intent.ACTION_VIEW).setData(Uri.parse(context.getString(R.string.link_super))).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                    val shortcutInfos = ArrayList<ShortcutInfo>()
                    shortcutInfos.clear()
                    for (i in 0..3) {
                        try {
                            val shortcutInfo: ShortcutInfo = ShortcutInfo.Builder(context, i.toString())
                                    .setShortLabel(shortcutLabel[i])
                                    .setLongLabel(shortcutLabel[i])
                                    .setIcon(shortcutIcons[i])
                                    .setIntent(shortcutsIntent[i])
                                    .setRank(i)
                                    .build()
                            shortcutInfos.add(shortcutInfo)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    shortcutManager.addDynamicShortcuts(shortcutInfos)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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

    fun saveFile(fileName: String, content: String) {
        try {
            val fOut = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            fOut.write(content.toByteArray())

            fOut.flush()
            fOut.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(IOException::class)
    fun readBytesFile(fileName: String): ByteArray {

        return context.getFileStreamPath(fileName).readBytes()
    }

    @Throws(IOException::class)
    fun readBytes(fileName: String): ByteArray {

        return context.getFileStreamPath(fileName).readBytes()
    }

    @Throws(Exception::class)
    fun readPrivateFile(fileName: String): String {

        return context.getFileStreamPath(fileName).readText(Charsets.UTF_8)
    }

    fun readFileLine(fileName: String): Array<String> {

        return context.getFileStreamPath(fileName).readLines().toTypedArray()
    }

    fun removeLine(fileName: String, lineToRemove: String) {
        try {
            val textLine = readFileLine(fileName)
            for (text in textLine) {
                if (text != lineToRemove) {
                    saveFileAppendLine(fileName + "TEMP", text)
                }
            }
            val fileOld = context.getFileStreamPath(fileName)
            fileOld.delete()
            val fileNew = context.getFileStreamPath(fileName + "TEMP")
            fileNew.renameTo(context.getFileStreamPath(fileName))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
        }
    }

    fun countLine(fileName: String): Int {
        var nLines = 0
        val file = context.getFileStreamPath(fileName)
        if (file.exists()) {
            try {
                val reader = BufferedReader(FileReader(context.getFileStreamPath(fileName)))
                while (reader.readLine() != null) {
                    nLines++
                }
                reader.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return nLines
    }

    fun countLineAssets(fileName: String): Int {
        var nLines = 0
        try {
            val reader = BufferedReader(InputStreamReader(context.assets.open(fileName)))
            while (reader.readLine() != null) {
                nLines++
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return nLines
    }

    fun readFromAssets(context: Context, fileName: String): ArrayList<String> {
        val contentLine = ArrayList<String>(countLineAssets(fileName))
        try {
            val reader = BufferedReader(InputStreamReader(context.assets.open(fileName)))
            var i = 0
            while ((reader.readLine()) != null) {
                contentLine.add(reader.readLine())
                i++
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return contentLine
    }

    fun editPlaceType(placeTypeRaw: String): String {
        var placeType = ""
        if (placeTypeRaw.contains("_")) {
            val partOne = placeTypeRaw.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            val partTwo = placeTypeRaw.split("_".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[1]
            placeType = capitalize(partOne) + " " + capitalize(partTwo)
        } else {
            placeType = capitalize(placeTypeRaw)
        }
        return placeType
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

    fun saveDefaultPreference(KEY: String, VALUE: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editorSharedPreferences = sharedPreferences.edit()
        editorSharedPreferences.putString(KEY, VALUE)
        editorSharedPreferences.apply()
    }

    fun saveDefaultPreference(KEY: String, VALUE: Int) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editorSharedPreferences = sharedPreferences.edit()
        editorSharedPreferences.putInt(KEY, VALUE)
        editorSharedPreferences.apply()
    }

    fun saveDefaultPreference(KEY: String, VALUE: Boolean) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editorSharedPreferences = sharedPreferences.edit()
        editorSharedPreferences.putBoolean(KEY, VALUE)
        editorSharedPreferences.apply()
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

    fun fileToAsset(fileName: String): Asset? {
        val byteStream: ByteArrayOutputStream? = null
        try {
            if (BuildConfig.DEBUG) {
                println(readPrivateFile(fileName))
            }

            var bytes = ByteArray(0)
            try {
                bytes = readBytes((fileName))
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return Asset.createFromBytes(bytes)

        } finally {
            if (null != byteStream) {
                try {
                    byteStream!!.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    /*Device Check Point*/
    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        return if (model.startsWith(manufacturer)) {
            capitalize(model)
        } else {
            capitalize(manufacturer) + " " + model
        }
    }

    fun capitalize(string: String?): String {
        if (string == null || string.length == 0) {
            return ""
        }
        val first = string[0]
        return if (Character.isUpperCase(first)) {
            string
        } else {
            Character.toUpperCase(first) + string.substring(1)
        }
    }

    fun getCountryIso(): String {
        var countryISO = "Unknown Area"
        try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            countryISO = telephonyManager.networkCountryIso.toUpperCase()
            if (countryISO.length < 2) {
                countryISO = "Unknown Area"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            countryISO = "Unknown Area"
        }

        return countryISO
    }

    /*GUI*/
    fun setColorAlpha(color: Int, alphaPercent: Float): Int {
        val alpha = Math.round(Color.alpha(color) * alphaPercent)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    fun Toast(toastContent: String, toastGravity: Int, toastColor: Int, textColor: Int) {
        val inflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.toast_view, null/*(ViewGroup) activity.findViewById(R.id.toastView)*/)

        val drawToast = context.getDrawable(R.drawable.toast_background) as LayerDrawable
        val backToast = drawToast.findDrawableByLayerId(R.id.backtemp) as GradientDrawable
        backToast.setColor(toastColor)

        val textView = layout.findViewById<View>(R.id.toastText) as TextView
        textView.text = Html.fromHtml("<small>$toastContent</small>")

        textView.background = drawToast
        textView.setTextColor(textColor)
        textView.setShadowLayer(0.02f, 2f, 2f, context.getColor(R.color.trans_dark_high))

        val toast = android.widget.Toast(context)
        toast.setGravity(Gravity.FILL_HORIZONTAL or toastGravity, 0, 0)
        toast.duration = android.widget.Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
    }

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

    fun bitmapToCircle(bitmap: Bitmap): Bitmap? {
        val output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        val canvas = Canvas(output)
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)

        paint.isAntiAlias = true;
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle((bitmap.getWidth() / 2).toFloat(), (bitmap.getHeight() / 2).toFloat(), (bitmap.getWidth() / 2).toFloat(), paint);
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output
    }

    fun DpToPixel(dp: Float): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun PixelToDp(px: Float): Float {
        val resources = context.resources
        val metrics = resources.displayMetrics
        return px / (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
    }

    fun DpToInteger(dp: Int): Int {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
    }

    /*Action Center*/
    fun notificationCreator(titleText: String, contentText: String, notificationId: Int) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val mBuilder = Notification.Builder(context)
            mBuilder.setContentTitle(Html.fromHtml("<b><font color='" + context.getColor(R.color.default_color_darker) + "'>" + titleText + "</font></b>"))
            mBuilder.setContentText(Html.fromHtml("<font color='" + context.getColor(R.color.default_color_light) + "'>" + contentText + "</font>"))
            mBuilder.setTicker(context.getString(R.string.app_name))
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
                        Icon.createWithResource(context, R.drawable.draw_share_menu),
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

    /* Send File to Watch Module */
    fun sendLocationsData(activity: Activity, dataPath: String, dataKey: String) {
        val putDataMapRequest = PutDataMapRequest.create(dataPath)
        putDataMapRequest.dataMap.putAsset(dataKey, fileToAsset(".Locations"))
        putDataMapRequest.dataMap.putLong("locationtime", Date().time)
        val putDataRequest = putDataMapRequest.asPutDataRequest()
        putDataRequest.setUrgent()

        val dataItemTask = Wearable.getDataClient(activity).putDataItem(putDataRequest)
        dataItemTask
                .addOnSuccessListener { dataItem ->
                    println("*** Locations Data Sent Successfully ***")
                }
                .addOnFailureListener(
                        object : OnFailureListener {
                            override fun onFailure(e: Exception) {
                                println("*** $e")
                            }
                        })
    }

    fun sendLocationsDetails(activity: Activity, dataPath: String, dataKey: String) {
        if (context.getFileStreamPath(".Details").exists()) {
            val putDataMapRequest = PutDataMapRequest.create(dataPath)
            putDataMapRequest.dataMap.putAsset(dataKey, fileToAsset(".Details"))
            putDataMapRequest.dataMap.putLong("detailtime", Date().time)
            val putDataRequest = putDataMapRequest.asPutDataRequest()
            putDataRequest.setUrgent()

            val dataItemTask = Wearable.getDataClient(activity).putDataItem(putDataRequest)
            dataItemTask
                    .addOnSuccessListener { dataItem ->
                        println("*** Details Data Sent Successfully ***")
                    }
                    .addOnFailureListener(
                            object : OnFailureListener {
                                override fun onFailure(e: Exception) {
                                    println("*** $e")
                                }
                            })
        }
    }

    /*Firebase Remote Config*/
    fun versionCodeRemoteConfigKey(): String {
        var versionCodeKey: String? = null
        if (readPreference(".BETA", "isBetaTester", false)) {
            versionCodeKey = context.getString(R.string.BETAintegerVersionCodeNewUpdatePhone)
        } else {
            versionCodeKey = context.getString(R.string.integerVersionCodeNewUpdatePhone)
        }
        return versionCodeKey
    }

    fun versionNameRemoteConfigKey(): String {
        var versionCodeKey: String? = null
        if (readPreference(".BETA", "isBetaTester", false)) {
            versionCodeKey = context.getString(R.string.BETAstringVersionNameNewUpdatePhone)
        } else {
            versionCodeKey = context.getString(R.string.stringVersionNameNewUpdatePhone)
        }
        return versionCodeKey
    }

    fun upcomingChangeLogRemoteConfigKey(): String {
        var versionCodeKey: String? = null
        if (readPreference(".BETA", "isBetaTester", false)) {
            versionCodeKey = context.getString(R.string.BETAstringUpcomingChangeLogPhone)
        } else {
            versionCodeKey = context.getString(R.string.stringUpcomingChangeLogPhone)
        }
        return versionCodeKey
    }

    fun upcomingChangeLogSummaryConfigKey(): String {
        var versionCodeKey: String? = null
        if (readPreference(".BETA", "isBetaTester", false)) {
            versionCodeKey = context.getString(R.string.BETAstringUpcomingChangeLogSummaryPhone)
        } else {
            versionCodeKey = context.getString(R.string.stringUpcomingChangeLogSummaryPhone)
        }
        return versionCodeKey
    }

    /*In-App Purchase*/
    fun cloudBackupSubscribed(): Boolean {

        return readPreference(".SubscribedItem", "cloud.backup", false)
    }

    fun alreadyDonated(): Boolean {

        return readPreference(".SubscribedItem", "donation.subscription", false)
    }
}