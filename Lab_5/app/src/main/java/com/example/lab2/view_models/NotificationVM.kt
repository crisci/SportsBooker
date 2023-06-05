package com.example.lab2.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.lab2.entities.*
import com.example.lab2.utils.toTimestamp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject

class NotificationVM @Inject constructor() : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _notificationsMatchesToReview = MutableLiveData<MutableList<MatchToReview>>(
        mutableListOf()
    )
    val notificationsMatchesToReview: LiveData<MutableList<MatchToReview>> get() = _notificationsMatchesToReview

    private val _notificationsInvitations =
        MutableLiveData<MutableList<Invitation>>(mutableListOf())
    val notificationsInvitations: LiveData<MutableList<Invitation>> get() = _notificationsInvitations

    private var invitationsListener: ListenerRegistration? = null

    private val _numberOfUnseenNotifications = MutableLiveData<Int>(0)
    val numberOfUnseenNotifications: LiveData<Int> = _numberOfUnseenNotifications

    private val _notifications : MutableLiveData<MutableList<Notification>> = MutableLiveData(mutableListOf())
    val notifications : LiveData<MutableList<Notification>> get() = _notifications


    // UI States
    var error: MutableLiveData<String?> = MutableLiveData()
    var loadingState: MutableLiveData<Boolean> = MutableLiveData(false)
    var invitationSuccess: MutableLiveData<Boolean> = MutableLiveData(false)


    init {
        viewModelScope.launch {
            loadingState.value = true
            val results = withContext(Dispatchers.IO){
                val resultInvitation = try{
                    val documentsInvitations = db.collection("invitations")
                        .whereEqualTo("sentTo", db.document("players/${auth.currentUser!!.uid}"))
                        .get().await()
                    //val numOfUnseenInvites = calculateNumberOfUnseenNotifications(documentsInvitations)
                    val invitations = processInvitations(documentsInvitations)
                    Result(invitations, null)
                }catch (err: Exception){
                    Result(null, err)
                }

                val resultReviews = try{
                    val startOfPreviousWeek = LocalDate.now().minusWeeks(1).with(DayOfWeek.MONDAY).atStartOfDay()
                    val twoHoursAgo = LocalDateTime.now().minusHours(2)
                    val startOfPreviousWeekTimestamp = Timestamp(startOfPreviousWeek.toEpochSecond(ZoneOffset.UTC), 0)
                    val twoHoursAgoTimestamp = Timestamp(twoHoursAgo.toEpochSecond(ZoneOffset.UTC), 0)

                    val documentsReviews = db.collection("matches")
                        .whereArrayContains(
                            "listOfPlayers",
                            db.document("players/${auth.currentUser!!.uid}")
                        )
                        .whereLessThan("timestamp", twoHoursAgoTimestamp)
                        .whereGreaterThan("timestamp", startOfPreviousWeekTimestamp)
                        .get().await()

                    val reviews = processReviews(documentsReviews)
                    Result(reviews, null)
                }catch (err: Exception){
                    Result(null, err)
                }

                Pair(resultInvitation, resultReviews)

            }

            val resultInvitation = results.first
            val resultReviews = results.second

            if(resultInvitation.value != null && resultReviews.value != null){
                error.value = null
                sortNotifications(resultInvitation.value!!, resultReviews.value!!)
            }else{
                Log.e("invitation", resultReviews.throwable?.stackTraceToString()!! )
                error.value = "Unable to retrieve notifications. Try again later."
            }

            loadingState.value = false
        }
    }

    private suspend fun sortNotifications(invitations: MutableList<Invitation>, reviews: MutableList<MatchToReview>) {
        viewModelScope.launch {
            val list = (invitations + reviews) as MutableList<Notification>
            withContext(Dispatchers.Default){
                list.sortByDescending { it.timestamp }
            }
            _notifications.postValue(list)
        }
    }

    suspend fun processInvitations(documents : QuerySnapshot) : MutableList<Invitation>{
        val invitations = mutableListOf<Invitation>()

        for(notification in documents.documents) {
            val match = notification.getDocumentReference("match")?.get()?.await()
            val court = match!!.getDocumentReference("court")?.get()?.await()
            val sender = notification.getDocumentReference("sentBy")?.get()?.await()
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

        return invitations
    }

    suspend fun calculateNumberOfUnseenNotifications(documents: QuerySnapshot) : Long {
        var numOfUnseenNotifications : Long = 0

        for(notification in documents.documents){
            when(notification.getBoolean("seen")){
                false -> numOfUnseenNotifications += 1
                else -> {}
            }
        }

        return numOfUnseenNotifications
    }

    suspend fun processReviews(snapshotMatch: QuerySnapshot) : MutableList<MatchToReview>{

        var listMatchesToReview = mutableListOf<MatchToReview>()

        val listMatchReferences = snapshotMatch!!.documents.map { it.reference }
        val ratingSnapshot = db.collection("player_rating_mvp")
            .whereEqualTo("reviewer", db.document("players/${auth.currentUser!!.uid}"))
            .get().await()

        val listMatchesAlreadyRatedByThePlayer = ratingSnapshot!!.documents.map { it.getDocumentReference("match")!! }

        val notRatedMatches = listMatchReferences.filter { matchRef -> !listMatchesAlreadyRatedByThePlayer.any { it == matchRef } }

        /* TODO: Invalid Query. A non-empty array is required for 'in' filters. (fired when there are no notifications,
            possibly notRatedMatches is [] and whereIn requires it to be non-empty).
         */
        if(notRatedMatches.isNotEmpty()){
            val notRatedMatchesSnapshot = db.collection("matches")
                .whereIn(FieldPath.documentId(), notRatedMatches.map { it.id })
                .get().await()

            for (i in notRatedMatchesSnapshot.documents) {
                val match = firebaseToMatch(i)
                val court = firebaseToCourt(i.getDocumentReference("court")?.get()?.await()!!)
                val matchDateTime = LocalDateTime.of(match.date, match.time)
                val dateTimeNotification = matchDateTime.plusHours(1).plusMinutes(30)
                val matchToReview = MatchToReview(match, court, match.matchId, dateTimeNotification.toTimestamp())
                listMatchesToReview.add(matchToReview)
            }
        }

        return listMatchesToReview
    }

    fun playerHasSeenNotification(notification: Invitation) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val notificationRef = db.collection("invitations").document(notification.id!!)
                notificationRef.update("seen", true)
            }
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val notificationRef = db.collection("invitations").document(notificationId)
                notificationRef.delete()
            }
            _notifications.value = _notifications.value?.filter { it.id != notificationId }?.toMutableList()
        }
    }

    fun deleteReviewNotification(notificationId: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                // TODO: something must be done here to flag that this review notification doesn't have to be shown!
                //val notificationRef = db.collection("invitations").document(notificationId)
                //notificationRef.delete()
            }
            _notifications.value = _notifications.value?.filter { it.id != notificationId }?.toMutableList()
        }
    }

    fun joinTheMatch(notification: Invitation) {
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                val matchRef = db.collection("matches").document(notification.match.matchId)
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
    }

}
