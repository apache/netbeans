/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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


