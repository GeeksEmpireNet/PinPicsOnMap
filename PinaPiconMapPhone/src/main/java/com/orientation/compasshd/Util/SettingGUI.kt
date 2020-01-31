/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 2:50 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.appinvite.AppInviteInvitation
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.orientation.compasshd.BuildConfig
import com.orientation.compasshd.Compass.TimeCheckCompass
import com.orientation.compasshd.PinPicsOnMapApplication
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.Functions.FunctionsClass
import com.orientation.compasshd.Util.Functions.FunctionsClassDialogue
import com.orientation.compasshd.Util.Functions.FunctionsClassPreferences
import com.orientation.compasshd.Util.Functions.PublicVariable
import kotlinx.android.synthetic.main.setting_gui.*
import java.io.FileNotFoundException
import javax.inject.Inject

class SettingGUI : androidx.fragment.app.FragmentActivity() {

    lateinit var functionsClass: FunctionsClass
    lateinit var functionsClassDialogue: FunctionsClassDialogue

    @Inject lateinit var functionsClassPreferences: FunctionsClassPreferences

    var drawableResult: Drawable? = null

    lateinit var googleSignInClient: GoogleSignInClient

    var API: Int = 0
    val SELECT_PHOTO = 333
    val REQUEST_INVITE = 102
    val REQUEST_SING_GOOGLE = 66
    var Google_Sign_In = -1

