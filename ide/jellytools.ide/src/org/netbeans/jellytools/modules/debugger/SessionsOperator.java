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
package org.netbeans.jellytools.modules.debugger;

import java.awt.Component;
import org.netbeans.jellytools.Bundle;
import org.netbeans.jellytools.TopComponentOperator;
import org.netbeans.jellytools.TreeTableOperator;
import org.netbeans.jellytools.modules.debugger.actions.FinishAllAction;
import org.netbeans.jellytools.modules.debugger.actions.FinishDebuggerAction;
import org.netbeans.jellytools.modules.debugger.actions.SessionsAction;
import org.netbeans.jemmy.ComponentChooser;
import org.netbeans.jemmy.TimeoutExpiredException;
import org.netbeans.jemmy.operators.JTableOperator;

/**
 * Provides access to the Sessions tom component. <p> Usage:<br>
 * <pre>
 *      SessionsOperator so = SessionsOperator.invoke();
 *      so.makeCurrent("MyClass");
 *      so.finishAll();
 *      so.close();
 * </pre>
 *
 *
 * @author Jiri Skrivanek
 */
public class SessionsOperator extends TopComponentOperator {

    private static final SessionsAction invokeAction = new SessionsAction();

    /**
     * Waits for Sessions top component and creates a new operator for it.
     */
    public SessionsOperator() {
        super(waitTopComponent(null,
                Bundle.getStringTrimmed("org.netbeans.modules.debugger.ui.views.Bundle",
                "CTL_Sessions_view"),
                0, viewSubchooser));
    }

    /**
     * Opens Sessions top component from main menu Window|Debugging|Sessions and
     * returns SessionsOperator.
     *
     * @return instance of SessionsOperator
     */
    public static SessionsOperator invoke() {
        invokeAction.perform();
        return new SessionsOperator();
    }

    public TreeTableOperator treeTable() {
        return new TreeTableOperator(this);
    }

    /**
     * ******************************** Actions ***************************
     */
    /**
     * Performs Finish All action on Sessions view.
     */
    public void finishAll() {
        FinishAllAction faa = new FinishAllAction();
        try {
            faa.perform(this);
        } catch (TimeoutExpiredException tee) {
            // try to close sessions one by one because it randomly fails for no apparent reason
            FinishDebuggerAction finishDebuggerAction = new FinishDebuggerAction();
            do {
                finishDebuggerAction.perform();
            } while (finishDebuggerAction.isEnabled());
        }
    }

    /**
     * Calls Make Current popup on given session. It throws TimeoutExpiredException
     * if session with given name not found.
     *
     * @param sessionName display name of session
     */
    public void makeCurrent(final String sessionName) {
        final JTableOperator table = new JTableOperator(this);
        table.waitState(new ComponentChooser() {
            @Override
            public boolean checkComponent(Component comp) {
                for (int i = 0; i < table.getRowCount(); i++) {
                    String text = table.getValueAt(i, 0).toString();
                    if (table.getComparator().equals(text, sessionName)) {
                        table.clickOnCell(i, 0, 2);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public String getDescription() {
                return "Session " + sessionName + " in table of sessions";
            }
        });
    }
    /**
     * SubChooser to determine OutputWindow TopComponent Used in constructor.
     */
    private static final ComponentChooser viewSubchooser = new ComponentChooser() {
        private static final String CLASS_NAME = "org.netbeans.modules.debugger.ui.views.View";

        @Override
        public boolean checkComponent(Component comp) {
            return comp.getClass().getName().endsWith(CLASS_NAME);
        }

        @Override
        public String getDescription() {
            return "component instanceof " + CLASS_NAME;// NOI18N
        }
    };
}
