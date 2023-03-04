/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * VisualMIDletMIDP20.java
 * 
 * Created on Apr 18, 2007, 11:50:43 AM
 * 
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package allComponents;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import javax.microedition.pim.PIM;
import org.netbeans.microedition.lcdui.LoginScreen;
import org.netbeans.microedition.lcdui.pda.PIMBrowser;
import org.netbeans.microedition.lcdui.wma.SMSComposer;
import org.netbeans.microedition.lcdui.SimpleTableModel;
import org.netbeans.microedition.lcdui.SplashScreen;
import org.netbeans.microedition.lcdui.TableItem;
import org.netbeans.microedition.lcdui.WaitScreen;


/**
 * @author Lukas
 */
public class VisualMIDletMIDP20 extends MIDlet implements CommandListener {

    private boolean midletPaused = false;

    private java.util.Hashtable __previousDisplayables = new java.util.Hashtable();//GEN-BEGIN:|fields|0|
    private Command okCommand;
    private SplashScreen splashScreen;
    private List list;
    private Form form;
    private ChoiceGroup choiceGroup;
    private DateField dateField;
    private Gauge gauge;
    private ImageItem imageItem;
    private StringItem stringItem;
    private TextField textField;
    private TableItem tableItem;
    private TextBox textBox;
    private WaitScreen waitScreen;
    private Alert alert;
    private SMSComposer smsComposer;
    private LoginScreen loginScreen;
    private PIMBrowser pimBrowser;
    private Image aaaaaa;
    private SimpleTableModel simpleTableModel;
    private SimpleTableModel simpleTableModel1;//GEN-END:|fields|0|

    public VisualMIDletMIDP20() {
    }

    private void switchToPreviousDisplayable() {//GEN-BEGIN:|methods|0|
        Displayable __currentDisplayable = getDisplay().getCurrent();
        if (__currentDisplayable != null) {
            Displayable __nextDisplayable = (Displayable) __previousDisplayables.get(__currentDisplayable);
            if (__nextDisplayable != null) {
                switchDisplayable(null, __nextDisplayable);
            }
        }
    }//GEN-END:|methods|0|

    private void initialize() {//GEN-LINE:|0-initialize|0|0-preInitialize
        // write pre-initialize user code here
//GEN-LINE:|0-initialize|1|0-postInitialize
        // write post-initialize user code here
    }//GEN-LINE:|0-initialize|2|

    public void resumeMIDlet() {//GEN-LINE:|4-resumeMIDlet|0|4-preAction
        // write pre-action user code here
//GEN-LINE:|4-resumeMIDlet|1|4-postAction
        // write post-action user code here
    }//GEN-LINE:|4-resumeMIDlet|2|

    public void startMIDlet() {//GEN-LINE:|3-startMIDlet|0|3-preAction
        // write pre-action user code here
        switchDisplayable(null, getSplashScreen());//GEN-LINE:|3-startMIDlet|1|3-postAction
        // write post-action user code here
    }//GEN-LINE:|3-startMIDlet|2|

    public void switchDisplayable(Alert alert, Displayable nextDisplayable) {//GEN-LINE:|5-switchDisplayable|0|5-preSwitch
        // write pre-switch user code here
        Display display = getDisplay();//GEN-BEGIN:|5-switchDisplayable|1|5-postSwitch
        Displayable __currentDisplayable = display.getCurrent();
        if (__currentDisplayable != null  &&  nextDisplayable != null) {
            __previousDisplayables.put(nextDisplayable, __currentDisplayable);
        }
        if (alert == null) {
            display.setCurrent(nextDisplayable);
        } else {
            display.setCurrent(alert, nextDisplayable);
        }//GEN-END:|5-switchDisplayable|1|5-postSwitch
        // write post-switch user code here
    }//GEN-LINE:|5-switchDisplayable|2|

    public void method() {//GEN-LINE:|46-switch|0|46-preSwitch
        // enter pre-switch user code here
        switch (0) {//GEN-BEGIN:|46-switch|1|47-preAction
        case 1://GEN-END:|46-switch|1|47-preAction
            // write pre-action user code here
            switchDisplayable(null, getTextBox());//GEN-LINE:|46-switch|2|47-postAction
            // write post-action user code here
            break;//GEN-BEGIN:|46-switch|3|48-preAction
        case 2://GEN-END:|46-switch|3|48-preAction
            // write pre-action user code here
            switchDisplayable(null, getSmsComposer());//GEN-LINE:|46-switch|4|48-postAction
            // write post-action user code here
            break;//GEN-BEGIN:|46-switch|5|46-postSwitch
        }//GEN-END:|46-switch|5|46-postSwitch
        // enter post-switch user code here
    }//GEN-LINE:|46-switch|6|

