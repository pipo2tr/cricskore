package com.pipo2tr.cricskore.app.ui.game

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
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
import com.pipo2tr.cricskore.app.utils.BallInfo
import com.pipo2tr.cricskore.app.utils.BallType
import com.pipo2tr.cricskore.app.utils.GameSummaryParser
import com.pipo2tr.cricskore.app.utils.MatchDescription
import com.pipo2tr.cricskore.app.utils.TeamInfo
import com.pipo2tr.cricskore.app.utils.truncateText

@Composable
fun TeamScore(summary: GameSummaryParser, isRefreshing: Boolean) {
    val currentTime = TimeTextDefaults.timeSource(TimeTextDefaults.timeFormat()).currentTime
    LoadingTimeText(isRefreshing, currentTime)
    MatchInfo(summary.description)
    Divider(color = Color.DarkGray, thickness = 1.dp)
    if (summary.isYetToBegin) {
        ShowTeamVs(summary)
    } else {
        TeamScores(summary)
    }
    Divider(color = Color.DarkGray, thickness = 1.dp)
    MatchSummary(summary.status)
    CreateOverList(summary.currentOver)
    if (summary.totalOvers.isNotBlank()) {
        Text(
            "Over ${summary.totalOvers}",
            textAlign = TextAlign.Center,
            fontSize = 8.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }

}

@Composable
fun ShowTeamVs(summary: GameSummaryParser) {
    val (teamA, teamB) = summary.teams
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(verticalAlignment = CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
            AsyncImageWithPlaceHolder(
                imgRes = teamA.logoPath,
                contentDescription = teamA.name,
                size = 64.dp
            )
            Text(
                text = "VS",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            AsyncImageWithPlaceHolder(
                imgRes = teamB.logoPath,
                contentDescription = teamB.name,
                size = 64.dp
            )
        }
    }
}

@Composable
fun MatchInfo(description: MatchDescription) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            Text(
                truncateText(description.tourney, 30),
                textAlign = TextAlign.Start,
                fontSize = 10.sp,
            )
            Text(
                description.title,
                textAlign = TextAlign.Start,
                color = Color.Gray,
                fontSize = 8.sp
            )
        }

    }
}


@Composable
fun TeamScores(summary: GameSummaryParser) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Row(verticalAlignment = CenterVertically) {
            BattingTeamScore(summary.getBattingTeam())
            Divider(
                color = MaterialTheme.colors.background,
                modifier = Modifier
                    .width(16.dp)
            )
            BowlingTeamScore(summary.getBowlingTeam())
        }
    }
}

@Composable
fun BattingTeamScore(team: TeamInfo) {
    val name = team.abbr
    val (score, overs) = team.scorecard()
    Row(horizontalArrangement = Arrangement.Start, verticalAlignment = CenterVertically) {
        AsyncImageWithPlaceHolder(
            imgRes = team.logoPath,
            contentDescription = team.name,
            size = 48.dp
        )
        Column(modifier = Modifier.padding(4.dp, 4.dp)) {
            Text(name, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(score, color = Color.LightGray, fontSize = 12.sp)
            Text(overs, color = Color.Gray, fontSize = 10.sp)
        }
    }

}

@Composable
fun BowlingTeamScore(team: TeamInfo) {
    val name = team.abbr
    val (score, overs) = team.scorecard()
    Log.d("ImageResource", team.logoPath)
    Row(horizontalArrangement = Arrangement.End, verticalAlignment = CenterVertically) {
        Column(modifier = Modifier.padding(4.dp, 4.dp)) {
            Text(
                name,
                color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold
            )
            Text(score, color = Color.LightGray, fontSize = 12.sp)
            Text(overs, color = Color.Gray, fontSize = 10.sp)
        }
        AsyncImageWithPlaceHolder(
            imgRes = team.logoPath,
            contentDescription = team.name,
            size = 48.dp
        )
    }
}

@Composable
fun MatchSummary(summary: String) {
    Text(
        summary,
        color = Color.LightGray,
        fontSize = 10.sp,
        fontWeight = FontWeight.Normal,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun CreateOverList(over: List<BallInfo>) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp, 0.dp)
    ) {
        over.forEachIndexed { index, element ->
            val isLast = index == over.size - 1
            when (element.type) {
                BallType.DOT -> CurrentOver(
                    text = "•",
                    isLast = isLast
                )

                BallType.RUNS -> CurrentOver(
                    text = element.value,
                    isLast = isLast
                )

                BallType.BOUNDARY -> CurrentOver(
                    text = element.value,
                    textColor = Color.White,
                    bgColor = Color.Blue,
                    isLast = isLast
                )

                BallType.WICKET -> CurrentOver(
                    text = "W",
                    textColor = Color.White,
                    bgColor = Color.Red,
                    isLast = isLast
                )

                BallType.EXTRA -> CurrentOver(
                    text = element.value,
                    textColor = Color.Yellow,
                    isLast = isLast
                )


            }

        }
    }

}


@Composable
fun CurrentOver(
    text: String,
    textColor: Color = Color.White,
    bgColor: Color = Color.DarkGray,
    isLast: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(14.dp)
            .background(color = bgColor, shape = RoundedCornerShape(2.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
    if (!isLast) {
        Spacer(modifier = Modifier.width(2.dp))
    }
}
