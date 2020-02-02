package com.orientation.compasshd.Maps.MapsExtensions

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.text.Html
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.orientation.compasshd.Maps.PhoneMapsView
import com.orientation.compasshd.Maps.ViewModel.PhoneMapsViewModel
import com.orientation.compasshd.Util.Functions.FunctionsClassDebug
import com.orientation.compasshd.Util.Functions.FunctionsClassPreferences
import com.orientation.compasshd.Util.Functions.PublicVariable
import com.orientation.compasshd.Util.UserInformation.UsersInformationDataStructure
import kotlinx.android.synthetic.main.maps_view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

fun PhoneMapsView.createViewModelObserver() : PhoneMapsViewModel {

    val phoneMapsViewModel = ViewModelProvider(this).get(PhoneMapsViewModel::class.java)

    phoneMapsViewModel.weatherInformation.observe(this,
            Observer { weatherInformation ->
                weatherInfoView.visibility = View.VISIBLE
                weatherIconView.visibility = View.VISIBLE

                weatherSpinLoading.visibility = View.INVISIBLE
                weatherSpinLoadingInfo.visibility = View.INVISIBLE

                weatherInfoView.text = Html.fromHtml(weatherInformation)!!

                weatherIconView.colorFilter = null
                weatherIconView.setPadding(3, 3, 3, 3)

                Glide.with(this)
                        .load(phoneMapsViewModel.weatherIcon)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(glideException: GlideException?, any: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, boolean: Boolean): Boolean {

                                return false
                            }

                            override fun onResourceReady(drawable: Drawable?, any: Any?, target: com.bumptech.glide.request.target.Target<Drawable>?, dataSource: DataSource?, boolean: Boolean): Boolean {
                                runOnUiThread {
                                    weatherIconView.setImageDrawable(drawable)

                                    weatherInfoView.setOnClickListener {
                                        val searchIntent = Intent(Intent.ACTION_WEB_SEARCH)
                                        searchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                        searchIntent.putExtra(SearchManager.QUERY, "${phoneMapsViewModel.countryName} ${phoneMapsViewModel.cityName} Forecast")
                                        startActivity(searchIntent)
                                    }
                                    weatherIconView.setOnClickListener {
                                        val searchIntent = Intent(Intent.ACTION_WEB_SEARCH)
                                        searchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                                        searchIntent.putExtra(SearchManager.QUERY, "${phoneMapsViewModel.countryName} ${phoneMapsViewModel.cityName} Forecast")
                                        startActivity(searchIntent)
                                    }
                                }

                                return false
                            }
                        })
                        .submit()

                try {
                    indexAppInfo(phoneMapsViewModel.cityName, phoneMapsViewModel.temperature, phoneMapsViewModel.weatherIcon,
                            Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                            Calendar.getInstance().get(Calendar.MINUTE))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            })

    return phoneMapsViewModel
}

fun extractDataOfExistenceUsers(context: Context, functionsClassPreferences: FunctionsClassPreferences) = CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    if (firebaseUser != null) {
        val firestoreDatabase = FirebaseFirestore.getInstance()


        val cityNames = arrayListOf<String>()
        val savedLocations = arrayListOf<LatLng>()
        val hashKeyCityLocation = HashMap<String, LatLng>()
        FunctionsClassDebug.PrintDebug("*** ${cityNames} | ${savedLocations} ***")

        val listAllFiles = File(context.externalMediaDirs[0].path, File.separator + "PinPicsOnMap").listFiles()
        listAllFiles.forEach {
            FunctionsClassDebug.PrintDebug("*** ${it} ***")

            val listAllCityNames = it.listFiles()
            listAllCityNames.forEach { fileName ->
                //CityName_
                //(latitude,longitude)_
                //System.currentTimeMillis().JPEG
                val fileNameText = fileName.name
                val finalNameTextSplits = fileNameText.split("_")

                val cityName = finalNameTextSplits[0]
                cityNames.add(cityName)

                val locationText = finalNameTextSplits[1].replace("(", "").replace(")", "").split(",")
                val latitude = locationText[0]
                val longitude = locationText[1]
                val location = LatLng(latitude.toDouble(), longitude.toDouble())
                savedLocations.add(location)

                hashKeyCityLocation[cityName] = location
                FunctionsClassDebug.PrintDebug("*** ${finalNameTextSplits[0]} | ${location} ***")
            }
        }

        val hashSet = HashSet<String>(cityNames)
        cityNames.clear()
        cityNames.addAll(hashSet)

        val usersInformationDataStructure = UsersInformationDataStructure().UserInformationFirestore(
                firebaseUser.uid,
                firebaseUser.email!!,
                firebaseUser.displayName!!,
                firebaseUser.photoUrl.toString(),
                FieldValue.serverTimestamp(),
                true.toString()
        )

        hashKeyCityLocation.forEach {
            firestoreDatabase.document(
                    "PinPicsOnMap/" +
                            "Messenger/" +
                            "${PublicVariable.LOCATION_COUNTRY_NAME}/" +
                            "${it.key/*CityName*/}/" +
                            "People/" +
                            "${firebaseUser.uid}/"
            ).set(usersInformationDataStructure).addOnSuccessListener {
                functionsClassPreferences.saveDefaultPreference("OldUser", false)

            }.addOnCanceledListener {


            }

            val linkedHashMapUserLocation = LinkedHashMap<Any, Any>()
            linkedHashMapUserLocation["locationLatitude"] = it.value.latitude
            linkedHashMapUserLocation["locationLongitude"] = it.value.longitude
            firestoreDatabase.collection(
                    "PinPicsOnMap/" +
                            "Messenger/" +
                            "${PublicVariable.LOCATION_COUNTRY_NAME}/" +
                            "${it.key/*CityName*/}/" +
                            "People/" +
                            "${firebaseUser.uid}/" +
                            "Visited-${it.value.latitude}-${it.value.longitude}"
            ).add(linkedHashMapUserLocation).addOnSuccessListener {

            }.addOnCanceledListener {

            }
        }



    }


}