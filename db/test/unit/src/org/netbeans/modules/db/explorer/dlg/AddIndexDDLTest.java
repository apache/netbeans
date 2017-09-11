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

import java.sql.Types;
import java.util.HashSet;
import org.netbeans.modules.db.test.DDLTestBase;

/**
 *
 * @author David
 */
public class AddIndexDDLTest extends DDLTestBase {

    public AddIndexDDLTest(String name) {
        super(name);
    }
    
    public void testAddIndex() throws Exception {
        String tablename = "mytable";
        String pkname = "id";
        String colname = "col";
        String ixname = "col_ix";
        
        createBasicTable(tablename, pkname);
        addBasicColumn(tablename, colname, Types.VARCHAR, 255);
        
        AddIndexDDL ddl = new AddIndexDDL(getSpecification(), getSchema(),
                fixIdentifier(tablename));
        
        HashSet cols = new HashSet();
        cols.add(fixIdentifier(colname));
        
        boolean wasException = ddl.execute(ixname, false, cols);
        
        assertFalse(wasException);
        assertTrue(columnInIndex(tablename, colname, ixname));
        
        colname = "col2";
        ixname = "col2_ix";
        addBasicColumn(tablename, colname, Types.VARCHAR, 255);
        
        cols.clear();
        cols.add(fixIdentifier(colname));
        wasException = ddl.execute(ixname, true, cols);
        assertFalse(wasException);
        assertTrue(columnInIndex(tablename, colname, ixname));
        assertTrue(indexIsUnique(tablename, ixname));
    }

}
