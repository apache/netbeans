/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.explorer;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netbeans.lib.ddl.impl.DriverSpecification;
import org.netbeans.modules.db.test.DatabaseMetaDataAdapter;
import org.netbeans.modules.db.test.ResultSetAdapter;

/**
 *
 * @author jhavlin
 */
public class DatabaseConnectorTest {

    public DatabaseConnectorTest() {
    }

    /**
     * Set schema that does not exist. Test for bug 75595.
     */
    @Test
    public void testSetSchemaThatDoesNotExist() {
        DriverSpecification ds = new DriverSpecification(null);
        ds.setMetaData(new CustomMetaData("A", "B", "C"));
        ds.setSchema("D");
        assertNull(ds.getSchema());
    }

    /**
     * Set schema that exists. Test for bug 75595.
     */
    @Test
    public void testSetSchemaThatExists() {
        DriverSpecification ds = new DriverSpecification(null);
        ds.setMetaData(new CustomMetaData("A", "B", "C"));
        ds.setSchema("B");
        assertEquals("B", ds.getSchema());
    }

    /**
     * Set schema when no database meta data are available. Test for bug 75595.
     */
    @Test
    public void testSetSchemaWithNoMetaData() {
        DriverSpecification ds = new DriverSpecification(null);
        ds.setMetaData(null);
        ds.setSchema("ANY");
        assertEquals("ANY", ds.getSchema());
    }

    private static class CustomMetaData extends DatabaseMetaDataAdapter {

        String[] schemas;

        public CustomMetaData(String... schemas) {
            this.schemas = schemas;
        }

        @Override
        public ResultSet getSchemas() throws SQLException {
            return new CustomResultSet(schemas);
        }
    }

    private static class CustomResultSet extends ResultSetAdapter {

        String[] schemas;
        int currentRow = 0;

        public CustomResultSet(String[] schemas) {
            this.schemas = schemas;
        }

        @Override
        public String getString(int columnIndex) throws SQLException {
            assert columnIndex == 1;
            return schemas[currentRow - 1];
        }

        @Override
        public boolean next() throws SQLException {
            if (currentRow < schemas.length) {
                currentRow++;
                return true;
            } else {
                return false;
            }
        }
    }
}