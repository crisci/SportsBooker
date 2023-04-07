package com.example.lab_2.entities
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.serialization.Serializable
import java.time.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Period

@Serializable
data class User(
    val full_name: String = "Achille Mago",
    val nickname: String = "@achillemago",
    val address: String = "Turin",
    val description: String = "Lorem",
    val email: String = "achille.mago@polito.it",
    //TODO: dob
    val picture: String = "",
    val interests : List<String> = mutableListOf()
) {


    fun toJson(): String =
        Json.encodeToString(this)

    companion object {

        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(jsonString: String): User =
            json.decodeFromString<User>(jsonString)


    }
}