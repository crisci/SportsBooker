package com.example.lab2.entities

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class PartialRegistration(
    val userId: String,
    val name: String,
    val surname: String,
    val email: String,
    val photoUrl: String
) {

    fun toJson(): String =
        Json.encodeToString(this)

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        fun fromJson(jsonString: String): PartialRegistration =
            json.decodeFromString<PartialRegistration>(jsonString)
    }

}
