/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.Functions

import android.text.Spannable

class PublicVariable {
    companion object {
        var Latitude: Double = 0.000
        var Longitude: Double = 0.000

        var eligibleToLoadShowAds: Boolean = false
        var GROUP_CHAT_STATE_BACKGROUND: Boolean = false

        var PUBLIC_INTERNET_IP: String? = null
        var LOCATION_INFO_DETAIL: String? = null
        var LOCATION_INFO_DETAIL_HTML: Spannable? = null
        var LOCATION_COUNTRY_NAME: String? = null
        var LOCATION_STATE_PROVINCE: String? = null
        var LOCATION_CITY_NAME: String? = null
        var LOCATION_KNOWN_NAME: String? = null
        var FRONT_GROUP_NAME: String? = null
        var FRONT_INDIVIDUAL_CHAT_NAME: String? = null

        const val SUBSCRIBE_TO_TOPIC_SUCCESSFUL = "SubscribeToTopicSuccessful"
        const val SUBSCRIBE_TO_TOPIC_FAILED = "SubscribeToTopicFailed"
        const val MESSENGER_DATA_DATABASE_NAME = "MessengerData"
    }
}
