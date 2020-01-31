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

import android.app.Activity
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.text.Html
import android.text.Spannable
import com.google.android.gms.maps.model.LatLng
import java.util.*

class FunctionsClassMaps {

    lateinit var activity: Activity
    lateinit var context: Context

    constructor(activity: Activity, context: Context) {
        this.activity = activity
        this.context = context
    }

    constructor(context: Context) {
        this.context = context
    }

    init {

    }

    fun loadLocationInfo(latLng: LatLng): ArrayList<Address>? {
        var locationAddresses: ArrayList<Address>? = null
        PublicVariable.LOCATION_INFO_DETAIL = "Unknown (${latLng.latitude}.${latLng.longitude}"
        try {
            val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
            locationAddresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) as ArrayList<Address>

            if (locationAddresses.size > 0) {
                //locationAddresses[0].getAddressLine(0)

                try {
                    val country = locationAddresses[0].countryName
                    PublicVariable.LOCATION_COUNTRY_NAME = country
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                try {
                    val state = locationAddresses[0].adminArea
                    PublicVariable.LOCATION_STATE_PROVINCE = state
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    val city = locationAddresses[0].locality
                    PublicVariable.LOCATION_CITY_NAME = city
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                try {
                    val knownName = locationAddresses[0].featureName
                    if (knownName != null) {
                        PublicVariable.LOCATION_KNOWN_NAME = knownName
                        PublicVariable.LOCATION_INFO_DETAIL =
                                "${PublicVariable.LOCATION_COUNTRY_NAME}, ${PublicVariable.LOCATION_STATE_PROVINCE}, ${PublicVariable.LOCATION_CITY_NAME}, $knownName"
                        PublicVariable.LOCATION_INFO_DETAIL_HTML = Html.fromHtml("<big><b>${PublicVariable.LOCATION_COUNTRY_NAME}</b></big> | <big>${PublicVariable.LOCATION_CITY_NAME}</big><br/>" +
                                "${knownName}") as Spannable?
                    } else {
                        PublicVariable.LOCATION_INFO_DETAIL =
                                "${PublicVariable.LOCATION_COUNTRY_NAME}, ${PublicVariable.LOCATION_STATE_PROVINCE}, ${PublicVariable.LOCATION_CITY_NAME}"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            } else {
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return locationAddresses
    }

    fun CountryName(latLng: LatLng): String {
        var countryName: String = "Unknown"
        try {
            val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
            val addresses: ArrayList<Address> = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) as ArrayList<Address>

            if (addresses.size > 0) {
                countryName = addresses[0].countryName
            }
        } catch (e: Exception) {
            e.printStackTrace()
            countryName = "Unknown"

        }
        return countryName
    }

    fun StateProvince(latLng: LatLng): String {
        var stateProvince: String = "Unknown"
        try {
            val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
            val addresses: ArrayList<Address> = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) as ArrayList<Address>

            if (addresses.size > 0) {
                stateProvince = addresses[0].adminArea
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stateProvince = "Unknown"

        }
        return stateProvince
    }

    fun CityName(latLng: LatLng): String {
        var cityName: String = "Unknown"
        try {
            val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
            val addresses: ArrayList<Address> = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) as ArrayList<Address>

            if (addresses.size > 0) {
                cityName = addresses[0].locality
            }
        } catch (e: Exception) {
            e.printStackTrace()
            cityName = "Unknown"

        }
        return cityName
    }

    fun KnownName(latLng: LatLng): String {
        var knownName: String = "Unknown"
        try {
            val geocoder: Geocoder = Geocoder(context, Locale.getDefault())
            val addresses: ArrayList<Address> = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1) as ArrayList<Address>

            if (addresses.size > 0) {
                if (addresses[0].featureName != null) {
                    knownName = addresses[0].featureName
                } else {
                    knownName = "Unknown"
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            knownName = "Unknown"
        }

        return knownName
    }
}