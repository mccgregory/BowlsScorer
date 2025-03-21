package com.example.bowls

import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.view.KeyEvent
import android.view.SoundEffectConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import androidx.activity.OnBackPressedCallback
//Curved clock imports
//import androidx.wear.compose.foundation.CurvedRow
//import androidx.wear.compose.foundation.CurvedText
//import java.time.LocalTime
//import java.time.format.DateTimeFormatter
//import kotlinx.coroutines.delay
//Rectangular clock imports
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private lateinit var notificationManager: NotificationManager
    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private var showExitDialog by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        Log.d("BowlsScorer", "App launched - LOGCAT TEST")
        Toast.makeText(this, "App Started!", Toast.LENGTH_SHORT).show()
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d("BowlsScorer", "Back pressed - dispatcher")
                Toast.makeText(this@MainActivity, "Back Blocked!", Toast.LENGTH_SHORT).show()
                coroutineScope.launch { showExitDialog = true }
            }
        })

        // Enable Do-Not-Disturb

        // Set immersive mode with WindowInsetsControllerCompat
//        WindowCompat.setDecorFitsSystemWindows(window, false)
//        val controller = WindowInsetsControllerCompat(window, window.decorView)
//        controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
//        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
                if (showExitDialog) {
                    AlertDialog(
                        onDismissRequest = { showExitDialog = false },
                        title = { Text("Exit App") },
                        text = { Text("Are you sure you want to exit?") },
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
                        onNewGame = {
                            shouldShowOnboarding = true
                            gameSingles = false
                        }
                    )
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        Log.d("BowlsScorer", "Key down - code: $keyCode, action: ${event?.action}")
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.d("BowlsScorer", "Swipe-right intercepted - KeyEvent caught")
            Toast.makeText(this, "Swipe Caught!", Toast.LENGTH_SHORT).show()
            coroutineScope.launch { showExitDialog = true }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        super.onDestroy()
        disableDoNotDisturb()
    }

    override fun onUserLeaveHint() {
        println("User attempted to leave app via gesture—blocked")
        super.onUserLeaveHint()
    }


    private fun disableDoNotDisturb() {
        try {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
                println("Do-Not-Disturb disabled")
            }
        } catch (e: Exception) {
            println("Failed to disable DND: ${e.message}")
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
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { coroutineScope.launch { showExitDialog = true } }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Welcome to Bowls Scorer", color = Color.White, modifier = Modifier.padding(8.dp).offset(y = 20.dp))
            Button(
                modifier = Modifier.padding(vertical = 15.dp).offset(y = 15.dp),
                onClick = { onContinueClicked(); gameSinglesClicked() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)
            ) { Text("Singles") }
            Button(
                modifier = Modifier.padding(vertical = 10.dp).offset(y = 10.dp),
                onClick = onContinueClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)
            ) { Text("Doubles") }
            Text(text = "Choose game...", color = Color.White, modifier = Modifier.padding(8.dp).offset(y = 0.dp))
        }

        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text("Exit App") },
                text = { Text("Are you sure you want to exit?") },
                confirmButton = { Button(onClick = { showExitDialog = false; context.finish() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.Black)) { Text("Confirm") } },
                dismissButton = { Button(onClick = { showExitDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)) { Text("Cancel") } },
                containerColor = Color.Black, titleContentColor = Color.White, textContentColor = Color.White
            )
        }
    }
}

