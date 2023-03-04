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

import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.execute.SQLExecuteCookie;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.netbeans.spi.project.ActionProvider;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Andrei Badea, Jiri Rechtacek
 */
@ServiceProvider(service=ActionProvider.class)
public class RunSQLAction extends SQLExecutionBaseAction implements ActionProvider {

    private static final Logger LOGGER = Logger.getLogger(RunSQLAction.class.getName());
    private static final boolean LOG = LOGGER.isLoggable(Level.FINE);

    private static final String ICON_PATH = "org/netbeans/modules/db/sql/editor/resources/runsql.png"; // NOI18N

    @Override
    protected String getIconBase() {
        return ICON_PATH;
    }

    protected String getDisplayName(SQLExecution sqlExecution) {
        return NbBundle.getMessage(RunSQLAction.class, "LBL_RunSqlAction");
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(RunSQLAction.class);
    }

    protected void actionPerformed(SQLExecution sqlExecution) {
        if (LOG) {
            LOGGER.log(Level.FINE, "actionPerformed for " + sqlExecution); // NOI18N
        }
        DatabaseConnection conn = sqlExecution.getDatabaseConnection();
        if (conn != null) {
            sqlExecution.execute();
        } else {
            conn = selectDatabaseConnection();
            if (conn != null) {
                LOGGER.finer("Set DatabaseConnection: " + conn);
                sqlExecution.setDatabaseConnection(conn);
                sqlExecution.execute();
            }
        }
    }

    public String[] getSupportedActions() {
        return new String[] { ActionProvider.COMMAND_RUN_SINGLE };
    }

    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        Collection<? extends DataObject> files = context.lookupAll(DataObject.class);
        if (files.isEmpty()) {
            return false;
        }
        for (DataObject d : files) {
            if (d.getLookup().lookup(SQLExecuteCookie.class) != null
                    || (FileUtil.getMIMEType(d.getPrimaryFile()) != null
                    && FileUtil.getMIMEType(d.getPrimaryFile()).equals("text/x-sql"))) { // NOI18N
                return true;
            }
        }
        return false;
    }

    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        Lookup.Result<SQLExecution> result = context.lookup(new Lookup.Template<SQLExecution>(SQLExecution.class));
        if (! result.allInstances().isEmpty()) {
            SQLExecution sqlExecution = result.allInstances().iterator().next();
            LOGGER.finer("Using " + sqlExecution + " for executing " + command);
            actionPerformed(sqlExecution);
            return ;
        }
        Collection<? extends DataObject> files = context.lookupAll(DataObject.class);
        if (files.isEmpty()) {
            assert false : "Any DataObject must found in lookup for command " + command;
            return ;
        }
        for (DataObject d : files) {
            if (FileUtil.getMIMEType(d.getPrimaryFile()) != null
                    && FileUtil.getMIMEType(d.getPrimaryFile()).equals("text/x-sql")) { // NOI18N
                SQLExecuteCookie execCookie = d.getCookie(SQLExecuteCookie.class);
                LOGGER.finer("Using SQLExecuteCookie: " + execCookie + " for executing " + command);
                if (execCookie != null) {
                    if (execCookie.getDatabaseConnection() == null) {
                        DatabaseConnection conn = selectDatabaseConnection();
                        LOGGER.finer("Attach DatabaseConnection: " + conn + " for executing " + command);
                        execCookie.setDatabaseConnection(conn);
                    }
                }
                EditorCookie editorCookie = d.getCookie(EditorCookie.class);
                if (editorCookie != null) {
                    if (editorCookie.getOpenedPanes() != null) {
                    } else {
                        LOGGER.finer("Opening " + d + " in the editor.");
                        editorCookie.open();
                    }
                }
                if (execCookie != null) {
                    execCookie.execute();
                    return ;
                }
            }
        }
    }

}
