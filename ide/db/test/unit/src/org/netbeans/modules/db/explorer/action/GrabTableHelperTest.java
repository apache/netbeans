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

package org.netbeans.modules.db.explorer.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.sql.Types;
import java.util.Iterator;
import java.util.Vector;
import org.netbeans.lib.ddl.impl.CreateTable;
import org.netbeans.lib.ddl.impl.TableColumn;
import org.netbeans.modules.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.explorer.node.TableNode;
import org.netbeans.modules.db.test.DDLTestBase;

/**
 *
 * @author Rob Englander
 */
public class GrabTableHelperTest extends DDLTestBase {

    public GrabTableHelperTest(String name) {
        super(name);
    }

    public void testGrabTable() throws Exception {
        File file = null;

        try {
            String tablename = "GRABTABLE";
            String pkName = "id";
            String col1 = "col1";
            String col2 = "col2";
            String filename = "grabtable.grab";

            if ( dblocation != null  &&  dblocation.length() > 0 ) {
                filename = dblocation + "/" + filename;
            }

            file = new File(filename);
            if ( file.exists() ) {
                file.delete();
            }

            createBasicTable(tablename, pkName);
            addBasicColumn(tablename, col1, Types.VARCHAR, 255);
            addBasicColumn(tablename, col2, Types.INTEGER, 0);

            // Fix the identifiers based on how the database converts
            // the casing of identifiers.   This is because we still
            // quote existing (versus) new identifiers, so we need to
            // make sure they are quoted correctly.
            String fixedName = fixIdentifier(tablename);
            pkName = fixIdentifier(pkName);
            col1 = fixIdentifier(col1);
            col2 = fixIdentifier(col2);

            TableNode tableNode = getTableNode(tablename);
            assertNotNull(tableNode);


            new GrabTableHelper().execute(tableNode.getLookup().lookup(DatabaseConnection.class),
                    getSpecification(), tableNode.getTableHandle(), file);

            assertTrue(file.exists());

            // Now recreate the table info and make sure it's accurate
            FileInputStream fstream = new FileInputStream(file);
            ObjectInputStream istream = new ObjectInputStream(fstream);
            CreateTable cmd = (CreateTable)istream.readObject();
            istream.close();
            cmd.setSpecification(getSpecification());
            cmd.setObjectOwner(getSchema());

            assertEquals(fixedName, cmd.getObjectName());

            Vector cols = cmd.getColumns();
            assertTrue(cols.size() == 3);

            Iterator it = cols.iterator();

            while ( it.hasNext() ) {
                TableColumn col = (TableColumn)it.next();

                if ( col.getColumnName().equals(pkName)) {
                    assertEquals(col.getColumnType(), Types.INTEGER);
                    assertEquals(col.getObjectType(), TableColumn.PRIMARY_KEY);
                } else if ( col.getColumnName().equals(col1) ) {
                    assertEquals(col.getColumnType(), Types.VARCHAR);
                    assertEquals(col.getColumnSize(), 255);
                } else if ( col.getColumnName().equals(col2) ) {
                    assertEquals(col.getColumnType(), Types.INTEGER);
                } else {
                    fail("Unexpected column with name " + col.getColumnName());
                }
            }

            // OK, now see if we can actually create this guy
            dropTable(tablename);
            cmd.execute();

            assertFalse(cmd.wasException());
            assertTrue(tableExists(fixedName));
        } finally {
            if ( file != null && file.exists()) {
                file.delete();
            }
        }
    }
}