@Composable
fun Scorer(gameSingles: Boolean, onNewGame: () -> Unit, modifier: Modifier = Modifier) {
    val mContext = LocalContext.current
    val view = LocalView.current
    var showDeadEndDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var editingEnd by remember { mutableStateOf<Int?>(null) }
    var addingEnd by remember { mutableStateOf<Int?>(null) }
    var tempAddUpScore by remember { mutableStateOf(0) }
    var tempAddDownScore by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    var myScore by rememberSaveable { mutableStateOf(0) }
    var theirScore by rememberSaveable { mutableStateOf(0) }
    var strtMyScore by rememberSaveable { mutableStateOf(0) }
    var strtTheirScore by rememberSaveable { mutableStateOf(0) }
    var endCount by rememberSaveable { mutableStateOf(1) }
    var meClick by rememberSaveable { mutableStateOf(false) }
    var themClick by rememberSaveable { mutableStateOf(false) }
    var maxClick by rememberSaveable { mutableStateOf(0) }
    var maxScorePerSide by rememberSaveable { mutableStateOf(0) }
    var bowls by rememberSaveable { mutableStateOf(0) }
    var gameOver by rememberSaveable { mutableStateOf(false) }
    var isScoringCurrentEnd by rememberSaveable { mutableStateOf(false) }
    var tempMyScore by remember { mutableStateOf(0) }
    var tempTheirScore by remember { mutableStateOf(0) }
    var tempMeClick by remember { mutableStateOf(false) }
    var tempThemClick by remember { mutableStateOf(false) }
    var tempBowls by remember { mutableStateOf(0) }
    var currentUpScore by remember { mutableStateOf(0) }
    var currentDownScore by remember { mutableStateOf(0) }

    val endHistory = rememberSaveable(
        saver = Saver(
            save = { history: MutableList<Triple<Int, Int, Int>> -> history.map { triple -> listOf(triple.first, triple.second, triple.third) } },
            restore = { saved: List<List<Int>> -> mutableListOf<Triple<Int, Int, Int>>().apply { saved.forEach { innerList -> add(Triple(innerList[0], innerList[1], innerList[2])) } }.toMutableStateList() }
        )
    ) { mutableStateListOf<Triple<Int, Int, Int>>() }

    if (gameSingles) {
        maxClick = 4
        maxScorePerSide = 2
    } else {
        maxClick = 8
        maxScorePerSide = 4
    }
// Drop clock in here?
//    var currentTime by remember { mutableStateOf(LocalTime.now()) }
//    LaunchedEffect(Unit) {
//        while (true) {
//            currentTime = LocalTime.now()
//            delay(1000)
//        }
//    }
//    Column(
//        modifier = Modifier.fillMaxWidth(),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        CurvedRow {
//            CurvedText(
//                text = currentTime.format(DateTimeFormatter.ofPattern("HH:mm")),
//                color = Color.White,
//                modifier = Modifier.background(Color.Black)
//            )
//        }
//    }
// Drop clock in here?

    LaunchedEffect(myScore, theirScore) {
        if (myScore >= 21 || theirScore >= 21) {
            gameOver = true
            println("Game Over: myScore=$myScore, theirScore=$theirScore")
        }
    }

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
        isScoringCurrentEnd = false
        endHistory.clear()
        currentUpScore = 0
        currentDownScore = 0
    }

    fun resetCurrentEnd() {
        meClick = false
        themClick = false
        bowls = 0
        currentUpScore = 0
        currentDownScore = 0
        isScoringCurrentEnd = false
        println("Reset current end: meClick=$meClick, themClick=$themClick, bowls=$bowls")
    }

    fun completeEnd() {
        println("Completing end $endCount: currentUpScore=$currentUpScore, currentDownScore=$currentDownScore")
        val upScore = currentUpScore
        val downScore = currentDownScore
        endHistory.add(Triple(endCount, upScore, downScore))
        endCount++
        resetCurrentEnd()
        myScore = endHistory.sumOf { it.second }
        theirScore = endHistory.sumOf { it.third }
        strtMyScore = myScore
        strtTheirScore = theirScore
        println("After completeEnd: myScore=$myScore, theirScore=$theirScore")
        view.playSoundEffect(SoundEffectConstants.CLICK)
    }

    fun completeEditEnd() {
        val index = endHistory.indexOfFirst { it.first == editingEnd }
        if (index != -1) {
            endHistory[index] = Triple(editingEnd!!, tempMyScore, tempTheirScore)
            myScore = endHistory.sumOf { it.second }
            theirScore = endHistory.sumOf { it.third }
            strtMyScore = myScore
            strtTheirScore = theirScore
            Toast.makeText(mContext, "End $editingEnd updated", Toast.LENGTH_SHORT).show()
        }
        editingEnd = null
        resetCurrentEnd()
        view.playSoundEffect(SoundEffectConstants.CLICK)
    }

    fun completeAddEnd() {
        if (addingEnd != null) {
            println("Before adding End $addingEnd: ${endHistory.map { "(${it.first}, ${it.second}, ${it.third})" }}")
            val updatedHistory = mutableListOf<Triple<Int, Int, Int>>()
            val newEnd = Triple(addingEnd!!, tempAddUpScore, tempAddDownScore)
            endHistory.filter { it.first < addingEnd!! }.forEach { updatedHistory.add(it) }
            updatedHistory.add(newEnd)
            endHistory.filter { it.first >= addingEnd!! }.forEach { updatedHistory.add(Triple(it.first + 1, it.second, it.third)) }
            endHistory.clear()
            endHistory.addAll(updatedHistory)
            endHistory.sortBy { it.first }
            endCount = endHistory.size + 1
            println("After adding End $addingEnd: ${endHistory.map { "(${it.first}, ${it.second}, ${it.third})" }}")
            myScore = endHistory.sumOf { it.second }
            theirScore = endHistory.sumOf { it.third }
            strtMyScore = myScore
            strtTheirScore = theirScore
            Toast.makeText(mContext, "End $addingEnd added", Toast.LENGTH_SHORT).show()
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
            .pointerInput(Unit) { detectTapGestures(/* ... */) },
        color = if (isScoringCurrentEnd) Color(0xFF8B0000) else Color.Black // Dark red
    ) {
        if (gameOver) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = if (myScore >= 21) "Up Wins!" else "Down Wins!", color = if (myScore >= 21) Color.White else Color.Yellow, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(8.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.SpaceAround, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = { resetGame(); onNewGame() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)) { Text("New Game") }
                    Button(onClick = { mContext.finish() }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.Black)) { Text("Exit") }
                }
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
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically), horizontalAlignment = Alignment.Start) {
                        Text("Up", modifier = Modifier.padding(start = 8.dp, bottom = 4.dp).offset(x = 35.dp, y = 10.dp), color = Color.White, fontSize = 25.sp)
                        Surface(modifier = Modifier
                            .padding(start = 8.dp, end = 4.dp)
                            .offset(x = 2.dp, y = 0.dp)
                            .pointerInput(Unit) { detectTapGestures(onTap = { if (!tempThemClick && !tempMeClick || tempMeClick) { tempMeClick = true; tempBowls++; if (tempBowls < maxClick && tempMyScore < maxScorePerSide) tempMyScore++ } }, onLongPress = { if (tempMyScore > 0) { tempMyScore--; tempBowls = maxOf(0, tempBowls - 1) } }) }, color = Color(0xFFB0B0B0), shape = RoundedCornerShape(8.dp)) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text("$tempMyScore", color = Color.White, fontSize = 50.sp) }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically), horizontalAlignment = Alignment.End) {
                        Text("Down", modifier = Modifier.padding(end = 8.dp, bottom = 4.dp).offset(x = (-35).dp, y = 10.dp), color = Color.Yellow, fontSize = 25.sp)
                        Surface(modifier = Modifier
                            .padding(start = 4.dp, end = 8.dp)
                            .offset(x = (-10).dp, y = 0.dp)
                            .pointerInput(Unit) { detectTapGestures(onTap = { if (!tempThemClick && !tempMeClick || tempThemClick) { tempThemClick = true; tempBowls++; if (tempBowls < maxClick && tempTheirScore < maxScorePerSide) tempTheirScore++ } }, onLongPress = { if (tempTheirScore > 0) { tempTheirScore--; tempBowls = maxOf(0, tempBowls - 1) } }) }, color = Color(0xFFB0B0B0), shape = RoundedCornerShape(8.dp)) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text("$tempTheirScore", color = Color.Yellow, fontSize = 50.sp) }
                        }
                    }
                }
                val originalEnd = endHistory.firstOrNull { it.first == editingEnd }
                val hasChanges = originalEnd != null && (tempMyScore != originalEnd.second || tempTheirScore != originalEnd.third)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp).offset(y = (-10).dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { if (hasChanges) completeEditEnd() else { editingEnd = null; resetCurrentEnd() } },
                        colors = ButtonDefaults.buttonColors(containerColor = if (hasChanges) Color.Green else Color.Red, contentColor = if (hasChanges) Color.Black else Color.White),
                        modifier = Modifier.size(width = 60.dp, height = 30.dp)
                    ) { Text(if (hasChanges) "Save" else "Cancel", fontSize = 14.sp) }
                    Button(
                        onClick = { replaceEnd(editingEnd!!) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue, contentColor = Color.White),
                        modifier = Modifier.size(width = 60.dp, height = 30.dp)
                    ) { Text("ADD", fontSize = 14.sp) }
                }
                Text("Editing End $editingEnd", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(top = 8.dp).offset(y = (-4).dp))
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
                        Surface(modifier = Modifier
                            .padding(start = 8.dp, end = 4.dp)
                            .offset(x = 2.dp, y = 0.dp)
                            .pointerInput(Unit) { detectTapGestures(onTap = { if (!tempThemClick && !tempMeClick || tempMeClick) { tempMeClick = true; tempBowls++; if (tempBowls < maxClick && tempAddUpScore < maxScorePerSide) tempAddUpScore++ } }, onLongPress = { if (tempAddUpScore > 0) { tempAddUpScore--; tempBowls = maxOf(0, tempBowls - 1) } }) }, color = Color(0xFFB0B0B0), shape = RoundedCornerShape(8.dp)) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text("$tempAddUpScore", color = Color.White, fontSize = 50.sp) }
                        }
                    }
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.End) {
                        Text("Down", modifier = Modifier.padding(end = 8.dp, bottom = 4.dp).offset(x = (-35).dp, y = 10.dp), color = Color.Yellow, fontSize = 25.sp)
                        Surface(modifier = Modifier
                            .padding(start = 4.dp, end = 8.dp)
                            .offset(x = (-10).dp, y = 0.dp)
                            .pointerInput(Unit) { detectTapGestures(onTap = { if (!tempThemClick && !tempMeClick || tempThemClick) { tempThemClick = true; tempBowls++; if (tempBowls < maxClick && tempAddDownScore < maxScorePerSide) tempAddDownScore++ } }, onLongPress = { if (tempAddDownScore > 0) { tempAddDownScore--; tempBowls = maxOf(0, tempBowls - 1) } }) }, color = Color(0xFFB0B0B0), shape = RoundedCornerShape(8.dp)) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text("$tempAddDownScore", color = Color.Yellow, fontSize = 50.sp) }
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
                        onClick = { view.playSoundEffect(SoundEffectConstants.CLICK); if (hasChanges) completeAddEnd() else { addingEnd = null; resetCurrentEnd() } },
                        colors = ButtonDefaults.buttonColors(containerColor = if (hasChanges) Color.Green else Color.Red, contentColor = if (hasChanges) Color.Black else Color.White),
                        modifier = Modifier.size(width = 60.dp, height = 30.dp)
                    ) { Text(if (hasChanges) "Save" else "Cancel", fontSize = 14.sp) }
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp), horizontalAlignment = Alignment.Start) {
                    Text("Up", modifier = Modifier.padding(start = 8.dp, bottom = 4.dp).offset(x = 35.dp, y = 0.dp), color = Color.White, fontSize = 25.sp)
                    Surface(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 4.dp)
                            .offset(x = 2.dp, y = 0.dp)
                            .pointerInput(Unit) { detectTapGestures(onTap = { if (!gameOver && addingEnd == null && (!themClick && !meClick || meClick)) { isScoringCurrentEnd = true; meClick = true; bowls++; if (bowls <= maxClick && currentUpScore < maxScorePerSide) currentUpScore++ } }, onLongPress = { if (currentUpScore > 0) { currentUpScore--; bowls = maxOf(0, bowls - 1); meClick = bowls > 0; if (bowls == 0) isScoringCurrentEnd = false } }) },
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp)
                    ) { Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) { Text(if (isScoringCurrentEnd) "$currentUpScore" else "$myScore", color = Color.White, fontSize = 60.sp) } }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp), horizontalAlignment = Alignment.End) {
                    Text("Down", modifier = Modifier.padding(end = 8.dp, bottom = 4.dp).offset(x = (-35).dp, y = 0.dp), color = Color.Yellow, fontSize = 25.sp)
                    Surface(
                        modifier = Modifier
                            .padding(start = 4.dp, end = 8.dp)
                            .offset(x = (-10).dp, y = 0.dp)
                            .pointerInput(Unit) { detectTapGestures(onTap = { if (!gameOver && addingEnd == null && (!themClick && !meClick || themClick)) { isScoringCurrentEnd = true; themClick = true; bowls++; if (bowls <= maxClick && currentDownScore < maxScorePerSide) currentDownScore++ } }, onLongPress = { if (currentDownScore > 0) { currentDownScore--; bowls = maxOf(0, bowls - 1); themClick = bowls > 0; if (bowls == 0) isScoringCurrentEnd = false } }) },
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp)
                    ) { Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) { Text(if (isScoringCurrentEnd) "$currentDownScore" else "$theirScore", color = Color.Yellow, fontSize = 60.sp) } }
                }
            }
