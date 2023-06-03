package com.example.lab2.database

import android.app.Application
import com.example.lab2.database.court.CourtDAO
import com.example.lab2.database.player.PlayerDAO
import com.example.lab2.database.player_badge_rating.PlayerBadgeRatingDAO
import com.example.lab2.database.player_reservation_join.PlayerReservationDAO
import com.example.lab2.database.reservation.ReservationDAO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object APPModule {

    @Singleton
    @Provides
    fun getAppDB(context: Application): ReservationAppDatabase {
        return ReservationAppDatabase.getDatabase(context)
    }

    @Singleton
    @Provides
    fun getPlayerDAO(appDB: ReservationAppDatabase): PlayerDAO {
        return appDB.playerDao()
    }

    @Singleton
    @Provides
    fun getCourtDAO(appDB: ReservationAppDatabase): CourtDAO {
        return appDB.courtDao()
    }

    @Singleton
    @Provides
    fun getPlayerBadgeRatingDAO(appDB: ReservationAppDatabase): PlayerBadgeRatingDAO {
        return appDB.playerBadgeRatingDAO()
    }

    @Singleton
    @Provides
    fun getPlayerReservationDAO(appDB: ReservationAppDatabase): PlayerReservationDAO {
        return appDB.playerReservationDAO()
    }

    @Singleton
    @Provides
    fun getReservationDAO(appDB: ReservationAppDatabase): ReservationDAO {
        return appDB.reservationDao()
    }

}