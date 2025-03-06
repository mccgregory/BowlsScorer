package com.example.bowls

import android.content.Context
import android.os.Bundle
import android.view.SoundEffectConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val showSplash by remember { mutableStateOf(true) }

            LaunchedEffect(Unit) {
                delay(1500L)
                setContent {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) { MyApp(modifier = Modifier.fillMaxSize()) }
                }
            }

            if (showSplash) {
                // Splash UI handled by SplashTheme in styles.xml
            }
        }
    }
}

@Composable
fun MyApp(modifier: Modifier = Modifier) {
    var shouldShowOnboarding by rememberSaveable { mutableStateOf(true) }
    var gameSingles by rememberSaveable { mutableStateOf(false) }
    Surface(modifier) {
        if (shouldShowOnboarding) {
            OnboardingScreen(
                gameSinglesClicked = { gameSingles = true },
                onContinueClicked = { shouldShowOnboarding = false }
            )
        } else {
            Scorer(
                gameSingles = gameSingles,
                onNewGame = {
                    shouldShowOnboarding = true
                    gameSingles = false // Reset game type choice
                }
            )
        }
    }
}

@Composable
fun OnboardingScreen(
    gameSinglesClicked: () -> Unit,
    onContinueClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        coroutineScope.launch {
                            showExitDialog = true
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Bowls Scorer",
                modifier = Modifier
                    .padding(8.dp)
                    .offset(y = 10.dp)
            )
            Text(
                "Choose game...",
                modifier = Modifier
                    .padding(8.dp)
                    .offset(y = 0.dp)
            )
            Button(
                modifier = Modifier
                    .padding(vertical = 15.dp)
                    .offset(y = -20.dp),
                onClick = {
                    onContinueClicked()
                    gameSinglesClicked()
                }
            ) { Text("Singles") }
            Button(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .offset(y = -30.dp),
                onClick = onContinueClicked
            ) { Text("Doubles") }
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit App") },
                text = { Text("Are you sure you want to exit?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            context.finish()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.Black
                        )
                    ) { Text("Confirm") }
                },
                dismissButton = {
                    Button(
                        onClick = { showExitDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green,
                            contentColor = Color.Black
                        )
                    ) { Text("Cancel") }
                },
                containerColor = Color.Black,
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}

