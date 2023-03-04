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

import org.netbeans.modules.bugtracking.tasks.dashboard.DashboardRefresher;
import org.netbeans.modules.bugtracking.tasks.dashboard.DashboardViewer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.netbeans.modules.bugtracking.tasks.filter.DisplayTextTaskFilter;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.WindowManager;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.modules.bugtracking.IssueImpl;
import org.netbeans.modules.bugtracking.QueryImpl;
import org.netbeans.modules.bugtracking.RepositoryImpl;
import org.netbeans.modules.bugtracking.RepositoryRegistry;
import org.netbeans.modules.bugtracking.tasks.dashboard.TaskNode;
import org.netbeans.modules.bugtracking.settings.DashboardSettings;
import org.openide.awt.ActionReferences;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(dtd = "-//org.netbeans.modules.demotasklist//Dashboard//EN",
autostore = false)
@TopComponent.Description(preferredID = "DashboardTopComponent",
iconBase = "org/netbeans/modules/bugtracking/tasks/resources/dashboard.png",
persistenceType = TopComponent.PERSISTENCE_ALWAYS)
@TopComponent.Registration(mode = "explorer", openAtStartup = false, position = 350)
@ActionID(category = "Window", id = "org.netbeans.modules.tasks.ui.DashboardTopComponent")
@ActionReferences({
    @ActionReference(path = "Menu/Window", position = 751),
    @ActionReference(path = "Shortcuts", name = "DS-6")        
})
@TopComponent.OpenActionRegistration(displayName = "#CTL_DashboardAction",
preferredID = "DashboardTopComponent")
@NbBundle.Messages({
    "CTL_DashboardAction=Tas&ks",
    "CTL_DashboardTopComponent=Tasks",
    "HINT_DashboardTopComponent=This is a Tasks window"
})
public final class DashboardTopComponent extends TopComponent {

    private static DashboardTopComponent instance;
    private final ComponentAdapter componentAdapter;
    private final JComponent dashboardComponent;
    private FilterDocumentListener filterListener;
    private CategoryNameDocumentListener categoryNameListener;
    private final Timer filterTimer;
    private ActiveTaskPanel activeTaskPanel;
    private final GridBagConstraints activeTaskConstrains;
    private FilterPanel filterPanel;
    private DisplayTextTaskFilter displayTextTaskFilter = null;
    private CategoryNamePanel categoryNamePanel;
    private NotifyDescriptor categoryNameDialog;
    private final DashboardActiveListener dashboardSelectionListener;
    private final Timer dashboardRefreshTime;
    private final DashboardRefresher refresher;
    private final DashboardViewer dashboard;
    private boolean firstStart = true;

