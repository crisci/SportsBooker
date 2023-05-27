package com.example.lab2

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBar
import androidx.lifecycle.ViewModelProvider
import com.example.lab2.viewmodels.MyReservationsVM
import com.example.lab2.viewmodels.MainVM
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowProfileActivity : AppCompatActivity() {

    private lateinit var fullName: TextView
    private lateinit var nickname: TextView
    private lateinit var location: TextView
    private lateinit var description: TextView
    private lateinit var age: TextView
    private lateinit var profileImage: ImageView
    private lateinit var interestsLayout: LinearLayout
    private lateinit var badgesLayout: LinearLayout
    private lateinit var statisticsLayout: LinearLayout
    private lateinit var backButton: ImageButton
    private lateinit var editProfile: ImageButton

    @Inject
    lateinit var vm: MainVM
    lateinit var reservationVm: MyReservationsVM


    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if(response.resultCode == RESULT_OK) {
            updateContent()
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar()
        findViews()

        // Set ViewModels & Current User Data
        reservationVm = ViewModelProvider(this)[MyReservationsVM::class.java]

        reservationVm.refreshMyStatistics(playerId = 1)

        badgesLayout.setOnClickListener { showCustomDialog() }

        vm.user.observe(this) {
            updateContent()
        }

        reservationVm.getMyStatistics().observe(this){
            updateContent()
        }

        backButton.setOnClickListener {
            finish()
        }

        editProfile.setOnClickListener {
            val intentEditProfile = Intent(this, EditProfileActivity::class.java).apply {
                addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE)
                putExtra("user", vm.user.value?.toJson())
            }
            launcher.launch(intentEditProfile)
        }

    }

    private fun findViews(){
        fullName = findViewById(R.id.nameSurname)
        nickname = findViewById(R.id.nickname)
        location = findViewById(R.id.location)
        description = findViewById(R.id.description)
        profileImage = findViewById(R.id.profile_image)
        age = findViewById(R.id.age)
        interestsLayout = findViewById(R.id.profile_interests)
        badgesLayout = findViewById(R.id.profile_badges)
        statisticsLayout = findViewById(R.id.profile_statistics)
    }

    @SuppressLint("SetTextI18n")
    private fun updateContent() {
        fullName.text = vm.user.value?.full_name
        nickname.text = "@${vm.user.value?.nickname}"
        description.text = vm.user.value?.description
        location.text = vm.user.value?.address
        age.text = "${vm.user.value?.getAge()}yo"

        if (vm.user.value?.image == "") {
            profileImage.setBackgroundResource(R.drawable.profile_picture)
        }
        else {
            val profileImageUrl = vm.user.value?.image
            Picasso.get().load(profileImageUrl).into(profileImage)
        }

        badgesLayout.removeAllViews()
        interestsLayout.removeAllViews()
        statisticsLayout.removeAllViews()

        // Show only 3 small badges, that's why "drop 2"
        val badges = vm.user.value?.badges?.toList()?.dropLast(2)?.toMap()?.map {  BadgeView(this, badge = it) }
        badges?.forEach { badgesLayout.addView(it) }

        val interests = vm.user.value?.interests?.sortedBy { it.name }?.map {  InterestView(this, sport = it) }
        interests?.forEach { interestsLayout.addView(it) }

        val statistics = reservationVm.getMyStatistics().value?.sortedBy { it.sport.name }?.map {  StatisticView(this, statistic = it) }

        if (statistics.isNullOrEmpty()) {
            findViewById<TextView>(R.id.no_stats).visibility = View.VISIBLE
        }else{
            findViewById<TextView>(R.id.no_stats).visibility = View.GONE
            statistics.forEach { statisticsLayout.addView(it) }
        }
    }

    private fun setSupportActionBar(){

        supportActionBar?.elevation = 0f

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.toolbar_show_profile)
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.example_1_bg)))
        val titleTextView = supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title_show_profile)
        titleTextView?.setText(R.string.profile_title)
        backButton = supportActionBar?.customView?.findViewById(R.id.edit_profile_back_button)!!
        editProfile = supportActionBar?.customView?.findViewById(R.id.profile_edit_button)!!

    }

    private fun showCustomDialog() {

        val skillsDialog = Dialog(this)
        skillsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // Remove default title
        skillsDialog.setCancelable(true) // Allow user to exit dialog by clicking outside
        skillsDialog.setContentView(R.layout.skills_dialog)
        skillsDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val exitButton = skillsDialog.findViewById<Button>(R.id.close_skills_dialog)
        val skillsContainer = skillsDialog.findViewById<GridView>(R.id.skills_container)
        skillsContainer.adapter = SkillAdapter(this, vm.user.value?.badges!!)


        exitButton.setOnClickListener {
            skillsDialog.dismiss()
        }

        skillsDialog.show()
    }

}

