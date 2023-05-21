package com.example.lab2

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RatingBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.lab2.calendar.RatingModalVM
import com.example.lab2.database.court_review.CourtReview
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RatingModalBottomSheet : BottomSheetDialogFragment() {

    lateinit var ratingModalVM: RatingModalVM

    private lateinit var cleanlinessRating: RatingBar
    private lateinit var maintenanceRating: RatingBar
    private lateinit var lightingRating: RatingBar
    private lateinit var amenitiesRating: RatingBar
    private lateinit var accessibilityRating: RatingBar
    private lateinit var safetyRating: RatingBar
    private lateinit var comfortRating: RatingBar
    private lateinit var textReview: TextInputEditText
    private lateinit var courtName: TextView


    private fun submitReview() {
        val courtReview = CourtReview(
            1,
            ratingModalVM.getCourtToReview().value!!.courtId,
            textReview.text.toString(),
            cleanlinessRating.rating,
            maintenanceRating.rating,
            lightingRating.rating,
            amenitiesRating.rating,
            accessibilityRating.rating,
            safetyRating.rating,
            comfortRating.rating
        )
        ratingModalVM.submitReview(courtReview)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.rating_field_layout, container, false)

        ratingModalVM = ViewModelProvider(requireActivity())[RatingModalVM::class.java]

        cleanlinessRating = view.findViewById(R.id.cleanlinessRatingBar)
        maintenanceRating = view.findViewById(R.id.maintenanceRatingBar)
        lightingRating = view.findViewById(R.id.lightingRatingBar)
        amenitiesRating = view.findViewById(R.id.amenitiesRatingBar)
        accessibilityRating = view.findViewById(R.id.accessibilityRatingBar)
        safetyRating = view.findViewById(R.id.safetyRatingBar)
        comfortRating = view.findViewById(R.id.comfortRatingBar)
        textReview = view.findViewById(R.id.textReview)
        courtName = view.findViewById(R.id.courtName)
        courtName.text = "${ratingModalVM.getCourtToReview().value!!.name}"

        val sendReviewButton = view.findViewById<Button>(R.id.send_review)
        sendReviewButton.setOnClickListener {
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
        }

        return view
    }

    companion object {
        const val TAG = "ModalBottomSheet"
    }
}