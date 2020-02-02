/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 2:50 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Compass

import android.Manifest
import android.app.SearchManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.net.Uri
import android.os.*
import android.util.TypedValue
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.android.gms.location.*
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.orientation.compasshd.BindService
import com.orientation.compasshd.BuildConfig
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.Functions.FunctionsClass
import com.orientation.compasshd.Util.Functions.FunctionsClassDebug
import com.orientation.compasshd.Util.Functions.FunctionsClassPreferences
import com.orientation.compasshd.Util.LocationData.CompassUtil
import com.orientation.compasshd.Util.LocationData.Coordinates
import com.orientation.compasshd.Util.LocationData.FetchAddressIntentService
import com.orientation.compasshd.Util.LocationData.WeatherJSON
import com.orientation.compasshd.Util.NavAdapter.NavDrawerItem
import com.orientation.compasshd.Util.NavAdapter.PopupOptionAdapter
import java.util.*
import kotlin.math.roundToInt

class FloatingCompass : Service() {

    lateinit var functionsClass: FunctionsClass
    lateinit var coordinates: Coordinates
    lateinit var fusedLocationClient: FusedLocationProviderClient

    internal lateinit var weatherJSON: WeatherJSON

    var weatherInformationLoaded: Boolean = false

    lateinit var windowManager: WindowManager
    lateinit var layoutParams: WindowManager.LayoutParams
    lateinit var viewGroup: ViewGroup

    lateinit var imageViewCompass: ImageView
    lateinit var backgroundCompass: ImageView
    lateinit var menuImageView: ImageView
    lateinit var moveImageVie: ImageView
    lateinit var reactivate: TextView

    lateinit var compassUtil: CompassUtil
    lateinit var frameLayout: FrameLayout
    lateinit var drawerLayout: androidx.drawerlayout.widget.DrawerLayout
    lateinit var drawerListView: ListView

    var geoDecodeAttempt: Int = 0

    companion object {
        internal var country = "UK"
        internal var city = "London"
        lateinit var temperature: String
        lateinit var humidity: String
        lateinit var weather: String
        var weatherIconLink: String? = null
        var weatherConditionIcon: Bitmap? = null
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        var drawableCompass: Drawable? = null
        try {
            val path = Uri.parse(TimeCheckCompass.imagePath)
            backgroundCompass.setImageDrawable(null)
            if (intent.getStringExtra("time") == "day") {
                drawableCompass = Drawable.createFromStream(contentResolver.openInputStream(path), path.toString())
                imageViewCompass.setImageDrawable(null)

                val moveDrawable = getDrawable(R.drawable.ic_move)
                val optionDrawable = getDrawable(R.drawable.ic_menu_more)
                moveDrawable!!.setTint(getColor(R.color.light))
                optionDrawable!!.setTint(getColor(R.color.light))
                moveImageVie.setImageDrawable(moveDrawable)
                menuImageView.setImageDrawable(optionDrawable)
            } else if (intent.getStringExtra("time") == "night") {
                drawableCompass = Drawable.createFromStream(contentResolver.openInputStream(path), path.toString())
                val tintDrawable = Drawable.createFromStream(contentResolver.openInputStream(path), path.toString())
                tintDrawable.setTint(functionsClass.setColorAlpha(getColor(R.color.red), 175f))
                imageViewCompass.setImageDrawable(tintDrawable)

                val moveDrawable = getDrawable(R.drawable.ic_move)
                val optionDrawable = getDrawable(R.drawable.ic_menu_more)
                moveDrawable!!.setTint(getColor(R.color.red))
                optionDrawable!!.setTint(getColor(R.color.red))
                moveImageVie.setImageDrawable(moveDrawable)
                menuImageView.setImageDrawable(optionDrawable)
            }
            imageViewCompass.background = drawableCompass
        } catch (e: Exception) {
            if (intent.getStringExtra("time") == "day") {
                backgroundCompass.setImageDrawable(getDrawable(R.drawable.daymod))
                drawableCompass = getDrawable(R.drawable.daymod_arrow)

                val moveDrawable = getDrawable(R.drawable.ic_move)
                val optionDrawable = getDrawable(R.drawable.ic_menu_more)
                moveDrawable!!.setTint(getColor(R.color.light))
                optionDrawable!!.setTint(getColor(R.color.light))
                moveImageVie.setImageDrawable(moveDrawable)
                menuImageView.setImageDrawable(optionDrawable)
            } else if (intent.getStringExtra("time") == "night") {
                backgroundCompass.setImageDrawable(getDrawable(R.drawable.nightmod))
                drawableCompass = getDrawable(R.drawable.nightmod_arrow)

                val moveDrawable = getDrawable(R.drawable.ic_move)
                val optionDrawable = getDrawable(R.drawable.ic_menu_more)
                moveDrawable!!.setTint(getColor(R.color.red))
                optionDrawable!!.setTint(getColor(R.color.red))

                moveImageVie.setImageDrawable(moveDrawable)
                menuImageView.setImageDrawable(optionDrawable)
            }
            imageViewCompass.setImageDrawable(drawableCompass)
        }

        drawerLayout.addDrawerListener(object : androidx.drawerlayout.widget.DrawerLayout.DrawerListener {
            override fun onDrawerSlide(view: View, v: Float) {

            }

            override fun onDrawerOpened(view: View) {

            }

            override fun onDrawerClosed(view: View) {

            }

            override fun onDrawerStateChanged(i: Int) {}
        })
        frameLayout.setOnLongClickListener {
            compassUtil.stop()
            drawerLayout.openDrawer(drawerListView)

            val anim = AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_in)
            reactivate.startAnimation(anim)
            reactivate.visibility = View.VISIBLE
            reactivate.text = getString(R.string.sensor_alert)

            drawerListView.adapter = ArrayAdapter(applicationContext, R.layout.xyz_item, CompassUtil.XYZ)
            drawerListView.selector = ColorDrawable(getColor(R.color.default_color))
            drawerListView.onItemClickListener = AdapterView.OnItemClickListener { arg0, arg1, position, arg3 ->
                if (position == 0) {
                    Toast.makeText(applicationContext, "Thanks", Toast.LENGTH_SHORT).show()
                    val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_gx)))
                    marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(marketIntent)

