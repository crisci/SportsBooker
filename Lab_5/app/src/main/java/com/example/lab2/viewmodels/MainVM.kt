package com.example.lab2.viewmodels


import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.LauncherActivity
import com.example.lab2.entities.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Singleton
class MainVM @Inject constructor(): ViewModel() {

    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance()
    var error : MutableLiveData<String?> = MutableLiveData()
    private val _user = MutableLiveData<User>()
    val user: LiveData<User> get() = _user
    private val _eventLogout = MutableLiveData<Unit>()
    val eventLogout: LiveData<Unit> get() = _eventLogout
    private var userListener: ListenerRegistration? = null

    private val _allPlayers: MutableLiveData<List<User>> = MutableLiveData(emptyList())
    private val _filteredPlayers: MutableLiveData<List<User>> = MutableLiveData(emptyList())
    val allPlayers: LiveData<List<User>> get() = _filteredPlayers


    val userId: String get() = auth.currentUser!!.uid;

    fun listenToUserUpdates(userId: String) {
        Log.w("MainVM", "Start listening: /players/$userId")
        userListener = db.collection("players").document(userId)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("MainVM", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("MainVM", "Current data: ${snapshot.data}")
                    _user.value = User.fromFirebase(snapshot)
                } else {
                    Log.d("MainVM", "Current data: null")
                }
            }
    }


    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
    }

    fun logout(callback: () -> Unit) {
        auth.signOut()
        callback()
    }



    var listBookedReservations = MutableLiveData<MutableSet<Int>>(mutableSetOf())


    fun updateUser(editedUser: User) {

        db.collection("players")
            .document(userId)
            .set(User.toFirebase(editedUser))
            .addOnSuccessListener {
                _user.value = editedUser
                error.value = null
            }
            .addOnFailureListener {
                e ->
                    error.value = e.message
            }
    }

    fun updateUserImage(imageUri: Uri) {

        val fileName = user.value?.full_name?.filterNot { it.isWhitespace() } ?: UUID.randomUUID().toString()

        storeImage(imageUri, fileName){ imageUrl ->
            if(imageUrl != null) {
                db.collection("players")
                    .document(userId)
                    .update("image", imageUrl)
                    .addOnSuccessListener {
                        user.value?.image = imageUrl
                    }
                    .addOnFailureListener {
                            e ->
                                error.value = e.message!!
                    }
            }
        }
    }


    // Store image in Firebase Storage and return its url to be saved (or null if failed)
    fun storeImage(imageUri: Uri, fileName: String, callback: (String?) -> Unit) {

        storage.getReference("images/$fileName")
            .putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    callback(imageUrl)
                }
            }
            .addOnFailureListener { e ->
                callback(null)
            }
    }

    fun getAllPlayers() {
        viewModelScope.launch{
            db.collection("players").get()
                .addOnSuccessListener { it.documents.mapNotNull { player ->
                    User.fromFirebase(player)
                    }.toMutableList().also { list ->
                        _allPlayers.postValue(list)
                    _filteredPlayers.postValue(list)
                    }
                }
        }
    }

    fun filterPlayers(query : String?) {
        if(query != null){
            _filteredPlayers.value = _allPlayers.value?.filter { it.full_name.lowercase().contains(query.lowercase()) || it.nickname.lowercase().contains(query.lowercase()) }
        }else{
            _filteredPlayers.value = _allPlayers.value
        }
    }

}