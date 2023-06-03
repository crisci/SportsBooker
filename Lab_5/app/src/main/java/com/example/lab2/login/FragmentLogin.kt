package com.example.lab2.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.example.lab2.R
import com.example.lab2.databinding.FragmentLoginBinding
import com.example.lab2.reservation.my_reservations.MyReservationsActivity
import com.example.lab2.view_models.MainVM
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class FragmentLogin : Fragment(R.layout.fragment_login) {

    companion object {
        fun newInstance() = FragmentLogin()
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var binding: FragmentLoginBinding

    @Inject
    lateinit var mainVM: MainVM


    private lateinit var authStateListener: FirebaseAuth.AuthStateListener

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        binding = FragmentLoginBinding.bind(view)

        firebaseAuth = FirebaseAuth.getInstance()

        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser

            // Qui si riceve la callback se l'utente ha loggato
            if (user != null) {
                mainVM.listenToUserUpdates(user.uid)
                observeUserUpdates(this)
            }
        }

        firebaseAuth.addAuthStateListener(authStateListener)

        view.findViewById<View>(R.id.login).setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()



            if (email.isNotEmpty() && password.isNotEmpty()) {
                firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireActivity(),
                                "Logged in successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            showValidationError(task.exception)
                        }
                    }
            } else {
                Toast.makeText(requireActivity(), "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return view
    }

    override fun onStop() {
        super.onStop()
        if (authStateListener != null) {
            firebaseAuth.removeAuthStateListener(authStateListener)
        }
    }

    private fun showValidationError(exception: Exception?) {
        if (exception is FirebaseAuthException) {
            when (exception.errorCode) {
                "ERROR_INVALID_EMAIL" -> binding.email.error = "Invalid email"
                "ERROR_WRONG_PASSWORD" -> Toast.makeText(
                    requireActivity(),
                    "Wrong email and/or password",
                    Toast.LENGTH_SHORT
                ).show()

                else -> Toast.makeText(
                    requireActivity(),
                    "Error: ${exception.errorCode}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(
                requireActivity(),
                "Error: ${exception?.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun observeUserUpdates(lifecycleOwner: LifecycleOwner) {
        mainVM.user.observe(lifecycleOwner) { user ->
            // Once the user data is received, navigate to HomeActivity
            val intent = Intent(requireActivity(), MyReservationsActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

}