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

package org.netbeans.modules.maven.apisupport;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.xml.namespace.QName;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.queries.CollocationQuery;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.spi.LayerUtil;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.spi.PlatformJarProvider;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.modules.maven.embedder.EmbedderFactory;
import org.netbeans.modules.maven.indexer.api.NBVersionInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryInfo;
import org.netbeans.modules.maven.indexer.api.RepositoryPreferences;
import org.netbeans.modules.maven.indexer.api.RepositoryQueries;
import org.netbeans.modules.maven.model.ModelOperation;
import org.netbeans.modules.maven.model.Utilities;
import org.netbeans.modules.maven.model.pom.Build;
import org.netbeans.modules.maven.model.pom.Configuration;
import org.netbeans.modules.maven.model.pom.POMExtensibilityElement;
import org.netbeans.modules.maven.model.pom.POMModel;
import org.netbeans.modules.maven.model.pom.Plugin;
import org.netbeans.modules.maven.options.MavenVersionSettings;
import org.netbeans.spi.project.AuxiliaryProperties;
import org.netbeans.spi.project.ProjectServiceProvider;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.XMLFileSystem;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author mkleint
 */
@ProjectServiceProvider(service=NbModuleProvider.class, projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM)
public class MavenNbModuleImpl implements NbModuleProvider {

    
    private final Project project;
    private final DependencyAdder dependencyAdder = new DependencyAdder();

    public static final String MAVEN_CENTRAL = "central";
    public static final String APACHE_SNAPSHOT_REPO_ID = "apache.snapshots";
    // this repository is not good anymore, dev-SNAPSHOT version are buil on apache snapshot
    // netbeans-snapshot contains "8.3-dev"  public static final String NETBEANS_SNAPSHOT_REPO_ID = "netbeans-snapshot";
    /**
     * the property defined by nbm-maven-plugin's run-ide goal.
     * can help finding the defined netbeans platform.
     */ 
    private static final String PROP_NETBEANS_INSTALL = "netbeans.installation"; //NOI18N

    public static final String GROUPID_MOJO = "org.codehaus.mojo";
    public static final String GROUPID_APACHE = "org.apache.netbeans.utilities";
    public static final String NBM_PLUGIN = "nbm-maven-plugin";

    public static final String NETBEANSAPI_GROUPID = "org.netbeans.api";

    /** Creates a new instance of MavenNbModuleImpl 
     * @param project 
     */
    public MavenNbModuleImpl(Project project) {
        this.project = project;
    }

    static List<RepositoryInfo> netbeansRepo() {
        return Arrays.asList(
                RepositoryPreferences.getInstance().getRepositoryInfoById(MAVEN_CENTRAL));
    }

    /**
     * Returns the latest known version of the NetBeans maven plugin which is not a SNAPSHOT release.
     * This method will not wait for the index to be downloaded, it will return a default value instead.
     */
    public static String getLatestNbmPluginVersion() {
        return MavenVersionSettings.getDefault().getVersion(GROUPID_APACHE, NBM_PLUGIN);
    }

    private File getModuleXmlLocation() {
        String file = PluginBackwardPropertyUtils.getPluginProperty(project, 
                    "descriptor", null, null); //NOI18N
        if (file == null) {
            file = "src/main/nbm/module.xml"; //NOI18N
        }
        File rel = new File(file);
        if (!rel.isAbsolute()) {
            rel = new File(FileUtil.toFile(project.getProjectDirectory()), file);
        }
        return FileUtil.normalizeFile(rel);
    }
    
