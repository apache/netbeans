/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.test.html5.debug;

import java.util.Map;
import org.netbeans.jellytools.EditorOperator;
import org.netbeans.jellytools.MainWindowOperator;
import org.netbeans.jellytools.actions.Action;
import org.netbeans.jellytools.actions.ActionNoBlock;
import org.netbeans.jellytools.modules.debugger.BreakpointsWindowOperator;
import org.netbeans.jellytools.modules.debugger.actions.ToggleBreakpointAction;
import org.netbeans.jellytools.modules.debugger.actions.DeleteAllBreakpointsAction;
import org.netbeans.jemmy.EventTool;
import org.netbeans.jemmy.Waitable;
import org.netbeans.jemmy.Waiter;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JDialogOperator;
import org.netbeans.jemmy.operators.JTextFieldOperator;
import org.netbeans.test.html5.GeneralHTMLProject;

/**
 *
 * @author Vladimir Riha
 */
public class JavaScriptDebugger extends GeneralHTMLProject {

    public static final int VARIABLES_TIMEOUTS = 5000;

    public JavaScriptDebugger(String arg0) {
        super(arg0);
    }

    /**
     * Sets breakpoint in editor on line with specified text.
     *
     * @param eo EditorOperator instance where to set breakpoint
     * @param text text to find for setting breakpoint
     * @return line number where breakpoint was set (starts from 1)
     */
    public int setLineBreakpoint(EditorOperator eo, String text) throws Exception {
        eo.select(text); // NOI18N
        final int line = eo.getLineNumber();
        // toggle breakpoint via pop-up menu
        new ToggleBreakpointAction().performShortcut(eo.txtEditorPane());
        // wait breakpoint established
        new Waiter(new Waitable() {
            @Override
            public Object actionProduced(Object editorOper) {
                Object[] annotations = ((EditorOperator) editorOper).getAnnotations(line);
                for (int i = 0; i < annotations.length; i++) {
                    if ("Breakpoint".equals(EditorOperator.getAnnotationType(annotations[i]))) { // NOI18N
                        return Boolean.TRUE;
                    }
                }
                return null;
            }

            @Override
            public String getDescription() {
                return ("Wait breakpoint established on line " + line); // NOI18N
            }
        }).waitAction(eo);
        return line;
    }

    /**
     * Creates a new XMLHTTPRequest breakpoint (note: not supported by embedded
     * browser)
     *
     * @param urlFilter URL filter for which this breakpoint should be triggered
     * (pass empty string to break on all URLs)
     */
    public void setXHRBreakpoint(String urlFilter) {
        new Action("Window|Debugging|Breakpoints", null).perform();
        BreakpointsWindowOperator window = BreakpointsWindowOperator.invoke();
        new ActionNoBlock("Debug|New Breakpoint", null).perform();
        JDialogOperator nb = new JDialogOperator("New Breakpoint");
        JComboBoxOperator box = new JComboBoxOperator(nb, 0);
        box.selectItem("JavaScript");
        box = new JComboBoxOperator(nb, 1);
        box.selectItem("XMLHttpRequest");
        JTextFieldOperator filter = new JTextFieldOperator(nb, 0);
        filter.setText(urlFilter);
        JButtonOperator bOk = new JButtonOperator(nb, "OK");
        bOk.push();
    }

    public void disableAllBreakpoints() {
        new Action("Window|Debugging|Breakpoints", null).perform();
        BreakpointsWindowOperator window = BreakpointsWindowOperator.invoke();
        new ActionNoBlock(null, "Disable All").performPopup(window);
    }

    public void cleanBreakpoints() {
        BreakpointsWindowOperator window = BreakpointsWindowOperator.invoke();
        new DeleteAllBreakpointsAction().performPopup(window);
    }

    /**
     * Waits for variable to appear in Variables window (since Variables is
     * minimized by default, there could be "loading" message).
     *
     * @param expectedVariable
     */
    public void waitForVariable(final String expectedVariable) {
        try {
            Waiter waiter = new Waiter(new Waitable() {
                @Override
                public Object actionProduced(Object obj) {
                    try {
                        VariablesOperator vo = new VariablesOperator("Variables");
                        return ((Map<String, Variable>) vo.getVariables()).get(expectedVariable) != null ? Boolean.TRUE : null;
                    } catch (Exception ex) {
                        return null;
                    }
                }

                @Override
                public String getDescription() {
                    return ("Wait for Variables to contain " + expectedVariable);
                }
            });
            waiter.getTimeouts().setTimeout("Waiter.WaitingTime", VARIABLES_TIMEOUTS);
            waiter.waitAction(null);
        } catch (InterruptedException e) {
        }
    }

    /**
     * Waits for given action to be enabled in Debug main menu
     *
     * @param actionName action name to wait for
     */
    public void waitDebugger(String actionName) {
        for (int i = 0; i < 10; i++) {
            boolean enabled = MainWindowOperator.getDefault().menuBar().showMenuItem("Debug|" + actionName).isEnabled();
            MainWindowOperator.getDefault().menuBar().closeSubmenus();
            if (!enabled) {
                break;
            }
            new EventTool().waitNoEvent(300);
        }
    }

    /**
     * Saves given file and then waits (waitNoEvent) for given time (for
     * instance for file to be reloaded in browser)
     *
     * @param eo file to be saved
     * @param waitLimit time to wait in ms
     */
    public void saveAndWait(EditorOperator eo, long waitLimit) {
        eo.save();
        evt.waitNoEvent(waitLimit);
    }
}
