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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
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
