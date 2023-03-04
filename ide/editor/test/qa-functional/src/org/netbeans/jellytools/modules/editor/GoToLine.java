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
 * GoToLine.java
 *
 * Created on 2/11/03 10:58 AM
 */
package org.netbeans.jellytools.modules.editor;

import java.awt.Robot;
import java.awt.event.InputEvent;
import org.netbeans.jemmy.operators.*;

/** Class implementing all necessary methods for handling "Go to Line" NbDialog.
 *
 * @author eh103527
 * @version 1.0
 */
public class GoToLine extends JDialogOperator {

    /** Creates new GoToLine that can handle it.
     */
    public GoToLine() {
        super(java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("goto-title"));
    }
    
    private JLabelOperator _lblGoToLine;
    private JComboBoxOperator _cboGoToLine;
    private JButtonOperator _btGoto;
    private JButtonOperator _btClose;
    private JButtonOperator _btHelp;
    
    
    //******************************
    // Subcomponents definition part
    //******************************
    
    /** Tries to find "Go to Line:" JLabel in this dialog.
     * @return JLabelOperator
     */
    public JLabelOperator lblGoToLine() {
        if (_lblGoToLine==null) {
            _lblGoToLine = new JLabelOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("goto-line"));
        }
        return _lblGoToLine;
    }
    
    /** Tries to find null JComboBox in this dialog.
     * @return JComboBoxOperator
     */
    public JComboBoxOperator cboGoToLine() {
        if (_cboGoToLine==null) {
            _cboGoToLine = new JComboBoxOperator(this);
        }
        return _cboGoToLine;
    }
    
    /** Tries to find "Goto" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btGoto() {
        if (_btGoto==null) {
            _btGoto = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("goto-button-goto"));
        }
        return _btGoto;
    }
    
    /** Tries to find "Close" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btClose() {
        if (_btClose==null) {
            _btClose = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.netbeans.editor.Bundle").getString("goto-button-cancel"));
        }
        return _btClose;
    }
    
    /** Tries to find "Help" JButton in this dialog.
     * @return JButtonOperator
     */
    public JButtonOperator btHelp() {
        if (_btHelp==null) {
            _btHelp = new JButtonOperator(this, java.util.ResourceBundle.getBundle("org.openide.explorer.propertysheet.Bundle").getString("CTL_Help"));
        }
        return _btHelp;
    }
    
    
    //****************************************
    // Low-level functionality definition part
    //****************************************
    
    /** returns selected item for cboGoToLine
     * @return String item
     */
    public String getSelectedGoToLine() {
        return cboGoToLine().getSelectedItem().toString();
    }
    
    /** selects item for cboGoToLine
     * @param item String item
     */
    public void selectGoToLine(String item) {
        cboGoToLine().selectItem(item);
    }
    
    /** types text for cboGoToLine
     * @param text String text
     */
    public void typeGoToLine(String text) {
        cboGoToLine().typeText(text);
    }
    
    /** clicks on "Goto" JButton
     */
    public void goTo() {
        btGoto().push();
    }
    
    /** clicks on "Close" JButton
     */
    public void close() {
        btClose().push();
    }
    
    /** clicks on "Help" JButton
     */
    public void help() {
        btHelp().push();
    }
    
    
    //*****************************************
    // High-level functionality definition part
    //*****************************************
    
    /** Performs verification of GoToLine by accessing all its components.
     */
    public void verify() {
        lblGoToLine();
        cboGoToLine();
        btGoto();
        btClose();
        btHelp();
    }
    
    public static void goToLine(int line) {
        GoToLine op=new GoToLine();
        op.typeGoToLine(String.valueOf(line));
        op.goTo();
        while (op.isVisible()) {
            op.goTo();
        }
    }
    
    public static void goToLine(int line,Robot robot) {
        GoToLine op=new GoToLine();
        java.awt.Point p;
        int x,y;
        
        String s=String.valueOf(line);
        int c;
        robot.waitForIdle();
        robot.delay(200);
        for (int i=0;i < s.length();i++) {
            c=(int)s.charAt(i);
            robot.keyPress(c);
            robot.delay(50);
            robot.keyRelease(c);
        }
        p=op.btGoto().getLocationOnScreen();
        x=p.x+op.btGoto().getWidth()/2;
        y=p.y+op.btGoto().getHeight()/2;
        robot.mouseMove(x,y);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
        robot.delay(50);
        robot.waitForIdle();
        while (op.isVisible()) {
            robot.delay(50);
        }
    }
    
    /** Performs simple test of GoToLine
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        //new GoToLine().verify();
        System.out.println("GoToLine verification finished.");
        char c='0';
        System.out.println("Char c="+Integer.toHexString((int)c));
    }
}

