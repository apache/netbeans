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

package org.netbeans.modules.db.sql.editor.ui.actions;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.DefaultFocusTraversalPolicy;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.swing.AbstractListModel;
import javax.swing.Action;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.api.db.explorer.ConnectionListener;
import org.netbeans.api.db.explorer.ConnectionManager;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.db.api.sql.execute.SQLExecution;
import org.openide.awt.Mnemonics;
import org.openide.cookies.EditorCookie;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 *
 * @author Andrei Badea
 */
public class ConnectionAction extends SQLExecutionBaseAction {

    @Override
    protected String getDisplayName(SQLExecution sqlExecution) {
        // just needed in order to satisfy issue 101775
        return NbBundle.getMessage(ConnectionAction.class, "LBL_DatabaseConnection");
    }

    @Override
    protected void actionPerformed(SQLExecution sqlExecution) {
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        return new ConnectionContextAwareDelegate(this, actionContext);
    }

    private static final class ConnectionContextAwareDelegate extends ContextAwareDelegate {

        private final Lookup actionContext;
        private ToolbarPresenter toolbarPresenter;

        public ConnectionContextAwareDelegate(ConnectionAction parent, Lookup actionContext) {
            super(parent, actionContext);
            this.actionContext = actionContext;
        }

        @Override
        public Component getToolbarPresenter() {
            toolbarPresenter = new ToolbarPresenter(actionContext);
            toolbarPresenter.setEnabled(this.isEnabled());
            toolbarPresenter.setSQLExecution(getSQLExecution());
            return toolbarPresenter;
        }

        @Override
        public void setEnabled(boolean enabled) {
            if (toolbarPresenter != null) {
                toolbarPresenter.setEnabled(enabled);
            }
            super.setEnabled(enabled);
        }

        @Override
        protected void setSQLExecution(final SQLExecution sqlExecution) {
            Mutex.EVENT.readAccess(new Runnable() {
                @Override
                public void run() {
                    if (toolbarPresenter != null) {
                        // test for null necessary since the sqlExecution property
                        // can change just before the toolbar presenter is created
                        toolbarPresenter.setSQLExecution(sqlExecution);
                    }
                }
            });
            super.setSQLExecution(sqlExecution);
        }
    }

    /**
     * Toolbar presenter for the connectionAction - displays the list of connections
     * for the user to choose. Until the connection list is retrieved an
     * informational message is presented to the user and the combobox is
     * not editable.
     */
    private static final class ToolbarPresenter extends JPanel implements ListDataListener {

        private final Lookup actionContext;
        private final DatabaseConnectionModel model = new DatabaseConnectionModel();
        private boolean externalEnabled = true;
        private JComboBox combo;
        private JLabel comboLabel;
        private ListDataListener listDataListener;

        public ToolbarPresenter(final Lookup actionContext) {
            assert SwingUtilities.isEventDispatchThread();
            initComponents();
            this.actionContext = actionContext;
            this.listDataListener = WeakListeners.create(ListDataListener.class, this, model);
            model.addListDataListener(listDataListener);
        }

        @Override
        public Dimension getMinimumSize() {
            Dimension dim = super.getMinimumSize();
            int minWidth = comboLabel.getWidth() * 2;
            return new Dimension(minWidth, dim.height);
        }

        public void setSQLExecution(SQLExecution sqlExecution) {
            model.setSQLExecution(sqlExecution);
        }

