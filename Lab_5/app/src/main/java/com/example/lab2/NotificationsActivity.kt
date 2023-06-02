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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2.calendar.displayText
import com.example.lab2.viewmodels.NotificationVM
import com.example.lab2.viewmodels_firebase.Invitation
import com.example.lab2.viewmodels_firebase.MatchToReview
import com.example.lab2.viewmodels_firebase.MatchWithCourt
import com.example.lab2.viewmodels_firebase.Notification
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipments
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.firebase.Timestamp
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@AndroidEntryPoint
class NotificationsActivity: AppCompatActivity(), NotificationAdapter.OnClickListener {


    @Inject
    lateinit var notificationVM: NotificationVM
    private lateinit var backButton: ImageView
    private lateinit var leaveRatingLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val recyclerViewNotifications = findViewById<RecyclerView>(R.id.recyclerViewNotifications)
        recyclerViewNotifications.layoutManager = LinearLayoutManager(this)
        val adapterCard = NotificationAdapter(emptyList(), this)
        recyclerViewNotifications.adapter = adapterCard

        notificationVM.notificationsInvitations.observe(this) {
            Log.d("NotificationsActivity", "notificationsInvitations: $it")
            adapterCard.setNotification(it + notificationVM.notificationsMatchesToReview.value!!)
            it.forEach { n -> notificationVM.playerHasSeenNotification(n) }
        }

        notificationVM.notificationsMatchesToReview.observe(this) {
            Log.d("NotificationsActivity", "notificationsMatchesToReview: $it")
            adapterCard.setNotification(it + notificationVM.notificationsInvitations.value!!)
        }

        setSupportActionBar()

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

    override fun onClickRateNow(matchToReview: MatchToReview) {
        leaveRatingLayout.visibility = View.VISIBLE
        leaveRatingLayout.setOnClickListener {
            val modalBottomSheet = RatingModalBottomSheet()
            modalBottomSheet.show(supportFragmentManager, RatingModalBottomSheet.TAG)
        }
    }

    private fun setSupportActionBar() {
        supportActionBar?.elevation = 0f
        supportActionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.example_1_bg)))
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.toolbar)
        val titleTextView =
            supportActionBar?.customView?.findViewById<TextView>(R.id.custom_toolbar_title)
        titleTextView?.setText(R.string.notifications_title)
    }
}



class NotificationAdapter(private var list: List<Notification>, private val listener: OnClickListener) : RecyclerView.Adapter<NotificationAdapter.NotificationVH>() {

    abstract class NotificationVH(v: View): RecyclerView.ViewHolder(v) {
        abstract fun bind(notification: Notification)
    }

    companion object {
        private const val VIEW_TYPE_INVITATION = 0
        private const val VIEW_TYPE_MATCH_TO_REVIEW = 1
    }

    interface OnClickListener {
        fun onClickAccept(invitation: Invitation)
        fun onClickDecline(invitationId: String)

        fun onClickRateNow(matchToReview: MatchToReview)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationVH {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.invitation_card -> InvitationViewHolder(v)
            R.layout.match_to_review_card -> MatchToReviewViewHolder(v)
            else -> throw IllegalArgumentException("Invalid type of data $viewType")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (list[position]) {
            is Invitation -> R.layout.invitation_card
            is MatchToReview -> R.layout.match_to_review_card
            else -> throw IllegalArgumentException("Invalid type of data $position")
        }
    }

    override fun onBindViewHolder(holder: NotificationVH , position: Int) {
        holder.bind(list[position])
        /*val notification = list[holder.absoluteAdapterPosition]
        when (notification) {
            is Invitation -> {
                holder.senderName.text = invitation.sender.full_name
                Picasso.get().load(invitation.sender.image).into(holder.profileImage, object :
                    Callback {
                    override fun onSuccess() {
                        holder.shimmer.stopShimmer()
                        holder.shimmer.hideShimmer()
                    }
                    override fun onError(e: Exception?) {
                    }
                })
                holder.sportName.text = invitation.court.sport
                holder.dateDetail.text = setupDate(invitation.match.date)
                holder.timeDetail.text = invitation.match.time.format(DateTimeFormatter.ofPattern("HH:mm"))
                holder.acceptButton.setOnClickListener() {
                    listener.onClickAccept(invitation)
                }
                holder.declineButton.setOnClickListener() {
                    listener.onClickDecline(invitation.id!!)
                }
                holder.notificationTime.text = getTimeAgo(invitation.timestamp)
            }
            is MatchToReviewViewHolder -> {
                val matchToReview = notification as MatchToReview
                holder.rateNowButton.setOnClickListener() {
                    listener.onClickRateNow(matchToReview)
                }
            }
        }*/
    }