    lateinit var interstitialAd: InterstitialAd
    lateinit var adRequestInterstitial: AdRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as PinPicsOnMapApplication)
                .dependencyGraph.inject(this@SettingGUI)
        setContentView(R.layout.setting_gui)

        functionsClass = FunctionsClass(this@SettingGUI)
        functionsClassDialogue = FunctionsClassDialogue(this@SettingGUI, functionsClassPreferences)

        functionsClassDialogue.changeLog(false)

        try {
            actionBar!!.setDisplayHomeAsUpEnabled(true)
            val tempBitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_compass)
            val scaleBitmap = Bitmap.createScaledBitmap(tempBitmap, tempBitmap.width / 5, tempBitmap.height / 5, false)
            val logoDrawable = BitmapDrawable(resources, scaleBitmap)
            actionBar!!.setHomeAsUpIndicator(logoDrawable)
        } catch (e: Exception) {
            e.printStackTrace()
        }


        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        mail.setOnClickListener {
            Google_Sign_In = 2
            signIn()
        }
        twitter.setOnClickListener { goInstagram() }
        facebook.setOnClickListener { shareFacebook() }
        share.setOnClickListener { shareAll() }

        pickImage.setOnClickListener(View.OnClickListener {
            val API = Build.VERSION.SDK_INT
            if (API > 22) {
                if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                    val permission = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
                    requestPermissions(permission, 666)
                    return@OnClickListener
                }
            }
            val photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, SELECT_PHOTO)
        })
        pickImage.setOnLongClickListener {
            pickImage.background = null
            error.visibility = View.INVISIBLE

            functionsClassPreferences
                    .CompassConfiguration()
                    .saveCompassConfig(FunctionsClassPreferences.COMPASS_IMAGE_FILE_PATH, null)

            true
        }

        small.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                functionsClassPreferences
                        .CompassConfiguration()
                        .saveCompassConfig(FunctionsClassPreferences.COMPASS_VIEW_SIZE_MODE, 1)
            }
        }
        small.setOnClickListener {
            small.isChecked = true
            medium.isChecked = false
            large.isChecked = false

            val HW = 100

            functionsClassPreferences
                    .CompassConfiguration()
                    .saveCompassConfig(FunctionsClassPreferences.COMPASS_VIEW_SIZE_WH, HW)
        }

        medium.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                functionsClassPreferences
                        .CompassConfiguration()
                        .saveCompassConfig(FunctionsClassPreferences.COMPASS_VIEW_SIZE_MODE, 2)
            }
        }
        medium.setOnClickListener {
            small.isChecked = false
            medium.isChecked = true
            large.isChecked = false

            val HW = 200

            functionsClassPreferences
                    .CompassConfiguration()
                    .saveCompassConfig(FunctionsClassPreferences.COMPASS_VIEW_SIZE_WH, HW)
        }

        large.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                functionsClassPreferences
                        .CompassConfiguration()
                        .saveCompassConfig(FunctionsClassPreferences.COMPASS_VIEW_SIZE_MODE, 3)
            }
        }
        large.setOnClickListener {
            small.isChecked = false
            medium.isChecked = false
            large.isChecked = true

            val HW = 300

            functionsClassPreferences
                    .CompassConfiguration()
                    .saveCompassConfig(FunctionsClassPreferences.COMPASS_VIEW_SIZE_WH, HW)
        }

        miniCompass.setOnClickListener { }
        miniCompass.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                functionsClassPreferences
                        .CompassConfiguration()
                        .saveCompassConfig(FunctionsClassPreferences.COMPASS_MINIMIZE_MODE, true)
            } else {
                functionsClassPreferences
                        .CompassConfiguration()
                        .saveCompassConfig(FunctionsClassPreferences.COMPASS_MINIMIZE_MODE, false)
            }
        }

        autoMini.setOnClickListener { }
        autoMini.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                functionsClassPreferences
                        .CompassConfiguration()
                        .saveCompassConfig(FunctionsClassPreferences.COMPASS_AUTO_MINIMIZE, true)
            } else {
                functionsClassPreferences
                        .CompassConfiguration()
                        .saveCompassConfig(FunctionsClassPreferences.COMPASS_AUTO_MINIMIZE, false)
            }
        }
    }

    public override fun onStart() {
        super.onStart()
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

        whatsnewImage.setOnClickListener { functionsClassDialogue.changeLog(true) }
        whatsnewText.setOnClickListener { functionsClassDialogue.changeLog(true) }

        MobileAds.initialize(applicationContext, getString(R.string.ad_app_id))
        val adRequestBanner = AdRequest.Builder()
                .addTestDevice("65B5827710CBE90F4A99CE63099E524C")
                .addTestDevice("CDCAA1F20B5C9C948119E886B31681DE")
                .addTestDevice("D101234A6C1CF51023EE5815ABC285BD")
                .addTestDevice("5901E5EE74F9B6652E05621140664A54")
                .addTestDevice("F54D998BCE077711A17272B899B44798")
                .build()
        adView.loadAd(adRequestBanner)

        interstitialAd = InterstitialAd(this)
        adRequestInterstitial = AdRequest.Builder()
                .addTestDevice("65B5827710CBE90F4A99CE63099E524C")
                .addTestDevice("CDCAA1F20B5C9C948119E886B31681DE")
                .addTestDevice("D101234A6C1CF51023EE5815ABC285BD")
                .addTestDevice("DD428143B4772EC7AA87D1E2F9DA787C")
                .addTestDevice("5901E5EE74F9B6652E05621140664A54")
                .addTestDevice("F54D998BCE077711A17272B899B44798")
                .build()
        interstitialAd.setImmersiveMode(true)
        interstitialAd.adUnitId = getString(R.string.AdUnitSettingGUI)
        interstitialAd.loadAd(adRequestInterstitial)
        interstitialAd.adListener = object : AdListener() {
            override fun onAdLoaded() {
                if (PublicVariable.eligibleToLoadShowAds) {
                    interstitialAd.show()
                }
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                if (PublicVariable.eligibleToLoadShowAds) {
                    interstitialAd.loadAd(adRequestInterstitial)
                }
            }

            override fun onAdOpened() {}

            override fun onAdLeftApplication() {}

            override fun onAdClosed() {}
        }
    }

    public override fun onResume() {
        super.onResume()
        PublicVariable.eligibleToLoadShowAds = true


        val pathDefault = Uri.parse("android.resource://" + packageName + "/" + R.drawable.pick_image)
        val imagePath: String = functionsClassPreferences.CompassConfiguration().readCompassConfig(FunctionsClassPreferences.COMPASS_IMAGE_FILE_PATH, pathDefault.path).toString()
        val path = Uri.parse(imagePath)
        try {
            drawableResult = Drawable.createFromStream(contentResolver.openInputStream(path), path.toString())
            error.visibility = View.VISIBLE
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            error.visibility = View.INVISIBLE
            drawableResult = null
        }

        pickImage.background = drawableResult

        API = Build.VERSION.SDK_INT

        window.clearFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        )
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)

        if (functionsClass.getTime() == "day") {
            this@SettingGUI.window.decorView.setBackgroundColor(getColor(R.color.light))
            actionBar!!.setBackgroundDrawable(ColorDrawable(getColor(R.color.light)))
            actionBar!!.title = Html.fromHtml("<font color='" + getColor(R.color.dark) + "'>" + getString(R.string.preferencesCompass) + "</font>")
            actionBar!!.subtitle = Html.fromHtml("<font color='" + getColor(R.color.dark) + "'>" + getString(R.string.full_app_name) + "</font>")

            window.statusBarColor = getColor(R.color.default_color_light)
            window.navigationBarColor = getColor(R.color.default_color_light)

            activitySettingGUI.background = ColorDrawable(getColor(R.color.default_color_light))

            val drawLayerFrontSetting = getDrawable(R.drawable.detail_preference_gui) as LayerDrawable
            val backdrawLayerFrontSetting = drawLayerFrontSetting.findDrawableByLayerId(R.id.frontLayer) as GradientDrawable
            backdrawLayerFrontSetting.setColor(getColor(R.color.light))
            allSetting.background = drawLayerFrontSetting

            val drawLayerFrontShare = getDrawable(R.drawable.detail_share_gui) as LayerDrawable
            val backdrawLayerFrontShare = drawLayerFrontShare.findDrawableByLayerId(R.id.frontLayer) as GradientDrawable
            backdrawLayerFrontShare.setColor(getColor(R.color.light))
            allShare.background = drawLayerFrontShare

            small.setTextColor(getColor(R.color.dark))
            small.buttonTintList = ColorStateList.valueOf(getColor(R.color.dark))
            medium.setTextColor(getColor(R.color.dark))
            medium.buttonTintList = ColorStateList.valueOf(getColor(R.color.dark))
            large.setTextColor(getColor(R.color.dark))
            large.buttonTintList = ColorStateList.valueOf(getColor(R.color.dark))

            miniCompass.setTextColor(getColor(R.color.dark))
            miniCompass.buttonTintList = ColorStateList.valueOf(getColor(R.color.dark))
            autoMini.setTextColor(getColor(R.color.dark))
            autoMini.buttonTintList = ColorStateList.valueOf(getColor(R.color.dark))

            whatsnewText.setTextColor(getColor(R.color.dark))
        } else if (functionsClass.getTime() == "night") {
            this@SettingGUI.window.decorView.setBackgroundColor(getColor(R.color.dark))
            actionBar!!.setBackgroundDrawable(ColorDrawable(getColor(R.color.dark)))
            actionBar!!.title = Html.fromHtml("<font color='" + getColor(R.color.light) + "'>" + getString(R.string.preferencesCompass) + "</font>")
            actionBar!!.subtitle = Html.fromHtml("<font color='" + getColor(R.color.light) + "'>" + getString(R.string.full_app_name) + "</font>")

            window.statusBarColor = getColor(R.color.default_color_darker)
            window.navigationBarColor = getColor(R.color.default_color_darker)

            activitySettingGUI.background = ColorDrawable(getColor(R.color.default_color_darker))

            val drawLayerFrontSetting = getDrawable(R.drawable.detail_preference_gui) as LayerDrawable
            val backdrawLayerFrontSetting = drawLayerFrontSetting.findDrawableByLayerId(R.id.frontLayer) as GradientDrawable
            backdrawLayerFrontSetting.setColor(getColor(R.color.dark))
            allSetting.background = drawLayerFrontSetting

            val drawLayerFrontShare = getDrawable(R.drawable.detail_share_gui) as LayerDrawable
            val backdrawLayerFrontShare = drawLayerFrontShare.findDrawableByLayerId(R.id.frontLayer) as GradientDrawable
            backdrawLayerFrontShare.setColor(getColor(R.color.dark))
            allShare.background = drawLayerFrontShare

            small.setTextColor(getColor(R.color.light))
            small.buttonTintList = ColorStateList.valueOf(getColor(R.color.light))
            medium.setTextColor(getColor(R.color.light))
            medium.buttonTintList = ColorStateList.valueOf(getColor(R.color.light))
            large.setTextColor(getColor(R.color.light))
            large.buttonTintList = ColorStateList.valueOf(getColor(R.color.light))

            miniCompass.setTextColor(getColor(R.color.light))
            miniCompass.buttonTintList = ColorStateList.valueOf(getColor(R.color.light))
            autoMini.setTextColor(getColor(R.color.light))
            autoMini.buttonTintList = ColorStateList.valueOf(getColor(R.color.light))

            whatsnewText.setTextColor(getColor(R.color.light))
        }

        if (functionsClassPreferences.CompassConfiguration().readCompassConfig(FunctionsClassPreferences.COMPASS_MINIMIZE_MODE, true) as Boolean) {
            miniCompass.isChecked = true
        } else if (!(functionsClassPreferences.CompassConfiguration().readCompassConfig(FunctionsClassPreferences.COMPASS_MINIMIZE_MODE, true) as Boolean)) {
            miniCompass.isChecked = false
        }

        if (functionsClassPreferences.CompassConfiguration().readCompassConfig(FunctionsClassPreferences.COMPASS_AUTO_MINIMIZE, true) as Boolean) {
            autoMini.isChecked = true
        } else if (!(functionsClassPreferences.CompassConfiguration().readCompassConfig(FunctionsClassPreferences.COMPASS_AUTO_MINIMIZE, true) as Boolean)) {
            autoMini.isChecked = false
        }

        when (functionsClassPreferences.CompassConfiguration().readCompassConfig(FunctionsClassPreferences.COMPASS_VIEW_SIZE_MODE, 2) as Int) {
            1 -> {
                small.isChecked = true
            }
            2 -> {
                medium.isChecked = true
            }
            3 -> {
                large.isChecked = true
            }
        }
    }

    public override fun onPause() {
        super.onPause()
        PublicVariable.eligibleToLoadShowAds = false
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.setting_gui_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId

        when (id) {
            R.id.osp -> {
                val browserGEOSP = Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.link_facebook_app)))
                startActivity(Intent.createChooser(browserGEOSP, ""))
            }
            R.id.support -> {
                val contactOption = arrayOf("Send an Email", "Contact via Forum", "Join Beta Program", "Rate & Write Review")
                var builder = AlertDialog.Builder(this@SettingGUI)
                if (functionsClass.returnAPI() > 22) {
                    if (TimeCheckCompass.time == "day") {
                        builder = AlertDialog.Builder(this@SettingGUI, R.style.GeeksEmpire_Dialogue_Light)
                    } else if (TimeCheckCompass.time == "night") {
                        builder = AlertDialog.Builder(this@SettingGUI, R.style.GeeksEmpire_Dialogue_Dark)
                    }
                }
                builder.setTitle(getString(R.string.supportCategory))
                builder.setSingleChoiceItems(contactOption, 0, null)
                builder.setPositiveButton(android.R.string.ok) { dialog, whichButton ->
                    val selectedPosition = (dialog as AlertDialog).listView.checkedItemPosition
                    if (selectedPosition == 0) {
                        val textMsg = ("\n\n\n\n\n"
                                + functionsClass.getDeviceName() + " | " + "API " + Build.VERSION.SDK_INT + " | " + functionsClass.getCountryIso().toUpperCase())
                        val email = Intent(Intent.ACTION_SEND)
                        email.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.support)))
                        email.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_tag) + " [" + BuildConfig.VERSION_NAME + "] ")
                        email.putExtra(Intent.EXTRA_TEXT, textMsg)
                        email.type = "text/*"
                        email.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(Intent.createChooser(email, getString(R.string.feedback_tag)))
                    } else if (selectedPosition == 1) {
                        val a = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_xda)))
                        startActivity(a)
                    } else if (selectedPosition == 2) {
                        val a = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_alpha)))
                        startActivity(a)

                    } else if (selectedPosition == 3) {
                        val a = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_compass)))
                        startActivity(a)
                    }
                }
                builder.setNegativeButton(android.R.string.cancel) { dialog, whichButton -> }
                builder.show()
            }
            android.R.id.home -> {
                val l = Intent(applicationContext, TimeCheckCompass::class.java)
                l.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(l)

                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            SELECT_PHOTO -> if (resultCode == Activity.RESULT_OK) {
                try {
                    val imageUri = data!!.data
                    val imageStream = contentResolver.openInputStream(imageUri!!)
                    val path = imageUri.toString()

                    functionsClassPreferences
                            .CompassConfiguration()
                            .saveCompassConfig(FunctionsClassPreferences.COMPASS_IMAGE_FILE_PATH, path)

                    val drawableResult = Drawable.createFromStream(imageStream, imageUri.toString())
                    val selectedImage = BitmapFactory.decodeStream(imageStream)

                    pickImage.background = drawableResult
                    error.visibility = View.VISIBLE
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    error.text = e.message
                }

            }
            REQUEST_INVITE -> if (resultCode == Activity.RESULT_OK) {
                // Get the invitation IDs of all sent messages
                val ids = AppInviteInvitation.getInvitationIds(resultCode, data!!)
                for (id in ids) {
                    Log.d(packageName, "Sent Invitation To >> $id")
                }
            } else {
            }
            REQUEST_SING_GOOGLE -> {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                if (result.isSuccess) {
                    val googleSignInAccount = result.signInAccount
                    if (Google_Sign_In == 2) {
                        onInviteClicked()
                    }
                }
            }
        }
    }

    private fun signIn() {
        googleSignInClient.revokeAccess()

        startActivityForResult(googleSignInClient.signInIntent, REQUEST_SING_GOOGLE)
    }

    private fun onInviteClicked() {
        val intent = AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setDeepLink(Uri.parse(getString(R.string.invitation_deep_link)))
                .setCustomImage(Uri.parse(getString(R.string.invitation_custom_image)))
                .build()
        startActivityForResult(intent, REQUEST_INVITE)
    }

    private fun shareAll() {
        val shareText = getString(R.string.invitation_title) +
                "\n" + getString(R.string.invitation_message) +
                "\n" + getString(R.string.invitation_deep_link)
        val path = Uri.parse("android.resource://" + packageName + "/" + R.drawable.ic_launcher)

        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_TEXT, shareText)
        intent.putExtra(Intent.EXTRA_STREAM, path)
        intent.type = "image/*"
        intent.type = "text/*"
        startActivity(intent)
    }

    private fun goInstagram() {
        val g = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_instagram)))
        startActivity(g)
    }

    private fun shareFacebook() {
        val g = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.link_facebook)))
        startActivity(g)
    }
}
