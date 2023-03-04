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

package org.netbeans.modules.j2ee.earproject;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Icon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ant.AntBuildExtender;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.api.ejbjar.Ear;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.common.SharabilityUtility;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.javaee.project.api.ant.ui.J2EEProjectProperties;
import org.netbeans.modules.j2ee.common.ui.BrokenServerSupport;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.InstanceRemovedException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.ui.ProjectProblems;
import org.netbeans.modules.j2ee.common.dd.DDHelper;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.javaee.project.spi.JavaEEProjectSettingsImplementation;
import org.netbeans.modules.javaee.project.api.ant.DeployOnSaveUtils;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.Type;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.earproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.j2ee.earproject.classpath.ClassPathSupportCallbackImpl;
import org.netbeans.modules.j2ee.earproject.ui.IconBaseProvider;
import org.netbeans.modules.j2ee.earproject.ui.J2eeArchiveLogicalViewProvider;
import org.netbeans.modules.j2ee.earproject.ui.customizer.CustomizerProviderImpl;
import org.netbeans.modules.j2ee.earproject.ui.customizer.EarProjectProperties;
import org.netbeans.modules.j2ee.earproject.util.EarProjectUtil;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation2;
import org.netbeans.modules.j2ee.spi.ejbjar.EjbJarFactory;
import org.netbeans.modules.java.api.common.Roots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.api.common.queries.QuerySupport;
import org.netbeans.modules.javaee.project.api.ClientSideDevelopmentSupport;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettingConstants;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.javaee.project.api.problems.PlatformUpdatedCallBackImpl;
import org.netbeans.modules.javaee.project.spi.ear.EarDDGeneratorImplementation;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.spi.java.project.support.LookupMergerSupport;
import org.netbeans.spi.java.project.support.ui.BrokenReferencesSupport;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.ant.AntBuildExtenderFactory;
import org.netbeans.spi.project.ant.AntBuildExtenderImplementation;
import org.netbeans.spi.project.support.LookupProviderSupport;
import org.netbeans.spi.project.support.ant.AntBasedProjectRegistration;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.netbeans.spi.project.support.ant.ProjectXmlSavedHook;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.netbeans.spi.project.ui.support.UILookupMergerSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem.AtomicAction;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Represents an Enterprise Application project.
 *
 * This is the project api centric view of the enterprise application.
 *
 * @author vince kraemer
 */
@AntBasedProjectRegistration(
    iconResource="org/netbeans/modules/j2ee/earproject/ui/resources/projectIcon.gif",
    type=EarProjectType.TYPE,
    sharedNamespace=EarProjectType.PROJECT_CONFIGURATION_NAMESPACE,
    privateNamespace=EarProjectType.PRIVATE_CONFIGURATION_NAMESPACE
)
public final class EarProject implements Project, AntProjectListener {

    private static Logger LOGGER = Logger.getLogger(EarProject.class.getName());
    
    private final Icon EAR_PROJECT_ICON = ImageUtilities.loadImageIcon("org/netbeans/modules/j2ee/earproject/ui/resources/projectIcon.gif", false); // NOI18N
    public static final String ARTIFACT_TYPE_EAR = "ear";
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator eval;
    private final ReferenceHelper refHelper;
    private final GeneratedFilesHelper genFilesHelper;
    private final Lookup lookup;
    private final ProjectEar appModule;
    private final Ear ear;
    private final UpdateHelper updateHelper;
    private final UpdateProjectImpl updateProject;
    private final ClassPathProviderImpl cpProvider;
    private PropertyChangeListener j2eePlatformListener;
    private EarProjectLookup earLookup;
    private final ClientSideDevelopmentSupport easelSupport;
    
    private AntBuildExtender buildExtender;
    public ClassPathSupport cs;
            
