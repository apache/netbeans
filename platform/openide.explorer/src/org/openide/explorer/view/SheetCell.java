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
package org.openide.explorer.view;

import java.awt.Color;
import java.beans.*;
import java.util.EventObject;
import java.text.MessageFormat;
import java.awt.Graphics;
import java.awt.Component;
import java.awt.Container;
import java.awt.KeyboardFocusManager;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.accessibility.AccessibleContext;
import javax.accessibility.AccessibleRole;
import javax.swing.plaf.UIResource;
import javax.swing.table.TableModel;
import javax.swing.tree.TreePath;
import org.netbeans.modules.openide.explorer.PropertyPanelBridge;
import org.netbeans.modules.openide.explorer.TTVEnvBridge;
import org.netbeans.swing.etable.ETable;
import org.netbeans.swing.outline.Outline;
import org.netbeans.swing.outline.OutlineModel;

import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.nodes.Node.Property;
import org.openide.explorer.propertysheet.*;

/**
 * TableCellEditor/Renderer implementation. Component returned is the PropertyPanel
 *
 * @author Jan Rojcek
 */
abstract class SheetCell extends AbstractCellEditor implements TableModelListener, PropertyChangeListener, TableCellEditor, TableCellRenderer {

    // Tooltips larger than this are screwed up.
    private static final int MAX_TOOLTIP_LENGTH = 1000;
    
    /* Determines how to paint renderer */
    private Boolean flat;

    public SheetCell() {
        setFlat(false);
    }

    /**
     * Set how to paint renderer.
     * @param f <code>true</code> means flat, <code>false</code> means with button border
     */
    public void setFlat(boolean f) {
        
        Color controlDkShadow = Color.lightGray;
        if (UIManager.getColor ("controlDkShadow") != null) controlDkShadow = UIManager.getColor ("controlDkShadow"); // NOI18N
        Color controlLtHighlight = Color.black;
        if (UIManager.getColor ("controlLtHighlight") != null) controlLtHighlight = UIManager.getColor ("controlLtHighlight"); // NOI18N
        Color buttonFocusColor = Color.blue;
        if (UIManager.getColor ("Button.focus") != null) buttonFocusColor = UIManager.getColor ("Button.focus"); // NOI18N
        
        flat = f ? Boolean.TRUE : Boolean.FALSE;
    }

    //
    // Editor
    //
    
    /** Actually edited node (its property) */
    private Node node;
    /** Edited property */
    private Property prop;

    /** Returns <code>null<code>.
     * @return <code>null</code>
     */        
    public Object getCellEditorValue() {
        return null;
    }

    public abstract Node nodeForRow(int row);
    
