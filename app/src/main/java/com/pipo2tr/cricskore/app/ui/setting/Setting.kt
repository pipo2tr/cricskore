package com.pipo2tr.cricskore.app.ui.setting

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.ScalingLazyColumn
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.ToggleChip
import androidx.wear.compose.material.ToggleChipDefaults
import androidx.wear.compose.material.itemsIndexed
import androidx.wear.compose.material.rememberScalingLazyListState
import com.pipo2tr.cricskore.app.theme.CricSkoreTheme
import com.pipo2tr.cricskore.app.utils.PREF_AUTOMATIC_REFRESH
import com.pipo2tr.cricskore.app.utils.PREF_KEY_REFRESH_TIME
import com.pipo2tr.cricskore.app.utils.getAppPref

val items = listOf<Int>(5, 7, 10, 13, 15, 20, 25, 30)

@Composable
fun Setting() {
    val state = rememberScalingLazyListState()
    val pref = getAppPref(LocalContext.current)
    var automaticRefresh by remember {
        mutableStateOf(
            pref.getBoolean(
                PREF_AUTOMATIC_REFRESH,
                false
            )
        )
    }
    var currentItem by remember {
        mutableIntStateOf(
            items.indexOf(
                pref.getInt(
                    PREF_KEY_REFRESH_TIME,
                    10
                )
            )
        )
    }

    fun onAutoRefreshToggle(value: Boolean) {
        automaticRefresh = value
        with(pref.edit()) {
            putBoolean(PREF_AUTOMATIC_REFRESH, value)
            apply()
        }
    }

    CricSkoreTheme {
        ScalingLazyColumn(state = state) {
            item {
                Text(
                    "Select Refresh Trigger",
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            item {
                ToggleChip(
                    modifier = Modifier.fillMaxWidth(),
                    checked = automaticRefresh,
                    onCheckedChange = {
                        onAutoRefreshToggle(it)
                    },
                    label = {
                        Text("Auto refresh scores", fontSize = 12.sp)
                    },
                    toggleControl = {
                        Checkbox(
                            checked = automaticRefresh,
                            enabled = true,
                            onCheckedChange = {
                                onAutoRefreshToggle(it)
                            },
                        )
                    },
                )
            }
            if (automaticRefresh) {
                item {
                    Text(
                        "Select Refresh time",
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                itemsIndexed(items) { index, item ->
                    RefreshTimePicker(
                        isChecked = currentItem == index,
                        label = "$item seconds",
                        onCheckChange = {
                            currentItem = index
                            with(pref.edit()) {
                                putInt(PREF_KEY_REFRESH_TIME, items[index])
                                apply()
                            }
                        })
                }
            }

        }
    }
}

@Composable
fun RefreshTimePicker(isChecked: Boolean, label: String, onCheckChange: () -> Unit) {
    ToggleChip(
        checked = isChecked,
        onCheckedChange = { onCheckChange() },
        label = { Text(label, fontSize = 12.sp) },
        toggleControl = { RadioButton(selected = isChecked, onClick = onCheckChange) },
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp, 0.dp),
        colors = ToggleChipDefaults.toggleChipColors(
            uncheckedToggleControlColor = ToggleChipDefaults.SwitchUncheckedIconColor
        ),
    )
}
