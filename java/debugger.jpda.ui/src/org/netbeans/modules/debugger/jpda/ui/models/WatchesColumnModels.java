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

package org.netbeans.modules.debugger.jpda.ui.models;

import java.awt.Component;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.swing.InputMap;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import org.netbeans.api.debugger.Properties;
import org.netbeans.modules.debugger.jpda.ui.WatchPanel;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 * @author   Daniel Prusa
 */
public class WatchesColumnModels {

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree
     * table view representation.
     */
    public static class AbstractColumn extends ColumnModel {

        private String id;
        private String displayName;
        private String shortDescription;
        private Class type;
        private boolean defaultVisible;
        private PropertyEditor propertyEditor;
        private boolean sortable;

        Properties properties = Properties.getDefault ().
            getProperties ("debugger").getProperties ("views");

        public AbstractColumn(String id, String displayName, String shortDescription,
                              Class type) {
            this(id, displayName, shortDescription, type, true);
        }

        public AbstractColumn(String id, String displayName, String shortDescription,
                              Class type, boolean defaultVisible) {
            this(id, displayName, shortDescription, type, defaultVisible, null);
        }

        public AbstractColumn(String id, String displayName, String shortDescription,
                              Class type, boolean defaultVisible,
                              PropertyEditor propertyEditor) {
            this(id, displayName, shortDescription, type, defaultVisible, propertyEditor, true);
        }

        public AbstractColumn(String id, String displayName, String shortDescription,
                              Class type, boolean defaultVisible,
                              PropertyEditor propertyEditor, boolean sortable) {
            this.id = id;
            this.displayName = displayName;
            this.shortDescription = shortDescription;
            this.type = type;
            this.defaultVisible = defaultVisible;
            this.propertyEditor = propertyEditor;
            this.sortable = sortable;
        }

        @Override
        public String getID() {
            return id;
        }

        @Override
        public String getDisplayName() {
            return NbBundle.getBundle (WatchesColumnModels.class).getString(displayName);
        }

        @Override
        public String getShortDescription() {
            return NbBundle.getBundle (WatchesColumnModels.class).getString(shortDescription);
        }

        @Override
        public Class getType() {
            return type;
        }

        /**
         * Set true if column is visible.
         *
         * @param visible set true if column is visible
         */
        @Override
        public void setVisible (boolean visible) {
            properties.setBoolean (getID () + ".visible", visible);
        }

        /**
         * Set true if column should be sorted by default.
         *
         * @param sorted set true if column should be sorted by default
         */
        @Override
        public void setSorted (boolean sorted) {
            if (sortable) {
                properties.setBoolean (getID () + ".sorted", sorted);
            }
        }

        /**
         * Set true if column should be sorted by default in descending order.
         *
         * @param sortedDescending set true if column should be
         *        sorted by default in descending order
         */
        @Override
        public void setSortedDescending (boolean sortedDescending) {
            if (sortable) {
                properties.setBoolean (
                    getID () + ".sortedDescending",
                    sortedDescending
                 );
            }
        }

        /**
         * Should return current order number of this column.
         *
         * @return current order number of this column
         */
        @Override
        public int getCurrentOrderNumber () {
            int cn = properties.getInt (getID () + ".currentOrderNumber", -1);
            if (cn >= 0 && !properties.getBoolean("outlineOrdering", false)) {
                cn++; // Shift the old TreeTable ordering, which did not count the first nodes column.
            }
            return cn;
        }

        /**
         * Is called when current order number of this column is changed.
         *
         * @param newOrderNumber new order number
         */
        @Override
        public void setCurrentOrderNumber (int newOrderNumber) {
            properties.setInt (
                getID () + ".currentOrderNumber",
                newOrderNumber
            );
            properties.setBoolean("outlineOrdering", true);
        }

        /**
         * Return column width of this column.
         *
         * @return column width of this column
         */
        @Override
        public int getColumnWidth () {
            return properties.getInt (getID () + ".columnWidth", 150);
        }

        /**
         * Is called when column width of this column is changed.
         *
         * @param newColumnWidth a new column width
         */
        @Override
        public void setColumnWidth (int newColumnWidth) {
            properties.setInt (getID () + ".columnWidth", newColumnWidth);
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        @Override
        public boolean isVisible () {
            return properties.getBoolean (getID () + ".visible", defaultVisible);
        }

        @Override
        public boolean isSortable() {
            return sortable;
        }

        /**
         * True if column should be sorted by default.
         *
         * @return true if column should be sorted by default
         */
        @Override
        public boolean isSorted () {
            if (sortable) {
                return properties.getBoolean (getID () + ".sorted", false);
            } else {
                return false;
            }
        }

        /**
         * True if column should be sorted by default in descending order.
         *
         * @return true if column should be sorted by default in descending
         * order
         */
        @Override
        public boolean isSortedDescending () {
            if (sortable) {
                return properties.getBoolean (
                    getID () + ".sortedDescending",
                    false
                );
            } else {
                return false;
            }
        }

        /**
         * Returns {@link java.beans.PropertyEditor} to be used for
         * this column. Default implementation returns <code>null</code> -
         * means use default PropertyEditor.
         *
         * @return {@link java.beans.PropertyEditor} to be used for
         *         this column
         */
        @Override
        public PropertyEditor getPropertyEditor() {
            return propertyEditor;
        }

        public TableCellEditor getTableCellEditor() {
            return new WatchesTableCellEditor();
        }

    }