    public EarProject(final AntProjectHelper helper) throws IOException {
        this.helper = helper;
        eval = createEvaluator();
        AuxiliaryConfiguration aux = helper.createAuxiliaryConfiguration();
        refHelper = new ReferenceHelper(helper, aux, helper.getStandardPropertyEvaluator());
        buildExtender = AntBuildExtenderFactory.createAntExtender(new EarExtenderImplementation());
        genFilesHelper = new GeneratedFilesHelper(helper,buildExtender);
        appModule = new ProjectEar(this);
        ear = EjbJarFactory.createEar(new EarImpl2(appModule));
        updateProject = new UpdateProjectImpl(this, this.helper, aux);
        updateHelper = new UpdateHelper(updateProject, helper);
        cpProvider = new ClassPathProviderImpl(helper, evaluator());
        easelSupport = ClientSideDevelopmentSupport.createInstance(this, EarProjectType.TYPE, EarProjectUtil.USG_LOGGER_NAME);
        helper.addAntProjectListener(new AntProjectListener() {

            @Override
            public void configurationXmlChanged(AntProjectEvent ev) {
            }

            @Override
            public void propertiesChanged(AntProjectEvent ev) {
                updateEaselWebProject();
            }
        });
        lookup = createLookup(aux, cpProvider);
        cs = new ClassPathSupport( eval, refHelper, 
                updateHelper.getAntProjectHelper(), updateHelper, new ClassPathSupportCallbackImpl(helper));
        updateEaselWebProject();
    }

    private void updateEaselWebProject() {
        Project p = null;
        List<ClassPathSupport.Item> vcpis = EarProjectProperties.getJarContentAdditional(this);
        for (ClassPathSupport.Item item : vcpis) {
            if (item.getType() != ClassPathSupport.Item.TYPE_ARTIFACT || item.getArtifact() == null) {
                continue;
            }
            Project vcpiProject = item.getArtifact().getProject();
            J2eeModuleProvider jmp = vcpiProject.getLookup().lookup(J2eeModuleProvider.class);
            if (null != jmp && jmp.getJ2eeModule().getType() == Type.WAR) {
                p = vcpiProject;
                break;
            }
        }
        easelSupport.setWebProject(p);
    }

    public ClassPathSupport getClassPathSupport() {
        return cs;
    }
    
    public UpdateHelper getUpdateHelper() {
        return updateHelper;
    }
    
    public FileObject getProjectDirectory() {
        return helper.getProjectDirectory();
    }

    public ClientSideDevelopmentSupport getEaselSupport() {
        return easelSupport;
    }

    public ReferenceHelper getReferenceHelper() {
        return refHelper;
    }
    
    @Override
    public String toString() {
        return "EarProject[" + getProjectDirectory() + "]"; // NOI18N
    }
    
    private PropertyEvaluator createEvaluator() {
        // XXX might need to use a custom evaluator to handle active platform substitutions... TBD
        return helper.getStandardPropertyEvaluator();
    }
    
    public PropertyEvaluator evaluator() {
        return eval;
    }
    
    public Lookup getLookup() {
        return lookup;
    }
    
    public AntProjectHelper getAntProjectHelper() {
        return helper;
    }
    
