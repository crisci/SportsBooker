package com.example.lab2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlin.properties.Delegates

class ShowProfileActivity : AppCompatActivity() {

    private lateinit var nameSurname: String
    private lateinit var nickname : String
    private lateinit var email: String
    private var age by Delegates.notNull<Int>()
    private lateinit var description: String
    private lateinit var location: String
    private lateinit var mastery: String
    private var interests = mutableListOf<String>()
    private var statistics = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}