package com.example.lab2

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.lab2.viewmodels.MainVM
import com.example.lab2.databinding.FragmentSignupBinding
import com.example.lab2.entities.Sport
import com.example.lab2.viewmodels.SignupVM
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject

@AndroidEntryPoint
class FragmentSignup : Fragment(R.layout.fragment_signup) {

    companion object {
        fun newInstance() = FragmentSignup()
    }

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var viewModel: FragmentSignupViewModel
    private lateinit var signupVM: SignupVM
    private lateinit var binding: FragmentSignupBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        firebaseAuth = FirebaseAuth.getInstance()

        navController = findNavController()

        signupVM = ViewModelProvider(requireActivity())[SignupVM::class.java]

        val view = inflater.inflate(R.layout.fragment_signup, container, false)
        binding = FragmentSignupBinding.bind(view)

        binding.dateOfBirthEditText.setOnClickListener {

            val constraintsBuilder =
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.now())
                    .build()

            Locale.setDefault(Locale.ENGLISH)
            val datePicker =
                MaterialDatePicker.Builder.datePicker()
                    .setTitleText("Select date")
                    .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                    .setCalendarConstraints(constraintsBuilder)
                    .build()

            datePicker.addOnPositiveButtonClickListener {
                val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                utc.timeInMillis = it
                val format = SimpleDateFormat("yyyy-MM-dd")
                val formatted: String = format.format(utc.time)
                binding.dateOfBirthEditText.setText(formatted)
            }
            datePicker.show(childFragmentManager, "datePicker")
        }

        binding.signup.setOnClickListener {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()
            val confirmPassword = binding.confirmPasswordEditText.text.toString()
            val name = binding.nameEditText.text.toString()
            val surname = binding.surnameEditText.text.toString()
            val dateOfBirth = binding.dateOfBirthEditText.text.toString()

            //TODO it would be good if we used a component that searches for real locations/cities, to avoid typos
            val location = binding.locationEditText.text.toString()

            val username = binding.usernameEditText.text.toString()

            binding.name.error = null
            binding.surname.error = null
            binding.dateOfBirth.error = null
            binding.location.error = null
            binding.username.error = null
            binding.email.error = null

            //TODO if passwords do not match,
            // the error exclamation mark must not overlap/replace the eye icon to show/hide
            // the password, this can be fixed either enlarging the view or moving the eye icon on
            // the left or creating a custom layout
            binding.password.error = null
            binding.confirmPassword.error = null


            if (email.isNotEmpty()
                && password.isNotEmpty() && confirmPassword.isNotEmpty()
                && name.isNotEmpty() && surname.isNotEmpty()
                && dateOfBirth.isNotEmpty() && location.isNotEmpty() && username.isNotEmpty()) {

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
                                    signupVM.createPlayer(
                                        task.result.user?.uid!!,
                                        email,
                                        name,
                                        surname,
                                        dateOfBirth,
                                        location,
                                        username,
                                        mutableListOf()
                                    )
                                    val bundle = Bundle()
                                    bundle.putString("uid", task.result.user?.uid)
                                    navController.navigate(R.id.action_signup_to_select_interests, bundle)
                                } else {
                                    showValidationError(task.exception)
                                }
                            }
                } else {
                    //TODO if passwords do not match,
                    // the error exclamation mark must not overlap/replace the eye icon to show/hide
                    // the password, this can be fixed either enlarging the view or moving the eye icon on
                    // the left or creating a custom layout
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