    private Lookup createLookup(AuxiliaryConfiguration aux, ClassPathProviderImpl cpProvider) {
        SubprojectProvider spp = refHelper.createSubprojectProvider();
        final J2eeArchiveLogicalViewProvider lvp = new J2eeArchiveLogicalViewProvider(this, updateHelper, evaluator(), refHelper, appModule);
        Lookup base = Lookups.fixed(new Object[] {
            QuerySupport.createProjectInformation(helper, this, EAR_PROJECT_ICON),
            aux,
            spp,
            helper.createAuxiliaryProperties(),
            new ProjectEarProvider(),
            appModule, //implements J2eeModuleProvider
            // FIXME this is just fallback for code searching for the old SPI in lookup
            // remove in next release
            new EarImpl(ear, appModule),
            new EarActionProvider(this, updateHelper),
            lvp,
            new MyIconBaseProvider(),
            new CustomizerProviderImpl(this, helper, refHelper),
            LookupMergerSupport.createClassPathProviderMerger(cpProvider),
            new ProjectXmlSavedHookImpl(),
            UILookupMergerSupport.createProjectOpenHookMerger(new ProjectOpenedHookImpl()),
            QuerySupport.createSources(this, helper, evaluator(),
                    Roots.propertyBased(new String[]{EarProjectProperties.META_INF}, new String[]{NbBundle.getMessage(EarProject.class, "LBL_Node_ConfigBase")}, false, null, null)),
            new RecommendedTemplatesImpl(),
            helper.createSharabilityQuery(evaluator(),
                    new String[] {"${"+EarProjectProperties.SOURCE_ROOT+"}"}, // NOI18N
                    new String[] {
                "${"+ProjectProperties.BUILD_DIR+"}", // NOI18N
                "${"+EarProjectProperties.DIST_DIR+"}"} // NOI18N
            ),
            this,
            new EarProjectOperations(this),
            new AntArtifactProviderImpl(),
            UILookupMergerSupport.createPrivilegedTemplatesMerger(),
            UILookupMergerSupport.createRecommendedTemplatesMerger(),
            LookupProviderSupport.createSourcesMerger(),
            buildExtender,
            new SourceForBinaryQueryImpl(this),
            new JavaEEProjectSettingsImpl(this),
            new EarDDGeneratorImpl(this),
            easelSupport,
            BrokenReferencesSupport.createReferenceProblemsProvider(helper, refHelper, eval, lvp.getBreakableProperties(), lvp.getPlatformProperties()),
            BrokenReferencesSupport.createPlatformVersionProblemProvider(helper, eval, PlatformUpdatedCallBackImpl.create(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, updateHelper, false, new String[]{"name", "minimum-ant-version","use-manifest"}), JavaPlatform.getDefault().getSpecification().getName(), ProjectProperties.PLATFORM_ACTIVE, ProjectProperties.JAVAC_SOURCE, ProjectProperties.JAVAC_TARGET),
            UILookupMergerSupport.createProjectProblemsProviderMerger(),
        });
        earLookup = new EarProjectLookup(this, base, new WebBrowserProvider(this));
        evaluator().addPropertyChangeListener(earLookup);
        return LookupProviderSupport.createCompositeLookup(earLookup, "Projects/org-netbeans-modules-j2ee-earproject/Lookup"); //NOI18N
    }

    private static class EarProjectLookup extends ProxyLookup implements PropertyChangeListener{
        private Lookup base;
        private EarProject project;
        private WebBrowserProvider webProvider;

        public EarProjectLookup(EarProject project, Lookup base, WebBrowserProvider webProvider) {
            super(base);
            this.project = project;
            this.base = base;
            this.webProvider = webProvider;
            updateLookup();
        }

