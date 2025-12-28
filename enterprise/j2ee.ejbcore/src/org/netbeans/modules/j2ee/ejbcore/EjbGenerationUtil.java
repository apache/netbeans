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
        return pkgs.toArray(new String[0]);
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
            packageName = packageName.replace("/", ".");
        }
        return packageName+"";
    }

}
