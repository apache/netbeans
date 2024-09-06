/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.project.dependency.reload;

import java.io.Closeable;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.dependency.ProjectReload;
import org.netbeans.modules.project.dependency.reload.MockProjectReloadImplementation.ProjectData;
import org.netbeans.modules.project.dependency.spi.ProjectReloadImplementation;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author sdedic
 */
public class MockProjectReloadImplementation implements ProjectReloadImplementation<MockProjectReloadImplementation.ProjectData>,
        Closeable, ProjectReloadImplementation.ExtendedQuery<ProjectData> {
    Project project;

    public MockProjectReloadImplementation() {
    }

    public MockProjectReloadImplementation(Project project) {
        this.project = project;
    }
    
    public void setProject(Project p) {
        this.project = p;
    }
    
    Map<Object, Reference<ProjectStateData<ProjectData>>> lastData = new HashMap<>();
    
    public static class ProjectData {
        public String contents;
        public volatile int counter;

        public ProjectData(String s) {
            this.contents = s;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.contents);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ProjectData other = (ProjectData) obj;
            return Objects.equals(this.contents, other.contents);
        }
    }
    
    private static final WeakReference<ProjectStateData<ProjectData>> NULL = new WeakReference<>(null);

    protected ProjectStateBuilder<ProjectData> createStateData(ProjectStateBuilder b, ProjectReload.StateRequest request) {
        FileObject dir = project.getProjectDirectory();
        FileObject pf = dir.getFileObject("project.txt");
        b.files(pf, dir.getFileObject("settings.properties"));
        try {
            ProjectData pd = new ProjectData(pf.asText());
            b.data(pd);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return b;
    }
    
    volatile boolean closeCalled = false;
    volatile boolean releasedCalled = false;

    @Override
    public void close() throws IOException {
        closeCalled = true;
    }

    @Override
    public void projectDataReleased(ProjectStateData<ProjectData> data) {
        if (data == sdRef.get()) {
            releasedCalled = true;
        }
    }
    
    Reference<ProjectStateData> sdRef = new WeakReference<>(null);
    
    volatile ProjectData loadProjectData;
    
    protected ProjectStateData doCreateStateData(Project project, ProjectReload.StateRequest request, LoadContext<ProjectData> context) {
        ProjectStateBuilder<ProjectData> b = ProjectStateData.builder(
                request.getMinQuality().isAtLeast(ProjectReload.Quality.LOADED) ? request.getMinQuality() : ProjectReload.Quality.LOADED);
        b.timestamp(Instant.now().toEpochMilli());
        loadProjectData = context.getLoadContext(ProjectData.class);
        ProjectStateData<ProjectData> psd = createStateData(b, request).
                privateData(
                    (d) -> {
                        sdRef = new WeakReference<>(d);
                        return this;
                    }).
                build();
        return psd;
    }

    @Override
    public boolean satisfies(ProjectReload.StateRequest pending, ProjectReload.StateRequest current) {
        return true;
    }

    @Override
    public boolean checkState(ProjectReload.StateRequest request, ProjectStateData<ProjectData> data) {
        return true;
    }
    
    @Override
    public Object createVariant(Lookup l) {
        return null;
    }
    
    @Override
    public CompletableFuture<ProjectStateData<ProjectData>> reload(Project project, ProjectReload.StateRequest request, LoadContext<ProjectData> context) {
        ProjectStateData psd = doCreateStateData(project, request, context);
        Object k = createVariant(request.getContext());
        lastData.put(k, new WeakReference<>(psd));
        return CompletableFuture.completedFuture(psd);
    }
}
