/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.Functions

import android.app.Activity
import android.content.Context
import com.orientation.compasshd.BuildConfig

class FunctionsClassDebug {

    lateinit var activity: Activity
    lateinit var context: Context

    constructor(activity: Activity, context: Context) {
        this.activity = activity
        this.context = context
    }

    constructor(context: Context) {
        this.context = context
    }

    init {

    }

    companion object {
        fun PrintDebug(debugMessage: Any?) {
            if (BuildConfig.DEBUG) {
                println(debugMessage)
            }
        }
    }
}