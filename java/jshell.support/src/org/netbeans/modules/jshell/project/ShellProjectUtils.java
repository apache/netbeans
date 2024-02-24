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
package org.netbeans.modules.jshell.project;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.queries.BinaryForSourceQuery;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.ClassIndex.SearchScope;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.ClasspathInfo.PathKind;
import org.netbeans.api.java.source.SourceUtils;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.java.api.common.project.ProjectProperties;
import org.netbeans.modules.java.j2seproject.api.J2SEPropertyEvaluator;
import org.netbeans.modules.jshell.launch.PropertyNames;
import org.netbeans.modules.maven.api.execute.RunUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;

/**
 *
 * @author sdedic
 */
public final class ShellProjectUtils {
    /**
     * Determines if the project wants to launch a JShell. 
     * @param p the project
     * @return true, if JShell support is enabled in the active configuration.
     */
    public static boolean isJShellRunEnabled(Project p) {
        J2SEPropertyEvaluator  prjEval = p.getLookup().lookup(J2SEPropertyEvaluator.class);
        if (prjEval != null) {
            return Boolean.parseBoolean(prjEval.evaluator().evaluate("${" + PropertyNames.JSHELL_ENABLED +"}"));
        }
        return false;
    }
    
    /**
     * Determines a Project given a debugger session. Acquires a baseDir from the
     * debugger and attempts to find a project which owns it. May return {@code null{
     * @param s
     * @return project or {@code null}.
     */
    public static Project getSessionProject(Session s) {
        Map m = s.lookupFirst(null, Map.class);
        if (m == null) {
            return null;
        }
        Object bd = m.get("baseDir"); // NOI18N
        if (bd instanceof File) {
            FileObject fob = FileUtil.toFileObject((File)bd);
            if (fob == null || !fob.isFolder()) {
                return null;
            }
            try {
                Project p = ProjectManager.getDefault().findProject(fob);
                return p;
            } catch (IOException | IllegalArgumentException ex) {
                Exceptions.printStackTrace(ex);
                return null;
            }
        }
        return null;
    }

    public static boolean isNormalRoot(SourceGroup sg) {
        return UnitTestForSourceQuery.findSources(sg.getRootFolder()).length == 0;
    }

    public static JavaPlatform findPlatform(ClassPath bootCP) {
        Set<URL> roots = to2Roots(bootCP);
        for (JavaPlatform platform : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
            Set<URL> platformRoots = to2Roots(platform.getBootstrapLibraries());
            if (platformRoots.containsAll(roots)) {
                return platform;
            }
        }
        return null;
    }

    public static Set<URL> to2Roots(ClassPath bootCP) {
        Set<URL> roots = new HashSet<>();
        for (ClassPath.Entry e : bootCP.entries()) {
            roots.add(e.getURL());
        }
        return roots;
    }
    
    /**
     * Returns set of modules imported by the project. Adds to the passed collection
     * if not null. Module names from `required' clause will be returned
     * 
     * @param project the project
     * @param in optional; the collection
     * @return original collection or a new one with imported modules added
     */
    public static Collection<String> findProjectImportedModules(Project project, Collection<String> in) {
        Collection<String> result = in != null ? in : new HashSet<>();
        if (project == null) {
            return result;
        }
        
        for (SourceGroup sg : org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (isNormalRoot(sg)) {
                ClasspathInfo cpi = ClasspathInfo.create(sg.getRootFolder());
                ClassPath mcp = cpi.getClassPath(PathKind.COMPILE);
                
                for (FileObject r : mcp.getRoots()) {
                    URL u = URLMapper.findURL(r, URLMapper.INTERNAL);
                    String modName = SourceUtils.getModuleName(u);
                    if (modName != null) {
                        result.add(modName);
                    }
                }
            }
        }
        
        return result;
    }
    
