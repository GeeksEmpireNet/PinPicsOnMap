/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/30/20 3:07 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.Functions

import android.app.*
import android.content.Context
import android.graphics.*
import android.graphics.Shader.TileMode
import android.graphics.drawable.*
import android.os.Build
import android.os.Vibrator
import android.text.Html
import android.view.Gravity
import android.view.LayoutInflater
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.palette.graphics.Palette
import com.orientation.compasshd.PinPicsOnMapApplication
import com.orientation.compasshd.R
import net.geeksempire.chat.vicinity.Util.FunctionsClass.FunctionsClassConverter
import javax.inject.Inject
import javax.inject.Singleton

interface InterfaceUI {

}
@Singleton
class FunctionsClassUI @Inject constructor (var context: Context) : InterfaceUI {

    @Inject lateinit var functionsClassConverter: FunctionsClassConverter

    init {
        (context as PinPicsOnMapApplication)
                .dependencyGraph.inject(this@FunctionsClassUI)
    }

    fun extractDominantColor(drawable: Drawable): Int {

        var DominanctColor = context.getColor(R.color.default_color)
        var bitmap: Bitmap?
        if (Build.VERSION.SDK_INT >= 26) {
            if (drawable is VectorDrawable) {
                bitmap = functionsClassConverter.drawableToBitmap(drawable)
            } else if (drawable is AdaptiveIconDrawable) {
                try {
                    bitmap = (drawable.background as BitmapDrawable).bitmap
                } catch (e: Exception) {
                    try {
                        bitmap = (drawable.foreground as BitmapDrawable).bitmap
                    } catch (e1: Exception) {
                        bitmap = functionsClassConverter.drawableToBitmap(drawable)
                    }

                }

            } else {
                bitmap = functionsClassConverter.drawableToBitmap(drawable)
            }
        } else {
            bitmap = functionsClassConverter.drawableToBitmap(drawable)
        }
        var currentColor: Palette
        try {
            if (bitmap != null && !bitmap.isRecycled) {
                currentColor = Palette.from(bitmap).generate()

                val defaultColor = context.getColor(R.color.default_color)
                DominanctColor = currentColor.getDominantColor(defaultColor)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                if (bitmap != null && !bitmap.isRecycled) {
                    currentColor = Palette.from(bitmap).generate()

                    val defaultColor = context.getColor(R.color.default_color)
                    DominanctColor = currentColor.getMutedColor(defaultColor)
                }
            } catch (e1: Exception) {
                e1.printStackTrace()
            }

        }

        return DominanctColor
    }

    fun extractVibrantColor(drawable: Drawable): Int {

        var VibrantColor = if (FunctionsClass(context).getTime() == "day") {
            context.getColor(R.color.default_color_darker)
        } else {
            context.getColor(R.color.default_color_light)
        }
        var bitmap: Bitmap?
        if (Build.VERSION.SDK_INT >= 26) {
            if (drawable is VectorDrawable) {
                bitmap = functionsClassConverter.drawableToBitmap(drawable)
            } else if (drawable is AdaptiveIconDrawable) {
                try {
                    bitmap = (drawable.background as BitmapDrawable).bitmap
                } catch (e: Exception) {
                    try {
                        bitmap = (drawable.foreground as BitmapDrawable).bitmap
                    } catch (e1: Exception) {
                        bitmap = functionsClassConverter.drawableToBitmap(drawable)
                    }
                }

            } else {
                bitmap = functionsClassConverter.drawableToBitmap(drawable)
            }
        } else {
            bitmap = functionsClassConverter.drawableToBitmap(drawable)
        }
        var currentColor: Palette
        try {
            if (bitmap != null && !bitmap.isRecycled) {
                currentColor = Palette.from(bitmap).generate()

                val defaultColor = context.getColor(R.color.default_color)
                VibrantColor = currentColor.getVibrantColor(defaultColor)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                if (bitmap != null && !bitmap.isRecycled) {
                    currentColor = Palette.from(bitmap).generate()

                    val defaultColor = context.getColor(R.color.default_color)
                    VibrantColor = currentColor.getMutedColor(defaultColor)
                }
            } catch (e1: Exception) {
                e1.printStackTrace()
            }

        }

        return VibrantColor
    }

