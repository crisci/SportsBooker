package com.example.lab2

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.lab2.viewmodels.MainVM
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyReservationsActivity : AppCompatActivity() {

    private lateinit var navController : NavController
    private lateinit var myProfileButton: ImageView
    private lateinit var notificationsButton: FrameLayout

    @Inject
    lateinit var mainVM: MainVM

    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ){}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_reservations)
        setSupportActionBar()
        mainVM.setUser() //TODO: At the moment, there is a default value for parameter "userId"

        notificationsButton = supportActionBar?.customView?.findViewById<FrameLayout>(R.id.notifications)!!
        notificationsButton.setOnClickListener {
            val intentNotifications = Intent(this, NotificationsActivity::class.java)
            launcher.launch(intentNotifications)
        }

        navController = (
            supportFragmentManager
                .findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        ).navController

        myProfileButton = supportActionBar?.customView?.findViewById<ImageView>(R.id.custom_my_profile)!!
        myProfileButton.setOnClickListener {
            val intentShowProfile = Intent(this, ShowProfileActivity::class.java)
            launcher.launch(intentShowProfile)
        }

    }

    private fun setSupportActionBar(){
        supportActionBar?.elevation = 0f
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.example_1_bg)))
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.toolbar_with_profile)
        val titleTextView = supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title)
        titleTextView?.setText(R.string.my_reservations_title)
    }
}