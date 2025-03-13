# BowlsScorer
Crown Green Bowls scorer app for Galaxy Watch 5

Here is an explanation of the current code:
The App starts with a menu offering Singles or Doubles i.e. Each player has 2 bowls each, this is to limit the maximum score each end.
So, with Singles a person can have 2 bowls nearer to the jack than another (therefore maximum score is 2). With Doubles the maximum can be 4 bowls.
Each End is won by one side or the other.
It is recorded as UP if you score i.e. you are up, or DOWN if yor competitor wins i.e. you are down.
So for Singles each end score can be: 1-0, 2-0, 0-0, 0-1,0-2.
Doubles each end score can be: 1-0,2-0,3-0,4-0,0-0,0-1,0-2,0-3,0-4.
The 0-0 ends are called a 'dead end', caused by someone knocking the jack off the green or by it being impossible to decide which side were nearer to the jack.
The logic in the App is mainly to reduce the possibility of getting the score wrong.
So, for example,  if you start scoring on one side (Up say for your score) you can't tehn add anything to the other (Down - score for your competitor) or vice-versa.
There is a Red cross 'X' on the bottom LHS of screen for resetting the latest score in case of you starting to score incorrectly.
There is a Green button with an "END" label next to it which causes the score to be recorded and display the END count, so starts from 1 (i.e. end 1 is being played).
If The green button is clicked without any scores being added it records a dead-end and increments the end count, checks whether that was an inentional click, and pops up a toast showing "This is a Dead END". 
The actual scoring on the Scoring Screen, UP or Down is achieved by clicking the actual Score digits.
The Winning score is 21, when a screen shows the winner and offers the options to play again or end the scoring by Exiting the App.

A proper exit of the program can be made with a 'long-press' of the screen for 3 seconds - so this will close the App and tidy up.

**NOTES ON DEVELOPMENT.**
Developed on Android Studio Ladybug Feature Drop | 2024.2.2
Build #AI-242.23726.103.2422.12816248, built on December 18, 2024
Runtime version: 21.0.4+-12508038-b607.1 amd64
VM: OpenJDK 64-Bit Server VM by JetBrains s.r.o.
Toolkit: sun.awt.windows.WToolkit

**Android studio is running on a Windows 10 x64 PC**
Java is JDK-17 set in the File>Project Structure>Modules under Source Compatability and Target Compatability boxes 

FOR COLLABORATING.
==================
The repository URL:
https://github.com/mccgregory/BowlsScorer
 
