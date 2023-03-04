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

package org.netbeans.modules.gradle.javaee.web;

import org.netbeans.modules.gradle.api.NbGradleProject;
import org.netbeans.modules.gradle.java.api.GradleJavaProject;
import org.netbeans.modules.gradle.java.api.GradleJavaSourceSet;
import org.netbeans.modules.gradle.java.api.ProjectSourcesClassPathProvider;
import org.netbeans.modules.gradle.javaee.BaseEEModuleImpl;
import org.netbeans.modules.gradle.javaee.api.GradleWebProject;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.web.WebAppMetadataModelFactory;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.dd.spi.webservices.WebservicesMetadataModelFactory;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.util.Exceptions;

/**
 *
 * @author Laszlo Kishalmi
 */
public class WebModuleImpl extends BaseEEModuleImpl implements WebModuleImplementation2 {


    private static final String WEB_INF = "WEB-INF";

    final PropertyChangeListener pcl = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (NbGradleProject.PROP_PROJECT_INFO.equals(evt.getPropertyName())) {
                updateProperties();
            }
        }
    };

    final PropertyChangeSupport cs = new PropertyChangeSupport(this);

    Profile profile;
    FileObject deploymentDescriptor;
    MetadataModel<WebAppMetadata> webAppMetadataModel;
    MetadataModel<WebservicesMetadata> webservicesMetadataModel;
    MetadataModel<WebAppMetadata> webAppAnnMetadataModel;

    public WebModuleImpl(Project project, WebModuleProviderImpl provider) {
        super(project, provider, "web.xml", J2eeModule.WEB_XML); //NOI18N
    }

    @Override
    public FileObject getDocumentBase() {
        GradleWebProject wp = GradleWebProject.get(project);
        return wp != null ? FileUtil.toFileObject(wp.getWebAppDir()) : null;
    }

    @Override
    public FileObject getContentDirectory() throws IOException {
        GradleWebProject wp = GradleWebProject.get(project);
        FileObject fo = wp != null ? FileUtil.toFileObject(wp.getExplodedWarDir()) : null;
        return fo;
    }

    @Override
    public String getContextPath() {
        if(getDeploymentDescriptor() != null || (getJ2eeProfile() != null && getJ2eeProfile().isAtLeast(Profile.JAVA_EE_6_WEB))) {
            try {
                String path = provider.getConfigSupport().getWebContextRoot();
                if (path != null) {
                    return path;
                }
            } catch (ConfigurationException e) {
                // TODO #95280: inform the user that the context root cannot be retrieved
            }
        }
        return null;
    }

    public void setContextPath(String path) {
        if(getDeploymentDescriptor() != null || (getJ2eeProfile() != null && getJ2eeProfile().isAtLeast(Profile.JAVA_EE_6_WEB))) {
            try {
                provider.getConfigSupport().setWebContextRoot(path);
            } catch (ConfigurationException e) {
                // TODO #95280: inform the user that the context root cannot be retrieved
            }
        }
    }

    @Override
    public Profile getJ2eeProfile() {
        return JavaEEProjectSettings.getProfile(project);
    }

    @Override
    public FileObject getWebInf() {
        FileObject root = getDocumentBase();
        return root != null ? root.getFileObject(WEB_INF) : null;
    }

    /**
     * Creates new WEB-INF folder in the web root.
     *
     * @return {@code FileObject} of the WEB-INF folder or {@code null} in cases of
     * missing document base directory
     * @throws IOException if the folder failed to be created
     */
    public FileObject createWebInf() throws IOException {
        FileObject root = getDocumentBase();
        if (root != null) {
            return root.createFolder(WEB_INF);
        }
        return null;
    }

    @Override
    public FileObject getDeploymentDescriptor() {
        return deploymentDescriptor;
    }

    @Override
    public FileObject[] getJavaSources() {
        GradleJavaProject javaProject = GradleJavaProject.get(project);
        GradleJavaSourceSet mainSources = javaProject.getMainSourceSet();
        FileObject[] ret = new FileObject[0];
        if (mainSources != null) {
            Collection<File> availableDirs = mainSources.getAvailableDirs();
            ret = new FileObject[availableDirs.size()];
            int i = 0;
            for (File availableDir : availableDirs) {
                ret[i++] = FileUtil.toFileObject(availableDir);
            }
        }
        return ret;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!cs.hasListeners(null)) {
            NbGradleProject.addPropertyChangeListener(project, pcl);
            updateProperties();
        }
        cs.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        cs.removePropertyChangeListener(listener);
        if (!cs.hasListeners(null)) {
            NbGradleProject.removePropertyChangeListener(project, pcl);
        }
    }

    private void updateProperties() {
        profile = null;
        GradleWebProject wp = GradleWebProject.get(project);
        if (wp != null) {
            File webXml = wp.getWebXml();
            if (webXml != null) {
                deploymentDescriptor = FileUtil.toFileObject(webXml);
            }
        }
        cs.firePropertyChange(null, null, null);
    }

    @Override
    public J2eeModule.Type getModuleType() {
        return J2eeModule.Type.WAR;
    }

    @Override
    public String getModuleVersion() {
        WebApp wapp = getWebApp();
        String version = null;
        if (wapp != null) {
            version = wapp.getVersion();
        }
        if (version == null) {
            version = WebApp.VERSION_3_1;
        }
        return version;
    }

    @Override
    public FileObject getArchive() throws IOException {
        GradleWebProject gwp = GradleWebProject.get(project);
        return gwp != null ? FileUtil.toFileObject(gwp.getMainWar()) : null;
    }

    private WebApp getWebApp() {
        try {
            FileObject dd = getDeploymentDescriptor();
            if (dd != null) {
                return DDProvider.getDefault().getDDRoot(dd);
            }
        } catch (java.io.IOException e) {
            ErrorManager.getDefault().log(e.getLocalizedMessage());
        }
        return null;
    }
    @Override
    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == WebAppMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getAnnotationMetadataModel();
            return model;
        } else if (type == WebservicesMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getWebservicesMetadataModel();
            return model;
        }
        return null;
    }

    @Override
    public synchronized MetadataModel<WebAppMetadata> getMetadataModel() {
        if (webAppMetadataModel == null) {
            final FileObject ddFO = getDeploymentDescriptor();
            final FileObject webInf = getOrCreateWebInf();

            if (ddFO == null && webInf != null) {
                webInf.addFileChangeListener(new FileChangeAdapter() {
                    @Override
                    public void fileDataCreated(FileEvent fe) {
                        if ("web.xml".equals(fe.getFile().getNameExt())) { // NOI18N
                            webInf.removeFileChangeListener(this);
                            resetMetadataModel();
                        }
                    }
                });
            }

            GradleWebProject wp = GradleWebProject.get(project);
            ProjectSourcesClassPathProvider gcp = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);

            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            MetadataUnit mu = MetadataUnit.create(
                    gcp.getProjectSourcesClassPath(ClassPath.BOOT),
                    gcp.getProjectSourcesClassPath(ClassPath.COMPILE),
                    gcp.getProjectSourcesClassPath(ClassPath.SOURCE),
                    ddFile);
            webAppMetadataModel = WebAppMetadataModelFactory.createMetadataModel(mu, true);
        }
        return webAppMetadataModel;
    }

    private FileObject getOrCreateWebInf() {
        FileObject webInf = getWebInf();
        if (webInf == null) {
            try {
                webInf = createWebInf();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return webInf;
    }

    private synchronized void resetMetadataModel() {
        webAppMetadataModel = null;
    }

    private synchronized MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
        if (webservicesMetadataModel == null) {
            FileObject ddFO = getWebServicesDeploymentDescriptor();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            ProjectSourcesClassPathProvider cpProvider = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                // XXX: add listening on deplymentDescriptor
                ddFile);
            webservicesMetadataModel = WebservicesMetadataModelFactory.createMetadataModel(metadataUnit);
        }
        return webservicesMetadataModel;
    }

    private FileObject getWebServicesDeploymentDescriptor() {
        FileObject root = getDocumentBase();
        if (root != null) {
            return root.getFileObject(J2eeModule.WEBSERVICES_XML);
        }
        return null;
    }

    @Override
    protected File getDDFile(String path) {
        GradleWebProject gwp = GradleWebProject.get(project);
        return FileUtil.normalizeFile(new File(gwp.getWebAppDir(), path));
    }

    /**
     * The server plugin needs all models to be either merged on annotation-based.
     * Currently only the web model does a bit of merging, other models don't. So
     * for web we actually need two models (one for the server plugins and another
     * for everyone else). Temporary solution until merging is implemented
     * in all models.
     */
    public synchronized MetadataModel<WebAppMetadata> getAnnotationMetadataModel() {
        if (webAppAnnMetadataModel == null) {
            FileObject ddFO = getDeploymentDescriptor();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            ProjectSourcesClassPathProvider cpProvider = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);

            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                // XXX: add listening on deplymentDescriptor
                ddFile);
            webAppAnnMetadataModel = WebAppMetadataModelFactory.createMetadataModel(metadataUnit, false);
        }
        return webAppAnnMetadataModel;
    }

}
