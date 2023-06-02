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
    val notificationsMatchesToReview: LiveData<List<Notification>> get() = _notificationsMatchesToReview

    private val _notificationsInvitations = MutableLiveData<List<Invitation>>()
    val notificationsInvitations: LiveData<List<Invitation>> get() = _notificationsInvitations

    private var invitationsListener: ListenerRegistration? = null
    private var matchToReviewListener: ListenerRegistration? = null

    private fun startListeningForNotifications() {


        val listInvitations = mutableListOf<Invitation>()
        var listMatchesToReview = mutableListOf<MatchToReview>()


        invitationsListener = db.collection("invitations")
            .whereEqualTo("receiver", db.document("players/${auth.currentUser!!.uid}"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle the error
                    return@addSnapshotListener
                }

                val invitations = snapshot?.toObjects(Invitation::class.java) ?: emptyList()
                listInvitations.addAll(invitations)
                _notificationsInvitations.value = listInvitations
                Log.d("NotificationVM", "Invitations: ${listInvitations.toString()}")
            }

        matchToReviewListener = db.collection("matches")
            .whereArrayContains(
                "listOfPlayers",
                db.collection("players").document(auth.currentUser!!.uid)
            )
            .whereLessThan("timestamp", Timestamp.now())
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener { snapshotMatch, error ->

                if (error != null) {
                    // Handle the error
                    return@addSnapshotListener
                }

                val matchIds = snapshotMatch!!.documents.map { it.reference }
                var listLast5Matches = snapshotMatch.documents.map { firebaseToMatch(it) }

                    db.collection("player_rating_mvp")
                        .whereEqualTo("player", db.collection("players").document(auth.currentUser!!.uid))
                        .whereNotIn("match", matchIds)
                        .get()
                        .addOnSuccessListener {ratingSnapshot ->
                           for (r in ratingSnapshot.documents) {
                               listLast5Matches = listLast5Matches.filter { it.matchId != r.getDocumentReference("match")!!.id }
                               _notificationsMatchesToReview.value = listLast5Matches
                           }
                        }


            }


    }
    fun stopListeningForNotifications() {
        invitationsListener?.remove()
        matchToReviewListener?.remove()
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

    fun sendInvitation(sender: String, recipient: User, match: Match) {
        Log.i("sendInvitation", "$sender + ${recipient.full_name} + ${match.matchId}")
        throw Exception("sendInvitation() needs to be implemented")
    }
}
