/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
