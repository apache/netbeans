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
package org.netbeans.swing.outline;

import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author aoanc
 */
public class DefaultOutlineCellRendererTest extends NbTestCase {
    public DefaultOutlineCellRendererTest(String name) {
        super(name);
    }

    @Override
    protected boolean runInEQ() {
        return true;
    }

    /**
     * Test of getTableCellRendererComponent method, of class
     * DefaultOutlineCellRenderer.
     */
    public void testGetTableCellRendererComponent() {
        System.out.println("getTableCellRendererComponent");
        UIManager.put("Table.alternateRowColor", Color.red);
        final DefaultMutableTreeNode node1 = new DefaultMutableTreeNode();
        final DefaultMutableTreeNode node2 = new DefaultMutableTreeNode();
        final Object[][] values = new Object[][]{{node1, "01"}, {node2, "11"}};
        final String[] names = new String[]{"col1", "col2"};
        TreeModel treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
        DefaultTableModel tableModel = new DefaultTableModel(values, names);
        OutlineModel model = new DefaultOutlineModel(treeModel, tableModel, false, "Col1") {
        };
        final Outline outline = new Outline(model);
        ColorRenderDataProvider rdp = new ColorRenderDataProvider();
        outline.setRenderDataProvider(rdp);
        DefaultOutlineCellRenderer instance = new DefaultOutlineCellRenderer();
        Component result = instance.getTableCellRendererComponent(outline, node1, false, false, 0, 0);
        assertEquals("First line backgroundColor defaults to", outline.getBackground(), result.getBackground());
        assertEquals("Foreground defaults to", outline.getForeground(), result.getForeground());
        result = instance.getTableCellRendererComponent(outline, node2, false, false, 1, 0);
        assertEquals("Second line backgroundColor defaults to", Color.red, result.getBackground());
        assertEquals("Foreground defaults to", outline.getForeground(), result.getForeground());
        
        rdp.backgroundColor = Color.BLUE; // Custom background color
        rdp.foregroundColor = Color.GREEN; // Custom foreground color
        
        result = instance.getTableCellRendererComponent(outline, node1, false, false, 0, 0);
        assertEquals("First line backgroundColor is set as", Color.BLUE, result.getBackground());
        assertEquals("Foreground is set as", Color.GREEN, result.getForeground());
        result = instance.getTableCellRendererComponent(outline, node2, false, false, 1, 0);
        assertEquals("Second line backgroundColor is set as", Color.BLUE, result.getBackground());
        assertEquals("Foreground is set as", Color.GREEN, result.getForeground());
    }
    
    private static class ColorRenderDataProvider implements RenderDataProvider {
        
        Color backgroundColor;
        Color foregroundColor;
        
        @Override
        public String getDisplayName(Object o) {
            return "name";
        }

        @Override
        public boolean isHtmlDisplayName(Object o) {
            return false;
        }

        @Override
        public Color getBackground(Object o) {
            return backgroundColor;
        }

        @Override
        public Color getForeground(Object o) {
            return foregroundColor;
        }

        @Override
        public String getTooltipText(Object o) {
            return "hi";
        }

        @Override
        public Icon getIcon(Object o) {
            return null;
        }
        
    }
}
