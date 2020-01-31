/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geekstools.floatshort.PRO.Widget.RoomDatabase

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [MessengerDataModel::class], version = 1, exportSchema = false)
abstract class MessengerDataInterface : RoomDatabase() {
    abstract fun initDataAccessObject(): MessengerDataDAO
}