// THIS COLUMN GREG?
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { resetCurrentEnd() }, modifier = Modifier.size(40.dp)) {
                        Icon(Icons.Filled.Clear, "Clear", modifier = Modifier.size(40.dp), tint = Color.Red)
                    }
                    Row {
                        Text(text = "END", color = Color.Green, fontSize = 25.sp, modifier = Modifier.padding(end = 8.dp, top = 4.dp).offset(x = 45.dp))
                        Button(
                            onClick = { if (!gameOver) { if (!meClick && !themClick) showDeadEndDialog = true else completeEnd() } },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                            modifier = Modifier.height(40.dp).width(80.dp).offset(x = 45.dp)
                        ) { Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterStart) { Text(text = "$endCount", fontSize = 26.sp, modifier = Modifier.offset(x = -15.dp, y = -4.dp)) } }
                    }
                }
//  NEW CLOCK
        var currentTime by remember { mutableStateOf(LocalTime.now()) }
        LaunchedEffect(Unit) {
            while (true) {
                currentTime = LocalTime.now()
                delay(1000)
            }
        }
//       Spacer(modifier = Modifier.height(8.dp)) // Add this
                Text(
                    text = "TEST",
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black)
                        .padding(4.dp)
                        .offset(x = 0.dp, y = 60.dp),
                    fontSize = 16.sp
                )
