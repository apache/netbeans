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
package org.netbeans.modules.javascript.gulp;

import java.util.Iterator;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.gulp.exec.GulpExecutable;
import org.netbeans.modules.javascript.gulp.file.GulpTasks;
import org.netbeans.modules.javascript.gulp.file.Gulpfile;
import org.netbeans.modules.javascript.gulp.preferences.GulpPreferences;
import org.netbeans.modules.javascript.gulp.ui.customizer.GulpCustomizerProvider;
import org.netbeans.modules.javascript.gulp.util.GulpUtils;
import org.netbeans.modules.web.clientproject.spi.build.BuildToolImplementation;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;


@ProjectServiceProvider(
        service = {
            BuildToolImplementation.class,
            GulpBuildTool.class,
        }, projectType = {
            "org-netbeans-modules-web-clientproject", // NOI18N
            "org-netbeans-modules-php-project", // NOI18N
            "org-netbeans-modules-web-project", // NOI18N
            "org-netbeans-modules-maven", // NOI18N
        }
)
public final class GulpBuildTool implements BuildToolImplementation {

    private static final Logger LOGGER = Logger.getLogger(GulpBuildTool.class.getName());

    public static final String IDENTIFIER = "Gulp"; // NOI18N

    private final Project project;
    private final Gulpfile projectGulpfile;
    private final GulpTasks projectGulpTasks;
    private final GulpPreferences gulpPreferences;
    final ConcurrentMap<FileObject, GulpTasks> gulpTasks = new ConcurrentHashMap<>();
    private final ChangeListener cleanupListener = new CleanupListener();


    public GulpBuildTool(Project project) {
        assert project != null;
        this.project = project;
        projectGulpfile = Gulpfile.create(project.getProjectDirectory());
        projectGulpTasks = GulpTasks.create(project, projectGulpfile);
        gulpPreferences = new GulpPreferences(project);
    }

    @NonNull
    public static GulpBuildTool forProject(Project project) {
        GulpBuildTool buildTool = inProject(project);
        assert buildTool != null : "GulpBuildTool should be found in project " + project.getClass().getName() + " (lookup: " + project.getLookup() + ")";
        return buildTool;
    }

    @CheckForNull
    public static GulpBuildTool inProject(Project project) {
        assert project != null;
        return project.getLookup().lookup(GulpBuildTool.class);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @NbBundle.Messages("GulpBuildTool.name=Gulp")
    @Override
    public String getDisplayName() {
        return Bundle.GulpBuildTool_name();
    }

    public Gulpfile getProjectGulpfile() {
        return projectGulpfile;
    }

    public GulpTasks getProjectGulpTasks() {
        return projectGulpTasks;
    }

    public GulpTasks getGulpTasks(@NullAllowed FileObject gulpfile) {
        if (gulpfile == null
                || gulpfile.getParent().equals(project.getProjectDirectory())) {
            return getProjectGulpTasks();
        }
        GulpTasks tasks = gulpTasks.get(gulpfile);
        if (tasks != null) {
            return tasks;
        }
        Gulpfile file = Gulpfile.create(gulpfile.getParent());
        tasks = GulpTasks.create(project, file);
        GulpTasks currentTasks = gulpTasks.putIfAbsent(gulpfile, tasks);
        if (currentTasks != null) {
            return currentTasks;
        }
        // register listener
        file.addChangeListener(WeakListeners.change(cleanupListener, file));
        return tasks;
    }

    public GulpPreferences getGulpPreferences() {
        return gulpPreferences;
    }

    @Override
    public boolean isEnabled() {
        return projectGulpfile.exists();
    }

    @NbBundle.Messages("GulpBuildTool.configure=Do you want to configure project actions to call Gulp tasks?")
    @Override
    public boolean run(String commandId, boolean waitFinished, boolean warnUser) {
        assert isEnabled() : project.getProjectDirectory().getNameExt();
        assert projectGulpfile.exists() : project.getProjectDirectory().getNameExt();
        String gulpBuild = gulpPreferences.getTask(commandId);
        if (gulpBuild != null) {
            GulpExecutable gulp = GulpExecutable.getDefault(project, warnUser);
            if (gulp != null) {
                GulpUtils.logUsageGulpBuild();
                Future<Integer> result = gulp.run(gulpBuild.split(" ")); // NOI18N
                if (waitFinished) {
                    try {
                        result.get();
                    } catch (InterruptedException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                    } catch (CancellationException ex) {
                        // cancelled by user
                        LOGGER.log(Level.FINE, null, ex);
                    } catch (ExecutionException ex) {
                        LOGGER.log(Level.INFO, null, ex);
                        if (warnUser) {
                            // XXX open customizer? show error dialog?
                        }
                    }
                }
            }
        } else if (warnUser) {
            Object option = DialogDisplayer.getDefault().notify(
                    new NotifyDescriptor.Confirmation(Bundle.GulpBuildTool_configure(), NotifyDescriptor.YES_NO_OPTION));
            if (option == NotifyDescriptor.YES_OPTION) {
                project.getLookup().lookup(CustomizerProvider2.class).showCustomizer(GulpCustomizerProvider.CUSTOMIZER_IDENT, null);
            }
        }
        return true;
    }

    //~ Inner classes

    private final class CleanupListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            Iterator<FileObject> iterator = gulpTasks.keySet().iterator();
            while (iterator.hasNext()) {
                FileObject gulpfile = iterator.next();
                if (!gulpfile.isValid()) {
                    LOGGER.log(Level.FINE, "Removing invalid gulp file {0}", gulpfile);
                    iterator.remove();
                }
            }
        }

    }

}
