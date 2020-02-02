/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 4:08 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Compass.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.location.LocationManager
import android.net.Uri
import android.os.Handler
import android.text.Html
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.firebase.appindexing.Action
import com.google.firebase.appindexing.FirebaseAppIndex
import com.google.firebase.appindexing.FirebaseUserActions
import com.google.firebase.appindexing.Indexable
import com.google.firebase.appindexing.builders.Actions
import com.orientation.compasshd.Compass.FloatingCompass
import com.orientation.compasshd.Compass.FloatingCompassMini
import com.orientation.compasshd.Maps.PhoneMapsView
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.DataHolder.ListItemsDataHolder
import com.orientation.compasshd.Util.Functions.FunctionsClass
import com.orientation.compasshd.Util.Functions.FunctionsClassDebug
import com.orientation.compasshd.Util.LocationData.Coordinates
import com.orientation.compasshd.Util.SettingGUI
import java.util.*

class FloatingCompassPopupOptionAdapter(
        var context: Context, var floatingCompassInstance: FloatingCompass, var listItemsDataHolders: ArrayList<ListItemsDataHolder>, var time: String, var anchorView: View) : BaseAdapter() {

    var functionsClass: FunctionsClass = FunctionsClass(context)
    var coordinates: Coordinates

    var icons: Array<ImageView>
    var titles: Array<TextView>
    lateinit var weatherIconView: ImageView
    lateinit var weatherInfoView: TextView
    lateinit var weatherIconBitmap: Bitmap

    var latitude: Double = 0.toDouble()
    var longitude: Double = 0.toDouble()

    lateinit var getCity: String
    lateinit var getUrl: String

    init {
        coordinates = Coordinates(context)
        latitude = coordinates.latitude
        longitude = coordinates.longitude

        icons = Array<ImageView>(listItemsDataHolders.size) { ImageView(context) }
        titles = Array<TextView>(listItemsDataHolders.size) { TextView(context) }
    }

    override fun getCount(): Int {
        return listItemsDataHolders.size
    }

    override fun getItem(position: Int): Any {
        return listItemsDataHolders[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val mInflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = mInflater.inflate(R.layout.popup_items, null)
        }

        val popup_item = convertView!!.findViewById<View>(R.id.popup_item) as RelativeLayout
        icons[position] = convertView.findViewById<View>(R.id.icon) as ImageView
        titles[position] = convertView.findViewById<View>(R.id.title) as TextView

        weatherIconView = icons[0]
        weatherInfoView = titles[0]

        val itemRippleDrawable = context.getDrawable(R.drawable.popup_ripple_item) as RippleDrawable
        val gradientDrawableItem = itemRippleDrawable.findDrawableByLayerId(android.R.id.mask) as GradientDrawable
        if (time == "day") {
            itemRippleDrawable.setColor(ColorStateList.valueOf(context.getColor(R.color.darker)))
            gradientDrawableItem.setColor(context.getColor(R.color.darker))
            icons[position].setColorFilter(context.getColor(R.color.dark))
            titles[position].setTextColor(context.getColor(R.color.darker))
        } else if (time == "night") {
            itemRippleDrawable.setColor(ColorStateList.valueOf(context.getColor(R.color.red)))
            gradientDrawableItem.setColor(context.getColor(R.color.red))
            icons[position].setColorFilter(context.getColor(R.color.red))
            titles[position].setTextColor(context.getColor(R.color.red))
        }

        popup_item.background = itemRippleDrawable
        titles[position].text = listItemsDataHolders[position].getTitle()
        icons[position].setImageDrawable(listItemsDataHolders[position].getIcon())

        popup_item.setOnClickListener(View.OnClickListener {
            val itemId = listItemsDataHolders[position].getId()
            when (itemId) {
                0 -> {
                    floatingCompassInstance.downloadWeatherInformation()
                }
                1 -> {
                    if ((context.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                        val mapView = Intent(context, PhoneMapsView::class.java)
                        mapView.putExtra("time", time)
                        mapView.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(mapView)
                    } else {
                        val gpsOptionsIntent = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        gpsOptionsIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        gpsOptionsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(gpsOptionsIntent)

                        try {
                            Handler().postDelayed({
                                val m = Intent(context, FloatingCompassMini::class.java)
                                m.putExtra("time", time)
                                m.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                context.startService(m)
                            }, 5)

                            context.sendBroadcast(Intent("Close_Compass"))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }
                2 -> {
                    optionMenuPlaceType(context, anchorView, time, coordinates)
                }
                3 -> {
                    val set = Intent(context, SettingGUI::class.java)
                    set.putExtra("time", time)
                    set.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(set)
                }
                4 -> {
                    try {
                        Handler().postDelayed({
                            val m = Intent(context, FloatingCompassMini::class.java)
                            m.putExtra("time", time)
                            m.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startService(m)
                        }, 5)

                        context.sendBroadcast(Intent("Close_Compass"))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                5 -> {
                    context.sendBroadcast(Intent("Close_Compass"))
                }
            }
        })

        //temp + humidity + weather
        try {
            weatherInfoView.text = Html.fromHtml(
                    "<b>" + FloatingCompass.temperature + "</b>" + "<big>°ᶜ</big>" + " " + "<small>" + FloatingCompass.weather + "</small><br/>"
                            + "<small>" + "\uD83D\uDCA6" + " " + FloatingCompass.humidity + "%</small>"
            )
            weatherIconView.colorFilter = null
            weatherIconView.setImageBitmap(FloatingCompass.weatherConditionIcon)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            indexAppInfo(FloatingCompass.city, FloatingCompass.temperature, FloatingCompass.weatherIconLink!!,
                    Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                    Calendar.getInstance().get(Calendar.MINUTE))
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {

        }

        if (!functionsClass.networkConnection()) {
            try {
                weatherInfoView.text = Html.fromHtml(
                        "<b>" + context.getString(R.string.no_internet) + "</b>"
                                + "<br/>" + "<small><b>" + context.getString(R.string.refresh) + "</b></small>"
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        return convertView
    }

    @Throws(Exception::class)
    fun indexAppInfo(cityName: String, temperature: String, weatherJSONIconUrl: String, hrs: Int, mins: Int) {
        FirebaseAppIndex.getInstance().removeAll()

        getCity = "${cityName} | ${temperature}°ᶜ | ${hrs}:${mins}"
        getUrl = BASE_URL.buildUpon().appendPath(getCity).build().toString()

        val articleToIndex = Indexable.Builder()
                .setName(getCity)
                .setUrl(getUrl)
                .setImage(weatherJSONIconUrl)
                .build()

        val updateTask = FirebaseAppIndex.getInstance().update(articleToIndex)
        updateTask.addOnSuccessListener { println("Indexed") }
        updateTask.addOnFailureListener { e -> e.printStackTrace() }

        val startTask = FirebaseUserActions.getInstance().start(getAction(getCity, getUrl))
        startTask.addOnSuccessListener { println("Indexed") }
        startTask.addOnFailureListener { e -> e.printStackTrace() }
    }

    fun getAction(titleForAction: String, urlForAction: String): Action {
        return Actions.newView(titleForAction, urlForAction)
    }

    fun optionMenuPlaceType(context: Context, anchorView: View, time: String, coordinates: Coordinates) {
        val popupPlaceType = ListPopupWindow(context)
        val navDrawerItem = ArrayList<ListItemsDataHolder>()
        val popupItemsText = functionsClass.readFromAssets(context, "place_type")
        val popupItemsIcon = context.getDrawable(R.drawable.ic_pin_current_day)

        FunctionsClassDebug.PrintDebug("*** ${popupItemsText.indices} ***")

        for (i in popupItemsText.indices) {
            navDrawerItem.add(ListItemsDataHolder(
                    popupItemsText[i],
                    popupItemsIcon!!,
                    i)
            )
        }
        val placesListAdapter = PlacesListAdapter(context, navDrawerItem, time, coordinates)

        val Height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 297f, context.resources.displayMetrics).toInt()
        val Width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 175f, context.resources.displayMetrics).toInt()

        popupPlaceType.setAdapter(placesListAdapter)
        popupPlaceType.anchorView = anchorView
        popupPlaceType.width = Width
        popupPlaceType.height = Height
        popupPlaceType.isModal = true
        popupPlaceType.setOnDismissListener { navDrawerItem.clear() }

        if (time == "day") {
            val day = context.getDrawable(R.drawable.popup_background)
            day!!.setTint(context.getColor(R.color.light))
            popupPlaceType.setBackgroundDrawable(day)
        } else if (time == "night") {
            val night = context.getDrawable(R.drawable.popup_background)
            night!!.setTint(context.getColor(R.color.dark))
            popupPlaceType.setBackgroundDrawable(context.getDrawable(R.drawable.popup_background))
        }
        popupPlaceType.show()
        if (functionsClass.returnAPI() < 23) {
            if (popupPlaceType.isShowing) {
                Handler().postDelayed({
                    //popupPlaceType.dismiss();
                }, 2000)
            }
        }
    }

    companion object {

        private val BASE_URL = Uri.parse("https://www.geeksempire.net/PinPicsOnMap.html/")
    }
}