    public static Set<String>   findProjectModules(Project project, Set<String> in) {
        Set<String> result = in != null ? in : new HashSet<>();
        if (project == null) {
            return result;
        }
        
        for (SourceGroup sg : org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (isNormalRoot(sg)) {
                FileObject fo = sg.getRootFolder().getFileObject("module-info.java");
                if (fo == null) {
                    continue;
                }
                URL u = URLMapper.findURL(sg.getRootFolder(), URLMapper.INTERNAL);
                BinaryForSourceQuery.Result r = BinaryForSourceQuery.findBinaryRoots(u);
                for (URL u2 : r.getRoots()) {
                    String modName = SourceUtils.getModuleName(u2, true);
                    if (modName != null) {
                        result.add(modName);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Collects project modules and packages from them.
     * For each modules, provides a list of (non-empty) packages from that module.
     * @param project
     * @return 
     */
    public static Map<String, Collection<String>>   findProjectModulesAndPackages(Project project) {
        Map<String, Collection<String>> result = new HashMap<>();
        if (project == null) {
            return result;
        }
        for (SourceGroup sg : org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (isNormalRoot(sg)) {
                URL u = URLMapper.findURL(sg.getRootFolder(), URLMapper.INTERNAL);
                BinaryForSourceQuery.Result r = BinaryForSourceQuery.findBinaryRoots(u);
                for (URL u2 : r.getRoots()) {
                    String modName = SourceUtils.getModuleName(u2, true);
                    if (modName != null) {
                        FileObject root = URLMapper.findFileObject(u);
                        Collection<String> pkgs = getPackages(root); //new HashSet<>();
                        if (!pkgs.isEmpty()) {
                            Collection<String> oldPkgs = result.get(modName);
                            if (oldPkgs != null) {
                                oldPkgs.addAll(pkgs);
                            } else {
                                result.put(modName, pkgs);
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
    
    private static Collection<String> getPackages(FileObject root) {
        ClasspathInfo cpi = ClasspathInfo.create(root);
        // create CPI from just the single source root, to avoid packages from other
        // modules
        ClasspathInfo rootCpi = new ClasspathInfo.Builder(
                cpi.getClassPath(PathKind.BOOT)).
                setClassPath(cpi.getClassPath(PathKind.COMPILE)).
                setModuleSourcePath(cpi.getClassPath(PathKind.MODULE_SOURCE)).
                setModuleCompilePath(cpi.getClassPath(PathKind.MODULE_COMPILE)).
                setSourcePath(
                    ClassPathSupport.createClassPath(root)
                ).build();
        
        Collection<String> pkgs = new HashSet<>(rootCpi.getClassIndex().getPackageNames("", false, 
                Collections.singleton(SearchScope.SOURCE)));
        pkgs.remove(""); // NOI18N
        return pkgs;
    }
    
    public static boolean isModularProject(Project project) {
        if (project == null) { 
            return false;
        }
        JavaPlatform platform = findPlatform(project);
        if (platform == null || !isModularJDK(platform)) {
            return false;
        }
        String s = SourceLevelQuery.getSourceLevel(project.getProjectDirectory());
        if (!(s != null && new SpecificationVersion("9").compareTo(new SpecificationVersion(s)) <= 0)) {
            return false;
        }
        // find module-info.java
        for (SourceGroup sg : ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (isNormalRoot(sg)) {
                if (sg.getRootFolder().getFileObject("module-info.java") != null) { // NOI18N
                    return true;
                }
            }
        }
        return false;
    }
    
    public static boolean isModularJDK(JavaPlatform pl) {
        if (pl != null) {
            Specification plSpec = pl.getSpecification();
            SpecificationVersion jvmversion = plSpec.getVersion();
            if (jvmversion.compareTo(new SpecificationVersion("9")) >= 0) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Returns classpath to execute the application merged from all source roots of a project.
     * 
     * @param project
     * @return 
     */
    public static ClassPath projectRuntimeModulePath(Project project) {
        List<ClassPath> delegates = new ArrayList<>();
        for (SourceGroup sg : org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            ClassPath del = ClassPath.getClassPath(sg.getRootFolder(), JavaClassPathConstants.MODULE_EXECUTE_PATH); 
            if (del != null && !del.entries().isEmpty()) {
                delegates.add(del);
            }
        }
        return ClassPathSupport.createProxyClassPath(delegates.toArray(ClassPath[]::new));
    }
    
    public static ClassPath projecRuntimeClassPath(Project project) {
        if (project == null) {
            return null;
        }
        boolean modular = isModularProject(project);
        List<ClassPath> delegates = new ArrayList<>();
        for (SourceGroup sg : org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (!isNormalRoot(sg)) {
                continue;
            }
            ClassPath del; 
            if (modular) {
                del = ClassPath.getClassPath(sg.getRootFolder(), JavaClassPathConstants.MODULE_EXECUTE_CLASS_PATH);
            } else {
                del = ClassPath.getClassPath(sg.getRootFolder(), ClassPath.EXECUTE); 
            }
            if (del != null && !del.entries().isEmpty()) {
                delegates.add(del);
            }
        }
        return ClassPathSupport.createProxyClassPath(delegates.toArray(ClassPath[]::new));
    }

    @NonNull
    public static List<String[]> compilerPathOptions(Project project) {
        List<String[]> result = new ArrayList<>();
        List<String> exportMods = new ArrayList<>(
                ShellProjectUtils.findProjectImportedModules(project, 
                    ShellProjectUtils.findProjectModules(project, null))
        );
        boolean modular = isModularProject(project);
        if (exportMods.isEmpty() || !modular) {
            return result;
        }
        Collections.sort(exportMods);
        result.add(new String[] { "--add-modules", String.join(",", exportMods) });
        
        // now export everything from the project:
        Map<String, Collection<String>> packages = ShellProjectUtils.findProjectModulesAndPackages(project);
        for (Map.Entry<String, Collection<String>> en : packages.entrySet()) {
            String p = en.getKey();
            Collection<String> vals = en.getValue();

            for (String v : vals) {
                result.add(new String[] { "--add-exports", String.format("%s/%s=ALL-UNNAMED", p, v)});
            }
        }
        return result;
    }

    @NonNull
    public static List<String> launchVMOptions(Project project) {
        List<String> exportMods = new ArrayList<>(
                ShellProjectUtils.findProjectImportedModules(project, 
                    ShellProjectUtils.findProjectModules(project, null))
        );
        
        List<String> addReads = new ArrayList<>();
        boolean modular = isModularProject(project);
        if (exportMods.isEmpty() || !modular) {
            return addReads;
        }
        exportMods.add("jdk.jshell"); // NOI18N
        Collections.sort(exportMods);
        addReads.add("--add-modules"); // NOI18N
        addReads.add(String.join(",", exportMods));
        addReads.add("--add-reads"); // NOI18N
        addReads.add("jdk.jshell=ALL-UNNAMED"); // NOI18N
        
        // now export everything from the project:
        Map<String, Collection<String>> packages = ShellProjectUtils.findProjectModulesAndPackages(project);
        for (Map.Entry<String, Collection<String>> en : packages.entrySet()) {
            String p = en.getKey();
            Collection<String> vals = en.getValue();

            for (String v : vals) {
                addReads.add("--add-exports"); // NOI18N
                addReads.add(String.format("%s/%s=ALL-UNNAMED",  // NOI18N
                        p, v));
            }
        }
        return addReads;
    }
    
    public static FileObject findProjectRoots(Project project, List<URL> urls) {
        if (project == null) {
            return null;
        }
        FileObject ret = null;
        Set<URL> knownURLs = new HashSet<>();
        for (SourceGroup sg : org.netbeans.api.project.ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
            if (org.netbeans.modules.jshell.project.ShellProjectUtils.isNormalRoot(sg)) {
                if (urls != null) {
                    URL u = URLMapper.findURL(sg.getRootFolder(), URLMapper.INTERNAL);
                    BinaryForSourceQuery.Result r = BinaryForSourceQuery.findBinaryRoots(u);
                    for (URL ru : r.getRoots()) {
                        // ignore JARs, prefer output folder:
                        if (FileUtil.isArchiveArtifact(ru)) {
                            continue;
                        }
                        if (knownURLs.add(ru)) {
                            urls.add(ru);
                        }
                    }
                }
                if (ret == null) {
                    ret = sg.getRootFolder();
                }
            }
        }
        return ret;
    }
    
    public static JavaPlatform findPlatform(Project project) {
        FileObject ref = findProjectRoots(project, null);
        if (ref == null) {
            return null;
        }
        JavaPlatform platform = findPlatform(ClassPath.getClassPath(ref, ClassPath.BOOT));
        return platform != null ? platform : JavaPlatform.getDefault();
    }
    
    /**
     * Attempts to detect Compile on Save enabled.
     */
    public static boolean isCompileOnSave(Project p) {
        J2SEPropertyEvaluator  prjEval = p.getLookup().lookup(J2SEPropertyEvaluator.class);
        if (prjEval == null) {
            // try maven approach
            return RunUtils.isCompileOnSaveEnabled(p);
        }
        String compileOnSaveProperty = prjEval.evaluator().getProperty(ProjectProperties.COMPILE_ON_SAVE);
        if (compileOnSaveProperty == null || !Boolean.valueOf(compileOnSaveProperty)) {
            return false;
        }
        Map<String, String> props = prjEval.evaluator().getProperties();
        if (props == null) {
            return false;
        }
        for (Map.Entry<String, String> e : props.entrySet()) {
            if (e.getKey().startsWith(ProjectProperties.COMPILE_ON_SAVE_UNSUPPORTED_PREFIX)) {
                if (e.getValue() != null && Boolean.valueOf(e.getValue())) {
                    return false;
                }
            }
        }                    
        return true;
    }
    
    public static String quoteCmdArg(String s) {
        if (s.indexOf(' ') == -1) {
            return s;
        }
        return '"' + s + '"'; // NOI18N
    }
    
    public static List<String> quoteCmdArgs(List<String> args) {
        List<String> ret = new ArrayList<>();
        for (String a : args) {
            ret.add(quoteCmdArg(a));
        }
        return ret;
    }
}