    public DashboardTopComponent() {
        initComponents();
        setName(NbBundle.getMessage(DashboardTopComponent.class, "CTL_DashboardTopComponent")); //NOI18N
        filterTimer = new Timer(500, new FilterTimerListener());
        filterTimer.stop();
        refresher = DashboardRefresher.getInstance();
        filterPanel = FilterPanel.getInstance();
        if (filterListener == null) {
            filterListener = new FilterDocumentListener(filterTimer);
        }
        dashboard = DashboardViewer.getInstance();
        dashboardComponent = dashboard.getComponent();
        dashboardRefreshTime = new Timer(10000, new RefreshTimerListener());
        dashboardSelectionListener = new DashboardActiveListener();
        activeTaskConstrains = new GridBagConstraints(0, 1, 2, 1, 1.0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 3, 0, 0), 0, 0);
        componentAdapter = new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                if (activeTaskPanel != null) {
                    activeTaskPanel.setTaskNameText();
                }
            }
        };
        
        Action filterAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (filterPanel != null) {
                    filterPanel.handleFilterShortcut();
                }
            }
        };
        this.getActionMap().put(DashboardUtils.getFindActionMapKey(), filterAction);
    }

    public static synchronized DashboardTopComponent getDefault() {
        if (instance == null) {
            instance = new DashboardTopComponent();
        }
        return instance;
    }

    /**
     * Obtain the DashboardTopComponent instance. Never call {@link #getDefault}
     * directly!
     */
    public static synchronized DashboardTopComponent findInstance() {
        final String PREFERRED_ID = "DashboardTopComponent"; //NOI18N
        TopComponent win = WindowManager.getDefault().findTopComponent(PREFERRED_ID);

        if (win == null) {
            return getDefault();
        }
        if (win instanceof DashboardTopComponent) {
            return (DashboardTopComponent) win;
        }
        return getDefault();
    }

    public void activateTask(TaskNode taskNode) {
        deactivateTask();
        dashboard.setActiveTaskNode(taskNode);
        if (activeTaskPanel == null) {
            activeTaskPanel = new ActiveTaskPanel(taskNode);
        } else {
            activeTaskPanel.setTaskNode(taskNode);
        }
        add(activeTaskPanel, activeTaskConstrains);
        repaint();
        validate();
    }

    public void deactivateTask() {
        if (activeTaskPanel != null) {
            dashboard.setActiveTaskNode(null);
            this.remove(activeTaskPanel);
            repaint();
            validate();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBackground(new java.awt.Color(255, 255, 255));
        setLayout(new java.awt.GridBagLayout());
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    /**
     * Gets default instance. Do not use directly: reserved for *.settings files
     * only, i.e. deserialization routines; otherwise you could get a
     * non-deserialized instance. To obtain the singleton instance, use
     * {@link #findInstance}.
     */
    @Override
    public void componentOpened() {
        removeAll();
        filterPanel.addDocumentListener(filterListener);
        add(filterPanel, new GridBagConstraints(0, 3, 2, 1, 1.0, 0, GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 1, 0, 0), 0, 0));

        add(dashboardComponent, new GridBagConstraints(0, 5, 2, 1, 1.0, 0.8, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(1, 1, 0, 0), 0, 0));
        RepositoryRegistry.getInstance().addPropertyChangeListener(dashboard);

        addComponentListener(componentAdapter);
        DashboardSettings.getInstance().addPropertyChangedListener(dashboard);
        TopComponent.getRegistry().addPropertyChangeListener(dashboardSelectionListener);
        refresher.setRefreshEnabled(true);
        refresher.setDashboardBusy(false);
        if (firstStart) {
            firstStart = false;
            //load data after the component is displayed
            WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
                @Override
                public void run() {
                    dashboard.loadData();
                }
            });
        }
    }

    @Override
    protected void componentClosed() {
        filterPanel.removeDocumentListener(filterListener);
        RepositoryRegistry.getInstance().removePropertyChangeListener(dashboard);
        DashboardSettings.getInstance().removePropertyChangedListener(dashboard);
        filterPanel.clear();
        dashboard.clearFilters();
        refresher.setRefreshEnabled(false);
        super.componentClosed();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean requestFocusInWindow() {
        // Needed to transfer focus to dashboard e.g. when switched to it from Window menu.
        boolean b = super.requestFocusInWindow();
        if (dashboardComponent != null) {
            b = dashboardComponent.requestFocusInWindow();
        }
        return b;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void requestFocus() {
        // Needed to transfer focus to dashboard e.g. if it is the active TopComponent after restart.
        super.requestFocus();
        if (dashboardComponent != null) {
            dashboardComponent.requestFocus();
        }
    }

    public Category createCategory() {
        categoryNamePanel = new CategoryNamePanel(NbBundle.getMessage(DashboardTopComponent.class, "LBL_CreateCatNameLabel"), ""); //NOI18N

        boolean confirm = showCategoryNameDialog(categoryNamePanel, NbBundle.getMessage(DashboardTopComponent.class, "LBL_CreateCatTitle")); //NOI18N
        if (confirm) {
            Category category = new Category(categoryNamePanel.getCategoryName());
            dashboard.addCategory(category);
            return category;
        }
        return null;
    }

    public void renameCategory(Category category) {
        categoryNamePanel = new CategoryNamePanel(NbBundle.getMessage(DashboardTopComponent.class, "LBL_RenameCatNameLabel"), category.getName()); //NOI18N

        boolean confirm = showCategoryNameDialog(categoryNamePanel, NbBundle.getMessage(DashboardTopComponent.class, "LBL_RenameCatTitle")); //NOI18N
        if (confirm) {
            dashboard.renameCategory(category, categoryNamePanel.getCategoryName());
        }
    }

    private boolean showCategoryNameDialog(CategoryNamePanel panel, String message) {
        categoryNameDialog = new NotifyDescriptor(
                panel,
                message,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                null,
                NotifyDescriptor.OK_OPTION);
        categoryNameDialog.setValid(false);
        if (categoryNameListener == null) {
            categoryNameListener = new CategoryNameDocumentListener();
        }
        panel.addDocumentListener(categoryNameListener);
        boolean confirm = DialogDisplayer.getDefault().notify(categoryNameDialog) == NotifyDescriptor.OK_OPTION;
        panel.removeDocumentListener(categoryNameListener);
        return confirm;
    }

    public void addTask(IssueImpl issue) {
        addTask(new TaskNode(issue, null));
    }
    
    private boolean openedByUserAction = false;
    public void addTask(TaskNode... taskNodes) {
        final CategoryPicker picker = new CategoryPicker(taskNodes);
        final NotifyDescriptor nd = new NotifyDescriptor(
                picker,
                NbBundle.getMessage(DashboardTopComponent.class, "LBL_AddTaskToCat"), //NOI18N
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.PLAIN_MESSAGE,
                null,
                NotifyDescriptor.OK_OPTION);

        picker.setCategoryListener(new CategoryPicker.CategoryComboListener() {
            @Override
            public void comboItemsChanged(boolean categoryAvailable) {
                nd.setValid(categoryAvailable);
            }
        });
        nd.setValid(false);
        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            Category category = picker.getChosenCategory();
            dashboard.addTaskToCategory(category, taskNodes);
            
            if(!openedByUserAction && !isOpened()) {
                openedByUserAction = true;
                activate();
            }
        }
    }

    public void select(RepositoryImpl repo, boolean activate) {
        dashboard.select(repo);
        if(activate) {
            activate();
        }
    }
    
    public void select(QueryImpl impl, boolean activate) {
        dashboard.select(impl, true);
        if(activate) {
            activate();
        }
    }
    
    public String getFilterText() {
        return filterPanel.getFilterText();
    }

    public void showTodayCategory(){
        filterPanel.showTodayCategory();
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

    private void activate() {
        if(!isOpened()) {
            open();
        }
        requestActive();
    }

    private class FilterTimerListener implements ActionListener {

        private final RequestProcessor RP = new RequestProcessor(FilterTimerListener.class.getName());
        private boolean wasEmpty = true;

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == filterTimer) {
                final boolean isEmpty = filterPanel.getFilterText().isEmpty();
                if(wasEmpty && !isEmpty) {
                    dashboard.saveExpandedState();
                    wasEmpty = false;
                } else if(isEmpty) {
                    wasEmpty = true;
                }
                filterTimer.stop();
                RP.schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (!isEmpty) {
                            DisplayTextTaskFilter newTaskFilter = new DisplayTextTaskFilter(filterPanel.getFilterText());
                            int hits = dashboard.updateTaskFilter(displayTextTaskFilter, newTaskFilter);
                            displayTextTaskFilter = newTaskFilter;
                            filterPanel.setHitsCount(hits);
                        } else {
                            if (displayTextTaskFilter != null) {
                                dashboard.removeTaskFilter(displayTextTaskFilter, true);
                                displayTextTaskFilter = null;
                            }
                            filterPanel.clear();
                        }
                    }
                }, 100, TimeUnit.MILLISECONDS);
            }
        }
    }

    private class FilterDocumentListener implements DocumentListener {

        private Timer timer;

        public FilterDocumentListener(Timer timer) {
            this.timer = timer;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            timer.restart();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            timer.restart();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            timer.restart();
        }
    }

    private class CategoryNameDocumentListener implements DocumentListener {

        public CategoryNameDocumentListener() {
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            checkCategoryName();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            checkCategoryName();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            checkCategoryName();
        }

        private void checkCategoryName() {
            if (categoryNamePanel.getCategoryName().isEmpty()) {
                categoryNamePanel.setErrorText(NbBundle.getMessage(DashboardTopComponent.class, "LBL_CatNameErrEmpty")); //NOI18N
                categoryNameDialog.setValid(false);
            } else if (!dashboard.isCategoryNameUnique(categoryNamePanel.getCategoryName())) {
                categoryNamePanel.setErrorText(NbBundle.getMessage(DashboardTopComponent.class, "LBL_CatNameErrUnique")); //NOI18N
                categoryNameDialog.setValid(false);
            } else {
                categoryNamePanel.setErrorText("");
                categoryNameDialog.setValid(true);
            }
        }
    }

    private class DashboardActiveListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (TopComponent.Registry.PROP_ACTIVATED.equals(evt.getPropertyName())) {
                if (DashboardTopComponent.this == TopComponent.getRegistry().getActivated()) {
                    refresher.setDashboardBusy(true);
                    dashboardRefreshTime.stop();
                } else {
                    dashboardRefreshTime.restart();
                }
            }
        }
    }

    private class RefreshTimerListener implements ActionListener {

        public RefreshTimerListener() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            // user didnt use the dasboard for some time, refresh could be performed
            if (e.getSource() == dashboardRefreshTime) {
                dashboardRefreshTime.stop();
                refresher.setDashboardBusy(false);
            }
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.bugtracking.tasks.DashboardTopComponent"); //NOI18N
    }
}
