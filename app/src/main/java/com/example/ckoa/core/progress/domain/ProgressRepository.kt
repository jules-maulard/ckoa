package com.example.ckoa.core.progress.domain

interface ProgressRepository {
    suspend fun getDailyGameState(date: String): DailyGameState?
    suspend fun saveDailyGameState(state: DailyGameState)
    suspend fun getAllHistoryBeforeDate(date: String): List<DailyGameState>
}