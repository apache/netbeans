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

package org.netbeans.modules.j2ee.clientproject.api;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.api.queries.FileEncodingQuery;
import org.netbeans.modules.j2ee.clientproject.AppClientProject;
import org.netbeans.modules.j2ee.clientproject.AppClientProjectType;
import org.netbeans.modules.j2ee.clientproject.AppClientProvider;
import org.netbeans.modules.j2ee.clientproject.Utils;
import org.netbeans.modules.j2ee.clientproject.ui.customizer.AppClientProjectProperties;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.j2ee.dd.api.client.AppClient;
import org.netbeans.modules.j2ee.dd.api.client.DDProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.javaee.project.api.ant.AntProjectConstants;
import org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.ui.PlatformUiSupport;
import org.netbeans.spi.java.project.support.PreferredProjectPlatform;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.filesystems.FileLock;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Creates a AppClientProject from scratch according to some initial configuration.
 */
public class AppClientProjectGenerator {
    private static final String DEFAULT_CONF_FOLDER = "conf"; //NOI18N
    private static final String DEFAULT_SRC_FOLDER = "src"; //NOI18N
    private static final String DEFAULT_TEST_FOLDER = "test"; //NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup"; //NOI18N
    private static final String DEFAULT_JAVA_FOLDER = "java"; //NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; //NOI18N
    
    public static final String MINIMUM_ANT_VERSION = "1.6.5"; // NOI18N
    
    private static final String MANIFEST_FILE = "MANIFEST.MF"; // NOI18N
    
    private AppClientProjectGenerator() {}
    
