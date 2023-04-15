package com.example.lab_2

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
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import android.widget.GridView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.lab2.BadgeView
import com.example.lab2.InterestView
import com.example.lab2.SkillAdapter
import com.example.lab2.StatisticView
import com.example.lab_2.entities.User

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
    var image_uri: Uri? = null

    private lateinit var sharedPref: SharedPreferences


    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if(response.resultCode == RESULT_OK) {
            val data: Intent? = response.data
            val userModified = data?.getStringExtra("user")
            user = User.fromJson(userModified!!)
            updateContent()
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

        supportActionBar?.setDisplayShowTitleEnabled(false)

        //load the shared preferences and update the views
        sharedPref = this.getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val userPref = sharedPref.getString("user", null) ?: User().toJson()
        user = User.fromJson(userPref)
        updateContent()

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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.editmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, EditProfileActivity::class.java).apply {
            addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE)
            putExtra("user", user.toJson())
        }
        return when (item.itemId) {
            R.id.edit -> {
                launcher.launch(intent)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }


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

        val badges = user.badges.map {  BadgeView(this, badge = it) }
        badges.forEach { badgesLayout.addView(it) }

        val interests = user.interests.map {  InterestView(this, sport = it) }
        interests.forEach { interestsLayout.addView(it) }

        val statistics = user.statistics.map {  StatisticView(this, statistic = it.value) }
        statistics.forEach { statisticsLayout.addView(it) }
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

