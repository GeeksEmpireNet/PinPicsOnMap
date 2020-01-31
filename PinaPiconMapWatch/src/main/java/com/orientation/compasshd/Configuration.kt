/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.widget.Toast
import java.util.*

class Configuration : Activity() {

    override fun onCreate(saved: Bundle?) {
        super.onCreate(saved)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(Intent(applicationContext, BindService::class.java))
        } else {
            startService(Intent(applicationContext, BindService::class.java))
        }

        val API = Build.VERSION.SDK_INT
        if (API > 22) {
            if (!Settings.canDrawOverlays(applicationContext)) {
                infoPermission()
            }
            val Permissions = arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.INTERNET)
            requestPermissions(Permissions, 666)
        } else {
            val s = Intent(this@Configuration, TimeCheck::class.java)
            s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(s)

            finish()
        }
    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onPause() {
        super.onPause()
    }

    fun infoPermission() {
        var alertDialog: AlertDialog.Builder
        val hrs = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        alertDialog = if (hrs >= 18 || hrs < 6) {//Night
            AlertDialog.Builder(this@Configuration, R.style.GeeksEmpire_Dialogue_Night)
        } else {//Day
            AlertDialog.Builder(this@Configuration, R.style.GeeksEmpire_Dialogue_Day)
        }
        alertDialog.setTitle(Html.fromHtml(resources.getString(R.string.permission_title)))
        alertDialog.setMessage(Html.fromHtml(resources.getString(R.string.permission_msg)))
        alertDialog.setIcon(R.drawable.ic_launcher)
        alertDialog.setCancelable(false)

        alertDialog.setPositiveButton("Grant") { dialog, which ->
            val i = Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:$packageName"))
            startActivity(i)

            Toast.makeText(applicationContext, "After Turning ON the Switch Restart App", Toast.LENGTH_LONG).show()
            dialog.dismiss()
        }
        alertDialog.setNegativeButton("Later") { dialog, which ->
            stopService(Intent(applicationContext, BindService::class.java))

            dialog.dismiss()
        }
        alertDialog.setNeutralButton("Read More") { dialog, i ->
            val faq = Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://android-developers.blogspot.com/2015/08/building-better-apps-with-runtime.html"))
            startActivity(faq)
            stopService(Intent(applicationContext, BindService::class.java))

            dialog.dismiss()
        }
        alertDialog.setOnDismissListener { finish() }
        alertDialog.show()
    }
}
