package com.example.lab2.entities

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

@kotlinx.serialization.Serializable
class MatchWithCourtAndEquipments(
    val reservationId: String? = null,
    var match: Match,
    val court: Court,
    var equipments: List<Equipment>,
    var finalPrice: Double
)

fun firebaseToMatchWithCourtAndEquipments(
    m: DocumentSnapshot,
    c: DocumentSnapshot,
    r: DocumentSnapshot
): MatchWithCourtAndEquipments {

    val mappedEquipments: List<Equipment> = (r.get("listOfEquipments") as List<String>).map {
        when (it.lowercase()) {
            "racket" -> Equipment("Racket", 2.0)
            "tennis balls" -> Equipment("Tennis balls", 1.5)
            "football ball" -> Equipment("Football ball", 5.0)
            "shin guards" -> Equipment("Shin guards", 3.5)
            "cleats" -> Equipment("Cleats", 2.5)
            "golf clubs" -> Equipment("Golf clubs", 5.5)
            "golf balls" -> Equipment("Golf balls", 1.5)
            "golf cart" -> Equipment("Golf cart", 30.0)
            else -> Equipment("Unknown", 0.0)
        }
    }

    return MatchWithCourtAndEquipments(
        r.id,
        firebaseToMatch(m),
        firebaseToCourt(c),
        mappedEquipments,
        r.getDouble("finalPrice")!!
    )
}

fun MatchWithCourtAndEquipments.formatPrice(): String {
    return String.format("%.02f", finalPrice)
}

fun MatchWithCourtAndEquipmentsToFirebase(
    playerId: String,
    r: MatchWithCourtAndEquipments
): Map<String, Any> {

    val map: MutableMap<String, Any> = mutableMapOf()

    val mappedEquipments = r.equipments.map { it.name }

    map["finalPrice"] = r.finalPrice
    map["listOfEquipments"] = mappedEquipments
    map["match"] = FirebaseFirestore.getInstance().document("matches/${r.match.matchId}")
    map["player"] = FirebaseFirestore.getInstance().document("players/${playerId}")

    return map

}

