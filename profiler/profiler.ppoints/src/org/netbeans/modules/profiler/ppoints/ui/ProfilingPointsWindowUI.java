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

package org.netbeans.modules.profiler.ppoints.ui;

import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.modules.profiler.api.ProfilerIDESettings;
import org.netbeans.modules.profiler.ppoints.CodeProfilingPoint;
import org.netbeans.modules.profiler.ppoints.ProfilingPoint;
import org.netbeans.modules.profiler.ppoints.ProfilingPointsManager;
import org.netbeans.modules.profiler.ppoints.Utils;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import org.netbeans.lib.profiler.ui.components.ProfilerToolbar;
import org.netbeans.lib.profiler.ui.swing.ProfilerTable;
import org.netbeans.lib.profiler.ui.swing.ProfilerTableContainer;
import org.netbeans.modules.profiler.api.ProfilerDialogs;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.openide.util.Lookup;


/**
 *
 * @author Jiri Sedlacek
 */
@NbBundle.Messages({
    "ProfilingPointsWindowUI_AllProjectsString=All Projects",
    "ProfilingPointsWindowUI_ProjectLabelText=Pr&oject:",
    "ProfilingPointsWindowUI_InclSubprojCheckboxText=in&clude open subprojects",
    "ProfilingPointsWindowUI_AddButtonToolTip=Add Profiling Point",
    "ProfilingPointsWindowUI_RemoveButtonToolTip=Delete Profiling Point(s)",
    "ProfilingPointsWindowUI_EditButtonToolTip=Edit Profiling Point",
    "ProfilingPointsWindowUI_DisableButtonToolTip=Enable/Disable Profiling Point(s)",
    "ProfilingPointsWindowUI_ShowSourceItemText=Show in Source",
    "ProfilingPointsWindowUI_ShowStartItemText=Show Start in Source",
    "ProfilingPointsWindowUI_ShowEndItemText=Show End in Source",
    "ProfilingPointsWindowUI_ShowReportItemText=Show Report",
    "ProfilingPointsWindowUI_EnableItemText=Enable",
    "ProfilingPointsWindowUI_DisableItemText=Disable",
    "ProfilingPointsWindowUI_EnableDisableItemText=Enable/Disable",
    "ProfilingPointsWindowUI_EditItemText=Edit",
    "ProfilingPointsWindowUI_RemoveItemText=Delete",
    "ProfilingPointsWindowUI_ScopeColumnName=Scope",
    "ProfilingPointsWindowUI_ProjectColumnName=Project",
    "ProfilingPointsWindowUI_PpColumnName=Profiling Point",
    "ProfilingPointsWindowUI_ResultsColumnName=Results",
    "ProfilingPointsWindowUI_ScopeColumnToolTip=Profiling Point scope",
    "ProfilingPointsWindowUI_ProjectColumnToolTip=Project for which the Profiling Point is defined",
    "ProfilingPointsWindowUI_PpColumnToolTip=Profiling Point",
    "ProfilingPointsWindowUI_ResultsColumnToolTip=Data or current state of the Profiling Point",
    "ProfilingPointsWindowUI_NoStartDefinedMsg=No start point defined for this Profiling Point",
    "ProfilingPointsWindowUI_NoEndDefinedMsg=No end point defined for this Profiling Point"
})
public class ProfilingPointsWindowUI extends JPanel implements ActionListener, ListSelectionListener, PropertyChangeListener,
                                                               MouseListener, MouseMotionListener, KeyListener {
    //~ Static fields/initializers -----------------------------------------------------------------------------------------------
    private static final Icon PPOINT_ADD_ICON = Icons.getIcon(ProfilingPointsIcons.ADD);
    private static final Icon PPOINT_REMOVE_ICON = Icons.getIcon(ProfilingPointsIcons.REMOVE);
    private static final Icon PPOINT_EDIT_ICON = Icons.getIcon(ProfilingPointsIcons.EDIT);
    private static final Icon PPOINT_ENABLE_DISABLE_ICON = Icons.getIcon(ProfilingPointsIcons.ENABLE_DISABLE);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------

    private AbstractTableModel profilingPointsTableModel;
    private JButton addButton;
    private JButton disableButton;
    private JButton editButton;
    private JButton removeButton;
    private JCheckBox dependenciesCheckbox;
    private ProjectSelector ppointProjectSelector;
    private ProfilerTable profilingPointsTable;
    private JLabel projectLabel;
    private JMenuItem disableItem;
    private JMenuItem editItem;
    private JMenuItem enableDisableItem;
    private JMenuItem enableItem;
    private JMenuItem removeItem;
    private JMenuItem showEndInSourceItem;
    private JMenuItem showInSourceItem;
    private JMenuItem showReportItem;
    private JMenuItem showStartInSourceItem;
    private JPopupMenu profilingPointsPopup;
    private ProfilingPoint[] profilingPoints = new ProfilingPoint[0];
    private boolean profilingInProgress = false;

    //~ Constructors -------------------------------------------------------------------------------------------------------------

    public ProfilingPointsWindowUI() {
        initComponents();
        updateProjectsCombo();
        notifyProfilingStateChanged();
        ProfilingPointsManager.getDefault().addPropertyChangeListener(this);
    }
    

    public Lookup.Provider getSelectedProject() {
        return ppointProjectSelector.getProject();
    }

    public void actionPerformed(ActionEvent e) {
       if (e.getSource() == addButton) {
            SystemAction.get(InsertProfilingPointAction.class).performAction(getSelectedProject());
        } else if (e.getSource() == removeButton) {
            int[] selectedRows = profilingPointsTable.getSelectedRows();
            ProfilingPoint[] selectedProfilingPoints = new ProfilingPoint[selectedRows.length];

            for (int i = 0; i < selectedRows.length; i++) {
                selectedProfilingPoints[i] = getProfilingPointAt(selectedRows[i]);
            }

            ProfilingPointsManager.getDefault().removeProfilingPoints(selectedProfilingPoints);
        } else if (e.getSource() == editButton) {
            ProfilingPoint selectedProfilingPoint = getProfilingPointAt(profilingPointsTable.getSelectedRow());
            selectedProfilingPoint.customize(false, false);
        } else if (e.getSource() == disableButton) {
            int[] selectedRows = profilingPointsTable.getSelectedRows();

            for (int i : selectedRows) {
                ProfilingPoint selectedProfilingPoint = getProfilingPointAt(i);
                selectedProfilingPoint.setEnabled(!selectedProfilingPoint.isEnabled());
                repaint();
            }
        } else if (e.getSource() == showInSourceItem) {
            CodeProfilingPoint selectedProfilingPoint = (CodeProfilingPoint) getProfilingPointAt(profilingPointsTable
                                                                                                                                                                                                                                                                       .getSelectedRow());
            Utils.openLocation(selectedProfilingPoint.getLocation());
        } else if (e.getSource() == showStartInSourceItem) {
            CodeProfilingPoint.Paired selectedProfilingPoint = (CodeProfilingPoint.Paired) getProfilingPointAt(profilingPointsTable
                                                                                                               .getSelectedRow());
            CodeProfilingPoint.Location location = selectedProfilingPoint.getStartLocation();

            if (location == null) {
                ProfilerDialogs.displayWarning(
                        Bundle.ProfilingPointsWindowUI_NoStartDefinedMsg());
            } else {
                Utils.openLocation(location);
            }
        } else if (e.getSource() == showEndInSourceItem) {
            CodeProfilingPoint.Paired selectedProfilingPoint = (CodeProfilingPoint.Paired) getProfilingPointAt(profilingPointsTable
                                                                                                               .getSelectedRow());
            CodeProfilingPoint.Location location = selectedProfilingPoint.getEndLocation();

            if (location == null) {
                ProfilerDialogs.displayWarning(
                        Bundle.ProfilingPointsWindowUI_NoEndDefinedMsg());
            } else {
                Utils.openLocation(location);
            }
        } else if (e.getSource() == showReportItem) {
            int[] selectedRows = profilingPointsTable.getSelectedRows();

            if (selectedRows.length == 0) {
                return;
            }

            for (int selectedRow : selectedRows) {
                ProfilingPoint selectedProfilingPoint = getProfilingPointAt(selectedRow);
                selectedProfilingPoint.showResults(null);
            }
        } else if (e.getSource() == enableItem) {
            int selectedRow = profilingPointsTable.getSelectedRow();

            if (selectedRow == -1) {
                return;
            }

            ProfilingPoint selectedProfilingPoint = getProfilingPointAt(selectedRow);
            selectedProfilingPoint.setEnabled(true);
        } else if (e.getSource() == disableItem) {
            int selectedRow = profilingPointsTable.getSelectedRow();

            if (selectedRow == -1) {
                return;
            }

            ProfilingPoint selectedProfilingPoint = getProfilingPointAt(selectedRow);
            selectedProfilingPoint.setEnabled(false);
        } else if (e.getSource() == enableDisableItem) {
            int[] selectedRows = profilingPointsTable.getSelectedRows();

            if (selectedRows.length == 0) {
                return;
            }

            for (int selectedRow : selectedRows) {
                ProfilingPoint selectedProfilingPoint = getProfilingPointAt(selectedRow);
                selectedProfilingPoint.setEnabled(!selectedProfilingPoint.isEnabled());
            }
        } else if (e.getSource() == editItem) {
            int selectedRow = profilingPointsTable.getSelectedRow();

            if (selectedRow == -1) {
                return;
            }

            ProfilingPoint selectedProfilingPoint = getProfilingPointAt(selectedRow);
            selectedProfilingPoint.customize(false, false);
        } else if (e.getSource() == removeItem) {
            deletePPs();
        }
    }

    public void keyPressed(KeyEvent e) {
        if ((e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU)
                || ((e.getKeyCode() == KeyEvent.VK_F10) && (e.getModifiers() == InputEvent.SHIFT_MASK))) {
            int[] selectedRows = profilingPointsTable.getSelectedRows();

            if (selectedRows.length != 0) {
                Rectangle rowBounds = profilingPointsTable.getCellRect(selectedRows[0], 1, true);
                showProfilingPointsPopup(e.getComponent(), rowBounds.x + 20,
                                         rowBounds.y + (profilingPointsTable.getRowHeight() / 2));
            }
        } else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
            deletePPs();
        }
    }

    public void keyReleased(KeyEvent e) {
    }

    public void keyTyped(KeyEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
        if (e.getModifiers() == InputEvent.BUTTON1_MASK) {
            int clickedRow = profilingPointsTable.rowAtPoint(e.getPoint());

            if ((clickedRow != -1) && (e.getClickCount() == 2)) {
                ProfilingPoint profilingPoint = getProfilingPointAt(clickedRow);

                if (profilingPoint instanceof CodeProfilingPoint) {
                    Utils.openLocation(((CodeProfilingPoint) profilingPoint).getLocation());
                }
            }
        } else if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
            int clickedRow = profilingPointsTable.rowAtPoint(e.getPoint());
            int selectedRowCount = profilingPointsTable.getSelectedRowCount();
            if ((clickedRow != -1) && (selectedRowCount != 0)) {
                if (selectedRowCount == 1)
                    profilingPointsTable.setRowSelectionInterval(clickedRow, clickedRow);
                showProfilingPointsPopup(e.getComponent(), e.getX(), e.getY());

                return;
            }
        }

        dispatchResultsRendererEvent(e);
    }

    public void mouseDragged(MouseEvent e) {
        dispatchResultsRendererEvent(e);
    }

    public void mouseEntered(MouseEvent e) {
        dispatchResultsRendererEvent(e);
    }

    public void mouseExited(MouseEvent e) {
        dispatchResultsRendererEvent(e);
    }

    public void mouseMoved(MouseEvent e) {
        dispatchResultsRendererEvent(e);
    }

    public void mousePressed(MouseEvent e) {
        if (e.getModifiers() == InputEvent.BUTTON3_MASK) {
            int clickedRow = profilingPointsTable.rowAtPoint(e.getPoint());

            if (clickedRow != -1) {
                int[] selectedRows = profilingPointsTable.getSelectedRows();

                if (selectedRows.length == 0) {
                    profilingPointsTable.setRowSelectionInterval(clickedRow, clickedRow);
                } else {
                    boolean changeSelection = true;

                    for (int selectedRow : selectedRows) {
                        if (selectedRow == clickedRow) {
                            changeSelection = false;
                        }
                    }

                    if (changeSelection) {
                        profilingPointsTable.setRowSelectionInterval(clickedRow, clickedRow);
                    }
                }
            }
        }

        dispatchResultsRendererEvent(e);
    }

    public void mouseReleased(MouseEvent e) {
        dispatchResultsRendererEvent(e);
    }

    public void notifyProfilingStateChanged() {
        profilingInProgress = ProfilingPointsManager.getDefault().isProfilingSessionInProgress();
        updateButtons();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName() == ProfilingPointsManager.PROPERTY_PROJECTS_CHANGED) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() { updateProjectsCombo(); } // also refreshes profiling points
            });
        } else if (evt.getPropertyName() == ProfilingPointsManager.PROPERTY_PROFILING_POINTS_CHANGED) {
            refreshProfilingPoints();
        }
    }

    public void valueChanged(ListSelectionEvent e) {
        updateButtons();
    }

    private ProfilingPoint getProfilingPointAt(int row) {
        return profilingPoints[profilingPointsTable.convertRowIndexToModel(row)];
    }

    private void createProfilingPointsTable() {
        profilingPointsTableModel = new AbstractTableModel() {
            public String getColumnName(int col) {
                if (col == 0) {
                    return Bundle.ProfilingPointsWindowUI_ScopeColumnName();
                } else if (col == 1) {
                    return Bundle.ProfilingPointsWindowUI_ProjectColumnName();
                } else if (col == 2) {
                    return Bundle.ProfilingPointsWindowUI_PpColumnName();
                } else if (col == 3) {
                    return Bundle.ProfilingPointsWindowUI_ResultsColumnName();
                }
                return null;
            }

            public int getRowCount() {
                return profilingPoints.length;
            }

            public int getColumnCount() {
                return 4;
            }

            public Class getColumnClass(int col) {
                return ProfilingPoint.class;
            }

            public Object getValueAt(int row, int col) {
                return profilingPoints[row];
            }
        };

        profilingPointsTable = new ProfilerTable(profilingPointsTableModel, true, true, null) {
            public TableCellRenderer getCellRenderer(int row, int column) {
                if (convertColumnIndexToModel(column) == 3) {
                    return (row < 0 || row >= getRowCount()) ?
                            getDefaultRenderer(String.class) : // Prevent AIOOBE when accessing non-existing PP ??
                            getProfilingPointAt(row).getResultsRenderer();
                } else {
                    return super.getCellRenderer(row, column);
                }
            }
        };
        //    profilingPointsTable.getAccessibleContext().setAccessibleName(TABLE_ACCESS_NAME);

        profilingPointsTable.setMainColumn(2);
        profilingPointsTable.setFitWidthColumn(2);
        profilingPointsTable.setDefaultSortOrder(SortOrder.ASCENDING);
        profilingPointsTable.setSortColumn(1);

        profilingPointsTable.setColumnRenderer(0, Utils.getScopeRenderer());
        profilingPointsTable.setColumnRenderer(1, Utils.getProjectRenderer());
        profilingPointsTable.setColumnRenderer(2, Utils.getPresenterRenderer());
//        profilingPointsTable.setColumnRenderer(3, null);

        profilingPointsTable.setDefaultColumnWidth(0, 50);
        profilingPointsTable.setDefaultColumnWidth(1, 165);
        profilingPointsTable.setDefaultColumnWidth(3, 200);
        profilingPointsTable.setColumnToolTips(new String[] {
            Bundle.ProfilingPointsWindowUI_ScopeColumnToolTip(), 
            Bundle.ProfilingPointsWindowUI_ProjectColumnToolTip(), 
            Bundle.ProfilingPointsWindowUI_PpColumnToolTip(), 
            Bundle.ProfilingPointsWindowUI_ResultsColumnToolTip()
        });
        profilingPointsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        profilingPointsTable.getSelectionModel().addListSelectionListener(this);
        profilingPointsTable.addMouseListener(this);
        profilingPointsTable.addMouseMotionListener(this);
        profilingPointsTable.addKeyListener(this);
    }

    private void deletePPs() {
        int[] selectedRows = profilingPointsTable.getSelectedRows();

        if (selectedRows.length == 0) {
            return;
        }

        List<ProfilingPoint> pointsToRemove = new ArrayList<>();

        for (int selectedRow : selectedRows) {
            ProfilingPoint selectedProfilingPoint = getProfilingPointAt(selectedRow);
            pointsToRemove.add(selectedProfilingPoint);
        }

        for (ProfilingPoint pointToRemove : pointsToRemove) {
            ProfilingPointsManager.getDefault().removeProfilingPoint(pointToRemove);
        }
    }

    private void dispatchResultsRendererEvent(MouseEvent e) {
        int column = profilingPointsTable.columnAtPoint(e.getPoint());

        if (column != 3) {
            //    if (column != 2) { // TODO: revert to 3 once Scope is enabled
            profilingPointsTable.setCursor(Cursor.getDefaultCursor()); // Workaround for forgotten Hand cursor from HTML renderer, TODO: fix it!

            return;
        }

        int row = profilingPointsTable.rowAtPoint(e.getPoint());

        if (row == -1) {
            return;
        }

        ProfilingPoint profilingPoint = getProfilingPointAt(row);
        ProfilingPoint.ResultsRenderer resultsRenderer = profilingPoint.getResultsRenderer();
        Rectangle cellRect = profilingPointsTable.getCellRect(row, column, true);
        resultsRenderer.dispatchMouseEvent(e, cellRect);
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        ProfilerToolbar toolbar = ProfilerToolbar.create(true);

        projectLabel = new JLabel();
        org.openide.awt.Mnemonics.setLocalizedText(projectLabel, Bundle.ProfilingPointsWindowUI_ProjectLabelText());
        projectLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

        ppointProjectSelector = new ProjectSelector(Bundle.ProfilingPointsWindowUI_AllProjectsString()) {
            protected void selectionChanged() { refreshProfilingPoints(); }
            protected int getPreferredWidth() { return 200; }
        };
        projectLabel.setLabelFor(ppointProjectSelector);
        toolbar.add(projectLabel);
        toolbar.add(ppointProjectSelector);

        if (ProfilingPointsUIHelper.get().displaySubprojectsOption()) {
            dependenciesCheckbox = new JCheckBox();
            dependenciesCheckbox.setOpaque(false);
            UIUtils.addBorder(dependenciesCheckbox, BorderFactory.createEmptyBorder(0, 4, 0, 3));
            org.openide.awt.Mnemonics.setLocalizedText(dependenciesCheckbox, Bundle.ProfilingPointsWindowUI_InclSubprojCheckboxText());
            dependenciesCheckbox.setSelected(ProfilerIDESettings.getInstance().getIncludeProfilingPointsDependencies());
            toolbar.add(dependenciesCheckbox);
            dependenciesCheckbox.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        ProfilerIDESettings.getInstance().setIncludeProfilingPointsDependencies(dependenciesCheckbox.isSelected());
                        refreshProfilingPoints();
                    }
                });
        }

        toolbar.addSeparator();

        addButton = new JButton(PPOINT_ADD_ICON);
        addButton.setToolTipText(Bundle.ProfilingPointsWindowUI_AddButtonToolTip());
        addButton.addActionListener(this);
        toolbar.add(addButton);

        removeButton = new JButton(PPOINT_REMOVE_ICON);
        removeButton.setToolTipText(Bundle.ProfilingPointsWindowUI_RemoveButtonToolTip());
        removeButton.addActionListener(this);
        toolbar.add(removeButton);

        toolbar.addSeparator();

        editButton = new JButton(PPOINT_EDIT_ICON);
        editButton.setToolTipText(Bundle.ProfilingPointsWindowUI_EditButtonToolTip());
        editButton.addActionListener(this);
        toolbar.add(editButton);

        disableButton = new JButton(PPOINT_ENABLE_DISABLE_ICON);
        disableButton.setToolTipText(Bundle.ProfilingPointsWindowUI_DisableButtonToolTip());
        disableButton.addActionListener(this);
        toolbar.add(disableButton);

        createProfilingPointsTable();

        showInSourceItem = new JMenuItem(Bundle.ProfilingPointsWindowUI_ShowSourceItemText());
        showInSourceItem.setFont(showInSourceItem.getFont().deriveFont(Font.BOLD));
        showInSourceItem.addActionListener(this);
        showStartInSourceItem = new JMenuItem(Bundle.ProfilingPointsWindowUI_ShowStartItemText());
        showStartInSourceItem.setFont(showInSourceItem.getFont().deriveFont(Font.BOLD));
        showStartInSourceItem.addActionListener(this);
        showEndInSourceItem = new JMenuItem(Bundle.ProfilingPointsWindowUI_ShowEndItemText());
        showEndInSourceItem.addActionListener(this);
        showReportItem = new JMenuItem(Bundle.ProfilingPointsWindowUI_ShowReportItemText());
        showReportItem.addActionListener(this);
        enableItem = new JMenuItem(Bundle.ProfilingPointsWindowUI_EnableItemText());
        enableItem.addActionListener(this);
        disableItem = new JMenuItem(Bundle.ProfilingPointsWindowUI_DisableItemText());
        disableItem.addActionListener(this);
        enableDisableItem = new JMenuItem(Bundle.ProfilingPointsWindowUI_EnableDisableItemText());
        enableDisableItem.addActionListener(this);
        editItem = new JMenuItem(Bundle.ProfilingPointsWindowUI_EditItemText());
        editItem.addActionListener(this);
        removeItem = new JMenuItem(Bundle.ProfilingPointsWindowUI_RemoveItemText());
        removeItem.addActionListener(this);

        profilingPointsPopup = new JPopupMenu();
        profilingPointsPopup.add(showInSourceItem);
        profilingPointsPopup.add(showStartInSourceItem);
        profilingPointsPopup.add(showEndInSourceItem);
        profilingPointsPopup.add(showReportItem);
        profilingPointsPopup.addSeparator();
        profilingPointsPopup.add(editItem);
        profilingPointsPopup.add(enableItem);
        profilingPointsPopup.add(disableItem);
        profilingPointsPopup.add(enableDisableItem);
        profilingPointsPopup.addSeparator();
        profilingPointsPopup.add(removeItem);

        add(toolbar.getComponent(), BorderLayout.NORTH);
        add(new ProfilerTableContainer(profilingPointsTable, false, null), BorderLayout.CENTER);
    }

    private void refreshProfilingPoints() {
        List<ProfilingPoint> sortedProfilingPoints = ProfilingPointsManager.getDefault().getProfilingPoints(
                getSelectedProject(), ProfilerIDESettings.getInstance().getIncludeProfilingPointsDependencies(), false);
        profilingPoints = sortedProfilingPoints.toArray(new ProfilingPoint[0]);
        profilingPointsTableModel.fireTableDataChanged();
        repaint();
    }

    private void showProfilingPointsPopup(Component source, int x, int y) {
        int[] selectedRows = profilingPointsTable.getSelectedRows();

        if (selectedRows.length == 0) {
            return;
        }

        boolean singleSelection = selectedRows.length == 1;
        ProfilingPoint selectedProfilingPoint = getProfilingPointAt(selectedRows[0]);

        showInSourceItem.setVisible(!singleSelection || selectedProfilingPoint instanceof CodeProfilingPoint.Single);
        showInSourceItem.setEnabled(singleSelection);

        showStartInSourceItem.setVisible(singleSelection && selectedProfilingPoint instanceof CodeProfilingPoint.Paired);

        showEndInSourceItem.setVisible(singleSelection && selectedProfilingPoint instanceof CodeProfilingPoint.Paired);

        showReportItem.setEnabled(true);

        enableItem.setVisible(singleSelection && !selectedProfilingPoint.isEnabled());
        enableItem.setEnabled(!profilingInProgress);

        disableItem.setVisible(singleSelection && selectedProfilingPoint.isEnabled());
        disableItem.setEnabled(!profilingInProgress);

        enableDisableItem.setVisible(!singleSelection);
        enableDisableItem.setEnabled(!profilingInProgress);

        editItem.setEnabled(singleSelection && !profilingInProgress);

        removeItem.setEnabled(!profilingInProgress);

        profilingPointsPopup.show(source, x, y);
    }

    private void updateButtons() {
        int[] selectedRows = profilingPointsTable.getSelectedRows();
        addButton.setEnabled(!profilingInProgress);

        if (selectedRows.length == 0) {
            editButton.setEnabled(false);
            removeButton.setEnabled(false);
            disableButton.setEnabled(false);
        } else if (selectedRows.length == 1) {
            editButton.setEnabled(!profilingInProgress);
            removeButton.setEnabled(!profilingInProgress);
            disableButton.setEnabled(!profilingInProgress);
        } else {
            editButton.setEnabled(false);
            removeButton.setEnabled(!profilingInProgress);
            disableButton.setEnabled(!profilingInProgress);
        }
    }

    private void updateProjectsCombo() {
        ppointProjectSelector.resetModel();
        refreshProfilingPoints();
    }
}
