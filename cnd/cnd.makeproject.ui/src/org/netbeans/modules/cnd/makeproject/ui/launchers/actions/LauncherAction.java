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
package org.netbeans.modules.cnd.makeproject.ui.launchers.actions;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu.Separator;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.makeproject.api.LaunchersRegistryAccessor;
import org.netbeans.modules.cnd.makeproject.api.ProjectActionEvent;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationSupport;
import org.netbeans.modules.cnd.makeproject.api.launchers.Launcher;
import org.netbeans.modules.cnd.makeproject.api.launchers.LauncherExecutor;
import org.netbeans.modules.cnd.makeproject.api.launchers.LaunchersRegistryFactory;
import org.netbeans.modules.cnd.makeproject.ui.actions.MakeProjectActionsSupport;
import org.netbeans.modules.nativeexecution.api.ExecutionListener;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.BuildExecutionSupport;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

/**
 *
 */
public class LauncherAction extends AbstractAction implements ContextAwareAction, Presenter.Menu, Presenter.Popup{

    private Project project;
    private JMenu subMenu = null;
    private boolean isSubmenu;
    private final ProjectActionEvent.PredefinedType actionType;
    private final String displayName;
    private Action delegate;
    private static final String DEFAULT_ACTION_NAME = NbBundle.getMessage(LauncherAction.class, "LBL_DefaultAction_Name"); //NOI18N

    public LauncherAction(ProjectActionEvent.PredefinedType actionType, String displayName) {
        this.actionType = actionType;
        this.displayName = displayName; 
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, Boolean.TRUE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isSubmenu) {
            delegate.actionPerformed(e);
        }
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        this.project = actionContext.lookup(Project.class);
        isSubmenu = true;
        if (project == null) {
            isSubmenu = false;
        }         
        isSubmenu =  isSubmenu && ConfigurationSupport.getProjectActiveConfiguration(project) != null && 
                LaunchersRegistryFactory.getInstance(project.getProjectDirectory()).hasLaunchers();   
        switch (actionType) {
        }
        
