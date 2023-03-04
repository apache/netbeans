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

package org.netbeans.modules.apisupport.project.suite;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.ProjectXMLManager;
import org.netbeans.modules.apisupport.project.spi.BrandingModel;
import org.netbeans.modules.apisupport.project.ui.customizer.ClusterInfo;
import org.netbeans.modules.apisupport.project.universe.ClusterUtils;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteCustomizerLibraries;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteProperties;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.universe.ModuleEntry;
import org.netbeans.modules.apisupport.project.universe.ModuleList;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.netbeans.modules.apisupport.project.universe.HarnessVersion;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Mutex;
import org.openide.util.MutexException;

/**
 * Servers for generating new NetBeans Modules templates.
 *
 * @author Martin Krauskopf
 */
public class SuiteProjectGenerator {
    
    private static final String PLATFORM_PROPERTIES_PATH =
            "nbproject/platform.properties"; // NOI18N
    public static final String PROJECT_PROPERTIES_PATH = "nbproject/project.properties"; // NOI18N
    public static final String PRIVATE_PROPERTIES_PATH = "nbproject/private/private.properties"; // NOI18N

    /** Use static factory methods instead. */
    private SuiteProjectGenerator() {/* empty constructor*/}
    
    public static void createSuiteProject(final File projectDir, final String platformID, final boolean application) throws IOException {
        try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                public Void run() throws IOException {
                    final FileObject dirFO = FileUtil.createFolder(projectDir);
                    if (ProjectManager.getDefault().findProject(dirFO) != null) {
                        throw new IllegalArgumentException("Already a project in " + dirFO); // NOI18N
                    }
                    createSuiteProjectXML(dirFO);
                    createPlatformProperties(dirFO, platformID);
                    createProjectProperties(dirFO);
                    ModuleList.refresh();
                    ProjectManager.getDefault().clearNonProjectCache();
                    if (application) {
                        initApplication(dirFO, platformID);
                    }
                    return null;
                }
            });
        } catch (MutexException e) {
            throw (IOException) e.getException();
        }
    }
    
    /**
     * Creates basic <em>nbbuild/project.xml</em> or whatever
     * <code>AntProjectHelper.PROJECT_XML_PATH</code> is pointing to for
     * <em>Suite</em>.
     */
    private static void createSuiteProjectXML(FileObject projectDir) throws IOException {
        ProjectXMLManager.generateEmptySuiteTemplate(
                createFileObject(projectDir, AntProjectHelper.PROJECT_XML_PATH),
                projectDir.getNameExt());
    }
    
    private static void createPlatformProperties(FileObject projectDir, String platformID) throws IOException {
        FileObject plafPropsFO = createFileObject(
                projectDir, PLATFORM_PROPERTIES_PATH);
        EditableProperties props = new EditableProperties(true);
        props.setProperty("nbplatform.active", platformID); // NOI18N

        NbPlatform plaf = NbPlatform.getPlatformByID(platformID);
        if (plaf != null && plaf.getHarnessVersion().compareTo(HarnessVersion.V65) > 0) {
            List<String> clusterPath = new ArrayList<String>();
            File[] files = plaf.getDestDir().listFiles();
            for (File file : files) {
                if (ClusterUtils.isValidCluster(file))
                    clusterPath.add(SuiteProperties.toPlatformClusterEntry(file.getName()));
            }
            props.setProperty(SuiteProperties.CLUSTER_PATH_PROPERTY, SuiteUtils.getAntProperty(clusterPath));
        }
        storeProperties(plafPropsFO, props);
    }
    
    private static void createProjectProperties(FileObject projectDir) throws IOException {
        // #60026: ${modules} has to be defined right away.
        FileObject propsFO = createFileObject(projectDir, PROJECT_PROPERTIES_PATH);
        EditableProperties props = new EditableProperties(true);
        props.setProperty("modules", ""); // NOI18N
        storeProperties(propsFO, props);
    }

    private static void initApplication(FileObject dirFO, String platformID) throws IOException  {
        SuiteProject project = (SuiteProject) ProjectManager.getDefault().findProject(dirFO);
        SuiteProperties suiteProps = new SuiteProperties(project, project.getHelper(), project.getEvaluator(), Collections.<NbModuleProject>emptySet());
        BrandingModel branding = suiteProps.getBrandingModel();
        branding.setBrandingEnabled(true);
        branding.setName(branding.getName());
        branding.setTitle(branding.getTitle());
        branding.store();
        NbPlatform plaf = NbPlatform.getPlatformByID(platformID);
        if (plaf != null) {
            ModuleEntry bootstrapModule = plaf.getModule("org.netbeans.bootstrap");
            if (bootstrapModule != null) {
                if (plaf.getHarnessVersion().compareTo(HarnessVersion.V65) <= 0) {
                    // Will be stripped of version suffix if appropriate for the platform.
                    suiteProps.setEnabledClusters(new String[] {bootstrapModule.getClusterDirectory().getName()});
                } else {
                    suiteProps.setClusterPath(Collections.singletonList(
                            ClusterInfo.create(bootstrapModule.getClusterDirectory(), true, true)));
                }
            }
        }
        suiteProps.setDisabledModules(SuiteCustomizerLibraries.DISABLED_PLATFORM_MODULES.toArray(new String[0]));
        suiteProps.storeProperties();
    }
    
    /** Just utility method. */
    private static void storeProperties(FileObject bundleFO, EditableProperties props) throws IOException {
        OutputStream os = bundleFO.getOutputStream();
        try {
            props.store(os);
        } finally {
            os.close();
        }
    }
    
    /**
     * Creates a new <code>FileObject</code>.
     * Throws <code>IllegalArgumentException</code> if such an object already
     * exists. Throws <code>IOException</code> if creation fails.
     */
    private static FileObject createFileObject(FileObject dir, String relToDir) throws IOException {
        FileObject createdFO = dir.getFileObject(relToDir);
        if (createdFO != null) {
            throw new IllegalArgumentException("File " + createdFO + " already exists."); // NOI18N
        }
        createdFO = FileUtil.createData(dir, relToDir);
        return createdFO;
    }
}


