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

package org.netbeans.modules.gradle.javaee;

import org.netbeans.modules.gradle.api.GradleBaseProject;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 *
 * @author Laszlo Kishalmi
 */
public abstract class BaseEEModuleImpl implements J2eeModuleImplementation2, ModuleChangeReporter {

    protected final Project project;
    protected final BaseEEModuleProvider provider;
    protected final String ddName;
    protected final String ddPath;

    public BaseEEModuleImpl(Project project, BaseEEModuleProvider provider, String ddName, String ddPath) {
        this.project = project;
        this.provider = provider;
        this.ddName = ddName;
        this.ddPath = ddPath;
    }

    @Override
    public String getUrl() {
        GradleBaseProject gbp = GradleBaseProject.get(project);
        return "/" + gbp.getName();
    }

    @Override
    public Iterator<J2eeModule.RootedEntry> getArchiveContents() throws IOException {
        FileObject fo = getContentDirectory();
        if (fo != null) {
            return new ContentIterator(fo);
        }
        return null;
    }
    

    @Override
    public File getResourceDirectory() {
        GradleBaseProject gbp = GradleBaseProject.get(project);
        return gbp != null ? new File(gbp.getProjectDir(), "src/main/setup") : null;
    }

    @Override
    public File getDeploymentConfigurationFile(String name) {
        if (name == null) {
            return null;
        }
        if (ddName.equals(name)) {
            name = ddPath;
        } else {
            String path = provider.getConfigSupport().getContentRelativePath(name);
            if (path != null) {
                name = path;
            }
        }
        return getDDFile(name);
    }

    protected File getDDFile(String path) {
        GradleJavaProject gjp = GradleJavaProject.get(project);
        GradleJavaSourceSet main = gjp.getMainSourceSet();
        File dir = null;
        if (main != null && !main.getResourcesDirs().isEmpty()) {
            dir = main.getResourcesDirs().iterator().next();
        }
        return dir != null ? FileUtil.normalizeFile(new File(dir, path)) : null;
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject metaInf = getMetaInf();
        if (metaInf != null) {
            return metaInf.getFileObject(ddName); //NOI18N
        }
        return null;
    }
    
    public FileObject getMetaInf() {
        GradleJavaProject gjp = GradleJavaProject.get(project);
        if (gjp != null) {
            Set<File> resourceDirs = gjp.getMainSourceSet().getResourcesDirs();
            for (File resourceDir : resourceDirs) {
                FileObject fo = FileUtil.toFileObject(resourceDir);
                if (fo != null) {
                    FileObject metaInf = fo.getFileObject("META-INF"); //NOI18N
                    if (metaInf != null) {
                        return  metaInf;
                    }
                }
            }
        }
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    @Override
    public EjbChangeDescriptor getEjbChanges(long timestamp) {
        return DummyModuleChangeReporter.DUMMY_REPORTER.getEjbChanges(timestamp);
    }

    @Override
    public boolean isManifestChanged(long timestamp) {
        return DummyModuleChangeReporter.DUMMY_REPORTER.isManifestChanged(timestamp);
    }

    private static final class ContentIterator implements Iterator<J2eeModule.RootedEntry> {
        private ArrayList<FileObject> ch;
        private FileObject root;
        
        private ContentIterator(FileObject f) {
            this.ch = new ArrayList<FileObject>();
            ch.add(f);
            this.root = f;
        }
        
        @Override
        public boolean hasNext() {
            return ! ch.isEmpty();
        }
        
        @Override
        public J2eeModule.RootedEntry next() {
            FileObject f = ch.get(0);
            ch.remove(0);
            if (f.isFolder()) {
                f.refresh();
                for (FileObject fo : f.getChildren()) {
                    ch.add(fo);
                }
            }
            return new FSRootRE(root, f);
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
    
    private static final class FSRootRE implements J2eeModule.RootedEntry {
        private FileObject f;
        private FileObject root;
        
        FSRootRE(FileObject rt, FileObject fo) {
            f = fo;
            root = rt;
        }
        
        @Override
        public FileObject getFileObject() {
            return f;
        }
        
        @Override
        public String getRelativePath() {
            return FileUtil.getRelativePath(root, f);
        }
    }

}
