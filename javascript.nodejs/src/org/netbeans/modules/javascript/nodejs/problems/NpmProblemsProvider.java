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
package org.netbeans.modules.javascript.nodejs.problems;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.nodejs.exec.NpmExecutable;
import org.netbeans.modules.javascript.nodejs.file.PackageJson;
import org.netbeans.modules.javascript.nodejs.util.NodeJsUtils;
import org.netbeans.modules.javascript.nodejs.util.StringUtils;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.netbeans.spi.project.ui.support.ProjectProblemsProviderSupport;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

public final class NpmProblemsProvider implements ProjectProblemsProvider {

    final FileChangeListener fileChangeListener = new FileChangeListener();
    final Project project;
    private final ProjectProblemsProviderSupport problemsProviderSupport = new ProjectProblemsProviderSupport(this);
    private final PackageJson packageJson;


    private NpmProblemsProvider(Project project) {
        this.project = project;
        packageJson = new PackageJson(project.getProjectDirectory());
    }

    private static NpmProblemsProvider create(Project project) {
        NpmProblemsProvider npmProblemsProvider = new NpmProblemsProvider(project);
        FileObject projectDirectory = project.getProjectDirectory();
        projectDirectory.addFileChangeListener(WeakListeners.create(
                org.openide.filesystems.FileChangeListener.class, npmProblemsProvider.fileChangeListener, projectDirectory));
        return npmProblemsProvider;
    }

    @ProjectServiceProvider(service = ProjectProblemsProvider.class, projectType = "org-netbeans-modules-web-clientproject") // NOI18N
    public static ProjectProblemsProvider forHtml5Project(Project project) {
        return create(project);
    }

    @ProjectServiceProvider(service = ProjectProblemsProvider.class, projectType = "org-netbeans-modules-php-project") // NOI18N
    public static ProjectProblemsProvider forPhpProject(Project project) {
        return create(project);
    }

    @ProjectServiceProvider(service = ProjectProblemsProvider.class, projectType = "org-netbeans-modules-web-project") // NOI18N
    public static ProjectProblemsProvider forWebProject(Project project) {
        return create(project);
    }

    @ProjectServiceProvider(service = ProjectProblemsProvider.class, projectType = "org-netbeans-modules-maven") // NOI18N
    public static ProjectProblemsProvider forMavenProject(Project project) {
        return create(project);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        problemsProviderSupport.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        problemsProviderSupport.removePropertyChangeListener(listener);
    }

    @Override
    public Collection<? extends ProjectProblem> getProblems() {
        return problemsProviderSupport.getProblems(new ProjectProblemsProviderSupport.ProblemsCollector() {
            @Override
            public Collection<? extends ProjectProblem> collectProblems() {
                Collection<ProjectProblemsProvider.ProjectProblem> currentProblems = new ArrayList<>();
                checkDependencies(currentProblems);
                return currentProblems;
            }
        });
    }

    @NbBundle.Messages({
        "NpmProblemsProvider.dependencies.none.title=Missing npm modules",
        "# {0} - project name",
        "NpmProblemsProvider.dependencies.none.description=Project {0} uses npm modules but they are not installed.",
    })
    void checkDependencies(Collection<ProjectProblem> currentProblems) {
        if (npmInstallRequired()) {
            ProjectProblem problem = ProjectProblem.createWarning(
                    Bundle.NpmProblemsProvider_dependencies_none_title(),
                    Bundle.NpmProblemsProvider_dependencies_none_description(NodeJsUtils.getProjectDisplayName(project)),
                    new ProjectProblemResolverImpl("npmInstall", new NpmInstallResult())); // NOI18N
            currentProblems.add(problem);
        }
    }

    boolean npmInstallRequired() {
        if (!packageJson.exists()) {
            return false;
        }
        if (packageJson.getNodeModulesDir().isDirectory()) {
            return false;
        }
        return !packageJson.getDependencies().isEmpty();
    }

    void fireProblemsChanged() {
        problemsProviderSupport.fireProblemsChange();
    }

    //~ Inner classes

    private final class FileChangeListener extends FileChangeAdapter {

        @Override
        public void fileDataCreated(FileEvent fe) {
            processFileChange(fe.getFile().getNameExt());
        }

        @Override
        public void fileChanged(FileEvent fe) {
            processFileChange(fe.getFile().getNameExt());
        }

        @Override
        public void fileFolderCreated(FileEvent fe) {
            processFolderChange(fe.getFile().getNameExt());
        }

        @Override
        public void fileDeleted(FileEvent fe) {
            processFolderChange(fe.getFile().getNameExt());
        }

        @Override
        public void fileRenamed(FileRenameEvent fe) {
            String oldName = fe.getName() + (StringUtils.hasText(fe.getExt()) ? "." + fe.getExt() : ""); // NOI18N
            processFileChange(fe.getFile().getNameExt());
            processFileChange(oldName);
            processFolderChange(fe.getFile().getNameExt());
            processFolderChange(oldName);
        }

        private void processFileChange(String fileName) {
            if (PackageJson.FILE_NAME.equals(fileName)) {
                fireProblemsChanged();
            }
        }

        private void processFolderChange(String folderName) {
            if (PackageJson.NODE_MODULES_DIR.equals(folderName)) {
                fireProblemsChanged();
            }
        }

    }

    private static final class ProjectProblemResolverImpl implements ProjectProblemResolver {

        private final String ident;
        private final Future<Result> resolver;


        public ProjectProblemResolverImpl(String ident, Future<Result> resolver) {
            assert ident != null;
            assert resolver != null;
            this.ident = ident;
            this.resolver = resolver;
        }

        @Override
        public Future<Result> resolve() {
            return resolver;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 71 * hash + Objects.hashCode(this.ident);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ProjectProblemResolverImpl other = (ProjectProblemResolverImpl) obj;
            if (!Objects.equals(this.ident, other.ident)) {
                return false;
            }
            return true;
        }

    }

    private final class NpmInstallResult implements Future<Result> {

        // @GuardedBy("this")
        private Future<Integer> task;


        @Override
        public synchronized boolean cancel(boolean mayInterruptIfRunning) {
            if (task == null) {
                return false;
            }
            return task.cancel(mayInterruptIfRunning);
        }

        @Override
        public synchronized boolean isCancelled() {
            if (task == null) {
                return false;
            }
            return task.isCancelled();
        }

        @Override
        public synchronized boolean isDone() {
            if (task == null) {
                return false;
            }
            return task.isDone();
        }

        @Override
        public Result get() throws InterruptedException, ExecutionException {
            try {
                getTask().get();
            } catch (CancellationException ex) {
                // cancelled by user
            }
            if (npmInstallRequired()) {
                synchronized (this) {
                    task = null;
                }
                return Result.create(Status.UNRESOLVED);
            }
            fireProblemsChanged();
            return Result.create(Status.RESOLVED);
        }

        @Override
        public Result get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }

        public synchronized Future<Integer> getTask() {
            if (task == null) {
                NpmExecutable npm = NpmExecutable.getDefault(project, true);
                if (npm != null) {
                    NodeJsUtils.logUsageNpmInstall();
                    task = npm.install();
                } else {
                    task = new DummyTask();
                }
            }
            return task;
        }

    }

    private static final class DummyTask implements Future<Integer> {

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
            return false;
        }

        @Override
        public Integer get() throws InterruptedException, ExecutionException {
            return -1;
        }

        @Override
        public Integer get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return get();
        }

    }

}
