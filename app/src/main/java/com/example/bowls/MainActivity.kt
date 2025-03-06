package com.example.bowls

import android.content.Context
import android.os.Bundle
import android.view.SoundEffectConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    var showDeadEndDialog by remember { mutableStateOf(false) } // For dead end confirmation
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

    // Choose Game
    if (gameSingles) { maxClick = 2 + 1 } // Max per end is 2
    else { maxClick = 4 + 1 }             // Max per end is 4

    // Check for game over (winner at 21)
    LaunchedEffect(myScore, theirScore) {
        if (myScore >= 21 || theirScore >= 21) {
            gameOver = true
        }
    }

    // Reset function for new game
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
    }

    // Function to handle dead end confirmation
    fun handleDeadEnd() {
        endCount++
        meClick = false
        themClick = false
        bowls = 0
        strtMyScore = myScore
        strtTheirScore = theirScore
        mToast(mContext)
        view.playSoundEffect(SoundEffectConstants.CLICK)
    }

    // Surface container with long press detection
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
            // Game Over Screen
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
                                showDeadEndDialog = true // Show confirmation dialog
                            } else {
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

        // Exit Dialog
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
                            handleDeadEnd() // Proceed with dead end logic
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