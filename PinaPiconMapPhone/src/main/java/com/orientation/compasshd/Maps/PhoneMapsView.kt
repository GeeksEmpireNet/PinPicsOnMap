/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 5:17 PM
 * Last modified 1/30/20 5:17 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Maps

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.app.SearchManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.*
import android.provider.MediaStore
import android.text.Html
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.circularreveal.CircularRevealGridLayout
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appindexing.Action
import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.FirebaseUserActions
import com.google.firebase.appindexing.Indexable
import com.google.firebase.appindexing.builders.Actions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.google.maps.android.ui.IconGenerator
import com.orientation.compasshd.BuildConfig
import com.orientation.compasshd.Compass.FloatingCompass
import com.orientation.compasshd.Messenger.Group.MessengerGroup
import com.orientation.compasshd.Messenger.Util.ChatHistory
import com.orientation.compasshd.Messenger.Util.UserStatesIntentService
import com.orientation.compasshd.PinPicsOnMapApplication
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.Functions.*
import com.orientation.compasshd.Util.IAP.InAppBilling
import com.orientation.compasshd.Util.LocationData.Coordinates
import com.orientation.compasshd.Util.LocationData.FetchAddressIntentService
import com.orientation.compasshd.Util.LocationData.WeatherJSON
import com.orientation.compasshd.Util.UserInformation.UsersInformationDataStructure
import kotlinx.android.synthetic.main.maps_view.*
import net.geeksempire.chat.vicinity.Util.FunctionsClass.FunctionsClassSystemCheckpoint
import java.io.*
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.math.roundToInt

