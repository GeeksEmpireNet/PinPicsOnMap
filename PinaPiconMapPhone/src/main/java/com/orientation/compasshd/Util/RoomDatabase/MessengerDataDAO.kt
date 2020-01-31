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

import androidx.room.*

@Dao
interface MessengerDataDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertNewWidgetData(vararg arrayOfMessengerDataModels: MessengerDataModel)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateWidgetData(vararg arrayOfMessengerDataModels: MessengerDataModel)

    @Delete
    fun delete(messengerDataModel: MessengerDataModel)

    @Query("SELECT * FROM MessengerData ORDER BY AppName ASC")
    fun getAllWidgetData(): List<MessengerDataModel>

    @Query("SELECT * FROM MessengerData WHERE WidgetId IN (:WidgetId)")
    fun loadWidgetById(WidgetId: Int): MessengerDataModel

    @Query("UPDATE MessengerData SET WidgetLabel = :WidgetLabel WHERE WidgetId = :WidgetId")
    fun updateWidgetLabelByWidgetId(WidgetId: Int, WidgetLabel: String): Int

    @Query("UPDATE MessengerData SET Recovery = :AddedWidgetRecovery WHERE WidgetId = :WidgetId")
    fun updateRecoveryByWidgetId(WidgetId: Int, AddedWidgetRecovery: Boolean): Int

    @Query("DELETE FROM MessengerData WHERE WidgetId = :WidgetId")
    fun deleteByWidgetId(WidgetId: Int)
}