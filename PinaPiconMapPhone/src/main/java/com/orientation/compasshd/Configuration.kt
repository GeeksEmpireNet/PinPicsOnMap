/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 4:08 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.view.View
import android.view.WindowManager
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.orientation.compasshd.Compass.TimeCheckCompass
import com.orientation.compasshd.Maps.PhoneMapsView
import com.orientation.compasshd.Maps.TimeCheckMap
import com.orientation.compasshd.Util.Functions.FunctionsClass
import com.orientation.compasshd.Util.LoginCheckpoint
import kotlinx.android.synthetic.main.login_waiting.*

class Configuration : Activity() {

    lateinit var functionsClass: FunctionsClass

    lateinit var TimeCheckReference: String
    lateinit var timeDayNight: String

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            666 -> {
                if (!Settings.canDrawOverlays(applicationContext)) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    startActivity(intent)
                }
            }
        }
    }

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        FirebaseApp.initializeApp(this)
        setContentView(R.layout.login_waiting)

        functionsClass = FunctionsClass(this@Configuration)

        if (TimeCheckCompass.time == "day") {
            spinKit.setColor(getColor(R.color.light))

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.statusBarColor = getColor(R.color.default_color_light)
            window.navigationBarColor = getColor(R.color.default_color_light)
            window.decorView.setBackgroundColor(Color.TRANSPARENT)
        } else if (TimeCheckCompass.time == "night") {
            spinKit.setColor(getColor(R.color.red))

            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.statusBarColor = getColor(R.color.default_color_darker)
            window.navigationBarColor = getColor(R.color.default_color_darker)
            window.decorView.setBackgroundColor(Color.TRANSPARENT)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        }

        TimeCheckReference = intent.getStringExtra("TimeCheck")
        if (TimeCheckReference == "TimeCheckCompass") {
            timeDayNight = TimeCheckCompass.time
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(applicationContext, BindService::class.java))
            } else {
                startService(Intent(applicationContext, BindService::class.java))
            }
        } else if (TimeCheckReference == "TimeCheckMap") {
            timeDayNight = TimeCheckMap.time

        }

        if (!Settings.canDrawOverlays(applicationContext)
                || checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED
                || checkSelfPermission(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_DENIED) {
            infoPermission()
        }
    }

    public override fun onStart() {
        super.onStart()

        if (functionsClass.readDefaultPreference("UserAgreement", false)) {
            agreementView.visibility = View.INVISIBLE
            policyView.visibility = View.INVISIBLE
            agreed.visibility = View.INVISIBLE
        }

        agreementView.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://geeksempire.net/Projects/Android/PinPicsOnMap/TermsOfService.html")))
        }

        policyView.setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://geeksempire.net/Projects/Android/PinPicsOnMap/PrivacyPolicy.html")))
        }

        agreed.setOnClickListener {

            if (FirebaseAuth.getInstance().currentUser == null) {
                startActivity(Intent(applicationContext, LoginCheckpoint::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).apply {
                    putExtra("TimeCheck", "TimeCheckMap")
                }, ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
            } else {
                startActivity(Intent(applicationContext, PhoneMapsView::class.java),
                        ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
            }

            functionsClass.saveDefaultPreference("UserAgreement", true)
        }
    }

    public override fun onResume() {
        super.onResume()
        if (Settings.canDrawOverlays(applicationContext)
                && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.GET_ACCOUNTS) == PackageManager.PERMISSION_GRANTED
                && functionsClass.readDefaultPreference("UserAgreement", false)) {
            TimeCheckCompass.run = false
            stopService(Intent(applicationContext, BindService::class.java))

            val intent = Intent()
            if (TimeCheckReference == "TimeCheckCompass") {
                intent.setClass(this@Configuration, TimeCheckCompass::class.java)
            } else if (TimeCheckReference == "TimeCheckMap") {
                intent.setClass(this@Configuration, TimeCheckMap::class.java)
            } else {
                intent.setClass(this@Configuration, TimeCheckMap::class.java)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)

            finish()
        } else {
            if (functionsClass.readDefaultPreference("UserAgreement", false)) {
                agreementView.visibility = View.INVISIBLE
                policyView.visibility = View.INVISIBLE
                agreed.visibility = View.INVISIBLE
            } else {


            }
        }
    }

    public override fun onPause() {
        super.onPause()
    }

    public override fun onDestroy() {
        super.onDestroy()
        try {
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            firebaseUser!!.reload()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun infoPermission() {
        var alertDialog = AlertDialog.Builder(this@Configuration, R.style.GeeksEmpire_Dialogue_Night)
        if (timeDayNight == "day") {
            alertDialog = AlertDialog.Builder(this@Configuration, R.style.GeeksEmpire_Dialogue_Day)
        } else if (timeDayNight == "night") {
            alertDialog = AlertDialog.Builder(this@Configuration, R.style.GeeksEmpire_Dialogue_Night)
        }
        alertDialog.setTitle(Html.fromHtml(getString(R.string.permission_title)))
        alertDialog.setMessage(Html.fromHtml(getString(R.string.permission_msg)))
        alertDialog.setIcon(R.drawable.ic_launcher)
        alertDialog.setCancelable(false)

        alertDialog.setPositiveButton("Grant") { dialog, which ->
            val allPermissions = arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.GET_ACCOUNTS,
                    Manifest.permission.INTERNET)
            requestPermissions(allPermissions, 666)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(Intent(applicationContext, BindService::class.java))
            } else {
                startService(Intent(applicationContext, BindService::class.java))
            }
        }
        alertDialog.setNegativeButton("Later") { dialog, which ->
            stopService(Intent(applicationContext, BindService::class.java))

            this@Configuration.finish()
            dialog.dismiss()
        }
        alertDialog.setOnDismissListener { dialog ->

        }
        alertDialog.show()
    }
}
