/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/30/20 4:14 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.Functions

import android.content.Context
import androidx.preference.PreferenceManager
import javax.inject.Inject
import javax.inject.Singleton

interface InterfacePreferences {
    fun savePreference(PreferenceName: String, KEY: String, VALUE: Any)
    fun saveDefaultPreference(KEY: String, VALUE: Any)

    fun readPreference(PreferenceName: String, KEY: String, defaultVALUE: Any): Any
    fun readDefaultPreference(KEY: String, defaultVALUE: Any): Any
}
@Singleton
class FunctionsClassPreferences @Inject constructor (var context: Context) : InterfacePreferences {

    companion object {
        /**
         * Key = path
         */
        const val COMPASS_IMAGE_FILE_PATH = "path"
        /**
         * Key = sizePref
         */
        const val COMPASS_VIEW_SIZE_MODE = "sizePref"
        /**
         * Key = size
         */
        const val COMPASS_VIEW_SIZE_WH = "size"
        /**
         * Key = mini
         */
        const val COMPASS_MINIMIZE_MODE = "mini"
        /**
         * Key = autoMini
         */
        const val COMPASS_AUTO_MINIMIZE = "autoMini"
    }

    inner class CompassConfiguration {

        fun saveCompassConfig(dataKey: String, dataToSave: Any?) {
            context.getSharedPreferences(".Config", Context.MODE_PRIVATE).edit().apply {

                when (dataToSave) {
                    is String -> {
                        this.putString(dataKey, dataToSave)
                    }
                    is Int -> {
                        this.putInt(dataKey, dataToSave)
                    }
                    is Boolean -> {
                        this.putBoolean(dataKey, dataToSave)
                    }
                }

                this.apply()
            }
        }

        fun readCompassConfig(dataKey: String, defaultVALUE: Any?) : Any? {
            val sharedPreferences = context.getSharedPreferences(".Config", Context.MODE_PRIVATE)

            return when (defaultVALUE) {
                is String -> {
                    return sharedPreferences.getString(dataKey, defaultVALUE as String?)
                }
                is Int -> {
                    return sharedPreferences.getInt(dataKey, defaultVALUE as Int)
                }
                is Boolean -> {
                    return sharedPreferences.getBoolean(dataKey, defaultVALUE as Boolean)
                }
                else -> {

                }
            }
        }
    }

    /**
     * Save Data To SharedPreference of Given PreferenceName File
     */
    override fun savePreference(PreferenceName: String, KEY: String, VALUE: Any) {
        context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE).edit().apply {

            when (VALUE) {
                is String -> {
                    this.putString(KEY, VALUE)
                }
                is Int -> {
                    this.putInt(KEY, VALUE)
                }
                is Long -> {
                    this.putLong(KEY, VALUE)
                }
                is Boolean -> {
                    this.putBoolean(KEY, VALUE)
                }
            }

            this.apply()
        }
    }

    /**
     * Save Data To Default SharedPreference
     */
    override fun saveDefaultPreference(KEY: String, VALUE: Any) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().apply {

            when (VALUE) {
                is String -> {
                    this.putString(KEY, VALUE)
                }
                is Int -> {
                    this.putInt(KEY, VALUE)
                }
                is Long -> {
                    this.putLong(KEY, VALUE)
                }
                is Boolean -> {
                    this.putBoolean(KEY, VALUE)
                }
            }

            this.apply()
        }
    }

    /**
     * Read Data From SharedPreference of Given PreferenceName File
     */
    override fun readPreference(PreferenceName: String, KEY: String, defaultVALUE: Any): Any {
        val sharedPreferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)

        return when (defaultVALUE) {
            is String -> {
                return sharedPreferences.getString(KEY, defaultVALUE as String).toString()
            }
            is Int -> {
                return sharedPreferences.getInt(KEY, defaultVALUE as Int)
            }
            is Long -> {
                return sharedPreferences.getLong(KEY, defaultVALUE as Long)
            }
            is Boolean -> {
                return sharedPreferences.getBoolean(KEY, defaultVALUE as Boolean)
            }
            else -> {

            }
        }
    }

    /**
     * Read Data From Default SharedPreference
     */
    override fun readDefaultPreference(KEY: String, defaultVALUE: Any): Any {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        return when (defaultVALUE) {
            is String -> {
                return sharedPreferences.getString(KEY, defaultVALUE as String).toString()
            }
            is Int -> {
                return sharedPreferences.getInt(KEY, defaultVALUE as Int)
            }
            is Long -> {
                return sharedPreferences.getLong(KEY, defaultVALUE as Long)
            }
            is Boolean -> {
                return sharedPreferences.getBoolean(KEY, defaultVALUE as Boolean)
            }
            else -> {

            }
        }
    }
}