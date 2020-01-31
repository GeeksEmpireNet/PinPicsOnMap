/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/30/20 3:38 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.DependencyInjection.UI

import com.orientation.compasshd.Util.Functions.FunctionsClassUI
import com.orientation.compasshd.Util.Functions.InterfaceUI
import dagger.Binds
import dagger.Module

@Module
abstract class UserInterfaceModule {

    @Binds
    abstract fun provideUI(functionsClassUI: FunctionsClassUI/*This is Instance Of Return Type*/): InterfaceUI
}