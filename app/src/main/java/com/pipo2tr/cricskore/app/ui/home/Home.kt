package com.pipo2tr.cricskore.app.ui.home

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.AutoCenteringParams
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import androidx.wear.compose.material.itemsIndexed
import androidx.wear.compose.material.rememberScalingLazyListState
import com.pipo2tr.cricskore.R
import com.pipo2tr.cricskore.app.theme.CricSkoreTheme
import com.pipo2tr.cricskore.app.ui.common.Indicator
import com.pipo2tr.cricskore.app.utils.Item
import com.pipo2tr.cricskore.app.utils.NetworkCallback
import com.pipo2tr.cricskore.app.utils.Networking
import com.pipo2tr.cricskore.app.utils.PREF_KEY_SCORECARD
import com.pipo2tr.cricskore.app.utils.getAppPref
import com.pipo2tr.cricskore.app.utils.liveScoreParser

data class HomeState(var isLoading: Boolean, var livescores: List<Item>?, var error: String?)
data class DialogState(var showDialog: Boolean, var id: String, var teams: String)

@Composable
fun Home(onPreviewClicked: (String) -> Unit, onSettingsClicked: () -> Unit) {
    val ctx = LocalContext.current
    val networking = remember {
        Networking(ctx)
    }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    val pref = getAppPref(ctx)
    var homeState by remember {
        mutableStateOf(HomeState(true, null, null))
    }
    var dialogState by remember { mutableStateOf(DialogState(false, "", "")) }
    val savedId = pref.getString(PREF_KEY_SCORECARD, "") ?: ""
    LaunchedEffect(refreshTrigger) {
        homeState.isLoading = true
        networking.getLiveScores(NetworkCallback(
            onResponse = { response ->
                homeState = if (response.isSuccessful) {
                    HomeState(false, liveScoreParser(savedId, response.body!!.string()), null)
                } else {
                    HomeState(false, null, response.body!!.string())
                }
            },
            onFailure = {
                HomeState(false, null, it.message)
            }
        ))
    }

    val listState = rememberScalingLazyListState()
    CricSkoreTheme {
        Scaffold(
            positionIndicator = {
                PositionIndicator(
                    scalingLazyListState = listState
                )
            },
        ) {
            if (homeState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Indicator()
                }
            } else if (!homeState.error.isNullOrBlank()) {
                Text(homeState.error!!)
            } else if (!homeState.livescores.isNullOrEmpty()) {
                ScalingLazyColumn(
                    modifier = Modifier
                        .background(MaterialTheme.colors.background),
                    autoCentering = AutoCenteringParams(itemIndex = 0),
                    state = listState,

                    ) {
                    item {
                        Text(
                            text = ctx.getString(R.string.app_name),
                            fontSize = 18.sp,
                            style = TextStyle(
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Cursive
                            ),
                            modifier = Modifier.padding(5.dp)

                        )
                    }
                    itemsIndexed(homeState.livescores!!) { _, item ->
                        MatchPreview(
                            team1 = item.team1,
                            team2 = item.team2,
                            id = item.id,
                            onClick = onPreviewClicked,
                            onLongClick = {
                                dialogState = DialogState(
                                    true,
                                    item.id,
                                    item.team1.first + " vs " + item.team2.first
                                )
                            }
                        )
                    }
                    item {
                        IconButton(onClick = onSettingsClicked) {
                            Icon(
                                imageVector = Icons.Rounded.Settings,
                                contentDescription = "SettingsIcon",
                                tint = MaterialTheme.colors.primary
                            )
                        }
                    }
                }
                PinScorecardDialog(pref = pref, dialogState = dialogState) {
                    dialogState = DialogState(false, "", "")
                    refreshTrigger++
                }
            }
        }

    }
}


@Composable
fun PinScorecardDialog(pref: SharedPreferences, dialogState: DialogState, closeDialog: () -> Unit) {
    Dialog(dialogState.showDialog, onDismissRequest = { closeDialog() }) {
        Alert(
            title = { Text("Title text displayed here", textAlign = TextAlign.Center) },
            negativeButton = {
                Button(
                    colors = ButtonDefaults.secondaryButtonColors(),
                    onClick = {
                        closeDialog()
                    }) {
                    Text("✘")
                }
            },
            positiveButton = {
                Button(onClick = {
                    with(pref.edit()) {
                        putString("ScoreCard", dialogState.id)
                        apply()
                    }
                    closeDialog()
                }) { Text("✔", fontWeight = FontWeight.Bold) }
            },
            contentPadding =
            PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 32.dp),
        ) {
            Text(
                text = "Would you like to pin this scoreCard for ${dialogState.teams}?",
                textAlign = TextAlign.Center
            )
        }
    }
}
