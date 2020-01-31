/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/30/20 4:21 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Maps

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.orientation.compasshd.Messenger.Group.MessengerGroup
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.Functions.FunctionsClass
import com.orientation.compasshd.Util.Functions.FunctionsClassDebug
import com.orientation.compasshd.Util.Functions.FunctionsClassPreferences
import com.orientation.compasshd.Util.Functions.PublicVariable
import com.orientation.compasshd.Util.LocationData.Coordinates
import com.orientation.compasshd.Util.LocationData.FetchAddressIntentService
import com.orientation.compasshd.Util.UserInformation.UsersInformationDataStructure
import kotlinx.android.synthetic.main.location_picker.*
import net.geeksempire.loadingspin.SpriteFactory
import net.geeksempire.loadingspin.Style
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.*

class AddToMap : androidx.fragment.app.FragmentActivity(),
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowLongClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnInfoWindowCloseListener,
        GoogleMap.OnMarkerDragListener {

    lateinit var functionsClass: FunctionsClass
    lateinit var functionsClassPreferences: FunctionsClassPreferences

    lateinit var supportMapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var coordinates: Coordinates

    var initMarker: Marker? = null

    lateinit var time: String

    var LocationXY = LatLng(51.476852, -0.000000)
    internal var Latitude: Double = 0.0
    internal var Longitude: Double = 0.0

    var geoDecodeAttempt: Int = 0

    private val LOCATIONS_PATH = "/locations"
    private val LOCATIONS_KEY = "latlong"

    private val PREFERENCES_PATH = "/details"
    private val PREFERENCES_KEY = "information"

    lateinit var intentGet: Intent

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    lateinit var firebaseUser: FirebaseUser
    private lateinit var firestoreDatabase: FirebaseFirestore

    companion object {
        var PhotoPickerAction: Boolean = false
        var PhotoPickerUri: Uri? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.location_picker)

        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 666)
        if (!(getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

            finish()
            return
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(this@AddToMap)

        firebaseUser = FirebaseAuth.getInstance()!!.currentUser!!
        firestoreDatabase = FirebaseFirestore.getInstance()

        val hrs = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hrs >= 18 || hrs < 6) {//Night
            time = "night"

            donePicker.setBackgroundColor(getColor(R.color.dark_transparent))
            donePicker.setTextColor(getColor(R.color.default_color_light))
        } else {
            time = "day"

            donePicker.setBackgroundColor(getColor(R.color.light_transparent))
            donePicker.setTextColor(getColor(R.color.default_color_darker))
        }

        functionsClass = FunctionsClass(applicationContext)
        functionsClassPreferences = FunctionsClassPreferences(applicationContext)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        supportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.retainInstance = true

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@AddToMap)
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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

                                            if (PublicVariable.LOCATION_COUNTRY_NAME != null || PublicVariable.LOCATION_CITY_NAME != null) {
                                                supportMapFragment.getMapAsync(this@AddToMap)
                                            } else {
                                                val intent = Intent(this@AddToMap, FetchAddressIntentService::class.java)
                                                intent.action = "LocationData"
                                                intent.putExtra(FetchAddressIntentService.Constants.RESULT_RECEIVER, this)
                                                intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
                                                startService(intent)
                                            }
                                        }
                                        FetchAddressIntentService.Constants.FAILURE_RESULT -> {
                                            FunctionsClassDebug.PrintDebug("*** FetchAddressIntentService.Constants.FAILURE_RESULT == ${geoDecodeAttempt} ***")

                                            if (geoDecodeAttempt <= 13) {
                                                val intent = Intent(this@AddToMap, FetchAddressIntentService::class.java)
                                                intent.action = "LocationData"
                                                intent.putExtra(FetchAddressIntentService.Constants.RESULT_RECEIVER, this)
                                                intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
                                                startService(intent)
                                            } else {

                                            }

                                            geoDecodeAttempt++
                                        }
                                    }
                                }
                            }

                            val intent = Intent(this@AddToMap, FetchAddressIntentService::class.java)
                            intent.action = "LocationData"
                            intent.putExtra(FetchAddressIntentService.Constants.RESULT_RECEIVER, resultReceiver)
                            intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
                            startService(intent)

                            coordinates = Coordinates(applicationContext)

                            Latitude = location!!.latitude
                            Longitude = location.longitude
                            LocationXY = LatLng(Latitude, Longitude)
                        }.addOnFailureListener {
                            FunctionsClassDebug.PrintDebug("*** ${it} ***")
                        }
                    } else {
                        FunctionsClassDebug.PrintDebug("*** ${checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)} ***")
                    }
                }
            }, null)
        }

        intentGet = intent
        FunctionsClassDebug.PrintDebug("*** Intent Action ${intentGet} ***")
        if (!intentGet.hasExtra(Intent.EXTRA_STREAM)) {

            PhotoPickerAction = true
            return
        }

        Handler().postDelayed({
            if (intentGet.action == Intent.ACTION_SEND) {
                val imageUri = intentGet.getParcelableExtra(Intent.EXTRA_STREAM) as Uri
                FunctionsClassDebug.PrintDebug("*** ${imageUri} ***")

                Glide.with(this@AddToMap)
                        .load(imageUri)
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
                                val bitmap = functionsClass.drawableToBitmap(drawable!!)
                                runOnUiThread {
                                    pickedImage.setImageBitmap(functionsClass.bitmapToCircle(bitmap!!))
                                }

                                return false
                            }
                        })
                        .submit()
            } else if (intentGet.action == Intent.ACTION_SEND_MULTIPLE) {
                val imageUris = intentGet.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                FunctionsClassDebug.PrintDebug("*** ${imageUris} ***")
                if (imageUris.size > 2) {
                    Toast.makeText(applicationContext, getString(R.string.pickedErrorMoreTwo), Toast.LENGTH_LONG).show()

                    pickedImage.isEnabled = true
                    PhotoPickerAction = true

                    pickedImage.setOnClickListener {
                        val API = Build.VERSION.SDK_INT
                        if (API > 22) {
                            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                                val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                                requestPermissions(permission, 666)
                            }
                        }
                        val photoPickerIntent = Intent(Intent.ACTION_PICK)
                        photoPickerIntent.type = "image/*"
                        startActivityForResult(photoPickerIntent, 123)
                    }

                    return@postDelayed
                }
                pickedImage.visibility = View.INVISIBLE
                pickedImageOne.visibility = View.VISIBLE
                pickedImageTwo.visibility = View.VISIBLE

                Glide.with(this@AddToMap)
                        .load(imageUris[0])
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
                                val bitmap = functionsClass.drawableToBitmap(drawable!!)
                                pickedImageOne.setImageBitmap(functionsClass.bitmapToCircle(bitmap!!))

                                return false
                            }
                        })
                        .submit()

                Glide.with(this@AddToMap)
                        .load(imageUris[1])
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
                                val bitmap = functionsClass.drawableToBitmap(drawable!!)
                                pickedImageTwo.setImageBitmap(functionsClass.bitmapToCircle(bitmap!!))

                                return false
                            }
                        })
                        .submit()
            }
        }, 1000)
    }

    override fun onStart() {
        super.onStart()

        currentLocation.setOnClickListener { view ->
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@AddToMap)
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
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
                                                coordinates = Coordinates(applicationContext)

                                                val addressOutput = resultData?.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY).toString()
                                                FunctionsClassDebug.PrintDebug("*** FetchAddressIntentService.Constants.SUCCESS_RESULT == ${addressOutput} ***")

                                                if (PublicVariable.LOCATION_COUNTRY_NAME != null || PublicVariable.LOCATION_CITY_NAME != null) {
                                                    Latitude = location!!.latitude
                                                    Longitude = location!!.longitude
                                                    LocationXY = LatLng(Latitude, Longitude)

                                                    if (initMarker != null) {
                                                        initMarker!!.remove()
                                                    }
                                                    var drawableCurrent = 0
                                                    if (time == "day") {
                                                        drawableCurrent = R.drawable.ic_pin_day
                                                    } else if (time == "night") {
                                                        drawableCurrent = R.drawable.ic_pin_night
                                                    }
                                                    val tempBitmapCurrent = BitmapFactory.decodeResource(resources, drawableCurrent)
                                                    val bitmapScaleCurrent = Bitmap.createScaledBitmap(tempBitmapCurrent, tempBitmapCurrent.width / 4, tempBitmapCurrent.height / 4, false)
                                                    val bitmapDescriptorCurrent = BitmapDescriptorFactory.fromBitmap(bitmapScaleCurrent)
                                                    try {
                                                        initMarker = googleMap.addMarker(
                                                                MarkerOptions()
                                                                        .position(LocationXY)
                                                                        .draggable(true)
                                                                        .icon(bitmapDescriptorCurrent)
                                                        )
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                    }

                                                    view.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_location_button))
                                                    val cameraUpdateFactory = CameraUpdateFactory
                                                            .newLatLngZoom(LocationXY, 15f)
                                                    googleMap.animateCamera(cameraUpdateFactory)

                                                    supportMapFragment.getMapAsync(this@AddToMap)
                                                } else {
                                                    val intent = Intent(this@AddToMap, FetchAddressIntentService::class.java)
                                                    intent.action = "LocationData"
                                                    intent.putExtra(FetchAddressIntentService.Constants.RESULT_RECEIVER, this)
                                                    intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
                                                    startService(intent)
                                                }
                                            }
                                            FetchAddressIntentService.Constants.FAILURE_RESULT -> {
                                                FunctionsClassDebug.PrintDebug("*** FetchAddressIntentService.Constants.FAILURE_RESULT == ${geoDecodeAttempt} ***")

                                                if (geoDecodeAttempt <= 13) {
                                                    val intent = Intent(this@AddToMap, FetchAddressIntentService::class.java)
                                                    intent.action = "LocationData"
                                                    intent.putExtra(FetchAddressIntentService.Constants.RESULT_RECEIVER, this)
                                                    intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
                                                    startService(intent)
                                                } else {

                                                }

                                                geoDecodeAttempt++
                                            }
                                        }
                                    }
                                }

                                val intent = Intent(this@AddToMap, FetchAddressIntentService::class.java)
                                intent.action = "LocationData"
                                intent.putExtra(FetchAddressIntentService.Constants.RESULT_RECEIVER, resultReceiver)
                                intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
                                startService(intent)
                            }.addOnFailureListener {
                                FunctionsClassDebug.PrintDebug("*** ${it} ***")
                            }
                        } else {
                            FunctionsClassDebug.PrintDebug("*** ${checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)} ***")
                        }
                    }
                }, null)
            }
        }

        donePicker.setOnClickListener { view ->
            val cameraUpdateFactory = CameraUpdateFactory
                    .newLatLngZoom(LocationXY, 15f)
            googleMap.animateCamera(cameraUpdateFactory)

            view.isEnabled = false
            currentLocation.isEnabled = false

            loading.visibility = View.VISIBLE
            loading.setColor(getColor(R.color.red))
            loading.setIndeterminateDrawable(SpriteFactory.create(Style.DOUBLE_BOUNCE))

            Handler().postDelayed({
                if (intentGet.action == Intent.ACTION_SEND_MULTIPLE) {
                    val imageUris = intentGet.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                    if (imageUris.size > 2) {
                        Toast.makeText(applicationContext, getString(R.string.pickedErrorMoreTwo), Toast.LENGTH_LONG).show()
                        return@postDelayed
                    }

                    var uriCounter = 0
                    imageUris.forEachIndexed { index, imageUri ->
                        FunctionsClassDebug.PrintDebug("*** ${imageUri} ***")

                        Glide.with(this@AddToMap)
                                .load(imageUri)
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
                                        val filePath = File(externalMediaDirs[0].path,
                                                File.separator
                                                        + "PinPicsOnMap"
                                                        + File.separator
                                                        + "($Latitude,$Longitude)")
                                        filePath.mkdirs()

                                        val locationImagePath = (PublicVariable.LOCATION_CITY_NAME + "_"
                                                + "(" + Latitude + "," + Longitude + ")"
                                                + "_" + System.currentTimeMillis() + "." + Bitmap.CompressFormat.JPEG)

                                        val file = File(filePath, locationImagePath)
                                        val fileOutputStream = FileOutputStream(file)
                                        val bitmap = functionsClass.drawableToBitmap(drawable!!)
                                        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                                        fileOutputStream.flush()
                                        fileOutputStream.close()

                                        functionsClass.savePreference("(${Latitude},${Longitude})", "LocationDetail", PublicVariable.LOCATION_INFO_DETAIL!!)
                                        functionsClass.savePreference("(${Latitude},${Longitude})", "CityName", PublicVariable.LOCATION_CITY_NAME!!)

                                        if (getFileStreamPath(".Locations").exists()) {
                                            val lines = functionsClass.readFileLine(".Locations")
                                            var lineExist = false
                                            for (line in lines) {
                                                if (line == "(${Latitude},${Longitude})") {
                                                    lineExist = true

                                                    break
                                                }
                                            }
                                            if (!lineExist) {
                                                functionsClass.saveFileAppendLine(".Locations", "(${Latitude},${Longitude})")
                                            }
                                        } else {
                                            functionsClass.saveFileAppendLine(".Locations", "(${Latitude},${Longitude})")
                                        }

                                        if (getFileStreamPath(".Details").exists()) {
                                            val lines = functionsClass.readFileLine(".Details")
                                            var lineExist = false
                                            for (line in lines) {
                                                if (line == "(${Latitude},${Longitude})") {
                                                    lineExist = true

                                                    break
                                                }
                                            }
                                            if (!lineExist) {
                                                functionsClass.saveFileAppendLine(".Details", functionsClass.readPreference("(${Latitude},${Longitude})", "LocationDetail", "Unknown").toString())
                                            }
                                        } else {
                                            functionsClass.saveFileAppendLine(".Details", functionsClass.readPreference("(${Latitude},${Longitude})", "LocationDetail", "Unknown").toString())
                                        }

                                        uriCounter++
                                        if (uriCounter == imageUris.size) {
                                            Handler().postDelayed(Runnable {
                                                if (getFileStreamPath(".Locations").exists()) {
                                                    functionsClass.sendLocationsData(this@AddToMap, LOCATIONS_PATH, LOCATIONS_KEY)
                                                    Handler().postDelayed({
                                                        functionsClass.sendLocationsDetails(this@AddToMap, PREFERENCES_PATH, PREFERENCES_KEY)
                                                    }, 1333)

                                                    Handler().postDelayed({
                                                        this@AddToMap.finish()
                                                    }, 2000)
                                                }
                                            }, 1333)

                                            val picsCounter = functionsClassPreferences.readPreference(".SavedDataInfo", "LastPicsCount", 0).toString().toInt()
                                            functionsClassPreferences.savePreference(".SavedDataInfo", "LastPicsCount", picsCounter + 2)
                                        }

                                        return false
                                    }
                                })
                                .submit()
                    }
                } else if (intentGet.action == Intent.ACTION_SEND) {
                    val imageUri = intentGet.getParcelableExtra(Intent.EXTRA_STREAM) as Uri
                    FunctionsClassDebug.PrintDebug("*** ${imageUri} ***")

                    Glide.with(this@AddToMap)
                            .load(imageUri)
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
                                    val filePath = File(externalMediaDirs[0].path,
                                            File.separator
                                                    + "PinPicsOnMap"
                                                    + File.separator
                                                    + "($Latitude,$Longitude)")
                                    filePath.mkdirs()

                                    val locationImagePath = (PublicVariable.LOCATION_CITY_NAME + "_"
                                            + "(" + Latitude + "," + Longitude + ")"
                                            + "_" + System.currentTimeMillis() + "." + Bitmap.CompressFormat.JPEG)

                                    val file = File(filePath, locationImagePath)
                                    val fileOutputStream = FileOutputStream(file)
                                    val bitmap = functionsClass.drawableToBitmap(drawable!!)
                                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                                    fileOutputStream.flush()
                                    fileOutputStream.close()

                                    functionsClass.savePreference("(${Latitude},${Longitude})", "LocationDetail", PublicVariable.LOCATION_INFO_DETAIL!!)
                                    functionsClass.savePreference("(${Latitude},${Longitude})", "CityName", PublicVariable.LOCATION_CITY_NAME!!)

                                    if (getFileStreamPath(".Locations").exists()) {
                                        val lines = functionsClass.readFileLine(".Locations")
                                        var lineExist = false
                                        for (line in lines) {
                                            if (line == "(${Latitude},${Longitude})") {
                                                lineExist = true

                                                break
                                            }
                                        }
                                        if (!lineExist) {
                                            functionsClass.saveFileAppendLine(".Locations", "(${Latitude},${Longitude})")
                                        }
                                    } else {
                                        functionsClass.saveFileAppendLine(".Locations", "(${Latitude},${Longitude})")
                                    }

                                    if (getFileStreamPath(".Details").exists()) {
                                        val lines = functionsClass.readFileLine(".Details")
                                        var lineExist = false
                                        for (line in lines) {
                                            if (line == "(${Latitude},${Longitude})") {
                                                lineExist = true

                                                break
                                            }
                                        }
                                        if (!lineExist) {
                                            functionsClass.saveFileAppendLine(".Details", functionsClass.readPreference("(${Latitude},${Longitude})", "LocationDetail", "Unknown").toString())
                                        }
                                    } else {
                                        functionsClass.saveFileAppendLine(".Details", functionsClass.readPreference("(${Latitude},${Longitude})", "LocationDetail", "Unknown").toString())
                                    }

                                    runOnUiThread {
                                        Handler().postDelayed(Runnable {
                                            if (getFileStreamPath(".Locations").exists()) {
                                                functionsClass.sendLocationsData(this@AddToMap, LOCATIONS_PATH, LOCATIONS_KEY)
                                                Handler().postDelayed({
                                                    functionsClass.sendLocationsDetails(this@AddToMap, PREFERENCES_PATH, PREFERENCES_KEY)
                                                }, 1333)

                                                Handler().postDelayed({
                                                    this@AddToMap.finish()
                                                }, 2000)
                                            }
                                        }, 1333)
                                    }

                                    val picsCounter = functionsClassPreferences.readPreference(".SavedDataInfo", "LastPicsCount", 0).toString().toInt()
                                    functionsClassPreferences.savePreference(".SavedDataInfo", "LastPicsCount", picsCounter + 1)

                                    return false
                                }
                            })
                            .submit()
                } else if (PhotoPickerAction) {
                    val imageUri = PhotoPickerUri
                    if (imageUri != null) {
                        FunctionsClassDebug.PrintDebug("*** ${imageUri} ***")

                        Glide.with(this@AddToMap)
                                .load(imageUri)
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
                                        val filePath = File(externalMediaDirs[0].path,
                                                File.separator
                                                        + "PinPicsOnMap"
                                                        + File.separator
                                                        + "($Latitude,$Longitude)")
                                        filePath.mkdirs()

                                        val locationImagePath = (PublicVariable.LOCATION_CITY_NAME + "_"
                                                + "(" + Latitude + "," + Longitude + ")"
                                                + "_" + System.currentTimeMillis() + "." + Bitmap.CompressFormat.JPEG)

                                        val file = File(filePath, locationImagePath)
                                        val fileOutputStream = FileOutputStream(file)
                                        val bitmap = functionsClass.drawableToBitmap(drawable!!)
                                        bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)
                                        fileOutputStream.flush()
                                        fileOutputStream.close()

                                        functionsClass.savePreference("(${Latitude},${Longitude})", "LocationDetail", PublicVariable.LOCATION_INFO_DETAIL!!)
                                        functionsClass.savePreference("(${Latitude},${Longitude})", "CityName", PublicVariable.LOCATION_CITY_NAME!!)

                                        if (getFileStreamPath(".Locations").exists()) {
                                            val lines = functionsClass.readFileLine(".Locations")
                                            var lineExist = false
                                            for (line in lines) {
                                                if (line == "(${Latitude},${Longitude})") {
                                                    lineExist = true

                                                    break
                                                }
                                            }
                                            if (!lineExist) {
                                                functionsClass.saveFileAppendLine(".Locations", "(${Latitude},${Longitude})")
                                            }
                                        } else {
                                            functionsClass.saveFileAppendLine(".Locations", "(${Latitude},${Longitude})")
                                        }

                                        if (getFileStreamPath(".Details").exists()) {
                                            val lines = functionsClass.readFileLine(".Details")
                                            var lineExist = false
                                            for (line in lines) {
                                                if (line == "(${Latitude},${Longitude})") {
                                                    lineExist = true

                                                    break
                                                }
                                            }
                                            if (!lineExist) {
                                                functionsClass.saveFileAppendLine(".Details", functionsClass.readPreference("(${Latitude},${Longitude})", "LocationDetail", "Unknown").toString())
                                            }
                                        } else {
                                            functionsClass.saveFileAppendLine(".Details", functionsClass.readPreference("(${Latitude},${Longitude})", "LocationDetail", "Unknown").toString())
                                        }

                                        Handler().postDelayed(Runnable {
                                            if (getFileStreamPath(".Locations").exists()) {
                                                functionsClass.sendLocationsData(this@AddToMap, LOCATIONS_PATH, LOCATIONS_KEY)
                                                Handler().postDelayed({
                                                    functionsClass.sendLocationsDetails(this@AddToMap, PREFERENCES_PATH, PREFERENCES_KEY)
                                                }, 1333)

                                                Handler().postDelayed({
                                                    this@AddToMap.finish()
                                                }, 2000)
                                            }
                                        }, 1333)

                                        val picsCounter = functionsClassPreferences.readPreference(".SavedDataInfo", "LastPicsCount", 0).toString().toInt()
                                        functionsClassPreferences.savePreference(".SavedDataInfo", "LastPicsCount", picsCounter + 1)

                                        return false
                                    }
                                })
                                .submit()
                    } else {
                        pickedImage.setImageBitmap(null)
                    }
                }
            }, 333)
        }

        if (PhotoPickerAction) {
            pickedImage.setOnClickListener {
                val API = Build.VERSION.SDK_INT
                if (API > 22) {
                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                        requestPermissions(permission, 666)
                    }
                }
                val photoPickerIntent = Intent(Intent.ACTION_PICK)
                photoPickerIntent.type = "image/*"
                startActivityForResult(photoPickerIntent, 123)
            }
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        this@AddToMap.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            123 -> if (resultCode == Activity.RESULT_OK) {
                try {
                    intentGet = data!!
                    val imageUri = data!!.data
                    val imageStream = contentResolver.openInputStream(imageUri!!)
                    val path = imageUri.toString()

                    PhotoPickerUri = imageUri

                    val drawableResult = Drawable.createFromStream(imageStream, imageUri.toString())
                    val bitmap = functionsClass.drawableToBitmap(drawableResult!!)

                    pickedImage.setImageBitmap(functionsClass.bitmapToCircle(bitmap!!))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                }

            }
        }
    }

    /*Map Functions*/
    override fun onMapReady(googleMapReady: GoogleMap?) {
        googleMap = googleMapReady!!

        googleMap.setOnMapClickListener(this@AddToMap)
        googleMap.setOnMapLongClickListener(this@AddToMap)
        googleMap.setOnMarkerClickListener(this@AddToMap)
        googleMap.setOnInfoWindowClickListener(this@AddToMap)
        googleMap.setOnInfoWindowLongClickListener(this@AddToMap)
        googleMap.setOnInfoWindowCloseListener(this@AddToMap)

        googleMap.uiSettings.isCompassEnabled = false
        googleMap.uiSettings.isZoomControlsEnabled = false
        googleMap.uiSettings.setAllGesturesEnabled(true)
        googleMap.uiSettings.isMapToolbarEnabled = false

        googleMap.isTrafficEnabled = false
        googleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        if (initMarker != null) {
            initMarker!!.remove()
        }
        var drawableCurrent = 0
        if (time == "day") {
            drawableCurrent = R.drawable.ic_pin_day
        } else if (time == "night") {
            drawableCurrent = R.drawable.ic_pin_night
        }
        val tempBitmapCurrent = BitmapFactory.decodeResource(resources, drawableCurrent)
        val bitmapScaleCurrent = Bitmap.createScaledBitmap(tempBitmapCurrent, tempBitmapCurrent.width / 4, tempBitmapCurrent.height / 4, false)
        val bitmapDescriptorCurrent = BitmapDescriptorFactory.fromBitmap(bitmapScaleCurrent)
        try {
            initMarker = googleMap.addMarker(
                    MarkerOptions()
                            .position(LocationXY)
                            .draggable(true)
                            .icon(bitmapDescriptorCurrent)
            )

            currentLocation.visibility = View.VISIBLE
            donePicker.visibility = View.VISIBLE
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val cameraUpdateFactory = CameraUpdateFactory
                .newLatLngZoom(LocationXY, 15f)
        googleMap.animateCamera(cameraUpdateFactory)

        /*User Profile*/
        val usersInformationDataStructure = UsersInformationDataStructure().UserInformationFirestore(
                firebaseUser.uid,
                firebaseUser.email!!,
                firebaseUser.displayName!!,
                firebaseUser.photoUrl.toString(),
                FieldValue.serverTimestamp(),
                true.toString()
        )

        /*Save User Information*/
        firestoreDatabase.document(
                "PinPicsOnMap/" +
                        "People/" +
                        "Profile/" +
                        "${firebaseUser.uid}/"
        ).set(usersInformationDataStructure).addOnSuccessListener {
            val messagesCollectionReferencePath: String =
                    "PinPicsOnMap/" +
                            "Messenger/" +
                            "${PublicVariable.LOCATION_COUNTRY_NAME}/" +
                            "${functionsClass.locationCityName()}/" +
                            "GroupsMessages/"

            val linkedHashMapUserGroupHistory = LinkedHashMap<Any, Any>()
            linkedHashMapUserGroupHistory["GroupAddress"] = messagesCollectionReferencePath
            linkedHashMapUserGroupHistory["theChatName"] = "${PublicVariable.LOCATION_COUNTRY_NAME!!}-${functionsClass.locationCityName()}"
            linkedHashMapUserGroupHistory["ISO"] = functionsClass.getCountryIso()
            linkedHashMapUserGroupHistory["CountryName"] = PublicVariable.LOCATION_COUNTRY_NAME!!
            linkedHashMapUserGroupHistory["CityName"] = functionsClass.locationCityName()
            linkedHashMapUserGroupHistory["JoinedTime"] = FieldValue.serverTimestamp()
            linkedHashMapUserGroupHistory["lastLocationLatitude"] = LocationXY.latitude
            linkedHashMapUserGroupHistory["lastLocationLongitude"] = LocationXY.longitude
            linkedHashMapUserGroupHistory["lastLocationDetail"] = functionsClass.locationDetail()

            firestoreDatabase.document(
                    "PinPicsOnMap/" +
                            "People/" +
                            "Profile/" +
                            "${firebaseUser.uid}/" +
                            "Groups/" +
                            "${PublicVariable.LOCATION_COUNTRY_NAME}-${functionsClass.locationCityName()}/"
            ).set(linkedHashMapUserGroupHistory).addOnSuccessListener {

            }.addOnCanceledListener {

            }
        }.addOnCanceledListener {

        }

        /*Add User To City*/
        firestoreDatabase.document(
                "PinPicsOnMap/" +
                        "Messenger/" +
                        "${PublicVariable.LOCATION_COUNTRY_NAME}/" +
                        "${functionsClass.locationCityName()}/" +
                        "People/" +
                        "${firebaseUser.uid}/"
        ).set(usersInformationDataStructure).addOnSuccessListener {
            functionsClassPreferences.savePreference(".AlreadyBeenHere", (functionsClass.locationCountryName() + "-" + functionsClass.locationCityName()), true)

        }.addOnCanceledListener {


        }

        /*Add Location to Visited Directory*/
        val linkedHashMapUserLocation = LinkedHashMap<Any, Any>()
        linkedHashMapUserLocation["locationLatitude"] = LocationXY.latitude
        linkedHashMapUserLocation["locationLongitude"] = LocationXY.longitude
        linkedHashMapUserLocation["locationDetail"] = functionsClass.locationDetail()
        firestoreDatabase.document(
                "PinPicsOnMap/" +
                        "Messenger/" +
                        "${PublicVariable.LOCATION_COUNTRY_NAME}/" +
                        "${functionsClass.locationCityName()}/" +
                        "People/" +
                        "${firebaseUser.uid}/" +
                        "Places/" +
                        "Visited-${LocationXY.latitude}-${LocationXY.longitude}"
        ).set(linkedHashMapUserLocation).addOnSuccessListener {

        }.addOnCanceledListener {

        }

        /*Subscribe To Topic*/
        val countryNameTopic = MessengerGroup.countryName.replace(" ", "")
        val cityNameTopic = MessengerGroup.cityName.replace(" ", "")

        try {
            FirebaseMessaging.getInstance().subscribeToTopic("${countryNameTopic}-${cityNameTopic}").addOnSuccessListener {

                val bundle: Bundle = Bundle()
                bundle.putString("LocationName", "${countryNameTopic}-${cityNameTopic}")
                firebaseAnalytics.logEvent(PublicVariable.SUBSCRIBE_TO_TOPIC_SUCCESSFUL, bundle)
            }.addOnFailureListener {

                val bundle: Bundle = Bundle()
                bundle.putString("LocationName", "${countryNameTopic}-${cityNameTopic}")
                firebaseAnalytics.logEvent(PublicVariable.SUBSCRIBE_TO_TOPIC_FAILED, bundle)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()

            val bundle: Bundle = Bundle()
            bundle.putString("LocationName", "${countryNameTopic}-${cityNameTopic}")
            firebaseAnalytics.logEvent(PublicVariable.SUBSCRIBE_TO_TOPIC_FAILED, bundle)
        }

        loading.visibility = View.INVISIBLE
    }

    override fun onMarkerClick(marker: Marker?): Boolean {

        return false
    }

    override fun onInfoWindowLongClick(marker: Marker?) {

    }

    override fun onInfoWindowClick(marker: Marker?) {

    }

    override fun onMapLongClick(latLng: LatLng?) {
        LocationXY = LatLng(latLng!!.latitude, latLng!!.longitude)
        Latitude = latLng!!.latitude
        Longitude = latLng!!.longitude

        if (initMarker != null) {
            initMarker!!.remove()
        }
        var drawableCurrent = 0
        if (time == "day") {
            drawableCurrent = R.drawable.ic_pin_day
        } else if (time == "night") {
            drawableCurrent = R.drawable.ic_pin_night
        }
        val tempBitmapCurrent = BitmapFactory.decodeResource(resources, drawableCurrent)
        val bitmapScaleCurrent = Bitmap.createScaledBitmap(tempBitmapCurrent, tempBitmapCurrent.width / 4, tempBitmapCurrent.height / 4, false)
        val bitmapDescriptorCurrent = BitmapDescriptorFactory.fromBitmap(bitmapScaleCurrent)
        try {
            initMarker = googleMap.addMarker(
                    MarkerOptions()
                            .position(LocationXY)
                            .draggable(true)
                            .icon(bitmapDescriptorCurrent)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val cameraUpdateFactory = CameraUpdateFactory
                .newLatLngZoom(LocationXY, 15f)
        googleMap.animateCamera(cameraUpdateFactory)
    }

    override fun onMapClick(latLng: LatLng?) {

    }

    override fun onInfoWindowClose(marker: Marker?) {

    }

    override fun onMarkerDragEnd(marker: Marker?) {
        LocationXY = LatLng(marker!!.position.latitude, marker!!.position.longitude)
        Latitude = marker!!.position.latitude
        Longitude = marker!!.position.longitude

        val cameraUpdateFactory = CameraUpdateFactory
                .newLatLngZoom(LocationXY, 15f)
        googleMap.animateCamera(cameraUpdateFactory)
    }

    override fun onMarkerDragStart(marker: Marker?) {

    }

    override fun onMarkerDrag(marker: Marker?) {

    }
}