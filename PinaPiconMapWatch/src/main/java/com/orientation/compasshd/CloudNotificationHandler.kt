/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd

import android.util.Log

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CloudNotificationHandler : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (BuildConfig.DEBUG) {
            Log.d(">>> ", "From: " + remoteMessage!!.from!!)
            Log.d(">>> ", "Notification Message Body: " + remoteMessage.notification!!.body!!)
        }
    }
}
