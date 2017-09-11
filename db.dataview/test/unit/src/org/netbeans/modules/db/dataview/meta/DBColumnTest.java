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

package org.netbeans.modules.db.dataview.meta;

import org.netbeans.junit.NbTestCase;
/**
 *
 * @author jawed
 */
public class DBColumnTest extends NbTestCase {
    
    private DBTable table;
    
    public DBColumnTest(String testName) {
        super(testName);
    }

    public static  org.netbeans.junit.NbTest suite() {
         org.netbeans.junit.NbTestSuite suite = new  org.netbeans.junit.NbTestSuite(DBColumnTest.class);
        return suite;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createTable();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void createTable(){
        String aName = "aName";
        String aSchema = "aSchema";
        String aCatalog = "aCatalog";
        table = new DBTable(aName, aSchema, aCatalog);
    }
//---------- Test Cases ---------------
    
    public void testConstructor() {
        DBColumn col = new DBColumn(table, "colName", 12, "varchar", 10, 5, true, false);
        assertEquals("colName", col.getName());
        assertEquals(12, col.getJdbcType());
        assertEquals(10, col.getScale());
        assertEquals(5, col.getPrecision());
    }
    
    public void testEqualsAndHashCode() {
        DBColumn col1 = new DBColumn(table, "colName", 12,"varchar", 10, 5, true, false);
        DBColumn col2 = new DBColumn(table, "colName", 12,"varchar", 10, 5, true, false);
        DBColumn col3 = new DBColumn(table, "colNameDiff", 12,"varchar", 10, 5, true, false);
        assertEquals(col1,col1);
        assertEquals(col1.getDisplayName(),col2.getDisplayName());
        assertEquals(col2.getName(),col1.getName());
        
        assertTrue(! col1.equals(col3) );
        assertTrue(col1.hashCode() != col3.hashCode() );
        assertTrue(! col1.equals(null) );
        assertTrue(! col1.equals(new Object()) );
    }
}
