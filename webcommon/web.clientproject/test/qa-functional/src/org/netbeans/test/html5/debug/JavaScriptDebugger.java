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
