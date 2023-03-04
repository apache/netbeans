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
package org.netbeans.modules.bugtracking.tasks;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.DocumentListener;
import org.netbeans.modules.bugtracking.commons.UIUtils;
import org.netbeans.modules.bugtracking.tasks.dashboard.DashboardViewer;
import org.netbeans.modules.bugtracking.tasks.filter.OpenedCategorizedTaskFilter;
import org.netbeans.modules.bugtracking.settings.DashboardSettings;
import org.netbeans.modules.bugtracking.spi.IssueScheduleInfo;
import org.netbeans.modules.bugtracking.tasks.actions.Actions.SortDialogAction;
import org.netbeans.modules.bugtracking.tasks.filter.ScheduleCategoryFilter;
import org.netbeans.modules.team.commons.ColorManager;
import org.netbeans.modules.team.commons.treelist.TreeLabel;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 *
 * @author jpeska
 */
public class FilterPanel extends javax.swing.JPanel {

    private final Color BACKGROUND_COLOR;
    private final Color FOREGROUND_COLOR;
    private final TreeLabel lblTitle;
    private final JTextField textFilter;
    private final TreeLabel lblCount;
    private final JButton btnFilter;
    private final JButton btnSort;
    //private final JButton btnGroup;
    private final OpenedCategorizedTaskFilter openedTaskFilter;
    private final ScheduleCategoryFilter scheduleCategoryFilter;
    private final DashboardToolbar toolBar;
    private final RequestProcessor REQUEST_PROCESSOR;

    private final String TODAY_SETTING_ID = "scheduleToday";
    private final String THIS_WEEK_SETTING_ID = "scheduleThisWeek";
    private final String ALL_SETTING_ID = "scheduleAll";
    private ShowScheduleAction showTodayAction;
    private static FilterPanel INSTANCE = null;

