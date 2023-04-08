package com.example.lab_2

import android.app.Instrumentation.ActivityResult
import android.content.ClipDescription
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract.CommonDataKinds.Nickname
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import com.example.lab_2.entities.User
import org.w3c.dom.Text

class ShowProfileActivity : AppCompatActivity() {


    private var user: User = User()

    private lateinit var full_name: TextView
    private lateinit var nickname: TextView
    private lateinit var location: TextView
    private lateinit var description: TextView
    private lateinit var profileImage: ImageView
    private lateinit var sharedPref: SharedPreferences


    private val launcher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: androidx.activity.result.ActivityResult) {
        if(response.resultCode == RESULT_OK) {
            val data: Intent? = response.data
            val userModified = data?.getStringExtra("user")
            user = User.fromJson(userModified!!)
            println(user.toString())
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
        nickname.text = user.nickname
        description.text = user.description
        location.text = user.address
    }

}