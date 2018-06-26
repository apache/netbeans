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
package org.netbeans.modules.javascript.gulp.ui.actions;

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
import org.netbeans.modules.javascript.gulp.GulpBuildTool;
import org.netbeans.modules.javascript.gulp.GulpBuildToolSupport;
import org.netbeans.modules.javascript.gulp.file.GulpTasks;
import org.netbeans.modules.javascript.gulp.ui.options.GulpOptionsPanelController;
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

@ActionID(id = "org.netbeans.modules.javascript.gulp.ui.actions.RunGulpTaskAction", category = "Build")
@ActionRegistration(displayName = "#RunGulpTaskAction.name", lazy = false)
@ActionReferences({
    @ActionReference(path = "Editors/text/gulp+javascript/Popup", position = 795),
    @ActionReference(path = "Loaders/text/gulp+javascript/Actions", position = 150),
    @ActionReference(path = "Projects/org-netbeans-modules-web-clientproject/Actions", position = 181),
    @ActionReference(path = "Projects/org-netbeans-modules-php-project/Actions", position = 131),
    @ActionReference(path = "Projects/org-netbeans-modules-web-project/Actions", position = 671),
    @ActionReference(path = "Projects/org-netbeans-modules-maven/Actions", position = 771),
})
@NbBundle.Messages("RunGulpTaskAction.name=Gulp Tasks")
public final class RunGulpTaskAction extends AbstractAction implements ContextAwareAction, Presenter.Popup {

    static final Logger LOGGER = Logger.getLogger(RunGulpTaskAction.class.getName());

    @NullAllowed
    private final Project project;
    @NullAllowed
    private final FileObject gulpfile;


    public RunGulpTaskAction() {
        this(null);
    }

    private RunGulpTaskAction(Project project) {
        this(project, null);
    }

    private RunGulpTaskAction(Project project, FileObject gulpfile) {
        this.project = project;
        this.gulpfile = gulpfile;
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
        // gulpfile directly
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

    private Action createAction(Project contextProject, @NullAllowed FileObject gulpfile) {
        assert contextProject != null;
        GulpBuildTool gulpBuildTool = GulpBuildTool.inProject(contextProject);
        if (gulpBuildTool == null) {
            return this;
        }
        if (gulpfile != null) {
            return new RunGulpTaskAction(contextProject, gulpfile);
        }
        if (!gulpBuildTool.getProjectGulpfile().exists()) {
            return this;
        }
        return new RunGulpTaskAction(contextProject);
    }

    @Override
    public JMenuItem getPopupPresenter() {
        if (project == null) {
            return new Actions.MenuItem(this, false);
        }
        return BuildTools.getDefault().createTasksMenu(new TasksMenuSupportImpl(project, gulpfile));
    }

    //~ Inner classes

    private static final class TasksMenuSupportImpl extends GulpBuildToolSupport implements BuildTools.TasksMenuSupport {

        public TasksMenuSupportImpl(Project project, FileObject gulpfile) {
            super(project, gulpfile);
        }

        @NbBundle.Messages({
            "TasksMenuSupportImpl.tasks.label=&Task(s)",
            "TasksMenuSupportImpl.tasks.loading=Loading Tasks...",
            "TasksMenuSupportImpl.tasks.manage.advanced=Manage Task(s)",
            "TasksMenuSupportImpl.gulp.configure=Configure Gulp...",
        })
        @Override
        public String getTitle(BuildTools.TasksMenuSupport.Title title) {
            switch (title) {
                case MENU:
                    return Bundle.RunGulpTaskAction_name();
                case LOADING_TASKS:
                    return Bundle.TasksMenuSupportImpl_tasks_loading();
                case CONFIGURE_TOOL:
                    return Bundle.TasksMenuSupportImpl_gulp_configure();
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
            return GulpTasks.DEFAULT_TASK;
        }

        @Override
        public void reloadTasks() {
            assert !EventQueue.isDispatchThread();
            gulpTasks.reset();
            try {
                gulpTasks.loadTasks(null, null);
            } catch (ExecutionException | TimeoutException ex) {
                LOGGER.log(Level.INFO, null, ex);
            }
        }

        @Override
        public void configure() {
            OptionsDisplayer.getDefault().open(GulpOptionsPanelController.OPTIONS_PATH);
        }

    }

}
