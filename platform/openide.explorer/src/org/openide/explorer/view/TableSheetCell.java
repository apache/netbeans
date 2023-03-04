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
package org.openide.explorer.view;

import java.lang.ref.Reference;
import org.openide.explorer.propertysheet.*;
import org.openide.nodes.Node;

import org.openide.nodes.Node.Property;
import org.openide.util.NbBundle;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.KeyboardFocusManager;

import java.beans.*;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;

import java.text.MessageFormat;

import java.util.EventObject;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.accessibility.AccessibleContext;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.*;

import org.netbeans.modules.openide.explorer.TTVEnvBridge;
import org.openide.explorer.view.SheetCell.FocusedPropertyPanel;


/**
 * TableCellEditor/Renderer implementation. Component returned is the PropertyPanel
 *
 * @author Jan Rojcek
 */
class TableSheetCell extends AbstractCellEditor implements TableModelListener, PropertyChangeListener, TableCellEditor,
    TableCellRenderer {
    /* Table sheet cell works only with NodeTableModel */
    private NodeTableModel tableModel;

    /* Determines how to paint renderer */
    private Boolean flat;

    //
    // Editor
    //

    /** Actually edited node (its property) */
    private Node node;

    /** Edited property */
    private Property prop;

    //
    // Renderer
    //

    /** Default header renderer */
    private TableCellRenderer headerRenderer = new JTable().getTableHeader().getDefaultRenderer();

    /** Null panel is used if cell value is null */
    private NullPanel nullPanel;

    /** Two-tier cache for property panels
     * Map<TreeNode, WeakHashMap<Node.Property, Reference<FocusedPropertyPanel>> */
    private Map panelCache = new WeakHashMap(); // weak! #31275
    private FocusedPropertyPanel renderer = null;
    private PropertyPanel editor = null;

    public TableSheetCell(NodeTableModel tableModel) {
        this.tableModel = tableModel;
        setFlat(false);
    }

    /**
     * Set how to paint renderer.
     * @param f <code>true</code> means flat, <code>false</code> means with button border
     */
    public void setFlat(boolean f) {
        Color controlDkShadow = Color.lightGray;

        if (UIManager.getColor("controlDkShadow") != null) {
            controlDkShadow = UIManager.getColor("controlDkShadow"); // NOI18N
        }

        Color controlLtHighlight = Color.black;

        if (UIManager.getColor("controlLtHighlight") != null) {
            controlLtHighlight = UIManager.getColor("controlLtHighlight"); // NOI18N
        }

        Color buttonFocusColor = Color.blue;

        if (UIManager.getColor("Button.focus") != null) {
            buttonFocusColor = UIManager.getColor("Button.focus"); // NOI18N
        }

        flat = f ? Boolean.TRUE : Boolean.FALSE;
    }

    /** Returns <code>null<code>.
     * @return <code>null</code>
     */
    public Object getCellEditorValue() {
        return null;
    }

    /** Returns editor of property.
     * @param table
     * @param value
     * @param isSelected
     * @param r row
     * @param c column
     * @return <code>PropertyPanel</code>
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int r, int c) {
        prop = (Property) value;
        node = tableModel.nodeForRow(r);
        node.addPropertyChangeListener(this);
        tableModel.addTableModelListener(this);

        // create property panel
        PropertyPanel propPanel = getEditor(prop, node);

        propPanel.setBackground(table.getSelectionBackground());
        propPanel.setForeground(table.getSelectionForeground());

        //Fix for 35534, text shifts when editing.  Maybe better fix possible
        //in EditablePropertyDisplayer or InplaceEditorFactory.
        propPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, table.getSelectionBackground()));

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

    /** Forwards node property change to property model
     * @param evt event
     */
    public void propertyChange(PropertyChangeEvent evt) {
        //        stopCellEditing(); //XXX ?
        tableModel.fireTableDataChanged();
    }

    /**
     * Detaches listeners.
     * Calls <code>fireEditingStopped</code> and returns true.
     * @return true
     */
    @Override
    public boolean stopCellEditing() {
        if (prop != null) {
            detachEditor();
        }

        return super.stopCellEditing();
    }

    /**
     * Detaches listeners.
     * Calls <code>fireEditingCanceled</code>.
     */
    @Override
    public void cancelCellEditing() {
        if (prop != null) {
            detachEditor();
        }

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
    private void detachEditor() {
        node.removePropertyChangeListener(this);
        tableModel.removeTableModelListener(this);
        node = null;
        prop = null;
    }

    private FocusedPropertyPanel getRenderer(Property p, Node n) {
        TTVEnvBridge bridge = TTVEnvBridge.getInstance(this);
        bridge.setCurrentBeans(new Node[] { n });

        if (renderer == null) {
            renderer = new FocusedPropertyPanel(p, PropertyPanel.PREF_READ_ONLY | PropertyPanel.PREF_TABLEUI);
            renderer.putClientProperty("beanBridgeIdentifier", this); //NOI18N
        }

        renderer.setProperty(p);
        renderer.putClientProperty("flat", Boolean.TRUE);

        return renderer;
    }

    /** Getter for actual cell renderer.
     * @param table
     * @param value
     * @param isSelected
     * @param hasFocus
     * @param row
     * @param column
     * @return <code>PropertyPanel</code>
     */
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
    ) {
        // Header renderer
        if (row == -1) {
            Component comp = headerRenderer.getTableCellRendererComponent(
                    table, value, isSelected, hasFocus, row, column
                );

            if (comp instanceof JComponent) {
                String tip = (column > 0) ? tableModel.propertyForColumn(column).getShortDescription()
                                          : table.getColumnName(0);
                ((JComponent) comp).setToolTipText(tip);
            }

            return comp;
        }

        Property property = (Property) value;
        Node n = tableModel.nodeForRow(row);

        if (property != null) {
            FocusedPropertyPanel propPanel = getRenderer(property, n);
            propPanel.setFocused(hasFocus);

            String tooltipText = null;

            try {
                Object tooltipValue = property.getValue();

                if (null != tooltipValue) {
                    tooltipText = tooltipValue.toString();
                }
            } catch (IllegalAccessException eaE) {
                Logger.getLogger(TableSheetCell.class.getName()).log(Level.WARNING, null, eaE);
            } catch (InvocationTargetException itE) {
                Logger.getLogger(TableSheetCell.class.getName()).log(Level.WARNING, null, itE);
            }

            propPanel.setToolTipText(createHtmlTooltip(tooltipText, propPanel.getFont()));
            propPanel.setOpaque(true);

            if (isSelected) {
                Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

                boolean tableHasFocus = (table == focusOwner) || table.isAncestorOf(focusOwner) ||
                    (focusOwner instanceof Container && ((Container) focusOwner).isAncestorOf(table));

                if ((table == focusOwner) && table.isEditing()) {
                    //XXX really need to check if the editor has focus
                    tableHasFocus = true;
                }

                propPanel.setBackground(
                    tableHasFocus ? table.getSelectionBackground() : TreeTable.getUnfocusedSelectedBackground()
                );

                propPanel.setForeground(
                    tableHasFocus ? table.getSelectionForeground() : TreeTable.getUnfocusedSelectedForeground()
                );
            } else {
                propPanel.setBackground(table.getBackground());
                propPanel.setForeground(table.getForeground());
            }

            return propPanel;
        }

        if (nullPanel == null) {
            nullPanel = new NullPanel(n);
            nullPanel.setOpaque(true);
        } else {
            nullPanel.setNode(n);
        }

        if (isSelected) {
            Component focusOwner = KeyboardFocusManager.getCurrentKeyboardFocusManager().getFocusOwner();

            boolean tableHasFocus = hasFocus || (table == focusOwner) || table.isAncestorOf(focusOwner) ||
                (focusOwner instanceof Container && ((Container) focusOwner).isAncestorOf(table));

            nullPanel.setBackground(
                tableHasFocus ? table.getSelectionBackground() : TreeTable.getUnfocusedSelectedBackground()
            );

            //XXX may want to handle inverse theme here and use brighter if
            //below a threshold.  Deferred to centralized color management
            //being implemented.
            nullPanel.setForeground(table.getSelectionForeground().darker());
        } else {
            nullPanel.setBackground(table.getBackground());
            nullPanel.setForeground(table.getForeground());
        }

        nullPanel.setFocused(hasFocus);

        return nullPanel;
    }

    private PropertyPanel getEditor(Property p, Node n) {
        int prefs = PropertyPanel.PREF_TABLEUI;

        TTVEnvBridge bridge = TTVEnvBridge.getInstance(this);

        //workaround for issue 38132 - use env bridge to pass the 
        //node to propertypanel so it can call PropertyEnv.setBeans()
        //with it.  The sad thing is almost nobody uses PropertyEnv.getBeans(),
        //but we have to do it for all cases.
        bridge.setCurrentBeans(new Node[] { n });

        if (editor == null) {
            editor = new PropertyPanel(p, prefs);

            editor.putClientProperty("flat", Boolean.TRUE); //NOI18N
            editor.putClientProperty("beanBridgeIdentifier", this); //NOI18N

            editor.setProperty(p);

            return editor;
        }

        editor.setProperty(p);

        //Okay, the property panel has already grabbed the beans, clear
        //them so no references are held.
        return editor;
    }
    
    void updateUI() {
        headerRenderer = new JTable().getTableHeader().getDefaultRenderer();
    }

    private static String getString(String key) {
        return NbBundle.getMessage(TableSheetCell.class, key);
    }

    /**
     * HTML-ize a tooltip, splitting long lines. It's package private for unit
     * testing.
     */
    static String createHtmlTooltip(String value, Font font) {
        if (value == null) {
            return "null"; // NOI18N
        }

        // break up massive tooltips
        String token = null;

        if (value.indexOf(" ") != -1) { //NOI18N
            token = " "; //NOI18N
        } else if (value.indexOf(",") != -1) { //NOI18N
            token = ","; //NOI18N
        } else if (value.indexOf(";") != -1) { //NOI18N
            token = ";"; //NOI18N
        } else if (value.indexOf("/") != -1) { //NOI18N
            token = "/"; //NOI18N
        } else if (value.indexOf(">") != -1) { //NOI18N
            token = ">"; //NOI18N
        } else if (value.indexOf("\\") != -1) { //NOI18N
            token = "\\"; //NOI18N
        } else {
            //give up
            return makeDisplayble(value, font);
        }

        StringTokenizer tk = new StringTokenizer(value, token, true);

        StringBuffer sb = new StringBuffer(value.length() + 20);
        sb.append("<html>"); //NOI18N

        int charCount = 0;
        int lineCount = 0;

        while (tk.hasMoreTokens()) {
            String a = tk.nextToken();
            a = makeDisplayble(a, font);
            charCount += a.length();
            sb.append(a);

            if (tk.hasMoreTokens()) {
                charCount++;
            }

            if (charCount > 80) {
                sb.append("<br>"); //NOI18N
                charCount = 0;
                lineCount++;

                if (lineCount > 10) {
                    //Don't let things like VCS variables create
                    //a tooltip bigger than the screen. 99% of the
                    //time this is not a problem.
                    sb.append(NbBundle.getMessage(TableSheetCell.class, "MSG_ELLIPSIS")); //NOI18N

                    return sb.toString();
                }
            }
        }

        sb.append("</html>"); //NOI18N

        return sb.toString();
    }

    /**
     * Makes the given String displayble. Probably there doesn't exists
     * perfect solution for all situation. (someone prefer display those
     * squares for undisplayable chars, someone unicode placeholders). So lets
     * try do the best compromise.
     */
    private static String makeDisplayble(String str, Font f) {
        if (null == str) {
            return str;
        }

        if (null == f) {
            f = new JLabel().getFont();
        }

        StringBuffer buf = new StringBuffer((int) (str.length() * 1.3)); // x -> \u1234
        char[] chars = str.toCharArray();

        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            switch (c) {
            case '\t':
                buf.append("&nbsp;&nbsp;&nbsp;&nbsp;" + // NOI18N
                    "&nbsp;&nbsp;&nbsp;&nbsp;"
                ); // NOI18N
                break;

            case '\n':
                break;

            case '\r':
                break;

            case '\b':
                buf.append("\\b");

                break; // NOI18N

            case '\f':
                buf.append("\\f");

                break; // NOI18N

            default:

                if (!processHtmlEntity(buf, c)) {
                    if ((null == f) || f.canDisplay(c)) {
                        buf.append(c);
                    } else {
                        buf.append("\\u"); // NOI18N

                        String hex = Integer.toHexString(c);

                        for (int j = 0; j < (4 - hex.length()); j++)
                            buf.append('0');

                        buf.append(hex);
                    }
                }
            }
        }

        return buf.toString();
    }

    private static boolean processHtmlEntity(StringBuffer buf, char c) {
        switch (c) {
        case '>':
            buf.append("&gt;");

            break; // NOI18N

        case '<':
            buf.append("&lt;");

            break; // NOI18N

        case '&':
            buf.append("&amp;");

            break; // NOI18N

        default:
            return false;
        }

        return true;
    }

    private static class NullPanel extends JPanel {
        private Reference<Node> weakNode;
        private boolean focused = false;

        NullPanel(Node node) {
            this.weakNode = new WeakReference<Node>(node);
        }

        void setNode(Node node) {
            this.weakNode = new WeakReference<Node>(node);
        }

        @Override
        public AccessibleContext getAccessibleContext() {
            if (accessibleContext == null) {
                accessibleContext = new AccessibleNullPanel();
            }

            return accessibleContext;
        }

        public void setFocused(boolean val) {
            focused = val;
        }

        @Override
        public void paintComponent(Graphics g) {
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
                g.setColor(bdr);
            }
        }

        @Override
        public void addComponentListener(java.awt.event.ComponentListener l) {
            //do nothing
        }

        @Override
        public void addHierarchyListener(java.awt.event.HierarchyListener l) {
            //do nothing
        }

        @Override
        public void repaint() {
            //do nothing
        }

        @Override
        public void repaint(int x, int y, int width, int height) {
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
        public void firePropertyChange(String s, Object a, Object b) {
            //do nothing
        }

        private class AccessibleNullPanel extends AccessibleJPanel {
            AccessibleNullPanel() {
            }

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
                                getString("ACSD_NullPanel"), new Object[] { node.getDisplayName() }
                            );
                    }
                }

                return description;
            }
        }
    }

}
