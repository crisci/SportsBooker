package com.example.lab2

import androidx.recyclerview.widget.DiffUtil
import com.example.lab2.database.reservation.ReservationWithCourtAndEquipments

class FilterDiffCallback (
    private  val filters: List<String?>,
    private val newFilters: List<String?>): DiffUtil.Callback() {

        override fun getOldListSize(): Int = filters.size

        override fun getNewListSize(): Int = newFilters.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return filters[oldItemPosition] == newFilters[newItemPosition]
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            val sport1 = filters[oldItemPosition]
            val sport2 = newFilters[newItemPosition]
            return sport1 == sport2
        }

}