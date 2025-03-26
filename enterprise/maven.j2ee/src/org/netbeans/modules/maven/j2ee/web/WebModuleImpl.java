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

package org.netbeans.modules.maven.j2ee.web;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.maven.model.Dependency;
import org.apache.maven.project.MavenProject;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.dd.api.web.DDProvider;
import org.netbeans.modules.j2ee.dd.api.web.WebApp;
import org.netbeans.modules.j2ee.dd.api.web.WebAppMetadata;
import org.netbeans.modules.j2ee.dd.api.webservices.WebservicesMetadata;
import org.netbeans.modules.j2ee.dd.spi.MetadataUnit;
import org.netbeans.modules.j2ee.dd.spi.web.WebAppMetadataModelFactory;
import org.netbeans.modules.j2ee.dd.spi.webservices.WebservicesMetadataModelFactory;
import org.netbeans.modules.j2ee.deployment.common.api.ConfigurationException;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.classpath.ProjectSourcesClassPathProvider;
import org.netbeans.modules.maven.j2ee.BaseEEModuleImpl;
import org.netbeans.modules.web.spi.webmodule.WebModuleImplementation2;
import org.openide.ErrorManager;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;


/**
 * war/webapp related apis implementation..
 * @author  Milos Kleint
 */
public class WebModuleImpl extends BaseEEModuleImpl implements WebModuleImplementation2, J2eeModuleImplementation2 {

    private static final String WEB_INF = "WEB-INF"; //NOI18N
    private MetadataModel<WebAppMetadata> webAppMetadataModel;
    private MetadataModel<WebAppMetadata> webAppAnnMetadataModel;
    private MetadataModel<WebservicesMetadata> webservicesMetadataModel;
    private boolean inplace = false;


    public WebModuleImpl(Project project, WebModuleProviderImpl provider) {
        super(project, provider, "web.xml", J2eeModule.WEB_XML); //NOI18N
    }


    @Override
    public J2eeModule.Type getModuleType() {
        return J2eeModule.Type.WAR;
    }

    @Override
    public FileObject getArchive() throws IOException {
        return getArchive(Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_WAR, "war", "war");
    }

