package com.example.lab2

import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NotificationsActivity: AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        //val recyclerViewNotifications = findViewById<RecyclerView>(R.id.recyclerViewNotifications)
        //recyclerViewNotifications.adapter = AdapterNotifications()
    }

}

class ViewHolderNotifications {

}

class AdapterNotifications() {

}