@Composable
fun Scorer(gameSingles: Boolean, onNewGame: () -> Unit, modifier: Modifier = Modifier) {
    val mContext = LocalContext.current     // For Toast
    val view = LocalView.current            // For CLICK sound
    var showExitDialog by remember { mutableStateOf(false) }
    var showDeadEndDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var editingEnd by remember { mutableStateOf<Triple<Int, Int, Int>?>(null) } // Track the end being edited
    val coroutineScope = rememberCoroutineScope()

    var myScore by rememberSaveable { mutableStateOf(0) }
    var theirScore by rememberSaveable { mutableStateOf(0) }
    var strtMyScore by rememberSaveable { mutableStateOf(0) }
    var strtTheirScore by rememberSaveable { mutableStateOf(0) }
    var endCount by rememberSaveable { mutableStateOf(1) }
    var meClick by rememberSaveable { mutableStateOf(false) }
    var themClick by rememberSaveable { mutableStateOf(false) }
    var maxClick by rememberSaveable { mutableStateOf(0) }
    var bowls by rememberSaveable { mutableStateOf(0) }
    var gameOver by rememberSaveable { mutableStateOf(false) }

    // Store history of ends: list of (end number, Up score, Down score)
    val endHistory = rememberSaveable(
        saver = Saver(
            save = { history ->
                history.map { triple -> listOf(triple.first, triple.second, triple.third) }
            },
            restore = { saved ->
                val list = mutableListOf<Triple<Int, Int, Int>>()
                saved.forEach { innerList ->
                    list.add(Triple(innerList[0], innerList[1], innerList[2]))
                }
                list.toMutableStateList() // Convert to SnapshotStateList
            }
        )
    ) {
        mutableStateListOf<Triple<Int, Int, Int>>()
    }

    // Set max points per end based on game type
    if (gameSingles) { maxClick = 2 + 1 } // Max per end is 2
    else { maxClick = 4 + 1 }             // Max per end is 4

    // Check for game over (winner at 21)
    LaunchedEffect(myScore, theirScore) {
        if (myScore >= 21 || theirScore >= 21) {
            gameOver = true
        }
    }

    // Reset game state for a new game
    fun resetGame() {
        myScore = 0
        theirScore = 0
        strtMyScore = 0
        strtTheirScore = 0
        endCount = 1
        meClick = false
        themClick = false
        bowls = 0
        gameOver = false
        endHistory.clear() // Clear history on new game
    }

    // Handle dead end logic after confirmation
    fun handleDeadEnd() {
        endHistory.add(Triple(endCount, myScore - strtMyScore, theirScore - strtTheirScore))
        endCount++
        meClick = false
        themClick = false
        bowls = 0
        strtMyScore = myScore
        strtTheirScore = theirScore
        mToast(mContext)
        view.playSoundEffect(SoundEffectConstants.CLICK)
    }

    // Update scores after editing an end
    fun updateScoresAfterEdit(editedEnd: Triple<Int, Int, Int>, newUpScore: Int, newDownScore: Int) {
        val index = endHistory.indexOfFirst { it.first == editedEnd.first }
        if (index != -1) {
            val oldUpScore = endHistory[index].second
            val oldDownScore = endHistory[index].third
            endHistory[index] = Triple(editedEnd.first, newUpScore, newDownScore)

            // Adjust running totals from this end onward
            val scoreAdjustmentUp = newUpScore - oldUpScore
            val scoreAdjustmentDown = newDownScore - oldDownScore
            for (i in index until endHistory.size) {
                endHistory[i] = Triple(
                    endHistory[i].first,
                    endHistory[i].second + scoreAdjustmentUp,
                    endHistory[i].third + scoreAdjustmentDown
                )
            }
            // Update current scores
            myScore += scoreAdjustmentUp
            theirScore += scoreAdjustmentDown
            strtMyScore = myScore
            strtTheirScore = theirScore
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = {
                        coroutineScope.launch {
                            showExitDialog = true
                        }
                    }
                )
            },
        color = Color.Black
    ) {
        if (gameOver) {
            // Game Over Screen: Show winner and options
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (myScore >= 21) "Up Wins!" else "Down Wins!",
                    color = if (myScore >= 21) Color.White else Color.Yellow,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(8.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.SpaceAround,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            resetGame()
                            onNewGame()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green,
                            contentColor = Color.Black
                        )
                    ) { Text("New Game") }
                    Button(
                        onClick = { mContext.finish() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.Black
                        )
                    ) { Text("Exit") }
                }
            }
        } else {
            // Main Scoring Screen
            Column(
                verticalArrangement = Arrangement.spacedBy(
                    space = 2.dp,
                    alignment = Alignment.CenterVertically
                ),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    "Up",
                    modifier = Modifier
                        .padding(8.dp)
                        .offset(x = 35.dp, y = -30.dp),
                    color = Color.White,
                    fontSize = 25.sp
                )

                Button(
                    onClick = {
                        if (!gameOver && (!themClick && !meClick || meClick)) {
                            meClick = true
                            bowls++
                            if (bowls < maxClick) {
                                myScore++
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .padding(8.dp)
                        .offset(x = 2.dp, y = (-50).dp)
                ) { Text("$myScore", fontSize = 50.sp) }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(
                    space = 2.dp,
                    alignment = Alignment.CenterVertically
                ),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    "Down",
                    modifier = Modifier
                        .padding(8.dp)
                        .offset(x = -35.dp, y = -30.dp),
                    color = Color.Yellow,
                    fontSize = 25.sp
                )

                Button(
                    onClick = {
                        if (!gameOver && (!themClick && !meClick || themClick)) {
                            themClick = true
                            bowls++
                            if (bowls < maxClick) {
                                theirScore++
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black,
                        contentColor = Color.Yellow
                    ),
                    modifier = Modifier
                        .padding(8.dp)
                        .offset(x = -10.dp, y = -50.dp)
                ) { Text("$theirScore", fontSize = 50.sp) }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.padding(24.dp)
            ) {
                IconButton(
                    onClick = {
                        meClick = false
                        themClick = false
                        bowls = 0
                        myScore = strtMyScore
                        theirScore = strtTheirScore
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .offset(x = -10.dp, y = -10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear",
                        modifier = Modifier.size(40.dp),
                        tint = Color.Red
                    )
                }
                // Blue Circle Button for History (Sinking Sun Effect)
                Button(
                    onClick = {
                        showHistoryDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Blue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .size(40.dp)
                        .offset(x = 0.dp, y = 20.dp),
                    shape = CircleShape
                ) {
                    Text("H", fontSize = 12.sp, textAlign = TextAlign.Center)
                }
                Text(
                    "END",
                    modifier = Modifier.offset(x = 22.dp, y = 0.dp),
                    color = Color.Green,
                    fontSize = 25.sp
                )
                Button(
                    onClick = {
                        if (!gameOver) {
                            if (!meClick && !themClick) {
                                showDeadEndDialog = true
                            } else {
                                endHistory.add(Triple(endCount, myScore - strtMyScore, theirScore - strtTheirScore))
                                endCount++
                                meClick = false
                                themClick = false
                                bowls = 0
                                strtMyScore = myScore
                                strtTheirScore = theirScore
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Green,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier.offset(x = 20.dp, y = 0.dp)
                ) { Text("$endCount", fontSize = 20.sp) }
            }
        }

        // Exit Dialog for long press
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit App") },
                text = { Text("Are you sure you want to exit?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            mContext.finish()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.Black
                        )
                    ) { Text("Confirm") }
                },
                dismissButton = {
                    Button(
                        onClick = { showExitDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green,
                            contentColor = Color.Black
                        )
                    ) { Text("Cancel") }
                },
                containerColor = Color.Black,
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }

        // Dead End Confirmation Dialog
        if (showDeadEndDialog) {
            AlertDialog(
                onDismissRequest = { showDeadEndDialog = false },
                title = { Text("Confirm Dead End") },
                text = { Text("This is a dead end. Move to next end?") },
                confirmButton = {
                    Button(
                        onClick = {
                            showDeadEndDialog = false
                            handleDeadEnd()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green,
                            contentColor = Color.Black
                        )
                    ) { Text("Confirm") }
                },
                dismissButton = {
                    Button(
                        onClick = { showDeadEndDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.Black
                        )
                    ) { Text("Cancel") }
                },
                containerColor = Color.Black,
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }

        // History Dialog
        if (showHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showHistoryDialog = false },
                title = { Text("End History") },
                text = {
                    if (endHistory.isEmpty()) {
                        Text("No ends played yet.", color = Color.White)
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Display ends in reverse order (last end first)
                            endHistory.reversed().forEach { (endNum, upScore, downScore) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "$endNum:    $upScore    $downScore",
                                        color = Color.White,
                                        fontSize = 14.sp
                                    )
                                    Button(
                                        onClick = { editingEnd = Triple(endNum, upScore, downScore) },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color.Blue,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.size(width = 60.dp, height = 24.dp)
                                    ) {
                                        Text("Edit", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showHistoryDialog = false },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green,
                            contentColor = Color.Black
                        )
                    ) { Text("Close") }
                },
                dismissButton = {},
                containerColor = Color.Black,
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }

        // Edit End Dialog
        if (editingEnd != null) {
            var upScoreInput by remember { mutableStateOf(editingEnd!!.second.toString()) }
            var downScoreInput by remember { mutableStateOf(editingEnd!!.third.toString()) }

            AlertDialog(
                onDismissRequest = { editingEnd = null },
                title = { Text("Edit End ${editingEnd!!.first}") },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Up Score:", color = Color.White, fontSize = 14.sp)
                            TextField(
                                value = upScoreInput,
                                onValueChange = { upScoreInput = it },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(40.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.White),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.DarkGray,
                                    unfocusedContainerColor = Color.DarkGray,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Down Score:", color = Color.Yellow, fontSize = 14.sp)
                            TextField(
                                value = downScoreInput,
                                onValueChange = { downScoreInput = it },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(40.dp),
                                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp, color = Color.Yellow),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.DarkGray,
                                    unfocusedContainerColor = Color.DarkGray,
                                    focusedTextColor = Color.Yellow,
                                    unfocusedTextColor = Color.Yellow,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val newUpScore = upScoreInput.toIntOrNull() ?: editingEnd!!.second
                            val newDownScore = downScoreInput.toIntOrNull() ?: editingEnd!!.third
                            updateScoresAfterEdit(editingEnd!!, newUpScore, newDownScore)
                            editingEnd = null
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Green,
                            contentColor = Color.Black
                        )
                    ) { Text("Save") }
                },
                dismissButton = {
                    Button(
                        onClick = { editingEnd = null },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red,
                            contentColor = Color.Black
                        )
                    ) { Text("Cancel") }
                },
                containerColor = Color.Black,
                titleContentColor = Color.White,
                textContentColor = Color.White
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() {
    OnboardingScreen(gameSinglesClicked = {}, onContinueClicked = {})
}

@Preview(showBackground = true)
@Composable
fun ScorerPreview() {
    Scorer(gameSingles = true, onNewGame = {})
}

private fun mToast(context: Context) {
    Toast.makeText(context, "This is a DEAD END", Toast.LENGTH_LONG).show()
}

private fun Context.finish() {
    (this as? ComponentActivity)?.finish()
    android.os.Process.killProcess(android.os.Process.myPid())
}