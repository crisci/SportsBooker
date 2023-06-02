package com.example.lab2

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.lab2.viewmodels.RatingModalVM
import com.example.lab2.database.court_review.CourtReview
import com.example.lab2.entities.User
import com.example.lab2.viewmodels_firebase.DetailsViewModel
import com.example.lab2.viewmodels_firebase.MatchWithCourt
import com.example.lab2.viewmodels_firebase.MatchWithCourtAndEquipments
import com.facebook.shimmer.ShimmerFrameLayout
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.json.Json
import java.time.format.DateTimeFormatter

@AndroidEntryPoint
class RatingModalBottomSheet : BottomSheetDialogFragment() {

    lateinit var ratingModalVM: RatingModalVM
    lateinit var detailsVM: DetailsViewModel

    private lateinit var cleanlinessCourtRatingBar: RatingBar
    private lateinit var playingSurfaceQualityRatingBar: RatingBar
    private lateinit var lightingRatingBar: RatingBar
    private lateinit var textReview: TextInputEditText
    private lateinit var courtName: TextView
    private lateinit var courtImageView: ImageView
    private lateinit var sportLabel : TextView
    private lateinit var location: TextView
    private lateinit var dateDetail: TextView
    private lateinit var hourDetail: TextView
    private lateinit var playersRecyclerView: RecyclerView

    private lateinit var appPreferences: AppPreferences


    /*    private fun submitReview() {
        val courtReview = CourtReview(
            1,
            ratingModalVM.getCourtToReview().value!!.courtId,
            textReview.text.toString(),
            cleanlinessCourtRatingBar.rating,
            playingSurfaceQualityRatingBar.rating,
            lightingRatingBar.rating
        )
        ratingModalVM.submitReview(courtReview)
    }*/

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val bundle = arguments
        val matchId = bundle?.getString("matchId")
        //ratingModalVM.getMatchToReview(matchId!!)

        val view = inflater.inflate(R.layout.rating_field_layout, container, false)

        ratingModalVM = ViewModelProvider(requireActivity())[RatingModalVM::class.java]
        detailsVM = ViewModelProvider(requireActivity())[DetailsViewModel::class.java]

        detailsVM.getMatchDetails(matchId!!)

        detailsVM.matchWithCourt.observe(viewLifecycleOwner) {
            updateContent(it)
        }

        cleanlinessCourtRatingBar = view.findViewById(R.id.cleanlinessCourtRatingBar)
        playingSurfaceQualityRatingBar = view.findViewById(R.id.playingSurfaceQualityRatingBar)
        lightingRatingBar = view.findViewById(R.id.lightingRatingBar)
        textReview = view.findViewById(R.id.textReview)
        sportLabel = view.findViewById(R.id.sport_label)
        location = view.findViewById(R.id.location)
        dateDetail = view.findViewById(R.id.date_detail)
        hourDetail = view.findViewById(R.id.hour_detail)
        playersRecyclerView = view.findViewById(R.id.recycler_view_mvp)

        detailsVM.listOfPlayers.observe(viewLifecycleOwner) {
            playersRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, true)
            playersRecyclerView.adapter = AdapterPlayersMVP(it)
        }

        courtName = view.findViewById(R.id.courtName)
        courtName.text = "Campo 1"
        courtImageView = view.findViewById(R.id.courtImageView)
        //courtImageView.setImageBitmap(ratingModalVM.getCourtToReview().value!!.courtPhoto)

        val sendReviewButton = view.findViewById<Button>(R.id.send_review)
        /*        sendReviewButton.setOnClickListener {
            // courtId
            submitReview()
            dismiss()
            val dialog = Dialog(requireContext(),R.style.RoundedDialog)
            dialog.setContentView(R.layout.thank_you_review_layout)
            dialog.setCancelable(true)
            dialog.show()
            val handler = Handler(Looper.getMainLooper())
            // Close the "thank you" dialog after 2.5 seconds
            handler.postDelayed({
                dialog.dismiss()
            }, 2500)
        }*/

        return view
    }

    fun updateContent(matchWithCourt: MatchWithCourt) {
        Picasso.get().load(matchWithCourt.court.image).into(courtImageView, object : Callback {
            override fun onSuccess() {
                sportLabel.text = matchWithCourt.court.sport
                courtName.text = matchWithCourt.court.name
                location.text = "Via Giovanni Magni, 32"
                dateDetail.text = matchWithCourt.match.date.format(
                    DateTimeFormatter.ofPattern("dd-MM"))
                hourDetail.text = matchWithCourt.match.time.format(DateTimeFormatter.ofPattern("HH:mm"))
                //loading.visibility = View.GONE
                //details.visibility = View.VISIBLE

            }

            override fun onError(e: Exception?) {
                Toast.makeText(context, "Unable to load the details", Toast.LENGTH_SHORT)
                    .show()
                dismiss()
            }

        })
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}

class ViewHolderPlayersMVP(v: View): RecyclerView.ViewHolder(v) {
    val playerImage: ImageView? = v.findViewById(R.id.player_image_details) ?: null
    val shimmer: ShimmerFrameLayout? = v.findViewById(R.id.shimmer_layout) ?: null
}

class AdapterPlayersMVP(private var listOfPlayers: List<User>): RecyclerView.Adapter<ViewHolderPlayersMVP>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderPlayersMVP {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.player_details, parent, false)
        return ViewHolderPlayersMVP(v)
    }

    override fun getItemCount(): Int = listOfPlayers.size

    override fun onBindViewHolder(holder: ViewHolderPlayersMVP, position: Int) {
        Picasso.get().load(listOfPlayers[position].image)
            .into(holder.playerImage, object : Callback {
                override fun onSuccess() {
                    holder.shimmer?.stopShimmer()
                    holder.shimmer?.hideShimmer()
                }

                override fun onError(e: Exception?) {
                }

            })
    }
}
