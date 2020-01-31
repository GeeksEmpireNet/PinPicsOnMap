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
import android.view.View
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.Functions.PublicVariable

class SplitStreetViewMap : androidx.fragment.app.FragmentActivity(),
        OnMapReadyCallback,
        GoogleMap.OnMarkerDragListener,
        StreetViewPanorama.OnStreetViewPanoramaChangeListener,
        StreetViewPanorama.OnStreetViewPanoramaCameraChangeListener,
        StreetViewPanorama.OnStreetViewPanoramaLongClickListener {

    lateinit var googleStreetView: StreetViewPanorama
    lateinit var googleMap: GoogleMap
    lateinit var marker: Marker

    lateinit var supportMapFragment: SupportMapFragment
    lateinit var streetViewPanoramaFragment: SupportStreetViewPanoramaFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.split_street_view_panorama_and_map)

        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        if (intent.hasExtra("LocationX") && intent.hasExtra("LocationY")) {
            val LocationX = intent.getDoubleExtra("LocationX", 48.8586074)
            val LocationY = intent.getDoubleExtra("LocationY", 2.2947401)
            LocationXY = LatLng(LocationX, LocationY)
        }
        val markerPosition = LocationXY

        streetViewPanoramaFragment = supportFragmentManager.findFragmentById(R.id.streetviewpanorama) as SupportStreetViewPanoramaFragment
        streetViewPanoramaFragment.retainInstance = true

        streetViewPanoramaFragment.getStreetViewPanoramaAsync { panorama ->
            googleStreetView = panorama
            googleStreetView!!.isStreetNamesEnabled = true
            googleStreetView!!.isUserNavigationEnabled = true
            googleStreetView!!.isZoomGesturesEnabled = true
            googleStreetView!!.isPanningGesturesEnabled = true
            googleStreetView!!.setOnStreetViewPanoramaChangeListener(this@SplitStreetViewMap)

            val streetViewPanoramaLocation = panorama.location
            if (streetViewPanoramaLocation == null) {
                (findViewById<View>(R.id.streetError) as TextView).visibility = View.VISIBLE
            }
        }

        supportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.retainInstance = true
        supportMapFragment.getMapAsync { map ->
            googleMap = map
            map.setOnMarkerDragListener(this@SplitStreetViewMap)
            map.uiSettings.isCompassEnabled = false
            map.uiSettings.isZoomControlsEnabled = false
            map.uiSettings.setAllGesturesEnabled(true)
            map.uiSettings.isMapToolbarEnabled = true
            map.uiSettings.isIndoorLevelPickerEnabled = true

            map.isIndoorEnabled = true
            map.isTrafficEnabled = true
            map.mapType = GoogleMap.MAP_TYPE_HYBRID

            marker = map.addMarker(MarkerOptions()
                    .position(markerPosition)
                    .draggable(true))

            val cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(LocationXY, 17f)
            map.animateCamera(cameraUpdateFactory)
        }

        val pinnedLocation = findViewById<View>(R.id.pinnedLocation) as ImageView
        pinnedLocation.setOnClickListener { view ->
            view.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_location_button))
            val cameraUpdateFactory = CameraUpdateFactory.newLatLngZoom(LocationXY, 17f)
            googleMap!!.animateCamera(cameraUpdateFactory)
            googleStreetView!!.setPosition(LocationXY)

            marker!!.remove()
            marker = googleMap!!.addMarker(MarkerOptions()
                    .position(LocationXY)
                    .draggable(true))
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onResume() {
        super.onResume()
        PublicVariable.eligibleToLoadShowAds = true
    }

    override fun onPause() {
        super.onPause()
        PublicVariable.eligibleToLoadShowAds = false
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(MARKER_POSITION_KEY, marker!!.position)
    }

    override fun onMapReady(googleMap: GoogleMap) {}

    override fun onMarkerDragStart(marker: Marker) {

    }

    override fun onMarkerDrag(marker: Marker) {

    }

    override fun onMarkerDragEnd(marker: Marker) {
        googleStreetView!!.setPosition(marker.position)
    }

    override fun onStreetViewPanoramaChange(streetViewPanoramaLocation: StreetViewPanoramaLocation?) {
        if (streetViewPanoramaLocation != null /*&& streetViewPanoramaLocation.links != null*/) {
            marker!!.position = streetViewPanoramaLocation.position
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

        private val MARKER_POSITION_KEY = "MarkerPosition"

        private var LocationXY = LatLng(48.8586074, 2.2947401)
    }
}
