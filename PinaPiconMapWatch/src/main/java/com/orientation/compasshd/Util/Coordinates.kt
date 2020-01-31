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

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import com.google.android.gms.maps.model.LatLng
import com.orientation.compasshd.Util.Functions.FunctionsClassDebug
import com.orientation.compasshd.Util.Functions.FunctionsClassPreferences

class Coordinates(context: Context) : LocationListener {

    private var coordinatesContext: Context = context

    private var functionsClassPreferences: FunctionsClassPreferences = FunctionsClassPreferences(coordinatesContext)

    private var gpsEnabled = false
    private var networkEnabled = false
    private var canGetLocation = false

    private var lastLocation: Location? = null
    internal var latitude: Double = 0.toDouble()
    internal var longitude: Double = 0.toDouble()

    private var locationManager: LocationManager? = null

    val locationLatitudeLongitude: LatLng
        get() = LatLng(getLatitude(), getLongitude())

    init {
        //ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION
        if ((context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                && (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        ) {
            getLocation()
        } else {

        }
    }

    override fun onLocationChanged(location: Location) {
        FunctionsClassDebug.PrintDebug("*** Location Changed == ${location} ***")

        latitude = location.latitude
        longitude = location.longitude

        functionsClassPreferences.savePreference(".LastKnownLocation", "LocationProvider", location.provider)
        functionsClassPreferences.savePreference(".LastKnownLocation", "LastLatitude", latitude.toString())
        functionsClassPreferences.savePreference(".LastKnownLocation", "LastLongitude", longitude.toString())

        coordinatesContext.sendBroadcast(Intent("RELOAD_WEATHER"))
    }

    override fun onProviderDisabled(provider: String) {

    }

    override fun onProviderEnabled(provider: String) {

    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    private fun getLocation(): Location? {
        try {
            locationManager = coordinatesContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            gpsEnabled = locationManager!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
            networkEnabled = locationManager!!.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            if (!gpsEnabled && !networkEnabled) {
                FunctionsClassDebug.PrintDebug("*** Network & Gps Are Not Enabled ***")
            } else {
                this.canGetLocation = true

                /*val criteria = Criteria()
                criteria.accuracy = Criteria.ACCURACY_COARSE*/

                if (gpsEnabled) {
                    if (lastLocation == null) {
                        locationManager!!.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                                this@Coordinates
                        )

                        if (locationManager != null) {
                            lastLocation = locationManager!!.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                            if (lastLocation != null) {
                                latitude = lastLocation!!.latitude
                                longitude = lastLocation!!.longitude
                            }
                        }
                    }
                }
                if (networkEnabled) {
                    locationManager!!.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES.toFloat(),
                            this@Coordinates
                    )

                    if (locationManager != null) {
                        lastLocation = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                        if (lastLocation != null) {
                            latitude = lastLocation!!.latitude
                            longitude = lastLocation!!.longitude
                        }
                    }
                }
            }

        } catch (e: SecurityException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return lastLocation
    }

    fun stopUsingGPS() {
        if (locationManager != null) {
            locationManager!!.removeUpdates(this@Coordinates)
        }
    }

    fun getLatitude(): Double {
        if (lastLocation != null) {
            latitude = lastLocation!!.latitude
        }
        return latitude
    }

    fun getLongitude(): Double {
        if (lastLocation != null) {
            longitude = lastLocation!!.longitude
        }
        return longitude
    }

    fun canGetLocation(): Boolean {
        var canGetLocation: Boolean = false
        if ((coordinatesContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(
                        LocationManager.GPS_PROVIDER
                )
                || (coordinatesContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(
                        LocationManager.NETWORK_PROVIDER
                )
        ) {
            canGetLocation = true
        }
        return canGetLocation
    }

    companion object {
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Long = (1).toLong()//meters
        private const val MIN_TIME_BW_UPDATES = (1000 * 1 * 60)/*1 min*/ * (13).toLong()//millis
    }
}