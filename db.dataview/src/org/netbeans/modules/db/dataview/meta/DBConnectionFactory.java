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
package org.netbeans.modules.db.dataview.meta;

import java.sql.Connection;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.dataview.spi.DBConnectionProvider;

/**
 * DBConnectionFactory is used to serve out DB Session The actual physical
 * connection handling is implemented by classes implements DBConnectionProvider.
 *
 * If No DBConnectionProvider found, then it uses DBExplorer connection
 * 
 * @author Ahimanikya Satapathy
 */
public final class DBConnectionFactory {

    private static volatile DBConnectionFactory INSTANCE = null;
    private volatile Throwable ex = null;
    private static Logger mLogger = Logger.getLogger(DBConnectionFactory.class.getName());

    public static DBConnectionFactory getInstance() {
        synchronized (DBConnectionFactory.class) {
            if (INSTANCE == null) {
                if (INSTANCE == null) {
                    INSTANCE = new DBConnectionFactory();
                }
            }
        }
        return INSTANCE;
    }

    private DBConnectionFactory() {
    }

    public void closeConnection(Connection con) {
        DBConnectionProvider connectionProvider = findDBConnectionProvider();
        if (connectionProvider != null) {
            connectionProvider.closeConnection(con);
        }
    }

    public Connection getConnection(DatabaseConnection dbConn) {
        DBConnectionProvider connectionProvider = findDBConnectionProvider();
        this.ex = null;
        try {
            if (connectionProvider != null) {
                return connectionProvider.getConnection(dbConn);
            } else {
                return showConnectionDialog(dbConn);
            }
        } catch (Exception e) {
            mLogger.log(Level.WARNING, "Failed to set connection:" + e); // NOI18N
            this.ex = e;
            return null;
        }
    }

    public Throwable getLastException() {
        return ex;
    }

    private Connection showConnectionDialog(final DatabaseConnection dbConn) {
        if (dbConn == null) {
            return null;
        }
        Connection conn = dbConn.getJDBCConnection(!SwingUtilities.isEventDispatchThread());
        if (conn == null) {
            // this call is automatically redirected to AWT thread if needed
            ConnectionManager.getDefault().showConnectionDialog(dbConn);
            return dbConn.getJDBCConnection(!SwingUtilities.isEventDispatchThread());
        } else {
            return conn;
        }
    }

    private DBConnectionProvider findDBConnectionProvider() {
        Iterator<DBConnectionProvider> it = ServiceLoader.load(DBConnectionProvider.class).iterator();
        if (it.hasNext()) {
            return it.next();
        }

        /*
         * This gives the user/module/components that use etlengine DBConnection
         * factory an option to associate a required class loader with the
         * DBConnectionFactory class. Our requirement is to get the classLoader
         * whose getResources() should be able to point to
         * META-INF/services/org.netbeans.modules.db.model.spi.DBConnectionProvider
         */
        ClassLoader loader = DBConnectionFactory.class.getClassLoader();

        it = ServiceLoader.load(DBConnectionProvider.class, loader).iterator();
        if (it.hasNext()) {
            return it.next();
        }

        return null;
    }
}
