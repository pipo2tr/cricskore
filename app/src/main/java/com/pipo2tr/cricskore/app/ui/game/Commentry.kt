package com.pipo2tr.cricskore.app.ui.game

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.items
import androidx.wear.compose.material.rememberScalingLazyListState
import com.pipo2tr.cricskore.app.utils.CommentaryInfo
import com.pipo2tr.cricskore.app.utils.GameSummaryParser

@Composable
fun Commentary(summary: GameSummaryParser) {
    val state = rememberScalingLazyListState()
    ScalingLazyColumn(
        state = state,
        modifier = Modifier.fillMaxWidth(),
        autoCentering = AutoCenteringParams(itemIndex = 0)
    ) {
        items(summary.commentary) {
            BallCommentary(comms = it)
        }
    }

}

@Composable
fun BallCommentary(comms: CommentaryInfo) {
    Column(modifier = Modifier.fillMaxWidth(0.95f)) {
        Text(text = "${comms.over}, ${comms.comm}", color = Color.LightGray, fontSize = 12.sp)
        Divider(color = Color.DarkGray, thickness = 1.dp)
    }
}
