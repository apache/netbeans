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

import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.swing.Icon;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import static org.netbeans.api.project.Sources.TYPE_GENERIC;
import org.netbeans.modules.java.openjdk.project.JDKProject.Root;
import org.netbeans.modules.java.openjdk.project.JDKProject.RootKind;
import org.netbeans.spi.project.support.GenericSources;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;

/**
 *
 * @author lahvac
 */
public class SourcesImpl implements Sources, FileChangeListener, ChangeListener {

    public static final String SOURCES_TYPE_JDK_PROJECT = "jdk-project-sources";
    public static final String SOURCES_TYPE_JDK_PROJECT_TESTS = "jdk-project-sources-tests";
    public static final String SOURCES_TYPE_JDK_PROJECT_NATIVE = "jdk-project-sources-native";

    private final ChangeSupport cs = new ChangeSupport(this);
    private final JDKProject project;
    private final Map<Root, SourceGroup> root2SourceGroup = new HashMap<Root, SourceGroup>();

    public SourcesImpl(JDKProject project) {
        this.project = project;
        
        for (Root r : project.getRoots()) {
            r.addChangeListener(this);
        }
    }

    private boolean initialized;
    private final Map<String, List<SourceGroup>> key2SourceGroups = new HashMap<>();
    
    @Override
    public synchronized SourceGroup[] getSourceGroups(String type) {
        if (!initialized) {
            recompute();
            initialized = true;
        }
        
        List<SourceGroup> groups = key2SourceGroups.get(type);
        if (groups != null)
            return groups.toArray(new SourceGroup[0]);

        return new SourceGroup[0];
    }

    private final Set<File> seen = new HashSet<>();
    
    private synchronized void recompute() {
        key2SourceGroups.clear();

        for (SourceGroup sg : GenericSources.genericOnly(project).getSourceGroups(TYPE_GENERIC)) {
            addSourceGroup(TYPE_GENERIC, sg);
        }

        Set<File> newFiles = new HashSet<>();
        for (Root root : project.getRoots()) {
            URL srcURL = root.getLocation();

            if ("file".equals(srcURL.getProtocol())) {
                try {
                    newFiles.add(Utilities.toFile(srcURL.toURI()));
                } catch (URISyntaxException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            FileObject src = URLMapper.findFileObject(srcURL);
            if (src == null) {
                root2SourceGroup.remove(root);
            } else {
                SourceGroup sg = root2SourceGroup.get(root);

                if (sg == null) {
                    sg = new SourceGroupImpl(GenericSources.group(project, src, root.displayName, root.displayName, null, null), root.includes, root.excludes);

                    root2SourceGroup.put(root, sg);
                }

                if (root.kind == RootKind.NATIVE_SOURCES) {
                    addSourceGroup(SOURCES_TYPE_JDK_PROJECT_NATIVE, sg);
                } else {
                    addSourceGroup(JavaProjectConstants.SOURCES_TYPE_JAVA, sg);
                }

                if (root.kind == RootKind.TEST_SOURCES) {
                    addSourceGroup(SOURCES_TYPE_JDK_PROJECT_TESTS, sg);
                }

                addSourceGroup(SOURCES_TYPE_JDK_PROJECT, sg);

                if (!FileUtil.isParentOf(project.getProjectDirectory(), src)) {
                    addSourceGroup(TYPE_GENERIC, GenericSources.group(project, src, root.displayName, root.displayName, null, null));
                }
            }
        }
        Set<File> added = new HashSet<>(newFiles);
        added.removeAll(seen);
        Set<File> removed = new HashSet<>(seen);
        removed.removeAll(newFiles);
        for (File a : added) {
            FileUtil.addFileChangeListener(this, a);
            seen.add(a);
            FileOwnerQuery.markExternalOwner(Utilities.toURI(a), null, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
            FileOwnerQuery.markExternalOwner(Utilities.toURI(a), project, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        }
        for (File r : removed) {
            FileUtil.removeFileChangeListener(this, r);
            seen.remove(r);
            FileOwnerQuery.markExternalOwner(Utilities.toURI(r), null, FileOwnerQuery.EXTERNAL_ALGORITHM_TRANSIENT);
        }
        cs.fireChange();
    }

    private void addSourceGroup(String type, SourceGroup sg) {
        List<SourceGroup> groups = key2SourceGroups.get(type);

        if (groups == null) {
            key2SourceGroups.put(type, groups = new ArrayList<>());
        }

        groups.add(sg);
    }

    @Override public void addChangeListener(ChangeListener listener) {
        cs.addChangeListener(listener);
    }

    @Override public void removeChangeListener(ChangeListener listener) {
        cs.removeChangeListener(listener);
    }

    @Override
    public void fileFolderCreated(FileEvent fe) {
        recompute();
    }

    @Override
    public void fileDataCreated(FileEvent fe) { }

    @Override
    public void fileChanged(FileEvent fe) { }

    @Override
    public void fileDeleted(FileEvent fe) {
        recompute();
    }

    @Override
    public void fileRenamed(FileRenameEvent fe) {
        recompute();
    }

    @Override
    public void fileAttributeChanged(FileAttributeEvent fe) { }

    @Override
    public void stateChanged(ChangeEvent e) {
        recompute();
    }

    private static final class SourceGroupImpl implements SourceGroup {

        //XXX: listeners
        private final SourceGroup delegate;
        private final Pattern includes;
        private final Pattern excludes;

        public SourceGroupImpl(SourceGroup delegate, Pattern includes, Pattern excludes) {
            this.delegate = delegate;
            this.includes = includes;
            this.excludes = excludes;
        }

        @Override
        public FileObject getRootFolder() {
            return delegate.getRootFolder();
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public String getDisplayName() {
            return delegate.getDisplayName();
        }

        @Override
        public Icon getIcon(boolean opened) {
            return delegate.getIcon(opened);
        }

        @Override
        public boolean contains(FileObject file) {
            if (delegate.contains(file)) {
                if (includes == null && excludes == null) return true;

                String rel = FileUtil.getRelativePath(delegate.getRootFolder(), file);

                if (includes != null && !includes.matcher(rel).matches()) {
                    return false;
                }

                if (excludes == null) return true;

                return !excludes.matcher(rel).matches();
            } else {
                return false;
            }
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }
}
