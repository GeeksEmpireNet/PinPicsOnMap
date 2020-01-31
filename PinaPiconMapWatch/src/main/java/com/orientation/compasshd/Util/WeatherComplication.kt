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

import android.app.PendingIntent
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.Icon
import android.os.AsyncTask
import android.support.wearable.complications.ComplicationData
import android.support.wearable.complications.ComplicationManager
import android.support.wearable.complications.ComplicationProviderService
import android.support.wearable.complications.ComplicationText
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.orientation.compasshd.BuildConfig
import com.orientation.compasshd.R
import com.orientation.compasshd.TimeCheck
import com.orientation.compasshd.Util.Functions.FunctionsClass
import kotlin.math.roundToInt


class WeatherComplication : ComplicationProviderService() {

    lateinit var functionsClass: FunctionsClass

    internal var weatherIconBitmap: Bitmap? = null

    internal var Latitude: Double = 0.toDouble()
    internal var Longitude: Double = 0.toDouble()

    internal var country = "UK"
    internal var city = "London"

    internal lateinit var weatherJSON: WeatherJSON
    internal var temperature: String = "Temperature"
    internal var weather: String = "Weather"
    internal var weatherIcon: String = "Icon"
    internal var humidity: Int = 0

    companion object {
        var doExecute: Boolean = false

        var complicationManagerInit: ComplicationManager? = null
    }

    override fun onComplicationActivated(complicationId: Int, dataType: Int, complicationManager: ComplicationManager?) {
        if (BuildConfig.DEBUG) {
            println("onComplicationActivated $dataType")
        }
        complicationManagerInit = complicationManager

        functionsClass = FunctionsClass(applicationContext)
        functionsClass.savePreference("ComplicationProviderService", "ComplicationedId", complicationId)

        val weatherConditionInfo: WeatherConditionInfo = WeatherConditionInfo()
        weatherConditionInfo.execute()
    }

    override fun onComplicationUpdate(complicationId: Int, type: Int, complicationManager: ComplicationManager) {
        complicationManagerInit = complicationManager

        if (type == ComplicationData.TYPE_LARGE_IMAGE || type == ComplicationData.TYPE_ICON || type == ComplicationData.TYPE_RANGED_VALUE) {
            functionsClass = FunctionsClass(applicationContext)
            functionsClass.savePreference("ComplicationProviderService", "ComplicationedId", complicationId)

            if (doExecute) {
                val weatherConditionInfo: WeatherConditionInfo = WeatherConditionInfo()
                weatherConditionInfo.execute()
            }

            doExecute = true
        }
    }

    override fun onComplicationDeactivated(complicationId: Int) {
        println("onComplicationDeactivated")
    }

    private inner class WeatherConditionInfo : AsyncTask<Void, Void, String>() {
        override fun onPreExecute() {

        }

        override fun doInBackground(vararg params: Void): String {
            try {
                /*val geocoder = Geocoder(applicationContext, Locale.getDefault())
                var addresses: List<Address>? = null
                addresses = geocoder.getFromLocation(latitude, longitude, 1)

                city = addresses[0].locality
                country = functionsClass.getCountryIso()*/

                val jsonWeatherLink = ("https://api.openweathermap.org/data/2.5/weather?q=" + city + "," + country + "&APPID=" + getString(R.string.openMapWeather))
//                val jsonWeatherLink = ("https://api.openweathermap.org/data/2.5/weather?q=" + "Mashhad" + "," + "UK" + "&APPID=" + getString(R.string.openMapWeather))
                weatherJSON = WeatherJSON(jsonWeatherLink)
                weatherJSON.fetchJSON()

                temperature = ((weatherJSON.temperature.toDouble() - 273.15).roundToInt()).toString()
                humidity = weatherJSON.humidity
                weather = weatherJSON.weather
                weatherIcon = weatherJSON.weatherIconUrl

                println(temperature)
                println(humidity)
                println(weather)
                println(weatherIcon)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return "$country -  $city"
        }

        override fun onPostExecute(result: String) {
            println(result)
            try {
                doExecute = false

                Glide.with(applicationContext)
                        .load(weatherIcon)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                    glideException: GlideException?,
                                    any: Any?,
                                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                                    boolean: Boolean
                            ): Boolean {

                                return false
                            }

                            override fun onResourceReady(
                                    drawable: Drawable?,
                                    any: Any?,
                                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                                    dataSource: DataSource?,
                                    boolean: Boolean
                            ): Boolean {
                                weatherIconBitmap = functionsClass.drawableToBitmap(drawable!!)!!

//                                val requester = ProviderUpdateRequester(applicationContext, ComponentName(applicationContext, WeatherComplication::class.java))
//                                requester.requestUpdate(functionsClass.readPreference("ComplicationProviderService", "ComplicationedId", 0))


                                val recoveryIntent = Intent(applicationContext, TimeCheck::class.java)
                                recoveryIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                                val complicationData = ComplicationData.Builder(ComplicationData.TYPE_RANGED_VALUE)
                                        .setIcon(Icon.createWithBitmap(weatherIconBitmap))
                                        .setShortText(ComplicationText.plainText("${temperature} °ᶜ"))
                                        .setMinValue(0f)
                                        .setMaxValue(100f)
                                        .setValue((humidity).toFloat())
                                        .setTapAction(PendingIntent
                                                .getService(applicationContext, 666, recoveryIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                                        .build()
                                complicationManagerInit!!.updateComplicationData(functionsClass.readPreference("ComplicationProviderService", "ComplicationedId", 0), complicationData)

                                return false
                            }
                        })
                        .submit()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }
}