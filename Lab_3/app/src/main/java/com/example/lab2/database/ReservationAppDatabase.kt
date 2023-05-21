package com.example.lab2.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.lab2.database.court.Court
import com.example.lab2.database.court.CourtDAO
import com.example.lab2.database.court_review.CourtReview
import com.example.lab2.database.court_review.CourtReviewDAO
import com.example.lab2.database.player.Player
import com.example.lab2.database.player.PlayerDAO
import com.example.lab2.database.player_badge_rating.PlayerBadgeRating
import com.example.lab2.database.player_badge_rating.PlayerBadgeRatingDAO
import com.example.lab2.database.player_reservation_join.PlayerReservation
import com.example.lab2.database.player_reservation_join.PlayerReservationDAO
import com.example.lab2.database.reservation.Reservation
import com.example.lab2.database.reservation.ReservationDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Database(entities = [Player::class, Reservation::class, Court::class, PlayerBadgeRating::class, PlayerReservation::class, CourtReview::class], version = 1, exportSchema = false)
@TypeConverters(EquipmentConverter::class, DateConverter::class, TimeConverter::class)
abstract class ReservationAppDatabase : RoomDatabase() {
    abstract fun playerDao() : PlayerDAO
    abstract fun reservationDao() : ReservationDAO
    abstract fun courtDao() : CourtDAO
    abstract fun playerBadgeRatingDAO() : PlayerBadgeRatingDAO
    abstract fun playerReservationDAO() : PlayerReservationDAO

    abstract fun courtReviewDao() : CourtReviewDAO


    companion object {
        @Volatile
        private var INSTANCE: ReservationAppDatabase? = null

        fun getDatabase(context: Context) : ReservationAppDatabase =
            (INSTANCE ?:
            synchronized(this){
                val i = INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ReservationAppDatabase::class.java,
                    "sample.db"
                ).createFromAsset("database/sample.db").build()
                INSTANCE = i
                INSTANCE
            })!!
    }
}

