package com.example.lab2.database.player

import androidx.lifecycle.LiveData
import androidx.room.Query
import com.example.lab2.database.ReservationAppDatabase
import com.example.lab2.database.player_reservation_join.ReservationWithPlayers
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

/*  The repository depends on a PlayerDAO instance, which is responsible for data access operations.
*
* */
class PlayerRepository @Inject constructor(private val playerDAO: PlayerDAO) {

    /* Suspending function: it can be called from a coroutine and will
    *  suspend until the operation is completed.
    *  We also use withContext to switch to the Dispatchers.IO context,
    * indicating that the operation should be performed on a background thread
    * */

    suspend fun loadPlayerById(playerId: Int) = playerDAO.loadPlayerById(playerId)

    suspend fun loadReservationsByPlayerId(playerId: Int, date: LocalDate) =
        withContext(Dispatchers.IO) { playerDAO.loadReservationsByPlayerId(playerId, date) }

    suspend fun insertPlayer(player: Player) = playerDAO.insertPlayer(player)

    suspend fun deletePlayers() = playerDAO.deletePlayers()

}