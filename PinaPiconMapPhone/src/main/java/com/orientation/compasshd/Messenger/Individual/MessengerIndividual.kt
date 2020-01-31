/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 1:45 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Messenger.Individual

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.Functions.FunctionsClass

class MessengerIndividual : Activity() {

    lateinit var functionsClass: FunctionsClass

    lateinit var cityName: String
    lateinit var time: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.individual_messenger)

        functionsClass = FunctionsClass(applicationContext)
        time = functionsClass.getTime()

        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = if (time == "day") {
            getColor(R.color.light)
        } else {
            getColor(R.color.dark)
        }
        window.navigationBarColor = if (time == "day") {
            getColor(R.color.light)
        } else {
            getColor(R.color.dark)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}