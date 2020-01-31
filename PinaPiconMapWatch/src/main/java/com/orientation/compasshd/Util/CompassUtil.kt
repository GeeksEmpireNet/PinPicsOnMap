/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.ImageView
import com.orientation.compasshd.BindService

class CompassUtil(context: Context) : SensorEventListener {

    lateinit var sensorManager: SensorManager
    lateinit var gsensor: Sensor
    lateinit var msensor: Sensor
    lateinit var osensor: Sensor

    var compassView: ImageView? = null

    private val mGravity = FloatArray(3)
    private val mGeomagnetic = FloatArray(3)
    private var azimuth = 0f
    private var currectAzimuth = 0f

    init {
        try {
            sensorManager = context
                    .getSystemService(Context.SENSOR_SERVICE) as SensorManager
            gsensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            msensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            osensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        } catch (e: Exception) {
            e.printStackTrace()
            context.stopService(Intent(context, BindService::class.java))
        }
    }

    fun start() {
        try {
            sensorManager.registerListener(this, gsensor, SensorManager.SENSOR_DELAY_FASTEST)
            sensorManager.registerListener(this, msensor, SensorManager.SENSOR_DELAY_FASTEST)
            sensorManager.registerListener(this, osensor, SensorManager.SENSOR_DELAY_FASTEST)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stop() {
        try {
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun adjustArrow() {
        if (compassView == null) {
            return
        }

        val an = RotateAnimation(-currectAzimuth, -azimuth,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f)
        currectAzimuth = azimuth

        an.duration = 333
        an.repeatCount = 0
        an.fillAfter = true

        compassView!!.startAnimation(an)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val alpha = 0.97f

        synchronized(this) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                mGravity[0] = alpha * mGravity[0] + (1 - alpha) * event.values[0]
                mGravity[1] = alpha * mGravity[1] + (1 - alpha) * event.values[1]
                mGravity[2] = alpha * mGravity[2] + (1 - alpha) * event.values[2]
            }
            if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                mGeomagnetic[0] = alpha * mGeomagnetic[0] + (1 - alpha) * event.values[0]
                mGeomagnetic[1] = alpha * mGeomagnetic[1] + (1 - alpha) * event.values[1]
                mGeomagnetic[2] = alpha * mGeomagnetic[2] + (1 - alpha) * event.values[2]
            }
            if (event.sensor.type == Sensor.TYPE_ORIENTATION) {
                XYZ[1] = Math.round(event.values[0]).toString()
                XYZ[2] = Math.round(event.values[1]).toString()
                XYZ[3] = Math.round(event.values[2]).toString()
            }

            val R = FloatArray(9)
            val I = FloatArray(9)
            val success = SensorManager.getRotationMatrix(R, I, mGravity, mGeomagnetic)
            if (success) {
                val orientation = FloatArray(3)
                SensorManager.getOrientation(R, orientation)

                azimuth = Math.toDegrees(orientation[0].toDouble()).toFloat() // orientation
                azimuth = (azimuth + 360) % 360

                adjustArrow()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}

    companion object {

        var XYZ = arrayOf("More Apps", "X", "Y", "Z", "Restart")
    }
}