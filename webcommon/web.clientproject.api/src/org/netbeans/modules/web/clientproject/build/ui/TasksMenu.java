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
package org.netbeans.modules.web.clientproject.build.ui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.web.clientproject.api.build.BuildTools.TasksMenuSupport;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.clientproject.build.AdvancedTask;
import org.netbeans.modules.web.clientproject.build.AdvancedTasksStorage;
import org.netbeans.modules.web.clientproject.build.Tasks;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

public class TasksMenu extends JMenu {

    static final Logger LOGGER = Logger.getLogger(TasksMenu.class.getName());

    private static final RequestProcessor RP = new RequestProcessor(TasksMenu.class);

    final TasksMenuSupport support;
    final AdvancedTasksStorage advancedTasksStorage;
    final Object lock = new Object();

    // @GuardedBy("EDT")
    private boolean menuBuilt = false;
    private volatile Tasks tasks = null;


    public TasksMenu(TasksMenuSupport support) {
        super(support.getTitle(TasksMenuSupport.Title.MENU));
        this.support = support;
        advancedTasksStorage = AdvancedTasksStorage.forBuildToolSupport(support);
    }

    boolean isMenuBuilt() {
        assert EventQueue.isDispatchThread();
        return menuBuilt;
    }

    void setMenuBuilt(boolean menuBuilt) {
        assert EventQueue.isDispatchThread();
        this.menuBuilt = menuBuilt;
    }

    @Override
    public JPopupMenu getPopupMenu() {
        assert EventQueue.isDispatchThread();
        if (!isMenuBuilt()) {
            setMenuBuilt(true);
            buildMenu();
        }
        return super.getPopupMenu();
    }

    @NbBundle.Messages({
        "# {0} - tasks/targets of the build tool",
        "# {1} - project name",
        "TasksMenu.error.execution=Error occured while getting {0} for project {1} - review IDE log for details.",
        "# {0} - tasks/targets of the build tool",
        "# {1} - project name",
        "TasksMenu.error.timeout=Timeout occured while getting {0} for project {1}.",
    })
    private void buildMenu() {
        assert EventQueue.isDispatchThread();
        assert tasks == null : tasks;
        // load tasks
        addLoadingMenuItem();
        RP.post(new Runnable() {
            @Override
            public void run() {
                AdvancedTasksStorage.Data data = advancedTasksStorage.loadTasks();
                Future<List<String>> simpleTasks = support.getTasks();
                try {
                    tasks = new Tasks(data.getTasks(), data.isShowSimpleTasks(), simpleTasks.get(1, TimeUnit.MINUTES));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    errorOccured(Bundle.TasksMenu_error_execution(
                            support.getTitle(TasksMenuSupport.Title.MENU),
                            ProjectUtils.getInformation(support.getProject()).getDisplayName()));
                } catch (TimeoutException ex) {
                    LOGGER.log(Level.INFO, null, ex);
                    errorOccured(Bundle.TasksMenu_error_timeout(
                            support.getTitle(TasksMenuSupport.Title.MENU),
                            ProjectUtils.getInformation(support.getProject()).getDisplayName()));
                }
                rebuildMenu();
            }
        });
    }

