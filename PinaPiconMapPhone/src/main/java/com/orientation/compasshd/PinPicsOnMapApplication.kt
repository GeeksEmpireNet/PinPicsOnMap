/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 2:50 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd

import android.app.Application
import com.orientation.compasshd.Util.DependencyInjection.Preferences.DaggerDependencyGraph
import com.orientation.compasshd.Util.DependencyInjection.Preferences.DependencyGraph

class PinPicsOnMapApplication : Application() {

    val dependencyGraph: DependencyGraph by lazy {
        DaggerDependencyGraph.factory().create(applicationContext)
    }

    override fun onCreate() {
        super.onCreate()
    }
}