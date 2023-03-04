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
package org.netbeans.jellytools;

import java.lang.reflect.Field;
import javax.swing.JDialog;
import org.netbeans.jemmy.*;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JListOperator;

/** Enable to manipulate NetBeans wizards. You can access list of steps on the
 * left side, all buttons at the bottom (Back, Next, Finish, Cancel, Help).
 * Each step of a particular wizard is represented by an ancestor of
 * WizardOperator, i.e. all components are described there.
 * Timeout "WizardOperator.WaitWizardStepTimeout" is defined during waiting for
 * exact step (methods stepsWaitSelectedIndex(int) and
 * stepsWaitSelectedValue(String)). */
public class WizardOperator extends NbDialogOperator {
    
    private JButtonOperator _btNext;
    private JButtonOperator _btBack;
    private JButtonOperator _btFinish;
    private JListOperator _lstSteps;

    private static int WAIT_TIME = 60000;
    
    static {
	Timeouts.initDefault("WizardOperator.WaitWizardStepTimeout", WAIT_TIME);
    }
    
    
    /** Generic constructor
     * @param dialog instance of NbDialog
     */    
    public WizardOperator(JDialog dialog) {
        super(dialog);
    }

    /** Creates a new instance of WizardOperator.
     * It waits for a dialog with given title.
     * @param title  title of a wizard window
     */
    public WizardOperator(String title) {
        super(title);
    }
    
    /** Returns operator of "Next >" button.
     * @return  JButtonOperator instance of "Next >" button
     */
    public JButtonOperator btNext() {
        if (_btNext == null) {
            String nextCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_NEXT");
            _btNext = new JButtonOperator(this, nextCaption);
        }
        return _btNext;
    }
    
    /** Returns operator of "< Back" button.
     * @return  JButtonOperator instance of "< Back" button
     */
    public JButtonOperator btBack() {
        if (_btBack == null) {
            String backCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_PREVIOUS");
            _btBack = new JButtonOperator(this, backCaption);
        }
        return _btBack;
    }
    
    /** Returns operator of "Finish" button.
     * @return  JButtonOperator instance of "Finish" button
     */
    public JButtonOperator btFinish() {
        if (_btFinish == null) {
            String finishCaption = Bundle.getStringTrimmed("org.openide.Bundle", "CTL_FINISH");
            _btFinish = new JButtonOperator(this, finishCaption);
        }
        return _btFinish;
    }
    
    /** Returns operator of the list of steps on the left side in wizard dialog.
     * @return  JListOperator instance of list of steps
     */
    public JListOperator lstSteps() {
        if (_lstSteps == null) {
            _lstSteps = new JListOperator(this);
        }
        return _lstSteps;
    }
    
    /** Pushes "Next >" button. */
    public void next() {
        btNext().push();
    }
    
    /** Pushes "< Back" button. */
    public void back() {
        btBack().push();
    }
    
    /** Pushes "Finish" button. */
    public void finish() {
        btFinish().push();
    }
    
    /** Returns index of currently selected step which is bold faced.
     * @return  index of currently selected step (starts at 0)
     */
    public int stepsGetSelectedIndex() {
        int selectedIndex = -1;
        try {
            Field field = lstSteps().getCellRenderer().getClass().getDeclaredField("selected");
            field.setAccessible(true);
            selectedIndex = field.getInt(lstSteps().getCellRenderer());
        } catch (NoSuchFieldException e1) {
            throw new JemmyException("Field selected not found in CellRenderer.", e1);
        } catch (IllegalAccessException e2) {
            throw new JemmyException("Illegal access to field selected.", e2);
        }
        return selectedIndex;
    }
    
    /** Waits for panel with given index.
     * Timeout is declared as "WizardOperator.WaitWizardStepTimeout".
     * @param index int index of requested panel */    
    public void stepsWaitSelectedIndex(final int index) {
	try {
	    Waiter waiter = new Waiter(new Waitable() {
		    public Object actionProduced(Object obj) {
                        return index==stepsGetSelectedIndex()?new Object():null;
		    }
		    public String getDescription() {
			return("Wait WizardOperator step");
		    }
		});
	    Timeouts times = getTimeouts().cloneThis();
	    times.setTimeout("Waiter.WaitingTime", times.getTimeout("WizardOperator.WaitWizardStepTimeout"));
	    waiter.setTimeouts(times);
	    waiter.setOutput(getOutput());
	    waiter.waitAction(null);
	} catch(InterruptedException e) {}
    }
    
    /** Returns currently selected step which is bold faced.
     * @return  value of currently selected step without leading number
     */
    public String stepsGetSelectedValue() {
        return lstSteps().getModel().getElementAt(stepsGetSelectedIndex()).toString();
    }
    
    /** Waits for panel with given name.
     * Timeout is declared as "WizardOperator.WaitWizardStepTimeout".
     * @param panelName String requested panel name */    
    public void stepsWaitSelectedValue(String panelName) {
        int index = lstSteps().findItemIndex(panelName);
        if (index<0) throw new JemmyException("Panel with name \""+panelName+"\" not found.");
        stepsWaitSelectedIndex(index);
    }
    
    /** Checks if given panel name is currently selected/shown in wizard. It
     * compares name for exact match.
     * @param panelName  name of panel
     */
    protected void checkPanel(String panelName) {
        if(!stepsGetSelectedValue().equals(panelName)) {
            throw new JemmyException("Wrong panel! Found \""+stepsGetSelectedValue()+"\" instead of \""+panelName+"\".");
        }
    }
    
    /** Performs verification by accessing all sub-components */    
    public void verify() {
        btBack();
        btNext();
        btFinish();
        btCancel();
        btHelp();
        lstSteps();
    }
    
}
