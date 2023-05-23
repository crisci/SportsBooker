package com.example.lab2

import androidx.recyclerview.widget.DiffUtil
import java.time.LocalTime

class TimeslotDiffCallback (
    private  val filters: List<LocalTime?>,
    private val newFilters: List<LocalTime?>): DiffUtil.Callback() {

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