    /**
     * Create a new Application client project.
     *
     * @param dir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the name for the project
     * @param mainClass the name for the main class
     * @param j2eeLevel defined in <code>org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule</code>
     * @param serverInstanceID provided by j2eeserver module
     *
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    @Deprecated
    public static AntProjectHelper createProject(File dir, final String name, 
            final String mainClass, final String j2eeLevel, 
            final String serverInstanceID) throws IOException {
        return createProject(dir, name, mainClass, j2eeLevel, serverInstanceID, null);
    }

    @Deprecated
    public static AntProjectHelper createProject(File dir, final String name, 
            final String mainClass, final String j2eeLevel, 
            final String serverInstanceID, final String librariesDefinition
            ) throws IOException {

        AppClientProjectCreateData createData = new AppClientProjectCreateData();
        createData.setProjectDir(dir);
        createData.setName(name);
        createData.setMainClass(mainClass);
        createData.setJavaEEProfile(Profile.fromPropertiesString(j2eeLevel));
        createData.setServerInstanceID(serverInstanceID);
        createData.setLibrariesDefinition(librariesDefinition);
        return createProject(createData);
    }

    public static AntProjectHelper createProject(final AppClientProjectCreateData createData) throws IOException {
        File dir = createData.getProjectDir();

        final AntProjectHelper[] h = new AntProjectHelper[1];
        final FileObject projectDir = FileUtil.createFolder(dir);

        // create project in one FS atomic action:
        FileSystem fs = projectDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                AntProjectHelper helper = createProjectImpl(createData, projectDir);
                h[0] = helper;
            }});
        return h[0];
    }

    private static AntProjectHelper createProjectImpl(final AppClientProjectCreateData createData,
            final FileObject projectDir) throws IOException {

        String name = createData.getName();
        String mainClass = createData.getMainClass();
        String serverInstanceID = createData.getServerInstanceID();
        Profile j2eeProfile = createData.getJavaEEProfile();

        FileObject srcRoot = projectDir.createFolder(DEFAULT_SRC_FOLDER);
        FileObject javaRoot = srcRoot.createFolder(DEFAULT_JAVA_FOLDER);
        FileObject confRoot = srcRoot.createFolder(DEFAULT_CONF_FOLDER);
        projectDir.createFolder(DEFAULT_TEST_FOLDER);
        
        // create application-client.xml
        String resource;
        if(null == j2eeProfile) {
            resource = "org-netbeans-modules-j2ee-clientproject/application-client-6.xml"; // NOI18N
        } else { 
            switch (j2eeProfile) {
                case JAKARTA_EE_11_FULL:
                case JAKARTA_EE_11_WEB:
                    resource = "org-netbeans-modules-j2ee-clientproject/application-client-11.xml"; // NOI18N
                    break;
                case JAKARTA_EE_10_FULL:
                case JAKARTA_EE_10_WEB:
                    resource = "org-netbeans-modules-j2ee-clientproject/application-client-10.xml"; // NOI18N
                    break;
                case JAKARTA_EE_9_1_FULL:
                case JAKARTA_EE_9_1_WEB:
                case JAKARTA_EE_9_FULL:
                case JAKARTA_EE_9_WEB:
                    resource = "org-netbeans-modules-j2ee-clientproject/application-client-9.xml"; // NOI18N
                    break;
                case JAKARTA_EE_8_FULL:
                case JAKARTA_EE_8_WEB:
                case JAVA_EE_8_FULL:
                case JAVA_EE_8_WEB:
                    resource = "org-netbeans-modules-j2ee-clientproject/application-client-8.xml"; // NOI18N
                    break;
                case JAVA_EE_7_FULL:
                case JAVA_EE_7_WEB:
                    resource = "org-netbeans-modules-j2ee-clientproject/application-client-7.xml"; // NOI18N
                    break;
                case JAVA_EE_5:
                    resource = "org-netbeans-modules-j2ee-clientproject/application-client-5.xml"; // NOI18N
                    break;
                case J2EE_14:
                    resource = "org-netbeans-modules-j2ee-clientproject/application-client-1.4.xml"; // NOI18N
                    break;
                default:
                    resource = "org-netbeans-modules-j2ee-clientproject/application-client-6.xml"; // NOI18N
                    break;
            }
        }
        FileObject foSource = FileUtil.getConfigFile(resource);
        FileObject ddFile = FileUtil.copyFile(foSource, confRoot, "application-client"); //NOI18N
        AppClient appClient = DDProvider.getDefault().getDDRoot(ddFile);
        appClient.setDisplayName(name);
        appClient.write(ddFile);
        if (createData.isCDIEnabled()) {
            DDHelper.createBeansXml(j2eeProfile, confRoot);
        }
        
        final AntProjectHelper h = setupProject(projectDir, name,
                DEFAULT_SRC_FOLDER, DEFAULT_TEST_FOLDER,
                null, null, null, mainClass, j2eeProfile,
                serverInstanceID, createData.getLibrariesDefinition(), 
                createData.skipTests());
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.put(AppClientProjectProperties.SOURCE_ROOT, DEFAULT_SRC_FOLDER); //NOI18N
        ep.setProperty(AppClientProjectProperties.META_INF, "${"+AppClientProjectProperties.SOURCE_ROOT+"}/"+DEFAULT_CONF_FOLDER); //NOI18N
        ep.setProperty(AppClientProjectProperties.SRC_DIR, "${"+AppClientProjectProperties.SOURCE_ROOT+"}/"+DEFAULT_JAVA_FOLDER); //NOI18N
        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        AppClientProject p = (AppClientProject) ProjectManager.getDefault().findProject(projectDir);
        ProjectManager.getDefault().saveProject(p);
        
        final ReferenceHelper refHelper = p.getReferenceHelper();        
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    copyRequiredLibraries(h, refHelper, createData);
                    return null;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex.getException());
        }        
        
        if ( mainClass != null ) {
            createMainClass( mainClass, javaRoot );
        }
        
        createManifest(confRoot, MANIFEST_FILE);
        
        return h;
    }
    
    /**
     * Imports an existing Application client project into NetBeans project.
     *
     * @param dir the top-level directory (need not yet exist but if it does it must be empty) - "nbproject" location
     * @param name the name for the project
     * @param sourceFolders top-level location(s) of java sources - must not be null
     * @param testFolders top-level location(s) of test(s) - must not be null
     * @param confFolder top-level location of configuration file(s) folder - must not be null
     * @param libFolder top-level location of libraries
     * @param j2eeLevel defined in <code>org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule</code>
     * @param serverInstanceID provided by j2eeserver module
     *
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    @Deprecated
    public static AntProjectHelper importProject(final File dir, final String name,
            final File[] sourceFolders, final File[] testFolders, final File confFolder, 
            final File libFolder, final String j2eeLevel, final String serverInstanceID) throws IOException {
        
        return importProject(dir, name, sourceFolders, testFolders, confFolder,
                libFolder, j2eeLevel, serverInstanceID, null);
    }

    @Deprecated
    public static AntProjectHelper importProject(final File dir, final String name,
            final File[] sourceFolders, final File[] testFolders, final File confFolder, 
            final File libFolder, final String j2eeLevel, final String serverInstanceID,
            final String librariesDefinition) throws IOException {

        AppClientProjectCreateData createData = new AppClientProjectCreateData();
        createData.setProjectDir(dir);
        createData.setName(name);
        createData.setSourceFolders(sourceFolders);
        createData.setTestFolders(testFolders);
        createData.setConfFolder(confFolder);
        createData.setLibFolder(libFolder);
        createData.setJavaEEProfile(Profile.fromPropertiesString(j2eeLevel));
        createData.setServerInstanceID(serverInstanceID);
        createData.setLibrariesDefinition(librariesDefinition);
        return importProject(createData);
    }

    public static AntProjectHelper importProject(final AppClientProjectCreateData createData) throws IOException {
        File dir = createData.getProjectDir();
        
        final AntProjectHelper[] h = new AntProjectHelper[1];
        final FileObject projectDir = FileUtil.createFolder(dir);

        // create project in one FS atomic action:
        FileSystem fs = projectDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                AntProjectHelper helper = importProjectImpl(createData, projectDir);
                h[0] = helper;
            }});
        return h[0];
    }
    
    private static AntProjectHelper importProjectImpl(final AppClientProjectCreateData createData, final FileObject projectDir) throws IOException {
        final File[] sourceFolders = createData.getSourceFolders();
        final File[] testFolders = createData.getTestFolders();
        String name = createData.getName();
        String serverInstanceID = createData.getServerInstanceID();
        File confFolder = createData.getConfFolder();
        Profile j2eeProfile = createData.getJavaEEProfile();

        assert sourceFolders != null && testFolders != null: "Package roots can't be null";   //NOI18N
        
        final AntProjectHelper h = setupProject(projectDir, name, null, null,
                confFolder, createData.getLibFolder(),
                null, null, j2eeProfile, serverInstanceID, createData.getLibrariesDefinition(), 
                createData.skipTests());
        
        final AppClientProject p = (AppClientProject) ProjectManager.getDefault().findProject(projectDir);
        final ReferenceHelper refHelper = p.getReferenceHelper();
        
        try {
            ProjectManager.mutex().writeAccess( new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    Element data = h.getPrimaryConfigurationData(true);
                    Document doc = data.getOwnerDocument();
                    NodeList nl = data.getElementsByTagNameNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots"); // NOI18N
                    assert nl.getLength() == 1;
                    Element sourceRoots = (Element) nl.item(0);
                    nl = data.getElementsByTagNameNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
                    assert nl.getLength() == 1;
                    Element testRoots = (Element) nl.item(0);
                    for (int i=0; i<sourceFolders.length; i++) {
                        String propName = "src.dir" + (i == 0 ? "" : Integer.toString(i+1)); //NOI18N
                        String srcReference = refHelper.createForeignFileReference(sourceFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                        Element root = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                        root.setAttribute("id",propName);   //NOI18N
                        sourceRoots.appendChild(root);
                        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.put(propName,srcReference);
                        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                    }

                    if (testFolders.length == 0) {
                        EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        props.put("test.src.dir", ""); // NOI18N
                        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props); // #47609
                    } else {
                        for (int i=0; i<testFolders.length; i++) {
                            if (!testFolders[i].exists()) {
                                FileUtil.createFolder(testFolders[i]);
                            }
                            String propName = "test.src.dir" + (i == 0 ? "" : Integer.toString(i+1)); //NOI18N
                            String testReference = refHelper.createForeignFileReference(testFolders[i], JavaProjectConstants.SOURCES_TYPE_JAVA);
                            Element root = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
                            root.setAttribute("id",propName);   //NOI18N
                            testRoots.appendChild(root);
                            EditableProperties props = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH); // #47609
                            props.put(propName,testReference);
                            h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        }
                    }
                    h.putPrimaryConfigurationData(data,true);
                    copyRequiredLibraries(h, refHelper, createData);
                    ProjectManager.getDefault().saveProject(p);
                    return null;
                }
            });
        } catch (MutexException me ) {
            IOException ex = new IOException("project creation failed");
            ex.initCause(me);
            throw ex;
        }
        
        // AB: fix for #53170: if j2eeLevel is 1.4 and application-client.xml is version 1.3, we upgrade it to version 1.4
        FileObject confFolderFO = FileUtil.toFileObject(confFolder);
        FileObject appClientXML = confFolderFO == null ? null
                : confFolderFO.getFileObject(AppClientProvider.FILE_DD);
        if (appClientXML != null) {
            try {
                AppClient root = DDProvider.getDefault().getDDRoot(appClientXML);
                boolean writeDD = false;
                boolean upgradeTo14 = root.getVersion() == null ? true :
                    new BigDecimal(AppClient.VERSION_1_4).compareTo(root.getVersion()) > 0;
                if (upgradeTo14 && Profile.J2EE_14.equals(j2eeProfile)) {
                    root.setVersion(new BigDecimal(AppClient.VERSION_1_4));
                    writeDD = true;
                }
                // also set the display name if not set (#55733)
                String dispName = root.getDefaultDisplayName();
                if (null == dispName || dispName.trim().length() == 0) {
                    root.setDisplayName(name);
                    writeDD = true;
                }
                if (writeDD) {
                    root.write(appClientXML);
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        } else {
            // XXX just temporary, since now the import would fail due to another bug
            String resource;
            if (null == j2eeProfile) {
                resource = "org-netbeans-modules-j2ee-clientproject/application-client-6.xml"; // NOI18N
            } else {
                switch (j2eeProfile) {
                    case JAKARTA_EE_11_FULL:
                    case JAKARTA_EE_11_WEB:
                        resource = "org-netbeans-modules-j2ee-clientproject/application-client-11.xml"; // NOI18N
                        break;
                    case JAKARTA_EE_10_FULL:
                    case JAKARTA_EE_10_WEB:
                        resource = "org-netbeans-modules-j2ee-clientproject/application-client-10.xml"; // NOI18N
                        break;
                    case JAKARTA_EE_9_1_FULL:
                    case JAKARTA_EE_9_1_WEB:
                    case JAKARTA_EE_9_FULL:
                    case JAKARTA_EE_9_WEB:
                        resource = "org-netbeans-modules-j2ee-clientproject/application-client-9.xml"; // NOI18N
                        break;
                    case JAKARTA_EE_8_FULL:
                    case JAKARTA_EE_8_WEB:
                    case JAVA_EE_8_FULL:
                    case JAVA_EE_8_WEB:
                        resource = "org-netbeans-modules-j2ee-clientproject/application-client-8.xml"; // NOI18N
                        break;
                    case JAVA_EE_7_FULL:
                    case JAVA_EE_7_WEB:
                        resource = "org-netbeans-modules-j2ee-clientproject/application-client-7.xml"; // NOI18N
                        break;
                    case JAVA_EE_5:
                        resource = "org-netbeans-modules-j2ee-clientproject/application-client-5.xml"; // NOI18N
                        break;
                    case J2EE_14:
                        resource = "org-netbeans-modules-j2ee-clientproject/application-client-1.4.xml"; // NOI18N
                        break;
                    default:
                        resource = "org-netbeans-modules-j2ee-clientproject/application-client-6.xml"; // NOI18N
                        break;
                }
            }
            FileUtil.copyFile(FileUtil.getConfigFile(resource),
                    confFolderFO, "application-client"); //NOI18N
        }
        createManifest(confFolderFO, MANIFEST_FILE);
        return h;
    }
    
    private static void copyRequiredLibraries(AntProjectHelper h, ReferenceHelper rh,
            AppClientProjectCreateData createData) throws IOException {

        if (!h.isSharableProject()) {
            return;
        }
        if (!createData.skipTests() && rh.getProjectLibraryManager().getLibrary("junit") == null) { // NOI18N
            if (LibraryManager.getDefault().getLibrary("junit") != null) {
                rh.copyLibrary(LibraryManager.getDefault().getLibrary("junit")); // NOI18N
            }
        }
        if (!createData.skipTests() && rh.getProjectLibraryManager().getLibrary("junit_4") == null) { // NOI18N
            if (LibraryManager.getDefault().getLibrary("junit_4") != null) {
                rh.copyLibrary(LibraryManager.getDefault().getLibrary("junit_4")); // NOI18N
            }
        }
        Profile j2eeProfile = createData.getJavaEEProfile();
        String libraryName = null;
        if (j2eeProfile.equals(Profile.JAVA_EE_6_FULL) || j2eeProfile.equals(Profile.JAVA_EE_6_WEB)) {
            libraryName = AntProjectConstants.ENDORSED_LIBRARY_NAME_6;
            if (rh.getProjectLibraryManager().getLibrary(libraryName) == null) { // NOI18N
                rh.copyLibrary(LibraryManager.getDefault().getLibrary(libraryName)); // NOI18N
            }
        } else if (j2eeProfile.equals(Profile.JAVA_EE_7_FULL) || j2eeProfile.equals(Profile.JAVA_EE_7_WEB)) {
            libraryName = AntProjectConstants.ENDORSED_LIBRARY_NAME_7;
            if (rh.getProjectLibraryManager().getLibrary(libraryName) == null) { // NOI18N
                rh.copyLibrary(LibraryManager.getDefault().getLibrary(libraryName)); // NOI18N
            }
        }
        SharabilityUtility.makeSureProjectHasCopyLibsLibrary(h, rh);
    }
    
    /**
     * Imports an existing Application client project into NetBeans project
     * with a flag to specify whether the project contains java source files
     * or was created from an exploded archive.
     * @return the helper object permitting it to be further customized
     * @param fromJavaSource indicate whether the project is "from" source or an exploded archive
     * @param dir the top-level directory (need not yet exist but if it does it must be empty) - "nbproject" location
     * @param name the name for the project
     * @param sourceFolders top-level location(s) of java sources - must not be null
     * @param testFolders top-level location(s) of test(s) - must not be null
     * @param confFolder top-level location of configuration file(s) folder - must not be null
     * @param libFolder top-level location of libraries
     * @param j2eeLevel defined in <code>org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule</code>
     * @param serverInstanceID provided by j2eeserver module
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper importProject(final File dir, final String name,
            final File[] sourceFolders, final File[] testFolders, final File confFolder,
            final File libFolder, String j2eeLevel, String serverInstanceID,boolean fromJavaSource) throws IOException {
        AntProjectHelper h = importProject(dir,name,sourceFolders,testFolders,
                confFolder,libFolder,j2eeLevel,serverInstanceID);
        EditableProperties subEp = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        subEp.setProperty(AppClientProjectProperties.JAVA_SOURCE_BASED,fromJavaSource+""); // NOI18N        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH,subEp);
        Project subP = ProjectManager.getDefault().findProject(h.getProjectDirectory());
        ProjectManager.getDefault().saveProject(subP);
        return h;
    }
    
    private static AntProjectHelper setupProject(FileObject dirFO, String name,
            String srcRoot, String testRoot, File configFiles, File libraries,
            String resources, String mainClass, Profile j2eeProfile,
            String serverInstanceID, String librariesDefinition, 
            boolean skipTests) throws IOException {

        Utils.logUI(NbBundle.getBundle(AppClientProjectGenerator.class), "UI_APP_PROJECT_CREATE_SHARABILITY", // NOI18N
                new Object[]{(librariesDefinition != null), Boolean.FALSE});

        AntProjectHelper h = ProjectGenerator.createProject(dirFO, AppClientProjectType.TYPE, librariesDefinition);
        final AppClientProject prj = (AppClientProject)ProjectManager.getDefault().findProject(h.getProjectDirectory());
        final ReferenceHelper referenceHelper = prj.getReferenceHelper();
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode(MINIMUM_ANT_VERSION)); // NOI18N
        data.appendChild(minant);
        
        //TODO: ma154696: not sure if needed
        //        Element addLibs = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, "ejb-module-additional-libraries"); //NOI18N
        //        data.appendChild(addLibs);
        
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        EditableProperties epPriv = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        Element sourceRoots = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"source-roots");  //NOI18N
        if (srcRoot != null) {
            Element root = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute("id","src.dir");   //NOI18N
            root.setAttribute("name",NbBundle.getMessage(AppClientProjectGenerator.class, "NAME_src.dir"));
            sourceRoots.appendChild(root);
            ep.setProperty("src.dir", srcRoot); // NOI18N
        }
        data.appendChild(sourceRoots);
        Element testRoots = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"test-roots");  //NOI18N
        if (testRoot != null) {
            Element root = doc.createElementNS(AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE,"root");   //NOI18N
            root.setAttribute("id","test.src.dir");   //NOI18N
            root.setAttribute("name",NbBundle.getMessage(AppClientProjectGenerator.class, "NAME_test.src.dir"));
            testRoots.appendChild(root);
            ep.setProperty("test.src.dir", testRoot); // NOI18N
        }
        data.appendChild(testRoots);
        h.putPrimaryConfigurationData(data, true);
        
        if (configFiles != null) {
            String ref = createFileReference(referenceHelper, dirFO, FileUtil.toFileObject(configFiles));
            ep.setProperty(AppClientProjectProperties.META_INF, ref);
        }
        if (libraries != null) {
            String ref = createFileReference(referenceHelper, dirFO, FileUtil.toFileObject(libraries));
            ep.setProperty(AppClientProjectProperties.LIBRARIES_DIR, ref);
        }
        
        if (resources != null) {
            ep.setProperty(AppClientProjectProperties.RESOURCE_DIR, resources);
        } else {
            ep.setProperty(AppClientProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
        }
        
        //XXX the name of the dist.ear.jar file should be different, but now it cannot be since the name is used as a key in module provider mapping
        ep.setProperty(AppClientProjectProperties.DIST_EAR_JAR, "${"+AppClientProjectProperties.DIST_DIR+"}/" + "${" + AppClientProjectProperties.JAR_NAME + "}"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAR_NAME, PropertyUtils.getUsablePropertyName(name) + ".jar"); // NOI18N
        ep.setProperty(AppClientProjectProperties.BUILD_EAR_CLASSES_DIR, "${"+AppClientProjectProperties.BUILD_DIR+"}/jar"); // NOI18N
        
        ep.setProperty("dist.dir", "dist"); // NOI18N
        ep.setComment("dist.dir", new String[] {"# " + NbBundle.getMessage(AppClientProjectGenerator.class, "COMMENT_dist.dir")}, false); // NOI18N
        //        ep.setProperty("dist.jar", "${dist.dir}/" + PropertyUtils.getUsablePropertyName(name) + ".jar"); // NOI18N
        ep.setProperty(AppClientProjectProperties.DIST_JAR, "${"+AppClientProjectProperties.DIST_DIR+"}/" + "${" + AppClientProjectProperties.JAR_NAME + "}"); // NOI18N
        ep.setProperty("build.sysclasspath", "ignore"); // NOI18N
        ep.setComment("build.sysclasspath", new String[] {"# " + NbBundle.getMessage(AppClientProjectGenerator.class, "COMMENT_build.sysclasspath")}, false); // NOI18N
        ep.setProperty("run.classpath", new String[] { // NOI18N
            "${javac.classpath}:", // NOI18N
            "${build.classes.dir}", // NOI18N
        });
        ep.setProperty("debug.classpath", new String[] { // NOI18N
            "${run.classpath}", // NOI18N
        });
        ep.setProperty("jar.compress", "false"); // NOI18N
        if (mainClass != null) {
            ep.setProperty("main.class", mainClass); // NOI18N
        }
        
        ep.setProperty("javac.compilerargs", ""); // NOI18N
        ep.setComment("javac.compilerargs", new String[] { // NOI18N
            "# " + NbBundle.getMessage(AppClientProjectGenerator.class, "COMMENT_javac.compilerargs"), // NOI18N
        }, false);
        
        JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
        SpecificationVersion v = defaultPlatform.getSpecification().getVersion();
        String sourceLevel = v.toString();
        // #89131: these levels are not actually distinct from 1.5.
        // #181215: JDK 6 should be the default source/binary format for Java EE 6 projects
        // #181215: Not neccessary anymore because NetBeans should run on minimum JDK 8
        ep.setProperty(AppClientProjectProperties.JAVAC_SOURCE, sourceLevel); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVAC_TARGET, sourceLevel); // NOI18N
        
        ep.setProperty("javac.deprecation", "false"); // NOI18N
        ep.setProperty("javac.test.classpath", skipTests ? new String[] { // NOI18N
            "${javac.classpath}:", // NOI18N
            "${build.classes.dir}", // NOI18N
        } : new String[] { // NOI18N
            "${javac.classpath}:", // NOI18N
            "${build.classes.dir}:", // NOI18N
            "${libs.junit.classpath}:", // NOI18N
            "${libs.junit_4.classpath}", // NOI18N
        });
        ep.setProperty("run.test.classpath", new String[] { // NOI18N
            "${javac.test.classpath}:", // NOI18N
            "${build.test.classes.dir}", // NOI18N
        });
        ep.setProperty("debug.test.classpath", new String[] { // NOI18N
            "${run.test.classpath}", // NOI18N
        });
        
        ep.setProperty("build.generated.dir", "${build.dir}/generated"); // NOI18N
        //ep.setProperty("meta.inf.dir", "${src.dir}/META-INF"); // NOI18N
        
        ep.setProperty("build.dir", DEFAULT_BUILD_DIR); // NOI18N
        ep.setComment("build.dir", new String[] {"# " + NbBundle.getMessage(AppClientProjectGenerator.class, "COMMENT_build.dir")}, false); // NOI18N
        //ep.setProperty("build.classes.dir", "${build.dir}/classes"); // NOI18N
        ep.setProperty("build.classes.dir", "${build.dir}/jar"); // NOI18N
        ep.setProperty("build.generated.sources.dir", "${build.dir}/generated-sources"); // NOI18N
        ep.setProperty("build.test.classes.dir", "${build.dir}/test/classes"); // NOI18N
        ep.setProperty("build.test.results.dir", "${build.dir}/test/results"); // NOI18N
        ep.setProperty("build.classes.excludes", "**/*.java,**/*.form"); // NOI18N
        ep.setProperty("dist.javadoc.dir", "${dist.dir}/javadoc"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVA_PLATFORM, "default_platform"); // NOI18N
        
        ep.setProperty("run.jvmargs", ""); // NOI18N
        ep.setComment("run.jvmargs", new String[] { // NOI18N
            "# " + NbBundle.getMessage(AppClientProjectGenerator.class, "COMMENT_run.jvmargs"), // NOI18N
            "# " + NbBundle.getMessage(AppClientProjectGenerator.class, "COMMENT_run.jvmargs_2"), // NOI18N
            "# " + NbBundle.getMessage(AppClientProjectGenerator.class, "COMMENT_run.jvmargs_3"), // NOI18N
        }, false);
        
        ep.setProperty(AppClientProjectProperties.JAVADOC_PRIVATE, "false"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_NO_TREE, "false"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_USE, "true"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_NO_NAVBAR, "false"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_NO_INDEX, "false"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_SPLIT_INDEX, "true"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_AUTHOR, "false"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_VERSION, "false"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_WINDOW_TITLE, ""); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_ENCODING, "${" + AppClientProjectProperties.SOURCE_ENCODING + "}"); // NOI18N
        ep.setProperty(AppClientProjectProperties.JAVADOC_ADDITIONALPARAM, ""); // NOI18N
        
        //Deployment deployment = Deployment.getDefault();
        J2eePlatform j2eePlatform = null;
        try {
            j2eePlatform = Deployment.getDefault().getServerInstance(serverInstanceID).getJ2eePlatform();
            if (!j2eePlatform.getSupportedProfiles(J2eeModule.Type.CAR).contains(j2eeProfile)) {
                Logger.getLogger(AppClientProjectGenerator.class.getName())
                                .log(Level.WARNING, NbBundle.getMessage(AppClientProjectGenerator.class,
                                "MSG_Warning_SpecLevelNotSupported",
                                new Object[] {j2eeProfile, Deployment.getDefault().getServerInstance(serverInstanceID).getDisplayName()}));
            }
        } catch (InstanceRemovedException ie) {
            // noop ignore
            Logger.getLogger(AppClientProjectGenerator.class.getName()).log(Level.FINE, "{0}", ie);
        }
        
        ep.setProperty(AppClientProjectProperties.J2EE_PLATFORM, j2eeProfile.toPropertiesString());
        ep.setProperty("manifest.file", "${" +AppClientProjectProperties.META_INF + "}/" + MANIFEST_FILE); // NOI18N
        
        ep.setProperty(ProjectProperties.JAVAC_CLASSPATH, "");
        J2EEProjectProperties.setServerProperties(ep, epPriv, null, null, serverInstanceID, j2eeProfile, J2eeModule.Type.CAR);
        AppClientProjectProperties.generateExtraServerProperty(epPriv);

        String mainClassArgs = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_MAIN_CLASS_ARGS);
        if (mainClassArgs != null && !mainClassArgs.equals("")) {
            ep.put(AppClientProjectProperties.APPCLIENT_MAINCLASS_ARGS, mainClassArgs);
        } else if ((j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, AppClientProjectProperties.CLIENT_NAME)) != null) {
            ep.put(AppClientProjectProperties.CLIENT_NAME, mainClassArgs);
        }
        
        if (j2eeProfile.equals(Profile.JAVA_EE_6_FULL) || j2eeProfile.equals(Profile.JAVA_EE_6_WEB)) {
            ep.setProperty(ProjectProperties.ENDORSED_CLASSPATH, new String[]{AntProjectConstants.ENDORSED_LIBRARY_CLASSPATH_6});
        } else if (j2eeProfile.equals(Profile.JAVA_EE_7_FULL) || j2eeProfile.equals(Profile.JAVA_EE_7_WEB)) {
            ep.setProperty(ProjectProperties.ENDORSED_CLASSPATH, new String[]{AntProjectConstants.ENDORSED_LIBRARY_CLASSPATH_7});
        }
        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        // XXX this seems to be used in runtime only so, not part of sharable server
        // set j2ee.appclient environment
        File[] accrt = j2eePlatform.getToolClasspathEntries(J2eePlatform.TOOL_APP_CLIENT_RUNTIME);
        Map<String, String> roots = J2EEProjectProperties.extractPlatformLibrariesRoot(j2eePlatform);
        epPriv.setProperty(AppClientProjectProperties.APPCLIENT_TOOL_RUNTIME, J2EEProjectProperties.toClasspathString(accrt, roots));
        
        String acMain = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_MAIN_CLASS);
        if (acMain != null) {
            epPriv.setProperty(AppClientProjectProperties.APPCLIENT_TOOL_MAINCLASS, acMain);
        }
        String jvmOpts = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_JVM_OPTS);
        if (jvmOpts != null) {
            epPriv.setProperty(AppClientProjectProperties.APPCLIENT_TOOL_JVMOPTS, jvmOpts);
        }
        String args = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME,
                AppClientProjectProperties.J2EE_PLATFORM_APPCLIENT_ARGS);
        if (args != null) {
            epPriv.setProperty(AppClientProjectProperties.APPCLIENT_TOOL_ARGS, args);
        }

        // WORKAROUND for --retrieve option in asadmin deploy command
        // works only for local domains
        // see also http://www.netbeans.org/issues/show_bug.cgi?id=82929
        String copyProperty = j2eePlatform.getToolProperty(J2eePlatform.TOOL_APP_CLIENT_RUNTIME, J2eePlatform.TOOL_PROP_CLIENT_JAR_LOCATION);
        if (copyProperty != null) {
            ep.setProperty(AppClientProjectProperties.APPCLIENT_TOOL_CLIENT_JAR, copyProperty);
        } else {
            ep.remove(AppClientProjectProperties.APPCLIENT_TOOL_CLIENT_JAR);
        }

        J2EEProjectProperties.createDeploymentScript(dirFO, ep, epPriv, serverInstanceID, J2eeModule.Type.CAR);

        // use the default encoding
        Charset enc = FileEncodingQuery.getDefaultEncoding();
        epPriv.setProperty(AppClientProjectProperties.SOURCE_ENCODING, enc.name());
        
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, epPriv);
        
        return h;
    }

    private static String createFileReference(ReferenceHelper refHelper, FileObject projectFO, FileObject referencedFO) {
        String relPath = FileUtil.getRelativePath(projectFO, referencedFO);
        if (relPath != null) {
            return relPath;
        } else {
            return refHelper.createForeignFileReference(FileUtil.toFile(referencedFO), null);
        }
    }
    
    private static void createMainClass( String mainClassName, FileObject srcFolder ) throws IOException {
        
        int lastDotIdx = mainClassName.lastIndexOf( '.' );
        String mName, pName;
        if ( lastDotIdx == -1 ) {
            mName = mainClassName.trim();
            pName = null;
        } else {
            mName = mainClassName.substring( lastDotIdx + 1 ).trim();
            pName = mainClassName.substring( 0, lastDotIdx ).trim();
        }
        
        if ( mName.length() == 0 ) {
            return;
        }
        
        FileObject mainTemplate = FileUtil.getConfigFile( "Templates/Classes/Main.java" ); // NOI18N
        
        if ( mainTemplate == null ) {
            return; // Don't know the template
        }
        
        DataObject mt = DataObject.find( mainTemplate );
        
        FileObject pkgFolder = srcFolder;
        if ( pName != null ) {
            String fName = pName.replace( '.', '/' ); // NOI18N
            pkgFolder = FileUtil.createFolder( srcFolder, fName );
        }
        DataFolder pDf = DataFolder.findFolder( pkgFolder );
        mt.createFromTemplate( pDf, mName );
        
    }
    
    /**
     * Set J2SE platform to be used.
     *
     * @param helper "reference" to project to be updated
     * @param platformName the name of the J2SE platform
     * @param sourceLevel the source level to be set
     */
    // AB: this method is also called from the enterprise application, so we can't pass UpdateHelper here
    // well, actually we can, but let's not expose too many classes
    public static void setPlatform(final AntProjectHelper helper, final String platformName, final String sourceLevel) {
        FileObject projectDir = helper.getProjectDirectory();
        if (projectDir == null) {
            return;
        }
        // issue 89278: do not fire file change events under ProjectManager.MUTEX,
        // it is deadlock-prone
        try {
            projectDir.getFileSystem().runAtomicAction(new AtomicAction() {
                @Override
                public void run() throws IOException {
                    ProjectManager.mutex().writeAccess(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                AppClientProject project = (AppClientProject)ProjectManager.getDefault().findProject(helper.getProjectDirectory());
                                UpdateHelper updateHelper = project.getUpdateHelper();
                                EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                String finalPlatformName = platformName;
                                if (finalPlatformName == null) {
                                    finalPlatformName = PreferredProjectPlatform.getPreferredPlatform(JavaPlatform.getDefault().getSpecification().getName()).getDisplayName();
                                }
                                // #89131: these levels are not actually distinct from 1.5.
                                // #181215: JDK 6 should be the default source/binary format for Java EE 6 projects
                                // #181215: Not neccessary anymore because NetBeans should run on minimum JDK 8
                                String srcLevel = sourceLevel;
                                PlatformUiSupport.storePlatform(ep, updateHelper, AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, finalPlatformName, srcLevel != null ? new SpecificationVersion(srcLevel) : null);
                                helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
                                ProjectManager.getDefault().saveProject(ProjectManager.getDefault().findProject(helper.getProjectDirectory()));
                            } catch (IOException e) {
                                Exceptions.printStackTrace(e);
                            }
                        }
                    });
                }
            });
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    /**
     * Create a new application manifest file with minimal initial contents.
     * @param dir the directory to create it in
     * @param path the relative path of the file
     * @throws IOException in case of problems
     */
    private static void createManifest(FileObject dir, String path) throws IOException {
        if (dir.getFileObject(path) == null) {
            FileObject manifest = FileUtil.createData(dir, path);
            try (FileLock lock = manifest.lock();
                    OutputStream os = manifest.getOutputStream(lock); PrintWriter pw = new PrintWriter(os, true)) {
                pw.println("Manifest-Version: 1.0"); // NOI18N
                pw.println("X-COMMENT: Main-Class will be added automatically by build"); // NOI18N
                pw.println(); // safest to end in \n\n due to JRE parsing bug
            }
        }
    }
    
}