        private void updateLookup() {
            if ("true".equals(project.evaluator().getProperty(EarProjectProperties.DISPLAY_BROWSER)) &&
                project.evaluator().getProperty(EarProjectProperties.APPLICATION_CLIENT) == null) {
                setLookups(base, Lookups.singleton(webProvider));
            } else {
                setLookups(base);
            }
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(EarProjectProperties.DISPLAY_BROWSER) ||
                evt.getPropertyName().equals(EarProjectProperties.APPLICATION_CLIENT)) {
                updateLookup();
            }
        }
    }

    public void configurationXmlChanged(AntProjectEvent ev) {
    }
    
    public void propertiesChanged(AntProjectEvent ev) {
        // currently ignored
        //TODO: should not be ignored!
    }
    
    public String getBuildXmlName() {
        String storedName = helper.getStandardPropertyEvaluator().getProperty(EarProjectProperties.BUILD_FILE);
        return storedName == null ? GeneratedFilesHelper.BUILD_XML_PATH : storedName;
    }
    
    // Package private methods -------------------------------------------------
    
    public ProjectEar getAppModule() {
        return appModule;
    }
    
    public Ear getEar() {
        return ear;
    }
    
    /** Store configured project name. */
    public void setName(final String name) {
        ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
            public Void run() {
                Element data = helper.getPrimaryConfigurationData(true);
                // XXX replace by XMLUtil when that has findElement, findText, etc.
                NodeList nl = data.getElementsByTagNameNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                Element nameEl;
                if (nl.getLength() == 1) {
                    nameEl = (Element) nl.item(0);
                    NodeList deadKids = nameEl.getChildNodes();
                    while (deadKids.getLength() > 0) {
                        nameEl.removeChild(deadKids.item(0));
                    }
                } else {
                    nameEl = data.getOwnerDocument().createElementNS(EarProjectType.PROJECT_CONFIGURATION_NAMESPACE, "name"); // NOI18N
                    data.insertBefore(nameEl, /* OK if null */data.getChildNodes().item(0));
                }
                nameEl.appendChild(data.getOwnerDocument().createTextNode(name));
                helper.putPrimaryConfigurationData(data, true);
                return null;
            }
        });
    }
    
    public void registerJ2eePlatformListener(final J2eePlatform platform) {
        // listen to classpath changes
        j2eePlatformListener = new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals(J2eePlatform.PROP_CLASSPATH)) {
                    ProjectManager.mutex().writeAccess(new Mutex.Action<Void>() {
                        public Void run() {
                            EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            EditableProperties projectProps = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                                Map<String, String> roots = J2EEProjectProperties.extractPlatformLibrariesRoot(platform);
                                String classpath = J2EEProjectProperties.toClasspathString(platform.getClasspathEntries(), roots);
                                ep.setProperty(J2EEProjectProperties.J2EE_PLATFORM_CLASSPATH, classpath);
                            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                            try {
                                ProjectManager.getDefault().saveProject(EarProject.this);
                            } catch (IOException e) {
                                Exceptions.printStackTrace(e);
                            }
                            return null;
                        }
                    });
                }
            }
        };
        platform.addPropertyChangeListener(j2eePlatformListener);
    }
    
    public void unregisterJ2eePlatformListener(J2eePlatform platform) {
        if (j2eePlatformListener != null) {
            platform.removePropertyChangeListener(j2eePlatformListener);
        }
    }
    
    // Private innerclasses ----------------------------------------------------
    private final class ProjectXmlSavedHookImpl extends ProjectXmlSavedHook {
        
        ProjectXmlSavedHookImpl() {}
        
        protected void projectXmlSaved() throws IOException {
            genFilesHelper.refreshBuildScript(
                    GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                    EarProject.class.getResource("resources/build-impl.xsl"),
                    false);
            genFilesHelper.refreshBuildScript(
                    getBuildXmlName(),
                    EarProject.class.getResource("resources/build.xsl"),
                    false);
        }
        
    }
    
    /** Package-private for unit tests only. */
    final class ProjectOpenedHookImpl extends ProjectOpenedHook {
        
        ProjectOpenedHookImpl() {}
        
        protected void projectOpened() {
            helper.getStandardPropertyEvaluator().addPropertyChangeListener(EarProject.this.appModule);

            J2eeArchiveLogicalViewProvider logicalViewProvider = EarProject.this.getLookup().lookup(J2eeArchiveLogicalViewProvider.class);
            if (logicalViewProvider != null) {
                logicalViewProvider.initialize();
            }

            try {
                getAppModule().setModules(EarProjectProperties.getModuleMap(EarProject.this));
                // Check up on build scripts.
                genFilesHelper.refreshBuildScript(
                        GeneratedFilesHelper.BUILD_IMPL_XML_PATH,
                        EarProject.class.getResource("resources/build-impl.xsl"),
                        true);
                genFilesHelper.refreshBuildScript(
                        getBuildXmlName(),
                        EarProject.class.getResource("resources/build.xsl"),
                        true);
            } catch (IOException e) {
                LOGGER.log(Level.INFO, null, e);
            }


            // Register copy on save support
            try {
                getAppModule().copyOnSaveSupport.initialize();
            }
            catch (FileStateInvalidException e) {
                LOGGER.log(Level.INFO, null, e);
            }

            // register project's classpaths to GlobalPathRegistry
            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            
            try {
                getProjectDirectory().getFileSystem().runAtomicAction(new AtomicAction() {
                    public void run() throws IOException {
                        ProjectManager.mutex().writeAccess(new Runnable() {
                            public void run() {
                                updateProject();
                            }
                        });
                    }
                });
                
            } catch (IOException e ) {
                Exceptions.printStackTrace(e);
            }
            
            String compileOnSave = EarProject.this.getUpdateHelper().
                    getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).getProperty(EarProjectProperties.J2EE_COMPILE_ON_SAVE);
            if (Boolean.parseBoolean(compileOnSave)) {
                Deployment.getDefault().enableCompileOnSaveSupport(appModule);
            }
            
            if (logicalViewProvider != null &&  logicalViewProvider.hasBrokenLinks()) {
                ProjectProblems.showAlert(EarProject.this);
            }

            String servInstID = EarProject.this.getUpdateHelper().
                    getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).
                    getProperty(EarProjectProperties.J2EE_SERVER_INSTANCE);
            J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
            String serverType = null;
            if (platform != null) {
                // updates j2ee.platform.cp & wscompile.cp & reg. j2ee platform listener
                EarProjectProperties.setServerInstance(EarProject.this, EarProject.this.updateHelper, servInstID);
            } else {
                // if there is some server instance of the type which was used
                // previously do not ask and use it
                serverType = EarProject.this.getUpdateHelper().
                        getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).
                        getProperty(EarProjectProperties.J2EE_SERVER_TYPE);
                if (serverType != null) {
                    String instanceID = J2EEProjectProperties.getMatchingInstance(serverType, Type.EAR, EarProject.this.getJ2eeProfile());
                    if (instanceID != null) {
                        EarProjectProperties.setServerInstance(EarProject.this, EarProject.this.updateHelper, instanceID);
                        platform = Deployment.getDefault().getJ2eePlatform(instanceID);
                    }
                }
                if (platform == null) {
                    BrokenServerSupport.showAlert();
                }
            }

            // initialize the server configuration
            // it MUST BE called AFTER classpaths are registered to GlobalPathRegistry
            // and after server resolve!!
            // DDProvider (used here) needs classpath set correctly when resolving Java Extents for annotations
            J2eeModuleProvider pwm = EarProject.this.getLookup().lookup(J2eeModuleProvider.class);
            pwm.getConfigSupport().ensureConfigurationReady();


            // the only purpose of below code is to force deployment descriptor
            // creation if necesary (that is if JEE 1.4 and dd file is missing)
            if (pwm.getConfigSupport().isDescriptorRequired() || Profile.J2EE_14.equals(getJ2eeProfile())) {
                appModule.getMetadataModel();
            }
            
            // UI Logging
            EarProjectUtil.logUI(NbBundle.getBundle(EarProject.class), "UI_EAR_PROJECT_OPENED", // NOI18N
                    new Object[] {(serverType != null ? serverType : Deployment.getDefault().getServerID(servInstID)), servInstID});
            
            // Usage Logging
            String serverName = ""; // NOI18N
            try {
                if (servInstID != null) {
                    serverName = Deployment.getDefault().getServerInstance(servInstID).getServerDisplayName();
                }
            }
            catch (InstanceRemovedException ier) {
                // ignore
            }
            EarProjectUtil.logUsage(EarProject.class, "USG_PROJECT_OPEN_EAR", new Object[] { serverName }); // NOI18N
        }
        
        private void updateProject() {
            // Make it easier to run headless builds on the same machine at least.
            EditableProperties ep = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
            ep.setProperty("netbeans.user", System.getProperty("netbeans.user"));
            
            // #134642 - use Ant task from copylibs library
            SharabilityUtility.makeSureProjectHasCopyLibsLibrary(helper, refHelper);
            
            //update lib references in project properties
            EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            J2EEProjectProperties.removeObsoleteLibraryLocations(ep);
            J2EEProjectProperties.removeObsoleteLibraryLocations(props);
            
            if (props.getProperty(EarProjectProperties.J2EE_DEPLOY_ON_SAVE) == null) {
                String server = evaluator().getProperty(EarProjectProperties.J2EE_SERVER_INSTANCE);
                props.setProperty(EarProjectProperties.J2EE_DEPLOY_ON_SAVE, 
                    server == null ? "false" : DeployOnSaveUtils.isDeployOnSaveSupported(server));
            }
            
            if (props.getProperty(EarProjectProperties.J2EE_COMPILE_ON_SAVE) == null) {
                props.setProperty(EarProjectProperties.J2EE_COMPILE_ON_SAVE, 
                        props.getProperty(EarProjectProperties.J2EE_DEPLOY_ON_SAVE));
            }
            
            helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
            
            helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
            try {
                ProjectManager.getDefault().saveProject(EarProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        
        protected void projectClosed() {
            helper.getStandardPropertyEvaluator().removePropertyChangeListener(EarProject.this.appModule);

            // listen to j2ee platform classpath changes
            EditableProperties privateProperties = updateHelper.getProperties( AntProjectHelper.PRIVATE_PROPERTIES_PATH );
            String servInstID = privateProperties.getProperty(EarProjectProperties.J2EE_SERVER_INSTANCE);
            J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(servInstID);
            if (platform != null) {
                unregisterJ2eePlatformListener(platform);
            }
            
            // Probably unnecessary, but just in case:
            try {
                ProjectManager.getDefault().saveProject(EarProject.this);
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }

            // Unregister copy on save support
            try {
                getAppModule().copyOnSaveSupport.cleanup();
            }
            catch (FileStateInvalidException e) {
                LOGGER.log(Level.INFO, null, e);
            }

            Deployment.getDefault().disableCompileOnSaveSupport(appModule);
            
            // unregister project's classpaths to GlobalPathRegistry
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            
            easelSupport.close();
        }
        
    }
    
    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {
        
        // List of primarily supported templates
        
        private static final String[] TYPES = new String[] {
            "XML",                  // NOI18N
            "ear-types",            // NOI18N
            "wsdl",                 // NOI18N
            "simple-files",         // NOI18N
            "ant-script",           // NOI18N
            "ant-task",             // NOI18N
        };
        
        private static final String[] PRIVILEGED_NAMES = new String[] {
            "Templates/J2EE/ApplicationXml",                // NOI18N
            "deployment-descriptor",                // NOI18N
        };
        public String[] getRecommendedTypes() {
            return TYPES;
        }
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
    }
    
    static class MyIconBaseProvider implements IconBaseProvider {
        public String getIconBase() {
            return "org/netbeans/modules/j2ee/earproject/ui/resources/"; // NOI18N
        }
    }
    
    /** May return <code>null</code>. */
    public FileObject getOrCreateMetaInfDir() {
        String metaInfProp = helper.getStandardPropertyEvaluator().
                getProperty(EarProjectProperties.META_INF);
        if (metaInfProp == null) {
            // IZ 91941
            // does project.properties exist? if yes, something is probably wrong...
            File projectProperties = helper.resolveFile(AntProjectHelper.PROJECT_PROPERTIES_PATH);
            if (projectProperties.exists()) {
                // file exists, log warning
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING,
                            "Cannot resolve {0} property for {1}", // NOI18N
                            new Object[] {EarProjectProperties.META_INF, this});
                }
            }
            metaInfProp = "src/conf"; // NOI18N
        }
        FileObject metaInfFO = null;
        try {
            File prjDirF = FileUtil.toFile(getProjectDirectory());
            File metaInfF = PropertyUtils.resolveFile(prjDirF, metaInfProp);
            metaInfFO = FileUtil.createFolder(metaInfF);
        } catch (IOException ex) {
            LOGGER.log(Level.INFO, null, ex);
        }
        return metaInfFO;
    }
    
    FileObject getFileObject(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFileObject(prop);
        } else {
            return null;
        }
    }
    
    File getFile(String propname) {
        String prop = helper.getStandardPropertyEvaluator().getProperty(propname);
        if (prop != null) {
            return helper.resolveFile(prop);
        } else {
            return null;
        }
    }
    
    public String getServerID() {
        return helper.getStandardPropertyEvaluator().getProperty(EarProjectProperties.J2EE_SERVER_TYPE);
    }
    
    public String getServerInstanceID() {
        return helper.getStandardPropertyEvaluator().getProperty(EarProjectProperties.J2EE_SERVER_INSTANCE);
    }
    
    @Deprecated
    public String getJ2eePlatformVersion() {
        return helper.getStandardPropertyEvaluator().getProperty(EarProjectProperties.J2EE_PLATFORM);
    }

    public Profile getJ2eeProfile() {
        return  Profile.fromPropertiesString(helper.getStandardPropertyEvaluator().getProperty(EarProjectProperties.J2EE_PLATFORM));
    }
    
    public GeneratedFilesHelper getGeneratedFilesHelper() {
        return genFilesHelper;
    }
    
    private final class AntArtifactProviderImpl implements AntArtifactProvider{
        public AntArtifact[] getBuildArtifacts() {
            return new AntArtifact[] {
                helper.createSimpleAntArtifact(ARTIFACT_TYPE_EAR, "dist.jar", evaluator(), "dist", "clean"), // NOI18N
            };
        }
        
    }

    // FIXME this is just fallback for code searching for the old SPI in lookup
    // remove in next release
    @SuppressWarnings("deprecation")
    private static class EarImpl implements EarImplementation {

        private final Ear apiEar;

        private final ProjectEar projectEar;

        public EarImpl(Ear apiEar, ProjectEar projectEar) {
            this.apiEar = apiEar;
            this.projectEar = projectEar;
        }

        public void addCarModule(Car module) {
            apiEar.addCarModule(module);
        }

        public void addEjbJarModule(EjbJar module) {
            apiEar.addEjbJarModule(module);
        }

        public void addWebModule(WebModule module) {
            apiEar.addWebModule(module);
        }

        public FileObject getDeploymentDescriptor() {
            return apiEar.getDeploymentDescriptor();
        }

        public String getJ2eePlatformVersion() {
            return apiEar.getJ2eePlatformVersion();
        }

        public FileObject getMetaInf() {
            return projectEar.getMetaInf();
        }
    }

    private static class EarImpl2 implements EarImplementation2 {

        private final ProjectEar projectEar;

        public EarImpl2(ProjectEar projectEar) {
            this.projectEar = projectEar;
        }

        public void addCarModule(Car module) {
            projectEar.addCarModule(module);
        }

        public void addEjbJarModule(EjbJar module) {
            projectEar.addEjbJarModule(module);
        }

        public void addWebModule(WebModule module) {
            projectEar.addWebModule(module);
        }

        public FileObject getDeploymentDescriptor() {
            return projectEar.getDeploymentDescriptor();
        }

        public Profile getJ2eeProfile() {
            return projectEar.getJ2eeProfile();
        }

        public FileObject getMetaInf() {
            return projectEar.getMetaInf();
        }

    }

    private class EarExtenderImplementation implements AntBuildExtenderImplementation {
        //add targets here as required by the external plugins..
        public List<String> getExtensibleTargets() {
            String[] targets = new String[] {
                "pre-dist", //NOI18N
            };
            return Arrays.asList(targets);
        }

        public Project getOwningProject() {
            return EarProject.this;
        }

    }

    private class JavaEEProjectSettingsImpl implements JavaEEProjectSettingsImplementation {

        private final EarProject project;

        public JavaEEProjectSettingsImpl(EarProject project) {
            this.project = project;
            evaluator().addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (EarProjectProperties.SELECTED_BROWSER.equals(evt.getPropertyName())) {
                        easelSupport.resetBrowserSupport();
                    }
                }
            });
        }

        @Override
        public void setProfile(Profile profile) {
            setInSharedProperties(JavaEEProjectSettingConstants.J2EE_PLATFORM, profile.toPropertiesString());
        }

        @Override
        public Profile getProfile() {
            return project.getJ2eeProfile();
        }

        @Override
        public void setBrowserID(String browserID) {
            setInPrivateProperties(JavaEEProjectSettingConstants.SELECTED_BROWSER, browserID);
        }

        @Override
        public String getBrowserID() {
            return evaluator().getProperty(JavaEEProjectSettingConstants.SELECTED_BROWSER);
        }

        @Override
        public void setServerInstanceID(String serverInstanceID) {
            setInPrivateProperties(JavaEEProjectSettingConstants.J2EE_SERVER_INSTANCE, serverInstanceID);
        }

        @Override
        public String getServerInstanceID() {
            return evaluator().getProperty(JavaEEProjectSettingConstants.J2EE_SERVER_INSTANCE);
        }

        private void setInSharedProperties(String key, String value) {
            setInProperties(key, value, AntProjectHelper.PROJECT_PROPERTIES_PATH);
        }

        private void setInPrivateProperties(String key, String value) {
            setInProperties(key, value, AntProjectHelper.PRIVATE_PROPERTIES_PATH);
        }

        private void setInProperties(String key, String value, String propertiesPath) {
            try {
                UpdateHelper helper = project.getUpdateHelper();
                EditableProperties projectProperties = helper.getProperties(propertiesPath);
                projectProperties.setProperty(key, value);
                helper.putProperties(propertiesPath, projectProperties);
                ProjectManager.getDefault().saveProject(project);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Project properties couldn't be saved.", ex);
            }
        }
    }

    private static final class EarDDGeneratorImpl implements EarDDGeneratorImplementation {

        final Project project;

        public EarDDGeneratorImpl(Project project) {
            this.project = project;
        }

        @Override
        public FileObject setupDD(boolean force) {
            //#118047 avoid using the EarProject instance directly to allow for alternate implementations.
            EarImplementation earImpl = project.getLookup().lookup(EarImplementation.class);
            if (earImpl == null) {
                return null;
            }

            FileObject metaInf = earImpl.getMetaInf();
            FileObject dd = metaInf.getFileObject(ProjectEar.FILE_DD);
            if (dd != null) {
                return dd; // already created
            }

            Profile profile = JavaEEProjectSettings.getProfile(project);
            boolean create = force || DDHelper.isApplicationXMLCompulsory(project);

            try {
                dd = DDHelper.createApplicationXml(profile, metaInf, create);
                if (dd != null) {
                    Application app = DDProvider.getDefault().getDDRoot(dd);
                    app.setDisplayName(ProjectUtils.getInformation(project).getDisplayName());
                    //#118047 avoiding the use of EarProject not possible here.
                    // API for retrieval of getJarContentAdditional() not present.
                    EarProject defInst = project.getLookup().lookup(EarProject.class);
                    if (defInst != null) {
                        for (ClassPathSupport.Item vcpi : EarProjectProperties.getJarContentAdditional(defInst)) {
                            EarProjectProperties.addItemToAppDD(defInst, app, vcpi);
                        }
                    }
                    app.write(dd);
                }
                return dd;
            } catch (IOException ex) {
                return null;
            }
        }
    }
}
