/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.LocationData

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import com.google.android.gms.maps.model.LatLng
import com.orientation.compasshd.Util.Functions.FunctionsClassMaps

class FetchAddressIntentService : IntentService("FetchAddressIntentService") {

    private var resultReceiver: ResultReceiver? = null

    override fun onHandleIntent(intent: Intent?) {
        //if(intent == null) {return}
        intent ?: return

        when (intent.action) {
            "LocationData" -> {
                resultReceiver = intent.getParcelableExtra<ResultReceiver>(Constants.RESULT_RECEIVER)

                val locationInfo = intent.getParcelableExtra<Location>(Constants.LOCATION_DATA_EXTRA)

                var geoAddresses: ArrayList<Address>? = null
                try {
                    geoAddresses = FunctionsClassMaps(applicationContext).loadLocationInfo(
                            LatLng(
                                    locationInfo.latitude,
                                    locationInfo.longitude
                            )
                    )
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }

                // Handle case where no address was found.
                if (geoAddresses != null) {
                    if (geoAddresses!!.isEmpty()) {
                        deliverResultToReceiver(Constants.FAILURE_RESULT, null)
                    } else {
                        deliverResultToReceiver(Constants.SUCCESS_RESULT, geoAddresses[0].getAddressLine(0))
                    }
                } else {
                    deliverResultToReceiver(Constants.FAILURE_RESULT, null)
                }
            }
            "LatLongData" -> {
                resultReceiver = intent.getParcelableExtra<ResultReceiver>(Constants.RESULT_RECEIVER)

                val locationInfo = intent.getParcelableExtra<LatLng>(Constants.LOCATION_DATA_EXTRA)

                var geoAddresses: ArrayList<Address>? = null
                try {
                    geoAddresses = FunctionsClassMaps(applicationContext).loadLocationInfo(
                            LatLng(
                                    locationInfo.latitude,
                                    locationInfo.longitude
                            )
                    )
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }

                // Handle case where no address was found.
                if (geoAddresses != null) {
                    if (geoAddresses!!.isEmpty()) {
                        deliverResultToReceiver(Constants.FAILURE_RESULT, null)
                    } else {
                        deliverResultToReceiver(Constants.SUCCESS_RESULT, geoAddresses[0].getAddressLine(0))
                    }
                } else {
                    deliverResultToReceiver(Constants.FAILURE_RESULT, null)
                }
            }
            else -> {
                deliverResultToReceiver(Constants.FAILURE_RESULT, null)
            }
        }


    }

    private fun deliverResultToReceiver(resultCode: Int, hintMessage: String?) {
        val bundle = Bundle().apply {
            putString(Constants.RESULT_DATA_KEY, hintMessage)
        }
        resultReceiver?.send(resultCode, bundle)
    }


    object Constants {
        const val SUCCESS_RESULT = 0
        const val FAILURE_RESULT = 1
        const val PACKAGE_NAME = "net.geeksempire.chat.vicinity"
        const val RESULT_RECEIVER = "$PACKAGE_NAME.RECEIVER"
        const val RESULT_DATA_KEY = "$PACKAGE_NAME.RESULT_DATA_KEY"
        const val LOCATION_DATA_EXTRA = "$PACKAGE_NAME.LOCATION_DATA_EXTRA"
    }
}
