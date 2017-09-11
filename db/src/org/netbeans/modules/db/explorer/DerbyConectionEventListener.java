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

package org.netbeans.modules.db.explorer;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.openide.util.Exceptions;

/**
 * This class receives notifications about connection events which
 * are used to apply the hacks for Derby. It gathers these hacks
 * together instead of having them spread all over the code.
 *
 * <p>In the future and if necessary a ConnectionEventListener interface should be defined,
 * which this class should implement. It should be possible to register
 * implementations of the CEL inteface, for example in the layer.</p>
 * 
 * @author Andrei Badea
 */
public class DerbyConectionEventListener {
    
    // XXX this class should be called DerbyConnectionEventListener (double 'n' in connection)
    
    private static final DerbyConectionEventListener DEFAULT = new DerbyConectionEventListener();
    
    private static final String DERBY_DATABASE_FORCE_LOCK = "derby.database.forceDatabaseLock"; // NOI18N
    private static final String DERBY_SYSTEM_HOME = "derby.system.home"; // NOI18N
    private static final String DERBY_SYSTEM_SHUTDOWN_STATE = "XJ015"; // NOI18N
    
    public static DerbyConectionEventListener getDefault() {
        return DEFAULT;
    }

    /**
     * Called before a database connection is connected.
     *
     * @param dbconn the database connection.
     */
    public void beforeConnect(DatabaseConnection dbconn) {
        if (!dbconn.getDriver().equals("org.apache.derby.jdbc.EmbeddedDriver")) { // NOI18N
            return;
        }
        
        // force the database lock -- useful on Linux, see issue 63957
        if (System.getProperty(DERBY_DATABASE_FORCE_LOCK) == null) {
            System.setProperty(DERBY_DATABASE_FORCE_LOCK, "true"); // NOI18N
        }
        
        // set the system directory, see issue 64316
        if (System.getProperty(DERBY_SYSTEM_HOME) == null) { // NOI18N
            File derbySystemHome = new File(System.getProperty("netbeans.user"), "derby"); // NOI18N
            derbySystemHome.mkdirs();
            System.setProperty(DERBY_SYSTEM_HOME, derbySystemHome.getAbsolutePath()); // NOI18N
        }
    }
    
    /**
     * Called after a database connection was disconnected. 
     *
     * @param dbconn the database connection.
     * @param conn the closed {@link java.sql.Connection}. This parameter is needed since dbconn.getJDBCConnection()
        returns null at the moment when afterDisconnect is called.
     */
    public void afterDisconnect(DatabaseConnection dbconn, Connection conn) {
        if (!dbconn.getDriver().equals("org.apache.derby.jdbc.EmbeddedDriver")) { // NOI18N
            return;
        }
        
        // shutdown the Derby database instance
        try {
            DbDriverManager.getDefault().getSameDriverConnection(conn, "jdbc:derby:;shutdown=true", new Properties()); // NOI18N
        } catch (SQLException e) {
            if (!DERBY_SYSTEM_SHUTDOWN_STATE.equals(e.getSQLState())) { // NOI18N
                Exceptions.printStackTrace(e);
            }
        }
    }
}
