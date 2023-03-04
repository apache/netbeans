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

package org.netbeans.modules.db.sql.editor.ui.actions;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.openide.awt.Actions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 * @author Andrei Badea
 */
public class RunSQLSelectionAction extends SQLExecutionBaseAction {

    private static final Logger LOGGER = Logger.getLogger(RunSQLSelectionAction.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);
    private static final String ICON_PATH =
            "org/netbeans/modules/db/sql/editor/resources/runsql-partial.png"; // NOI18N

    protected void initialize() {
        putValue(Action.NAME, NbBundle.getMessage(RunSQLSelectionAction.class, "LBL_RunSQLSelectionAction"));
        putValue("noIconInMenu", Boolean.TRUE);
    }

    @Override
    protected String getIconBase() {
        return ICON_PATH;
    }

    public String getDisplayName(SQLExecution sqlExecution) {
        if (sqlExecution == null || sqlExecution.isSelection()) {
            return NbBundle.getMessage(RunSQLSelectionAction.class, "LBL_RunSelectionAction");
        } else {
            return NbBundle.getMessage(RunSQLSelectionAction.class, "LBL_RunCurrentStatementAction");
        }
    }

    public void actionPerformed(SQLExecution sqlExecution) {
        if (LOG) {
            LOGGER.log(Level.FINE, "actionPerformed for " + sqlExecution); // NOI18N
        }
        DatabaseConnection dbconn = sqlExecution.getDatabaseConnection();
        if (dbconn != null) {
            sqlExecution.executeSelection();
        } else {
            notifyNoDatabaseConnection();
        }
    }

    public Action createContextAwareInstance(Lookup actionContext) {
        return new SelectionContextAwareDelegate(this, actionContext);
    }

    private static final class SelectionContextAwareDelegate extends ContextAwareDelegate implements Presenter.Popup {

        public SelectionContextAwareDelegate(RunSQLSelectionAction parent, Lookup actionContext) {
            super(parent, actionContext);
        }

        public JMenuItem getPopupPresenter() {
            return new Actions.MenuItem(this, false);
        }
    }
}
