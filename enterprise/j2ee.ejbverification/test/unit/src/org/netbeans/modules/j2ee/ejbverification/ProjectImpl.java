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
package org.netbeans.modules.j2ee.ejbverification;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.Icon;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.api.ejbjar.EnterpriseReferenceContainer;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarsInProject;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Martin Adamek
 */
public final class ProjectImpl implements Project {

    private final Lookup lookup;
    private FileObject projectDirectory;

    public ProjectImpl(String moduleVersion, J2eeModule.Type type, EnterpriseReferenceContainer erContainer) {
        lookup = Lookups.fixed(
                new ClassPathProviderImpl(),
                new J2eeModuleProviderImpl(moduleVersion, type),
                new SourcesImpl(),
                erContainer,
                new EjbJarsInProjectImpl());
    }

    public FileObject getProjectDirectory() {
        return projectDirectory;
    }

    public Lookup getLookup() {
        return lookup;
    }

    public void setProjectDirectory(FileObject fileObject) {
        this.projectDirectory = fileObject;
    }

    private class SourcesImpl implements Sources {

        public SourcesImpl() {
        }

        public SourceGroup[] getSourceGroups(String type) {
            return new SourceGroup[]{new SourceGroupImpl()};
        }

        public void addChangeListener(ChangeListener listener) {
        }

        public void removeChangeListener(ChangeListener listener) {
        }
    }

    private class SourceGroupImpl implements SourceGroup {

        public SourceGroupImpl() {
        }

        public FileObject getRootFolder() {
            return projectDirectory.getFileObject("src").getFileObject("java");
        }

        public String getName() {
            return "Sources";
        }

        public String getDisplayName() {
            return "Sources";
        }

        public Icon getIcon(boolean opened) {
            return null;
        }

        public boolean contains(FileObject file) {
            return FileUtil.isParentOf(projectDirectory, file);
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }
    }

    private static class J2eeModuleProviderImpl extends J2eeModuleProvider {

        private final String moduleVersion;
        private final J2eeModule.Type type;

        public J2eeModuleProviderImpl(String moduleVersion, J2eeModule.Type type) {
            this.moduleVersion = moduleVersion;
            this.type = type;
        }

        public J2eeModule getJ2eeModule() {
            J2eeModuleImplementation2 j2eeModuleImpl = new J2eeModuleImpl(moduleVersion, type);
            return J2eeModuleFactory.createJ2eeModule(j2eeModuleImpl);
        }

        public ModuleChangeReporter getModuleChangeReporter() {
            return null;
        }

        public File getDeploymentConfigurationFile(String name) {
            return null;
        }

        public FileObject findDeploymentConfigurationFile(String name) {
            return null;
        }

        public void setServerInstanceID(String severInstanceID) {
        }

        public String getServerInstanceID() {
            return null;
        }

        public String getServerID() {
            return null;
        }
    }

    private static class J2eeModuleImpl implements J2eeModuleImplementation2 {

        private final String moduleVersion;
        private final J2eeModule.Type type;

        public J2eeModuleImpl(String moduleVersion, J2eeModule.Type type) {
            this.moduleVersion = moduleVersion;
            this.type = type;
        }

        public String getModuleVersion() {
            return moduleVersion;
        }

        public J2eeModule.Type getModuleType() {
            return type;
        }

        public String getUrl() {
            return null;
        }

        public void setUrl(String url) {
        }

        public FileObject getArchive() throws IOException {
            return null;
        }

        public Iterator getArchiveContents() throws IOException {
            return null;
        }

        public FileObject getContentDirectory() throws IOException {
            return null;
        }

        public File getResourceDirectory() {
            return null;
        }

        public File getDeploymentConfigurationFile(String name) {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
        }

        public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
            return null;
        }
    }

    private class EjbJarsInProjectImpl implements EjbJarsInProject {

        public EjbJarsInProjectImpl() {
        }

        public EjbJar[] getEjbJars() {
            return new EjbJar[]{EjbJar.getEjbJar(projectDirectory)};
        }
    }

    public final class ClassPathProviderImpl implements ClassPathProvider {

        @Override
        public ClassPath findClassPath(FileObject file, String type) {
            if (ClassPath.SOURCE.equals(type)) {
                return ClassPathSupport.createClassPath(projectDirectory.getFileObject("src").getFileObject("java"));
            } else if (ClassPath.COMPILE.equals(type)) {
                return ClassPathSupport.createClassPath(projectDirectory.getFileObject("src").getFileObject("java"));
            } else if (ClassPath.BOOT.equals(type)) {
                return JavaPlatformManager.getDefault().getDefaultPlatform().getBootstrapLibraries();
            }
            return null;
        }
    }
}
