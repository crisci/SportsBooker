package com.example.lab2

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2.entities.Reservation
import com.example.lab2.entities.formatPrice

class YourReservationActivity : AppCompatActivity() {

    private val list = listOf<Reservation>(
        Reservation("Campo 1", "Via Pippo Pluto 10, Torino", 7.00, "18:00"),
        Reservation("Campo 1", "Via Pippo Pluto 10, Torino", 7.00, "18:00"),
        Reservation("Campo 1", "Via Pippo Pluto 10, Torino", 7.00, "18:00")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_reservation_view)


        val recyclerViewCard = findViewById<RecyclerView>(R.id.your_reservation_recycler_view)
        recyclerViewCard.adapter = AdapterCard(list)
        recyclerViewCard.layoutManager = LinearLayoutManager(this)
    }

}

class ViewHolderCard(v: View): RecyclerView.ViewHolder(v) {
    val name: TextView = v.findViewById(R.id.court_name_reservation)
    val location: TextView = v.findViewById(R.id.location_reservation)
    val price: TextView = v.findViewById(R.id.price_reservation)
    val time: TextView = v.findViewById(R.id.time_reservation)
}

class AdapterCard(private val list: List<Reservation>): RecyclerView.Adapter<ViewHolderCard>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderCard {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.reservation_card_layout, parent, false)
        return ViewHolderCard(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolderCard, position: Int) {
        holder.name.text = list[position].fieldName
        holder.location.text = list[position].location
        holder.price.text = "€ ${list[position].formatPrice()}"
        holder.time.text = list[position].time
    }

}
