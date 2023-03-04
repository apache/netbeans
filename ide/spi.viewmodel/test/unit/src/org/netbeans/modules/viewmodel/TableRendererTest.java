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

package org.netbeans.modules.viewmodel;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.event.CellEditorListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.Model;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.Models;
import org.netbeans.spi.viewmodel.TableModel;
import org.netbeans.spi.viewmodel.TableRendererModel;
import org.netbeans.spi.viewmodel.TreeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.datatransfer.PasteType;

/**
 * Test of TableRendererModel.
 *
 * @author Martin Entlicher
 */
public class TableRendererTest extends NbTestCase {

    private OutlineTable ot;

    public TableRendererTest(String name) {
        super(name);
    }


    private void setUpModel() {
        Model mr = new TableRendererModelImpl();

        ArrayList l = new ArrayList ();
        l.add(mr);
        l.add(new ColumnModelImpl("col1"));
        l.add(new ColumnModelImpl("col2"));
        Models.CompoundModel mcm = Models.createCompoundModel(l);
        OutlineTable tt = BasicTest.createView(mcm);

        RequestProcessor rp = tt.currentTreeModelRoot.getRootNode().getRequestProcessor();
        BasicTest.waitFinished (rp);

        this.ot = tt;
        //root = tt.getExplorerManager ().getRootContext ();
    }

