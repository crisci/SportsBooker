package com.example.lab2.viewmodels_firebase

import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.entities.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.squareup.picasso.Picasso
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.lang.reflect.Field
import javax.inject.Inject

@HiltViewModel
class DetailsViewModel @Inject constructor() : ViewModel() {

    private val _reservation = MutableLiveData<MatchWithCourtAndEquipments>()
    val reservation: LiveData<MatchWithCourtAndEquipments> = _reservation

    private val _listOfPlayers = MutableLiveData<List<User>>()
    val listOfPlayers: LiveData<List<User>> = _listOfPlayers

    private val _matchWithCourt = MutableLiveData<MatchWithCourt>()
    val matchWithCourt: LiveData<MatchWithCourt> = _matchWithCourt

    private val _avg = MutableLiveData<Double>()
    val avg: LiveData<Double> = _avg

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _exceptionMessage = MutableLiveData<String>()
    val exceptionMessage: LiveData<String> get() = _exceptionMessage

    fun getReservationDetails(reservationId: String) {
        db.collection("reservations").whereEqualTo(FieldPath.documentId(), reservationId).get()
            .addOnSuccessListener { r ->
                db.collection("matches").whereEqualTo(
                    FieldPath.documentId(),
                    r.first().getDocumentReference("match")?.id
                ).get().addOnSuccessListener { m ->
                    db.collection("courts").whereEqualTo(
                        FieldPath.documentId(),
                        m.first().getDocumentReference("court")?.id
                    ).get().addOnSuccessListener { c ->
                        avg(c)
                        val details =
                            firebaseToMatchWithCourtAndEquipments(m.first(), c.first(), r.first())
                        _reservation.postValue(details)
                        getPlayers(m)
                    }.addOnFailureListener {
                        _exceptionMessage.value = "Unable to load the details"
                    }
                }.addOnFailureListener {
                    _exceptionMessage.value = "Unable to load the details"
                }
            }.addOnFailureListener {
                _exceptionMessage.value = "Unable to load the details"
        }
    }

    fun getMatchDetails(matchId: String) {
        db.collection("matches").whereEqualTo(
            FieldPath.documentId(),
            matchId
        ).get().addOnSuccessListener { m ->
            db.collection("courts").whereEqualTo(
                FieldPath.documentId(),
                m.first().getDocumentReference("court")?.id
            ).get().addOnSuccessListener { c ->
                avg(c)
                val details =
                    firebaseToMatchWithCourt(m.first(), c.first())
                _matchWithCourt.postValue(details)
                getPlayers(m)
            }.addOnFailureListener {
                _exceptionMessage.value = "Unable to load the details"
            }
        }.addOnFailureListener {
            _exceptionMessage.value = "Unable to load the details"
        }
    }

    private fun getPlayers(m: QuerySnapshot) {
        CoroutineScope(Dispatchers.IO).launch {
            val listOfReferences = m.first().get("listOfPlayers") as List<DocumentReference>
            val list = mutableListOf<User>()
            for (r in listOfReferences) {
                val user =
                    db.collection("players").whereEqualTo(FieldPath.documentId(), r.id).get()
                        .await()
                list.add(User.fromFirebase(user.first()))
            }
            _listOfPlayers.postValue(list.filter { it.userId != auth.currentUser!!.uid})
        }
    }

    private fun avg(c: QuerySnapshot) {
        CoroutineScope(Dispatchers.IO).launch {
            db.collection("court_reviews").whereEqualTo("court", c.first().reference).get().addOnSuccessListener { reviews ->
                var sum = 0.0
                for(rv in reviews) {
                    val map = rv.get("listOfRatings") as HashMap<String, Double>
                    sum += map["cleanliness"]!! + map["lighting"]!! + map["maintenance"]!!
                }
                sum = sum/3/reviews.documents.size
                _avg.postValue(sum)
            }
        }
    }

}