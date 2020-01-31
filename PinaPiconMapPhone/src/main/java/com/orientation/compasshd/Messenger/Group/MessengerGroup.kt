/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 5:17 PM
 * Last modified 1/30/20 5:17 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package com.orientation.compasshd.Messenger.Group

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.TypedValue
import android.view.*
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.widget.NestedScrollView
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
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.*
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.messaging.FirebaseMessaging
import com.orientation.compasshd.Messenger.DataStructure.MessagesDataStructures
import com.orientation.compasshd.Messenger.DataStructure.PeopleDataStructures
import com.orientation.compasshd.Messenger.Util.UserStatesIntentService
import com.orientation.compasshd.PinPicsOnMapApplication
import com.orientation.compasshd.R
import com.orientation.compasshd.Util.Functions.FunctionsClass
import com.orientation.compasshd.Util.Functions.FunctionsClassDebug
import com.orientation.compasshd.Util.Functions.FunctionsClassUI
import com.orientation.compasshd.Util.Functions.PublicVariable
import com.orientation.compasshd.Util.NavAdapter.NavDrawerItem
import com.orientation.compasshd.Util.NavAdapter.PlacesListAdapter
import kotlinx.android.synthetic.main.group_messenger.*
import kotlinx.android.synthetic.main.group_messenger.view.*
import kotlinx.android.synthetic.main.item_group_messeger_all_people.view.*
import kotlinx.android.synthetic.main.item_group_messeger_others.view.*
import kotlinx.android.synthetic.main.item_group_messeger_self.view.*
import net.geeksempire.chat.vicinity.Util.FunctionsClass.FunctionsClassConverter
import java.util.*
import javax.inject.Inject
import kotlin.collections.LinkedHashMap
import kotlin.collections.set

class MessengerGroup : Activity() {

    lateinit var functionsClass: FunctionsClass

    @Inject lateinit var functionsClassConverter: FunctionsClassConverter
    @Inject lateinit var functionsClassUI: FunctionsClassUI

    private lateinit var firebaseAnalytics: FirebaseAnalytics

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseUser: FirebaseUser

    private lateinit var firebaseRecyclerAdapter: FirestoreRecyclerAdapter<MessagesDataStructures, MessageViewHolder>
    private lateinit var firebaseRecyclerAdapterPeople: FirestoreRecyclerAdapter<PeopleDataStructures, PeopleViewHolder>
    private lateinit var firebaseFirestore: FirebaseFirestore

    private lateinit var firebaseCloudFunctions: FirebaseFunctions

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var linearLayoutManagerPeople: LinearLayoutManager

    var defaultColorNotification: Int = 0
    var titleColorNotification: Int = 0
    var contentColorNotification: Int = 0

    var lastVisibleItem: Int = 0

    private var lastMessageId: String? = null

    var fullGroupPath: String? = null
    lateinit var time: String

    private var ANONYMOUS = "ANONYMOUS"
    private var MESSAGE_SENT_EVENT = "COMMUNITY_MESSAGE_SENT"

    var lastTranslate = 0.0f;

    companion object {
        lateinit var location: Location

        var countryName: String = "UK"
        var cityName: String = "London"

        var notificationTopic: String = "London"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        (application as PinPicsOnMapApplication)
                .dependencyGraph.inject(this@MessengerGroup)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.group_messenger)

        linearLayoutManager = LinearLayoutManager(this@MessengerGroup, RecyclerView.VERTICAL, false)
        linearLayoutManager.stackFromEnd = false

        linearLayoutManagerPeople = LinearLayoutManager(this@MessengerGroup, RecyclerView.VERTICAL, false)
        linearLayoutManagerPeople.stackFromEnd = false

        functionsClass = FunctionsClass(applicationContext)

        functionsClassUI.Toast("DI", Gravity.TOP)

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

        fullMessageView.setBackgroundColor(if (time == "day") {
            getColor(R.color.light)
        } else {
            getColor(R.color.dark)
        })

        messageRecyclerView.setBackgroundColor(if (time == "day") {
            getColor(R.color.light)
        } else {
            getColor(R.color.dark)
        })