    public static ColumnModel createDefaultLocalsColumn() {
        return new AbstractColumn("DefaultLocalsColumn",
                "CTL_LocalsView_Column_Name_Name",
                "CTL_LocalsView_Column_Name_Desc",
                null);
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree
     * table view representation.
     */
    public static ColumnModel createDefaultWatchesColumn() {
        return new AbstractColumn("DefaultWatchesColumn",
                "CTL_WatchesView_Column_Name_Name",
                "CTL_WatchesView_Column_Name_Desc",
                null);
    }

    static class WatchesTableCellEditor implements TableCellEditor, FocusListener {

        private JEditorPane editorPane;
        private final List<CellEditorListener> listeners = new ArrayList<CellEditorListener>();
        private Object value;
        private Node node;

        WatchesTableCellEditor() {
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.value = value;
            editorPane = new WatchesEditorPane("text/x-java", "");
            editorPane.addFocusListener(this);
            // Remove control keys:
            KeyStroke enterKs = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
            KeyStroke escKs = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            InputMap im = editorPane.getInputMap();
            im.put(enterKs, "none");
            im.put(escKs, "none");

            WatchPanel.setupContext(editorPane, null);
            try {
                // [TODO] remove reflection after value is instance of Node instead of VisualizerNode
                Field nodeField = value.getClass().getDeclaredField("node");
                nodeField.setAccessible(true);
                node = (Node)nodeField.get(value);
            } catch (NoSuchFieldException ex) {
            } catch (SecurityException ex) {
            } catch (IllegalAccessException ex) {
            } catch (IllegalArgumentException ex) {
            }
            if (node != null) {
                editorPane.setText(node.getDisplayName());
            }
            JScrollPane scrollPane = new JScrollPane();
            scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
            scrollPane.setBackground(table.getBackground());
            scrollPane.setViewportView(editorPane);
            return scrollPane;
        }

        @Override
        public Object getCellEditorValue() {
            return editorPane.getText();
        }

        @Override
        public boolean isCellEditable(EventObject anEvent) {
            return true;
        }

        @Override
        public boolean shouldSelectCell(EventObject anEvent) {
            return true;
        }

        @Override
        public boolean stopCellEditing() {
            fireEditingStopped();
            return true;
        }

        @Override
        public void cancelCellEditing() {
        }

        @Override
        public void addCellEditorListener(CellEditorListener listener) {
            synchronized(listeners) {
                listeners.add(listener);
            }
        }

        @Override
        public void removeCellEditorListener(CellEditorListener listener) {
            synchronized(listeners) {
                listeners.remove(listener);
            }
        }

        @Override
        public void focusGained(FocusEvent e) {
        }

        @Override
        public void focusLost(FocusEvent e) {
            fireEditingStopped();
        }
        
        private void fireEditingStopped() {
            synchronized(listeners) {
                List<CellEditorListener> list = new ArrayList<CellEditorListener>(listeners);
                for (CellEditorListener listener : list) {
                    listener.editingStopped(new ChangeEvent(this));
                }
            }
        }

        class WatchesEditorPane extends JEditorPane {

            KeyStroke enter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);

            WatchesEditorPane(String type, String text) {
                super(type, text);
            }

            @Override
            protected void processKeyEvent(KeyEvent e) {
                KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
                if (enter.equals(ks)) {
                    // Prevent JComponent.processKeyBindings() to be called (it is called from
                    // JComponent.processKeyEvent() ), notify only registered key listeners
                    int id = e.getID();
                    for (KeyListener keyListener : getKeyListeners()) {
                        switch(id) {
                          case KeyEvent.KEY_TYPED:
                              keyListener.keyTyped(e);
                              break;
                          case KeyEvent.KEY_PRESSED:
                              keyListener.keyPressed(e);
                              break;
                          case KeyEvent.KEY_RELEASED:
                              keyListener.keyReleased(e);
                              break;
                        }
                    }
                    if (!e.isConsumed() && id == KeyEvent.KEY_PRESSED) {
                        synchronized(listeners) {
                            List<CellEditorListener> list = new ArrayList<CellEditorListener>(listeners);
                            for (CellEditorListener listener : list) {
                                listener.editingStopped(new ChangeEvent(this));
                            }
                        }
                    }
                    e.consume();
                } else {
                    super.processKeyEvent(e);
                }
            }

        }
        
    }
}
