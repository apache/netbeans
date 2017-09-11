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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.db.explorer.dlg;

import java.util.Vector;
import org.netbeans.modules.db.test.DDLTestBase;

public class CreateTableDDLTest extends DDLTestBase {

    public CreateTableDDLTest(String name) {
        super(name);
    }

    public void testCreateBasicTable() throws Exception {
        String tablename = "basicTable";
        Vector cols = new Vector();
        Vector pkcols = new Vector();
        
        dropTable(tablename);
        
        ColumnItem col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "id");
        TypeElement type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.PRIMARY_KEY, new Boolean(true));
        cols.add(col);

        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "firstname");
        type = new TypeElement("java.sql.Types.VARCHAR", "VARCHAR");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.SIZE, "255");
        cols.add(col);
        
        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "socsec");
        type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        cols.add(col);
        
        CreateTableDDL ddl = new CreateTableDDL(getSpecification(), getSchema(), tablename);
        
        boolean wasException = ddl.execute(cols, pkcols);
        
        assertFalse(wasException);
        
        assertTrue(tableExists(tablename));
        assertTrue(columnInPrimaryKey(tablename, "id"));
    }
    
    public void testCreateTableWithPrimaryKeys() throws Exception {
        String tablename = "basicTable";
        Vector cols = new Vector();
        Vector pkcols = new Vector();
        
        dropTable(tablename);
        
        ColumnItem col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "id");
        TypeElement type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        cols.add(col);
        pkcols.add(col);

        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "id2");
        type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        cols.add(col);
        pkcols.add(col);
        
        CreateTableDDL ddl = new CreateTableDDL(getSpecification(), getSchema(), tablename);
        
        boolean wasException = ddl.execute(cols, pkcols);
        
        assertFalse(wasException);
        
        assertTrue(tableExists(tablename));
        assertTrue(columnInPrimaryKey(tablename, "id"));
        assertTrue(columnInPrimaryKey(tablename, "id2"));
        
    }
    
    public void testCreateTableWithSecondaryIndex() throws Exception {
        String tablename = "basicTable";
        Vector cols = new Vector();
        Vector pkcols = new Vector();
        
        dropTable(tablename);
        
        ColumnItem col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "id");
        TypeElement type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.PRIMARY_KEY, new Boolean(true));
        cols.add(col);

        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "firstname");
        type = new TypeElement("java.sql.Types.VARCHAR", "VARCHAR");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.SIZE, "255");
        cols.add(col);
        
        col = new ColumnItem();
        col.setProperty(ColumnItem.NAME, "socsec");
        type = new TypeElement("java.sql.Types.INTEGER", "INTEGER");
        col.setProperty(ColumnItem.TYPE, type);
        col.setProperty(ColumnItem.INDEX, new Boolean(true));
        cols.add(col);
        
        CreateTableDDL ddl = new CreateTableDDL(getSpecification(), getSchema(), tablename);
        
        boolean wasException = ddl.execute(cols, pkcols);
        
        assertFalse(wasException);
        
        assertTrue(tableExists(tablename));
        assertTrue(columnInPrimaryKey(tablename, "id"));
        assertTrue(columnInAnyIndex(tablename, "socsec"));
        
    }
}
