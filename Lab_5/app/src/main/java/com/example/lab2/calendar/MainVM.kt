package com.example.lab2.calendar


import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.lab2.entities.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainVM @Inject constructor(): ViewModel() {

    // TODO: At the moment User is set to DEFAULT, so that other activities can still work
    // TODO: It needs to be changed to MutableLiveData<User?> = MutableLiveData(null)

    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    val auth = FirebaseAuth.getInstance().signInAnonymously() //TODO: This is needed for the storage, will be replace with real auth
    var user : MutableLiveData<User> = MutableLiveData(User.default())
    var currentUserId : MutableLiveData<String> = MutableLiveData("")
    var error : MutableLiveData<String?> = MutableLiveData()


    var listBookedReservations = MutableLiveData<MutableSet<Int>>(mutableSetOf())

    fun setUser(userId: String = "mbvhLWL5YbPoYIqRskD1XkVVILv1") {
        db.collection("players").document(userId).addSnapshotListener{ value, error ->
            if(value!=null){
                user.value = User.fromFirebase(value)
                currentUserId.value = userId
                this.error.value = null
            }
            if(error != null){
                this.error.value = error.message
            }
        }
    }

    fun getUser(): LiveData<User> {
        return user
    }

    fun updateUser(editedUser: User) {

        db.collection("players")
            .document(currentUserId.value!!)
            .set(User.toFirebase(editedUser))
            .addOnSuccessListener {
                user.value = editedUser
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
                    .document(currentUserId.value!!)
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

}