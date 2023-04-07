package com.example.lab_2

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.example.lab_2.entities.User
import java.util.Date

class EditProfileActivity : AppCompatActivity() {

    private lateinit var user: User

    private lateinit var description_m: EditText
    private lateinit var full_name_m: EditText
    private lateinit var nickname_m: EditText
    private lateinit var address_m: EditText
    private lateinit var email_m: EditText


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        full_name_m = findViewById(R.id.editNameSurname)
        description_m = findViewById(R.id.editDescription)
        nickname_m = findViewById(R.id.editNickname)
        address_m = findViewById(R.id.editLocation)
        email_m = findViewById(R.id.editEmail)

        // get the user information sended by the showProfile Activity
        // than update the content of the views
        val extras = intent.extras
        if(extras != null) {
            user = User.fromJson(extras.getString("user")!!)
        }
        updateContent()

        val confirmButton: Button = findViewById(R.id.confirm_button)
        confirmButton.setOnClickListener {
            val result: Intent = Intent()
            val editedUser: User = User(
                full_name = full_name_m.text.toString(),
                nickname = nickname_m.text.toString(),
                address = address_m.text.toString(),
                description = description_m.text.toString(),
                email = email_m.text.toString())
            println(editedUser.toString())
            result.putExtra("user", editedUser.toJson())
            setResult(Activity.RESULT_OK, result)
            finish()
        }
    }


    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        //TODO: do the same for the other fields to be modified
        //in alternative you can create a fake user that every time you save this user as the ShowProfileActivity
        //when confirm is pressed than the user is commited and sended back
        //you can use editedUser
        full_name_m.setText(savedInstanceState.getString("full_name"))
        nickname_m.setText(savedInstanceState.getString("nickname"))
        description_m.setText(savedInstanceState.getString("description"))
        address_m.setText(savedInstanceState.getString("address"))
        email_m.setText(savedInstanceState.getString("email"))
        user = User(full_name_m.text.toString(), nickname_m.text.toString(), address_m.text.toString(), description_m.text.toString())

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        //TODO: do the same for the other fields
        outState.putString("full_name", full_name_m.text.toString())
        outState.putString("nickname", nickname_m.text.toString())
        outState.putString("description", description_m.text.toString())
        outState.putString("address", address_m.text.toString())
        outState.putString("email", email_m.text.toString())
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.backmenu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.backmenu -> {
                finish()
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }

    private fun updateContent() {
        full_name_m.setText(user.full_name)
        nickname_m.setText(user.nickname)
        description_m.setText(user.description)
        address_m.setText(user.address)
        email_m.setText(user.email)
    }

}