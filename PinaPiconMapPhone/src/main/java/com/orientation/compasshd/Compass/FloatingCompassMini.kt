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

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import com.orientation.compasshd.BindService
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.Functions.FunctionsClass
import com.orientation.compasshd.Util.Functions.FunctionsClassPreferences
import com.orientation.compasshd.Util.LocationData.CompassUtil
import com.orientation.compasshd.Util.LocationData.Coordinates
import java.io.FileNotFoundException

class FloatingCompassMini : Service() {

    lateinit var functionClass: FunctionsClass

    lateinit var windowManager: WindowManager
    lateinit var layoutParams: WindowManager.LayoutParams
    lateinit var viewGroup: ViewGroup

    private var imageViewCompass: ImageView? = null

    lateinit var compassUtil: CompassUtil

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val HW = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 52f, this.resources.displayMetrics).toInt()
        if (Build.VERSION.SDK_INT > 25) {
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
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                    PixelFormat.TRANSLUCENT)
        }

        val displayMetrics = resources.displayMetrics
        val dpHeight = displayMetrics.heightPixels.toFloat()
        val dpWidth = displayMetrics.widthPixels.toFloat()

        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = (dpWidth - HW / 1.5).toInt()
        layoutParams.y = intent.getIntExtra("yPosition", 133)
        layoutParams.windowAnimations = android.R.style.Animation_Dialog

        val layoutInflater = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewGroup = layoutInflater.inflate(R.layout.floating_compass_mini, null, false) as ViewGroup
        windowManager.addView(viewGroup, layoutParams)

        imageViewCompass = viewGroup.findViewById<View>(R.id.imageViewCompass) as ImageView

        compassUtil = CompassUtil(applicationContext)
        compassUtil.compassView = imageViewCompass
        if (FunctionsClassPreferences(applicationContext).CompassConfiguration().readCompassConfig(FunctionsClassPreferences.COMPASS_MINIMIZE_MODE, true) as Boolean) {
            compassUtil.start()
        }

        val path = Uri.parse(TimeCheckCompass.imagePath)
        var drawableCompass: Drawable? = null
        try {
            if (intent.getStringExtra("time") == "day") {
                drawableCompass = Drawable.createFromStream(contentResolver.openInputStream(path), path.toString())
                imageViewCompass!!.setImageDrawable(null)
            } else if (intent.getStringExtra("time") == "night") {
                drawableCompass = Drawable.createFromStream(contentResolver.openInputStream(path), path.toString())
                val tintDrawable = Drawable.createFromStream(contentResolver.openInputStream(path), path.toString())
                tintDrawable.setTint(functionClass.setColorAlpha(getColor(R.color.red), 175f))
                imageViewCompass!!.setImageDrawable(tintDrawable)
            }
            imageViewCompass!!.background = drawableCompass
        } catch (e: FileNotFoundException) {
            if (intent.getStringExtra("time") == "day") {
                drawableCompass = getDrawable(R.drawable.daymod)
            } else if (intent.getStringExtra("time") == "night") {
                drawableCompass = getDrawable(R.drawable.nightmod)
            }
            imageViewCompass!!.setImageDrawable(drawableCompass)
        }

        imageViewCompass!!.setOnClickListener { }
        imageViewCompass!!.setOnLongClickListener {
            stopSelf()
            startService(Intent(applicationContext, FloatingCompass::class.java).putExtra("time", intent.getStringExtra("time")))
            false
        }
        imageViewCompass!!.setOnTouchListener(object : View.OnTouchListener {
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
                    }
                }
                return false
            }
        })

        Toast.makeText(applicationContext, getString(R.string.longclick), Toast.LENGTH_LONG).show()
        return Service.START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        functionClass = FunctionsClass(applicationContext)
        functionClass.addAppShortcuts()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(applicationContext, BindService::class.java))
        } else {
            startService(Intent(applicationContext, BindService::class.java))
        }
    }

    override fun onDestroy() {
        compassUtil.stop()
        Coordinates(applicationContext).stopUsingGPS()
        if (viewGroup.isShown) {
            stopService(Intent(applicationContext, BindService::class.java))
            windowManager.removeView(viewGroup)
        }
        super.onDestroy()
    }
}
