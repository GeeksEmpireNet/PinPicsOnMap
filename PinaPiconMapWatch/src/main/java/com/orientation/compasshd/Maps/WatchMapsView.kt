/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 1:45 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Maps

import android.Manifest
import android.app.ActivityOptions
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.os.StrictMode
import android.support.wearable.activity.WearableActivityDelegate
import android.view.View
import android.widget.TextView
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.Coordinates
import com.orientation.compasshd.Util.FetchAddressIntentService
import com.orientation.compasshd.Util.Functions.FunctionsClass
import com.orientation.compasshd.Util.Functions.FunctionsClassDebug
import com.orientation.compasshd.Util.Functions.PublicVariable
import kotlinx.android.synthetic.main.maps_view.*
import java.util.*

class WatchMapsView : androidx.fragment.app.FragmentActivity(), OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowLongClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnInfoWindowCloseListener,
        LocationListener {

    lateinit var functionsClass: FunctionsClass

    lateinit var googleMap: GoogleMap
    lateinit var supportMapFragment: SupportMapFragment

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var coordinates: Coordinates

    lateinit var initMarker: Marker

    lateinit var time: String

    var geoDecodeAttempt: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps_view)

        val hrs = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hrs >= 18 || hrs < 6) {//Night
            time = "night"
        } else {
            time = "day"
        }

        functionsClass = FunctionsClass(this@WatchMapsView, applicationContext)

        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 666)

        coordinates = Coordinates(applicationContext)

        supportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.retainInstance = true

        val wearableActivityDelegate = WearableActivityDelegate(object : WearableActivityDelegate.AmbientCallback {
            override fun onExitAmbient() {

            }

            override fun onUpdateAmbient() {

            }

            override fun onEnterAmbient(ambientDetails: Bundle?) {

            }
        })
        wearableActivityDelegate.setAmbientEnabled()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@WatchMapsView)
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //Location Update Listener
            //https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest
            val locationRequest = LocationRequest.create()
            locationRequest.interval = 1000
            locationRequest.fastestInterval = 500
            locationRequest.numUpdates = 1
            locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult?) {
                    locationResult ?: return
                    FunctionsClassDebug.PrintDebug("*** LocationResult == ${locationResult} ***")

                    if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                            val resultReceiver: ResultReceiver = object : ResultReceiver(Handler()) {
                                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                                    when (resultCode) {
                                        FetchAddressIntentService.Constants.SUCCESS_RESULT -> {
                                            val addressOutput = resultData?.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY).toString()
                                            FunctionsClassDebug.PrintDebug("*** FetchAddressIntentService.Constants.SUCCESS_RESULT == ${addressOutput} ***")

                                            supportMapFragment.getMapAsync(this@WatchMapsView)
                                        }
                                        FetchAddressIntentService.Constants.FAILURE_RESULT -> {
                                            FunctionsClassDebug.PrintDebug("*** FetchAddressIntentService.Constants.FAILURE_RESULT == ${geoDecodeAttempt} ***")

                                            if (geoDecodeAttempt <= 7) {
                                                val intent = Intent(this@WatchMapsView, FetchAddressIntentService::class.java)
                                                intent.action = "LocationData"
                                                intent.putExtra(FetchAddressIntentService.Constants.RESULT_RECEIVER, this)
                                                intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
                                                startService(intent)
                                            } else {
                                                PublicVariable.LOCATION_INFO_DETAIL = functionsClass.getCountryIso() + " | " + "Unknown Area"
                                                PublicVariable.LOCATION_COUNTRY_NAME = functionsClass.getCountryIso()
                                                PublicVariable.LOCATION_CITY_NAME = functionsClass.getCountryIso()

                                                supportMapFragment.getMapAsync(this@WatchMapsView)
                                            }

                                            geoDecodeAttempt++
                                        }
                                    }
                                }
                            }

                            val intent = Intent(this@WatchMapsView, FetchAddressIntentService::class.java)
                            intent.action = "LocationData"
                            intent.putExtra(FetchAddressIntentService.Constants.RESULT_RECEIVER, resultReceiver)
                            intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
                            startService(intent)

                            Latitude = coordinates.getLatitude()
                            Longitude = coordinates.getLongitude()
                            LocationXY = LatLng(location!!.latitude, location.longitude)
                        }.addOnFailureListener {
                            FunctionsClassDebug.PrintDebug("*** ${it} ***")
                        }
                    } else {
                        FunctionsClassDebug.PrintDebug("*** ${checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)} ***")
                    }
                }
            }, null)
        } else {

        }

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        val intentFilter = IntentFilter()
        intentFilter.addAction("NEW_LOCATION_DATA")
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action.equals("NEW_LOCATION_DATA")) {
                    if (googleMap != null) {
                        googleMap.clear()
                    }
                    startActivity(Intent(applicationContext, WatchMapsView::class.java),
                            ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
                }
            }
        }
        registerReceiver(broadcastReceiver, intentFilter)
    }

    public override fun onStart() {
        super.onStart()
        currentLocation.setOnClickListener { view ->
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Location Update Listener
                //https://developers.google.com/android/reference/com/google/android/gms/location/LocationRequest
                val locationRequest = LocationRequest.create()
                locationRequest.interval = 1000
                locationRequest.fastestInterval = 500
                locationRequest.numUpdates = 1
                locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        locationResult ?: return
                        FunctionsClassDebug.PrintDebug("*** LocationResult == ${locationResult} ***")

                        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                                coordinates = Coordinates(applicationContext)

                                val resultReceiver: ResultReceiver = object : ResultReceiver(Handler()) {
                                    override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                                        when (resultCode) {
                                            FetchAddressIntentService.Constants.SUCCESS_RESULT -> {
                                                val addressOutput = resultData?.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY).toString()
                                                FunctionsClassDebug.PrintDebug("*** FetchAddressIntentService.Constants.SUCCESS_RESULT == ${addressOutput} ***")

                                                supportMapFragment.getMapAsync(this@WatchMapsView)
                                            }
                                            FetchAddressIntentService.Constants.FAILURE_RESULT -> {
                                                FunctionsClassDebug.PrintDebug("*** FetchAddressIntentService.Constants.FAILURE_RESULT == ${geoDecodeAttempt} ***")

                                                if (geoDecodeAttempt <= 7) {
                                                    val intent = Intent(this@WatchMapsView, FetchAddressIntentService::class.java)
                                                    intent.action = "LocationData"
                                                    intent.putExtra(FetchAddressIntentService.Constants.RESULT_RECEIVER, this)
                                                    intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
                                                    startService(intent)
                                                } else {
                                                    PublicVariable.LOCATION_INFO_DETAIL = functionsClass.getCountryIso() + " | " + "Unknown Area"
                                                    PublicVariable.LOCATION_COUNTRY_NAME = functionsClass.getCountryIso()
                                                    PublicVariable.LOCATION_CITY_NAME = functionsClass.getCountryIso()

                                                    supportMapFragment.getMapAsync(this@WatchMapsView)
                                                }

                                                geoDecodeAttempt++
                                            }
                                        }
                                    }
                                }

                                val intent = Intent(this@WatchMapsView, FetchAddressIntentService::class.java)
                                intent.action = "LocationData"
                                intent.putExtra(FetchAddressIntentService.Constants.RESULT_RECEIVER, resultReceiver)
                                intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
                                startService(intent)

                                Latitude = coordinates.getLatitude()
                                Longitude = coordinates.getLongitude()
                                LocationXY = LatLng(location!!.latitude, location.longitude)
                            }.addOnFailureListener {
                                FunctionsClassDebug.PrintDebug("*** ${it} ***")
                            }
                        } else {
                            FunctionsClassDebug.PrintDebug("*** ${checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)} ***")
                        }
                    }
                }, null)
            } else {

            }
        }
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onDestroy() {
        super.onDestroy()
        try {
            coordinates.stopUsingGPS()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    override fun onMapReady(readyGoogleMap: GoogleMap) {
        googleMap = readyGoogleMap

        readyGoogleMap.setOnMapClickListener(this)
        readyGoogleMap.setOnMapLongClickListener(this)
        readyGoogleMap.setOnMarkerClickListener(this)
        readyGoogleMap.setOnInfoWindowClickListener(this)
        readyGoogleMap.setOnInfoWindowLongClickListener(this)
        readyGoogleMap.setOnInfoWindowCloseListener(this)

        readyGoogleMap.uiSettings.isCompassEnabled = false
        readyGoogleMap.uiSettings.isZoomControlsEnabled = false
        readyGoogleMap.isTrafficEnabled = false
        readyGoogleMap.uiSettings.setAllGesturesEnabled(true)
        readyGoogleMap.uiSettings.isMapToolbarEnabled = true

        readyGoogleMap.isTrafficEnabled = false
        readyGoogleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        //init marker
        var drawableCurrent = 0
        if (time == "day") {
            drawableCurrent = R.drawable.ic_pin_current_day
        } else if (time == "night") {
            drawableCurrent = R.drawable.ic_pin_current_night
        }
        val tempBitmapCurrent = BitmapFactory.decodeResource(resources, drawableCurrent)
        val bitmapScaleCurrent = Bitmap.createScaledBitmap(tempBitmapCurrent, tempBitmapCurrent.width / 9, tempBitmapCurrent.height / 9, false)
        val bitmapDescriptorCurrent = BitmapDescriptorFactory.fromBitmap(bitmapScaleCurrent)
        try {
            initMarker = readyGoogleMap.addMarker(
                    MarkerOptions()
                            .position(LocationXY)
                            .title(functionsClass.locationDetail())
                            .draggable(false)
                            .icon(bitmapDescriptorCurrent)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //other marker
        var drawableSaved = 0
        if (time == "day") {
            drawableSaved = R.drawable.ic_pin_day
        } else if (time == "night") {
            drawableSaved = R.drawable.ic_pin_night
        }
        val tempBitmapSaved = BitmapFactory.decodeResource(resources, drawableSaved)
        val bitmapScaleSaved = Bitmap.createScaledBitmap(tempBitmapSaved, tempBitmapSaved.width / 4, tempBitmapSaved.height / 4, false)
        val bitmapDescriptorSaved = BitmapDescriptorFactory.fromBitmap(bitmapScaleSaved)
        if (getFileStreamPath(".Locations").exists() && getFileStreamPath(".Details").exists()) {
            val saveLocations = functionsClass.readFileLine(".Locations")
            val locationDetails = functionsClass.readFileLine(".Details")
            if (saveLocations != null) {
                if (locationDetails != null) {
                    for ((index, markerLocationInfo) in saveLocations.withIndex()) {
                        FunctionsClassDebug.PrintDebug("*** ${markerLocationInfo} --- ${locationDetails[index]} ***")

                        var markerLocation = markerLocationInfo
                        markerLocation = markerLocation.replace("(", "")
                        markerLocation = markerLocation.replace(")", "")
                        val positionOnMap = markerLocation.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                        val latitude = java.lang.Double.valueOf(positionOnMap[0])
                        val longitude = java.lang.Double.valueOf(positionOnMap[1])
                        val latLng = LatLng(latitude, longitude)

                        readyGoogleMap.addMarker(
                                MarkerOptions()
                                        .position(latLng)
                                        .title(locationDetails[index])
                                        .draggable(false)
                                        .icon(bitmapDescriptorSaved)
                        )

                    }
                }
            }
        } else {
            FunctionsClassDebug.PrintDebug("*** ${getFileStreamPath(".Locations").exists()} --- ${getFileStreamPath(".Details").exists()} ***")
        }

        readyGoogleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View {
                val view = layoutInflater.inflate(R.layout.location_info_window, null)
                createInfoView(marker, view)
                return view
            }

            override fun getInfoContents(marker: Marker): View? {
                return null
            }
        })

        Handler().postDelayed({
            val cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(LocationXY, 13f)
            readyGoogleMap.moveCamera(cameraUpdateFactory)
            currentLocation.isEnabled = true
        }, 250)

        switchCamera.setOnClickListener {
            when (switchPhase) {
                0 -> {
                    Latitude = coordinates.getLatitude()
                    Longitude = coordinates.getLongitude()
                    LocationXY = coordinates.locationLatitudeLongitude
                    functionsClass.setCameraPosition(googleMap, LocationXY, true)

                    switchPhase = 1
                }
                1 -> {
                    Latitude = coordinates.getLatitude()
                    Longitude = coordinates.getLongitude()
                    LocationXY = coordinates.locationLatitudeLongitude
                    functionsClass.setCameraPosition(googleMap, LocationXY, false)

                    switchPhase = 0
                }
            }

            val cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(LocationXY, 15f)
            googleMap.moveCamera(cameraUpdateFactory)
        }
    }

    override fun onInfoWindowClick(marker: Marker) {}

    override fun onInfoWindowLongClick(marker: Marker) {
        val map = Intent(applicationContext, SplitStreetViewMap::class.java)
        map.putExtra("LocationX", marker.position.latitude)
        map.putExtra("LocationY", marker.position.longitude)
        map.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(map)
    }

    override fun onInfoWindowClose(marker: Marker) {

    }

    override fun onMapClick(latLng: LatLng) {

    }

    override fun onMapLongClick(latLng: LatLng) {
        dismissOverly.show()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
        return false
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {}

    override fun onLocationChanged(location: Location) {

    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {

    }

    override fun onProviderEnabled(s: String) {

    }

    override fun onProviderDisabled(s: String) {

    }

    private fun createInfoView(marker: Marker, view: View) {
        try {
            val locationInfo = marker.title
            println("*** $locationInfo ***")
            val locationInfoView = view.findViewById<View>(R.id.locationInfo) as TextView
            if (time == "day") {
                locationInfoView.setBackgroundColor(getColor(R.color.light))
                locationInfoView.setTextColor(getColor(R.color.dark))
            } else if (time == "night") {
                locationInfoView.setBackgroundColor(getColor(R.color.dark))
                locationInfoView.setTextColor(getColor(R.color.light))
            }
            locationInfoView.text = locationInfo
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {

        private var LocationXY = LatLng(48.8586074, 2.2947401)
        internal var switchPhase = 0
        internal var Latitude: Double = 0.000
        internal var Longitude: Double = 0.000
    }
}
