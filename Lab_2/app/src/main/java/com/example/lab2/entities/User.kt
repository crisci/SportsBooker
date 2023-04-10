package com.example.lab_2.entities
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.lab2.DateAsLongSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
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
    var image: String? = null,
    @Serializable(with = DateAsLongSerializer::class)
    var birthday: LocalDate = LocalDate.of(1995, 1, 1),
    val interests : List<String> = mutableListOf()
) {

    fun getAge(): Int {
        val today = LocalDate.now()
        val period = Period.between(birthday, today)
        return period.years
    }

    fun toJson(): String =
        Json.encodeToString(this)

    companion object {

        private val json = Json { ignoreUnknownKeys = true }

        fun fromJson(jsonString: String): User =
            json.decodeFromString<User>(jsonString)


    }
}