    public static FilterPanel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new FilterPanel();
        }
        return INSTANCE;
    }

    private FilterPanel() {
        REQUEST_PROCESSOR = DashboardViewer.getInstance().getRequestProcessor();
        BACKGROUND_COLOR = ColorManager.getDefault().getExpandableRootBackground();
        FOREGROUND_COLOR = ColorManager.getDefault().getExpandableRootForeground();
        openedTaskFilter = new OpenedCategorizedTaskFilter();
        scheduleCategoryFilter = new ScheduleCategoryFilter();
        initComponents();
        setBackground(BACKGROUND_COLOR);
        final JLabel iconLabel = new JLabel(ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/find.png", true)); //NOI18N
        add(iconLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));

        lblTitle = new TreeLabel(NbBundle.getMessage(FilterPanel.class, "LBL_Filter")); // NOI18N
        lblTitle.setBackground(BACKGROUND_COLOR);
//        lblTitle.setFont(lblTitle.getFont().deriveFont(Font.BOLD));
        lblTitle.setForeground(FOREGROUND_COLOR);
        add(lblTitle, new GridBagConstraints(2, 0, 1, 1, 0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));

        textFilter = new JTextField();
        textFilter.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                if (textFilter.equals(e.getSource())) {
                    if (e.getKeyCode() == Event.ESCAPE) {
                        textFilter.setText("");
                    }
                }
            }

        });
        textFilter.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                focusChanged(true);
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });
        textFilter.setMinimumSize(new java.awt.Dimension(150, 20));
        textFilter.setPreferredSize(new java.awt.Dimension(150, 20));
        add(textFilter, new GridBagConstraints(3, 0, 1, 1, 1, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 2, 5), 0, 0));

        lblCount = new TreeLabel("");
        lblCount.setVisible(false);
        lblCount.setBackground(BACKGROUND_COLOR);
        lblCount.setForeground(FOREGROUND_COLOR);
        add(lblCount, new GridBagConstraints(4, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));

        add(new JLabel(), new GridBagConstraints(5, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));

        toolBar = new DashboardToolbar();

        btnSort = new JButton(new SortDialogAction());
        btnSort.setIcon(ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/sort.png", true)); //NOI18N
        btnSort.setToolTipText(NbBundle.getMessage(FilterPanel.class, "LBL_SortTooltip")); //NOI18N
        toolBar.addButton(btnSort);

        btnFilter = new JButton(ImageUtilities.loadImageIcon("org/netbeans/modules/bugtracking/tasks/resources/filter.png", true)); //NOI18N
        btnFilter.setToolTipText(NbBundle.getMessage(FilterPanel.class, "LBL_FilterTooltip")); //NOI18N
        final JPopupMenu filterPopup = createFilterPopup();
        btnFilter.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (btnFilter.isEnabled()) {
                    filterPopup.show(e.getComponent(), btnFilter.getX(), btnFilter.getY() + btnFilter.getHeight());
                }
            }
        });
        toolBar.addButton(btnFilter);
        add(toolBar, new GridBagConstraints(6, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(0, 0, 0, 3), 0, 0));
    }

    public void addDocumentListener(DocumentListener listener) {
        textFilter.getDocument().addDocumentListener(listener);
    }

    public void removeDocumentListener(DocumentListener listener) {
        textFilter.getDocument().removeDocumentListener(listener);
    }

    public String getFilterText() {
        return textFilter.getText();
    }

    public void setHitsCount(final int hits) {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                if (!lblCount.isVisible()) {
                    lblCount.setVisible(true);
                }
                lblCount.setText("(" + hits + " " + NbBundle.getMessage(FilterPanel.class, "LBL_Matches") + ")"); //NOI18N
            }
        });
    }

    public void showTodayCategory() {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                showTodayAction.menuItem.setSelected(true);
                showTodayAction.actionPerformed(null);
            }
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        if (ColorManager.getDefault().isAqua()) {
            Graphics2D g2d = (Graphics2D) g;
            Paint oldPaint = g2d.getPaint();
            g2d.setPaint(new GradientPaint(0, 0, Color.white, 0, getHeight() / 2, getBackground()));
            g2d.fillRect(0, 0, getWidth(), getHeight());
            g2d.setPaint(oldPaint);
        } else {
            super.paintComponent(g);
        }
    }

    void clear() {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                textFilter.setText("");
                lblCount.setText("");
                lblCount.setVisible(false);
            }
        });
    }

    void handleFilterShortcut() {
        UIUtils.runInAWT(new Runnable() {
            @Override
            public void run() {
                textFilter.requestFocusInWindow();
            }
        });
    }

    private void focusChanged(boolean hasFocus) {
        if (hasFocus) {
            UIUtils.runInAWT(new Runnable() {
                @Override
                public void run() {
                    textFilter.selectAll();
                }
            });
        }
    }

    private JPopupMenu createFilterPopup() {
        final JPopupMenu popup = new JPopupMenu();
        final JCheckBoxMenuItem chbShowFinished = new JCheckBoxMenuItem();
        AbstractAction action = new AbstractAction(NbBundle.getMessage(FilterPanel.class, "LBL_ShowAll")) { //NOI18N

            @Override
            public void actionPerformed(ActionEvent e) {
                REQUEST_PROCESSOR.post(new Runnable() {
                    @Override
                    public void run() {
                        boolean selected = chbShowFinished.isSelected();
                        if (selected) {
                            int hits = DashboardViewer.getInstance().removeTaskFilter(openedTaskFilter, true);
                            manageHitCount(hits);
                        } else {
                            int hits = DashboardViewer.getInstance().applyTaskFilter(openedTaskFilter, true);
                            manageHitCount(hits);
                        }
                        DashboardSettings.getInstance().setShowFinishedTasks(selected);
                    }
                });
            }
        };
        chbShowFinished.setAction(action);
        boolean showFinishedTasks = DashboardSettings.getInstance().showFinishedTasks();
        chbShowFinished.setSelected(showFinishedTasks);
        if (!showFinishedTasks) {
            int hits = DashboardViewer.getInstance().applyTaskFilter(openedTaskFilter, true);
            manageHitCount(hits);
        }
        popup.add(chbShowFinished);

        //<editor-fold defaultstate="collapsed" desc="schedule filters section">
        popup.addSeparator();

        JCheckBoxMenuItem chbToday = new JCheckBoxMenuItem();
        IssueScheduleInfo todayInfo = DashboardUtils.getToday();
        showTodayAction = new ShowScheduleAction(
                NbBundle.getMessage(FilterPanel.class, "LBL_ScheduleToday"),
                TODAY_SETTING_ID, todayInfo,
                chbToday
        ) {

            @Override
            public void afterUpdate() {
                DashboardViewer.getInstance().selectTodayCategory();
            }

        };
        chbToday.setAction(showTodayAction);
        boolean showTodaySchedule = DashboardSettings.getInstance().showSchedule(TODAY_SETTING_ID);
        chbToday.setSelected(showTodaySchedule);
        if (showTodaySchedule) {
            scheduleCategoryFilter.addInfo(todayInfo);
        }
        popup.add(chbToday);

        JCheckBoxMenuItem chbThisWeek = new JCheckBoxMenuItem();
        IssueScheduleInfo thisWeekInfo = DashboardUtils.getThisWeek();
        AbstractAction showThisWeekAction = new ShowScheduleAction(
                NbBundle.getMessage(FilterPanel.class, "LBL_ScheduleThisWeek"),
                THIS_WEEK_SETTING_ID, thisWeekInfo,
                chbThisWeek
        );
        chbThisWeek.setAction(showThisWeekAction);
        boolean showThisWeekSchedule = DashboardSettings.getInstance().showSchedule(THIS_WEEK_SETTING_ID);
        chbThisWeek.setSelected(showThisWeekSchedule);
        if (showThisWeekSchedule) {
            scheduleCategoryFilter.addInfo(thisWeekInfo);
        }
        popup.add(chbThisWeek);

        JCheckBoxMenuItem chbAll = new JCheckBoxMenuItem();
        IssueScheduleInfo allInfo = DashboardUtils.getAll();
        AbstractAction showAllAction = new ShowScheduleAction(
                NbBundle.getMessage(FilterPanel.class, "LBL_ScheduleAll"),
                ALL_SETTING_ID, allInfo,
                chbAll
        );
        chbAll.setAction(showAllAction);
        boolean showAllSchedule = DashboardSettings.getInstance().showSchedule(ALL_SETTING_ID);
        chbAll.setSelected(showAllSchedule);
        if (showAllSchedule) {
            scheduleCategoryFilter.addInfo(allInfo);
        }
        popup.add(chbAll);

        DashboardViewer.getInstance().applyCategoryFilter(scheduleCategoryFilter, false);
        //</editor-fold>
        return popup;
    }

    private void manageHitCount(int hits) {
        if (DashboardViewer.getInstance().showHitCount() && hits != -1) {
            setHitsCount(hits);
        } else {
            clear();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

    private class ShowScheduleAction extends AbstractAction {

        private final IssueScheduleInfo scheduleInfo;
        private final JMenuItem menuItem;
        private final String settingId;

        public ShowScheduleAction(String name, String settingId, IssueScheduleInfo scheduleInfo, JMenuItem menuItem) {
            super(name);
            this.settingId = settingId;
            this.scheduleInfo = scheduleInfo;
            this.menuItem = menuItem;
        }

        //NOI18N
        @Override
        public void actionPerformed(ActionEvent e) {
            REQUEST_PROCESSOR.post(new Runnable() {
                @Override
                public void run() {
                    boolean selected = menuItem.isSelected();
                    if (selected) {
                        scheduleCategoryFilter.addInfo(scheduleInfo);
                        int hits = DashboardViewer.getInstance().removeTaskFilter(openedTaskFilter, true);
                        manageHitCount(hits);
                    } else {
                        scheduleCategoryFilter.removeInfo(scheduleInfo);
                        int hits = DashboardViewer.getInstance().applyTaskFilter(openedTaskFilter, true);
                        manageHitCount(hits);
                    }
                    DashboardViewer.getInstance().updateCategoryFilter(scheduleCategoryFilter);
                    DashboardSettings.getInstance().updateShowSchedule(settingId, selected);
                    afterUpdate();
                }
            });
        }

        public void afterUpdate() {

        }
    }
}
