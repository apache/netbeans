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
package org.netbeans.modules.websvc.rest.spi;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.classpath.ProjectClassPathModifier;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModelAction;
import org.netbeans.modules.j2ee.persistence.api.PersistenceScope;
import org.netbeans.modules.javaee.specs.support.api.JaxRsStackSupport;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.modules.websvc.jaxws.spi.JAXWSSupportProvider;
import org.netbeans.modules.websvc.rest.ApplicationSubclassGenerator;
import org.netbeans.modules.websvc.rest.MiscPrivateUtilities;
import org.netbeans.modules.websvc.rest.WebXmlUpdater;
import org.netbeans.modules.websvc.rest.model.api.RestApplication;
import org.netbeans.modules.websvc.rest.model.api.RestApplicationModel;
import org.netbeans.modules.websvc.rest.model.api.RestApplications;
import org.netbeans.modules.websvc.rest.model.api.RestServicesModel;
import org.netbeans.modules.websvc.rest.model.spi.RestServicesMetadataModelFactory;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.RequestProcessor;

/**
 * All development project type supporting REST framework should provide
 * one instance of this in project lookup.
 *
 * @author Nam Nguyen
 */
public abstract class RestSupport {
    public static final String SWDP_LIBRARY = "restlib"; //NOI18N
    public static final String RESTAPI_LIBRARY = "restapi"; //NOI18N
    protected static final String GFV3_RESTLIB = "restlib_gfv3ee6"; // NOI18N
    protected static final String GFV31_RESTLIB = "restlib_gfv31ee6"; // NOI18N
    public static final String PROP_SWDP_CLASSPATH = "libs.swdp.classpath"; //NOI18N
    public static final String PROP_RESTBEANS_TEST_DIR = "restbeans.test.dir"; //NOI18N
    public static final String PROP_RESTBEANS_TEST_FILE = "restbeans.test.file";//NOI18N
    public static final String PROP_RESTBEANS_TEST_URL = "restbeans.test.url";//NOI18N
    public static final String PROP_BASE_URL_TOKEN = "base.url.token";//NOI18N
    public static final String PROP_APPLICATION_PATH = "rest.application.path";//NOI18N
    public static final String BASE_URL_TOKEN = "___BASE_URL___";//NOI18N
    public static final String RESTBEANS_TEST_DIR = "build/generated-sources/rest-test";//NOI18N
    public static final String COMMAND_TEST_RESTBEANS = "test-restbeans";//NOI18N
    public static final String COMMAND_DEPLOY = "run-deploy";//NOI18N
    public static final String TEST_RESBEANS = "test-resbeans";//NOI18N
    public static final String TEST_RESBEANS_HTML = TEST_RESBEANS + ".html";//NOI18N
    public static final String TEST_RESBEANS_JS = TEST_RESBEANS + ".js";
    public static final String TEST_RESBEANS_CSS = TEST_RESBEANS + ".css";//NOI18N
    public static final String TEST_RESBEANS_CSS2 = "css_master-all.css";//NOI18N
    public static final String REST_SERVLET_ADAPTOR = "ServletAdaptor";//NOI18N
    public static final String JAX_RS_APPLICATION_CLASS = "javax.ws.rs.core.Application"; //NOI18N
    public static final String REST_SERVLET_ADAPTOR_CLASS = "com.sun.jersey.spi.container.servlet.ServletContainer"; //NOI18N
    public static final String REST_SERVLET_ADAPTOR_CLASS_OLD = "com.sun.ws.rest.impl.container.servlet.ServletAdaptor";  //NOI18N
    public static final String REST_SERVLET_ADAPTOR_CLASS_2_0 = "org.glassfish.jersey.servlet.ServletContainer"; //NOI18N
    public static final String REST_SPRING_SERVLET_ADAPTOR_CLASS = "com.sun.jersey.spi.spring.container.servlet.SpringServlet";    //NOI18N
    public static final String REST_SERVLET_ADAPTOR_MAPPING = "/resources/*";//NOI18N
    public static final String PARAM_WEB_RESOURCE_CLASS = "webresourceclass";//NOI18N
    public static final String WEB_RESOURCE_CLASS = "webresources.WebResources";//NOI18N
    public static final String REST_API_JAR = "jsr311-api.jar";//NOI18N
    public static final String REST_RI_JAR = "jersey";//NOI18N
    //public static final String IGNORE_PLATFORM_RESTLIB = "restlib.ignore.platform";//NOI18N
    public static final String JSR311_JAR_PATTERN = "jsr311-api.*\\.jar";//NOI18N
    public static final String JERSEY_API_LOCATION = "modules/ext/rest";//NOI18N
    public static final String JTA_USER_TRANSACTION_CLASS = "javax/transaction/UserTransaction.class";  //NOI18
    public static final String J2EE_SERVER_TYPE = "j2ee.server.type";       //NOI18N
    public static final String TOMCAT_SERVER_TYPE = "tomcat";       //NOI18N
    public static final String GFV3_SERVER_TYPE = "gfv3";          //NOI18N
    public static final String GFV2_SERVER_TYPE = "J2EE";          //NOI18N

