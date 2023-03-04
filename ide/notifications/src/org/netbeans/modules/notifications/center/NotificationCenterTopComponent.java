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
package org.netbeans.modules.notifications.center;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.notifications.NotificationImpl;
import org.netbeans.modules.notifications.NotificationSettings;
import org.netbeans.modules.notifications.Utils;
import org.netbeans.swing.etable.ETable;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.QuickSearch;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.netbeans.modules.notifications//NotificationCenter//EN",
        autostore = false)
@TopComponent.Description(
        preferredID = "NotificationCenterTopComponent",
        iconBase = "org/netbeans/modules/notifications/resources/notificationsTC.png",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "output", openAtStartup = false, position = 123)
@ActionID(category = "Window", id = "org.netbeans.modules.notifications.NotificationCenterTopComponent")
@ActionReference(path = "Menu/Window/Tools", position = 648)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_NotificationCenterAction",
        preferredID = "NotificationCenterTopComponent")
public final class NotificationCenterTopComponent extends TopComponent {

    private static final int PREVIEW_DETAILS_REFRESH_DELAY = 300;
    private static final int TABLE_REFRESH_PERIOD = 60000;
    private final NotificationCenterManager notificationManager;
    private JPanel detailsPanel;
    private NotificationTable notificationTable;
    private Timer previewRefreshTimer;
    private JScrollPane notificationScroll;
    private final Timer tableRefreshTimer;
    private final NotificationTable.ProcessKeyEventListener tableKeyListener;
    private JPanel pnlSearch;
    private JToggleButton btnSearch;
    private QuickSearch quickSearch;
    private final QuickSearch.Callback filterCallback;
    private JLabel lblEmptyDetails;
    private final Font italicFont;

    public NotificationCenterTopComponent() {
        notificationManager = NotificationCenterManager.getInstance();
        filterCallback = new QuickFilterCallback();
        tableRefreshTimer = new Timer(TABLE_REFRESH_PERIOD, new RefreshTimerListener());
        tableRefreshTimer.stop();
        tableKeyListener = new TableKeyListener();
        italicFont = new JLabel().getFont().deriveFont(Font.ITALIC);
        setName(NbBundle.getMessage(NotificationCenterTopComponent.class, "CTL_NotificationCenterTopComponent"));
        setToolTipText(NbBundle.getMessage(NotificationCenterTopComponent.class, "HINT_NotificationCenterTopComponent"));
    }

