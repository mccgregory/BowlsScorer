package com.mccgregory.bowlsscorer

import android.os.Bundle
import android.view.SoundEffectConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var showExitDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                if (showExitDialog) {
                    AlertDialog(
                        onDismissRequest = { showExitDialog = false },
                        title = { Text("Exit App") },
                        text = { Text("Are you sure you want to exit? Scores will be lost.") },
                        confirmButton = {
                            Button(
                                onClick = { finish() },
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
                var shouldShowOnboarding by rememberSaveable { mutableStateOf(true) }
                var gameSingles by rememberSaveable { mutableStateOf(false) }
                if (shouldShowOnboarding) {
                    OnboardingScreen(
                        gameSinglesClicked = { gameSingles = true },
                        onContinueClicked = { shouldShowOnboarding = false }
                    )
                } else {
                    Scorer(
                        gameSingles = gameSingles,
                        onNewGame = { shouldShowOnboarding = true; gameSingles = false }
                    )
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                coroutineScope.launch { showExitDialog = true }
            }
        })
    }

    override fun onKeyDown(keyCode: Int, event: android.view.KeyEvent?): Boolean {
        if (keyCode == android.view.KeyEvent.KEYCODE_BACK) {
            coroutineScope.launch { showExitDialog = true }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun OnboardingScreen(gameSinglesClicked: () -> Unit, onContinueClicked: () -> Unit) {
    val mContext = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Select Game Type", fontSize = 20.sp, color = Color.White, textAlign = TextAlign.Center)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    Toast.makeText(mContext, "Singles Chosen!", Toast.LENGTH_SHORT).show()
                    gameSinglesClicked()
                    onContinueClicked()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue, contentColor = Color.Black),
                modifier = Modifier.size(width = 80.dp, height = 40.dp)
            ) { Text("Singles", fontSize = 14.sp) }
            Button(
                onClick = {
                    Toast.makeText(mContext, "Doubles Chosen!", Toast.LENGTH_SHORT).show()
                    onContinueClicked()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Blue, contentColor = Color.Black),
                modifier = Modifier.size(width = 80.dp, height = 40.dp)
            ) { Text("Doubles", fontSize = 14.sp) }
        }
    }
}

