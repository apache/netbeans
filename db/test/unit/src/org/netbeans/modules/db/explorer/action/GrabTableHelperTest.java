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
