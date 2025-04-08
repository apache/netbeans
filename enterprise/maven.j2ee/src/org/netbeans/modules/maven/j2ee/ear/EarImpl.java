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

package org.netbeans.modules.maven.j2ee.ear;

import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Plugin;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluationException;
import org.codehaus.plexus.component.configurator.expression.ExpressionEvaluator;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.netbeans.api.j2ee.core.Profile;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.j2ee.api.ejbjar.Car;
import org.netbeans.modules.j2ee.api.ejbjar.EjbJar;
import org.netbeans.modules.j2ee.dd.api.application.Application;
import org.netbeans.modules.j2ee.dd.api.application.ApplicationMetadata;
import org.netbeans.modules.j2ee.dd.api.application.DDProvider;
import org.netbeans.modules.j2ee.dd.api.common.RootInterface;
import org.netbeans.modules.j2ee.deployment.common.api.EjbChangeDescriptor;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule.RootedEntry;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleChangeReporter;
import org.netbeans.modules.j2ee.deployment.devmodules.api.ModuleListener;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeApplicationImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleFactory;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleImplementation2;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.metadata.model.api.MetadataModel;
import org.netbeans.modules.j2ee.metadata.model.spi.MetadataModelFactory;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation;
import org.netbeans.modules.j2ee.spi.ejbjar.EarImplementation2;
import org.netbeans.modules.javaee.project.api.JavaEEProjectSettings;
import org.netbeans.modules.maven.api.Constants;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.api.PluginPropertyUtils;
import org.netbeans.modules.maven.j2ee.EjbChangeDescriptorImpl;
import org.netbeans.modules.maven.j2ee.ear.model.ApplicationMetadataModelImpl;
import org.netbeans.modules.maven.spi.debug.AdditionalDebuggedProjects;
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.Utilities;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * implementation of ear related netbeans functionality
 * @author Milos Kleint 
 */
