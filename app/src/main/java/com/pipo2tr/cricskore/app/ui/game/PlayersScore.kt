package com.pipo2tr.cricskore.app.ui.game

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeTextDefaults
import com.pipo2tr.cricskore.app.ui.common.AsyncImageWithPlaceHolder
import com.pipo2tr.cricskore.app.ui.common.LoadingTimeText
import com.pipo2tr.cricskore.app.utils.GameSummaryParser

@Composable
fun PlayerScores(summary: GameSummaryParser, isRefreshing: Boolean) {

    val currentTime = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()).currentTime
    LoadingTimeText(isLoading = isRefreshing, text = currentTime)
    Divider(color = Color.DarkGray, thickness = 1.dp)
    BattingInfo(summary)
    Divider(color = Color.DarkGray, thickness = 1.dp)
    BowlingInfo(summary)
    Divider(color = Color.DarkGray, thickness = 1.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 0.dp)
    ) {
        Text(
            summary.partnership,
            color = Color.LightGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            summary.lastWicket,
            color = Color.LightGray,
            fontSize = 10.sp,
            fontWeight = FontWeight.Normal,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun BattingInfo(summary: GameSummaryParser) {
    val team = summary.getBattingTeam()
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        AsyncImageWithPlaceHolder(
            imgRes = team.logoPath,
            contentDescription = team.name,
            size = 36.dp
        )
        Column(horizontalAlignment = Alignment.Start) {
            summary.batsMen.forEach { batsman ->
                Text(batsman.name.padEnd(14), textAlign = TextAlign.Start, fontSize = 10.sp)
            }

        }
        Divider(
            color = MaterialTheme.colors.background,
            modifier = Modifier
                .width(16.dp)
        )
        Column(horizontalAlignment = Alignment.End) {
            summary.batsMen.forEach { batsman ->
                Text(
                    "${batsman.runs}(${batsman.balls})",
                    textAlign = TextAlign.End,
                    fontSize = 8.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

@Composable
fun BowlingInfo(summary: GameSummaryParser) {
    val team = summary.getBowlingTeam()
    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        AsyncImageWithPlaceHolder(
            imgRes = team.logoPath,
            contentDescription = team.name,
            size = 36.dp
        )
        Column(horizontalAlignment = Alignment.Start) {
            summary.bowlers.forEach { bowler ->
                Text(bowler.name.padEnd(14), textAlign = TextAlign.Start, fontSize = 10.sp)
            }
        }
        Divider(
            color = MaterialTheme.colors.background,
            modifier = Modifier
                .width(16.dp)
        )
        Column(horizontalAlignment = Alignment.End) {
            summary.bowlers.forEach { bowler ->
                Text(
                    "${bowler.wickets}/${bowler.runs}(${bowler.overs})",
                    textAlign = TextAlign.End,
                    fontSize = 8.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

