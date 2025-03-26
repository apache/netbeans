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

package org.netbeans.modules.websvc.saas.codegen.java.support;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.ClassIndex;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.Utilities;

/**
 *
 * @author Andrei Badea
 */
public class SourceGroupSupport {
    
    // XXX some of the methods are also in org.netbeans.modules.j2ee.persistence.wizard.Util

    private SourceGroupSupport() {
    }

    public static SourceGroup[] getJavaSourceGroups(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        Set<SourceGroup> testGroups = getTestSourceGroups(sourceGroups);
        List<SourceGroup> result = new ArrayList<>();
        for (int i = 0; i < sourceGroups.length; i++) {
            if (!testGroups.contains(sourceGroups[i])) {
                result.add(sourceGroups[i]);
            }
        }
        return result.toArray(new SourceGroup[0]);
    }

    public static boolean isValidPackageName(String packageName) {
        if (packageName.length() > 0 && packageName.charAt(0) == '.') { // NOI18N
            return false;
        }
        StringTokenizer tukac = new StringTokenizer(packageName, "."); // NOI18N
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if ("".equals(token)) // NOI18N
                return false;
            if (!Utilities.isJavaIdentifier(token))
                return false;
        }
        return true;
    }

    // returns true if the folder is writable or is in a writable parent dir 
    // but does not yet exist
    public static boolean isFolderWritable(SourceGroup sourceGroup, String packageName) {
        try {
            FileObject fo = getFolderForPackage(sourceGroup, packageName, false);

            while ((fo == null) && (packageName.lastIndexOf('.') != -1)) {
                packageName = packageName.substring(0, packageName.lastIndexOf('.'));
                fo = getFolderForPackage(sourceGroup, packageName, false);
            }
            return ((fo == null) || fo.canWrite());
        } catch (IOException ex) {
            return false;
        }
    }

    public static SourceGroup findSourceGroupForFile(Project project, FileObject folder) {
        return findSourceGroupForFile(getJavaSourceGroups(project), folder);
    }
    
    public static SourceGroup findSourceGroupForFile(SourceGroup[] sourceGroups, FileObject folder) {
        for (int i = 0; i < sourceGroups.length; i++) {
            if (FileUtil.isParentOf(sourceGroups[i].getRootFolder(), folder)) {
                return sourceGroups[i];
            }
        }
        return null;
    }

    public static String getPackageForFolder(SourceGroup sourceGroup, FileObject folder) {
        String relative = FileUtil.getRelativePath(sourceGroup.getRootFolder(), folder);
        if (relative != null) {
            return relative.replace('/', '.'); // NOI18N
        } else {
            return ""; // NOI18N
        }
    }

    public static String packageForFolder(FileObject folder) {
        Project project = FileOwnerQuery.getOwner(folder);
        SourceGroup[] sources = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        SourceGroup sg = findSourceGroupForFile(sources, folder);
        if (sg != null) {
            return getPackageForFolder(sg, folder);
        } else {
            return "";          //NOI18N
        }
    }

    public static FileObject getFolderForPackage(SourceGroup sourceGroup, String pgkName) throws IOException {
        return getFolderForPackage(sourceGroup, pgkName, false);
    }

    public static FileObject getFolderForPackage(SourceGroup sourceGroup, String pgkName, boolean create) throws IOException {
        if (sourceGroup == null || pgkName == null) {
            return null;
        }
        String relativePkgName = pgkName.replace('.', '/');
        FileObject folder = sourceGroup.getRootFolder().getFileObject(relativePkgName);
        if (folder != null) {
            return folder;
        } else if (create) {
            return FileUtil.createFolder(sourceGroup.getRootFolder(), relativePkgName);
        }
        return null;
    }

    private static Map<FileObject, SourceGroup> createFoldersToSourceGroupsMap(final SourceGroup[] sourceGroups) {
        Map<FileObject, SourceGroup> result;
        if (sourceGroups.length == 0) {
            result = Collections.emptyMap();
        } else {
            result = new HashMap<>(2 * sourceGroups.length, .5f);
            for (int i = 0; i < sourceGroups.length; i++) {
                SourceGroup sourceGroup = sourceGroups[i];
                result.put(sourceGroup.getRootFolder(), sourceGroup);
            }
        }
        return result;
    }

    private static Set<SourceGroup> getTestSourceGroups(SourceGroup[] sourceGroups) {
        Map<FileObject, SourceGroup> foldersToSourceGroupsMap = createFoldersToSourceGroupsMap(sourceGroups);
        Set<SourceGroup> testGroups = new HashSet<>();
        for (int i = 0; i < sourceGroups.length; i++) {
            testGroups.addAll(getTestTargets(sourceGroups[i], foldersToSourceGroupsMap));
        }
        return testGroups;
    }

    private static List<SourceGroup> getTestTargets(SourceGroup sourceGroup, Map<FileObject, SourceGroup> foldersToSourceGroupsMap) {
        final URL[] rootURLs = UnitTestForSourceQuery.findUnitTests(sourceGroup.getRootFolder());
        if (rootURLs.length == 0) {
            return new ArrayList<>();
        }
        List<SourceGroup> result = new ArrayList<>();
        List<FileObject> sourceRoots = getFileObjects(rootURLs, true);
        for (int i = 0; i < sourceRoots.size(); i++) {
            FileObject sourceRoot = sourceRoots.get(i);
            SourceGroup srcGroup = foldersToSourceGroupsMap.get(sourceRoot);
            if (srcGroup != null) {
                result.add(srcGroup);
            }
        }
        return result;
    }

    private static List<FileObject> getFileObjects(URL[] urls, boolean quiet) {
        List<FileObject> result = new ArrayList<>();
        for (int i = 0; i < urls.length; i++) {
            FileObject sourceRoot = URLMapper.findFileObject(urls[i]);
            if (sourceRoot != null) {
                result.add(sourceRoot);
            } else if (! quiet) {
                ErrorManager.getDefault().notify(
                        ErrorManager.INFORMATIONAL,
                        new IllegalStateException("No FileObject found for the following URL: " + urls[i])); //NOI18N
            }
        }
        return result;
    }
    
    public static String getPackageName(String qualifiedClassName) {
        int i = qualifiedClassName.lastIndexOf('.');
        return i > 0 ? qualifiedClassName.substring(0, i) : null;
    }
    
    public static String getClassName(String qualifiedClassName) {
        return qualifiedClassName.substring(qualifiedClassName.lastIndexOf('.')+1);
    }
    
    public static FileObject findSourceRoot(Project project) {
        SourceGroup[] sourceGroups = ProjectUtils.getSources(project).getSourceGroups(
                JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (sourceGroups != null && sourceGroups.length > 0) {
            return sourceGroups[0].getRootFolder();
        }
        return null;
    }
    
    public static JavaSource getJavaSourceFromClassName(String qualifiedClassName, Project project) throws IOException {
        FileObject fo = getFileObjectFromClassName(qualifiedClassName, project);
        if (fo != null) {
            return JavaSource.forFileObject(fo);
        } else {
            return null;
        }
    }
    
    public static FileObject getFileObjectFromClassName(String qualifiedClassName, Project project) throws IOException {
        FileObject root = findSourceRoot(project);
        ClasspathInfo cpInfo = ClasspathInfo.create(root);
        ClassIndex ci = cpInfo.getClassIndex();
        int beginIndex = qualifiedClassName.lastIndexOf('.')+1;
        String simple = qualifiedClassName.substring(beginIndex);
        Set<ElementHandle<TypeElement>> handles = ci.getDeclaredTypes(
                simple, ClassIndex.NameKind.SIMPLE_NAME, 
                Collections.singleton(ClassIndex.SearchScope.SOURCE));
        for (ElementHandle<TypeElement> handle : handles) {
            if (qualifiedClassName.equals(handle.getQualifiedName())) {
                return SourceUtils.getFile(handle, cpInfo);
            }
        }
        return null;
    }
    
    public static FileObject findJavaSourceFile(Project project, String name) {
        for (SourceGroup group : getJavaSourceGroups(project)) {
            Enumeration<? extends FileObject> files = group.getRootFolder().getChildren(true);
            while (files.hasMoreElements()) {
                FileObject fo = files.nextElement();
                if ("java".equals(fo.getExt())) { //NOI18N
                    if (name.equals(fo.getName())) {
                        return fo;
                    }
                }
            }
        }
        return null;
    }
    
    
    public static List<ClassPath> gerClassPath(Project project) {
        List<ClassPath> paths = new ArrayList<ClassPath>();
        List<SourceGroup> groups = new ArrayList<SourceGroup>();
        groups.addAll(Arrays.asList(ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)));
        ClassPathProvider cpp = project.getLookup().lookup(ClassPathProvider.class);
        for (SourceGroup group : groups) {
            ClassPath cp = cpp.findClassPath(group.getRootFolder(), ClassPath.COMPILE);
            if (cp != null) {
                paths.add(cp);
            }
            cp = cpp.findClassPath(group.getRootFolder(), ClassPath.SOURCE);
            if (cp != null) {
                paths.add(cp);
            }
        }
        return paths;
    }
}
