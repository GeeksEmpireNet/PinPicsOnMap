/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.LocationData


import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset

class WeatherJSON(url: String) {
    var weather = "weather"
        internal set
    var temperature = "temperature"
        internal set
    var humidity = "humidity"
        internal set
    var weatherIconUrl = "icon"
        internal set

    private var urlString: String? = null
    @Volatile
    var parsingComplete = false

    init {
        this.urlString = url
    }

    private fun readAndParseJSON(data: String) {
        try {
            parsingComplete = false

            val jsonObject = JSONObject(data)
            /**** Weather ::: {
             * "coord":{"lon":-0.13,"lat":51.51},
             * "weather":[{"id":701,"main":"Mist","description":"mist","icon":"50n"}],
             * "base":"stations",
             * "main":{"temp":287.4,"pressure":1021,"humidity":87,"temp_min":285.15,"temp_max":289.26},
             * "visibility":8000,
             * "wind":{"speed":2.1,"deg":50},
             * "clouds":{"all":11},
             * "dt":1561769115,
             * "sys":{"type":1,"id":1414,"message":0.0115,"country":"GB","sunrise":1561779962,"sunset":1561839692},
             * "timezone":3600,
             * "id":2643743,
             * "name":"London",
             * "cod":200
             * }
             * ****/

            // We create out JSONObject from the data
            /*val jObj = JSONObject(data)

            // We start extracting the info
            val loc = Location()

            val coordObj = getObject("coord", jObj)
            loc.setLatitude(getFloat("lat", coordObj))
            loc.setLongitude(getFloat("lon", coordObj))

            val sysObj = getObject("sys", jObj)
            loc.setCountry(getString("country", sysObj))
            loc.setSunrise(getInt("sunrise", sysObj))
            loc.setSunset(getInt("sunset", sysObj))
            loc.setCity(getString("name", jObj))
            weather.location = loc

            // We get weather info (This is an array)
            val jArr = jObj.getJSONArray("weather")

            // We use only the first value
            val JSONWeather = jArr.getJSONObject(0)
            weather.currentCondition.setWeatherId(getInt("id", JSONWeather))
            weather.currentCondition.setDescr(getString("description", JSONWeather))
            weather.currentCondition.setCondition(getString("main", JSONWeather))
            weather.currentCondition.setIcon(getString("icon", JSONWeather))

            val mainObj = getObject("main", jObj)
            weather.temperature.setTemp(getFloat("temp", mainObj))
            weather.currentCondition.setHumidity(getInt("humidity", mainObj))
            weather.currentCondition.setPressure(getInt("pressure", mainObj))
            weather.temperature.setMaxTemp(getFloat("temp_max", mainObj))
            weather.temperature.setMinTemp(getFloat("temp_min", mainObj))

            // Wind
            val wObj = getObject("wind", jObj)
            weather.wind.setSpeed(getFloat("speed", wObj))
            weather.wind.setDeg(getFloat("deg", wObj))

            // Clouds
            val cObj = getObject("clouds", jObj)
            weather.clouds.setPerc(getInt("all", cObj))*/

            weather = jsonObject.getJSONArray("weather").getJSONObject(0).getString("description").toUpperCase()
            temperature = jsonObject.getJSONObject("main").getString("temp")//temp - 273.15 = celsius
            humidity = jsonObject.getJSONObject("main").getString("humidity")
            weatherIconUrl = "https://openweathermap.org/img/w/" + jsonObject.getJSONArray("weather").getJSONObject(0).getString("icon") + ".png"

            parsingComplete = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    suspend fun fetchJsonFromServer() {
        withContext(Dispatchers.IO) {
            val url = URL(urlString)
            readAndParseJSON(url.readText(Charset.defaultCharset()))
        }
    }

    private fun convertStreamToString(`is`: InputStream): String {
        val s = java.util.Scanner(`is`).useDelimiter("\\A")
        return if (s.hasNext()) s.next() else ""
    }
}
