package com.example.lab_2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import java.util.Date

class EditProfileActivity : AppCompatActivity() {

    lateinit var imageView: ImageView;
    lateinit var galleryBtn: Button;
    lateinit  var cameraBtn:Button;

    lateinit var editFullName: EditText;
    lateinit var editNickname: EditText;
    lateinit var editDob: EditText;  //TODO: Date??
    lateinit var editEmail: EditText;
    lateinit var editDescription: EditText;
    lateinit var editLocation: EditText;

    var fullName = "Achille Mago"



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        imageView = findViewById(R.id.image)
        galleryBtn = findViewById(R.id.edit_picture)
        editFullName = findViewById(R.id.editNameSurname)
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


}