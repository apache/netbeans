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
package org.netbeans.modules.db.explorer.node;

import java.sql.Types;
import org.netbeans.modules.db.test.DDLTestBase;

/**
 * @author <href="mailto:david@vancouvering.com">David Van Couvering</href>
 */
public class DDLHelperTest extends DDLTestBase {
    public DDLHelperTest(String name) {
        super(name);
    }

    public void testDeleteIndex() throws Exception {
        String tablename = "testIndexDelete";
        String colname = "indexcol";
        String indexname = "indexcol_idx";
        
        createBasicTable(tablename, "id");
        addBasicColumn(tablename, colname, Types.INTEGER, 0);
        
        // Create an index
        createSimpleIndex(tablename, indexname, colname);
        
        DDLHelper.deleteIndex(getSpecification(), getSchema(),
                fixIdentifier(tablename), 
                fixIdentifier(indexname));
        
        assertFalse(indexExists(tablename, indexname));
    }
    
    public void testDeleteTable() throws Exception {
        String tablename = "testDeleteTable";
        
        createBasicTable(tablename, "id");
        assertTrue(tableExists(tablename));
        
        DDLHelper.deleteTable(getSpecification(), getSchema(), fixIdentifier(tablename));
        
        assertFalse(tableExists(tablename));
    }
    
    public void testDeleteView() throws Exception {
        String tablename = "testDeleteViewTable";
        String viewname = "testDeleteView";
        
        createBasicTable(tablename, "id");
        
        createView(viewname, "SELECT * FROM " + tablename);
        assertTrue(viewExists(viewname));
        
        DDLHelper.deleteView(getSpecification(), getSchema(), fixIdentifier(viewname));
        
        assertFalse(viewExists(viewname));
    }
    

}
