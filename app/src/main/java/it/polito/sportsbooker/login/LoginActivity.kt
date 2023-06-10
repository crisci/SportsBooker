package it.polito.sportsbooker.login

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import it.polito.sportsbooker.R
import it.polito.sportsbooker.databinding.ActivityLoginBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.elevation = 0f
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.example_1_bg)))
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.toolbar_login)

        navController = (
                supportFragmentManager
                    .findFragmentById(R.id.fragmentContainerViewAuthentication) as NavHostFragment
                ).navController

        val selectedTab = binding.selectedView
        val loginTab = binding.loginTextView
        val signupTab = binding.signupTextView
        val leftGuideline = binding.guideline4
        val rightGuideline = binding.guideline5
        signupTab.setOnClickListener {
            selectedTab.animate()
                .x(rightGuideline.x)
                .setDuration(500)
                .start()
            if (navController.currentDestination?.id == R.id.login
                || navController.currentDestination?.id == R.id.complete_registration_google
                || navController.currentDestination?.id == R.id.select_interests) {
                navController.navigate(R.id.action_to_signup)
            }
        }
        loginTab.setOnClickListener {
            selectedTab.animate()
                .x(leftGuideline.x)
                .setDuration(500)
                .start()
            if (navController.currentDestination?.id == R.id.signup
                || navController.currentDestination?.id == R.id.select_interests
                || navController.currentDestination?.id == R.id.complete_registration_google) {
                navController.navigate(R.id.action_to_login)
            }
        }

    }

}

