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
package org.netbeans.modules.project.ui.fallback;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.ProjectFactory;
import org.netbeans.spi.project.ProjectState;
import org.openide.filesystems.FileObject;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = ProjectFactory.class, position = 1_000_000)
public final class FallbackFactory implements ProjectFactory {
    private static final ThreadLocal<FileObject> FALLBACK = new ThreadLocal<>();
    private static final Map<FileObject, Reference<Project>> EXISTING = new HashMap<>();

    public FallbackFactory() {
    }

    public static Project findProjectOrFallback(FileObject dir) throws IOException {
        ProjectManager.getDefault().clearNonProjectCache();
        FileObject prev = FALLBACK.get();
        try {
            FALLBACK.set(dir);
            Project prj = ProjectManager.getDefault().findProject(dir);
            EXISTING.put(dir, new WeakReference<>(prj));
            return prj;
        } finally {
            FALLBACK.set(prev);
        }

    }

    @Override
    public boolean isProject(FileObject dir) {
        if (FALLBACK.get() == dir) {
            return true;
        }
        Reference<Project> ref = EXISTING.get(dir);
        return ref != null && ref.get() != null;
    }

    @Override
    public Project loadProject(FileObject projectDirectory, ProjectState state) throws IOException {
        if (isProject(projectDirectory)) {
            return new FallbackProject(projectDirectory, state);
        } else {
            return null;
        }
    }

    @Override
    public void saveProject(Project project) throws IOException, ClassCastException {
    }
}