    fun extractDarkMutedColor(drawable: Drawable): Int {

        var DarkMutedColor = context.getColor(R.color.default_color)
        var bitmap: Bitmap?
        if (Build.VERSION.SDK_INT >= 26) {
            if (drawable is VectorDrawable) {
                bitmap = functionsClassConverter.drawableToBitmap(drawable)
            } else if (drawable is AdaptiveIconDrawable) {
                try {
                    bitmap = (drawable.background as BitmapDrawable).bitmap
                } catch (e: Exception) {
                    try {
                        bitmap = (drawable.foreground as BitmapDrawable).bitmap
                    } catch (e1: Exception) {
                        bitmap = functionsClassConverter.drawableToBitmap(drawable)
                    }

                }

            } else {
                bitmap = functionsClassConverter.drawableToBitmap(drawable)
            }
        } else {
            bitmap = functionsClassConverter.drawableToBitmap(drawable)
        }
        var currentColor: Palette
        try {
            if (bitmap != null && !bitmap.isRecycled) {
                currentColor = Palette.from(bitmap).generate()

                val defaultColor = context.getColor(R.color.default_color)
                DarkMutedColor = currentColor.getDarkMutedColor(defaultColor)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            try {
                if (bitmap != null && !bitmap.isRecycled) {
                    currentColor = Palette.from(bitmap).generate()

                    val defaultColor = context.getColor(R.color.default_color)
                    DarkMutedColor = currentColor.getMutedColor(defaultColor)
                }
            } catch (e1: Exception) {
                e1.printStackTrace()
            }

        }

        return DarkMutedColor
    }

    fun brightenBitmap(bitmap: Bitmap): Bitmap {
        val canvas = Canvas(bitmap)
        val paint = Paint(Color.RED)
        val filter = LightingColorFilter(-0x1, 0x00222222) // lighten
        //ColorFilter filter = new LightingColorFilter(0xFF7F7F7F, 0x00000000)    // darken
        paint.colorFilter = filter
        canvas.drawBitmap(bitmap, Matrix(), paint)
        return bitmap
    }

    fun darkenBitmap(bitmap: Bitmap): Bitmap {
        val canvas = Canvas(bitmap)
        val paint = Paint(Color.RED)
        //ColorFilter filter = new LightingColorFilter(0xFFFFFFFF , 0x00222222) // lighten
        val filter = LightingColorFilter(-0x808081, 0x00000000)    // darken
        paint.colorFilter = filter
        canvas.drawBitmap(bitmap, Matrix(), paint)
        return bitmap
    }

    fun manipulateColor(color: Int, aFactor: Float): Int {
        val a = Color.alpha(color)
        val r = Math.round(Color.red(color) * aFactor)
        val g = Math.round(Color.green(color) * aFactor)
        val b = Math.round(Color.blue(color) * aFactor)
        return Color.argb(
                a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255)
        )
    }

    fun mixColors(color1: Int, color2: Int, ratio: Float /*0 -- 1*/): Int {
        /*ratio = 1 >> color1*/
        val inverseRation = 1f - ratio
        val r = Color.red(color1) * ratio + Color.red(color2) * inverseRation
        val g = Color.green(color1) * ratio + Color.green(color2) * inverseRation
        val b = Color.blue(color1) * ratio + Color.blue(color2) * inverseRation
        return Color.rgb(r.toInt(), g.toInt(), b.toInt())
    }

    fun setColorAlpha(color: Int, alphaValue: Float /*1 -- 255*/): Int {
        val alpha = Math.round(Color.alpha(color) * alphaValue)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        return Color.argb(alpha, red, green, blue)
    }