        return this;
    }
    
    @Override
    public JMenuItem getPopupPresenter() {
        return createMenuItem();
    }

    @Override
    public JMenuItem getMenuPresenter() {
        return createMenuItem();
    }

    private JMenuItem createMenuItem() {
        if (isSubmenu) {
            createSubMenu();
            return subMenu;
        } else {
            switch (actionType) {
                case BUILD:
                    delegate = MakeProjectActionsSupport.buildAction();
                    break;
                case RUN:
                    delegate = MakeProjectActionsSupport.runAction();
                    break;
                case DEBUG:
                    delegate = MakeProjectActionsSupport.debugAction();
                    break;
                case DEBUG_STEPINTO:
                    delegate = MakeProjectActionsSupport.stepIntoAction();
                    break;
                default:
                    assert false;
                    return null;
            }
            JMenuItem item = new JMenuItem(delegate);
            return item;
        }
    }

    // This method is shared between multiple actions
    private void createSubMenu() {
        subMenu = new JMenu(displayName);
        subMenu.setEnabled(isSubmenu);
        subMenu.putClientProperty(DynamicMenuContent.HIDE_WHEN_DISABLED, getValue(DynamicMenuContent.HIDE_WHEN_DISABLED));
        LaunchersRegistryAccessor.getDefault().assertPrivateListenerNotNull(project.getProjectDirectory());  //for debugging purposes only
        LaunchersRegistryFactory.getInstance(project.getProjectDirectory()).getLaunchers().forEach((launcher) -> {
            subMenu.add(new LauncherExecutableAction(launcher));
        });
        
        JMenuItem add;
        subMenu.add(new Separator());
        switch (actionType) {
            case BUILD:
                add = subMenu.add(MakeProjectActionsSupport.buildAction());
                break;
            case RUN:
                add = subMenu.add(MakeProjectActionsSupport.runAction());
                break;
            case DEBUG:
                add = subMenu.add(MakeProjectActionsSupport.debugAction());
                break;
            case DEBUG_STEPINTO:
                add = subMenu.add(MakeProjectActionsSupport.stepIntoAction());//NOI18N
                break;
            default:
                assert false;
                return;
        }
        add.setText(DEFAULT_ACTION_NAME);
    }
    
    public class LauncherExecutableAction extends AbstractAction {

        private LauncherExecutor executor;
        private final Launcher launcher;
        private final RunActionItem runActionItem = new RunActionItem();

        public LauncherExecutableAction(Launcher launcher) {
            super(launcher.getDisplayedName());
            this.executor = LauncherExecutor.createExecutor(launcher, actionType, new ExecutionListenerImpl());
            this.launcher = launcher;
        }

        @Override
        public void actionPerformed(ActionEvent ae) {
            executor.execute(project);
        }
        
        private class ExecutionListenerImpl implements ExecutionListener {

            @Override
            public void executionStarted(int pid) {
                BuildExecutionSupport.registerRunningItem(runActionItem);
            }

            @Override
            public void executionFinished(int rc) {
                BuildExecutionSupport.registerFinishedItem(runActionItem);
            }
            
        }
        
        private class RunActionItem implements BuildExecutionSupport.ActionItem {

            @Override
            public String getAction() {
                switch (actionType) {
                    case BUILD:
                        return ActionProvider.COMMAND_BUILD;
                    case RUN:
                        return ActionProvider.COMMAND_RUN;
                    case DEBUG:
                        return ActionProvider.COMMAND_DEBUG;
                    case DEBUG_STEPINTO:
                        return ActionProvider.COMMAND_DEBUG_STEP_INTO;
                    default:
                        assert false;
                        return null;
                }
            }

            @Override
            public FileObject getProjectDirectory() {
                return project.getProjectDirectory();
            }

            @Override
            public String getDisplayName() {
                return ProjectUtils.getInformation(project).getDisplayName() + ": " + launcher.getDisplayedName();//NOI18N
            }

            @Override
            public void repeatExecution() {
                for (Launcher l : LaunchersRegistryFactory.getInstance(project.getProjectDirectory()).getLaunchers()) {
                    if (l.equals(launcher)) {
                        executor = LauncherExecutor.createExecutor(l, actionType, new ExecutionListenerImpl());
                        break;
                    }
                }
                executor.execute(project);
            }

            @Override
            public boolean isRunning() {
                return executor.isRunning();
            }

            @Override
            public void stopRunning() {
                // TODO
            }

            @Override
            public boolean equals(Object obj) {
                if (obj instanceof RunActionItem) {
                    return ((RunActionItem) obj).getDisplayName().equals(getDisplayName());
                }
                return false;
            }

            @Override
            public int hashCode() {
                return getDisplayName().hashCode();
            }            
        }
    }
    
    public static LauncherAction buildAsAction() {
        return new LauncherAction(ProjectActionEvent.PredefinedType.BUILD, NbBundle.getMessage(LauncherAction.class, "LBL_BuildAsAction_Name"));//NOI18N
    }
    
    public static LauncherAction runAsAction() {
        return new LauncherAction(ProjectActionEvent.PredefinedType.RUN, NbBundle.getMessage(LauncherAction.class, "LBL_RunAsAction_Name"));//NOI18N
    }
    
    public static LauncherAction debugAsAction() {
        return new LauncherAction(ProjectActionEvent.PredefinedType.DEBUG, NbBundle.getMessage(LauncherAction.class, "LBL_DebugAsAction_Name"));//NOI18N
    }
    
    public static LauncherAction stepIntoAction() {
        return new LauncherAction(ProjectActionEvent.PredefinedType.DEBUG_STEPINTO, NbBundle.getMessage(LauncherAction.class, "LBL_StepIntoAction_Name"));//NOI18N
    }
}
