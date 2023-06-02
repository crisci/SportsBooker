package com.example.lab2

import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2.calendar.displayText
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.entities.Equipment
import com.example.lab2.entities.User
import com.example.lab2.viewmodels.MainVM
import com.example.lab2.viewmodels.MyReservationsVM
import com.example.lab2.viewmodels_firebase.DetailsViewModel
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipments
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.textview.MaterialTextView
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class DetailsActivity : AppCompatActivity() {

    @Inject
    lateinit var mainVM : MainVM

    lateinit var reservationVM: MyReservationsVM
    lateinit var detailsViewModel: DetailsViewModel

    private lateinit var backButton: ImageView

    private lateinit var db: ReservationAppDatabase
    private lateinit var sport: TextView
    private lateinit var court: TextView
    private lateinit var location: TextView
    private lateinit var hour: TextView
    private lateinit var price: TextView
    private lateinit var yourEquipments: TextView
    private lateinit var description: TextView
    private lateinit var courtPhoto: ImageView
    private lateinit var rating: RatingBar
    private lateinit var date: TextView
    private lateinit var details: ConstraintLayout
    private lateinit var loading: ConstraintLayout
    private lateinit var players_details: TextView
    private lateinit var players_layout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        reservationVM = ViewModelProvider(this)[MyReservationsVM::class.java]
        detailsViewModel = ViewModelProvider(this)[DetailsViewModel::class.java]

        db = ReservationAppDatabase.getDatabase(this)
        sport = findViewById(R.id.sport_name_detail_reservation)
        court = findViewById(R.id.court_name_detail_reservation)
        location = findViewById(R.id.location_detail_reservation)
        hour = findViewById(R.id.hour_detail)
        price = findViewById(R.id.price_detail)
        yourEquipments = findViewById(R.id.your_equipments_detail)
        description = findViewById(R.id.court_description_detail)
        courtPhoto = findViewById(R.id.court_image)
        rating = findViewById(R.id.detail_rating)
        date = findViewById(R.id.date_detail)
        details = findViewById(R.id.details)
        loading = findViewById(R.id.loading_details)
        players_details = findViewById(R.id.other_players_detail)

        supportActionBar?.elevation = 0f
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.example_1_bg)))
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM;
        supportActionBar?.setCustomView(R.layout.toolbar)
        val titleTextView =
            supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title)
        titleTextView?.text = "Details"


        backButton = supportActionBar?.customView?.findViewById<ImageView>(R.id.custom_back_icon)!!
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val reservationId = intent.getStringExtra("reservationId")!!
        detailsViewModel.getReservationDetails(reservationId, applicationContext, currentPlayer = mainVM.user.value!!)


        detailsViewModel.listOfPlayers.observe(this) {
            if (detailsViewModel.listOfPlayers.value?.isNotEmpty()!!) {
                val adapterPlayers = AdapterPlayers(detailsViewModel.listOfPlayers.value ?: emptyList(), this, detailsViewModel.reservation.value!!)
                val listReservationsRecyclerView = this.findViewById<RecyclerView>(R.id.players_details)
                listReservationsRecyclerView.adapter = adapterPlayers
                listReservationsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
            } else {
                players_details.text = "Invite other players"
                val adapterPlayers = AdapterPlayers(emptyList(), this, detailsViewModel.reservation.value!!)
                val listReservationsRecyclerView = this.findViewById<RecyclerView>(R.id.players_details)
                listReservationsRecyclerView.adapter = adapterPlayers
                listReservationsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, true)
            }
        }

        detailsViewModel.reservation.observe(this) {
            updateView(detailsViewModel.reservation.value!!)
        }

        detailsViewModel.avg.observe(this) {
            rating.rating = detailsViewModel.avg.value!!.toFloat()
        }

    }

    private fun updateView(reservation: MatchWithCourtAndEquipments) {
        Picasso.get().load(reservation.court.image).into(courtPhoto, object : Callback {
            override fun onSuccess() {
                sport.text = reservation.court.sport
                court.text = reservation.court.name
                location.text = "Via Giovanni Magni, 32"
                hour.text = reservation.match.time.format(DateTimeFormatter.ofPattern("HH:mm"))
                date.text = setupDate(reservation.match.date)
                price.text = "€${String.format("%.02f", reservation.finalPrice)}"
                description.text = reservation.court.description
                setupEquipments(reservation.equipments)
                loading.visibility = View.GONE
                details.visibility = View.VISIBLE

            }

            override fun onError(e: Exception?) {
                Toast.makeText(applicationContext, "Unable to load the details", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }

        })
    }

    private fun setupEquipments(equipmets: List<Equipment>) {
        if (equipmets.isNotEmpty()) {
            val equipmentDetails = findViewById<LinearLayout>(R.id.equipments_container_detail)
            for (e in equipmets) {
                val equipmentView = MaterialTextView(this)
                equipmentView.text = "${e.name} €${String.format("%.02f", e.price)}"
                equipmentView.textSize = 16f
                equipmentView.setPadding(0, 16, 0, 0)
                equipmentDetails.addView(equipmentView)
            }
        } else {
            yourEquipments.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            yourEquipments.text = "You don't have any equipment booked"
        }
    }

    private fun setupDate(date: LocalDate): String {
        return "${date.dayOfWeek.displayText()} ${date.format(DateTimeFormatter.ofPattern("dd"))} ${date.month.displayText()}"
    }
}


