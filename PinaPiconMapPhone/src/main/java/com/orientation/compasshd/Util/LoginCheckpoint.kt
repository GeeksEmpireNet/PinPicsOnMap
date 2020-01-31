/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 1:45 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Util

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.orientation.compasshd.BindService
import com.orientation.compasshd.Compass.TimeCheckCompass
import com.orientation.compasshd.Maps.TimeCheckMap
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.Functions.FunctionsClass
import com.orientation.compasshd.Util.Functions.FunctionsClassDebug
import com.orientation.compasshd.Util.Functions.FunctionsClassPreferences
import kotlinx.android.synthetic.main.login_waiting.*

class LoginCheckpoint : Activity() {

    lateinit var functionsClass: FunctionsClass
    lateinit var functionsClassPreferences: FunctionsClassPreferences

    lateinit var TimeCheckReference: String
    lateinit var timeDayNight: String

    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.login_waiting)

        functionsClass = FunctionsClass(this@LoginCheckpoint)
        functionsClassPreferences = FunctionsClassPreferences(this@LoginCheckpoint)

        TimeCheckReference = intent.getStringExtra("TimeCheck")
        timeDayNight = TimeCheckCompass.time

        if (timeDayNight == "day") {
            spinKit.setColor(getColor(R.color.light))
        } else if (timeDayNight == "night") {
            spinKit.setColor(getColor(R.color.red))
        }

        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
        window.statusBarColor = if (TimeCheckCompass.time == "day") getColor(R.color.default_color_light) else getColor(R.color.default_color_darker)
        window.navigationBarColor = if (TimeCheckCompass.time == "day") getColor(R.color.default_color_light) else getColor(R.color.default_color_darker)

        try {
            if (functionsClass.networkConnection()) {
                if (FirebaseAuth.getInstance().currentUser == null) {
                    firebaseAuth = FirebaseAuth.getInstance()

                    firebaseAuth.addAuthStateListener { firebaseAuth ->
                        val user = firebaseAuth.currentUser
                        if (user == null) {

                        } else {

                        }
                    }

                    val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.webClientId))
                            .requestEmail()
                            .build()

                    val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
                    try {
                        googleSignInClient.signOut()
                        googleSignInClient.revokeAccess()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                    val signInIntent = googleSignInClient.signInIntent
                    startActivityForResult(signInIntent, 666)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    public override fun onResume() {
        super.onResume()
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 666) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val googleSignInAccount = task.getResult(ApiException::class.java)
                val authCredential = GoogleAuthProvider.getCredential(googleSignInAccount!!.idToken, null)
                firebaseAuth.signInWithCredential(authCredential)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val firebaseUser = firebaseAuth.currentUser
                                if (firebaseUser != null) {
                                    println("Firebase Activities Done Successfully")

                                    val historyCollectionReferencePath: String = "PinPicsOnMap/" +
                                            "People/" +
                                            "Profile/" +
                                            "${firebaseUser.uid}/" +
                                            "Groups/"

                                    FirebaseFirestore.getInstance()
                                            .collection(historyCollectionReferencePath)
                                            .orderBy("JoinedTime", Query.Direction.ASCENDING)
                                            .get().addOnSuccessListener { querySnapshots ->
                                                querySnapshots.documents.forEach { documentSnapshot ->
                                                    FunctionsClassDebug.PrintDebug("*** DocumentSnapshot == ${documentSnapshot} ***")
                                                    functionsClassPreferences.savePreference(".AlreadyBeenHere", documentSnapshot["theChatName"].toString(), true)
                                                }

                                                TimeCheckCompass.run = false
                                                stopService(Intent(applicationContext, BindService::class.java))

                                                val intent = Intent()
                                                if (TimeCheckReference == "TimeCheckCompass") {
                                                    intent.setClass(this@LoginCheckpoint, TimeCheckCompass::class.java)
                                                } else if (TimeCheckReference == "TimeCheckMap") {
                                                    intent.setClass(this@LoginCheckpoint, TimeCheckMap::class.java)
                                                } else {
                                                    intent.setClass(this@LoginCheckpoint, TimeCheckMap::class.java)
                                                }
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                startActivity(intent)

                                                finish()
                                            }.addOnFailureListener {
                                                it.printStackTrace()
                                            }
                                }
                            } else {

                            }
                        }
            } catch (e: ApiException) {
                e.printStackTrace()
            }

        }
    }
}
