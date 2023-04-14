package com.example.lab2

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import com.example.lab2.entities.BadgeType
import com.example.lab_2.R

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
            R.layout.badge_layout, this, true)

        badgeImage = view.findViewById(R.id.badge_image)
        when (badge.key) {
            BadgeType.SPEED ->  badgeImage.setImageResource(R.drawable.badge_speed)
            BadgeType.PRECISION ->  badgeImage.setImageResource( R.drawable.badge_precision)
            BadgeType.TEAM_WORK ->  badgeImage.setImageResource(R.drawable.badge_team)
        }
    }

}
