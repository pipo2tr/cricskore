package com.pipo2tr.cricskore.app.ui.game

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.pipo2tr.cricskore.app.utils.GameSummaryParser


class GameSummaryViewModel() : ViewModel() {

    private var _scorecard: MutableState<GameSummaryParser?> = mutableStateOf(null)
    var scorecard: GameSummaryParser?
        get() = _scorecard.value
        set(value) {
            _scorecard.value = value
        }

    private var _firstLoad = mutableStateOf(true)

    var firstLoad
        get() = _firstLoad.value
        set(value) {
            _firstLoad.value = value
        }

    private var _error = mutableStateOf("")

    var error
        get() = _error.value
        set(value) {
            _error.value = value
        }

    fun onSuccess(summary: GameSummaryParser) {
        scorecard = summary
        error = ""
        firstLoad = false
    }

    fun onFailure(err: String?) {
        scorecard = null
        error = if (err.isNullOrBlank()) {
            "Something went wrong when getting this games score"
        } else if (err.contains("Chain Request")) {
            "No internet"
        } else {
            err
        }
        firstLoad = false
    }

}