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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.metadata.model;

import java.lang.ref.WeakReference;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.api.Action;
import org.netbeans.modules.db.metadata.model.api.Metadata;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.MetadataModelException;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCMetadata;
import org.netbeans.modules.db.metadata.model.jdbc.mssql.MSSQLMetadata;
import org.netbeans.modules.db.metadata.model.jdbc.mysql.MySQLMetadata;
import org.netbeans.modules.db.metadata.model.jdbc.oracle.OracleMetadata;

/**
 *
 * @author Andrei Badea
 */
public class JDBCConnMetadataModel implements MetadataModelImplementation {

    private final static Logger LOGGER = Logger.getLogger(JDBCConnMetadataModel.class.getName());

    private final ReentrantLock lock = new ReentrantLock();
    private final WeakReference<Connection> connRef;
    private final String defaultSchemaName;

    private JDBCMetadata jdbcMetadata;

    public JDBCConnMetadataModel(Connection conn, String defaultSchemaName) {
        this.connRef = new WeakReference<Connection>(conn);
        if (defaultSchemaName != null && defaultSchemaName.trim().length() == 0) {
            this.defaultSchemaName = null;
        } else {
            this.defaultSchemaName = defaultSchemaName;
        }
    }

    public void runReadAction(Action<Metadata> action) throws MetadataModelException {
        lock.lock();
        try {
            // Prevent conn from being GC'd while under read access
            // by holding it in a variable.
            Connection conn = connRef.get();
            if (conn == null) {
                return;
            }
            try {
                enterReadAccess(conn);
                if (jdbcMetadata != null) {
                    Metadata metadata = jdbcMetadata.getMetadata();
                    action.run(metadata);
                }
            } catch (SQLException e) {
                throw new MetadataModelException(e);
            } catch (MetadataException e) {
                throw new MetadataModelException(e);
            }
        } finally {
            lock.unlock();
        }
    }

    public void refresh() {
        LOGGER.fine("Refreshing model");
        lock.lock();
        try {
            jdbcMetadata = null;
        } finally {
            lock.unlock();
        }
    }

    private void enterReadAccess(final Connection conn) throws SQLException {
        if (conn == null) {
            throw new NullPointerException("Connection can not be null");
        }
        Connection oldConn = (jdbcMetadata != null) ? jdbcMetadata.getConnection() : null;
        if (oldConn != conn) {
            if (conn != null) {
                jdbcMetadata = createMetadata(conn, defaultSchemaName);
            } else {
                jdbcMetadata = null;
            }
        }
    }

    private static JDBCMetadata createMetadata(Connection conn, String defaultSchemaName) {
        try {
            DatabaseMetaData dmd = conn.getMetaData();
            if ("Oracle".equals(dmd.getDatabaseProductName())) { // NOI18N
                return new OracleMetadata(conn, defaultSchemaName);
            }

            if ("mysql".equalsIgnoreCase(dmd.getDatabaseProductName())) { // NOI18N
                return new MySQLMetadata(conn, defaultSchemaName);
            }
            
            String driverName = dmd.getDriverName();
            if (driverName != null) {
                if ((driverName.contains("Microsoft") && driverName.contains("SQL Server")) //NOI18N
                        || driverName.contains("jTDS")) { //NOI18N
                    return new MSSQLMetadata(conn, defaultSchemaName);
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.INFO, null, e);
        }
        return new JDBCMetadata(conn, defaultSchemaName);
    }
}
