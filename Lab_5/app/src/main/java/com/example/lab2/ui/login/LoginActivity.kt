package com.example.lab2.ui.login

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.lab2.MyReservationsActivity
import com.example.lab2.databinding.ActivityLoginBinding

import com.example.lab2.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var navController : NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.elevation = 0f

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setCustomView(R.layout.toolbar_login)

        navController = (
                supportFragmentManager
                    .findFragmentById(R.id.fragmentContainerViewAuthentication) as NavHostFragment
                ).navController

        val selectedTab = binding.selectedTab
        val loginTab = binding.loginTab
        val signupTab = binding.signupTab
        signupTab.setOnClickListener {
            selectedTab.animate().x(signupTab.x).duration = 100
            if (navController.currentDestination?.id == R.id.login) {
                navController.navigate(R.id.action_login_to_signup)
            }
        }
        loginTab.setOnClickListener { 
            selectedTab.animate().x(loginTab.x).duration = 100
            if (navController.currentDestination?.id == R.id.signup) {
                navController.navigate(R.id.action_signup_to_login)
            }
        }


    }
}