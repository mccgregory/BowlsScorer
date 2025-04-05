package com.example.bowls

import java.util.Locale // Add this import at the top of MainActivity.kt
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
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
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import kotlinx.coroutines.launch
import android.util.Log
import androidx.activity.compose.BackHandler // For Compose back handling
import android.os.Vibrator
import android.os.VibrationEffect
import java.text.SimpleDateFormat
import java.io.File

class MainActivity : ComponentActivity() {
//    private val coroutineScope = CoroutineScope(Dispatchers.Main)
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Enable Do-Not-Disturb
        enableDoNotDisturb()

        // Set aggressive immersive mode
//        window.decorView.systemUiVisibility = (
//                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                        or View.SYSTEM_UI_FLAG_FULLSCREEN
//                )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(androidx.core.view.WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            Surface(modifier = Modifier.fillMaxSize()) {
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
//--------------------- end of onCreate
    override fun onDestroy() {
        super.onDestroy()
        disableDoNotDisturb()
        // Restore system UI on exit
//        window.decorView.systemUiVisibility = 0
    }


    // Wear OS-specific dismissal override
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        // Prevent right-swipe dismissal
        println("User attempted to leave app via gesture—blocked")
    }

    private fun enableDoNotDisturb() {
        try {
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
                println("Do-Not-Disturb enabled")
            } else {
                println("DND permission not granted; skipping (Wear OS limitation)")
            }
        } catch (e: Exception) {
            println("Failed to enable DND: ${e.message}")
        }
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
                text = "Welcome to Bowls Scorer",
                color = Color.White,
                modifier = Modifier.padding(8.dp).offset(y = 20.dp)
            )
            Button(
                modifier = Modifier.padding(vertical = 15.dp).offset(y = (15).dp),
                onClick = { onContinueClicked(); gameSinglesClicked() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)
            ) {
                Text("Singles")
            }
            Button(
                modifier = Modifier.padding(vertical = 10.dp).offset(y = (10).dp),
                onClick = onContinueClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)
            ) {
                Text("Doubles")
            }
            Text(
                text = "Choose game...",
                color = Color.White,
                modifier = Modifier.padding(8.dp).offset(y = 0.dp)
            )
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
    var showExitDialog by remember { mutableStateOf(false) }
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
    var startTime by rememberSaveable { mutableStateOf<Long?>(null) } // Added here
    var hasSavedFile by rememberSaveable { mutableStateOf(false) } // Track if we've saved
    var fileList by remember { mutableStateOf(emptyList<File>()) } // Ensure this is here

    var tempMyScore by remember { mutableStateOf(0) }
    var tempTheirScore by remember { mutableStateOf(0) }
    var tempMeClick by remember { mutableStateOf(false) }
    var tempThemClick by remember { mutableStateOf(false) }
    var tempBowls by remember { mutableStateOf(0) }
    var currentUpScore by remember { mutableStateOf(0) }
    var currentDownScore by remember { mutableStateOf(0) }
    var showGameOverOptions by remember { mutableStateOf(false) } // New state for post-history options
    val endHistory = rememberSaveable(
        saver = Saver(
            save = { history: MutableList<Triple<Int, Int, Int>> -> history.map { triple -> listOf(triple.first, triple.second, triple.third) } },
            restore = { saved: List<List<Int>> -> mutableListOf<Triple<Int, Int, Int>>().apply { saved.forEach { innerList -> add(Triple(innerList[0], innerList[1], innerList[2])) } }.toMutableStateList() }
        )
    ) { mutableStateListOf<Triple<Int, Int, Int>>() }

// Get the context in the composable scope
    val context = LocalContext.current
    val vibrator = context.getSystemService(Vibrator::class.java) as Vibrator
    BackHandler(enabled = true, onBack = {
        Log.d("BowlsScorer", "Swipe-right intercepted")
        // Trigger vibration
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
        coroutineScope.launch { showExitDialog = true }
    })
