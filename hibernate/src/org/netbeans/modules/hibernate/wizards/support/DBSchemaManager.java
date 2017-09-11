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

package org.netbeans.modules.hibernate.wizards.support;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.dbschema.DBException;
import org.netbeans.modules.dbschema.DBIdentifier;
import org.netbeans.modules.dbschema.SchemaElement;
import org.netbeans.modules.dbschema.SchemaElementUtil;
import org.netbeans.modules.dbschema.jdbcimpl.ConnectionProvider;
import org.netbeans.modules.dbschema.jdbcimpl.SchemaElementImpl;
import org.netbeans.modules.dbschema.util.NameUtil;
import org.netbeans.modules.hibernate.loaders.cfg.HibernateCfgDataObject;
import org.netbeans.modules.j2ee.core.api.support.progress.ProgressSupport;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Andrei Badea, gowri
 */
public class DBSchemaManager {

    public static final String DBSCHEMA_EXT = "dbschema"; // NOI18N

    private DatabaseConnection oldDBConn;
    private boolean oldDBConnWasConnected;
    private Connection conn;
    private SchemaElement schemaElement;

    private SQLException exception;

    private FileObject schemaFileObject;
    private SchemaElement fileSchemaElement;

    public SchemaElement getSchemaElement(final DatabaseConnection dbconn) throws SQLException {
        assert SwingUtilities.isEventDispatchThread();

        if (oldDBConn == dbconn && schemaElement != null) {
            return schemaElement;
        }

        schemaElement = null;

        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        if (oldDBConn != null && oldDBConn != dbconn && !oldDBConnWasConnected) {
            // need to disconnect the old connection
            actions.add(new ProgressSupport.BackgroundAction() {
                public void run(ProgressSupport.Context actionContext) {
                    actionContext.progress(NbBundle.getMessage(DBSchemaManager.class, "LBL_ClosingPreviousConnection"));

                    ConnectionManager.getDefault().disconnect(oldDBConn);
                    oldDBConn = null;
                    conn = null;
                }
            });
        } else {
            // no need to disconnect the old connection, just cleanup
            // before connecting the new connection
            oldDBConn = null;
            conn = null;
        }

        actions.add(new ProgressSupport.EventThreadAction() {
            public void run(ProgressSupport.Context actionContext) {
                ConnectionManager.getDefault().showConnectionDialog(dbconn);
                conn = dbconn.getJDBCConnection();
            }

            @Override
            public boolean isEnabled() {
                if (dbconn==null) {
                    oldDBConnWasConnected=false;
                } else {
                    conn = dbconn.getJDBCConnection();
                    oldDBConnWasConnected = conn != null;
                }
                return !oldDBConnWasConnected;
            }
        });

        actions.add(new ProgressSupport.Action() {
            
            private SchemaElementImpl schemaElementImpl;
            private boolean cancelled;
            
            public void run(final ProgressSupport.Context actionContext) {
                actionContext.progress(NbBundle.getMessage(DBSchemaManager.class, "LBL_RetrievingSchema"));
                
                oldDBConn = dbconn;
                
                ConnectionProvider connectionProvider = null;
                try {
                    connectionProvider = new ConnectionProvider(conn, dbconn.getDriverClass());
                    connectionProvider.setSchema(dbconn.getSchema());
                } catch (SQLException e) {
                    exception = e;
                    return;
                }
                
                synchronized (this) {
                    if (cancelled) {
                        return;
                    }
                    schemaElementImpl = new SchemaElementImpl(connectionProvider);
                }
                
                try {
                    schemaElementImpl.setName(DBIdentifier.create("dbschema")); // NOI18N
                } catch (DBException e) {
                    Logger.getLogger("global").log(Level.INFO, null, e);
                    return;
                }
                schemaElement = new SchemaElement(schemaElementImpl);
                
                schemaElementImpl.addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent event) {
                        String propertyName = event.getPropertyName();
                        String message = null;
                        
                        if ("totalCount".equals(propertyName)) { // NOI18N
                            int workunits = ((Integer)event.getNewValue()).intValue();
                            actionContext.switchToDeterminate(workunits);
                        } else if ("progress".equals(propertyName)) { // NOI18N
                            int workunit = ((Integer)event.getNewValue()).intValue();
                            actionContext.progress(workunit);
                        } else if ("tableName".equals(propertyName)) { // NOI18N
                            message = NbBundle.getMessage(DBSchemaManager.class, "LBL_RetrievingTable", event.getNewValue());
                        } else if ("viewName".equals(propertyName)) { // NOI18N
                            message = NbBundle.getMessage(DBSchemaManager.class, "LBL_RetrievingView", event.getNewValue());
                        } else if ("FKt".equals(propertyName)) { // NOI18N
                            message = NbBundle.getMessage(DBSchemaManager.class, "LBL_RetrievingTableKeys", event.getNewValue());
                        } else if ("FKv".equals(propertyName)) { // NOI18N
                            message = NbBundle.getMessage(DBSchemaManager.class, "LBL_RetrievingViewKeys", event.getNewValue());
                        }
                        
                        if (message != null) {
                            actionContext.progress(message);
                        }
                    }
                });
                
                schemaElementImpl.initTables(connectionProvider);
            }
            
            @Override
            public boolean isEnabled() {
                return conn != null;
            }
            
            @Override
            public boolean cancel() {
                synchronized (this) {
                    cancelled = true;
                    if (schemaElementImpl != null) {
                        schemaElementImpl.setStop(true);
                    }
                }
                return true;
            }

