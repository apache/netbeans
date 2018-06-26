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
package org.netbeans.modules.javascript.grunt.file;

import java.awt.EventQueue;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.grunt.exec.GruntExecutable;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;

public final class GruntTasks implements ChangeListener {

    public static final String DEFAULT_TASK = "default"; // NOI18N

    private final Project project;
    private final Gruntfile gruntfile;
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    final FileChangeListener nodeModulesListener = new NodeModulesListener();

    private volatile List<String> tasks;


    private GruntTasks(Project project, Gruntfile gruntfile) {
        assert project != null;
        assert gruntfile != null;
        this.project = project;
        this.gruntfile = gruntfile;
    }

    public static GruntTasks create(Project project, Gruntfile gruntfile) {
        assert project != null;
        assert gruntfile != null : project.getProjectDirectory();
        GruntTasks gruntTasks = new GruntTasks(project, gruntfile);
        // listeners
        gruntfile.addChangeListener(gruntTasks);
        FileUtil.addFileChangeListener(gruntTasks.nodeModulesListener, new File(gruntfile.getFile().getParent(), "node_modules")); // NOI18N
        return gruntTasks;
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @CheckForNull
    public List<String> getTasks() {
        List<String> tasksRef = tasks;
        return tasksRef == null ? null : Collections.unmodifiableList(tasksRef);
    }

    public List<String> loadTasks(@NullAllowed Long timeout, @NullAllowed TimeUnit unit) throws ExecutionException, TimeoutException {
        List<String> tasksRef = tasks;
        if (tasksRef != null) {
            return Collections.unmodifiableList(tasksRef);
        }
        assert !EventQueue.isDispatchThread();
        Future<List<String>> tasksJob = getTasksJob();
        if (tasksJob == null) {
            // some error
            return null;
        }
        try {
            List<String> allTasks;
            if (timeout != null) {
                assert unit != null;
                allTasks = tasksJob.get(timeout, unit);
            } else {
                allTasks = tasksJob.get();
            }
            tasks = new CopyOnWriteArrayList<>(allTasks);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        return getTasks();
    }

    @CheckForNull
    private Future<List<String>> getTasksJob() {
        GruntExecutable grunt = GruntExecutable.getDefault(project, gruntfile.getFile().getParentFile(), false);
        if (grunt == null) {
            return null;
        }
        return grunt.listTasks();
    }

    public void reset() {
        tasks = null;
        changeSupport.fireChange();
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        reset();
    }

    //~ Inner classes

    private final class NodeModulesListener extends FileChangeAdapter {

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            reset();
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            reset();
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            reset();
        }

    }

}