public class EarImpl implements EarImplementation, EarImplementation2,
        J2eeApplicationImplementation2,
        ModuleChangeReporter,
        AdditionalDebuggedProjects {

    private Project project;
    private J2eeModuleProvider provider;
    private MetadataModel<ApplicationMetadata> metadataModel;

    public EarImpl(Project project, J2eeModuleProvider provider) {
        this.project = project;
        this.provider = provider;
    }

    private NbMavenProject mavenproject() {
        return project.getLookup().lookup(NbMavenProject.class);
    }

    @Override
    public Profile getJ2eeProfile() {
        Profile profile = JavaEEProjectSettings.getProfile(project);
        if (profile != null) {
            return profile;
        }
        if (isApplicationXmlGenerated()) {
            String version = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS,
                    Constants.PLUGIN_EAR, "version", "generate-application-xml", null); //NOI18N
            //TODO what if the default changes?
            if (version != null) {
                version = version.trim();
                profile = getProfile(version);
                return profile;
            }
        } else {
            DDProvider prov = DDProvider.getDefault();
            FileObject dd = getDeploymentDescriptor();
            if (dd != null) {
                try {
                    Application app = prov.getDDRoot(dd);
                    String appVersion = app.getVersion().toString();
                    appVersion = appVersion.trim();
                    profile = getProfile(appVersion);
                    return profile;
                } catch (IOException exc) {
                    ErrorManager.getDefault().notify(exc);
                }
            } else {
                //TODO try to check the pom model again and user 'version' element if existing..
                return Profile.JAKARTA_EE_8_FULL;
            }
        }
        // hardwire?
//        System.out.println("eariml: getj2eepaltform");
        return Profile.JAKARTA_EE_8_FULL;
    }
    
    /**
     * Get {@code Profile} based on the EAR version written in the pom.xml file
     * @param version
     * @return Profile
     */
    private static Profile getProfile(String version) {
        return switch (version) {
                case "11" -> Profile.JAKARTA_EE_11_FULL;
                case "10" -> Profile.JAKARTA_EE_10_FULL;
                case "9" -> Profile.JAKARTA_EE_9_1_FULL;
                case "8" -> Profile.JAKARTA_EE_8_FULL;
                case "7" -> Profile.JAVA_EE_7_FULL;
                case "6" -> Profile.JAVA_EE_6_FULL;
                case "5" -> Profile.JAVA_EE_5;
                default -> Profile.JAKARTA_EE_8_FULL;
            };
    }

    @Override
    public String getJ2eePlatformVersion() {
        return getJ2eeProfile().toPropertiesString();
    }

    /** META-INF folder for the Ear.
     */
    @Override
    public FileObject getMetaInf() {
        String appsrcloc =  PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_EAR, "earSourceDirectory", "ear", null);//NOI18N
        if (appsrcloc == null) {
            appsrcloc = "src/main/application";//NOI18N
        }
        URI dir = FileUtilities.getDirURI(project.getProjectDirectory(), appsrcloc);
        FileObject root = FileUtilities.convertURItoFileObject(dir);
        if (root == null) {
            final File fil = new File(dir);
            final boolean rootCreated = fil.mkdirs();
            if (rootCreated) {
                project.getProjectDirectory().refresh();
                root = FileUtil.toFileObject(fil);
            }
        }
        if (root != null) {
            FileObject metainf = root.getFileObject("META-INF");//NOI18N
            if (metainf == null) {
                try {
                    metainf = root.createFolder("META-INF");
                } catch (IOException iOException) {
                    Exceptions.printStackTrace(iOException);
                }
            }
            return metainf;
        }
        return null;
    }

    /** Deployment descriptor (application.xml file) of the ejb module.
     */
    @Override
    public FileObject getDeploymentDescriptor() {
        if (isApplicationXmlGenerated()) {
            String generatedLoc = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS,
                    Constants.PLUGIN_EAR, "generatedDescriptorLocation", "generate-application-xml", null);//NOI18N
            if (generatedLoc == null) {
                generatedLoc = mavenproject().getMavenProject().getBuild().getDirectory();
            }
            FileObject fo = FileUtilities.convertURItoFileObject(FileUtilities.getDirURI(project.getProjectDirectory(), generatedLoc));
            if (fo != null) {
                return fo.getFileObject("application.xml");//NOI18N
            } else {
                //TODO maybe run the generate-resources phase to get a DD
//                System.out.println("we don't have the application.xmk generated yet at=" + generatedLoc);
            }
        }
        String customLoc =  PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_EAR, "applicationXml", "ear", null);//NOI18N
        if (customLoc != null) {
            FileObject fo = FileUtilities.convertURItoFileObject(FileUtilities.getDirURI(project.getProjectDirectory(), customLoc));
            if (fo != null) {
                return fo;
            }
        }

        return null;
    }

    /** 
     * Add web module into EAR application.
     *
     * @param module the module to be added
     */
    @Override
    public void addWebModule(WebModule webModule) {
        //TODO this probably means adding the module as dependency to the pom.
        throw new IllegalStateException("Not implemented for maven based projects.");//NOI18N
    }

    /** 
     * Add EJB module into EAR application.
     * 
     * @param module the module to be added
     */
    @Override
    public void addEjbJarModule(EjbJar ejbJar) {
        //TODO this probably means adding the module as dependency to the pom.
        throw new IllegalStateException("Not implemented for maven based projects.");//NOI18N
    }

    private boolean isApplicationXmlGenerated() {
        String str = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS,
                Constants.PLUGIN_EAR,
                "generateApplicationXml", //NOI18N
                "generate-application-xml", null);//NOI18N
        //either the default or explicitly set generation of application.xml file
        return (str == null || Boolean.parseBoolean(str));
    }

    boolean isValid() {
        //TODO how to check and what to check for..
        return true;
    }

    @Override
    public J2eeModule.Type getModuleType() {
        return J2eeModule.Type.EAR;
    }

    /**
     * Returns module specification version
     */
    @Override
    public String getModuleVersion() {
        Profile profile = getJ2eeProfile();
        if (null == profile) {
            return Application.VERSION_8;
        }
        else {
            return switch (profile) {
                case JAKARTA_EE_11_FULL -> Application.VERSION_11;
                case JAKARTA_EE_10_FULL -> Application.VERSION_10;
                case JAKARTA_EE_9_1_FULL, JAKARTA_EE_9_FULL -> Application.VERSION_9;
                case JAKARTA_EE_8_FULL, JAVA_EE_8_FULL -> Application.VERSION_8;
                case JAVA_EE_7_FULL -> Application.VERSION_7;
                case JAVA_EE_6_FULL -> Application.VERSION_6;
                case JAVA_EE_5 -> Application.VERSION_5;
                default -> Application.VERSION_8;
            };
        }
    }

    /**
     * Returns the location of the module within the application archive.
     */
    @Override
    public String getUrl() {
        String toRet =  "/" + mavenproject().getMavenProject().getBuild().getFinalName(); //NOI18N
        return toRet;
    }


    /**
     * Returns the archive file for the module of null if the archive file 
     * does not exist (for example, has not been compiled yet).
     */
    @Override
    public FileObject getArchive() throws IOException {
        //TODO get the correct values for the plugin properties..
        MavenProject proj = mavenproject().getMavenProject();
        String finalName = proj.getBuild().getFinalName();
        String loc = proj.getBuild().getDirectory();
        File fil = FileUtil.normalizeFile(new File(loc, finalName + ".ear"));//NOI18N
//        System.out.println("ear = get archive=" + fil);
        return FileUtil.toFileObject(fil);
    }

    /**
     * Returns the contents of the archive, in copyable form.
     *  Used for incremental deployment.
     *  Currently uses its own {@link RootedEntry} interface.
     *  If the J2eeModule instance describes a
     *  j2ee application, the result should not contain module archives.
     *
     * @return Iterator through {@link RootedEntry}s
     */
    @Override
    public Iterator getArchiveContents() throws IOException {
        //      System.out.println("ear get archive content");
        FileObject fo = getContentDirectory();
        if (fo != null) {
            return new ContentIterator(fo);
        }
        return null;
    }

    /**
     * This call is used in in-place deployment. 
     *  Returns the directory staging the contents of the archive
     *  This directory is the one from which the content entries returned
     *  by {@link #getArchiveContents} came from.
     * 
     * @return FileObject for the content directory
     */
    @Override
    public FileObject getContentDirectory() throws IOException {
        final MavenProject proj = mavenproject().getMavenProject();
        final String finalName = proj.getBuild().getFinalName();
        final String buildDir = proj.getBuild().getDirectory();

        if (finalName != null && buildDir != null) {
            File file = FileUtil.normalizeFile(new File(buildDir, finalName));
            FileObject fo = FileUtil.toFileObject(file);

            if (fo != null) {
                fo.refresh();
            }
            return FileUtil.toFileObject(file);
        }
        return null;
    }

    /**
     * Returns a live bean representing the final deployment descriptor
     * that will be used for deploment of the module. This can be
     * taken from sources, constructed on fly or a combination of these
     * but it needs to be available even if the module has not been built yet.
     * 
     * @param location Parameterized by location because of possibility of multiple 
     * deployment descriptors for a single module (jsp.xml, webservices.xml, etc).
     * Location must be prefixed by /META-INF or /WEB-INF as appropriate.
     * @return a live bean representing the final DD
     */
    public RootInterface getDeploymentDescriptor(String location) {
        if ("application.xml".equals(location)) { //NOI18N
            location = J2eeModule.APP_XML;
        }
        if (J2eeModule.APP_XML.equals(location)) {
            try {

                FileObject content = getDeploymentDescriptor();
                if (content == null) {
//                    System.out.println("getDeploymentDescriptor.application dd is null");
                    String str = 
                            "<application xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\" " + //NOI18N
                            "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " + //NOI18N
                            "xsi:schemaLocation=\"http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/application-client_8.xsd\" version=\"8\">" + //NOI18N
                            "<description>description</description>" +//NOI18N
                            "<display-name>" + mavenproject().getMavenProject().getArtifactId() + "</display-name></application>";//NOI18N
                    try {
                        ByteArrayInputStream bais = new ByteArrayInputStream(str.getBytes(StandardCharsets.UTF_8));
                        return DDProvider.getDefault().getDDRoot(new InputSource(bais));
                    } catch (SAXException | IOException ex) {
                        ex.printStackTrace();
                    }
                } else {
                    return DDProvider.getDefault().getDDRoot(content);
                }
            } catch (IOException e) {
                ErrorManager.getDefault().log(e.getLocalizedMessage());
            }
        }
//        System.out.println("no dd for=" + location);
        return null;
    }

    @Override
    public J2eeModule[] getModules() {
        MavenProject mp = mavenproject().getMavenProject();
        @SuppressWarnings("unchecked")
        Set<Artifact> artifactSet = mp.getArtifacts();
        @SuppressWarnings("unchecked")
        List<Dependency> deps = mp.getRuntimeDependencies();
        String fileNameMapping = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_EAR, "fileNameMapping", "ear", null); //NOI18N
        if (fileNameMapping == null) {
            // EAR maven plugin property was renamed from fileNameMapping to outputFileNameMapping in version 3.0.0
            fileNameMapping = PluginPropertyUtils.getPluginProperty(project, Constants.GROUP_APACHE_PLUGINS, Constants.PLUGIN_EAR, "outputFileNameMapping", "ear", null); //NOI18N
        }
        if (fileNameMapping == null) {
            fileNameMapping = "standard"; //NOI18N
        }

        List<J2eeModule> toRet = new ArrayList<>();
        EarImpl.MavenModule[] mm = readPomModules();
        //#162173 order by dependency list, artifacts is unsorted set.
        for (Dependency d : deps) {
            if ("war".equals(d.getType()) || "ejb".equals(d.getType()) || "app-client".equals(d.getType())) {//NOI18N
                for (Artifact a : artifactSet) {
                    if (a.getGroupId().equals(d.getGroupId()) &&
                            a.getArtifactId().equals(d.getArtifactId()) &&
                            Objects.equals(a.getClassifier(), d.getClassifier())) {
                        URI uri = Utilities.toURI(FileUtil.normalizeFile(a.getFile()));
                        //#174744 - it's of essence we use the URI based method. items in local repo might not be available yet.
                        Project owner = FileOwnerQuery.getOwner(uri);
                        boolean found = false;
                        if (owner != null) {
                            J2eeModuleProvider prov = owner.getLookup().lookup(J2eeModuleProvider.class);
                            if (prov != null) {
                                J2eeModule mod = prov.getJ2eeModule();
                                EarImpl.MavenModule m = findMavenModule(a, mm);
                                J2eeModule module = J2eeModuleFactory.createJ2eeModule(new ProxyJ2eeModule(mod, m, fileNameMapping));
                                //#162173 respect order in pom configuration.. shall we?
                                if (m.pomIndex > -1 && toRet.size() > m.pomIndex) {
                                    toRet.add(m.pomIndex, module);
                                } else {
                                    toRet.add(module);
                                }
                                found = true;
                            }
                        }
                        if (!found) {
                            // FIXME obviously J2EE platform version is wrong and won't really work
                            // if this j2ee module will be used (it has to be module version such as 2.4, 2.5 for web)
                            J2eeModule mod = J2eeModuleFactory.createJ2eeModule(new NonProjectJ2eeModule(a, getJ2eePlatformVersion()));
                            EarImpl.MavenModule m = findMavenModule(a, mm);
                            J2eeModule module = J2eeModuleFactory.createJ2eeModule(new ProxyJ2eeModule(mod, m, fileNameMapping));
                            //#162173 respect order in pom configuration.. shall we?
                            if (m.pomIndex > -1 && toRet.size() > m.pomIndex) {
                                toRet.add(m.pomIndex, module);
                            } else {
                                toRet.add(module);
                            }
                        }
                        break;
                    }
                }
            }
        }
        return toRet.toArray(J2eeModule[]::new);
    }

    @Override
    public List<Project> getProjects() {
        MavenProject mp = mavenproject().getMavenProject();
        @SuppressWarnings("unchecked")
        Set<Artifact> artifactSet = mp.getArtifacts();
        @SuppressWarnings("unchecked")
        List<Dependency> deps = mp.getRuntimeDependencies();
        List<Project> toRet = new ArrayList<>();
        EarImpl.MavenModule[] mm = readPomModules();
        //#162173 order by dependency list, artifacts is unsorted set.
        for (Dependency d : deps) {
            if ("war".equals(d.getType()) || "ejb".equals(d.getType()) || "app-client".equals(d.getType())) {//NOI18N
                for (Artifact a : artifactSet) {
                    if (a.getGroupId().equals(d.getGroupId()) &&
                            a.getArtifactId().equals(d.getArtifactId()) &&
                            Objects.equals(a.getClassifier(), d.getClassifier())) {
                        URI uri = Utilities.toURI(FileUtil.normalizeFile(a.getFile()));
                        //#174744 - it's of essence we use the URI based method. items in local repo might not be available yet.
                        Project owner = FileOwnerQuery.getOwner(uri);
                        if (owner != null) {
                            EarImpl.MavenModule m = findMavenModule(a, mm);
                            //#162173 respect order in pom configuration.. shall we?
                            if (m.pomIndex > -1 && toRet.size() > m.pomIndex) {
                                toRet.add(m.pomIndex, owner);
                            } else {
                                toRet.add(owner);
                            }
                            
                        }
                    }
                }
            }
        }
        // This might happened if someone calls getProjects() before the EAR childs were actually opened
        // Typically if childs are needed during the project creation
        if (toRet.isEmpty()) {
            FileObject parentFO = project.getProjectDirectory().getParent();
            for (FileObject childFO : parentFO.getChildren()) {
                if (childFO.isData()) {
                    continue;
                }
                
                try {
                    Project childProject = ProjectManager.getDefault().findProject(childFO);
                    if (childProject != null && !childProject.equals(project)) {
                        toRet.add(childProject);
                    }
                } catch (IOException | IllegalArgumentException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }
        return toRet;
    }


    File getDDFile(String path) {
        //TODO what is the actual path.. sometimes don't have any sources for deployment descriptors..
        URI dir = mavenproject().getEarAppDirectory();
        File fil = new File(new File(dir), path);
        if (!fil.getParentFile().exists()) {
            fil.getParentFile().mkdirs();
        }
        fil = FileUtil.normalizeFile(fil);
        return fil;
    }


    @Override
    public void addModuleListener(ModuleListener ml) {
    }

    @Override
    public void removeModuleListener(ModuleListener ml) {
    }

    @Override
    public EjbChangeDescriptor getEjbChanges(long timestamp) {
        return new EjbChangeDescriptorImpl();
    }

    @Override
    public boolean isManifestChanged(long timestamp) {
        return false;
    }

    @Override
    public void addCarModule(Car arg0) {
        throw new UnsupportedOperationException("Not supported yet.");//NOI18N
    }


    private static final class ContentIterator implements Iterator {

        private final List<FileObject> filesUnderRoot;
        private final FileObject root;


        private ContentIterator(FileObject root) {
            this.root = root;

            filesUnderRoot = new ArrayList<FileObject>();
            filesUnderRoot.add(root);
        }

        @Override
        public boolean hasNext() {
            return !filesUnderRoot.isEmpty();
        }

        @Override
        public Object next() {
            FileObject nextFile = filesUnderRoot.get(0);
            filesUnderRoot.remove(0);
            if (nextFile.isFolder()) {
                nextFile.refresh();
                filesUnderRoot.addAll(Arrays.asList(nextFile.getChildren()));
            }
            return new RootedFileObject(root, nextFile);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class RootedFileObject implements J2eeModule.RootedEntry {

        private final FileObject file;
        private final FileObject root;


        private RootedFileObject(FileObject root, FileObject file) {
            this.file = file;
            this.root = root;
        }

        @Override
        public FileObject getFileObject() {
            return file;
        }

        @Override
        public String getRelativePath() {
            return FileUtil.getRelativePath(root, file);
        }
    }

    /**
     * Returns the module resource directory, or null if the module has no resource
     * directory.
     * 
     * @return the module resource directory, or null if the module has no resource
     *         directory.
     */

    @Override
    public File getResourceDirectory() {
        //TODO .. in ant projects equals to "setup" directory.. what's it's use?
        File toRet = new File(FileUtil.toFile(project.getProjectDirectory()), "src" + File.separator + "main" + File.separator + "setup");//NOI18N
        return toRet;
    }

    /**
     * Returns source deployment configuration file path for the given deployment 
     * configuration file name.
     *
     * @param name file name of the deployment configuration file, WEB-INF/sun-web.xml
     *        for example.
     * 
     * @return absolute path to the deployment configuration file, or null if the
     *         specified file name is not known to this J2eeModule.
     */
    @Override
    public File getDeploymentConfigurationFile(String name) {
        if (name == null) {
            return null;
        }
        String path = provider.getConfigSupport().getContentRelativePath(name);
        if (path == null) {
            path = name;
        }
        return getDDFile(path);
    }

    /**
     * Add a PropertyChangeListener to the listener list.
     * 
     * @param listener PropertyChangeListener
     */
    @Override
    public void addPropertyChangeListener(PropertyChangeListener arg0) {
        //TODO..
    }

    /**
     * Remove a PropertyChangeListener from the listener list.
     * 
     * @param listener PropertyChangeListener
     */
    @Override
    public void removePropertyChangeListener(PropertyChangeListener arg0) {
        //TODO..
    }

    /**
     * Get metadata model of enterprise application.
     */
    public synchronized MetadataModel<ApplicationMetadata> getMetadataModel() {
        if (metadataModel == null) {
            metadataModel = MetadataModelFactory.createMetadataModel(new ApplicationMetadataModelImpl(project));
        }
        return metadataModel;
    }


    @Override
    public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
        if (type == ApplicationMetadata.class) {
            @SuppressWarnings("unchecked") // NOI18N
            MetadataModel<T> model = (MetadataModel<T>)getMetadataModel();
            return model;
        }
        return null;
    }

    private EarImpl.MavenModule findMavenModule(Artifact art, EarImpl.MavenModule[] mm) {
        EarImpl.MavenModule toRet = null;
        for (EarImpl.MavenModule m : mm) {
            if (art.getGroupId().equals(m.groupId) && art.getArtifactId().equals(m.artifactId)) {
                m.artifact = art;
                toRet = m;
                break;
            }
        }
        if (toRet == null) {
            toRet = new EarImpl.MavenModule();
            toRet.artifact = art;
            toRet.groupId = art.getGroupId();
            toRet.artifactId = art.getArtifactId();
            toRet.classifier = art.getClassifier();
            //add type as well?
        }
        return toRet;
    }

    private EarImpl.MavenModule[] readPomModules() {
        MavenProject prj = mavenproject().getMavenProject();
        MavenModule[] toRet = new MavenModule[0];
        if (prj.getBuildPlugins() == null) {
            return toRet;
        }
        for (Object obj : prj.getBuildPlugins()) {
            Plugin plug = (Plugin)obj;
            if (Constants.PLUGIN_EAR.equals(plug.getArtifactId()) &&
                    Constants.GROUP_APACHE_PLUGINS.equals(plug.getGroupId())) {
                   toRet =  checkConfiguration(prj, plug.getConfiguration());
            }
        }
        if (toRet == null) {  //NOI18N
            if (prj.getPluginManagement() != null) {
                for (Object obj : prj.getPluginManagement().getPlugins()) {
                    Plugin plug = (Plugin)obj;
                    if (Constants.PLUGIN_EAR.equals(plug.getArtifactId()) &&
                            Constants.GROUP_APACHE_PLUGINS.equals(plug.getGroupId())) {
                        toRet = checkConfiguration(prj, plug.getConfiguration());
                        break;
                    }
                }
            }
        }
        return toRet;
    }

    private MavenModule[] checkConfiguration(MavenProject prj, Object conf) {
        List<MavenModule> toRet = new ArrayList<>();
        if (conf instanceof Xpp3Dom xpp3Dom) {
            ExpressionEvaluator eval = PluginPropertyUtils.createEvaluator(project);
            Xpp3Dom dom = xpp3Dom;
            Xpp3Dom modules = dom.getChild("modules"); //NOI18N
            if (modules != null) {
                int index = 0;
                for (Xpp3Dom module : modules.getChildren()) {
                    MavenModule mm = new MavenModule();
                    mm.type = module.getName();
                    if (module.getChildren() != null) {
                        for (Xpp3Dom param : module.getChildren()) {
                            String value = param.getValue();
                            if (value == null) {
                                continue;
                            }
                            try {
                                Object evaluated = eval.evaluate(value.trim());
                                value = evaluated != null ? ("" + evaluated) : value.trim();  //NOI18N
                            } catch (ExpressionEvaluationException e) {
                                //log silently
                            }
                            if (null != param.getName()) {
                                switch (param.getName()) {
                                    case "groupId" -> mm.groupId = value;
                                    case "artifactId" -> mm.artifactId = value;
                                    case "classifier" -> mm.classifier = value;
                                    case "uri" -> mm.uri = value;
                                    case "bundleDir"-> mm.bundleDir = value;
                                    case "bundleFileName"-> mm.bundleFileName = value;
                                    case "excluded"-> mm.excluded = Boolean.parseBoolean(value);
                                }
                            }
                        }
                    }
                    mm.pomIndex = index;
                    index++;
                    toRet.add(mm);
                }
            }
        }
        return toRet.toArray(MavenModule[]::new);
    }

    private static class MavenModule {
        String uri;
        Artifact artifact;
        String groupId;
        String artifactId;
        String type;
        String classifier;
        String bundleDir;
        String bundleFileName;
        int pomIndex = -1;
        boolean excluded = false;


        String resolveUri(String fileNameMapping) {
            if (uri != null) {
                return uri;
            }
            String bDir = resolveBundleDir();
            return "/" + bDir + resolveBundleName(fileNameMapping);
        }

        String resolveBundleDir() {
            String toRet = bundleDir;
            if (toRet != null) {
                // Using slashes
                toRet = toRet.replace('\\', '/'); //NOI18N

                // Remove '/' prefix if any so that directory is a relative path
                if (toRet.startsWith("/")) { //NOI18N
                    toRet = toRet.substring(1);
                }

                if (toRet.length() > 0 && !toRet.endsWith("/")) { //NOI18N
                    // Adding '/' suffix to specify a directory structure if it is not empty
                    toRet = toRet + "/"; //NOI18N
                }
                return toRet;

            }
            return "";
        }

        String resolveBundleName(String fileNameMapping) {
            if (bundleFileName != null) {
                return bundleFileName;
            }
            if ("standard".equals(fileNameMapping)) { //NOI18N
                return artifact.getFile().getName();
            }
            if ("full".equals(fileNameMapping)) { //NOI18N
                final String dashedGroupId = groupId.replace( '.', '-'); //NOI18N
                return dashedGroupId + "-" + artifact.getFile().getName(); //NOI18N
            }
            if ("no-version".equals(fileNameMapping)) {
                final String version = "-" + artifact.getBaseVersion();      //NOI18N
                return artifact.getFile().getName().replaceAll(version, ""); //NOI18N
            }
            //TODO it seems the fileNameMapping can also be a class (from ear-maven-plugin's classpath
            // of type FileNameMapping that resolves the name.. we ignore it for now.. not common usecase anyway..
            return artifact.getFile().getName();
        }

    }

    private static class ProxyJ2eeModule implements J2eeModuleImplementation2 {
        private final J2eeModule module;
        private final EarImpl.MavenModule mavenModule;
        private final String fileNameMapping;

        ProxyJ2eeModule(J2eeModule module, EarImpl.MavenModule mavModule, String fileNameMapping) {
            this.mavenModule = mavModule;
            this.module = module;
            this.fileNameMapping = fileNameMapping;
        }

        @Override
        public String getModuleVersion() {
            return module.getModuleVersion();
        }

        @Override
        public J2eeModule.Type getModuleType() {
            return module.getType();
        }

        @Override
        public String getUrl() {
            return mavenModule.resolveUri(fileNameMapping);
        }

        @Override
        public FileObject getArchive() throws IOException {
            return module.getArchive();
        }

        @Override
        public Iterator<J2eeModule.RootedEntry> getArchiveContents() throws IOException {
            return module.getArchiveContents();
        }

        @Override
        public FileObject getContentDirectory() throws IOException {
            return module.getContentDirectory();
        }

        @Override
        public <T> MetadataModel<T> getMetadataModel(Class<T> type) {
            return module.getMetadataModel(type);
        }

        @Override
        public File getResourceDirectory() {
            return module.getResourceDirectory();
        }

        @Override
        public File getDeploymentConfigurationFile(String name) {
            return module.getDeploymentConfigurationFile(name);
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            module.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            module.removePropertyChangeListener(listener);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final ProxyJ2eeModule other = (ProxyJ2eeModule) obj;
            return Objects.equals(this.module, other.module);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 31 * hash + Objects.hashCode(this.module);
            hash = 31 * hash + Objects.hashCode(this.mavenModule);
            hash = 31 * hash + Objects.hashCode(this.fileNameMapping);
            return hash;
        }

    }

}