    public void testRenderers() {
        setUpModel();
        JTable t = ot.treeTable.getTable();
        ot.revalidate();

        javax.swing.JFrame f = new javax.swing.JFrame();
        f.setLayout(new BorderLayout());
        f.add(ot, BorderLayout.CENTER);
        f.setSize(600, 500);
        f.setVisible(true);
        //while (f.isVisible()) {
            try {
                Thread.sleep(333);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        //}
        
        System.out.println("table rows = "+t.getRowCount());
        TableCellRenderer tcr = t.getCellRenderer(0, 0);
        Component c = tcr.getTableCellRendererComponent(t, null, true, true, 0, 0);
        //System.err.println("c = "+c);
        assertTrue("Renderer component = "+c, c instanceof RendererComponent);
        assertEquals("Renderer of 0:DN", ((RendererComponent) c).getText());

        tcr = t.getCellRenderer(0, 1);
        c = tcr.getTableCellRendererComponent(t, null, true, true, 0, 1);
        assertEquals("Renderer of 0:col1", ((RendererComponent) c).getText());

        tcr = t.getCellRenderer(0, 2);
        c = tcr.getTableCellRendererComponent(t, null, true, true, 0, 2);
        assertEquals("Renderer of 0:col2", ((RendererComponent) c).getText());

        tcr = t.getCellRenderer(1, 0);
        c = tcr.getTableCellRendererComponent(t, null, true, true, 1, 0);
        assertFalse("Renderer component = "+c, c instanceof RendererComponent);

        tcr = t.getCellRenderer(1, 1);
        c = tcr.getTableCellRendererComponent(t, null, true, true, 1, 1);
        assertFalse("Renderer component = "+c, c instanceof RendererComponent);

        tcr = t.getCellRenderer(2, 1);
        c = tcr.getTableCellRendererComponent(t, null, true, true, 2, 1);
        assertEquals("Renderer of 2:col1", ((RendererComponent) c).getText());

        tcr = t.getCellRenderer(6, 0);
        c = tcr.getTableCellRendererComponent(t, null, true, true, 6, 0);
        assertEquals("Renderer of 6:DN", ((RendererComponent) c).getText());

        tcr = t.getCellRenderer(8, 2);
        c = tcr.getTableCellRendererComponent(t, null, true, true, 8, 2);
        assertEquals("Renderer of 8:col2", ((RendererComponent) c).getText());
    }

    public void testEditors() {
        setUpModel();
        JTable t = ot.treeTable.getTable();
        ot.revalidate();

        javax.swing.JFrame f = new javax.swing.JFrame();
        f.setLayout(new BorderLayout());
        f.add(ot, BorderLayout.CENTER);
        f.setSize(600, 500);
        f.setVisible(true);
        //while (f.isVisible()) {
            try {
                Thread.sleep(333);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        //}

        System.out.println("table rows = "+t.getRowCount());
        TableCellEditor tce = t.getCellEditor(0, 0);
        assertTrue(tce+"is not editable.", tce.isCellEditable(getMouseClickAt(t, 0, 0)));
        //assertTrue(t+"is not editable.", t.isCellEditable(0, 0));
        Component c = tce.getTableCellEditorComponent(t, null, true, 0, 0);
        //System.err.println("c = "+c);
        assertTrue("Editor component = "+c, c instanceof EditorComponent);
        assertEquals("Editor of 0:DN", ((EditorComponent) c).getText());

        tce = t.getCellEditor(0, 1);
        assertTrue(tce+"is not editable.", tce.isCellEditable(getMouseClickAt(t, 0, 1)));
        assertTrue(t+"is not editable.", t.isCellEditable(0, 1));
        c = tce.getTableCellEditorComponent(t, null, true, 0, 1);
        assertTrue("Editor component = "+c, c instanceof EditorComponent);
        assertEquals("Editor of 0:col1", ((EditorComponent) c).getText());

        tce = t.getCellEditor(0, 2);
        assertTrue(tce+"is not editable.", tce.isCellEditable(getMouseClickAt(t, 0, 2)));
        assertTrue(t+"is not editable.", t.isCellEditable(0, 2));
        c = tce.getTableCellEditorComponent(t, null, true, 0, 2);
        assertTrue("Editor component = "+c, c instanceof EditorComponent);
        assertEquals("Editor of 0:col2", ((EditorComponent) c).getText());

        tce = t.getCellEditor(1, 0);
        assertFalse(tce+"is editable.", tce.isCellEditable(getMouseClickAt(t, 1, 0)));
        assertFalse(t+"is editable.", t.isCellEditable(1, 0));
        c = tce.getTableCellEditorComponent(t, null, true, 1, 0);
        assertFalse("Editor component = "+c, c instanceof EditorComponent);

        tce = t.getCellEditor(1, 2);
        assertFalse(tce+"is editable.", tce.isCellEditable(getMouseClickAt(t, 1, 2)));
        assertFalse(t+"is editable.", t.isCellEditable(1, 2));
        c = tce.getTableCellEditorComponent(t, null, true, 1, 2);
        assertFalse("Editor component = "+c, c instanceof EditorComponent);

        tce = t.getCellEditor(3, 1);
        assertTrue(tce+"is not editable.", tce.isCellEditable(getMouseClickAt(t, 3, 1)));
        assertTrue(t+"is not editable.", t.isCellEditable(3, 1));
        c = tce.getTableCellEditorComponent(t, null, true, 3, 1);
        assertTrue("Editor component = "+c, c instanceof EditorComponent);
        assertEquals("Editor of 3:col1", ((EditorComponent) c).getText());

        tce = t.getCellEditor(6, 0);
        assertTrue(tce+"is not editable.", tce.isCellEditable(getMouseClickAt(t, 6, 0)));
        assertTrue(t+"is not editable.", t.isCellEditable(6, 0));
        c = tce.getTableCellEditorComponent(t, null, true, 6, 0);
        assertTrue("Editor component = "+c, c instanceof EditorComponent);
        assertEquals("Editor of 6:DN", ((EditorComponent) c).getText());

        tce = t.getCellEditor(9, 2);
        assertTrue(tce+"is not editable.", tce.isCellEditable(getMouseClickAt(t, 9, 2)));
        assertTrue(t+"is not editable.", t.isCellEditable(9, 2));
        c = tce.getTableCellEditorComponent(t, null, true, 9, 2);
        assertTrue("Editor component = "+c, c instanceof EditorComponent);
        assertEquals("Editor of 9:col2", ((EditorComponent) c).getText());
    }

    public void testTooltip() {
        setUpModel();
        JTable t = ot.treeTable.getTable();
        ot.revalidate();

        javax.swing.JFrame f = new javax.swing.JFrame();
        f.setLayout(new BorderLayout());
        f.add(ot, BorderLayout.CENTER);
        f.setSize(600, 500);
        f.setVisible(true);
        //while (f.isVisible()) {
            try {
                Thread.sleep(333);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        //}

        MouseEvent event = getMouseClickAt(t, 0, 0);
        String tipText = t.getToolTipText(event);
        JToolTip tip = t.createToolTip();
        tip.setTipText(tipText);
        assertTrue("Bad ToolTip class: "+tip, tip instanceof RendererComponent.ToolTipComponent);
        assertEquals("ToolTip for Renderer of 0:DN", tip.getTipText());

        event = getMouseClickAt(t, 1, 1);
        tipText = t.getToolTipText(event);
        tip = t.createToolTip();
        tip.setTipText(tipText);
        assertFalse("Bad ToolTip class: "+tip, tip instanceof RendererComponent.ToolTipComponent);

        event = getMouseClickAt(t, 2, 2);
        tipText = t.getToolTipText(event);
        tip = t.createToolTip();
        tip.setTipText(tipText);
        assertTrue("Bad ToolTip class: "+tip, tip instanceof RendererComponent.ToolTipComponent);
        assertEquals("ToolTip for Renderer of 2:col2", tip.getTipText());
    }

    private MouseEvent getMouseClickAt(JTable t, int row, int col) {
        Point p = t.getCellRect(row, col, false).getLocation();
        MouseEvent me = new MouseEvent(t, 1000, System.currentTimeMillis(), 0, p.x, p.y, 1, false);
        return me;
    }

    private static class RendererComponent extends JLabel {

        private String s;

        public RendererComponent(String s) {
            super(s);
            this.s = s;
        }

        @Override
        public String getToolTipText() {
            return "ToolTip for "+s;
        }

        @Override
        public JToolTip createToolTip() {
            return new ToolTipComponent();
        }

        private static class ToolTipComponent extends JToolTip {
            
        }

    }

    private static final class CellRendererImpl implements TableCellRenderer {

        private Object node;
        private String columnID;

        public CellRendererImpl(Object node, String columnID) {
            this.node = node;
            this.columnID = columnID;
        }

        public Object getNode() {
            return node;
        }

        public String getColumnID() {
            return columnID;
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return new RendererComponent("Renderer of "+node.toString()+":"+columnID);
        }
        
    }

    private static class EditorComponent extends JTextField {
        public EditorComponent(String s) {
            super(s);
        }
    }

    private static final class CellEditorImpl implements TableCellEditor {

        private Object node;
        private String columnID;

        public CellEditorImpl(Object node, String columnID) {
            this.node = node;
            this.columnID = columnID;
        }

        public Object getNode() {
            return node;
        }

        public String getColumnID() {
            return columnID;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return new EditorComponent("Editor of "+node.toString()+":"+columnID);
        }

        @Override
        public Object getCellEditorValue() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            if (anEvent.getSource() instanceof JTable) {
                JTable table = (JTable) anEvent.getSource();
                if (anEvent instanceof MouseEvent) {
                    MouseEvent event = (MouseEvent) anEvent;
                    Point p = event.getPoint();
                    int row = table.rowAtPoint(p);
                    int col = table.columnAtPoint(p);
                    Rectangle rect = table.getCellRect(row, col, true);
                    p.translate(-rect.x, -rect.y);
                    System.out.println("isCellEditable("+anEvent+")");
                    System.out.println("Point "+p+"in rectangle "+rect);
                    if (p.x > rect.width - 24) {
                        // last 24 points not editable
                        return false;
                    }
                }
            }
            return true;
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        @Override
        public boolean stopCellEditing() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void cancelCellEditing() {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void addCellEditorListener(CellEditorListener l) {
        }

        @Override
        public void removeCellEditorListener(CellEditorListener l) {
        }
    }

    private static final class TableRendererModelImpl implements TableRendererModel, TreeModel, TableModel, ExtendedNodeModel {

        private List<ModelListener> listeners = new ArrayList<ModelListener>();

        // TableRendererModel
        
        @Override
        public boolean canRenderCell(Object node, String columnID) throws UnknownTypeException {
            return Integer.parseInt((String) node) % 2 == 0; // Use this renderer for even rows only.
        }

        @Override
        public TableCellRenderer getCellRenderer(Object node, String columnID) throws UnknownTypeException {
            if (!(Integer.parseInt((String) node) % 2 == 0)) {
                throw new IllegalStateException("Trying to get renderer even if we can not provide it node = "+node);
            }
            return new CellRendererImpl(node, columnID);
        }

        @Override
        public boolean canEditCell(Object node, String columnID) throws UnknownTypeException {
            return Integer.parseInt((String) node) % 3 == 0; // Use this renderer for every third row only.
        }

        @Override
        public TableCellEditor getCellEditor(Object node, String columnID) throws UnknownTypeException {
            if (!(Integer.parseInt((String) node) % 3 == 0)) {
                throw new IllegalStateException("Trying to get editor even if we can not provide it node = "+node);
            }
            return new CellEditorImpl(node, columnID);
        }

        @Override
        public void addModelListener(ModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeModelListener(ModelListener l) {
            listeners.remove(l);
        }

        // TreeModel

        @Override
        public Object getRoot() {
            return ROOT;
        }

        @Override
        public Object[] getChildren(Object parent, int from, int to) throws UnknownTypeException {
            if (ROOT.equals(parent)) {
                return new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9" };
            }
            throw new UnknownTypeException(parent);
        }

        @Override
        public boolean isLeaf(Object node) throws UnknownTypeException {
            return !ROOT.equals(node);
        }

        @Override
        public int getChildrenCount(Object node) throws UnknownTypeException {
            return Integer.MAX_VALUE;
        }

        // TableModel

        @Override
        public Object getValueAt(Object node, String columnID) throws UnknownTypeException {
            return ((String) node) + " " + columnID;
        }

        @Override
        public boolean isReadOnly(Object node, String columnID) throws UnknownTypeException {
            return Integer.parseInt((String) node) % 2 == 0;
        }

        @Override
        public void setValueAt(Object node, String columnID, Object value) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getDisplayName(Object node) throws UnknownTypeException {
            return node.toString();
        }

        @Override
        public String getIconBase(Object node) throws UnknownTypeException {
            return null;
        }

        @Override
        public String getShortDescription(Object node) throws UnknownTypeException {
            return "Short Description of "+node;
        }

        @Override
        public boolean canRename(Object node) throws UnknownTypeException {
            return Integer.parseInt((String) node) % 3 == 0;
        }

        @Override
        public boolean canCopy(Object node) throws UnknownTypeException {
            return false;
        }

        @Override
        public boolean canCut(Object node) throws UnknownTypeException {
            return false;
        }

        @Override
        public Transferable clipboardCopy(Object node) throws IOException, UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Transferable clipboardCut(Object node) throws IOException, UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public PasteType[] getPasteTypes(Object node, Transferable t) throws UnknownTypeException {
            return new PasteType[] {};
        }

        @Override
        public void setName(Object node, String name) throws UnknownTypeException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public String getIconBaseWithExtension(Object node) throws UnknownTypeException {
            return null;
        }
        
    }

    private static class ColumnModelImpl extends ColumnModel {

        private String id;

        ColumnModelImpl(String id) {
            this.id = id;
        }

        @Override
        public Class getType() {
            return String.class;
        }

        @Override
        public String getDisplayName() {
            return "Test Column "+id;
        }

        @Override
        public String getID() {
            return id;
        }

    }
}