class ViewHolderPlayers(v: View): RecyclerView.ViewHolder(v) {
    val playerImage: ImageView? = v.findViewById(R.id.player_image_details) ?: null
    val shimmer: ShimmerFrameLayout? = v.findViewById(R.id.shimmer_layout) ?: null
    val addBtn: Button? = v.findViewById(R.id.add_player_btn) ?: null
}

class AdapterPlayers(private var listOfPlayers: List<User>, private var mContext: Context, private var reservation: MatchWithCourtAndEquipments): RecyclerView.Adapter<ViewHolderPlayers>() {

    private val PLAYER = 0
    private val INVITE_BTN = 1
    var selectedPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPlayers {
        if (viewType == INVITE_BTN) {
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.invite_players_btn, parent, false)
            return ViewHolderPlayers(v)
        }else{
            val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.player_details, parent, false)
            return ViewHolderPlayers(v)
        }
    }

        override fun getItemCount(): Int = listOfPlayers.size + 1

        override fun getItemViewType(position: Int): Int {
            return if (position < itemCount - 1) {
                PLAYER;
            } else {
                INVITE_BTN;
            }
        }

        override fun onBindViewHolder(holder: ViewHolderPlayers, position: Int) {
            if(position < itemCount - 1){
                Picasso.get().load(listOfPlayers[position].image)
                    .into(holder.playerImage, object : Callback {
                        override fun onSuccess() {
                            holder.shimmer?.stopShimmer()
                            holder.shimmer?.hideShimmer()
                        }
                        override fun onError(e: Exception?) {}
                    })
                holder.playerImage?.setOnClickListener {
                    val playerIntent = Intent(holder.itemView.context, PlayerProfileActivity::class.java)
                    val playerString = Json.encodeToString(User.serializer(), listOfPlayers[holder.absoluteAdapterPosition])
                    playerIntent.putExtra("playerString", playerString)
                    holder.itemView.context.startActivity(playerIntent)
                }
            }else{
                holder.addBtn?.setOnClickListener {
                    val intentSearchPlayers =
                        Intent(mContext, SearchPlayersActivity::class.java)
                    val jsonRes = Json.encodeToString(MatchWithCourtAndEquipments.serializer(), reservation)
                    intentSearchPlayers.putExtra("jsonReservation", jsonRes)
                    mContext.startActivity(intentSearchPlayers)
                }
            }
        }
    }



