/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/30/20 3:38 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.DependencyInjection.Converter

import dagger.Binds
import dagger.Module
import net.geeksempire.chat.vicinity.Util.FunctionsClass.FunctionsClassConverter
import net.geeksempire.chat.vicinity.Util.FunctionsClass.InterfaceConverter

@Module
abstract class ConverterModule {

    @Binds
    abstract fun provideConverter(functionsClassConverter: FunctionsClassConverter/*This is Instance Of Return Type*/): InterfaceConverter
}