    inner class InvitationViewHolder(v: View): NotificationVH(v) {
        val senderName: TextView = v.findViewById(R.id.sender_name)
        val profileImage: ImageView = v.findViewById(R.id.profile_image)
        val shimmer: ShimmerFrameLayout = v.findViewById(R.id.shimmer_layout)
        val sportName: TextView = v.findViewById(R.id.sport_name)
        val dateDetail: TextView = v.findViewById(R.id.date_detail)
        val timeDetail: TextView = v.findViewById(R.id.hour_detail)
        val acceptButton: Button = v.findViewById(R.id.accept_invitation_button)
        val declineButton: Button = v.findViewById(R.id.decline_invitation_button)
        val notificationTime: TextView = v.findViewById(R.id.notification_time)

        override fun bind(notification: Notification) {
            val invitation = notification as Invitation
            senderName.text = invitation.sender.full_name
            Picasso.get().load(invitation.sender.image).into(profileImage, object :
                Callback {
                override fun onSuccess() {
                    shimmer.stopShimmer()
                    shimmer.hideShimmer()
                }
                override fun onError(e: Exception?) {
                }
            })
            sportName.text = invitation.court.sport
            dateDetail.text = setupDate(invitation.match.date)
            timeDetail.text = invitation.match.time.format(DateTimeFormatter.ofPattern("HH:mm"))
            acceptButton.setOnClickListener() {
                listener.onClickAccept(invitation)
            }
            declineButton.setOnClickListener() {
                listener.onClickDecline(invitation.id!!)
            }
            notificationTime.text = getTimeAgo(invitation.timestamp)
        }
    }

    inner class MatchToReviewViewHolder(v: View): NotificationVH(v) {
        val rateNowButton: Button = v.findViewById(R.id.rate_now_button)
        val sportName: TextView = v.findViewById(R.id.sport_name)
        val dateDetail: TextView = v.findViewById(R.id.date_detail)
        val hourDetail: TextView = v.findViewById(R.id.hour_detail)
        //TODO we will discuss what to show
        override fun bind(notification: Notification) {
            val matchToReview = notification as MatchToReview
            sportName.text = matchToReview.court.sport
            dateDetail.text = setupDate(matchToReview.match.date)
            hourDetail.text = matchToReview.match.time.format(DateTimeFormatter.ofPattern("HH:mm"))
            rateNowButton.setOnClickListener() {
                listener.onClickRateNow(matchToReview)
            }
        }
    }

    private fun getTimeAgo(timestamp: Timestamp): String {
        val now = Timestamp.now()
        val difference = now.seconds - timestamp.seconds

        val seconds = difference
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24
        val weeks = days / 7
        val months = weeks / 4
        val years = months / 12

        return when {
            years > 0 -> "$years ${if (years == 1L) "year" else "years"} ago"
            months > 0 -> "$months ${if (months == 1L) "month" else "months"} ago"
            weeks > 0 -> "$weeks ${if (weeks == 1L) "week" else "weeks"} ago"
            days > 0 -> "$days ${if (days == 1L) "day" else "days"} ago"
            hours > 0 -> "$hours ${if (hours == 1L) "hour" else "hours"} ago"
            minutes > 0 -> "$minutes ${if (minutes == 1L) "minute" else "minutes"} ago"
            else -> "$seconds ${if (seconds == 1L) "second" else "seconds"} ago"
        }
    }

    fun setNotification(newListNotifications: List<Notification>) {

        val diffs = DiffUtil.calculateDiff(
            NotificationDiffCallback(list, newListNotifications)
        )
        list = newListNotifications
        diffs.dispatchUpdatesTo(this)
    }
    override fun getItemCount(): Int {
        return list.size
    }

    private fun setupDate(date: LocalDate): String {
        return "${date.dayOfWeek.displayText()} ${date.format(DateTimeFormatter.ofPattern("dd"))} ${date.month.displayText()}"
    }

}