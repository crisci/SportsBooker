package it.polito.sportsbooker.profile

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import it.polito.sportsbooker.R
import it.polito.sportsbooker.entities.BadgeType

class BadgeView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val badge: Map.Entry<BadgeType, Int>,
) : LinearLayout(context, attrs, defStyleAttr) {
    private val badgeImage: ImageView

    init {

        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(
            R.layout.badge_layout, this, true
        )

        badgeImage = view.findViewById(R.id.skill_image)
        when (badge.key) {
            BadgeType.SPEED -> badgeImage.setImageResource(R.drawable.badge_speed)
            BadgeType.PRECISION -> badgeImage.setImageResource(R.drawable.badge_precision)
            BadgeType.TEAM_WORK -> badgeImage.setImageResource(R.drawable.badge_team)
            BadgeType.STRATEGY -> badgeImage.setImageResource(R.drawable.badge_speed) // TODO change
            BadgeType.ENDURANCE -> badgeImage.setImageResource(R.drawable.badge_precision) // TODO change
        }
    }

}
