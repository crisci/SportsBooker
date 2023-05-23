package com.example.lab2

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
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
import androidx.lifecycle.lifecycleScope
import com.example.lab2.calendar.MyReservationsVM
import com.example.lab2.calendar.UserViewModel
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.entities.BadgeType
import com.example.lab2.entities.User
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ShowProfileActivity : AppCompatActivity() {
    private var user: User = User()

    private lateinit var full_name: TextView
    private lateinit var nickname: TextView
    private lateinit var location: TextView
    private lateinit var description: TextView
    private lateinit var age: TextView
    private lateinit var profileImage: ImageView
    private lateinit var interestsLayout: LinearLayout
    private lateinit var badgesLayout: LinearLayout
    private lateinit var statisticsLayout: LinearLayout
    private lateinit var skills: LinearLayout
    private lateinit var backButton: ImageButton
    private lateinit var editProfile: ImageButton
    var image_uri: Uri? = null

    private lateinit var sharedPref: SharedPreferences

    private lateinit var db : ReservationAppDatabase

    @Inject
    lateinit var vm: UserViewModel
    lateinit var reservationVm: MyReservationsVM


    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if(response.resultCode == RESULT_OK) {
            val data: Intent? = response.data
            val userModified = data?.getStringExtra("user")
            user = User.fromJson(userModified!!)
            vm.setUser(user)
            //commit the data in the shared preferences
            with(sharedPref.edit()) {
                putString("user", user.toJson())
                apply()
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        reservationVm = ViewModelProvider(this)[MyReservationsVM::class.java]

        full_name = findViewById(R.id.nameSurname)
        nickname = findViewById(R.id.nickname)
        location = findViewById(R.id.location)
        description = findViewById(R.id.description)
        profileImage = findViewById(R.id.profile_image)
        age = findViewById(R.id.age)
        interestsLayout = findViewById(R.id.profile_interests)
        badgesLayout = findViewById(R.id.profile_badges)
        statisticsLayout = findViewById(R.id.profile_statistics)
        skills = findViewById(R.id.profile_badges)


        skills.setOnClickListener { showCustomDialog() }

        supportActionBar?.elevation = 0f

        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setCustomView(R.layout.toolbar_show_profile)
        val titleTextView = supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title_show_profile)
        titleTextView?.text = "Profile"
        backButton = supportActionBar?.customView?.findViewById<ImageButton>(R.id.edit_profile_back_button)!!
        editProfile = supportActionBar?.customView?.findViewById<ImageButton>(R.id.profile_edit_button)!!

        reservationVm.refreshMyStatistics(playerId = 1)

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
                putExtra("user", user.toJson())
            }
            launcher.launch(intentEditProfile)
        }


        //load the shared preferences and update the views
        sharedPref = this.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val userPref = sharedPref.getString("user", null) ?: User().toJson()
        user = User.fromJson(userPref)
        vm.setUser(user)

        db = ReservationAppDatabase.getDatabase(this)

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("user", user.toJson())
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        user = User.fromJson(savedInstanceState.getString("user")!!)
        updateContent()
    }

    /*    override fun onCreateOptionsMenu(menu: Menu): Boolean {
            super.onCreateOptionsMenu(menu)
            val inflater: MenuInflater = menuInflater
            inflater.inflate(R.menu.editmenu, menu)
            return true
        }

        override fun onOptionsItemSelected(item: MenuItem): Boolean {
            val intentEditProfile = Intent(this, EditProfileActivity::class.java).apply {
                addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE)
                putExtra("user", user.toJson())
            }
            val intentMyReservations = Intent(this, MyReservationsActivity::class.java)

            return when (item.itemId) {
                R.id.edit -> {
                    launcher.launch(intentEditProfile)
                    true
                }
                else -> super.onContextItemSelected(item)
            }
        }*/


    private fun updateContent() {
        full_name.text = user.full_name
        nickname.text = "@${user.nickname}"
        description.text = user.description
        location.text = user.address
        age.text = "${user.getAge()}yo"

        if (user.image == null) {
            profileImage.setBackgroundResource(R.drawable.profile_picture)
        }
        else {
            val inputStream = applicationContext.openFileInput(user.image)
            val rotated = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            //val rotated = rotateBitmap(inputImageBitmap!!)
            rotated?.let {
                // Set the new image to the ImageView
                profileImage.setImageBitmap(it)
            }
        }

        badgesLayout.removeAllViews()
        interestsLayout.removeAllViews()
        statisticsLayout.removeAllViews()

        // Show only 3 small badges, that's why "drop 2"
        val badges = user.badges.toList().dropLast(2).toMap<BadgeType, Int>().map {  BadgeView(this, badge = it) }
        badges.forEach { badgesLayout.addView(it) }

        val interests = user.interests.sortedBy { it.name }.map {  InterestView(this, sport = it) }
        interests.forEach { interestsLayout.addView(it) }

        val statistics = reservationVm.getMyStatistics().value?.sortedBy { it.sport.name }?.map {  StatisticView(this, statistic = it) }

        if (statistics.isNullOrEmpty()) {
            findViewById<TextView>(R.id.no_stats).visibility = View.VISIBLE
        }else{
            findViewById<TextView>(R.id.no_stats).visibility = View.GONE
            statistics.forEach { statisticsLayout.addView(it) }
        }
    }

    private fun showCustomDialog() {

        val skillsDialog: Dialog = Dialog(this)
        skillsDialog.requestWindowFeature(Window.FEATURE_NO_TITLE) // Remove default title
        skillsDialog.setCancelable(true) // Allow user to exit dialog by clicking outside
        skillsDialog.setContentView(R.layout.skills_dialog)
        skillsDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val exitButton = skillsDialog.findViewById<Button>(R.id.close_skills_dialog)
        val skillsContainer = skillsDialog.findViewById<GridView>(R.id.skills_container)
        skillsContainer.adapter = SkillAdapter(this, user.badges)


        exitButton.setOnClickListener {
            skillsDialog.dismiss()
        }

        skillsDialog.show()
    }

}

