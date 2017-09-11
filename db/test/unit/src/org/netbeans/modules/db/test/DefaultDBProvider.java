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

package org.netbeans.modules.db.test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author David Van Couvering
 */
public class DefaultDBProvider implements DBProvider {
    public void createSchema(Connection conn, String schemaName) throws Exception {
        dropSchema(conn, schemaName);
        conn.createStatement().executeUpdate("CREATE SCHEMA " + schemaName);
    }
    
    public void setSchema(Connection conn, String schemaName) throws Exception {
        conn.createStatement().executeUpdate("SET SCHEMA " + schemaName);
    }
    
    public void dropSchema(Connection conn, String schemaName) throws Exception {
        if (schemaExists(conn, schemaName)) {
            return;
        }
        conn.createStatement().executeUpdate("DROP SCHEMA " + schemaName);
    }

    public void createTestTable(Connection conn, String schemaName, String tableName, String idName) throws Exception {
        if (tableExists(conn, schemaName, tableName)) {
            return;
        }
        conn.createStatement().executeUpdate("CREATE TABLE " + schemaName + '.' + tableName + " (" +
                idName + " integer primary key)");
    }
    
    public void dropTable(Connection conn, String schemaName, String tableName) throws Exception {
        if (!tableExists(conn, schemaName, tableName)) {
            return;
        }
        try {
            conn.createStatement().executeUpdate("DROP TABLE " + schemaName + "." + tableName);
        } catch (SQLException sqle) {
            System.out.println("Exception when dropping table, probably because it doesn't exist: " + sqle.getMessage());
        }
    }

    public void dropView(Connection conn, String schemaName, String tableName) throws Exception {
        conn.createStatement().executeUpdate("DROP VIEW " + schemaName + "." + tableName);
    }

    public boolean tableExists(Connection conn, String schema, String tableName) throws Exception {
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getTables(null, schema, tableName, null);
        return rs.next();        
    }

    public boolean schemaExists(Connection conn, String schemaName) throws Exception {
        DatabaseMetaData md = conn.getMetaData();
        
        ResultSet rs  = md.getSchemas();
        
        while ( rs.next() ) {
            if ( schemaName.toLowerCase().equals(rs.getString(1).toLowerCase())) {
                return true;
            }
        }
    
        return false;
    }
    
    public boolean columnInIndex(Connection conn, String schemaName, String tableName, String colname, String indexName)
            throws Exception {
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs = md.getIndexInfo(null, schemaName, tableName, false, false);

        while ( rs.next() ) {
            String ixName = rs.getString(6);
            if ( ixName != null && ixName.equals(indexName)) {
                String ixColName = rs.getString(9);
                if ( ixColName.equals(colname) ) {
                    return true;
                }
            }
        }

        return false;
        
    }

}
