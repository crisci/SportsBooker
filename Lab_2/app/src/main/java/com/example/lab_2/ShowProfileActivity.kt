package com.example.lab_2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem

class ShowProfileActivity : AppCompatActivity() {

    /*private val launcher = registerActivityLifecycleCallbacks(
        ActivityResultContracts.StartActivityForResult()) { processResponse(it) }

    private fun processResponse(response: ActivityResult) {
        if(response.resultCode == RESULT_OK)
            val data: Intent? = response.data
    }*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
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
        }

        return when (item.itemId) {
            R.id.edit -> {
                startActivity(intent)
                true
            }
            else -> super.onContextItemSelected(item)
        }
    }
}