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

package org.netbeans.modules.j2ee.earproject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntArtifactQuery;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectCreateData;
import org.netbeans.modules.j2ee.clientproject.api.AppClientProjectGenerator;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.application.Module;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.javaee.project.api.ant.AntProjectConstants;
import org.netbeans.modules.javaee.project.api.ant.DeployOnSaveUtils;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.earproject.util.EarProjectUtil;
import org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectCreateData;
import org.netbeans.modules.j2ee.ejbjarproject.api.EjbJarProjectGenerator;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.javaee.project.api.ear.EarDDGenerator;
import org.netbeans.modules.web.project.api.WebProjectCreateData;
import org.netbeans.modules.web.project.api.WebProjectUtilities;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.ProjectGenerator;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Creates a fresh EarProject from scratch or imports an existing Enterprise
 * Application.
 *
 * @author vince kraemer
 */
public final class EarProjectGenerator {

    private static final String DEFAULT_DOC_BASE_FOLDER = "src/conf"; //NOI18N
    private static final String DEFAULT_BUILD_DIR = "build"; //NOI18N
    private static final String DEFAULT_RESOURCE_FOLDER = "setup"; //NOI18N
    
    private static final String SOURCE_ROOT_REF = "${" + EarProjectProperties.SOURCE_ROOT + "}"; //NOI18N
    
    private final File prjDir;
    private final String name;
    private final Profile j2eeProfile;
    private final String serverInstanceID;
    private final String sourceLevel;
    private final FileObject prjDirFO;
    private String librariesDefinition;
    
    private EarProjectGenerator(File prjDir, FileObject prjDirFO, String name, Profile j2eeProfile,
            String serverInstanceID, String sourceLevel, String librariesDefinition) {
        this.prjDir = prjDir;
        this.prjDirFO = prjDirFO;
        this.name = name;
        this.j2eeProfile = j2eeProfile;
        this.serverInstanceID = serverInstanceID;
        // #181215: JDK 6 should be the default source/binary format for Java EE 6 projects
        // #181215: Not neccessary anymore because NetBeans should run on minimum JDK 8
        this.sourceLevel = sourceLevel;
        this.librariesDefinition = librariesDefinition;
    }
    
