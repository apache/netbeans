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
package org.netbeans.modules.db.dataview.output;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultRowSorter;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import org.netbeans.modules.db.dataview.table.JXTableRowHeader;
import org.netbeans.modules.db.dataview.table.MultiColPatternFilter;
import org.netbeans.modules.db.dataview.table.ResultSetJXTable;
import static org.netbeans.modules.db.dataview.table.SuperPatternFilter.MODE.LITERAL_FIND;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 * DataViewUI hosting display of design-level SQL test output.
 *
 * @author Ahimanikya Satapathy
 */
@NbBundle.Messages({
    "LBL_fetched_rows=Fetched Rows:"
})
class DataViewUI extends JPanel {
    private static final String IMG_PREFIX = "org/netbeans/modules/db/dataview/images/"; // NOI18N
    
    private final JButton[] editButtons = new JButton[5];
    private final DataViewTableUI dataPanel;
    private final JScrollPane dataPanelScrollPane;
    private final DataViewPageContext pageContext;
    private final DataViewActionHandler actionHandler;
    
    private JButton commit;
    private JButton refreshButton;
    private JButton truncateButton;
    private JButton deleteRow;
    private JButton insert;
    private JTextField refreshField;
    private JTextField matchBoxField;
    private JLabel fetchedRowsLabel;
    private JLabel limitRow;
    private JButton cancel;

