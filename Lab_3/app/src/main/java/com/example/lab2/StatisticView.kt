package com.example.lab2

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.example.lab2.entities.Sport
import com.example.lab2.entities.Statistic

class StatisticView(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private val statistic: Statistic,
) : LinearLayout(context, attrs, defStyleAttr) {
    private val playedCount: TextView
    //private val victoriesCount: TextView
    //private val drawCount: TextView
    //private val drawLayout: LinearLayout
    private val image:ImageView


    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.statistic_layout, this, true)

        playedCount = view.findViewById(R.id.statistic_played_count)
        //victoriesCount = view.findViewById(R.id.statistic_victories_count)
        //drawCount = view.findViewById(R.id.statistic_draws_count)
        //drawLayout = view.findViewById(R.id.statistic_draw_layout)
        image = view.findViewById(R.id.statistic_image)

        playedCount.text = "${statistic.gamesPlayed}"
        //victoriesCount.text = "${statistic.gamesWon}"

        /*
        if (statistic.gamesDrawn == null){
            val parent: LinearLayout = view.findViewById(R.id.statistic_layout_parent)
            parent.removeView(drawLayout)
        } else {
            drawCount.text = "${statistic.gamesDrawn}"
        }

         */

        when (statistic.sport) {
            Sport.SOCCER -> image.setImageResource(R.drawable.sport_soccer)
            Sport.GOLF ->  image.setImageResource(R.drawable.sport_golf)
            Sport.TENNIS ->  image.setImageResource(R.drawable.sport_tennis)
            Sport.BASEBALL ->  image.setImageResource(R.drawable.sport_baseball)
            Sport.BASKETBALL ->  image.setImageResource(R.drawable.sport_basketball)
            Sport.PADEL ->  image.setImageResource(R.drawable.sport_padel)
        }

    }

}
