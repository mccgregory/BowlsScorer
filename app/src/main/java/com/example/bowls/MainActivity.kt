// This is the Bowls2 MainActivity.kt - This is the latest code
package com.example.bowls

import android.content.Context
import android.os.Bundle
import android.view.SoundEffectConstants
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) { MyApp(modifier = Modifier.fillMaxSize()) }
        }
    }
}

@Composable
fun MyApp(modifier: Modifier = Modifier) {
    var shouldShowOnboarding by rememberSaveable() { mutableStateOf(true) }
    var gameSingles by rememberSaveable { mutableStateOf(false) }
    Surface(modifier) {
            if (shouldShowOnboarding) {
                OnboardingScreen(gameSinglesClicked = { gameSingles = true } , onContinueClicked = {
                    shouldShowOnboarding = false })
        } else {
                Scorer(gameSingles)
        }
    }
}

@Composable
fun OnboardingScreen( gameSinglesClicked: () -> Unit, onContinueClicked: () -> Unit, modifier: Modifier = Modifier)
{
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.SpaceAround,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Bowls Scorer", modifier = Modifier
            .padding(8.dp)
            .offset(y = 10.dp))
        Text("Choose game...", modifier = Modifier
            .padding(8.dp)
            .offset(y = 0.dp))
        Button(
            modifier = Modifier
                .padding(vertical = 15.dp)
                .offset(y = -20.dp),
            onClick =  { onContinueClicked()
                         gameSinglesClicked()  }
        ) { Text("Singles") }
        Button( modifier = Modifier
            .padding(vertical = 10.dp)
            .offset(y = -30.dp),
            onClick = onContinueClicked
        ) { Text("Doubles") }
    }
}

@Composable
fun Scorer( gameSingles: Boolean, modifier: Modifier = Modifier) {
    val mContext = LocalContext.current     // For Toast
    val view = LocalView.current            // For CLICK sound

    var myScore = rememberSaveable() { mutableStateOf(0) }
    var theirScore = rememberSaveable() { mutableStateOf(0) }
    var strtMyScore = rememberSaveable() { mutableStateOf(0) }
    var strtTheirScore = rememberSaveable() { mutableStateOf(0) }
    var endCount = rememberSaveable() { mutableStateOf(1) }
    var meClick = rememberSaveable() { mutableStateOf(false) }
    var themClick = rememberSaveable() { mutableStateOf(false) }
    var maxClick = rememberSaveable() { mutableStateOf(0) }
    var bowls = rememberSaveable() { mutableStateOf(0) }

// Choose Game
    if (gameSingles) { maxClick.value = 2 + 1 } //Max per end is 2
    else { maxClick.value = 4 + 1 }             //Max per end is 4

    // A surface container
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black    ) {
        //==================================================================
        Column(
            verticalArrangement = Arrangement.spacedBy(
                space = 2.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.Start
        ) {
            Text("Up" , modifier = Modifier
                .padding(8.dp)
                .offset(x = 35.dp, y = -30.dp),color = Color.White,
                fontSize = 25.sp
            )

            Button(onClick = {      // Add to myScore
                if (!themClick.value && !meClick.value || meClick.value) {
                    meClick.value = true
                    bowls.value++
                    if (bowls.value < maxClick.value) {
                        myScore.value++
                    }
                }
            },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White), modifier = Modifier
                    .padding(8.dp)
                    .offset(x = 2.dp, y = (-50).dp)
            )
            { Text("${myScore.value}", fontSize = 50.sp) }

        }    // End of First Column
        //==================================================================
        Column(
            verticalArrangement = Arrangement.spacedBy(
                space = 2.dp,
                alignment = Alignment.CenterVertically
            ),
            horizontalAlignment = Alignment.End
            // horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Down" , modifier = Modifier
                .padding(8.dp)
                .offset(x = -35.dp, y = -30.dp),color = Color.Yellow,
                fontSize = 25.sp)

            Button(onClick = {
                if (!themClick.value && !meClick.value || themClick.value) {
                    themClick.value = true
                    bowls.value++
                    if (bowls.value < maxClick.value) {
                        theirScore.value++
                    }
                }
            } ,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.Yellow), modifier = Modifier
                    .padding(8.dp)
                    .offset(x = -10.dp, y = -50.dp)
            ) {
                Text("${theirScore.value}", fontSize = 50.sp)
            }
        }       // End of Second Column
        //==================================================================
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.padding(24.dp)
        )
        {
            IconButton(         //Clear mistakes in scoring
                onClick = {
                    meClick.value = false
                    themClick.value = false
                    bowls.value = 0
                    myScore.value = strtMyScore.value
                    theirScore.value = strtTheirScore.value
                }, modifier = Modifier
                    .size(40.dp)
                    .offset(x = -10.dp, y = -10.dp)  // Position modifiers HERE
            ) {
                Icon(
                    imageVector = Icons.Filled.Clear,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),        // Don't modify position here!!
                    tint = Color.Red
                )
            }
            Text("END" , modifier = Modifier.offset(x = 22.dp, y = 0.dp),color = Color.Green,
                fontSize = 25.sp
            )
            Button( onClick = {      // Add to Bowls and moves to NEXT End
//TODO TEST FOR A 'DEAD-END'
                if (!meClick.value && !themClick.value) {
                    mToast(mContext)
                    view.playSoundEffect(SoundEffectConstants.CLICK)
                }
                endCount.value++
                meClick.value = false
                themClick.value = false
                bowls.value = 0
                strtMyScore.value = myScore.value
                strtTheirScore.value = theirScore.value
            },
                colors = ButtonDefaults.buttonColors( containerColor = Color.Green,
                    contentColor = Color.Black)
                , modifier = Modifier.offset(x = 20.dp, y = 0.dp)
            )
            { Text("${endCount.value}", fontSize = 20.sp) }
        }    //End of  3rd ROW
    }           // Surface {
}               // fun Scorer {

@Preview(showBackground = true, widthDp = 320, heightDp = 320)
@Composable
fun OnboardingPreview() {
    OnboardingScreen(gameSinglesClicked ={}, onContinueClicked = {})
}

@Preview(showBackground = true)
@Composable
fun ScorerPreview() {
        Scorer(gameSingles = true )
}

// Function to generate a Toast
private fun mToast(context: Context){
    Toast.makeText(context, "This is a DEAD END", Toast.LENGTH_LONG).show()
}
