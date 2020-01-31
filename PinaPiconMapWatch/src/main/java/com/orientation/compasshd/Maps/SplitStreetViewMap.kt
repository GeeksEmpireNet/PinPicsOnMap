/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Maps

import android.os.Bundle
import android.support.wearable.activity.WearableActivityDelegate
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.orientation.compasshd.R
import kotlinx.android.synthetic.main.split_street_view_panorama_and_map.*

class SplitStreetViewMap : androidx.fragment.app.FragmentActivity(),
        OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener,
        StreetViewPanorama.OnStreetViewPanoramaChangeListener,
        StreetViewPanorama.OnStreetViewPanoramaCameraChangeListener,
        StreetViewPanorama.OnStreetViewPanoramaLongClickListener {

    lateinit var googleStreetView: StreetViewPanorama
    lateinit var supportStreetViewPanoramaFragment: SupportStreetViewPanoramaFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.split_street_view_panorama_and_map)

        if (intent.hasExtra("LocationX") && intent.hasExtra("LocationY")) {
            val LocationX = intent.getDoubleExtra("LocationX", 48.8586074)
            val LocationY = intent.getDoubleExtra("LocationY", 2.2947401)
            LocationXY = LatLng(LocationX, LocationY)
        }

        supportStreetViewPanoramaFragment = supportFragmentManager.findFragmentById(R.id.streetviewpanorama) as SupportStreetViewPanoramaFragment
        supportStreetViewPanoramaFragment.getStreetViewPanoramaAsync { panorama ->
            googleStreetView = panorama
            googleStreetView!!.isStreetNamesEnabled = true
            googleStreetView!!.isUserNavigationEnabled = true
            googleStreetView!!.isZoomGesturesEnabled = true
            googleStreetView!!.isPanningGesturesEnabled = true
            googleStreetView!!.setOnStreetViewPanoramaChangeListener(this@SplitStreetViewMap)
            googleStreetView!!.setOnStreetViewPanoramaCameraChangeListener(this@SplitStreetViewMap)
            googleStreetView!!.setOnStreetViewPanoramaLongClickListener(this@SplitStreetViewMap)

            val streetViewPanoramaLocation = panorama.location
            if (streetViewPanoramaLocation == null) {
                (findViewById<View>(R.id.streetError) as TextView).visibility = View.VISIBLE
            }
        }

        val wearableActivityDelegate = WearableActivityDelegate(object : WearableActivityDelegate.AmbientCallback {
            override fun onExitAmbient() {

            }

            override fun onUpdateAmbient() {

            }

            override fun onEnterAmbient(ambientDetails: Bundle?) {

            }
        })
        wearableActivityDelegate.setAmbientEnabled()

        val pinnedLocation = findViewById<View>(R.id.pinnedLocation) as ImageView

        pinnedLocation.setOnClickListener { view ->
            view.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_location_button))
            val cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(LocationXY, 17f)
            googleStreetView!!.setPosition(LocationXY)
        }

        backButton.setOnClickListener {
            this@SplitStreetViewMap.finish()
        }

        (findViewById<View>(R.id.streetError) as TextView).setOnLongClickListener {
            true
        }
    }

    public override fun onStart() {
        super.onStart()
    }

    public override fun onDestroy() {
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
    }

    override fun onMapReady(googleMap: GoogleMap) {

    }

    override fun onMarkerDragStart(marker: Marker) {

    }

    override fun onMarkerDrag(marker: Marker) {

    }

    override fun onMarkerDragEnd(marker: Marker) {
        googleStreetView!!.setPosition(marker.position)
    }

    override fun onStreetViewPanoramaChange(streetViewPanoramaLocation: StreetViewPanoramaLocation?) {
        if (streetViewPanoramaLocation != null /*&& streetViewPanoramaLocation.links != null*/) {
            if ((findViewById<View>(R.id.streetError) as TextView).isShown) {
                (findViewById<View>(R.id.streetError) as TextView).visibility = View.INVISIBLE
            }
        } else {
            (findViewById<View>(R.id.streetError) as TextView).visibility = View.VISIBLE
        }
    }

    override fun onPointerCaptureChanged(hasCapture: Boolean) {

    }

    override fun onStreetViewPanoramaCameraChange(streetViewPanoramaCamera: StreetViewPanoramaCamera) {

    }

    override fun onStreetViewPanoramaLongClick(streetViewPanoramaOrientation: StreetViewPanoramaOrientation) {

    }

    companion object {

        private var LocationXY = LatLng(48.8586074, 2.2947401)
    }
}
