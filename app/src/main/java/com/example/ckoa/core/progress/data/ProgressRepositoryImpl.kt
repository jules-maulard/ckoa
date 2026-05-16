package com.example.ckoa.core.progress.data

import com.example.ckoa.core.progress.domain.DailyGameState
import com.example.ckoa.core.progress.domain.GameStatus
import com.example.ckoa.core.progress.domain.GameStep
import com.example.ckoa.core.progress.domain.ProgressRepository

class ProgressRepositoryImpl(
    private val progressDao: ProgressDao
) : ProgressRepository {
    override suspend fun getDailyGameState(date: String): DailyGameState? {
        val entity = progressDao.getGameStateByDate(date) ?: return null
        return mapEntityToDomain(entity)
    }

    override suspend fun saveDailyGameState(state: DailyGameState) {
        val entity = mapDomainToEntity(state)
        progressDao.insertOrUpdateGameState(entity)
    }

    override suspend fun getAllHistoryBeforeDate(date: String): List<DailyGameState> {
        val entities = progressDao.getAllHistoryBeforeDate(date)
        return entities.map { mapEntityToDomain(it) }
    }

    private fun mapEntityToDomain(entity: DailyGameStateEntity): DailyGameState {
        return DailyGameState(
            date = entity.date,
            currentStep = GameStep.valueOf(entity.currentStep),
            flagGameStatus = GameStatus.valueOf(entity.flagGameStatus),
            flagGuesses = entity.flagGuesses,
            shapeGameStatus = GameStatus.valueOf(entity.shapeGameStatus),
            shapeGuesses = entity.shapeGuesses
        )
    }

    private fun mapDomainToEntity(domain: DailyGameState): DailyGameStateEntity {
        return DailyGameStateEntity(
            date = domain.date,
            currentStep = domain.currentStep.name,
            flagGameStatus = domain.flagGameStatus.name,
            flagGuesses = domain.flagGuesses,
            shapeGameStatus = domain.shapeGameStatus.name,
            shapeGuesses = domain.shapeGuesses
        )
    }
}