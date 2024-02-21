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

package org.netbeans.modules.apisupport.project;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import static org.netbeans.modules.apisupport.project.NbModuleType.NETBEANS_ORG;
import static org.netbeans.modules.apisupport.project.NbModuleType.STANDALONE;
import static org.netbeans.modules.apisupport.project.NbModuleType.SUITE_COMPONENT;
import org.netbeans.modules.apisupport.project.api.LayerHandle;
import org.netbeans.modules.apisupport.project.api.ManifestManager;
import org.netbeans.modules.apisupport.project.spi.LayerUtil;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.spi.PlatformJarProvider;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.ClusterUtils;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.PlatformLayersCacheManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.XMLFileSystem;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Utilities;

class NbModuleProviderImpl implements NbModuleProvider {

    private final NbModuleProject prj;

    NbModuleProviderImpl(NbModuleProject prj) {
        this.prj = prj;
    }

    @Override public String getSpecVersion() {
        return prj.getSpecVersion();
    }

    @Override public String getCodeNameBase() {
        return prj.getCodeNameBase();
    }

    @Override public String getSourceDirectoryPath() {
        return prj.getSourceDirectoryPath();
    }
    
    @Override public String getTestSourceDirectoryPath() {
        return getResourceDirectoryPath(true);
    }

    @Override public FileObject getSourceDirectory() {
        return prj.getSourceDirectory();
    }

    @Override public FileObject getManifestFile() {
        return prj.getManifestFile();
    }

    @Override public String getResourceDirectoryPath(boolean inTests) {
        return prj.evaluator().getProperty(inTests ? "test.unit.src.dir" : "src.dir");
    }

    @Override public void addDependencies(NbModuleProvider.ModuleDependency[] dependencies) throws IOException {
        for (NbModuleProvider.ModuleDependency dep : dependencies) {
            if (dep.isTestDependency()) {
                ApisupportAntUtils.addTestDependency(prj, dep.getCodeNameBase(), dep.getClusterName());
            } else {
                ApisupportAntUtils.addDependency(prj, dep.getCodeNameBase(), dep.getReleaseVersion(), dep.getVersion(), dep.isUseInCompiler(), dep.getClusterName());
            }
        }
    }

    @Override public void addModulesToTargetPlatform(NbModuleProvider.ModuleDependency[] dependencies) throws IOException {
        if(prj.getModuleType() == NbModuleType.SUITE_COMPONENT) {
            final Project suiteProject = ApisupportAntUtils.getSuiteProject(prj);
            if(suiteProject!=null) {
                final SuiteProperties suiteProps = ApisupportAntUtils.getSuiteProperties((SuiteProject)suiteProject);
                for (NbModuleProvider.ModuleDependency dep : dependencies) {
                    boolean isClusterIncludedInTargetPlatform;
                    if((isClusterIncludedInTargetPlatform = ApisupportAntUtils.isClusterIncludedInTargetPlatform(suiteProps, dep.getClusterName()))
                        && !ApisupportAntUtils.isModuleIncludedInTargetPlatform(suiteProps, dep.getCodeNameBase())) {
                        ApisupportAntUtils.addModuleToTargetPlatform(suiteProject, suiteProps, dep.getCodeNameBase());
                    } else if(!isClusterIncludedInTargetPlatform) {
                        ApisupportAntUtils.addClusterToTargetPlatform(suiteProject, suiteProps, dep.getClusterName(), dep.getCodeNameBase());
                    }
                }
            }
        }
    }

    @Override public SpecificationVersion getDependencyVersion(String codenamebase) throws IOException {
        ModuleEntry entry = prj.getModuleList().getEntry(codenamebase);
        return entry != null ? new SpecificationVersion(entry.getSpecificationVersion()) : null;
    }

    @Override public String getProjectFilePath() {
        return "nbproject/project.xml";
    }

    @Override public File getModuleJarLocation() {
        return prj.getModuleJarLocation();
    }

    @Override public boolean hasDependency(String codeNameBase) throws IOException {
        ProjectXMLManager pxm = new ProjectXMLManager(prj);
        for (org.netbeans.modules.apisupport.project.ModuleDependency d : pxm.getDirectDependencies()) {
            if (d.getModuleEntry().getCodeNameBase().equals(codeNameBase)) {
                return true;
            }
        }
        return false;
    }

    @Override public String getReleaseDirectoryPath() {
        return prj.evaluator().getProperty("release.dir");
    }

    @Override public FileObject getReleaseDirectory() throws IOException {
        return FileUtil.createFolder(prj.getProjectDirectory(), getReleaseDirectoryPath());
    }

    @Override public File getClassesDirectory() {
        return prj.getClassesDirectory();
    }

