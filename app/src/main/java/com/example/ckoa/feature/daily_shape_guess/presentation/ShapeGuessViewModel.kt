package com.example.ckoa.feature.daily_shape_guess.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ckoa.core.country.domain.Country
import com.example.ckoa.core.country.domain.GetDailyCountryUseCase
import com.example.ckoa.core.progress.domain.GameStatus
import com.example.ckoa.feature.daily_shape_guess.domain.GetShapeProposalsUseCase
import com.example.ckoa.feature.daily_shape_guess.domain.SubmitShapeGuessUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ShapeGuessViewModel (
    private val getDailyCountryUseCase: GetDailyCountryUseCase,
    private val getShapeProposalsUseCase: GetShapeProposalsUseCase,
    private val submitShapeGuessUseCase: SubmitShapeGuessUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(ShapeGuessState())
    val state: StateFlow<ShapeGuessState> = _state.asStateFlow()

    private var targetCountry: Country? = null
    private val maxAllowedGuesses = 6

    init {
        initializeDailyGame()
    }

    private fun initializeDailyGame() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            try {
                val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                targetCountry = getDailyCountryUseCase(currentDate)

                _state.update {
                    it.copy(
                        isLoading = false,
                        targetShapeGeoJson = targetCountry?.geoJsonCoordinates ?: "",
                        gameStatus = GameStatus.PLAYING
                    )
                }
            } catch (exception: Exception) {
                _state.update {
                    it.copy(isLoading = false, errorMessage = exception.message)
                }
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }

        viewModelScope.launch {
            val proposals = getShapeProposalsUseCase(query)
            _state.update { it.copy(proposals = proposals) }
        }
    }

    fun submitGuess(guessedIsoCode: String) {
        val currentTarget = targetCountry ?: return

        if (_state.value.gameStatus != GameStatus.PLAYING) return

        viewModelScope.launch {
            try {
                val guessResult = submitShapeGuessUseCase(
                    guessedIsoCode = guessedIsoCode,
                    targetIsoCode = currentTarget.isoCode
                )

                val updatedGuesses = _state.value.guesses + guessResult

                val newGameStatus = when {
                    guessResult.isCorrect -> GameStatus.WON
                    updatedGuesses.size >= maxAllowedGuesses -> GameStatus.LOST
                    else -> GameStatus.PLAYING
                }

                _state.update {
                    it.copy(
                        guesses = updatedGuesses,
                        gameStatus = newGameStatus,
                        searchQuery = "",
                        proposals = emptyList()
                    )
                }
            } catch (exception: Exception) {
                _state.update { it.copy(errorMessage = exception.message) }
            }
        }
    }
}