//=========================
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
    startTime = null
    hasSavedFile = false
    Log.d("BowlsScorer", "resetGame called, startTime reset to $startTime")
}
//===========================
fun saveMatchFile() {
    Log.d("BowlsScorer", "saveMatchFile called")
    val endTime = System.currentTimeMillis()
    val elapsedTime = startTime?.let { endTime - it } ?: 0L
    val fileName = "B${SimpleDateFormat("HHmm-dd-MM-yyyy", Locale.US).format(endTime)}" // Added Locale.US

    val file = File(context.filesDir, "$fileName.txt")
    try {
        // Calculate running totals for Game Scores
        val gameScores = mutableListOf<Pair<Int, Int>>()
        var runningUpScore = 0
        var runningDownScore = 0
        for (end in endHistory) {
            runningUpScore += end.second // Up score for this end
            runningDownScore += end.third // Down score for this end
            gameScores.add(Pair(runningUpScore, runningDownScore))
        }

        // Build the scores section with both End Scores and Game Scores
        val scoresBuilder = StringBuilder()
        scoresBuilder.append("End Scores          Game Scores\n")
        scoresBuilder.append("==========          ===========\n")
        endHistory.forEachIndexed { index, (endNum, upScore, downScore) ->
            val gameScore = gameScores[index]
            // Format: "End 1: 4-0            4-0"
            scoresBuilder.append("End $endNum: $upScore-$downScore".padEnd(20))
            scoresBuilder.append("${gameScore.first}-${gameScore.second}\n")
        }

        // Write the file
        file.writeText(
            "Start Time: ${startTime?.let { SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(it) } ?: "Not recorded"}\n" +
                    scoresBuilder.toString() + " \n" +
                    "End Time: ${SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(endTime)}\n" +
                    "Elapsed Time: ${elapsedTime / 60000} minutes\n"
        )
        Log.d("BowlsScorer", "Saved match to ${file.absolutePath}")
        Toast.makeText(context, "Match saved as $fileName", Toast.LENGTH_SHORT).show()
        fileList = context.filesDir.listFiles()?.filter { it.name.startsWith("B") } ?: emptyList()
    } catch (e: Exception) {
        Log.e("BowlsScorer", "Failed to save match: ${e.message}")
    }
}
//-----------------------------------------
    if (showExitDialog) {
        Dialog(
            onDismissRequest = { showExitDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                color = Color.Black
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Are you REALLY sure you want to lose these precious scores?",
                        color = Color.White,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(
                            onClick = { showExitDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.White),
                            modifier = Modifier.size(width = 60.dp, height = 30.dp)
                        ) {
                            Text("No", fontSize = 14.sp)
                        }
                        Button(
                            onClick = { (context as? ComponentActivity)?.finish() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                            modifier = Modifier.size(width = 60.dp, height = 30.dp)
                        ) {
                            Text("Yes", fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }

    if (gameSingles) {
        maxClick = 4 // 2 bowls per player, 2 players
        maxScorePerSide = 2 // Max score per side per end
    } else {
        maxClick = 8 // 2 bowls per player, 4 players
        maxScorePerSide = 4 // Max score per side per end
    }

    LaunchedEffect(myScore, theirScore) {
        Log.d("BowlsScorer", "LaunchedEffect checking: myScore=$myScore, theirScore=$theirScore")
        if (myScore >= 21 || theirScore >= 21) {
            Log.d("BowlsScorer", "Setting gameOver to true")
            gameOver = true
        }
    }
//----------------------------------------------------
    LaunchedEffect(gameOver) {
        if (gameOver && !hasSavedFile) {
            Log.d("BowlsScorer", "Game over confirmed, saving file")
            saveMatchFile()
            hasSavedFile = true
            showHistoryDialog = true
        }
    }
//----------------------------------------------------


//--------------------------------------------------
    if (gameOver && !showHistoryDialog && !showGameOverOptions) {
        // This block is now redundant but kept for clarity; it won’t show unless dialogs are off
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
        }
    }

    if (showHistoryDialog) {
        Dialog(
            onDismissRequest = {
                showHistoryDialog = false
                showGameOverOptions = true // Show options after history is dismissed
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                color = Color.Black
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp)
                ) {
                    Text(
                        "End History",
                        color = Color(0xFFD3D3D3),
                        fontSize = 24.sp,
                        modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp)
                    )
                    // ... (rest of history dialog unchanged)
                }
            }
        }
    }
//-----------------------------------------------
    if (showGameOverOptions) {
        Dialog(
            onDismissRequest = { showGameOverOptions = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize().background(Color.Black),
                color = Color.Black
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
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
                                hasSavedFile = false // Reset for next game
                                showGameOverOptions = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black)
                        ) { Text("New Game") }
                        Button(
                            onClick = { mContext.finish() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.Black)
                        ) { Text("Exit") }
                    }
                }
            }
        }
    }
