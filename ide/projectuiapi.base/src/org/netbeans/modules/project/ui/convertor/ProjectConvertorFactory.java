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
package org.netbeans.modules.project.ui.convertor;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.uiapi.ProjectConvertorServiceFactory;
import org.netbeans.modules.project.uiapi.ProjectOpenedTrampoline;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectFactory2;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.ProjectConvertor;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.Parameters;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Tomas Zezula
 */
@ServiceProvider(service = ProjectFactory.class, position = Integer.MAX_VALUE)
public final class ProjectConvertorFactory implements ProjectFactory2 {

    private final Lookup.Result<ProjectConvertorAcceptor> acceptors;
    private final Lookup.Result<ProjectConvertorServiceFactory> services;
    private final Set</*@GuardedBy("ProjectManager.mutex")*/FileObject> excluded;

    public ProjectConvertorFactory() {
        this.acceptors = Lookup.getDefault().lookupResult(ProjectConvertorAcceptor.class);
        this.services = Lookup.getDefault().lookupResult(ProjectConvertorServiceFactory.class);
        this.excluded = new HashSet<>();
    }

    @Override
    @CheckForNull
    public ProjectManager.Result isProject2(@NonNull FileObject projectDirectory) {
        Parameters.notNull("projectDirectory", projectDirectory);   //NOI18N
        final ProjectConvertor.Result res = isProjectImpl(projectDirectory);
        return res != null ?
            toProjectManagerResult(res) :
            null;
    }

    @Override
    public boolean isProject(@NonNull FileObject projectDirectory) {
        return isProject2(projectDirectory) != null;
    }

    @Override
    @CheckForNull
    public Project loadProject(
        @NonNull final FileObject projectDirectory,
        @NonNull final ProjectState state) throws IOException {
        Parameters.notNull("projectDirectory", projectDirectory);   //NOI18N
        Parameters.notNull("state", state); //NOI18N
        final ProjectConvertor.Result res = isProjectImpl(projectDirectory);
        return res != null ?
            new ConvertorProject(projectDirectory, state, res):
            null;
    }

    @Override
    public void saveProject(@NonNull final Project project) throws IOException, ClassCastException {
        Parameters.notNull("project", project); //NOI18N
        throw new IllegalStateException("ConvertorProject cannot be modified"); //NOI18N
    }

    public static boolean isConvertorProject(@NonNull final Project project) {
        Parameters.notNull("project", project); //NOI18N
        return project.getLookup().lookup(ConvertorProject.class) != null;
    }

    public static void unregisterConvertorProject(@NonNull final Project project) {
        Parameters.notNull("project", project);
        if (!ProjectManager.mutex().isWriteAccess()) {
            throw new IllegalStateException("Requires a write access on the ProjectManager's mutex");   //NOI18N
        }
        final ConvertorProject cp = project.getLookup().lookup(ConvertorProject.class);
        if (cp == null) {
            throw new IllegalArgumentException(String.format(
                "The project: %s located in: %s of type: %s is not a convertor project.", //NOI18N
                ProjectUtils.getInformation(project).getDisplayName(),
                FileUtil.getFileDisplayName(project.getProjectDirectory()),
                project.getClass().getName()
            ));
        }
        cp.projectState.notifyDeleted();
    }

    @CheckForNull
    private ProjectConvertor.Result isProjectImpl(@NonNull final FileObject projectDirectory) {
        return ProjectManager.mutex().readAccess(new Mutex.Action<ProjectConvertor.Result>() {
            @Override
            public ProjectConvertor.Result run() {
                if (!excluded.contains(projectDirectory)) {
                    for (ProjectConvertorAcceptor pc : acceptors.allInstances()) {
                        ProjectConvertor.Result result = pc.isProject(projectDirectory);
                        if (result != null) {
                            return result;
                        }
                    }
                }
                return null;
            }
        });
    }

    @NonNull
    private ProjectManager.Result toProjectManagerResult(@NonNull final ProjectConvertor.Result res) {
        return new ProjectManager.Result(res.getDisplayName(), null, res.getIcon());
    }

    private final class ConvertorProject implements Project {
        private final FileObject projectDirectory;
        private final ProjectState projectState;
        private final ProjectConvertor.Result result;
        private final ThreadLocal<DynamicLookup> underConstruction;
        //@GuardedBy("this")
        private DynamicLookup projectLkp;