    public static final int PROJECT_TYPE_DESKTOP = 0; //NOI18N
    public static final int PROJECT_TYPE_WEB = 1; //NOI18N
    public static final int PROJECT_TYPE_NB_MODULE = 2; //NOI18N

    public static final String PROP_REST_RESOURCES_PATH = "rest.resources.path";//NOI18N
    public static final String PROP_REST_CONFIG_TYPE = "rest.config.type"; //NOI18N
    public static final String PROP_REST_JERSEY = "rest.jersey.type";      //NOI18N

    // IDE generates Application subclass
    public static final String CONFIG_TYPE_IDE = "ide"; //NOI18N
    // user does everything manually
    public static final String CONFIG_TYPE_USER= "user"; //NOI18N
    // Jersey servlet is registered in deployment descriptor
    public static final String CONFIG_TYPE_DD= "dd"; //NOI18N

    public static final String JERSEY_CONFIG_IDE="ide";         //NOI18N
    public static final String JERSEY_CONFIG_SERVER="server";   //NOI18N

    public static final String CONTAINER_RESPONSE_FILTER = "com.sun.jersey.spi.container.ContainerResponseFilters";//NOI18N

    protected static final String JERSEY_SPRING_JAR_PATTERN = "jersey-spring.*\\.jar";//NOI18N

    private volatile PropertyChangeListener restModelListener;

    private RequestProcessor RP = new RequestProcessor(RestSupport.class);

    private AntProjectHelper helper;
    private RestServicesModel restServicesModel;
    private RestApplicationModel restApplicationModel;
    private final Project project;

    private ApplicationSubclassGenerator applicationSubclassGenerator;
    private WebXmlUpdater webXmlUpdater;

    /** Creates a new instance of RestSupport */
    public RestSupport(Project project) {
        if (project == null) {
            throw new IllegalArgumentException("Null project");
        }
        this.project = project;
        applicationSubclassGenerator = new ApplicationSubclassGenerator(this);
        webXmlUpdater = new WebXmlUpdater(this);
        RP.post(new Runnable() {
            @Override
            public void run() {
                String configType = getProjectProperty(PROP_REST_CONFIG_TYPE);
                if (CONFIG_TYPE_IDE.equals(configType)) {
                    initListener();
                }
            }
        });
    }

    private void initListener() {
        if ( restModelListener == null ){
            restModelListener = new PropertyChangeListener() {

                @Override
                public void propertyChange( PropertyChangeEvent evt ) {
                    applicationSubclassGenerator.refreshApplicationSubclass();
                }
            };
            RestServicesModel servicesModel = getRestServicesModel();
            if (servicesModel != null) {
                servicesModel.addPropertyChangeListener(restModelListener);
            }
        }
    }

