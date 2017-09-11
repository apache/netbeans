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

import java.sql.*;

/**
 *
 * @author David
 */
public class DerbyDBProvider extends DefaultDBProvider {

    private String quote(String name, boolean identifier) {
        String quote = identifier ? "\"" : "'";
        return quote + name.replace(quote, quote + quote) + quote;
    }

    private String qualifiedName(String schema, String object) {
        return quote(schema, true) + "." + quote(object, true);
    }

    @Override
    public void dropSchema(Connection conn, String schemaName) throws Exception {
        if (!schemaExists(conn, schemaName)) {
            return;
        }

        // With Derby, you can't just drop the schema.  You have go manually
        // deal with all the constraints
        
        // drop views first, as they depend on tables
        DatabaseMetaData md = conn.getMetaData();
        ResultSet rs;
        Statement s = conn.createStatement();

        rs = md.getFunctions(null, schemaName, "%");
        while (rs.next()) {
            s.execute("DROP FUNCTION " + qualifiedName(schemaName, rs.getString("FUNCTION_NAME")));
        }
        rs.close();

        rs = md.getProcedures(null, schemaName, "%");
        while (rs.next()) {
            s.execute("DROP PROCEDURE " + qualifiedName(schemaName, rs.getString("PROCEDURE_NAME")));
        }
        rs.close();

        rs = md.getExportedKeys(null, schemaName, "%");
        while (rs.next()) {
            String referencingTable = qualifiedName(rs.getString("FKTABLE_SCHEMA"),
                    rs.getString("FKTABLE_NAME"));
            s.execute("ALTER TABLE " + referencingTable
                    + " DROP FOREIGN KEY " + rs.getString("FK_NAME"));
        }
        rs.close();

        rs = md.getTables(null, schemaName, "%", new String[]{"VIEW"});
        while (rs.next()) {
            s.execute("DROP VIEW " + qualifiedName(schemaName, rs.getString("TABLE_NAME")));
        }
        rs.close();

        rs = md.getTables(null, schemaName, "%", new String[]{"SYNONYM"});
        while (rs.next()) {
            s.execute("DROP SYNONYM " + qualifiedName(schemaName, rs.getString("TABLE_NAME")));
        }
        rs.close();

        rs = md.getTables(null, schemaName, "%", new String[]{"TABLE"});
        while (rs.next()) {
            s.execute("DROP TABLE " + qualifiedName(schemaName, rs.getString("TABLE_NAME")));
        }
        rs.close();

        rs = md.getUDTs(null, schemaName, "%", null);
        while (rs.next()) {
            s.execute("DROP TYPE " + qualifiedName(schemaName, rs.getString("TYPE_NAME"))
                    + " RESTRICT ");
        }
        rs.close();

        try {
            PreparedStatement psf = conn.prepareStatement(
                    "SELECT SEQUENCENAME FROM SYS.SYSSEQUENCES A, SYS.SYSSCHEMAS S"
                    + " WHERE A.SCHEMAID = S.SCHEMAID "
                    + " AND S.SCHEMANAME = ?");
            psf.setString(1, schemaName);
            rs = psf.executeQuery();
            while (rs.next()) {
                s.execute("DROP SEQUENCE " + qualifiedName(schemaName, rs.getString(1)) + " RESTRICT");
            }
            psf.close();
        } catch (SQLException ex) {
            // SQLState 42X05 => Table does not exists => OK, no sequences to drop
            if (!ex.getSQLState().equals("42X05")) {
                throw ex;
            }
        }

        if (!schemaName.equals("APP")) {
            s.executeUpdate("DROP SCHEMA " + quote(schemaName, true) + " RESTRICT");
        }
        s.close();
    }
}
