package com.example.lab2.viewmodels

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.database.court_review.CourtReview
import com.example.lab2.entities.User
import com.example.lab2.viewmodels_firebase.Match
import com.example.lab2.viewmodels_firebase.playerMVPVoteToFirebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class RatingModalVM @Inject constructor(): ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _selectedMVP = MutableLiveData<String>()
    val selectedMVP: LiveData<String> = _selectedMVP
    fun setSelectedMVP(mvpUserId: String) {
        _selectedMVP.value = mvpUserId
    }

    fun submitReview(review: CourtReview) {
        db.collection("court_reviews")
            .whereEqualTo("reviewer",db.document("players/${auth.currentUser!!.uid}"))
            .whereEqualTo("court",db.document("courts/${review.courtId}"))
            .get()
            .addOnSuccessListener {
                if (it.isEmpty) {
                    db.collection("court_reviews")
                        .add(review)
                        .addOnSuccessListener {
                            Log.d("RatingModalVM", "Review added")
                        }
                        .addOnFailureListener {
                            Log.d("RatingModalVM", "Review failed")
                        }
                } else {
                    it.documents[0].reference.update("review", review.review)
                        .addOnSuccessListener {
                            Log.d("RatingModalVM", "Review updated")
                        }
                        .addOnFailureListener {
                            Log.d("RatingModalVM", "Review update failed")
                        }
                }
            }
    }

    fun submitMVP(match: Match) {
        val playerVote = playerMVPVoteToFirebase(mvpUserId = _selectedMVP.value!!, match = match)
        db.collection("player_rating_mvp")
            .add(playerVote).addOnSuccessListener {
                db.collection("player_rating_mvp")
                    .whereEqualTo("match", db.document("matches/${match.matchId}"))
                    .get()
                    .addOnSuccessListener {
                        val numberOfVotes = it.documents.size.toLong()
                        if (numberOfVotes == match.numOfPlayers) {
                            Log.d("RatingModalVM", "All votes received")

                            // Count the votes for each player
                            val voteCountMap = mutableMapOf<String, Long>()
                            for (voteDoc in it.documents) {
                                val playerId = voteDoc.getString("mvpUserId")
                                if (playerId != null) {
                                    voteCountMap[playerId] = voteCountMap.getOrDefault(playerId, 0) + 1
                                }
                            }

                            // Find the player with the most votes
                            var maxVotes = 0L
                            var mvpPlayerId: String? = null
                            for ((playerId, voteCount) in voteCountMap) {
                                if (voteCount > maxVotes) {
                                    maxVotes = voteCount
                                    mvpPlayerId = playerId
                                }
                            }

                            if (mvpPlayerId != null) {
                                // Update the "mvp" field in the "matches" collection
                                val matchRef = db.collection("matches").document(match.matchId)
                                matchRef.update("mvp", db.document("players/$mvpPlayerId"))
                                    .addOnSuccessListener {
                                        Log.d("RatingModalVM", "MVP updated successfully")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.e("RatingModalVM", "Failed to update MVP: $e")
                                    }
                            }


                        }
                    }
            }
    }
}