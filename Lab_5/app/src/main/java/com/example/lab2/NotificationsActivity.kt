package com.example.lab2

import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2.calendar.displayText
import com.example.lab2.viewmodels.NotificationVM
import com.example.lab2.viewmodels_firebase.Invitation
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipments
import com.facebook.shimmer.ShimmerFrameLayout
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsActivity: AppCompatActivity(), AdapterInvitations.OnClickListener {


    @Inject
    lateinit var notificationVM: NotificationVM

    private lateinit var myProfileButton: ImageView
    private lateinit var backButton: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val recyclerViewInvitations = findViewById<RecyclerView>(R.id.recyclerViewInvitations)
        recyclerViewInvitations.layoutManager = LinearLayoutManager(this)
        val adapterCard = AdapterInvitations(emptyList(), this)
        recyclerViewInvitations.adapter = adapterCard

        notificationVM.invitations.observe(this) {
            Log.d("NotificationsActivity", "invitations: $it")
            adapterCard.setInvitations(it)
            it.forEach { n ->  notificationVM.playerHasSeenNotification(n) }
        }

        setSupportActionBar()
        myProfileButton = supportActionBar?.customView?.findViewById(R.id.custom_my_profile)!!
        myProfileButton.setOnClickListener {
            val intentShowProfile = Intent(this, ShowProfileActivity::class.java)
            startActivity(intentShowProfile)
        }

        backButton = supportActionBar?.customView?.findViewById<ImageView>(R.id.custom_back_icon)!!
        backButton.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onClickAccept(invitation: Invitation) {
        notificationVM.joinTheMatch(invitation)
    }

    override fun onClickDecline(invitationId: String) {
        notificationVM.deleteNotification(invitationId)
    }

    private fun setSupportActionBar(){
        supportActionBar?.elevation = 0f
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.example_1_bg)))
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.toolbar)
        val titleTextView = supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title)
        titleTextView?.setText(R.string.notifications_title)
    }

}

class InvitationViewHolder(v: View): RecyclerView.ViewHolder(v) {
    val senderName: TextView = v.findViewById(R.id.sender_name)
    val profileImage: ImageView = v.findViewById(R.id.profile_image)
    val shimmer: ShimmerFrameLayout = v.findViewById(R.id.shimmer_layout)
    val sportName: TextView = v.findViewById(R.id.sport_name)
    val dateDetail: TextView = v.findViewById(R.id.date_detail)
    val timeDetail: TextView = v.findViewById(R.id.hour_detail)
    val acceptButton: Button = v.findViewById(R.id.accept_invitation_button)
    val declineButton: Button = v.findViewById(R.id.decline_invitation_button)
}

class AdapterInvitations(private var list: List<Invitation>, private val listener: OnClickListener) : RecyclerView.Adapter<InvitationViewHolder>() {

    interface OnClickListener {
        fun onClickAccept(invitation: Invitation)
        fun onClickDecline(invitationId: String)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.invitation_card, parent, false)
        return InvitationViewHolder(view)
    }

    override fun onBindViewHolder(holder: InvitationViewHolder, position: Int) {
        holder.senderName.text = list[position].sender.full_name
        Picasso.get().load(list[position].sender.image).into(holder.profileImage, object :
            Callback {
            override fun onSuccess() {
                holder.shimmer.stopShimmer()
                holder.shimmer.hideShimmer()
            }
            override fun onError(e: Exception?) {
            }
        })
        holder.sportName.text = list[position].court.sport
        holder.dateDetail.text = setupDate(list[position].match.date)
        holder.timeDetail.text = list[position].match.time.format(DateTimeFormatter.ofPattern("HH:mm"))
        holder.acceptButton.setOnClickListener() {
            listener.onClickAccept(list[holder.absoluteAdapterPosition])
        }
        holder.declineButton.setOnClickListener() {
            listener.onClickDecline(list[holder.absoluteAdapterPosition].id!!)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setInvitations(newInvitations: List<Invitation>) {

        val diffs = DiffUtil.calculateDiff(
            InvitationDiffCallback(list, newInvitations)
        )
        list = newInvitations
        diffs.dispatchUpdatesTo(this)
    }

    private fun setupDate(date: LocalDate): String {
        return "${date.dayOfWeek.displayText()} ${date.format(DateTimeFormatter.ofPattern("dd"))} ${date.month.displayText()}"
    }

}