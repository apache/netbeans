/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
