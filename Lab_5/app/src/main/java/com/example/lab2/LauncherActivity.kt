package com.example.lab2

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.lab2.ui.login.LoginActivity
import com.example.lab2.viewmodels.MainVM
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LauncherActivity : AppCompatActivity() {

    @Inject
    lateinit var vm: MainVM

    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)

        auth = FirebaseAuth.getInstance()

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                // User is signed in, listen to user updates
                vm.listenToUserUpdates(user.uid)
                observeUserUpdates()
            } else {
                // User is signed out, navigate to LoginActivity
                navigateToLoginActivity()
            }
        }
    }

    private fun observeUserUpdates() {
        vm.user.observe(this) { user ->
            // Once the user data is received, navigate to HomeActivity
            navigateToHomeActivity()
        }
    }

    private fun navigateToLoginActivity() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun navigateToHomeActivity() {
        startActivity(Intent(this, MyReservationsActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener)
        }
    }
}