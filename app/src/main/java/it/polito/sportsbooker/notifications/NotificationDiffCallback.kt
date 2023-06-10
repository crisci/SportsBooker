package it.polito.sportsbooker.notifications

import androidx.recyclerview.widget.DiffUtil
import it.polito.sportsbooker.entities.Invitation
import it.polito.sportsbooker.entities.MatchToReview
import it.polito.sportsbooker.entities.Notification

class NotificationDiffCallback(
    private val notifications: List<Notification>,
    private val newNotifications: List<Notification>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = notifications.size

    override fun getNewListSize(): Int = newNotifications.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItem = notifications[oldItemPosition]
        val newItem = newNotifications[newItemPosition]
        return when {
            oldItem is Invitation && newItem is Invitation -> {
                (oldItem.sender == newItem.sender
                        && oldItem.timestamp == newItem.timestamp)
            }

            oldItem is MatchToReview && newItem is MatchToReview -> {
                oldItem.match?.matchId == newItem.match?.matchId
            }

            else -> false
        }
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldContent = notifications[oldItemPosition]
        val newContent = newNotifications[newItemPosition]
        return when {
            oldContent is Invitation && newContent is Invitation -> {
                (oldContent.sender == newContent.sender
                        && oldContent.timestamp == newContent.timestamp)
            }

            oldContent is MatchToReview && newContent is MatchToReview -> {
                oldContent.match?.matchId == newContent.match?.matchId
            }

            else -> false
        }
    }
}