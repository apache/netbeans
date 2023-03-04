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
package org.netbeans.modules.php.phing.ui.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.phing.PhingBuildTool;
import org.netbeans.modules.php.phing.exec.PhingExecutable;
import org.netbeans.modules.php.phing.file.PhingTargets;
import org.netbeans.modules.php.phing.ui.options.PhingOptionsPanelController;
import org.netbeans.modules.php.phing.util.PhingUtils;
import org.netbeans.modules.web.clientproject.api.build.BuildTools;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

@ActionID(id = "org.netbeans.modules.php.phing.ui.actions.RunPhingTargetAction", category = "Build")
@ActionRegistration(displayName = "#RunPhingTargetAction.name", lazy = false)
@ActionReferences({
    @ActionReference(path = "Loaders/text/x-ant+xml/Actions", position = 150),
    // XXX cannot be added here since empty space is shown if action is disabled :/
    //@ActionReference(path = "Editors/text/x-ant+xml/Popup", position = 850),
    @ActionReference(path = "Projects/org-netbeans-modules-php-project/Actions", position = 120),
})
@NbBundle.Messages("RunPhingTargetAction.name=Phing Targets")
public final class RunPhingTargetAction extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    static final Logger LOGGER = Logger.getLogger(RunPhingTargetAction.class.getName());

    @NullAllowed
    private final Project project;
    @NullAllowed
    private final FileObject buildXml;


    public RunPhingTargetAction() {
        this(null);
    }

    private RunPhingTargetAction(Project project) {
        this(project, null);
    }

    private RunPhingTargetAction(Project project, FileObject buildXml) {
        this.project = project;
        this.buildXml = buildXml;
        setEnabled(project != null);
        putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
        // hide this action in IDE Options > Keymap
        putValue(Action.NAME, null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        assert false;
    }

    @Override
    public Action createContextAwareInstance(Lookup context) {
        Project contextProject = context.lookup(Project.class);
        if (contextProject != null) {
            // project action
            return createAction(contextProject);
        }
        // file action?
        FileObject file = context.lookup(FileObject.class);
        if (file == null) {
            DataObject dataObject = context.lookup(DataObject.class);
            if (dataObject != null) {
                file = dataObject.getPrimaryFile();
            }
        }
        if (file == null) {
            return this;
        }
        contextProject = ProjectConvertors.getNonConvertorOwner(file);
        if (contextProject == null) {
            return this;
        }
        if (file.getParent().equals(contextProject.getProjectDirectory())) {
            return createAction(contextProject);
        }
        return createAction(contextProject, file);
    }

    private Action createAction(Project contextProject) {
        return createAction(contextProject, null);
    }

    private Action createAction(Project contextProject, @NullAllowed FileObject buildXml) {
        assert contextProject != null;
        PhingBuildTool phingBuildTool = PhingBuildTool.inProject(contextProject);
        if (phingBuildTool == null) {
            return this;
        }
        if (buildXml != null) {
            return new RunPhingTargetAction(contextProject, buildXml);
        }
        if (!phingBuildTool.getProjectBuildXml().exists()) {
            return this;
        }
        return new RunPhingTargetAction(contextProject);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        if (project == null) {
            return new Actions.MenuItem(this, false);
        }
        return BuildTools.getDefault().createTasksMenu(new TasksMenuSupportImpl(project, buildXml));
    }

    //~ Inner classes

    private static final class TasksMenuSupportImpl implements BuildTools.TasksMenuSupport {

        private final Project project;
        @NullAllowed
        private final FileObject buildXml;
        private final PhingTargets phingTargets;


        public TasksMenuSupportImpl(Project project, @NullAllowed FileObject buildXml) {
            assert project != null;
            this.project = project;
            this.buildXml = buildXml;
            phingTargets = PhingBuildTool.forProject(project).getPhingTargets(buildXml);
        }

        @Override
        public Project getProject() {
            return project;
        }

        @Override
        public FileObject getWorkDir() {
            if (buildXml == null) {
                return project.getProjectDirectory();
            }
            return buildXml.getParent();
        }

        @Override
        public String getIdentifier() {
            return PhingBuildTool.IDENTIFIER;
        }

        @Override
        public String getBuildToolExecName() {
            return PhingExecutable.PHING_NAME;
        }

        @NbBundle.Messages({
            "TasksMenuSupportImpl.targets.label=&Target(s)",
            "TasksMenuSupportImpl.targets.loading=Loading Targets...",
            "TasksMenuSupportImpl.targets.manage.advanced=Manage Target(s)",
            "TasksMenuSupportImpl.phing.configure=Configure Phing...",
        })
        @Override
        public String getTitle(BuildTools.TasksMenuSupport.Title title) {
            switch (title) {
                case MENU:
                    return Bundle.RunPhingTargetAction_name();
                case LOADING_TASKS:
                    return Bundle.TasksMenuSupportImpl_targets_loading();
                case CONFIGURE_TOOL:
                    return Bundle.TasksMenuSupportImpl_phing_configure();
                case MANAGE_ADVANCED:
                    return Bundle.TasksMenuSupportImpl_targets_manage_advanced();
                case TASKS_LABEL:
                    return Bundle.TasksMenuSupportImpl_targets_label();
                default:
                    assert false : "Unknown title: " + title;
            }
            return null;
        }

        @Override
        public String getDefaultTaskName() {
            return PhingTargets.DEFAULT_TARGET;
        }

        @Override
        public Future<List<String>> getTasks() {
            return new TargetsFuture(phingTargets);
        }

        @Override
        public void runTask(String... args) {
            assert !EventQueue.isDispatchThread();
            PhingExecutable phing = getPhingExecutable();
            if (phing != null) {
                PhingUtils.logUsagePhingBuild();
                phing.run(args);
            }
        }

        @Override
        public void reloadTasks() {
            assert !EventQueue.isDispatchThread();
            phingTargets.reset();
            try {
                phingTargets.loadTargets(null, null);
            } catch (ExecutionException | TimeoutException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        @Override
        public void configure() {
            OptionsDisplayer.getDefault().open(PhingOptionsPanelController.OPTIONS_PATH);
        }

        @CheckForNull
        private PhingExecutable getPhingExecutable() {
            if (buildXml == null) {
                return PhingExecutable.getDefault(project, true);
            }
            return PhingExecutable.getDefault(project, FileUtil.toFile(buildXml).getParentFile(), true);
        }

    }

    private static final class TargetsFuture implements Future<List<String>> {

        private final PhingTargets phingTargets;


        public TargetsFuture(PhingTargets phingTargets) {
            assert phingTargets != null;
            this.phingTargets = phingTargets;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return phingTargets.getTargets() != null;
        }

        @Override
        public List<String> get() throws InterruptedException, ExecutionException {
            try {
                return phingTargets.loadTargets(null, null);
            } catch (TimeoutException ex) {
                assert false;
            }
            return null;
        }

        @Override
        public List<String> get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return phingTargets.loadTargets(timeout, unit);
        }

    }

}
