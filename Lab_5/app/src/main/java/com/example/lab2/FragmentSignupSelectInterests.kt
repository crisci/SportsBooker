package com.example.lab2

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.lab2.viewmodels.MainVM
import com.example.lab2.viewmodels.SignupVM
import com.example.lab2.databinding.FragmentSignupSelectInterestsBinding
import com.example.lab2.entities.Sport
import com.example.lab2.entities.User
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class FragmentSignupSelectInterests : Fragment(R.layout.fragment_signup_select_interests) {

    companion object {
        fun newInstance() = FragmentSignup()
    }

    private lateinit var navController: NavController
    private lateinit var firebaseAuth: FirebaseAuth

    @Inject
    lateinit var mainVM: MainVM

    private lateinit var signupVM: SignupVM

    private lateinit var viewModel: FragmentSignupViewModel
    private lateinit var binding: FragmentSignupSelectInterestsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        firebaseAuth = FirebaseAuth.getInstance()

        signupVM = ViewModelProvider(requireActivity())[SignupVM::class.java]

        navController = findNavController()

        val view = inflater.inflate(R.layout.fragment_signup_select_interests, container, false)
        binding = FragmentSignupSelectInterestsBinding.bind(view)

        for (s in Sport.values()) {
            val formattedSport = s.toString().lowercase().replaceFirstChar { it.uppercase() }
            val chip = Chip(requireContext())
            chip.text = formattedSport
            chip.isCheckable = true
            //chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            chip.setTextAppearance(R.style.InterestsChipTextAppearance)
            chip.setChipBackgroundColorResource(R.color.white)
            chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.example_1_bg))
            chip.chipStrokeWidth = 2f
            chip.chipStrokeColor = ContextCompat.getColorStateList(requireContext(), R.color.example_1_bg)
            chip.setOnCheckedChangeListener { buttonView, isChecked ->
                if(isChecked) {
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    chip.setChipBackgroundColorResource(R.color.example_1_bg)
                } else {
                    chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.example_1_bg))
                    chip.setChipBackgroundColorResource(R.color.white)
                }
            }
            binding.chipGroupSelectInterests.addView(chip)
        }

        binding.finish.setOnClickListener {
            val selectedInterests = mutableListOf<String>()
            for (i in 0 until binding.chipGroupSelectInterests.childCount) {
                val chip = binding.chipGroupSelectInterests.getChildAt(i) as Chip
                if(chip.isChecked) {
                    selectedInterests.add(chip.text.toString())
                }
            }
            if(selectedInterests.size == 0) {
                Toast.makeText(requireContext(), "Please select at least 1 sport", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val userId = arguments?.getString("userId")
            val name = arguments?.getString("name")
            val surname = arguments?.getString("surname")
            val username = arguments?.getString("username")
            val email = arguments?.getString("email")
            val dateOfBirth = arguments?.getString("dateOfBirth")
            val location = arguments?.getString("location")
            try {
                signupVM.createPlayer(
                    userId!!,
                    name!!,
                    surname!!,
                    username!!,
                    email!!,
                    dateOfBirth!!,
                    location!!,
                    selectedInterests.map { Sport.valueOf(it.uppercase()) }.toMutableList()
                )
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error creating user", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            navController.navigate(R.id.action_select_interests_to_login)
        }
        return view
    }
}