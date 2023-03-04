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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.StaticResource;
import org.netbeans.modules.web.clientproject.api.build.BuildTools;
import org.netbeans.modules.web.clientproject.api.util.StringUtilities;
import org.netbeans.modules.web.clientproject.build.AdvancedTask;
import org.netbeans.modules.web.clientproject.build.AdvancedTasksStorage;
import org.netbeans.modules.web.clientproject.build.Tasks;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.awt.StatusDisplayer;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.ListView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

// Inspired by AntNavigatorPanel
public final class NavigatorPanelImpl implements NavigatorPanel, LookupListener, ChangeListener {

    private static final Logger LOGGER = Logger.getLogger(NavigatorPanelImpl.class.getName());

    private final BuildTools.NavigatorPanelSupport navigatorPanelSupport;
    private final ExplorerManager manager = new ExplorerManager();
    private final Lookup lookup = ExplorerUtils.createLookup(manager, new ActionMap());

    // @GuadedBy("EDT")
    private Lookup.Result<FileObject> selection;
    // @GuadedBy("EDT")
    private JComponent panel;


    public NavigatorPanelImpl(BuildTools.NavigatorPanelSupport navigatorPanelSupport) {
        assert navigatorPanelSupport != null;
        this.navigatorPanelSupport = navigatorPanelSupport;
    }

    @Override
    public String getDisplayName() {
        return navigatorPanelSupport.getDisplayName();
    }

    @Override
    public String getDisplayHint() {
        return navigatorPanelSupport.getDisplayHint();
    }

    @Override
    public JComponent getComponent() {
        assert EventQueue.isDispatchThread();
        if (panel == null) {
            final ListView view = new ListView();
            view.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            class Panel extends JPanel implements ExplorerManager.Provider, Lookup.Provider {
                // make sure action context works correctly
                {
                    setLayout(new BorderLayout());
                    add(view, BorderLayout.CENTER);
                }
                @Override
                public ExplorerManager getExplorerManager() {
                    return manager;
                }
                @Override
                public boolean requestFocusInWindow() {
                    return view.requestFocusInWindow();
                }
                @Override
                public Lookup getLookup() {
                    return lookup;
                }
            }
            panel = new Panel();
        }
        return panel;
    }

    @Override
    public void panelActivated(Lookup context) {
        assert EventQueue.isDispatchThread();
        selection = context.lookupResult(FileObject.class);
        selection.addLookupListener(this);
        navigatorPanelSupport.addChangeListener(this);
        processChange();
    }

