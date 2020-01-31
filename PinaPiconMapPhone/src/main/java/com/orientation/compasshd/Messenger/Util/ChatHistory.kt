/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/29/20 4:08 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Messenger.Util

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.Query
import com.google.firebase.functions.FirebaseFunctions
import com.orientation.compasshd.Messenger.DataStructure.HistoryDataStructures
import com.orientation.compasshd.Messenger.Group.MessengerGroup
import com.orientation.compasshd.PinPicsOnMapApplication
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.Functions.FunctionsClass
import com.orientation.compasshd.Util.Functions.FunctionsClassDebug
import com.orientation.compasshd.Util.Functions.FunctionsClassUI
import kotlinx.android.synthetic.main.chat_history.*
import kotlinx.android.synthetic.main.item_chat_history.view.*
import javax.inject.Inject

class ChatHistory : Activity() {

    lateinit var functionsClass: FunctionsClass

    @Inject lateinit var functionsClassUI: FunctionsClassUI

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var firebaseRecyclerAdapter: FirestoreRecyclerAdapter<HistoryDataStructures, MessageViewHolder>
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var firebaseCloudFunctions: FirebaseFunctions

    private lateinit var linearLayoutManager: LinearLayoutManager

    lateinit var time: String

    var lastVisibleItem: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as PinPicsOnMapApplication)
                .dependencyGraph.inject(this@ChatHistory)
        setContentView(R.layout.chat_history)

        linearLayoutManager = LinearLayoutManager(this@ChatHistory, RecyclerView.VERTICAL, false)
        linearLayoutManager.stackFromEnd = false

        functionsClass = FunctionsClass(applicationContext)

        time = functionsClass.getTime()

        val window = window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = if (time == "day") {
            getColor(R.color.lighter)
        } else {
            getColor(R.color.darker)
        }
        window.navigationBarColor = if (time == "day") {
            getColor(R.color.light)
        } else {
            getColor(R.color.dark)
        }

        if (time == "day") {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            }
        } else {

        }

        fullView.setBackgroundColor(if (time == "day") {
            getColor(R.color.light)
        } else {
            getColor(R.color.dark)
        })

        recyclerView.setBackgroundColor(if (time == "day") {
            getColor(R.color.light)
        } else {
            getColor(R.color.dark)
        })

        if (time == "day") {
            chatToolbar.setBackgroundColor(getColor(R.color.lighter))

            chatSummary.setTextColor(getColor(R.color.light))
        } else {
            chatToolbar.setBackgroundColor(getColor(R.color.darker))

            chatSummary.setTextColor(getColor(R.color.dark))
        }

        chatSummary.text = Html.fromHtml("<big><b> History </b> Of</big> Chat With People")
        chatSummary.setTextColor(if (time == "day") {
            getColor(R.color.dark)
        } else {
            getColor(R.color.light)
        })

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        firebaseFirestore = FirebaseFirestore.getInstance()
        try {
            val firestoreSettings: FirebaseFirestoreSettings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(false)
                    .setSslEnabled(true)
                    .build()
            firebaseFirestore.firestoreSettings = firestoreSettings
        } catch (e: Exception) {
            e.printStackTrace()
        }
        firebaseCloudFunctions = FirebaseFunctions.getInstance()

        val historyCollectionReferencePath: String = "PinPicsOnMap/" +
                "People/" +
                "Profile/" +
                "${firebaseUser.uid}/" +
                "Groups/"

        val messagesCollectionReference: Query = firebaseFirestore
                .collection(historyCollectionReferencePath)
                .orderBy("JoinedTime", Query.Direction.ASCENDING)

        val firebaseRecyclerOptions = FirestoreRecyclerOptions.Builder<HistoryDataStructures>()
                .setQuery(
                        messagesCollectionReference,
                        HistoryDataStructures::class.java
                )
                .build()

        firebaseRecyclerAdapter =
                object : FirestoreRecyclerAdapter<HistoryDataStructures, MessageViewHolder>(firebaseRecyclerOptions) {
                    override fun onCreateViewHolder(viewGroup: ViewGroup, position: Int): MessageViewHolder {
                        progressBar.visibility = ProgressBar.INVISIBLE

                        lastVisibleItem = (this.snapshots.size - 1)

                        val inflater = LayoutInflater.from(viewGroup.context)
                        return MessageViewHolder(inflater.inflate(R.layout.item_chat_history, viewGroup, false))
                    }

                    override fun onBindViewHolder(
                            viewHolder: MessageViewHolder,
                            position: Int,
                            historyDataStructures: HistoryDataStructures) {
                        FunctionsClassDebug.PrintDebug("*** ${historyDataStructures} ***")

                        historyDataStructures.keyDatabase = this.snapshots.getSnapshot(position).id
                        if (historyDataStructures.theChatName != null) {
                            val nameOfChat = Html.fromHtml("<big><b>${historyDataStructures.CountryName}</b></big>" + " | " + "${historyDataStructures.CityName}")
                            viewHolder.theChatName.text = nameOfChat

                            viewHolder.theChatName.visibility = TextView.VISIBLE
                        }
                        viewHolder.theChatName.setTextColor(if (time == "day") {
                            getColor(R.color.dark)
                        } else {
                            getColor(R.color.light)
                        })

                        val countryNameDash = historyDataStructures.CountryName!!.toLowerCase().replace(" ", "-")
                        Glide.with(this@ChatHistory)
                                //https://assets.thebasetrip.com/api/v2/countries/flags/united-states.png
                                .load("https://assets.thebasetrip.com/api/v2/countries/flags/" + "${countryNameDash}" + ".png")
                                .listener(object : RequestListener<Drawable> {
                                    override fun onLoadFailed(
                                            glideException: GlideException?,
                                            any: Any?,
                                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                                            boolean: Boolean
                                    ): Boolean {

                                        return false
                                    }

                                    override fun onResourceReady(
                                            drawable: Drawable?,
                                            any: Any?,
                                            target: com.bumptech.glide.request.target.Target<Drawable>?,
                                            dataSource: DataSource?,
                                            boolean: Boolean
                                    ): Boolean {
                                        runOnUiThread {
                                            val backgroundColor = functionsClassUI.extractDominantColor(drawable!!)
                                            viewHolder.theChatName.setTextColor(backgroundColor)
                                        }

                                        return false
                                    }
                                })
                                .into(viewHolder.theChatImage)

                        viewHolder.fullItemView.setOnClickListener {
                            val intent: Intent = Intent(applicationContext, MessengerGroup::class.java).apply {
                                putExtra("fullGroupPath", historyDataStructures.GroupAddress)
                                putExtra("CountryName", historyDataStructures.CountryName)
                                putExtra("CityName", historyDataStructures.CityName)
                            }
                            startActivity(intent, ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
                        }
                    }

                    override fun onDataChanged() {

                    }

                    override fun onError(e: FirebaseFirestoreException) {
                        super.onError(e)
                        e.printStackTrace()
                    }
                }

        firebaseRecyclerAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                val friendlyMessageCount = firebaseRecyclerAdapter.itemCount
                val lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()
                if (lastVisiblePosition == -1 || positionStart >= friendlyMessageCount - 1 && lastVisiblePosition == positionStart - 1) {
                    Handler().postDelayed({
                        nestedScrollView.smoothScrollTo(
                                0,
                                recyclerView.height
                        )
                    }, 500)
                }
            }
        })

        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = firebaseRecyclerAdapter

        nestedScrollView.smoothScrollTo(
                0,
                recyclerView.height
        )
    }

    override fun onStart() {
        super.onStart()

        MobileAds.initialize(applicationContext, getString(R.string.ad_app_id))
        val adRequestBanner = AdRequest.Builder()
                .addTestDevice("65B5827710CBE90F4A99CE63099E524C")
                .addTestDevice("CDCAA1F20B5C9C948119E886B31681DE")
                .addTestDevice("D101234A6C1CF51023EE5815ABC285BD")
                .addTestDevice("5901E5EE74F9B6652E05621140664A54")
                .addTestDevice("F54D998BCE077711A17272B899B44798")
                .build()
        adView.loadAd(adRequestBanner)
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                FunctionsClassDebug.PrintDebug("$$$ Ads Loaded $$$")
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                adView.loadAd(adRequestBanner)
                FunctionsClassDebug.PrintDebug("$$$ Ads Error ${errorCode} $$$")
            }

            override fun onAdOpened() {

            }

            override fun onAdLeftApplication() {

            }

            override fun onAdClosed() {

            }
        }
    }

    override fun onResume() {
        super.onResume()
        firebaseRecyclerAdapter.startListening()
    }

    override fun onPause() {
        super.onPause()
        firebaseRecyclerAdapter.stopListening()
    }

    override fun onBackPressed() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        super.onBackPressed()
    }

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        var fullItemView: RelativeLayout

        var theChatImage: ImageView
        var theChatName: TextView

        init {
            fullItemView = itemView.fullItemView

            theChatImage = itemView.theChatImage
            theChatName = itemView.theChatName
        }
    }
}