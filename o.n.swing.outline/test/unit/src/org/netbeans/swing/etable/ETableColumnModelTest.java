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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2009 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.swing.etable;

import java.util.Arrays;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.util.Properties;
import javax.swing.table.TableColumn;
import org.netbeans.junit.NbTestCase;

/**
 * Tests for class ETableColumnModel.
 * @author David Strupl
 */
public class ETableColumnModelTest extends NbTestCase {
    
    public ETableColumnModelTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ () {
        return true;
    }

    /**
     * Test of readSettings, writeSettings methods, of class org.netbeans.swing.etable.ETableColumnModel.
     */
    public void testReadWriteSettings() {
        ETable et = new ETable();
        System.out.println("testReadWriteSettings");
        ETableColumnModel etcm = new ETableColumnModel();
        ETableColumn etc1 = new ETableColumn(0, et);
        etcm.addColumn(etc1);
        ETableColumn etc2 = new ETableColumn(1, et);
        etcm.addColumn(etc2);
        ETableColumn etc3 = new ETableColumn(2, et);
        etcm.addColumn(etc3);
        etcm.setColumnHidden(etc3, true);
        Properties p = new Properties();
        
        etcm.writeSettings(p, "test");
        ETableColumnModel etcm2 = new ETableColumnModel();
        etcm2.readSettings(p, "test", et);
        
        assertEquals("Should restore 2 columns", 2, etcm2.getColumnCount());
        assertEquals("One hidden column", 1, etcm2.hiddenColumns.size());
    }

    /**
     * Test of getComparator method, of class org.netbeans.swing.etable.ETableColumnModel.
     */
    public void testGetComparator() {
        System.out.println("testGetComparator");
        ETableColumnModel etcm = new ETableColumnModel();
        assertTrue(etcm.getComparator() instanceof ETable.OriginalRowComparator);
        TableModel tm = new DefaultTableModel(new Object[][] {{"b"},{"a"}}, new Object[] {"a", "b"}); 
        ETable table = new ETable(tm);
        ETable.RowMapping rm1 = new ETable.RowMapping(0, tm, table);
        ETable.RowMapping rm2 = new ETable.RowMapping(1, tm, table);
        assertTrue("Without sort use index of rows, ", etcm.getComparator().compare(rm1, rm2) < 0);
        
        ETableColumn etc = new ETableColumn(0, new ETable());
        etcm.addColumn(etc);
        etcm.toggleSortedColumn(etc, true);
        assertTrue("Sorting according to data model failed, ", etcm.getComparator().compare(rm1, rm2) > 0);
    }

    /**
     * Test of toggleSortedColumn method, of class org.netbeans.swing.etable.ETableColumnModel.
     */
    public void testToggleSortedColumn() {
        System.out.println("testToggleSortedColumn");
        ETableColumnModel etcm = new ETableColumnModel();
        ETableColumn etc = new ETableColumn(0, null);
        etcm.addColumn(etc);
        
        etcm.toggleSortedColumn(etc, true);
        assertTrue(etcm.sortedColumns.contains(etc));
        assertTrue(etc.isAscending());
        assertTrue(etc.isSorted());
        
        etcm.toggleSortedColumn(etc, true);
        assertTrue(etcm.sortedColumns.contains(etc));
        assertFalse(etc.isAscending());
        assertTrue(etc.isSorted());
        
        etcm.toggleSortedColumn(etc, true);
        assertFalse(etcm.sortedColumns.contains(etc));
        assertFalse(etc.isSorted());
    }

    /**
     * Test of setColumnHidden method, of class org.netbeans.swing.etable.ETableColumnModel.
     */
    public void testSetColumnHidden() {
        System.out.println("testSetColumnHidden");
        ETableColumnModel etcm = new ETableColumnModel();
        ETableColumn etc = new ETableColumn(0, null);
        etcm.addColumn(etc);
        
        etcm.setColumnHidden(etc, true);
        assertTrue(etcm.hiddenColumns.contains(etc));
        assertTrue(etcm.getColumnCount() == 0);
        assertTrue(etcm.isColumnHidden(etc));
        
        etcm.setColumnHidden(etc, false);
        assertFalse(etcm.hiddenColumns.contains(etc));
        assertTrue(etcm.getColumnCount() == 1);
        assertFalse(etcm.isColumnHidden(etc));
    }
    
