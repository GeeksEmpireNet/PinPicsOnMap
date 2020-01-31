/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/30/20 3:37 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.DependencyInjection.Preferences

import com.orientation.compasshd.Util.Functions.FunctionsClassPreferences
import com.orientation.compasshd.Util.Functions.InterfacePreferences
import dagger.Binds
import dagger.Module

@Module
abstract class PreferencesModule {

    @Binds
    abstract fun provideSharedPreferences(functionsClassPreferences: FunctionsClassPreferences/*This is Instance Of Return Type*/): InterfacePreferences
}