            @Override
            protected boolean isBackground() {
                return true;
            }
        });

        exception = null;
        boolean success = ProgressSupport.invoke(actions, true);
        if (exception != null) {
            throw exception;
        }

        if (!success) {
            // the Cancel button was pressed
            schemaElement = null;
        }
        return schemaElement;
    }

    public SchemaElement getSchemaElement(final FileObject fo) {
        assert SwingUtilities.isEventDispatchThread();

        if (fo == schemaFileObject) {
            return fileSchemaElement;
        }

        schemaFileObject = null;
        fileSchemaElement = null;

        List<ProgressSupport.Action> actions = new ArrayList<ProgressSupport.Action>();

        actions.add(new ProgressSupport.BackgroundAction() {
            public void run(ProgressSupport.Context actionContext) {
                actionContext.progress(NbBundle.getMessage(DBSchemaManager.class, "LBL_ReadingSchemaFile"));

                schemaFileObject = fo;
                fileSchemaElement = SchemaElementUtil.forName(fo);
            }
        });

        ProgressSupport.invoke(actions);

        return fileSchemaElement;
    }

    /**
     * Updates the dbschema files in the given <code>DBSchemaFileList</code> instance
     * with the contents of the <code>schemaElement</code>
     * parameter if these dbschema files come from the same database URL
     * and database schema as <code>schemaElement</code>. If there are no such
     * dbschema files a new dbschema is written in <code>folder</code>.
     */
    public static FileObject updateDBSchemas(SchemaElement schemaElement, DBSchemaFileList dbschemaFileList, FileObject folder, String projectName) throws IOException {
        FileObject result = updateDBSchemas(schemaElement, dbschemaFileList);
        if (result == null) {
            result = writeDBSchema(schemaElement, folder, projectName);
        }
        return result;
    }

    /*
     * Updates the dbschema files according to the description of
     * {@link #updateDBSchemas(SchemaElement, DBSchemaFileList, FileObject, String)}
     * and returns the last one.
     */
    private static FileObject updateDBSchemas(SchemaElement schemaElement, DBSchemaFileList dbschemaFileList) throws IOException {
        FileObject updatedDBSchemaFile = null;
        DBIdentifier schemaFullName = schemaElement.getSchema();
        String schemaName = schemaFullName != null ? schemaFullName.getName() : null;

        for (FileObject dbschemaFile : dbschemaFileList.getFileList()) {
            SchemaElement existingSchemaElement = SchemaElementUtil.forName(dbschemaFile);
            DBIdentifier existingSchemaFullName = existingSchemaElement.getSchema();
            String existingSchemaName = existingSchemaFullName != null ? existingSchemaFullName.getName() : null;

            if (Utilities.compareObjects(existingSchemaElement.getUrl(), schemaElement.getUrl()) &&
                    Utilities.compareObjects(existingSchemaName, schemaName)) {
                DBIdentifier existingDBSchemaName = existingSchemaElement.getName();
                overwriteDBSchema(schemaElement, dbschemaFile, existingDBSchemaName);
                updatedDBSchemaFile = dbschemaFile;
            }
        }

        return updatedDBSchemaFile;
    }

    /**
     * Writes <code>schemaElement</code> as a new dbschema file in <code>folder</code>.
     */
    private static FileObject writeDBSchema(SchemaElement schemaElement, FileObject folder, String projectName) throws IOException {
        String schemaName = schemaElement.getSchema().getName();
        String fileName = (schemaName != null && schemaName != "" ? schemaName + "_" : "") + projectName; // NOI18N
        // #65887: the schema name should not contain the schema db element separator
        fileName = fileName.replace(NameUtil.dbElementSeparator, '_'); // NOI18N

        String freeFileName = FileUtil.findFreeFileName(folder, fileName, DBSCHEMA_EXT);
        DBIdentifier dbschemaName = DBIdentifier.create(freeFileName);

        try {
            schemaElement.setName(dbschemaName);
        } catch (DBException e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }

        FileObject dbschemaFile = folder.createData(freeFileName, DBSCHEMA_EXT);
        writeSchemaElement(schemaElement, dbschemaFile);

        return dbschemaFile;
    }

    /**
     * Overwrites <code>dbschemaFile</code> with the contents of
     * <code>schemaElement</code>.
     */
    private static void overwriteDBSchema(SchemaElement schemaElement, FileObject dbschemaFile, DBIdentifier dbschemaName) throws IOException {
        try {
            schemaElement.setName(dbschemaName);
        } catch (DBException e) {
            IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }

        // cannot just overwrite the file, DBSchemaDataObject would not
        // notice the file has changed.
        FileObject parent = dbschemaFile.getParent();
        String fileName = dbschemaFile.getName();
        String fileExt = dbschemaFile.getExt();
        dbschemaFile.delete();
        FileObject newDBSchemaFile = parent.createData(fileName, fileExt);

        writeSchemaElement(schemaElement, newDBSchemaFile);
    }

    /**
     * Writes the contents of <code>schemaElement</code> to the existing
     * <code>dbschemaFile</code>.
     */
    private static void writeSchemaElement(SchemaElement schemaElement, FileObject dbschemaFile) throws IOException {
        FileLock lock = dbschemaFile.lock();
        try {
            OutputStream os = new BufferedOutputStream(dbschemaFile.getOutputStream(lock));
            try {
                schemaElement.save(os);
            } finally {
                os.close();
            }
        } finally {
            lock.releaseLock();
        }
    }
}
