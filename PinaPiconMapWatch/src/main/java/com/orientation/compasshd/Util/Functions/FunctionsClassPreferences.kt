/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.Functions

import android.app.Activity
import android.content.Context
import android.preference.PreferenceManager

class FunctionsClassPreferences {

    lateinit var activity: Activity
    lateinit var context: Context

    constructor(activity: Activity, context: Context) {
        this.activity = activity
        this.context = context
    }

    constructor(context: Context) {
        this.context = context
    }

    init {

    }

    /*File IO*/
    fun savePreference(PreferenceName: String, KEY: String, VALUE: String?) {
        val sharedPreferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        val editorSharedPreferences = sharedPreferences.edit()
        editorSharedPreferences.putString(KEY, VALUE)
        editorSharedPreferences.apply()
    }

    fun savePreference(PreferenceName: String, KEY: String, VALUE: Int) {
        val sharedPreferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        val editorSharedPreferences = sharedPreferences.edit()
        editorSharedPreferences.putInt(KEY, VALUE)
        editorSharedPreferences.apply()
    }

    fun savePreference(PreferenceName: String, KEY: String, VALUE: Long) {
        val sharedPreferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        val editorSharedPreferences = sharedPreferences.edit()
        editorSharedPreferences.putLong(KEY, VALUE)
        editorSharedPreferences.apply()
    }

    fun savePreference(PreferenceName: String, KEY: String, VALUE: Boolean) {
        val sharedPreferences = context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE)
        val editorSharedPreferences = sharedPreferences.edit()
        editorSharedPreferences.putBoolean(KEY, VALUE)
        editorSharedPreferences.apply()
    }

    fun saveDefaultPreference(KEY: String, VALUE: Int) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editorSharedPreferences = sharedPreferences.edit()
        editorSharedPreferences.putInt(KEY, VALUE)
        editorSharedPreferences.apply()
    }

    fun saveDefaultPreference(KEY: String, VALUE: String) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editorSharedPreferences = sharedPreferences.edit()
        editorSharedPreferences.putString(KEY, VALUE)
        editorSharedPreferences.apply()
    }

    fun saveDefaultPreference(KEY: String, VALUE: Boolean) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val editorSharedPreferences = sharedPreferences.edit()
        editorSharedPreferences.putBoolean(KEY, VALUE)
        editorSharedPreferences.apply()
    }

    fun readPreference(PreferenceName: String, KEY: String, defaultVALUE: String?): String? {
        return context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE).getString(KEY, defaultVALUE)
    }

    fun readPreference(PreferenceName: String, KEY: String, defaultVALUE: Int): Int {
        return context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE).getInt(KEY, defaultVALUE)
    }

    fun readPreference(PreferenceName: String, KEY: String, defaultVALUE: Long): Long {
        return context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE).getLong(KEY, defaultVALUE)
    }

    fun readPreference(PreferenceName: String, KEY: String, defaultVALUE: Boolean): Boolean {
        return context.getSharedPreferences(PreferenceName, Context.MODE_PRIVATE).getBoolean(KEY, defaultVALUE)
    }

    fun readDefaultPreference(KEY: String, defaultVALUE: Int): Int {
        return PreferenceManager.getDefaultSharedPreferences(context).getInt(KEY, defaultVALUE)
    }

    fun readDefaultPreference(KEY: String, defaultVALUE: String): String? {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(KEY, defaultVALUE)
    }

    fun readDefaultPreference(KEY: String, defaultVALUE: Boolean): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(KEY, defaultVALUE)
    }
}