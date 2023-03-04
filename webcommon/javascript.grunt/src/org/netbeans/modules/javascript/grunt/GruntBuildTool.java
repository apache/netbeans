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
package org.netbeans.modules.javascript.grunt;

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
import org.netbeans.modules.javascript.grunt.exec.GruntExecutable;
import org.netbeans.modules.javascript.grunt.file.GruntTasks;
import org.netbeans.modules.javascript.grunt.file.Gruntfile;
import org.netbeans.modules.javascript.grunt.preferences.GruntPreferences;
import org.netbeans.modules.javascript.grunt.ui.customizer.GruntCustomizerProvider;
import org.netbeans.modules.javascript.grunt.util.GruntUtils;
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
            GruntBuildTool.class,
        }, projectType = {
            "org-netbeans-modules-web-clientproject", // NOI18N
            "org-netbeans-modules-php-project", // NOI18N
            "org-netbeans-modules-web-project", // NOI18N
            "org-netbeans-modules-maven", // NOI18N
        }
)
public final class GruntBuildTool implements BuildToolImplementation {

    private static final Logger LOGGER = Logger.getLogger(GruntBuildTool.class.getName());

    public static final String IDENTIFIER = "Grunt"; // NOI18N

    private final Project project;
    private final Gruntfile projectGruntfile;
    private final GruntTasks projectGruntTasks;
    private final GruntPreferences gruntPreferences;
    final ConcurrentMap<FileObject, GruntTasks> gruntTasks = new ConcurrentHashMap<>();
    private final ChangeListener cleanupListener = new CleanupListener();


    public GruntBuildTool(Project project) {
        assert project != null;
        this.project = project;
        projectGruntfile = Gruntfile.create(project.getProjectDirectory());
        projectGruntTasks = GruntTasks.create(project, projectGruntfile);
        gruntPreferences = new GruntPreferences(project);
    }

    @NonNull
    public static GruntBuildTool forProject(Project project) {
        GruntBuildTool buildTool = inProject(project);
        assert buildTool != null : "GruntBuildTool should be found in project " + project.getClass().getName() + " (lookup: " + project.getLookup() + ")";
        return buildTool;
    }

    @CheckForNull
    public static GruntBuildTool inProject(Project project) {
        assert project != null;
        return project.getLookup().lookup(GruntBuildTool.class);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @NbBundle.Messages("GruntBuildTool.name=Grunt")
    @Override
    public String getDisplayName() {
        return Bundle.GruntBuildTool_name();
    }

    public Gruntfile getProjectGruntfile() {
        return projectGruntfile;
    }

    public GruntTasks getProjectGruntTasks() {
        return projectGruntTasks;
    }

    public GruntTasks getGruntTasks(@NullAllowed FileObject gruntfile) {
        if (gruntfile == null
                || gruntfile.getParent().equals(project.getProjectDirectory())) {
            return getProjectGruntTasks();
        }
        GruntTasks tasks = gruntTasks.get(gruntfile);
        if (tasks != null) {
            return tasks;
        }
        Gruntfile file = Gruntfile.create(gruntfile.getParent());
        tasks = GruntTasks.create(project, file);
        GruntTasks currentTasks = gruntTasks.putIfAbsent(gruntfile, tasks);
        if (currentTasks != null) {
            return currentTasks;
        }
        // register listener
        file.addChangeListener(WeakListeners.change(cleanupListener, file));
        return tasks;
    }

    public GruntPreferences getGruntPreferences() {
        return gruntPreferences;
    }

    @Override
    public boolean isEnabled() {
        return projectGruntfile.exists();
    }

    @NbBundle.Messages("GruntBuildTool.configure=Do you want to configure project actions to call Grunt tasks?")
    @Override
    public boolean run(String commandId, boolean waitFinished, boolean warnUser) {
        assert isEnabled() : project.getProjectDirectory().getNameExt();
        assert projectGruntfile.exists() : project.getProjectDirectory().getNameExt();
        String gruntBuild = gruntPreferences.getTask(commandId);
        if (gruntBuild != null) {
            GruntExecutable grunt = GruntExecutable.getDefault(project, warnUser);
            if (grunt != null) {
                GruntUtils.logUsageGruntBuild();
                Future<Integer> result = grunt.run(gruntBuild.split(" ")); // NOI18N
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
                    new NotifyDescriptor.Confirmation(Bundle.GruntBuildTool_configure(), NotifyDescriptor.YES_NO_OPTION));
            if (option == NotifyDescriptor.YES_OPTION) {
                project.getLookup().lookup(CustomizerProvider2.class).showCustomizer(GruntCustomizerProvider.CUSTOMIZER_IDENT, null);
            }
        }
        return true;
    }

    //~ Inner classes

    private final class CleanupListener implements ChangeListener {

        @Override
        public void stateChanged(ChangeEvent e) {
            Iterator<FileObject> iterator = gruntTasks.keySet().iterator();
            while (iterator.hasNext()) {
                FileObject gruntfile = iterator.next();
                if (!gruntfile.isValid()) {
                    LOGGER.log(Level.FINE, "Removing invalid grunt file {0}", gruntfile);
                    iterator.remove();
                }
            }
        }

    }

}
