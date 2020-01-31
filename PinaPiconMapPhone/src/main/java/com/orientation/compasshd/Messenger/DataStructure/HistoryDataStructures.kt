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
import com.google.firebase.firestore.ServerTimestamp

class HistoryDataStructures {

    var keyDatabase: String? = null
    var GroupAddress: String? = null
    var theChatName: String? = null
    var ISO: String? = null
    var CountryName: String? = null
    var CityName: String? = null
    var lastLocationDetail: String? = null

    var lastLocationLatitude: Double? = null
    var lastLocationLongitude: Double? = null

    @ServerTimestamp
    var JoinedTime: Timestamp? = null

    constructor() {

    }

    constructor(GroupAddress: String,
                theChatName: String,
                ISO: String, CountryName: String, CityName: String,
                lastLocationDetail: String, lastLocationLatitude: Double, lastLocationLongitude: Double,
                JoinedTime: Timestamp) {
        this.GroupAddress = GroupAddress
        this.theChatName = theChatName

        this.ISO = ISO
        this.CountryName = CountryName
        this.CityName = CityName
        this.lastLocationDetail = lastLocationDetail

        this.lastLocationLatitude = lastLocationLatitude
        this.lastLocationLongitude = lastLocationLongitude

        this.JoinedTime = JoinedTime
    }
}
