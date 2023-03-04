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

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.modules.java.j2seplatform.api.J2SEPlatformCreator;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class JavaPlatformSupport {
    
    private List<JavaPlatform> justCreatedPlatforms = new ArrayList<JavaPlatform>(); // platforms created during import
    private File defaultNetBeansPlatformFile = null; // NetBeans default platform directory

    private static JavaPlatformSupport inst;
    
    private JavaPlatformSupport() {
        JavaPlatform defPlf = JavaPlatformManager.getDefault().getDefaultPlatform();
        Collection installFolder = defPlf.getInstallFolders();
        if (!installFolder.isEmpty()) {
            defaultNetBeansPlatformFile = FileUtil.toFile((FileObject) installFolder.toArray()[0]);
        }
    }
    
    public static synchronized JavaPlatformSupport getJavaPlatformSupport() {
        if (inst == null) {
            inst = new JavaPlatformSupport();
        }
        return inst;
    }
    
    private List<JavaPlatform> getAllPlatforms() {
        List<JavaPlatform> all = new ArrayList<JavaPlatform>(justCreatedPlatforms);
        all.addAll(Arrays.<JavaPlatform>asList(JavaPlatformManager.getDefault().getInstalledPlatforms()));
        return all;
    }
    
    /** 
     * Returns and if necessary creates JavaPlatform of the given Eclipse project.
     * @return null for default platform
     */
    public JavaPlatform getJavaPlatform(EclipseProject eclProject, List<String> importProblems) {
        String eclPlfDir = eclProject.getJDKDirectory();
        // eclPlfDir can be null in a case when a JDK was set for an eclipse
        // project in Eclipse then the directory with JDK was deleted from
        // filesystem and then a project is imported into NetBeans
        if (eclPlfDir == null) {
            return null;
        }
        File eclPlfFile = FileUtil.normalizeFile(new File(eclPlfDir));
        if (defaultNetBeansPlatformFile != null && eclPlfFile.equals(defaultNetBeansPlatformFile)) { // use default platform
            return null;
        }
        JavaPlatform nbPlf = null;
        for (JavaPlatform current : getAllPlatforms()) {
            Collection<FileObject> instFolders = current.getInstallFolders();
            if (instFolders.isEmpty()) {
                // ignore
                continue;
            }
            File nbPlfDir = FileUtil.toFile(instFolders.iterator().next());
            if (nbPlfDir.equals(eclPlfFile)) {
                nbPlf = current;
                // found
                break;
            }
        }
        if (nbPlf != null) {
            return nbPlf;
        }
        // If we are not able to find any platform let's use the "broken
        // platform" which can be easily added by user with "Resolve Reference
        // Problems" feature. Such behaviour is much better then using a default
        // platform when user imports more projects.
        FileObject fo = FileUtil.toFileObject(eclPlfFile);
        if (fo != null) {
            try {
                JavaPlatform plat = J2SEPlatformCreator.createJ2SEPlatform(fo);
                JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(plat.getDisplayName(), null);
                if (platforms.length > 0) {
                    return platforms[0];
                }
                if (plat.findTool("javac") != null) { //NOI18N
                    justCreatedPlatforms.add(plat);
                    return plat;
                } else {
                    importProblems.add(NbBundle.getMessage(Importer.class, "MSG_JRECannotBeUsed", eclProject.getName())); //NOI18N
                    return null;
                }
            } catch (IOException ex) {
                importProblems.add("Cannot create J2SE platform for '" + eclPlfFile + "'. " + "Default platform will be used instead."); // XXX I18N
                return null;
            }
        } else {
            importProblems.add(NbBundle.getMessage(Importer.class, "MSG_JDKDoesnExistUseDefault", // NOI18N
                    eclProject.getName(), eclPlfFile.getAbsolutePath()));
            return null;
        }
    }
    
}
