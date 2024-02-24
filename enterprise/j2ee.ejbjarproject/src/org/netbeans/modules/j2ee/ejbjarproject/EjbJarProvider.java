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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.swing.SwingUtilities;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJarMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.j2ee.ejbjarproject.ui.customizer.EjbJarProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileUtil;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.modules.j2ee.dd.spi.ejb.EjbJarMetadataModelFactory;
import org.netbeans.modules.j2ee.dd.spi.webservices.WebservicesMetadataModelFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ResourceChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ResourceChangeReporterFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ResourceChangeReporterImplementation;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;


/** A ejb module implementation on top of project.
 *
 * @author  Pavel Buzek
 */
public final class EjbJarProvider extends J2eeModuleProvider
        implements J2eeModuleImplementation2, ModuleChangeReporter, EjbChangeDescriptor, PropertyChangeListener {
    
    public static final String FILE_DD = "ejb-jar.xml";//NOI18N

    private final ResourceChangeReporter rcr = ResourceChangeReporterFactory.createResourceChangeReporter(new EjbJarResourceChangeReporter());

    private final EjbJarProject project;
    private final AntProjectHelper helper;
    private MetadataModel<EjbJarMetadata> ejbJarMetadataModel;
    private MetadataModel<WebservicesMetadata> webservicesMetadataModel;
    
    private PropertyChangeSupport propertyChangeSupport;
    private J2eeModule j2eeModule;
    private ClassPathProviderImpl cpProvider;
    
    private long notificationTimeout = 0; // used to suppress repeating the same messages
    
    EjbJarProvider(EjbJarProject project, AntProjectHelper helper, ClassPathProviderImpl cpProvider) {
        this.project = project;
        this.helper = helper;
        this.cpProvider = cpProvider;
        //project.evaluator().addPropertyChangeListener(this);
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject ddFO = null;
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo != null) {
            ddFO = metaInfFo.getFileObject(FILE_DD);
        }
        if (ddFO == null && !Profile.JAVA_EE_5.equals(getJ2eeProfile())) {
            // ...generate the DD from template...
        }
        return ddFO;
    }
    
    /** @deprecated use getJavaSources */
    @Deprecated
    public ClassPath getClassPath() {
        ClassPathProvider cpp = project.getClassPathProvider();
        if (cpp != null) {
            return cpp.findClassPath(getFileObject(EjbJarProjectProperties.SRC_DIR), ClassPath.SOURCE);
        }
        return null;
    }

    public FileObject[] getJavaSources() {
        return project.getSourceRoots().getRoots();
    }
    
    public FileObject getMetaInf() {
        String value = helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.META_INF);

        return resolveMetaInf(value);
    }

    FileObject resolveMetaInf(String value) {
        FileObject metaInf = value != null ? helper.resolveFileObject(value) : null;
        if (metaInf == null) {
            Profile version = project.getAPIEjbJar().getJ2eeProfile();
            if (needConfigurationFolder(version)) {
                String path = (value != null ? helper.resolvePath(value) : "");
                showErrorMessage(NbBundle.getMessage(EjbJarProvider.class,"MSG_MetaInfCorrupted", project.getName(), path));
            }
        }
        return metaInf;
    }

    /** Package-private for unit test only. */
    static boolean needConfigurationFolder(final Profile version) {
        return Profile.J2EE_13.equals(version) ||
                Profile.J2EE_14.equals(version);
    }
    
    public File getMetaInfAsFile() {
        return getFile(EjbJarProjectProperties.META_INF);
    }

    @Override
    public File getResourceDirectory() {
        File f = getFile(EjbJarProjectProperties.RESOURCE_DIR);
        if (f == null) {
            f = new File(FileUtil.toFile(project.getProjectDirectory()), "setup"); // NOI18N
        }
        return f;
    }
    
    @Override
    public File getDeploymentConfigurationFile(String name) {
        String path = getConfigSupport().getContentRelativePath(name);
        if (path == null) {
            path = name;
        }
        if (path.startsWith("META-INF/")) { // NOI18N
            path = path.substring(8); // removing "META-INF/"
        }
        return FileUtil.normalizeFile(new File(getMetaInfAsFile(), path));
    }
    
    public ClassPathProvider getClassPathProvider() {
        return project.getClassPathProvider();
    }
    
    @Override
    public FileObject getArchive() {
        return getFileObject(EjbJarProjectProperties.DIST_JAR);
    }
    
    private FileObject getFileObject(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFileObject(prop);
        }
        
        return null;
    }
    
    private File getFile(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFile(prop);
        }
        return null;
    }
    
    @Override
    public synchronized J2eeModule getJ2eeModule () {
        if (j2eeModule == null) {
            j2eeModule = J2eeModuleFactory.createJ2eeModule(this);
        }
        return j2eeModule;
    }
    
    @Override
    public org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter getModuleChangeReporter() {
        return this;
    }

    @Override
    public ResourceChangeReporter getResourceChangeReporter() {
        return rcr;
    }

    @Override
    public DeployOnSaveSupport getDeployOnSaveSupport() {
        return project.getDeployOnSaveSupport();
    }
    
    @Override
    public boolean isOnlyCompileOnSaveEnabled() {
        return Boolean.parseBoolean(project.evaluator().getProperty(EjbJarProjectProperties.J2EE_COMPILE_ON_SAVE)) &&
            !Boolean.parseBoolean(project.evaluator().getProperty(EjbJarProjectProperties.J2EE_DEPLOY_ON_SAVE));
    }
    
    
    @Override
    public String getServerID() {
        return helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.J2EE_SERVER_TYPE);
    }
    
    @Override
    public String getServerInstanceID() {
        return helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.J2EE_SERVER_INSTANCE);
    }
    
    @Override
    public void setServerInstanceID(String serverInstanceID) {
        assert serverInstanceID != null : "passed serverInstanceID cannot be null"; // NOI18N
        EjbJarProjectProperties.setServerInstance(project, helper, serverInstanceID);
    }
    
    @Override
    public Iterator<J2eeModule.RootedEntry> getArchiveContents() throws java.io.IOException {
        FileObject content = getContentDirectory();
        content.refresh();
        return new IT(content);
    }
    
    @Override
    public FileObject getContentDirectory() {
        return getFileObject(ProjectProperties.BUILD_CLASSES_DIR);
    }
    
    public FileObject getBuildDirectory() {
        return getFileObject(EjbJarProjectProperties.BUILD_DIR);
    }
    
    public File getContentDirectoryAsFile() {
        return getFile(ProjectProperties.BUILD_CLASSES_DIR);
    }
    
    @Override
    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == EjbJarMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getMetadataModel();
            return model;
        } else if (type == WebservicesMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getWebservicesMetadataModel();
            return model;
        }
        return null;
    }
    
    public FileObject getDD() {
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo==null) {
            return null;
        }
        return metaInfFo.getFileObject(WebServicesConstants.WEBSERVICES_DD, "xml"); // NOI18N
    }
    
    @Override
    public org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor getEjbChanges(long timestamp) {
        return this;
    }
    
    @Override
    public J2eeModule.Type getModuleType() {
        return J2eeModule.Type.EJB;
    }
    
    @Override
    public String getModuleVersion() {
        // return a version based on the Java EE version
        Profile platformVersion = getJ2eeProfile();
        if (platformVersion == null) {
            platformVersion = Profile.JAVA_EE_7_FULL;
        }

        if (platformVersion.isAtLeast(Profile.JAKARTA_EE_9_WEB)) {
            return EjbJar.VERSION_4_0;
        } else if (platformVersion.isAtLeast(Profile.JAVA_EE_7_WEB)) {
            return EjbJar.VERSION_3_2;
        } else if (platformVersion.isAtLeast(Profile.JAVA_EE_6_WEB)) {
            return EjbJar.VERSION_3_1;
        } else if (Profile.JAVA_EE_5.equals(platformVersion)) {
            return EjbJar.VERSION_3_0;
        } else {
            return EjbJar.VERSION_2_1;
        }
    }
    
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(org.netbeans.modules.j2ee.dd.api.ejb.EjbJar.PROPERTY_VERSION)) {
            String oldVersion = (String) evt.getOldValue();
            String newVersion = (String) evt.getNewValue();
            getPropertyChangeSupport().firePropertyChange(J2eeModule.PROP_MODULE_VERSION, oldVersion, newVersion);
        } else if (evt.getPropertyName ().equals (EjbJarProjectProperties.J2EE_SERVER_INSTANCE)) {
            Deployment d = Deployment.getDefault();
            String oldServerID = evt.getOldValue () == null ? null : d.getServerID ((String) evt.getOldValue ());
            String newServerID = evt.getNewValue () == null ? null : d.getServerID ((String) evt.getNewValue ());
            fireServerChange (oldServerID, newServerID);
        }  else if (EjbJarProjectProperties.RESOURCE_DIR.equals(evt.getPropertyName())) {
            String oldValue = (String) evt.getOldValue();
            String newValue = (String) evt.getNewValue();
            getPropertyChangeSupport().firePropertyChange(
                    J2eeModule.PROP_RESOURCE_DIRECTORY, 
                    oldValue == null ? null : new File(oldValue),
                    newValue == null ? null : new File(newValue));
        }
    }
    
    @Override
    public String getUrl() {
        EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String name = ep.getProperty(EjbJarProjectProperties.JAR_NAME);
        return name == null ? "" : ('/' + name);
    }
    
    @Override
    public boolean isManifestChanged(long timestamp) {
        return false;
    }
    
    public void setUrl(String url) {
        throw new UnsupportedOperationException("Cannot customize URL of EJB module"); // NOI18N
    }
    
    @Override
    public boolean ejbsChanged() {
        return false;
    }
    
    @Override
    public String[] getChangedEjbs() {
        return new String[] {};
    }

    public Profile getJ2eeProfile() {
        return Profile.fromPropertiesString(helper.getStandardPropertyEvaluator().getProperty(EjbJarProjectProperties.J2EE_PLATFORM));
    }

    public synchronized MetadataModel<EjbJarMetadata> getMetadataModel() {
        if (ejbJarMetadataModel == null) {
            FileObject ddFO = getDeploymentDescriptor();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                // XXX: add listening on deplymentDescriptor
                ddFile);
            ejbJarMetadataModel = EjbJarMetadataModelFactory.createMetadataModel(metadataUnit);
        }
        return ejbJarMetadataModel;
    }
    
    public synchronized MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
        if (webservicesMetadataModel == null) {
            FileObject ddFO = getDD();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
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

    @Override
    public FileObject[] getSourceRoots() {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        List<FileObject> roots = new LinkedList<FileObject>();
        FileObject metaInf = getMetaInf();
        if (metaInf != null) {
            roots.add(metaInf);
        }
        
        for (int i = 0; i < groups.length; i++) {
            roots.add(groups[i].getRootFolder());
        }
        FileObject[] rootArray = new FileObject[roots.size()];
        return roots.toArray(rootArray);
    }
    
    private void showErrorMessage(final String message) {
        // only display the messages if the project is open
        if(new Date().getTime() > notificationTimeout && isProjectOpen()) {
            // DialogDisplayer waits for the AWT thread, blocking the calling
            // thread -- deadlock-prone, see issue #64888. therefore invoking
            // only in the AWT thread
            Runnable r = new Runnable() {
                @Override
                public void run() {
                    if (!SwingUtilities.isEventDispatchThread()) {
                        SwingUtilities.invokeLater(this);
                    } else {
                        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
                    }
                }
            };
            r.run();
            
            // set timeout to suppress the same messages during next 20 seconds (feel free to adjust the timeout
            // using more suitable value)
            notificationTimeout = new Date().getTime() + 20000;
        }
    }
    
    private boolean isProjectOpen() {
        // OpenProjects.getDefault() is null when this method is called upon
        // IDE startup from the project's impl of ProjectOpenHook
        if (OpenProjects.getDefault() != null) {
            Project[] projects = OpenProjects.getDefault().getOpenProjects();
            for (int i = 0; i < projects.length; i++) {
                if (projects[i].equals(project)) {
                    return true;
                }
            }
            return false;
        } else {
            // be conservative -- don't know anything about the project
            // so consider it open
            return true;
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        getPropertyChangeSupport().addPropertyChangeListener(listener);
    }

    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeSupport == null) {
            return;
        }
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    private PropertyChangeSupport getPropertyChangeSupport() {
        synchronized (this) {
            if (propertyChangeSupport == null) {
                propertyChangeSupport = new PropertyChangeSupport(this);
                // XXX need to listen on the module version
                // try {
                //     project.getAPIEjbJar().getMetadataModel().runReadAction(new MetadataModelAction<EjbJarMetadata, Void>() {
                //         public Void run(EjbJarMetadata metadata) throws MetadataModelException, IOException {
                //             EjbJar ejbJar = metadata.getRoot();
                //             PropertyChangeListener l = (PropertyChangeListener) WeakListeners.create(PropertyChangeListener.class, EjbJarProvider.this, ejbJar);
                //             ejbJar.addPropertyChangeListener(l);
                //             return null;
                //         }
                //     });
                // } catch (MetadataModelException e) {
                //     // TODO MetadataModel: handle the exception
                // } catch (IOException e) {
                //     // TODO MetadataModel: handle the exception
                // }
            }
            return propertyChangeSupport;
        }
    }

    @Override
    public File[] getRequiredLibraries() {
        ClassPath cp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    FileUtil.toFile(project.getProjectDirectory()), project.evaluator(), new String[]{"javac.classpath"}));
        List<File> files = new ArrayList<File>();
        for (FileObject fo : cp.getRoots()) {
            if (FileUtil.getArchiveFile(fo) == null) {
                continue;
            }
            files.add(FileUtil.toFile(FileUtil.getArchiveFile(fo)));
        }
        return files.toArray(new File[0]);
    }

    private class EjbJarResourceChangeReporter implements ResourceChangeReporterImplementation {

        @Override
        public boolean isServerResourceChanged(long lastDeploy) {
            File resDir = getResourceDirectory();
            if (resDir != null && resDir.exists() && resDir.isDirectory()) {
                File[] children = resDir.listFiles();
                if (children != null) {
                    for (File file : children) {
                        if (file.lastModified() > lastDeploy) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    private static class IT implements Iterator<J2eeModule.RootedEntry> {
        Enumeration<? extends FileObject> ch;
        FileObject root;
        
        private IT(FileObject f) {
            this.ch = f.getChildren(true);
            this.root = f;
        }
        
        @Override
        public boolean hasNext() {
            return ch.hasMoreElements();
        }
        
        @Override
        public J2eeModule.RootedEntry next() {
            FileObject f = (FileObject) ch.nextElement();
            return new FSRootRE(root, f);
        }
        
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
        
    }
    
    private static final class FSRootRE implements J2eeModule.RootedEntry {
        FileObject f;
        FileObject root;
        
        FSRootRE(FileObject root, FileObject f) {
            this.f = f;
            this.root = root;
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
