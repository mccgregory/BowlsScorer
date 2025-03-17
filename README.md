# BowlsScorer
The 'Crown Green Bowls' Game: So, with Singles a person can have 2 bowls nearer to the jack than another (therefore maximum score is 2).
With Doubles the maximum score can be 4 bowls each END. Each End is won by one side or the other.
It is recorded as UP if YOU score i.e. you are up, or DOWN if your competitor (THEY) win the END i.e. you are down
- This communicated to the Scorers (One HOME one AWAY) for them to record each END.
-  So, for Singles each end score can be: 1-0, 2-0, 0-0, 0-1, 0-2.
-   Doubles, each end score can be: 1-0, 2-0, 3-0, 4-0, 0-0, 0-1, 0-2, 0-3, 0-4.
-    The 0-0 ENDs are called a 'dead end', caused by someone knocking the jack off the green, or by it being impossible to decide which side were nearer to the jack.
The game is started by the AWAY player rolling out a jack (usually smaller bowl with distinctive colour - often yellow) The AWAY player then rolls out one Bowl (or 'wood'). The HOME player now bowls out their 1st wood. The AWAY player rolls his last wood, then followed by the HOME player rolling their last wood. The players then score the END.

The'Crown Green Bowls' App The logic in the App is mainly to reduce the possibility of getting the score wrong.
So, for example, if you start scoring on one side (Up say for your score) you can't then add anything to the other (Down - score for your competitor) or vice-versa.

The App starts with a menu offering Singles or Doubles i.e. Each player has 2 bowls per END, this is to limit the maximum score each END.
There is a Red cross 'X' on the bottom LHS of screen for resetting the latest score in case of you starting to score incorrectly.
There is a Green button with an "END" label next to it which causes the score to be recorded and displays the END count, so starts from 1 (i.e. end 1 is being played).
It is important to note that the END score reflects the END currently being played.
If The green button is clicked without any scores being added it records a dead-end and increments the end count, checks whether that was an intentional click,
and pops up a toast showing "This is a Dead END". 

he actual scoring on the Scoring Screen, UP or Down is achieved by clicking the large Score digits.
The Winning score is 21, when a screen shows the winner and offers the options to play again or end the scoring by Exiting the App.

A proper exit of the program can be made with a 'long-press' of the screen for 3 seconds - this is available most places in the running App - so this will close the App and tidy up.

END Editing: There is a blue unmarked button at the very bottom of the Watch screen which accesses the game History - i.e. you can view the past ENDs.
If you click the blue button on the RHS of each END, you can select that END for editing. 
The editing can be to correct the END score, or to completely replace that END with a forgotten, or missed END - thus correcting ALL the END numbers and the Final score.

**NOTES ON DEVELOPMENT.**
Developed on Android Studio Ladybug Feature Drop | 2024.2.2 Build #AI-242.23726.103.2422.12816248, built on December 18, 2024 Runtime version: 21.0.4+-12508038-b607.1 amd64 VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o. Toolkit: sun.awt.windows.WToolkit

Android studio is running on a Windows 10 x64 PC Java is JDK-17 set in the File>Project Structure>Modules under 'Source Compatibility' and 'Target Compatibility' boxes
The target is a Samsung Watch 5, with Wear OS

FOR COLLABORATING.
==================
The repository URL:
https://github.com/mccgregory/BowlsScorer

**FINAL THINGS I SHOULD ADD**
a. tidy up the Editing Screen - aesthetics backgroung colour.
b. REmove the screen swpping so the Min Scoring SCreen is visible to adjust scores direcly, no through an END Scoring Screen.
c. **Checkout anomaly with disabling swipe-right**
**Swipe-right UPDATE:** 
I tested the code and swiped-right - it asked me to confirm or cancel.
I cancelled, and carried on testing successfully.
Then I cancelled again, this time it exited straightaway - no safety net!
I ran the App again, right-swiped after entering some scores - EXITED IMMEDIATELY! Uggh!

1. Creation of a File record probably with a unique time/date filename.
2. Perhaps eventually, a FileHandling App for the Watch to Access, read, correct and print files. Or toclear them out!   
 
