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
