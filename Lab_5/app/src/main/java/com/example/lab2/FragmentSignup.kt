package com.example.lab2

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.example.lab2.databinding.FragmentSignupBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FragmentSignup : Fragment(R.layout.fragment_signup) {

    companion object {
        fun newInstance() = FragmentSignup()
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var viewModel: FragmentSignupViewModel
    private lateinit var binding: FragmentSignupBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        firebaseAuth = FirebaseAuth.getInstance()

        val view = inflater.inflate(R.layout.fragment_signup, container, false)
        binding = FragmentSignupBinding.bind(view)

        binding.signup.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()

            binding.email.error = null
            binding.password.error = null
            binding.confirmPassword.error = null

            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                if(password.length < 6) {
                    binding.password.error = "Password must be at least 6 characters long"
                    return@setOnClickListener
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.email.error = "Invalid email"
                    return@setOnClickListener
                }
                if (password == confirmPassword) {
                        firebaseAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Toast.makeText(
                                        requireActivity(),
                                        "Account created successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    showValidationError(task.exception)
                                }
                            }
                } else {
                    binding.password.error = "Passwords do not match"
                    binding.confirmPassword.error = "Passwords do not match"
                }
            } else {
                Toast.makeText(requireActivity(), "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        return view
    }

    private fun showValidationError(exception: Exception?) {
        if (exception is FirebaseAuthException) {
            when(exception.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> binding.email.error = "Email already in use"
                "ERROR_WEAK_PASSWORD" -> binding.password.error = "Password must be at least 6 characters long"
                "ERROR_INVALID_EMAIL" -> binding.email.error = "Invalid email"
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this)[FragmentSignupViewModel::class.java]
        // TODO: Use the ViewModel
    }

}