    /**
     * Ensure the project is ready for REST development.
     * Typical implementation would need to invoke addSwdpLibraries
     * REST development with servlet container would need to add servlet adaptor
     * to web.xml.
     */
    public final void ensureRestDevelopmentReady(final RestConfig restConfig) throws IOException {
        assert restConfig != null;

        if (isRestSupportOn()) {
            return;
        }

        setProjectProperty(PROP_REST_CONFIG_TYPE, restConfig == RestConfig.DD ? CONFIG_TYPE_DD : CONFIG_TYPE_IDE);

        RP.post(new Runnable() {
            @Override
            public void run() {
                try {
                    ensureRestDevelopmentReadyImpl(restConfig);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        });
    }

    private void ensureRestDevelopmentReadyImpl(RestConfig restConfig) throws IOException {

        // check whether project EE level or project/server classpath provide
        // JAX-RS APIs. The value should be false only in case of EE5 specification
        // and server without any Jersey on its classpath, eg. Tomcat or some
        // very very old GF (v2? or older)

        // extend build script if necessary
        extendBuildScripts();

        boolean hasJersey2 = hasJersey2(true);
        boolean hasJaxRsOnClasspath = hasJaxRsOnClasspath(false);

        final boolean hasJaxRs = isEESpecWithJaxRS() || hasJaxRsOnClasspath ||
                hasJersey1(true) || hasJersey2;

        if (isEE5() && (hasJersey2 || !hasJaxRs)) {
            webXmlUpdater.addJersey2ResourceConfigToWebApp(restConfig);
        }
        // add latest JAX-RS APIs to project's classpath:
        if (!hasJaxRs || !hasJaxRsOnClasspath) {
            boolean jaxRSApiAdded = false;
            JaxRsStackSupport support = getJaxRsStackSupport();
            if (support != null) {
                // in this case server is enhancing project's classpath:
                jaxRSApiAdded = support.addJsr311Api(getProject());
            }
            if (!jaxRSApiAdded) {
                // fallback on IDE's default impl if server does not have its own
                // jax-rs impl:
                JaxRsStackSupport.getDefault().addJsr311Api(getProject());
            }
        }

        // make sure the project classpath contains Jersey JARs:
        if (!hasJersey2 && !hasJersey1(true)) {
            extendJerseyClasspath();
        }

        handleSpring();

        ProjectManager.getDefault().saveProject(getProject());
    }


    protected void extendJerseyClasspath() {
        JaxRsStackSupport support = getJaxRsStackSupport();
        boolean jerseyAdded = false;
        if (support != null) {
            jerseyAdded = support.extendsJerseyProjectClasspath(getProject());
        }
        // fallback on IDE's default library:
        if (!jerseyAdded){
            JaxRsStackSupport.getDefault().extendsJerseyProjectClasspath(getProject());
        }
    }

    protected abstract void extendBuildScripts() throws IOException;

    protected abstract void handleSpring() throws IOException;

    public abstract String getApplicationPathFromDialog(List<RestApplication> restApplications);

    public synchronized RestServicesModel getRestServicesModel() {
        if (restServicesModel == null) {
            FileObject sourceRoot = MiscUtilities.findSourceRoot(getProject());
            if (sourceRoot == null) {
                return null;
            }
            ClassPathProvider cpProvider = getProject().getLookup().lookup(ClassPathProvider.class);
            if (cpProvider != null) {
                ClassPath compileCP = cpProvider.findClassPath(sourceRoot, ClassPath.COMPILE);
                ClassPath bootCP = cpProvider.findClassPath(sourceRoot, ClassPath.BOOT);
                ClassPath sourceCP = cpProvider.findClassPath(sourceRoot, ClassPath.SOURCE);
                if (compileCP != null && bootCP != null) {
                    MetadataUnit metadataUnit = MetadataUnit.create(
                            bootCP,
                            extendClassPathWithJaxRsApisIfNecessary(compileCP),
                            sourceCP,
                            null);
                    restServicesModel = RestServicesMetadataModelFactory.
                            createMetadataModel(metadataUnit, project);
                }
            }
        }
        return restServicesModel;
    }

    public synchronized RestApplicationModel getRestApplicationsModel() {
        if (restApplicationModel == null) {
            MetadataUnit metadataUnit = MetadataUnit.create(
                    MiscPrivateUtilities.getClassPath(getProject(), ClassPath.BOOT),
                    MiscPrivateUtilities.getClassPath(getProject(), ClassPath.COMPILE),
                    MiscPrivateUtilities.getClassPath(getProject(), ClassPath.SOURCE),
                    null
                    );
            restApplicationModel = RestServicesMetadataModelFactory.
                    createApplicationMetadataModel(metadataUnit, project);
        }
        return restApplicationModel;
    }

    private static ClassPath extendClassPathWithJaxRsApisIfNecessary(ClassPath classPath) {
        // @todo: should we also add the jakarta variants?
        if (classPath.findResource("javax/ws/rs/core/Application.class") != null) {
            return classPath;
        }
        File jerseyRoot = InstalledFileLocator.getDefault().locate(JERSEY_API_LOCATION, "org.netbeans.modules.websvc.restlib", false);
        if (jerseyRoot != null && jerseyRoot.isDirectory()) {
            File[] jsr311Jars = jerseyRoot.listFiles(new MiscPrivateUtilities.JerseyFilter(JSR311_JAR_PATTERN));
            if (jsr311Jars != null && jsr311Jars.length>0) {
                FileObject fo = FileUtil.toFileObject(jsr311Jars[0]);
                if (fo != null) {
                    fo = FileUtil.getArchiveRoot(fo);
                    if (fo != null) {
                        return ClassPathSupport.createProxyClassPath(classPath,
                                ClassPathSupport.createClassPath(fo));
                    }
                }
            }
        }
        return classPath;
    }

    public abstract FileObject generateTestClient(File testdir, String url)
        throws IOException;

    public Project getProject() {
        return project;
    }

    public void setProjectProperty(String name, String value) {
        MiscPrivateUtilities.setProjectProperty(getProject(), getAntProjectHelper(), name, value, AntProjectHelper.PROJECT_PROPERTIES_PATH);
    }

    public void setPrivateProjectProperty(String name, String value) {
        MiscPrivateUtilities.setProjectProperty(getProject(), getAntProjectHelper(), name, value, AntProjectHelper.PRIVATE_PROPERTIES_PATH );
    }

    public String getProjectProperty(String name) {
        if (getAntProjectHelper() == null) {
            return null;
        }
        return helper.getStandardPropertyEvaluator().getProperty(name);
    }

    public void removeProjectProperties(final String[] propertyNames) {
        if (getAntProjectHelper() == null) {
            return;
        }
        try {
        ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction() {
            @Override
            public Object run() throws IOException {
                // and save the project
                try {
                    MiscPrivateUtilities.removeProperty(getAntProjectHelper(), propertyNames,
                            AntProjectHelper.PROJECT_PROPERTIES_PATH);
                    MiscPrivateUtilities.removeProperty(getAntProjectHelper(), propertyNames,
                            AntProjectHelper.PRIVATE_PROPERTIES_PATH );
                    ProjectManager.getDefault().saveProject(getProject());
                }
                catch(IOException ioe) {
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, ioe.getLocalizedMessage(), ioe);
                }
                return null;
            }
        });
        }
        catch (MutexException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, e);
        }

    }

    public AntProjectHelper getAntProjectHelper() {
        if (helper == null) {
            JAXWSSupportProvider provider = project.getLookup().lookup(JAXWSSupportProvider.class);
            if (provider != null) {
                JAXWSSupport support = provider.findJAXWSSupport(project.getProjectDirectory());
                if (support != null) {
                    helper = support.getAntProjectHelper();
                }
            }
        }
        return helper;
    }

    /**
     * Check to see if there is JTA support.
     */
    public boolean hasJTASupport() {
        return MiscPrivateUtilities.hasResource(getProject(), "javax/transaction/UserTransaction.class");  // NOI18N
    }

    /**
     * Check to see if there is Spring framework support.
     *
     */
    public boolean hasSpringSupport() {
        return MiscPrivateUtilities.hasResource(getProject(), "org/springframework/transaction/annotation/Transactional.class"); // NOI18N
    }

    public String getServerType() {
        return getProjectProperty(J2EE_SERVER_TYPE);
    }

    public boolean isServerTomcat() {
        String serverType = getServerType();

        if (serverType != null) {
            return serverType.toLowerCase().contains(TOMCAT_SERVER_TYPE);
        }

        return false;
    }

    public boolean isServerGFV3() {
        if ( getServerType() == null ){
            return false;
        }
        return getServerType().startsWith(GFV3_SERVER_TYPE);
    }

    public boolean isServerGFV2() {
        return GFV2_SERVER_TYPE.equals(getServerType());
    }

    public abstract File getLocalTargetTestRest();

    public String getBaseURL() {
        String applicationPath = getApplicationPath();
        if (applicationPath != null) {
            if (!applicationPath.startsWith("/")) {
                applicationPath = "/"+applicationPath;
            }
        }
        return MiscUtilities.getContextRootURL(getProject())+"||"+applicationPath;            //NOI18N
    }


    public abstract void deploy() throws IOException;

    public boolean isRestSupportOn() {
        if (getAntProjectHelper() == null) {
            return false;
        }
        return getProjectProperty(PROP_REST_CONFIG_TYPE) != null;
    }

    /**
     * Get persistence.xml file.
     */
    public FileObject getPersistenceXml() {
        PersistenceScope ps = PersistenceScope.getPersistenceScope(getProject().getProjectDirectory());
        if (ps != null) {
            return ps.getPersistenceXml();
        }
        return null;
    }
    /** Get deployment descriptor (DD API root bean)
     *
     * @return WebApp bean
     * @throws java.io.IOException
     */
    public WebApp getWebApp() throws IOException {
        return webXmlUpdater.findWebApp();
    }

    WebXmlUpdater getWebXmlUpdater() {
        return webXmlUpdater;
    }

    public boolean hasServerJerseyLibrary(){
        return JaxRsStackSupport.getInstance(project) != null;
    }

    public JaxRsStackSupport getJaxRsStackSupport(){
        return JaxRsStackSupport.getInstance(project);
    }

    /**
     * Returns true if JAX-RS APIs are available for the project. That means if
     * project's profile is EE7 (web profile or full) or EE6 (full).
     */
    public boolean isEESpecWithJaxRS(){
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if ( webModule == null ){
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        boolean isJee6 = Profile.JAVA_EE_6_WEB.equals(profile) ||
                Profile.JAVA_EE_6_FULL.equals(profile);
        // Fix for BZ#216345: JAVA_EE_6_WEB profile doesn't contain JAX-RS API
        return (isJee6 && MiscPrivateUtilities.supportsTargetProfile(project, Profile.JAVA_EE_6_FULL)) || profile.isAtLeast(Profile.JAVA_EE_7_WEB);
    }

    /**
     * Is this a Jakarta EE 11 profile project?
     */
    public boolean isJakartaEE11() {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if ( webModule == null ){
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        return Profile.JAKARTA_EE_11_WEB.equals(profile) ||
                        Profile.JAKARTA_EE_11_FULL.equals(profile);
    }
    
    /**
     * Is this JAKARTAEE10 profile project?
     */
    public boolean isJakartaEE10() {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if ( webModule == null ){
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        return Profile.JAKARTA_EE_10_WEB.equals(profile) ||
                        Profile.JAKARTA_EE_10_FULL.equals(profile);
    }

    /**
     * Is this JAKARTAEE91 profile project?
     */
    public boolean isJakartaEE91() {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if (webModule == null) {
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        return Profile.JAKARTA_EE_9_1_WEB.equals(profile)
                || Profile.JAKARTA_EE_9_1_FULL.equals(profile);
    }
    
    /**
     * Is this JAKARTAEE9 profile project?
     */
    public boolean isJakartaEE9() {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if ( webModule == null ){
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        return Profile.JAKARTA_EE_9_WEB.equals(profile) ||
                        Profile.JAKARTA_EE_9_FULL.equals(profile);
    }
    /**
     * Is this JAKARTAEE8 profile project?
     */
    public boolean isJakartaEE8() {
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if ( webModule == null ){
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        return Profile.JAKARTA_EE_8_WEB.equals(profile) ||
                        Profile.JAKARTA_EE_8_FULL.equals(profile);
    }

    /**
     * Is this EE8 profile project?
     */
    public boolean isEE8(){
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if ( webModule == null ){
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        return Profile.JAVA_EE_8_WEB.equals(profile) ||
                        Profile.JAVA_EE_8_FULL.equals(profile);
    }

    /**
     * Is this EE7 profile project?
     */
    public boolean isEE7(){
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if ( webModule == null ){
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        return Profile.JAVA_EE_7_WEB.equals(profile) ||
                        Profile.JAVA_EE_7_FULL.equals(profile);
    }

    /**
     * Is this EE6 profile project?
     */
    public boolean isEE6(){
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if ( webModule == null ){
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        return Profile.JAVA_EE_6_WEB.equals(profile) ||
                        Profile.JAVA_EE_6_FULL.equals(profile);
    }

    public boolean isEE5(){
        WebModule webModule = WebModule.getWebModule(project.getProjectDirectory());
        if ( webModule == null ){
            return false;
        }
        Profile profile = webModule.getJ2eeProfile();
        return Profile.JAVA_EE_5.equals(profile);
    }

    /**
     * Is Jersey 2.x jar available on project's classpath or on classpath of
     * server used by this project?
     */
    public boolean hasJersey2(boolean checkServerClasspath) {
        if (MiscPrivateUtilities.hasResource(getProject(), "org/glassfish/jersey/servlet/ServletContainer.class")) {
            return true;
        }
        if (checkServerClasspath) {
            JaxRsStackSupport support = getJaxRsStackSupport();
            if (support != null){
                return support.isBundled(REST_SERVLET_ADAPTOR_CLASS_2_0);
            }
        }
        return false;
    }

    /**
     * Is Jersey 12.x jar available on project's classpath or on classpath of
     * server used by this project?
     */
    public boolean hasJersey1(boolean checkServerClasspath) {
        if (MiscPrivateUtilities.hasResource(getProject(), "com/sun/jersey/spi/container/servlet/ServletContainer.class")) {
            return true;
        }
        if (checkServerClasspath) {
            JaxRsStackSupport support = getJaxRsStackSupport();
            if (support != null){
                return support.isBundled(REST_SERVLET_ADAPTOR_CLASS);
            }
        }
        return false;
    }

    public boolean hasJaxRsOnClasspath(boolean checkServerClasspath) {
        if (MiscPrivateUtilities.hasResource(getProject(), "jakarta/ws/rs/core/Application.class")
                || MiscPrivateUtilities.hasResource(getProject(), "javax/ws/rs/core/Application.class")) {
            return true;
        }
        if (checkServerClasspath) {
            JaxRsStackSupport support = getJaxRsStackSupport();
            if (support != null){
                return support.isBundled(JAX_RS_APPLICATION_CLASS);
            }
        }
        return false;
    }

    /*
     * Make REST support configuration for current porject state:
     * - Modify Application config class getClasses() method
     * or
     * - Update deployment descriptor with Jersey specific options
     */
    public void configure(String... packages) throws IOException {
        String configType = getProjectProperty(PROP_REST_CONFIG_TYPE);
        if ( CONFIG_TYPE_DD.equals( configType)){
            webXmlUpdater.configRestPackages(packages);
        }
        else if (CONFIG_TYPE_IDE.equals(configType)) {
            initListener();
            applicationSubclassGenerator.refreshApplicationSubclass();
        }
    }

    public String getApplicationPath() {
        String pathFromDD = MiscUtilities.getApplicationPathFromDD(webXmlUpdater.findWebApp());
        String applPath = ApplicationSubclassGenerator.getApplicationPathFromAnnotations(this, pathFromDD);
        return (applPath == null ? "webresources" : applPath);
    }

    /** log rest resource detection
     *
     * @param prj project instance
     */
    public abstract void logResourceCreation();

    public List<RestApplication> getRestApplications() {
        RestApplicationModel applicationModel = getRestApplicationsModel();
        if (applicationModel != null) {
            try {
                return applicationModel.runReadAction(
                        new MetadataModelAction<RestApplications, List<RestApplication>>() {
                    public List<RestApplication> run(RestApplications metadata)
                            throws IOException {
                        return metadata.getRestApplications();
                    }
                });
            } catch (IOException ex) {
                return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    public boolean hasJerseyServlet() {
        return WebXmlUpdater.hasRestServletAdaptor(webXmlUpdater.findWebApp());
    }

    protected void addJerseySpringJar() throws IOException {
        FileObject srcRoot = MiscUtilities.findSourceRoot(getProject());
        if (srcRoot != null) {
            ClassPath cp = ClassPath.getClassPath(srcRoot, ClassPath.COMPILE);
            if (cp ==null ||cp.findResource(
                    "com/sun/jersey/api/spring/Autowire.class") == null)        //NOI18N
            {
                File jerseyRoot = InstalledFileLocator.getDefault().locate(JERSEY_API_LOCATION, null, false);
                if (jerseyRoot != null && jerseyRoot.isDirectory()) {
                    File[] jerseyJars = jerseyRoot.listFiles(new MiscPrivateUtilities.JerseyFilter(JERSEY_SPRING_JAR_PATTERN));
                    if (jerseyJars != null && jerseyJars.length>0) {
                        URL url = FileUtil.getArchiveRoot(jerseyJars[0].toURI().toURL());
                        ProjectClassPathModifier.addRoots(new URL[] {url}, srcRoot, ClassPath.COMPILE);
                    }
                }
            }
        }
    }

    public int getProjectType() {
        return PROJECT_TYPE_WEB;
    }

    public static enum RestConfig {
        // Application subclass registration:
        IDE,
        // this type is not used anymore but kept for existing projects
        USER,
        // web.xml deployment descriptor registration
        DD;

        // application class-name (useful only for IDE config type)
        private String appClassName;

        public void setAppClassName(String appClassName) {
            this.appClassName = appClassName;
        }

        public String getAppClassName() {
            return appClassName;
        }
    }

}
