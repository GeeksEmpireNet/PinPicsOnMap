/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 11/11/19 7:26 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util.UserInformation

import com.google.firebase.firestore.FieldValue
import java.util.*

class UsersInformationDataStructure {

    fun UserInformationFirestore(
            theUserUID: String,
            theUserEmail: String, theUserName: String, theUserImageURL: String, theUserSignUp: FieldValue,
            userStates: String
    ): HashMap<Any, Any> {
        val linkedHashMapUserInformation = LinkedHashMap<Any, Any>()
        linkedHashMapUserInformation["theUserUID"] = theUserUID
        linkedHashMapUserInformation["theUserEmail"] = theUserEmail
        linkedHashMapUserInformation["theUserName"] = theUserName
        linkedHashMapUserInformation["theUserImageURL"] = theUserImageURL

        linkedHashMapUserInformation["theUserLastSignIn"] = FieldValue.serverTimestamp()
        linkedHashMapUserInformation["theUserSignUp"] = theUserSignUp
        linkedHashMapUserInformation["userStates"] = userStates

        return linkedHashMapUserInformation
    }
}