    /*****************************
     *  WebModule related methods
     *****************************/
    @Override
    public FileObject getWebInf() {
        FileObject root = getDocumentBase();
        if (root != null) {
            return root.getFileObject(WEB_INF); //NOI18N
        }
        return null;
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
    public FileObject getDocumentBase() {
        return WebProjectUtils.getDocumentBase(project);
    }

    /**
     * to be used to denote that a war:inplace goal is used to build the web app.
     */
    public void setWarInplace(boolean inplace) {
        this.inplace = inplace;
    }

    @Override
    public Profile getJ2eeProfile() {
        Profile profile = JavaEEProjectSettings.getProfile(project);
        if (profile != null) {
            return profile;
        }

        Profile pomProfile = getProfileFromPOM(project);
        if (pomProfile != null) {
            // If might happened in cases when WAR project uses Java EE full stack
            // Simply return corresponding Web profile until #232478 will be resolved
            if (Profile.JAVA_EE_6_FULL.equals(pomProfile)) {
                return Profile.JAVA_EE_6_WEB;
            }
            if (Profile.JAVA_EE_7_FULL.equals(pomProfile)) {
                return Profile.JAVA_EE_7_WEB;
            }
            if (Profile.JAVA_EE_8_FULL.equals(pomProfile)) {
                return Profile.JAVA_EE_8_WEB;
            }
            if (Profile.JAKARTA_EE_8_FULL.equals(pomProfile)) {
                return Profile.JAKARTA_EE_8_WEB;
            }
            if (Profile.JAKARTA_EE_9_FULL.equals(pomProfile)) {
                return Profile.JAKARTA_EE_9_WEB;
            }
            if (Profile.JAKARTA_EE_9_1_FULL.equals(pomProfile)) {
                return Profile.JAKARTA_EE_9_1_WEB;
            }
            if (Profile.JAKARTA_EE_10_FULL.equals(pomProfile)) {
                return Profile.JAKARTA_EE_10_WEB;
            }
            if (Profile.JAKARTA_EE_11_FULL.equals(pomProfile)) {
                return Profile.JAKARTA_EE_11_WEB;
            }
            return pomProfile;
        }

        Profile descriptorProfile = getProfileFromDescriptor();
        if (descriptorProfile != null) {
            return descriptorProfile;
        }

        return Profile.JAVA_EE_8_WEB;
    }

    private Profile getProfileFromDescriptor() {
        DDProvider prov = DDProvider.getDefault();
        FileObject dd = getDeploymentDescriptor();
        if (dd != null) {
            try {
                WebApp wa = prov.getDDRoot(dd);
                String waVersion = wa.getVersion();

                if (WebApp.VERSION_2_4.equals(waVersion)) {
                    return Profile.J2EE_14;
                }
                if (WebApp.VERSION_2_5.equals(waVersion)) {
                    return Profile.JAVA_EE_5;
                }
                if (WebApp.VERSION_3_0.equals(waVersion)) {
                    return Profile.JAVA_EE_6_WEB;
                }
                if (WebApp.VERSION_3_1.equals(waVersion)) {
                    return Profile.JAVA_EE_7_WEB;
                }
                if (WebApp.VERSION_4_0.equals(waVersion)) {
                    return Profile.JAKARTA_EE_8_WEB;
                }
                if (WebApp.VERSION_5_0.equals(waVersion)) {
                    return Profile.JAKARTA_EE_9_WEB;
                }
                if (WebApp.VERSION_6_0.equals(waVersion)) {
                    return Profile.JAKARTA_EE_10_WEB;
                }
                if (WebApp.VERSION_6_1.equals(waVersion)) {
                    return Profile.JAKARTA_EE_11_WEB;
                }
            } catch (IOException exc) {
                ErrorManager.getDefault().notify(exc);
            }
        }
        return null;
    }

    /**
     * {@link List} containing Java EE implementations described by {@link DependencyDesc}.
     *
     * Fore more information see this <a href="https://netbeans.org/bugzilla/show_bug.cgi?id=230447">link</a>.
     * <p>
     * In more detail:
     * <ul>
     *   GlassFish:
     *   <li>2.X supports Java EE 5</li>
     *   <li>3.X supports Java EE 6</li>
     *   <li>4.X supports Java EE 7</li>
     *   WebLogic:
     *   <li>10.X supports Java EE 5</li>
     *   <li>12.X supports Java EE 6</li>
     *   <li>No support for Java EE 7 yet</li>
     * </ul>
     * </p>
     */
    private static Map<Profile, List<DependencyDesc>> javaEEMap = new LinkedHashMap<>();
    static {
        List<DependencyDesc> javaEE5 = new ArrayList<>();
        List<DependencyDesc> javaEE6Web = new ArrayList<>();
        List<DependencyDesc> javaEE6Full = new ArrayList<>();
        List<DependencyDesc> javaEE7Web = new ArrayList<>();
        List<DependencyDesc> javaEE7Full = new ArrayList<>();
        List<DependencyDesc> javaEE8Web = new ArrayList<>();
        List<DependencyDesc> javaEE8Full = new ArrayList<>();
        List<DependencyDesc> jakartaEE8Web = new ArrayList<>();
        List<DependencyDesc> jakartaEE8Full = new ArrayList<>();
        List<DependencyDesc> jakartaEE9Web = new ArrayList<>();
        List<DependencyDesc> jakartaEE9Full = new ArrayList<>();
        List<DependencyDesc> jakartaEE91Web = new ArrayList<>();
        List<DependencyDesc> jakartaEE91Full = new ArrayList<>();
        List<DependencyDesc> jakartaEE10Web = new ArrayList<>();
        List<DependencyDesc> jakartaEE10Full = new ArrayList<>();
        List<DependencyDesc> jakartaEE11Web = new ArrayList<>();
        List<DependencyDesc> jakartaEE11Full = new ArrayList<>();

        // Java EE specification
        javaEE5.add(new DependencyDesc("javaee", "javaee-api", "5.0"));
        javaEE5.add(new DependencyDesc("javax", "javaee-web-api", "5.0"));
        javaEE6Full.add(new DependencyDesc("javax", "javaee-api", "6.0"));
        javaEE6Web.add(new DependencyDesc("javax", "javaee-web-api", "6.0"));
        javaEE7Full.add(new DependencyDesc("javax", "javaee-api", "7.0"));
        javaEE7Web.add(new DependencyDesc("javax", "javaee-web-api", "7.0"));
        javaEE8Full.add(new DependencyDesc("javax", "javaee-api", "8.0"));
        javaEE8Web.add(new DependencyDesc("javax", "javaee-web-api", "8.0"));
        jakartaEE8Web.add(new DependencyDesc("jakarta.platform","jakarta.jakartaee-api","8.0.0"));
        jakartaEE8Full.add(new DependencyDesc("jakarta.platform","jakarta.jakartaee-web-api","8.0.0"));
        jakartaEE9Web.add(new DependencyDesc("jakarta.platform","jakarta.jakartaee-api","9.0.0"));
        jakartaEE9Full.add(new DependencyDesc("jakarta.platform","jakarta.jakartaee-web-api","9.0.0"));
        jakartaEE91Web.add(new DependencyDesc("jakarta.platform","jakarta.jakartaee-api","9.1.0"));
        jakartaEE91Full.add(new DependencyDesc("jakarta.platform","jakarta.jakartaee-web-api","9.1.0"));
        jakartaEE10Web.add(new DependencyDesc("jakarta.platform","jakarta.jakartaee-api","10.0.0"));
        jakartaEE10Full.add(new DependencyDesc("jakarta.platform","jakarta.jakartaee-web-api","10.0.0"));
        jakartaEE11Web.add(new DependencyDesc("jakarta.platform","jakarta.jakartaee-api","11.0.0-M1"));
        jakartaEE11Full.add(new DependencyDesc("jakarta.platform","jakarta.jakartaee-web-api","11.0.0-M1"));

        // GlassFish implementations
        javaEE5.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-all", "2"));
        javaEE5.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-web", "2"));
        javaEE6Full.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-all", "3"));
        javaEE6Web.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-web", "3"));
        javaEE7Full.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-all", "4.0"));
        javaEE7Web.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-web", "4.0.1"));
        javaEE7Full.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-all", "4.1.2"));
        javaEE7Web.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-web", "4.1.2"));
        javaEE8Full.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-all", "5.1.0"));
        javaEE8Web.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-web", "5.1.0"));
        jakartaEE8Full.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-all", "5.1.0"));
        jakartaEE8Web.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-web", "5.1.0"));
        jakartaEE9Full.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-all", "6.0.0"));
        jakartaEE9Web.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-web", "6.0.0"));
        jakartaEE91Full.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-all", "6.2.5"));
        jakartaEE91Web.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-web", "6.2.5"));
        jakartaEE10Full.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-all", "7.0.11"));
        jakartaEE10Web.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-web", "7.0.11"));
        jakartaEE11Full.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-all", "8.0.0-M1"));
        jakartaEE11Web.add(new DependencyDesc("org.glassfish.main.extras", "glassfish-embedded-web", "8.0.0-M1"));
        

        // WebLogic implementations
        javaEE5.add(new DependencyDesc("weblogic", "weblogic", "10"));
        javaEE6Full.add(new DependencyDesc("weblogic", "weblogic", "12"));

        // JBoss implementations
        javaEE5.add(new DependencyDesc("org.jboss.spec", "jboss-javaee-5.0", null));
        javaEE5.add(new DependencyDesc("org.jboss.spec", "jboss-javaee-all-5.0", null));
        javaEE6Full.add(new DependencyDesc("org.jboss.spec", "jboss-javaee-6.0", null));
        javaEE6Full.add(new DependencyDesc("org.jboss.spec", "jboss-javaee-all-6.0", null));
        javaEE6Web.add(new DependencyDesc("org.jboss.spec", "jboss-javaee-web-6.0", null));
        javaEE7Full.add(new DependencyDesc("org.jboss.spec", "jboss-javaee-7.0", null));
        javaEE7Full.add(new DependencyDesc("org.jboss.spec", "jboss-javaee-all-7.0", null));
        javaEE7Web.add(new DependencyDesc("org.jboss.spec", "jboss-javaee-web-7.0", null));
        javaEE8Full.add(new DependencyDesc("org.jboss.spec", "jboss-javaee-8.0", null));
        javaEE8Full.add(new DependencyDesc("org.jboss.spec", "jboss-javaee-all-8.0", null));
        javaEE8Web.add(new DependencyDesc("org.jboss.spec", "jboss-javaee-web-8.0", null));
        jakartaEE8Full.add(new DependencyDesc("org.jboss.spec", "jboss-jakartaee-8.0", null));
        jakartaEE8Full.add(new DependencyDesc("org.jboss.spec", "jboss-jakartaee-all-8.0", null));
        jakartaEE8Web.add(new DependencyDesc("org.jboss.spec", "jboss-jakartaee-web-8.0", null));

        javaEEMap.put(Profile.JAKARTA_EE_11_FULL, jakartaEE11Full);
        javaEEMap.put(Profile.JAKARTA_EE_11_WEB, jakartaEE11Web);
        javaEEMap.put(Profile.JAKARTA_EE_10_FULL, jakartaEE10Full);
        javaEEMap.put(Profile.JAKARTA_EE_10_WEB, jakartaEE10Web);
        javaEEMap.put(Profile.JAKARTA_EE_9_1_FULL, jakartaEE91Full);
        javaEEMap.put(Profile.JAKARTA_EE_9_1_WEB, jakartaEE91Web);
        javaEEMap.put(Profile.JAKARTA_EE_9_FULL, jakartaEE9Full);
        javaEEMap.put(Profile.JAKARTA_EE_9_WEB, jakartaEE9Web);
        javaEEMap.put(Profile.JAKARTA_EE_8_FULL, jakartaEE8Full);
        javaEEMap.put(Profile.JAKARTA_EE_8_WEB, jakartaEE8Web);
        javaEEMap.put(Profile.JAVA_EE_8_FULL, javaEE8Full);
        javaEEMap.put(Profile.JAVA_EE_8_WEB, javaEE8Web);
        javaEEMap.put(Profile.JAVA_EE_7_FULL, javaEE7Full);
        javaEEMap.put(Profile.JAVA_EE_7_WEB, javaEE7Web);
        javaEEMap.put(Profile.JAVA_EE_6_FULL, javaEE6Full);
        javaEEMap.put(Profile.JAVA_EE_6_WEB, javaEE6Web);
        javaEEMap.put(Profile.JAVA_EE_5, javaEE5);
    }

    private static class DependencyDesc {

        private final String groupID;
        private final String artifactID;
        private final String version;


        public DependencyDesc(
                String groupID,
                String artifactID,
                String version) {

            this.groupID = groupID;
            this.artifactID = artifactID;
            this.version = version;
        }
    }

    // Trying to guess the Java EE version based on the dependency in pom.xml - See issue #230447
    private Profile getProfileFromPOM(final Project project) {
        final NbMavenProject nbMavenProject = project.getLookup().lookup(NbMavenProject.class);
        if (nbMavenProject != null) {
            MavenProject mavenProject = nbMavenProject.getMavenProject();
            List<Dependency> dependencies = mavenProject.getDependencies();

            for (Map.Entry<Profile, List<DependencyDesc>> entry : javaEEMap.entrySet()) {
                for (DependencyDesc dependencyDesc : entry.getValue()) {
                    Dependency dependency = checkForDependency(dependencies, dependencyDesc);
                    if (dependency != null) {
                        String version = dependency.getVersion();
                        if (dependencyDesc.version == null || (version != null && version.startsWith(dependencyDesc.version))) {
                            return entry.getKey();
                        }
                    }
                }
            }
        }
        return null;
    }

    private Dependency checkForDependency(List<Dependency> dependencies, DependencyDesc dependencyDesc) {
        if (dependencies != null) {
            for (Dependency dependency : dependencies) {
                if (dependency.getArtifactId().equals(dependencyDesc.artifactID) && dependency.getGroupId().equals(dependencyDesc.groupID)) {
                    return dependency;
                }
            }
        }
        return null;
    }

    @Override
    public File getDDFile(final String path) {
        URI webappDir = mavenproject().getWebAppDirectory();

        // #237478
        if (webappDir.getAuthority() != null) {
            return null;
        }
        File file = new File(new File(webappDir), path);

        return FileUtil.normalizeFile(file);
    }

    @Override
    public FileObject getDeploymentDescriptor() {
        File dd = getDDFile(J2eeModule.WEB_XML);
        if (dd != null) {
            return FileUtil.toFileObject(dd);
        }
        return null;
    }

    @Override
    public String getContextPath() {
        // #170528the javaee6 level might not have a descriptor,
        // but I still keep the check for older versions, as it was known to fail without one
        // in older versions it probably means the web.xml file is generated..
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
        return "/" + mavenproject().getMavenProject().getArtifactId(); //NOI18N;
    }

    public void setContextPath(String newPath) {
        //TODO store as pom profile configuration, probably for the deploy-plugin.
        // #170528 the javaee6 level might not have a descriptor,
        // but I still keep the check for older versions, as it was known to fail without one
        // in older versions it probably means the web.xml file is generated..
        if (getDeploymentDescriptor() != null|| (getJ2eeProfile() != null && getJ2eeProfile().isAtLeast(Profile.JAVA_EE_6_WEB))) {
            try {
                provider.getConfigSupport().setWebContextRoot(newPath);
            }
            catch (ConfigurationException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    @Override
    public String getModuleVersion() {
        WebApp wapp = getWebApp ();
        String version = null;
        if (wapp != null) {
            version = wapp.getVersion();
        }
        if (version == null) {
            version = WebApp.VERSION_4_0;
        }
        return version;
    }

    private WebApp getWebApp () {
        try {
            FileObject deploymentDescriptor = getDeploymentDescriptor ();
            if(deploymentDescriptor != null) {
                return DDProvider.getDefault().getDDRoot(deploymentDescriptor);
            }
        } catch (java.io.IOException e) {
            ErrorManager.getDefault ().log (e.getLocalizedMessage ());
        }
        return null;
    }

    @Override
    public FileObject getContentDirectory() throws IOException {
        FileObject webappFO;
        if (inplace) {
            webappFO = getDocumentBase();
        } else {
            MavenProject mavenProject = mavenproject().getMavenProject();
            String webappLocation = WebProjectUtils.getPluginProperty(project, "webappDirectory"); // NOI18N
            if (webappLocation == null) {
                webappLocation = mavenProject.getBuild().getDirectory() + File.separator + mavenProject.getBuild().getFinalName();
            }
            File webapp = FileUtilities.resolveFilePath(FileUtil.toFile(project.getProjectDirectory()), webappLocation);
            webappFO = FileUtil.toFileObject(webapp);
        }
        if (webappFO != null) {
            webappFO.refresh();
        }
        return webappFO;
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

            File ddFile = ddFO != null ? FileUtil.toFile(ddFO) : null;
            ProjectSourcesClassPathProvider cpProvider = project.getLookup().lookup(ProjectSourcesClassPathProvider.class);
            MetadataUnit metadataUnit = MetadataUnit.create(
                cpProvider.getProjectSourcesClassPath(ClassPath.BOOT),
                cpProvider.getProjectSourcesClassPath(ClassPath.COMPILE),
                cpProvider.getProjectSourcesClassPath(ClassPath.SOURCE),
                ddFile);
            webAppMetadataModel = WebAppMetadataModelFactory.createMetadataModel(metadataUnit, true);
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