    @Override public FileSystem getEffectiveSystemFilesystem() throws IOException {
        FileSystem projectLayer = LayerHandle.forProject(prj).layer(false);
        switch (prj.getModuleType()) {
        case STANDALONE:
            Set<File> jars = prj.getLookup().lookup(PlatformJarProvider.class).getPlatformJars();
            NbPlatform plaf = prj.getPlatform(true);
            Collection<FileSystem> platformLayers = getCachedLayers(plaf != null ? plaf.getDestDir() : null, jars);
            return LayerUtil.mergeFilesystems(projectLayer, platformLayers);
        case SUITE_COMPONENT:
            SuiteProject suite = SuiteUtils.findSuite(prj);
            if (suite == null) {
                throw new IOException("Could not load suite for " + prj); // NOI18N
            }
            List<FileSystem> readOnlyLayers = new ArrayList<FileSystem>();
            Set<NbModuleProject> modules = SuiteUtils.getSubProjects(suite);
            for (NbModuleProject sister : modules) {
                if (sister == prj) {
                    continue;
                }
                LayerHandle handle = LayerHandle.forProject(sister);
                FileSystem roLayer = handle.layer(false);
                if (roLayer != null) {
                    readOnlyLayers.add(roLayer);
                }
            }
            plaf = suite.getPlatform(true);
            jars = suite.getLookup().lookup(PlatformJarProvider.class).getPlatformJars();
            readOnlyLayers.addAll(getCachedLayers(plaf != null ? plaf.getDestDir() : null, jars));
            return LayerUtil.mergeFilesystems(projectLayer, readOnlyLayers);
        case NETBEANS_ORG:
            List<URL> otherLayerURLs = new ArrayList<URL>();
            for (NbModuleProject p2 : getProjectsForNetBeansOrgProject()) {
                if (p2.getManifest() == null) {
                    //profiler for example.
                    continue;
                }
                ManifestManager mm = ManifestManager.getInstance(p2.getManifest(), false, true);
                String layer = mm.getLayer();
                if (layer != null) {
                    FileObject src = p2.getSourceDirectory();
                    if (src != null) {
                        FileObject layerXml = src.getFileObject(layer);
                        if (layerXml != null) {
                            otherLayerURLs.add(layerXml.toURL());
                        }
                    }
                }
                layer = mm.getGeneratedLayer();
                if (layer != null) {
                    File layerXml = new File(getClassesDirectory(), layer);
                    if (layerXml.isFile()) {
                        otherLayerURLs.add(Utilities.toURI(layerXml).toURL());
                    }
                }
                // TODO cache
            }
            XMLFileSystem xfs = new XMLFileSystem();
            try {
                xfs.setXmlUrls(otherLayerURLs.toArray(new URL[0]));
            } catch (PropertyVetoException ex) {
                assert false : ex;
            }
            return LayerUtil.mergeFilesystems(projectLayer, Collections.singletonList((FileSystem) xfs));
        default:
            throw new AssertionError();
        }
    }

    private Set<NbModuleProject> getProjectsForNetBeansOrgProject() throws IOException {
        ModuleList list = prj.getModuleList();
        Set<NbModuleProject> projects = new HashSet<NbModuleProject>();
        projects.add(prj);
        for (ModuleEntry other : list.getAllEntries()) {
            if (other.getClusterDirectory().getName().equals("extra")) {
                // NOI18N
                continue;
            }
            File root = other.getSourceLocation();
            assert root != null : other;
            FileObject fo = FileUtil.toFileObject(root);
            if (fo == null) {
                continue; // #142696, project deleted during scan
            }
            NbModuleProject p2;
            try {
                p2 = (NbModuleProject) ProjectManager.getDefault().findProject(fo);
            } catch (IOException x) {
                Logger.getLogger(NbModuleProject.class.getName()).log(Level.INFO, "could not load " + fo, x);
                continue;
            }
            if (p2 == null) {
                continue;
            }
            projects.add(p2);
        }
        return projects;
    }

    /**
     * Returns possibly cached list of filesystems representing the XML layers of the supplied platform module JARs.
     * If cache is not ready yet, this call blocks until the cache is created.
     * Layer filesystems are already ordered to handle masked ("_hidden") files correctly.
     * @param platformJars
     * @return List of read-only layer filesystems
     * @throws java.io.IOException
     */
    private Collection<FileSystem> getCachedLayers(File rootDir, final Set<File> platformJars) throws IOException {
        if (rootDir == null) {
            return Collections.emptySet();
        }
        File[] clusters = rootDir.listFiles(new FileFilter() {
            @Override public boolean accept(File pathname) {
                return ClusterUtils.isValidCluster(pathname);
            }
        });
        Collection<FileSystem> cache = PlatformLayersCacheManager.getCache(clusters, new FileFilter() {
            @Override public boolean accept(File jar) {
                return platformJars.contains(jar);
            }
        });
        return cache;
    }

}