        private void initComponents() {
            setLayout(new BorderLayout(4, 0));
            setBorder(new EmptyBorder(0, 2, 0, 8));
            setOpaque(false);
            setFocusTraversalPolicyProvider(true);
            setFocusTraversalPolicy(new DefaultFocusTraversalPolicy() {
                @Override
                public Component getDefaultComponent(Container aContainer) {
                    if (!SwingUtilities.isEventDispatchThread()) {
                        return null;
                    }
                    final EditorCookie ec = actionContext.lookup(
                            EditorCookie.class);
                    if (ec != null) {
                        JEditorPane[] panes = ec.getOpenedPanes();
                        if (panes != null) {
                            for (JEditorPane pane : panes) {
                                if (pane.isShowing()) {
                                    return pane;
                                }
                            }
                        }
                    }

                    return null;
                }
           });

            combo = new JComboBox();
            combo.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    DatabaseConnection dbconn = (DatabaseConnection)combo.getSelectedItem();
                    combo.setToolTipText(dbconn != null ? dbconn.getDisplayName() : null);
                }
            });
            combo.setOpaque(false);
            combo.setModel(new DefaultComboBoxModel(
                    new String[] { NbBundle.getMessage(ToolbarPresenter.class, "ConnectionAction.ToolbarPresenter.LoadingConnections") }));

            combo.setRenderer(new DatabaseConnectionRenderer());
            String accessibleName = NbBundle.getMessage(ConnectionAction.class, "LBL_DatabaseConnection");
            combo.getAccessibleContext().setAccessibleName(accessibleName);
            combo.getAccessibleContext().setAccessibleDescription(accessibleName);
            combo.setPreferredSize (new Dimension (400, combo.getPreferredSize ().height));

            add(combo, BorderLayout.CENTER);

            comboLabel = new JLabel();
            Mnemonics.setLocalizedText(comboLabel, NbBundle.getMessage(ConnectionAction.class, "LBL_ConnectionAction"));
            comboLabel.setOpaque(false);
            comboLabel.setLabelFor(combo);
            add(comboLabel, BorderLayout.WEST);
        }

        @Override
        public void setEnabled(boolean enabled) {
            externalEnabled = enabled;
            updateState();
        }

        private void updateState() {
            combo.setEnabled(externalEnabled && model.isInitialized());
        }
        
        private void initialized() {
            if (model.isInitialized()) {
                model.removeListDataListener(listDataListener);
                listDataListener = null;
                combo.setModel(model);
                updateState();
            }
        }
        
        @Override
        public void intervalAdded(ListDataEvent e) {
            initialized();
        }

        @Override
        public void intervalRemoved(ListDataEvent e) {
            initialized();
        }

        @Override
        public void contentsChanged(ListDataEvent e) {
            initialized();
        }
    }

    /**
     * DatabaseConnectionModel monitors the netbeans connection list and an
     * SQLExecution. The list model reflects the connection of the SQLExecution
     * as the selected value and dynamicly updates on connection and connection
     * list changes.
     * 
     * The initialized flag is set as soon as the first connection list retrieval
     * is done.
     */
    private static final class DatabaseConnectionModel extends AbstractListModel implements ComboBoxModel, ConnectionListener, PropertyChangeListener {
        private static final RequestProcessor RP = new RequestProcessor(ToolbarPresenter.class);
        private static final Comparator dbconComparator = new Comparator<DatabaseConnection>() {
            @Override
            public int compare(DatabaseConnection o1, DatabaseConnection o2) {
                return o1.getDisplayName().compareToIgnoreCase(o2.getDisplayName());
            }
        };
        private final ConnectionListener listener;
        private List<DatabaseConnection> connectionList = new ArrayList<>(); // must be ArrayList
        private SQLExecution sqlExecution;
        private boolean initialized = false;
        
        private final Runnable connectionUpdate = new Runnable() {
            @Override
            public void run() {
                final ArrayList<DatabaseConnection> newList = new ArrayList<>(
                        Arrays.asList(ConnectionManager.getDefault().getConnections()));
                newList.sort(dbconComparator);

                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        connectionList = newList;

                        DatabaseConnection selectedItem = (DatabaseConnection) getSelectedItem();
                        if (selectedItem != null && !connectionList.contains(selectedItem)) {
                            setSelectedItem(null);
                        }

                        initialized = true;
                        fireContentsChanged(this, 0, connectionList.size());
                    }
                });
            }
        };

        @SuppressWarnings("LeakingThisInConstructor")
        public DatabaseConnectionModel() {
            listener = WeakListeners.create (ConnectionListener.class, this, ConnectionManager.getDefault());
            ConnectionManager.getDefault().addConnectionListener(listener);
            RP.post(connectionUpdate);
        }

        @Override
        public Object getElementAt(int index) {
            return connectionList.get(index);
        }

        @Override
        public int getSize() {
            return connectionList.size();
        }

        @Override
        public void setSelectedItem(Object object) {
            if (sqlExecution != null) {
                sqlExecution.setDatabaseConnection((DatabaseConnection)object);
            }
        }

        @Override
        public Object getSelectedItem() {
            return sqlExecution != null ? sqlExecution.getDatabaseConnection() : null;
        }

        public void setSQLExecution(SQLExecution sqlExecution) {
            if (this.sqlExecution != null) {
                this.sqlExecution.removePropertyChangeListener(this);
            }
            this.sqlExecution = sqlExecution;
            if (this.sqlExecution != null) {
                this.sqlExecution.addPropertyChangeListener(this);
            }
            fireContentsChanged(this, 0, connectionList.size()); // because the selected item might have changed
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propertyName = evt.getPropertyName();
            if (propertyName == null || propertyName.equals(SQLExecution.PROP_DATABASE_CONNECTION)) {
                Mutex.EVENT.readAccess(new Runnable() {
                    @Override
                    public void run() {
                        fireContentsChanged(this, 0, connectionList.size()); // because the selected item might have changed
                    }
                });
            }
        }

        public boolean isInitialized() {
            return initialized;
        }

        @Override
        public void connectionsChanged() {
            RP.post(connectionUpdate);
        }
    }

    private static final class DatabaseConnectionRenderer extends DefaultListCellRenderer {

        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Object displayName;
            String tooltipText;

            if (value instanceof DatabaseConnection) {
                DatabaseConnection dbconn = (DatabaseConnection)value;
                tooltipText = dbconn.getDisplayName();
                displayName = tooltipText;
            } else {
                tooltipText = null;
                displayName = value;
            }
            JLabel component = (JLabel)super.getListCellRendererComponent(list, displayName, index, isSelected, cellHasFocus);
            component.setToolTipText(tooltipText);

            return component;
        }
    }
}
