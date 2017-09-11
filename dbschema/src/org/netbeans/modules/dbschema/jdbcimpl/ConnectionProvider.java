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

package org.netbeans.modules.dbschema.jdbcimpl;

import java.sql.*;
import java.util.ResourceBundle;

public class ConnectionProvider {

	final ResourceBundle bundle = ResourceBundle.getBundle("org.netbeans.modules.dbschema.resources.Bundle"); // NOI18N

    private Connection con;
    private DatabaseMetaData dmd;

    private String driver;
    private String url;
    private String username;
    private String password;
    private String schema;

    /** Creates new ConnectionProvider */
    public ConnectionProvider(Connection con, String driver) throws SQLException{
        this.con = con;
        this.driver = driver;
        dmd = con.getMetaData();
    }
    
    public ConnectionProvider(String driver, String url, String username, String password) throws ClassNotFoundException, SQLException {
        this.driver = driver;
        this.url = url;
        this.username = username;
        this.password = password;
    
        Class.forName(driver);
        con = DriverManager.getConnection(url, username, password);
        dmd = con.getMetaData();
    }
  
    public Connection getConnection() {
        return con;
    }
  
    public DatabaseMetaData getDatabaseMetaData() throws SQLException {
        return dmd;
    }

    public String getDriver() {
        return driver;
    }
    
    public void setSchema(String schema) {
        this.schema = schema;
    }
    
    public String getSchema() {
        return schema;
    }
    
    public void closeConnection() {
        if (con != null)
            try {
                con.close();
            } catch (SQLException exc) {
                if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                    System.out.println(bundle.getString("UnableToCloseConnection")); //NOI18N
                con = null;
            }
    }
}
