Hello,
this is the Login demo!
 
Scenario
========
1) Open the Demo project in NetBeans.
2) Run the project.
3) In the logging window log as user: demo  password: demo
Any other name will be rejected.
4) In the Profile window, you can set the phone/email/subscription.
5) Click on Update to update the model with the typed values. 
You should observe an animated test to warn that profile has been updated.
6) Click on logout to go back to login window.
7) Log again as done in step 3).
8) You will observe an updated profile.
9) Close the Window.

Adding new user
===============
- Edit the class demo.security.Authenticator
- To add Joe with password barteam add USERS.put("Joe", "barteam");
- Then re-run the project

Known issues
============
- Do not resize the windows, you will observe wrong background resizing.
- TextBox font is a bit too small.

Workaround put in place
=======================
- Styleclass has been set on Profile and Login controllers in order to set 
a background on both screen. Style class should be set thanks to the VT.

Difficulties and issues encountered in the VT when making the demo
==================================================================
- Difficult to align properly elements.
- Spacing of dynamic content is difficult to handle.
- Double values. too much digits. Makes very difficult to compare values.
- Project Panel, background is useless. We should get ridoff it.
- FileChooser has no memory. Tedious to select FXML files.
- Inline Editor is not working well.
- No visual feedback when moving elements in StageView (at least on mac)
- Tedious to set colors/Font properties on multiple element. CSS would help there.
- Button disabled should be in Type info panel.
- Need static edit to switch from code <==> UI layout. Otherwise NPE....
- Resizing the background issues.