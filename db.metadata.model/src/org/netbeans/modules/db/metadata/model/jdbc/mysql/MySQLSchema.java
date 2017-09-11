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
 * Portions Copyrighted 2008-2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.db.metadata.model.jdbc.mysql;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.db.metadata.model.api.MetadataException;
import org.netbeans.modules.db.metadata.model.api.Procedure;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCCatalog;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCProcedure;
import org.netbeans.modules.db.metadata.model.jdbc.JDBCSchema;

/**
 *
 * @author David, Jiri Rechtacek
 */
public class MySQLSchema extends JDBCSchema {
    
    private static final Logger LOGGER = Logger.getLogger(MySQLSchema.class.getName());
    
    public MySQLSchema(JDBCCatalog jdbcCatalog, String name, boolean _default, boolean synthetic) {
        super(jdbcCatalog, name, _default, synthetic);
    }

    @Override
    protected void createProcedures() {
        LOGGER.log(Level.FINE, "Initializing MySQL procedures in {0}", this);
        Map<String, Procedure> newProcedures = new LinkedHashMap<String, Procedure>();
        // routines
        try {
            DatabaseMetaData dmd = jdbcCatalog.getJDBCMetadata().getDmd();
            Statement stmt = dmd.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT NAME, TYPE" // NOI18N
                                            + " FROM mysql.proc WHERE DB='" + jdbcCatalog.getName() + "'" // NOI18N
                                            + " AND ( TYPE = 'PROCEDURE' OR TYPE = 'FUNCTION' )"); // NOI18N
            try {
                while (rs.next()) {
                    String procedureName = rs.getString("NAME"); // NOI18N
                    Procedure procedure = createJDBCProcedure(procedureName).getProcedure();
                    newProcedures.put(procedureName, procedure);
                    LOGGER.log(Level.FINE, "Created MySQL procedure: {0}, type: {1}", new Object[]{procedure, rs.getString("TYPE")});
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                stmt.close();
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        // information_schema.triggers
        try {
            DatabaseMetaData dmd = jdbcCatalog.getJDBCMetadata().getDmd();
            Statement stmt = dmd.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery("SELECT TRIGGER_NAME" // NOI18N
                                            + " FROM information_schema.triggers WHERE TRIGGER_SCHEMA='" + jdbcCatalog.getName() + "'"); // NOI18N
            try {
                while (rs.next()) {
                    String procedureName = rs.getString("TRIGGER_NAME"); // NOI18N
                    Procedure procedure = createJDBCProcedure(procedureName).getProcedure();
                    newProcedures.put(procedureName, procedure);
                    LOGGER.log(Level.FINE, "Created MySQL trigger: {0}", new Object[]{procedure});
                }
            } finally {
                if (rs != null) {
                    rs.close();
                }
                stmt.close();
            }
        } catch (SQLException e) {
            throw new MetadataException(e);
        }
        procedures = Collections.unmodifiableMap(newProcedures);
    }
    
    @Override
    protected JDBCProcedure createJDBCProcedure(String procedureName) {
        return new MySQLProcedure(this, procedureName);
    }

    @Override
    public String toString() {
        return "MySQLSchema[jdbcCatalog=" + jdbcCatalog.getName() + ", name=" + getName() + "]";
    }

}
