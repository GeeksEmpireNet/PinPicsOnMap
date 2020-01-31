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

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.orientation.compasshd.Maps.WatchMapsView
import com.orientation.compasshd.Util.CompassUtil
import com.orientation.compasshd.Util.Coordinates
import com.orientation.compasshd.Util.Functions.FunctionsClass
import java.io.FileNotFoundException

class FloatingCompass : Service() {

    lateinit var functionsClass: FunctionsClass

    lateinit var windowManager: WindowManager
    lateinit var layoutParams: WindowManager.LayoutParams
    lateinit var viewGroup: ViewGroup

    lateinit var coordinates: Coordinates
    lateinit var compassUtil: CompassUtil

    lateinit var imageViewCompass: ImageView
    lateinit var move: ImageView
    lateinit var close: ImageView

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val path = Uri.parse(TimeCheck.imagePath)
        var drawableCompass: Drawable? = null
        try {
            drawableCompass = Drawable.createFromStream(contentResolver.openInputStream(path), path.toString())
        } catch (e: FileNotFoundException) {
            if (intent.getStringExtra("time") == "day") {
                (viewGroup.findViewById<View>(R.id.backgroungCompass) as ImageView).setImageDrawable(resources.getDrawable(R.drawable.daymod))
                drawableCompass = resources.getDrawable(R.drawable.daymod_arrow)

                val moveDrawable = resources.getDrawable(R.drawable.ic_move)
                val closeDrawable = resources.getDrawable(android.R.drawable.ic_menu_close_clear_cancel)
                moveDrawable.setTint(getColor(R.color.red))
                closeDrawable.setTint(getColor(R.color.red))
                move.setImageDrawable(moveDrawable)
                close.setImageDrawable(closeDrawable)
            } else if (intent.getStringExtra("time") == "night") {
                (viewGroup.findViewById<View>(R.id.backgroungCompass) as ImageView).setImageDrawable(resources.getDrawable(R.drawable.nightmod))
                drawableCompass = resources.getDrawable(R.drawable.nightmod_arrow)

                val moveDrawable = resources.getDrawable(R.drawable.ic_move)
                val closeDrawable = resources.getDrawable(android.R.drawable.ic_menu_close_clear_cancel)
                moveDrawable.setTint(getColor(R.color.light))
                closeDrawable.setTint(getColor(R.color.light))
                move.setImageDrawable(moveDrawable)
                close.setImageDrawable(closeDrawable)
            }
        }

        imageViewCompass.setImageDrawable(drawableCompass)
        imageViewCompass.setOnLongClickListener {
            val map = Intent(applicationContext, WatchMapsView::class.java)
            map.putExtra("time", intent.getStringExtra("time"))
            map.putExtra("LocationX", coordinates.getLatitude())
            map.putExtra("LocationY", coordinates.getLongitude())
            map.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(map)
            true
        }

        val displayMetrics = resources.displayMetrics
        val dpHeight = displayMetrics.heightPixels.toFloat()
        val dpWidth = displayMetrics.widthPixels.toFloat()

        move.setOnClickListener { }
        move.setOnTouchListener(object : View.OnTouchListener {
            private val paramsF = layoutParams
            private var initialX: Int = 0
            private var initialY: Int = 0
            private var initialTouchX: Float = 0.toFloat()
            private var initialTouchY: Float = 0.toFloat()

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = paramsF.x
                        initialY = paramsF.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                    }
                    MotionEvent.ACTION_UP -> {
                    }
                    MotionEvent.ACTION_MOVE -> {
                        paramsF.x = initialX + (event.rawX - initialTouchX).toInt()
                        paramsF.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(viewGroup, paramsF)

                        if (getSharedPreferences(".Config", Context.MODE_PRIVATE).getBoolean("automini", true) == true) {
                            if (event.rawX > dpWidth - layoutParams.width / 2) {
                                move.setOnTouchListener(null)
                                try {
                                    Handler().postDelayed({
                                        val m = Intent(applicationContext, FloatingCompassMini::class.java)
                                        m.putExtra("time", intent.getStringExtra("time"))
                                        m.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startService(m)
                                    }, 5)

                                    if (viewGroup == null) {
                                        return true
                                    }
                                    if (viewGroup.isShown) {
                                        windowManager.removeViewImmediate(viewGroup)
                                        stopSelf()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }
                        }
                    }
                }
                return false
            }
        })

        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()
        firebaseRemoteConfig.setConfigSettings(configSettings)
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_default)
        var cacheExpiration = (13 * 60).toLong()
        if (firebaseRemoteConfig.info.configSettings.isDeveloperModeEnabled) {
            cacheExpiration = 0
        }
        firebaseRemoteConfig.fetch(cacheExpiration)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseRemoteConfig.activateFetched()
                        if (firebaseRemoteConfig.getLong(functionsClass.versionCodeRemoteConfigKey()) > functionsClass.appVersionCode(packageName)) {
                            functionsClass.notificationCreator(
                                    getString(R.string.updateAvailable),
                                    firebaseRemoteConfig.getString(functionsClass.upcomingChangeLogRemoteConfigKey()),
                                    firebaseRemoteConfig.getLong(functionsClass.versionCodeRemoteConfigKey()).toInt()
                            )
                            Toast.makeText(applicationContext, getString(R.string.updateAvailable), Toast.LENGTH_LONG).show()
                        } else {
                        }
                    } else {
                    }
                }

        return Service.START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        functionsClass = FunctionsClass(applicationContext)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(applicationContext, BindService::class.java))
        } else {
            startService(Intent(applicationContext, BindService::class.java))
        }
        coordinates = Coordinates(applicationContext)
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        val HW = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TimeCheck.imageSize.toFloat(), this.resources.displayMetrics).toInt()
        if (android.os.Build.VERSION.SDK_INT > 25) {
            layoutParams = WindowManager.LayoutParams(
                    HW,
                    HW,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT)
        } else {
            layoutParams = WindowManager.LayoutParams(
                    HW,
                    HW,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT)
        }

        layoutParams.gravity = Gravity.TOP or Gravity.LEFT
        layoutParams.x = 50
        layoutParams.y = 100
        layoutParams.windowAnimations = android.R.style.Animation_Dialog

        val layoutInflater = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewGroup = layoutInflater.inflate(R.layout.floating_compass, null, false) as ViewGroup
        windowManager.addView(viewGroup, layoutParams)

        imageViewCompass = viewGroup.findViewById<View>(R.id.imageViewCompass) as ImageView
        move = viewGroup.findViewById<View>(R.id.move) as ImageView
        close = viewGroup.findViewById<View>(R.id.close) as ImageView

        close.setOnClickListener { stopSelf() }

        compassUtil = CompassUtil(applicationContext)
        compassUtil.compassView = imageViewCompass
        compassUtil.start()

        TimeCheck.run = true
    }

    override fun onDestroy() {
        compassUtil.stop()
        if (viewGroup.isShown) {
            TimeCheck.run = false
            stopService(Intent(applicationContext, BindService::class.java))
            windowManager.removeView(viewGroup)
        }
        super.onDestroy()
    }
}