    void rebuildMenu() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                removeAll();
                addMenuItems();
                refreshMenu();
            }
        });
    }

    void errorOccured(String message) {
        if (isShowing()) {
            DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
        }
    }

    void refreshMenu() {
        JPopupMenu popupMenu = getPopupMenu();
        popupMenu.pack();
        popupMenu.invalidate();
        popupMenu.revalidate();
        popupMenu.repaint();
    }

    void addMenuItems() {
        assert EventQueue.isDispatchThread();
        if (tasks == null
                || tasks.getSimpleTasks() == null) {
            // build tool cli error?
            addConfigureToolMenuItem();
            return;
        }
        // ui
        VerticalGridLayout vgl = new VerticalGridLayout();
        getPopupMenu().setLayout(vgl);
        // items
        List<String> simpleTasks = tasks.getSimpleTasks();
        assert simpleTasks != null;
        Set<String> allTasks = new LinkedHashSet<>(simpleTasks);
        // default task
        final String defaultTaskName = support.getDefaultTaskName();
        if (defaultTaskName != null) {
            allTasks.remove(defaultTaskName);
            addTaskMenuItem(true, defaultTaskName);
            addSeparator();
        }
        // other tasks
        addAdvancedTasksMenuItems();
        if (tasks.isShowSimpleTasks()) {
            addTasksMenuItems(allTasks);
        }
        if (!tasks.getAdvancedTasks().isEmpty()
                || (tasks.isShowSimpleTasks() && !allTasks.isEmpty())) {
            addSeparator();
        }
        // config
        addManageMenuItems(allTasks);
        addReloadTasksMenuItem();
    }

    @CheckForNull
    private void addTasksMenuItems(Collection<String> tasks) {
        assert EventQueue.isDispatchThread();
        assert tasks != null;
        for (String task : tasks) {
            addTaskMenuItem(false, task);
        }
    }

    private void addAdvancedTasksMenuItems() {
        assert EventQueue.isDispatchThread();
        assert tasks != null;
        for (AdvancedTask task : tasks.getAdvancedTasks()) {
            addTaskMenuItem(task);
        }
    }

    @NbBundle.Messages("TasksMenu.menu.manage=Manage...")
    private void addManageMenuItems(final Collection<String> simpleTasks) {
        assert EventQueue.isDispatchThread();
        assert simpleTasks != null;
        // item Manage...
        JMenuItem menuItem = new JMenuItem(Bundle.TasksMenu_menu_manage());
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                assert EventQueue.isDispatchThread();
                List<String> simpleTasksWithDefaultTask = new ArrayList<>(simpleTasks.size() + 1);
                // add empty task for default task/action
                simpleTasksWithDefaultTask.add(""); // NOI18N
                simpleTasksWithDefaultTask.addAll(simpleTasks);
                AdvancedTasksPanel panel = AdvancedTasksPanel.open(support.getTitle(TasksMenuSupport.Title.MANAGE_ADVANCED),
                        support.getTitle(TasksMenuSupport.Title.TASKS_LABEL),
                        support.getBuildToolExecName(),
                        simpleTasksWithDefaultTask,
                        tasks.getAdvancedTasks(),
                        tasks.isShowSimpleTasks());
                if (panel != null) {
                    final AdvancedTasksStorage.Data data;
                    synchronized (lock) {
                        data = new AdvancedTasksStorage.Data()
                                .setTasks(panel.getTasks())
                                .setShowSimpleTasks(panel.isShowSimpleTasks());
                    }
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            assert !EventQueue.isDispatchThread();
                            try {
                                synchronized (lock) {
                                    advancedTasksStorage.storeTasks(data);
                                }
                            } catch (IOException ex) {
                                LOGGER.log(Level.INFO, "Cannot store tasks", ex);
                            }
                        }
                    });
                }
            }
        });
        add(menuItem);
    }

    private void addTaskMenuItem(final boolean isDefault, final String task) {
        JMenuItem menuitem = new JMenuItem(task);
        menuitem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        if (isDefault) {
                            support.runTask();
                        } else {
                            support.runTask(task);
                        }
                    }
                });
            }
        });
        add(menuitem);
    }

    private void addTaskMenuItem(final AdvancedTask task) {
        assert task != null;
        JMenuItem menuitem = new JMenuItem(task.getName());
        menuitem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        String fullCommand = task.getFullCommand();
                        if (StringUtilities.hasText(fullCommand)) {
                            support.runTask(Utilities.parseParameters(fullCommand));
                        } else {
                            support.runTask();
                        }
                    }
                });
            }
        });
        add(menuitem);
    }

    private void addLoadingMenuItem() {
        JMenuItem menuItem = new JMenuItem(support.getTitle(TasksMenuSupport.Title.LOADING_TASKS));
        menuItem.setEnabled(false);
        add(menuItem);
    }

    private void addConfigureToolMenuItem() {
        JMenuItem menuItem = new JMenuItem(support.getTitle(TasksMenuSupport.Title.CONFIGURE_TOOL));
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                support.configure();
            }
        });
        add(menuItem);
    }

    @NbBundle.Messages("TasksMenu.menu.reload=Reload")
    private void addReloadTasksMenuItem() {
        JMenuItem menuItem = new JMenuItem(Bundle.TasksMenu_menu_reload());
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                assert EventQueue.isDispatchThread();
                RP.post(new Runnable() {
                    @Override
                    public void run() {
                        support.reloadTasks();
                    }
                });
                setMenuBuilt(false);
            }
        });
        add(menuItem);
    }

}