    /** Returns editor of property.
     * @param table
     * @param value
     * @param isSelected
     * @param r row
     * @param c column
     * @return <code>PropertyPanel</code>
     */
    public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int r, int c) {
        prop = (Property)value;
        node = nodeForRow(r);
        node.addPropertyChangeListener(this);
        // create property panel
        PropertyPanel propPanel = getEditor(prop, node);

        propPanel.setBackground(table.getSelectionBackground());
        propPanel.setForeground(table.getSelectionForeground());
        //Fix for 35534, text shifts when editing.  Maybe better fix possible
        //in EditablePropertyDisplayer or InplaceEditorFactory.
        propPanel.setBorder(BorderFactory.createMatteBorder(0,1,0,0, 
            table.getSelectionBackground()));
        return propPanel;
    }

    /** Cell should not be selected
     * @param ev event
     * @return <code>false</code>
     */
    @Override
    public boolean shouldSelectCell(EventObject ev) {
        return true;
    }

    /** Return true.
     * @param e event
     * @return <code>true</code>
     */
    @Override
    public boolean isCellEditable(EventObject e) {
        return true;
    }

    /**
     * Detaches listeners.
     * Calls <code>fireEditingStopped</code> and returns true.
     * @return true
     */
    @Override
    public boolean stopCellEditing() {
        if (prop != null)
            detachEditor();
        return super.stopCellEditing();
    }

    /**
     * Detaches listeners.
     * Calls <code>fireEditingCanceled</code>.
     */
    @Override
    public void  cancelCellEditing() { 
        if (prop != null)
            detachEditor();
        super.cancelCellEditing();
    }

    /** Table has changed. If underlied property was switched then cancel editing.
     * @param e event
     */
    public void tableChanged(TableModelEvent e) {
        cancelCellEditing();
    }

    /** Removes listeners and frees resources.
     */
    protected void detachEditor() {
        node.removePropertyChangeListener(this);
        node = null;
        prop = null;
    }

    //
    // Renderer
    //

    /** Default header renderer */
    private TableCellRenderer headerRenderer = (new JTableHeader()).getDefaultRenderer();

    /** Null panel is used if cell value is null */
    private NullPanel nullPanel;
    /** Two-tier cache for property panels 
     * Map<TreeNode, WeakHashMap<Node.Property, Reference<FocusedPropertyPanel>> */
    private Map panelCache = new WeakHashMap(); // weak! #31275

    private FocusedPropertyPanel renderer = null;
    private FocusedPropertyPanel getRenderer (Property p, Node n) {
        TTVEnvBridge bridge = TTVEnvBridge.getInstance(this);
        bridge.setCurrentBeans(new Node[] {n});
        if (renderer == null) {
            renderer = new FocusedPropertyPanel(p, PropertyPanel.PREF_READ_ONLY | PropertyPanel.PREF_TABLEUI);
            renderer.putClientProperty("beanBridgeIdentifier", 
                this); //NOI18N
        }
        renderer.setProperty(p);
        renderer.putClientProperty("flat",Boolean.TRUE);
        return renderer;
    }

    public abstract String getShortDescription(int column);
    
    /** Getter for actual cell renderer.
     * @param table
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     * @return <code>PropertyPanel</code>
     */
    public Component getTableCellRendererComponent(JTable table,
                                                   Object value,
                                                   boolean isSelected,
                                                   boolean hasFocus,
                                                   int row, int column) {

        // Header renderer
        if (row == -1) {
            Component comp = headerRenderer.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, column);
            if (comp instanceof JComponent) {
                String tip = column > 0 ? 
                    getShortDescription(column) :
                    table.getColumnName( 0 );
                ((JComponent) comp).setToolTipText(tip);
            }
            return comp;
        }

        Property property = (Property)value;
        Node n = nodeForRow(row);
        Component focusOwner = KeyboardFocusManager.
                getCurrentKeyboardFocusManager().getFocusOwner();
        boolean tableHasFocus = hasFocus || table == focusOwner ||
            table.isAncestorOf(focusOwner) ||
            (focusOwner instanceof Container &&
             ((Container) focusOwner).isAncestorOf(table));
        if (tableHasFocus && !table.isPaintingForPrint()) {
            boolean rowIsLead =
                (table.getSelectionModel().getLeadSelectionIndex() == row);
            boolean colIsLead =
                (table.getColumnModel().getSelectionModel().getLeadSelectionIndex() == column);

            hasFocus = (rowIsLead && colIsLead);
        } else {
            hasFocus = false;
        }
        Component defaultRendererComponent = table.getDefaultRenderer(
                Object.class).getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
        Color bg = getRealColor(defaultRendererComponent.getBackground());
        Color fg = defaultRendererComponent.getForeground();

        if (property != null) {
            FocusedPropertyPanel propPanel = getRenderer (property, n);
            propPanel.setFocused(hasFocus);
            Object computeTooltip = table.getClientProperty("ComputingTooltip");
            if (Boolean.TRUE.equals(computeTooltip)) {
                String toolT = null;
                PropertyEditor propEd = property.getPropertyEditor();
                Object propertyToolTipShortDescription = table.getClientProperty("PropertyToolTipShortDescription");
                if (Boolean.TRUE.equals(propertyToolTipShortDescription) && property.getShortDescription() != null) {
                    toolT = property.getShortDescription();
                } else if (propEd != null) {
                    try {
                        propEd.setValue(property.getValue());
                        toolT = propEd.getAsText();
                    } catch (Exception ex) {
                        // no problem here - just leave null tooltip
                    }

                }
                if (toolT == null) {
                    Object val = null;
                    try {
                        val = property.getValue();
                    } catch (Exception ex) {
                        // no problem here - just leave null tooltip
                    }
                    if (val != null) {
                        toolT = val.toString();
                    }
                }
                if (toolT != null && (toolT = toolT.trim ()).length () > 0) {
                    int ttl = toolT.length();
                    if (ttl > MAX_TOOLTIP_LENGTH &&
                        !toolT.regionMatches(false, 0, "<html>", 0, 6)) {   // Do not cut HTML tooltips

                        toolT = toolT.substring(0, MAX_TOOLTIP_LENGTH) + "...";
                    }
                    propPanel.setToolTipText (toolT);
                } else {
                    propPanel.setToolTipText (null);
                }
            }
            propPanel.setOpaque(true);
            if (isSelected && !tableHasFocus) {
                propPanel.setBackground(getNoFocusSelectionBackground());
                propPanel.setForeground(getNoFocusSelectionForeground());
            } else {
                propPanel.setBackground(bg);
                propPanel.setForeground(fg);
            }
            if (table instanceof ETable) {
                ETable et = (ETable)table;
                et.setCellBackground(propPanel, isSelected, row, column);
            }
            return propPanel;
        }

        if (nullPanel == null) {
            nullPanel = new NullPanel(n);
            nullPanel.setOpaque(true);
        } else {
            nullPanel.setNode(n);
        }

        if (isSelected && !tableHasFocus) {
            nullPanel.setBackground(getNoFocusSelectionBackground());
            nullPanel.setForeground(getNoFocusSelectionForeground());
        } else {
            nullPanel.setBackground(bg);
            nullPanel.setForeground(fg);
        }

        if (table instanceof ETable) {
            ETable et = (ETable)table;
            et.setCellBackground(nullPanel, isSelected, row, column);
        }
        nullPanel.setFocused (hasFocus);
        return nullPanel;
    }
    
    private Color getRealColor(Color c) {
        if (c instanceof UIResource) {
            // There's a condition on color instanceof UIResource in SynthStyle.
            // This is why if we know which color we want to set, we must assure,
            // that the color is not an instance of UIResource:
            float[] components = c.getComponents(null);
            int cn = components.length - 1;
            float[] colorComponents = new float[cn];
            System.arraycopy(components, 0, colorComponents, 0, cn);
            c = new Color(c.getColorSpace(), colorComponents, components[cn]);
            //bg = new Color(bg.getRGB());
        }
        return c;
    }

    protected PropertyPanel editor=null;
    private PropertyPanel getEditor(Property p, Node n) {
        int prefs = PropertyPanel.PREF_TABLEUI;

        TTVEnvBridge bridge = TTVEnvBridge.getInstance(this);
        //workaround for issue 38132 - use env bridge to pass the 
        //node to propertypanel so it can call PropertyEnv.setBeans()
        //with it.  The sad thing is almost nobody uses PropertyEnv.getBeans(),
        //but we have to do it for all cases.
        bridge.setCurrentBeans(new Node[] {n});
        
        if (editor == null) {
            editor = new PropertyPanel(p, prefs);
            
            editor.putClientProperty("flat", Boolean.TRUE); //NOI18N
            editor.putClientProperty("beanBridgeIdentifier", 
                this); //NOI18N
            
            //Intentionally set the property again so it will look up the
            //bean bridge
            editor.setProperty(p);
            
            return editor;
        }
        
        editor.setProperty(p);
        //Okay, the property panel has already grabbed the beans, clear
        //them so no references are held.
        
        return editor;
    }
    
    private PropertyPanel obtainPanel (Node node, Property prop) {
        return getEditor(prop, node);
    }
    
    private static class NullPanel extends JPanel {
        private WeakReference<Node> weakNode;
        
        NullPanel(Node node) {
            this.weakNode = new WeakReference<Node> (node);
        }
        
        void setNode(Node node) {
            this.weakNode = new WeakReference<Node> (node);
        }

        @Override
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleNullPanel();
            }
            return accessibleContext;
        }
        
        private boolean focused = false;
        public void setFocused (boolean val) {
            focused = val;
        }
        
        @Override
        public void paintComponent (Graphics g) {
            super.paintComponent(g);
            if (focused) {
                Color bdr = UIManager.getColor("Tree.selectionBorderColor"); //NOI18N
                if (bdr == null) {
                    //Button focus color doesn't work on win classic - better to
                    //get the color from a value we know will work - Tim
                    if (getForeground().equals(Color.BLACK)) { //typical
                        bdr = getBackground().darker();
                    } else {
                        bdr = getForeground().darker();
                    }
                }
                g.setColor(bdr);
                g.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
                g.setColor (bdr);
            }
        }
        
        @Override
        public void addComponentListener (java.awt.event.ComponentListener l) {
            //do nothing
        }
        
        @Override
        public void addHierarchyListener (java.awt.event.HierarchyListener l) {
            //do nothing
        }
        
        @Override
        public void repaint() {
            //do nothing
        }
        
        @Override
        public void repaint (int x, int y, int width, int height) {
            //do nothing
        }
        
        @Override
        public void invalidate() {
            //do nothing
        }
        
        @Override
        public void revalidate() {
            //do nothing
        }
        
        @Override
        public void validate() {
            //do nothing
        }
        
        @Override
        public void firePropertyChange (String s, Object a, Object b) {
            //do nothing
        }        

        private class AccessibleNullPanel extends AccessibleJPanel {
            AccessibleNullPanel() {}
            @Override
            public String getAccessibleName() {
                String name = super.getAccessibleName();

                if (name == null) {
                    name = getString("ACS_NullPanel");
                }
                return name;
            }

            @Override
            public String getAccessibleDescription() {
                String description = super.getAccessibleDescription();

                if (description == null) {
                    Node node = weakNode.get ();
                    if (node != null) {
                        description = MessageFormat.format(
                            getString("ACSD_NullPanel"),
                            new Object[] {
                                node.getDisplayName()
                            }
                        );
                    }
                }
                return description;
            }
        }
    }

    /** Table cell renderer component. Paints focus border on property panel. */
    static class FocusedPropertyPanel extends PropertyPanel {
        //XXX delete this class when new property panel is committed
        boolean focused;
        
        public FocusedPropertyPanel(Property p, int preferences) {
            super (p, preferences);
            setDoubleBuffered(true);
        }
        
        public void setFocused(boolean focused) {
            this.focused = focused;
        }

        @Override
        public void addComponentListener (java.awt.event.ComponentListener l) {
            //do nothing
        }
        
        @Override
        public void addHierarchyListener (java.awt.event.HierarchyListener l) {
            //do nothing
        }
        
        @Override
        public void repaint(long tm, int x, int y, int width, int height) {
            //do nothing
        }
        
        @Override
        public void revalidate() {
            //do nothing
        }

        @Override
        public void firePropertyChange (String s, Object a, Object b) {
            //do nothing
            if ("flat".equals (s)) {
                super.firePropertyChange(s,a,b);
            }
        }
        
        @Override
        public boolean isShowing() {
            return true;
        }
        
        @Override
        public void update(Graphics g) {
            //do nothing
        }
        
        @Override
        public void paint(Graphics g) {
            //do this for self-painting editors in Options window - because
            //we've turned off most property changes, the background won't be
            //painted correctly otherwise
            Color c = getBackground();
            Color old = g.getColor();
            g.setColor(c);
            g.fillRect(0,0,getWidth(),getHeight());
            g.setColor(old);
            
            super.paint(g);

            if (focused) {
                Color bdr = UIManager.getColor("Tree.selectionBorderColor"); //NOI18N
                if (bdr == null) {
                    //Button focus color doesn't work on win classic - better to
                    //get the color from a value we know will work - Tim
                    if (getForeground().equals(Color.BLACK)) { //typical
                        bdr = getBackground().darker();
                    } else {
                        bdr = getForeground().darker();
                    }
                }
                g.setColor(bdr);
                g.drawRect(1, 1, getWidth() - 3, getHeight() - 3);
            }
            g.setColor(old);
        }
        
        ////////////////// Accessibility support ///////////////////////////////

        @Override
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleFocusedPropertyPanel();
            }
            return accessibleContext;
        }

        private class AccessibleFocusedPropertyPanel extends AccessibleJComponent {
            AccessibleFocusedPropertyPanel() {}
            @Override
            public AccessibleRole getAccessibleRole() {
                return AccessibleRole.PANEL;
            }

            @Override
            public String getAccessibleName() {
                @SuppressWarnings("deprecation")
                FeatureDescriptor fd = ((ExPropertyModel)getModel()).getFeatureDescriptor();
                @SuppressWarnings("deprecation")
                PropertyEditor editor = getPropertyEditor();
                
                return MessageFormat.format(
                    getString("ACS_PropertyPanelRenderer"),
                    new Object[] {
                        fd.getDisplayName(),
                        (editor == null) ? getString("CTL_No_value") : editor.getAsText()
                    }
                );
            }

            @Override
            public String getAccessibleDescription() {
                @SuppressWarnings("deprecation")
                FeatureDescriptor fd = ((ExPropertyModel)getModel()).getFeatureDescriptor();
                @SuppressWarnings("deprecation")
                Node node = (Node)((ExPropertyModel)getModel()).getBeans()[0];
                Class<?> clazz = getModel().getPropertyType();
                return MessageFormat.format(
                    getString("ACSD_PropertyPanelRenderer"),
                    new Object[] {
                        fd.getShortDescription(),
                        clazz == null ? getString("CTL_No_type") : clazz.getName(), 
                        node.getDisplayName()
                    }
                );
            }
        }
    }
    
    private static String getString(String key) {
        return NbBundle.getMessage(SheetCell.class, key);
    }
    
    private static Color noFocusSelectionBackground=null;
    static Color getNoFocusSelectionBackground() {
        if (noFocusSelectionBackground == null) {
            //allow theme/ui custom definition
            noFocusSelectionBackground = 
                UIManager.getColor("nb.explorer.noFocusSelectionBackground"); //NOI18N
            if (noFocusSelectionBackground == null) {
                //try to get standard shadow color
                noFocusSelectionBackground = UIManager.getColor("controlShadow"); //NOI18N
                if (noFocusSelectionBackground == null) {
                    //Okay, the look and feel doesn't suport it, punt
                    noFocusSelectionBackground = Color.lightGray;
                }
                //Lighten it a bit because disabled text will use controlShadow/
                //gray
                noFocusSelectionBackground = betterBrighter(noFocusSelectionBackground);
            }
        }
        return noFocusSelectionBackground;
    }

    private static Color betterBrighter(Color c) {
        Color bb = c.brighter();
        if (Color.WHITE.equals(bb)) {
            bb = new Color((255 + c.getRed())/2, (255 + c.getGreen())/2, (255 + c.getBlue())/2);
        }
        return bb;
    }
    
    private static Color noFocusSelectionForeground=null;
    static Color getNoFocusSelectionForeground() {
        if (noFocusSelectionForeground == null) {
            //allow theme/ui custom definition
            noFocusSelectionForeground =
            UIManager.getColor("nb.explorer.noFocusSelectionForeground"); //NOI18N
            if (noFocusSelectionForeground == null) {
                //try to get standard shadow color
                noFocusSelectionForeground = UIManager.getColor("textText"); //NOI18N
                if (noFocusSelectionForeground == null) {
                    //Okay, the look and feel doesn't suport it, punt
                    noFocusSelectionForeground = Color.BLACK;
                }
            }
        }
        return noFocusSelectionForeground;
    }

    /**
     *
     */
    static class TableSheetCell extends SheetCell {
        /* Table sheet cell works only with NodeTableModel */
        private NodeTableModel tableModel;
        private ETable table;
        public TableSheetCell(NodeTableModel tableModel, ETable table) {
            this.tableModel = tableModel;
            this.table = table;
        }
        public Node nodeForRow(int row) {
            int r = table.convertRowIndexToModel(row);
            return tableModel.nodeForRow(r);
        }
        public String getShortDescription(int column) {
            return tableModel.propertyForColumn(column).getShortDescription();
        }
        public void propertyChange(PropertyChangeEvent evt) {
    //        stopCellEditing(); //XXX ?
            tableModel.fireTableDataChanged();
        }
        @Override
        protected void detachEditor() {
            super.detachEditor();
            tableModel.removeTableModelListener(this);
        }
        @Override
        public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int r, int c) {
           tableModel.addTableModelListener(this);
           return super.getTableCellEditorComponent(table, value, isSelected, r, c);
        }
    }
    
    public static class OutlineSheetCell extends SheetCell {
        private Outline outline;
        public OutlineSheetCell(Outline outline) {
            this.outline = outline;
        }
        public Node nodeForRow(int row) {
            int r = outline.convertRowIndexToModel(row);
            TreePath tp = outline.getLayoutCache().getPathForRow(r);
            return Visualizer.findNode(tp.getLastPathComponent());
        }
        public String getShortDescription(int column) {
            return outline.getOutlineModel().getColumnName(column);
        }
        public void propertyChange(PropertyChangeEvent evt) {
            // We get this event while editing
            stopCellEditingNoCommit();
            if( SwingUtilities.isEventDispatchThread() ) {
                outline.tableChanged(new TableModelEvent(outline.getModel(), 0, outline.getRowCount()));
            } else {
                SwingUtilities.invokeLater( new Runnable() {
                    public void run() {
                        outline.tableChanged(new TableModelEvent(outline.getModel(), 0, outline.getRowCount()));
                    }
                });
            }
        }
        
        @Override
        public boolean stopCellEditing() {

            PropertyPanelBridge.commit(editor);

            return stopCellEditingNoCommit();
        }

        private boolean stopCellEditingNoCommit() {
            PropertiesRowModel prm = null;
            if (outline instanceof OutlineView.OutlineViewOutline) {
                OutlineView.OutlineViewOutline ovo = (OutlineView.OutlineViewOutline) outline;
                prm = ovo.getRowModel();
            }
            if (prm != null) {
                // See PropertiesRowModel.setValueFor()
                // Intentionally do nothing when the cell editor components are
                // PropertyPanels that will propagate the change into the target
                // property object - no need to do anything in the set value method.
                prm.setIgnoreSetValue(true);
            }
            try {
                return super.stopCellEditing();
            } finally {
                if (prm != null) {
                    prm.setIgnoreSetValue(false);
                }
            }
        }
        @Override
        protected void detachEditor() {
            super.detachEditor();
            TableModel tableModel = outline.getModel();
            tableModel.removeTableModelListener(this);
        }
        @Override
        public Component getTableCellEditorComponent(JTable table,
                                                 Object value,
                                                 boolean isSelected,
                                                 int r, int c) {
           TableModel tableModel = outline.getModel();
           tableModel.addTableModelListener(this);
           return super.getTableCellEditorComponent(table, value, isSelected, r, c);
        }
        
    }
}
