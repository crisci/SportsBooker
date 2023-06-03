package com.example.lab2

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2.calendar.displayText
import com.example.lab2.entities.Sport
import com.example.lab2.entities.User
import com.example.lab2.viewmodels.MainVM
import com.example.lab2.viewmodels.NewMatchesVM
import com.example.lab2.viewmodels.NotificationVM
import com.example.lab2.viewmodels_firebase.Court
import com.example.lab2.viewmodels_firebase.Invitation
import com.example.lab2.viewmodels_firebase.Match
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipments
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.card.MaterialCardView
import com.google.firebase.Timestamp
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject


@AndroidEntryPoint
class RankingActivity : AppCompatActivity() {

    @Inject
    lateinit var mainVM: MainVM
    @Inject
    lateinit var notificationVM: NotificationVM

    private lateinit var backButton: ImageView
    private lateinit var filterView: RecyclerView

    lateinit var vm: NewMatchesVM


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ranking)
        setSupportActionBar()

        vm = ViewModelProvider(this)[NewMatchesVM::class.java]

        // TODO filter view as done in myReservations and Find New Match
        filterView = findViewById(R.id.ranking_filter)

        val adapterCardFilters = AdapterFilters(Sport.values().map { it.name.lowercase().replaceFirstChar(Char::titlecase) }.toList(), vm::setSportFilter)
        filterView.adapter = adapterCardFilters
        filterView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);

        mainVM.getAllPlayers()

        vm.getSportFilter().observe(this){
            // TODO filter playersToShowHere
        }

        val recyclerViewPlayers = findViewById<RecyclerView>(R.id.recyclerViewRanking)
        recyclerViewPlayers.layoutManager = LinearLayoutManager(this)
        val adapterCard = AdapterPlayersRankList(emptyList())
        recyclerViewPlayers.adapter = adapterCard

        mainVM.allPlayers.observe(this){
            // TODO sport filter change
            adapterCard.setPlayers(it /*.sortedBy { v -> v.score?.get("Football") }*/)
        }

    }

    private fun setSupportActionBar(){
        supportActionBar?.setCustomView(R.layout.toolbar)
        supportActionBar?.elevation = 0f
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.example_1_bg)))
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        val titleTextView = supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title)
        titleTextView?.text = "Ranking"

        backButton = supportActionBar?.customView?.findViewById<ImageView>(R.id.custom_back_icon)!!
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

}

class PlayerRankViewHolder(v: View): RecyclerView.ViewHolder(v) {
    val card: MaterialCardView = v.findViewById(R.id.ranking_card)
    val fullName: TextView = v.findViewById(R.id.sender_name)
    val nickname: TextView = v.findViewById(R.id.nick)
    val profileImage: ImageView = v.findViewById(R.id.profile_image)
    val shimmer: ShimmerFrameLayout = v.findViewById(R.id.shimmer_layout)
    val points: TextView = v.findViewById(R.id.points)
}

class AdapterPlayersRankList(private var list: List<User>) : RecyclerView.Adapter<PlayerRankViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerRankViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.player_card_rank, parent, false)
        return PlayerRankViewHolder(view)
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: PlayerRankViewHolder, position: Int) {

        if(holder.bindingAdapterPosition == 0){
            holder.card.setCardBackgroundColor(Color.parseColor("#FFD700"))
        }else if(holder.bindingAdapterPosition == 1){
            holder.card.setCardBackgroundColor(Color.parseColor("#BEC2CB"))
        }else if(holder.bindingAdapterPosition == 2){
            holder.card.setCardBackgroundColor(Color.parseColor("#CD7F32"))
        }

        holder.fullName.text = list[position].full_name
        holder.nickname.text = list[position].nickname
        holder.points.text = "${list[position].score?.get("Football") ?: 0}pt"
        if(list[position].image != ""){
            Picasso.get().load(list[position].image).into(holder.profileImage, object :
                Callback {
                override fun onSuccess() {
                    holder.shimmer.stopShimmer()
                    holder.shimmer.hideShimmer()
                }
                override fun onError(e: Exception?) {
                }
            })
        }else{
            Picasso.get().load(R.drawable.profile_picture).into(holder.profileImage, object :
                Callback {
                override fun onSuccess() {
                    holder.shimmer.stopShimmer()
                    holder.shimmer.hideShimmer()
                }
                override fun onError(e: Exception?) {
                }
            })
        }

        holder.itemView.setOnClickListener{
            val playerIntent = Intent(holder.itemView.context, PlayerProfileActivity::class.java)
            val playerString = Json.encodeToString(User.serializer(), list[holder.absoluteAdapterPosition])
            playerIntent.putExtra("playerString", playerString)
            holder.itemView.context.startActivity(playerIntent)
        }
    }


    override fun getItemCount(): Int {
        return list.size
    }

    fun setPlayers(newPlayers: List<User>) {

        val diffs = DiffUtil.calculateDiff(
            UserDiffCallback(list, newPlayers)
        )
        list = newPlayers
        diffs.dispatchUpdatesTo(this)
    }

}
