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