class PhoneMapsView : androidx.fragment.app.FragmentActivity(),
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowLongClickListener,
        GoogleMap.OnInfoWindowClickListener,
        GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnInfoWindowCloseListener {

    @Inject lateinit var functionsClassPreferences: FunctionsClassPreferences

    lateinit var functionsClass: FunctionsClass
    lateinit var functionsClassDialogue: FunctionsClassDialogue

    lateinit var supportMapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap

    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var coordinates: Coordinates

    lateinit var initMarker: Marker
    lateinit var clickedMarker: Marker
    lateinit var instagramMarker: Marker
    lateinit var locationDetailMarker: Marker

    lateinit var infoWindowView: View
    lateinit var savedImage: CircularRevealGridLayout

    var googleMapReady: Boolean = false

    lateinit var locationImagePath: String
    lateinit var time: String

    lateinit var interstitialAd: InterstitialAd
    lateinit var adRequestInterstitial: AdRequest

    private val LOCATIONS_PATH = "/locations"
    private val LOCATIONS_KEY = "latlong"

    private val PREFERENCES_PATH = "/details"
    private val PREFERENCES_KEY = "information"

    internal var country = "UK"
    internal var city = "London"

    internal lateinit var weatherJSON: WeatherJSON
    internal lateinit var getCity: String
    internal lateinit var getUrl: String
    internal lateinit var temperature: String
    internal lateinit var humidity: String
    internal lateinit var weather: String
    internal lateinit var weatherIcon: String

    var geoDecodeAttempt: Int = 0

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    lateinit var firebaseUser: FirebaseUser
    private lateinit var firebaseFunctions: FirebaseFunctions

    lateinit var uploadTask: StorageTask<UploadTask.TaskSnapshot>

    private lateinit var firestoreDatabase: FirebaseFirestore

    companion object {
        private val BASE_URL = Uri.parse("https://www.geeksempire.net/compass.html/")

        private var LocationXY = LatLng(51.5007424, -0.1247307)
        internal var switchPhase = 0
        internal lateinit var cityName: String
        internal var Latitude: Double = 0.0
        internal var Longitude: Double = 0.0

        var triggerCameraIdle: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as PinPicsOnMapApplication)
                .dependencyGraph.inject(this@PhoneMapsView)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.maps_view)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        functionsClass = FunctionsClass(applicationContext)
        functionsClassDialogue = FunctionsClassDialogue(this@PhoneMapsView, functionsClassPreferences)

        functionsClassDialogue.changeLog(false)
        functionsClass.addAppShortcuts()

        supportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.retainInstance = true

        coordinates = Coordinates(applicationContext)

        firebaseAnalytics = FirebaseAnalytics.getInstance(this@PhoneMapsView)

        if (intent.hasExtra("time")) {
            time = intent.getStringExtra("time")
        } else {
            time = functionsClass.getTime()
        }

        if (time == "day") {
            val layerDrawableIcon = getDrawable(R.drawable.draw_weather_background) as LayerDrawable
            val layerDrawableBackgroundIcon = layerDrawableIcon.findDrawableByLayerId(R.id.backtemp) as GradientDrawable
            layerDrawableBackgroundIcon.setColor(getColor(R.color.light_transparent))

            val layerDrawableInfo = getDrawable(R.drawable.draw_weather_background) as LayerDrawable
            val layerDrawableBackgroundInfo = layerDrawableInfo.findDrawableByLayerId(R.id.backtemp) as GradientDrawable
            layerDrawableBackgroundInfo.setColor(getColor(R.color.light_transparent))

            weatherIconView.background = layerDrawableBackgroundIcon
            weatherInfoView.background = layerDrawableBackgroundInfo

            chatWithPeople.setBackgroundColor(getColor(R.color.light_transparent))
            chatWithPeople.setTextColor(getColor(R.color.dark))

            chatWithPeopleHistory.setBackgroundColor(getColor(R.color.light_transparent))
            chatWithPeopleHistory.setTextColor(getColor(R.color.dark))
            chatWithPeopleHistory.iconTint = ColorStateList.valueOf(getColor(R.color.default_color_darker))
        } else if (time == "night") {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }

            val layerDrawableIcon = getDrawable(R.drawable.draw_weather_background) as LayerDrawable
            val layerDrawableBackgroundIcon = layerDrawableIcon.findDrawableByLayerId(R.id.backtemp) as GradientDrawable
            layerDrawableBackgroundIcon.setColor(getColor(R.color.dark_transparent))

            val layerDrawableInfo = getDrawable(R.drawable.draw_weather_background) as LayerDrawable
            val layerDrawableBackgroundInfo = layerDrawableInfo.findDrawableByLayerId(R.id.backtemp) as GradientDrawable
            layerDrawableBackgroundInfo.setColor(getColor(R.color.dark_transparent))

            weatherIconView.background = layerDrawableBackgroundIcon
            weatherInfoView.background = layerDrawableBackgroundInfo

            chatWithPeople.setBackgroundColor(getColor(R.color.dark_transparent))
            chatWithPeople.setTextColor(getColor(R.color.light))

            chatWithPeopleHistory.setBackgroundColor(getColor(R.color.dark_transparent))
            chatWithPeopleHistory.setTextColor(getColor(R.color.light))
            chatWithPeopleHistory.iconTint = ColorStateList.valueOf(getColor(R.color.default_color_light))
        }

        if (!(getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val intent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

            finish()
            return
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this@PhoneMapsView)
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
                        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location ->
                            MessengerGroup.location = location

                            val resultReceiver: ResultReceiver = object : ResultReceiver(Handler()) {
                                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                                    when (resultCode) {
                                        FetchAddressIntentService.Constants.SUCCESS_RESULT -> {
                                            val addressOutput = resultData?.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY).toString()
                                            FunctionsClassDebug.PrintDebug("*** FetchAddressIntentService.Constants.SUCCESS_RESULT == ${addressOutput} ***")

                                            if (PublicVariable.LOCATION_COUNTRY_NAME != null || PublicVariable.LOCATION_CITY_NAME != null) {
                                                supportMapFragment.getMapAsync(this@PhoneMapsView)

                                                val weatherConditionInfo: WeatherConditionInfo = WeatherConditionInfo()
                                                weatherConditionInfo.execute()
                                            } else {
                                                val intent = Intent(this@PhoneMapsView, FetchAddressIntentService::class.java)
                                                intent.action = "LocationData"
                                                intent.putExtra(FetchAddressIntentService.Constants.RESULT_RECEIVER, this)
                                                intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
                                                startService(intent)
                                            }
                                        }
                                        FetchAddressIntentService.Constants.FAILURE_RESULT -> {
                                            FunctionsClassDebug.PrintDebug("*** FetchAddressIntentService.Constants.FAILURE_RESULT == ${geoDecodeAttempt} ***")

                                            if (geoDecodeAttempt <= 13) {
                                                val intent = Intent(this@PhoneMapsView, FetchAddressIntentService::class.java)
                                                intent.action = "LocationData"
                                                intent.putExtra(FetchAddressIntentService.Constants.RESULT_RECEIVER, this)
                                                intent.putExtra(FetchAddressIntentService.Constants.LOCATION_DATA_EXTRA, location)
                                                startService(intent)
                                            } else {
                                                if (!googleMapReady) {
                                                    startActivity(Intent(applicationContext, PhoneMapsView::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                                            ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
                                                    this@PhoneMapsView.finish()
                                                }
                                            }

                                            geoDecodeAttempt++
                                        }
                                    }
                                }
                            }

                            val intent = Intent(this@PhoneMapsView, FetchAddressIntentService::class.java)
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

                        firebaseFunctions
                                .getHttpsCallable("fetchUserPublicInternetAddress")
                                .call()
                                .continueWith { task ->
                                    val resultToContinueWith = task.result?.data as Map<String, Any>

                                    resultToContinueWith["ClientAddressIP"] as String

                                }.addOnSuccessListener {
                                    PublicVariable.PUBLIC_INTERNET_IP = it
                                    FunctionsClassDebug.PrintDebug("*** IP Address ${PublicVariable.PUBLIC_INTERNET_IP}")
                                }.addOnFailureListener {
                                    FunctionsClassSystemCheckpoint(applicationContext).GetNetworkAddressIP()
                                }
                    } else {
                        FunctionsClassDebug.PrintDebug("*** ${checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)} ***")
                    }
                }
            }, null)
        } else {

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE), 111)
        }

        firebaseFunctions = FirebaseFunctions.getInstance()
        firebaseUser = FirebaseAuth.getInstance()!!.currentUser!!

        if (functionsClass.networkConnection()) {
            val billingClient = BillingClient.newBuilder(this@PhoneMapsView).setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {

                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {

                } else {

                }
            }.enablePendingPurchases().build()
            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResponseCode: BillingResult) {
                    if (billingResponseCode.responseCode == BillingClient.BillingResponseCode.OK) {
                        functionsClass.savePreference(".SubscribedItem", "cloud.backup", false)
                        functionsClass.savePreference(".SubscribedItem", "donation.subscription", false)

                        val purchasesSubscriptions = billingClient.queryPurchases(BillingClient.SkuType.SUBS).purchasesList
                        FunctionsClassDebug.PrintDebug("*** ${purchasesSubscriptions.size} ***")

                        for (purchase in purchasesSubscriptions) {
                            FunctionsClassDebug.PrintDebug("*** Purchased Item: $purchase ***")

                            //"donation.subscription", "cloud.backup"
                            if (purchase.sku == "donation.subscription") {
                                functionsClass.savePreference(".SubscribedItem", "donation.subscription", true)
                                if (!purchase.isAutoRenewing) {
                                    val currentTime = System.currentTimeMillis()
                                    val sixMonth: Long = 15552000000//86400000*30
                                    if ((currentTime - purchase.purchaseTime) > sixMonth) {
                                        FunctionsClassDebug.PrintDebug("*** Purchases Subscriptions ::: Cancelled Completely***")

                                        functionsClass.savePreference(".SubscribedItem", "donation.subscription", false)
                                    } else {
                                        FunctionsClassDebug.PrintDebug("*** Purchases Subscriptions ::: Not Auto Renew ***")
                                    }
                                }
                            }

                            if (purchase.sku == "cloud.backup") {
                                functionsClass.savePreference(".SubscribedItem", "cloud.backup", true)
                                if (!purchase.isAutoRenewing) {
                                    val currentTime = System.currentTimeMillis()
                                    val oneMonth: Long = 2592000000//86400000*30
                                    if ((currentTime - purchase.purchaseTime) > oneMonth) {
                                        FunctionsClassDebug.PrintDebug("*** Purchases Subscriptions ::: Cancelled Completely***")

                                        functionsClass.savePreference(".SubscribedItem", "cloud.backup", false)
                                    } else {
                                        FunctionsClassDebug.PrintDebug("*** Purchases Subscriptions ::: Not Auto Renew ***")
                                    }
                                }
                            }
                        }

                        if (functionsClass.cloudBackupSubscribed()) {
                            launchSynchronizationProcess(true)
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {

                }
            })
        }

        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        MobileAds.initialize(applicationContext, getString(R.string.ad_app_id))
        adRequestInterstitial = AdRequest.Builder()
                .addTestDevice("CDCAA1F20B5C9C948119E886B31681DE")
                .addTestDevice("D101234A6C1CF51023EE5815ABC285BD")
                .addTestDevice("65B5827710CBE90F4A99CE63099E524C")
                .addTestDevice("DD428143B4772EC7AA87D1E2F9DA787C")
                .addTestDevice("5901E5EE74F9B6652E05621140664A54")
                .addTestDevice("3E192B3766F6EDE8127A5ADFAA0E7B67")
                .addTestDevice("F54D998BCE077711A17272B899B44798")
                .build()
        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = getString(R.string.AdUnitMapsView)
        interstitialAd.setImmersiveMode(true)
        if (interstitialAd.isLoaded) {
            FunctionsClassDebug.PrintDebug("*** InterstitialAd Loaded >>> Show ***")
            interstitialAd.show()
        } else {
            FunctionsClassDebug.PrintDebug("*** InterstitialAd Loading ***")
            interstitialAd.loadAd(adRequestInterstitial)
        }
        interstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                if (PublicVariable.eligibleToLoadShowAds && googleMapReady) {
                    interstitialAd.show()
                }
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                if (PublicVariable.eligibleToLoadShowAds) {
                    interstitialAd.loadAd(adRequestInterstitial)
                }
                FunctionsClassDebug.PrintDebug("$$$ Ads Error ${errorCode} $$$")
            }

            override fun onAdOpened() {

            }

            override fun onAdLeftApplication() {

            }

            override fun onAdClosed() {

            }
        }

        firestoreDatabase = FirebaseFirestore.getInstance()
        try {
            val firestoreSettings: FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .setSslEnabled(true)
                    .build()
            firestoreDatabase.firestoreSettings = firestoreSettings
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val registrationTokenDatabaseReference = FirebaseDatabase.getInstance().reference
                    .child("AuthUsers")
                    .child("Private")
                    .child("RegistrationToken")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid)

            FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
                registrationTokenDatabaseReference.setValue(it.token)
                functionsClassPreferences.savePreference(".UserInformation", "RegistrationToken", it.token)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        if (File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)!!.path, File.separator + "PinPicsOnMap").exists()
                && functionsClassPreferences.readDefaultPreference("OldUser", true) as Boolean) {
            FunctionsClassDebug.PrintDebug("*** Extrating Old User Data ***")

            val extractDataOfExistenceUsers: ExtractDataOfExistenceUsers = ExtractDataOfExistenceUsers()
            extractDataOfExistenceUsers.execute()
        } else {
            functionsClassPreferences.saveDefaultPreference("OldUser", false)
        }
    }

    public override fun onStart() {
        super.onStart()
        if (functionsClass.cloudBackupSubscribed() || BuildConfig.DEBUG) {
            syncCloud.setBackgroundColor(getColor(R.color.default_color_darker_transparent))
            syncCloud.iconTint = ColorStateList.valueOf(getColor(R.color.light))
            syncCloud.text = ""
        }

        syncCloud.setOnClickListener {
            if (functionsClass.networkConnection()) {
                if (functionsClass.cloudBackupSubscribed() || BuildConfig.DEBUG) {
                    launchSynchronizationProcess(false)
                } else {
                    startActivity(Intent(applicationContext, InAppBilling::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
                }
            }
        }

        chatWithPeople.setOnClickListener {
            startActivity(Intent(applicationContext, MessengerGroup::class.java).apply {
                putExtra("CountryName", PublicVariable.LOCATION_COUNTRY_NAME)
                putExtra("CityName", initMarker.snippet)
            }, ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
        }

        chatWithPeopleHistory.setOnClickListener {
            startActivity(Intent(applicationContext, ChatHistory::class.java).apply {
                putExtra("CountryName", PublicVariable.LOCATION_COUNTRY_NAME)
                putExtra("CityName", initMarker.snippet)
            }, ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
        }

        currentLocation.setOnClickListener { view ->
            Latitude = coordinates.getLatitude()
            Longitude = coordinates.getLongitude()
            LocationXY = coordinates.locationLatitudeLongitude

            initMarker.remove()
            var drawableCurrent = 0
            if (time == "day") {
                drawableCurrent = R.drawable.ic_pin_current_day
            } else if (time == "night") {
                drawableCurrent = R.drawable.ic_pin_current_night
            }

            val tempBitmapCurrent = BitmapFactory.decodeResource(resources, drawableCurrent)
            val bitmapScaleCurrent = Bitmap.createScaledBitmap(tempBitmapCurrent, tempBitmapCurrent.width / 4, tempBitmapCurrent.height / 4, false)
            val bitmapDescriptorCurrent = BitmapDescriptorFactory.fromBitmap(bitmapScaleCurrent)
            try {
                initMarker = googleMap.addMarker(
                        MarkerOptions()
                                .position(LocationXY)
                                .title(functionsClass.locationDetail())
                                .snippet(functionsClass.locationCityName())
                                .draggable(false)
                                .icon(bitmapDescriptorCurrent)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

            view.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_location_button))
            val cameraUpdateFactory = CameraUpdateFactory
                    .newLatLngZoom(LocationXY, 15f)
            googleMap.animateCamera(cameraUpdateFactory)
        }

        captureButton.setOnClickListener {
            val filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path,
                    File.separator
                            + "PinPicsOnMap"
                            + File.separator
                            + "(${clickedMarker.position.latitude},${clickedMarker.position.longitude})")
            filePath.mkdirs()
            locationImagePath = (clickedMarker.snippet + "_" + "(" + clickedMarker.position.latitude + "," + clickedMarker.position.longitude + ")"
                    + "_" + System.currentTimeMillis() + "." + Bitmap.CompressFormat.JPEG)
            FunctionsClassDebug.PrintDebug("*** ${locationImagePath} ***")

            val newFile = File(filePath, locationImagePath)
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile))
            takePictureIntent.putExtra(MediaStore.EXTRA_FULL_SCREEN, false)
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                Handler().postDelayed({
                    startActivityForResult(takePictureIntent, 666)
                }, 50)
            }
        }

        pickGallery.setOnClickListener {
            startActivity(Intent(applicationContext, AddToMap::class.java),
                    ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
        }

        switchCamera.setOnClickListener {
            when (switchPhase) {
                0 -> {
                    functionsClass.tiltCameraInPosition(googleMap, true)

                    switchPhase = 1
                }
                1 -> {
                    functionsClass.tiltCameraInPosition(googleMap, false)

                    switchPhase = 0
                }
            }
        }

        val syncClickHandler = Handler()
        var syncClickRunnable: Runnable? = null
        syncCloud.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    syncClickRunnable = Runnable {
                        syncCloud.text = getString(R.string.cloudBackup)
                        //syncCloud.layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
                    }
                    syncClickHandler.postDelayed(syncClickRunnable, 333)
                }
                MotionEvent.ACTION_UP -> {
                    syncCloud.text = ""
                    syncClickHandler.removeCallbacks(syncClickRunnable)
                }
            }
            false
        }

        val chatClickHandler = Handler()
        var chatClickRunnable: Runnable? = null
        chatWithPeople.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    chatClickRunnable = Runnable {
                        chatWithPeople.text = getString(R.string.chatWithPeople)
                    }
                    chatClickHandler.postDelayed(chatClickRunnable, 333)
                }
                MotionEvent.ACTION_UP -> {
                    chatWithPeople.text = ""
                    chatClickHandler.removeCallbacks(chatClickRunnable)
                }
            }
            false
        }

        val chatHistoryClickHandler = Handler()
        var chatHistoryClickRunnable: Runnable? = null
        chatWithPeopleHistory.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    chatHistoryClickRunnable = Runnable {
                        chatWithPeopleHistory.text = getString(R.string.chatWithPeopleHistory)
                    }
                    chatHistoryClickHandler.postDelayed(chatHistoryClickRunnable, 333)
                }
                MotionEvent.ACTION_UP -> {
                    chatWithPeopleHistory.text = ""
                    chatHistoryClickHandler.removeCallbacks(chatHistoryClickRunnable)
                }
            }
            false
        }

        val intentFilter: IntentFilter = IntentFilter()
        intentFilter.addAction("RELOAD_WEATHER")
        val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent!!.action == "RELOAD_WEATHER") {
                    val weatherConditionInfo: WeatherConditionInfo = WeatherConditionInfo()
                    weatherConditionInfo.execute()
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
                            FunctionsClassDebug.PrintDebug("*** Firebase Remote Configuration Fetched Successfully ***")

                            if (firebaseRemoteConfig.getLong(functionsClass.versionCodeRemoteConfigKey()) > BuildConfig.VERSION_CODE) {
                                functionsClass.notificationCreator(
                                        getString(R.string.updateAvailable),
                                        firebaseRemoteConfig.getString(functionsClass.upcomingChangeLogSummaryConfigKey()),
                                        firebaseRemoteConfig.getLong(functionsClass.versionCodeRemoteConfigKey()).toInt()
                                )
                            }

                            if (firebaseRemoteConfig.getLong(getString(R.string.newDataEmail)) > functionsClassPreferences.readPreference(".PromotionEmail", "FetchTime", 0.toLong()).toString().toLong()) {

                                val linkedHashMapData: LinkedHashMap<Any, Any> = LinkedHashMap<Any, Any>()
                                linkedHashMapData["UserUID"] = firebaseUser.uid
                                linkedHashMapData["UserEmail"] = firebaseUser.email.toString()
                                linkedHashMapData["Username"] = firebaseUser.displayName.toString()
                                linkedHashMapData["CurrentCountry"] = PublicVariable.LOCATION_COUNTRY_NAME.toString()
                                linkedHashMapData["CurrentCity"] = PublicVariable.LOCATION_CITY_NAME.toString()
                                linkedHashMapData["Latitude"] = Latitude
                                linkedHashMapData["Longitude"] = Longitude
                                linkedHashMapData["KnownName"] = PublicVariable.LOCATION_KNOWN_NAME.toString()

                                firebaseFunctions
                                        .getHttpsCallable("sendPromotionEmail")
                                        .call(linkedHashMapData)
                                        .addOnSuccessListener {
                                            FunctionsClassDebug.PrintDebug("*** Email Cloud Functions Was Successful ***")
                                        }.addOnFailureListener {
                                            FunctionsClassDebug.PrintDebug("*** Email Cloud Functions Failed ***")
                                        }
                                        .continueWith { task ->
                                            val resultToContinueWith = task.result?.data //as Map<String, Any>

                                            FunctionsClassDebug.PrintDebug("*** ${linkedHashMapData} ***")

                                            //resultToContinueWith["SomeKey"] as String

                                        }.addOnSuccessListener {

                                        }.addOnFailureListener {

                                        }

                                functionsClassPreferences.savePreference(".PromotionEmail", "FetchTime", firebaseRemoteConfig.getLong(getString(R.string.newDataEmail)))
                            }
                        }
                    } else {

                    }
                }
    }

    public override fun onResume() {
        super.onResume()
        PublicVariable.eligibleToLoadShowAds = true

        if ((MessengerGroup.countryName != null) && (MessengerGroup.cityName != null) && googleMapReady) {
            if (functionsClass.readPreference(".AlreadyBeenHere", (MessengerGroup.countryName + "-" + MessengerGroup.cityName), false)) {
                chatWithPeopleHistory.visibility = View.VISIBLE

                /*User States | Online*/
                try {
                    val intent = Intent(applicationContext, UserStatesIntentService::class.java).apply {
                        action = "ACTION_ONLINE"
                        putExtra("UserChatType", true)
                        putExtra("CountryName", MessengerGroup.countryName)
                        putExtra("CityName", MessengerGroup.cityName)
                        putExtra("ChatName", MessengerGroup.countryName + "-" + MessengerGroup.cityName)
                        putExtra("UID", firebaseUser.uid)
                    }
                    startService(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        PublicVariable.eligibleToLoadShowAds = false

        /*User States | Offline*/
        try {
            val intent = Intent(applicationContext, UserStatesIntentService::class.java).apply {
                action = "ACTION_OFFLINE"
                putExtra("UserChatType", true)
                putExtra("CountryName", functionsClass.locationCountryName())
                putExtra("CityName", functionsClass.locationCityName())
                putExtra("ChatName", functionsClass.locationCountryName() + "-" + functionsClass.locationCityName())
                putExtra("UID", firebaseUser.uid)
            }
            startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 666 && resultCode == Activity.RESULT_OK) {
            FunctionsClassDebug.PrintDebug("*** ${locationImagePath} ***")

            functionsClass.savePreference("(${clickedMarker.position.latitude},${clickedMarker.position.longitude})", "LocationDetail", clickedMarker.title)
            functionsClass.savePreference("(${clickedMarker.position.latitude},${clickedMarker.position.longitude})", "CityName", clickedMarker.snippet)

            val filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path,
                    File.separator
                            + "PinPicsOnMap"
                            + File.separator
                            + "(${clickedMarker.position.latitude},${clickedMarker.position.longitude})")
            val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            val file = File(filePath, locationImagePath)
            val contentUri = Uri.fromFile(file)
            mediaScanIntent.data = contentUri
            this.sendBroadcast(mediaScanIntent)

            if (file.exists()) {
                clickedMarker.remove()

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
                    googleMap.addMarker(
                            MarkerOptions()
                                    .position(LatLng(clickedMarker.position.latitude, clickedMarker.position.longitude))
                                    .title(clickedMarker.title)
                                    .snippet(clickedMarker.snippet)
                                    .draggable(false)
                                    .icon(bitmapDescriptorCurrent)
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                if (getFileStreamPath(".Locations").exists()) {
                    val lines = functionsClass.readFileLine(".Locations")
                    var lineExist = false
                    for (line in lines) {
                        if (line == "(${clickedMarker.position.latitude},${clickedMarker.position.longitude})") {
                            lineExist = true

                            break
                        }
                    }
                    if (!lineExist) {
                        functionsClass.saveFileAppendLine(".Locations", "(${clickedMarker.position.latitude},${clickedMarker.position.longitude})")
                    }
                } else {
                    functionsClass.saveFileAppendLine(".Locations", "(${clickedMarker.position.latitude},${clickedMarker.position.longitude})")
                }

                if (getFileStreamPath(".Details").exists()) {
                    val lines = functionsClass.readFileLine(".Details")
                    var lineExist = false
                    for (line in lines) {
                        if (line == "(${clickedMarker.position.latitude},${clickedMarker.position.longitude})") {
                            lineExist = true

                            break
                        }
                    }
                    if (!lineExist) {
                        functionsClass.saveFileAppendLine(".Details", functionsClass.readPreference("(${clickedMarker.position.latitude},${clickedMarker.position.longitude})", "LocationDetail", "Unknown").toString())
                    }
                } else {
                    functionsClass.saveFileAppendLine(".Details", functionsClass.readPreference("(${clickedMarker.position.latitude},${clickedMarker.position.longitude})", "LocationDetail", "Unknown").toString())
                }

                Handler().postDelayed(Runnable {
                    if (getFileStreamPath(".Locations").exists()) {
                        functionsClass.sendLocationsData(this@PhoneMapsView, LOCATIONS_PATH, LOCATIONS_KEY)

                        Handler().postDelayed({
                            functionsClass.sendLocationsDetails(this@PhoneMapsView, PREFERENCES_PATH, PREFERENCES_KEY)
                        }, 1333)
                    }
                }, 1333)

                val picsCounter: Int = functionsClassPreferences.readPreference(".SavedDataInfo", "LastPicsCount", 0).toString().toInt()
                functionsClassPreferences.savePreference(".SavedDataInfo", "LastPicsCount", picsCounter + 1)
            }
        }
    }

    /*Map Functions*/
    override fun onMapReady(readyGoogleMap: GoogleMap) {
        if (interstitialAd.isLoaded && PublicVariable.eligibleToLoadShowAds) {
            interstitialAd.show()
        }

        googleMap = readyGoogleMap
        googleMapReady = true

        currentLocation.visibility = View.VISIBLE
        switchCamera.visibility = View.VISIBLE

        readyGoogleMap.setOnMapClickListener(this@PhoneMapsView)
        readyGoogleMap.setOnMapLongClickListener(this@PhoneMapsView)
        readyGoogleMap.setOnMarkerClickListener(this@PhoneMapsView)
        readyGoogleMap.setOnInfoWindowClickListener(this@PhoneMapsView)
        readyGoogleMap.setOnInfoWindowLongClickListener(this@PhoneMapsView)
        readyGoogleMap.setOnInfoWindowCloseListener(this@PhoneMapsView)

        readyGoogleMap.uiSettings.isCompassEnabled = false
        readyGoogleMap.uiSettings.isZoomControlsEnabled = false
        readyGoogleMap.uiSettings.setAllGesturesEnabled(true)
        readyGoogleMap.uiSettings.isMapToolbarEnabled = false

        readyGoogleMap.isTrafficEnabled = false
        readyGoogleMap.mapType = GoogleMap.MAP_TYPE_HYBRID

        //instagram marker
        val bitmapInstagram = BitmapFactory.decodeResource(resources, R.drawable.ic_instagram)
        val bitmapChangedInstagram = Bitmap.createScaledBitmap(bitmapInstagram, bitmapInstagram.width / 5, bitmapInstagram.height / 5, false)
        val bitmapDescriptorInstagram = BitmapDescriptorFactory.fromBitmap(bitmapChangedInstagram)
        try {
            instagramMarker = readyGoogleMap.addMarker(
                    MarkerOptions()
                            .position(LatLng(LocationXY.latitude + 0.005357, LocationXY.longitude + 0.0))//LocationXY
                            .title("Instagram")
                            .snippet(getString(R.string.link_instagram))
                            .draggable(true)
                            .icon(bitmapDescriptorInstagram)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //init marker
        var drawableCurrent = 0
        if (time == "day") {
            drawableCurrent = R.drawable.ic_pin_current_day
        } else if (time == "night") {
            drawableCurrent = R.drawable.ic_pin_current_night
        }
        val tempBitmapCurrent = BitmapFactory.decodeResource(resources, drawableCurrent)
        val bitmapScaleCurrent = Bitmap.createScaledBitmap(tempBitmapCurrent, tempBitmapCurrent.width / 4, tempBitmapCurrent.height / 4, false)
        val bitmapDescriptorCurrent = BitmapDescriptorFactory.fromBitmap(bitmapScaleCurrent)
        try {
            initMarker = readyGoogleMap.addMarker(
                    MarkerOptions()
                            .position(LocationXY)
                            .title(functionsClass.locationDetail())
                            .snippet(functionsClass.locationCityName())
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
        if (getFileStreamPath(".Locations").exists()) {
            val saveLocations = functionsClass.readFileLine(".Locations")
            for (markerLocationInfo in saveLocations) {
                FunctionsClassDebug.PrintDebug("*** Pinned Location ::: ${markerLocationInfo} ***")

                var markerLocation = markerLocationInfo
                markerLocation = markerLocation.replace("(", "")
                markerLocation = markerLocation.replace(")", "")
                val positionOnMap = markerLocation.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                val latitude = java.lang.Double.valueOf(positionOnMap[0])
                val longitude = java.lang.Double.valueOf(positionOnMap[1])
                val latLng = LatLng(latitude, longitude)

                val filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path,
                        File.separator
                                + "PinPicsOnMap"
                                + File.separator
                                + "($latitude,$longitude)")
                val markerFiles = filePath.listFiles()
                if (filePath.exists() && markerFiles.size > 0) {
                    FunctionsClassDebug.PrintDebug("*** ${functionsClass.readPreference(markerLocationInfo, "LocationDetail", PublicVariable.LOCATION_INFO_DETAIL!!)} | ${functionsClass.readPreference(markerLocationInfo, "CityName", PublicVariable.LOCATION_INFO_DETAIL!!)} ***")

                    readyGoogleMap.addMarker(
                            MarkerOptions()
                                    .position(latLng)
                                    .title(functionsClass.readPreference(markerLocationInfo, "LocationDetail", PublicVariable.LOCATION_INFO_DETAIL!!))//Location Detail
                                    .snippet(functionsClass.readPreference(markerLocationInfo, "CityName", PublicVariable.LOCATION_INFO_DETAIL!!))//City Name
                                    .draggable(false)
                                    .icon(bitmapDescriptorSaved)
                    )
                } else {
                    functionsClass.removeLine(".Locations", markerLocationInfo)
                    functionsClass.removeLine(".Details", functionsClass.readPreference("($Latitude,$Longitude)", "LocationDetail", "Unknown").toString())
                }
            }
            Handler().postDelayed(Runnable {
                if (getFileStreamPath(".Locations").exists()) {
                    functionsClass.sendLocationsData(this@PhoneMapsView, LOCATIONS_PATH, LOCATIONS_KEY)

                    Handler().postDelayed({
                        functionsClass.sendLocationsDetails(this@PhoneMapsView, PREFERENCES_PATH, PREFERENCES_KEY)
                    }, 1333)
                }
            }, 1333)
        }

        infoWindowView = layoutInflater.inflate(R.layout.location_info_window, null)
        readyGoogleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(marker: Marker): View {
                cityName = marker.snippet
                val filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path,
                        File.separator
                                + "PinPicsOnMap"
                                + File.separator
                                + ("(" + marker.position.latitude + "," + marker.position.longitude + ")"))
                val newFiles = filePath.listFiles()
                if (newFiles != null) {
                    createInfoView(infoWindowView, newFiles, marker)
                }
                return infoWindowView
            }

            override fun getInfoContents(marker: Marker): View? {
                return null
            }
        })

        Handler().postDelayed({
            val cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(LocationXY, 15f)
            readyGoogleMap.moveCamera(cameraUpdateFactory)
            currentLocation.isEnabled = true
        }, 250)

        Handler().postDelayed({
            syncCloud.visibility = View.VISIBLE
            chatWithPeople.visibility = View.VISIBLE
            if (functionsClass.readPreference(".AlreadyBeenHere", (functionsClass.locationCountryName() + "-" + functionsClass.locationCityName()), false)) {
                chatWithPeopleHistory.visibility = View.VISIBLE
            }
        }, 1000)

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

        /*User States | Online*/
        try {
            val intent = Intent(applicationContext, UserStatesIntentService::class.java).apply {
                action = "ACTION_ONLINE"
                putExtra("UserChatType", true)
                putExtra("CountryName", functionsClass.locationCountryName())
                putExtra("CityName", functionsClass.locationCityName())
                putExtra("ChatName", functionsClass.locationCountryName() + "-" + functionsClass.locationCityName())
                putExtra("UID", firebaseUser.uid)
            }
            startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
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
    }

    override fun onInfoWindowClick(marker: Marker) {
        var filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path,
                File.separator
                        + "PinPicsOnMap"
                        + File.separator
                        + ("(" + marker.position.latitude + "," + marker.position.longitude + ")"))
        val newFiles = filePath.listFiles()
        var newFile: File
        if (newFiles != null) {
            if (newFiles.isNotEmpty()) {
                FunctionsClassDebug.PrintDebug("*** ${filePath} | ${Uri.fromFile(filePath)} ***")
                newFile = newFiles[0]

                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(Uri.fromFile(newFile), "image/*")
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                startActivity(intent)
            }
        } else {
            cityName = marker.snippet
            filePath = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path,
                    File.separator
                            + "PinPicsOnMap"
                            + File.separator
                            + "(${clickedMarker.position.latitude},${clickedMarker.position.longitude})")
            filePath.mkdirs()
            locationImagePath = (clickedMarker.snippet + "_" + "(" + clickedMarker.position.latitude + "," + clickedMarker.position.longitude + ")"
                    + "_" + System.currentTimeMillis() + "." + Bitmap.CompressFormat.JPEG)
            FunctionsClassDebug.PrintDebug("*** ${locationImagePath} ***")

            newFile = File(filePath, locationImagePath)
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile))
            takePictureIntent.putExtra(MediaStore.EXTRA_FULL_SCREEN, false)
            takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                startActivityForResult(takePictureIntent, 666)
            }
        }
    }

    override fun onInfoWindowLongClick(marker: Marker) {
        val street = Intent(applicationContext, SplitStreetViewMap::class.java)
        street.putExtra("LocationX", marker.position.latitude)
        street.putExtra("LocationY", marker.position.longitude)
        street.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(street)
    }

    override fun onInfoWindowClose(marker: Marker) {
        val weatherConditionInfo: WeatherConditionInfo = WeatherConditionInfo()
        weatherConditionInfo.execute()

        if (captureButton.isShown()) {
            val slideDown = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down)
            captureButton.startAnimation(slideDown)
            slideDown.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    captureButton.visibility = View.INVISIBLE

                    pickGallery.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down))
                    pickGallery.visibility = View.INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
        }

        try {
            savedImage.removeAllViews()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMapClick(latLng: LatLng) {
        if (captureButton.isShown()) {
            val slideDown = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down)
            captureButton.startAnimation(slideDown)
            slideDown.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    captureButton.visibility = View.INVISIBLE

                    pickGallery.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_down))
                    pickGallery.visibility = View.INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
        }

        Handler().postDelayed({
            syncCloud.visibility = View.VISIBLE
            chatWithPeople.visibility = View.VISIBLE
        }, 1000)
    }

    override fun onMapLongClick(latLng: LatLng) {

    }

    override fun onMarkerClick(marker: Marker): Boolean {
        syncCloud.visibility = View.INVISIBLE
        chatWithPeople.visibility = View.INVISIBLE
        chatWithPeopleHistory.visibility = View.INVISIBLE

        clickedMarker = marker

        if (clickedMarker.title == instagramMarker.title) {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_instagram)))
            startActivity(intent)
        } else {
            val slideUp = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_up)
            captureButton.startAnimation(slideUp)
            slideUp.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    captureButton.visibility = View.VISIBLE

                    pickGallery.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.slide_up))
                    pickGallery.visibility = View.VISIBLE

                    Handler().postDelayed({
                        saveImageLoading.visibility = View.INVISIBLE
                    }, 700)
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })

            saveImageLoading.setColor(if (time == "day") {
                getColor(R.color.light)
            } else /*if (time == "night")*/ {
                getColor(R.color.red)
            })
            saveImageLoading.visibility = View.VISIBLE

            Handler().postDelayed({
                val cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(LatLng(marker.position.latitude + 0.00357, marker.position.longitude), 15f)
                googleMap.animateCamera(cameraUpdateFactory)
            }, 1500)
        }

        return false
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {

    }

    private fun createInfoView(view: View, locationImageFile: Array<File?>, marker: Marker) {
        try {
            FunctionsClassDebug.PrintDebug("*** " + coordinates.latitude + " | " + coordinates.longitude + " ***")
            FunctionsClassDebug.PrintDebug("*** " + PublicVariable.LOCATION_INFO_DETAIL + " ***")

            if (locationImageFile != null) {
                val layoutParams = LinearLayout.LayoutParams(functionsClass.DpToInteger(99), functionsClass.DpToInteger(99))
                savedImage = view.findViewById<CircularRevealGridLayout>(R.id.savedImage)
                savedImage.setBackgroundColor(if (time == "day") {
                    getColor(R.color.light)
                } else {
                    getColor(R.color.dark)
                })
                try {
                    savedImage.removeAllViews()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                for ((index, file) in locationImageFile.withIndex()) {
                    if (index > 4) {
                        break
                    }
                    if (file!!.exists()) {
                        val imageView = ImageView(applicationContext)
                        imageView.layoutParams = layoutParams
                        imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                        val imageLocation = BitmapFactory.decodeStream(FileInputStream(file))
                        imageView.setImageBitmap(imageLocation)

                        savedImage.addView(imageView)
                    }
                }

                val textView = TextView(applicationContext)
                if (savedImage.childCount % 2 == 0) {
                    textView.layoutParams = LinearLayout.LayoutParams(functionsClass.DpToInteger(99), LinearLayout.LayoutParams.WRAP_CONTENT)
                } else {
                    textView.layoutParams = layoutParams
                }
                textView.setPadding(3, 13, 3, 13)
                textView.gravity = Gravity.CENTER
                textView.setTextColor(if (time == "day") {
                    getColor(R.color.dark)
                } else {
                    getColor(R.color.light)
                })
                textView.text = getString(R.string.clickToGallery)

                savedImage.addView(textView)
            }

            val locationInfo = clickedMarker.title

            weatherInfoView.text = locationInfo
            weatherIconView.setPadding(19, 19, 19, 19)
            weatherIconView.setImageDrawable(getDrawable(R.drawable.instagram_icon))

            weatherInfoView.setOnClickListener {
                val Search = Intent(Intent.ACTION_WEB_SEARCH)
                Search.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                Search.putExtra(SearchManager.QUERY, getString(R.string.bestPlaceAround) + " " + locationInfo)
                startActivity(Search)
            }

            weatherIconView.setOnClickListener {
                triggerCameraIdle = true
                val cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(LatLng(marker.position.latitude + 0.006135, marker.position.longitude), 15f)
                googleMap.animateCamera(cameraUpdateFactory)
                googleMap.setOnCameraIdleListener {
                    if (triggerCameraIdle) {
                        triggerCameraIdle = false
                        //instagram marker
                        try {
                            instagramMarker.remove()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                        val bitmapInstagram = BitmapFactory.decodeResource(resources, R.drawable.ic_instagram)
                        val bitmapChangedInstagram = Bitmap.createScaledBitmap(bitmapInstagram, bitmapInstagram.width / 4, bitmapInstagram.height / 4, false)
                        val bitmapDescriptorInstagram = BitmapDescriptorFactory.fromBitmap(bitmapChangedInstagram)
                        try {
                            instagramMarker = googleMap.addMarker(
                                    MarkerOptions()
                                            .position(LatLng(marker.position.latitude + 0.013111, marker.position.longitude + 0.0))
                                            .title("Instagram")
                                            .snippet(getString(R.string.link_instagram))
                                            .draggable(false)
                                            .icon(bitmapDescriptorInstagram)
                            )

                            //- 0.004111
                            val layerDrawableIcon = getDrawable(R.drawable.draw_weather_background) as LayerDrawable
                            val layerDrawableBackgroundIcon = layerDrawableIcon.findDrawableByLayerId(R.id.backtemp) as GradientDrawable
                            if (time == "day") {
                                layerDrawableBackgroundIcon.setColor(getColor(R.color.light_transparent))
                            } else {
                                layerDrawableBackgroundIcon.setColor(getColor(R.color.dark_transparent))
                            }
                            val iconGenerator: IconGenerator = IconGenerator(this@PhoneMapsView)
                            iconGenerator.setBackground(layerDrawableBackgroundIcon)
                            val bitmapIcon: Bitmap = iconGenerator
                                    .makeIcon(
                                            Html.fromHtml(
                                                    "<b><small><font color='" + if (time == "day") {
                                                        layerDrawableBackgroundIcon.setColor(getColor(R.color.dark))
                                                    } else {
                                                        layerDrawableBackgroundIcon.setColor(getColor(R.color.light))
                                                    } + "'>"
                                                            + PublicVariable.LOCATION_INFO_DETAIL
                                                            + "</font></small></b>"
                                            )
                                    )
                            val bitmapDescriptor: BitmapDescriptor = BitmapDescriptorFactory.fromBitmap(bitmapIcon)
                            locationDetailMarker = googleMap.addMarker(
                                    MarkerOptions()
                                            .position(LatLng(marker.position.latitude - 0.003111, marker.position.longitude + 0.0))
                                            .title(clickedMarker.title)
                                            .snippet(clickedMarker.snippet)
                                            .draggable(false)
                                            .icon(bitmapDescriptor)
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        captureButton.visibility = View.INVISIBLE
                        pickGallery.visibility = View.INVISIBLE

                        Handler().postDelayed({
                            var bitmap: Bitmap? = null
                            var snapshotReadyCallback: GoogleMap.SnapshotReadyCallback = GoogleMap.SnapshotReadyCallback { snapshot ->
                                bitmap = snapshot

                                try {
                                    val filePath = Environment.getExternalStorageDirectory().toString() + "/PinPicsOnMapScreenshot_" + System.currentTimeMillis() + ".JPG"
                                    val imageFile = File(filePath)
                                    val fileOutputStream = FileOutputStream(imageFile)

                                    bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream)

                                    fileOutputStream.flush()
                                    fileOutputStream.close()

                                    Handler().postDelayed({
                                        var intent = packageManager.getLaunchIntentForPackage("com.instagram.android")
                                        if (intent != null) {
                                            val shareIntent = Intent()
                                            shareIntent.action = Intent.ACTION_SEND
                                            //    shareIntent.setPackage("com.instagram.android")
                                            try {
                                                shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(MediaStore.Images.Media.insertImage(contentResolver, filePath, "@PinPicsOnMap", "#SaveEart #SaveNature")))
                                                shareIntent.putExtra(Intent.EXTRA_TEXT, "@PinPicsOnMap" + " 🌎 🌏 🌍  " + "#SaveEart #SaveNature");
                                            } catch (e: FileNotFoundException) {
                                                e.printStackTrace()
                                            }
                                            shareIntent.type = "image/*"
                                            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            startActivity(shareIntent)
                                        } else {
                                            intent = Intent(Intent.ACTION_VIEW)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            intent.data = Uri.parse("market://details?id=" + "com.instagram.android")
                                            startActivity(intent)
                                        }

                                        val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                                        val file = File(filePath)
                                        val contentUri = Uri.fromFile(file)
                                        mediaScanIntent.data = contentUri
                                        sendBroadcast(mediaScanIntent)

                                        val bitmapInstagramDefault = BitmapFactory.decodeResource(resources, R.drawable.ic_instagram)
                                        val bitmapChangedInstagramDefault = Bitmap.createScaledBitmap(bitmapInstagramDefault, bitmapInstagramDefault.width / 5, bitmapInstagramDefault.height / 5, false)
                                        val bitmapDescriptorInstagramDefault = BitmapDescriptorFactory.fromBitmap(bitmapChangedInstagramDefault)
                                        try {
                                            instagramMarker.remove()
                                            instagramMarker = googleMap.addMarker(
                                                    MarkerOptions()
                                                            .position(LatLng(LocationXY.latitude + 0.005357, LocationXY.longitude + 0.0))//LocationXY
                                                            .title("Instagram")
                                                            .snippet(getString(R.string.link_instagram))
                                                            .draggable(true)
                                                            .icon(bitmapDescriptorInstagramDefault)
                                            )
                                            locationDetailMarker.remove()
                                        } catch (e: Exception) {
                                            e.printStackTrace()
                                        }
                                    }, 333)
                                } catch (e: FileNotFoundException) {
                                    e.printStackTrace()
                                } catch (e: IOException) {
                                    e.printStackTrace()
                                }
                            }
                            googleMap.snapshot(snapshotReadyCallback)
                        }, 555)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    fun indexAppInfo(cityName: String, temperature: String, conditionIconUrl: String, hrs: Int, mins: Int) {
        FirebaseAppIndex.getInstance().removeAll()

        getCity = "${cityName} | ${temperature}°ᶜ | ${hrs}:${mins}"
        getUrl = BASE_URL.buildUpon().appendPath(getCity).build().toString()

        val articleToIndex = Indexable.Builder()
                .setName(getCity)
                .setUrl(getUrl)
                .setImage(conditionIconUrl)
                .build()

        val updateTask = FirebaseAppIndex.getInstance().update(articleToIndex)
        updateTask.addOnSuccessListener { println("Indexed") }
        updateTask.addOnFailureListener { e -> e.printStackTrace() }

        val startTask = FirebaseUserActions.getInstance().start(getAction(getCity, getUrl))
        startTask.addOnSuccessListener { println("Indexed") }
        startTask.addOnFailureListener { e -> e.printStackTrace() }
    }

    fun getAction(titleForAction: String, urlForAction: String): Action {
        return Actions.newView(titleForAction, urlForAction)
    }

    /*Get Weather Information*/
    private inner class WeatherConditionInfo : AsyncTask<Void, Void, String>() {

        override fun onPreExecute() {
            weatherInfoView.visibility = View.VISIBLE
            weatherIconView.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: Void): String {
            try {
                city = functionsClass.locationCityName()
                country = functionsClass.getCountryIso()

                val jsonWeatherLink = ("https://api.openweathermap.org/data/2.5/weather?q=" + city + "," + country + "&APPID=" + getString(R.string.openMapWeather))
//                val jsonWeatherLink = ("https://api.openweathermap.org/data/2.5/weather?q=" + "London" + "," + "UK" + "&APPID=" + getString(R.string.openMapWeather))
                weatherJSON = WeatherJSON(jsonWeatherLink)
                weatherJSON.fetchJSON()

                weather = weatherJSON.weather
                temperature = ((weatherJSON.temperature.toDouble() - 273.15).roundToInt()).toString()
                humidity = weatherJSON.humidity
                weatherIcon = weatherJSON.weatherIconUrl
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {

            }

            return "$country -  $city"
        }

        override fun onPostExecute(result: String) {
            FunctionsClassDebug.PrintDebug("*** ${result} ***")
            weatherSpinLoading.visibility = View.INVISIBLE
            weatherSpinLoadingInfo.visibility = View.INVISIBLE

            try {
                weatherInfoView.text = Html.fromHtml(
                        "<b><big>" + temperature + "</big></b>" + "<big>°ᶜ</big>" + " | " + weather + "<br/>"
                                + "" + "\uD83D\uDCA6" + " <small>" + humidity + "%</small>"
                )
                weatherIconView.colorFilter = null
                weatherIconView.setPadding(3, 3, 3, 3)
                Glide.with(this@PhoneMapsView)
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
                                runOnUiThread {
                                    weatherIconView.setImageDrawable(drawable)

                                    weatherInfoView.setOnClickListener {
                                        val Search = Intent(Intent.ACTION_WEB_SEARCH)
                                        Search.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                        Search.putExtra(SearchManager.QUERY, FloatingCompass.country + " " + FloatingCompass.city + " " + "Forecast")
                                        startActivity(Search)
                                    }
                                    weatherIconView.setOnClickListener {
                                        val Search = Intent(Intent.ACTION_WEB_SEARCH)
                                        Search.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                        Search.putExtra(SearchManager.QUERY, country + " " + city + " " + "Forecast")
                                        startActivity(Search)
                                    }
                                }

                                return false
                            }
                        })
                        .submit()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            try {
                indexAppInfo(city, temperature, weatherIcon,
                        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        Calendar.getInstance().get(Calendar.MINUTE))
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {

            }
        }
    }

    /*Extract City Names*/
    private inner class ExtractDataOfExistenceUsers : AsyncTask<Void, Void, HashMap<String, LatLng>>() {

        override fun onPreExecute() {

        }

        override fun doInBackground(vararg params: Void): HashMap<String, LatLng> {
            val cityNames = arrayListOf<String>()
            val savedLocations = arrayListOf<LatLng>()
            val hashKeyCityLocation = HashMap<String, LatLng>()
            FunctionsClassDebug.PrintDebug("*** ${cityNames} | ${savedLocations} ***")

            val listAllFiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)!!.path, File.separator + "PinPicsOnMap").listFiles()
            listAllFiles.forEach {
                FunctionsClassDebug.PrintDebug("*** ${it} ***")

                val listAllCityNames = it.listFiles()
                listAllCityNames.forEach { fileName ->
                    //CityName_
                    //(latitude,longitude)_
                    //System.currentTimeMillis().JPEG
                    val fileNameText = fileName.name
                    val finalNameTextSplits = fileNameText.split("_")

                    val cityName = finalNameTextSplits[0]
                    cityNames.add(cityName)

                    val locationText = finalNameTextSplits[1].replace("(", "").replace(")", "").split(",")
                    val latitude = locationText[0]
                    val longitude = locationText[1]
                    val location = LatLng(latitude.toDouble(), longitude.toDouble())
                    savedLocations.add(location)

                    hashKeyCityLocation[cityName] = location
                    FunctionsClassDebug.PrintDebug("*** ${finalNameTextSplits[0]} | ${location} ***")
                }
            }

            val hashSet = HashSet<String>(cityNames)
            cityNames.clear()
            cityNames.addAll(hashSet)

            return hashKeyCityLocation
        }

        override fun onPostExecute(result: HashMap<String, LatLng>) {
            FunctionsClassDebug.PrintDebug("*** ExtractCityNamesOfExistenceUsers ${result} ***")

            val usersInformationDataStructure = UsersInformationDataStructure().UserInformationFirestore(
                    firebaseUser.uid,
                    firebaseUser.email!!,
                    firebaseUser.displayName!!,
                    firebaseUser.photoUrl.toString(),
                    FieldValue.serverTimestamp(),
                    true.toString()
            )

            result.forEach {
                firestoreDatabase.document(
                        "PinPicsOnMap/" +
                                "Messenger/" +
                                "${PublicVariable.LOCATION_COUNTRY_NAME}/" +
                                "${it.key/*CityName*/}/" +
                                "People/" +
                                "${firebaseUser.uid}/"
                ).set(usersInformationDataStructure).addOnSuccessListener {
                    functionsClassPreferences.saveDefaultPreference("OldUser", false)

                }.addOnCanceledListener {


                }

                val linkedHashMapUserLocation = LinkedHashMap<Any, Any>()
                linkedHashMapUserLocation["locationLatitude"] = it.value.latitude
                linkedHashMapUserLocation["locationLongitude"] = it.value.longitude
                firestoreDatabase.collection(
                        "PinPicsOnMap/" +
                                "Messenger/" +
                                "${PublicVariable.LOCATION_COUNTRY_NAME}/" +
                                "${it.key/*CityName*/}/" +
                                "People/" +
                                "${firebaseUser.uid}/" +
                                "Visited-${it.value.latitude}-${it.value.longitude}"
                ).add(linkedHashMapUserLocation).addOnSuccessListener {

                }.addOnCanceledListener {

                }
            }
        }
    }

    /* [launchSynchronizationProcess] */
    private fun launchSynchronizationProcess(syncBackground: Boolean) {
        FunctionsClassDebug.PrintDebug("*** ${functionsClassPreferences.readPreference(".SavedDataInfo", "LastPicsCount", 0)} =!= ${functionsClassPreferences.readPreference(".SavedDataInfo", "LastSyncCount", 0)} ***")

        if (functionsClassPreferences.readPreference(".SavedDataInfo", "LastPicsCount", 0).toString().toInt() > functionsClassPreferences.readPreference(".SavedDataInfo", "LastSyncCount", 0).toString().toInt()) {
            if (functionsClass.networkConnection()) {
                FunctionsClassDebug.PrintDebug("*** Launch Synchronization Process ***")
                try {
                    syncCloud.isEnabled = false
                    uploadingWait.visibility = if (syncBackground) {
                        View.INVISIBLE
                    } else {
                        View.VISIBLE
                    }
                    loadingState.text = getString(R.string.downloading)

                    val firebaseStorage = FirebaseStorage.getInstance()
                    val firebaseStorageReference = firebaseStorage.reference

                    val locations = firebaseStorageReference.child("PinPicsOnMap/SubscribedUsers/" + firebaseUser.email + "/.Locations")
                    val details = firebaseStorageReference.child("PinPicsOnMap/SubscribedUsers/" + firebaseUser.email + "/.Details")
                    val allUploadedImageFiles = firebaseStorageReference.child("PinPicsOnMap/SubscribedUsers/" + firebaseUser.email + "/.AllUploadedImageFiles")
                    val allUploadedImageFilesXml = firebaseStorageReference.child("PinPicsOnMap/SubscribedUsers/" + firebaseUser.email + "/AllUploadedImageFiles.xml")

                    allUploadedImageFilesXml.getFile(File("/data/data/" + packageName + "/shared_prefs/" + "AllUploadedImageFiles.xml"))
                            .addOnSuccessListener {

                            }.addOnFailureListener {

                            }

                    locations.getFile(getFileStreamPath(".LocationsTemporary"))
                            .addOnSuccessListener {
                                if (getFileStreamPath(".Locations").exists()) {
                                    val cloudContent = functionsClass.readFileLine(".LocationsTemporary")
                                    val localContent = functionsClass.readFileLine(".Locations")
                                    val content = cloudContent + localContent
                                    val contentList = java.util.ArrayList<String>(content.asList())

                                    deleteFile(".LocationsTemporary")
                                    deleteFile(".Locations")

                                    val stringHashSet = LinkedHashSet<String>(contentList)
                                    contentList.clear()
                                    contentList.addAll(stringHashSet)

                                    contentList.forEachIndexed { index, text ->
                                        functionsClass.saveFileAppendLine(".Locations", text)
                                    }
                                } else {
                                    getFileStreamPath(".LocationsTemporary").renameTo(getFileStreamPath(".Locations"))
                                }

                                //Next Step
                                SyncDetailsFile(details, allUploadedImageFiles, firebaseStorageReference)
                            }.addOnFailureListener {
                                //Next Step
                                SyncDetailsFile(details, allUploadedImageFiles, firebaseStorageReference)
                            }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            FunctionsClassDebug.PrintDebug("*** No New Data To Synchronize ***")
        }
    }

    private fun SyncDataToCloudAll() {
        if (functionsClass.networkConnection()) {
            FunctionsClassDebug.PrintDebug("*** Sync Data To Cloud ***")
            try {
                if (!getFileStreamPath(".Locations").exists() && !getFileStreamPath(".Details").exists()) {
                    return
                }
                syncCloud.isEnabled = false
                loadingState.text = getString(R.string.uploading)

                val allFilesToUpload = ArrayList<File>()
                val nameOfCities = ArrayList<String?>()

                allFilesToUpload.add(getFileStreamPath(".Locations"))
                allFilesToUpload.add(getFileStreamPath(".Details"))

                val firebaseStorage = FirebaseStorage.getInstance()

                val listOfPreferences = functionsClass.readFileLine(".Locations")
                listOfPreferences.forEachIndexed { index, location ->

                    allFilesToUpload.add(File("/data/data/$packageName/shared_prefs/" + location + ".xml"))

                    val cityName: String = functionsClassPreferences.readPreference(location, "CityName", "Unknown Area") as String
                    nameOfCities.add(cityName)

                    val imagesFiles = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path,
                            File.separator
                                    + "PinPicsOnMap"
                                    + File.separator
                                    + location)
                    val imagesFilesList = imagesFiles.listFiles()
                    imagesFilesList.forEachIndexed { index, file ->
                        allFilesToUpload.add(file)
                    }
                }

                var uploadCounter = 0
                var finishedCounter = allFilesToUpload.size
                allFilesToUpload.forEachIndexed { index, file ->
                    if (functionsClassPreferences.readPreference("AllUploadedImageFiles", file.path, false) as Boolean) {
                        finishedCounter--
                        FunctionsClassDebug.PrintDebug("*** ${index} Already Uploaded ${file.path} --- ${file.name} ***")
                        if (uploadCounter == finishedCounter) {
                            Handler().postDelayed({
                                firebaseStorage.getReference("/" +
                                        "PinPicsOnMap" +
                                        "/"
                                        + "SubscribedUsers"
                                        + "/"
                                        + firebaseUser.email
                                        + "/"
                                        + ".AllUploadedImageFiles")
                                        .putFile(Uri.fromFile(getFileStreamPath(".AllUploadedImageFiles"))).addOnSuccessListener {
                                            firebaseStorage.getReference("/" +
                                                    "PinPicsOnMap" +
                                                    "/"
                                                    + "SubscribedUsers"
                                                    + "/"
                                                    + firebaseUser.email
                                                    + "/"
                                                    + "AllUploadedImageFiles.xml")
                                                    .putFile(Uri.fromFile(File("/data/data/$packageName/shared_prefs/" + "AllUploadedImageFiles" + ".xml"))).addOnSuccessListener {
                                                        uploadingWait.visibility = View.INVISIBLE
                                                        syncCloud.isEnabled = true

                                                        functionsClassPreferences.savePreference(".SavedDataInfo", "LastSyncCount", functionsClassPreferences.readPreference(".SavedDataInfo", "LastPicsCount", 0))
                                                    }
                                        }
                            }, 333)
                        }
                    } else {
                        FunctionsClassDebug.PrintDebug("*** ${index} File To Upload ${file.path} --- ${file.name} ***")

                        val uriFile = Uri.fromFile(file)

                        val storageReference = firebaseStorage.getReference("/" +
                                "PinPicsOnMap" +
                                "/"
                                + "SubscribedUsers"
                                + "/"
                                + firebaseUser.email
                                + "/"
                                + file.name)
                        uploadTask = storageReference.putFile(uriFile).addOnCompleteListener {

                        }.addOnSuccessListener {
                            uploadCounter++
                            FunctionsClassDebug.PrintDebug("${uploadCounter} Firebase Activities Done Successfully --- ${it.uploadSessionUri}")


                            if (file.name == ".Locations" || file.name == ".Details") {

                            } else {
                                functionsClassPreferences.savePreference("AllUploadedImageFiles", file.path, true)
                                functionsClass.saveFileAppendLine(".AllUploadedImageFiles", file.name)
                            }

                            if (uploadCounter == finishedCounter) {
                                Handler().postDelayed({
                                    firebaseStorage.getReference("/" +
                                            "PinPicsOnMap" +
                                            "/"
                                            + "SubscribedUsers"
                                            + "/"
                                            + firebaseUser.email
                                            + "/"
                                            + ".AllUploadedImageFiles")
                                            .putFile(Uri.fromFile(getFileStreamPath(".AllUploadedImageFiles"))).addOnSuccessListener {
                                                firebaseStorage.getReference("/" +
                                                        "PinPicsOnMap" +
                                                        "/"
                                                        + "SubscribedUsers"
                                                        + "/"
                                                        + firebaseUser.email
                                                        + "/"
                                                        + "AllUploadedImageFiles.xml")
                                                        .putFile(Uri.fromFile(File("/data/data/$packageName/shared_prefs/" + "AllUploadedImageFiles" + ".xml"))).addOnSuccessListener {
                                                            uploadingWait.visibility = View.INVISIBLE
                                                            syncCloud.isEnabled = true

                                                            functionsClassPreferences.savePreference(".SavedDataInfo", "LastSyncCount", functionsClassPreferences.readPreference(".SavedDataInfo", "LastPicsCount", 0))
                                                        }
                                            }
                                }, 333)
                            }
                        }.addOnFailureListener { exception ->
                            exception.printStackTrace()
                            FunctionsClassDebug.PrintDebug("*** ${getString(R.string.error)} ***")

                            Toast.makeText(applicationContext, getString(R.string.error), Toast.LENGTH_LONG).show()
                            uploadingWait.visibility = View.INVISIBLE
                            syncCloud.isEnabled = true

                            functionsClassPreferences.savePreference(".SavedDataInfo", "LastSyncCount", functionsClassPreferences.readPreference(".SavedDataInfo", "LastPicsCount", 0))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                syncCloud.isEnabled = true

                functionsClassPreferences.savePreference(".SavedDataInfo", "LastSyncCount", functionsClassPreferences.readPreference(".SavedDataInfo", "LastPicsCount", 0))
            }
        }
    }

    private fun SyncDetailsFile(details: StorageReference, allUploadedImageFiles: StorageReference, firebaseStorageReference: StorageReference) {
        FunctionsClassDebug.PrintDebug("*** Sync Details File ***")
        details.getFile(getFileStreamPath(".DetailsTemporary"))
                .addOnSuccessListener {
                    if (getFileStreamPath(".Details").exists()) {
                        val cloudContent = functionsClass.readFileLine(".DetailsTemporary")
                        val localContent = functionsClass.readFileLine(".Locations")
                        val content = cloudContent + localContent
                        val contentList = java.util.ArrayList<String>(content.asList())

                        deleteFile(".DetailsTemporary")
                        deleteFile(".Details")

                        val stringHashSet = LinkedHashSet<String>(contentList)
                        contentList.clear()
                        contentList.addAll(stringHashSet)

                        contentList.forEachIndexed { index, text ->
                            functionsClass.saveFileAppendLine(".Details", text)
                        }
                    } else {
                        getFileStreamPath(".DetailsTemporary").renameTo(getFileStreamPath(".Details"))
                    }

                    //Next Step
                    SyncAllImages(allUploadedImageFiles, firebaseStorageReference)
                }.addOnFailureListener {
                    //Next Step
                    SyncAllImages(allUploadedImageFiles, firebaseStorageReference)
                }
    }

    private fun SyncAllImages(allUploadedImageFiles: StorageReference, firebaseStorageReference: StorageReference) {
        FunctionsClassDebug.PrintDebug("*** Sync All Images ***")
        allUploadedImageFiles.getFile(getFileStreamPath(".AllUploadedImageFilesTemporary"))
                .addOnSuccessListener {
                    if (getFileStreamPath(".AllUploadedImageFiles").exists()) {
                        val cloudContent = functionsClass.readFileLine(".AllUploadedImageFilesTemporary")
                        val localContent = functionsClass.readFileLine(".AllUploadedImageFiles")
                        val content = cloudContent + localContent
                        val contentList = java.util.ArrayList<String>(content.asList())

                        deleteFile(".AllUploadedImageFilesTemporary")
                        deleteFile(".AllUploadedImageFiles")

                        val stringHashSet = LinkedHashSet<String>(contentList)
                        contentList.clear()
                        contentList.addAll(stringHashSet)

                        contentList.forEachIndexed { index, text ->
                            functionsClass.saveFileAppendLine(".AllUploadedImageFiles", text)
                        }
                    } else {
                        getFileStreamPath(".AllUploadedImageFilesTemporary").renameTo(getFileStreamPath(".AllUploadedImageFiles"))

                    }

                    val filesToDownload = functionsClass.readFileLine(".AllUploadedImageFiles")

                    Handler().postDelayed({
                        //Download All Files
                        var downloadCounter = 0
                        var finishedCounter = filesToDownload.size
                        filesToDownload.forEachIndexed { index, fileName ->

                            if (functionsClassPreferences.readPreference("AllDownloadedFiles", fileName, false) as Boolean) {
                                finishedCounter--
                                FunctionsClassDebug.PrintDebug("*** ${finishedCounter} Firebase Already Downloaded ${fileName} ***")
                                if (downloadCounter == finishedCounter) {
                                    SyncDataToCloudAll()
                                }
                            } else {
                                FunctionsClassDebug.PrintDebug("*** ${downloadCounter} Firebase Downloading ${fileName} ***")

                                if (fileName.contains(".xml")) {
                                    firebaseStorageReference
                                            .child("PinPicsOnMap/SubscribedUsers/" + firebaseUser.email + "/" + fileName)
                                            .getFile(File("/data/data/" + packageName + "/shared_prefs/" + fileName))
                                            .addOnSuccessListener {
                                                downloadCounter++

                                                if (downloadCounter == finishedCounter) {
                                                    SyncDataToCloudAll()
                                                }

                                                functionsClassPreferences.savePreference("AllDownloadedFiles", fileName, true)
                                            }
                                } else {
                                    /*[Images]*/
                                    /* /sdcard/Pictures/PinPicsOnMap/(Lat,Long)/...File....JPEG */
                                    val fileNameTemporary = fileName.split("_")
                                    val directoryToImages = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path,
                                            File.separator
                                                    + "PinPicsOnMap"
                                                    + File.separator
                                                    + fileNameTemporary[1])
                                    if (!directoryToImages.exists()) {
                                        directoryToImages.mkdirs()
                                    }
                                    firebaseStorageReference
                                            .child("PinPicsOnMap/SubscribedUsers/" + firebaseUser.email + "/" + fileName)
                                            .getFile(File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path,
                                                    File.separator
                                                            + "PinPicsOnMap"
                                                            + File.separator
                                                            + fileNameTemporary[1]
                                                            + File.separator
                                                            + fileName))
                                            .addOnSuccessListener {
                                                downloadCounter++

                                                if (downloadCounter == finishedCounter) {
                                                    SyncDataToCloudAll()
                                                }

                                                functionsClassPreferences.savePreference("AllDownloadedFiles", fileName, true)
                                            }
                                }
                            }
                        }
                    }, 333)
                }.addOnFailureListener {
                    SyncDataToCloudAll()
                }
    }
}