        if (intent.hasExtra("fullGroupPath")) {
            fullGroupPath = intent.getStringExtra("fullGroupPath")

            countryName = intent.getStringExtra("CountryName")
            cityName = intent.getStringExtra("CityName")
        } else {
            countryName = intent.getStringExtra("CountryName")
            cityName = intent.getStringExtra("CityName")
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(this@MessengerGroup)

        val countryNameTopic = countryName.replace(" ", "")
        val cityNameTopic = cityName.replace(" ", "")

        notificationTopic = "${countryNameTopic}-${cityNameTopic}"

        try {
            FirebaseMessaging.getInstance().subscribeToTopic("${countryNameTopic}-${cityNameTopic}").addOnSuccessListener {

                val bundle: Bundle = Bundle()
                bundle.putString("LocationName", "${countryNameTopic}-${cityNameTopic}")
                firebaseAnalytics.logEvent(PublicVariable.SUBSCRIBE_TO_TOPIC_SUCCESSFUL, bundle)
            }.addOnFailureListener {

                val bundle: Bundle = Bundle()
                bundle.putString("LocationName", "${countryNameTopic}-${cityNameTopic}")
                firebaseAnalytics.logEvent(PublicVariable.SUBSCRIBE_TO_TOPIC_FAILED, bundle)
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()

            val bundle: Bundle = Bundle()
            bundle.putString("LocationName", "${countryNameTopic}-${cityNameTopic}")
            firebaseAnalytics.logEvent(PublicVariable.SUBSCRIBE_TO_TOPIC_FAILED, bundle)
        }

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseUser = firebaseAuth.currentUser!!

        val chatLogo: ImageView = chatToolbar.chatLogo
        val chatSummary: TextView = chatToolbar.chatSummary

        if (time == "day") {
            chatToolbar.setBackgroundColor(getColor(R.color.lighter))
            adView.setBackgroundColor(getColor(R.color.lighter))

            chatSummary.setTextColor(getColor(R.color.light))
            messageEditText.setTextColor(getColor(R.color.dark))

            nestedScrollViewPeople.setBackgroundColor(getColor(R.color.light_transparent))
        } else {
            chatToolbar.setBackgroundColor(getColor(R.color.darker))
            adView.setBackgroundColor(getColor(R.color.darker))

            chatSummary.setTextColor(getColor(R.color.dark))
            messageEditText.setTextColor(getColor(R.color.light))

            nestedScrollViewPeople.setBackgroundColor(getColor(R.color.dark_transparent))
        }

        chatSummary.text = Html.fromHtml("<big><b>${countryName}</b></big> | ${cityName}")
        chatSummary.setTextColor(if (time == "day") {
            getColor(R.color.dark)
        } else {
            getColor(R.color.light)
        })

        if (PublicVariable.LOCATION_COUNTRY_NAME == countryName && PublicVariable.LOCATION_CITY_NAME == cityName) {
            chatSummary.text = PublicVariable.LOCATION_INFO_DETAIL_HTML

            val firebaseDatabaseCountriesLocation: FirebaseDatabase =
                    FirebaseDatabase.getInstance("https://ppom-countries-information.firebaseio.com/")
            val databaseReferenceCountriesLocation = firebaseDatabaseCountriesLocation.reference
                    .child("CountriesInformation")
                    .child(functionsClass.getCountryIso())
            databaseReferenceCountriesLocation.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val countryNameDash = dataSnapshot.child("Country").value.toString().toLowerCase().replace(" ", "-")
                    Glide.with(this@MessengerGroup)
                            //https://assets.thebasetrip.com/api/v2/countries/flags/united-states.png
                            //https://www.countryflags.io/US/flat/64.png
                            .load("https://assets.thebasetrip.com/api/v2/countries/flags/" + "${countryNameDash}" + ".png")
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                        glideException: GlideException?,
                                        any: Any?,
                                        drawable: com.bumptech.glide.request.target.Target<Drawable>?,
                                        boolean: Boolean
                                ): Boolean {
                                    return false
                                }

                                override fun onResourceReady(
                                        drawable: Drawable?,
                                        any: Any?,
                                        targetDrawable: com.bumptech.glide.request.target.Target<Drawable>?,
                                        dataSource: DataSource?,
                                        boolean: Boolean
                                ): Boolean {
                                    runOnUiThread {
                                        chatLogo.setImageDrawable(drawable)
                                    }

                                    return false
                                }
                            })
                            .submit()
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }

        Glide.with(this@MessengerGroup)
                .load(firebaseUser.photoUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                            glideException: GlideException?,
                            any: Any?,
                            drawable: com.bumptech.glide.request.target.Target<Drawable>?,
                            boolean: Boolean
                    ): Boolean {
                        return false
                    }

                    override fun onResourceReady(
                            drawable: Drawable?,
                            any: Any?,
                            targetDrawable: com.bumptech.glide.request.target.Target<Drawable>?,
                            dataSource: DataSource?,
                            boolean: Boolean
                    ): Boolean {
                        defaultColorNotification = functionsClassUI.extractDominantColor(drawable!!)
                        titleColorNotification = functionsClassUI.extractVibrantColor(drawable)
                        contentColorNotification = functionsClassUI.extractDarkMutedColor(drawable)

                        return false
                    }
                })
                .submit()

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

        val messagesCollectionReferencePath: String = if (intent.hasExtra("fullGroupPath")) {
            fullGroupPath!!
        } else {
            "PinPicsOnMap/" +
                    "Messenger/" +
                    "${countryName}/" +
                    "${cityName}/" +
                    "GroupsMessages/"
        }

        val messagesCollectionReference: Query = firebaseFirestore
                .collection(messagesCollectionReferencePath)
                .orderBy("messageTime", Query.Direction.ASCENDING)

        val firebaseRecyclerOptions = FirestoreRecyclerOptions.Builder<MessagesDataStructures>()
                .setQuery(
                        messagesCollectionReference,
                        MessagesDataStructures::class.java
                )
                .build()

        firebaseRecyclerAdapter =
                object : FirestoreRecyclerAdapter<MessagesDataStructures, MessageViewHolder>(firebaseRecyclerOptions) {
                    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MessageViewHolder {
                        progressBar.visibility = ProgressBar.INVISIBLE

                        lastVisibleItem = (this.snapshots.size - 1)

                        val inflater = LayoutInflater.from(viewGroup.context)
                        var messageViewHolder: MessageViewHolder
                        messageViewHolder = if (this.snapshots[viewType].theUserUniqueId == firebaseUser.uid) {
                            MessageViewHolder(applicationContext, inflater.inflate(R.layout.item_group_messeger_self, viewGroup, false), self = true)
                        } else {
                            MessageViewHolder(applicationContext, inflater.inflate(R.layout.item_group_messeger_others, viewGroup, false), self = false)
                        }
                        return messageViewHolder
                    }

                    override fun getItemViewType(position: Int): Int {

                        return position
                    }

                    override fun onBindViewHolder(
                            viewHolder: MessageViewHolder,
                            position: Int,
                            chatMessagesDataStructure: MessagesDataStructures) {

                        chatMessagesDataStructure.keyDatabase = this.snapshots.getSnapshot(position).id
                        if (chatMessagesDataStructure.theMessageSent != null && firebaseUser.uid == chatMessagesDataStructure.theUserUniqueId) {
                            if (chatMessagesDataStructure.theMessageSent.toString().toBoolean() || lastMessageId == chatMessagesDataStructure.keyDatabase) {
                                viewHolder.theMessageSent.visibility = View.VISIBLE
                            } else {
                                viewHolder.theMessageSent.visibility = View.INVISIBLE

                            }
                        } else {
                            viewHolder.theMessageSent.visibility = View.INVISIBLE
                        }

                        if (chatMessagesDataStructure.theTextMessage != null) {
                            val encryptedMessageContent = chatMessagesDataStructure.theTextMessage
                            viewHolder.theTextMessage.setText(functionsClassConverter.decryptEncodedData(functionsClassConverter.rawStringToByteArray(encryptedMessageContent.toString()), cityName))

                            viewHolder.theTextMessage.visibility = TextView.VISIBLE
                            viewHolder.theTextMessage.isEnabled = (firebaseUser.uid == chatMessagesDataStructure.theUserUniqueId)
                            viewHolder.theTextMessage.isFocusable = (firebaseUser.uid == chatMessagesDataStructure.theUserUniqueId)
                        }

                        viewHolder.theUserName.text = chatMessagesDataStructure.theUserName

                        try {
                            if (chatMessagesDataStructure.theUserUniqueId == firebaseUser.uid && chatMessagesDataStructure.SeenByNumber != null && chatMessagesDataStructure.SeenByNumber.toInt() > 0) {
                                viewHolder.seenByNumber.text = chatMessagesDataStructure.SeenByNumber
                                viewHolder.seenByNumber.visibility = View.VISIBLE
                            } else {
                                viewHolder.seenByNumber.visibility = View.INVISIBLE
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                        if (chatMessagesDataStructure.theUserImageURL == null) {
                            viewHolder.theUserImage.setImageDrawable(null)
                        } else {
                            Glide.with(this@MessengerGroup)
                                    .load(chatMessagesDataStructure.theUserImageURL)
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
                                            viewHolder.theUserImage.contentDescription = getString(R.string.theUserImageHint, chatMessagesDataStructure.theUserName)

                                            val profileImageColor = functionsClassUI.extractVibrantColor(drawable!!)
                                            viewHolder.theUserName.setTextColor(profileImageColor)

                                            return false
                                        }
                                    })
                                    .into(viewHolder.theUserImage)
                        }

                        viewHolder.theTextMessage.addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, end: Int) {

                            }

                            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                                if (firebaseUser.uid == chatMessagesDataStructure.theUserUniqueId) {
                                    viewHolder.theEditButton.visibility = if (charSequence.toString().trim { it <= ' ' }.isNotEmpty()) {
                                        View.VISIBLE
                                    } else {
                                        View.INVISIBLE
                                    }
                                    viewHolder.theEditButton.isEnabled = charSequence.toString().trim { it <= ' ' }.isNotEmpty()
                                }
                            }

                            override fun afterTextChanged(editable: Editable) {

                            }
                        })

                        viewHolder.theEditButton.visibility = View.INVISIBLE
                        viewHolder.theEditButton.isEnabled = false

                        viewHolder.theEditButton.setOnClickListener { view ->
                            if (firebaseUser.uid == chatMessagesDataStructure.theUserUniqueId) {
                                val plainTextMessage = viewHolder.theTextMessage.text.toString()
                                val updatedMessageContent = functionsClassConverter.encryptEncodedData(plainTextMessage, cityName).asList().toString()

                                firebaseFirestore
                                        .document(
                                                "${messagesCollectionReferencePath}" +
                                                        "${chatMessagesDataStructure.keyDatabase}/"
                                        )
                                        .update(
                                                "theTextMessage", updatedMessageContent,
                                                "messageTimeEdit", FieldValue.serverTimestamp()
                                        )
                                        .addOnSuccessListener {

                                            viewHolder.theEditButton.visibility = View.INVISIBLE
                                            viewHolder.theEditButton.isEnabled = false

                                            lastMessageId = chatMessagesDataStructure.keyDatabase
                                            firebaseFirestore
                                                    .document(
                                                            "${messagesCollectionReferencePath}" +
                                                                    "${lastMessageId}/"
                                                    )
                                                    .update(
                                                            "theMessageSent", "true"
                                                    )

                                            val linkedHashMapData: LinkedHashMap<Any, Any> = LinkedHashMap<Any, Any>()
                                            linkedHashMapData["topic"] = notificationTopic
                                            linkedHashMapData["myUID"] = firebaseUser.uid
                                            linkedHashMapData["ChatAction"] = getString(R.string.GroupChatAction)
                                            linkedHashMapData["ChatName"] = cityName
                                            linkedHashMapData["notificationLargeIcon"] = firebaseUser.photoUrl.toString()
                                            linkedHashMapData["messageContent"] = updatedMessageContent
                                            linkedHashMapData["defaultColor"] = defaultColorNotification.toString()
                                            linkedHashMapData["titleColor"] = titleColorNotification.toString()
                                            linkedHashMapData["contentColor"] = contentColorNotification.toString()
                                            linkedHashMapData["CountryName"] = countryName
                                            linkedHashMapData["CityName"] = cityName
                                            linkedHashMapData["push"] = true

                                            firebaseCloudFunctions
                                                    .getHttpsCallable("groupOnEditNotification")
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
                                                                FunctionsClassDebug.PrintDebug("Error Code == " + code + " | Details == " + details)
                                                            }
                                                        }
                                                    }
                                        }
                                        .addOnFailureListener {
                                            lastMessageId = chatMessagesDataStructure.keyDatabase
                                            firebaseFirestore
                                                    .document(
                                                            "${messagesCollectionReferencePath}" +
                                                                    "${lastMessageId}/"
                                                    )
                                                    .update(
                                                            "theMessageSent", "false"
                                                    )

                                            if (firebaseUser.uid == chatMessagesDataStructure.theUserUniqueId) {
                                                viewHolder.theEditButton.visibility = View.VISIBLE
                                                viewHolder.theEditButton.isEnabled = true
                                            } else {
                                                viewHolder.theEditButton.visibility = View.INVISIBLE
                                                viewHolder.theEditButton.isEnabled = false
                                            }
                                        }
                            }
                        }

                        viewHolder.seenByNumber.setOnClickListener {
                            functionsClass.Toast("Seen By " + (it as TextView).text + " People",
                                    Gravity.BOTTOM, if (time == "day") {
                                getColor(R.color.darker)
                            } else {
                                getColor(R.color.lighter)
                            },
                                    if (time == "day") {
                                        getColor(R.color.lighter)
                                    } else {
                                        getColor(R.color.darker)
                                    })
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
                                messageRecyclerView.height
                        )
                    }, 500)
                }
            }
        })

        messageRecyclerView.layoutManager = linearLayoutManager
        messageRecyclerView.adapter = firebaseRecyclerAdapter

        val scrollBounds = Rect()
        var firstVisibleItem = 0

        nestedScrollView.getHitRect(scrollBounds)
        (nestedScrollView as NestedScrollView).setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { scrollView, scrollX, scrollY, oldScrollX, oldScrollY ->
            Handler().postDelayed({
                if (messageRecyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {

                    for (i in 0 until messageRecyclerView.childCount) {
                        val childView = messageRecyclerView.getChildAt(i)
                        if (childView != null) {
                            if (childView.getLocalVisibleRect(scrollBounds)) {
                                //Here is the position of first visible view
                                if (messageRecyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                                    val position = i

                                    if (position != firstVisibleItem) {
                                        /*Go to Loop from Position to Old Position*/
                                        /*Add Each User to Map of <UID, ServerTimestamp>*/
                                        //OR
                                        /*Add Each User to Collection of Each Message and ServerTimestamp to Field*/
                                        firstVisibleItem = position

                                        for (seenItem in firstVisibleItem..lastVisibleItem) {
                                            FunctionsClassDebug.PrintDebug("*** SeenItem == ${seenItem} ***")

                                            try {
                                                if (firebaseUser.uid != firebaseRecyclerAdapter.snapshots.get(seenItem).theUserUniqueId) {
                                                    FunctionsClassDebug.PrintDebug("*** Add To Seen Users ***")

                                                    val linkedHashMapSeenBy: LinkedHashMap<Any, Any> = LinkedHashMap<Any, Any>()
                                                    linkedHashMapSeenBy["SeenByUID"] = firebaseUser.uid
                                                    linkedHashMapSeenBy["SeenByDate"] = FieldValue.serverTimestamp()
                                                    val seenByDocument = "${messagesCollectionReferencePath}" +
                                                            "${firebaseRecyclerAdapter.snapshots.get(seenItem).keyDatabase}/" +
                                                            "SeenBy/" +
                                                            "${firebaseUser.uid}/"
                                                    firebaseFirestore
                                                            .document(seenByDocument)
                                                            .set(linkedHashMapSeenBy)
                                                            .addOnSuccessListener {

                                                                val linkedHashMapSeenByPath: LinkedHashMap<Any, Any> = LinkedHashMap<Any, Any>()
                                                                linkedHashMapSeenByPath["SeenByPath"] = "${messagesCollectionReferencePath}" + "${firebaseRecyclerAdapter.snapshots.get(seenItem).keyDatabase}/" + "SeenBy/"
                                                                linkedHashMapSeenByPath["MessagePath"] = "${messagesCollectionReferencePath}" + "${firebaseRecyclerAdapter.snapshots.get(seenItem).keyDatabase}/"

                                                                firebaseCloudFunctions
                                                                        .getHttpsCallable("updateGroupChatSeenBy")
                                                                        .call(linkedHashMapSeenByPath)
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
                                                                                    FunctionsClassDebug.PrintDebug("Error Code == " + code + " | Details == " + details)

                                                                                }
                                                                            } else {

                                                                            }
                                                                        }

                                                            }

                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }

                                        }

                                        lastVisibleItem = position
                                    }

                                }

                                break
                            }
                        }
                    }

                }

            }, 2000)
        })

        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int, end: Int) {

            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, before: Int, count: Int) {
                sendButton.isEnabled = charSequence.toString().trim { it <= ' ' }.isNotEmpty()
            }

            override fun afterTextChanged(editable: Editable) {

            }
        })

        sendButton.setOnClickListener {
            val plainTextMessage = messageEditText.text.toString()
            val messageContent = functionsClassConverter.encryptEncodedData(plainTextMessage, cityName).asList().toString()

            val messageHashMapData: LinkedHashMap<String, Any> = LinkedHashMap<String, Any>()
            messageHashMapData["NotificationTopic"] = "${countryName}-${cityName}"
            messageHashMapData["theTextMessage"] = messageContent
            messageHashMapData["theUserEmail"] = if (firebaseUser.isAnonymous) {
                "Anonymous"
            } else {
                firebaseUser.email!!
            }
            messageHashMapData["theUserName"] = firebaseUser.displayName.toString()
            messageHashMapData["theUserImageURL"] = firebaseUser.photoUrl.toString()
            messageHashMapData["theUserUniqueId"] = firebaseUser.uid
            messageHashMapData["messageTime"] = FieldValue.serverTimestamp()
            messageHashMapData["messageTimeEdit"] = FieldValue.serverTimestamp()
            messageHashMapData["defaultColor"] = defaultColorNotification.toString()

            firebaseFirestore
                    .collection(messagesCollectionReferencePath)
                    .add(messageHashMapData)
                    .addOnSuccessListener {
                        sendButton.isEnabled = false

                        lastMessageId = it.id
                        firebaseFirestore
                                .document(
                                        "${messagesCollectionReferencePath}" +
                                                "${lastMessageId}/"
                                )
                                .update(
                                        "theMessageSent", "true"
                                )

                        val linkedHashMapData: LinkedHashMap<Any, Any> = LinkedHashMap<Any, Any>()
                        linkedHashMapData["topic"] = notificationTopic
                        linkedHashMapData["myUID"] = firebaseUser.uid
                        linkedHashMapData["ChatAction"] = getString(R.string.GroupChatAction)
                        linkedHashMapData["ChatName"] = cityName
                        linkedHashMapData["notificationLargeIcon"] = firebaseUser.photoUrl.toString()
                        linkedHashMapData["messageContent"] = messageContent
                        linkedHashMapData["defaultColor"] = defaultColorNotification.toString()
                        linkedHashMapData["titleColor"] = titleColorNotification.toString()
                        linkedHashMapData["contentColor"] = contentColorNotification.toString()
                        linkedHashMapData["CountryName"] = countryName
                        linkedHashMapData["CityName"] = cityName
                        linkedHashMapData["push"] = true

                        firebaseCloudFunctions
                                .getHttpsCallable("groupMessageNotification")
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
                                            FunctionsClassDebug.PrintDebug("Error Code == " + code + " | Details == " + details)

                                        }
                                    } else {

                                    }
                                }

                        messageEditText.setText("")
                        firebaseAnalytics.logEvent(MESSAGE_SENT_EVENT, null)

                        nestedScrollView.smoothScrollTo(
                                0,
                                messageRecyclerView.height
                        )
                    }
                    .addOnFailureListener {
                        FunctionsClassDebug.PrintDebug(it)

                        textInputMessageEditText.isErrorEnabled = true
                        textInputMessageEditText.error = it.toString()

                        sendButton.isEnabled = true
                    }
        }

        nestedScrollView.smoothScrollTo(
                0,
                messageRecyclerView.height
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

        goTo.setBackgroundColor(if (time == "day") {
            getColor(R.color.lighter)
        } else {
            getColor(R.color.darker)
        })
        goTo.setTextColor(if (time == "day") {
            getColor(R.color.dark)
        } else {
            getColor(R.color.light)
        })
        goTo.strokeColor = if (time == "day") {
            ColorStateList.valueOf(getColor(R.color.dark_transparent))
        } else {
            ColorStateList.valueOf(getColor(R.color.light_transparent))
        }

        goTo.setOnClickListener {
            val popupPlaceType = ListPopupWindow(applicationContext)
            val navDrawerItem = ArrayList<NavDrawerItem>()
            val popupItemsText = functionsClass.readFromAssets(applicationContext, "place_type")
            val popupItemsIcon = getDrawable(R.drawable.ic_pin_current_day)

            FunctionsClassDebug.PrintDebug("*** ${popupItemsText.indices} | ${popupItemsText} ***")

            for (i in popupItemsText.indices) {
                navDrawerItem.add(NavDrawerItem(
                        popupItemsText[i],
                        popupItemsIcon!!,
                        i)
                )
            }
            val placesListAdapter = PlacesListAdapter(applicationContext, navDrawerItem, time, location)

            val Height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 297f, resources.displayMetrics).toInt()
            val Width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 175f, resources.displayMetrics).toInt()

            popupPlaceType.setAdapter(placesListAdapter)
            popupPlaceType.anchorView = goTo
            popupPlaceType.width = Width
            popupPlaceType.height = Height
            popupPlaceType.isModal = true
            popupPlaceType.setOnDismissListener {
                navDrawerItem.clear()
            }

            if (time == "day") {
                val day = getDrawable(R.drawable.popup_background)
                day!!.setTint(getColor(R.color.light))
                popupPlaceType.setBackgroundDrawable(day)
            } else if (time == "night") {
                val night = getDrawable(R.drawable.popup_background)
                night!!.setTint(getColor(R.color.dark))
                popupPlaceType.setBackgroundDrawable(getDrawable(R.drawable.popup_background))
            }
            popupPlaceType.show()
            if (functionsClass.returnAPI() < 23) {
                if (popupPlaceType.isShowing) {
                    Handler().postDelayed({
                        //popupPlaceType.dismiss();
                    }, 2000)
                }
            }
        }

        val goToClickHandler = Handler()
        var goToClickRunnable: Runnable? = null
        goTo.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    goToClickRunnable = Runnable {
                        goTo.text = getString(R.string.locationfind)
                    }
                    goToClickHandler.postDelayed(goToClickRunnable, 333)
                }
                MotionEvent.ACTION_UP -> {
                    goTo.text = ""
                    goToClickHandler.removeCallbacks(goToClickRunnable)
                }
            }
            false
        }

        chatLogo.setOnClickListener {
            if (nestedScrollViewPeople.isShown) {
                val slideAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_go_right)
                nestedScrollViewPeople.startAnimation(slideAnimation)
                slideAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {

                    }

                    override fun onAnimationRepeat(animation: Animation?) {

                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        nestedScrollViewPeople.visibility = View.INVISIBLE
                    }
                })
            } else {
                val slideAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_come_right)
                nestedScrollViewPeople.startAnimation(slideAnimation)
                slideAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {

                    }

                    override fun onAnimationRepeat(animation: Animation?) {

                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        nestedScrollViewPeople.visibility = View.VISIBLE
                    }
                })
            }
        }

        chatSummary.setOnClickListener {
            if (nestedScrollViewPeople.isShown) {
                val slideAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_go_right)
                nestedScrollViewPeople.startAnimation(slideAnimation)
                slideAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {

                    }

                    override fun onAnimationRepeat(animation: Animation?) {

                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        nestedScrollViewPeople.visibility = View.INVISIBLE
                    }
                })
            } else {
                val slideAnimation = AnimationUtils.loadAnimation(applicationContext, R.anim.slide_come_right)
                nestedScrollViewPeople.startAnimation(slideAnimation)
                slideAnimation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation?) {

                    }

                    override fun onAnimationRepeat(animation: Animation?) {

                    }

                    override fun onAnimationEnd(animation: Animation?) {
                        nestedScrollViewPeople.visibility = View.VISIBLE
                    }
                })
            }
        }

        val peopleCollectionPath = "PinPicsOnMap/" +
                "Messenger/" +
                "${countryName}/" +
                "${cityName}/" +
                "People"

        val peopleCollectionReference: Query = firebaseFirestore
                .collection(peopleCollectionPath)
                .orderBy("theUserLastSignIn", Query.Direction.ASCENDING)

        val firebaseRecyclerOptions = FirestoreRecyclerOptions.Builder<PeopleDataStructures>()
                .setQuery(
                        peopleCollectionReference,
                        PeopleDataStructures::class.java
                )
                .build()

        firebaseRecyclerAdapterPeople =
                object : FirestoreRecyclerAdapter<PeopleDataStructures, PeopleViewHolder>(firebaseRecyclerOptions) {
                    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): PeopleViewHolder {

                        val inflater = LayoutInflater.from(viewGroup.context)
                        return PeopleViewHolder(applicationContext, inflater.inflate(R.layout.item_group_messeger_all_people, viewGroup, false))
                    }

                    override fun onBindViewHolder(
                            peopleViewHolder: PeopleViewHolder,
                            position: Int,
                            peopleDataStructures: PeopleDataStructures) {

                        peopleViewHolder.peopleEmail.text = peopleDataStructures.theUserEmail
                        peopleViewHolder.peopleUserName.text = peopleDataStructures.theUserName

                        Glide.with(this@MessengerGroup)
                                .load(peopleDataStructures.theUserImageURL)
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
                                        peopleViewHolder.peopleUserImage.contentDescription = getString(R.string.theUserImageHint, peopleDataStructures.theUserName)

                                        runOnUiThread {
                                            if (peopleDataStructures.userStates!!.toBoolean()) {
                                                val bitmapUserImage = functionsClassUI.getCircularBitmapWithWhiteBorder(
                                                        functionsClassConverter.drawableToBitmap(drawable),
                                                        7,
                                                        getColor(R.color.online_color)
                                                )
                                                peopleViewHolder.peopleUserImage.setImageBitmap(bitmapUserImage)
                                            } else {
                                                val bitmapUserImage = functionsClassUI.getCircularBitmapWithWhiteBorder(
                                                        functionsClassConverter.drawableToBitmap(drawable),
                                                        7,
                                                        getColor(R.color.offline_color)
                                                )
                                                peopleViewHolder.peopleUserImage.setImageBitmap(bitmapUserImage)
                                            }
                                        }

                                        return false
                                    }
                                })
                                .submit()
                    }

                    override fun onDataChanged() {

                    }

                    override fun onError(e: FirebaseFirestoreException) {
                        super.onError(e)
                        e.printStackTrace()
                    }
                }

        peopleRecyclerView.layoutManager = linearLayoutManagerPeople
        peopleRecyclerView.adapter = firebaseRecyclerAdapterPeople

        firebaseRecyclerAdapterPeople.startListening()
    }

    override fun onResume() {
        super.onResume()
        firebaseRecyclerAdapter.startListening()

        PublicVariable.FRONT_GROUP_NAME = cityName
        PublicVariable.GROUP_CHAT_STATE_BACKGROUND = false

        /*User States | Online*/
        try {
            val intent = Intent(applicationContext, UserStatesIntentService::class.java).apply {
                action = "ACTION_ONLINE"
                putExtra("UserChatType", true)
                putExtra("CountryName", countryName)
                putExtra("CityName", cityName)
                putExtra("ChatName", countryName + "-" + cityName)
                putExtra("UID", firebaseUser.uid)
            }
            startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        firebaseRecyclerAdapter.stopListening()

        PublicVariable.FRONT_GROUP_NAME = null
        PublicVariable.GROUP_CHAT_STATE_BACKGROUND = true

        /*User States | Offline*/
        try {
            val intent = Intent(applicationContext, UserStatesIntentService::class.java).apply {
                action = "ACTION_OFFLINE"
                putExtra("UserChatType", true)
                putExtra("CountryName", countryName)
                putExtra("CityName", cityName)
                putExtra("ChatName", countryName + "-" + cityName)
                putExtra("UID", firebaseUser.uid)
            }
            startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        firebaseRecyclerAdapterPeople.stopListening()
    }

    override fun onBackPressed() {
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        super.onBackPressed()
    }

    class MessageViewHolder(context: Context, view: View, self: Boolean) : RecyclerView.ViewHolder(view) {

        var fullMessageItem: RelativeLayout

        var theTextMessage: EditText
        var theImageMessage: ImageView
        var theMessageSent: TextView
        var seenByNumber: TextView

        var theUserName: TextView
        var theUserImage: ImageView

        var theEditButton: Button

        init {
            if (self) {
                fullMessageItem = itemView.fullMessageItemSelf

                theTextMessage = itemView.theTextMessageSelf
                theImageMessage = itemView.theImageMessageSelf
                theMessageSent = itemView.theMessageSentSelf
                seenByNumber = itemView.seenByNumberSelf

                theUserName = itemView.theUserNameSelf
                theUserImage = itemView.theUserImageSelf

                theEditButton = itemView.editButtonSelf
            } else {
                fullMessageItem = itemView.fullMessageItem

                theTextMessage = itemView.theTextMessage
                theImageMessage = itemView.theImageMessage
                theMessageSent = itemView.theMessageSent
                seenByNumber = itemView.seenByNumber

                theUserName = itemView.theUserName
                theUserImage = itemView.theUserImage

                theEditButton = itemView.editButton
            }

            val functionsClass: FunctionsClass = FunctionsClass(context)
            val time = functionsClass.getTime()
            theTextMessage.setTextColor(if (time == "day") {
                context.getColor(R.color.dark)
            } else {
                context.getColor(R.color.light)
            })
        }
    }

    class PeopleViewHolder(context: Context, view: View) : RecyclerView.ViewHolder(view) {

        var fullPeopleItem: RelativeLayout

        var peopleUserImage: ImageView
        var peopleUserName: TextView
        var peopleEmail: TextView

        init {
            fullPeopleItem = itemView.fullPeopleItem

            peopleUserName = itemView.peopleUserName
            peopleUserImage = itemView.peopleUserImage
            peopleEmail = itemView.peopleEmail

            val functionsClass: FunctionsClass = FunctionsClass(context)
            val time = functionsClass.getTime()
            peopleUserName.setTextColor(if (time == "day") {
                context.getColor(R.color.dark)
            } else {
                context.getColor(R.color.light)
            })
            peopleEmail.setTextColor(if (time == "day") {
                context.getColor(R.color.default_color_darker)
            } else {
                context.getColor(R.color.default_color_light)
            })
        }
    }
}