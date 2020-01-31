/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 3:13 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.Functions

import com.orientation.compasshd.BuildConfig

class FunctionsClassDebug {

    companion object {
        fun PrintDebug(debugMessage: Any?) {
            if (BuildConfig.DEBUG) {
                println(debugMessage)
            }
        }
    }
}