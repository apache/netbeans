/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.event.TreeModelListener;
import javax.swing.table.TableModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author martin
 */
public class OutlineTest extends NbTestCase {
    
    public OutlineTest(String testName) {
        super(testName);
    }
    
    @Override
    protected boolean runInEQ () {
        return true;
    }
    
    public void testCheckAt() throws Exception {
        Outline outline = createTestingTable(false);
        CheckRenderDataProvider rdp = new TestCheckRenderDataProvider();
        outline.setRenderDataProvider(rdp);
        boolean checked = outline.checkAt(0, 0, null);
        boolean selected = rdp.isSelected(outline.getValueAt(0, 0));
        assert (checked && selected) : "At (0, 0) checked = "+checked+", selected = "+selected;
        checked = outline.checkAt(0, 1, null);
        assert !checked : "Checked at a non-tree cell";
        checked = outline.checkAt(0, 2, null);
        assert !checked : "Checked at a non-tree cell";
        checked = outline.checkAt(0, 3, null);
        assert !checked : "Checked at a non-tree cell";
        
        checked = outline.checkAt(2, 0, null);
        selected = rdp.isSelected(outline.getValueAt(2, 0));
        assert (checked && selected) : "At (2, 0) checked = "+checked+", selected = "+selected;
        checked = outline.checkAt(2, 0, null);
        selected = rdp.isSelected(outline.getValueAt(2, 0));
        assert (checked && !selected) : "At (2, 0) checked = "+checked+", selected = "+selected;
    }

    /**
     * Create a test ETable instance with some dummy data. BUT please
     * be aware that the tests result depend on this data so if you do
     * any change here make sure you fix all the tests.
     */
    private Outline createTestingTable(final boolean cellsEditable) {
        Outline outline = new Outline();
        final Object[][] values = 
            new Object [][] {
                {"a", "x", "tttttttt", new Integer(5)},
                {"a", "y", "ggggggggg", new Integer(10)},
                {"b", "z", "nnnnnnnn", new Integer(7)},
                {"b", "w", "mmmmmm", new Integer(1)},
                {"c", "m", "kkkkkkkkkk", new Integer(10000)},
                {"c", "n", "kkkkk", new Integer(4)}
            };
        String[] columnNames = new String [] { "AA", "BB", "CC", "DD"};
        TableModel tm = new javax.swing.table.DefaultTableModel(
                values,
                columnNames
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Object.class, java.lang.Object.class, java.lang.Integer.class
            };
            boolean[] canEdit = new boolean [] {
                cellsEditable, cellsEditable, cellsEditable, cellsEditable
            };

            @Override
            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        };
        TreeModel trm = new TreeModel() {

            @Override
            public Object getRoot() {
                return "root";
            }

            @Override
            public Object getChild(Object parent, int index) {
                if ("root".equals(parent)) {
                    return "Node "+values[index][0];
                } else {
                    throw new IllegalStateException("parent = "+parent);
                }
            }

            @Override
            public int getChildCount(Object parent) {
                if ("root".equals(parent)) {
                    return values.length;
                } else {
                    throw new IllegalStateException("parent = "+parent);
                }
            }

            @Override
            public boolean isLeaf(Object node) {
                if ("root".equals(node)) {
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public void valueForPathChanged(TreePath path, Object newValue) {
            }

            @Override
            public int getIndexOfChild(Object parent, Object child) {
                if ("root".equals(parent)) {
                    for (int i = 0; i < values.length; i++) {
                        if (("Node "+values[i][0]).equals(child)) {
                            return i;
                        }
                    }
                    throw new IllegalStateException("Unknown child: "+child);
                } else {
                    throw new IllegalStateException("parent = "+parent);
                }
            }

            @Override
            public void addTreeModelListener(TreeModelListener l) {
            }

            @Override
            public void removeTreeModelListener(TreeModelListener l) {
            }
        };
        outline.setRootVisible(false);
        outline.setModel(new DefaultOutlineModel(trm, tm, false, "test label"));
        return outline;
    }
    
    private static class TestCheckRenderDataProvider implements CheckRenderDataProvider {
        
        private Set<Object> selectedObjects = new HashSet<Object>();
        //private Set<Object> checkable = new HashSet<Object>(Arrays.asList("a", "b", "c"));

        @Override
        public boolean isCheckable(Object o) {
            return true;
        }

        @Override
        public boolean isCheckEnabled(Object o) {
            return o.toString().startsWith("Node ");
        }

        @Override
        public Boolean isSelected(Object o) {
            return selectedObjects.contains(o);
        }

        @Override
        public void setSelected(Object o, Boolean selected) {
            if (selected) {
                selectedObjects.add(o);
            } else {
                selectedObjects.remove(o);
            }
        }

        @Override
        public String getDisplayName(Object o) {
            return "Display: "+o;
        }

        @Override
        public boolean isHtmlDisplayName(Object o) {
            return false;
        }

        @Override
        public Color getBackground(Object o) {
            return null;
        }

        @Override
        public Color getForeground(Object o) {
            return null;
        }

        @Override
        public String getTooltipText(Object o) {
            return null;
        }

        @Override
        public Icon getIcon(Object o) {
            return null;
        }
        
    }
}