    private void init() {
        initComponents();
        detailsPanel = new JPanel(new GridLayout(1, 1));
        Color color = Utils.getTextBackground();
        detailsPanel.setBackground(new Color(color.getRed(), color.getGreen(), color.getBlue()));
        lblEmptyDetails = new JLabel(NbBundle.getMessage(NotificationCenterTopComponent.class, "LBL_EmptyDetails"), JLabel.CENTER);
        lblEmptyDetails.setFont(italicFont);
        lblEmptyDetails.setEnabled(false);

        JScrollPane scrollPane = new JScrollPane(detailsPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(10);
        scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
        splitPane.setRightComponent(scrollPane);

        toolBar.setFocusable(false);
        toolBar.setFloatable(false);
        btnSearch = new JToggleButton(ImageUtilities.loadImageIcon("org/netbeans/modules/notifications/resources/find16.png", true));
        btnSearch.setToolTipText(NbBundle.getMessage(NotificationCenterTopComponent.class, "LBL_SearchToolTip"));
        btnSearch.setFocusable(false);
        btnSearch.setSelected(NotificationSettings.isSearchVisible());
        //TODO delete 2 lines then quick search API clear text correctly
//        btnSearch.setToolTipText("Disabled due to Quick Search API defects");
//        btnSearch.setEnabled(false);
        btnSearch.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setSearchVisible(btnSearch.isSelected());
            }
        });
        toolBar.add(btnSearch);
        toolBar.add(new FiltersMenuButton(notificationManager.getActiveFilter()));

        initLeft();
        showDetails();
    }

    private void initLeft() {
        final JPanel pnlLeft = new JPanel(new GridBagLayout());
        notificationTable = (NotificationTable) notificationManager.getComponent();
        initNotificationTable();
        notificationScroll = new JScrollPane(notificationTable);

        pnlSearch = new JPanel(new GridBagLayout());
        GridBagConstraints searchConstrains = new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0);
        quickSearch = QuickSearch.attach(pnlSearch, searchConstrains, filterCallback, true);
        pnlSearch.add(new JLabel(), new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        setSearchVisible(btnSearch.isSelected());

        pnlLeft.add(pnlSearch, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
        pnlLeft.add(notificationScroll, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.NORTHWEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        splitPane.setLeftComponent(pnlLeft);
    }

    private void initNotificationTable() {
        notificationTable.getActionMap().put("delete", new AbstractAction() { // NOI18N
            @Override
            public void actionPerformed(ActionEvent e) {
                NotificationImpl notification = getSelectedNotification();
                if (notification != null) {
                    notification.clear();
                }
            }
        });
        notificationTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                scheduleDetailsRefresh();
            }
        });
    }

    private void updateTableColumnSizes() {
        ETable table = notificationTable;
        Font font = notificationScroll.getFont();
        FontMetrics fm = notificationScroll.getFontMetrics(font.deriveFont(Font.BOLD));
        int maxCharWidth = fm.charWidth('A'); // NOI18N
        int inset = 10;
        TableColumnModel columnModel = table.getColumnModel();

        TableColumn priorityColumn = columnModel.getColumn(0);
        String priorName = priorityColumn.getHeaderValue().toString();
        priorityColumn.setPreferredWidth(fm.stringWidth(priorName) + inset);

        TableColumn dateColumn = columnModel.getColumn(2);
        dateColumn.setPreferredWidth(15 * maxCharWidth + inset);

        TableColumn categoryColumn = columnModel.getColumn(3);
        categoryColumn.setPreferredWidth(7 * maxCharWidth + inset);

        TableColumn messageColumn = columnModel.getColumn(1);
        Border border = notificationScroll.getBorder();
        Insets insets;
        if (border != null) {
            insets = border.getBorderInsets(notificationScroll);
        } else {
            insets = new Insets(0, 0, 0, 0);
        }
        int remainingWidth = notificationScroll.getParent().getWidth() - insets.left - insets.right;
        remainingWidth -= 3 * columnModel.getColumnMargin();
        remainingWidth -= priorityColumn.getPreferredWidth();
        remainingWidth -= dateColumn.getPreferredWidth();
        remainingWidth -= categoryColumn.getPreferredWidth();
        messageColumn.setPreferredWidth(remainingWidth);
    }

    private NotificationImpl getSelectedNotification() {
        int selectedRowIndex = notificationTable.convertRowIndexToModel(notificationTable.getSelectedRow());
        if (selectedRowIndex != -1 && selectedRowIndex < notificationTable.getRowCount()) {
            return ((NotificationTableModel) notificationTable.getModel()).getEntry(selectedRowIndex);
        }
        return null;
    }

    private void scheduleDetailsRefresh() {
        if (previewRefreshTimer == null) {
            previewRefreshTimer = new Timer(PREVIEW_DETAILS_REFRESH_DELAY, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    showDetails();
                }
            });
            previewRefreshTimer.setRepeats(false);
        }
        previewRefreshTimer.restart();
    }

    private void showDetails() {
        NotificationImpl selected = getSelectedNotification();
        detailsPanel.removeAll();
        if (selected != null) {
            selected.markAsRead(true);
            JComponent popupComponent = selected.getDetailsComponent();
            detailsPanel.add(popupComponent);
        } else {
            detailsPanel.add(lblEmptyDetails);
        }
        detailsPanel.revalidate();
        detailsPanel.repaint();
    }

    private void setSearchVisible(boolean visible) {
        quickSearch.setAlwaysShown(visible);
        if (visible != btnSearch.isSelected()) {
            btnSearch.setSelected(visible);
        }
        this.revalidate();
        this.repaint();
        NotificationSettings.setSearchVisible(visible);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        splitPane = new javax.swing.JSplitPane();
        toolBar = new javax.swing.JToolBar();

        setLayout(new java.awt.GridBagLayout());

        splitPane.setContinuousLayout(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(splitPane, gridBagConstraints);

        toolBar.setOrientation(javax.swing.SwingConstants.VERTICAL);
        toolBar.setRollover(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weighty = 1.0;
        add(toolBar, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables

    @Override
    public void componentOpened() {
        removeAll();
        init();
        notificationTable.addProcessKeyEventListener(tableKeyListener);
        tableRefreshTimer.restart();
    }

    @Override
    public void componentClosed() {
        NotificationCenterManager.tcClosed();
        notificationTable.removeProcessKeyEventListener(tableKeyListener);
        tableRefreshTimer.stop();
    }

    @Override
    public void addNotify() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                splitPane.setDividerLocation(0.6);
                splitPane.validate(); // Have to validate to properly update column sizes
                updateTableColumnSizes();
            }
        });
        super.addNotify();
    }

    /**
     * Defines task list Help ID
     */
    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx(NotificationCenterTopComponent.class);
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    private class TableKeyListener implements NotificationTable.ProcessKeyEventListener {

        @Override
        public void processKeyEvent(KeyEvent e) {
            quickSearch.processKeyEvent(e);
        }
    }

    private class RefreshTimerListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            notificationTable.revalidate();
            notificationTable.repaint();
        }
    }

    private class QuickFilterCallback implements QuickSearch.Callback {

        @Override
        public void quickSearchUpdate(String searchText) {
            notificationManager.setMessageFilter(searchText);
            if (quickSearch != null && !quickSearch.isAlwaysShown()) {
                setSearchVisible(true);
            }
        }

        @Override
        public void showNextSelection(boolean forward) {
            notificationTable.showNextSelection(forward);
        }

        @Override
        public String findMaxPrefix(String prefix) {
            return prefix;
        }

        @Override
        public void quickSearchConfirmed() {
        }

        @Override
        public void quickSearchCanceled() {
            notificationManager.setMessageFilter(null);
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    notificationTable.requestFocusInWindow();
                }
            });
        }
    }
}
