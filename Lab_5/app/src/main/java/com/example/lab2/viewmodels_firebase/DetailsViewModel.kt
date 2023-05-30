package com.example.lab2.viewmodels_firebase

import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.entities.User
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

    private val db = FirebaseFirestore.getInstance()

    fun getReservationDetails(reservationId: String, context: Context) {
        db.collection("reservations").whereEqualTo(FieldPath.documentId(), reservationId).get()
            .addOnSuccessListener { r ->
                db.collection("matches").whereEqualTo(
                    FieldPath.documentId(),
                    r.first().getDocumentReference("match")?.id
                ).get().addOnSuccessListener { m ->
                    getPlayers(m)
                    db.collection("courts").whereEqualTo(
                        FieldPath.documentId(),
                        m.first().getDocumentReference("court")?.id
                    ).get().addOnSuccessListener { c ->
                        val details =
                            firebaseToMatchWithCourtAndEquipments(m.first(), c.first(), r.first())
                        Log.e("id", r.first().id)
                        Log.e("price", r.first().getDouble("finalPrice").toString())
                        Log.e("equipments", details.equipments.toString())
                        _reservation.postValue(details)
                    }.addOnFailureListener {
                        MainScope().launch {
                            Toast.makeText(
                                context,
                                "Unable to load the details",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }.addOnFailureListener {
                    MainScope().launch {
                        Toast.makeText(
                            context,
                            "Unable to load the details",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }.addOnFailureListener {
            MainScope().launch {
                Toast.makeText(
                    context,
                    "Unable to load the details",
                    Toast.LENGTH_SHORT
                ).show()
            }
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
            _listOfPlayers.postValue(list.filter { !it.email.equals("mattiamazzari@gmail.com") })
        }
    }


}