package com.example.lab2.entities

data class Reservation(
    val fieldName: String,
    val location: String,
    val price: Double,
    val time: String
)

fun Reservation.formatPrice(): String {
    return String.format("%.02f", price)
}
