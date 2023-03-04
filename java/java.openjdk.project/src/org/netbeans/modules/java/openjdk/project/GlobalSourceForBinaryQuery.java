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
package org.netbeans.modules.java.openjdk.project;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.openjdk.project.ModuleDescription.ModuleRepository;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.util.BaseUtilities;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author lahvac
 */
@ServiceProvider(service=SourceForBinaryQueryImplementation2.class, position=85)
public class GlobalSourceForBinaryQuery implements SourceForBinaryQueryImplementation2 {

    private static final Reference<Project> NO_PROJECT_CACHE = new WeakReference<Project>(null);

    private final Map<URL, Reference<Project>> projectCache = new HashMap<>();

    @Override
    public Result findSourceRoots2(URL binaryRoot) {
        Reference<Project> cachedProject = projectCache.get(binaryRoot);

        if (cachedProject == NO_PROJECT_CACHE) {
            return null;
        }

        Project prj = cachedProject != null ? cachedProject.get() : null;

        if (prj == null) {
            try {
                URI jdkRootCandidate = BaseUtilities.normalizeURI(binaryRoot.toURI().resolve("../../../../../"));

                if (jdkRootCandidate != null) {
                    ModuleRepository repository = ModuleDescription.getModuleRepository(jdkRootCandidate);

                    if (repository != null) {
                        String path = binaryRoot.getPath();
                        int lastSlash = path.lastIndexOf('/', path.length() - 2);
                        if (lastSlash >= 0) {
                            String moduleName = path.substring(lastSlash + 1, path.length() - 1);

                            FileObject root = repository.findModuleRoot(moduleName);

                            if (root != null) {
                                prj = FileOwnerQuery.getOwner(root);
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        if (prj == null) {
            projectCache.put(binaryRoot, NO_PROJECT_CACHE);

            return null;
        } else {
            projectCache.put(binaryRoot, new WeakReference<>(prj));

            return new ResultImpl(prj);
        }
    }

    @Override
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        return findSourceRoots2(binaryRoot);
    }

    private static final class ResultImpl implements Result, ChangeListener {

        private final ChangeSupport cs = new ChangeSupport(this);
        private final Sources sources;

        public ResultImpl(Project prj) {
            sources = ProjectUtils.getSources(prj);
            sources.addChangeListener(WeakListeners.change(this, sources));
        }

        @Override
        public boolean preferSources() {
            return false;
        }

        @Override
        public FileObject[] getRoots() {
            List<FileObject> roots = new ArrayList<>();

            for (SourceGroup sg : sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                roots.add(sg.getRootFolder());
            }

            return roots.toArray(new FileObject[0]);
        }

        @Override
        public void addChangeListener(ChangeListener l) {
            cs.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(ChangeListener l) {
            cs.removeChangeListener(l);
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            cs.fireChange();
        }

    }

}
