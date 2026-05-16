package com.example.ckoa.core.progress.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ProgressDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateGameState(gameState: DailyGameStateEntity)

    @Query("SELECT * FROM daily_game_state_table WHERE date = :date")
    suspend fun getGameStateByDate(date: String): DailyGameStateEntity?

    @Query("SELECT * FROM daily_game_state_table WHERE date < :date ORDER BY date DESC")
    suspend fun getAllHistoryBeforeDate(date: String): List<DailyGameStateEntity>
}