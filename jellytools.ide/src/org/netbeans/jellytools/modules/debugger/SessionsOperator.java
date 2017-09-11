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
