/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Messenger.DataStructure

import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

class MessagesDataStructures {

    var keyDatabase: String? = null
    var NotificationTopic: String? = null
    var theUserLocationLatitude: String? = null
    var theUserLocationLongitude: String? = null
    var theUserEmail: String? = null
    var theUserName: String? = null
    var theUserImageURL: String? = null
    var theUserUniqueId: String? = null
    var theTextMessage: String? = null
    var theImageMessageURL: String? = null
    var theMessageSeen: String? = null
    var theMessageSent: String? = null
    var SeenByNumber: String = "0"
    var defaultColor: String? = null
    var theUserInEmergency: String? = null

    @ServerTimestamp
    var messageTime: Timestamp? = null
    @ServerTimestamp
    var messageTimeEdit: Timestamp? = null

    var theUserLocationEdit: GeoPoint? = null

    constructor() {

    }

    constructor(NotificationTopic: String,
                theUserLocationLatitude: String, theUserLocationLongitude: String, theUserLocationEdit: GeoPoint,
                theTextMessage: String, theImageMessageURL: String,
                theUserEmail: String, theUserName: String, theUserImageURL: String, theMessageSent: String, theMessageSeen: String, SeenByNumber: String, theUserUniqueId: String,
                messageTime: Timestamp,
                messageTimeEdit: Timestamp,
                defaultColor: String,
                theUserInEmergency: String) {
        this.NotificationTopic = NotificationTopic

        this.theUserLocationLatitude = theUserLocationLatitude
        this.theUserLocationLongitude = theUserLocationLongitude
        this.theUserLocationEdit = theUserLocationEdit

        this.theTextMessage = theTextMessage
        this.theImageMessageURL = theImageMessageURL

        this.theUserEmail = theUserEmail
        this.theUserName = theUserName
        this.theUserImageURL = theUserImageURL
        this.theUserUniqueId = theUserUniqueId
        this.theMessageSent = theMessageSent
        this.theMessageSeen = theMessageSeen
        this.SeenByNumber = SeenByNumber

        this.defaultColor = defaultColor

        this.messageTime = messageTime
        this.messageTimeEdit = messageTimeEdit

        this.theUserInEmergency = theUserInEmergency
    }
}
