package com.orientation.compasshd.Maps.ViewModel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.Functions.FunctionsClass
import com.orientation.compasshd.Util.LocationData.WeatherJSON
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class PhoneMapsViewModel : ViewModel() {

    var countryName: String = "UK"
    var cityName: String = "London"

    lateinit var temperature: String
    lateinit var weatherIcon: String

    val weatherInformation: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun downloadWeatherInformation(context: Context) = viewModelScope.launch {
        val functionsClass = FunctionsClass(context)

        try {
            functionsClass.locationCityName()?.let {
                cityName = it

                countryName = functionsClass.getCountryIso()

                val jsonWeatherLink
                        = ("https://api.openweathermap.org/data/2.5/weather?q=" + cityName + "," + countryName + "&APPID=" + context.getString(R.string.openMapWeather))
                val weatherJSON = WeatherJSON(jsonWeatherLink)
                weatherJSON.fetchJsonFromServer()

                val weather = weatherJSON.weather
                val humidity = weatherJSON.humidity

                temperature = ((weatherJSON.temperature.toDouble() - 273.15).roundToInt()).toString()
                weatherIcon = weatherJSON.weatherIconUrl

                weatherInformation.postValue("<b><big>" + temperature + "</big></b>" + "<big>°ᶜ</big>" + " | " + weather + "<br/>"
                        + "" + "\uD83D\uDCA6" + " <small>" + humidity + "%</small>")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

        }
    }
}