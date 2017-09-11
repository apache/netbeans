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
