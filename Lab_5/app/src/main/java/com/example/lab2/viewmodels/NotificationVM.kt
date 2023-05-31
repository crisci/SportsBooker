package com.example.lab2.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab2.entities.User
import com.example.lab2.viewmodels_firebase.Invitation
import com.example.lab2.viewmodels_firebase.TimestampUtil
import com.example.lab2.viewmodels_firebase.firebaseToCourt
import com.example.lab2.viewmodels_firebase.firebaseToMatch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationVM @Inject constructor() : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _invitations = MutableLiveData<List<Invitation>>()
    val invitations: LiveData<List<Invitation>> = _invitations

    private val _numberOfUnseenNotifications = MutableLiveData<Int>(0)
    val numberOfUnseenNotifications: LiveData<Int> = _numberOfUnseenNotifications

    lateinit var notificationListener: ListenerRegistration

    init {
        notificationListener = db.collection("invitations")
            .whereEqualTo("sentTo", db.document("players/${auth.currentUser!!.uid}"))
            .addSnapshotListener { querySnapshot, e ->
                if (e != null) {
                    return@addSnapshotListener
                }
                if (querySnapshot != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val invitations = mutableListOf<Invitation>()
                        val notifications = querySnapshot.documents
                        for (notification in notifications) {
                            val seen = notification.getBoolean("seen")
                            if(seen==false) {
                                _numberOfUnseenNotifications.postValue(_numberOfUnseenNotifications.value?.plus(1))
                            }
                            else {
                                if(numberOfUnseenNotifications.value!! > 0)
                                    _numberOfUnseenNotifications.postValue(_numberOfUnseenNotifications.value?.minus(1))
                            }
                            val match =
                                notification.getDocumentReference("match")?.get()?.await()
                            val court = match!!.getDocumentReference("court")?.get()?.await()
                            val sender =
                                notification.getDocumentReference("sentBy")?.get()?.await()
                            val timestamp = notification.getTimestamp("timestamp")
                            invitations.add(
                                Invitation(
                                    id = notification.id,
                                    sender = User.fromFirebase(sender!!),
                                    match = firebaseToMatch(match),
                                    court = firebaseToCourt(court!!),
                                    timestamp = timestamp!!
                                )
                            )
                        }
                        _invitations.postValue(invitations)
                    }
                }
            }
    }

    fun playerHasSeenNotification(notification: Invitation) {
        val notificationRef = db.collection("invitations").document(notification.id!!)
        notificationRef.update("seen", true)
    }

    fun deleteNotification(notificationId: String) {
        val notificationRef = db.collection("invitations").document(notificationId)
        notificationRef.delete()
    }

    fun joinTheMatch(notification: Invitation) {
        //val notificationRef = db.collection("invitations").document(notification.id!!)
        //notificationRef.update("seen", true)
        viewModelScope.launch {
            val matchRef = db.collection("matches").document(notification.match.matchId!!)
            val court = matchRef.get().await().getDocumentReference("court")?.get()?.await()
            matchRef.update("listOfPlayers", notification.match.listOfPlayers
                .plus("players/${auth.currentUser!!.uid}")
            )
            matchRef.update("numOfPlayers", FieldValue.increment(1))
            db.collection("reservations").add(
                hashMapOf(
                    "match" to db.document("matches/${notification.match.matchId}"),
                    "player" to db.document("players/${auth.currentUser!!.uid}"),
                    "listOfEquipments" to listOf<DocumentReference>(),
                    "finalPrice" to court!!.getDouble("basePrice"),
                )
            )
            deleteNotification(notification.id!!)
        }
    }
}