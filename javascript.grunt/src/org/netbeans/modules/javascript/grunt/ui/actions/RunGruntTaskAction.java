/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript.grunt.ui.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.grunt.GruntBuildTool;
import org.netbeans.modules.javascript.grunt.GruntBuildToolSupport;
import org.netbeans.modules.javascript.grunt.file.GruntTasks;
import org.netbeans.modules.javascript.grunt.ui.options.GruntOptionsPanelController;
import org.netbeans.modules.web.clientproject.api.build.BuildTools;
import org.netbeans.spi.project.ui.support.ProjectConvertors;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;

@ActionID(id = "org.netbeans.modules.javascript.grunt.ui.actions.RunGruntTaskAction", category = "Build")
@ActionRegistration(displayName = "#RunGruntTaskAction.name", lazy = false)
@ActionReferences({
    @ActionReference(path = "Editors/text/grunt+javascript/Popup", position = 790),
    @ActionReference(path = "Loaders/text/grunt+javascript/Actions", position = 150),
    @ActionReference(path = "Projects/org-netbeans-modules-web-clientproject/Actions", position = 180),
    @ActionReference(path = "Projects/org-netbeans-modules-php-project/Actions", position = 130),
    @ActionReference(path = "Projects/org-netbeans-modules-web-project/Actions", position = 670),
    @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 770),
})
@NbBundle.Messages("RunGruntTaskAction.name=Grunt Tasks")
public final class RunGruntTaskAction extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    static final Logger LOGGER = Logger.getLogger(RunGruntTaskAction.class.getName());

    @NullAllowed
    private final Project project;
    @NullAllowed
    private final FileObject gruntfile;


    public RunGruntTaskAction() {
        this(null);
    }

    private RunGruntTaskAction(Project project) {
        this(project, null);
    }

    private RunGruntTaskAction(Project project, FileObject gruntfile) {
        this.project = project;
        this.gruntfile = gruntfile;
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
        // gruntfile directly
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

    private Action createAction(Project contextProject, @NullAllowed FileObject gruntfile) {
        assert contextProject != null;
        GruntBuildTool gruntBuildTool = GruntBuildTool.inProject(contextProject);
        if (gruntBuildTool == null) {
            return this;
        }
        if (gruntfile != null) {
            return new RunGruntTaskAction(contextProject, gruntfile);
        }
        if (!gruntBuildTool.getProjectGruntfile().exists()) {
            return this;
        }
        return new RunGruntTaskAction(contextProject);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        if (project == null) {
            return new Actions.MenuItem(this, false);
        }
        return BuildTools.getDefault().createTasksMenu(new TasksMenuSupportImpl(project, gruntfile));
    }

    //~ Inner classes

    private static final class TasksMenuSupportImpl extends GruntBuildToolSupport implements BuildTools.TasksMenuSupport {

        public TasksMenuSupportImpl(Project project, @NullAllowed FileObject gruntfile) {
            super(project, gruntfile);
        }

        @NbBundle.Messages({
            "TasksMenuSupportImpl.tasks.label=&Task(s)",
            "TasksMenuSupportImpl.tasks.loading=Loading Tasks...",
            "TasksMenuSupportImpl.tasks.manage.advanced=Manage Task(s)",
            "TasksMenuSupportImpl.grunt.configure=Configure Grunt...",
        })
        @Override
        public String getTitle(Title title) {
            switch (title) {
                case MENU:
                    return Bundle.RunGruntTaskAction_name();
                case LOADING_TASKS:
                    return Bundle.TasksMenuSupportImpl_tasks_loading();
                case CONFIGURE_TOOL:
                    return Bundle.TasksMenuSupportImpl_grunt_configure();
                case MANAGE_ADVANCED:
                    return Bundle.TasksMenuSupportImpl_tasks_manage_advanced();
                case TASKS_LABEL:
                    return Bundle.TasksMenuSupportImpl_tasks_label();
                default:
                    assert false : "Unknown title: " + title;
            }
            return null;
        }

        @Override
        public String getDefaultTaskName() {
            return GruntTasks.DEFAULT_TASK;
        }

        @Override
        public void reloadTasks() {
            assert !EventQueue.isDispatchThread();
            gruntTasks.reset();
            try {
                gruntTasks.loadTasks(null, null);
            } catch (ExecutionException | TimeoutException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        @Override
        public void configure() {
            OptionsDisplayer.getDefault().open(GruntOptionsPanelController.OPTIONS_PATH);
        }

    }

}
