/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2016 Sun Microsystems, Inc.
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
