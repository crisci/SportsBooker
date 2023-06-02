package com.example.lab2.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.query
import com.example.lab2.entities.User
import com.example.lab2.viewmodels_firebase.Invitation
import com.example.lab2.viewmodels_firebase.Match
import com.example.lab2.viewmodels_firebase.MatchToReview
import com.example.lab2.viewmodels_firebase.MatchWithCourt
import com.example.lab2.viewmodels_firebase.Notification
import com.example.lab2.viewmodels_firebase.TimestampUtil
import com.example.lab2.viewmodels_firebase.firebaseToCourt
import com.example.lab2.viewmodels_firebase.firebaseToMatch
import com.example.lab2.viewmodels_firebase.invitationToFirebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

class NotificationVM @Inject constructor() : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _notificationsMatchesToReview = MutableLiveData<List<MatchToReview>>()
    val notificationsMatchesToReview: LiveData<List<MatchToReview>> get() = _notificationsMatchesToReview

    private val _notificationsInvitations = MutableLiveData<List<Invitation>>()
    val notificationsInvitations: LiveData<List<Invitation>> get() = _notificationsInvitations

    private var invitationsListener: ListenerRegistration? = null

    private val _numberOfUnseenNotifications = MutableLiveData<Int>(0)
    val numberOfUnseenNotifications: LiveData<Int> = _numberOfUnseenNotifications

    fun startListeningForNotifications() {


        val listInvitations = mutableListOf<Invitation>()
        var listMatchesToReview = mutableListOf<MatchToReview>()


        invitationsListener = db.collection("invitations")
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
                            if (seen == false) {
                                _numberOfUnseenNotifications.postValue(
                                    _numberOfUnseenNotifications.value?.plus(
                                        1
                                    )
                                )
                            } else {
                                if (numberOfUnseenNotifications.value!! > 0)
                                    _numberOfUnseenNotifications.postValue(
                                        _numberOfUnseenNotifications.value?.minus(1)
                                    )
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
                        listInvitations.addAll(invitations)
                        _notificationsInvitations.postValue(listInvitations)
                        Log.d("NotificationVM", "Invitations: ${listInvitations.toString()}")
                    }
                }
            }

        db.collection("matches")
            .whereArrayContains(
                "listOfPlayers",
                db.collection("players").document(auth.currentUser!!.uid)
            )
            .whereLessThan("timestamp", Timestamp.now())
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener { snapshotMatch ->

                val listMatchReferences = snapshotMatch!!.documents.map { it.reference }

                    db.collection("player_rating_mvp")
                        .whereEqualTo("player", db.collection("players").document(auth.currentUser!!.uid))
                        .whereNotIn("match", listMatchReferences)
                        .get()
                        .addOnSuccessListener { ratingSnapshot ->
                            val listMatchesAlreadyRatedByThePlayer = ratingSnapshot!!.documents.map { it.getDocumentReference("match")!! }
                            val list = listMatchReferences.subtract(listMatchesAlreadyRatedByThePlayer.toSet())
                            CoroutineScope(Dispatchers.IO).launch {
                                for (m in list) {
                                    val match = m.get().await()
                                    val court = match.getDocumentReference("court")?.get()?.await()
                                    listMatchesToReview.add(
                                        MatchToReview(
                                            firebaseToMatch(match),
                                            firebaseToCourt(court!!),
                                            match.id!!,
                                            Timestamp.now()
                                        )
                                    )

                                }
                                _notificationsMatchesToReview.postValue(listMatchesToReview)
                            }
                        }


            }


    }
    fun stopListeningForNotifications() {
        invitationsListener?.remove()
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
        viewModelScope.launch {
            val matchRef = db.collection("matches").document(notification.match.matchId!!)
            val court = matchRef.get().await().getDocumentReference("court")?.get()?.await()
            val playerRef = db.collection("players").document(auth.currentUser!!.uid)
            matchRef.update("listOfPlayers", FieldValue.arrayUnion(playerRef))
            matchRef.update("numOfPlayers", FieldValue.increment(1))

            db.collection("reservations").add(
                hashMapOf(
                    "match" to db.document("matches/${notification.match.matchId}"),
                    "player" to playerRef,
                    "listOfEquipments" to listOf<DocumentReference>(),
                    "finalPrice" to court!!.getDouble("basePrice"),
                )
            )

            deleteNotification(notification.id!!)
        }
    }

    fun sendInvitation(sender: String, recipient: User, match: Match, callback: (Exception?) -> Unit) {
        viewModelScope.launch {
            val newInvitation = invitationToFirebase(match = match, sentBy = sender, sentTo = recipient)
            try {
                db.collection("invitations")
                    .whereEqualTo("match", db.document("matches/${match.matchId}"))
                    .whereEqualTo("sentBy", db.document("players/${sender}"))
                    .whereEqualTo("sentTo", db.document("players/${recipient.userId}"))
                    .get()
                    .addOnSuccessListener {
                        if (it.documents.isEmpty()) {
                            // Add a new invitation
                            db.collection("invitations").add(newInvitation)
                                .addOnFailureListener { e ->
                                    callback(e)
                                }
                        } else {
                            // An invitation already exists
                            callback(Exception("You have already sent an invitation to this player."))
                        }
                    }
            } catch (err: Exception) {
                callback(err)
            }
        }
    }
}