//-------------------------------------------------

//============  fun resetGame last home    =======================================
//
//    fun resetGame() {
//        myScore = 0
//        theirScore = 0
//        strtMyScore = 0
//        strtTheirScore = 0
//        endCount = 1
//        meClick = false
//        themClick = false
//        bowls = 0
//        gameOver = false
//        isScoringCurrentEnd = false
//        endHistory.clear()
//        currentUpScore = 0
//        currentDownScore = 0
//        startTime = null // Reset for new game
//    }
//===================================================
    fun resetCurrentEnd() {
        meClick = false
        themClick = false
        bowls = 0
        currentUpScore = 0
        currentDownScore = 0
        isScoringCurrentEnd = false
        println("Reset current end: meClick=$meClick, themClick=$themClick, bowls=$bowls, currentUpScore=$currentUpScore, currentDownScore=$currentDownScore, isScoringCurrentEnd=$isScoringCurrentEnd")
    }

    fun completeEnd() {
        println("Completing end $endCount: currentUpScore=$currentUpScore, currentDownScore=$currentDownScore")
        if (startTime == null) {
            startTime = System.currentTimeMillis()
            Log.d("BowlsScorer", "Set startTime to $startTime (End $endCount)")
        } else {
            Log.d("BowlsScorer", "startTime already set: $startTime (End $endCount)")
        }
        val upScore = currentUpScore
        val downScore = currentDownScore
        endHistory.add(Triple(endCount, upScore, downScore))
        endCount++
        resetCurrentEnd()
        myScore = endHistory.sumOf { it.second }
        theirScore = endHistory.sumOf { it.third }
        strtMyScore = myScore
        strtTheirScore = theirScore
        println("After completeEnd: myScore=$myScore, theirScore=$theirScore, endHistory=${endHistory.map { "(${it.first}, ${it.second}, ${it.third})" }}")
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
            Toast.makeText(mContext, "End $editingEnd updated to $tempMyScore-$tempTheirScore", Toast.LENGTH_SHORT).show()
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
            println("After adding and renumbering End $addingEnd: ${endHistory.map { "(${it.first}, ${it.second}, ${it.third})" }}")
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
        modifier = modifier.pointerInput(Unit) { detectTapGestures(onLongPress = { coroutineScope.launch { showExitDialog = true } }) },
        color = if (editingEnd != null || addingEnd != null) Color(0xFFB0B0B0) else if (isScoringCurrentEnd) Color(0xFF1E90FF) else Color.Black    ) {          // Setting background colour of EDIT Screens
 //---------------------------------------------
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
                            .pointerInput(Unit) { detectTapGestures(
                                onTap = { if (!tempThemClick && !tempMeClick || tempMeClick) { tempMeClick = true; tempBowls++; if (tempBowls < maxClick && tempMyScore < maxScorePerSide) tempMyScore++ } },
                                onLongPress = { if (tempMyScore > 0) { tempMyScore--; tempBowls = maxOf(0, tempBowls - 1); if (tempMyScore == 0) tempMeClick = false } }
                            ) }, color = Color(0xFFB0B0B0), shape = RoundedCornerShape(8.dp)) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) { Text("$tempMyScore", color = Color.White, fontSize = 50.sp) }
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically), horizontalAlignment = Alignment.End) {
                        Text("Down", modifier = Modifier.padding(end = 8.dp, bottom = 4.dp).offset(x = (-35).dp, y = 10.dp), color = Color.Yellow, fontSize = 25.sp)
                        Surface(modifier = Modifier
                            .padding(start = 4.dp, end = 8.dp)
                            .offset(x = (-10).dp, y = 0.dp)
                            .pointerInput(Unit) { detectTapGestures(
                                onTap = { if (!tempThemClick && !tempMeClick || tempThemClick) { tempThemClick = true; tempBowls++; if (tempBowls < maxClick && tempTheirScore < maxScorePerSide) tempTheirScore++ } },
                                onLongPress = { if (tempTheirScore > 0) { tempTheirScore--; tempBowls = maxOf(0, tempBowls - 1); if (tempTheirScore == 0) tempThemClick = false } }
                            ) }, color = Color(0xFFB0B0B0), shape = RoundedCornerShape(8.dp)) {
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
                        onClick = {
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue, contentColor = Color.White),
                        modifier = Modifier.size(width = 60.dp, height = 30.dp)
                    ) { Text("ADD", fontSize = 14.sp) }
                }
                Text(
                    "Editing End $editingEnd",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 8.dp).offset(y = (-4).dp)
                )
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
                                            if (tempAddUpScore == 0) tempMeClick = false
                                        }
                                    }
                                )
                            },
                            color = Color(0xFFB0B0B0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("$tempAddUpScore", color = Color.White, fontSize = 50.sp)
                            }
                        }
                    }
                    Column(verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.End) {
                        Text("Down", modifier = Modifier.padding(end = 8.dp, bottom = 4.dp).offset(x = (-35).dp, y = 10.dp), color = Color.Yellow, fontSize = 25.sp)
                        Surface(modifier = Modifier
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
                                            if (tempAddDownScore == 0) tempThemClick = false
                                        }
                                    }
                                )
                            },
                            color = Color(0xFFB0B0B0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(modifier = Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                                Text("$tempAddDownScore", color = Color.Yellow, fontSize = 50.sp)
                            }
                        }
                    }
                }
                Text(
                    "Adding End $addingEnd",
                    color = Color.White,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text("Up", modifier = Modifier.padding(start = 8.dp, bottom = 4.dp).offset(x = 35.dp, y = 0.dp), color = Color.White, fontSize = 25.sp)
                    Surface(
                        modifier = Modifier
                            .padding(start = 8.dp, end = 4.dp)
                            .offset(x = 2.dp, y = 0.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        println("Pre-Tap Up: meClick=$meClick, themClick=$themClick, bowls=$bowls, addingEnd=$addingEnd, currentUpScore=$currentUpScore, tempAddUpScore=$tempAddUpScore")
                                        if (!gameOver && addingEnd == null && (!themClick && !meClick || meClick)) {
                                            isScoringCurrentEnd = true
                                            meClick = true
                                            bowls++
                                            if (bowls <= maxClick && currentUpScore < maxScorePerSide) currentUpScore++
                                        }
                                        println("Post-Tap Up: meClick=$meClick, themClick=$themClick, bowls=$bowls, addingEnd=$addingEnd, currentUpScore=$currentUpScore, tempAddUpScore=$tempAddUpScore, isScoringCurrentEnd=$isScoringCurrentEnd")
                                    },
                                    onLongPress = {
                                        if (currentUpScore > 0) {
                                            currentUpScore--
                                            bowls = maxOf(0, bowls - 1)
                                            meClick = bowls > 0
                                            if (bowls == 0) isScoringCurrentEnd = false
                                        }
                                        println("Post-LongPress Up: meClick=$meClick, themClick=$themClick, bowls=$bowls, currentUpScore=$currentUpScore, isScoringCurrentEnd=$isScoringCurrentEnd")
                                    }
                                )
                            },
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.padding(8.dp),
                            contentAlignment = Alignment.Center) {
                            Text(
                                if (isScoringCurrentEnd) "$currentUpScore"
                                else "$myScore",
                                color = Color.White,
                                fontSize = 60.sp
                            )
                        }
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    Text("Down", modifier = Modifier.padding(end = 8.dp, bottom = 4.dp).offset(x = (-35).dp, y = 0.dp), color = Color.Yellow, fontSize = 25.sp)
                    Surface(
                        modifier = Modifier
                            .padding(start = 4.dp, end = 8.dp)
                            .offset(x = (-10).dp, y = 0.dp)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onTap = {
                                        println("Pre-Tap Down: meClick=$meClick, themClick=$themClick, bowls=$bowls, addingEnd=$addingEnd, currentDownScore=$currentDownScore, tempAddDownScore=$tempAddDownScore")
                                        if (!gameOver && addingEnd == null && (!themClick && !meClick || themClick)) {
                                            isScoringCurrentEnd = true
                                            themClick = true
                                            bowls++
                                            if (bowls <= maxClick && currentDownScore < maxScorePerSide) currentDownScore++
                                        }
                                        println("Post-Tap Down: meClick=$meClick, themClick=$themClick, bowls=$bowls, addingEnd=$addingEnd, currentDownScore=$currentDownScore, tempAddDownScore=$tempAddDownScore, isScoringCurrentEnd=$isScoringCurrentEnd")
                                    },
                                    onLongPress = {
                                        if (currentDownScore > 0) {
                                            currentDownScore--
                                            bowls = maxOf(0, bowls - 1)
                                            themClick = bowls > 0
                                            if (bowls == 0) isScoringCurrentEnd = false
                                        }
                                        println("Post-LongPress Down: meClick=$meClick, themClick=$themClick, bowls=$bowls, currentDownScore=$currentDownScore, isScoringCurrentEnd=$isScoringCurrentEnd")
                                    }
                                )
                            },
                        color = Color.Black,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(modifier = Modifier.padding(8.dp),
                            contentAlignment = Alignment.Center) {
                            Text(
                                if (isScoringCurrentEnd)
                                    "$currentDownScore"
                                else "$theirScore",
                                color = Color.Yellow,
                                fontSize = 60.sp
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Bottom, horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { resetCurrentEnd() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Filled.Clear, "Clear", modifier = Modifier.size(40.dp), tint = Color.Red)
                    }
                    Row {
                        Text(
                            text = "END",
                           color = Color.Green,
                            fontSize = 25.sp,
                            modifier = Modifier.padding(end = 8.dp,
                            top = 4.dp).offset(x = 45.dp)
                        )
                        Button(
                            onClick = { if (!gameOver) { if (!meClick && !themClick) showDeadEndDialog = true else completeEnd() } },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green, contentColor = Color.Black),
                            modifier = Modifier.height(40.dp).width(80.dp).offset(x = 45.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "$endCount",
                                    fontSize = 26.sp,
                                    modifier = Modifier.offset(x = -15.dp, y = -4.dp)
                                )
                            }
                        }
                    }
                }
                Button(
                    onClick = {
                        showHistoryDialog = true
                        Toast.makeText(mContext, "History opened", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Blue, contentColor = Color.White),
                    modifier = Modifier.size(40.dp).offset(y = 20.dp),
                    shape = CircleShape
                ) { Text("H", fontSize = 12.sp, textAlign = TextAlign.Center) }
            }
        }

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
//--------------------------------------------------------
        if (showHistoryDialog) {
            Dialog(
                onDismissRequest = {
                    showHistoryDialog = false
                    if (gameOver) { // Only save file at game over
                        val endTime = System.currentTimeMillis()
                        val elapsedTime = startTime?.let { endTime - it } ?: 0L
                        val fileName = "B${SimpleDateFormat("HHmm-dd-MM-yyyy", Locale.US).format(endTime)}"
                        val file = File(context.filesDir, "$fileName.txt")
                        file.writeText(
                            "Start Time: ${startTime?.let { SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(it) } ?: "Not recorded"}\n" +
                                    "End Scores: ${endHistory.joinToString { "${it.first}: ${it.second}-${it.third}" }}\n" +
                                    "End Time: ${SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.US).format(endTime)}\n" +
                                    "Elapsed Time: ${elapsedTime / 60000} minutes"
                        )
                        Toast.makeText(context, "Match saved as $fileName", Toast.LENGTH_SHORT).show()
                    }
                    showGameOverOptions = true
                },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize().background(Color.Black),
                    color = Color.Black
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(8.dp), // Reduced padding for more space
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "End History",
                            color = Color(0xFFD3D3D3),
                            fontSize = 20.sp, // Slightly smaller for Wear OS
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                        if (endHistory.isEmpty()) {
                            Text(
                                "No ends yet",
                                color = Color(0xFFD3D3D3),
                                fontSize = 18.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            val listState = rememberScalingLazyListState()
                            ScalingLazyColumn(
                                modifier = Modifier.fillMaxWidth().weight(1f).padding(vertical = 4.dp),
                                state = listState,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(endHistory.reversed().size) { index ->
                                    val (endNum, upScore, downScore) = endHistory.reversed()[index]
                                    Row(
//                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
//                                        horizontalArrangement = Arrangement.SpaceBetween,
//                                        verticalAlignment = Alignment.CenterVertically
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 4.dp, vertical = 4.dp) // Added vertical padding
                                            .heightIn(min = 32.dp), // Ensure minimum height for easier tapping
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically

                                    ) {
                                        Text("$endNum.  ", color = Color(0xFF1E90FF), fontSize = 26.sp, modifier = Modifier.weight(1f))
                                        Text(upScore.toString(), color = Color(0xFFD3D3D3), fontSize = 26.sp, modifier = Modifier.weight(1f))
                                        Text(downScore.toString(), color = Color(0xFFFFFF00), fontSize = 26.sp, modifier = Modifier.weight(1f))
                                        Button(
                                            onClick = { startEditing(endNum); showHistoryDialog = false },
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E90FF), contentColor = Color.White),
                                            modifier = Modifier.size(width = 48.dp, height = 24.dp)
                                        ) { Text("Edit", fontSize = 12.sp) }
                                    }
                                }
                            }
                        }
                        // Show Saved Matches only after game over
                        if (gameOver) {
//                            Text(
//                                "Saved Matches",
//                                color = Color(0xFFD3D3D3),
//                                fontSize = 18.sp,
//                                modifier = Modifier.padding(top = 8.dp)
//                            )
                            val files = context.filesDir.listFiles()?.filter { it.name.startsWith("B") } ?: emptyList()
                            if (files.isEmpty()) {
                                Text(
                                    "No saved matches",
                                    color = Color(0xFFD3D3D3),
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
//                            else {
//                                ScalingLazyColumn(
//                                    modifier = Modifier.fillMaxWidth().heightIn(max = 80.dp).padding(vertical = 4.dp), // Fixed max height
//                                    verticalArrangement = Arrangement.spacedBy(4.dp)
//                                ) {
//                                    items(files.size) { index ->
//                                        Text(
//                                            files[index].name,
//                                            color = Color(0xFFD3D3D3),
//                                            fontSize = 14.sp,
//                                            modifier = Modifier.padding(horizontal = 4.dp)
//                                        )
//                                    }
//                                }
//                            }
                        }
                        Button(
                            onClick = { showHistoryDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                            modifier = Modifier.padding(top = 8.dp).size(width = 80.dp, height = 24.dp) // Smaller button
                        ) { Text("Close", fontSize = 12.sp) }
                    }
                }
            }
        }
//--------------------------------------
    }
}                       // End of Scorer Composable

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() { OnboardingScreen(gameSinglesClicked = {}, onContinueClicked = {}) }

@Preview(showBackground = true)
@Composable
fun ScorerPreview() { Scorer(gameSingles = true, onNewGame = {}) }

private fun mToast(context: Context) { Toast.makeText(context, "This is a DEAD END", Toast.LENGTH_LONG).show() }

private fun Context.finish() { (this as? ComponentActivity)?.finish() }