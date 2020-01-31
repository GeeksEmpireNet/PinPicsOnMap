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
import com.orientation.compasshd.Util.CompassUtil
import java.io.FileNotFoundException

class FloatingCompassMini : Service() {

    lateinit var windowManager: WindowManager
    lateinit var params: WindowManager.LayoutParams
    lateinit var vG: ViewGroup

    lateinit var imageViewCompass: ImageView

    lateinit var compassUtil: CompassUtil

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
                drawableCompass = getDrawable(R.drawable.daymod)
            } else if (intent.getStringExtra("time") == "night") {
                drawableCompass = getDrawable(R.drawable.nightmod)
            }
        }

        imageViewCompass.setImageDrawable(drawableCompass)

        imageViewCompass.setOnClickListener {
            stopSelf()
            startService(Intent(applicationContext, FloatingCompass::class.java).putExtra("time", intent.getStringExtra("time")))
        }
        imageViewCompass.setOnTouchListener(object : View.OnTouchListener {
            private val paramsF = params
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
                        windowManager.updateViewLayout(vG, paramsF)
                    }
                }
                return false
            }
        })

        return Service.START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(applicationContext, BindService::class.java))
        } else {
            startService(Intent(applicationContext, BindService::class.java))
        }
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val HW = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48f, this.resources.displayMetrics).toInt()
        if (android.os.Build.VERSION.SDK_INT > 25) {
            params = WindowManager.LayoutParams(
                    HW,
                    HW,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT)
        } else {
            params = WindowManager.LayoutParams(
                    HW,
                    HW,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT)
        }

        params.gravity = Gravity.TOP or Gravity.LEFT
        params.x = 50
        params.y = 100
        params.windowAnimations = android.R.style.Animation_Dialog

        val layoutInflater = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        vG = layoutInflater.inflate(R.layout.floating_compass_mini, null, false) as ViewGroup
        windowManager.addView(vG, params)

        imageViewCompass = vG.findViewById<View>(R.id.imageViewCompass) as ImageView

        compassUtil = CompassUtil(applicationContext)
        compassUtil.compassView = imageViewCompass
        compassUtil.start()
    }

    override fun onDestroy() {
        compassUtil.stop()
        if (vG.isShown) {
            stopService(Intent(applicationContext, BindService::class.java))
            windowManager.removeView(vG)
        }
        super.onDestroy()
    }
}
