/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.php.editor.sql;

import java.util.prefs.Preferences;
import javax.swing.text.Document;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.php.editor.sql.ui.SelectConnectionPanel;
import org.openide.filesystems.FileObject;

/**
 * This class provides useful functionality around working with database connections,
 * such as selecting a connection to use for the project.
 *
 * @author David Van Couvering
 */
public final class DatabaseConnectionSupport {
    private static final String PROP_DBCONN = "dbconn"; // NOI18N

    private DatabaseConnectionSupport() {
    }

    // TODO - add support for setting a connection for a given file, instead
    // of the whole project

    public static DatabaseConnection selectDatabaseConnection(Document doc, boolean mySQLOnly, boolean passwordRequired) {
        DatabaseConnection dbconn = getDatabaseConnection(doc, false);

        dbconn = SelectConnectionPanel.selectConnection(dbconn, mySQLOnly, passwordRequired);

        setDatabaseConnection(doc, dbconn, true);

        return dbconn;
    }

    public static DatabaseConnection selectDatabaseConnection() {
        return selectDatabaseConnection(null, false, false);
    }

    public static DatabaseConnection selectDatabaseConnection(boolean mySQLOnly, boolean passwordRequired) {
        return selectDatabaseConnection(null, mySQLOnly, passwordRequired);
    }

    public static DatabaseConnection selectDatabaseConnection(Document doc) {
        return selectDatabaseConnection(doc, false, false);
    }

    private static DatabaseConnection getDatabaseConnection(String connectionName, boolean reconnect) {
        DatabaseConnection dbconn = null;
        if (connectionName != null) {
            dbconn = ConnectionManager.getDefault().getConnection(connectionName);
        }

        if (dbconn != null && dbconn.getJDBCConnection() == null && reconnect) {
            ConnectionManager.getDefault().showConnectionDialog(dbconn);
        }
        return dbconn;
    }

    public static DatabaseConnection getDatabaseConnection(Document doc, boolean reconnect) {
        if (doc == null) {
            return null;
        }
        Project project = getProject(doc);
        String name = project != null ? getProjectPreferences(project).get(PROP_DBCONN, null) : null;
        return getDatabaseConnection(name, reconnect);
    }

    private static Preferences getProjectPreferences(Project project) {
        return ProjectUtils.getPreferences(project, PHPSQLCompletion.class, false);
    }

    private static Project getProject(Document doc) {
        if (doc == null) {
            return null;
        }
        FileObject fo = NbEditorUtilities.getFileObject(doc);
        if (fo != null) {
            return FileOwnerQuery.getOwner(fo);
        }
        return null;
    }

    private static void setDatabaseConnection(Project project, DatabaseConnection dbconn) {
        if (project == null) {
            return;
        }

        Preferences prefs = getProjectPreferences(project);
        if (dbconn != null) {
            prefs.put(PROP_DBCONN, dbconn.getName());
        } else {
            prefs.remove(PROP_DBCONN);
        }
    }

    private static void setDatabaseConnection(Document doc, DatabaseConnection dbconn, boolean forProject) {
        // TODO - add support for document-specific preference
        Project project = getProject(doc);
        if (project != null) {
            setDatabaseConnection(getProject(doc), dbconn);
        }
    }

}