    /**
     * Creates a new empty Enterprise Application project.
     *
     * @param prjDir the top-level directory (need not yet exist but if it does it must be empty)
     * @param name the code name for the project
     * @return the helper object permitting it to be further customized
     * @throws IOException in case something went wrong
     */
    public static AntProjectHelper createProject(File prjDir, String name, Profile j2eeProfile,
            String serverInstanceId, String sourceLevel, String librariesDefinition) throws IOException {
        FileObject projectDir = FileUtil.createFolder(prjDir);
        final EarProjectGenerator earGen = new EarProjectGenerator(prjDir, projectDir, name, j2eeProfile,
                serverInstanceId, sourceLevel, librariesDefinition);
        final AntProjectHelper[] h = new AntProjectHelper[1];
        
        // create project in one FS atomic action:
        FileSystem fs = projectDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                AntProjectHelper helper = earGen.doCreateProject();
                h[0] = helper;
            }});
        return h[0];
    }
    
    public static AntProjectHelper importProject(File pDir, final File sDir, String name,
            Profile j2eeProfile, String serverInstanceID, final String platformName,
            String sourceLevel, final Map<FileObject, ModuleType> userModules,
            String librariesDefinition)
            throws IOException {
        FileObject projectDir = FileUtil.createFolder(pDir);
        final EarProjectGenerator earGen = new EarProjectGenerator(pDir, projectDir, name,
                j2eeProfile, serverInstanceID, sourceLevel, librariesDefinition);
        final AntProjectHelper[] h = new AntProjectHelper[1];
        
        // create project in one FS atomic action:
        FileSystem fs = projectDir.getFileSystem();
        fs.runAtomicAction(new FileSystem.AtomicAction() {
            @Override
            public void run() throws IOException {
                AntProjectHelper helper = earGen.doImportProject(sDir, userModules, platformName);
                h[0] = helper;
            }});
        return h[0];
    }
    
    private AntProjectHelper doCreateProject() throws IOException {
        final AntProjectHelper h = setupProject();
        FileObject docBase = FileUtil.createFolder(prjDirFO, DEFAULT_DOC_BASE_FOLDER);
        
        // create a default manifest
        FileUtil.copyFile(FileUtil.getConfigFile(
                "org-netbeans-modules-j2ee-earproject/MANIFEST.MF"), docBase, "MANIFEST"); // NOI18N
        
        final EarProject p = (EarProject)ProjectManager.getDefault().findProject(h.getProjectDirectory());
        final ReferenceHelper refHelper = p.getReferenceHelper();
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override
                public Void run() throws Exception {
                    EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    ep.put(EarProjectProperties.SOURCE_ROOT, "."); //NOI18N
                    ep.setProperty(EarProjectProperties.META_INF, DEFAULT_DOC_BASE_FOLDER);
                    ep.setProperty(EarProjectProperties.RESOURCE_DIR, DEFAULT_RESOURCE_FOLDER);
                    h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);        
                    ProjectManager.getDefault().saveProject(p);
                    copyRequiredLibraries(h, refHelper, serverInstanceID, j2eeProfile);
                    return null;
                }
            });
        } catch (MutexException ex) {
            Exceptions.printStackTrace(ex.getException());
        }
        EarProject earProject = p.getLookup().lookup(EarProject.class);
        assert earProject != null;
        setupDD(j2eeProfile, docBase, earProject);
        
        return h;
    }

    private static void copyRequiredLibraries(AntProjectHelper h, ReferenceHelper rh,
            String serverInstanceId, Profile j2eeProfile) throws IOException {

        if (!h.isSharableProject()) {
            return;
        }
        String libraryName = null;
        if (j2eeProfile.equals(Profile.JAVA_EE_6_FULL) || j2eeProfile.equals(Profile.JAVA_EE_6_WEB)) {
            libraryName = AntProjectConstants.ENDORSED_LIBRARY_NAME_6;
        }
        if (j2eeProfile.equals(Profile.JAVA_EE_7_FULL) || j2eeProfile.equals(Profile.JAVA_EE_7_WEB)) {
            libraryName = AntProjectConstants.ENDORSED_LIBRARY_NAME_7;
        }
        if (libraryName != null) {
            if (rh.getProjectLibraryManager().getLibrary(libraryName) == null) { // NOI18N
                rh.copyLibrary(LibraryManager.getDefault().getLibrary(libraryName)); // NOI18N
            }
        }
        SharabilityUtility.makeSureProjectHasCopyLibsLibrary(h, rh);
     }
    
    private AntProjectHelper doImportProject(final File srcPrjDir,
            Map<FileObject, ModuleType> userModules,
            String platformName) throws IOException {
        FileObject srcPrjDirFO = FileUtil.toFileObject(FileUtil.normalizeFile(srcPrjDir));
        FileObject docBase = FileUtil.createFolder(srcPrjDirFO, DEFAULT_DOC_BASE_FOLDER);
        
        AntProjectHelper earHelper = setupProject();
        ReferenceHelper referenceHelper = new ReferenceHelper(earHelper,
                earHelper.createAuxiliaryConfiguration(), earHelper.getStandardPropertyEvaluator());
        String sourceRoot = referenceHelper.createForeignFileReference(srcPrjDir, null);
        String metaInf = createFileReference(referenceHelper, srcPrjDirFO, docBase);
        
        EditableProperties ep = earHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(EarProjectProperties.SOURCE_ROOT, sourceRoot);
        ep.setProperty(EarProjectProperties.META_INF, metaInf);
        earHelper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        
        FileObject earDirFO = earHelper.getProjectDirectory();
        EarProject earProject = ProjectManager.getDefault().findProject(earDirFO).getLookup().lookup(EarProject.class);
        
        if (null != earProject) {
            Application app = null;
            try {
                FileObject appXml = earProject.getAppModule().getDeploymentDescriptor();
                FileObject fileBeingCopied = null;
                if (null != appXml) {
                    // make a backup copy of the application.xml and its siblings
                    Enumeration<? extends FileObject> filesToBackup = appXml.getParent().getChildren(false);
                    while (null != filesToBackup && filesToBackup.hasMoreElements()) {
                        fileBeingCopied = (FileObject) filesToBackup.nextElement();
                        if (fileBeingCopied.isData() && fileBeingCopied.canRead()) {
                            try {
                                FileUtil.copyFile(fileBeingCopied,
                                        appXml.getParent(),
                                        "original_"+fileBeingCopied.getName(), // NOI18N
                                        fileBeingCopied.getExt());
                            } catch (IOException ioe) {
                                // this is not fatal
                            }
                        }
                    }
                    app = DDProvider.getDefault().getDDRoot(appXml);
                    Module m[] = app.getModule();
                    if (null != m && m.length > 0) {
                        // make sure the config object has told us what to listen to...
                        earProject.getAppModule().getConfigSupport().ensureConfigurationReady();
                        // delete the modules
                        for (int k = 0; k < m.length; k++) {
                            app.removeModule(m[k]);
                        }
                        if (EarProjectUtil.isDDWritable(earProject)) {
                            app.write(earProject.getAppModule().getDeploymentDescriptor());
                        }
                        // notify the user here....
                        DialogDisplayer.getDefault().notify(
                                new NotifyDescriptor.Message(NbBundle.getMessage(EarProjectGenerator.class, "MESSAGE_CheckContextRoots"),
                                NotifyDescriptor.WARNING_MESSAGE));
                    }
                }
            } catch (IOException ioe) {
                Logger.getLogger(EarProjectGenerator.class.getName()).log(Level.INFO, ioe.getLocalizedMessage());
            }
        
            setupDD(j2eeProfile, docBase, earProject);

            if (userModules == null || userModules.isEmpty()) {
                userModules = ModuleType.detectModules(srcPrjDirFO);
            }
            addUserModules(earProject, userModules, platformName, earHelper, earProject);

            // XXX all web module URI-to-ContextRoot mapping should happen here

            ProjectManager.getDefault().saveProject(earProject);

            earProject.getAppModule().getConfigSupport().createInitialConfiguration();
            if (sourceLevel != null) {
                EarProjectGenerator.setPlatformSourceLevel(earHelper, sourceLevel);
            }
        }
        
        return earHelper;
    }
    
    private void addUserModules(EarProject p, final Map<FileObject, ModuleType> userModules,
            final String platformName, final AntProjectHelper h, final EarProject earProject) throws IOException {
        Set<Project> ejbs = new HashSet<Project>();
        Set<Project> webAndCars = new HashSet<Project>();
        for (Map.Entry<FileObject, ModuleType> entry : userModules.entrySet()) {
            FileObject subprojectDir = entry.getKey();
            ModuleType type = entry.getValue();
            Project subProject = addModule(p, type, platformName, subprojectDir);
            assert subProject != null : "Directory " + subprojectDir + " does not contain valid project";
            switch (type) {
                case EJB:
                    ejbs.add(subProject);
                    break;
                case WEB:
                case CLIENT:
                    webAndCars.add(subProject);
                    break;
                default:
                    assert false : "Unknown module type: " + type;
            }
        }
        Project[] webAndCarsArray = webAndCars.toArray(new Project[0]);
        for (Project ejb : ejbs) {
            addEJBToClassPaths(ejb, webAndCarsArray); // #74123
        }
    }
    
    /**
     * Adds EJB's artifact to Web and Application Client projects' classpaths.
     *
     * @param ejbJarProject must not be <code>null</code>
     * @param projects may contains also <code>null</code> elements
     */
    public static void addEJBToClassPaths(final Project ejbJarProject,
            final Project... projects) throws IOException {
        assert ejbJarProject != null;
        AntArtifact[] ejbArtifacts = AntArtifactQuery.findArtifactsByType(
                ejbJarProject, JavaProjectConstants.ARTIFACT_TYPE_JAR);
        for (AntArtifact artifact : ejbArtifacts) {
            for (Project project : projects) {
                if (project == null) {
                    continue;
                }
                URI[] locations = artifact.getArtifactLocations();
                if (locations.length > 0) { // sanity check
                    SourceGroup sgs[] = ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
                    if (sgs.length > 0) {
                        ProjectClassPathModifier.addAntArtifacts(new AntArtifact[]{artifact},
                            new URI[]{BaseUtilities.normalizeURI(locations[0])}, sgs[0].getRootFolder(), JavaClassPathConstants.COMPILE_ONLY);
                    }
                }
            }
        }
    }
    
    private Project addModule(EarProject p, final ModuleType type, 
            final String platformName, final FileObject subprojectRoot)
            throws IllegalArgumentException, IOException {
        
        // #87604 & #143772 - check first whether the module is not already a project
        Project existingProject = getExistingJ2EEModuleProject(subprojectRoot);
        if (existingProject != null) {
            EarProjectProperties.addJ2eeSubprojects(p, new Project[] { existingProject });
            return existingProject;
        }
        
        // module is not a project
        FileObject javaRoot = getJavaRoot(subprojectRoot);
        File srcFolders[] = getSourceFolders(javaRoot);
        File subProjDir = FileUtil.normalizeFile(
                new File(prjDir, subprojectRoot.getNameExt()));
        AntProjectHelper subProjHelper = null;
        switch (type) {
            case WEB:
                subProjHelper = addWebModule(subprojectRoot, srcFolders, subProjDir, platformName);
                break;
            case EJB:
                subProjHelper = addEJBModule(javaRoot, subprojectRoot, subProjDir, platformName);
                break;
            case CLIENT:
                subProjHelper = addAppClientModule(javaRoot, subprojectRoot, subProjDir, platformName);
                break;
            default:
                assert false : "Unknown module type: " + type;
        }
        Project subProject = null;
        if (null != subProjHelper) {
            subProject = ProjectManager.getDefault().findProject(
                    subProjHelper.getProjectDirectory());
            EarProjectProperties.addJ2eeSubprojects(p, new Project[] { subProject });
        }
        return subProject;
    }
    
    // get existing project but only java ee module
    private Project getExistingJ2EEModuleProject(final FileObject projectDirectory) throws IOException {
        Project project = ProjectManager.getDefault().findProject(projectDirectory);
        if (EarProjectUtil.isJavaEEModule(project)) {
            return project;
        }
        return null;
    }

    private AntProjectHelper addAppClientModule(final FileObject javaRoot, final FileObject subprojectRoot, final File subProjDir, final String platformName) throws IOException {
        FileObject docBaseFO = FileUtil.createFolder(subprojectRoot, DEFAULT_DOC_BASE_FOLDER);
        File docBase = FileUtil.toFile(docBaseFO);

        AppClientProjectCreateData createData = new AppClientProjectCreateData();
        createData.setProjectDir(subProjDir);
        createData.setName(subprojectRoot.getName());
        createData.setSourceFolders(new File[] { FileUtil.toFile(javaRoot) });
        createData.setTestFolders(new File[0]);
        createData.setConfFolder(docBase);
        createData.setJavaEEProfile(getAcceptableProfile(j2eeProfile, serverInstanceID, J2eeModule.Type.CAR));
        createData.setServerInstanceID(serverInstanceID);

        AntProjectHelper subProjHelper = AppClientProjectGenerator.importProject(createData);
        if (platformName != null || sourceLevel != null) {
            AppClientProjectGenerator.setPlatform(subProjHelper, platformName, sourceLevel);
        }
        return subProjHelper;
    }
    
    private AntProjectHelper addEJBModule(final FileObject javaRoot, final FileObject subprojectRoot, final File subProjDir, final String platformName) throws IOException {
        FileObject docBaseFO = FileUtil.createFolder(subprojectRoot, DEFAULT_DOC_BASE_FOLDER);
        File docBase = FileUtil.toFile(docBaseFO);

        EjbJarProjectCreateData createData = new EjbJarProjectCreateData();
        createData.setProjectDir(subProjDir);
        createData.setName(subprojectRoot.getName());
        createData.setSourceFolders(new File[] {FileUtil.toFile(javaRoot)});
        createData.setTestFolders(new File[0]);
        createData.setConfigFilesBase(docBase);
        createData.setJavaEEProfile(getAcceptableProfile(j2eeProfile, serverInstanceID, J2eeModule.Type.EJB));
        createData.setServerInstanceID(serverInstanceID);

        AntProjectHelper subProjHelper = EjbJarProjectGenerator.importProject(createData);
        if (platformName != null || sourceLevel != null) {
            EjbJarProjectGenerator.setPlatform(subProjHelper, platformName, sourceLevel);
        }
        return subProjHelper;
    }
    
    private AntProjectHelper addWebModule(final FileObject subprojectRoot, final File srcFolders[], final File subProjDir, final String platformName) throws IOException {
        WebProjectCreateData createData = new WebProjectCreateData();
        createData.setProjectDir(subProjDir);
        createData.setName(subprojectRoot.getName());
        createData.setWebModuleFO(subprojectRoot);
        createData.setSourceFolders(srcFolders);
        createData.setTestFolders(new File[0]);
        createData.setDocBase(FileUtil.createFolder(subprojectRoot, "web")); //NOI18N
        createData.setLibFolder(null);
        createData.setJavaEEProfile(getAcceptableProfile(j2eeProfile, serverInstanceID, J2eeModule.Type.WAR));
        createData.setServerInstanceID(serverInstanceID);
        createData.setBuildfile("build.xml"); //NOI18N
        createData.setJavaPlatformName(platformName);
        createData.setSourceLevel(sourceLevel);
        // # 109128, BluePrints structure
        createData.setWebInfFolder(subprojectRoot.getFileObject("web/WEB-INF"));
        return WebProjectUtilities.importProject(createData);
    }
    
    static FileObject setupDD(final Profile j2eeProfile, final FileObject docBase,
            final EarProject earProject) throws IOException {
        return EarDDGenerator.setupDD(earProject, false);
    }

    /** Check that the J2EE version requested for the EAR is also supported for
     * the module type and if not suggest a different version.
     * For now the only check is to use J2EE 1.4 if JavaEE5 is not supported.
     * Otherwise use the requestedVersion.
     */
    public static Profile getAcceptableProfile(Profile requestedProfile, String serverInstanceID, J2eeModule.Type moduleType) {
        J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceID);
        Set<Profile> profiles = platform.getSupportedProfiles(moduleType);
        if (!profiles.contains(requestedProfile) && (profiles.contains(Profile.J2EE_14))) {
            return Profile.J2EE_14;
        }
        return requestedProfile;
    }
    
    private static String relativePath(FileObject parent, FileObject child) {
        if (child.equals(parent)) {
            return "";
        }
        if (!FileUtil.isParentOf(parent, child)) {
            throw new IllegalArgumentException("Cannot find relative path, " + parent + " is not parent of " + child); // NOI18N
        }
        return child.getPath().substring(parent.getPath().length() + 1);
    }
    
    private AntProjectHelper setupProject() throws IOException {

        EarProjectUtil.logUI(NbBundle.getBundle(EarProjectGenerator.class), "UI_EAR_PROJECT_CREATE_SHARABILITY", // NOI18N
                new Object[]{(librariesDefinition != null), Boolean.FALSE});

        AntProjectHelper h = ProjectGenerator.createProject(prjDirFO, EarProjectType.TYPE, librariesDefinition);
        EarProject p = (EarProject)ProjectManager.getDefault().findProject(prjDirFO);
        Element data = h.getPrimaryConfigurationData(true);
        Document doc = data.getOwnerDocument();
        Element nameEl = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
        nameEl.appendChild(doc.createTextNode(name));
        data.appendChild(nameEl);
        Element minant = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "minimum-ant-version"); // NOI18N
        minant.appendChild(doc.createTextNode("1.6.5")); // NOI18N
        data.appendChild(minant);
        
        Element wmLibs = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, EarProjectProperties.TAG_WEB_MODULE_LIBRARIES); //NOI18N
        data.appendChild(wmLibs);
        
        Element addLibs = doc.createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, EarProjectProperties.TAG_WEB_MODULE__ADDITIONAL_LIBRARIES); //NOI18N
        data.appendChild(addLibs);
        
        h.putPrimaryConfigurationData(data, true);
        EditableProperties ep = h.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        // XXX the following just for testing, TBD:
        ep.setProperty(EarProjectProperties.DIST_DIR, "dist"); // NOI18N
        ep.setProperty(EarProjectProperties.DIST_JAR, "${" + EarProjectProperties.DIST_DIR + "}/${" + EarProjectProperties.JAR_NAME + "}"); // NOI18N
        
        ep.setProperty(EarProjectProperties.J2EE_PLATFORM, j2eeProfile.toPropertiesString());
        
        ep.setProperty(EarProjectProperties.JAR_NAME, name + ".ear"); // NOI18N
        ep.setProperty(EarProjectProperties.JAR_COMPRESS, "false"); // NOI18N
        ep.setProperty(EarProjectProperties.JAR_CONTENT_ADDITIONAL, "");
        
        ep.setProperty(EarProjectProperties.CLIENT_MODULE_URI, "");
        ep.setProperty(EarProjectProperties.LAUNCH_URL_RELATIVE, "");
        ep.setProperty(EarProjectProperties.DISPLAY_BROWSER, "true"); // NOI18N

        // deploy on save since nb 6.5
        ep.setProperty(EarProjectProperties.J2EE_COMPILE_ON_SAVE, "true");
        ep.setProperty(EarProjectProperties.J2EE_DEPLOY_ON_SAVE, DeployOnSaveUtils.isDeployOnSaveSupported(serverInstanceID));

        String srcLevel = sourceLevel;
        if (srcLevel == null) {
            JavaPlatform defaultPlatform = JavaPlatformManager.getDefault().getDefaultPlatform();
            SpecificationVersion v = defaultPlatform.getSpecification().getVersion();
            srcLevel = v.toString();
            // #89131: these levels are not actually distinct from 1.5.
            // #181215: JDK 6 should be the default source/binary format for Java EE 6 projects
            // #181215: Not neccessary anymore because NetBeans should run on minimum JDK 8
        }
        ep.setProperty(EarProjectProperties.JAVAC_SOURCE, srcLevel); //NOI18N
        ep.setProperty(EarProjectProperties.JAVAC_DEBUG, "true"); // NOI18N
        ep.setProperty(EarProjectProperties.JAVAC_DEPRECATION, "false"); // NOI18N
        
        //xxx Default should be 1.2
        //http://projects.netbeans.org/buildsys/j2se-project-ui-spec.html#Build_Compiling_Sources
        ep.setProperty(EarProjectProperties.JAVAC_TARGET, srcLevel); //NOI18N
        
        ep.setProperty(EarProjectProperties.BUILD_DIR, DEFAULT_BUILD_DIR);
        ep.setProperty(EarProjectProperties.BUILD_GENERATED_DIR, "${"+EarProjectProperties.BUILD_DIR+"}/generated"); // NOI18N
        ep.setProperty(EarProjectProperties.BUILD_CLASSES_EXCLUDES, "**/*.java,**/*.form,**/.nbattrs"); // NOI18N
        ep.setProperty(EarProjectProperties.NO_DEPENDENCIES, "false"); // NOI18N
        ep.setProperty(EarProjectProperties.JAVA_PLATFORM, "default_platform"); // NOI18N
        ep.setProperty(EarProjectProperties.DEBUG_CLASSPATH,
                "${"+EarProjectProperties.JAVAC_CLASSPATH+"}::${"+ // NOI18N
                EarProjectProperties.JAR_CONTENT_ADDITIONAL+"}:${"+ // NOI18N
                EarProjectProperties.RUN_CLASSPATH+"}"); // NOI18N

        if (j2eeProfile.equals(Profile.JAVA_EE_6_FULL) || j2eeProfile.equals(Profile.JAVA_EE_6_WEB)) {
            ep.setProperty(ProjectProperties.ENDORSED_CLASSPATH, new String[]{AntProjectConstants.ENDORSED_LIBRARY_CLASSPATH_6});
        }
        if (j2eeProfile.equals(Profile.JAVA_EE_7_FULL) || j2eeProfile.equals(Profile.JAVA_EE_7_WEB)) {
            ep.setProperty(ProjectProperties.ENDORSED_CLASSPATH, new String[]{AntProjectConstants.ENDORSED_LIBRARY_CLASSPATH_7});
        }
        
        EditableProperties privateEP = h.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);

        J2EEProjectProperties.setServerProperties(ep, privateEP, null, null, serverInstanceID, j2eeProfile, J2eeModule.Type.EAR);
        
        h.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        h.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, privateEP);
        ProjectManager.getDefault().saveProject(p);
        return h;
    }
    
    private String createFileReference(ReferenceHelper refHelper,
            FileObject sourceprojectFO, FileObject referencedFO) {
        if (FileUtil.isParentOf(prjDirFO, referencedFO)) {
            return relativePath(prjDirFO, referencedFO);
        } else if (FileUtil.isParentOf(sourceprojectFO, referencedFO)) {
            String s = relativePath(sourceprojectFO, referencedFO);
            return s.length() > 0 ? SOURCE_ROOT_REF + '/' + s : SOURCE_ROOT_REF;
        } else {
            return refHelper.createForeignFileReference(FileUtil.toFile(referencedFO), null);
        }
    }
    
    public static void setPlatformSourceLevel(final AntProjectHelper helper, final String sourceLevel) {
        FileObject projectDir = helper.getProjectDirectory();
        if (projectDir == null) {
            return;
        }
        try {
            projectDir.getFileSystem().runAtomicAction(new AtomicAction() {
                @Override
                public void run() throws IOException {
                    ProjectManager.mutex().writeAccess(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                // #89131: these levels are not actually distinct from 1.5.
                                String srcLevel = sourceLevel;
                                // #181215: JDK 6 should be the default source/binary format for Java EE 6 projects
                                // #181215: Not neccessary anymore because NetBeans should run on minimum JDK 8
                                ep.setProperty(EarProjectProperties.JAVAC_SOURCE, srcLevel);
                                ep.setProperty(EarProjectProperties.JAVAC_TARGET, srcLevel);
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
    
    private FileObject getJavaRoot(final FileObject moduleRoot) throws IOException {
        FileObject javaRoot = moduleRoot.getFileObject("src/java"); // NOI18N
        // XXX this is a hack. Remove once 56487 is resolved
        if (null == javaRoot) {
            FileObject srcDir = moduleRoot.getFileObject("src"); // NOI18N
            if (null == srcDir) {
                srcDir = moduleRoot.createFolder("src"); // NOI18N
            }
            javaRoot = srcDir.createFolder("java"); // NOI18N
        }
        return javaRoot;
        // end hack for 56487
    }
    
    private File[] getSourceFolders(final FileObject javaRoot) {
        return null == javaRoot ? new File[0] :
            new File[] { FileUtil.toFile(javaRoot) };
    }
    
}
