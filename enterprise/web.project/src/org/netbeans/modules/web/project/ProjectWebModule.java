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

package org.netbeans.modules.web.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.*;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.web.project.classpath.ClassPathProviderImpl;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.netbeans.modules.j2ee.deployment.devmodules.api.*;
import org.netbeans.modules.web.project.ui.customizer.WebProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileUtil;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.web.WebAppMetadataModelFactory;
import org.netbeans.modules.j2ee.dd.spi.webservices.WebservicesMetadataModelFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ResourceChangeReporterFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.ResourceChangeReporterImplementation;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.netbeans.modules.websvc.spi.webservices.WebServicesConstants;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;

/** A web module implementation on top of project.
 *
 * @author  Pavel Buzek
 * @author ads
 */
public final class ProjectWebModule extends J2eeModuleProvider
        implements J2eeModuleImplementation2, ModuleChangeReporter,
        EjbChangeDescriptor, PropertyChangeListener,
        Lookup.Provider
{

    public static final String FOLDER_WEB_INF = "WEB-INF";//NOI18N
//    public static final String FOLDER_CLASSES = "classes";//NOI18N
//    public static final String FOLDER_LIB     = "lib";//NOI18N
    public static final String FILE_DD        = "web.xml";//NOI18N

    public static final String LOOKUP_ITEM    = "lookup.item";//NOI18N

    private final ResourceChangeReporter rcr = ResourceChangeReporterFactory.createResourceChangeReporter(new WebResourceChangeReporter());

    private WebProject project;
    private UpdateHelper helper;
    private ClassPathProviderImpl cpProvider;
    private String fakeServerInstId = null; // used to get access to properties of other servers
    private Lookup myLookup;
    private InstanceContent myContent;

    private long notificationTimeout = 0; // used to suppress repeating the same messages

    private MetadataModel<WebAppMetadata> webAppMetadataModel;
    private MetadataModel<WebAppMetadata> webAppAnnMetadataModel;
    private MetadataModel<WebservicesMetadata> webservicesMetadataModel;

    private PropertyChangeSupport propertyChangeSupport;

    private J2eeModule j2eeModule;

    ProjectWebModule (WebProject project, UpdateHelper helper, ClassPathProviderImpl cpProvider) {
        this.project = project;
        this.helper = helper;
        this.cpProvider = cpProvider;
        myContent = new InstanceContent();
        myLookup = new AbstractLookup( myContent );
        //project.evaluator().addPropertyChangeListener(this);
    }

    public Lookup getLookup(){
        return myLookup;
    }

    public void addCookie( Object cookie ){
        if ( cookie == null ){
            return;
        }
        Object old = getLookup().lookup(cookie.getClass());
        myContent.add( cookie );
        getPropertyChangeSupport().firePropertyChange(LOOKUP_ITEM, old, cookie);
    }

    public void removeCookie( Object cookie ){
        if ( cookie == null ){
            return;
        }
        myContent.remove( cookie);
        getPropertyChangeSupport().firePropertyChange(LOOKUP_ITEM, cookie, null);
    }

    public FileObject getDeploymentDescriptor() {
        return getDeploymentDescriptor(false);
    }

    public FileObject getDeploymentDescriptor(boolean silent) {
        FileObject webInfFo = getWebInf(silent);
        if (webInfFo==null) {
            return null;
        }
        FileObject dd = webInfFo.getFileObject (FILE_DD);
        if (dd == null && !silent
                && (Profile.J2EE_13.equals(getJ2eeProfile()) ||
                    Profile.J2EE_14.equals(getJ2eeProfile()))) {
            showErrorMessage(NbBundle.getMessage(ProjectWebModule.class,"MSG_WebXmlNotFound", //NOI18N
                    webInfFo.getPath()));
        }
        return dd;
    }

    public String getContextPath () {
        try {
            return getConfigSupport().getWebContextRoot();
        } catch (ConfigurationException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

    public void setContextPath (String path) {
        try {
            getConfigSupport().setWebContextRoot(path);
        } catch (ConfigurationException e) {
            Exceptions.printStackTrace(e);
        }
    }

    public String getContextPath (String serverInstId) {
        fakeServerInstId = serverInstId;
        String result = getContextPath();
        fakeServerInstId = null;
        return result;
    }

    public void setContextPath (String serverInstId, String path) {
        fakeServerInstId = serverInstId;
        setContextPath(path);
        fakeServerInstId = null;
    }

    private void showErrorMessage(final String message) {
        synchronized (this) {
            if(new Date().getTime() > notificationTimeout && isProjectOpened()) {
                // set timeout to suppress the same messages during next 20 seconds (feel free to adjust the timeout
                // using more suitable value)
                notificationTimeout = new Date().getTime() + 20000;
            } else {
                return;
            }
        }
        // #240818
        DialogDisplayer.getDefault().notifyLater(new NotifyDescriptor.Message(message, NotifyDescriptor.ERROR_MESSAGE));
    }

    public FileObject getDocumentBase () {
        return getDocumentBase(false);
    }

    public FileObject getDocumentBase (boolean silent) {
        String value = helper.getAntProjectHelper().getStandardPropertyEvaluator()
                .getProperty(WebProjectProperties.WEB_DOCBASE_DIR);

        return resolveDocumentBase(value, silent);
    }

    FileObject resolveDocumentBase(String value, boolean silent) {
        FileObject docBase = value != null ? helper.getAntProjectHelper().resolveFileObject(value) : null;
        if (docBase == null && !silent) {
            String path = (value != null ? helper.getAntProjectHelper().resolvePath(value) : null);
            String errorMessage;
            if (path != null) {
                errorMessage = NbBundle.getMessage(ProjectWebModule.class, "MSG_DocBase_Corrupted", project.getName(), path);
            } else {
                errorMessage = NbBundle.getMessage(ProjectWebModule.class, "MSG_DocBase_Corrupted_Unknown", project.getName());
            }
            showErrorMessage(errorMessage);
        }
        return docBase;
    }

    @Deprecated
    public FileObject[] getJavaSources() {
        return project.getSourceRoots().getRoots();
    }

//    public ClassPath getJavaSources () {
//        ClassPathProvider cpp = (ClassPathProvider) project.getLookup ().lookup (ClassPathProvider.class);
//        if (cpp != null) {
//            return cpp.findClassPath (getFileObject ("src.dir"), ClassPath.SOURCE); //NOI18N
//        }
//        return null;
//    }

    public FileObject getWebInf () {
        return getWebInf(false);
    }

    public FileObject getWebInf (boolean silent) {
        String value = helper.getAntProjectHelper().getStandardPropertyEvaluator()
                .getProperty(WebProjectProperties.WEBINF_DIR);

        return resolveWebInf(null, value, silent, false);
    }

    FileObject resolveWebInf(String docBaseValue, String webInfValue, boolean silent, boolean useDocBase) {
        FileObject webInf = null;
        if (webInfValue != null){
             webInf = helper.getAntProjectHelper().resolveFileObject(webInfValue);
//             if (webInf == null && forceCreate){
//                webInf = helper.getAntProjectHelper().getProjectDirectory();
//                StringTokenizer st = new StringTokenizer(webInfValue, "/");
//                while (st.hasMoreTokens()) {
//                    String nameExt = st.nextToken();
//                    try {
//                        FileObject tmp = webInf.getFileObject(nameExt, null);
//                        webInf = tmp != null ? tmp : webInf.createFolder(nameExt);
//                    } catch (IOException ex) {
//                        Exceptions.printStackTrace(ex);
//                        webInf = null;
//                        break;
//                    }
//                }
//             }
        }

        //temporary solution for < 6.0 projects
        if (webInf == null) {
            FileObject documentBase = null;
            if (useDocBase) {
                documentBase = resolveDocumentBase(docBaseValue, silent);
            } else {
                documentBase = getDocumentBase(silent);
            }
            if (documentBase == null) {
                return null;
            }
            webInf = documentBase.getFileObject (FOLDER_WEB_INF);
//            if (webInf == null && forceCreate){
//                try {
//                    webInf = documentBase.createFolder(FOLDER_WEB_INF);
//                } catch (IOException ex) {
//                    Exceptions.printStackTrace(ex);
//                }
//            }
        }

        if (webInf == null && !silent) {
            showErrorMessage(NbBundle.getMessage(ProjectWebModule.class,"MSG_WebInfCorrupted2")); //NOI18N
        }
        return webInf;
    }

    public FileObject getConfDir() {
        return getFileObject(WebProjectProperties.CONF_DIR);
    }

    public File getConfDirAsFile() {
        return getFile(WebProjectProperties.CONF_DIR);
    }

    public FileObject getPersistenceXmlDir() {
        return getFileObject(WebProjectProperties.PERSISTENCE_XML_DIR);
    }

    public File getPersistenceXmlDirAsFile() {
        return getFile(WebProjectProperties.PERSISTENCE_XML_DIR);
    }

    public ClassPathProvider getClassPathProvider () {
        return project.getLookup().lookup(ClassPathProvider.class);
    }

    public FileObject getArchive () {
        return getFileObject ("dist.war"); //NOI18N
    }

    private FileObject getFileObject(String propname) {
        String prop = helper.getAntProjectHelper().getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.getAntProjectHelper().resolveFileObject(prop);
        } else {
            return null;
        }
    }

    private File getFile(String propname) {
        String prop = helper.getAntProjectHelper().getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.getAntProjectHelper().resolveFile(prop);
        } else {
            return null;
        }
    }

    public synchronized J2eeModule getJ2eeModule () {
        if (j2eeModule == null) {
            j2eeModule = J2eeModuleFactory.createJ2eeModule(this);
        }
        return j2eeModule;
    }

    public org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter getModuleChangeReporter () {
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
        return Boolean.parseBoolean(project.evaluator().getProperty(WebProjectProperties.J2EE_COMPILE_ON_SAVE)) &&
            !Boolean.parseBoolean(project.evaluator().getProperty(WebProjectProperties.J2EE_DEPLOY_ON_SAVE));
    }


    public File getDeploymentConfigurationFile(String name) {
        assert name != null : "File name of the deployement configuration file can't be null"; //NOI18N

        String path = getConfigSupport().getContentRelativePath(name);
        if (path == null) {
            path = name;
        }

        if (path.startsWith("WEB-INF/")) { //NOI18N
            path = path.substring(8); //removing "WEB-INF/"

            FileObject webInf = getWebInf();
            if (webInf == null) {
                //in case that docbase is null ... but normally it should not be
                return new File(getConfDirAsFile(), name);
            }
            return new File(FileUtil.toFile(webInf), path);
        } else {
            FileObject documentBase = getDocumentBase();
            if (documentBase == null) {
                //in case that docbase is null ... but normally it should not be
                return new File(getConfDirAsFile(), name);
            }
            return new File(FileUtil.toFile(documentBase), path);
        }
    }

    public FileObject getModuleFolder () {
        return getDocumentBase ();
    }

    public String getServerID () {
        String inst = getServerInstanceID ();
        if (inst != null) {
            String id = Deployment.getDefault().getServerID(inst);
            if (id != null) {
                return id;
            }
        }
        return helper.getAntProjectHelper().getStandardPropertyEvaluator ().getProperty (WebProjectProperties.J2EE_SERVER_TYPE);
    }

    public String getServerInstanceID () {
        if (fakeServerInstId != null)
            return fakeServerInstId;
        return helper.getAntProjectHelper().getStandardPropertyEvaluator ().getProperty (WebProjectProperties.J2EE_SERVER_INSTANCE);
    }

    public void setServerInstanceID(String severInstanceID) {
        WebProjectProperties.setServerInstance(project, helper, severInstanceID);
    }

    public Iterator<J2eeModule.RootedEntry> getArchiveContents () throws java.io.IOException {
        FileObject content = getContentDirectory();
        content.refresh();
        return new IT(content);
    }

    public FileObject getContentDirectory() {
        return getFileObject ("build.web.dir"); //NOI18N
    }

    public FileObject getBuildDirectory() {
        return getFileObject ("build.dir"); //NOI18N
    }

    public File getContentDirectoryAsFile() {
        return getFile ("build.web.dir"); //NOI18N
    }

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

    public synchronized MetadataModel<WebAppMetadata> getMetadataModel() {
        if (webAppMetadataModel == null) {
            FileObject ddFO = getDeploymentDescriptor();
            final FileObject webInf = getWebInf(true);
            if (ddFO == null && webInf != null) {
                webInf.addFileChangeListener(new FileChangeAdapter() {
                    @Override
                    public void fileDataCreated(FileEvent fe) {
                        if (FILE_DD.equals(fe.getFile().getNameExt())) {
                            webInf.removeFileChangeListener(this);
                            resetMetadataModel();
                        }
                    }
                });
            }
            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                ddFile);
            webAppMetadataModel = WebAppMetadataModelFactory.createMetadataModel(metadataUnit, true);
        }
        return webAppMetadataModel;
    }

    private synchronized void resetMetadataModel() {
        webAppMetadataModel = null;
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

    public void uncacheDescriptors() {
        // this.getConfigSupport().resetStorage();
        // reset timeout when closing the project
        synchronized (this) {
            notificationTimeout = 0;
        }
    }

    // TODO MetadataModel: rewrite when MetadataModel is ready
//    private Webservices getWebservices() {
//        if (Util.isJavaEE5orHigher(project)) {
//            WebServicesSupport wss = WebServicesSupport.getWebServicesSupport(project.getProjectDirectory());
//            try {
//                return org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.getDefault().getMergedDDRoot(wss);
//            } catch (IOException ex) {
//                ErrorManager.getDefault().notify(ex);
//            }
//        } else {
//            FileObject wsdd = getDD();
//            if(wsdd != null) {
//                try {
//                    return org.netbeans.modules.j2ee.dd.api.webservices.DDProvider.getDefault()
//                    .getDDRoot(getDD());
//                } catch (java.io.IOException e) {
//                    org.openide.ErrorManager.getDefault().log(e.getLocalizedMessage());
//                }
//            }
//        }
//        return null;
//    }

    public org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor getEjbChanges (long timestamp) {
        return this;
    }

    public J2eeModule.Type getModuleType () {
        return J2eeModule.Type.WAR;
    }

    @Override
    public String getModuleVersion () {
        // return a version based on the Java EE version
        Profile platformVersion = getJ2eeProfile();
        if (null == platformVersion) {
            return WebApp.VERSION_3_1;
        } else switch (platformVersion) {
            case JAKARTA_EE_11_FULL:
            case JAKARTA_EE_11_WEB:
                return WebApp.VERSION_6_1;
            case JAKARTA_EE_10_FULL:
            case JAKARTA_EE_10_WEB:
                return WebApp.VERSION_6_0;
            case JAKARTA_EE_9_1_FULL:
            case JAKARTA_EE_9_1_WEB:
            case JAKARTA_EE_9_FULL:
            case JAKARTA_EE_9_WEB:
                return WebApp.VERSION_5_0;
            case JAKARTA_EE_8_FULL:
            case JAKARTA_EE_8_WEB:
            case JAVA_EE_8_FULL:
            case JAVA_EE_8_WEB:
                return WebApp.VERSION_4_0;
            case JAVA_EE_7_FULL:
            case JAVA_EE_7_WEB:
                return WebApp.VERSION_3_1;
            case JAVA_EE_6_FULL:
            case JAVA_EE_6_WEB:
                return WebApp.VERSION_3_0;
            case JAVA_EE_5:
                return WebApp.VERSION_2_5;
            case J2EE_14:
                return WebApp.VERSION_2_4;
            default:
                return WebApp.VERSION_3_1;
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(org.netbeans.modules.j2ee.dd.api.web.WebApp.PROPERTY_VERSION)) {
            String oldVersion = (String) evt.getOldValue();
            String newVersion = (String) evt.getNewValue();
            getPropertyChangeSupport().firePropertyChange(J2eeModule.PROP_MODULE_VERSION, oldVersion, newVersion);
        } else if (evt.getPropertyName ().equals (WebProjectProperties.J2EE_SERVER_INSTANCE)) {
            Deployment d = Deployment.getDefault ();
            String oldServerID = evt.getOldValue () == null ? null : d.getServerID ((String) evt.getOldValue ());
            String newServerID = evt.getNewValue () == null ? null : d.getServerID ((String) evt.getNewValue ());
            fireServerChange (oldServerID, newServerID);
        }  else if (WebProjectProperties.RESOURCE_DIR.equals(evt.getPropertyName())) {
            String oldValue = (String) evt.getOldValue();
            String newValue = (String) evt.getNewValue();
            getPropertyChangeSupport().firePropertyChange(
                    J2eeModule.PROP_RESOURCE_DIRECTORY,
                    oldValue == null ? null : new File(oldValue),
                    newValue == null ? null : new File(newValue));
        }  else if (WebProjectProperties.WEB_DOCBASE_DIR.equals(evt.getPropertyName())) {
            getPropertyChangeSupport().firePropertyChange(
                    WebModuleImplementation2.PROPERTY_DOCUMENT_BASE,
                    (String)evt.getOldValue(),
                    (String)evt.getNewValue());
        }  else if (WebProjectProperties.WEBINF_DIR.equals(evt.getPropertyName())) {
            getPropertyChangeSupport().firePropertyChange(
                    WebModuleImplementation2.PROPERTY_WEB_INF,
                    (String)evt.getOldValue(),
                    (String)evt.getNewValue());
        }
    }

    public String getUrl () {
         EditableProperties ep =  helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
         String warName = ep.getProperty(WebProjectProperties.WAR_NAME);
         return warName == null ? "" : ("/"+warName); //NOI18N
    }

    public boolean isManifestChanged (long timestamp) {
        return false;
    }

    public void setUrl (String url) {
        throw new UnsupportedOperationException ("Cannot customize URL of web module"); //NOI18N
    }

    public boolean ejbsChanged () {
        return false;
    }

    public String[] getChangedEjbs () {
        return new String[] {};
    }

    public Profile getJ2eeProfile () {
        return Profile.fromPropertiesString(helper.getAntProjectHelper().getStandardPropertyEvaluator ().getProperty(WebProjectProperties.J2EE_PLATFORM));
    }

    public FileObject getDD() {
       FileObject webInfFo = getWebInf();
       if (webInfFo==null) {
           showErrorMessage(NbBundle.getMessage(ProjectWebModule.class,"MSG_WebInfCorrupted"));
           return null;
       }
       return getWebInf().getFileObject(WebServicesConstants.WEBSERVICES_DD, "xml"); // NOI18N
   }

    @Override
    public FileObject[] getSourceRoots() {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);

        List<FileObject> roots = new LinkedList<FileObject>();
        FileObject documentBase = getDocumentBase();
        if (documentBase != null)
            roots.add(documentBase);

        for (int i = 0; i < groups.length; i++) {
            roots.add(groups[i].getRootFolder());
        }

        FileObject[] rootArray = new FileObject[roots.size()];
        return roots.toArray(rootArray);
    }

    private boolean isProjectOpened() {
        // XXX workaround: OpenProjects.getDefault() can be null
        // when called from ProjectOpenedHook.projectOpened() upon IDE startup
        if (OpenProjects.getDefault() == null)
            return true;

        Project[] projects = OpenProjects.getDefault().getOpenProjects();
        for (int i = 0; i < projects.length; i++) {
            if (projects[i].equals(project))
                return true;
        }
        return false;
    }

    @Override
    public File getResourceDirectory() {
        File f = getFile(WebProjectProperties.RESOURCE_DIR);
        if (f == null) {
            f = new File(FileUtil.toFile(project.getProjectDirectory()), "setup"); // NOI18N
        }
        return f;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        //synchronized (this) {
            // XXX need to listen on the module version
            // if (!webAppPropChangeLInitialized) {
            //     try {
            //         project.getWebModule().getMetadataModel().runReadAction(new MetadataModelAction<WebAppMetadata, Void>() {
            //             public Void run(WebAppMetadata metadata) throws MetadataModelException, IOException {
            //                 WebApp webAp p = metadata.getRoot();
            //                 PropertyChangeListener l = (PropertyChangeListener) WeakListeners.create(PropertyChangeListener.class, ProjectWebModule.this, webApp);
            //                 webApp.addPropertyChangeListener(l);
            //                 return null;
            //             }
            //         });
            //         webAppPropChangeLInitialized = true;
            //     } catch (MetadataModelException e) {
            //         // TODO MetadataModel: how should we handle this?
            //     } catch (IOException e) {
            //         // TODO MetadataModel: how should we handle this?
            //     }
            // }
        //}
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

    private class WebResourceChangeReporter implements ResourceChangeReporterImplementation {

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
        ArrayList<FileObject> ch;
        FileObject root;

        private IT (FileObject f) {
            this.ch = new ArrayList<FileObject>();
            ch.add (f);
            this.root = f;
        }

        public boolean hasNext () {
            return ! ch.isEmpty();
        }

        public J2eeModule.RootedEntry next () {
            FileObject f = ch.get(0);
            ch.remove(0);
            if (f.isFolder()) {
                f.refresh();
                FileObject chArr[] = f.getChildren ();
                for (int i = 0; i < chArr.length; i++) {
                    ch.add(chArr [i]);
                }
            }
            return new FSRootRE (root, f);
        }

        public void remove () {
            throw new UnsupportedOperationException ();
        }

    }

    private static final class FSRootRE implements J2eeModule.RootedEntry {
        FileObject f;
        FileObject root;

        FSRootRE (FileObject root, FileObject f) {
            this.f = f;
            this.root = root;
        }

        public FileObject getFileObject () {
            return f;
        }

        public String getRelativePath () {
            return FileUtil.getRelativePath (root, f);
        }
    }
}