//NEW CLOCK
                Button(
                    onClick = { showHistoryDialog = true; Toast.makeText(mContext, "History opened", Toast.LENGTH_SHORT).show() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue, contentColor = Color.White),
                    modifier = Modifier.size(40.dp).offset(y = 20.dp),
                    shape = CircleShape
                ) { Text("H", fontSize = 12.sp, textAlign = TextAlign.Center) }
            }
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
            Dialog(
                onDismissRequest = { showHistoryDialog = false },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(modifier = Modifier.fillMaxSize().background(Color.Black), color = Color.Black) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text("End History", color = Color(0xFFD3D3D3), fontSize = 24.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp))
                        if (endHistory.isEmpty()) {
                            Text("No ends yet", color = Color(0xFFD3D3D3), fontSize = 22.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp))
                        } else {
                            val listState = rememberScalingLazyListState()
                            ScalingLazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 4.dp),
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(endHistory.reversed().size) { index ->
                                    val (endNum, upScore, downScore) = endHistory.reversed()[index]
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("$endNum.  ", color = Color(0xFF1E90FF), fontSize = 24.sp, modifier = Modifier.weight(1f))
                                        Text(upScore.toString(), color = Color(0xFFD3D3D3), fontSize = 24.sp, modifier = Modifier.weight(1f))
                                        Text(downScore.toString(), color = Color(0xFFFFFF00), fontSize = 24.sp, modifier = Modifier.weight(1f))
                                        Button(
                                            onClick = { startEditing(endNum); showHistoryDialog = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF), contentColor = Color.White),
                                            modifier = Modifier.size(width = 48.dp, height = 24.dp)
                                        ) { Text("Edit", fontSize = 14.sp) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() { OnboardingScreen(gameSinglesClicked = {}, onContinueClicked = {}) }

@Preview(showBackground = true)
@Composable
fun ScorerPreview() { Scorer(gameSingles = true, onNewGame = {}) }

private fun mToast(context: Context) { Toast.makeText(context, "This is a DEAD END", Toast.LENGTH_LONG).show() }

private fun Context.finish() { (this as? ComponentActivity)?.finish() }