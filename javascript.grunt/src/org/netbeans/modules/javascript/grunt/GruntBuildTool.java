/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
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