    @Override
    public void panelDeactivated() {
        assert EventQueue.isDispatchThread();
        selection.removeLookupListener(this);
        navigatorPanelSupport.removeChangeListener(this);
        selection = null;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    void display(Collection<? extends FileObject> selectedFiles) {
        if (selectedFiles.size() == 1) {
            FileObject fileObject = selectedFiles.iterator().next();
            if (fileObject.isValid()) {
                BuildTools.BuildToolSupport support = navigatorPanelSupport.getBuildToolSupport(fileObject);
                if (support != null) {
                    manager.setRootContext(new RootNode(support));
                    return;
                }
            }
        }
        manager.setRootContext(Node.EMPTY);
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        processChange();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        processChange();
    }

    private void processChange() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                if (selection != null) {
                    display(selection.allInstances());
                }
            }
        });
    }

    //~ Inner classes

    private static final class RootNode extends AbstractNode {

        public RootNode(BuildTools.BuildToolSupport support) {
            super(Children.create(new ChildFactoryImpl(support), true));
        }

    }

    private static final class ChildFactoryImpl extends ChildFactory<Object> {

        private final BuildTools.BuildToolSupport support;


        ChildFactoryImpl(BuildTools.BuildToolSupport support) {
            assert support != null;
            this.support = support;
        }

        @Override
        protected boolean createKeys(List<Object> toPopulate) {
            Tasks tasks = getTasks();
            if (tasks == null
                    || tasks.getSimpleTasks() == null) {
                // some error
                return true;
            }
            toPopulate.addAll(tasks.getAdvancedTasks());
            if (tasks.isShowSimpleTasks()) {
                List<String> simpleTasks = tasks.getSimpleTasks();
                assert simpleTasks != null;
                toPopulate.addAll(simpleTasks);
            }
            return true;
        }

        @Override
        protected Node createNodeForKey(Object key) {
            if (key instanceof AdvancedTask) {
                return new TaskNode(new AdvancedTaskNodeSupportImpl((AdvancedTask) key, support));
            }
            return new TaskNode(new SimpleTaskNodeSupportImpl((String) key, support));
        }

        @NbBundle.Messages("ChildFactoryImpl.tasks.error=Cannot get tasks for the current file.")
        @CheckForNull
        private Tasks getTasks() {
            AdvancedTasksStorage.Data data = AdvancedTasksStorage.forBuildToolSupport(support).loadTasks();
            Future<List<String>> simpleTasks = support.getTasks();
            try {
                return new Tasks(data.getTasks(), data.isShowSimpleTasks(), simpleTasks.get(1, TimeUnit.MINUTES));
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (ExecutionException | TimeoutException ex) {
                LOGGER.log(Level.INFO, null, ex);
                StatusDisplayer.getDefault().setStatusText(Bundle.ChildFactoryImpl_tasks_error());
            }
            return null;
        }

    }

    private static final class TaskNode extends AbstractNode {

        private final TaskNodeSupport support;


        public TaskNode(TaskNodeSupport support) {
            super(Children.LEAF);
            assert support != null;
            this.support = support;
        }

        @Override
        public String getDisplayName() {
            return support.getName();
        }

        @Override
        public String getShortDescription() {
            return support.getShortDescription();
        }

        @Override
        public Image getIcon(int type) {
            return ImageUtilities.loadImage(support.getIconPath());
        }

        @Override
        public Action[] getActions(boolean context) {
            return support.getActions();
        }

        @Override
        public Action getPreferredAction() {
            return support.getActions()[0];
        }

    }

    private interface TaskNodeSupport {

        @StaticResource
        String ADVANCED_TASK_ICON_PATH = "org/netbeans/modules/web/clientproject/ui/resources/advanced-task.gif"; // NOI18N
        @StaticResource
        String TASK_ICON_PATH = "org/netbeans/modules/web/clientproject/ui/resources/task.gif"; // NOI18N


        String getName();

        String getShortDescription();

        String getIconPath();

        Action[] getActions();

    }

    private static final class AdvancedTaskNodeSupportImpl implements TaskNodeSupport {

        private final AdvancedTask task;
        private final BuildTools.BuildToolSupport support;


        public AdvancedTaskNodeSupportImpl(AdvancedTask task, BuildTools.BuildToolSupport support) {
            assert task != null;
            assert support != null;
            this.task = task;
            this.support = support;
        }

        @Override
        public String getName() {
            return task.getName();
        }

        @Override
        public String getShortDescription() {
            return task.getFullCommand().trim();
        }

        @Override
        public String getIconPath() {
            return ADVANCED_TASK_ICON_PATH;
        }

        @Override
        public Action[] getActions() {
            return new Action[] {
                new RunAction(task.getFullCommand(), support),
            };
        }

    }

    private static final class SimpleTaskNodeSupportImpl implements TaskNodeSupport {

        private final String task;
        private final BuildTools.BuildToolSupport support;


        public SimpleTaskNodeSupportImpl(String task, BuildTools.BuildToolSupport support) {
            assert task != null;
            assert support != null;
            this.task = task;
            this.support = support;
        }

        @Override
        public String getName() {
            return task;
        }

        @Override
        public String getShortDescription() {
            return task.trim();
        }

        @Override
        public String getIconPath() {
            return TASK_ICON_PATH;
        }

        @Override
        public Action[] getActions() {
            return new Action[] {
                new RunAction(task, support),
            };
        }

    }

    private static final class RunAction extends AbstractAction {

        private static final RequestProcessor RP = new RequestProcessor(RunAction.class);

        final String command;
        final BuildTools.BuildToolSupport support;


        @NbBundle.Messages("RunAction.name=Run")
        public RunAction(String command, BuildTools.BuildToolSupport support) {
            super(Bundle.RunAction_name());
            assert command != null;
            assert support != null;
            this.command = command;
            this.support = support;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    if (StringUtilities.hasText(command)) {
                        support.runTask(Utilities.parseParameters(command));
                    } else {
                        support.runTask();
                    }
                }
            });
        }

    }

}
