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
package org.netbeans.modules.project.ui;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.ProjectConvertor;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.Parameters;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
public final class TestProject implements Project {

    static final String PROJECT_MARKER = "nbproject";   //NOI18N
    static final String CONVERTOR_MARKER = "build.gradle";  //NOI18N

    private final FileObject projectDirectory;
    private final ProjectState state;
    private final Lookup lkp;

    TestProject(
            @NonNull final FileObject projectDirectory,
            @NonNull final ProjectState state,
            @NonNull final Lookup lkp) {
        Parameters.notNull("projectDirectory", projectDirectory);   //NOI18N
        Parameters.notNull("state", state); //NOI18N
        Parameters.notNull("lkp", lkp); //NOI18N
        this.projectDirectory = projectDirectory;
        this.state = state;
        this.lkp = lkp;
    }

    @Override
    @NonNull
    public FileObject getProjectDirectory() {
        return projectDirectory;
    }

    @Override
    @NonNull
    public Lookup getLookup() {
        return lkp;
    }

    @Override
    public int hashCode() {
        return projectDirectory.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Project)) {
            return false;
        }
        return projectDirectory.equals(((Project)obj).getProjectDirectory());
    }

    @ServiceProvider(service = ProjectFactory.class)
    public static final class Factory implements ProjectFactory {

        public static volatile Callable<Lookup> LOOKUP_FACTORY = DefaultLookupFactory.INSTANCE;

        public Factory() {
        }

        @Override
        public boolean isProject(FileObject projectDirectory) {
            return projectDirectory.getFileObject(PROJECT_MARKER) != null;
        }

        @Override
        public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
            if (isProject(projectDirectory)) {
                try {
                    return new TestProject(projectDirectory, state, LOOKUP_FACTORY.call());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }

        @Override
        public void saveProject(Project project) throws IOException, ClassCastException {
        }
    }

    @ProjectConvertor.Registration(requiredPattern = ".*\\.gradle")
    public static final class Convertor implements ProjectConvertor {

        public static volatile Callable<Lookup> LOOKUP_FACTORY = DefaultLookupFactory.INSTANCE;
        public static volatile Runnable CALLBACK;

        @Override
        public Result isProject(@NonNull final FileObject projectDirectory) {
            if (projectDirectory.getFileObject(CONVERTOR_MARKER) != null) {
                try {
                    return new Result(
                        LOOKUP_FACTORY.call(),
                        new Callable<Project>() {
                            @Override
                            public Project call() throws Exception {
                                projectDirectory.createFolder(PROJECT_MARKER);
                                final Runnable action = CALLBACK;
                                if (action != null) {
                                    action.run();
                                }
                                return ProjectManager.getDefault().findProject(projectDirectory);
                            }
                        },
                        projectDirectory.getName(),
                        null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        }
    }

    static final class OpenHook extends ProjectOpenedHook {

        final AtomicInteger openCalls = new AtomicInteger();
        final AtomicInteger closeCalls = new AtomicInteger();

        @Override
        protected void projectOpened() {
            openCalls.incrementAndGet();
        }

        @Override
        protected void projectClosed() {
            closeCalls.incrementAndGet();
        }
    }

    static class DefaultLookupFactory implements Callable<Lookup> {

        static final DefaultLookupFactory INSTANCE = new DefaultLookupFactory();

        private DefaultLookupFactory() {}

        @Override
        public Lookup call() throws Exception {
            return Lookup.EMPTY;
        }
    }
}
