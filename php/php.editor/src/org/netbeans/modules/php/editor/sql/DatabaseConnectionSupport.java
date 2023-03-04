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