@Composable
fun Scorer(gameSingles: Boolean, onNewGame: () -> Unit, modifier: Modifier = Modifier) {
    val mContext = LocalContext.current
    val view = LocalView.current
    var myScore by rememberSaveable { mutableStateOf(0) }
    var theirScore by rememberSaveable { mutableStateOf(0) }
    var strtMyScore by rememberSaveable { mutableStateOf(0) }
    var strtTheirScore by rememberSaveable { mutableStateOf(0) }
    var tempMyScore by rememberSaveable { mutableStateOf(0) }
    var tempTheirScore by rememberSaveable { mutableStateOf(0) }
    var tempMeClick by rememberSaveable { mutableStateOf(false) }
    var tempThemClick by rememberSaveable { mutableStateOf(false) }
    var tempBowls by rememberSaveable { mutableStateOf(0) }
    var tempAddUpScore by rememberSaveable { mutableStateOf(0) }
    var tempAddDownScore by rememberSaveable { mutableStateOf(0) }
    var endCount by rememberSaveable { mutableStateOf(1) }
    val maxClick = if (gameSingles) 4 else 8
    val maxScorePerSide = if (gameSingles) 2 else 4
    var isScoringCurrentEnd by rememberSaveable { mutableStateOf(false) }
    var gameOver by rememberSaveable { mutableStateOf(false) }
    var editingEnd: Int? by rememberSaveable { mutableStateOf(null) }
    var addingEnd: Int? by rememberSaveable { mutableStateOf(null) }
    var showDeadEndDialog by rememberSaveable { mutableStateOf(false) }
    var showHistoryDialog by rememberSaveable { mutableStateOf(false) }
    val endHistorySaver = Saver<MutableList<Triple<Int, Int, Int>>, String>(
        save = { it.joinToString(",") { triple -> "${triple.first}:${triple.second}:${triple.third}" } },
        restore = { str ->
            val list = mutableListOf<Triple<Int, Int, Int>>()
            str.split(",").forEach {
                val parts = it.split(":")
                if (parts.size == 3) {
                    list.add(Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt()))
                }
            }
            list
        }
    )
    var endHistory by rememberSaveable(stateSaver = endHistorySaver) { mutableStateOf(mutableListOf<Triple<Int, Int, Int>>()) }

    fun resetGame() {
        myScore = 0
        theirScore = 0
        strtMyScore = 0
        strtTheirScore = 0
        tempMyScore = 0
        tempTheirScore = 0
        tempMeClick = false
        tempThemClick = false
        tempBowls = 0
        endCount = 1
        isScoringCurrentEnd = false
        gameOver = false
        editingEnd = null
        addingEnd = null
        endHistory.clear()
    }

    fun resetCurrentEnd() {
        tempMyScore = 0
        tempTheirScore = 0
        tempMeClick = false
        tempThemClick = false
        tempBowls = 0
        isScoringCurrentEnd = false
    }

    fun completeEnd() {
        myScore = strtMyScore + tempMyScore
        theirScore = strtTheirScore + tempTheirScore
        endHistory.add(Triple(endCount, tempMyScore, tempTheirScore))
        endCount++
        strtMyScore = myScore
        strtTheirScore = theirScore
        resetCurrentEnd()
        Toast.makeText(mContext, "End $endCount saved!", Toast.LENGTH_SHORT).show()
        view.playSoundEffect(SoundEffectConstants.CLICK)
    }

    fun completeEditEnd() {
        if (editingEnd != null) {
            val updatedHistory = mutableListOf<Triple<Int, Int, Int>>()
            endHistory.forEach {
                if (it.first == editingEnd) {
                    updatedHistory.add(Triple(it.first, tempMyScore, tempTheirScore))
                    myScore = myScore - it.second + tempMyScore
                    theirScore = theirScore - it.third + tempTheirScore
                } else {
                    updatedHistory.add(it)
                }
            }
            endHistory.clear()
            endHistory.addAll(updatedHistory)
            endHistory.sortBy { it.first }
            strtMyScore = myScore
            strtTheirScore = theirScore
            Toast.makeText(mContext, "End $editingEnd replaced with $tempMyScore-$tempTheirScore", Toast.LENGTH_SHORT).show()
            editingEnd = null
            resetCurrentEnd()
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    fun completeAddEnd() {
        if (addingEnd != null) {
            val updatedHistory = mutableListOf<Triple<Int, Int, Int>>()
            val newEnd = Triple(addingEnd!!, tempAddUpScore, tempAddDownScore)
            endHistory.filter { it.first < addingEnd!! }.forEach { updatedHistory.add(it) }
            updatedHistory.add(newEnd)
            endHistory.filter { it.first >= addingEnd!! }.forEach { updatedHistory.add(Triple(it.first + 1, it.second, it.third)) }
            endHistory.clear()
            endHistory.addAll(updatedHistory)
            endHistory.sortBy { it.first }
            endCount = endHistory.size + 1
            myScore = endHistory.sumOf { it.second }
            theirScore = endHistory.sumOf { it.third }
            strtMyScore = myScore
            strtTheirScore = theirScore
            Toast.makeText(mContext, "End $addingEnd replaced with $tempAddUpScore-$tempAddDownScore", Toast.LENGTH_SHORT).show()
            addingEnd = null
            tempAddUpScore = 0
            tempAddDownScore = 0
            tempMeClick = false
            tempThemClick = false
            tempBowls = 0
            resetCurrentEnd()
            view.playSoundEffect(SoundEffectConstants.CLICK)
        }
    }

    fun startEditing(endNum: Int) {
        val end = endHistory.firstOrNull { it.first == endNum }
        if (end != null) {
            tempMyScore = end.second
            tempTheirScore = end.third
            tempMeClick = false
            tempThemClick = false
            tempBowls = 0
            editingEnd = endNum
        }
    }

    fun startAdding(endNum: Int) {
        tempAddUpScore = 0
        tempAddDownScore = 0
        tempMeClick = false
        tempThemClick = false
        tempBowls = 0
        addingEnd = endNum
    }

    fun replaceEnd(endNum: Int) {
        editingEnd = null
        startAdding(endNum)
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        if (!gameOver && !isScoringCurrentEnd && editingEnd == null && addingEnd == null) {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            isScoringCurrentEnd = true
                        }
                    }
                )
            },
        color = if (isScoringCurrentEnd) Color(0xFF00008B) else Color.Black
    ) {
        if (gameOver) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (myScore > theirScore) "Game Over\nWe Win\n$myScore - $theirScore" else "Game Over\nThey Win\n$theirScore - $myScore",
                    color = if (myScore > theirScore) Color.Green else Color.Red,
                    fontSize = 25.sp,
                    textAlign = TextAlign.Center
                )
                Button(
                    onClick = {
                        resetGame()
                        onNewGame()
                        Toast.makeText(mContext, "New Game started!", Toast.LENGTH_SHORT).show()
                        view.playSoundEffect(SoundEffectConstants.CLICK)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                    modifier = Modifier.padding(top = 20.dp).size(width = 80.dp, height = 40.dp)
                ) { Text("New Game", fontSize = 14.sp) }
            }
        } else if (editingEnd != null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
                        Text("Up", modifier = Modifier.padding(start = 8.dp, bottom = 4.dp).offset(x = 35.dp, y = 10.dp), color = Color.White, fontSize = 25.sp)
                        Surface(
                            modifier = Modifier
                                .padding(start = 8.dp, end = 4.dp)
                                .offset(x = 2.dp, y = 0.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            if (!tempThemClick && !tempMeClick || tempMeClick) {
                                                tempMeClick = true
                                                tempBowls++
                                                if (tempBowls < maxClick && tempMyScore < maxScorePerSide) tempMyScore++
                                            }
                                        },
                                        onLongPress = {
                                            if (tempMyScore > 0) {
                                                tempMyScore--
                                                tempBowls = maxOf(0, tempBowls - 1)
                                            }
                                        }
                                    )
                                },
                            color = Color(0xFFB0B0B0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text("$tempMyScore", color = Color.White, fontSize = 36.sp)
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.End) {
                        Text("Down", modifier = Modifier.padding(end = 8.dp, bottom = 4.dp).offset(x = (-35).dp, y = 10.dp), color = Color.Yellow, fontSize = 25.sp)
                        Surface(
                            modifier = Modifier
                                .padding(start = 4.dp, end = 8.dp)
                                .offset(x = (-10).dp, y = 0.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            if (!tempThemClick && !tempMeClick || tempThemClick) {
                                                tempThemClick = true
                                                tempBowls++
                                                if (tempBowls < maxClick && tempTheirScore < maxScorePerSide) tempTheirScore++
                                            }
                                        },
                                        onLongPress = {
                                            if (tempTheirScore > 0) {
                                                tempTheirScore--
                                                tempBowls = maxOf(0, tempBowls - 1)
                                            }
                                        }
                                    )
                                },
                            color = Color(0xFFB0B0B0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text("$tempTheirScore", color = Color.Yellow, fontSize = 36.sp)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val hasChanges = tempMyScore > 0 || tempTheirScore > 0
                    Button(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            if (hasChanges) {
                                completeEditEnd()
                            } else {
                                editingEnd = null
                                resetCurrentEnd()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (hasChanges) Color.Green else Color.Red, contentColor = if (hasChanges) Color.Black else Color.White),
                        modifier = Modifier.size(width = 60.dp, height = 30.dp)
                    ) { Text(if (hasChanges) "Save" else "Cancel", fontSize = 14.sp) }
                    Button(
                        onClick = { replaceEnd(editingEnd!!) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                        modifier = Modifier.size(width = 60.dp, height = 30.dp)
                    ) { Text("ADD", fontSize = 14.sp) }
                }
                Text("Editing End $editingEnd", color = Color.White, fontSize = 20.sp)
            }
        } else if (addingEnd != null) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
                        Text("Up", modifier = Modifier.padding(start = 8.dp, bottom = 4.dp).offset(x = 35.dp, y = 10.dp), color = Color.White, fontSize = 25.sp)
                        Surface(
                            modifier = Modifier
                                .padding(start = 8.dp, end = 4.dp)
                                .offset(x = 2.dp, y = 0.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            if (!tempThemClick && !tempMeClick || tempMeClick) {
                                                tempMeClick = true
                                                tempBowls++
                                                if (tempBowls < maxClick && tempAddUpScore < maxScorePerSide) tempAddUpScore++
                                            }
                                        },
                                        onLongPress = {
                                            if (tempAddUpScore > 0) {
                                                tempAddUpScore--
                                                tempBowls = maxOf(0, tempBowls - 1)
                                            }
                                        }
                                    )
                                },
                            color = Color(0xFFB0B0B0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text("$tempAddUpScore", color = Color.White, fontSize = 36.sp)
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.End) {
                        Text("Down", modifier = Modifier.padding(end = 8.dp, bottom = 4.dp).offset(x = (-35).dp, y = 10.dp), color = Color.Yellow, fontSize = 25.sp)
                        Surface(
                            modifier = Modifier
                                .padding(start = 4.dp, end = 8.dp)
                                .offset(x = (-10).dp, y = 0.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            if (!tempThemClick && !tempMeClick || tempThemClick) {
                                                tempThemClick = true
                                                tempBowls++
                                                if (tempBowls < maxClick && tempAddDownScore < maxScorePerSide) tempAddDownScore++
                                            }
                                        },
                                        onLongPress = {
                                            if (tempAddDownScore > 0) {
                                                tempAddDownScore--
                                                tempBowls = maxOf(0, tempBowls - 1)
                                            }
                                        }
                                    )
                                },
                            color = Color(0xFFB0B0B0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text("$tempAddDownScore", color = Color.Yellow, fontSize = 36.sp)
                            }
                        }
                    }
                }
                Text("Adding End $addingEnd", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp))
                val hasChanges = tempAddUpScore > 0 || tempAddDownScore > 0
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            if (hasChanges) {
                                completeAddEnd()
                            } else {
                                addingEnd = null
                                resetCurrentEnd()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (hasChanges) Color.Green else Color.Red, contentColor = if (hasChanges) Color.Black else Color.White),
                        modifier = Modifier.size(width = 60.dp, height = 30.dp)
                    ) { Text(if (hasChanges) "Save" else "Cancel", fontSize = 14.sp) }
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.Start) {
                        Text("Up", modifier = Modifier.padding(start = 8.dp, bottom = 4.dp).offset(x = 35.dp, y = 10.dp), color = Color.White, fontSize = 25.sp)
                        Surface(
                            modifier = Modifier
                                .padding(start = 8.dp, end = 4.dp)
                                .offset(x = 2.dp, y = 0.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            if (!tempThemClick && !tempMeClick || tempMeClick) {
                                                tempMeClick = true
                                                tempBowls++
                                                if (tempBowls < maxClick && tempMyScore < maxScorePerSide) tempMyScore++
                                            }
                                        },
                                        onLongPress = {
                                            if (tempMyScore > 0) {
                                                tempMyScore--
                                                tempBowls = maxOf(0, tempBowls - 1)
                                            }
                                        }
                                    )
                                },
                            color = Color(0xFFB0B0B0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text("$myScore", color = Color.White, fontSize = 36.sp)
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.End) {
                        Text("Down", modifier = Modifier.padding(end = 8.dp, bottom = 4.dp).offset(x = (-35).dp, y = 10.dp), color = Color.Yellow, fontSize = 25.sp)
                        Surface(
                            modifier = Modifier
                                .padding(start = 4.dp, end = 8.dp)
                                .offset(x = (-10).dp, y = 0.dp)
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = {
                                            if (!tempThemClick && !tempMeClick || tempThemClick) {
                                                tempThemClick = true
                                                tempBowls++
                                                if (tempBowls < maxClick && tempTheirScore < maxScorePerSide) tempTheirScore++
                                            }
                                        },
                                        onLongPress = {
                                            if (tempTheirScore > 0) {
                                                tempTheirScore--
                                                tempBowls = maxOf(0, tempBowls - 1)
                                            }
                                        }
                                    )
                                },
                            color = Color(0xFFB0B0B0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                                Text("$theirScore", color = Color.Yellow, fontSize = 36.sp)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            if (isScoringCurrentEnd) {
                                completeEnd()
                            }
                        },
                        enabled = isScoringCurrentEnd && (tempMyScore > 0 || tempTheirScore > 0),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                        modifier = Modifier.size(width = 60.dp, height = 40.dp)
                    ) { Text("Confirm", fontSize = 14.sp) }
                    Button(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            resetCurrentEnd()
                            gameOver = true
                        },
                        enabled = !isScoringCurrentEnd,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.Black),
                        modifier = Modifier.size(width = 60.dp, height = 40.dp)
                    ) { Text("Finish", fontSize = 14.sp) }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            view.playSoundEffect(SoundEffectConstants.CLICK)
                            resetCurrentEnd()
                            showDeadEndDialog = true
                        },
                        enabled = isScoringCurrentEnd,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                        modifier = Modifier.size(width = 40.dp, height = 40.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Dead End", modifier = Modifier.size(20.dp))
                    }
                    Button(
                        onClick = {
                            showHistoryDialog = true
                            Toast.makeText(mContext, "History opened", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue, contentColor = Color.White),
                        modifier = Modifier.size(40.dp).offset(y = 20.dp),
                      //  shape = RoundedCornerShape(50%)
                    ) { Text("H", fontSize = 12.sp, textAlign = TextAlign.Center) }
                }
                Text("End $endCount", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(bottom = 8.dp))
            }
        }
        if (showDeadEndDialog) {
            Dialog(
                onDismissRequest = { showDeadEndDialog = false },
                properties = DialogProperties(dismissOnClickOutside = true)
            ) {
                Surface(
                    modifier = Modifier.background(Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Dead End?", fontSize = 20.sp, color = Color.White, textAlign = TextAlign.Center)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    resetCurrentEnd()
                                    endCount++
                                    Toast.makeText(mContext, "Dead End $endCount", Toast.LENGTH_SHORT).show()
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    showDeadEndDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow, contentColor = Color.Black),
                                modifier = Modifier.size(width = 60.dp, height = 40.dp)
                            ) { Text("Yes", fontSize = 14.sp) }
                            Button(
                                onClick = {
                                    resetCurrentEnd()
                                    Toast.makeText(mContext, "End reset", Toast.LENGTH_SHORT).show()
                                    view.playSoundEffect(SoundEffectConstants.CLICK)
                                    showDeadEndDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                                modifier = Modifier.size(width = 60.dp, height = 40.dp)
                            ) { Text("No", fontSize = 14.sp) }
                        }
                    }
                }
            }
        }
        if (showHistoryDialog) {
            Dialog(
                onDismissRequest = { showHistoryDialog = false },
                properties = DialogProperties(dismissOnClickOutside = true)
            ) {
                Surface(
                    modifier = Modifier.background(Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(8.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("End History", fontSize = 20.sp, color = Color.White, textAlign = TextAlign.Center)
                        ScalingLazyColumn(
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
                            state = rememberScalingLazyListState()
                        ) {
                            items(endHistory.reversed().size) { index ->
                                val (endNum, upScore, downScore) = endHistory.reversed()[index]
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("$endNum.  ", color = Color.White, fontSize = 16.sp)
                                    Text(upScore.toString(), color = Color.White, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                    Text(downScore.toString(), color = Color.Yellow, fontSize = 16.sp, modifier = Modifier.weight(1f))
                                    Button(
                                        onClick = {
                                            startEditing(endNum)
                                            showHistoryDialog = false
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue, contentColor = Color.White),
                                        modifier = Modifier.size(width = 40.dp, height = 30.dp)
                                    ) { Text("Edit", fontSize = 12.sp) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}