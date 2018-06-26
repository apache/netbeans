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

package org.netbeans.modules.j2ee.ejbcore;

import java.io.File;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.j2ee.dd.api.ejb.Ejb;
import org.netbeans.modules.j2ee.dd.api.ejb.EjbJar;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * This class contains common functionality for code generation
 * @author Chris Webster
 * @author Martin Adamek
 */
public class EjbGenerationUtil {

    private static final String[] EJB_NAME_CONTEXTS = new String[] {
                EnterpriseBeans.SESSION,
                EnterpriseBeans.ENTITY,
                EnterpriseBeans.MESSAGE_DRIVEN
    };
    
    public static String getFullClassName(String pkg, String className) {
        return (pkg==null||pkg.length()==0)?className:pkg+"."+className; //NOI18N
    }
    
    public static String getBaseName(String fullClassName) {
        return fullClassName.substring(fullClassName.lastIndexOf('.')+1); //NOI18N
    }
    
    public static String[] getPackages(Project project) {
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set<String> pkgs = new TreeSet<String>();
        for (int i = 0; i < groups.length; i++) {
            findPackages(groups[i].getRootFolder(),"", pkgs);
        }
        return pkgs.toArray(new String[pkgs.size()]);
    }
    
    private static void findPackages (FileObject root, String curPkg, Set<String> pkgs) {
        for (FileObject kid : root.getChildren()) {
	        String name = curPkg + (curPkg.length() != 0 ? "." : "") + kid.getName();
            pkgs.add (name);
	        findPackages (kid, name, pkgs);
        }
    }
    
    public static boolean isEjbNameInDD(String ejbName, EjbJar ejbJar) {
        EnterpriseBeans beans = ejbJar.getEnterpriseBeans();
        Object ejb = null;
        if (beans != null) {
            for (int i = 0; i < EJB_NAME_CONTEXTS.length; i++) {
                ejb = beans.findBeanByName(EJB_NAME_CONTEXTS[i], Ejb.EJB_NAME, ejbName);
                if (ejb != null) {
                    break;
                }
            }
        }
        return beans != null && ejb != null;
    }
    
    public static FileObject getPackageFileObject(SourceGroup location, String pkgName, Project project) {
        String relativePkgName = pkgName.replace('.', '/');
        FileObject fileObject = null;
        fileObject = location.getRootFolder().getFileObject(relativePkgName);
        if (fileObject != null) {
            return fileObject;
        } else {
            File rootFile = FileUtil.toFile(location.getRootFolder());
            File pkg = new File(rootFile,relativePkgName);
            pkg.mkdirs();
            fileObject = location.getRootFolder().getFileObject(relativePkgName);
        }
        return fileObject;
    }

    public static String getSelectedPackageName(FileObject targetFolder) {
        Project project = FileOwnerQuery.getOwner(targetFolder);
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        String packageName = null;
        for (int i = 0; i < groups.length && packageName == null; i++) {
            packageName = FileUtil.getRelativePath(groups [i].getRootFolder(), targetFolder);
        }
        if (packageName != null) {
            packageName = packageName.replaceAll("/", ".");
        }
        return packageName+"";
    }

}
