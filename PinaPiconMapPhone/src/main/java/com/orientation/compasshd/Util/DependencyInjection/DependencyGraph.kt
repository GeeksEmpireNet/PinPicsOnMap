/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/30/20 3:48 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.DependencyInjection.Preferences

import android.content.Context
import com.orientation.compasshd.Maps.PhoneMapsView
import com.orientation.compasshd.Messenger.Group.MessengerGroup
import com.orientation.compasshd.Messenger.Util.ChatHistory
import com.orientation.compasshd.Util.CloudNotificationHandler
import com.orientation.compasshd.Util.DependencyInjection.Converter.ConverterModule
import com.orientation.compasshd.Util.DependencyInjection.UI.UserInterfaceModule
import com.orientation.compasshd.Util.Functions.FunctionsClassUI
import com.orientation.compasshd.Util.SettingGUI
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton/*Each Module with Singleton Annotation Will Have Unique Instance*/
@Component(modules = [PreferencesModule::class, UserInterfaceModule::class, ConverterModule::class])
interface DependencyGraph {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance context: Context): DependencyGraph
    }

    fun inject(functionsClassUI: FunctionsClassUI)

    fun inject(phoneMapsView: PhoneMapsView)
    fun inject(settingGUI: SettingGUI)

    fun inject(chatHistory: ChatHistory)
    fun inject(messengerGroup: MessengerGroup)
    fun inject(messengerGroup: CloudNotificationHandler)
}