        @SuppressWarnings("LeakingThisInConstructor")
        ConvertorProject(
            @NonNull final FileObject projectDirectory,
            @NonNull final ProjectState projectState,
            @NonNull final ProjectConvertor.Result result) {
            Parameters.notNull("projectDirectory", projectDirectory);   //NOI18N
            Parameters.notNull("projectState", projectState);   //NOI18N
            Parameters.notNull("result", result); //NOI18N
            this.projectDirectory = projectDirectory;
            this.projectState = projectState;
            this.result = result;
            this.underConstruction = new ThreadLocal<>();
        }

        @Override
        @NonNull
        public FileObject getProjectDirectory() {
            return projectDirectory;
        }

        @Override
        @NonNull
        public DynamicLookup getLookup() {
            DynamicLookup dynLkp = underConstruction.get();
            if (dynLkp != null) {
                return dynLkp;
            }
            synchronized (this) {
                dynLkp = projectLkp;
            }
            if (dynLkp != null) {
                return  dynLkp;
            }
            dynLkp = new DynamicLookup(Lookups.singleton(this));
            try {
                underConstruction.set(dynLkp);
                final Lookup preLkp = Lookups.fixed(new OpenHook(), this);
                final Lookup convertorLkp = this.result.getLookup();
                if (convertorLkp == null) {
                    throw new IllegalStateException(String.format(
                        "Convertor: %s returned null lookup.",  //NOI18N
                        this.result));
                }
                final Queue<Object> postServices = new ArrayDeque<>();
                for (ProjectConvertorServiceFactory f : services.allInstances()) {
                    postServices.addAll(f.createServices(this, result));
                }
                final Lookup postLkp = Lookups.fixed(postServices.toArray());
                dynLkp.setBaseLookups(preLkp, convertorLkp, postLkp);
                synchronized (this) {
                    if (projectLkp == null) {
                        projectLkp = dynLkp;
                    } else {
                        dynLkp = projectLkp;
                    }
                }
                return dynLkp;
            } finally {
                underConstruction.remove();
            }
        }

        @Override
        public int hashCode() {
            return projectDirectory.hashCode();
        }

        @Override
        public boolean equals(@NullAllowed final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof Project)) {
                return false;
            }
            return projectDirectory.equals(((Project)obj).getProjectDirectory());
        }

        @NonNull
        private Project createProject() throws IOException {
            try {
                return ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Project>() {
                    @Override
                    public Project run() throws Exception {
                        excluded.add(projectDirectory);
                        try {
                            projectState.notifyDeleted();
                            final Project prj = result.createProject();
                            if (prj == null) {
                                throw new IllegalStateException(String.format(
                                    "The convertor %s created null project.",   //NOI18N
                                    result));
                            }
                            //Set the Lookup to the created project lookup
                            //Remove OpenHook as the OpenProjectList calls all POH in
                            //the project's Lookup even there the non merged, so it's safer
                            //to remove it.
                            //Also remove ProjectInfo as it's overriden by project's own
                            //no need for it anymore
                            final DynamicLookup dynLkp = getLookup();
                            final Lookup[] baseLkps = dynLkp.getBaseLookups();
                            dynLkp.setBaseLookups(
                                baseLkps[1],
                                prj.getLookup());
                            return prj;
                        } finally {
                            excluded.remove(projectDirectory);
                        }
                    }
                });
            } catch (final MutexException e) {
                final Exception root = e.getException();
                if (root instanceof RuntimeException) {
                    throw (RuntimeException) root;
                } else if (root instanceof IOException) {
                    throw (IOException) root;
                } else {
                    throw new RuntimeException(root);
                }
            }
        }

        private final class OpenHook extends ProjectOpenedHook {

            @Override
            protected void projectOpened() {
                try {
                    final Project prj = createProject();
                    for( ProjectOpenedHook hook : prj.getLookup().lookupAll(ProjectOpenedHook.class)) {
                        ProjectOpenedTrampoline.DEFAULT.projectOpened(hook);
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }

            @Override
            protected void projectClosed() {
                //Not called as the OpenHook is removed from project's Lookup
                //anyway it should do nothing as the OpenProjectList call all
                //POH in registered in the project's Lookup
            }
        }
    }

    private static final class DynamicLookup extends ProxyLookup {

        DynamicLookup() {
            super();
        }

        DynamicLookup(Lookup... lkps) {
            super(lkps);
        }

        void setBaseLookups(@NonNull final Lookup... lkps) {
            Parameters.notNull("lkps", lkps); //NOI18N
            setLookups(lkps);
        }

        @NonNull
        Lookup[] getBaseLookups() {
            return super.getLookups();
        }
    }
}
