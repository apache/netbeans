/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
                xfs.setXmlUrls(otherLayerURLs.toArray(new URL[otherLayerURLs.size()]));
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