    private final PropertyChangeListener pageContextListener =
            new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    updateFetchedLabel();
                }
            };

    /** Shared mouse listener used for setting the border painting property
     * of the toolbar buttons and for invoking the popup menu.
     */
    private static final MouseListener sharedMouseListener = new org.openide.awt.MouseUtils.PopupMouseAdapter() {

        @Override
        public void mouseEntered(MouseEvent evt) {
            Object src = evt.getSource();

            if (src instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) evt.getSource();
                if (button.isEnabled()) {
                    button.setContentAreaFilled(true);
                    button.setBorderPainted(true);
                }
            }
        }

        @Override
        public void mouseExited(MouseEvent evt) {
            Object src = evt.getSource();
            if (src instanceof AbstractButton) {
                AbstractButton button = (AbstractButton) evt.getSource();
                button.setContentAreaFilled(false);
                button.setBorderPainted(false);
            }
        }

        @Override
        protected void showPopup(MouseEvent evt) {
        }
    };
    
    private final ActionListener columnVisibilityToggler = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            JCheckBox source = (JCheckBox) e.getSource();
            int index = Integer.parseInt(e.getActionCommand());
            Set<Integer> currentVisibleColumns = dataPanel.getVisibleColumns();
            if (source.isSelected()) {
                currentVisibleColumns.add(index);
            } else {
                currentVisibleColumns.remove(index);
            }
            dataPanel.setVisibleColumns(currentVisibleColumns);
        }
    };
    
    private final ActionListener fitColumnWidthToggler = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (((JCheckBox) e.getSource()).isSelected()) {
                dataPanel.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
            } else {
                dataPanel.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            }
        }
    };
    
    private final ActionListener popupActionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JButton button = (JButton) e.getSource();
                JCheckBox firstEntry = null;
                JPopupMenu popupMenu = new JPopupMenu();

                JPanel menuPanel = new JPanel();
                menuPanel.setFocusCycleRoot(true);
                popupMenu.add(menuPanel);
                menuPanel.setLayout(new GridBagLayout());
                GridBagConstraints constraints = new GridBagConstraints();
                constraints.fill = GridBagConstraints.HORIZONTAL;
                constraints.anchor = GridBagConstraints.BASELINE_LEADING;
                constraints.weightx = 1;
                constraints.gridx = 0;
                
                Set<Integer> visibleColumns = dataPanel.getVisibleColumns();
                DataViewTableUIModel dvtm = dataPanel.getModel();
                
                for(int i = 0; i < dvtm.getColumnCount(); i++) {
                    JCheckBox columnEntry = new JCheckBox(dvtm.getColumnName(i));
                    columnEntry.setActionCommand(Integer.toString(i));
                    columnEntry.setSelected(visibleColumns.contains(i));
                    columnEntry.addActionListener(columnVisibilityToggler);
                    constraints.gridy += 1;
                    menuPanel.add(columnEntry, constraints);
                    if(firstEntry == null) {
                        firstEntry = columnEntry;
                    }
                }
                
                constraints.gridy += 1;
                menuPanel.add(new JSeparator(), constraints);
                
                JCheckBox checkboxItem = new JCheckBox("Fit column width");
                checkboxItem.setSelected(dataPanel.getAutoResizeMode() != JTable.AUTO_RESIZE_OFF);
                checkboxItem.addActionListener(fitColumnWidthToggler);
                
                constraints.gridy += 1;
                menuPanel.add(checkboxItem, constraints);
                
                popupMenu.show(button, 0, button.getHeight());
                if(firstEntry == null) {
                    checkboxItem.requestFocus();
                } else {
                    firstEntry.requestFocus();
                }
            }
        };

    @SuppressWarnings("OverridableMethodCallInConstructor")
    DataViewUI(DataView dataView, DataViewPageContext pageContext, boolean nbOutputComponent) {
        assert SwingUtilities.isEventDispatchThread() : "Must be called from AWT thread";  //NOI18N

        this.pageContext = pageContext;

        //do not show tab view if there is only one tab
        this.putClientProperty("TabPolicy", "HideWhenAlone"); //NOI18N
        this.putClientProperty("PersistenceType", "Never"); //NOI18N

        this.setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder());

        // Main pannel with toolbars
        JPanel panel = initializeMainPanel(nbOutputComponent);
        this.add(panel, BorderLayout.NORTH);

        actionHandler = new DataViewActionHandler(this, dataView, pageContext);

        //add resultset data panel
        dataPanel = new DataViewTableUI(this, actionHandler, dataView, pageContext);
        dataPanelScrollPane = new JScrollPane(dataPanel);
        dataPanelScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        dataPanelScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        JXTableRowHeader rowHeader = new JXTableRowHeader(dataPanel);
        dataPanelScrollPane.setRowHeaderView(rowHeader);
        dataPanelScrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, rowHeader.getTableHeader());
        
        Icon icon = ImageUtilities.loadIcon(IMG_PREFIX + "preferences-desktop.png", false);  // NOI18N
        JButton cornerButton = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                int iconSize = Math.min(getWidth(), getHeight());
                Graphics2D g2d = (Graphics2D) g;
                g2d.setColor(getBackground());
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                icon.paintIcon(null, g2d, (getWidth() - iconSize) / 2, (getHeight() - iconSize) / 2);
            }
        };
        cornerButton.addActionListener(popupActionListener);
        dataPanelScrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, cornerButton);

        this.add(dataPanelScrollPane, BorderLayout.CENTER);
        dataPanel.revalidate();
        dataPanel.repaint();

        dataPanel.setModel(pageContext.getModel());
        pageContext.addPropertyChangeListener(pageContextListener);
        updateFetchedLabel();
    }

    void handleColumnUpdated() {
        boolean editMode = dataPanel.getModel().hasUpdates();
        commit.setEnabled(editMode);
        cancel.setEnabled(editMode);
        insert.setEnabled(!editMode);
        deleteRow.setEnabled(!editMode);
        truncateButton.setEnabled(!editMode);
    }

    JButton[] getEditButtons() {
        return editButtons;
    }

    final void updateFetchedLabel() {
        assert SwingUtilities.isEventDispatchThread() : "Must be called from AWT thread";  //NOI18N

        fetchedRowsLabel.setText(Integer.toString(pageContext.getModel().getRowCount()));
    }

    boolean isCommitEnabled() {
        return commit.isEnabled();
    }

    DataViewTableUI getDataViewTableUI() {
        return dataPanel;
    }

    DataViewTableUIModel getDataViewTableUIModel() {
        return dataPanel.getModel();
    }

    void setCommitEnabled(boolean flag) {
        commit.setEnabled(flag);
    }

    void setCancelEnabled(boolean flag) {
        cancel.setEnabled(flag);
    }

    void disableButtons() {
        assert SwingUtilities.isEventDispatchThread() : "Must be called from AWT thread";  //NOI18N

        truncateButton.setEnabled(false);
        refreshButton.setEnabled(false);
        refreshField.setEnabled(false);
        matchBoxField.setEditable(false);

        deleteRow.setEnabled(false);
        commit.setEnabled(false);
        cancel.setEnabled(false);
        insert.setEnabled(false);

        dataPanel.revalidate();
        dataPanel.repaint();
    }

    int getPageSize() {
        int pageSize = pageContext.getPageSize();
        try {
            int count = Integer.parseInt(refreshField.getText().trim());
            return count < 0 ? pageSize : count;
        } catch (NumberFormatException ex) {
            return pageSize;
        }
    }

    boolean isDirty() {
        return dataPanel.getModel().hasUpdates();
    }

    void resetToolbar(boolean wasError) {
        assert SwingUtilities.isEventDispatchThread() : "Must be called from AWT thread";  //NOI18N

        refreshButton.setEnabled(true);
        refreshField.setEnabled(true);
        matchBoxField.setEditable(true);
        deleteRow.setEnabled(false);
        if (!wasError) {
            // editing controls
            if (! dataPanel.getModel().isEditable()) {
                commit.setEnabled(false);
                cancel.setEnabled(false);
                deleteRow.setEnabled(false);
                insert.setEnabled(false);
                truncateButton.setEnabled(false);
            } else {
                if (pageContext.hasRows()) {
                    truncateButton.setEnabled(true);
                } else {
                    deleteRow.setEnabled(false);
                    truncateButton.setEnabled(false);
                    pageContext.first();
                }
                insert.setEnabled(true);
                if (getDataViewTableUIModel().getUpdateKeys().isEmpty()) {
                    commit.setEnabled(false);
                    cancel.setEnabled(false);
                } else {
                    commit.setEnabled(true);
                    cancel.setEnabled(true);
                }
            }
        } else {
            disableButtons();
        }

        refreshField.setText("" + pageContext.getPageSize());
        if (dataPanel != null) {
            dataPanel.revalidate();
            dataPanel.repaint();
        }
        
        updateFetchedLabel();
    }

    private ActionListener createOutputListener() {

        ActionListener outputListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                Object src = e.getSource();
                if (src.equals(refreshButton)) {
                    actionHandler.refreshActionPerformed();
                } else if (src.equals(refreshField)) {
                    actionHandler.updateActionPerformed();
                } else if (src.equals(commit)) {
                    actionHandler.commitActionPerformed(false);
                } else if (src.equals(cancel)) {
                    actionHandler.cancelEditPerformed(false);
                } else if (src.equals(deleteRow)) {
                    actionHandler.deleteRecordActionPerformed();
                } else if (src.equals(insert)) {
                    actionHandler.insertActionPerformed();
                } else if (src.equals(truncateButton)) {
                    actionHandler.truncateActionPerformed();
                }
            }
        };

        return outputListener;
    }
    private static final Insets BUTTON_INSETS = new Insets(2, 1, 0, 1);

    private void processButton(AbstractButton button) {
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setMargin(BUTTON_INSETS);
        if (button instanceof AbstractButton) {
            button.addMouseListener(sharedMouseListener);
        }
        //Focus shouldn't stay in toolbar
        button.setFocusable(false);
    }

    private void initToolbarWest(JToolBar toolbar, ActionListener outputListener, boolean nbOutputComponent) {

        if (!nbOutputComponent) {
            JButton[] btns = getEditButtons();
            for (JButton btn : btns) {
                if (btn != null) {
                    toolbar.add(btn);
                }
            }
        }

        toolbar.addSeparator(new Dimension(10, 10));

        //add refresh button
        refreshButton = new JButton(ImageUtilities.loadIcon(IMG_PREFIX + "refresh.png")); // NOI18N
        refreshButton.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_refresh"));
        refreshButton.addActionListener(outputListener);
        processButton(refreshButton);

        toolbar.add(refreshButton);

        //add limit row label
        limitRow = new JLabel(NbBundle.getMessage(DataViewUI.class, "LBL_max_rows"));
        limitRow.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 8));
        toolbar.add(limitRow);

        //add refresh text field
        refreshField = new JTextField(5);
        refreshField.setMinimumSize(refreshField.getPreferredSize());
        refreshField.setMaximumSize(refreshField.getPreferredSize());
        refreshField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                refreshField.selectAll();
            }
        });
        refreshField.addActionListener(outputListener);
        toolbar.add(refreshField);
        toolbar.addSeparator(new Dimension(10, 10));

        JLabel fetchedRowsNameLabel = new JLabel(NbBundle.getMessage(DataViewUI.class, "LBL_fetched_rows"));
        fetchedRowsNameLabel.getAccessibleContext().setAccessibleName(NbBundle.getMessage(DataViewUI.class, "LBL_fetched_rows"));
        fetchedRowsNameLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        toolbar.add(fetchedRowsNameLabel);
        fetchedRowsLabel = new JLabel();
        toolbar.add(fetchedRowsLabel);

        toolbar.addSeparator(new Dimension(10, 10));
    }

    private void initToolbarEast(JToolBar toolbar) {
        // match box labble
        JLabel matchBoxRow = new JLabel(NbBundle.getMessage(DataViewUI.class, "LBL_matchbox"));
        matchBoxRow.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 8));
        toolbar.add(matchBoxRow);

        //add matchbox text field
        matchBoxField = new JTextField(10);
        matchBoxField.setText(""); // NOI18N
        matchBoxField.setMinimumSize(new Dimension(35, matchBoxField.getHeight()));
        matchBoxField.setSize(35, matchBoxField.getHeight());

        matchBoxField.addKeyListener(new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                processKeyEvents();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                processKeyEvents();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                processKeyEvents();
            }
        });
        toolbar.add(matchBoxField);
    }

    private void processKeyEvents() {
        ResultSetJXTable table = getDataViewTableUI();
        int[] rows = new int[table.getColumnCount()];
        for (int i = 0; i < table.getColumnCount(); i++) {
            rows[i] = i;
        }
        {
            MultiColPatternFilter filterP = new MultiColPatternFilter(rows);
            filterP.setFilterStr(matchBoxField.getText(), LITERAL_FIND);
            ((DefaultRowSorter) table.getRowSorter()).setRowFilter(filterP);
        }
    }

    private void initVerticalToolbar(ActionListener outputListener) {
        insert = new JButton(ImageUtilities.loadIcon(IMG_PREFIX + "row_add.png")); // NOI18N
        insert.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_insert")+" (Alt+I)");
        insert.setMnemonic('I');
        insert.addActionListener(outputListener);
        insert.setEnabled(false);
        processButton(insert);
        editButtons[0] = insert;

        deleteRow = new JButton(ImageUtilities.loadIcon(IMG_PREFIX + "row_delete.png")); // NOI18N
        deleteRow.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_deleterow"));
        deleteRow.addActionListener(outputListener);
        deleteRow.setEnabled(false);
        processButton(deleteRow);
        editButtons[1] = deleteRow;

        commit = new JButton(ImageUtilities.loadIcon(IMG_PREFIX + "row_commit.png")); // NOI18N
        commit.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_commit_all"));
        commit.addActionListener(outputListener);
        commit.setEnabled(false);
        processButton(commit);
        editButtons[2] = commit;

        cancel = new JButton(ImageUtilities.loadIcon(IMG_PREFIX + "cancel_edits.png")); // NOI18N
        cancel.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_cancel_edits_all"));
        cancel.addActionListener(outputListener);
        cancel.setEnabled(false);
        processButton(cancel);
        editButtons[3] = cancel;

        //add truncate button
        truncateButton = new JButton(ImageUtilities.loadIcon(IMG_PREFIX + "table_truncate.png")); // NOI18N
        truncateButton.setToolTipText(NbBundle.getMessage(DataViewUI.class, "TOOLTIP_truncate_table")+" (Alt+T)");
        truncateButton.setMnemonic('T');
        truncateButton.addActionListener(outputListener);
        truncateButton.setEnabled(false);
        processButton(truncateButton);
        editButtons[4] = truncateButton;
    }

    private JPanel initializeMainPanel(boolean nbOutputComponent) {

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEtchedBorder());
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));

        ActionListener outputListener = createOutputListener();
        initVerticalToolbar(outputListener);

        JToolBar toolbarWest = new JToolBar();
        toolbarWest.setFloatable(false);
        toolbarWest.setRollover(true);
        initToolbarWest(toolbarWest, outputListener, nbOutputComponent);
        
        JToolBar toolbarEast = new JToolBar();
        toolbarEast.setFloatable(false);
        toolbarEast.setRollover(true);
        initToolbarEast(toolbarEast);
        toolbarEast.setMinimumSize(toolbarWest.getPreferredSize());
        toolbarEast.setSize(toolbarWest.getPreferredSize());
        toolbarEast.setMaximumSize(toolbarWest.getPreferredSize());

        panel.add(toolbarWest);
        panel.add(Box.createHorizontalGlue());
        panel.add(toolbarEast);

        return panel;
    }

    public void enableDeleteBtn(boolean value) {
        deleteRow.setEnabled(value);
    }
}
