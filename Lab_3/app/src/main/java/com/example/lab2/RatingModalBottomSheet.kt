package com.example.lab2

import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
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

    private lateinit var cleanlinessCourtRatingBar: RatingBar
    private lateinit var playingSurfaceQualityRatingBar: RatingBar
    private lateinit var lightingRatingBar: RatingBar
    private lateinit var textReview: TextInputEditText
    private lateinit var courtName: TextView
    private lateinit var courtImageView: ImageView

    private lateinit var appPreferences: AppPreferences


    private fun submitReview() {
        val courtReview = CourtReview(
            1,
            ratingModalVM.getCourtToReview().value!!.courtId,
            textReview.text.toString(),
            cleanlinessCourtRatingBar.rating,
            playingSurfaceQualityRatingBar.rating,
            lightingRatingBar.rating
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

        cleanlinessCourtRatingBar = view.findViewById(R.id.cleanlinessCourtRatingBar)
        playingSurfaceQualityRatingBar = view.findViewById(R.id.playingSurfaceQualityRatingBar)
        lightingRatingBar = view.findViewById(R.id.lightingRatingBar)
        textReview = view.findViewById(R.id.textReview)

        appPreferences = AppPreferences(requireContext())

        // Example "Don't show again" checkbox behavior
        val dontShowAgainCheckbox: CheckBox = view.findViewById(R.id.checkbox_dont_show_again)
        dontShowAgainCheckbox.setOnCheckedChangeListener { _, isChecked ->
            // Update the preference when the checkbox state changes
            appPreferences.shouldShowRatingDialog = !isChecked
        }

        courtName = view.findViewById(R.id.courtName)
        courtName.text = "${ratingModalVM.getCourtToReview().value!!.name}"
        courtImageView = view.findViewById(R.id.courtImageView)
        courtImageView.setImageBitmap(ratingModalVM.getCourtToReview().value!!.courtPhoto)

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