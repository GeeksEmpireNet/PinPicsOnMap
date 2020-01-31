/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Messenger.Util


import android.app.IntentService
import android.content.Intent
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.orientation.compasshd.Util.Functions.FunctionsClassDebug
import java.util.*

class UserStatesIntentService : IntentService("UserStatesIntentService") {

    override fun onHandleIntent(intent: Intent?) {
        FunctionsClassDebug.PrintDebug("UserStatesIntentService.Action == " + intent!!.action)
        when (intent.action) {
            "ACTION_OFFLINE" -> {

                val linkedHashMapData: LinkedHashMap<Any, Any> = LinkedHashMap<Any, Any>()
                linkedHashMapData["CountryName"] = intent.getStringExtra("CountryName")
                linkedHashMapData["CityName"] = intent.getStringExtra("CityName")
                linkedHashMapData["ChatName"] = intent.getStringExtra("ChatName")
                linkedHashMapData["UID"] = intent.getStringExtra("UID")

                val firebaseCloudFunctions = FirebaseFunctions.getInstance()
                firebaseCloudFunctions
                        .getHttpsCallable(
                                if (intent.getBooleanExtra("UserChatType", true)) {
                                    "offlinehUserGroupChatStatus"
                                } else {
                                    "offlinehUserIndividualChatStatus"
                                }
                        )
                        .call(linkedHashMapData)
                        .continueWith { task ->
                            val resultToContinueWith = task.result?.data as Map<String, Any>

                            resultToContinueWith
                        }
                        .addOnCompleteListener { task ->
                            if (!task.isSuccessful) {
                                val e = task.exception
                                if (e is FirebaseFunctionsException) {
                                    val code = e.code.toString()
                                    val details = e.details.toString()
                                }

                            }

                        }

            }
            "ACTION_ONLINE" -> {

                val linkedHashMapData: LinkedHashMap<Any, Any> = LinkedHashMap<Any, Any>()
                linkedHashMapData["CountryName"] = intent.getStringExtra("CountryName")
                linkedHashMapData["CityName"] = intent.getStringExtra("CityName")
                linkedHashMapData["ChatName"] = intent.getStringExtra("ChatName")
                linkedHashMapData["UID"] = intent.getStringExtra("UID")

                val firebaseCloudFunctions = FirebaseFunctions.getInstance()
                firebaseCloudFunctions
                        .getHttpsCallable(
                                if (intent.getBooleanExtra("UserChatType", true)) {
                                    "onlinehUserGroupChatStatus"
                                } else {
                                    "onlinehUserIndividualChatStatus"
                                }
                        )
                        .call(linkedHashMapData)
                        .continueWith { task ->
                            val resultToContinueWith = task.result?.data as Map<String, Any>

                            resultToContinueWith
                        }
                        .addOnCompleteListener { task ->

                            if (!task.isSuccessful) {
                                val e = task.exception
                                if (e is FirebaseFunctionsException) {

                                    val code = e.code.toString()
                                    val details = e.details.toString()

                                }
                            }
                        }
            }
        }
    }
}
