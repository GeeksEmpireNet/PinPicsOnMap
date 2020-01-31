/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 4:08 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.orientation.compasshd.PinPicsOnMapApplication
import com.orientation.compasshd.Util.Functions.FunctionsClassDebug
import com.orientation.compasshd.Util.Functions.FunctionsClassUI
import com.orientation.compasshd.Util.Functions.PublicVariable
import net.geeksempire.chat.vicinity.Util.FunctionsClass.FunctionsClassConverter
import javax.inject.Inject

class CloudNotificationHandler : FirebaseMessagingService() {

    @Inject lateinit var functionsClassUI: FunctionsClassUI

    @Inject lateinit var functionsClassConverter: FunctionsClassConverter

    private lateinit var chatName: String
    private lateinit var communityLocation: String
    private lateinit var communityNickname: String

    private var myUID: String? = null
    private var targetUID: String? = null
    private var targetRegistrationToken: String? = null

    private lateinit var notificationLargeIcon: String
    private lateinit var notificationTitle: String
    private lateinit var notificationContent: String

    private lateinit var countryName: String
    private lateinit var cityName: String

    private lateinit var defaultColor: String
    private lateinit var titleColor: String
    private lateinit var contentColor: String

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        (applicationContext as PinPicsOnMapApplication)
                .dependencyGraph.inject(this@CloudNotificationHandler)

        val linkedHashMapData = remoteMessage.data
        FunctionsClassDebug.PrintDebug(linkedHashMapData)

        communityLocation = linkedHashMapData["CommunityLocation"].toString()
        chatName = linkedHashMapData["ChatName"].toString()
        communityNickname = linkedHashMapData["CommunityNickname"].toString()

        try {
            myUID = linkedHashMapData["myUID"]
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            targetUID = linkedHashMapData["targetUID"]
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            targetRegistrationToken = linkedHashMapData["targetRegistrationToken"].toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        notificationLargeIcon = linkedHashMapData["notificationLargeIcon"].toString()
        notificationTitle = linkedHashMapData["notificationTitle"].toString()

        val encryptedMessageContent = linkedHashMapData["notificationContent"].toString()
        notificationContent = functionsClassConverter.decryptEncodedData(functionsClassConverter.rawStringToByteArray(encryptedMessageContent), chatName).toString()

        countryName = linkedHashMapData["CountryName"].toString()
        cityName = linkedHashMapData["CityName"].toString()

        defaultColor = linkedHashMapData["defaultColor"].toString()
        titleColor = linkedHashMapData["contentColor"].toString()
        contentColor = linkedHashMapData["contentColor"].toString()

        //Private_Chat_Action || Community_Chat_Action
        val intent: Intent = Intent(linkedHashMapData["ChatAction"].toString())
        intent.putExtra("CommunityLocation", communityLocation)
        intent.putExtra("ChatName", chatName)
        intent.putExtra("CommunityNickname", communityNickname)
        if (targetUID != null) {
            intent.putExtra("targetUID", targetUID)
        }
        if (targetRegistrationToken != null) {
            intent.putExtra("targetRegistrationToken", targetRegistrationToken)
        }
        intent.putExtra("CountryName", countryName)
        intent.putExtra("CityName", cityName)
        intent.putExtra("fromNotification", true)
        val pendingIntent: PendingIntent = PendingIntent.getActivity(applicationContext, 666, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (myUID == null && chatName != PublicVariable.FRONT_INDIVIDUAL_CHAT_NAME) {
            Glide.with(applicationContext)
                    .load(notificationLargeIcon)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                                glideException: GlideException?,
                                any: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                exception: Boolean
                        ): Boolean {

                            return false
                        }

                        override fun onResourceReady(
                                drawable: Drawable?,
                                any: Any?,
                                target: com.bumptech.glide.request.target.Target<Drawable>?,
                                dataSource: DataSource?,
                                resources: Boolean
                        ): Boolean {
                            functionsClassUI
                                    .notificationCreator(
                                            chatName,
                                            functionsClassConverter.drawableToBitmap(drawable)!!,
                                            notificationTitle,
                                            notificationContent,
                                            (defaultColor.toInt()),
                                            (titleColor.toInt()),
                                            (contentColor.toInt()),
                                            System.currentTimeMillis().toInt(),//As Notification Id
                                            NotificationManager.IMPORTANCE_DEFAULT,
                                            pendingIntent
                                    )

                            return false
                        }
                    })
                    .submit()
        } else if (myUID != null) {
            if (myUID != FirebaseAuth.getInstance().currentUser!!.uid && chatName != PublicVariable.FRONT_GROUP_NAME) {
                Glide.with(applicationContext)
                        .load(notificationLargeIcon)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                    glideException: GlideException?,
                                    any: Any?,
                                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                                    exception: Boolean
                            ): Boolean {

                                return false
                            }

                            override fun onResourceReady(
                                    drawable: Drawable?,
                                    any: Any?,
                                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                                    dataSource: DataSource?,
                                    resources: Boolean
                            ): Boolean {
                                functionsClassUI
                                        .notificationCreator(
                                                chatName,
                                                functionsClassConverter.drawableToBitmap(drawable)!!,
                                                notificationTitle,
                                                notificationContent,
                                                (defaultColor.toInt()),
                                                (titleColor.toInt()),
                                                (contentColor.toInt()),
                                                System.currentTimeMillis().toInt(),//As Notification Id
                                                NotificationManager.IMPORTANCE_DEFAULT,
                                                pendingIntent
                                        )

                                return false
                            }
                        })
                        .submit()
            }
        }
    }

    override fun onNewToken(RegistrationToken: String) {
        if (FirebaseAuth.getInstance().currentUser != null) {
            try {
                val registrationTokenDatabaseReference = FirebaseDatabase.getInstance().reference
                        .child("AuthUsers")
                        .child("Private")
                        .child("RegistrationToken")
                        .child(FirebaseAuth.getInstance().currentUser!!.uid)
                registrationTokenDatabaseReference.setValue(RegistrationToken)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
