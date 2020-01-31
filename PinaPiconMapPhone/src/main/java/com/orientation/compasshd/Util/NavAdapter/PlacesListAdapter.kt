/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.NavAdapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.location.Location
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.Functions.FunctionsClass
import com.orientation.compasshd.Util.LocationData.Coordinates
import java.util.*

class PlacesListAdapter : BaseAdapter {

    lateinit var context: Context

    internal var functionClass: FunctionsClass

    lateinit var navDrawerItems: ArrayList<NavDrawerItem>

    lateinit var coordinates: Coordinates
    lateinit var location: Location

    internal var icons: Array<ImageView>
    internal var titles: Array<TextView>

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    lateinit var time: String

    constructor(context: Context, navDrawerItems: ArrayList<NavDrawerItem>, time: String, coordinates: Coordinates) {
        this.context = context
        this.navDrawerItems = navDrawerItems
        this.coordinates = coordinates
        this.time = time

        this.latitude = coordinates.latitude
        this.longitude = coordinates.longitude

        functionClass = FunctionsClass(context)

        icons = Array<ImageView>(navDrawerItems.size) { ImageView(context) }
        titles = Array<TextView>(navDrawerItems.size) { TextView(context) }
    }

    constructor(context: Context, navDrawerItems: ArrayList<NavDrawerItem>, time: String, location: Location) {
        this.context = context
        this.navDrawerItems = navDrawerItems
        this.location = location
        this.time = time

        this.latitude = location.latitude
        this.longitude = location.longitude

        functionClass = FunctionsClass(context)

        icons = Array<ImageView>(navDrawerItems.size) { ImageView(context) }
        titles = Array<TextView>(navDrawerItems.size) { TextView(context) }
    }

    init {

    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val layoutInflater: LayoutInflater = context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = layoutInflater.inflate(R.layout.popup_items, null)
        }

        val popup_item = convertView!!.findViewById<View>(R.id.popup_item) as RelativeLayout
        icons[position] = convertView.findViewById<View>(R.id.icon) as ImageView
        titles[position] = convertView.findViewById<View>(R.id.title) as TextView

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
        titles[position].text = functionClass.editPlaceType(navDrawerItems[position].getTitle())
        icons[position].setImageDrawable(navDrawerItems[position].getIcon())

        popup_item.setOnClickListener(View.OnClickListener { view ->
            val where = Intent(Intent.ACTION_VIEW)
            //where.setData(Uri.parse("geo:51.528308,-0.3817807?z=13&q=Restaurants"));
            where.data = Uri.parse(
                    "geo:" + latitude + "," + longitude
                            + "?z=13"
                            + "&"
                            + "q=" + navDrawerItems[position].getTitle())
            where.setPackage("com.google.android.apps.maps")
            where.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(where)
            println("Latitude: " + latitude + "," + "Longitude: "
                    + longitude + " >>> " + navDrawerItems[position].getTitle())
        })

        return convertView
    }

    override fun getItem(position: Int): Any {
        return navDrawerItems[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return navDrawerItems.size
    }
}