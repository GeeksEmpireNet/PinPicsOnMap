/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/30/20 4:15 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.Functions

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.text.Html
import com.orientation.compasshd.BuildConfig
import com.orientation.compasshd.Compass.TimeCheckCompass
import com.orientation.compasshd.R

class FunctionsClassDialogue (var context: Activity, var functionsClassPreferences: FunctionsClassPreferences) {

    fun changeLog(showChangeLog: Boolean) {
        var alertDialog: AlertDialog.Builder = AlertDialog.Builder(context, R.style.GeeksEmpire_Dialogue_Dark)
        //TimeCheckCompass.Companion.getTime().equals("day")
        if (TimeCheckCompass.time == "day") {
            alertDialog = AlertDialog.Builder(context, R.style.GeeksEmpire_Dialogue_Light)
        } else if (TimeCheckCompass.time == "day") {
            alertDialog = AlertDialog.Builder(context, R.style.GeeksEmpire_Dialogue_Dark)
        }
        alertDialog.setTitle(Html.fromHtml(context.getString(R.string.whatsnew)))
        alertDialog.setMessage(Html.fromHtml(context.getString(R.string.changelog)))
        alertDialog.setIcon(R.drawable.ic_launcher)
        alertDialog.setCancelable(true)
        alertDialog.setPositiveButton(context.getString(R.string.like_facebook)) { dialogInterface, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.link_facebook_app)))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            dialogInterface.dismiss()
        }
        alertDialog.setNeutralButton(context.getString(R.string.like_instagram)) { dialogInterface, which ->
            when (which) {
                DialogInterface.BUTTON_NEUTRAL -> {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.link_instagram)))
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                }
            }
            dialogInterface.dismiss()
        }
        alertDialog.setOnDismissListener { dialogInterface ->
            functionsClassPreferences.savePreference(".Updated", "VersionCode", BuildConfig.VERSION_CODE)
            dialogInterface.dismiss()
        }

        if (showChangeLog) {
            alertDialog.show()
        } else {
            if (BuildConfig.VERSION_CODE > functionsClassPreferences.readPreference(".Updated", "VersionCode", 0).toString().toInt()) {
                alertDialog.show()
            }
        }
    }
}