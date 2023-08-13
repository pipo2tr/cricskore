package com.pipo2tr.cricskore.app.ui.game

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.wear.compose.material.HorizontalPageIndicator
import androidx.wear.compose.material.PageIndicatorState
import androidx.wear.compose.material.SwipeToDismissBoxState
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.edgeSwipeToDismiss
import com.pipo2tr.cricskore.app.theme.CricSkoreTheme
import com.pipo2tr.cricskore.app.ui.common.ErrorScreen
import com.pipo2tr.cricskore.app.ui.common.Indicator
import com.pipo2tr.cricskore.app.utils.GameSummaryParser
import com.pipo2tr.cricskore.app.utils.NetworkCallback
import com.pipo2tr.cricskore.app.utils.Networking
import com.pipo2tr.cricskore.app.utils.PREF_AUTOMATIC_REFRESH
import com.pipo2tr.cricskore.app.utils.PREF_KEY_REFRESH_TIME
import com.pipo2tr.cricskore.app.utils.RefreshState
import com.pipo2tr.cricskore.app.utils.getAppPref
import kotlinx.coroutines.delay

//data class GameSummaryState(val isFirstLoad: Boolean, val summary: GameSummaryParser?)

const val maxPages = 3

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameSummary(id: String, swipeToDismissBoxState: SwipeToDismissBoxState) {
    val ctx = LocalContext.current
    val network = remember {
        Networking(ctx)
    }
    val pagerState = rememberPagerState { maxPages }
    val pref = getAppPref(ctx)
    val refreshTime = pref.getInt(PREF_KEY_REFRESH_TIME, 10).toLong()
    val autoRefresh = pref.getBoolean(PREF_AUTOMATIC_REFRESH, false)
    var refreshingState by remember {
        mutableStateOf(RefreshState.DONE)
    }
    val gameSummaryModel =
        viewModel<GameSummaryViewModel>()


    var startY by remember { mutableFloatStateOf(0f) }
    fun onRefresh() {
        network.getMatchSummary(id,
            NetworkCallback(
                onResponse = {
                    if (it.isSuccessful) {
                        gameSummaryModel.onSuccess(GameSummaryParser(it.body?.string()!!))
                    } else {
                        gameSummaryModel.onFailure(it.body?.string())
                    }
                    refreshingState = RefreshState.DONE
                },
                onFailure = {
                    gameSummaryModel.onFailure(it.message)
                    refreshingState = RefreshState.DONE
                }
            ))
    }

    val offsetY by animateDpAsState(
        targetValue = if (refreshingState == RefreshState.MANUAL) 50.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.8f)
    )

    LaunchedEffect(Unit) {
        onRefresh()
        if (autoRefresh) {
            while (true) {
                refreshingState = RefreshState.AUTO
                onRefresh()
                delay(refreshTime * 1000)
            }
        }
    }

    CricSkoreTheme {

        if (gameSummaryModel.firstLoad) {

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Indicator()
            }

        } else if (gameSummaryModel.error.isNotEmpty()) {

            ErrorScreen(gameSummaryModel.error)

        } else if (gameSummaryModel.scorecard !== null) {
            val modifier = if (autoRefresh) {
                Modifier
                    .edgeSwipeToDismiss(swipeToDismissBoxState, 20.dp)

            } else {
                Modifier
                    .edgeSwipeToDismiss(swipeToDismissBoxState, 20.dp)
                    .pointerInput(Unit) {
                        detectVerticalDragGestures { change, dragAmount ->
                            when {
                                change.position.y == 0f && dragAmount != 0f -> {
                                    startY = change.position.y
                                }

                                dragAmount > 0 && change.position.y - startY > 150 -> {
                                    if (refreshingState == RefreshState.DONE) {
                                        refreshingState = RefreshState.MANUAL
                                        onRefresh()
                                    }
                                }
                            }
                        }
                    }
            }
            HorizontalPager(
                state = pagerState,
                modifier = modifier
            ) {
                when (it) {
                    0 -> {
                        SummaryScreenLayout(refreshingState == RefreshState.MANUAL, offsetY) {
                            TeamScore(
                                gameSummaryModel.scorecard!!,
                                refreshingState == RefreshState.AUTO
                            )
                        }
                    }

                    1 -> {
                        SummaryScreenLayout(refreshingState == RefreshState.MANUAL, offsetY) {
                            if (gameSummaryModel.scorecard!!.isYetToBegin) {
                                GameStatus(gameSummaryModel.scorecard!!.status)
                            } else {
                                PlayerScores(
                                    gameSummaryModel.scorecard!!,
                                    refreshingState == RefreshState.AUTO
                                )
                            }
                        }
                    }

                    2 -> {
                        SummaryScreenLayout(refreshingState == RefreshState.MANUAL, offsetY) {
                            if (gameSummaryModel.scorecard!!.isYetToBegin) {
                                GameStatus(gameSummaryModel.scorecard!!.status)
                            } else {
                                Commentary(gameSummaryModel.scorecard!!)
                            }
                        }

                    }
                }
            }
            PageIndicator(pagerState.currentPage)
        }

    }
}

@Composable
fun GameStatus(status: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(status, Modifier.fillMaxWidth())
    }
}


@Composable
fun PageIndicator(selectedPage: Int) {
    var finalValue by remember { mutableIntStateOf(0) }

    val animatedSelectedPage by animateFloatAsState(
        targetValue = selectedPage.toFloat(),
    ) {
        finalValue = it.toInt()
    }

    val pageIndicatorState: PageIndicatorState = remember {
        object : PageIndicatorState {
            override val pageOffset: Float
                get() = animatedSelectedPage - finalValue
            override val selectedPage: Int
                get() = finalValue
            override val pageCount: Int
                get() = maxPages
        }
    }
    HorizontalPageIndicator(pageIndicatorState = pageIndicatorState)

}

@Composable
fun SummaryScreenLayout(
    refreshingState: Boolean,
    offsetY: Dp,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(8.dp),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (refreshingState) {
            Spacer(
                modifier = Modifier
                    .offset { IntOffset(0, offsetY.roundToPx()) }
                    .padding(10.dp)
            )
            Indicator()
        }
        content()
    }

}

