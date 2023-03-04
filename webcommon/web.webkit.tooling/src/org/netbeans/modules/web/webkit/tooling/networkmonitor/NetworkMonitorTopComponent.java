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
package org.netbeans.modules.web.webkit.tooling.networkmonitor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import org.openide.text.CloneableEditorSupport;
import org.openide.windows.TopComponent;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.RetainLocation;

@TopComponent.Description(
        preferredID = "NetworkMonitorTopComponent",
        persistenceType = TopComponent.PERSISTENCE_NEVER)
@RetainLocation(value = "output")
@Messages({
    "CTL_NetworkMonitorTopComponent=Network Monitor",
    "HINT_NetworkMonitorTopComponent=This is a Network Monitor window"
})
public final class NetworkMonitorTopComponent extends TopComponent
    implements TableModelListener, ChangeListener {

    private Model model;
    private final InputOutput io;
    private final MyProvider ioProvider;
    private final UIUpdater updater;
    private boolean debuggingSession;

    NetworkMonitorTopComponent(Model m, boolean debuggingSession) {
        assert SwingUtilities.isEventDispatchThread();
        this.debuggingSession = debuggingSession;
        initComponents();
        jResponse.setEditorKit(CloneableEditorSupport.getEditorKit("text/plain"));
        setName(Bundle.CTL_NetworkMonitorTopComponent());
        setToolTipText(Bundle.HINT_NetworkMonitorTopComponent());
        updater = new UIUpdater(this);
        setModel(m, debuggingSession);
        initRequestTable();
        jSplitPane.setDividerLocation(NbPreferences.forModule(NetworkMonitorTopComponent.class).getInt("separator", 200));
        selectedItemChanged();
        updateVisibility();
        ioProvider = new MyProvider(jIOContainerPlaceholder);
        IOContainer container = IOContainer.create(ioProvider);
        io = IOProvider.getDefault().getIO("callstack", new Action[0], container);
    }

    /**
     * Initializes the request table.
     */
    private void initRequestTable() {
        requestTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        requestTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                selectedItemChanged();
            }
        });
    }

    private static class UIUpdater implements ActionListener {

        private final Timer t;
        private final NetworkMonitorTopComponent comp;
        private ModelItem modelItem;

        public UIUpdater(NetworkMonitorTopComponent comp) {
            this.comp = comp;
            t = new Timer(200, this);
            t.setRepeats(false);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            comp._refreshDetailsView(modelItem);
        }

        public synchronized void showItem(ModelItem mi) {
            t.stop();
            modelItem = mi;
            t.start();
        }

    }

    void setModel(Model model, boolean debuggingSession) {
        this.model = model;
        this.debuggingSession = debuggingSession;
        TableModel tableModel = requestTable.getModel();
        if (tableModel != null) {
            tableModel.removeTableModelListener(this);
        }
        requestTable.setModel(model);
        requestTable.setRowSorter(new TriStateRowSorter(model));
        model.addTableModelListener(this);
        selectedItemChanged();
        updateVisibility();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        jClear = new javax.swing.JButton();
        requestTableScrollPane = new javax.swing.JScrollPane();
        requestTable = new javax.swing.JTable();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jHeadersPanel = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jHeaders = new javax.swing.JTextPane();
        jRequestPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jRequest = new javax.swing.JEditorPane();
        jRawResponseRequest = new javax.swing.JCheckBox();
        jResponsePanel = new javax.swing.JPanel();
        jRawResponseResponse = new javax.swing.JCheckBox();
        jScrollPane3 = new javax.swing.JScrollPane();
        jResponse = new javax.swing.JEditorPane();
        jFramesPanel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        jFrames = new javax.swing.JTextPane();
        jRawResponseFrames = new javax.swing.JCheckBox();
        jCallStackPanel = new javax.swing.JPanel();
        jIOContainerPlaceholder = new javax.swing.JPanel();
        jNoData = new javax.swing.JLabel();
        jNoConnection = new javax.swing.JLabel();

        jClear.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/web/webkit/tooling/networkmonitor/delete.gif"))); // NOI18N
        jClear.setToolTipText(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jClear.tooltip")); // NOI18N
        jClear.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jClearActionPerformed(evt);
            }
        });

        requestTableScrollPane.setViewportView(requestTable);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jClear)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(requestTableScrollPane)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jClear)
                .addGap(0, 0, 0)
                .addComponent(requestTableScrollPane))
        );

        jSplitPane.setLeftComponent(jPanel3);

        jHeadersPanel.setName(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jHeadersPanel.TabConstraints.tabTitle")); // NOI18N

        jHeaders.setEditable(false);
        jScrollPane5.setViewportView(jHeaders);

        javax.swing.GroupLayout jHeadersPanelLayout = new javax.swing.GroupLayout(jHeadersPanel);
        jHeadersPanel.setLayout(jHeadersPanelLayout);
        jHeadersPanelLayout.setHorizontalGroup(
            jHeadersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5)
        );
        jHeadersPanelLayout.setVerticalGroup(
            jHeadersPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane5)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jHeadersPanel.TabConstraints.tabTitle"), jHeadersPanel); // NOI18N

        jRequestPanel.setName(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.Request Data.TabConstraints.tabTitle")); // NOI18N

        jRequest.setEditable(false);
        jScrollPane2.setViewportView(jRequest);

        org.openide.awt.Mnemonics.setLocalizedText(jRawResponseRequest, org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jRawResponseRequest.text")); // NOI18N
        jRawResponseRequest.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRawResponseRequestItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jRequestPanelLayout = new javax.swing.GroupLayout(jRequestPanel);
        jRequestPanel.setLayout(jRequestPanelLayout);
        jRequestPanelLayout.setHorizontalGroup(
            jRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2)
            .addGroup(jRequestPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jRawResponseRequest))
        );
        jRequestPanelLayout.setVerticalGroup(
            jRequestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jRequestPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRawResponseRequest))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jRequestPanel.TabConstraints.tabTitle"), jRequestPanel); // NOI18N

        jResponsePanel.setName(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jResponsePanel.TabConstraints.tabTitle")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jRawResponseResponse, org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jRawResponseResponse.text")); // NOI18N
        jRawResponseResponse.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRawResponseResponseItemStateChanged(evt);
            }
        });

        jResponse.setEditable(false);
        jScrollPane3.setViewportView(jResponse);

        javax.swing.GroupLayout jResponsePanelLayout = new javax.swing.GroupLayout(jResponsePanel);
        jResponsePanel.setLayout(jResponsePanelLayout);
        jResponsePanelLayout.setHorizontalGroup(
            jResponsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jResponsePanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jRawResponseResponse))
            .addComponent(jScrollPane3)
        );
        jResponsePanelLayout.setVerticalGroup(
            jResponsePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jResponsePanelLayout.createSequentialGroup()
                .addComponent(jScrollPane3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRawResponseResponse))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jResponsePanel.TabConstraints.tabTitle"), jResponsePanel); // NOI18N

        jFramesPanel.setName(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jFramesPanel.TabConstraints.tabTitle")); // NOI18N

        jScrollPane4.setViewportView(jFrames);

        org.openide.awt.Mnemonics.setLocalizedText(jRawResponseFrames, org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jRawResponseFrames.text")); // NOI18N
        jRawResponseFrames.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jRawResponseFramesItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jFramesPanelLayout = new javax.swing.GroupLayout(jFramesPanel);
        jFramesPanel.setLayout(jFramesPanelLayout);
        jFramesPanelLayout.setHorizontalGroup(
            jFramesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jFramesPanelLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jRawResponseFrames))
            .addComponent(jScrollPane4)
        );
        jFramesPanelLayout.setVerticalGroup(
            jFramesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jFramesPanelLayout.createSequentialGroup()
                .addComponent(jScrollPane4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRawResponseFrames))
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jFramesPanel.TabConstraints.tabTitle"), jFramesPanel); // NOI18N

        jCallStackPanel.setName(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jCallStackPanel.TabConstraints.tabTitle")); // NOI18N

        javax.swing.GroupLayout jIOContainerPlaceholderLayout = new javax.swing.GroupLayout(jIOContainerPlaceholder);
        jIOContainerPlaceholder.setLayout(jIOContainerPlaceholderLayout);
        jIOContainerPlaceholderLayout.setHorizontalGroup(
            jIOContainerPlaceholderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jIOContainerPlaceholderLayout.setVerticalGroup(
            jIOContainerPlaceholderLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jCallStackPanelLayout = new javax.swing.GroupLayout(jCallStackPanel);
        jCallStackPanel.setLayout(jCallStackPanelLayout);
        jCallStackPanelLayout.setHorizontalGroup(
            jCallStackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jIOContainerPlaceholder, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jCallStackPanelLayout.setVerticalGroup(
            jCallStackPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jIOContainerPlaceholder, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab(org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jCallStackPanel.TabConstraints.tabTitle"), jCallStackPanel); // NOI18N

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane1))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jTabbedPane1))
        );

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jSplitPane.setRightComponent(jPanel1);

        org.openide.awt.Mnemonics.setLocalizedText(jNoData, org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jNoData.text")); // NOI18N
        jNoData.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jNoData.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 1, 1));

        org.openide.awt.Mnemonics.setLocalizedText(jNoConnection, org.openide.util.NbBundle.getMessage(NetworkMonitorTopComponent.class, "NetworkMonitorTopComponent.jNoConnection.text")); // NOI18N
        jNoConnection.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jNoConnection.setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 1, 1));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane)
            .addComponent(jNoData)
            .addComponent(jNoConnection)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jSplitPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jNoData, javax.swing.GroupLayout.PREFERRED_SIZE, 123, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jNoConnection, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jRawResponseResponseItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRawResponseResponseItemStateChanged
        ModelItem mi = lastSelectedItem;
        if (mi != null) {
            refreshDetailsView(mi);
        }
    }//GEN-LAST:event_jRawResponseResponseItemStateChanged

    private void jRawResponseRequestItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRawResponseRequestItemStateChanged
        ModelItem mi = lastSelectedItem;
        if (mi != null) {
            refreshDetailsView(mi);
        }
    }//GEN-LAST:event_jRawResponseRequestItemStateChanged

    private void jRawResponseFramesItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jRawResponseFramesItemStateChanged
        ModelItem mi = lastSelectedItem;
        if (mi != null) {
            refreshDetailsView(mi);
        }
    }//GEN-LAST:event_jRawResponseFramesItemStateChanged

    private void jClearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jClearActionPerformed
        resetModel();
    }//GEN-LAST:event_jClearActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jCallStackPanel;
    private javax.swing.JButton jClear;
    private javax.swing.JTextPane jFrames;
    private javax.swing.JPanel jFramesPanel;
    private javax.swing.JTextPane jHeaders;
    private javax.swing.JPanel jHeadersPanel;
    private javax.swing.JPanel jIOContainerPlaceholder;
    private javax.swing.JLabel jNoConnection;
    private javax.swing.JLabel jNoData;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JCheckBox jRawResponseFrames;
    private javax.swing.JCheckBox jRawResponseRequest;
    private javax.swing.JCheckBox jRawResponseResponse;
    private javax.swing.JEditorPane jRequest;
    private javax.swing.JPanel jRequestPanel;
    private javax.swing.JEditorPane jResponse;
    private javax.swing.JPanel jResponsePanel;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JSplitPane jSplitPane;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTable requestTable;
    private javax.swing.JScrollPane requestTableScrollPane;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentClosed() {
        setReopenNetworkComponent(false);
        model.passivate();
        // avoid memory leaks
        model.removeTableModelListener(this);
        // avoid memory leaks
        requestTable.setModel(new DefaultTableModel());
        ioProvider.close();
        NbPreferences.forModule(NetworkMonitorTopComponent.class).putInt("separator", jSplitPane.getDividerLocation());
    }

    static boolean canReopenNetworkComponent() {
        return NbPreferences.forModule(NetworkMonitorTopComponent.class).getBoolean("reopen", true);
    }

    static void setReopenNetworkComponent(boolean b) {
        NbPreferences.forModule(NetworkMonitorTopComponent.class).putBoolean("reopen", b);
    }

    private ModelItem lastSelectedItem = null;

    private void selectedItemChanged() {
        assert SwingUtilities.isEventDispatchThread();
        int index = requestTable.getSelectedRow();
        if (index != -1) {
            index = requestTable.convertRowIndexToModel(index);
        }
        final ModelItem mi = model.getItem(index);
        if (lastSelectedItem == mi) {
            return;
        } else {
            if (lastSelectedItem != null) {
                lastSelectedItem.setChangeListener(null);
            }
            lastSelectedItem = mi;
            if (lastSelectedItem != null) {
                lastSelectedItem.setChangeListener(this);
            }
        }
        refreshDetailsView(lastSelectedItem);
    }

    private void refreshDetailsView(ModelItem mi) {
        updater.showItem(mi);
    }

    private void _refreshDetailsView(ModelItem mi) {
        assert SwingUtilities.isEventDispatchThread();
        if (mi != null) {
            mi.updateHeadersPane(jHeaders);
            mi.updateResponsePane(jResponse, jRawResponseResponse.isSelected());
            mi.updateFramesPane(jFrames, jRawResponseFrames.isSelected());
            mi.updatePostDataPane(jRequest, jRawResponseRequest.isSelected());
            mi.updateCallStack(io);
        }
        updateTabVisibility(mi);
    }

    private void updateVisibility() {
        boolean empty = model.getRowCount() == 0;
        jSplitPane.setVisible(!empty);
        jNoData.setVisible(empty && debuggingSession);
        jNoConnection.setVisible(empty && !debuggingSession);
        if (!empty && requestTable.getSelectedRow() == -1) {
            refreshDetailsView(null);
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        updateVisibility();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refreshDetailsView(lastSelectedItem);
    }

    void resetModel() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                model.reset();
            }
        });
    }

    private void updateTabVisibility(ModelItem mi) {
        int index = 0;

        // Header - always visible
        boolean showHeaders = mi != null;
        index = showHideTab(jHeadersPanel, showHeaders, index);

        // Request Data:
        boolean postDataVisible = mi != null && mi.hasPostData();
        index = showHideTab(jRequestPanel, postDataVisible, index);

        // Response:
        boolean hasResponseData = mi != null && mi.hasResponseData();
        index = showHideTab(jResponsePanel, hasResponseData, index);

        // Frames:
        boolean hasFrames = mi != null && mi.hasFrames();
        index = showHideTab(jFramesPanel, hasFrames, index);

        // Call Stack:
        boolean hasCallStack = mi != null && mi.hasCallStack();
        showHideTab(jCallStackPanel, hasCallStack, index);

    }

    private int showHideTab(JPanel jPanel, boolean show, int index) {
        Component comp = index < jTabbedPane1.getTabCount() ? jTabbedPane1.getComponentAt(index) : null;
        if (show) {
             if (jPanel != comp) {
                 jTabbedPane1.add(jPanel, index);
             }
            return index+1;
        } else {
             if (jPanel == comp) {
                 jTabbedPane1.remove(index);
             }
            return index;
        }
    }

    public static class JTextPaneNonWrapping extends JTextPane {

        public JTextPaneNonWrapping() {
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            Component parent = getParent();

            return parent != null ? (getUI().getPreferredSize(this).width <= parent
                    .getSize().width) : true;
        }

    }

    private static class MyProvider implements IOContainer.Provider {

        private JPanel parent;

        public MyProvider(JPanel parent) {
            this.parent = parent;
        }

        @Override
        public void open() {
        }

        @Override
        public void requestActive() {
        }

        @Override
        public void requestVisible() {
        }

        @Override
        public boolean isActivated() {
            return false;
        }

        @Override
        public void add(JComponent comp, IOContainer.CallBacks cb) {
            assert parent != null;
            parent.setLayout(new BorderLayout());
            parent.add(comp, BorderLayout.CENTER);
        }

        @Override
        public void remove(JComponent comp) {
            assert parent != null;
            parent.remove(comp);
        }

        @Override
        public void select(JComponent comp) {
        }

        @Override
        public JComponent getSelected() {
            return null;
        }

        @Override
        public void setTitle(JComponent comp, String name) {
        }

        @Override
        public void setToolTipText(JComponent comp, String text) {
        }

        @Override
        public void setIcon(JComponent comp, Icon icon) {
        }

        @Override
        public void setToolbarActions(JComponent comp, Action[] toolbarActions) {
        }

        @Override
        public boolean isCloseable(JComponent comp) {
            return false;
        }

        private void close() {
            parent = null;
        }

    }

    /**
     * Table row sorter that cycles between ascending, descending and unsorted orders.
     */
    public static class TriStateRowSorter extends TableRowSorter<TableModel> {
        
        public TriStateRowSorter(TableModel model) {
            super(model);
        }

        @Override
        public void toggleSortOrder(int column) {
            List<? extends SortKey> sortKeys = getSortKeys();
            if (!sortKeys.isEmpty()) {
                SortKey sortKey = sortKeys.get(0);
                if (sortKey.getColumn() == column && sortKey.getSortOrder() == SortOrder.DESCENDING) {
                    setSortKeys(null);
                    return;
                }
            }
            super.toggleSortOrder(column);
        }

    }
}
