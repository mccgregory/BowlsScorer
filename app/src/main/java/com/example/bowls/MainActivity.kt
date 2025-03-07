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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.wear.compose.material.MaterialTheme
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
                        color = MaterialTheme.colors.background
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
                    .offset(y = (-20).dp),
                onClick = {
                    onContinueClicked()
                    gameSinglesClicked()
                }
            ) { Text("Singles") }
            Button(
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .offset(y = (-30).dp),
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.Black)
                    ) { Text("Confirm") }
                },
                dismissButton = {
                    Button(
                        onClick = { showExitDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)
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
    //---- State Declarations ----
    val mContext = LocalContext.current
    val view = LocalView.current
    var showExitDialog by remember { mutableStateOf(false) }
    var showDeadEndDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var editingEnd by remember { mutableStateOf<Int?>(null) }
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

    var tempMyScore by remember { mutableStateOf(0) }
    var tempTheirScore by remember { mutableStateOf(0) }
    var tempMeClick by remember { mutableStateOf(false) }
    var tempThemClick by remember { mutableStateOf(false) }
    var tempBowls by remember { mutableStateOf(0) }

    val endHistory = rememberSaveable(
        saver = Saver(
            save = { history: MutableList<Triple<Int, Int, Int>> ->
                history.map { triple -> listOf(triple.first, triple.second, triple.third) }
            },
            restore = { saved: List<List<Int>> ->
                mutableListOf<Triple<Int, Int, Int>>().apply {
                    saved.forEach { innerList -> add(Triple(innerList[0], innerList[1], innerList[2])) }
                }.toMutableStateList()
            }
        )
    ) { mutableStateListOf<Triple<Int, Int, Int>>() }

    //---- Game Logic ----
    if (gameSingles) { maxClick = 2 + 1 } else { maxClick = 4 + 1 }
    LaunchedEffect(myScore, theirScore) {
        if (myScore >= 21 || theirScore >= 21) gameOver = true
    }

    fun resetGame() {
        myScore = 0; theirScore = 0; strtMyScore = 0; strtTheirScore = 0
        endCount = 1; meClick = false; themClick = false; bowls = 0
        gameOver = false; endHistory.clear()
    }

    fun completeEnd() {
        endHistory.add(Triple(endCount, myScore, theirScore))
        endCount++; meClick = false; themClick = false; bowls = 0
        strtMyScore = myScore; strtTheirScore = theirScore
        view.playSoundEffect(SoundEffectConstants.CLICK)
    }

    fun completeEditEnd() {
        val index = endHistory.indexOfFirst { it.first == editingEnd }
        if (index != -1) {
            val oldUpScore = endHistory[index].second
            val oldDownScore = endHistory[index].third
            val upDiff = tempMyScore - oldUpScore
            val downDiff = tempTheirScore - oldDownScore
            endHistory[index] = Triple(editingEnd!!, tempMyScore, tempTheirScore)
            for (i in index + 1 until endHistory.size) {
                endHistory[i] = Triple(endHistory[i].first, endHistory[i].second + upDiff, endHistory[i].third + downDiff)
            }
            myScore += upDiff; theirScore += downDiff
            strtMyScore = myScore; strtTheirScore = theirScore
        }
        editingEnd = null
        view.playSoundEffect(SoundEffectConstants.CLICK)
    }

    fun startEditing(endNum: Int) {
        val end = endHistory.firstOrNull { it.first == endNum }
        if (end != null) {
            tempMyScore = end.second; tempTheirScore = end.third
            tempMeClick = false; tempThemClick = false; tempBowls = 0
            editingEnd = endNum
        }
    }

    //---- UI Layout ----
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = { coroutineScope.launch { showExitDialog = true } })
            },
        color = if (editingEnd != null) Color(0xFFB0B0B0) else Color.Black
    ) {
        //---- Game Over Screen ----
        if (gameOver) {
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
                    Button(onClick = { resetGame(); onNewGame() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)) { Text("New Game") }
                    Button(onClick = { mContext.finish() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.Black)) { Text("Exit") }
                }
            }
        }
        //---- Edit Mode Screen ----
        else if (editingEnd != null) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.Start
            ) {
                Text("Up", modifier = Modifier.padding(8.dp).offset(x = 35.dp, y = (-30).dp), color = Color.White, fontSize = 25.sp)
                Surface(
                    modifier = Modifier.padding(8.dp).offset(x = 2.dp, y = (-50).dp).pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { if (!tempThemClick && !tempMeClick || tempMeClick) { tempMeClick = true; tempBowls++; if (tempBowls < maxClick) tempMyScore++ } },
                            onLongPress = { if (tempMyScore > 0) { tempMyScore--; tempBowls = maxOf(0, tempBowls - 1) } }
                        )
                    },
                    color = Color(0xFFB0B0B0), shape = RoundedCornerShape(8.dp)
                ) { Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text("$tempMyScore", color = Color.White, fontSize = 50.sp) } }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.End
            ) {
                Text("Down", modifier = Modifier.padding(8.dp).offset(x = (-35).dp, y = (-30).dp), color = Color.Yellow, fontSize = 25.sp)
                Surface(
                    modifier = Modifier.padding(8.dp).offset(x = (-10).dp, y = (-50).dp).pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { if (!tempThemClick && !tempMeClick || tempThemClick) { tempThemClick = true; tempBowls++; if (tempBowls < maxClick) tempTheirScore++ } },
                            onLongPress = { if (tempTheirScore > 0) { tempTheirScore--; tempBowls = maxOf(0, tempBowls - 1) } }
                        )
                    },
                    color = Color(0xFFB0B0B0), shape = RoundedCornerShape(8.dp)
                ) { Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text("$tempTheirScore", color = Color.Yellow, fontSize = 50.sp) } }
            }
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.padding(24.dp)
            ) {
                IconButton(onClick = { tempMeClick = false; tempThemClick = false; tempBowls = 0; val end = endHistory.firstOrNull { it.first == editingEnd }; if (end != null) { tempMyScore = end.second; tempTheirScore = end.third } }, modifier = Modifier.size(40.dp).offset(x = (-10).dp, y = (-10).dp)) { Icon(Icons.Filled.Clear, "Clear", modifier = Modifier.size(40.dp), tint = Color.Red) }
                Button(onClick = { editingEnd = null }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.Black), modifier = Modifier.size(40.dp).offset(x = 0.dp, y = 20.dp), shape = CircleShape) { Text("X", fontSize = 12.sp, textAlign = TextAlign.Center) }
                Text("END", modifier = Modifier.offset(x = 22.dp, y = 0.dp), color = Color.Green, fontSize = 25.sp)
                Button(onClick = { completeEditEnd() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black), modifier = Modifier.offset(x = 20.dp, y = 0.dp)) { Text("Save", fontSize = 20.sp) }
            }
        }
        //---- Main Scoring Screen ----
        else {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.Start
            ) {
                Text("Up", modifier = Modifier.padding(8.dp).offset(x = 35.dp, y = (-30).dp), color = Color.White, fontSize = 25.sp)
                Surface(
                    modifier = Modifier.padding(8.dp).offset(x = 2.dp, y = (-50).dp).pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { if (!gameOver && (!themClick && !meClick || meClick)) { meClick = true; bowls++; if (bowls < maxClick) myScore++ } },
                            onLongPress = { if (myScore > 0) { myScore--; bowls = maxOf(0, bowls - 1) } }
                        )
                    },
                    color = Color.Black, shape = RoundedCornerShape(8.dp)
                ) { Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text("$myScore", color = Color.White, fontSize = 50.sp) } }
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.End
            ) {
                Text("Down", modifier = Modifier.padding(8.dp).offset(x = (-35).dp, y = (-30).dp), color = Color.Yellow, fontSize = 25.sp)
                Surface(
                    modifier = Modifier.padding(8.dp).offset(x = (-10).dp, y = (-50).dp).pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { if (!gameOver && (!themClick && !meClick || themClick)) { themClick = true; bowls++; if (bowls < maxClick) theirScore++ } },
                            onLongPress = { if (theirScore > 0) { theirScore--; bowls = maxOf(0, bowls - 1) } }
                        )
                    },
                    color = Color.Black, shape = RoundedCornerShape(8.dp)
                ) { Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text("$theirScore", color = Color.Yellow, fontSize = 50.sp) } }
            }
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    IconButton(
                        onClick = { meClick = false; themClick = false; bowls = 0; myScore = strtMyScore; theirScore = strtTheirScore },
                        modifier = Modifier.size(40.dp)
                    ) { Icon(Icons.Filled.Clear, "Clear", modifier = Modifier.size(40.dp), tint = Color.Red) }
                    Text("END", color = Color.Green, fontSize = 25.sp)
                    Button(
                        onClick = { if (!gameOver) { if (!meClick && !themClick) showDeadEndDialog = true else completeEnd() } },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                        modifier = Modifier.size(width = 50.dp, height = 40.dp)
                    ) { Text("$endCount", fontSize = 20.sp) }
                }
                Button(
                    onClick = { showHistoryDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue, contentColor = Color.White),
                    modifier = Modifier.size(40.dp).offset(y = 20.dp),
                    shape = CircleShape
                ) { Text("H", fontSize = 12.sp, textAlign = TextAlign.Center) }
            }
        }

        //---- Dialogs ----
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit App") },
                text = { Text("Are you sure you want to exit?") },
                confirmButton = { Button(onClick = { showExitDialog = false; mContext.finish() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.Black)) { Text("Confirm") } },
                dismissButton = { Button(onClick = { showExitDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)) { Text("Cancel") } },
                containerColor = Color.Black, titleContentColor = Color.White, textContentColor = Color.White
            )
        }
        if (showDeadEndDialog) {
            AlertDialog(
                onDismissRequest = { showDeadEndDialog = false },
                title = { Text("Confirm Dead End") },
                text = { Text("This is a dead end. Move to next end?") },
                confirmButton = { Button(onClick = { showDeadEndDialog = false; completeEnd(); mToast(mContext) }, colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)) { Text("Confirm") } },
                dismissButton = { Button(onClick = { showDeadEndDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.Black)) { Text("Cancel") } },
                containerColor = Color.Black, titleContentColor = Color.White, textContentColor = Color.White
            )
        }
        if (showHistoryDialog) {
            AlertDialog(
                onDismissRequest = { showHistoryDialog = false },
                title = { Text("End History") },
                text = {
                    if (endHistory.isEmpty()) {
                        Text("No ends played yet.", color = Color.White, fontSize = 26.sp)
                    } else {
                        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                            endHistory.reversed().forEach { (endNum, upScore, downScore) ->
                                Row(modifier = Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("$endNum:    $upScore    $downScore", color = Color.White, fontSize = 26.sp)
                                    Button(onClick = { startEditing(endNum); showHistoryDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Blue, contentColor = Color.White), modifier = Modifier.size(width = 40.dp, height = 20.dp)) { Text("Edit", fontSize = 10.sp) }
                                }
                            }
                        }
                    }
                },
                confirmButton = { Button(onClick = { showHistoryDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)) { Text("Close") } },
                dismissButton = {},
                containerColor = Color.Black, titleContentColor = Color.White, textContentColor = Color.White
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