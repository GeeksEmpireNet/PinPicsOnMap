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

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.orientation.compasshd.Util.Functions.PublicVariable

@Entity(tableName = PublicVariable.MESSENGER_DATA_DATABASE_NAME)
data class MessengerDataModel(
        @NonNull @PrimaryKey var WidgetId: Int,

        @NonNull @ColumnInfo(name = "PackageName") var PackageName: String,
        @NonNull @ColumnInfo(name = "AppName") var AppName: String,
        @ColumnInfo(name = "WidgetLabel") var WidgetLabel: String,
        @ColumnInfo(name = "Recovery") var Recovery: Boolean
)