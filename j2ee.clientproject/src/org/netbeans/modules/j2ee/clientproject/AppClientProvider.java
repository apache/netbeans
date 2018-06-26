/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.j2ee.clientproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;
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
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AppClientProjectProperties;
import org.netbeans.modules.java.api.common.classpath.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.client.AppClientMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.client.AppClientMetadataModelFactory;
import org.netbeans.modules.j2ee.dd.spi.webservices.WebservicesMetadataModelFactory;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ResourceChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ResourceChangeReporterFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ResourceChangeReporterImplementation;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.websvc.api.client.WebServicesClientConstants;
import org.netbeans.spi.java.classpath.ClassPathFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.project.classpath.support.ProjectClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * @author jungi
 */
public final class AppClientProvider extends J2eeModuleProvider
        implements J2eeModuleImplementation2, ModuleChangeReporter, EjbChangeDescriptor, PropertyChangeListener {
    
    public static final String FILE_DD = "application-client.xml";//NOI18N

    // tests only
    static boolean showMetaInfDialog = true;

    private final ResourceChangeReporter rcr = ResourceChangeReporterFactory.createResourceChangeReporter(new AppClientResourceChangeReporter());

    private final AppClientProject project;
    private final AntProjectHelper helper;
    private final ClassPathProviderImpl cpProvider;
    
    private MetadataModel<AppClientMetadata> appClientMetadataModel;
    private MetadataModel<WebservicesMetadata> webservicesMetadataModel;
    
    private PropertyChangeSupport propertyChangeSupport;
    private J2eeModule j2eeModule;
    
    private long notificationTimeout = 0; // used to suppress repeating the same messages
    
    AppClientProvider(AppClientProject project, AntProjectHelper helper, ClassPathProviderImpl cpProvider) {
        this.project = project;
        this.helper = helper;
        this.cpProvider = cpProvider;
        //project.evaluator().addPropertyChangeListener(this);
    }
    
    public FileObject getDeploymentDescriptor() {
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo == null) {
            return null;
        }
        return metaInfFo.getFileObject(FILE_DD);
    }
    
    public FileObject[] getJavaSources() {
        return project.getSourceRoots().getRoots();
    }
    
    public FileObject getMetaInf() {
        FileObject metaInf = getFileObject(AppClientProjectProperties.META_INF);
        if (metaInf == null) {
            Profile version = project.getAPICar().getJ2eeProfile();
            if (showMetaInfDialog && needConfigurationFolder(version)) {
                String relativePath = helper.getStandardPropertyEvaluator().getProperty(AppClientProjectProperties.META_INF);
                String path = (relativePath != null ? helper.resolvePath(relativePath) : "");
                showErrorMessage(NbBundle.getMessage(AppClientProvider.class, "MSG_MetaInfCorrupted", project.getName(), path));
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
        return getFile(AppClientProjectProperties.META_INF);
    }
    
    public File getResourceDirectory() {
        return getFile(AppClientProjectProperties.RESOURCE_DIR);
    }
    
    public File getDeploymentConfigurationFile(String name) {
        String path = getConfigSupport().getContentRelativePath(name);
        if (path == null) {
            path = name;
        }
        if (path.startsWith("META-INF/")) { // NOI18N
            path = path.substring(8); // removing "META-INF/"
        }
        return new File(getMetaInfAsFile(), path);
    }
    
    public ClassPathProvider getClassPathProvider() {
        return project.getClassPathProvider();
    }
    
    @Override
    public File[] getRequiredLibraries() {
        ClassPath cp = ClassPathFactory.createClassPath(
                    ProjectClassPathSupport.createPropertyBasedClassPathImplementation(
                    FileUtil.toFile(project.getProjectDirectory()), project.evaluator(), new String[]{"javac.classpath"}));
        List<File> files = new ArrayList<File>();
        for (FileObject fo : cp.getRoots()) {
            fo = FileUtil.getArchiveFile(fo);
            if (fo == null) {
                continue;
            }
            files.add(FileUtil.toFile(fo));
        }
        return files.toArray(new File[files.size()]);
    }

    public FileObject getArchive() {
        return getFileObject(AppClientProjectProperties.DIST_JAR);
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
    
    public synchronized J2eeModule getJ2eeModule () {
        if (j2eeModule == null) {
            j2eeModule = J2eeModuleFactory.createJ2eeModule(this);
        }
        return j2eeModule;
    }
    
    public ModuleChangeReporter getModuleChangeReporter() {
        return this;
    }

    @Override
    public ResourceChangeReporter getResourceChangeReporter() {
        return rcr;
    }

    @Override
    public String getServerID() {
        return helper.getStandardPropertyEvaluator().getProperty(AppClientProjectProperties.J2EE_SERVER_TYPE);
    }
    
    @Override
    public String getServerInstanceID() {
        return helper.getStandardPropertyEvaluator().getProperty(AppClientProjectProperties.J2EE_SERVER_INSTANCE);
    }
    
    public void setServerInstanceID(String serverInstanceID) {
        assert serverInstanceID != null : "passed serverInstanceID cannot be null";
        AppClientProjectProperties.setServerInstance(project, helper, serverInstanceID);
    }
    
    public Iterator<J2eeModule.RootedEntry> getArchiveContents() throws IOException {
        return new IT(getContentDirectory());
    }
    
    public FileObject getContentDirectory() {
        return getFileObject(ProjectProperties.BUILD_CLASSES_DIR);
    }
    
    public FileObject getBuildDirectory() {
        return getFileObject(AppClientProjectProperties.BUILD_DIR);
    }
    
    public File getContentDirectoryAsFile() {
        return getFile(ProjectProperties.BUILD_CLASSES_DIR);
    }
    
   // TODO MetadataModel: remove when transition to AppClientMetadata is finished
//    public RootInterface getDeploymentDescriptor(String location) {
//        if (J2eeModule.CLIENT_XML.equals(location)){
//            return Utils.getAppClient(project);
//        }
//        return null;
//    }
    
    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == AppClientMetadata.class) {
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
    
    private synchronized MetadataModel<AppClientMetadata> getMetadataModel() {
        if (appClientMetadataModel == null) {
            FileObject ddFO = getDeploymentDescriptor();
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                // XXX: add listening on deplymentDescriptor
                ddFile);
            appClientMetadataModel = AppClientMetadataModelFactory.createMetadataModel(metadataUnit);
        }
        return appClientMetadataModel;
    }
    
    private synchronized MetadataModel<WebservicesMetadata> getWebservicesMetadataModel() {
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
    
    /*
    private Webservices getWebservices() {
        if (Util.isJavaEE5orHigher(project)) {
            WebServicesSupport wss = WebServicesSupport.getWebServicesSupport(project.getProjectDirectory());
            try {
                return org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.getDefault().getMergedDDRoot(wss);
            } catch (IOException ex) {
                ErrorManager.getDefault().notify(ex);
            }
        } else {
            FileObject wsdd = getDD();
            if(wsdd != null) {
                try {
                    return org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.getDefault()
                            .getDDRoot(getDD());
                } catch (IOException e) {
                    ErrorManager.getDefault().log(e.getLocalizedMessage());
                }
            }
        }
        
        return null;
    }
    */
    public FileObject getDD() {
        FileObject metaInfFo = getMetaInf();
        if (metaInfFo==null) {
            return null;
        }
        return metaInfFo.getFileObject(WebServicesClientConstants.WEBSERVICES_DD, "xml"); // NOI18N
    }
    
    public EjbChangeDescriptor getEjbChanges(long timestamp) {
        return this;
    }
    
    public J2eeModule.Type getModuleType() {
        return J2eeModule.Type.CAR;
    }
    
    public String getModuleVersion() {
        Profile p = Profile.fromPropertiesString(project.evaluator().getProperty(AppClientProjectProperties.J2EE_PLATFORM));
        if (p == null) {
            p = Profile.JAVA_EE_6_FULL;
        }
        if (Profile.JAVA_EE_5.equals(p)) {
            return AppClient.VERSION_5_0;
        } else if (Profile.J2EE_14.equals(p)) {
            return AppClient.VERSION_1_4;
        } else {
            return AppClient.VERSION_6_0;
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(AppClient.PROPERTY_VERSION)) {
            String oldVersion = (String) evt.getOldValue();
            String newVersion = (String) evt.getNewValue();
            getPropertyChangeSupport().firePropertyChange(J2eeModule.PROP_MODULE_VERSION, oldVersion, newVersion);
        } else if (evt.getPropertyName().equals(AppClientProjectProperties.J2EE_SERVER_INSTANCE)) {
            Deployment d = Deployment.getDefault();
            String oldServerID = evt.getOldValue() == null ? null : d.getServerID((String) evt.getOldValue());
            String newServerID = evt.getNewValue() == null ? null : d.getServerID((String) evt.getNewValue());
            fireServerChange(oldServerID, newServerID);
        }  else if (AppClientProjectProperties.RESOURCE_DIR.equals(evt.getPropertyName())) {
            String oldValue = (String)evt.getOldValue();
            String newValue = (String)evt.getNewValue();
            getPropertyChangeSupport().firePropertyChange(
                    J2eeModule.PROP_RESOURCE_DIRECTORY, 
                    oldValue == null ? null : new File(oldValue),
                    newValue == null ? null : new File(newValue));
        }
    }
    
    public String getUrl() {
        EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        String name = ep.getProperty(AppClientProjectProperties.JAR_NAME);
        return name == null ? "" : ('/' + name);
    }
    
    public boolean isManifestChanged(long timestamp) {
        return false;
    }
    
    public void setUrl(String url) {
        throw new UnsupportedOperationException("Cannot customize URL of Application Client module"); // NOI18N
    }
    
    public boolean ejbsChanged() {
        return false;
    }
    
    public String[] getChangedEjbs() {
        return new String[] {};
    }
    
    public Profile getJ2eeProfile() {
        return Profile.fromPropertiesString(helper.getStandardPropertyEvaluator().getProperty(AppClientProjectProperties.J2EE_PLATFORM));
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

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // XXX need to listen on the module version
        getPropertyChangeSupport().addPropertyChangeListener(listener);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener listener) {
        if (propertyChangeSupport == null) {
            return;
        }
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    
    private synchronized PropertyChangeSupport getPropertyChangeSupport() {
        if (propertyChangeSupport == null) {
            propertyChangeSupport = new PropertyChangeSupport(this);
        }
        return propertyChangeSupport;
    }

    private class AppClientResourceChangeReporter implements ResourceChangeReporterImplementation {

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
        
        Enumeration ch;
        FileObject root;
        
        private IT(FileObject f) {
            this.ch = f.getChildren(true);
            this.root = f;
        }
        
        public boolean hasNext() {
            return ch.hasMoreElements();
        }
        
        public J2eeModule.RootedEntry next() {
            FileObject f = (FileObject) ch.nextElement();
            return new FSRootRE(root, f);
        }
        
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
        
        public FileObject getFileObject() {
            return f;
        }
        
        public String getRelativePath() {
            return FileUtil.getRelativePath(root, f);
        }
        
    }
    
}