    public void method1() {//GEN-LINE:|74-entry|0|75-preAction
        // write pre-action user code here
//GEN-LINE:|74-entry|1|75-postAction
        // write post-action user code here
    }//GEN-LINE:|74-entry|2|

    public void method2() {//GEN-LINE:|76-if|0|76-preIf
        // enter pre-if user code here
        if (true) {//GEN-LINE:|76-if|1|77-preAction
            // write pre-action user code here
//GEN-LINE:|76-if|2|77-postAction
            // write post-action user code here
        } else {//GEN-LINE:|76-if|3|78-preAction
            // write pre-action user code here
//GEN-LINE:|76-if|4|78-postAction
            // write post-action user code here
        }//GEN-LINE:|76-if|5|76-postIf
        // enter post-if user code here
    }//GEN-LINE:|76-if|6|

    public void commandAction(Command command, Displayable displayable) {//GEN-LINE:|7-commandAction|0|7-preCommandAction
        // write pre-action user code here
        if (displayable == list) {//GEN-BEGIN:|7-commandAction|1|20-preAction
            if (command == List.SELECT_COMMAND) {//GEN-END:|7-commandAction|1|20-preAction
                // write pre-action user code here
                switchDisplayable(null, getSplashScreen());//GEN-LINE:|7-commandAction|2|20-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|3|69-preAction
        } else if (displayable == loginScreen) {
            if (command == LoginScreen.LOGIN_COMMAND) {//GEN-END:|7-commandAction|3|69-preAction
                // write pre-action user code here
//GEN-LINE:|7-commandAction|4|69-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|5|72-preAction
        } else if (displayable == pimBrowser) {
            if (command == PIMBrowser.SELECT_PIM_ITEM) {//GEN-END:|7-commandAction|5|72-preAction
                // write pre-action user code here
//GEN-LINE:|7-commandAction|6|72-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|7|65-preAction
        } else if (displayable == smsComposer) {
            if (command == SMSComposer.SEND_COMMAND) {//GEN-END:|7-commandAction|7|65-preAction
                // write pre-action user code here
//GEN-LINE:|7-commandAction|8|65-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|9|16-preAction
        } else if (displayable == splashScreen) {
            if (command == SplashScreen.DISMISS_COMMAND) {//GEN-END:|7-commandAction|9|16-preAction
                // write pre-action user code here
                switchDisplayable(null, getList());//GEN-LINE:|7-commandAction|10|16-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|11|53-preAction
        } else if (displayable == textBox) {
            if (command == okCommand) {//GEN-END:|7-commandAction|11|53-preAction
                // write pre-action user code here
                switchDisplayable(null, getWaitScreen());//GEN-LINE:|7-commandAction|12|53-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|13|58-preAction
        } else if (displayable == waitScreen) {
            if (command == WaitScreen.FAILURE_COMMAND) {//GEN-END:|7-commandAction|13|58-preAction
                // write pre-action user code here
                switchDisplayable(getAlert(), getForm());//GEN-LINE:|7-commandAction|14|58-postAction
                // write post-action user code here
            } else if (command == WaitScreen.SUCCESS_COMMAND) {//GEN-LINE:|7-commandAction|15|57-preAction
                // write pre-action user code here
                switchDisplayable(null, getList());//GEN-LINE:|7-commandAction|16|57-postAction
                // write post-action user code here
            }//GEN-BEGIN:|7-commandAction|17|7-postCommandAction
        }//GEN-END:|7-commandAction|17|7-postCommandAction
        // write post-action user code here
    }//GEN-LINE:|7-commandAction|18|


    public Command getOkCommand() {//GEN-BEGIN:|52-getter|0|52-preInit
        if (okCommand == null) {//GEN-END:|52-getter|0|52-preInit
            // write pre-init user code here
            okCommand = new Command("Ok", Command.OK, 0);//GEN-LINE:|52-getter|1|52-postInit
            // write post-init user code here
        }//GEN-BEGIN:|52-getter|2|
        return okCommand;
    }//GEN-END:|52-getter|2|

    public SplashScreen getSplashScreen() {//GEN-BEGIN:|14-getter|0|14-preInit
        if (splashScreen == null) {//GEN-END:|14-getter|0|14-preInit
            // write pre-init user code here
            splashScreen = new org.netbeans.microedition.lcdui.SplashScreen(getDisplay());//GEN-BEGIN:|14-getter|1|14-postInit
            splashScreen.setTitle("splashScreen");
            splashScreen.setCommandListener(this);//GEN-END:|14-getter|1|14-postInit
            // write post-init user code here
        }//GEN-BEGIN:|14-getter|2|
        return splashScreen;
    }//GEN-END:|14-getter|2|

    public List getList() {//GEN-BEGIN:|18-getter|0|18-preInit
        if (list == null) {//GEN-END:|18-getter|0|18-preInit
            // write pre-init user code here
            list = new List("list", Choice.IMPLICIT);//GEN-BEGIN:|18-getter|1|18-postInit
            list.append("List Element 1", null);
            list.append("List Element 2", null);
            list.append("List Element 3", null);
            list.setCommandListener(this);
            list.setSelectedFlags(new boolean[] { false, false, false });//GEN-END:|18-getter|1|18-postInit
            // write post-init user code here
        }//GEN-BEGIN:|18-getter|2|
        return list;
    }//GEN-END:|18-getter|2|

    public void listAction() {//GEN-LINE:|18-action|0|18-preAction
        // enter pre-action user code here
        String __selectedString = getList().getString(getList().getSelectedIndex());//GEN-BEGIN:|18-action|1|23-preAction
        if (__selectedString != null) {
            if (__selectedString.equals("List Element 1")) {//GEN-END:|18-action|1|23-preAction
                // write pre-action user code here
                System.out.println("ds/lfkds;fkdas;fk");//GEN-LINE:|18-action|2|23-postAction
                // write post-action user code here
            } else if (__selectedString.equals("List Element 2")) {//GEN-LINE:|18-action|3|24-preAction
                // write pre-action user code here
                switchToPreviousDisplayable();//GEN-LINE:|18-action|4|24-postAction
                // write post-action user code here
            } else if (__selectedString.equals("List Element 3")) {//GEN-LINE:|18-action|5|25-preAction
                // write pre-action user code here
                method();//GEN-LINE:|18-action|6|25-postAction
                // write post-action user code here
            }//GEN-BEGIN:|18-action|7|18-postAction
        }//GEN-END:|18-action|7|18-postAction
        // enter post-action user code here
    }//GEN-LINE:|18-action|8|

    public Form getForm() {//GEN-BEGIN:|26-getter|0|26-preInit
        if (form == null) {//GEN-END:|26-getter|0|26-preInit
            // write pre-init user code here
            form = new Form("form", new Item[] { getChoiceGroup(), getDateField(), getGauge(), getImageItem(), getStringItem(), getTextField(), getTableItem() });//GEN-LINE:|26-getter|1|26-postInit
            // write post-init user code here
        }//GEN-BEGIN:|26-getter|2|
        return form;
    }//GEN-END:|26-getter|2|

    public ChoiceGroup getChoiceGroup() {//GEN-BEGIN:|27-getter|0|27-preInit
        if (choiceGroup == null) {//GEN-END:|27-getter|0|27-preInit
            // write pre-init user code here
            choiceGroup = new ChoiceGroup("choiceGroup", Choice.MULTIPLE);//GEN-BEGIN:|27-getter|1|27-postInit
            choiceGroup.append("Choice Element 1", null);
            choiceGroup.append("Choice Element 2", null);
            choiceGroup.append("Choice Element 3", null);
            choiceGroup.setSelectedFlags(new boolean[] { false, false, false });
            choiceGroup.setFont(0, null);
            choiceGroup.setFont(1, null);
            choiceGroup.setFont(2, null);//GEN-END:|27-getter|1|27-postInit
            // write post-init user code here
        }//GEN-BEGIN:|27-getter|2|
        return choiceGroup;
    }//GEN-END:|27-getter|2|

    public DateField getDateField() {//GEN-BEGIN:|31-getter|0|31-preInit
        if (dateField == null) {//GEN-END:|31-getter|0|31-preInit
            // write pre-init user code here
            dateField = new DateField("dateField", DateField.DATE_TIME);//GEN-BEGIN:|31-getter|1|31-postInit
            dateField.setDate(new java.util.Date(System.currentTimeMillis()));//GEN-END:|31-getter|1|31-postInit
            // write post-init user code here
        }//GEN-BEGIN:|31-getter|2|
        return dateField;
    }//GEN-END:|31-getter|2|

    public Gauge getGauge() {//GEN-BEGIN:|32-getter|0|32-preInit
        if (gauge == null) {//GEN-END:|32-getter|0|32-preInit
            // write pre-init user code here
            gauge = new Gauge("gauge", false, 100, 50);//GEN-LINE:|32-getter|1|32-postInit
            // write post-init user code here
        }//GEN-BEGIN:|32-getter|2|
        return gauge;
    }//GEN-END:|32-getter|2|

    public ImageItem getImageItem() {//GEN-BEGIN:|33-getter|0|33-preInit
        if (imageItem == null) {//GEN-END:|33-getter|0|33-preInit
            // write pre-init user code here
            imageItem = new ImageItem("imageItem", getAaaaaa(), ImageItem.LAYOUT_DEFAULT, "<Missing Image>");//GEN-LINE:|33-getter|1|33-postInit
            // write post-init user code here
        }//GEN-BEGIN:|33-getter|2|
        return imageItem;
    }//GEN-END:|33-getter|2|

    public StringItem getStringItem() {//GEN-BEGIN:|35-getter|0|35-preInit
        if (stringItem == null) {//GEN-END:|35-getter|0|35-preInit
            // write pre-init user code here
            stringItem = new StringItem("stringItem", "adasdasdsadasdsadasdasd");//GEN-LINE:|35-getter|1|35-postInit
            // write post-init user code here
        }//GEN-BEGIN:|35-getter|2|
        return stringItem;
    }//GEN-END:|35-getter|2|

    public TextField getTextField() {//GEN-BEGIN:|36-getter|0|36-preInit
        if (textField == null) {//GEN-END:|36-getter|0|36-preInit
            // write pre-init user code here
            textField = new TextField("textField", null, 32, TextField.ANY);//GEN-LINE:|36-getter|1|36-postInit
            // write post-init user code here
        }//GEN-BEGIN:|36-getter|2|
        return textField;
    }//GEN-END:|36-getter|2|

    public TableItem getTableItem() {//GEN-BEGIN:|37-getter|0|37-preInit
        if (tableItem == null) {//GEN-END:|37-getter|0|37-preInit
            // write pre-init user code here
            tableItem = new org.netbeans.microedition.lcdui.TableItem(getDisplay(), "tableItem");//GEN-BEGIN:|37-getter|1|37-postInit
            tableItem.setModel(getSimpleTableModel());//GEN-END:|37-getter|1|37-postInit
            // write post-init user code here
        }//GEN-BEGIN:|37-getter|2|
        return tableItem;
    }//GEN-END:|37-getter|2|

    public TextBox getTextBox() {//GEN-BEGIN:|50-getter|0|50-preInit
        if (textBox == null) {//GEN-END:|50-getter|0|50-preInit
            // write pre-init user code here
            textBox = new TextBox("textBox", null, 100, TextField.ANY);//GEN-BEGIN:|50-getter|1|50-postInit
            textBox.addCommand(getOkCommand());
            textBox.setCommandListener(this);//GEN-END:|50-getter|1|50-postInit
            // write post-init user code here
        }//GEN-BEGIN:|50-getter|2|
        return textBox;
    }//GEN-END:|50-getter|2|

    public WaitScreen getWaitScreen() {//GEN-BEGIN:|54-getter|0|54-preInit
        if (waitScreen == null) {//GEN-END:|54-getter|0|54-preInit
            // write pre-init user code here
            waitScreen = new org.netbeans.microedition.lcdui.WaitScreen(getDisplay());//GEN-BEGIN:|54-getter|1|54-postInit
            waitScreen.setTitle("waitScreen");
            waitScreen.setCommandListener(this);//GEN-END:|54-getter|1|54-postInit
            // write post-init user code here
        }//GEN-BEGIN:|54-getter|2|
        return waitScreen;
    }//GEN-END:|54-getter|2|

    public Alert getAlert() {//GEN-BEGIN:|61-getter|0|61-preInit
        if (alert == null) {//GEN-END:|61-getter|0|61-preInit
            // write pre-init user code here
            alert = new Alert("alert");//GEN-BEGIN:|61-getter|1|61-postInit
            alert.setTimeout(Alert.FOREVER);//GEN-END:|61-getter|1|61-postInit
            // write post-init user code here
        }//GEN-BEGIN:|61-getter|2|
        return alert;
    }//GEN-END:|61-getter|2|

    public SMSComposer getSmsComposer() {//GEN-BEGIN:|63-getter|0|63-preInit
        if (smsComposer == null) {//GEN-END:|63-getter|0|63-preInit
            // write pre-init user code here
            smsComposer = new org.netbeans.microedition.lcdui.wma.SMSComposer(getDisplay());//GEN-BEGIN:|63-getter|1|63-postInit
            smsComposer.setTitle("smsComposer");
            smsComposer.addCommand(SMSComposer.SEND_COMMAND);
            smsComposer.setCommandListener(this);//GEN-END:|63-getter|1|63-postInit
            // write post-init user code here
        }//GEN-BEGIN:|63-getter|2|
        return smsComposer;
    }//GEN-END:|63-getter|2|

    public LoginScreen getLoginScreen() {//GEN-BEGIN:|67-getter|0|67-preInit
        if (loginScreen == null) {//GEN-END:|67-getter|0|67-preInit
            // write pre-init user code here
            loginScreen = new org.netbeans.microedition.lcdui.LoginScreen(getDisplay());//GEN-BEGIN:|67-getter|1|67-postInit
            loginScreen.setTitle("loginScreen");
            loginScreen.addCommand(LoginScreen.LOGIN_COMMAND);
            loginScreen.setCommandListener(this);//GEN-END:|67-getter|1|67-postInit
            // write post-init user code here
        }//GEN-BEGIN:|67-getter|2|
        return loginScreen;
    }//GEN-END:|67-getter|2|

    public PIMBrowser getPimBrowser() {//GEN-BEGIN:|70-getter|0|70-preInit
        if (pimBrowser == null) {//GEN-END:|70-getter|0|70-preInit
            // write pre-init user code here
            pimBrowser = new org.netbeans.microedition.lcdui.pda.PIMBrowser(getDisplay(), PIM.CONTACT_LIST);//GEN-BEGIN:|70-getter|1|70-postInit
            pimBrowser.setTitle("pimBrowser");
            pimBrowser.addCommand(PIMBrowser.SELECT_PIM_ITEM);
            pimBrowser.setCommandListener(this);//GEN-END:|70-getter|1|70-postInit
            // write post-init user code here
        }//GEN-BEGIN:|70-getter|2|
        return pimBrowser;
    }//GEN-END:|70-getter|2|

    public Image getAaaaaa() {//GEN-BEGIN:|34-getter|0|34-preInit
        if (aaaaaa == null) {//GEN-END:|34-getter|0|34-preInit
            // write pre-init user code here
            aaaaaa = Image.createImage(0, 0);//GEN-LINE:|34-getter|1|34-postInit
            // write post-init user code here
        }//GEN-BEGIN:|34-getter|2|
        return aaaaaa;
    }//GEN-END:|34-getter|2|

    public SimpleTableModel getSimpleTableModel() {//GEN-BEGIN:|38-getter|0|38-preInit
        if (simpleTableModel == null) {//GEN-END:|38-getter|0|38-preInit
            // write pre-init user code here
            simpleTableModel = new org.netbeans.microedition.lcdui.SimpleTableModel(new java.lang.String[][] {//GEN-BEGIN:|38-getter|1|38-postInit
                new java.lang.String[] { null, null, null, null, null, null },
                new java.lang.String[] { null, null, null, null, null, null },
                new java.lang.String[] { null, null, null, null, null, null }}, null);//GEN-END:|38-getter|1|38-postInit
            // write post-init user code here
        }//GEN-BEGIN:|38-getter|2|
        return simpleTableModel;
    }//GEN-END:|38-getter|2|

    public SimpleTableModel getSimpleTableModel1() {//GEN-BEGIN:|39-getter|0|39-preInit
        if (simpleTableModel1 == null) {//GEN-END:|39-getter|0|39-preInit
            // write pre-init user code here
            simpleTableModel1 = new org.netbeans.microedition.lcdui.SimpleTableModel(null, null);//GEN-LINE:|39-getter|1|39-postInit
            // write post-init user code here
        }//GEN-BEGIN:|39-getter|2|
        return simpleTableModel1;
    }//GEN-END:|39-getter|2|













    public Display getDisplay () {
        return Display.getDisplay(this);
    }

    public void exitMIDlet() {
        switchDisplayable (null, null);
        destroyApp(true);
        notifyDestroyed();
    }

    public void startApp() {
        if (midletPaused) {
            resumeMIDlet ();
        } else {
            initialize ();
            startMIDlet ();
        }
        midletPaused = false;
    }

    public void pauseApp() {
        midletPaused = true;
    }

    public void destroyApp(boolean unconditional) {
    }

}
