package it.polito.sportsbooker.entities

@kotlinx.serialization.Serializable
data class Equipment(
    val name: String,
    val price: Double
)

