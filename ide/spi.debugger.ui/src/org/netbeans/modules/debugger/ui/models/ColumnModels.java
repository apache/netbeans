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

package org.netbeans.modules.debugger.ui.models;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import org.netbeans.api.debugger.Properties;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.editor.DialogBinding;
import org.netbeans.editor.EditorUI;
import org.netbeans.editor.Utilities;
import org.netbeans.modules.debugger.ui.WatchPanel;
import org.netbeans.spi.debugger.ui.Constants;
import org.netbeans.spi.debugger.ui.EditorContextDispatcher;
import org.netbeans.spi.viewmodel.ColumnModel;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;


/**
 * Defines model for one table view column. Can be used together with
 * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table
 * view representation.
 *
 * @author   Jan Jancura
 */
public class ColumnModels {

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
        private boolean useTableCellEditor;
        private boolean sortable;

        Properties properties = Properties.getDefault ().
            getProperties ("debugger").getProperties ("views");
        private Properties viewProperties = null;

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
            this(id, displayName, shortDescription, type, defaultVisible, propertyEditor, false, true);
        }

        public AbstractColumn(String id, String displayName, String shortDescription,
                              Class type, boolean defaultVisible,
                              PropertyEditor propertyEditor, boolean useTableCellEditor,
                              boolean sortable) {
            this.id = id;
            this.displayName = displayName;
            this.shortDescription = shortDescription;
            this.type = type;
            this.defaultVisible = defaultVisible;
            this.propertyEditor = propertyEditor;
            this.useTableCellEditor = useTableCellEditor;
            this.sortable = sortable;
        }

        public String getID() {
            return id;
        }
        
        public void setViewPath(String viewPath) {
            viewProperties = properties.getProperties(viewPath);
        }

        public String getDisplayName() {
            return NbBundle.getBundle (ColumnModels.class).getString(displayName);
        }

        @Override
        public String getShortDescription() {
            return NbBundle.getBundle (ColumnModels.class).getString(shortDescription);
        }

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
            setBooleanProperty(".visible", visible);
        }

        /**
         * Set true if column should be sorted by default.
         *
         * @param sorted set true if column should be sorted by default
         */
        @Override
        public void setSorted (boolean sorted) {
            if (sortable) {
                setBooleanProperty(".sorted", sorted);
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
                setBooleanProperty(".sortedDescending", sortedDescending);
            }
        }

        /**
         * Should return current order number of this column.
         *
         * @return current order number of this column
         */
        @Override
        public int getCurrentOrderNumber () {
            int cn = getIntProperty(".currentOrderNumber", -1);
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
            setIntProperty(".currentOrderNumber", newOrderNumber);
            properties.setBoolean("outlineOrdering", true);
        }

        /**
         * Return column width of this column.
         *
         * @return column width of this column
         */
        @Override
        public int getColumnWidth () {
            return getIntProperty(".columnWidth", 150);
        }

        /**
         * Is called when column width of this column is changed.
         *
         * @param newColumnWidth a new column width
         */
        @Override
        public void setColumnWidth (int newColumnWidth) {
            setIntProperty(".columnWidth", newColumnWidth);
        }

        /**
         * True if column should be visible by default.
         *
         * @return true if column should be visible by default
         */
        @Override
        public boolean isVisible () {
            return getBooleanProperty(".visible", defaultVisible);
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
                return getBooleanProperty(".sorted", false);
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
                return getBooleanProperty(".sortedDescending", false);
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
            return useTableCellEditor ? new WatchesTableCellEditor() : null;
        }
        
        private boolean getBooleanProperty(String propName, boolean defaultValue) {
            propName = getID () + propName;
            if (viewProperties != null &&
                viewProperties.getBoolean(propName, true) == viewProperties.getBoolean(propName, false)) {
                // it is defined in the view:
                return viewProperties.getBoolean (propName, defaultValue);
            } else {
                return properties.getBoolean (propName, defaultValue);
            }
        }
        
        private void setBooleanProperty(String propName, boolean value) {
            propName = getID () + propName;
            if (viewProperties != null) {
                viewProperties.setBoolean(propName, value);
            } else {
                properties.setBoolean(propName, value);
            }
        }
        
        private int getIntProperty(String propName, int defaultValue) {
            propName = getID () + propName;
            if (viewProperties != null &&
                viewProperties.getInt(propName, 0) == viewProperties.getInt(propName, -1)) {
                // it is defined in the view:
                return viewProperties.getInt (propName, defaultValue);
            } else {
                return properties.getInt (propName, defaultValue);
            }
        }

        private void setIntProperty(String propName, int value) {
            propName = getID () + propName;
            if (viewProperties != null) {
                viewProperties.setInt(propName, value);
            } else {
                properties.setInt(propName, value);
            }
        }
        
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view
     * representation.
     */
    public static ColumnModel createDefaultBreakpointsColumn() {
        return new AbstractColumn("DefaultBreakpointColumn",
                "CTL_BreakpointView_Column_Name_Name",
                "CTL_BreakpointView_Column_Name_Desc",
                null);
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table view
     * representation.
     */
    public static ColumnModel createDefaultCallStackColumn() {
        return new AbstractColumn("DefaultCallStackColumn",
                "CTL_CallstackView_Column_Name_Name",
                "CTL_CallstackView_Column_Name_Desc",
                null, true, null, false, false);
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table
     * view representation.
     */
    public static ColumnModel createCallStackLocationColumn() {
        return new AbstractColumn(Constants.CALL_STACK_FRAME_LOCATION_COLUMN_ID,
                "CTL_CallstackView_Column_Location_Name",
                "CTL_CallstackView_Column_Location_Desc",
                String.class,
                false);
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table
     * view representation.
     */
    public static ColumnModel createDefaultLocalsColumn() {
        return new AbstractColumn("DefaultLocalsColumn",
                "CTL_LocalsView_Column_Name_Name",
                "CTL_LocalsView_Column_Name_Desc",
                null, true, null, true, true);
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table
     * view representation.
     */
    public static ColumnModel createLocalsToStringColumn() {
        return new AbstractColumn(Constants.LOCALS_TO_STRING_COLUMN_ID,
                "CTL_LocalsView_Column_ToString_Name",
                "CTL_LocalsView_Column_ToString_Desc",
                String.class,
                false);
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree
     * table view representation.
     */
    public static ColumnModel createLocalsTypeColumn() {
        return new AbstractColumn(Constants.LOCALS_TYPE_COLUMN_ID,
                "CTL_LocalsView_Column_Type_Name",
                "CTL_LocalsView_Column_Type_Desc",
                String.class,
                true);
    }
    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table
     * view representation.
     */
    public static ColumnModel createLocalsValueColumn() {
        return new AbstractColumn(Constants.LOCALS_VALUE_COLUMN_ID,
                "CTL_LocalsView_Column_Value_Name",
                "CTL_LocalsView_Column_Value_Desc",
                String.class,
                true);
    }
    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table
     * view representation.
     */
    public static ColumnModel createDefaultSessionColumn() {
        return new AbstractColumn("DefaultSessionColumn",
                "CTL_SessionsView_Column_Name_Name",
                "CTL_SessionsView_Column_Name_Desc",
                null);
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table
     * view representation.
     */
    public static ColumnModel createSessionHostNameColumn() {
        return new AbstractColumn(Constants.SESSION_HOST_NAME_COLUMN_ID,
                "CTL_SessionsView_Column_HostName_Name",
                "CTL_SessionsView_Column_HostName_Desc",
                String.class,
                false);
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree
     * table view representation.
     */
    public static ColumnModel createSessionStateColumn () {
        return new AbstractColumn(Constants.SESSION_STATE_COLUMN_ID,
                "CTL_SessionsView_Column_State_Name",
                "CTL_SessionsView_Column_State_Desc",
                String.class,
                true);
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree table
     * view representation.
     */
    public static ColumnModel createSessionLanguageColumn () {
        return new AbstractColumn(Constants.SESSION_LANGUAGE_COLUMN_ID,
                "CTL_SessionsView_Column_Language_Name",
                "CTL_SessionsView_Column_Language_Desc",
                Session.class,
                true,
                new LanguagePropertyEditor ());
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree
     * table view representation.
     */
    public static ColumnModel createDefaultThreadColumn() {
        return new AbstractColumn("DefaultThreadColumn",
                "CTL_ThreadsView_Column_Name_Name",
                "CTL_ThreadsView_Column_Name_Desc",
                null);
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree
     * table view representation.
     */
    public static ColumnModel createThreadStateColumn() {
        return new AbstractColumn(Constants.THREAD_STATE_COLUMN_ID,
                "CTL_ThreadsView_Column_State_Name",
                "CTL_ThreadsView_Column_State_Desc",
                String.class,
                true);
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree
     * table view representation.
     */
    public static ColumnModel createThreadSuspendedColumn() {
        return new AbstractColumn(Constants.THREAD_SUSPENDED_COLUMN_ID,
                "CTL_ThreadsView_Column_Suspended_Name",
                "CTL_ThreadsView_Column_Suspended_Desc",
                Boolean.TYPE,
                false);
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
                null, true, null, true, true);
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree
     * table view representation.
     */
    public static ColumnModel createWatchToStringColumn() {
        return new AbstractColumn(Constants.WATCH_TO_STRING_COLUMN_ID,
                "CTL_WatchesView_Column_ToString_Name",
                "CTL_WatchesView_Column_ToString_Desc",
                String.class,
                false);
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree
     * table view representation.
     */
    public static ColumnModel createWatchTypeColumn() {
        return new AbstractColumn(Constants.WATCH_TYPE_COLUMN_ID,
                "CTL_WatchesView_Column_Type_Name",
                "CTL_WatchesView_Column_Type_Desc",
                String.class,
                true);
    }

    /**
     * Defines model for one table view column. Can be used together with
     * {@link org.netbeans.spi.viewmodel.TreeModel} for tree
     * table view representation.
     */
    public static ColumnModel createWatchValueColumn() {
        return new AbstractColumn(Constants.WATCH_VALUE_COLUMN_ID,
                "CTL_WatchesView_Column_Value_Name",
                "CTL_WatchesView_Column_Value_Desc",
                String.class,
                true);
    }

    private static class LanguagePropertyEditor extends PropertyEditorSupport {

        @Override
        public void setValue(Object value) {
            if (value != null && !(value instanceof Session)) {
                ErrorManager.getDefault().notify(
                        new IllegalArgumentException("Value "+value+" is not an instance of Session!"));
            }
            super.setValue(new WeakReference(value));
        }

        private Session getSession() {
            Reference<Session> sRef = (Reference<Session>) getValue();
            Session s = (sRef != null) ? sRef.get() : null;
            return s;
        }

        @Override
        public String[] getTags () {
            Session s = getSession();
            if (s == null) {
                return new String [0];
            } else {
                return s.getSupportedLanguages ();
            }
        }

        @Override
        public String getAsText () {
            Session s = getSession();
            if (s == null) {
                return "null";
            } else {
                return s.getCurrentLanguage();
            }
        }

        @Override
        public void setAsText (String text) {
            Session s = getSession();
            if (s != null) {
                s.setCurrentLanguage (text);
            }
        }
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

            FileObject file = WatchPanel.getRecentFile();
            int line = EditorContextDispatcher.getDefault().getMostRecentLineNumber();
            int col = WatchPanel.getRecentColumn();
            String mimeType = file != null ? file.getMIMEType() : "text/plain"; // NOI18N
            boolean doBind = true;
            if (!mimeType.startsWith("text/")) { // NOI18N
                // If the current file happens to be of unknown or not text MIME type, use the ordinary text one.
                mimeType = "text/plain"; // NOI18N
                doBind = false; // Do not do binding to an unknown file content.
            }
            editorPane = new WatchesEditorPane(mimeType, "");
            if (doBind && file != null) {
                Point lc = WatchPanel.adjustLineAndColumn(file, line, col);
                DialogBinding.bindComponentToFile(file, lc.x - 1, lc.y, 0, editorPane);
            }

            editorPane.addFocusListener(this);
            // Remove control keys:
            KeyStroke enterKs = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
            KeyStroke escKs = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
            InputMap im = editorPane.getInputMap();
            im.put(enterKs, "none");
            im.put(escKs, "none");
            setupUI(editorPane);

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

        private void setupUI(JEditorPane editorPane) {
            EditorUI eui = org.netbeans.editor.Utilities.getEditorUI(editorPane);
            if (eui == null) {
                return;
            }
            editorPane.putClientProperty(
                "HighlightsLayerExcludes", //NOI18N
                "^org\\.netbeans\\.modules\\.editor\\.lib2\\.highlighting\\.CaretRowHighlighting$" //NOI18N
            );
            // Do not draw text limit line
            try {
                java.lang.reflect.Field textLimitLineField = EditorUI.class.getDeclaredField("textLimitLineVisible"); // NOI18N
                textLimitLineField.setAccessible(true);
                textLimitLineField.set(eui, false);
            } catch (Exception ex) {}
            editorPane.repaint();
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
