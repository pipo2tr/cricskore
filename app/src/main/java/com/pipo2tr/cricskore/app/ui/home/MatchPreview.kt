package com.pipo2tr.cricskore.app.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.paint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.CardDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.pipo2tr.cricskore.app.utils.truncateText

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MatchPreview(
    modifier: Modifier = Modifier,
    team1: Pair<String, String>,
    team2: Pair<String, String>,
    id: String,
    onClick: (String) -> Unit,
    onLongClick: () -> Unit,
) {
    var (team1Name, team1Score) = team1
    var (team2Name, team2Score) = team2
    if (team1Score.isNotEmpty()) {
        team1Name = truncateText(team1Name, maxTruncate(team1Score))
    }

    if (team2Score.isNotEmpty()) {
        team2Name = truncateText(team2Name, maxTruncate(team2Score))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = true, onLongClick = { onLongClick() },
                onClick = {
                    onClick(id)
                },
            )
            .height(IntrinsicSize.Min)
            .clip(shape = MaterialTheme.shapes.large)
            .paint(
                painter = CardDefaults.cardBackgroundPainter(),
                contentScale = ContentScale.Crop
            )
            .padding(CardDefaults.ContentPadding)
    )
    {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(
                text = team1Name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Text(
                text = team1Score,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

        }

        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
            Text(
                text = team2Name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Text(
                text = team2Score,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}

fun maxTruncate(score: String): Int {
    if (score.length < 5) {
        return 18
    }
    return 15
}

