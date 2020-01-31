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

class PeopleDataStructures {

    var keyDatabase: String? = null
    var theUserUID: String? = null
    var theUserEmail: String? = null
    var theUserName: String? = null
    var theUserImageURL: String? = null
    var userStates: String? = null

    @ServerTimestamp
    var theUserLastSignIn: Timestamp? = null
    @ServerTimestamp
    var theUserSignUp: Timestamp? = null


    constructor() {

    }

    constructor(theUserUID: String,
                theUserEmail: String, theUserName: String, theUserImageURL: String,
                userStates: String,
                theUserLastSignIn: Timestamp, theUserSignUp: Timestamp) {
        this.theUserUID = theUserUID
        this.theUserEmail = theUserEmail
        this.theUserName = theUserName
        this.theUserImageURL = theUserImageURL
        this.userStates = userStates

        this.theUserLastSignIn = theUserLastSignIn
        this.theUserSignUp = theUserSignUp
    }
}
