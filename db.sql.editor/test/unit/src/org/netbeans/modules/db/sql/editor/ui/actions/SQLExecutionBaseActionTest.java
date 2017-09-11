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

package org.netbeans.modules.db.sql.editor.ui.actions;

import java.awt.Component;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.Action;
import javax.swing.JButton;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Andrei Badea
 */
public class SQLExecutionBaseActionTest extends NbTestCase {

    private InstanceContent instanceContent;
    private Lookup context;
    private SQLExecutionBaseActionImpl baseAction;
    private Action action;
    private SQLExecutionImpl sqlExecution;

    public SQLExecutionBaseActionTest(String testName) {
        super(testName);
    }

    public void setUp() {
        instanceContent = new InstanceContent();
        context = new AbstractLookup(instanceContent);
        baseAction = new SQLExecutionBaseActionImpl();
        action = baseAction.createContextAwareInstance(context);
        sqlExecution = new SQLExecutionImpl();
    }

    public void tearDown() {
        sqlExecution = null;
        action = null;
        baseAction = null;
        context = null;
        instanceContent = null;
    }

    protected boolean runInEQ() {
        return true;
    }

    public void testEnabled() {
        assertFalse("Should be disabled when no SQLExecution in the context", action.isEnabled());
        assertEquals("none", action.getValue(Action.NAME));

        instanceContent.add(sqlExecution);
        assertTrue("Should be enabled when SQLExecution in the context", action.isEnabled());
        assertEquals("idle", action.getValue(Action.NAME));

        sqlExecution.setExecuting(true);
        assertFalse("Should be disabled while executing", action.isEnabled());
        assertEquals("executing", action.getValue(Action.NAME));

        sqlExecution.setExecuting(false);
        assertTrue("Should be disabled when execution finished", action.isEnabled());
        assertEquals("idle", action.getValue(Action.NAME));

        instanceContent.remove(sqlExecution);
        assertFalse("Should be disabled when no SQLExecution removed from the context", action.isEnabled());
        assertEquals("none", action.getValue(Action.NAME));
    }

    public void testActionPerformed() {
        instanceContent.add(sqlExecution);
        Component tp = ((Presenter.Toolbar)action).getToolbarPresenter();
        assertTrue("The toolbar presenter should be a JButton", tp instanceof JButton);

        JButton button = (JButton)tp;
        button.doClick();
        assertTrue("Should perform the action when SQLExecution in the context", baseAction.actionPeformedCount == 1);

        instanceContent.remove(sqlExecution);
        button.doClick();
        assertTrue("Should not perform the action when no SQLExecution in the context", baseAction.actionPeformedCount == 1);

        instanceContent.add(sqlExecution);
        button.doClick();
        assertTrue("Should perform the action when SQLExecution in the context", baseAction.actionPeformedCount == 2);
    }

    private static final class SQLExecutionBaseActionImpl extends SQLExecutionBaseAction {

        private int actionPeformedCount;

        public String getDisplayName(SQLExecution sqlExecution) {
            if (sqlExecution == null) {
                return "none";
            } else if (sqlExecution.isExecuting()) {
                return "executing";
            } else {
                return "idle";
            }
        }

        protected void actionPerformed(SQLExecution sqlExecution) {
            assertNotNull(sqlExecution);
            actionPeformedCount++;
        }
    }

    private static final class SQLExecutionImpl implements SQLExecution {

        private PropertyChangeSupport propChangeSupport = new PropertyChangeSupport(this);
        private boolean executing = false;

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            propChangeSupport.removePropertyChangeListener(listener);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            propChangeSupport.addPropertyChangeListener(listener);
        }

        public boolean isExecuting() {
            return executing;
        }
        
        public void setExecuting(boolean executing) {
            this.executing = executing;
            propChangeSupport.firePropertyChange(SQLExecution.PROP_EXECUTING, null, null);
        }
        
        public boolean isSelection() {
            return false;
        }

        public void execute() {
        }

        public void executeSelection() {
        }

        public void setDatabaseConnection(DatabaseConnection dbconn) {
        }

        public DatabaseConnection getDatabaseConnection() {
            return null;
        }

        public void showHistory() {
            // not tested
        }
    }
}