    public void testHiddenColumnOrder() {
        ETableColumnModel etcm = new ETableColumnModel();
        ETableColumn etca = new ETableColumn(0, null);
        etca.setHeaderValue("a");
        etcm.addColumn(etca);
        ETableColumn etcb = new ETableColumn(1, null);
        etcb.setHeaderValue("b");
        etcm.addColumn(etcb);
        ETableColumn etcc = new ETableColumn(2, null);
        etcc.setHeaderValue("c");
        etcm.addColumn(etcc);
        ETableColumn etcd = new ETableColumn(3, null);
        etcd.setHeaderValue("d");
        etcm.addColumn(etcd);
        ETableColumn etce = new ETableColumn(4, null);
        etce.setHeaderValue("e");
        etcm.addColumn(etce);
        
        checkColumnOrder(etcm, "abcde");
        checkHiddenColumns(etcm, "", new int[] {});
        
        // Make b, c, d hidden:
        etcm.setColumnHidden(etcb, true);
        checkColumnOrder(etcm, "abcde");
        etcm.setColumnHidden(etcc, true);
        checkColumnOrder(etcm, "abcde");
        etcm.setColumnHidden(etcd, true);
        checkColumnOrder(etcm, "abcde");
        checkHiddenColumns(etcm, "bcd", new int[] {1, 2, 3});
        // Unhide d:
        etcm.setColumnHidden(etcd, false);
        checkColumnOrder(etcm, "abcde");
        checkHiddenColumns(etcm, "bc", new int[] {1, 2});
        // Move a after e:
        etcm.moveColumn(0, 2);
        checkColumnOrder(etcm, "bcdea");
        checkHiddenColumns(etcm, "bc", new int[] {0, 1});
        // Unhide b:
        etcm.setColumnHidden(etcb, false);
        checkColumnOrder(etcm, "bcdea");
        checkHiddenColumns(etcm, "c", new int[] {1});
        // Hide a:
        etcm.setColumnHidden(etca, true);
        checkColumnOrder(etcm, "bcdea");
        checkHiddenColumns(etcm, "ca", new int[] {1, 4});
        // Move d before b:
        etcm.moveColumn(1, 0);
        checkColumnOrder(etcm, "dbcea");
        checkHiddenColumns(etcm, "ca", new int[] {2, 4});
        
        // Reduce the number of columns:
        etcm.removeColumn(etce);
        checkColumnOrder(etcm, "dbca");
        checkHiddenColumns(etcm, "ca", new int[] {2, 3});
        etcm.removeColumn(etcd);
        checkColumnOrder(etcm, "bca");
        checkHiddenColumns(etcm, "ca", new int[] {1, 2});
        // Unhide a:
        etcm.setColumnHidden(etca, false);
        checkColumnOrder(etcm, "bca");
        checkHiddenColumns(etcm, "c", new int[] {1});
        // Move a before b:
        etcm.moveColumn(1, 0);
        checkColumnOrder(etcm, "abc");
        checkHiddenColumns(etcm, "c", new int[] {2});
        // Hide b instead of c:
        etcm.setColumnHidden(etcb, true);
        checkColumnOrder(etcm, "abc");
        checkHiddenColumns(etcm, "cb", new int[] {2, 1});
        etcm.setColumnHidden(etcc, false);
        checkColumnOrder(etcm, "abc");
        checkHiddenColumns(etcm, "b", new int[] {1});
        // Move a after c:
        etcm.moveColumn(0, 1);
        checkColumnOrder(etcm, "bca");
        checkHiddenColumns(etcm, "b", new int[] {0});
    }

    private static void checkColumnOrder(ETableColumnModel etcm, String names) {
        List<TableColumn> allColumns = etcm.getAllColumns();
        StringBuilder sb = new StringBuilder();
        for (TableColumn c : allColumns) {
            sb.append(c.getHeaderValue().toString());
        }
        assertEquals("All column names:", names, sb.toString());
    }
    
    private void checkHiddenColumns(ETableColumnModel etcm, String names, int[] indexes) {
        int n = indexes.length;
        assertEquals("Hidden columns size: ", n, etcm.hiddenColumns.size());
        assertEquals("Hidden columns positions size: ", n, etcm.hiddenColumnsPosition.size());
        StringBuilder sb = new StringBuilder();
        for (TableColumn c : etcm.hiddenColumns) {
            sb.append(c.getHeaderValue().toString());
        }
        assertEquals("Hidden column names:", names, sb.toString());
        int[] positions = new int[n];
        for (int i = 0; i < n; i++) {
            positions[i] = etcm.hiddenColumnsPosition.get(i);
        }
        assertEquals("Hidden columns indexes are wrong:", Arrays.toString(indexes), Arrays.toString(positions));
    }
    
    /**
     * Test of clearSortedColumns method, of class org.netbeans.swing.etable.ETableColumnModel.
     */
    public void testClearSortedColumns() {
        System.out.println("testClearSortedColumns");
        ETableColumnModel etcm = new ETableColumnModel();
        ETableColumn etc = new ETableColumn(0, null);
        etcm.addColumn(etc);
        etcm.toggleSortedColumn(etc, true);
        
        etcm.clearSortedColumns();
        assertFalse(etcm.sortedColumns.contains(etc));
    }
}
