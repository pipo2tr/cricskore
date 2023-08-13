/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.pipo2tr.cricskore.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.wear.compose.material.SwipeToDismissBox
import androidx.wear.compose.material.rememberSwipeToDismissBoxState
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.pipo2tr.cricskore.app.ui.game.GameSummary
import com.pipo2tr.cricskore.app.ui.home.Home
import com.pipo2tr.cricskore.app.ui.setting.Setting


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val boxState = rememberSwipeToDismissBoxState()
            val navController = rememberSwipeDismissableNavController()
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = "home",
            ) {
                composable("home") {
                    Home(onPreviewClicked = { id ->
                        navController.navigate("match/${id}")
                    }, onSettingsClicked = {
                        navController.navigate("setting")
                    })
                }
                composable("match/{id}") {
                    SwipeToDismissBox(state = boxState, onDismissed = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.id) {
                                inclusive = true
                            }
                        }
                    }) { bg ->
                        if (!bg) {
                            GameSummary(it.arguments?.getString("id")!!, boxState)
                        }
                    }
                }
                composable("setting") {
                    Setting()
                }
            }
        }
    }

}




