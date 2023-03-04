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
package org.netbeans.swing.etable;
import java.awt.Component;
import java.util.Properties;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.netbeans.junit.NbTestCase;

/**
 * Tests for ETableColumn class.
 * @author David Strupl
 */
public class ETableColumnTest extends NbTestCase {
    
    public ETableColumnTest(String testName) {
        super(testName);
    }

    @Override
    protected boolean runInEQ () {
        return true;
    }

    /**
     * Test of setSorted method, of class org.netbeans.swing.etable.ETableColumn.
     */
    public void testSetSorted() {
        System.out.println("testSetSorted");
        ETableColumn etc = new ETableColumn(2, null);
        etc.setSorted(2, true);
        
        assertEquals(2, etc.getSortRank());
        assertTrue(etc.isSorted());
        assertTrue(etc.isAscending());
    }

    /**
     * Test of setAscending method, of class org.netbeans.swing.etable.ETableColumn.
     */
    public void testSetAscending() {
        System.out.println("testSetAscending");
        ETableColumn etc = new ETableColumn(2, null);
//        Comparator c = new Comparator() {
//            public int compare(Object a1, Object a2) {
//                return 0;
//            }
//        };
        etc.setSorted(2, true);
        etc.setAscending(false);
        
        assertTrue(etc.getComparator() instanceof ETableColumn.FlippingComparator);
        etc.setAscending(true);
        assertFalse(etc.getComparator() instanceof ETableColumn.FlippingComparator);
    }

    /**
     * Test of setHeaderRenderer method, of class org.netbeans.swing.etable.ETableColumn.
     */
    public void testSetHeaderRenderer() {
        System.out.println("testSetHeaderRenderer");
        TableCellRenderer tcr = new TableCellRenderer() {
            public Component getTableCellRendererComponent(JTable table, Object value,
					    boolean isSelected, boolean hasFocus, 
					    int row, int column) {
                return null;
            }
        };
        ETableColumn etc = new ETableColumn(0, null);
        etc.setHeaderRenderer(tcr);
        assertEquals("Externally set headerRenderer should be returned, ", tcr, etc.getHeaderRenderer());
    }

    /**
     * Test of getHeaderRenderer method, of class org.netbeans.swing.etable.ETableColumn.
     * Changed in JDK 1.3.
    public void testGetHeaderRenderer() {
        System.out.println("testGetHeaderRenderer");
        ETableColumn etc = new ETableColumn(0, null);
        TableCellRenderer tcr1 = etc.createDefaultHeaderRenderer();
        TableCellRenderer tcr2 = etc.getHeaderRenderer();
        assertEquals("createDefaultHeaderRenderer and getHeaderRenderer should return the same object, ", tcr1, tcr2);
    }
    */

    /**
     * Test of readSettings and writeSettings methods, of class org.netbeans.swing.etable.ETableColumn.
     */
    public void testReadWriteSettings() {
        System.out.println("testReadWriteSettings");
        ETableColumn etc1 = new ETableColumn(1, 90, null);
        etc1.setWidth(100);
        etc1.setSorted(3, true);
        Properties p = new Properties();
        etc1.writeSettings(p, 1, "test");
        
        ETableColumn etc2 = new ETableColumn(null);
        etc2.readSettings(p, 1, "test");
        
        assertEquals(3, etc2.getSortRank());
        assertTrue(etc2.isSorted());
        assertTrue(etc2.isAscending());
        assertEquals(etc1.getWidth(), etc2.getWidth());
        assertEquals(etc1.getPreferredWidth(), etc2.getPreferredWidth());
    }

    /**
     * Test of compareTo method, of class org.netbeans.swing.etable.ETableColumn.
     */
    public void testCompareTo() {
        System.out.println("testCompareTo");
        ETableColumn etc1 = new ETableColumn(1, null);
        ETableColumn etc2 = new ETableColumn(2, null);
        assertTrue(etc1.compareTo(etc2) < 0);
    }
}