    fun getCircularBitmapWithWhiteBorder(
            bitmap: Bitmap?,
            borderWidth: Int,
            borderColor: Int
    ): Bitmap? {
        if (bitmap == null || bitmap.isRecycled) {
            return null
        }

        val width = bitmap.width + borderWidth
        val height = bitmap.height + borderWidth

        val canvasBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val shader = BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.shader = shader

        val canvas = Canvas(canvasBitmap)
        val radius = if (width > height) height.toFloat() / 2f else width.toFloat() / 2f
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius, paint)
        paint.shader = null
        paint.style = Paint.Style.STROKE
        paint.color = borderColor
        paint.strokeWidth = borderWidth.toFloat()
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), radius - borderWidth / 2, paint)
        return canvasBitmap
    }

    fun Toast(toastContent: String, gravity: Int) {
        val inflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val layout = inflater.inflate(R.layout.toast_view, null/*(ViewGroup) activity.findViewById(R.id.toastView)*/)

        var drawToast: LayerDrawable? = context.resources.getDrawable(R.drawable.toast_background) as LayerDrawable
        val backToast = drawToast!!.findDrawableByLayerId(R.id.backtemp) as GradientDrawable

        val textView = layout.findViewById(R.id.toastText) as TextView
        textView.text = toastContent
        backToast.setColor(context.resources.getColor(R.color.light_transparent))
        textView.background = drawToast
        textView.setTextColor(context.resources.getColor(R.color.dark))
        textView.setShadowLayer(0.02f, 2f, 2f, context.resources.getColor(R.color.dark_transparent_high))

        val toast = android.widget.Toast(context)
        toast.setGravity(Gravity.FILL_HORIZONTAL or gravity, 0, 0)
        toast.duration = android.widget.Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
    }

    fun notificationCreator(
            notificationChannelId: String,
            largeIcon: Bitmap, titleText: String, contentText: String,
            defaultColor: Int, titleColor: Int, contentColor: Int,
            notificationId: Int,
            notificationImportance: Int,
            pendingIntent: PendingIntent?
    ) {

        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(113)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationBuilder = Notification.Builder(context)
        notificationBuilder.setContentTitle(
                Html.fromHtml(
                        "<big><b><font color='" + titleColor + "'>" + titleText + "</font></b></big> | " + "<small>" + notificationChannelId + "</small>",
                        Html.FROM_HTML_MODE_LEGACY
                )
        )
        notificationBuilder.setContentText(
                Html.fromHtml(
                        "<font color='" + contentColor + "'>" + contentText + "</font>",
                        Html.FROM_HTML_MODE_LEGACY
                )
        )
        notificationBuilder.setTicker(context.resources.getString(R.string.app_name))
        notificationBuilder.setSmallIcon(R.drawable.ic_notification)
        notificationBuilder.setLargeIcon(getCircularBitmapWithWhiteBorder(largeIcon, 0, Color.TRANSPARENT))
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setColor(defaultColor)
        notificationBuilder.setVibrate(longArrayOf(0))
        notificationBuilder.setLights(defaultColor, 999, 333)
        notificationBuilder.setPriority(notificationImportance/*Notification.PRIORITY_MIN*/)
        if (pendingIntent != null) {
            notificationBuilder.setContentIntent(pendingIntent)

            val builderActionNotification = Notification.Action.Builder(
                    null,
                    notificationChannelId!!.toLowerCase(),
                    pendingIntent
            )
            notificationBuilder.addAction(builderActionNotification.build())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder.setChannelId(notificationChannelId)
            val notificationChannel = NotificationChannel(
                    notificationChannelId,
                    context.getString(R.string.app_name),
                    notificationImportance/*NotificationManager.IMPORTANCE_MIN*/
            )
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = defaultColor
            notificationChannel.enableVibration(true)
            notificationChannel.description = notificationChannelId
            notificationManager.createNotificationChannel(notificationChannel)
        }

        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    @Throws(IllegalArgumentException::class)
    fun hideKeyboard(context: Context, view: EditText) {
        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    @Throws(IllegalArgumentException::class)
    fun showKeyboard(context: Context, view: EditText) {
        val inputMethodManager = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.showSoftInput(view, 0)
    }
}