    private Xpp3Dom getModuleDom() throws IOException, XmlPullParserException {
        //TODO convert to FileOBject and have the IO stream from there..
        File file = getModuleXmlLocation();
        if (!file.exists()) {
            return null;
        }
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            return Xpp3DomBuilder.build(reader);
        }
    }
    
    @Override
    public String getSpecVersion() {
        NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
        String specVersion = AdaptNbVersion.adaptVersion(watch.getMavenProject().getVersion(), AdaptNbVersion.TYPE_SPECIFICATION);
        return specVersion;
    }

    @Override
    public String getCodeNameBase() {
        String codename = PluginBackwardPropertyUtils.getPluginProperty(project, 
                    "codeNameBase", "manifest", null);
        if (codename == null) {
            //this is deprecated in 3.8, but kept around for older versions
            try {
                Xpp3Dom dom = getModuleDom();
                if (dom != null) {
                    Xpp3Dom cnb = dom.getChild("codeNameBase"); //NOI18N
                    if (cnb != null) {
                        String val = cnb.getValue();
                        int slash = val.indexOf('/');
                        if (slash > -1) {
                            val = val.substring(0, slash);
                        }
                        return val;
                    }
                }
            } catch (IOException | XmlPullParserException e) {
                e.printStackTrace();
            }
            MavenProject prj = project.getLookup().lookup(NbMavenProject.class).getMavenProject();
            //same fallback is in nbm-maven-plugin, keep it synchronized with codeNameBase parameter
            codename = prj.getGroupId() + "." + prj.getArtifactId(); //NOI18N
            codename = codename.replace( "-", "." ); //NOI18N
        }
        return codename;
    }

    @Override
    public String getSourceDirectoryPath() {
        //TODO
        return "src/main/java"; //NOI18N
    }
    
    @Override
    public String getTestSourceDirectoryPath() {
        //TODO
        return "src/test/java"; //NOI18N
    }

    @Override
    public FileObject getSourceDirectory() {
        FileObject fo = project.getProjectDirectory().getFileObject(getSourceDirectoryPath());
        if (fo == null) {
            try {
                fo = FileUtil.createFolder(project.getProjectDirectory(),
                                           getSourceDirectoryPath());
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return fo;
    }

    @Override
    public FileObject getManifestFile() {
        String manifest = PluginBackwardPropertyUtils.getPluginProperty(project, 
                    "sourceManifestFile", "manifest", null);
        if (manifest != null) {
            return FileUtilities.convertStringToFileObject(manifest);
        }
        String path = "src/main/nbm/manifest.mf";  //NOI18N

        try {
            Xpp3Dom dom = getModuleDom();
            if (dom != null) {
                Xpp3Dom cnb = dom.getChild("manifest"); //NOI18N
                if (cnb != null) {
                    path = cnb.getValue();
                }
            }
        } catch (IOException | XmlPullParserException e) {
            e.printStackTrace();
        }
        return project.getProjectDirectory().getFileObject(path);
    }

    @Override
    public String getResourceDirectoryPath(boolean isTest) {
        NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
        List<Resource> res;
        String defaultValue;
        
        if (isTest) {
            res = watch.getMavenProject().getTestResources();           
            defaultValue = "src/test/resources"; //NOI18N
        } else {
            res = watch.getMavenProject().getResources();
            defaultValue = "src/main/resources"; //NOI18N
        }
        for (Resource resource : res) {
            FileObject fo = FileUtilities.convertStringToFileObject(resource.getDirectory());
            if (fo != null && FileUtil.isParentOf(project.getProjectDirectory(), fo)) {
                return FileUtil.getRelativePath(project.getProjectDirectory(), fo);
            }
        }
        return defaultValue;
    }

    @Override
    public void addDependencies(NbModuleProvider.ModuleDependency[] dependencies) throws IOException {
        for (NbModuleProvider.ModuleDependency mdep : dependencies) {
        String codeNameBase = mdep.getCodeNameBase();
        SpecificationVersion version = mdep.getVersion();
        String artifactId = codeNameBase.replace(".", "-"); //NOI18N
        NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
        if (hasDependency(codeNameBase)) {
            //TODO
            //not sure we ought to check for spec or release version.
            // just ignore for now, not any easy way to upgrade anyway I guess.
            continue;
        }
        Dependency dep = null;
        List<RepositoryInfo> nbrepo = netbeansRepo();
        if (nbrepo != null) {
            File platformFile = lookForModuleInPlatform(artifactId);
            if (platformFile != null) {
                List<NBVersionInfo> lst = RepositoryQueries.findBySHA1Result(platformFile, Collections.unmodifiableList(nbrepo)).getResults();
                for (NBVersionInfo elem : lst) {
                    dep = new Dependency();
                    dep.setArtifactId(elem.getArtifactId());
                    dep.setGroupId(elem.getGroupId());
                    dep.setVersion(elem.getVersion());
                    break;
                }
            }
        }
        if (dep == null) {
            //TODO try to guess 
            dep = new Dependency();
            dep.setGroupId(NETBEANSAPI_GROUPID); //NOI18N
            dep.setArtifactId(artifactId);
            if (version != null) {
                dep.setVersion(version.toString());
            } else {
                //try guessing the version according to the rest of netbeans dependencies..
                for (Dependency d : watch.getMavenProject().getModel().getDependencies()) {
                    if (NETBEANSAPI_GROUPID.equals(d.getGroupId())) { // NOI18N
                        dep.setVersion(d.getVersion());
                    }
                }
            }
        }
        if (dep.getVersion() == null) {
            if (nbrepo != null) {
                List<NBVersionInfo> versions = RepositoryQueries.getVersionsResult("org.netbeans.cluster", "platform", Collections.unmodifiableList(nbrepo)).getResults();
                if (!versions.isEmpty()) {
                    dep.setVersion(versions.get(0).getVersion());
                }
            }
        }
        if (dep.getVersion() == null) {
            dep.setVersion("99.99"); // NOI18N
        }
        if (mdep.isTestDependency()) {
            dep.setScope("test");
        }
        //#214674 heuristics to set the right expression if matching..
        MavenProject mp = watch.getMavenProject();
        String nbVersion = mp.getProperties() != null ? mp.getProperties().getProperty("netbeans.version") : null;
        if (nbVersion != null && nbVersion.equals(dep.getVersion())) {
            dep.setVersion("${netbeans.version}");
        }
        dependencyAdder.addDependency(dep);
        
        }
        dependencyAdder.run();
    }


    @Override public void addModulesToTargetPlatform(NbModuleProvider.ModuleDependency[] dependencies) throws IOException {
        
    }

    /**
     * 6.7 and higher apisupport uses this to add projects to Libraries for suite.
     *
     * Cannot use Maven-based apisupport projects this way as it doesn't build
     * modules into clusters. Workaround is to unpack resulting NBM somewhere
     * and add it as an external binary cluster.
     * @return null
     */
    @Override
    public File getModuleJarLocation() {
        return null;
    }

    public @Override boolean hasDependency(String codeNameBase) throws IOException {
        String artifactId = codeNameBase.replace(".", "-"); //NOI18N
        NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
        Set<Artifact> set = watch.getMavenProject().getDependencyArtifacts();
        if (set != null) {
            for (Artifact art : set) {
                if (art.getGroupId().startsWith("org.netbeans") && art.getArtifactId().equals(artifactId)) { // NOI18N
                    return true;
                }
            }
        }
        return false;
    }

    public @Override String getReleaseDirectoryPath() {
        return "src/main/release";
    }

    public @Override FileObject getReleaseDirectory() throws IOException {
        Utilities.performPOMModelOperations(project.getProjectDirectory().getFileObject("pom.xml"), Collections.<ModelOperation<POMModel>>singletonList(new ModelOperation<POMModel>() {
            public @Override void performOperation(POMModel model) {
                Build build = model.getProject().getBuild();
                if (build != null) {
                    // look at apache netbeans plugin first
                    Plugin nbmPlugin = PluginBackwardPropertyUtils.findPluginFromBuild(build);
                    if (nbmPlugin != null) {
                        Configuration configuration = nbmPlugin.getConfiguration();
                        if (configuration == null) {
                            configuration = model.getFactory().createConfiguration();
                            nbmPlugin.setConfiguration(configuration);
                        }
                        POMExtensibilityElement resources = ModelUtils.getOrCreateChild(configuration, "nbmResources", model);
                        boolean needed = true;
                        NEEDED: for (POMExtensibilityElement configurationElement : resources.getExtensibilityElements()) {
                            if (configurationElement.getQName().getLocalPart().equals("nbmResource")) {
                                for (POMExtensibilityElement dir : configurationElement.getExtensibilityElements()) {
                                    if (dir.getQName().getLocalPart().equals("directory")) {
                                        if (dir.getElementText().equals(getReleaseDirectoryPath())) {
                                            needed = false;
                                            break NEEDED;
                                        }
                                    }
                                }
                            }
                        }
                        if (needed) {
                            POMExtensibilityElement dir = model.getFactory().createPOMExtensibilityElement(new QName("directory"));
                            dir.setElementText(getReleaseDirectoryPath());
                            POMExtensibilityElement res = model.getFactory().createPOMExtensibilityElement(new QName("nbmResource"));
                            res.addExtensibilityElement(dir);
                            resources.addExtensibilityElement(res);
                        }
                    }
                }
            }
        }));
        return FileUtil.createFolder(project.getProjectDirectory(), getReleaseDirectoryPath());
    }
    
    public @Override File getClassesDirectory() {
        return new File(project.getLookup().lookup(NbMavenProject.class).getMavenProject().getBuild().getOutputDirectory());
    }
    
    private class DependencyAdder implements Runnable {
        List<Dependency> toAdd = new ArrayList<>();
        
        private synchronized void addDependency(Dependency dep) {
            toAdd.add(dep);
        }
        
        @Override
        public void run() {
            FileObject fo = project.getProjectDirectory().getFileObject("pom.xml"); //NOI18N
            final DependencyAdder monitor = this;
            ModelOperation<POMModel> operation = new ModelOperation<POMModel>() {
                @Override
                public void performOperation(POMModel model) {
                    synchronized (monitor) {
                        for (Dependency dep : toAdd) {
                            org.netbeans.modules.maven.model.pom.Dependency mdlDep =
                                    ModelUtils.checkModelDependency(model, dep.getGroupId(), dep.getArtifactId(), true);
                            mdlDep.setVersion(dep.getVersion());
                            if (dep.getScope() != null) {
                                mdlDep.setScope(dep.getScope());
                            }
                        }
                        toAdd.clear();
                    }
                }
            };
            Utilities.performPOMModelOperations(fo, Collections.singletonList(operation));
            project.getLookup().lookup(NbMavenProject.class).synchronousDependencyDownload();
        }
    }
            
    @Override
    public String getProjectFilePath() {
        return "pom.xml"; //NOI18N
    }

    /**
     * get specification version for the given module.
     * The module isn't necessary a project dependency, more a property of the associated 
     * netbeans platform.
     */ 
    @Override
    public SpecificationVersion getDependencyVersion(String codenamebase) throws IOException {
        String artifactId = codenamebase.replace(".", "-"); //NOI18N
        NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
        for (Artifact art : watch.getMavenProject().getArtifacts()) {
            if (art.getGroupId().startsWith("org.netbeans") && art.getArtifactId().equals(artifactId)) { //NOI18N
                File jar = art.getFile();
                if (jar.isFile()) {
                ExamineManifest exa = new ExamineManifest();
                exa.setJarFile(jar);
                try {
                    exa.checkFile();
                } catch (MojoExecutionException x) {
                    throw new IOException(x);
                }
                if (exa.getSpecVersion() != null) {
                    return new SpecificationVersion(exa.getSpecVersion());
                }
                }
            }
        }
        // #190149: look up artifact in repo with same version as some existing org.netbeans.api:* dep
        for (Artifact art : watch.getMavenProject().getArtifacts()) {
            if (art.getGroupId().startsWith("org.netbeans")) { // NOI18N
                Artifact art2 = EmbedderFactory.getProjectEmbedder().getLocalRepository().find(
                        new DefaultArtifact(NETBEANSAPI_GROUPID, artifactId, art.getVersion(), null, "jar", null, new DefaultArtifactHandler("jar"))); // NOI18N
                File jar = art2.getFile();
                if (jar != null && jar.isFile()) {
                    ExamineManifest exa = new ExamineManifest();
                    exa.setJarFile(jar);
                    try {
                        exa.checkFile();
                    } catch (MojoExecutionException x) {
                        throw new IOException(x);
                    }
                    if (exa.getSpecVersion() != null) {
                        return new SpecificationVersion(exa.getSpecVersion());
                    }
                }
            }
        }
        File fil = lookForModuleInPlatform(artifactId);
        if (fil != null) {
            ExamineManifest exa = new ExamineManifest();
            exa.setJarFile(fil);
            try {
                exa.checkFile();
            } catch (MojoExecutionException x) {
                throw new IOException(x);
            }
            if (exa.getSpecVersion() != null) {
                return new SpecificationVersion(exa.getSpecVersion());
            }
        }
        //TODO search local repository?? that's probably irrelevant here..
        
        //we're completely clueless.
        return null;
    }
    
    private File lookForModuleInPlatform(String artifactId) {
        File actPlatform = getActivePlatformLocation();
        if (actPlatform != null) {
            DirectoryScanner walk = new DirectoryScanner();
            walk.setBasedir(actPlatform);
            walk.setIncludes(new String[] {
                "**/" + artifactId + ".jar" //NOI18N
            });
            walk.scan();
            String[] candidates = walk.getIncludedFiles();
            assert candidates != null && candidates.length <= 1;
            if (candidates.length > 0) {
                return new File(actPlatform, candidates[0]);
            }
        }
        return null;
    }

    /**
     * get the NetBeans platform for the module
     * @return location of the root directory of NetBeans platform installation
     */
    private File getActivePlatformLocation() {
        File platformDir = findPlatformFolder();
        if (platformDir != null && platformDir.isDirectory()) {
            return platformDir;
        }
        platformDir = findIDEInstallation(project);
        if (platformDir != null && platformDir.isDirectory()) {
            return platformDir;
        }
        return null;
    }

    /**
     * Looks for the configured location of the IDE installation for a standalone or suite module.
     */
    static @CheckForNull File findIDEInstallation(Project project) {
        String installProp = PluginBackwardPropertyUtils.getPluginProperty(project, "netbeansInstallation", "run-ide", PROP_NETBEANS_INSTALL);
        if (installProp != null) {
            return FileUtilities.convertStringToFile(installProp);
        } else {
            return null;
        }
    }

    static Project findAppProject(Project nbmProject) {
        NbMavenProject mp = nbmProject.getLookup().lookup(NbMavenProject.class);
        if (mp == null) {
            return null;
        }
        String groupId = mp.getMavenProject().getGroupId();
        String artifactId = mp.getMavenProject().getArtifactId();
        List<Project> candidates = new ArrayList<>();
        for (Project p : OpenProjects.getDefault().getOpenProjects()) {
            NbMavenProject mp2 = p.getLookup().lookup(NbMavenProject.class);
            if (mp2 != null && NbMavenProject.TYPE_NBM_APPLICATION.equals(mp2.getPackagingType())) {
                for (Artifact dep : mp2.getMavenProject().getArtifacts()) {
                    if (dep.getGroupId().equals(groupId) && dep.getArtifactId().equals(artifactId)) {
                        candidates.add(p);
                    }
                }
            }
        }
        int size = candidates.size();
        if (size == 1) {
            return candidates.get(0);
        }
        //#242147
        if (size > 1) {
            //heuristic storm
            //1. similar path? colocation?
            List<Project> colocated = new ArrayList<>();
            URI moduleUri = nbmProject.getProjectDirectory().toURI();
            for (Project p : candidates) {
                if (CollocationQuery.areCollocated(moduleUri, p.getProjectDirectory().toURI())) {
                    colocated.add(p);
                }
            }
            if (colocated.size() == 1) {
                return colocated.get(0);
            }
            //2. what other options do we have? #242147
        }
        return null;
    }
    @ProjectServiceProvider(service=ProjectOpenedHook.class, projectType="org-netbeans-modules-maven/" + NbMavenProject.TYPE_NBM)
    public static class RemoveOldPathToNbApplicationModule extends ProjectOpenedHook {
        private final Project p;
        public RemoveOldPathToNbApplicationModule(Project p) {
            this.p = p;
        }
        protected @Override void projectOpened() {
            AuxiliaryProperties aux = p.getLookup().lookup(AuxiliaryProperties.class);
            if (aux != null) {
                aux.put("pathToNbApplicationModule", null, true);
            }
        }
        protected @Override void projectClosed() {}
    }

    private File findPlatformFolder() {
            Project appProject = findAppProject(project);
            if (appProject == null) {
                //not a project directory.
                return null;
            }
            NbMavenProject watch = appProject.getLookup().lookup(NbMavenProject.class);
            if (watch == null) {
                return null; //not a maven project.
            }
            String outputDir = PluginBackwardPropertyUtils.getPluginProperty(appProject,
                    "outputDirectory", "cluster-app", null); //NOI18N
            if( null == outputDir ) {
                outputDir = "target"; //NOI18N
            }
            
            String brandingToken = PluginBackwardPropertyUtils.getPluginProperty(appProject,
                    "brandingToken", "cluster-app", "netbeans.branding.token"); //NOI18N
            return FileUtilities.resolveFilePath(FileUtil.toFile(appProject.getProjectDirectory()), outputDir + File.separator + brandingToken);
    }

    @Override public FileSystem getEffectiveSystemFilesystem() throws IOException {
        FileSystem projectLayer = LayerHandle.forProject(project).layer(false);
        Collection<FileSystem> platformLayers = new ArrayList<>();
        PlatformJarProvider pjp = project.getLookup().lookup(PlatformJarProvider.class);
        if (pjp != null) {
            List<URL> urls = new ArrayList<>();
            for (File jar : pjp.getPlatformJars()) {
                // XXX use LayerHandle.forProject on this and sister modules instead
                urls.addAll(LayerUtil.layersOf(jar));
            }
            XMLFileSystem xmlfs = new XMLFileSystem();
            try {
                xmlfs.setXmlUrls(urls.toArray(new URL[0]));
            } catch (PropertyVetoException x) {
                throw new IOException(x);
            }
            platformLayers.add(xmlfs);
        }
        // XXX would using PlatformLayersCacheManager be beneficial? (would need to modify in several ways)
        return LayerUtil.mergeFilesystems(projectLayer, platformLayers);
    }

}
