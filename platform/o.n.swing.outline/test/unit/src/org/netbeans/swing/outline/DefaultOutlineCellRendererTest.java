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
