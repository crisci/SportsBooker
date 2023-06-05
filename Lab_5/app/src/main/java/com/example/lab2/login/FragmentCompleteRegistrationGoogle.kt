package com.example.lab2.login

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import com.example.lab2.R
import com.example.lab2.databinding.FragmentCompleteRegistrationGoogleBinding
import com.example.lab2.databinding.FragmentSignupBinding
import com.example.lab2.view_models.SignupVM
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject


class FragmentCompleteRegistrationGoogle : Fragment(R.layout.fragment_complete_registration_google) {

    private lateinit var binding: FragmentCompleteRegistrationGoogleBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var navController: NavController

    @Inject
    lateinit var signupVM: SignupVM

    companion object {
        fun newInstance() = FragmentCompleteRegistrationGoogle()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_complete_registration_google, container, false)
        binding = FragmentCompleteRegistrationGoogleBinding.bind(view)

        firebaseAuth = FirebaseAuth.getInstance()

        signupVM = ViewModelProvider(requireActivity())[SignupVM::class.java]

        val uid = arguments?.getString("uid")
        val name = arguments?.getString("name")
        val surname = arguments?.getString("surname")
        val email = arguments?.getString("email")
        val credential = arguments?.getParcelable<AuthCredential>("credential")
        val photoUrl = arguments?.getString("photoUrl")

        navController = findNavController()

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
            val dateOfBirth = binding.dateOfBirthEditText.text.toString()
            val location = binding.locationEditText.text.toString()
            val username = binding.usernameEditText.text.toString()

            binding.dateOfBirth.error = null
            binding.location.error = null
            binding.username.error = null

            if (dateOfBirth.isNotEmpty() && location.isNotEmpty() && username.isNotEmpty()
            ) {
                signupVM.createPlayer(
                    userId = uid!!,
                    name = name!!,
                    surname = surname!!,
                    username = username!!,
                    email = email!!,
                    location = location!!,
                    dateOfBirth = dateOfBirth!!,
                    selectedInterests = mutableListOf(),
                    photoUrl = photoUrl!!
                )
                val bundle = Bundle()
                bundle.putString("uid", uid)
                navController.navigate(
                    R.id.action_complete_registration_google_to_select_interests,
                    bundle
                )
            } else {
                Toast.makeText(requireActivity(), "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        return view
    }
}