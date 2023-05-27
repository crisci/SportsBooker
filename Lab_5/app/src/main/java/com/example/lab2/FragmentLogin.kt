package com.example.lab2

import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.lab2.viewmodels.MainVM
import com.example.lab2.databinding.FragmentLoginBinding
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
    private lateinit var viewModel: FragmentLoginVM
    private lateinit var binding: FragmentLoginBinding
    @Inject
    lateinit var mainVM: MainVM

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        binding = FragmentLoginBinding.bind(view)

        firebaseAuth = FirebaseAuth.getInstance()

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

                                mainVM.setUser(task.result.user?.uid!!)
                                val intent = Intent(requireActivity(), MyReservationsActivity::class.java)
                                startActivity(intent)

                            } else {
                                showValidationError(task.exception)
                            }
                        }
            }
            else {
                Toast.makeText(requireActivity(), "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        return view
    }

    private fun showValidationError(exception: Exception?) {
        if (exception is FirebaseAuthException) {
            when(exception.errorCode) {
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
        }
        else {
            Toast.makeText(
                requireActivity(),
                "Error: ${exception?.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[FragmentLoginVM::class.java]
        // TODO: Use the ViewModel
    }

}