                    stopSelf()
                } else if (position == 1 || position == 2 || position == 3) {
                    var itemName = drawerListView.getItemAtPosition(position).toString()
                    when (position) {
                        1 -> itemName = "X-Axis $itemName"
                        2 -> itemName = "Y-Axis $itemName"
                        3 -> itemName = "Z-Axis $itemName"
                    }
                    try {
                        val Search = Intent(Intent.ACTION_WEB_SEARCH)
                        Search.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        Search.putExtra(SearchManager.QUERY, itemName)
                        startActivity(Search)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                } else if (position == 4) {
                    reactivate.visibility = View.GONE
                    compassUtil.start()
                    drawerLayout.closeDrawer(drawerListView)
                    drawerListView.adapter = null
                }
            }
            true
        }

        val displayMetrics = resources.displayMetrics
        val dpHeight = displayMetrics.heightPixels.toFloat()
        val dpWidth = displayMetrics.widthPixels.toFloat()

        moveImageVie.setOnClickListener { }
        moveImageVie.setOnTouchListener(object : View.OnTouchListener {
            private val paramsF = layoutParams
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
                        windowManager.updateViewLayout(viewGroup, paramsF)

                        if (FunctionsClassPreferences(applicationContext).CompassConfiguration().readCompassConfig(FunctionsClassPreferences.COMPASS_AUTO_MINIMIZE, true) as Boolean) {
                            if (event.rawX > dpWidth - layoutParams.width / 2) {
                                moveImageVie.setOnTouchListener(null)
                                try {
                                    Handler().postDelayed({
                                        val m = Intent(applicationContext, FloatingCompassMini::class.java)
                                        m.putExtra("yPosition", paramsF.y)
                                        m.putExtra("time", intent.getStringExtra("time"))
                                        m.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        startService(m)
                                    }, 5)

                                    if (viewGroup == null) {
                                        return true
                                    }
                                    if (viewGroup.isShown) {
                                        windowManager.removeViewImmediate(viewGroup)
                                        stopSelf()
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }

                            }
                        }
                    }
                }
                return false
            }
        })

        menuImageView.setOnClickListener { view -> optionMenu(view, intent) }

        val intentFilter = IntentFilter()
        intentFilter.addAction("Close_Compass")
        val broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == "Close_Compass") {
                    if (viewGroup.isShown) {
                        TimeCheckCompass.run = false
                        windowManager.removeViewImmediate(viewGroup)
                        stopSelf()

                        stopService(Intent(applicationContext, BindService::class.java))
                    }
                }
            }
        }
        registerReceiver(broadcastReceiver, intentFilter)

        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_default)
        firebaseRemoteConfig.fetch(0)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseRemoteConfig.activate().addOnSuccessListener {
                            if (firebaseRemoteConfig.getLong(functionsClass.versionCodeRemoteConfigKey()) > BuildConfig.VERSION_CODE) {
                                functionsClass.notificationCreator(
                                        getString(R.string.updateAvailable),
                                        firebaseRemoteConfig.getString(functionsClass.upcomingChangeLogSummaryConfigKey()),
                                        firebaseRemoteConfig.getLong(functionsClass.versionCodeRemoteConfigKey()).toInt()
                                )
                            } else {

                            }
                        }
                    } else {

                    }
                }

        return Service.START_NOT_STICKY
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(applicationContext, BindService::class.java))
        } else {
            startService(Intent(applicationContext, BindService::class.java))
        }
        functionsClass = FunctionsClass(applicationContext)
        functionsClass.addAppShortcuts()

        this.coordinates = Coordinates(applicationContext)

        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        this.coordinates = Coordinates(applicationContext)

        val HW = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, TimeCheckCompass.imageSize.toFloat(), resources.displayMetrics).toInt()

        if (Build.VERSION.SDK_INT > 25) {
            layoutParams = WindowManager.LayoutParams(
                    HW,
                    HW,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT)
        } else {
            layoutParams = WindowManager.LayoutParams(
                    HW,
                    HW,
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT)
        }

        layoutParams.gravity = Gravity.TOP or Gravity.START
        layoutParams.x = 33
        layoutParams.y = 173
        layoutParams.windowAnimations = android.R.style.Animation_Dialog

        val layoutInflater = applicationContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        viewGroup = layoutInflater.inflate(R.layout.floating_compass, null, false) as ViewGroup
        windowManager.addView(viewGroup, layoutParams)

        drawerLayout = viewGroup.findViewById<View>(R.id.drawer_layout) as androidx.drawerlayout.widget.DrawerLayout
        drawerListView = viewGroup.findViewById<View>(R.id.left_drawer) as ListView

        moveImageVie = viewGroup.findViewById<View>(R.id.move) as ImageView
        menuImageView = viewGroup.findViewById<View>(R.id.menu) as ImageView
        imageViewCompass = viewGroup.findViewById<View>(R.id.imageViewCompass) as ImageView
        backgroundCompass = viewGroup.findViewById<View>(R.id.backgroundCompass) as ImageView
        reactivate = viewGroup.findViewById<View>(R.id.reactivate) as TextView
        frameLayout = viewGroup.findViewById<View>(R.id.content_frame) as FrameLayout

        compassUtil = CompassUtil(applicationContext)
        compassUtil.compassView = imageViewCompass
        compassUtil.start()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
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

                                            /*if (googleMapReady) {

                                            } else {

                                            }*/
                                            gpsLocationWeather()
                                        }
                                        FetchAddressIntentService.Constants.FAILURE_RESULT -> {
                                            FunctionsClassDebug.PrintDebug("*** FetchAddressIntentService.Constants.FAILURE_RESULT == ${geoDecodeAttempt} ***")

                                            if (geoDecodeAttempt <= 7) {
                                                val intent = Intent(applicationContext, FetchAddressIntentService::class.java)
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

                            val intent = Intent(applicationContext, FetchAddressIntentService::class.java)
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
        } else {

        }

        TimeCheckCompass.run = true
    }

    override fun onDestroy() {
        compassUtil.stop()
        Coordinates(applicationContext).stopUsingGPS()
        super.onDestroy()
    }

    fun optionMenu(anchorView: View, intent: Intent) {
        val listPopupWindow = ListPopupWindow(applicationContext)
        val navDrawerItem = ArrayList<NavDrawerItem>()
        val popupItemsText = arrayOf(
                getString(R.string.pinmap),
                getString(R.string.locationfind),
                getString(R.string.preferencesCompass),
                getString(R.string.minimize),
                getString(R.string.close))
        val popupItemsIcon = arrayOf(
                getDrawable(R.drawable.ic_pin_current_day),
                getDrawable(android.R.drawable.ic_menu_search),
                getDrawable(R.drawable.w_pref),
                getDrawable(R.drawable.w_pref),
                getDrawable(R.drawable.w_pref))
        val popupItemsId = intArrayOf(
                1,
                2,
                3,
                4,
                5)

        navDrawerItem.add(NavDrawerItem(getString(R.string.weatherLoading), getDrawable(R.drawable.w_pref)!!, 0))
        for (i in popupItemsText.indices) {
            navDrawerItem.add(NavDrawerItem(popupItemsText[i], popupItemsIcon[i]!!, popupItemsId[i]))
        }
        val popupOptionAdapter = PopupOptionAdapter(applicationContext, listPopupWindow, navDrawerItem, intent.getStringExtra("time"), anchorView)

        val Height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 198f, resources.displayMetrics).toInt()
        val Width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 175f, resources.displayMetrics).toInt()

        listPopupWindow.setAdapter(popupOptionAdapter)
        listPopupWindow.anchorView = anchorView
        listPopupWindow.width = Width
        listPopupWindow.height = Height
        listPopupWindow.isModal = true
        listPopupWindow.animationStyle = R.style.GeeksEmpire_ListPopup
        listPopupWindow.setOnDismissListener {
            navDrawerItem.clear()

            gpsLocationWeather()
        }

        if (intent.getStringExtra("time") == "day") {
            val day = getDrawable(R.drawable.popup_background)
            day!!.setTint(getColor(R.color.light))
            listPopupWindow.setBackgroundDrawable(day)
        } else if (intent.getStringExtra("time") == "night") {
            val night = getDrawable(R.drawable.popup_background)
            night!!.setTint(getColor(R.color.dark))
            listPopupWindow.setBackgroundDrawable(getDrawable(R.drawable.popup_background))
        }
        listPopupWindow.show()
    }

    fun gpsLocationWeather() {
        if (functionsClass.networkConnection()) {
            if (this.coordinates.canGetLocation()) {
                val latitude = this.coordinates.latitude
                val longitude = this.coordinates.longitude

                Toast.makeText(applicationContext,
                        functionsClass.locationCityName() +
                                "\nLatitude: " + latitude + "\nLongitude: " + longitude, Toast.LENGTH_LONG).show()
                if (latitude != 0.0 && longitude != 0.0) {
                    try {
                        Handler().postDelayed({
                            val weatherweatherJSONInfo = WeatherweatherJSONInfo()
                            weatherweatherJSONInfo.execute()
                        }, 333)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            } else {
                val gpsOptionsIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                gpsOptionsIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                gpsOptionsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(gpsOptionsIntent)
            }
        }
    }

    private inner class WeatherweatherJSONInfo : AsyncTask<Void, Void, String>() {
        override fun onPreExecute() {

        }

        override fun doInBackground(vararg params: Void): String {
            try {
                city = functionsClass.locationCityName()
                country = functionsClass.getCountryIso()

                val jsonWeatherLink = ("https://api.openweathermap.org/data/2.5/weather?q=" + city + "," + country + "&APPID=" + getString(R.string.openMapWeather))
//                val jsonWeatherLink = ("https://api.openweathermap.org/data/2.5/weather?q=" + "London" + "," + "UK" + "&APPID=" + getString(R.string.openMapWeather))
                weatherJSON = WeatherJSON(jsonWeatherLink)
             //   weatherJSON.fetchJsonFromServer()

                weather = weatherJSON.weather
                temperature = ((weatherJSON.temperature.toDouble() - 273.15).roundToInt()).toString()
                humidity = weatherJSON.humidity
                weatherIconLink = weatherJSON.weatherIconUrl
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {

            }

            return "$country -  $city"
        }

        override fun onPostExecute(result: String) {
            println(result)
            weatherInformationLoaded = true

            Glide.with(applicationContext)
                    .load(weatherIconLink)
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
                            weatherConditionIcon = functionsClass.drawableToBitmap(drawable!!)

                            return false
                        }
                    })
                    .submit()
        }
    }
}
