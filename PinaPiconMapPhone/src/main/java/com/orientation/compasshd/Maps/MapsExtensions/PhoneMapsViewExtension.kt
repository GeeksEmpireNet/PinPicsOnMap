package com.orientation.compasshd.Maps.MapsExtensions

import android.app.SearchManager
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
import com.orientation.compasshd.Maps.PhoneMapsView
import com.orientation.compasshd.Maps.ViewModel.PhoneMapsViewModel
import kotlinx.android.synthetic.main.maps_view.*
import java.util.*

fun PhoneMapsView.createViewModelObserver() : PhoneMapsViewModel {

    val phoneMapsViewModel = ViewModelProvider(this).get(PhoneMapsViewModel::class.java)

    phoneMapsViewModel.weatherInformation.observe(this,
            Observer { weatherInformation ->
                weatherInfoView.visibility = View.VISIBLE
                weatherIconView.visibility = View.VISIBLE

                weatherSpinLoading.visibility = View.INVISIBLE
                weatherSpinLoadingInfo.visibility = View.INVISIBLE

                weatherInfoView.text = Html.fromHtml(weatherInformation)

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